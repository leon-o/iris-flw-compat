package top.leonx.irisflw.flywheel;

import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.coderbot.iris.pipeline.newshader.ExtendedShader;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.ShaderAttributeInputs;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.math.Matrix4f;
import top.leonx.irisflw.iris.GlUniformMcMatrix4f;

import java.io.IOException;
import java.util.function.Consumer;

import static net.minecraft.client.gl.GlUniform.getUniformLocation;

public class IrisFlwCompatShaderWarp {
    public Shader shader;
    protected GlUniformMcMatrix4f uniformIrisProjMat;
    protected GlUniformMcMatrix4f uniformModelViewMat;
    protected GlUniformMcMatrix4f uniformNormalMatrix;
    protected GlUniformMcMatrix4f uniformModelViewProjMat;

    public IrisFlwCompatShaderWarp(Shader shader) {
        this.shader = shader;
        int progId = shader.getProgramRef();
        uniformIrisProjMat = new GlUniformMcMatrix4f(getUniformLocation(progId,"iris_ProjMat"));
        uniformModelViewMat = new GlUniformMcMatrix4f(getUniformLocation(progId,"iris_ModelViewMat"));
        uniformNormalMatrix = new GlUniformMcMatrix4f(getUniformLocation(progId,"flw_NormalMat"));
        uniformModelViewProjMat = new GlUniformMcMatrix4f(getUniformLocation(progId,"flw_ModelViewProjMat"));
    }

    public void bind() {
        shader.bind();
        setProjectionMatrix(CapturedRenderingState.INSTANCE.getGbufferProjection());
        setModelViewMatrix(CapturedRenderingState.INSTANCE.getGbufferModelView());
    }

    public void unbind(){
        shader.unbind();
    }

    public int getProgramHandle(){
        return shader.getProgramRef();
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
