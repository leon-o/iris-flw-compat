package top.leonx.irisflw.vertex.block;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.core.vertex.BlockVertex;
import com.jozufozu.flywheel.core.vertex.BlockWriterUnsafe;
import com.jozufozu.flywheel.util.RenderMath;
import net.coderbot.iris.vertices.NormI8;
import org.lwjgl.system.MemoryUtil;
import top.leonx.irisflw.vertex.IrisBlockVertexReader;

import java.nio.ByteBuffer;

/**
 * A vertex writer for the extended vertex format.
 * This writer will write the additional data to the buffer, including the midTexCoords, tangent, mid block pos, and blockId.
 */
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
            tangent = repackTangent(tangent);

            int midBlock = irisVertexList.getMidBlock(i);
            midBlock = repackMidBlock(midBlock);

            short entityX = irisVertexList.getEntityX(i);
            short entityY = irisVertexList.getEntityY(i);

            MemoryUtil.memPutFloat(ptr + 32, midU);
            MemoryUtil.memPutFloat(ptr + 36, midV);
            MemoryUtil.memPutFloat(ptr + 40, Float.intBitsToFloat(tangent));
            MemoryUtil.memPutFloat(ptr + 44, Float.intBitsToFloat(midBlock));

            MemoryUtil.memPutShort(ptr + 48, entityX);
            MemoryUtil.memPutShort(ptr + 50, entityY);
        }else{
            MemoryUtil.memPutLong(ptr + 32, 0);
            MemoryUtil.memPutLong(ptr + 40, 0);
            MemoryUtil.memPutInt(ptr + 48, 0);
        }

        ptr += ExtendedBlockVertex.EXTEND_FORMAT.getStride();
        advance();
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
