package com.MBEv2.core;

import org.joml.Vector3f;
import org.joml.Vector3i;

import static com.MBEv2.core.utils.Constants.*;

public class Block {

    private static final int[] BLOCK_TYPE = new int[256];

    private static final byte[] OCCLUSION_DATA = new byte[AMOUNT_OF_BLOCK_TYPES];
    private static final byte[] BLOCK_DATA = new byte[AMOUNT_OF_BLOCK_TYPES];
    private static final byte[][] BLOCK_XYZ_SUB_DATA = new byte[AMOUNT_OF_BLOCK_TYPES][0];
    private static final byte[][] BLOCK_UV_SUB_DATA = new byte[AMOUNT_OF_BLOCK_TYPES][0];
    public static final int[][] NORMALS = {
            {0, 0, 1},
            {0, 1, 0},
            {1, 0, 0},
            {0, 0, -1},
            {0, -1, 0},
            {-1, 0, 0}};

    public static final int[][] CORNERS_OF_SIDE = {
            {1, 0, 5, 4},
            {2, 0, 3, 1},
            {3, 1, 7, 5},
            {2, 3, 6, 7},
            {6, 4, 7, 5},
            {2, 0, 6, 4}};

    public static boolean occludes(byte toTestBlock, byte occludingBlock, int side, int x, int y, int z) {
        byte occlusionData = OCCLUSION_DATA[BLOCK_TYPE[Byte.toUnsignedInt(occludingBlock)]];
        byte blockData = BLOCK_DATA[BLOCK_TYPE[Byte.toUnsignedInt(toTestBlock)]];
        int occludingSide = (side + 3) % 6;
        int occlusionType = (occlusionData >> 6) & 3;
        int blockSideType = blockData & SIDE_MASKS[side];
        int occludingSideType = occlusionData & SIDE_MASKS[occludingSide];

        if ((occlusionType == OCCLUDES_ALL || occlusionType == OCCLUDES_DYNAMIC_ALL) && blockSideType != 0 && occludingSideType != 0)
            return true;

        if (occlusionType == OCCLUDES_DYNAMIC_SELF || occlusionType == OCCLUDES_DYNAMIC_ALL)
            return dynamicOcclusion(toTestBlock, occludingBlock, side, x, y, z, blockSideType, occludingSideType);

        return occlusionType == OCCLUDES_SELF && toTestBlock == occludingBlock;
    }

    public static boolean dynamicOcclusion(byte toTestBlock, byte occludingBlock, int side, int x, int y, int z, int blockSideType, int occludingSideType) {
        if (occludingBlock == WATER) {
            if (toTestBlock != WATER)
                return false;

            if (side == TOP || side == BOTTOM)
                return true;
            byte blockAboveToTestBlock = Chunk.getBlockInWorld(x, y + 1, z);
            int[] normal = NORMALS[side];
            byte blockAboveOccludingBlock = Chunk.getBlockInWorld(x + normal[0], y + 1, z + normal[2]);
            if (blockAboveOccludingBlock == WATER && blockAboveToTestBlock == WATER || blockAboveOccludingBlock != WATER && blockAboveToTestBlock != WATER)
                return true;
            return blockAboveToTestBlock != WATER;
        }

        if (blockSideType != 0 && occludingSideType != 0)
            return true;

        if (isBottomSlab(occludingBlock) && isBottomSlab(toTestBlock))
            return side != TOP && side != BOTTOM;
        if (isTopSlab(occludingBlock) && isTopSlab(toTestBlock))
            return side != TOP && side != BOTTOM;
        if (isFrontSlab(occludingBlock) && isFrontSlab(toTestBlock))
            return side != FRONT && side != BACK;
        if (isBackSlab(occludingBlock) && isBackSlab(toTestBlock))
            return side != FRONT && side != BACK;
        if (isLeftSlab(occludingBlock) && isLeftSlab(toTestBlock))
            return side != LEFT && side != RIGHT;
        if (isRightSlab(occludingBlock) && isRightSlab(toTestBlock))
            return side != LEFT && side != RIGHT;

        if (isUpDownPost(occludingBlock) && isUpDownPost(toTestBlock))
            return side == TOP || side == BOTTOM;
        if (isFrontBackPost(occludingBlock) && isFrontBackPost(toTestBlock))
            return side == FRONT || side == BACK;
        if (isLeftRightPost(occludingBlock) && isLeftRightPost(toTestBlock))
            return side == LEFT || side == RIGHT;

        if (isUpDownWall(occludingBlock) && isUpDownWall(toTestBlock))
            return side != TOP && side != BOTTOM;
        if (isFrontBackWall(occludingBlock) && isFrontBackWall(toTestBlock))
            return side != FRONT && side != BACK;
        if (isLeftRightWall(occludingBlock) && isLeftRightWall(toTestBlock))
            return side != LEFT && side != RIGHT;

        return false;
    }

    public static int getTextureIndex(byte block, int side) {

        switch (block) {
            case GRASS -> {
                if (side == TOP)
                    return GRASS;

                if (side == BOTTOM)
                    return DIRT;
                return GRASS_SIDE;
            }
            case DARK_OAK_LEAVES -> {
                return 86;
            }
            case UP_DOWN_OAK_LOG -> {
                if (side == TOP || side == BOTTOM)
                    return OAK_LOG_TOP;
                return OAK_LOG;
            }
            case FRONT_BACK_OAK_LOG -> {
                if (side == FRONT || side == BACK)
                    return OAK_LOG_TOP;
                if (side == BOTTOM)
                    return OAK_LOG;
                return ROTATED_OAK_LOG;
            }
            case LEFT_RIGHT_OAK_LOG -> {
                if (side == LEFT || side == RIGHT)
                    return OAK_LOG_TOP;
                if (side == TOP)
                    return OAK_LOG;
                return ROTATED_OAK_LOG;
            }
            case UP_DOWN_STRIPPED_OAK_LOG -> {
                if (side == TOP || side == BOTTOM)
                    return STRIPPED_OAK_LOG_TOP;
                return STRIPPED_OAK_LOG;
            }
            case FRONT_BACK_STRIPPED_OAK_LOG -> {
                if (side == FRONT || side == BACK)
                    return STRIPPED_OAK_LOG_TOP;
                if (side == BOTTOM)
                    return STRIPPED_OAK_LOG;
                return ROTATED_STRIPPED_OAK_LOG;
            }
            case LEFT_RIGHT_STRIPPED_OAK_LOG -> {
                if (side == LEFT || side == RIGHT)
                    return STRIPPED_OAK_LOG_TOP;
                if (side == TOP)
                    return STRIPPED_OAK_LOG;
                return ROTATED_STRIPPED_OAK_LOG;
            }
            case UP_DOWN_DARK_OAK_LOG -> {
                if (side == TOP || side == BOTTOM)
                    return DARK_OAK_LOG_TOP;
                return DARK_OAK_LOG;
            }
            case FRONT_BACK_DARK_OAK_LOG -> {
                if (side == FRONT || side == BACK)
                    return DARK_OAK_LOG_TOP;
                if (side == BOTTOM)
                    return DARK_OAK_LOG;
                return ROTATED_DARK_OAK_LOG;
            }
            case LEFT_RIGHT_DARK_OAK_LOG -> {
                if (side == LEFT || side == RIGHT)
                    return DARK_OAK_LOG_TOP;
                if (side == TOP)
                    return DARK_OAK_LOG;
                return ROTATED_DARK_OAK_LOG;
            }
            case UP_DOWN_STRIPPED_DARK_OAK_LOG -> {
                if (side == TOP || side == BOTTOM)
                    return STRIPPED_DARK_OAK_LOG_TOP;
                return STRIPPED_DARK_OAK_LOG;
            }
            case FRONT_BACK_STRIPPED_DARK_OAK_LOG -> {
                if (side == FRONT || side == BACK)
                    return STRIPPED_DARK_OAK_LOG_TOP;
                if (side == BOTTOM)
                    return STRIPPED_DARK_OAK_LOG;
                return ROTATED_STRIPPED_DARK_OAK_LOG;
            }
            case LEFT_RIGHT_STRIPPED_DARK_OAK_LOG -> {
                if (side == LEFT || side == RIGHT)
                    return STRIPPED_DARK_OAK_LOG_TOP;
                if (side == TOP)
                    return STRIPPED_DARK_OAK_LOG;
                return ROTATED_STRIPPED_DARK_OAK_LOG;
            }
            case UP_DOWN_SPRUCE_LOG -> {
                if (side == TOP || side == BOTTOM)
                    return SPRUCE_LOG_TOP;
                return SPRUCE_LOG;
            }
            case FRONT_BACK_SPRUCE_LOG -> {
                if (side == FRONT || side == BACK)
                    return SPRUCE_LOG_TOP;
                if (side == BOTTOM)
                    return SPRUCE_LOG;
                return ROTATED_SPRUCE_LOG;
            }
            case LEFT_RIGHT_SPRUCE_LOG -> {
                if (side == LEFT || side == RIGHT)
                    return SPRUCE_LOG_TOP;
                if (side == TOP)
                    return SPRUCE_LOG;
                return ROTATED_SPRUCE_LOG;
            }
            case UP_DOWN_STRIPPED_SPRUCE_LOG -> {
                if (side == TOP || side == BOTTOM)
                    return STRIPPED_SPRUCE_LOG_TOP;
                return STRIPPED_SPRUCE_LOG;
            }
            case FRONT_BACK_STRIPPED_SPRUCE_LOG -> {
                if (side == FRONT || side == BACK)
                    return STRIPPED_SPRUCE_LOG_TOP;
                if (side == BOTTOM)
                    return STRIPPED_SPRUCE_LOG;
                return ROTATED_STRIPPED_SPRUCE_LOG;
            }
            case LEFT_RIGHT_STRIPPED_SPRUCE_LOG -> {
                if (side == LEFT || side == RIGHT)
                    return STRIPPED_SPRUCE_LOG_TOP;
                if (side == TOP)
                    return STRIPPED_SPRUCE_LOG;
                return ROTATED_STRIPPED_SPRUCE_LOG;
            }
            case COBBLESTONE_BOTTOM_SLAB, COBBLESTONE_TOP_SLAB, COBBLESTONE_FRONT_SLAB, COBBLESTONE_BACK_SLAB,
                 COBBLESTONE_LEFT_SLAB, COBBLESTONE_RIGHT_SLAB, COBBLESTONE_UP_DOWN_POST, COBBLESTONE_FRONT_BACK_POST,
                 COBBLESTONE_LEFT_RIGHT_POST, COBBLESTONE_UP_DOWN_WALL, COBBLESTONE_FRONT_BACK_WALL,
                 COBBLESTONE_LEFT_RIGHT_WALL -> {
                return COBBLESTONE;
            }
            case STONE_BRICK_BOTTOM_SLAB, STONE_BRICK_TOP_SLAB, STONE_BRICK_FRONT_SLAB, STONE_BRICK_BACK_SLAB,
                 STONE_BRICK_LEFT_SLAB, STONE_BRICK_RIGHT_SLAB, STONE_BRICK_UP_DOWN_POST, STONE_BRICK_FRONT_BACK_POST,
                 STONE_BRICK_LEFT_RIGHT_POST, STONE_BRICK_UP_DOWN_WALL, STONE_BRICK_FRONT_BACK_WALL,
                 STONE_BRICK_LEFT_RIGHT_WALL -> {
                return STONE_BRICKS;
            }
            case GLASS_UP_DOWN_WALL, GLASS_FRONT_BACK_WALL, GLASS_LEFT_RIGHT_WALL -> {
                return GLASS;
            }
        }
        return block;
    }

    public static boolean playerIntersectsBlock(Vector3f playerPos, byte block, Vector3i blockPos) {
        int lowX = (int) Math.floor(playerPos.x - HALF_PLAYER_WIDTH);
        int highX = (int) Math.floor(playerPos.x + HALF_PLAYER_WIDTH);
        int lowY = (int) Math.floor(playerPos.y - HALF_PLAYER_WIDTH);
        int highY = (int) Math.floor(playerPos.y + HALF_PLAYER_WIDTH);
        int lowZ = (int) Math.floor(playerPos.z - HALF_PLAYER_WIDTH);
        int highZ = (int) Math.floor(playerPos.z + HALF_PLAYER_WIDTH);

        if (lowX == blockPos.x && lowY == blockPos.y && lowZ == blockPos.z && intersectsBlock(playerPos.x - HALF_PLAYER_WIDTH, playerPos.y - HALF_PLAYER_WIDTH, playerPos.z - HALF_PLAYER_WIDTH, block))
            return true;
        if (lowX == blockPos.x && lowY == blockPos.y && highZ == blockPos.z && intersectsBlock(playerPos.x - HALF_PLAYER_WIDTH, playerPos.y - HALF_PLAYER_WIDTH, playerPos.z + HALF_PLAYER_WIDTH, block))
            return true;
        if (lowX == blockPos.x && highY == blockPos.y && lowZ == blockPos.z && intersectsBlock(playerPos.x - HALF_PLAYER_WIDTH, playerPos.y + HALF_PLAYER_WIDTH, playerPos.z - HALF_PLAYER_WIDTH, block))
            return true;
        if (lowX == blockPos.x && highY == blockPos.y && highZ == blockPos.z && intersectsBlock(playerPos.x - HALF_PLAYER_WIDTH, playerPos.y + HALF_PLAYER_WIDTH, playerPos.z + HALF_PLAYER_WIDTH, block))
            return true;
        if (highX == blockPos.x && lowY == blockPos.y && lowZ == blockPos.z && intersectsBlock(playerPos.x + HALF_PLAYER_WIDTH, playerPos.y - HALF_PLAYER_WIDTH, playerPos.z - HALF_PLAYER_WIDTH, block))
            return true;
        if (highX == blockPos.x && lowY == blockPos.y && highZ == blockPos.z && intersectsBlock(playerPos.x + HALF_PLAYER_WIDTH, playerPos.y - HALF_PLAYER_WIDTH, playerPos.z + HALF_PLAYER_WIDTH, block))
            return true;
        if (highX == blockPos.x && highY == blockPos.y && lowZ == blockPos.z && intersectsBlock(playerPos.x + HALF_PLAYER_WIDTH, playerPos.y + HALF_PLAYER_WIDTH, playerPos.z - HALF_PLAYER_WIDTH, block))
            return true;
        return highX == blockPos.x && highY == blockPos.y && highZ == blockPos.z && intersectsBlock(playerPos.x + HALF_PLAYER_WIDTH, playerPos.y + HALF_PLAYER_WIDTH, playerPos.z + HALF_PLAYER_WIDTH, block);
    }

    public static boolean intersectsBlock(double x, double y, double z, byte block) {

        switch (BLOCK_TYPE[Byte.toUnsignedInt(block)]) {
            case FULL_BLOCK, GLASS_TYPE, LEAVE_TYPE -> {
                return true;
            }
            case AIR_TYPE, WATER_TYPE -> {
                return false;
            }
            case FRONT_SLAB -> {
                return fraction(z) >= 0.5;
            }
            case TOP_SLAB -> {
                return fraction(y) >= 0.5;
            }
            case RIGHT_SLAB -> {
                return fraction(x) >= 0.5;
            }
            case BACK_SLAB -> {
                return fraction(z) <= 0.5;
            }
            case BOTTOM_SLAB -> {
                return fraction(y) <= 0.5;
            }
            case LEFT_SLAB -> {
                return fraction(x) <= 0.5;
            }
            case UP_DOWN_WALL -> {
                y = fraction(y);
                return y >= 0.25 && y <= 0.75;
            }
            case FRONT_BACK_WALL -> {
                z = fraction(z);
                return z > 0.25 && z < 0.75;
            }
            case LEFT_RIGHT_WALL -> {
                x = fraction(x);
                return x > 0.25 && x < 0.75;
            }
            case UP_DOWN_POST -> {
                x = fraction(x);
                z = fraction(z);
                return x >= 0.25 && x <= 0.75 && z >= 0.25 && z <= 0.75;
            }
            case FRONT_BACK_POST -> {
                x = fraction(x);
                y = fraction(y);
                return x >= 0.25 && x <= 0.75 && y >= 0.25 && y <= 0.75;
            }
            case LEFT_RIGHT_POST -> {
                y = fraction(y);
                z = fraction(z);
                return y >= 0.25 && y <= 0.75 && z >= 0.25 && z <= 0.75;
            }
        }
        return true;
    }

    public static byte getToPlaceBlock(byte toPlaceBlock, int primaryCameraDirection) {
        if (isSlabType(toPlaceBlock)) {
            if (toPlaceBlock == COBBLESTONE_SLAB)
                return COBBLESTONE_SLABS[primaryCameraDirection];
            if (toPlaceBlock == STONE_BRICK_SLAB)
                return STONE_BRICK_SLABS[primaryCameraDirection];
        }
        if (isPostType(toPlaceBlock)) {
            if (toPlaceBlock == COBBLESTONE_POST)
                return COBBLESTONE_POSTS[primaryCameraDirection % 3];
            if (toPlaceBlock == STONE_BRICK_POST)
                return STONE_BRICK_POSTS[primaryCameraDirection % 3];
        }
        if (isWoodType(toPlaceBlock)) {
            if (toPlaceBlock == OAK_LOG)
                return OAK_LOGS[primaryCameraDirection % 3];
            if (toPlaceBlock == STRIPPED_OAK_LOG)
                return STRIPPED_OAK_LOGS[primaryCameraDirection % 3];
            if (toPlaceBlock == SPRUCE_LOG)
                return SPRUCE_LOGS[primaryCameraDirection % 3];
            if (toPlaceBlock == STRIPPED_SPRUCE_LOG)
                return STRIPPED_SPRUCE_LOGS[primaryCameraDirection % 3];
            if (toPlaceBlock == DARK_OAK_LOG)
                return DARK_OAK_LOGS[primaryCameraDirection % 3];
            if (toPlaceBlock == STRIPPED_DARK_OAK_LOG)
                return STRIPPED_DARK_OAK_LOGS[primaryCameraDirection % 3];
        }
        if (isWallType(toPlaceBlock)) {
            if (toPlaceBlock == GLASS_WALL)
                return GLASS_WALLS[primaryCameraDirection % 3];
            if (toPlaceBlock == COBBLESTONE_WALL)
                return COBBLESTONE_WALLS[primaryCameraDirection % 3];
            if (toPlaceBlock == STONE_BRICK_WALL)
                return STONE_BRICK_WALLS[primaryCameraDirection % 3];
        }
        return toPlaceBlock;
    }

    public static double fraction(double number) {
        int addend = number < 0 ? 1 : 0;
        return (number - (int) number) + addend;
    }

    public static byte getBlockData(byte block) {
        return BLOCK_DATA[BLOCK_TYPE[Byte.toUnsignedInt(block)]];
    }

    public static byte getSubX(byte block, int side, int corner) {
        if (BLOCK_XYZ_SUB_DATA[BLOCK_TYPE[Byte.toUnsignedInt(block)]].length == 0)
            return 0;
        return BLOCK_XYZ_SUB_DATA[BLOCK_TYPE[Byte.toUnsignedInt(block)]][CORNERS_OF_SIDE[side][corner] * 3];
    }

    public static byte getSubY(byte block, int side, int corner) {
        if (BLOCK_XYZ_SUB_DATA[BLOCK_TYPE[Byte.toUnsignedInt(block)]].length == 0)
            return 0;
        return BLOCK_XYZ_SUB_DATA[BLOCK_TYPE[Byte.toUnsignedInt(block)]][1 + CORNERS_OF_SIDE[side][corner] * 3];
    }

    public static byte getSubZ(byte block, int side, int corner) {
        if (BLOCK_XYZ_SUB_DATA[BLOCK_TYPE[Byte.toUnsignedInt(block)]].length == 0)
            return 0;
        return BLOCK_XYZ_SUB_DATA[BLOCK_TYPE[Byte.toUnsignedInt(block)]][2 + CORNERS_OF_SIDE[side][corner] * 3];
    }

    public static byte getSubU(byte block, int side, int corner) {
        if (BLOCK_UV_SUB_DATA[BLOCK_TYPE[Byte.toUnsignedInt(block)]].length == 0)
            return 0;
        return BLOCK_UV_SUB_DATA[BLOCK_TYPE[Byte.toUnsignedInt(block)]][(side << 3) + (corner << 1)];
    }

    public static byte getSubV(byte block, int side, int corner) {
        if (BLOCK_UV_SUB_DATA[BLOCK_TYPE[Byte.toUnsignedInt(block)]].length == 0)
            return 0;
        return BLOCK_UV_SUB_DATA[BLOCK_TYPE[Byte.toUnsignedInt(block)]][(side << 3) + (corner << 1) + 1];
    }

    public static boolean isBottomSlab(byte block) {
        return BLOCK_TYPE[Byte.toUnsignedInt(block)] == BOTTOM_SLAB;
    }

    public static boolean isTopSlab(byte block) {
        return BLOCK_TYPE[Byte.toUnsignedInt(block)] == TOP_SLAB;
    }

    public static boolean isFrontSlab(byte block) {
        return BLOCK_TYPE[Byte.toUnsignedInt(block)] == FRONT_SLAB;
    }

    public static boolean isBackSlab(byte block) {
        return BLOCK_TYPE[Byte.toUnsignedInt(block)] == BACK_SLAB;
    }

    public static boolean isRightSlab(byte block) {
        return BLOCK_TYPE[Byte.toUnsignedInt(block)] == RIGHT_SLAB;
    }

    public static boolean isLeftSlab(byte block) {
        return BLOCK_TYPE[Byte.toUnsignedInt(block)] == LEFT_SLAB;
    }

    public static boolean isUpDownPost(byte block) {
        return BLOCK_TYPE[Byte.toUnsignedInt(block)] == UP_DOWN_POST;
    }

    public static boolean isFrontBackPost(byte block) {
        return BLOCK_TYPE[Byte.toUnsignedInt(block)] == FRONT_BACK_POST;
    }

    public static boolean isLeftRightPost(byte block) {
        return BLOCK_TYPE[Byte.toUnsignedInt(block)] == LEFT_RIGHT_POST;
    }

    public static boolean isUpDownWall(byte block) {
        return BLOCK_TYPE[Byte.toUnsignedInt(block)] == UP_DOWN_WALL;
    }

    public static boolean isFrontBackWall(byte block) {
        return BLOCK_TYPE[Byte.toUnsignedInt(block)] == FRONT_BACK_WALL;
    }

    public static boolean isLeftRightWall(byte block) {
        return BLOCK_TYPE[Byte.toUnsignedInt(block)] == LEFT_RIGHT_WALL;
    }

    public static boolean isSlabType(byte block) {
        return block == COBBLESTONE_SLAB || block == STONE_BRICK_SLAB;
    }

    public static boolean isPostType(byte block) {
        return block == COBBLESTONE_POST || block == STONE_BRICK_POST;
    }

    public static boolean isWoodType(byte block) {
        return block == OAK_LOG || block == STRIPPED_OAK_LOG || block == SPRUCE_LOG || block == STRIPPED_SPRUCE_LOG || block == DARK_OAK_LOG || block == STRIPPED_DARK_OAK_LOG;
    }

    public static boolean isWallType(byte block) {
        return block == GLASS_WALL || block == COBBLESTONE_WALL || block == STONE_BRICK_WALL;
    }

    public static boolean isLeaveType(byte block) {
        return BLOCK_TYPE[Byte.toUnsignedInt(block)] == LEAVE_TYPE;
    }

    public static void init() {
        BLOCK_TYPE[AIR] = AIR_TYPE;
         BLOCK_TYPE[Byte.toUnsignedInt(OUT_OF_WORLD)] = AIR_TYPE;

        BLOCK_TYPE[GRASS] = FULL_BLOCK;
        BLOCK_TYPE[DIRT] = FULL_BLOCK;
        BLOCK_TYPE[STONE] = FULL_BLOCK;
        BLOCK_TYPE[MUD] = FULL_BLOCK;
        BLOCK_TYPE[ANDESITE] = FULL_BLOCK;
        BLOCK_TYPE[SNOW] = FULL_BLOCK;
        BLOCK_TYPE[SAND] = FULL_BLOCK;
        BLOCK_TYPE[STONE_BRICKS] = FULL_BLOCK;
        BLOCK_TYPE[COBBLESTONE] = FULL_BLOCK;
        BLOCK_TYPE[OAK_PLANKS] = FULL_BLOCK;
        BLOCK_TYPE[SPRUCE_PLANKS] = FULL_BLOCK;
        BLOCK_TYPE[DARK_OAK_PLANKS] = FULL_BLOCK;

        BLOCK_TYPE[GLASS] = GLASS_TYPE;

        BLOCK_TYPE[OAK_LEAVES] = LEAVE_TYPE;
        BLOCK_TYPE[SPRUCE_LEAVES] = LEAVE_TYPE;
        BLOCK_TYPE[DARK_OAK_LEAVES] = LEAVE_TYPE;

        BLOCK_TYPE[Byte.toUnsignedInt(WATER)] = WATER_TYPE;

        BLOCK_TYPE[COBBLESTONE_BOTTOM_SLAB] = BOTTOM_SLAB;
        BLOCK_TYPE[COBBLESTONE_TOP_SLAB] = TOP_SLAB;
        BLOCK_TYPE[COBBLESTONE_FRONT_SLAB] = FRONT_SLAB;
        BLOCK_TYPE[COBBLESTONE_BACK_SLAB] = BACK_SLAB;
        BLOCK_TYPE[COBBLESTONE_LEFT_SLAB] = LEFT_SLAB;
        BLOCK_TYPE[COBBLESTONE_RIGHT_SLAB] = RIGHT_SLAB;

        BLOCK_TYPE[STONE_BRICK_BOTTOM_SLAB] = BOTTOM_SLAB;
        BLOCK_TYPE[STONE_BRICK_TOP_SLAB] = TOP_SLAB;
        BLOCK_TYPE[STONE_BRICK_FRONT_SLAB] = FRONT_SLAB;
        BLOCK_TYPE[STONE_BRICK_BACK_SLAB] = BACK_SLAB;
        BLOCK_TYPE[STONE_BRICK_LEFT_SLAB] = LEFT_SLAB;
        BLOCK_TYPE[STONE_BRICK_RIGHT_SLAB] = RIGHT_SLAB;

        BLOCK_TYPE[COBBLESTONE_UP_DOWN_POST] = UP_DOWN_POST;
        BLOCK_TYPE[COBBLESTONE_FRONT_BACK_POST] = FRONT_BACK_POST;
        BLOCK_TYPE[COBBLESTONE_LEFT_RIGHT_POST] = LEFT_RIGHT_POST;

        BLOCK_TYPE[STONE_BRICK_UP_DOWN_POST] = UP_DOWN_POST;
        BLOCK_TYPE[STONE_BRICK_FRONT_BACK_POST] = FRONT_BACK_POST;
        BLOCK_TYPE[STONE_BRICK_LEFT_RIGHT_POST] = LEFT_RIGHT_POST;

        BLOCK_TYPE[GLASS_UP_DOWN_WALL] = UP_DOWN_WALL;
        BLOCK_TYPE[GLASS_FRONT_BACK_WALL] = FRONT_BACK_WALL;
        BLOCK_TYPE[GLASS_LEFT_RIGHT_WALL] = LEFT_RIGHT_WALL;

        BLOCK_TYPE[COBBLESTONE_UP_DOWN_WALL] = UP_DOWN_WALL;
        BLOCK_TYPE[COBBLESTONE_FRONT_BACK_WALL] = FRONT_BACK_WALL;
        BLOCK_TYPE[COBBLESTONE_LEFT_RIGHT_WALL] = LEFT_RIGHT_WALL;

        BLOCK_TYPE[STONE_BRICK_UP_DOWN_WALL] = UP_DOWN_WALL;
        BLOCK_TYPE[STONE_BRICK_FRONT_BACK_WALL] = FRONT_BACK_WALL;
        BLOCK_TYPE[STONE_BRICK_LEFT_RIGHT_WALL] = LEFT_RIGHT_WALL;

        BLOCK_TYPE[UP_DOWN_OAK_LOG] = FULL_BLOCK;
        BLOCK_TYPE[FRONT_BACK_OAK_LOG] = FULL_BLOCK;
        BLOCK_TYPE[LEFT_RIGHT_OAK_LOG] = FULL_BLOCK;

        BLOCK_TYPE[UP_DOWN_STRIPPED_OAK_LOG] = FULL_BLOCK;
        BLOCK_TYPE[FRONT_BACK_STRIPPED_OAK_LOG] = FULL_BLOCK;
        BLOCK_TYPE[Byte.toUnsignedInt(LEFT_RIGHT_STRIPPED_OAK_LOG)] = FULL_BLOCK;

        BLOCK_TYPE[UP_DOWN_SPRUCE_LOG] = FULL_BLOCK;
        BLOCK_TYPE[FRONT_BACK_SPRUCE_LOG] = FULL_BLOCK;
        BLOCK_TYPE[LEFT_RIGHT_SPRUCE_LOG] = FULL_BLOCK;

        BLOCK_TYPE[UP_DOWN_STRIPPED_SPRUCE_LOG] = FULL_BLOCK;
        BLOCK_TYPE[FRONT_BACK_STRIPPED_SPRUCE_LOG] = FULL_BLOCK;
        BLOCK_TYPE[Byte.toUnsignedInt(LEFT_RIGHT_STRIPPED_SPRUCE_LOG)] = FULL_BLOCK;

        BLOCK_TYPE[UP_DOWN_DARK_OAK_LOG] = FULL_BLOCK;
        BLOCK_TYPE[FRONT_BACK_DARK_OAK_LOG] = FULL_BLOCK;
        BLOCK_TYPE[LEFT_RIGHT_DARK_OAK_LOG] = FULL_BLOCK;

        BLOCK_TYPE[UP_DOWN_STRIPPED_DARK_OAK_LOG] = FULL_BLOCK;
        BLOCK_TYPE[FRONT_BACK_STRIPPED_DARK_OAK_LOG] = FULL_BLOCK;
        BLOCK_TYPE[Byte.toUnsignedInt(LEFT_RIGHT_STRIPPED_DARK_OAK_LOG)] = FULL_BLOCK;

        OCCLUSION_DATA[AIR_TYPE] = 0b00000000;
        BLOCK_DATA[AIR_TYPE] = 0b00000000;

        OCCLUSION_DATA[FULL_BLOCK] = 0b00111111;
        BLOCK_DATA[FULL_BLOCK] = 0b01111111;

        OCCLUSION_DATA[LEAVE_TYPE] = 0b00000000;
        BLOCK_DATA[LEAVE_TYPE] = 0b00111111;

        OCCLUSION_DATA[GLASS_TYPE] = 0b01111111;
        BLOCK_DATA[GLASS_TYPE] = 0b00111111;

        OCCLUSION_DATA[WATER_TYPE] = (byte) 0b11111111;
        BLOCK_DATA[WATER_TYPE] = (byte) 0b10111101;

        OCCLUSION_DATA[BOTTOM_SLAB] = (byte) 0b10010000;
        BLOCK_DATA[BOTTOM_SLAB] = 0b00111101;

        OCCLUSION_DATA[TOP_SLAB] = (byte) 0b10000010;
        BLOCK_DATA[TOP_SLAB] = 0b00101111;

        OCCLUSION_DATA[FRONT_SLAB] = (byte) 0b10000001;
        BLOCK_DATA[FRONT_SLAB] = 0b00110111;

        OCCLUSION_DATA[BACK_SLAB] = (byte) 0b10001000;
        BLOCK_DATA[BACK_SLAB] = 0b00111110;

        OCCLUSION_DATA[LEFT_SLAB] = (byte) 0b10100000;
        BLOCK_DATA[LEFT_SLAB] = 0b00111011;

        OCCLUSION_DATA[RIGHT_SLAB] = (byte) 0b10000100;
        BLOCK_DATA[RIGHT_SLAB] = 0b00011111;

        OCCLUSION_DATA[UP_DOWN_POST] = (byte) 0b10000000;
        BLOCK_DATA[UP_DOWN_POST] = 0b00010010;

        OCCLUSION_DATA[FRONT_BACK_POST] = (byte) 0b10000000;
        BLOCK_DATA[FRONT_BACK_POST] = 0b00001001;

        OCCLUSION_DATA[LEFT_RIGHT_POST] = (byte) 0b10000000;
        BLOCK_DATA[LEFT_RIGHT_POST] = 0b00100100;

        OCCLUSION_DATA[UP_DOWN_WALL] = (byte) 0b10000000;
        BLOCK_DATA[UP_DOWN_WALL] = 0b00101101;

        OCCLUSION_DATA[FRONT_BACK_WALL] = (byte) 0b10000000;
        BLOCK_DATA[FRONT_BACK_WALL] = 0b00110110;

        OCCLUSION_DATA[LEFT_RIGHT_WALL] = (byte) 0b10000000;
        BLOCK_DATA[LEFT_RIGHT_WALL] = 0b00011011;


        BLOCK_XYZ_SUB_DATA[UP_DOWN_WALL] = new byte[]{
                0, -4, 0,
                0, -4, 0,
                0, -4, 0,
                0, -4, 0,
                0, 4, 0,
                0, 4, 0,
                0, 4, 0,
                0, 4, 0};

        BLOCK_UV_SUB_DATA[UP_DOWN_WALL] = new byte[]{
                0, 4, 0, 4, 0, -4, 0, -4,
                0, 0, 0, 0, 0, -4, 0, -4,
                0, 4, 0, 4, 0, 0, 0, 0,
                0, 4, 0, 4, 0, -4, 0, -4,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 4, 0, 4, 0, -4, 0, -4};

        BLOCK_XYZ_SUB_DATA[FRONT_BACK_WALL] = new byte[]{
                0, 0, -4,
                0, 0, -4,
                0, 0, 4,
                0, 0, 4,
                0, 0, -4,
                0, 0, -4,
                0, 0, 4,
                0, 0, 4};

        BLOCK_UV_SUB_DATA[FRONT_BACK_WALL] = new byte[]{
                0, 0, 0, 0, 0, 0, 0, 0,
                4, 0, -4, 0, 4, 0, -4, 0,
                4, 0, -4, 0, 4, 0, -4, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, -5, 0, 3, 0, -5, 0, 3,// !!??!?!?
                -4, 0, 4, 0, -4, 0, 4, 0};

        BLOCK_XYZ_SUB_DATA[LEFT_RIGHT_WALL] = new byte[]{
                4, 0, 0,
                -4, 0, 0,
                4, 0, 0,
                -4, 0, 0,
                4, 0, 0,
                -4, 0, 0,
                4, 0, 0,
                -4, 0, 0};

        BLOCK_UV_SUB_DATA[LEFT_RIGHT_WALL] = new byte[]{
                4, 0, -4, 0, 4, 0, -4, 0,
                0, 4, 0, 4, 0, -4, 0, -4,
                0, 0, 0, 0, 0, 0, 0, 0,
                4, 0, -4, 0, 4, 0, -4, 0,
                -4, 0, -4, 0, 4, 0, 4, 0,
                0, 0, 0, 0, 0, 0, 0, 0
        };

        BLOCK_XYZ_SUB_DATA[LEFT_RIGHT_POST] = new byte[]{
                0, -4, -4,
                0, -4, -4,
                0, -4, 4,
                0, -4, 4,
                0, 4, -4,
                0, 4, -4,
                0, 4, 4,
                0, 4, 4};

        BLOCK_UV_SUB_DATA[LEFT_RIGHT_POST] = new byte[]{
                0, 0, 0, 0, 0, -8, 0, -8,
                4, 0, -4, 0, 4, 0, -4, 0,
                4, 4, -4, 4, 4, -4, -4, -4,
                0, 8, 0, 8, 0, 0, 0, 0,
                0, 0, 0, 8, 0, 0, 0, 8,
                -4, 4, 4, 4, -4, -4, 4, -4};

        BLOCK_XYZ_SUB_DATA[FRONT_BACK_POST] = new byte[]{
                4, -4, 0,
                -4, -4, 0,
                4, -4, 0,
                -4, -4, 0,
                4, 4, 0,
                -4, 4, 0,
                4, 4, 0,
                -4, 4, 0};

        BLOCK_UV_SUB_DATA[FRONT_BACK_POST] = new byte[]{
                4, 4, -4, 4, 4, -4, -4, -4,
                0, 8, 0, 8, 0, 0, 0, 0,
                0, 0, 0, 0, 0, -8, 0, -8,
                4, 4, -4, 4, 4, -4, -4, -4,
                -8, 0, -8, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, -8, 0, -8};

        BLOCK_XYZ_SUB_DATA[UP_DOWN_POST] = new byte[]{
                4, 0, -4,
                -4, 0, -4,
                4, 0, 4,
                -4, 0, 4,
                4, 0, -4,
                -4, 0, -4,
                4, 0, 4,
                -4, 0, 4};

        BLOCK_UV_SUB_DATA[UP_DOWN_POST] = new byte[]{
                0, 0, -8, 0, 0, 0, -8, 0,
                4, 4, -4, 4, 4, -4, -4, -4,
                8, 0, 0, 0, 8, 0, 0, 0,
                0, 0, -8, 0, 0, 0, -8, 0,
                -4, -4, -4, 4, 4, -4, 4, 4,
                0, 0, 8, 0, 0, 0, 8, 0};

        BLOCK_XYZ_SUB_DATA[BOTTOM_SLAB] = new byte[]{
                0, -8, 0,
                0, -8, 0,
                0, -8, 0,
                0, -8, 0,
                0, 0, 0,
                0, 0, 0,
                0, 0, 0,
                0, 0, 0};

        BLOCK_UV_SUB_DATA[BOTTOM_SLAB] = new byte[]{
                0, 8, 0, 8, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 8, 0, 8, 0, 0, 0, 0,
                0, 8, 0, 8, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 8, 0, 8, 0, 0, 0, 0};

        BLOCK_XYZ_SUB_DATA[TOP_SLAB] = new byte[]{
                0, 0, 0,
                0, 0, 0,
                0, 0, 0,
                0, 0, 0,
                0, 8, 0,
                0, 8, 0,
                0, 8, 0,
                0, 8, 0};

        BLOCK_UV_SUB_DATA[TOP_SLAB] = new byte[]{
                0, 0, 0, 0, 0, -8, 0, -8,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, -8, 0, -8,
                0, 0, 0, 0, 0, -8, 0, -8,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, -8, 0, -8};

        BLOCK_XYZ_SUB_DATA[FRONT_SLAB] = new byte[]{
                0, 0, 0,
                0, 0, 0,
                0, 0, 8,
                0, 0, 8,
                0, 0, 0,
                0, 0, 0,
                0, 0, 8,
                0, 0, 8};

        BLOCK_UV_SUB_DATA[FRONT_SLAB] = new byte[]{
                0, 0, 0, 0, 0, 0, 0, 0,
                8, 0, 0, 0, 8, 0, 0, 0,
                8, 0, 0, 0, 8, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, -8, 0, 0, 0, -8, 0, 0,
                -8, 0, 0, 0, -8, 0, 0, 0};

        BLOCK_XYZ_SUB_DATA[BACK_SLAB] = new byte[]{
                0, 0, -8,
                0, 0, -8,
                0, 0, 0,
                0, 0, 0,
                0, 0, -8,
                0, 0, -8,
                0, 0, 0,
                0, 0, 0};

        BLOCK_UV_SUB_DATA[BACK_SLAB] = new byte[]{
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, -8, 0, 0, 0, -8, 0,
                0, 0, -8, 0, 0, 0, -8, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 8, 0, 0, 0, 8,
                0, 0, 8, 0, 0, 0, 8, 0};

        BLOCK_XYZ_SUB_DATA[RIGHT_SLAB] = new byte[]{
                8, 0, 0,
                0, 0, 0,
                8, 0, 0,
                0, 0, 0,
                8, 0, 0,
                0, 0, 0,
                8, 0, 0,
                0, 0, 0};

        BLOCK_UV_SUB_DATA[RIGHT_SLAB] = new byte[]{
                0, 0, -8, 0, 0, 0, -8, 0,
                0, 8, 0, 8, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                8, 0, 0, 0, 8, 0, 0, 0,
                -8, 0, -8, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0};

        BLOCK_XYZ_SUB_DATA[LEFT_SLAB] = new byte[]{
                0, 0, 0,
                -8, 0, 0,
                0, 0, 0,
                -8, 0, 0,
                0, 0, 0,
                -8, 0, 0,
                0, 0, 0,
                -8, 0, 0};

        BLOCK_UV_SUB_DATA[LEFT_SLAB] = new byte[]{
                8, 0, 0, 0, 8, 0, 0, 0,
                0, 0, 0, 0, 0, -8, 0, -8,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, -8, 0, 0, 0, -8, 0,
                0, 0, 0, 0, 8, 0, 8, 0,
                0, 0, 0, 0, 0, 0, 0, 0};
    }
}
