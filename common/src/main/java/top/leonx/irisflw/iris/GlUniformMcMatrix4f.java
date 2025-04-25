package top.leonx.irisflw.iris;

import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniform;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

public class GlUniformMcMatrix4f extends GlUniform<Matrix4fc>{
    public GlUniformMcMatrix4f(int index) {
        super(index);
    }

    public void set(Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buf = stack.callocFloat(16);
            value.get(buf);

            GL30C.glUniformMatrix4fv(this.index, false, buf);
        }
    }

    public void set(Matrix4fc value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buf = stack.callocFloat(16);
            value.get(buf);

            GL30C.glUniformMatrix4fv(this.index, false, buf);
        }
    }
    public int getIndex() {
        return this.index;
    }
}
