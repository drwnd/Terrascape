package terrascape.entity;

import org.joml.Vector3i;

public final class WaterModel {

    public final int vao, vbo;
    public final int X, Y, Z;
    public final int vertexCount;

    public WaterModel(int vao, int vertexCount, Vector3i position, int vbo) {
        this.vao = vao;
        this.vertexCount = vertexCount;
        X = position.x;
        Y = position.y;
        Z = position.z;
        this.vbo = vbo;
    }
}

