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
import top.leonx.irisflw.accessors.*;
import top.leonx.irisflw.transformer.PreprocessorBase;
import top.leonx.irisflw.transformer.TemplatePreprocessor;

import java.util.Optional;

public class TemplateProgramCompiler<P extends WorldProgram> extends IrisProgramCompilerBase<P>{
    TemplatePreprocessor preprocessor;
    public TemplateProgramCompiler(GlProgram.Factory<P> factory, Template<? extends VertexData> template, FileResolution header) {
        super(factory, template, header);
        preprocessor = new TemplatePreprocessor(template,header);
    }

    P createIrisShaderProgram(ProgramContext ctx, boolean isShadow) {
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
        if (pipeline instanceof NewWorldRenderingPipeline newPipeline) {
            ProgramSet programSet = ((NewWorldRenderingPipelineAccessor) newPipeline).getProgramSet();
            Optional<ProgramSource> sourceOptional;

            if (isShadow) {
                sourceOptional = ((ProgramSetAccessor) programSet).getShadowFlw();
            } else {
                sourceOptional = ((ProgramSetAccessor) programSet).getGbuffersFlw();
            }

            if (sourceOptional.isPresent()) {
                ProgramSource source = sourceOptional.get();
                if (source.getVertexSource().isPresent()) {
                    String vertexSource = source.getVertexSource().get();
                    vertexSource = preprocessor.preprocess(vertexSource,
                                                           new PreprocessorBase.Context(ctx.spec.getVertexFile(),
                                                                                        ctx.ctx, ctx.vertexType));

                    ProgramSource processedSource = programSourceOverrideVertexSource(ctx, programSet, source, vertexSource);
                    ((ProgramDirectivesAccessor) processedSource.getDirectives()).setFlwAlphaTestOverride(
                            new AlphaTest(AlphaTestFunction.GREATER, ctx.alphaDiscard));

                    return createWorldProgramBySource(ctx, isShadow, (NewWorldRenderingPipelineAccessor) pipeline, processedSource);
                }

                //programCache.put(ctx,new IrisShaderProgram(ctx.spec.name, override));
                //return new IrisShaderProgram(ctx.spec.name, override);
            }
        }
        return null;
    }

}
