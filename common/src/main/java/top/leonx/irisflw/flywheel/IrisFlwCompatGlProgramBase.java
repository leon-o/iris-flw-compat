package top.leonx.irisflw.flywheel;

import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public abstract class IrisFlwCompatGlProgramBase extends GlProgram {
    public IrisFlwCompatGlProgramBase(int handle) {
        super(handle);
    }

    public abstract void bind();

    public abstract void clear();

    public abstract void setProjectionMatrix(Matrix4f projectionMatrix);

    public abstract void setModelViewMatrix(Matrix4f modelView);

    public static class Invalid extends IrisFlwCompatGlProgramBase {

        public static final Invalid INSTANCE = new Invalid();

        public Invalid() {
            super(0);
        }

        @Override
        public void bind() {

        }

        @Override
        public void clear() {

        }

        @Override
        public void setProjectionMatrix(Matrix4f projectionMatrix) {

        }

        @Override
        public void setModelViewMatrix(Matrix4f modelView) {

        }

        @Override
        public void delete() {
            // No-op for invalid program
        }
    }
}
