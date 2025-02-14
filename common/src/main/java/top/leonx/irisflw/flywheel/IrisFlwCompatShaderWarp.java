package top.leonx.irisflw.flywheel;

import com.mojang.blaze3d.shaders.Uniform;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL20;

import net.minecraft.client.renderer.ShaderInstance;
import top.leonx.irisflw.iris.GlUniformMcMatrix3f;
import top.leonx.irisflw.iris.GlUniformMcMatrix4f;

import java.lang.reflect.Field;

public class IrisFlwCompatShaderWarp {
    public ShaderInstance shader;
    protected GlUniformMcMatrix3f iris_uniformNormalMatrix;
    protected GlUniformMcMatrix4f uniformIrisProjMat;
    protected GlUniformMcMatrix4f iris_uniformModelViewMat;
    protected GlUniformMcMatrix4f flw_uniformModelViewProjMat;
    private static final Field MODEL_VIEW_MATRIX;

    public IrisFlwCompatShaderWarp(ShaderInstance shader) {
        this.shader = shader;
        int progId = shader.getId();

        try {
            // 初始化MODEL_VIEW_MATRIX
            Uniform modelView = (Uniform) MODEL_VIEW_MATRIX.get(shader);
            if (modelView == null) {
                // 使用原版着色器定义的Uniform名
                modelView = new Uniform("ModelViewMat", 10, 16, shader);
                modelView.set(new Matrix4f());
                MODEL_VIEW_MATRIX.set(shader, modelView); // Java 17允许修改非final字段
            }
        } catch (Exception e) {
            throw new RuntimeException("模型视图矩阵初始化失败", e);
        }

        // 统一使用原版Uniform命名
        uniformIrisProjMat = new GlUniformMcMatrix4f(GL20.glGetUniformLocation(progId, "iris_ProjMat"));
        iris_uniformModelViewMat = new GlUniformMcMatrix4f(GL20.glGetUniformLocation(progId, "iris_ModelViewMat"));
        iris_uniformNormalMatrix = new GlUniformMcMatrix3f(GL20.glGetUniformLocation(progId, "iris_NormalMat"));
        flw_uniformModelViewProjMat = new GlUniformMcMatrix4f(GL20.glGetUniformLocation(progId, "flw_ModelViewProjMat"));

        // 防御性校验
        if (iris_uniformModelViewMat.getIndex() == -1) {
            throw new IllegalStateException("必需Uniform iris_ModelViewMat 未找到");
        }
    }

    public void bind() {
        shader.apply();

        // 显式上传关键矩阵（Java 17需要更严格的状态管理）
        shader.MODEL_VIEW_MATRIX.upload();
        shader.PROJECTION_MATRIX.upload();

        setProjectionMatrix(CapturedRenderingState.INSTANCE.getGbufferProjection());
        setModelViewMatrix(CapturedRenderingState.INSTANCE.getGbufferModelView());
    }


    public void unbind() {
        shader.clear();
    }

    public int getProgramHandle() {
        return shader.getId();
    }

    public void setProjectionMatrix(Matrix4fc projectionMatrix) {
        uniformIrisProjMat.set(projectionMatrix);
    }

    public void setModelViewMatrix(Matrix4fc modelView) {
        iris_uniformModelViewMat.set(modelView);
        this.iris_uniformModelViewMat.set(modelView);
        if (this.iris_uniformNormalMatrix != null) {
            Matrix4f normalMatrix = new Matrix4f(modelView);
            normalMatrix.invert();
            normalMatrix.transpose();
            this.iris_uniformNormalMatrix.set(new Matrix3f(normalMatrix));
        }
    }

    static {
        try {
            MODEL_VIEW_MATRIX = ShaderInstance.class.getDeclaredField("f_173308_");
            MODEL_VIEW_MATRIX.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException("反射初始化失败，请检查MODEL_VIEW_MATRIX字段是否存在", e);
        }
    }

}