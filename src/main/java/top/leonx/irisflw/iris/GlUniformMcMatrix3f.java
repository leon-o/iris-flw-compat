package top.leonx.irisflw.iris;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniform;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

public class GlUniformMcMatrix3f extends GlUniform<Matrix3f> {

    public GlUniformMcMatrix3f(int index) {
        super(index);
    }


    public void set(Matrix3f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buf = stack.callocFloat(16);
            value.get(buf);

            GL30C.glUniformMatrix4fv(this.index, false, buf);
        }
    }
}
