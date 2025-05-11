package top.leonx.irisflw.mixin.flw;

import dev.engine_room.flywheel.api.vertex.VertexList;
import dev.engine_room.flywheel.backend.compile.core.Compilation;
import dev.engine_room.flywheel.lib.model.SimpleQuadMesh;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SimpleQuadMesh.class)
public interface SimpleQuadMeshAccessor {

    @Accessor
    VertexList getVertexList();
}
