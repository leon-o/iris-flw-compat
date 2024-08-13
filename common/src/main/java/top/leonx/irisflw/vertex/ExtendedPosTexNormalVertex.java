package top.leonx.irisflw.vertex;

import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.layout.CommonItems;
import com.jozufozu.flywheel.core.vertex.PosTexNormalVertex;
import com.jozufozu.flywheel.core.vertex.PosTexNormalVertexListUnsafe;
import com.jozufozu.flywheel.core.vertex.PosTexNormalWriterUnsafe;
import org.jetbrains.annotations.NotNull;
import top.leonx.irisflw.IrisFlw;

import java.nio.ByteBuffer;

public class ExtendedPosTexNormalVertex extends PosTexNormalVertex {

    public static final BufferLayout EXTEND_FORMAT = BufferLayout.builder()
            .addItems(CommonItems.VEC3,         //POSITION  3xFLOAT = 12
                    CommonItems.UV,             //UV        2xFLOAT = 8
                    CommonItems.NORMAL,         //NORMAL    3xBYTE  = 3
                    CommonItems.PADDING_BYTE,   //PADDING   1xBYTE  = 1     ^^^DEFAULT-VERTEX-FORMAT 32 BYTES
                    CommonItems.VEC4,           //EXTEND_DATA 4xFLOAT = 16  xy:midTexCoord, z:tangent w:midBlock
                    ExtendedLayoutItems.SHORT2  //MC_ENTITY 2xSHORT = 4
                    // Total: 44
            )
            .build();


    @Override
    public @NotNull BufferLayout getLayout() {
        if (IrisFlw.isUsingExtendedVertexFormat()) {
            return EXTEND_FORMAT;
        }
        return super.getLayout();
    }

    @Override
    public @NotNull PosTexNormalWriterUnsafe createWriter(@NotNull ByteBuffer buffer) {
        if (IrisFlw.isUsingExtendedVertexFormat()) {
            return new ExtendedPosTexNormalWriterUnsafe(this, buffer);
        }
        return super.createWriter(buffer);
    }

    @Override
    public @NotNull PosTexNormalVertexListUnsafe createReader(ByteBuffer buffer, int vertexCount) {
        if (IrisFlw.isUsingExtendedVertexFormat()) {
            return new ExtendedPosTexNormalWriterUnsafe.Reader(buffer, vertexCount);
        }
        return super.createReader(buffer, vertexCount);
    }

    @Override
    public @NotNull String getShaderHeader() {
        if (IrisFlw.isUsingExtendedVertexFormat()) {
            return """
                    layout (location = 0) in vec3 _flw_v_pos;
                    layout (location = 1) in vec2 _flw_v_texCoords;
                    layout (location = 2) in vec3 _flw_v_normal;
                    layout (location = 3) in vec4 _flw_v_packed_extended;  // x:midTexCoord, z:tangent, w:midBlock
                    layout (location = 4) in vec4 _flw_v_mc_Entity;        // x:entityId or blockId, y:render type

                    Vertex FLWCreateVertex() {
                        Vertex v;
                        v.pos = _flw_v_pos;
                        v.color = vec4(1.);
                        v.texCoords = _flw_v_texCoords;
                        v.light = vec2(0.);
                        v.normal = _flw_v_normal;
                        return v;
                    }
                    """;
        }
        return super.getShaderHeader();
    }
}
