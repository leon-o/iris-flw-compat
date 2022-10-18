package top.leonx.irisflw.iris;

public class BufferBuilderStateManager {
    private static boolean allowExtend = true;

    public static void setAllowExtend(boolean allow){
        allowExtend = allow;
    }

    public static boolean isAllowExtend() {
        return allowExtend;
    }
}
