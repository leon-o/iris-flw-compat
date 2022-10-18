package top.leonx.irisflw.compiler;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.compile.Template;
import com.jozufozu.flywheel.core.compile.VertexData;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.source.FileResolution;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import top.leonx.irisflw.accessors.ProgramSetAccessor;
import top.leonx.irisflw.transformer.ShaderPatcherBase;
import top.leonx.irisflw.transformer.TemplateShaderPatcher;

import java.util.Optional;

@SuppressWarnings("unused")
public class TemplateProgramCompiler<P extends WorldProgram> extends NewProgramCompiler<P>{
    TemplateShaderPatcher patcher;

    @Override
    public ShaderPatcherBase getShaderPatcher() {
        return patcher;
    }

    public TemplateProgramCompiler(GlProgram.Factory<P> factory, Template<? extends VertexData> template, FileResolution header) {
        super(factory, template, header);
        patcher = new TemplateShaderPatcher(template,header);
    }

    @Override
    protected Optional<ProgramSource> getProgramSourceReference(ProgramSet programSet, boolean isShadow) {
        if (isShadow) {
            return ((ProgramSetAccessor) programSet).getShadowFlw();
        } else {
            return ((ProgramSetAccessor) programSet).getGbuffersFlw();
        }
    }
}
