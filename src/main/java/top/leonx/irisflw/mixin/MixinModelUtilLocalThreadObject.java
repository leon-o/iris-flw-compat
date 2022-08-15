package top.leonx.irisflw.mixin;

import com.jozufozu.flywheel.core.model.ShadeSeparatedBufferBuilder;
import net.minecraft.client.render.BufferBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.leonx.irisflw.accessors.BufferBuilderAccessor;

@Mixin(targets = "com.jozufozu.flywheel.core.model.ModelUtil$ThreadLocalObjects",remap = false)
public class MixinModelUtilLocalThreadObject {

    @Final
    @Shadow
    public final ShadeSeparatedBufferBuilder separatedBufferBuilder = new ShadeSeparatedBufferBuilder(512);

    @Final
    @Shadow
    public final BufferBuilder unshadedBuilder = new BufferBuilder(512);

    @Inject(method = "begin",remap = false,at=@At("HEAD"))
    private void injectBegin(CallbackInfo ci){
        (((BufferBuilderAccessor) separatedBufferBuilder)).setIsFlyWheelBufferBuilder(true);
        (((BufferBuilderAccessor) unshadedBuilder)).setIsFlyWheelBufferBuilder(true);
    }
}
