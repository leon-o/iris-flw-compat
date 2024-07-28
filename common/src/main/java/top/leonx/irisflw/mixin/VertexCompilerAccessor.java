package top.leonx.irisflw.mixin;

import com.jozufozu.flywheel.core.compile.Template;
import com.jozufozu.flywheel.core.compile.VertexCompiler;
import com.jozufozu.flywheel.core.compile.VertexData;
import com.jozufozu.flywheel.core.source.FileResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VertexCompiler.class)
public interface VertexCompilerAccessor {
    @Accessor(remap = false)
    Template<? extends VertexData> getTemplate();

    @Accessor(remap = false)
    FileResolution getHeader();
}
