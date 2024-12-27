package terrascape.entity.entities;

import terrascape.server.*;
import terrascape.dataStorage.Chunk;
import terrascape.player.ShaderManager;
import terrascape.utils.Utils;
import org.joml.Vector3f;

import static terrascape.utils.Constants.*;

public abstract class Entity {

    protected Vector3f position;
    protected Vector3f velocity;
    protected float[] aabb;
    protected boolean isDead = false;

    public static void initAll() {
        TNT_Entity.init();
        FallingBlockEntity.init();
    }

    public abstract void update();

    protected abstract void renderUnique(ShaderManager shader, int modelIndexBuffer, float timeSinceLastTick);

    public abstract void delete();

    public void render(ShaderManager shader, int modelIndexBuffer, float timeSinceLastTick) {
        int x = Utils.floor(position.x);
        int y = Utils.floor(position.y);
        int z = Utils.floor(position.z);

        shader.setUniform("position",
                position.x - (1.0f / TARGET_TPS - timeSinceLastTick) * velocity.x,
                position.y - (1.0f / TARGET_TPS - timeSinceLastTick) * velocity.y,
                position.z - (1.0f / TARGET_TPS - timeSinceLastTick) * velocity.z);
        shader.setUniform("lightLevel", Chunk.getLightInWorld(x, y, z));

        renderUnique(shader, modelIndexBuffer, timeSinceLastTick);
    }

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

    public boolean isDead() {
        return isDead;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void addVelocity(float x, float y, float z) {
        velocity.add(x, y, z);
    }

    public static int packData(int x, int y, int z) {
        return x + 511 << 20 | y + 511 << 10 | z + 511;
    }

    public static int packData(int u, int v) {
        return u + 15 << 9 | v + 15;
    }

    public float[] getAabb() {
        return aabb;
    }
}
