package top.leonx.irisflw.vertex.block;

public class NormI8 {
    // There is no NormI8 in the Iris of 1.6.4, so we implement it ourselves.
    private static final float COMPONENT_RANGE = 127.0f;

    private static final float NORM = 1.0f / COMPONENT_RANGE;
    public static int pack(float x, float y, float z, float w) {
        return ((int) (x * 127) & 0xFF) | (((int) (y * 127) & 0xFF) << 8) | (((int) (z * 127) & 0xFF) << 16) | (((int) (w * 127) & 0xFF) << 24);
    }

    public static float unpackX(int norm) {
        return ((byte) (norm & 0xFF)) * NORM;
    }

    public static float unpackY(int norm) {
        return ((byte) ((norm >> 8) & 0xFF)) * NORM;
    }

    public static float unpackZ(int norm) {
        return ((byte) ((norm >> 16) & 0xFF)) * NORM;
    }

    public static float unpackW(int norm) {
        return ((byte) ((norm >> 24) & 0xFF)) * NORM;
    }
}
