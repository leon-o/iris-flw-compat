package top.leonx.irisflw.backend;

import dev.engine_room.flywheel.api.layout.FloatRepr;
import dev.engine_room.flywheel.api.layout.Layout;
import dev.engine_room.flywheel.api.layout.LayoutBuilder;
import dev.engine_room.flywheel.backend.LayoutAttributes;
import dev.engine_room.flywheel.backend.gl.array.VertexAttribute;
import dev.engine_room.flywheel.lib.util.ResourceUtil;
import dev.engine_room.flywheel.lib.vertex.VertexView;
import net.minecraft.resources.ResourceLocation;
import top.leonx.irisflw.backend.model.IrisExtVertexView;

import java.util.List;

public class IrisInternalVertex {
    public static final Layout EXT_LAYOUT = LayoutBuilder.create()
            .vector("position", FloatRepr.FLOAT, 3) // 12
            .vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4) // 16
            .vector("tex", FloatRepr.FLOAT, 2) // 24
//            .vector("overlay", FloatRepr.SHORT, 2)
            .vector("light", FloatRepr.UNSIGNED_SHORT, 2) // 28
            .vector("normal", FloatRepr.NORMALIZED_BYTE, 4) // 32
            .vector("extend", FloatRepr.FLOAT, 4) // 48
            .vector("mc_Entity", FloatRepr.SHORT, 2) // 52
            .build();

    public static final List<VertexAttribute> EXT_ATTRIBUTES = LayoutAttributes.attributes(EXT_LAYOUT);
    public static final int EXT_STRIDE = EXT_LAYOUT.byteSize();

    public static final ResourceLocation EXT_LAYOUT_SHADER = ResourceUtil.rl("internal/iris_instancing/vertex_ext_input.vert");
    public static final ResourceLocation LAYOUT_SHADER = ResourceUtil.rl("internal/iris_instancing/vertex_input.vert");

    private IrisInternalVertex() {
    }

    public static VertexView createVertexView() {
        return new IrisExtVertexView();
    }
}
