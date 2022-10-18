package top.leonx.irisflw.flywheel;

public class RenderLayerEventStateManager {
    private static boolean skip = true;

    private static boolean renderingShadow = false;

    public static boolean isSkip() {
        return skip;
    }

    public static void setSkip(boolean skip) {
        RenderLayerEventStateManager.skip = skip;
    }

    public static boolean isRenderingShadow() {
        return renderingShadow;
    }

    public static void setRenderingShadow(boolean renderingShadow) {
        RenderLayerEventStateManager.renderingShadow = renderingShadow;
    }
}
