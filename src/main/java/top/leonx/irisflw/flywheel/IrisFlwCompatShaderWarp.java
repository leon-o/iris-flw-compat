package top.leonx.irisflw.flywheel;

import com.mojang.math.Matrix4f;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL20;
import top.leonx.irisflw.iris.GlUniformMcMatrix4f;

public class IrisFlwCompatShaderWarp {
    public ShaderInstance shader;
    protected GlUniformMcMatrix4f uniformIrisProjMat;
    protected GlUniformMcMatrix4f uniformModelViewMat;
    protected GlUniformMcMatrix4f uniformNormalMatrix;
    protected GlUniformMcMatrix4f uniformModelViewProjMat;

    public IrisFlwCompatShaderWarp(ShaderInstance shader) {
        this.shader = shader;
        int progId = shader.getId();
        uniformIrisProjMat = new GlUniformMcMatrix4f(GL20.glGetUniformLocation(progId,"iris_ProjMat"));
        uniformModelViewMat = new GlUniformMcMatrix4f(GL20.glGetUniformLocation(progId,"iris_ModelViewMat"));
        uniformNormalMatrix = new GlUniformMcMatrix4f(GL20.glGetUniformLocation(progId,"flw_NormalMat"));
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
    public void setProjectionMatrix(Matrix4f projectionMatrix){
        uniformIrisProjMat.set(projectionMatrix);
    }

    public void setModelViewMatrix(Matrix4f modelView) {
        uniformModelViewMat.set(modelView);

        if (this.uniformNormalMatrix != null) {
            Matrix4f normalMatrix = new Matrix4f(modelView);
            normalMatrix.invert();
            normalMatrix.transpose();
            this.uniformNormalMatrix.set(normalMatrix);
        }
    }
}
