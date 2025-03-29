package top.leonx.irisflw.vertex;

/*import com.jozufozu.flywheel.api.vertex.ShadedVertexList;
import com.jozufozu.flywheel.core.vertex.BlockVertexListUnsafe;

import java.nio.ByteBuffer;

*//**
 * Our replacement for BlockVertexListUnsafe that implements IrisBlockVertexReader.
 * This is used to read the extended vertex format used by Iris.
 *//*
public class IrisBlockVertexListUnsafe extends BlockVertexListUnsafe implements IrisBlockVertexReader {

    private IrisBlockVertexReaderImpl impl;
    public IrisBlockVertexListUnsafe(ByteBuffer copyFrom, int vertexCount) {
        super(copyFrom, vertexCount);
        impl = new IrisBlockVertexReaderImpl(copyFrom,vertexCount);
    }

    @Override
    public float getX(int index) { // 4 bytes
        return impl.getX(index);
    }

    @Override
    public float getY(int index) { // 4 bytes
        return impl.getY(index);
    }

    @Override
    public float getZ(int index) { // 4 bytes
        return impl.getZ(index);
    }

    @Override
    public byte getR(int index) { // 1 byte
        return impl.getR(index);
    }

    @Override
    public byte getG(int index) { // 1 byte
        return impl.getG(index);
    }

    @Override
    public byte getB(int index) { // 1 byte
        return impl.getB(index);
    }

    @Override
    public byte getA(int index) { // 1 byte
        return impl.getA(index);
    }

    @Override
    public float getU(int index) { // 4 bytes
        return impl.getU(index);
    }

    @Override
    public float getV(int index) { // 4 bytes
        return impl.getV(index);
    }

    @Override
    public int getLight(int index) { // 4 bytes
        return impl.getLight(index);
    }

    @Override
    public float getNX(int index) { // 1 byte
        return impl.getNX(index);
    }

    @Override
    public float getNY(int index) { // 1 byte
        return impl.getNY(index);
    }

    @Override
    public float getNZ(int index) { // 1 byte
        return impl.getNZ(index);
    }

    @Override
    public short getEntityX(int index) {
        return impl.getEntityX(index);
    }

    @Override
    public short getEntityY(int index) {
        return impl.getEntityY(index);
    }

    @Override
    public float getMidTexU(int index) {
        return impl.getMidTexU(index);
    }

    @Override
    public float getMidTexV(int index) {
        return impl.getMidTexV(index);
    }

    @Override
    public int getTangent(int index) {
        return impl.getTangent(index);
    }

    @Override
    public int getMidBlock(int index) {
        return impl.getMidBlock(index);
    }

    @Override
    public int copyTo(long dst, int index) {
        return impl.copyTo(dst,index);
    }

    public static class Shaded extends BlockVertexListUnsafe.Shaded implements ShadedVertexList, IrisBlockVertexReader {

        private IrisBlockVertexReaderImpl impl;

        public Shaded(ByteBuffer buffer, int vertexCount, int unshadedStartVertex) {
            super(buffer, vertexCount, unshadedStartVertex);
            impl = new IrisBlockVertexReaderImpl(buffer,vertexCount);
        }

        @Override
        public float getX(int index) { // 4 bytes
            return impl.getX(index);
        }

        @Override
        public float getY(int index) { // 4 bytes
            return impl.getY(index);
        }

        @Override
        public float getZ(int index) { // 4 bytes
            return impl.getZ(index);
        }

        @Override
        public byte getR(int index) { // 1 byte
            return impl.getR(index);
        }

        @Override
        public byte getG(int index) { // 1 byte
            return impl.getG(index);
        }

        @Override
        public byte getB(int index) { // 1 byte
            return impl.getB(index);
        }

        @Override
        public byte getA(int index) { // 1 byte
            return impl.getA(index);
        }

        @Override
        public float getU(int index) { // 4 bytes
            return impl.getU(index);
        }

        @Override
        public float getV(int index) { // 4 bytes
            return impl.getV(index);
        }

        @Override
        public int getLight(int index) { // 4 bytes
            return impl.getLight(index);
        }

        @Override
        public float getNX(int index) { // 1 byte
            return impl.getNX(index);
        }

        @Override
        public float getNY(int index) { // 1 byte
            return impl.getNY(index);
        }

        @Override
        public float getNZ(int index) { // 1 byte
            return impl.getNZ(index);
        }

        @Override
        public short getEntityX(int index) {
            return impl.getEntityX(index);
        }

        @Override
        public short getEntityY(int index) {
            return impl.getEntityY(index);
        }

        @Override
        public float getMidTexU(int index) {
            return impl.getMidTexU(index);
        }

        @Override
        public float getMidTexV(int index) {
            return impl.getMidTexV(index);
        }

        @Override
        public int getTangent(int index) {
            return impl.getTangent(index);
        }

        @Override
        public int getMidBlock(int index) {
            return impl.getMidBlock(index);
        }

        @Override
        public int copyTo(long dst, int index) {
            return impl.copyTo(dst,index);
        }
    }
}*/
