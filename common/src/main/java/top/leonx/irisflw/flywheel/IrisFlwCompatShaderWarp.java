package top.leonx.irisflw.flywheel;

import com.mojang.blaze3d.shaders.Uniform;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL20;
import top.leonx.irisflw.iris.GlUniformMcMatrix3f;
import top.leonx.irisflw.iris.GlUniformMcMatrix4f;

import java.lang.reflect.Field;

public class IrisFlwCompatShaderWarp {
    public ShaderInstance shader;
    protected GlUniformMcMatrix4f uniformIrisProjMat;
    protected GlUniformMcMatrix4f iris_uniformModelViewMat;
    protected GlUniformMcMatrix3f uniformNormalMatrix;
    protected GlUniformMcMatrix4f uniformModelViewProjMat;
    private static String MODEL_VIEW_MATRIX;

    public static Field getModelViewMatrixField() throws Exception {
        if (MODEL_VIEW_MATRIX == null) {
            synchronized (IrisFlwCompatShaderWarp.class) {
                if (MODEL_VIEW_MATRIX == null) {
                    if (isClassLoaded("net.minecraftforge.fml.loading.FMLLoader")) {
                        MODEL_VIEW_MATRIX = "f_173308_";
                    } else if (isClassLoaded("net.fabricmc.loader.impl.FabricLoaderImpl")) {
                        MODEL_VIEW_MATRIX = "field_29470";
                    } else {
                        throw new IllegalStateException("Unsupported mod loader");
                    }
                }
            }
        }

        Field field = ShaderInstance.class.getDeclaredField(MODEL_VIEW_MATRIX);
        field.setAccessible(true);
        return field;
    }

    private static boolean isClassLoaded(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    public IrisFlwCompatShaderWarp(ShaderInstance shader) {
        this.shader = shader;
        int progId = shader.getId();

        // 使用反射来修改 final 字段（不推荐）
        try {

            Field modelViewMatrixField = getModelViewMatrixField();
            modelViewMatrixField.setAccessible(true);
            Uniform modelViewMatrix = (Uniform) modelViewMatrixField.get(shader);

            if (modelViewMatrix == null) {
                modelViewMatrix = new Uniform("ModelViewMat", 10, 16, shader);
                modelViewMatrix.set(new Matrix4f());
                modelViewMatrixField.set(shader, modelViewMatrix);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to access MODEL_VIEW_MATRIX field", e);
        }

        uniformIrisProjMat = new GlUniformMcMatrix4f(GL20.glGetUniformLocation(progId, "iris_ProjMat"));
        iris_uniformModelViewMat = new GlUniformMcMatrix4f(GL20.glGetUniformLocation(progId, "iris_ModelViewMat"));
        uniformNormalMatrix = new GlUniformMcMatrix3f(GL20.glGetUniformLocation(progId, "iris_NormalMat"));
        uniformModelViewProjMat = new GlUniformMcMatrix4f(GL20.glGetUniformLocation(progId, "flw_ModelViewProjMat"));
    }

    public void bind() {
        GL20.glEnable(GL20.GL_DEPTH_TEST); // 启用深度测试
        shader.apply();
        setProjectionMatrix(CapturedRenderingState.INSTANCE.getGbufferProjection());
        setModelViewMatrix(CapturedRenderingState.INSTANCE.getGbufferModelView());
    }

    public void unbind() {
        shader.clear();
        GL20.glDisable(GL20.GL_DEPTH_TEST); // 禁用深度测试
    }

    public int getProgramHandle() {
        return shader.getId();
    }

    public void setProjectionMatrix(Matrix4fc projectionMatrix) {
        uniformIrisProjMat.set(projectionMatrix);
    }

    public void setModelViewMatrix(Matrix4fc modelView) {
        iris_uniformModelViewMat.set(modelView);

        if (uniformNormalMatrix != null) {
            Matrix4f normalMatrix = new Matrix4f(modelView);
            normalMatrix.invert();
            normalMatrix.transpose();
            uniformNormalMatrix.set(new Matrix3f(normalMatrix));
        }
    }
}
