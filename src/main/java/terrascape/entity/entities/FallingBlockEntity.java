package terrascape.entity.entities;

import org.joml.Vector3f;
import terrascape.dataStorage.Chunk;
import terrascape.server.Block;
import terrascape.server.ServerLogic;
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

    public FallingBlockEntity(Vector3f position, Vector3f velocity, short block) {
        this.position = position;
        this.velocity = velocity;
        aabb = FALLING_BLOCK_AABB;
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
            ServerLogic.placeBlock(block, Utils.floor(position.x), Utils.floor(position.y), Utils.floor(position.z), false);
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

    @Override
    public byte[] toBytes() {
        byte[] bytes = new byte[getByteSize()];
        bytes[0] = FALLING_BLOCK_ENTITY_TYPE;
        putBaseByteData(bytes);
        System.arraycopy(Utils.toByteArray(block), 0, bytes, BASE_BYTE_SIZE, 2);
        return bytes;
    }

    @Override
    public int getByteSize() {
        return BASE_BYTE_SIZE + 2;
    }

    protected static FallingBlockEntity getFromBytesCustom(byte[] bytes, int startIndex) {
        short block = Utils.getShort(bytes, startIndex);

        return new FallingBlockEntity(null, null, block);
    }
}
