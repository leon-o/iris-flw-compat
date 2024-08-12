package top.leonx.irisflw.mixin;

import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.vertex.BlockVertex;
import com.jozufozu.flywheel.core.vertex.PosTexNormalVertex;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import top.leonx.irisflw.vertex.ExtendedBlockVertex;
import top.leonx.irisflw.vertex.ExtendedPosTexNormalVertex;

@Mixin(Formats.class)
public class MixinFormats {

    // Set the BLOCK format to our ExtendedBlockVertex.
    @Final
    @Shadow
    public static BlockVertex BLOCK = new ExtendedBlockVertex();

    @Final
    @Shadow
    public static final PosTexNormalVertex POS_TEX_NORMAL = new ExtendedPosTexNormalVertex();
}
