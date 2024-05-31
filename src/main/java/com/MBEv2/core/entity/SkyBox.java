package com.MBEv2.core.entity;

import org.joml.Vector3f;

public class SkyBox {

    private final int id, vertexCount;
    private Texture texture1;
    private Texture texture2;
    private Vector3f position;

    public SkyBox(int id, int vertexCount, Vector3f position) {
        this.id = id;
        this.vertexCount = vertexCount;
        this.position = position;
    }

    public int getId() {
        return id;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public Texture getTexture1() {
        return texture1;
    }

    public Texture getTexture2(){
        return texture2;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setTexture(Texture texture1, Texture texture2) {
        this.texture1 = texture1;
        this.texture2 = texture2;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }
}
