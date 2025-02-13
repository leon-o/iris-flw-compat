package top.leonx.irisflw.flywheel;

import com.mojang.blaze3d.shaders.Uniform;
import org.joml.*;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL20;
import top.leonx.irisflw.iris.GlUniformMcMatrix3f;
import top.leonx.irisflw.iris.GlUniformMcMatrix4f;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class IrisFlwCompatShaderWarp {
    public ShaderInstance shader;
    protected GlUniformMcMatrix4f uniformIrisProjMat;
    protected GlUniformMcMatrix4f iris_uniformModelViewMat;
    //protected GlUniformMcMatrix4f uniformModelViewMat;
    protected GlUniformMcMatrix3f uniformNormalMatrix;
    protected GlUniformMcMatrix4f uniformModelViewProjMat;

    public IrisFlwCompatShaderWarp(ShaderInstance shader) {
        this.shader = shader;
        int progId = shader.getId();

        // Use reflection to modify the final field (not recommended)
        try {
            Field modelViewMatrixField = ShaderInstance.class.getDeclaredField("MODEL_VIEW_MATRIX");
            modelViewMatrixField.setAccessible(true);
            modelViewMatrixField.set(shader, new Uniform("ModelViewMat", 10, 16, shader));
        } catch (Exception e) {
            throw new RuntimeException("Failed to modify MODEL_VIEW_MATRIX field", e);
        }

        uniformIrisProjMat = new GlUniformMcMatrix4f(GL20.glGetUniformLocation(progId,"iris_ProjMat"));
        iris_uniformModelViewMat = new GlUniformMcMatrix4f(GL20.glGetUniformLocation(progId,"iris_ModelViewMat"));
        uniformNormalMatrix = new GlUniformMcMatrix3f(GL20.glGetUniformLocation(progId,"iris_NormalMat"));
        uniformModelViewProjMat = new GlUniformMcMatrix4f(GL20.glGetUniformLocation(progId,"flw_ModelViewProjMat"));
    }

    public void bind() {
        shader.apply();
        setProjectionMatrix(CapturedRenderingState.INSTANCE.getGbufferProjection());
        setModelViewMatrix(CapturedRenderingState.INSTANCE.getGbufferModelView());
    }

    public void unbind(){
        shader.clear();
    }

    public int getProgramHandle(){
        return shader.getId();
    }

    public void setProjectionMatrix(Matrix4fc projectionMatrix) {
        uniformIrisProjMat.set((Matrix4f) projectionMatrix);
    }

    public void setModelViewMatrix(Matrix4fc modelView) {
        iris_uniformModelViewMat.set((Matrix4f) modelView);

        if (this.uniformNormalMatrix != null) {
            Matrix4f normalMatrix = new Matrix4f(modelView);
            normalMatrix.invert();
            normalMatrix.transpose();
            this.uniformNormalMatrix.set(new Matrix3f(normalMatrix));
        }
    }
}