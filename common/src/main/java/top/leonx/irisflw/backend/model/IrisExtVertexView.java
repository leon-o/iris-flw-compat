package top.leonx.irisflw.backend.model;

import dev.engine_room.flywheel.lib.math.DataPacker;
import dev.engine_room.flywheel.lib.vertex.AbstractVertexView;
import net.irisshaders.iris.vertices.NormI8;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import top.leonx.irisflw.backend.IrisInternalVertex;

public class IrisExtVertexView extends AbstractVertexView {
    public IrisExtVertexView() {
    }

    public static final long STRIDE = IrisInternalVertex.EXT_STRIDE;



    private final CubiodBuilderTriView triView = new CubiodBuilderTriView();

    private final Vector3f[] pos = new Vector3f[4];

    public void readAllExtended(IrisVertexReader src) {

        for(int i = 0; i < vertexCount; ++i)
        {
            float midU = src.midU(i);
            float midV = src.midV(i);

            int tangent = src.getTangent(i);
            tangent = repackTangent(tangent);

            int midBlock = src.getMidBlock(i);
            midBlock = repackMidBlock(midBlock);

            var mcEntity = src.mcEntity(i);
            short entityX = (short) mcEntity.x;
            short entityY = (short) mcEntity.y;

            midU(i, midU);
            midV(i, midV);
            packedTangent(i, tangent);
            packedMidBlock(i, midBlock);
            mcEntityX(i, entityX);
            mcEntityY(i, entityY);
        }
    }

    public long stride() {
        return IrisInternalVertex.EXT_STRIDE;
    }

    public float x(int index) {
        return MemoryUtil.memGetFloat(this.ptr + (long)index * STRIDE);
    }

    public float y(int index) {
        return MemoryUtil.memGetFloat(this.ptr + (long)index * STRIDE + 4L);
    }

    public float z(int index) {
        return MemoryUtil.memGetFloat(this.ptr + (long)index * STRIDE + 8L);
    }

    public float r(int index) {
        return DataPacker.unpackNormU8(MemoryUtil.memGetByte(this.ptr + (long)index * STRIDE + 12L));
    }

    public float g(int index) {
        return DataPacker.unpackNormU8(MemoryUtil.memGetByte(this.ptr + (long)index * STRIDE + 13L));
    }

    public float b(int index) {
        return DataPacker.unpackNormU8(MemoryUtil.memGetByte(this.ptr + (long)index * STRIDE + 14L));
    }

    public float a(int index) {
        return DataPacker.unpackNormU8(MemoryUtil.memGetByte(this.ptr + (long)index * STRIDE + 15L));
    }

    public float u(int index) {
        return MemoryUtil.memGetFloat(this.ptr + (long)index * STRIDE + 16L);
    }

    public float v(int index) {
        return MemoryUtil.memGetFloat(this.ptr + (long)index * STRIDE + 20L);
    }

    @Override
    public int overlay(int i) {
        return OverlayTexture.NO_OVERLAY;
    }

    public int light(int index) {
        return MemoryUtil.memGetInt(this.ptr + (long)index * STRIDE + 24L);
    }

    public float normalX(int index) {
        return DataPacker.unpackNormI8(MemoryUtil.memGetByte(this.ptr + (long)index * STRIDE + 28L));
    }

    public float normalY(int index) {
        return DataPacker.unpackNormI8(MemoryUtil.memGetByte(this.ptr + (long)index * STRIDE + 29L));
    }

    public float normalZ(int index) {
        return DataPacker.unpackNormI8(MemoryUtil.memGetByte(this.ptr + (long)index * STRIDE + 30L));
    }

    public void x(int index, float x) {
        MemoryUtil.memPutFloat(this.ptr + (long)index * STRIDE, x);
    }

    public void y(int index, float y) {
        MemoryUtil.memPutFloat(this.ptr + (long)index * STRIDE + 4L, y);
    }

    public void z(int index, float z) {
        MemoryUtil.memPutFloat(this.ptr + (long)index * STRIDE + 8L, z);
    }

    public void r(int index, float r) {
        MemoryUtil.memPutByte(this.ptr + (long)index * STRIDE + 12L, DataPacker.packNormU8(r));
    }

    public void g(int index, float g) {
        MemoryUtil.memPutByte(this.ptr + (long)index * STRIDE + 13L, DataPacker.packNormU8(g));
    }

    public void b(int index, float b) {
        MemoryUtil.memPutByte(this.ptr + (long)index * STRIDE + 14L, DataPacker.packNormU8(b));
    }

    public void a(int index, float a) {
        MemoryUtil.memPutByte(this.ptr + (long)index * STRIDE + 15L, DataPacker.packNormU8(a));
    }

    public void u(int index, float u) {
        MemoryUtil.memPutFloat(this.ptr + (long)index * STRIDE + 16L, u);
    }

    public void v(int index, float v) {
        MemoryUtil.memPutFloat(this.ptr + (long)index * STRIDE + 20L, v);
    }

    @Override
    public void overlay(int i, int i1) {

    }

    public void light(int index, int light) {
        MemoryUtil.memPutInt(this.ptr + (long)index * STRIDE + 24L, light);
    }

    public void normalX(int index, float normalX) {
        MemoryUtil.memPutByte(this.ptr + (long)index * STRIDE + 28L, DataPacker.packNormI8(normalX));
    }

    public void normalY(int index, float normalY) {
        MemoryUtil.memPutByte(this.ptr + (long)index * STRIDE + 29L, DataPacker.packNormI8(normalY));
    }

    public void normalZ(int index, float normalZ) {
        MemoryUtil.memPutByte(this.ptr + (long)index * STRIDE + 30L, DataPacker.packNormI8(normalZ));
    }

    public void midU(int index, float midU) {
        MemoryUtil.memPutInt(this.ptr + (long)index * STRIDE + 32L, Float.floatToIntBits(midU));
    }

    public void midV(int index, float midV) {
        MemoryUtil.memPutInt(this.ptr + (long)index * STRIDE + 36L, Float.floatToIntBits(midV));
    }

    public void packedTangent(int index, int tangent) {
        MemoryUtil.memPutInt(this.ptr + (long)index * STRIDE + 40L, tangent);
    }

    public void packedMidBlock(int index, int midBlock) {
        MemoryUtil.memPutInt(this.ptr + (long)index * STRIDE + 44L, midBlock);
    }

    public void mcEntityX(int index, short blockId) {
        MemoryUtil.memPutShort(this.ptr + (long)index * STRIDE + 48L, blockId);
    }
    public void mcEntityY(int index, short renderType) {
        MemoryUtil.memPutShort(this.ptr + (long)index * STRIDE + 50L, renderType);
    }

    private int repackTangent(int packedTangent) {
        // Add 1.0f to each component to avoid negative values
        var x = NormI8.unpackX(packedTangent) + 1.0f;
        var y = NormI8.unpackY(packedTangent) + 1.0f;
        var z = NormI8.unpackZ(packedTangent) + 1.0f;
        var w = NormI8.unpackW(packedTangent) + 1.0f;
        return NormI8.pack(x, y, z, w);
    }

    private int repackMidBlock(int midBlock) {
        // Add 2.0f to each component to avoid negative values
        float x = (midBlock & 0xFF) * 0.015625F + 2.0f;
        float y = ((midBlock >> 8) & 0xFF) * 0.015625F + 2.0f;
        float z = ((midBlock >> 16) & 0xFF) * 0.015625F + 2.0f;
        int emission = (midBlock >> 24) & 0xFF;

        return (int)(x * 64.0F) & 255 | ((int)(y * 64.0F) & 255) << 8 | ((int)(z * 64.0F) & 255) << 16 | emission << 24;
    }
}
