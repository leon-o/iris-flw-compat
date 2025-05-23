package top.leonx.irisflw.backend.model;

import net.irisshaders.iris.vertices.views.TriView;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class CubiodBuilderTriView implements TriView {
    Vector3f[] pos;
    Vector2f[] uvs = new Vector2f[4];
    public void setup(Vector3f[] pos,float minU, float maxU, float minV, float maxV){
        this.pos = pos;
        uvs[0] = new Vector2f(maxU, minV);
        uvs[1] = new Vector2f(minU, minV);
        uvs[2] = new Vector2f(minU, maxV);
        uvs[3] = new Vector2f(maxU, maxV);
    }
    @Override
    public float x(int i) {
        return pos[i].x();
    }

    @Override
    public float y(int i) {
        return pos[i].y();
    }

    @Override
    public float z(int i) {
        return pos[i].z();
    }

    @Override
    public float u(int i) {
        return uvs[i].x;
    }

    @Override
    public float v(int i) {
        return uvs[i].y;
    }
}
