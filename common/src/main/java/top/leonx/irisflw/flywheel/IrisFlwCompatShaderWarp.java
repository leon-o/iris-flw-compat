package top.leonx.irisflw.flywheel;

import com.mojang.blaze3d.shaders.Uniform;
import org.joml.*;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL20;
import top.leonx.irisflw.iris.GlUniformMcMatrix3f;
import top.leonx.irisflw.iris.GlUniformMcMatrix4f;

public class IrisFlwCompatShaderWarp {
    public ShaderInstance shader;
    protected GlUniformMcMatrix4f uniformIrisProjMat;
    protected GlUniformMcMatrix4f iris_uniformModelViewMat;
    protected GlUniformMcMatrix3f uniformNormalMatrix;
    protected GlUniformMcMatrix4f uniformModelViewProjMat;

    public IrisFlwCompatShaderWarp(ShaderInstance shader) {
        this.shader = shader;
        int progId = shader.getId();

        // Ensure MODEL_VIEW_MATRIX is properly initialized
        if (shader.MODEL_VIEW_MATRIX == null) {
            shader.MODEL_VIEW_MATRIX = new Uniform("ModelViewMat", 10, 16, shader);
            shader.MODEL_VIEW_MATRIX.set(new Matrix4f().identity());
        }

        uniformIrisProjMat = new GlUniformMcMatrix4f(GL20.glGetUniformLocation(progId, "iris_ProjMat"));
        iris_uniformModelViewMat = new GlUniformMcMatrix4f(GL20.glGetUniformLocation(progId, "iris_ModelViewMat"));
        uniformNormalMatrix = new GlUniformMcMatrix3f(GL20.glGetUniformLocation(progId, "iris_NormalMat"));
        uniformModelViewProjMat = new GlUniformMcMatrix4f(GL20.glGetUniformLocation(progId, "flw_ModelViewProjMat"));
    }

    public void bind() {
        shader.apply();

        // Enable depth testing and depth writing
        GL20.glEnable(GL20.GL_DEPTH_TEST);
        GL20.glDepthMask(true);

        // Set projection and model-view matrices
        setProjectionMatrix(CapturedRenderingState.INSTANCE.getGbufferProjection());
        setModelViewMatrix(CapturedRenderingState.INSTANCE.getGbufferModelView());

        // Ensure Shader uniform variables are set
        updateShaderUniforms();
    }

    public void unbind() {
        shader.clear();

        // Restore depth testing and depth writing state
        GL20.glDepthMask(false);
        GL20.glDisable(GL20.GL_DEPTH_TEST);
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

        // Ensure MODEL_VIEW_MATRIX is updated
        shader.MODEL_VIEW_MATRIX.set(new Matrix4f(modelView));
    }

    private void updateShaderUniforms() {
        // Ensure all Shader uniform variables are set
        if (uniformIrisProjMat != null) {
            GL20.glUniformMatrix4fv(uniformIrisProjMat.getLocation(), false, uniformIrisProjMat.getMatrix().get(new float[16]));
        }
        if (iris_uniformModelViewMat != null) {
            GL20.glUniformMatrix4fv(iris_uniformModelViewMat.getLocation(), false, iris_uniformModelViewMat.getMatrix().get(new float[16]));
        }
        if (uniformNormalMatrix != null) {
            GL20.glUniformMatrix3fv(uniformNormalMatrix.getLocation(), false, uniformNormalMatrix.getMatrix().get(new float[9]));
        }
        if (uniformModelViewProjMat != null) {
            GL20.glUniformMatrix4fv(uniformModelViewProjMat.getLocation(), false, uniformModelViewProjMat.getMatrix().get(new float[16]));
        }
    }
}
