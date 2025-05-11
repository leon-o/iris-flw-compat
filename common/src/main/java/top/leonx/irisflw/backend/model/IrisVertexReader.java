package top.leonx.irisflw.backend.model;

import dev.engine_room.flywheel.api.vertex.VertexList;
import dev.engine_room.flywheel.lib.math.DataPacker;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Vector2i;
import org.lwjgl.system.MemoryUtil;

public class IrisVertexReader implements VertexList {

    public static long STRIDE = IrisVertexFormats.TERRAIN.getVertexSize();
    private final long ptr;
    private final int vertexCount;

    public IrisVertexReader(long ptr, int vertexCount) {
        this.ptr = ptr;
        this.vertexCount = vertexCount;
    }

    public long stride() {
        return STRIDE;
    }

    public long ptr() {
        return this.ptr;
    }

    public int vertexCount() {
        return this.vertexCount;
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

    public Vector2i mcEntity(int index) {
        var renderEntity = MemoryUtil.memGetShort(this.ptr + (long)index * STRIDE + 32L);
        var renderedBlockEntity = MemoryUtil.memGetShort(this.ptr + (long)index * STRIDE + 34L);
        return new Vector2i(renderEntity, renderedBlockEntity);
    }

    public float midU(int index) {
        return MemoryUtil.memGetFloat(this.ptr + (long)index * STRIDE + 36L);
    }

    public float midV(int index) {
        return MemoryUtil.memGetFloat(this.ptr + (long)index * STRIDE + 40L);
    }

    public int getTangent(int index) {
        return MemoryUtil.memGetInt(this.ptr + (long)index * STRIDE + 44L);
    }

    public int getMidBlock(int index) {
        return MemoryUtil.memGetInt(this.ptr + (long)index * STRIDE + 48L);
    }
}
