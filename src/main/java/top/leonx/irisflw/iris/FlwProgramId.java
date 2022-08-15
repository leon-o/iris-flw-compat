package top.leonx.irisflw.iris;

import net.coderbot.iris.shaderpack.loading.ProgramGroup;
import net.coderbot.iris.shaderpack.loading.ProgramId;

import java.util.Objects;
import java.util.Optional;

public enum FlwProgramId {
    Shadow(ProgramGroup.Shadow, ""),
    ShadowSolid(ProgramGroup.Shadow, "solid", Shadow),
    ShadowCutout(ProgramGroup.Shadow, "cutout", Shadow),
    Basic(ProgramGroup.Gbuffers, "basic"),
    Line(ProgramGroup.Gbuffers, "line", Basic),
    Textured(ProgramGroup.Gbuffers, "textured", Basic),
    TexturedLit(ProgramGroup.Gbuffers, "textured_lit", Textured),
    SkyBasic(ProgramGroup.Gbuffers, "skybasic", Basic),
    SkyTextured(ProgramGroup.Gbuffers, "skytextured", Textured),
    Clouds(ProgramGroup.Gbuffers, "clouds", Textured),
    Terrain(ProgramGroup.Gbuffers, "terrain", TexturedLit),
    TerrainSolid(ProgramGroup.Gbuffers, "terrain_solid", Terrain),
    TerrainCutoutMip(ProgramGroup.Gbuffers, "terrain_cutout_mip", Terrain),
    TerrainCutout(ProgramGroup.Gbuffers, "terrain_cutout", Terrain),
    DamagedBlock(ProgramGroup.Gbuffers, "damagedblock", Terrain),
    Block(ProgramGroup.Gbuffers, "block", Terrain),
    BeaconBeam(ProgramGroup.Gbuffers, "beaconbeam", Textured),
    Item(ProgramGroup.Gbuffers, "item", TexturedLit),
    Entities(ProgramGroup.Gbuffers, "entities", TexturedLit),
    EntitiesGlowing(ProgramGroup.Gbuffers, "entities_glowing", Entities),
    ArmorGlint(ProgramGroup.Gbuffers, "armor_glint", Textured),
    SpiderEyes(ProgramGroup.Gbuffers, "spidereyes", Textured),
    Hand(ProgramGroup.Gbuffers, "hand", TexturedLit),
    Weather(ProgramGroup.Gbuffers, "weather", TexturedLit),
    Water(ProgramGroup.Gbuffers, "water", Terrain),
    HandWater(ProgramGroup.Gbuffers, "hand_water", Hand),
    Final(ProgramGroup.Final, "");

    private final ProgramGroup group;
    private final String sourceName;
    private final FlwProgramId fallback;

    private FlwProgramId(ProgramGroup var3, String var4) {
        this.group = var3;
        this.sourceName = var4.isEmpty() ? var3.getBaseName() : var3.getBaseName() + "_" + var4;
        this.fallback = null;
    }

    private FlwProgramId(ProgramGroup var3, String var4, FlwProgramId var5) {
        this.group = var3;
        this.sourceName = var4.isEmpty() ? var3.getBaseName() : var3.getBaseName() + "_" + var4;
        this.fallback = (FlwProgramId) Objects.requireNonNull(var5);
    }

    public ProgramGroup getGroup() {
        return this.group;
    }

    public String getSourceName() {
        return this.sourceName;
    }

    public Optional<FlwProgramId> getFallback() {
        return Optional.ofNullable(this.fallback);
    }
}
