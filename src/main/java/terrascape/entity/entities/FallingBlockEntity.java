package terrascape.entity.entities;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import terrascape.dataStorage.Chunk;
import terrascape.player.ObjectLoader;
import terrascape.player.ShaderManager;
import terrascape.server.Block;
import terrascape.server.GameLogic;
import terrascape.utils.Utils;

import static terrascape.utils.Constants.*;

public class FallingBlockEntity extends Entity {

    private static final float[] FALLING_BLOCK_AABB = new float[]{-0.5f, 0.5f, -0.5f, 0.5f, -0.5f, 0.5f};
    public int vao, vbo;
    private final short block;

    public FallingBlockEntity(Vector3f position, Vector3f velocity) {
        this.position = position;
        this.velocity = velocity;
        aabb = FALLING_BLOCK_AABB;

        short block = Chunk.getBlockInWorld(Utils.floor(position.x), Utils.floor(position.y), Utils.floor(position.z));
        this.block = block;

        long vao_vbo = ObjectLoader.loadVAO_VBO(0, 2, getFallingBlockVertices(block));
        vao = (int) (vao_vbo >> 32 & 0xFFFFFFFFL);
        vbo = (int) (vao_vbo & 0xFFFFFFFFL);
    }

    public static void init() {

    }

    @Override
    public void update() {
        move();

        if (velocity.y != 0.0f) return;

        isDead = true;
        short previousBlock = Chunk.getBlockInWorld(Utils.floor(position.x), Utils.floor(position.y), Utils.floor(position.z));
        if ((Block.getBlockProperties(previousBlock) & REPLACEABLE) != 0)
            GameLogic.placeBlock(block, Utils.floor(position.x), Utils.floor(position.y), Utils.floor(position.z), false);
//        else
//            // TODO Spawn item

    }

    @Override
    protected void renderUnique(ShaderManager shader, int modelIndexBuffer, float timeSinceLastTick) {
        GL30.glBindVertexArray(vao);
        GL20.glEnableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, modelIndexBuffer);

        GL11.glDrawElements(GL11.GL_TRIANGLES, 36, GL11.GL_UNSIGNED_INT, 0);
    }

    @Override
    public void delete() {
        ObjectLoader.removeVAO(vao);
        ObjectLoader.removeVBO(vbo);
        isDead = true;
    }

    private static int[] getFallingBlockVertices(short block) {
        int northTexture = Block.getTextureIndex(block, NORTH);

        int sideTL = Byte.toUnsignedInt((byte) northTexture);
        int sideTR = Byte.toUnsignedInt((byte) northTexture) + 1;
        int sideBL = Byte.toUnsignedInt((byte) northTexture) + 16;
        int sideBR = Byte.toUnsignedInt((byte) northTexture) + 17;

        return new int[]{
                packData((sideTL & 15) << 4, (sideTL >> 4) << 4), packData(8, 8, 8),
                packData((sideBL & 15) << 4, (sideBL >> 4) << 4), packData(8, 8, -8),
                packData((sideTR & 15) << 4, (sideTR >> 4) << 4), packData(-8, 8, 8),
                packData((sideBR & 15) << 4, (sideBR >> 4) << 4), packData(-8, 8, -8),

                packData((sideTL & 15) << 4, (sideTL >> 4) << 4), packData(8, -8, 8),
                packData((sideTR & 15) << 4, (sideTR >> 4) << 4), packData(-8, -8, 8),
                packData((sideBL & 15) << 4, (sideBL >> 4) << 4), packData(8, -8, -8),
                packData((sideBR & 15) << 4, (sideBR >> 4) << 4), packData(-8, -8, -8),

                packData((sideTL & 15) << 4, (sideTL >> 4) << 4), packData(8, 8, 8),
                packData((sideBL & 15) << 4, (sideBL >> 4) << 4), packData(8, -8, 8),
                packData((sideTR & 15) << 4, (sideTR >> 4) << 4), packData(8, 8, -8),
                packData((sideBR & 15) << 4, (sideBR >> 4) << 4), packData(8, -8, -8),

                packData((sideTL & 15) << 4, (sideTL >> 4) << 4), packData(-8, 8, 8),
                packData((sideBL & 15) << 4, (sideBL >> 4) << 4), packData(-8, -8, 8),
                packData((sideTR & 15) << 4, (sideTR >> 4) << 4), packData(8, 8, 8),
                packData((sideBR & 15) << 4, (sideBR >> 4) << 4), packData(8, -8, 8),

                packData((sideTL & 15) << 4, (sideTL >> 4) << 4), packData(-8, 8, -8),
                packData((sideBL & 15) << 4, (sideBL >> 4) << 4), packData(-8, -8, -8),
                packData((sideTR & 15) << 4, (sideTR >> 4) << 4), packData(-8, 8, 8),
                packData((sideBR & 15) << 4, (sideBR >> 4) << 4), packData(-8, -8, 8),

                packData((sideTL & 15) << 4, (sideTL >> 4) << 4), packData(8, 8, -8),
                packData((sideBL & 15) << 4, (sideBL >> 4) << 4), packData(8, -8, -8),
                packData((sideTR & 15) << 4, (sideTR >> 4) << 4), packData(-8, 8, -8),
                packData((sideBR & 15) << 4, (sideBR >> 4) << 4), packData(-8, -8, -8),
        };
    }
}
