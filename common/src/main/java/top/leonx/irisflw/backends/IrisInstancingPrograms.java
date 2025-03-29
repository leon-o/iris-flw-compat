package top.leonx.irisflw.backends;

import com.google.common.collect.ImmutableList;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.backend.compile.*;
import dev.engine_room.flywheel.backend.gl.GlCompat;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import dev.engine_room.flywheel.backend.glsl.GlslVersion;
import dev.engine_room.flywheel.backend.glsl.ShaderSources;
import dev.engine_room.flywheel.backend.glsl.SourceComponent;
import dev.engine_room.flywheel.backend.util.AtomicReferenceCounted;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

public class IrisInstancingPrograms extends AtomicReferenceCounted {
    private static final List<String> EXTENSIONS = getExtensions(GlCompat.MAX_GLSL_VERSION);

    @Nullable
    private static IrisInstancingPrograms instance;

    private final PipelineCompiler pipeline;

    private final OitPrograms oitPrograms;

    private IrisInstancingPrograms(PipelineCompiler pipeline, OitPrograms oitPrograms) {
        this.pipeline = pipeline;
        this.oitPrograms = oitPrograms;
    }

    private static List<String> getExtensions(GlslVersion glslVersion) {
        var extensions = ImmutableList.<String>builder();
        if (glslVersion.compareTo(GlslVersion.V330) < 0) {
            extensions.add("GL_ARB_shader_bit_encoding");
        }
        return extensions.build();
    }

    public static void reload(ShaderSources sources, List<SourceComponent> vertexComponents, List<SourceComponent> fragmentComponents) {
        if (!GlCompat.SUPPORTS_INSTANCING) {
            return;
        }
        try {
            var createMethod = PipelineCompiler.class.getDeclaredMethod("create", ShaderSources.class, Pipeline.class, List.class, List.class, Collection.class);
            // ignore package private
            createMethod.setAccessible(true);
            var pipelineCompiler = (PipelineCompiler) createMethod.invoke(null, sources, IrisFlwPipelines.IRIS_INSTANCING, vertexComponents, fragmentComponents, EXTENSIONS);
            var fullscreen = OitPrograms.createFullscreenCompiler(sources);
            IrisInstancingPrograms newInstance = new IrisInstancingPrograms(pipelineCompiler, fullscreen);

            setInstance(newInstance);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setInstance(@Nullable IrisInstancingPrograms newInstance) {
        if (instance != null) {
            instance.release();
        }
        if (newInstance != null) {
            newInstance.acquire();
        }
        instance = newInstance;
    }

    @Nullable
    public static IrisInstancingPrograms get() {
        return instance;
    }

    public static boolean allLoaded() {
        return instance != null;
    }

    public static void kill() {
        setInstance(null);
    }

    public GlProgram get(InstanceType<?> instanceType, ContextShader contextShader, Material material, PipelineCompiler.OitMode mode, boolean isShadow) {
        return pipeline.get(instanceType, contextShader, material, mode);
    }

    public OitPrograms oitPrograms() {
        return oitPrograms;
    }

    @Override
    protected void _delete() {
        pipeline.delete();
        oitPrograms.delete();
    }
}
