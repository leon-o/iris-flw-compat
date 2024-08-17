package top.leonx.irisflw.vertex;

import com.jozufozu.flywheel.core.vertex.AbstractVertexList;
import com.jozufozu.flywheel.util.RenderMath;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.coderbot.iris.vertices.IrisVertexFormats;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

/**
 * A vertex list reader for the Iris extended vertex format.
 */
public class IrisBlockVertexReaderImpl extends AbstractVertexList implements IrisBlockVertexReader {
    private final IntList offsets = new IntArrayList();
    private final int vertexSize;
    public IrisBlockVertexReaderImpl(ByteBuffer copyFrom, int vertexCount) {
        super(copyFrom, vertexCount);
        var elements = IrisVertexFormats.TERRAIN.getElements();
        int offset = 0;
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i) != DefaultVertexFormat.ELEMENT_PADDING) {
                offsets.add(offset);
            }
            offset += elements.get(i).getByteSize();
        }
        vertexSize = IrisVertexFormats.TERRAIN.getVertexSize();
    }

    private long ptr(long index) {
        return base + index * vertexSize;
    }

    @Override
    public float getX(int index) {
        return MemoryUtil.memGetFloat(ptr(index) + offsets.getInt(0));
    }

    @Override
    public float getY(int index) {
        return MemoryUtil.memGetFloat(ptr(index) + offsets.getInt(0) + 4);
    }

    @Override
    public float getZ(int index) {
        return MemoryUtil.memGetFloat(ptr(index) + offsets.getInt(0) + 8);
    }

    @Override
    public byte getR(int index) {
        return MemoryUtil.memGetByte(ptr(index) + offsets.getInt(1));
    }

    @Override
    public byte getG(int index) {
        return MemoryUtil.memGetByte(ptr(index) + offsets.getInt(1) + 1);
    }

    @Override
    public byte getB(int index) {
        return MemoryUtil.memGetByte(ptr(index) + offsets.getInt(1) + 2);
    }

    @Override
    public byte getA(int index) {
        return MemoryUtil.memGetByte(ptr(index) + offsets.getInt(1) + 3);
    }

    @Override
    public float getU(int index) {
        return MemoryUtil.memGetFloat(ptr(index) + offsets.getInt(2));
    }

    @Override
    public float getV(int index) {
        return MemoryUtil.memGetFloat(ptr(index) + offsets.getInt(2) + 4);
    }

    @Override
    public int getLight(int index) {
        return MemoryUtil.memGetInt(ptr(index) + offsets.getInt(3));
    }

    @Override
    public float getNX(int index) {
        return RenderMath.f(MemoryUtil.memGetByte(ptr(index) + offsets.getInt(4)));
    }

    @Override
    public float getNY(int index) {
        return RenderMath.f(MemoryUtil.memGetByte(ptr(index) + offsets.getInt(4) + 1));
    }

    @Override
    public float getNZ(int index) {
        return RenderMath.f(MemoryUtil.memGetByte(ptr(index) + offsets.getInt(4) + 2));
    }

    @Override
    public short getEntityX(int index) {
        return MemoryUtil.memGetShort(ptr(index) + offsets.getInt(5));
    }

    @Override
    public short getEntityY(int index) {
        return MemoryUtil.memGetShort(ptr(index) + offsets.getInt(5) + 2);
    }

    @Override
    public float getMidTexU(int index) {
        return MemoryUtil.memGetFloat(ptr(index) + offsets.getInt(6));
    }

    @Override
    public float getMidTexV(int index) {
        return MemoryUtil.memGetFloat(ptr(index) + offsets.getInt(6) + 4);
    }

    @Override
    public int getTangent(int index) {
        return MemoryUtil.memGetInt(ptr(index) + offsets.getInt(7));
    }

    @Override
    public int getMidBlock(int index) {
        return MemoryUtil.memGetInt(ptr(index) + offsets.getInt(8));
    }


    @Override
    public int copyTo(long dst, int index) {
        MemoryUtil.memCopy(ptr(index), dst, vertexSize);
        return vertexSize;
    }
}
