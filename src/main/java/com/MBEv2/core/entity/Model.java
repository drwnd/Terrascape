package com.MBEv2.core.entity;

import org.joml.Vector3i;

public class Model {

    private final int id, vbo;
    private final int vertexCount;
    private final Vector3i position;
    private Texture texture;

    public Model(int id, int vertexCount, Vector3i pos, int vbo) {
        this.id = id;
        this.vertexCount = vertexCount;
        this.position = pos;
        this.vbo = vbo;
    }

    public int getId() {
        return id;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public Vector3i getPosition(){
        return position;
    }

    public int getVbo() {
        return vbo;
    }
}
