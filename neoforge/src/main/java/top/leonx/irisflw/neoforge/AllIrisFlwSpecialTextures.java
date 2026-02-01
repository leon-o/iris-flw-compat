package top.leonx.irisflw.neoforge;

import net.createmod.catnip.render.BindableTexture;
import net.minecraft.resources.ResourceLocation;
import top.leonx.irisflw.IrisFlw;

public enum AllIrisFlwSpecialTextures implements BindableTexture {
    CHECKERED("checkerboard.png");

    public static final String ASSET_PATH = "textures/special/";
    private final ResourceLocation location;

    AllIrisFlwSpecialTextures(String filename) {
        location = ResourceLocation.fromNamespaceAndPath(IrisFlw.MOD_ID,ASSET_PATH + filename);
    }

    public ResourceLocation getLocation() {
        return location;
    }
}
