package top.leonx.irisflw.accessors;

import top.leonx.irisflw.flywheel.IrisFlwCompatShaderWarp;

public interface WorldProgramAccessor {
    void setShader(IrisFlwCompatShaderWarp shader);

    void instanceUnbind();
}
