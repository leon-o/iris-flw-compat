package top.leonx.irisflw.flywheel;

public class RenderLayerEventStateManager {
    private static boolean renderingShadow = false;

    public static boolean isRenderingShadow() {
        return renderingShadow;
    }

    public static void setRenderingShadow(boolean renderingShadow) {
        RenderLayerEventStateManager.renderingShadow = renderingShadow;
    }
}
