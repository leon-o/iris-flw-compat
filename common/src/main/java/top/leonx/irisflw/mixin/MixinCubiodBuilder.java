package top.leonx.irisflw.mixin;

import com.jozufozu.flywheel.core.hardcoded.PartBuilder;
import com.jozufozu.flywheel.core.vertex.PosTexNormalWriterUnsafe;
import net.irisshaders.iris.vertices.NormalHelper;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.leonx.irisflw.vertex.CubiodBuilderTriView;
import top.leonx.irisflw.vertex.ExtendedPosTexNormalWriterUnsafe;

@Mixin(PartBuilder.CuboidBuilder.class)
public class MixinCubiodBuilder {

    @Unique
    public CubiodBuilderTriView irisFlw$polygonView = new CubiodBuilderTriView();

    @Inject(method = "quad", at = @At("HEAD"), cancellable = true)
    public void irisFlw$quad(PosTexNormalWriterUnsafe buffer, Vector3f[] vertices, float minU, float minV, float maxU, float maxV, Vector3f normal, CallbackInfo ci){
        if(buffer instanceof ExtendedPosTexNormalWriterUnsafe extended){
            this.irisFlw$polygonView.setup(vertices, minU, maxU, minV, maxV);
            float midU = minU + (maxU - minU) / 2.0F;
            float midV = minV + (maxV - minV) / 2.0F;
            var tangent = NormalHelper.computeTangent(normal.x(), normal.y(), normal.z(), this.irisFlw$polygonView);

            extended.putVertexExtended(vertices[0].x(), vertices[0].y(), vertices[0].z(), normal.x(), normal.y(), normal.z(), maxU, minV, midU, midV, tangent, 0);
            extended.putVertexExtended(vertices[1].x(), vertices[1].y(), vertices[1].z(), normal.x(), normal.y(), normal.z(), minU, minV, midU, midV, tangent, 0);
            extended.putVertexExtended(vertices[2].x(), vertices[2].y(), vertices[2].z(), normal.x(), normal.y(), normal.z(), minU, maxV, midU, midV, tangent, 0);
            extended.putVertexExtended(vertices[3].x(), vertices[3].y(), vertices[3].z(), normal.x(), normal.y(), normal.z(), maxU, maxV, midU, midV, tangent, 0);
            ci.cancel();
        }
    }
}
