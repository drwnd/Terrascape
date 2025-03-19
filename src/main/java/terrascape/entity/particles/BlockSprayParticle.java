package terrascape.entity.particles;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import terrascape.player.ObjectLoader;
import terrascape.player.ShaderManager;
import terrascape.server.Block;

import static terrascape.utils.Constants.NANOSECONDS_PER_SECOND;
import static terrascape.utils.Constants.TOP;

public final class BlockSprayParticle extends Particle {

    private static int vao;
    private final byte textureIndex;

    public BlockSprayParticle(Vector3f position, short block) {
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
        return -0.006f;
    }

    @Override
    public float getMaxAliveTime() {
        return 1.5f * NANOSECONDS_PER_SECOND;
    }

    @Override
    public float getParticleSize() {
        return 0.125f;
    }

    public static void init() {
        vao = ObjectLoader.loadParticle(vertexOffsets(), vertexVelocities(), textureCoordinates());
    }

    private static float[] vertexOffsets() {
        float[] offsets = new float[972];
        int index = 0;

        for (int x = 0; x <= 8; x++)
            for (int z = 0; z <= 8; z++) {

                float offsetX = x * 0.125f - 0.5f;
                float offsetZ = z * 0.125f - 0.5f;

                for (int vertex = 0; vertex < 4; vertex++) {
                    offsets[index++] = offsetX;
                    offsets[index++] = 0.0f;
                    offsets[index++] = offsetZ;
                }
            }
        return offsets;
    }

    private static float[] vertexVelocities() {
        float[] velocities = new float[972];
        int index = 0;

        for (int x = 0; x <= 8; x++)
            for (int z = 0; z <= 8; z++) {

                float speedFactor = 0.04f / (float) Math.sqrt((x - 4) * (x - 4) + (z - 4) * (z - 4));

                for (int vertex = 0; vertex < 4; vertex++) {
                    velocities[index++] = (x - 4) * speedFactor;
                    velocities[index++] = 0.1f + speedFactor;
                    velocities[index++] = (z - 4) * speedFactor;
                }
            }

        return velocities;
    }

    private static float[] textureCoordinates() {
        float[] textureCoordinates = new float[648];
        int index = 0;

        for (int x = 0; x <= 8; x++)
            for (int z = 0; z <= 8; z++) {

                float textureU = x * 0.109f;
                float textureV = z * 0.109f;

                for (int vertex = 0; vertex < 4; vertex++) {
                    textureCoordinates[index++] = textureU;
                    textureCoordinates[index++] = textureV;
                }
            }

        return textureCoordinates;
    }
}
