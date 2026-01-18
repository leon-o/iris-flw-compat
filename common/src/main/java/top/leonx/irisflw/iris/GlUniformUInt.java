package top.leonx.irisflw.iris;

import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniform;
import org.lwjgl.opengl.GL30C;

public class GlUniformUInt extends GlUniform<Integer> {
    public GlUniformUInt(int index) {
        super(index);
    }

    public void set(Integer value) {
        this.setInt(value);
    }

    public void setInt(int value) {
        GL30C.glUniform1ui(this.index, value);
    }
}
