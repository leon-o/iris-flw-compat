package top.leonx.irisflw.compiler;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.compile.Template;
import com.jozufozu.flywheel.core.compile.VertexData;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.source.FileResolution;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import top.leonx.irisflw.accessors.ProgramSetAccessor;
import top.leonx.irisflw.transformer.TemplateShaderPatcher;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

@SuppressWarnings("unused")
public class TemplateProgramCompiler<P extends WorldProgram> extends NewProgramCompiler<TemplateShaderPatcher,P>{

    public TemplateProgramCompiler(GlProgram.Factory<P> factory, Template<? extends VertexData> template, FileResolution header) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(factory, template, header, TemplateShaderPatcher.class);
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
