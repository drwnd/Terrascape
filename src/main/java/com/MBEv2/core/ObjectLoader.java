package com.MBEv2.core;

import com.MBEv2.core.entity.GUIElement;
import com.MBEv2.core.entity.Model;
import com.MBEv2.core.entity.SkyBox;
import com.MBEv2.core.utils.Utils;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class ObjectLoader {

    public static Model loadModel(int[] vertices, Vector3i position) {
        int vao = createVAO();
        int vbo = storeDateInAttributeList(2, vertices);
        unbind();
        return new Model(vao, vertices.length, position, vbo);
    }

    public static long loadVAO_VBO(int[] vertices) {
        int vao = createVAO();
        int vbo = storeDateInAttributeList(2, vertices);
        unbind();
        return (long) vao << 32 | vbo;
    }

    public static SkyBox loadSkyBox(float[] vertices, float[] textureCoordinates, int[] indices, Vector3f position) {
        int vao = createVAO();
        storeIndicesInBuffer(indices);
        storeDateInAttributeList(0, 3, vertices);
        storeDateInAttributeList(1, 2, textureCoordinates);
        unbind();
        return new SkyBox(vao, indices.length, position);
    }

    public static GUIElement loadGUIElement(float[] vertices, float[] textureCoordinates, Vector2f position) {
        int vao = createVAO();
        int vbo1 = storeDateInAttributeList(0, 2, vertices);
        int vbo2 = storeDateInAttributeList(1, 2, textureCoordinates);
        unbind();
        return new GUIElement(vao, vertices.length, vbo1, vbo2, position);
    }

    public static int loadTexture(String filename) throws Exception {
        int width, height;
        ByteBuffer buffer;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer c = stack.mallocInt(1);

            buffer = STBImage.stbi_load(filename, w, h, c, 4);
            if (buffer == null)
                throw new Exception("Image FIle " + filename + " not loaded " + STBImage.stbi_failure_reason());

            width = w.get();
            height = h.get();
        }

        int id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        STBImage.stbi_image_free(buffer);
        return id;
    }

    public static int loadTextRow() {
        int vao = createVAO();

        int[] textData = new int[256];
        for (int i = 0; i < textData.length; i += 4) {
            textData[i] = i >> 2;
            textData[i + 1] = i >> 2 | 128;
            textData[i + 2] = i >> 2 | 256;
            textData[i + 3] = i >> 2 | 384;
        }
        storeDateInAttributeList(1, textData);
        unbind();

        return vao;
    }

    private static int createVAO() {
        int vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);
        return vao;
    }

    private static void storeIndicesInBuffer(int[] indices) {
        int vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbo);
        IntBuffer buffer = Utils.storeDateInIntBuffer(indices);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
    }

    private static int storeDateInAttributeList(int attributeNo, int size, float[] data) {
        int vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        FloatBuffer buffer = Utils.storeDateInFloatBuffer(data);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(attributeNo, size, GL15.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        return vbo;
    }

    private static int storeDateInAttributeList(int size, int[] data) {
        int vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
        GL30.glVertexAttribIPointer(0, size, GL15.GL_INT, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        return vbo;
    }

    private static void unbind() {
        GL30.glBindVertexArray(0);
    }

    public static void removeVAO(int vao) {
        GL30.glBindVertexArray(vao);
        GL30.glDeleteVertexArrays(vao);
    }

    public static void removeVBO(int vbo) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL30.glDeleteBuffers(vbo);
    }

//    public static void removeTexture(int texture) {
//        GL15.glBindTexture(GL15.GL_TEXTURE_2D, texture);
//        GL11.glDeleteTextures(texture);
//    }

    public static void cleanUp() {

    }
}
