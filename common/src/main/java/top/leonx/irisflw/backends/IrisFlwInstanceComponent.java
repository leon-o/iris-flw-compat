package top.leonx.irisflw.backends;

import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.layout.Layout;
import dev.engine_room.flywheel.backend.compile.component.BufferTextureInstanceComponent;
import dev.engine_room.flywheel.backend.glsl.generate.*;
import dev.engine_room.flywheel.lib.math.MoreMath;

import java.util.ArrayList;

public class IrisFlwInstanceComponent extends BufferTextureInstanceComponent {
    public IrisFlwInstanceComponent(InstanceType<?> instanceType) {
        super(instanceType);
    }
}
