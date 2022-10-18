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
import top.leonx.irisflw.accessors.NewWorldRenderingPipelineAccessor;
import top.leonx.irisflw.accessors.ProgramDirectivesAccessor;
import top.leonx.irisflw.transformer.ShaderPatcherBase;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class NewProgramCompiler <P extends WorldProgram> extends IrisProgramCompilerBase<P>{

    public abstract ShaderPatcherBase getShaderPatcher();
    private final Map<ProgramSet,ProgramFallbackResolver> resolvers = new HashMap<>();
    private final Iterable<StringPair> environmentDefines;
    public NewProgramCompiler(GlProgram.Factory<P> factory, Template<? extends VertexData> template, FileResolution header) {
        super(factory, template, header);
        environmentDefines = StandardMacros.createStandardEnvironmentDefines();
    }

    @Override
    P createIrisShaderProgram(ProgramContext ctx, boolean isShadow) {
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
        if (pipeline instanceof NewWorldRenderingPipeline newPipeline) {
            ProgramSet programSet = ((NewWorldRenderingPipelineAccessor) newPipeline).getProgramSet();
            Optional<ProgramSource> sourceReferenceOpt = getProgramSourceReference(programSet, isShadow);
            if(sourceReferenceOpt.isEmpty())
                return null;

            ProgramSource sourceRef = sourceReferenceOpt.get();
            if(sourceRef.getVertexSource().isEmpty())
                return null;

            var patcher = getShaderPatcher();

            String vertexSource = sourceRef.getVertexSource().get();
            String newVertexSource = patcher.patch(vertexSource,new ShaderPatcherBase.Context(ctx.spec.getVertexFile(),
                ctx.ctx, ctx.vertexType));
            newVertexSource = JcppProcessor.glslPreprocessSource(newVertexSource, environmentDefines);
            ProgramSource newProgramSource = programSourceOverrideVertexSource(ctx, programSet, sourceRef, newVertexSource);
            ((ProgramDirectivesAccessor) newProgramSource.getDirectives()).setFlwAlphaTestOverride(
                    new AlphaTest(AlphaTestFunction.GREATER, ctx.alphaDiscard));
            return createWorldProgramBySource(ctx, isShadow, (NewWorldRenderingPipelineAccessor) newPipeline, newProgramSource);
        }
        return null;
    }

    protected Optional<ProgramSource> getProgramSourceReference(ProgramSet programSet, boolean isShadow){

        var resolver = resolvers.computeIfAbsent(programSet, ProgramFallbackResolver::new);

        if(isShadow){
            return resolver.resolve(ProgramId.Shadow);
        }else{
            return resolver.resolve(ProgramId.Block);
        }
    }

    @Override
    public void clear() {
        super.clear();
        resolvers.clear();
    }
}
