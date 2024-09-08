package com.MBEv2.core.entity.entities;

import com.MBEv2.core.Block;
import com.MBEv2.core.Chunk;
import com.MBEv2.core.entity.Entity;
import com.MBEv2.core.utils.Utils;
import com.MBEv2.test.GameLogic;
import org.joml.Vector3f;
import org.joml.Vector3i;

import static com.MBEv2.core.utils.Constants.*;
import static com.MBEv2.core.utils.Constants.CHUNK_SIZE_BITS;

public class TNT_Entity extends Entity {
    private static final float[] TNT_AABB = new float[]{-0.5f, 0.5f, -0.25f, 0.75f, -0.5f, 0.5f};
    public static int vao;

    private int fuse;

    public TNT_Entity(int fuse, Vector3f position, Vector3f velocity) {
        this.fuse = fuse;
        this.position = position;
        this.velocity = velocity;
        aabb = TNT_AABB;
    }

    @Override
    public void update() {
        fuse++;
        move();

        if (fuse >= 80) {
            explode();
            delete();
        }
    }

    @Override
    public int getVAO() {
        return vao;
    }

    @Override
    public int getVertexCount() {
        return 36;
    }

    @Override
    public void delete() {
        isDead = true;
    }

    public void explode() {

        //Corners
        castExplosionRay(position, -0.5773502691896258, -0.5773502691896258, -0.5773502691896258, 5);
        castExplosionRay(position, -0.5773502691896258, -0.5773502691896258, 0.5773502691896258, 5);
        castExplosionRay(position, -0.5773502691896258, 0.5773502691896258, -0.5773502691896258, 5);
        castExplosionRay(position, -0.5773502691896258, 0.5773502691896258, 0.5773502691896258, 5);
        castExplosionRay(position, 0.5773502691896258, -0.5773502691896258, -0.5773502691896258, 5);
        castExplosionRay(position, 0.5773502691896258, -0.5773502691896258, 0.5773502691896258, 5);
        castExplosionRay(position, 0.5773502691896258, 0.5773502691896258, -0.5773502691896258, 5);
        castExplosionRay(position, 0.5773502691896258, 0.5773502691896258, 0.5773502691896258, 5);
        //Edges
        castExplosionRay(position, 0.7071067811865475, 0.7071067811865475, 0, 5);
        castExplosionRay(position, 0.7071067811865475, -0.7071067811865475, 0, 5);
        castExplosionRay(position, -0.7071067811865475, 0.7071067811865475, 0, 5);
        castExplosionRay(position, -0.7071067811865475, -0.7071067811865475, 0, 5);
        castExplosionRay(position, 0.7071067811865475, 0, 0.7071067811865475, 5);
        castExplosionRay(position, 0.7071067811865475, 0, -0.7071067811865475, 5);
        castExplosionRay(position, -0.7071067811865475, 0, 0.7071067811865475, 5);
        castExplosionRay(position, -0.7071067811865475, 0, -0.7071067811865475, 5);
        castExplosionRay(position, 0, 0.7071067811865475, 0.7071067811865475, 5);
        castExplosionRay(position, 0, 0.7071067811865475, -0.7071067811865475, 5);
        castExplosionRay(position, 0, -0.7071067811865475, 0.7071067811865475, 5);
        castExplosionRay(position, 0, -0.7071067811865475, -0.7071067811865475, 5);
        //Middle
        castExplosionRay(position, 0, 0, 1, 5);
        castExplosionRay(position, 0, 0, -1, 5);
        castExplosionRay(position, 0, 1, 0, 5);
        castExplosionRay(position, 0, -1, 0, 5);
        castExplosionRay(position, 1, 0, 0, 5);
        castExplosionRay(position, -1, 0, 0, 5);
    }

    public static void castExplosionRay(Vector3f origin, double dirX, double dirY, double dirZ, int blastStrength) {
        int x = Utils.floor(origin.x);
        int y = Utils.floor(origin.y);
        int z = Utils.floor(origin.z);

        int xDir = dirX < 0 ? -1 : 1;
        int yDir = dirY < 0 ? -1 : 1;
        int zDir = dirZ < 0 ? -1 : 1;

        double dirXSquared = dirX * dirX;
        double dirYSquared = dirY * dirY;
        double dirZSquared = dirZ * dirZ;
        double xUnit = (float) Math.sqrt(1 + (dirYSquared + dirZSquared) / dirXSquared);
        double yUnit = (float) Math.sqrt(1 + (dirXSquared + dirZSquared) / dirYSquared);
        double zUnit = (float) Math.sqrt(1 + (dirXSquared + dirYSquared) / dirZSquared);

        double lengthX = xUnit * (dirX < 0 ? Utils.fraction(origin.x) : 1 - Utils.fraction(origin.x));
        double lengthY = yUnit * (dirY < 0 ? Utils.fraction(origin.y) : 1 - Utils.fraction(origin.y));
        double lengthZ = zUnit * (dirZ < 0 ? Utils.fraction(origin.z) : 1 - Utils.fraction(origin.z));

        double length = 0;
        int blastResistance = 0;

        while (blastResistance < blastStrength && length < 8.0) {
            short block = Chunk.getBlockInWorld(x, y, z);
            int blockType = Block.getBlockType(block);
            if (blockType == LIQUID_TYPE) return;
            if (blockType != AIR_TYPE) {
                blastResistance++;
                int inChunkX = x & CHUNK_SIZE_MASK;
                int inChunkY = y & CHUNK_SIZE_MASK;
                int inChunkZ = z & CHUNK_SIZE_MASK;
                Chunk chunk = Chunk.getChunk(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS);
                chunk.placeBlock(inChunkX, inChunkY, inChunkZ, AIR);
                chunk.setMeshed(false);
                chunk.setModified();
                GameLogic.addBlockChange(x, y, z, block);
                GameLogic.restartGenerator(NONE);
                if (block == TNT) {
                    Vector3i targetPosition = new Vector3i(x, y, z);
                    double distanceSquared = (x - origin.x) * (x - origin.x) + (y - origin.y) * (y - origin.y) + (z - origin.z) * (z - origin.z);
                    Vector3f velocity = new Vector3f(
                            (float) ((x - origin.x) / distanceSquared),
                            (float) ((y - origin.y) / distanceSquared),
                            (float) ((z - origin.z) / distanceSquared));
                    spawnTNTEntity(targetPosition, velocity, 20 + (int) (Math.random() * 60));
                }
            }

            if (lengthX <= lengthZ && lengthX <= lengthY) {
                x += xDir;
                length = lengthX;
                lengthX += xUnit;
            } else if (lengthZ <= lengthX && lengthZ <= lengthY) {
                z += zDir;
                length = lengthZ;
                lengthZ += zUnit;
            } else {
                y += yDir;
                length = lengthY;
                lengthY += yUnit;
            }
        }
    }

    public static int[] TNTEntityVertices() {
        int sideTL = Byte.toUnsignedInt(TNT_SIDE_TEXTURE) - 1;
        int sideTR = Byte.toUnsignedInt(TNT_SIDE_TEXTURE);
        int sideBL = Byte.toUnsignedInt(TNT_SIDE_TEXTURE) + 15;
        int sideBR = Byte.toUnsignedInt(TNT_SIDE_TEXTURE) + 16;

        int topTL = Byte.toUnsignedInt(TNT_TOP_TEXTURE) - 1;
        int topTR = Byte.toUnsignedInt(TNT_TOP_TEXTURE);
        int topBL = Byte.toUnsignedInt(TNT_TOP_TEXTURE) + 15;
        int topBR = Byte.toUnsignedInt(TNT_TOP_TEXTURE) + 16;

        int bottomTL = Byte.toUnsignedInt(TNT_BOTTOM_TEXTURE) - 1;
        int bottomTR = Byte.toUnsignedInt(TNT_BOTTOM_TEXTURE);
        int bottomBL = Byte.toUnsignedInt(TNT_BOTTOM_TEXTURE) + 15;
        int bottomBR = Byte.toUnsignedInt(TNT_BOTTOM_TEXTURE) + 16;

        return new int[]{
                packData(sideTL & 15, topTL >> 4, 4, 6, 4),
                packData(sideBL & 15, topBL >> 4, 4, 6, -4),
                packData(sideTR & 15, topTR >> 4, -4, 6, 4),
                packData(sideBR & 15, topBR >> 4, -4, 6, -4),

                packData(sideTL & 15, bottomTL >> 4, 4, -2, 4),
                packData(sideTR & 15, bottomTR >> 4, -4, -2, 4),
                packData(sideBL & 15, bottomBL >> 4, 4, -2, -4),
                packData(sideBR & 15, bottomBR >> 4, -4, -2, -4),

                packData(sideTL & 15, sideTL >> 4, 4, 6, 4),
                packData(sideBL & 15, sideBL >> 4, 4, -2, 4),
                packData(sideTR & 15, sideTR >> 4, 4, 6, -4),
                packData(sideBR & 15, sideBR >> 4, 4, -2, -4),

                packData(sideTL & 15, sideTL >> 4, -4, 6, 4),
                packData(sideBL & 15, sideBL >> 4, -4, -2, 4),
                packData(sideTR & 15, sideTR >> 4, 4, 6, 4),
                packData(sideBR & 15, sideBR >> 4, 4, -2, 4),

                packData(sideTL & 15, sideTL >> 4, -4, 6, -4),
                packData(sideBL & 15, sideBL >> 4, -4, -2, -4),
                packData(sideTR & 15, sideTR >> 4, -4, 6, 4),
                packData(sideBR & 15, sideBR >> 4, -4, -2, 4),

                packData(sideTL & 15, sideTL >> 4, 4, 6, -4),
                packData(sideBL & 15, sideBL >> 4, 4, -2, -4),
                packData(sideTR & 15, sideTR >> 4, -4, 6, -4),
                packData(sideBR & 15, sideBR >> 4, -4, -2, -4),
        };
    }

    private static int packData(int u, int v, int x, int y, int z) {
        return u << 23 | v << 14 | (x + 7) << 8 | (y + 7) << 4 | (z + 7);
    }

    public static void spawnTNTEntity(Vector3i targetPosition, int fuse) {
        Vector3f position = new Vector3f(targetPosition.x + 0.5f, targetPosition.y + 0.25f, targetPosition.z + 0.5f);
        Vector3f velocity = new Vector3f((float) (Math.random() * 0.3 - 0.15), (float) (Math.random() * 0.3 - 0.15), (float) (Math.random() * 0.3 - 0.15));
        TNT_Entity entity = new TNT_Entity(fuse, position, velocity);
        GameLogic.spawnEntity(entity);
        GameLogic.placeBlock(AIR, targetPosition.x, targetPosition.y, targetPosition.z);
    }

    public static void spawnTNTEntity(Vector3i targetPosition, Vector3f velocity, int fuse) {
        Vector3f position = new Vector3f(targetPosition.x + 0.5f, targetPosition.y + 0.25f, targetPosition.z + 0.5f);
        TNT_Entity entity = new TNT_Entity(fuse, position, velocity);
        GameLogic.spawnEntity(entity);
        GameLogic.placeBlock(AIR, targetPosition.x, targetPosition.y, targetPosition.z);
    }
}
