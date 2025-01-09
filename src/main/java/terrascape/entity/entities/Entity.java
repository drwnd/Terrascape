package terrascape.entity.entities;

import terrascape.player.ObjectLoader;
import terrascape.server.*;
import terrascape.dataStorage.Chunk;
import terrascape.utils.Utils;
import org.joml.Vector3f;

import java.util.LinkedList;

import static terrascape.utils.Constants.*;

public abstract class Entity {

    protected Vector3f position;
    protected Vector3f velocity;
    protected float[] aabb;
    protected boolean isDead = false;
    public static int vao, vbo;

    public static void initAll() {
        long vao_vbo = ObjectLoader.loadVAO_VBO(0, 2, getEntityVertices());
        vao = (int) (vao_vbo >> 32 & 0xFFFFFFFFL);
        vbo = (int) (vao_vbo & 0xFFFFFFFFL);

    }

    public abstract void update();

    public abstract void delete();

    public abstract byte getTopTexture();

    public abstract byte getSideTexture();

    public abstract byte getBottomTexture();


    public void move() {
        velocity.mul(AIR_FRICTION);
        velocity.y -= GRAVITY_ACCELERATION;

        float intersection;

        position.x += velocity.x;
        intersection = getIntersectionX(position, aabb);
        position.x -= intersection;
        if (intersection != 0.0f) velocity.x = 0.0f;

        position.y += velocity.y;
        intersection = getIntersectionY(position, aabb);
        position.y -= intersection;
        if (intersection != 0.0f) velocity.y = 0.0f;

        position.z += velocity.z;
        intersection = getIntersectionZ(position, aabb);
        position.z -= intersection;
        if (intersection != 0.0f) velocity.z = 0.0f;
    }

    public static float getIntersectionX(Vector3f position, float[] aabb) {
        float intersection = 0.0f;

        float minX = aabb[MIN_X] + position.x;
        float maxX = aabb[MAX_X] + position.x;
        float minY = aabb[MIN_Y] + position.y;
        float maxY = aabb[MAX_Y] + position.y;
        float minZ = aabb[MIN_Z] + position.z;
        float maxZ = aabb[MAX_Z] + position.z;

        for (int x = Utils.floor(minX), maxBlockX = Utils.floor(maxX); x <= maxBlockX; x++)
            for (int y = Utils.floor(minY), maxBlockY = Utils.floor(maxY); y <= maxBlockY; y++)
                for (int z = Utils.floor(minZ), maxBlockZ = Utils.floor(maxZ); z <= maxBlockZ; z++) {

                    short block = Chunk.getBlockInWorld(x, y, z);
                    int blockProperties = Block.getBlockProperties(block);
                    if ((blockProperties & NO_COLLISION) != 0) continue;

                    byte[] blockXYZSubData = Block.getXYZSubData(block);
                    for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                        float blockMinX = x + blockXYZSubData[MIN_X + aabbIndex] * 0.0625f;
                        float blockMaxX = 1 + x + blockXYZSubData[MAX_X + aabbIndex] * 0.0625f;
                        float blockMinY = y + blockXYZSubData[MIN_Y + aabbIndex] * 0.0625f;
                        float blockMaxY = 1 + y + blockXYZSubData[MAX_Y + aabbIndex] * 0.0625f;
                        float blockMinZ = z + blockXYZSubData[MIN_Z + aabbIndex] * 0.0625f;
                        float blockMaxZ = 1 + z + blockXYZSubData[MAX_Z + aabbIndex] * 0.0625f;

                        if (minX < blockMaxX && maxX > blockMinX
                                && minY < blockMaxY && maxY > blockMinY
                                && minZ < blockMaxZ && maxZ > blockMinZ) {

                            intersection = Utils.absMax(intersection, Utils.absMin(minX - blockMaxX, maxX - blockMinX));
                        }
                    }
                }
        return intersection;
    }

    public static float getIntersectionY(Vector3f position, float[] aabb) {
        float intersection = 0.0f;

        float minX = aabb[MIN_X] + position.x;
        float maxX = aabb[MAX_X] + position.x;
        float minY = aabb[MIN_Y] + position.y;
        float maxY = aabb[MAX_Y] + position.y;
        float minZ = aabb[MIN_Z] + position.z;
        float maxZ = aabb[MAX_Z] + position.z;

        for (int x = Utils.floor(minX), maxBlockX = Utils.floor(maxX); x <= maxBlockX; x++)
            for (int y = Utils.floor(minY), maxBlockY = Utils.floor(maxY); y <= maxBlockY; y++)
                for (int z = Utils.floor(minZ), maxBlockZ = Utils.floor(maxZ); z <= maxBlockZ; z++) {

                    short block = Chunk.getBlockInWorld(x, y, z);
                    int blockProperties = Block.getBlockProperties(block);
                    if ((blockProperties & NO_COLLISION) != 0) continue;

                    byte[] blockXYZSubData = Block.getXYZSubData(block);
                    for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                        float blockMinX = x + blockXYZSubData[MIN_X + aabbIndex] * 0.0625f;
                        float blockMaxX = 1 + x + blockXYZSubData[MAX_X + aabbIndex] * 0.0625f;
                        float blockMinY = y + blockXYZSubData[MIN_Y + aabbIndex] * 0.0625f;
                        float blockMaxY = 1 + y + blockXYZSubData[MAX_Y + aabbIndex] * 0.0625f;
                        float blockMinZ = z + blockXYZSubData[MIN_Z + aabbIndex] * 0.0625f;
                        float blockMaxZ = 1 + z + blockXYZSubData[MAX_Z + aabbIndex] * 0.0625f;

                        if (minX < blockMaxX && maxX > blockMinX
                                && minY < blockMaxY && maxY > blockMinY
                                && minZ < blockMaxZ && maxZ > blockMinZ) {

                            intersection = Utils.absMax(intersection, Utils.absMin(minY - blockMaxY, maxY - blockMinY));
                        }
                    }
                }
        return intersection;
    }

    public static float getIntersectionZ(Vector3f position, float[] aabb) {
        float intersection = 0.0f;

        float minX = aabb[MIN_X] + position.x;
        float maxX = aabb[MAX_X] + position.x;
        float minY = aabb[MIN_Y] + position.y;
        float maxY = aabb[MAX_Y] + position.y;
        float minZ = aabb[MIN_Z] + position.z;
        float maxZ = aabb[MAX_Z] + position.z;

        for (int x = Utils.floor(minX), maxBlockX = Utils.floor(maxX); x <= maxBlockX; x++)
            for (int y = Utils.floor(minY), maxBlockY = Utils.floor(maxY); y <= maxBlockY; y++)
                for (int z = Utils.floor(minZ), maxBlockZ = Utils.floor(maxZ); z <= maxBlockZ; z++) {

                    short block = Chunk.getBlockInWorld(x, y, z);
                    int blockProperties = Block.getBlockProperties(block);
                    if ((blockProperties & NO_COLLISION) != 0) continue;

                    byte[] blockXYZSubData = Block.getXYZSubData(block);
                    for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                        float blockMinX = x + blockXYZSubData[MIN_X + aabbIndex] * 0.0625f;
                        float blockMaxX = 1 + x + blockXYZSubData[MAX_X + aabbIndex] * 0.0625f;
                        float blockMinY = y + blockXYZSubData[MIN_Y + aabbIndex] * 0.0625f;
                        float blockMaxY = 1 + y + blockXYZSubData[MAX_Y + aabbIndex] * 0.0625f;
                        float blockMinZ = z + blockXYZSubData[MIN_Z + aabbIndex] * 0.0625f;
                        float blockMaxZ = 1 + z + blockXYZSubData[MAX_Z + aabbIndex] * 0.0625f;

                        if (minX < blockMaxX && maxX > blockMinX
                                && minY < blockMaxY && maxY > blockMinY
                                && minZ < blockMaxZ && maxZ > blockMinZ) {

                            intersection = Utils.absMax(intersection, Utils.absMin(minZ - blockMaxZ, maxZ - blockMinZ));
                        }
                    }
                }
        return intersection;
    }

    public static boolean entityIntersectsBlock(float minX, float maxX, float minY, float maxY, float minZ, float maxZ, int blockX, int blockY, int blockZ, short block) {

        int blockProperties = Block.getBlockProperties(block);
        if ((blockProperties & NO_COLLISION) != 0) return false;
        byte[] blockXYZSubData = Block.getXYZSubData(block);

        for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
            float minBlockX = blockX + blockXYZSubData[MIN_X + aabbIndex] * 0.0625f;
            float maxBlockX = 1 + blockX + blockXYZSubData[MAX_X + aabbIndex] * 0.0625f;
            float minBlockY = blockY + blockXYZSubData[MIN_Y + aabbIndex] * 0.0625f;
            float maxBlockY = 1 + blockY + blockXYZSubData[MAX_Y + aabbIndex] * 0.0625f;
            float minBlockZ = blockZ + blockXYZSubData[MIN_Z + aabbIndex] * 0.0625f;
            float maxBlockZ = 1 + blockZ + blockXYZSubData[MAX_Z + aabbIndex] * 0.0625f;
            if (minX < maxBlockX && maxX > minBlockX && minY < maxBlockY && maxY > minBlockY && minZ < maxBlockZ && maxZ > minBlockZ)
                return true;
        }
        return false;
    }

    public static boolean entityIntersectsBlock(int blockX, int blockY, int blockZ, short block) {
        byte[] blockXYZSubData = Block.getXYZSubData(block);

        int currentClusterX = blockX >> ENTITY_CLUSTER_SIZE_BITS;
        int currentClusterY = blockY >> ENTITY_CLUSTER_SIZE_BITS;
        int currentClusterZ = blockZ >> ENTITY_CLUSTER_SIZE_BITS;

        for (int clusterX = currentClusterX - 1; clusterX <= currentClusterX + 1; clusterX++)
            for (int clusterZ = currentClusterZ - 1; clusterZ <= currentClusterZ + 1; clusterZ++)
                for (int clusterY = currentClusterY - 1; clusterY <= currentClusterY + 1; clusterY++) {

                    LinkedList<Entity> entityCluster = Chunk.getEntityCluster(clusterX, clusterY, clusterZ);
                    if (entityCluster == null) continue;

                    for (Entity entity : entityCluster) {

                        float[] aabb = entity.getAabb();
                        Vector3f position = entity.getPosition();

                        for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                            float minBlockX = blockX + blockXYZSubData[MIN_X + aabbIndex] * 0.0625f;
                            float maxBlockX = 1 + blockX + blockXYZSubData[MAX_X + aabbIndex] * 0.0625f;
                            float minBlockY = blockY + blockXYZSubData[MIN_Y + aabbIndex] * 0.0625f;
                            float maxBlockY = 1 + blockY + blockXYZSubData[MAX_Y + aabbIndex] * 0.0625f;
                            float minBlockZ = blockZ + blockXYZSubData[MIN_Z + aabbIndex] * 0.0625f;
                            float maxBlockZ = 1 + blockZ + blockXYZSubData[MAX_Z + aabbIndex] * 0.0625f;
                            if (aabb[MIN_X] + position.x < maxBlockX && aabb[MAX_X] + position.x > minBlockX
                                    && aabb[MIN_Y] + position.y < maxBlockY && aabb[MAX_Y] + position.y > minBlockY
                                    && aabb[MIN_Z] + position.z < maxBlockZ && aabb[MAX_Z] + position.z > minBlockZ)
                                return true;
                        }
                    }
                }
        return false;
    }

    public boolean isDead() {
        return isDead;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public void addVelocity(float x, float y, float z) {
        velocity.add(x, y, z);
    }

    private static int packPositionData(int x, int y, int z) {
        return x + 511 << 20 | y + 511 << 10 | z + 511;
    }

    private static int packMiscellaneousData(int u, int v, int side) {
        return side | u << 3 | v << 4;
    }

    public float[] getAabb() {
        return aabb;
    }

    private static int[] getEntityVertices() {

        return new int[]{
                packMiscellaneousData(0, 0, TOP), packPositionData(8, 8, 8),
                packMiscellaneousData(1, 0, TOP), packPositionData(8, 8, -8),
                packMiscellaneousData(0, 1, TOP), packPositionData(-8, 8, 8),
                packMiscellaneousData(1, 1, TOP), packPositionData(-8, 8, -8),

                packMiscellaneousData(0, 0, BOTTOM), packPositionData(8, -8, 8),
                packMiscellaneousData(1, 0, BOTTOM), packPositionData(-8, -8, 8),
                packMiscellaneousData(0, 1, BOTTOM), packPositionData(8, -8, -8),
                packMiscellaneousData(1, 1, BOTTOM), packPositionData(-8, -8, -8),

                packMiscellaneousData(0, 0, WEST), packPositionData(8, 8, 8),
                packMiscellaneousData(0, 1, WEST), packPositionData(8, -8, 8),
                packMiscellaneousData(1, 0, WEST), packPositionData(8, 8, -8),
                packMiscellaneousData(1, 1, WEST), packPositionData(8, -8, -8),

                packMiscellaneousData(0, 0, NORTH), packPositionData(-8, 8, 8),
                packMiscellaneousData(0, 1, NORTH), packPositionData(-8, -8, 8),
                packMiscellaneousData(1, 0, NORTH), packPositionData(8, 8, 8),
                packMiscellaneousData(1, 1, NORTH), packPositionData(8, -8, 8),

                packMiscellaneousData(0, 0, EAST), packPositionData(-8, 8, -8),
                packMiscellaneousData(0, 1, EAST), packPositionData(-8, -8, -8),
                packMiscellaneousData(1, 0, EAST), packPositionData(-8, 8, 8),
                packMiscellaneousData(1, 1, EAST), packPositionData(-8, -8, 8),

                packMiscellaneousData(0, 0, SOUTH), packPositionData(8, 8, -8),
                packMiscellaneousData(0, 1, SOUTH), packPositionData(8, -8, -8),
                packMiscellaneousData(1, 0, SOUTH), packPositionData(-8, 8, -8),
                packMiscellaneousData(1, 1, SOUTH), packPositionData(-8, -8, -8)
        };
    }
}
