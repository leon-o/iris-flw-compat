package top.leonx.irisflw.mixin.flw;

import dev.engine_room.flywheel.api.model.Mesh;
import dev.engine_room.flywheel.backend.engine.MeshPool;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.leonx.irisflw.IrisFlw;
import top.leonx.irisflw.backend.model.IrisExtVertexView;

@Mixin(value = MeshPool.PooledMesh.class, remap = false)
public abstract class MixinPooledMesh {
    @Shadow @Final private Mesh mesh;

    @Shadow public abstract int vertexCount();

    @Inject(method = "byteSize", at = @At("HEAD"), cancellable = true)
    public void irisflw$byteSize(CallbackInfoReturnable<Integer> cir)
    {
        if(IrisFlw.isUsingExtendedVertexFormat())
        {
            cir.setReturnValue((int) (IrisExtVertexView.STRIDE * this.vertexCount()));
        }
    }
}
