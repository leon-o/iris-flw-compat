package top.leonx.irisflw.iris;

import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniform;
import org.lwjgl.opengl.GL30C;

public class GlUniformUInt2v extends GlUniform<int[]> {
    public GlUniformUInt2v(int index) {
        super(index);
    }

    public void set(int[] value) {
        if (value.length != 2) {
            throw new IllegalArgumentException("value.length != 2");
        } else {
            GL30C.glUniform2uiv(this.index, value);
        }
    }

    public void set(int x, int y) {
        GL30C.glUniform2ui(this.index, x, y);
    }
}
