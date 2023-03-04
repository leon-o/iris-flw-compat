package top.leonx.irisflw.iris;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniform;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

public class GlUniformMcMatrix3f extends GlUniform<Matrix3f>{

    private static final FloatBuffer buf = MemoryStack.stackGet()
            .mallocFloat(9);

    public GlUniformMcMatrix3f(int index) {
        super(index);
    }


    public void set(Matrix3f value) {
        value.store(buf);
        GL30C.glUniformMatrix3fv(this.index, false, buf);
    }
}
