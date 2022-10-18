package top.leonx.irisflw.transformer;

import com.jozufozu.flywheel.core.compile.Template;
import com.jozufozu.flywheel.core.compile.VertexData;
import com.jozufozu.flywheel.core.source.FileResolution;

@SuppressWarnings("unused")
public class GlslTransformerShaderPatcher extends ShaderPatcherBase{
    //private static EnumASTTransformer<Parameters, PatchShaderType> transformer;
    public GlslTransformerShaderPatcher(Template<? extends VertexData> template, FileResolution header) {
        super(template, header);

        // todo
    }

    @Override
    public String patch(String irisSource, Context key) {
        return null;
    }
}
