package top.leonx.irisflw.backends;

import dev.engine_room.flywheel.backend.compile.core.LinkResult;
import dev.engine_room.flywheel.backend.compile.core.ProgramLinker;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import dev.engine_room.flywheel.backend.gl.shader.GlShader;
import dev.engine_room.flywheel.backend.gl.shader.ShaderType;
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
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.NotNull;
import top.leonx.irisflw.IrisFlw;
import top.leonx.irisflw.accessors.IrisRenderingPipelineAccessor;
import top.leonx.irisflw.accessors.ProgramDirectivesAccessor;
import top.leonx.irisflw.accessors.ProgramSourceAccessor;
import top.leonx.irisflw.flywheel.IrisFlwCompatShaderWarp;
import top.leonx.irisflw.flywheel.RenderLayerEventStateManager;
import top.leonx.irisflw.transformer.GlslTransformerShaderPatcher;

import java.util.*;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;

public class IrisProgramLinker extends ProgramLinker {
    private final Map<ProgramSet,ProgramFallbackResolver> resolvers = new HashMap<>();
    private final Iterable<StringPair> environmentDefines;
    private final GlslTransformerShaderPatcher patcher;
    public IrisProgramLinker() {
        environmentDefines = StandardMacros.createStandardEnvironmentDefines();
        patcher = new GlslTransformerShaderPatcher();
    }

    public GlProgram link(List<GlShader> shaders, Consumer<GlProgram> preLink) {
        // this probably doesn't need caching
        return linkInternal(shaders, preLink).unwrap();
    }

    private LinkResult linkInternal(List<GlShader> shaders, Consumer<GlProgram> preLink) {
        int handle = glCreateProgram();
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

        String vertexSource = null;
        String vertexShaderName = null;
        for (GlShader shader : shaders) {
            if(shader instanceof IntermediateGlShader intermediateGlShader && intermediateGlShader.type == ShaderType.VERTEX){
                vertexSource = intermediateGlShader.getSource();
                vertexShaderName = intermediateGlShader.getName();
                break;
            }
        }

        if (pipeline instanceof IrisRenderingPipeline newPipeline && vertexSource != null && vertexShaderName != null) {
            ProgramSet programSet = ((IrisRenderingPipelineAccessor) newPipeline).getProgramSet();
            var isShadow = RenderLayerEventStateManager.isRenderingShadow();
            Optional<ProgramSource> sourceReferenceOpt = getProgramSourceReference(programSet, vertexShaderName, isShadow);
            if(sourceReferenceOpt.isEmpty())
                return null;

            ProgramSource sourceRef = sourceReferenceOpt.get();
            if(sourceRef.getVertexSource().isEmpty())
                return null;

            String irisShaderSource = sourceRef.getVertexSource().get();
            String newVertexSource = patcher.patch(irisShaderSource, vertexSource);
            newVertexSource = JcppProcessor.glslPreprocessSource(newVertexSource, environmentDefines);
            ProgramSource newProgramSource = programSourceOverrideVertexSource(vertexShaderName, programSet, sourceRef, newVertexSource);
            ((ProgramDirectivesAccessor) newProgramSource.getDirectives()).setFlwAlphaTestOverride(
                    new AlphaTest(AlphaTestFunction.GREATER, 0.5f));
            return createWorldProgramBySource(vertexShaderName, isShadow, (IrisRenderingPipelineAccessor) newPipeline, newProgramSource);
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
            return LinkResult.success(new IrisFlwCompatShaderWarp(override, ShaderType.VERTEX, name), "");
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
    protected ProgramSource programSourceOverrideVertexSource(String shaderName, ProgramSet programSet, ProgramSource source, String vertexSource) {
        ShaderProperties properties = ((ProgramSourceAccessor) source).getShaderProperties();
        BlendModeOverride blendModeOverride = ((ProgramSourceAccessor) source).getBlendModeOverride();
        //Get a copy of program
        return new ProgramSource(source.getName() + "_" + shaderName, vertexSource,
                source.getGeometrySource().orElse(null),
                source.getTessControlSource().orElse(null),
                source.getTessEvalSource().orElse(null),
                source.getFragmentSource().orElse(null), programSet, properties, blendModeOverride);
    }

    protected Optional<ProgramSource> getProgramSourceReference(ProgramSet programSet, String flwShaderName, boolean isShadow){

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
            if(flwShaderName.contains("passthru")){
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
