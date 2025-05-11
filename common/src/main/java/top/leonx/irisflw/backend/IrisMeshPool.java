package top.leonx.irisflw.backend;

import dev.engine_room.flywheel.backend.InternalVertex;
import dev.engine_room.flywheel.backend.engine.IndexPool;
import dev.engine_room.flywheel.backend.engine.MeshPool;
import dev.engine_room.flywheel.backend.gl.array.GlVertexArray;
import dev.engine_room.flywheel.backend.gl.buffer.GlBuffer;
import top.leonx.irisflw.IrisFlw;

public class IrisMeshPool extends MeshPool {
    private final GlBuffer vbo;
    private final IndexPool indexPool;

    public IrisMeshPool() {
        try {
            var indexPoolField = MeshPool.class.getDeclaredField("indexPool");
            indexPoolField.setAccessible(true);
            this.indexPool = (IndexPool) indexPoolField.get(this);

            var vboField = MeshPool.class.getDeclaredField("vbo");
            vboField.setAccessible(true);
            this.vbo = (GlBuffer) vboField.get(this);

            if(IrisFlw.isUsingExtendedVertexFormat())
            {
                var vertexViewField = MeshPool.class.getDeclaredField("vertexView");
                vertexViewField.setAccessible(true);
                vertexViewField.set(this, IrisInternalVertex.createVertexView());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void bind(GlVertexArray vertexArray) {
        this.indexPool.bind(vertexArray);
        if (IrisFlw.isUsingExtendedVertexFormat()) {
            vertexArray.bindVertexBuffer(0, this.vbo.handle(), 0L, IrisInternalVertex.EXT_STRIDE);
            vertexArray.bindAttributes(0, 0, IrisInternalVertex.EXT_ATTRIBUTES);
        }else{
            vertexArray.bindVertexBuffer(0, this.vbo.handle(), 0L, InternalVertex.STRIDE);
            vertexArray.bindAttributes(0, 0, InternalVertex.ATTRIBUTES);
        }
    }
}
