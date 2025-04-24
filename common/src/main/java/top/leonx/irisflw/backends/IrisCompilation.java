package top.leonx.irisflw.backends;

import dev.engine_room.flywheel.backend.compile.core.Compilation;
import dev.engine_room.flywheel.backend.compile.core.ShaderResult;
import dev.engine_room.flywheel.backend.gl.shader.ShaderType;
import top.leonx.irisflw.mixin.flw.FlwCompilationAccessor;

public class IrisCompilation extends Compilation {
    private final FlwCompilationAccessor compilationAccessor;
    public IrisCompilation()
    {
        compilationAccessor=(FlwCompilationAccessor)this;
    }

    @Override
    public ShaderResult compile(ShaderType shaderType, String name) {
//        int handle = GL20.glCreateShader(shaderType.glEnum);
        String source = this.compilationAccessor.getFullSource().toString();
//        GlCompat.safeShaderSource(handle, source);
//        GL20.glCompileShader(handle);
        String shaderName = name + "." + shaderType.extension;
        FlwCompilationAccessor.invokeDumpSource(source, shaderName);
        return ShaderResult.success(new IntermediateGlShader(source, shaderType, name), "");
    }
}
