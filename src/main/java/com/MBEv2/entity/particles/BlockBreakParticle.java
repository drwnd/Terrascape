package com.MBEv2.entity.particles;

import com.MBEv2.core.Block;
import com.MBEv2.core.ObjectLoader;
import com.MBEv2.core.ShaderManager;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import static com.MBEv2.utils.Constants.*;

public class BlockBreakParticle extends Particle {

    private static int vao;
    private final byte textureIndex;

    public BlockBreakParticle(Vector3f position, short block) {
        super(position);
        textureIndex = (byte) Block.getTextureIndex(block, TOP);
    }

    @Override
    protected void renderUnique(ShaderManager shader, int modelIndexBuffer) {

        shader.setUniform("textureOffset_", (float) (textureIndex & 15), (float) (textureIndex >> 4 & 15));

        GL30.glBindVertexArray(vao);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, modelIndexBuffer);

        GL11.glDrawElements(GL11.GL_TRIANGLES, 3072, GL11.GL_UNSIGNED_INT, 0);
    }

    @Override
    public float getGravity() {
        return -0.008f;
    }


    @Override
    public float getMaxAliveTime() {
        return 2 * NANOSECONDS_PER_SECOND;
    }

    @Override
    public float getParticleSize() {
        return 0.125f;
    }

    public static void init() {
        vao = ObjectLoader.loadParticle(vertexOffsets(), vertexVelocities(), textureCoordinates());
    }

    public static float[] vertexOffsets() {
        float[] offsets = new float[6144];
        int index = 0;

        for (int x = 0; x < 8; x++)
            for (int y = 0; y < 8; y++)
                for (int z = 0; z < 8; z++) {

                    float offsetX = x * 0.125f - 0.4375f;
                    float offsetY = y * 0.125f - 0.4375f;
                    float offsetZ = z * 0.125f - 0.4375f;

                    for (int vertex = 0; vertex < 4; vertex++) {
                        offsets[index++] = offsetX;
                        offsets[index++] = offsetY;
                        offsets[index++] = offsetZ;
                    }
                }
        return offsets;
    }

    public static float[] vertexVelocities() {
        float[] velocities = new float[6144];
        int index = 0;

        for (int x = 0; x < 8; x++)
            for (int y = 0; y < 8; y++)
                for (int z = 0; z < 8; z++) {

                    float velocityX = (float) (Math.random() * 2.0 - 1.0);
                    float velocityY = (float) (Math.random() * 2.0);
                    float velocityZ = (float) (Math.random() * 2.0 - 1.0);

                    for (int vertex = 0; vertex < 4; vertex++) {
                        velocities[index++] = velocityX * 0.05f;
                        velocities[index++] = velocityY * 0.05f;
                        velocities[index++] = velocityZ * 0.05f;
                    }
                }

        return velocities;
    }

    public static float[] textureCoordinates() {
        float[] textureCoordinates = new float[4096];
        int index = 0;

        for (int x = 0; x < 8; x++)
            for (int y = 7; y >= 0; y--)
                for (int z = 0; z < 8; z++) {

                    float textureU = x * 0.125f;
                    float textureV = y * 0.125f;

                    for (int vertex = 0; vertex < 4; vertex++) {
                        textureCoordinates[index++] = textureU;
                        textureCoordinates[index++] = textureV;
                    }
                }

        return textureCoordinates;
    }
}
