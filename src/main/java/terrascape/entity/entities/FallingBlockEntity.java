package terrascape.entity.entities;

import org.joml.Vector3f;
import terrascape.dataStorage.Chunk;
import terrascape.server.Block;
import terrascape.server.ServerLogic;
import terrascape.utils.Utils;

import static terrascape.utils.Constants.*;

public final class FallingBlockEntity extends Entity {

    public FallingBlockEntity(Vector3f position, Vector3f velocity) {
        this(position, velocity, Chunk.getBlockInWorld(Utils.floor(position.x), Utils.floor(position.y), Utils.floor(position.z)));
    }

    public FallingBlockEntity(Vector3f position, Vector3f velocity, short block) {
        this.position = position;
        this.velocity = velocity;
        this.block = block;

        if (Block.getBlockType(block) == FULL_BLOCK) aabb = FALLING_BLOCK_AABB;
        else {
            byte[] XYZSubData = Block.getXYZSubData(block);
            float[] aabb = new float[XYZSubData.length];
            for (int index = 0; index < XYZSubData.length; index += 2) {
                aabb[index] = XYZSubData[index] * 0.0625f - 0.5f;
                aabb[index + 1] = XYZSubData[index + 1] * 0.0625f + 0.5f;
            }
            this.aabb = aabb;
        }
        initRotationArrays(aabb.length / 3);
        textures = new short[aabb.length];

        for (int aabbIndex = 0; aabbIndex < aabb.length; aabbIndex += 6) {
            textures[aabbIndex + NORTH] = getTexture(block, NORTH, aabbIndex);
            textures[aabbIndex + TOP] = getTexture(block, TOP, aabbIndex);
            textures[aabbIndex + WEST] = getTexture(block, WEST, aabbIndex);
            textures[aabbIndex + SOUTH] = getTexture(block, SOUTH, aabbIndex);
            textures[aabbIndex + BOTTOM] = getTexture(block, BOTTOM, aabbIndex);
            textures[aabbIndex + EAST] = getTexture(block, EAST, aabbIndex);
        }
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
    public short getTextureUV(int side, int aabbIndex) {
        return textures[aabbIndex + side];
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

    static FallingBlockEntity getFromBytesCustom(byte[] bytes, int startIndex) {
        short block = Utils.getShort(bytes, startIndex);

        return new FallingBlockEntity(null, null, block);
    }

    private static short getTexture(short block, int side, int aabbIndex) {
        int textureIndex = Block.getTextureIndex(block, side);
        int blockType = Block.getBlockType(block);
        int u = ((textureIndex & 15) << 4) + Block.getSubU(blockType, side, getCorner(side), aabbIndex);
        int v = ((textureIndex >> 4 & 15) << 4) + Block.getSubV(blockType, side, getCorner(side), aabbIndex);

        return (short) ((u & 0xFF) << 8 | (v & 0xFF));
    }

    private static int getCorner(int side) {
        return side == EAST ? 0 : 1;
    }

    private static final float[] FALLING_BLOCK_AABB = new float[]{-0.5f, 0.5f, -0.5f, 0.5f, -0.5f, 0.5f};
    private final short block;
    private final short[] textures;
}
