package com.MBEv2.core.entity;

import com.MBEv2.core.Block;
import com.MBEv2.core.Chunk;
import com.MBEv2.core.utils.Utils;
import org.joml.Vector3f;
import org.joml.Vector3i;

import static com.MBEv2.core.utils.Constants.*;
import static com.MBEv2.core.utils.Settings.*;

public record Target(Vector3i position, Vector3f inBlockPosition, int side, short block) {

    public static Target getTarget(Vector3f origin, Vector3f dir) {

        int x = Utils.floor(origin.x);
        int y = Utils.floor(origin.y);
        int z = Utils.floor(origin.z);

        int xDir = dir.x < 0 ? -1 : 1;
        int yDir = dir.y < 0 ? -1 : 1;
        int zDir = dir.z < 0 ? -1 : 1;

        int xSide = dir.x < 0 ? RIGHT : LEFT;
        int ySide = dir.y < 0 ? TOP : BOTTOM;
        int zSide = dir.z < 0 ? FRONT : BACK;

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

            if (blockType != AIR_TYPE && blockType != LIQUID_TYPE) {

                Vector3f currentPosition = new Vector3f(
                        (float) (origin.x + (length + 0.01) * dir.x),
                        (float) (origin.y + (length + 0.01) * dir.y),
                        (float) (origin.z + (length + 0.01) * dir.z));

                byte value = intersectsBlock(block, currentPosition, dir, intersectedSide, xUnit, yUnit, zUnit);

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

    public static byte intersectsBlock(short block, Vector3f origin, Vector3f dir, int intersectedSide, double xUnit, double yUnit, double zUnit) {
        byte[] aabb = Block.getXYZSubData(block);

        origin.x = Utils.fraction(origin.x) * 16.0f;
        origin.y = Utils.fraction(origin.y) * 16.0f;
        origin.z = Utils.fraction(origin.z) * 16.0f;

        int x = Utils.floor(origin.x);
        int y = Utils.floor(origin.y);
        int z = Utils.floor(origin.z);

        int xDir = dir.x < 0 ? -1 : 1;
        int yDir = dir.y < 0 ? -1 : 1;
        int zDir = dir.z < 0 ? -1 : 1;

        int xSide = dir.x < 0 ? RIGHT : LEFT;
        int ySide = dir.y < 0 ? TOP : BOTTOM;
        int zSide = dir.z < 0 ? FRONT : BACK;

        double lengthX = xUnit * (dir.x < 0 ? Utils.fraction(origin.x) : 1 - Utils.fraction(origin.x));
        double lengthY = yUnit * (dir.y < 0 ? Utils.fraction(origin.y) : 1 - Utils.fraction(origin.y));
        double lengthZ = zUnit * (dir.z < 0 ? Utils.fraction(origin.z) : 1 - Utils.fraction(origin.z));

        while (true) {
            for (int aabbIndex = 0; aabbIndex < aabb.length; aabbIndex += 6)
                if (x >= aabb[MIN_X + aabbIndex] && x <= aabb[MAX_X + aabbIndex] + 15 &&
                        y >= aabb[MIN_Y + aabbIndex] && y <= aabb[MAX_Y + aabbIndex] + 15 &&
                        z >= aabb[MIN_Z + aabbIndex] && z <= aabb[MAX_Z + aabbIndex] + 15) {

                    origin.x = x * 0.0625f;
                    origin.y = y * 0.0625f;
                    origin.z = z * 0.0625f;
                    return (byte) (0x80 | intersectedSide);
                }

            if (lengthX < lengthZ && lengthX < lengthY) {
                x += xDir;
                if (x == -1 || x == 17) return 0;
                lengthX += xUnit;
                intersectedSide = xSide;
            } else if (lengthZ < lengthX && lengthZ < lengthY) {
                z += zDir;
                if (z == -1 || z == 17) return 0;
                lengthZ += zUnit;
                intersectedSide = zSide;
            } else {
                y += yDir;
                if (y == -1 || y == 17) return 0;
                lengthY += yUnit;
                intersectedSide = ySide;
            }
        }
    }
}
