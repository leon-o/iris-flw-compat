package top.leonx.irisflw.mixin;

import com.jozufozu.flywheel.core.model.BlockModel;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.leonx.irisflw.IrisFlw;

@Mixin(value = BlockModel.class, remap = false)
public class MixinBlockModel {

    @Redirect(method = "<init>*", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferBuilder$DrawState;format()Lcom/mojang/blaze3d/vertex/VertexFormat;"))
    private VertexFormat irisFlw$ReturnIrisFormat(BufferBuilder.DrawState drawState) {
        // If we are using extended vertex format and the drawState format is TERRAIN, we must return the BLOCK format to avoid BlockModel throwing an exception.
        if (IrisFlw.isUsingExtendedVertexFormat() && drawState.format() == IrisVertexFormats.TERRAIN) {
            return DefaultVertexFormat.BLOCK;
        }
        return drawState.format();
    }
}
