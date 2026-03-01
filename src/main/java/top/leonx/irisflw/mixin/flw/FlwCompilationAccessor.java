package top.leonx.irisflw.mixin.flw;

import dev.engine_room.flywheel.backend.compile.core.Compilation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Compilation.class)
public interface FlwCompilationAccessor {

    @Accessor
    StringBuilder getGeneratedSource();

    @Accessor
    StringBuilder getFullSource();

    @Invoker(value = "dumpSource")
    static void invokeDumpSource(String source, String fileName)
    {

    }
}
