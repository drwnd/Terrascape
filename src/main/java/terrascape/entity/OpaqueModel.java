package terrascape.entity;

import org.joml.Vector3i;

public class OpaqueModel {

    private final int vao, vbo;
    private final int solidVertexCount, foliageVertexCount, decorationVertexCount;
    private final Vector3i position;

    public OpaqueModel(int vao, int solidVertexCount, int foliageVertexCount, int decorationVertexCount, Vector3i pos, int vbo) {
        this.vao = vao;
        this.solidVertexCount = solidVertexCount;
        this.foliageVertexCount = foliageVertexCount;
        this.decorationVertexCount = decorationVertexCount;
        this.position = pos;
        this.vbo = vbo;
    }

    public int getVao() {
        return vao;
    }

    public int getSolidVertexCount() {
        return solidVertexCount;
    }

    public int getFoliageVertexCount() {
        return foliageVertexCount;
    }

    public int getDecorationVertexCount() {
        return decorationVertexCount;
    }

    public Vector3i getPosition() {
        return position;
    }

    public int getVbo() {
        return vbo;
    }
}
