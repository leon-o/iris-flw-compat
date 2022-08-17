package top.leonx.irisflw.flywheel;

public class RenderLayerEventStateManager {
    private static boolean skip = true;

    public static boolean isSkip() {
        return skip;
    }

    public static void setSkip(boolean skip) {
        RenderLayerEventStateManager.skip = skip;
    }
}
