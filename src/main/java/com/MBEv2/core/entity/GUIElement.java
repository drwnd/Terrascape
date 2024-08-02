package com.MBEv2.core.entity;

import org.joml.Vector2f;

public class GUIElement {

    private final int vao, vertexCount;
    private final int vbo1, vbo2;
    private Texture texture;
    private Vector2f position;

    public GUIElement(int vao, int vertexCount, int vbo1, int vbo2, Vector2f position) {
        this.vao = vao;
        this.vbo1 = vbo1;
        this.vbo2 = vbo2;
        this.vertexCount = vertexCount;
        this.position = position;
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

    public Vector2f getPosition() {
        return position;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }
}
