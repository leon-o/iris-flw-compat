package top.leonx.irisflw.compiler;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.compile.ProgramContext;
import com.jozufozu.flywheel.core.compile.Template;
import com.jozufozu.flywheel.core.compile.VertexData;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.source.FileResolution;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.AlphaTestFunction;
import net.coderbot.iris.gl.shader.StandardMacros;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.coderbot.iris.shaderpack.*;
import net.coderbot.iris.shaderpack.loading.ProgramId;
import net.coderbot.iris.shaderpack.preprocessor.JcppProcessor;
import top.leonx.irisflw.accessors.IrisRenderingPipelineAccessor;
import top.leonx.irisflw.accessors.ProgramDirectivesAccessor;
import top.leonx.irisflw.accessors.ProgramSourceAccessor;
import top.leonx.irisflw.transformer.ShaderPatcherBase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class NewProgramCompiler <TP extends ShaderPatcherBase,P extends WorldProgram> extends IrisProgramCompilerBase<P>{
    private final Map<ProgramSet,ProgramFallbackResolver> resolvers = new HashMap<>();
    private final Iterable<StringPair> environmentDefines;
    public NewProgramCompiler(GlProgram.Factory<P> factory, Template<? extends VertexData> template, FileResolution header,Class<TP> patcherClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(factory, template, header);
        //environmentDefines = StandardMacros.createStandardEnvironmentDefines();
        Method method = StandardMacros.class.getMethod("createStandardEnvironmentDefines");
        environmentDefines =(Iterable<StringPair>) method.invoke(null);
        patcher = patcherClass.getDeclaredConstructor(Template.class, FileResolution.class).newInstance(template,header);
    }

    private final TP patcher;

    @Override
    P createIrisShaderProgram(ProgramContext ctx, boolean isShadow) {
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
        if (pipeline instanceof NewWorldRenderingPipeline newPipeline) {
            ProgramSet programSet = ((IrisRenderingPipelineAccessor) newPipeline).getProgramSet();
            Optional<ProgramSource> sourceReferenceOpt = getProgramSourceReference(programSet, ctx.spec.name, isShadow);
            if(sourceReferenceOpt.isEmpty())
                return null;

            ProgramSource sourceRef = sourceReferenceOpt.get();
            if(sourceRef.getVertexSource().isEmpty())
                return null;

            String vertexSource = sourceRef.getVertexSource().get();
            String newVertexSource = patcher.patch(vertexSource,new ShaderPatcherBase.Context(ctx.spec.getVertexFile(),
                ctx.ctx, ctx.vertexType));
            newVertexSource = JcppProcessor.glslPreprocessSource(newVertexSource, environmentDefines);
            ProgramSource newProgramSource = programSourceOverrideVertexSource(ctx, programSet, sourceRef, newVertexSource);
            ((ProgramDirectivesAccessor) newProgramSource.getDirectives()).setFlwAlphaTestOverride(
                    new AlphaTest(AlphaTestFunction.GREATER, ctx.alphaDiscard));
            return createWorldProgramBySource(ctx, isShadow, (IrisRenderingPipelineAccessor) newPipeline, newProgramSource);
        }
        return null;
    }

    protected Optional<ProgramSource> getProgramSourceReference(ProgramSet programSet, ResourceLocation flwShaderName, boolean isShadow){

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
            if(Objects.equals(flwShaderName.getNamespace(), "flywheel")
                && Objects.equals(flwShaderName.getPath(), "passthru")){
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

    @Override
    public void clear() {
        super.clear();
        resolvers.clear();
    }
}
