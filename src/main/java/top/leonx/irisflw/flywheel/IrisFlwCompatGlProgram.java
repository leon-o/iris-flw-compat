package top.leonx.irisflw.flywheel;

import com.mojang.blaze3d.shaders.Uniform;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.backend.engine.MaterialEncoder;
import dev.engine_room.flywheel.backend.gl.shader.ShaderType;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformInt;
import net.irisshaders.iris.mixin.texture.TextureAtlasAccessor;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL20;
import top.leonx.irisflw.IrisFlw;
import top.leonx.irisflw.iris.*;

public class IrisFlwCompatGlProgram extends IrisFlwCompatGlProgramBase {
    public ShaderInstance shader;
    protected GlUniformMcMatrix4f uniformIrisProjMat;
    protected GlUniformMcMatrix4f iris_uniformModelViewMat;
    //protected GlUniformMcMatrix4f uniformModelViewMat;
    protected GlUniformMcMatrix3f uniformNormalMatrix;
    protected GlUniformMcMatrix4f uniformModelViewProjMat;
    protected GlUniformInt2v uniformAtlasSize;
    protected GlUniformUInt uniformFlwBaseVertex;
    protected GlUniformInt uniformFlwInstance;
    protected GlUniformUInt2v uniformPackedMaterial;
//    protected GlUniformInt uniformBlockEntity;
//    protected GlUniformInt uniformEntity;

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
        uniformAtlasSize = new GlUniformInt2v(GL20.glGetUniformLocation(progId, "atlasSize"));
        uniformFlwBaseVertex = new GlUniformUInt(GL20.glGetUniformLocation(progId, "_flw_baseVertex"));
        uniformFlwInstance = new GlUniformInt(GL20.glGetUniformLocation(progId, "_flw_baseInstance"));
        uniformPackedMaterial = new GlUniformUInt2v(GL20.glGetUniformLocation(progId, "_flw_packedMaterial"));
        // todo: add these uniforms
        // uniformBlockEntity = new GlUniformInt(GL20.glGetUniformLocation(progId, "blockEntityId"));
        // uniformEntity = new GlUniformInt(GL20.glGetUniformLocation(progId, "entityId"));
    }

    private Material material;
    private int baseVertex;
    private int baseInstance;

    @Override
    public void setAdditionUniform(Material material, int baseVertex, int baseInstance) {
        this.material = material;
        this.baseVertex = baseVertex;
        this.baseInstance = baseInstance;
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

        uniformFlwBaseVertex.set(baseVertex);
        uniformFlwInstance.set(baseInstance);

        if(material != null)
        {
            var tex = Minecraft.getInstance().getTextureManager().getTexture(material.texture());
            if (tex instanceof TextureAtlas atlas)
            {
                uniformAtlasSize.set(
                        ((TextureAtlasAccessor) atlas).callGetWidth(),
                        ((TextureAtlasAccessor) atlas).callGetHeight()
                );
            }else{
                uniformAtlasSize.set(2048, 2048);
            }

            int packedFogAndCutout = MaterialEncoder.packUberShader(material);
            int packedMaterialProperties = MaterialEncoder.packProperties(material);
            uniformPackedMaterial.set(packedFogAndCutout, packedMaterialProperties);
        }else{
            uniformAtlasSize.set(2048, 2048);
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
