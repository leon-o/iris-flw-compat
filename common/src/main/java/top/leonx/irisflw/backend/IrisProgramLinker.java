package top.leonx.irisflw.backend;

import dev.engine_room.flywheel.api.material.LightShader;
import dev.engine_room.flywheel.backend.compile.ContextShader;
import dev.engine_room.flywheel.backend.compile.core.LinkResult;
import dev.engine_room.flywheel.backend.compile.core.ProgramLinker;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import dev.engine_room.flywheel.backend.gl.shader.GlShader;
import dev.engine_room.flywheel.backend.gl.shader.ShaderType;
import dev.engine_room.flywheel.lib.material.LightShaders;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.blending.AlphaTestFunction;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.shader.StandardMacros;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.helpers.StringPair;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.irisshaders.iris.shaderpack.preprocessor.JcppProcessor;
import net.irisshaders.iris.shaderpack.programs.ProgramFallbackResolver;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.properties.ShaderProperties;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.leonx.irisflw.IrisFlw;
import top.leonx.irisflw.accessors.IrisRenderingPipelineAccessor;
import top.leonx.irisflw.accessors.ProgramDirectivesAccessor;
import top.leonx.irisflw.accessors.ProgramSourceAccessor;
import top.leonx.irisflw.flywheel.IrisFlwCompatGlProgram;
import top.leonx.irisflw.flywheel.IrisFlwCompatGlProgramBase;
import top.leonx.irisflw.flywheel.RenderLayerEventStateManager;
import top.leonx.irisflw.mixin.flw.FlwCompilationAccessor;
import top.leonx.irisflw.transformer.GlslTransformerFragPatcher;
import top.leonx.irisflw.transformer.GlslTransformerVertPatcher;

import java.util.*;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;

public class IrisProgramLinker extends ProgramLinker {
    private final Map<ProgramSet,ProgramFallbackResolver> resolvers = new HashMap<>();
    private final Iterable<StringPair> environmentDefines;
    private final GlslTransformerVertPatcher vertPatcher;

    public static final boolean PATCH_FRAG = false;

    private GlslTransformerFragPatcher fragPatcher;

    public ContextShader contextShader = ContextShader.DEFAULT;
    public LightShader lightShader = LightShaders.SMOOTH_WHEN_EMBEDDED;

    public IrisProgramLinker() {
        environmentDefines = StandardMacros.createStandardEnvironmentDefines();
        vertPatcher = new GlslTransformerVertPatcher();
        if(PATCH_FRAG)
            fragPatcher = new GlslTransformerFragPatcher();
    }

    public GlProgram link(List<GlShader> shaders, Consumer<GlProgram> preLink) {
        // this probably doesn't need caching
        LinkResult linkResult = linkInternal(shaders, preLink);
        if(linkResult == null || linkResult instanceof LinkResult.Failure)
            return IrisFlwCompatGlProgramBase.Invalid.INSTANCE;
        return linkResult.unwrap();
    }

    private LinkResult linkInternal(List<GlShader> shaders, Consumer<GlProgram> preLink) {
        int handle = glCreateProgram();
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

        String vertexSource = null;
        String vertexShaderName = null;
        String fragSource = null;
        String fragShaderName = null;
        for (GlShader shader : shaders) {
            if (shader instanceof IntermediateGlShader intermediateGlShader)
                if (intermediateGlShader.type == ShaderType.VERTEX) {
                    vertexSource = intermediateGlShader.getSource();
                    vertexShaderName = intermediateGlShader.getName();
                }else if (intermediateGlShader.type == ShaderType.FRAGMENT)
                {
                    fragSource = intermediateGlShader.getSource();
                    fragShaderName = intermediateGlShader.getName();
                }
        }

        if (pipeline instanceof IrisRenderingPipeline newPipeline) {
            if (vertexSource != null && vertexShaderName != null && fragSource != null && fragShaderName != null) {
                return getIrisShaderLinkResult((IrisRenderingPipelineAccessor) newPipeline, vertexShaderName, vertexSource, fragShaderName, fragSource);
            }
        }

        var out = new GlProgram(handle);

        for (GlShader shader : shaders) {
            glAttachShader(handle, shader.handle());
        }

        preLink.accept(out);

        glLinkProgram(handle);
        String log = glGetProgramInfoLog(handle);

        if (linkSuccessful(handle)) {
            return LinkResult.success(out, log);
        } else {
            out.delete();
            return LinkResult.failure(log);
        }
    }

    private @Nullable LinkResult getIrisShaderLinkResult(IrisRenderingPipelineAccessor newPipeline, String vertexName, String vertexSource, String fragName, String newFragSource) {
        ProgramSet programSet = newPipeline.getProgramSet();
        var isShadow = RenderLayerEventStateManager.isRenderingShadow();
        var isEmbedded = contextShader == ContextShader.EMBEDDED;
        Optional<ProgramSource> sourceReferenceOpt = getProgramSourceReference(programSet, vertexName, isShadow, isEmbedded);
        if (sourceReferenceOpt.isEmpty())
            return null;

        ProgramSource sourceRef = sourceReferenceOpt.get();
        var vertexRef = sourceRef.getVertexSource().orElseThrow();
        var fragRef = sourceRef.getFragmentSource().orElseThrow();

        String newVertexSource = vertPatcher.patch(vertexRef, vertexSource, isShadow, isEmbedded, lightShader, IrisFlw.isUsingExtendedVertexFormat());
        newVertexSource = JcppProcessor.glslPreprocessSource(newVertexSource, environmentDefines);

        if(PATCH_FRAG)
            newFragSource = fragPatcher.patch(fragRef, newFragSource);
        else
            newFragSource = fragRef;

        var shaderName = vertexName+"_"+fragName;

        ProgramSource newProgramSource = programSourceOverrideVertexSource(shaderName, programSet, sourceRef, newVertexSource, newFragSource);
        ((ProgramDirectivesAccessor) newProgramSource.getDirectives()).setFlwAlphaTestOverride(
                new AlphaTest(AlphaTestFunction.GREATER, 0.5f));
        return createWorldProgramBySource(shaderName, isShadow, newPipeline, newProgramSource);
    }

    private static boolean linkSuccessful(int handle) {
        return glGetProgrami(handle, GL_LINK_STATUS) == GL_TRUE;
    }
    protected LinkResult createWorldProgramBySource(String name, boolean isShadow, IrisRenderingPipelineAccessor pipeline, ProgramSource processedSource) {
        ShaderInstance override = null;
        try {
            if (isShadow) {
                override = pipeline.callCreateShadowShader(
                        getFlwShaderName(name, true), processedSource, ProgramId.Block, AlphaTest.ALWAYS,
                        IrisVertexFormats.TERRAIN, false, false, false, false);
            } else {
                override = pipeline.callCreateShader(
                        getFlwShaderName(name, false), processedSource, ProgramId.Block, AlphaTest.ALWAYS,
                        IrisVertexFormats.TERRAIN, FogMode.OFF, false, false, false, false, false);
            }

        } catch (Exception exception) {
            IrisFlw.LOGGER.error("Fail to compile shader", exception);
            return LinkResult.failure(exception.toString());
        }

        if (override != null) {
            return LinkResult.success(new IrisFlwCompatGlProgram(override, ShaderType.VERTEX, name), "");
        }
        return null;
    }
    static int programCounter;
    private String getFlwShaderName(String shaderName, boolean isShadow) {
        String randomId = String.valueOf(programCounter);
        programCounter++;
        if (isShadow)
            return String.format("shadow_flw_%s_%s", shaderName, randomId);
        else
            return String.format("gbuffers_flw_%s_%s", shaderName, randomId);
    }

    @NotNull
    protected ProgramSource programSourceOverrideVertexSource(String shaderName, ProgramSet programSet, ProgramSource source, String vertexSource, String fragSource) {
        ShaderProperties properties = ((ProgramSourceAccessor) source).getShaderProperties();
        BlendModeOverride blendModeOverride = ((ProgramSourceAccessor) source).getBlendModeOverride();

        FlwCompilationAccessor.invokeDumpSource(vertexSource, source.getName() + "_" + shaderName + '.' + ShaderType.VERTEX.extension);
        FlwCompilationAccessor.invokeDumpSource(fragSource, source.getName() + "_" + shaderName + '.' + ShaderType.FRAGMENT.extension);

        //Get a copy of program
        return new ProgramSource(source.getName() + "_" + shaderName, vertexSource,
                source.getGeometrySource().orElse(null),
                source.getTessControlSource().orElse(null),
                source.getTessEvalSource().orElse(null),
                fragSource, programSet, properties, blendModeOverride);
    }

    protected Optional<ProgramSource> getProgramSourceReference(ProgramSet programSet, String flwShaderName, boolean isShadow, boolean isEmbedded){

        // Tessellation is currently not supported
        var resolver = resolvers.computeIfAbsent(programSet, ProgramFallbackResolver::new);

        if(isShadow){
            var shadow = resolver.resolve(ProgramId.Shadow).orElse(null);
            if(shadow==null)
                return Optional.empty();
            ShaderProperties properties = ((ProgramSourceAccessor) shadow).getShaderProperties();
            BlendModeOverride blendModeOverride = ((ProgramSourceAccessor) shadow).getBlendModeOverride();
            return Optional.of(new ProgramSource("shadow_flw",
                    shadow.getVertexSource().orElseThrow(),
                    shadow.getGeometrySource().orElse(null),
                    null,
                    null,
                    shadow.getFragmentSource().orElseThrow(),
                    programSet, properties, blendModeOverride));
        }else{
            var refProgramId = ProgramId.Block;
            if(isEmbedded){
                // Temporarily hardcoded, maybe configurable in the future
                refProgramId = ProgramId.Terrain;
            }
            var refProgram = resolver.resolve(refProgramId).orElse(null);
            if(refProgram==null)
                return Optional.empty();

            ShaderProperties properties = ((ProgramSourceAccessor) refProgram).getShaderProperties();
            BlendModeOverride blendModeOverride = ((ProgramSourceAccessor) refProgram).getBlendModeOverride();

            return Optional.of(new ProgramSource("gbuffer_flw",
                    refProgram.getVertexSource().orElseThrow(),
                    refProgram.getGeometrySource().orElse(null),
                    null,
                    null,
                    refProgram.getFragmentSource().orElseThrow(),
                    programSet, properties, blendModeOverride));
        }
    }
}
