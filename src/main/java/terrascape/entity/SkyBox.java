package terrascape.entity;

import org.joml.Vector3f;

public final class SkyBox {

    public SkyBox(int vao, int vertexCount, Vector3f position) {
        this.vao = vao;
        this.vertexCount = vertexCount;
        this.position = position;
    }

    public int getVao() {
        return vao;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public Texture getTexture1() {
        return texture1;
    }

    public Texture getTexture2() {
        return texture2;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setTexture(Texture texture1, Texture texture2) {
        this.texture1 = texture1;
        this.texture2 = texture2;
    }

    private final int vao, vertexCount;
    private Texture texture1;
    private Texture texture2;
    private final Vector3f position;
}
