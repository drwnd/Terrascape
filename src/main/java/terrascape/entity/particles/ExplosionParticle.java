package terrascape.entity.particles;

import terrascape.player.ObjectLoader;
import terrascape.player.ShaderManager;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import static terrascape.utils.Constants.*;

public class ExplosionParticle extends Particle {

    public ExplosionParticle(Vector3f position) {
        super(position);
    }

    @Override
    protected void renderUnique(ShaderManager shader, int modelIndexBuffer) {
        shader.setUniform("textureOffset_", 6.0f, 15.0f);

        GL30.glBindVertexArray(vao);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, modelIndexBuffer);

        GL11.glDrawElements(GL11.GL_TRIANGLES, 3072, GL11.GL_UNSIGNED_INT, 0);

    }

    @Override
    public float getGravity() {
        return 0;
    }

    @Override
    public float getMaxAliveTime() {
        return 0.25f * NANOSECONDS_PER_SECOND;
    }

    @Override
    public float getParticleSize() {
        return 0.25f;
    }

    public static void init() {
        vao = ObjectLoader.loadParticle(vertexOffsets(), vertexVelocities(), textureCoordinates());
    }

    public static float[] vertexOffsets() {
        return BlockBreakParticle.vertexOffsets();
    }

    public static float[] vertexVelocities() {
        float[] velocities = new float[6144];
        int index = 0;

        for (int x = 0; x < 8; x++)
            for (int y = 0; y < 8; y++)
                for (int z = 0; z < 8; z++) {

                    float velocityX = (float) (Math.random() * 2.0 - 1.0);
                    float velocityY = (float) (Math.random() * 2.0 - 1.0);
                    float velocityZ = (float) (Math.random() * 2.0 - 1.0);

                    for (int vertex = 0; vertex < 4; vertex++) {
                        velocities[index++] = velocityX * 2.0f;
                        velocities[index++] = velocityY * 2.0f;
                        velocities[index++] = velocityZ * 2.0f;
                    }
                }

        return velocities;
    }

    public static float[] textureCoordinates() {
        return BlockBreakParticle.textureCoordinates();
    }

    private static int vao;
}
