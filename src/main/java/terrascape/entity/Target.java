package terrascape.entity;

import terrascape.server.Block;
import terrascape.dataStorage.Chunk;
import terrascape.utils.Utils;
import org.joml.Vector3f;
import org.joml.Vector3i;

import static terrascape.utils.Constants.*;
import static terrascape.utils.Settings.*;

public record Target(Vector3i position, Vector3f inBlockPosition, int side, short block) {

    private static final byte[][] VINE_AABBS = new byte[][]{
            {0, 0, 0, 0, 15, 0},
            {0, 0, 15, 0, 0, 0},
            {15, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, -15},
            {0, 0, 0, -15, 0, 0},
            {0, -15, 0, 0, 0, 0}};


    public static Target getTarget(Vector3f origin, Vector3f dir) {

        int x = Utils.floor(origin.x);
        int y = Utils.floor(origin.y);
        int z = Utils.floor(origin.z);

        int xDir = dir.x < 0 ? -1 : 1;
        int yDir = dir.y < 0 ? -1 : 1;
        int zDir = dir.z < 0 ? -1 : 1;

        int xSide = dir.x < 0 ? WEST : EAST;
        int ySide = dir.y < 0 ? TOP : BOTTOM;
        int zSide = dir.z < 0 ? NORTH : SOUTH;

        double dirXSquared = dir.x * dir.x;
        double dirYSquared = dir.y * dir.y;
        double dirZSquared = dir.z * dir.z;
        double xUnit = (float) Math.sqrt(1 + (dirYSquared + dirZSquared) / dirXSquared);
        double yUnit = (float) Math.sqrt(1 + (dirXSquared + dirZSquared) / dirYSquared);
        double zUnit = (float) Math.sqrt(1 + (dirXSquared + dirYSquared) / dirZSquared);

        double lengthX = xUnit * (dir.x < 0 ? Utils.fraction(origin.x) : 1 - Utils.fraction(origin.x));
        double lengthY = yUnit * (dir.y < 0 ? Utils.fraction(origin.y) : 1 - Utils.fraction(origin.y));
        double lengthZ = zUnit * (dir.z < 0 ? Utils.fraction(origin.z) : 1 - Utils.fraction(origin.z));
        double length = 0;

        int intersectedSide = 0;
        while (length < REACH) {

            short block = Chunk.getBlockInWorld(x, y, z);
            int blockType = Block.getBlockType(block);

            if (blockType != AIR_TYPE && !Block.isLiquidType(blockType)) {

                Vector3f currentPosition = new Vector3f((float) (origin.x + (length + 0.01) * dir.x), (float) (origin.y + (length + 0.01) * dir.y), (float) (origin.z + (length + 0.01) * dir.z));

                byte value = intersectsBlock(block, currentPosition, dir, intersectedSide, xUnit, yUnit, zUnit, x, y, z);

                if (value != 0) return new Target(new Vector3i(x, y, z), currentPosition, value & 0x7, block);
            }

            if (lengthX < lengthZ && lengthX < lengthY) {
                x += xDir;
                length = lengthX;
                lengthX += xUnit;
                intersectedSide = xSide;
            } else if (lengthZ < lengthX && lengthZ < lengthY) {
                z += zDir;
                length = lengthZ;
                lengthZ += zUnit;
                intersectedSide = zSide;
            } else {
                y += yDir;
                length = lengthY;
                lengthY += yUnit;
                intersectedSide = ySide;
            }
        }
        return null;
    }

    public static byte intersectsBlock(short block, Vector3f origin, Vector3f dir, int intersectedSide, double xUnit, double yUnit, double zUnit, int x, int y, int z) {
        byte[] aabb;
        if (Block.getBlockType(block) == VINE_TYPE) {
            int vineSides = 0;
            if (Block.getBlockOcclusionData(Chunk.getBlockInWorld(x, y, z - 1), NORTH) == -1L) vineSides |= 1 << SOUTH;
            if (Block.getBlockOcclusionData(Chunk.getBlockInWorld(x, y, z + 1), SOUTH) == -1L) vineSides |= 1 << NORTH;
            if (Block.getBlockOcclusionData(Chunk.getBlockInWorld(x, y - 1, z), TOP) == -1L) vineSides |= 1 << BOTTOM;
            if (Block.getBlockOcclusionData(Chunk.getBlockInWorld(x, y + 1, z), BOTTOM) == -1L) vineSides |= 1 << TOP;
            if (Block.getBlockOcclusionData(Chunk.getBlockInWorld(x - 1, y, z), WEST) == -1L) vineSides |= 1 << EAST;
            if (Block.getBlockOcclusionData(Chunk.getBlockInWorld(x + 1, y, z), EAST) == -1L) vineSides |= 1 << WEST;

            aabb = new byte[Integer.bitCount(vineSides) * 6];

            int index = 0;
            for (int side = 0; side < 6; side++) {
                if ((vineSides & 1 << side) == 0) continue;
                aabb[index++] = VINE_AABBS[side][MIN_X];
                aabb[index++] = VINE_AABBS[side][MAX_X];
                aabb[index++] = VINE_AABBS[side][MIN_Y];
                aabb[index++] = VINE_AABBS[side][MAX_Y];
                aabb[index++] = VINE_AABBS[side][MIN_Z];
                aabb[index++] = VINE_AABBS[side][MAX_Z];
            }

        } else aabb = Block.getXYZSubData(block);

        origin.x = Utils.fraction(origin.x) * 16.0f;
        origin.y = Utils.fraction(origin.y) * 16.0f;
        origin.z = Utils.fraction(origin.z) * 16.0f;

        int inBlockX = Utils.floor(origin.x);
        int inBlockY = Utils.floor(origin.y);
        int inBlockZ = Utils.floor(origin.z);

        int xDir = dir.x < 0 ? -1 : 1;
        int yDir = dir.y < 0 ? -1 : 1;
        int zDir = dir.z < 0 ? -1 : 1;

        int xSide = dir.x < 0 ? WEST : EAST;
        int ySide = dir.y < 0 ? TOP : BOTTOM;
        int zSide = dir.z < 0 ? NORTH : SOUTH;

        double lengthX = xUnit * (dir.x < 0 ? Utils.fraction(origin.x) : 1 - Utils.fraction(origin.x));
        double lengthY = yUnit * (dir.y < 0 ? Utils.fraction(origin.y) : 1 - Utils.fraction(origin.y));
        double lengthZ = zUnit * (dir.z < 0 ? Utils.fraction(origin.z) : 1 - Utils.fraction(origin.z));

        while (true) {
            for (int aabbIndex = 0; aabbIndex < aabb.length; aabbIndex += 6)
                if (inBlockX >= aabb[MIN_X + aabbIndex] && inBlockX <= aabb[MAX_X + aabbIndex] + 15
                        && inBlockY >= aabb[MIN_Y + aabbIndex] && inBlockY <= aabb[MAX_Y + aabbIndex] + 15
                        && inBlockZ >= aabb[MIN_Z + aabbIndex] && inBlockZ <= aabb[MAX_Z + aabbIndex] + 15) {

                    origin.x = inBlockX * 0.0625f;
                    origin.y = inBlockY * 0.0625f;
                    origin.z = inBlockZ * 0.0625f;
                    return (byte) (0x80 | intersectedSide);
                }

            if (lengthX < lengthZ && lengthX < lengthY) {
                inBlockX += xDir;
                if (inBlockX == -1 || inBlockX == 17) return 0;
                lengthX += xUnit;
                intersectedSide = xSide;
            } else if (lengthZ < lengthX && lengthZ < lengthY) {
                inBlockZ += zDir;
                if (inBlockZ == -1 || inBlockZ == 17) return 0;
                lengthZ += zUnit;
                intersectedSide = zSide;
            } else {
                inBlockY += yDir;
                if (inBlockY == -1 || inBlockY == 17) return 0;
                lengthY += yUnit;
                intersectedSide = ySide;
            }
        }
    }
}
