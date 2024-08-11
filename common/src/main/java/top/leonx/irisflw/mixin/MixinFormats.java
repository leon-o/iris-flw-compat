package top.leonx.irisflw.mixin;

import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.vertex.BlockVertex;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import top.leonx.irisflw.vertex.ExtendedBlockVertex;

@Mixin(Formats.class)
public class MixinFormats {

    // Set the BLOCK format to our IrisFlwBlockVertex.
    @Final
    @Shadow
    public static BlockVertex BLOCK = new ExtendedBlockVertex();
}
