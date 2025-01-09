package terrascape.entity.entities;

import org.joml.Vector3f;
import terrascape.dataStorage.Chunk;
import terrascape.server.Block;
import terrascape.server.GameLogic;
import terrascape.utils.Utils;

import static terrascape.utils.Constants.*;

public class FallingBlockEntity extends Entity {

    private static final float[] FALLING_BLOCK_AABB = new float[]{-0.5f, 0.5f, -0.5f, 0.5f, -0.5f, 0.5f};
    private final short block;
    private final byte topTexture, sideTexture, bottomTexture;

    public FallingBlockEntity(Vector3f position, Vector3f velocity) {
        this.position = position;
        this.velocity = velocity;
        aabb = FALLING_BLOCK_AABB;

        short block = Chunk.getBlockInWorld(Utils.floor(position.x), Utils.floor(position.y), Utils.floor(position.z));
        this.block = block;
        topTexture = (byte) Block.getTextureIndex(block, TOP);
        sideTexture = (byte) Block.getTextureIndex(block, NORTH);
        bottomTexture = (byte) Block.getTextureIndex(block, BOTTOM);
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
    public void delete() {
        isDead = true;
    }

    @Override
    public byte getTopTexture() {
        return topTexture;
    }

    @Override
    public byte getSideTexture() {
        return sideTexture;
    }

    @Override
    public byte getBottomTexture() {
        return bottomTexture;
    }
}
