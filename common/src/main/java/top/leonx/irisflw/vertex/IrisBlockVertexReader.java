package top.leonx.irisflw.vertex;

public interface IrisBlockVertexReader {
    short getEntityX(int index);

    short getEntityY(int index);

    float getMidTexU(int index);
    float getMidTexV(int index);

    int getTangent(int index);
    int getMidBlock(int index);
    int copyTo(long dst, int index);
}
