package top.leonx.irisflw.iris;

import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniform;
import org.joml.Matrix3f;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

public class GlUniformMcMatrix3f extends GlUniform<Matrix3f> {

    public GlUniformMcMatrix3f(int index) {
        super(index);
    }


    public void set(Matrix3f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buf = stack.callocFloat(12);
            value.get(buf);

            GL30C.glUniformMatrix3fv(this.index, false, buf);
        }
    }
}
