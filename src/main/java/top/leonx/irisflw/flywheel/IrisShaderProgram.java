package top.leonx.irisflw.flywheel;

import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import net.minecraft.client.render.Shader;
import net.minecraft.util.Identifier;

public class IrisShaderProgram extends WorldProgram {
    public Shader shader;

    public IrisShaderProgram(Identifier name, Shader shader) {
        super(name, shader.getProgramRef());
        this.shader = shader;
    }

    @Override
    public void bind() {
        //super.bind();
        shader.bind();
        //fog.bind();
        uploadWindowSize();
        uploadTime(AnimationTickHolder.getRenderTime());
    }
}
