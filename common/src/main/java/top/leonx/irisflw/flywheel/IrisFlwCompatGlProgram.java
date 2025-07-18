package top.leonx.irisflw.flywheel;

import com.mojang.blaze3d.shaders.Uniform;
import dev.engine_room.flywheel.backend.gl.shader.ShaderType;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL20;
import top.leonx.irisflw.iris.GlUniformMcMatrix3f;
import top.leonx.irisflw.iris.GlUniformMcMatrix4f;

public class IrisFlwCompatGlProgram extends IrisFlwCompatGlProgramBase {
    public ShaderInstance shader;
    protected GlUniformMcMatrix4f uniformIrisProjMat;
    protected GlUniformMcMatrix4f iris_uniformModelViewMat;
    //protected GlUniformMcMatrix4f uniformModelViewMat;
    protected GlUniformMcMatrix3f uniformNormalMatrix;
    protected GlUniformMcMatrix4f uniformModelViewProjMat;

    public IrisFlwCompatGlProgram(ShaderInstance shader, ShaderType type, String name) {
        super(shader.getId());
        this.shader = shader;
        int progId = shader.getId();

        //ModelViewMat may be removed if the shader doesn't use it.
        //If the MODEL_VIEW_MATRIX is null, the game will crash when we call ExtendedShader::apply().
        if (shader.MODEL_VIEW_MATRIX == null) {
            shader.MODEL_VIEW_MATRIX = new Uniform("ModelViewMat", 10, 16, shader);
            shader.MODEL_VIEW_MATRIX.set(new Matrix4f());
        }

        uniformIrisProjMat = new GlUniformMcMatrix4f(GL20.glGetUniformLocation(progId, "iris_ProjMat"));
        iris_uniformModelViewMat = new GlUniformMcMatrix4f(GL20.glGetUniformLocation(progId, "iris_ModelViewMat"));
        uniformNormalMatrix = new GlUniformMcMatrix3f(GL20.glGetUniformLocation(progId, "iris_NormalMat"));
        uniformModelViewProjMat = new GlUniformMcMatrix4f(GL20.glGetUniformLocation(progId, "flw_ModelViewProjMat"));
    }

    public void bind() {
        shader.apply();
        if (RenderLayerEventStateManager.isRenderingShadow()) {
            setProjectionMatrix(ShadowRenderer.PROJECTION);
            setModelViewMatrix(ShadowRenderer.MODELVIEW);
        } else {
            setProjectionMatrix((Matrix4f) CapturedRenderingState.INSTANCE.getGbufferProjection());
            setModelViewMatrix((Matrix4f) CapturedRenderingState.INSTANCE.getGbufferModelView());
        }
    }

    public void clear() {
        shader.clear();
    }

    public int getProgramHandle() {
        return shader.getId();
    }

    public void setProjectionMatrix(Matrix4f projectionMatrix) {
        uniformIrisProjMat.set(projectionMatrix);
    }

    public void setModelViewMatrix(Matrix4f modelView) {
        iris_uniformModelViewMat.set(modelView);

        if (this.uniformNormalMatrix != null) {
            Matrix4f normalMatrix = new Matrix4f(modelView);
            normalMatrix.invert();
            normalMatrix.transpose();
            this.uniformNormalMatrix.set(new Matrix3f(normalMatrix));
        }
    }
}
