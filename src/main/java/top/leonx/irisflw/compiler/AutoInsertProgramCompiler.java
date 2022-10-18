package top.leonx.irisflw.compiler;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.compile.Template;
import com.jozufozu.flywheel.core.compile.VertexData;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.source.FileResolution;
import top.leonx.irisflw.transformer.AutoInsertShaderPatcher;
import top.leonx.irisflw.transformer.ShaderPatcherBase;

public class AutoInsertProgramCompiler<P extends WorldProgram> extends NewProgramCompiler<P>{

    AutoInsertShaderPatcher patcher;

    @Override
    public ShaderPatcherBase getShaderPatcher() {
        return patcher;
    }

    public AutoInsertProgramCompiler(GlProgram.Factory<P> factory, Template<? extends VertexData> template, FileResolution header) {
        super(factory, template, header);

        // Todo write yourself transformer to patch shader.
        patcher = new AutoInsertShaderPatcher(template,header);
    }
}
