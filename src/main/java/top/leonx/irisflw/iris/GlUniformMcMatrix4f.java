package top.leonx.irisflw.iris;

import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniform;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

public class GlUniformMcMatrix4f extends GlUniform<Matrix4f> {

    private static final FloatBuffer buf = MemoryStack.stackGet()
            .mallocFloat(16);

    public GlUniformMcMatrix4f(int index) {
        super(index);
    }

    @Override
    public void set(Matrix4f value) {
        value.writeColumnMajor(buf);
        GL30C.glUniformMatrix4fv(this.index, false, buf);
    }
}
