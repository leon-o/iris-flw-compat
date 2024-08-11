package top.leonx.irisflw.vertex;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.core.vertex.BlockVertex;
import com.jozufozu.flywheel.core.vertex.BlockWriterUnsafe;
import com.jozufozu.flywheel.util.RenderMath;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class ExtendedBlockWriterUnsafe extends BlockWriterUnsafe {
    public ExtendedBlockWriterUnsafe(BlockVertex type, ByteBuffer buffer) {
        super(type, buffer);
    }

    @Override
    public void writeVertex(VertexList list, int i) {
        float x = list.getX(i);
        float y = list.getY(i);
        float z = list.getZ(i);

        float xN = list.getNX(i);
        float yN = list.getNY(i);
        float zN = list.getNZ(i);

        float u = list.getU(i);
        float v = list.getV(i);

        byte r = list.getR(i);
        byte g = list.getG(i);
        byte b = list.getB(i);
        byte a = list.getA(i);

        int light = list.getLight(i);

        MemoryUtil.memPutFloat(ptr, x);
        MemoryUtil.memPutFloat(ptr + 4, y);
        MemoryUtil.memPutFloat(ptr + 8, z);
        MemoryUtil.memPutByte(ptr + 12, r);
        MemoryUtil.memPutByte(ptr + 13, g);
        MemoryUtil.memPutByte(ptr + 14, b);
        MemoryUtil.memPutByte(ptr + 15, a);
        MemoryUtil.memPutFloat(ptr + 16, u);
        MemoryUtil.memPutFloat(ptr + 20, v);
        MemoryUtil.memPutInt(ptr + 24, light << 8); // light is packed in the low byte of each short
        MemoryUtil.memPutByte(ptr + 28, RenderMath.nb(xN));
        MemoryUtil.memPutByte(ptr + 29, RenderMath.nb(yN));
        MemoryUtil.memPutByte(ptr + 30, RenderMath.nb(zN));
        if (list instanceof IrisBlockVertexReader irisVertexList) {
            float midU = irisVertexList.getMidTexU(i);
            float midV = irisVertexList.getMidTexV(i);
            int tangent = irisVertexList.getTangent(i);
            int midBlock = irisVertexList.getMidBlock(i);
            MemoryUtil.memPutFloat(ptr + 32, midU);
            MemoryUtil.memPutFloat(ptr + 36, midV);
            MemoryUtil.memPutFloat(ptr + 40, Float.intBitsToFloat(tangent));
            MemoryUtil.memPutFloat(ptr + 44, Float.intBitsToFloat(midBlock));
        }else{
            MemoryUtil.memPutLong(ptr + 32, 0);
            MemoryUtil.memPutLong(ptr + 40, 0);
        }

        ptr += IrisFlwBlockVertex.EXTEND_FORMAT.getStride();
        advance();
    }
}
