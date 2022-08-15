package top.leonx.irisflw.accessors;

import net.minecraft.client.render.Shader;

public interface WorldProgramAccessor {
    void setShader(Shader shader);

    void instanceUnbind();
}
