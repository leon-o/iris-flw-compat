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
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import top.leonx.irisflw.accessors.NewWorldRenderingPipelineAccessor;
import top.leonx.irisflw.accessors.ProgramDirectivesAccessor;
import top.leonx.irisflw.transformer.AutoInsertPreprocessor;
import top.leonx.irisflw.transformer.PreprocessorBase;

import java.util.Optional;

public class AutoInsertProgramCompiler<P extends WorldProgram> extends IrisProgramCompilerBase<P>{

    AutoInsertPreprocessor preprocessor;
    public AutoInsertProgramCompiler(GlProgram.Factory<P> factory, Template<? extends VertexData> template, FileResolution header) {
        super(factory, template, header);
        preprocessor= new AutoInsertPreprocessor(template,header);
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

            String vertexSource = sourceRef.getVertexSource().get();
            String newVertexSource = preprocessor.preprocess(vertexSource,new PreprocessorBase.Context(ctx.spec.getVertexFile(),
                                                                              ctx.ctx, ctx.vertexType));
            ProgramSource newProgramSource = programSourceOverrideVertexSource(ctx, programSet, sourceRef, newVertexSource);
            ((ProgramDirectivesAccessor) newProgramSource.getDirectives()).setFlwAlphaTestOverride(
                    new AlphaTest(AlphaTestFunction.GREATER, ctx.alphaDiscard));
            return createWorldProgramBySource(ctx, isShadow, (NewWorldRenderingPipelineAccessor) newPipeline, newProgramSource);
        }
        return null;
    }

    private Optional<ProgramSource> getProgramSourceReference(ProgramSet programSet, boolean isShadow){
        if(isShadow){
            return programSet.getShadow();
        }else{
            if (programSet.getGbuffersBlock().isPresent()){
                return programSet.getGbuffersBlock();
            }else if(programSet.getGbuffersTerrain().isPresent()){
                return programSet.getGbuffersTerrain();
            }else if(programSet.getGbuffersTexturedLit().isPresent()){
                return programSet.getGbuffersTexturedLit();
            }else if(programSet.getGbuffersTextured().isPresent()){
                return programSet.getGbuffersTextured();
            }else if(programSet.getGbuffersBasic().isPresent()){
                return programSet.getGbuffersBasic();
            }
        }

        return Optional.empty();
    }
}
