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

    public IrisFlwCompatShaderWarp(ShaderInstance shader) {
        this.shader = shader;
        int progId = shader.getId();

        // 使用反射来修改 final 字段（不推荐）
        try {
            Field modelViewMatrixField = ShaderInstance.class.getDeclaredField("MODEL_VIEW_MATRIX");
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

        if (this.uniformNormalMatrix != null) {
            Matrix4f normalMatrix = new Matrix4f(modelView);
            normalMatrix.invert();
            normalMatrix.transpose();
            this.uniformNormalMatrix.set(new Matrix3f(normalMatrix));
        }
    }
}