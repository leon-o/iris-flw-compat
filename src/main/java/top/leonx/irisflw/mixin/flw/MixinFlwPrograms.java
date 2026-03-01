package top.leonx.irisflw.mixin.flw;

import dev.engine_room.flywheel.backend.compile.FlwPrograms;
import dev.engine_room.flywheel.backend.glsl.ShaderSources;
import dev.engine_room.flywheel.backend.glsl.SourceComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.leonx.irisflw.backend.IrisInstancingPrograms;

import java.util.List;

@Mixin(FlwPrograms.class)
public class MixinFlwPrograms {

    @Final
    @Shadow
    private static ResourceLocation COMPONENTS_HEADER_FRAG;

    @Shadow
    public static ShaderSources SOURCES;


    @Inject(method = "reload", at = @At("TAIL"), remap = false)
    private static void irisflw$reload$tail(ResourceManager resourceManager, CallbackInfo ci) {
        IrisInstancingPrograms.setInstance(null);

        var fragmentComponentsHeader = SOURCES.get(COMPONENTS_HEADER_FRAG);

        List<SourceComponent> vertexComponents = List.of();
        List<SourceComponent> fragmentComponents = List.of(fragmentComponentsHeader);

        IrisInstancingPrograms.reload(SOURCES, vertexComponents, fragmentComponents);
    }
}
