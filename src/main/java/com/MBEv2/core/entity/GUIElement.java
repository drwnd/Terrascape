package com.MBEv2.core.entity;

public class GUIElement {

    private final int vao, vertexCount;
    private final int vbo1, vbo2;
    private Texture texture;

    public GUIElement(int vao, int vertexCount, int vbo1, int vbo2) {
        this.vao = vao;
        this.vbo1 = vbo1;
        this.vbo2 = vbo2;
        this.vertexCount = vertexCount;
    }

    public int getVao() {
        return vao;
    }

    public int getVbo1() {
        return vbo1;
    }

    public int getVbo2() {
        return vbo2;
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
