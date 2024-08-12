package top.leonx.irisflw.vertex;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.core.layout.CommonItems;
import com.jozufozu.flywheel.core.vertex.PosTexNormalVertex;
import com.jozufozu.flywheel.core.vertex.PosTexNormalVertexListUnsafe;
import com.jozufozu.flywheel.core.vertex.PosTexNormalWriterUnsafe;
import com.jozufozu.flywheel.util.RenderMath;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class ExtendedPosTexNormalWriterUnsafe extends PosTexNormalWriterUnsafe implements BlockSensitiveBufferBuilder {

    private short currentBlock = -1;
    private short currentRenderType = -1;
    private int currentLocalPosX;
    private int currentLocalPosY;
    private int currentLocalPosZ;


    public ExtendedPosTexNormalWriterUnsafe(PosTexNormalVertex type, ByteBuffer buffer) {
        super(type, buffer);
    }

    @Override
    public void writeVertex(VertexList list, int i) {
        super.writeVertex(list, i);
    }

    @Override
    public void putVertex(float x, float y, float z, float nX, float nY, float nZ, float u, float v) {
        this.putVertexExtended(x, y, z, nX, nY, nZ, u, v, 0, 0, 0, 0);
    }


    public void putVertexExtended(float x, float y, float z, float nX, float nY, float nZ, float u, float v,
                                  float midU, float midV, int tangent, int midBlock) {
        MemoryUtil.memPutFloat(ptr, x);
        MemoryUtil.memPutFloat(ptr + 4, y);
        MemoryUtil.memPutFloat(ptr + 8, z);
        MemoryUtil.memPutFloat(ptr + 12, u);
        MemoryUtil.memPutFloat(ptr + 16, v);
        MemoryUtil.memPutByte(ptr + 20, RenderMath.nb(nX));
        MemoryUtil.memPutByte(ptr + 21, RenderMath.nb(nY));
        MemoryUtil.memPutByte(ptr + 22, RenderMath.nb(nZ));

        // MidU, MidV
        MemoryUtil.memPutFloat(ptr + 24, midU);
        MemoryUtil.memPutFloat(ptr + 28, midV);

        // Tangent
        MemoryUtil.memPutFloat(ptr + 32, Float.intBitsToFloat(tangent));

        // MidBlockPos
        MemoryUtil.memPutFloat(ptr + 36, Float.intBitsToFloat(midBlock));

        // mc_Entity.x
        MemoryUtil.memPutShort(ptr + 40, currentBlock);
        MemoryUtil.memPutShort(ptr + 42, currentRenderType);

        ptr += 44;
        advance();
    }

    @Override
    public void beginBlock(short block, short renderType, int localPosX, int localPosY, int localPosZ) {
        this.currentBlock = block;
        this.currentRenderType = renderType;
        this.currentLocalPosX = localPosX;
        this.currentLocalPosY = localPosY;
        this.currentLocalPosZ = localPosZ;
    }

    @Override
    public void endBlock() {
        this.currentBlock = -1;
        this.currentRenderType = -1;
        this.currentLocalPosX = 0;
        this.currentLocalPosY = 0;
        this.currentLocalPosZ = 0;
    }

    public static class Reader extends PosTexNormalVertexListUnsafe implements IrisBlockVertexReader {
        private final IntList offsets = new IntArrayList();
        private final int vertexSize;

        public Reader(ByteBuffer copyFrom, int vertexCount) {
            super(copyFrom, vertexCount);
            var elements = ExtendedPosTexNormalVertex.EXTEND_FORMAT.getLayoutItems();
            int offset = 0;
            for (int i = 0; i < elements.size(); i++) {
                if (elements.get(i) != CommonItems.PADDING_BYTE) {
                    offsets.add(offset);
                }
                offset += elements.get(i).size();
            }
            vertexSize = ExtendedPosTexNormalVertex.EXTEND_FORMAT.getStride();
        }

        private long ptr(long index) {
            return base + index * vertexSize;
        }

        @Override
        public float getX(int index) {
            return MemoryUtil.memGetFloat(ptr(index));
        }

        @Override
        public float getY(int index) {
            return MemoryUtil.memGetFloat(ptr(index) + 4);
        }

        @Override
        public float getZ(int index) {
            return MemoryUtil.memGetFloat(ptr(index) + 8);
        }

        @Override
        public float getU(int index) {
            return MemoryUtil.memGetFloat(ptr(index) + offsets.getInt(1));
        }

        @Override
        public float getV(int index) {
            return MemoryUtil.memGetFloat(ptr(index) + offsets.getInt(1) + 4);
        }

        @Override
        public float getNX(int index) {
            return RenderMath.f(MemoryUtil.memGetByte(ptr(index) + offsets.getInt(2)));
        }

        @Override
        public float getNY(int index) {
            return RenderMath.f(MemoryUtil.memGetByte(ptr(index) + offsets.getInt(2) + 1));
        }

        @Override
        public float getNZ(int index) {
            return RenderMath.f(MemoryUtil.memGetByte(ptr(index) + offsets.getInt(2) + 2));
        }

        @Override
        public float getMidTexU(int index) {
            return MemoryUtil.memGetFloat(ptr(index) + offsets.getInt(3));
        }

        @Override
        public float getMidTexV(int index) {
            return MemoryUtil.memGetFloat(ptr(index) + offsets.getInt(3) + 4);
        }

        @Override
        public int getTangent(int index) {
            return MemoryUtil.memGetInt(ptr(index) + offsets.getInt(3) + 8);
        }

        @Override
        public int getMidBlock(int index) {
            return MemoryUtil.memGetInt(ptr(index) + offsets.getInt(3) + 12);
        }

        @Override
        public short getEntityX(int index) {
            return MemoryUtil.memGetShort(ptr(index) + offsets.getInt(4));
        }

        @Override
        public short getEntityY(int index) {
            return MemoryUtil.memGetShort(ptr(index) + offsets.getInt(4) + 2);
        }

        @Override
        public int copyTo(long dst, int index) {
            return 0;
        }
    }
}
