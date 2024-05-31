package com.MBEv2.core.entity;

public class GUIElement {

    private final int id, vertexCount;
    private Texture texture;

    public GUIElement(int id, int vertexCount) {
        this.id = id;
        this.vertexCount = vertexCount;
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
}
