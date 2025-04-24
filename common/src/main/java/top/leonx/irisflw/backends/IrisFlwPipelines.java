package top.leonx.irisflw.backends;

import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.compile.Pipeline;
import dev.engine_room.flywheel.lib.util.ResourceUtil;

public class IrisFlwPipelines {
    public static final Pipeline IRIS_INSTANCING = Pipeline.builder().compilerMarker("iris_instancing")
            .vertexMain(ResourceUtil.rl("internal/iris_instancing/main.vert"))
            .fragmentMain(ResourceUtil.rl("internal/iris_instancing/main.frag"))
            .assembler(IrisFlwInstanceComponent::new)
            .onLink(program -> {
                program.setSamplerBinding("_flw_instances", Samplers.INSTANCE_BUFFER);
                program.setSamplerBinding("_flw_lightLut", Samplers.LIGHT_LUT);
                program.setSamplerBinding("_flw_lightSections", Samplers.LIGHT_SECTIONS);
            })
            .build();
}
