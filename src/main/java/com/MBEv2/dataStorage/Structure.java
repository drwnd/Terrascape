package com.MBEv2.dataStorage;

import com.MBEv2.core.Block;

import static com.MBEv2.utils.Constants.*;

public record Structure(short[] blocks, int lengthX, int lengthY, int lengthZ) {

    public static Structure testStructure;

    public static byte OAK_TREE = 0;
    public static byte SPRUCE_TREE = 1;
    public static byte DARK_OAK_TREE = 2;
//    public static byte PINE_TREE = 3;
//    public static byte REDWOOD_TREE = 4;
//    public static byte BLACK_WOOD_TREE = 5;

    public static final int MIRROR_X = 1;
    public static final int MIRROR_Z = 2;
    public static final int ROTATE_90 = 4;
//    public static final int ROTATE_180 = MIRROR_X | MIRROR_Z;
//    public static final int ROTATE_270 = ROTATE_180 | ROTATE_90;

    public static void init() throws Exception {
        oakTrees[0] = FileManager.loadStructure("Structures/Oak_Tree_1");
        oakTrees[1] = FileManager.loadStructure("Structures/Oak_Tree_2");
        oakTrees[2] = FileManager.loadStructure("Structures/Oak_Tree_3");
        oakTrees[3] = FileManager.loadStructure("Structures/Oak_Tree_4");

        spruceTrees[0] = FileManager.loadStructure("Structures/Spruce_Tree_1");
        spruceTrees[1] = FileManager.loadStructure("Structures/Spruce_Tree_2");
        spruceTrees[2] = FileManager.loadStructure("Structures/Spruce_Tree_3");
        spruceTrees[3] = FileManager.loadStructure("Structures/Spruce_Tree_4");

        darkOakTrees[0] = FileManager.loadStructure("Structures/Dark_Oak_Tree_1");
        darkOakTrees[1] = FileManager.loadStructure("Structures/Dark_Oak_Tree_2");
        darkOakTrees[2] = FileManager.loadStructure("Structures/Dark_Oak_Tree_3");
        darkOakTrees[3] = FileManager.loadStructure("Structures/Dark_Oak_Tree_4");

        pineTrees[0] = FileManager.loadStructure("Structures/Pine_Tree_1");
        pineTrees[1] = FileManager.loadStructure("Structures/Pine_Tree_2");
        pineTrees[2] = FileManager.loadStructure("Structures/Pine_Tree_3");
        pineTrees[3] = FileManager.loadStructure("Structures/Pine_Tree_4");

        blackWoodTrees[0] = FileManager.loadStructure("Structures/Black_Wood_Tree_1");
        blackWoodTrees[1] = FileManager.loadStructure("Structures/Black_Wood_Tree_2");
        blackWoodTrees[2] = FileManager.loadStructure("Structures/Black_Wood_Tree_3");
        blackWoodTrees[3] = FileManager.loadStructure("Structures/Black_Wood_Tree_4");
    }


    public static Structure getStructureVariation(byte name, int x, int y, int z) {
        int positionHash = x + y + z;   // Good enough
        int variationCount = structures[name].length;

        return structures[name][positionHash % variationCount];
    }


//    public short get(int structureX, int structureY, int structureZ) {
//        int index = structureY * lengthX * lengthZ + structureX * lengthZ + structureZ;
//        return blocks[index];
//    }

    public short get(int structureX, int structureY, int structureZ, byte transform) {
        if (lengthX == lengthZ && (transform & ROTATE_90) != 0) {
            int temp = lengthX - structureX - 1;
            structureX = structureZ;
            structureZ = temp;
        }
        if ((transform & MIRROR_X) != 0) structureX = lengthX - structureX - 1;
        if ((transform & MIRROR_Z) != 0) structureZ = lengthZ - structureZ - 1;

        short block = blocks[structureY * lengthX * lengthZ + structureX * lengthZ + structureZ];

        return getTransformedBlock(block, lengthX == lengthZ ? transform : transform & 3);
    }

    public static byte getStructureTransform(double feature, double lowBound, double highBound) {
        // Clamps to range [0, 1], 1 is unlikely though, because RNGs don't usually include 1
        feature -= lowBound;
        feature /= (highBound - lowBound);

        return (byte) (feature * 8.0);
    }

    public static short getTransformedBlock(short block, int transform) {
        if ((block & 0xFFFF) < STANDARD_BLOCKS_THRESHOLD) return getTransformedNonStandardBlock(block, transform);

        int blockType = Block.getBlockType(block);

        if ((transform & MIRROR_X) != 0) block = mirrorBaseBlockX(block);
        if ((transform & MIRROR_Z) != 0) block = mirrorBaseBlockZ(block);
        if ((transform & ROTATE_90) != 0) block = rotateBaseBlock90(block);

        if ((transform & MIRROR_X) != 0) blockType = mirrorBlockTypeX(blockType);
        if ((transform & MIRROR_Z) != 0) blockType = mirrorBlockTypeZ(blockType);
        if ((transform & ROTATE_90) != 0) blockType = rotateBlockType90(blockType);

        return (short) (block & BASE_BLOCK_MASK | blockType);
    }

    public static int rotateBlockType90(int blockType) {
        return switch (blockType) {

            case NORTH_SLAB -> EAST_SLAB;
            case EAST_SLAB -> SOUTH_SLAB;
            case SOUTH_SLAB -> WEST_SLAB;
            case WEST_SLAB -> NORTH_SLAB;
            case NORTH_PLATE -> EAST_PLATE;
            case EAST_PLATE -> SOUTH_PLATE;
            case SOUTH_PLATE -> WEST_PLATE;
            case WEST_PLATE -> NORTH_PLATE;
            case NORTH_SOCKET -> EAST_SOCKET;
            case EAST_SOCKET -> SOUTH_SOCKET;
            case SOUTH_SOCKET -> WEST_SOCKET;
            case WEST_SOCKET -> NORTH_SOCKET;
            case NORTH_PLAYER_HEAD -> EAST_PLAYER_HEAD;
            case EAST_PLAYER_HEAD -> SOUTH_PLAYER_HEAD;
            case SOUTH_PLAYER_HEAD -> WEST_PLAYER_HEAD;
            case WEST_PLAYER_HEAD -> NORTH_PLAYER_HEAD;

            case NORTH_SOUTH_POST -> EAST_WEST_POST;
            case EAST_WEST_POST -> NORTH_SOUTH_POST;
            case NORTH_SOUTH_WALL -> EAST_WEST_WALL;
            case EAST_WEST_WALL -> NORTH_SOUTH_WALL;

            case THIN_BOTTOM_NORTH_STAIR -> THIN_BOTTOM_EAST_STAIR;
            case THIN_BOTTOM_EAST_STAIR -> THIN_BOTTOM_SOUTH_STAIR;
            case THIN_BOTTOM_SOUTH_STAIR -> THIN_BOTTOM_WEST_STAIR;
            case THIN_BOTTOM_WEST_STAIR -> THIN_BOTTOM_NORTH_STAIR;
            case THIN_TOP_NORTH_STAIR -> THIN_TOP_EAST_STAIR;
            case THIN_TOP_EAST_STAIR -> THIN_TOP_SOUTH_STAIR;
            case THIN_TOP_SOUTH_STAIR -> THIN_TOP_WEST_STAIR;
            case THIN_TOP_WEST_STAIR -> THIN_TOP_NORTH_STAIR;
            case THIN_NORTH_EAST_STAIR -> THIN_SOUTH_EAST_STAIR;
            case THIN_SOUTH_EAST_STAIR -> THIN_SOUTH_WEST_STAIR;
            case THIN_SOUTH_WEST_STAIR -> THIN_NORTH_WEST_STAIR;
            case THIN_NORTH_WEST_STAIR -> THIN_NORTH_EAST_STAIR;

            case BOTTOM_NORTH_STAIR -> BOTTOM_EAST_STAIR;
            case BOTTOM_EAST_STAIR -> BOTTOM_SOUTH_STAIR;
            case BOTTOM_SOUTH_STAIR -> BOTTOM_WEST_STAIR;
            case BOTTOM_WEST_STAIR -> BOTTOM_NORTH_STAIR;
            case TOP_NORTH_STAIR -> TOP_EAST_STAIR;
            case TOP_EAST_STAIR -> TOP_SOUTH_STAIR;
            case TOP_SOUTH_STAIR -> TOP_WEST_STAIR;
            case TOP_WEST_STAIR -> TOP_NORTH_STAIR;
            case NORTH_EAST_STAIR -> SOUTH_EAST_STAIR;
            case SOUTH_EAST_STAIR -> SOUTH_WEST_STAIR;
            case SOUTH_WEST_STAIR -> NORTH_WEST_STAIR;
            case NORTH_WEST_STAIR -> NORTH_EAST_STAIR;

            case THICK_BOTTOM_NORTH_STAIR -> THICK_BOTTOM_EAST_STAIR;
            case THICK_BOTTOM_EAST_STAIR -> THICK_BOTTOM_SOUTH_STAIR;
            case THICK_BOTTOM_SOUTH_STAIR -> THICK_BOTTOM_WEST_STAIR;
            case THICK_BOTTOM_WEST_STAIR -> THICK_BOTTOM_NORTH_STAIR;
            case THICK_TOP_NORTH_STAIR -> THICK_TOP_EAST_STAIR;
            case THICK_TOP_EAST_STAIR -> THICK_TOP_SOUTH_STAIR;
            case THICK_TOP_SOUTH_STAIR -> THICK_TOP_WEST_STAIR;
            case THICK_TOP_WEST_STAIR -> THICK_TOP_NORTH_STAIR;
            case THICK_NORTH_EAST_STAIR -> THICK_SOUTH_EAST_STAIR;
            case THICK_SOUTH_EAST_STAIR -> THICK_SOUTH_WEST_STAIR;
            case THICK_SOUTH_WEST_STAIR -> THICK_NORTH_WEST_STAIR;
            case THICK_NORTH_WEST_STAIR -> THICK_NORTH_EAST_STAIR;

            case UP_DOWN_FENCE_NORTH -> UP_DOWN_FENCE_EAST;
            case UP_DOWN_FENCE_EAST -> UP_DOWN_FENCE_SOUTH;
            case UP_DOWN_FENCE_SOUTH -> UP_DOWN_FENCE_WEST;
            case UP_DOWN_FENCE_WEST -> UP_DOWN_FENCE_NORTH;
            case UP_DOWN_FENCE_WEST_SOUTH_EAST -> UP_DOWN_FENCE_NORTH_WEST_SOUTH;
            case UP_DOWN_FENCE_NORTH_WEST_SOUTH -> UP_DOWN_FENCE_NORTH_WEST_EAST;
            case UP_DOWN_FENCE_NORTH_WEST_EAST -> UP_DOWN_FENCE_NORTH_SOUTH_EAST;
            case UP_DOWN_FENCE_NORTH_SOUTH_EAST -> UP_DOWN_FENCE_WEST_SOUTH_EAST;
            case UP_DOWN_FENCE_NORTH_EAST -> UP_DOWN_FENCE_SOUTH_EAST;
            case UP_DOWN_FENCE_SOUTH_EAST -> UP_DOWN_FENCE_WEST_SOUTH;
            case UP_DOWN_FENCE_WEST_SOUTH -> UP_DOWN_FENCE_NORTH_WEST;
            case UP_DOWN_FENCE_NORTH_WEST -> UP_DOWN_FENCE_NORTH_EAST;
            case UP_DOWN_FENCE_NORTH_SOUTH -> UP_DOWN_FENCE_WEST_EAST;
            case UP_DOWN_FENCE_WEST_EAST -> UP_DOWN_FENCE_NORTH_SOUTH;

            case NORTH_SOUTH_FENCE -> EAST_WEST_FENCE;
            case EAST_WEST_FENCE -> NORTH_SOUTH_FENCE;
            case NORTH_SOUTH_FENCE_UP -> EAST_WEST_FENCE_UP;
            case EAST_WEST_FENCE_UP -> NORTH_SOUTH_FENCE_UP;
            case NORTH_SOUTH_FENCE_DOWN -> EAST_WEST_FENCE_DOWN;
            case EAST_WEST_FENCE_DOWN -> NORTH_SOUTH_FENCE_DOWN;
            case NORTH_SOUTH_FENCE_UP_DOWN -> EAST_WEST_FENCE_UP_DOWN;
            case EAST_WEST_FENCE_UP_DOWN -> NORTH_SOUTH_FENCE_UP_DOWN;

            case NORTH_SOUTH_FENCE_WEST -> EAST_WEST_FENCE_NORTH;
            case EAST_WEST_FENCE_NORTH -> NORTH_SOUTH_FENCE_EAST;
            case NORTH_SOUTH_FENCE_EAST -> EAST_WEST_FENCE_SOUTH;
            case EAST_WEST_FENCE_SOUTH -> NORTH_SOUTH_FENCE_WEST;
            case NORTH_SOUTH_FENCE_UP_WEST -> EAST_WEST_FENCE_NORTH_UP;
            case EAST_WEST_FENCE_NORTH_UP -> NORTH_SOUTH_FENCE_UP_EAST;
            case NORTH_SOUTH_FENCE_UP_EAST -> EAST_WEST_FENCE_UP_SOUTH;
            case EAST_WEST_FENCE_UP_SOUTH -> NORTH_SOUTH_FENCE_UP_WEST;
            case NORTH_SOUTH_FENCE_WEST_DOWN -> EAST_WEST_FENCE_NORTH_DOWN;
            case EAST_WEST_FENCE_NORTH_DOWN -> NORTH_SOUTH_FENCE_DOWN_EAST;
            case NORTH_SOUTH_FENCE_DOWN_EAST -> EAST_WEST_FENCE_SOUTH_DOWN;
            case EAST_WEST_FENCE_SOUTH_DOWN -> NORTH_SOUTH_FENCE_WEST_DOWN;
            case NORTH_SOUTH_FENCE_UP_WEST_DOWN -> EAST_WEST_FENCE_NORTH_UP_DOWN;
            case EAST_WEST_FENCE_NORTH_UP_DOWN -> NORTH_SOUTH_FENCE_UP_DOWN_EAST;
            case NORTH_SOUTH_FENCE_UP_DOWN_EAST -> EAST_WEST_FENCE_UP_SOUTH_DOWN;
            case EAST_WEST_FENCE_UP_SOUTH_DOWN -> NORTH_SOUTH_FENCE_UP_WEST_DOWN;

            case NORTH_SOUTH_FENCE_WEST_EAST -> EAST_WEST_FENCE_NORTH_SOUTH;
            case EAST_WEST_FENCE_NORTH_SOUTH -> NORTH_SOUTH_FENCE_WEST_EAST;
            case NORTH_SOUTH_FENCE_UP_WEST_EAST -> EAST_WEST_FENCE_NORTH_UP_SOUTH;
            case EAST_WEST_FENCE_NORTH_UP_SOUTH -> NORTH_SOUTH_FENCE_UP_WEST_EAST;
            case NORTH_SOUTH_FENCE_WEST_DOWN_EAST -> EAST_WEST_FENCE_NORTH_SOUTH_DOWN;
            case EAST_WEST_FENCE_NORTH_SOUTH_DOWN -> NORTH_SOUTH_FENCE_WEST_DOWN_EAST;
            case NORTH_SOUTH_FENCE_UP_WEST_DOWN_EAST -> EAST_WEST_FENCE_NORTH_UP_SOUTH_DOWN;
            case EAST_WEST_FENCE_NORTH_UP_SOUTH_DOWN -> NORTH_SOUTH_FENCE_UP_WEST_DOWN_EAST;

            default -> blockType;
        };
    }

    public static int mirrorBlockTypeZ(int blockType) {
        return switch (blockType) {

            case NORTH_SLAB -> SOUTH_SLAB;
            case SOUTH_SLAB -> NORTH_SLAB;
            case NORTH_PLATE -> SOUTH_PLATE;
            case SOUTH_PLATE -> NORTH_PLATE;
            case NORTH_SOCKET -> SOUTH_SOCKET;
            case SOUTH_SOCKET -> NORTH_SOCKET;
            case NORTH_PLAYER_HEAD -> SOUTH_PLAYER_HEAD;
            case SOUTH_PLAYER_HEAD -> NORTH_PLAYER_HEAD;

            case THIN_BOTTOM_NORTH_STAIR -> THIN_BOTTOM_SOUTH_STAIR;
            case THIN_BOTTOM_SOUTH_STAIR -> THIN_BOTTOM_NORTH_STAIR;
            case THIN_TOP_NORTH_STAIR -> THIN_TOP_SOUTH_STAIR;
            case THIN_TOP_SOUTH_STAIR -> THIN_TOP_NORTH_STAIR;
            case THIN_NORTH_WEST_STAIR -> THIN_SOUTH_WEST_STAIR;
            case THIN_SOUTH_WEST_STAIR -> THIN_NORTH_WEST_STAIR;
            case THIN_NORTH_EAST_STAIR -> THIN_SOUTH_EAST_STAIR;
            case THIN_SOUTH_EAST_STAIR -> THIN_NORTH_EAST_STAIR;

            case BOTTOM_NORTH_STAIR -> BOTTOM_SOUTH_STAIR;
            case BOTTOM_SOUTH_STAIR -> BOTTOM_NORTH_STAIR;
            case TOP_NORTH_STAIR -> TOP_SOUTH_STAIR;
            case TOP_SOUTH_STAIR -> TOP_NORTH_STAIR;
            case NORTH_WEST_STAIR -> SOUTH_WEST_STAIR;
            case SOUTH_WEST_STAIR -> NORTH_WEST_STAIR;
            case NORTH_EAST_STAIR -> SOUTH_EAST_STAIR;
            case SOUTH_EAST_STAIR -> NORTH_EAST_STAIR;

            case THICK_BOTTOM_NORTH_STAIR -> THICK_BOTTOM_SOUTH_STAIR;
            case THICK_BOTTOM_SOUTH_STAIR -> THICK_BOTTOM_NORTH_STAIR;
            case THICK_TOP_NORTH_STAIR -> THICK_TOP_SOUTH_STAIR;
            case THICK_TOP_SOUTH_STAIR -> THICK_TOP_NORTH_STAIR;
            case THICK_NORTH_WEST_STAIR -> THICK_SOUTH_WEST_STAIR;
            case THICK_SOUTH_WEST_STAIR -> THICK_NORTH_WEST_STAIR;
            case THICK_NORTH_EAST_STAIR -> THICK_SOUTH_EAST_STAIR;
            case THICK_SOUTH_EAST_STAIR -> THICK_NORTH_EAST_STAIR;

            case UP_DOWN_FENCE_NORTH -> UP_DOWN_FENCE_SOUTH;
            case UP_DOWN_FENCE_SOUTH -> UP_DOWN_FENCE_NORTH;
            case UP_DOWN_FENCE_NORTH_WEST -> UP_DOWN_FENCE_WEST_SOUTH;
            case UP_DOWN_FENCE_WEST_SOUTH -> UP_DOWN_FENCE_NORTH_WEST;
            case UP_DOWN_FENCE_NORTH_EAST -> UP_DOWN_FENCE_SOUTH_EAST;
            case UP_DOWN_FENCE_SOUTH_EAST -> UP_DOWN_FENCE_NORTH_EAST;
            case UP_DOWN_FENCE_NORTH_WEST_EAST -> UP_DOWN_FENCE_WEST_SOUTH_EAST;
            case UP_DOWN_FENCE_WEST_SOUTH_EAST -> UP_DOWN_FENCE_NORTH_WEST_EAST;

            case EAST_WEST_FENCE_NORTH -> EAST_WEST_FENCE_SOUTH;
            case EAST_WEST_FENCE_SOUTH -> EAST_WEST_FENCE_NORTH;
            case EAST_WEST_FENCE_NORTH_UP -> EAST_WEST_FENCE_UP_SOUTH;
            case EAST_WEST_FENCE_UP_SOUTH -> EAST_WEST_FENCE_NORTH_UP;
            case EAST_WEST_FENCE_NORTH_DOWN -> EAST_WEST_FENCE_SOUTH_DOWN;
            case EAST_WEST_FENCE_SOUTH_DOWN -> EAST_WEST_FENCE_NORTH_DOWN;
            case EAST_WEST_FENCE_NORTH_UP_DOWN -> EAST_WEST_FENCE_UP_SOUTH_DOWN;
            case EAST_WEST_FENCE_UP_SOUTH_DOWN -> EAST_WEST_FENCE_NORTH_UP_DOWN;

            default -> blockType;
        };
    }

    public static int mirrorBlockTypeX(int blockType) {
        return switch (blockType) {

            case WEST_SLAB -> EAST_SLAB;
            case EAST_SLAB -> WEST_SLAB;
            case WEST_PLATE -> EAST_PLATE;
            case EAST_PLATE -> WEST_PLATE;
            case WEST_SOCKET -> EAST_SOCKET;
            case EAST_SOCKET -> WEST_SOCKET;
            case WEST_PLAYER_HEAD -> EAST_PLAYER_HEAD;
            case EAST_PLAYER_HEAD -> WEST_PLAYER_HEAD;

            case THIN_BOTTOM_WEST_STAIR -> THIN_BOTTOM_EAST_STAIR;
            case THIN_BOTTOM_EAST_STAIR -> THIN_BOTTOM_WEST_STAIR;
            case THIN_TOP_WEST_STAIR -> THIN_TOP_EAST_STAIR;
            case THIN_TOP_EAST_STAIR -> THIN_TOP_WEST_STAIR;
            case THIN_NORTH_WEST_STAIR -> THIN_NORTH_EAST_STAIR;
            case THIN_NORTH_EAST_STAIR -> THIN_NORTH_WEST_STAIR;
            case THIN_SOUTH_WEST_STAIR -> THIN_SOUTH_EAST_STAIR;
            case THIN_SOUTH_EAST_STAIR -> THIN_SOUTH_WEST_STAIR;

            case BOTTOM_WEST_STAIR -> BOTTOM_EAST_STAIR;
            case BOTTOM_EAST_STAIR -> BOTTOM_WEST_STAIR;
            case TOP_WEST_STAIR -> TOP_EAST_STAIR;
            case TOP_EAST_STAIR -> TOP_WEST_STAIR;
            case NORTH_WEST_STAIR -> NORTH_EAST_STAIR;
            case NORTH_EAST_STAIR -> NORTH_WEST_STAIR;
            case SOUTH_WEST_STAIR -> SOUTH_EAST_STAIR;
            case SOUTH_EAST_STAIR -> SOUTH_WEST_STAIR;

            case THICK_BOTTOM_WEST_STAIR -> THICK_BOTTOM_EAST_STAIR;
            case THICK_BOTTOM_EAST_STAIR -> THICK_BOTTOM_WEST_STAIR;
            case THICK_TOP_WEST_STAIR -> THICK_TOP_EAST_STAIR;
            case THICK_TOP_EAST_STAIR -> THICK_TOP_WEST_STAIR;
            case THICK_NORTH_WEST_STAIR -> THICK_NORTH_EAST_STAIR;
            case THICK_NORTH_EAST_STAIR -> THICK_NORTH_WEST_STAIR;
            case THICK_SOUTH_WEST_STAIR -> THICK_SOUTH_EAST_STAIR;
            case THICK_SOUTH_EAST_STAIR -> THICK_SOUTH_WEST_STAIR;

            case UP_DOWN_FENCE_WEST -> UP_DOWN_FENCE_EAST;
            case UP_DOWN_FENCE_EAST -> UP_DOWN_FENCE_WEST;
            case UP_DOWN_FENCE_NORTH_WEST -> UP_DOWN_FENCE_NORTH_EAST;
            case UP_DOWN_FENCE_NORTH_EAST -> UP_DOWN_FENCE_NORTH_WEST;
            case UP_DOWN_FENCE_WEST_SOUTH -> UP_DOWN_FENCE_SOUTH_EAST;
            case UP_DOWN_FENCE_SOUTH_EAST -> UP_DOWN_FENCE_WEST_SOUTH;
            case UP_DOWN_FENCE_NORTH_WEST_SOUTH -> UP_DOWN_FENCE_NORTH_SOUTH_EAST;
            case UP_DOWN_FENCE_NORTH_SOUTH_EAST -> UP_DOWN_FENCE_NORTH_WEST_SOUTH;

            case NORTH_SOUTH_FENCE_WEST -> NORTH_SOUTH_FENCE_EAST;
            case NORTH_SOUTH_FENCE_EAST -> NORTH_SOUTH_FENCE_WEST;
            case NORTH_SOUTH_FENCE_UP_WEST -> NORTH_SOUTH_FENCE_UP_EAST;
            case NORTH_SOUTH_FENCE_UP_EAST -> NORTH_SOUTH_FENCE_UP_WEST;
            case NORTH_SOUTH_FENCE_WEST_DOWN -> NORTH_SOUTH_FENCE_DOWN_EAST;
            case NORTH_SOUTH_FENCE_DOWN_EAST -> NORTH_SOUTH_FENCE_WEST_DOWN;
            case NORTH_SOUTH_FENCE_UP_WEST_DOWN -> NORTH_SOUTH_FENCE_UP_DOWN_EAST;
            case NORTH_SOUTH_FENCE_UP_DOWN_EAST -> NORTH_SOUTH_FENCE_UP_WEST_DOWN;

            default -> blockType;
        };
    }

    public static short rotateBaseBlock90(short block) {
        return switch (block & BASE_BLOCK_MASK) {

            case NORTH_SOUTH_OAK_LOG -> EAST_WEST_OAK_LOG;
            case EAST_WEST_OAK_LOG -> NORTH_SOUTH_OAK_LOG;
            case NORTH_SOUTH_STRIPPED_OAK_LOG -> EAST_WEST_STRIPPED_OAK_LOG;
            case EAST_WEST_STRIPPED_OAK_LOG -> NORTH_SOUTH_STRIPPED_OAK_LOG;
            case NORTH_SOUTH_SPRUCE_LOG -> EAST_WEST_SPRUCE_LOG;
            case EAST_WEST_SPRUCE_LOG -> NORTH_SOUTH_SPRUCE_LOG;
            case NORTH_SOUTH_STRIPPED_SPRUCE_LOG -> EAST_WEST_STRIPPED_SPRUCE_LOG;
            case EAST_WEST_STRIPPED_SPRUCE_LOG -> NORTH_SOUTH_STRIPPED_SPRUCE_LOG;
            case NORTH_SOUTH_DARK_OAK_LOG -> EAST_WEST_DARK_OAK_LOG;
            case EAST_WEST_DARK_OAK_LOG -> NORTH_SOUTH_DARK_OAK_LOG;
            case NORTH_SOUTH_STRIPPED_DARK_OAK_LOG -> EAST_WEST_STRIPPED_DARK_OAK_LOG;
            case EAST_WEST_STRIPPED_DARK_OAK_LOG -> NORTH_SOUTH_STRIPPED_DARK_OAK_LOG;
            case NORTH_SOUTH_PINE_LOG -> EAST_WEST_PINE_LOG;
            case EAST_WEST_PINE_LOG -> NORTH_SOUTH_PINE_LOG;
            case NORTH_SOUTH_STRIPPED_PINE_LOG -> EAST_WEST_STRIPPED_PINE_LOG;
            case EAST_WEST_STRIPPED_PINE_LOG -> NORTH_SOUTH_STRIPPED_PINE_LOG;
            case NORTH_SOUTH_REDWOOD_LOG -> EAST_WEST_REDWOOD_LOG;
            case EAST_WEST_REDWOOD_LOG -> NORTH_SOUTH_REDWOOD_LOG;
            case NORTH_SOUTH_STRIPPED_REDWOOD_LOG -> EAST_WEST_STRIPPED_REDWOOD_LOG;
            case EAST_WEST_STRIPPED_REDWOOD_LOG -> NORTH_SOUTH_STRIPPED_REDWOOD_LOG;
            case NORTH_SOUTH_BLACK_WOOD_LOG -> EAST_WEST_BLACK_WOOD_LOG;
            case EAST_WEST_BLACK_WOOD_LOG -> NORTH_SOUTH_BLACK_WOOD_LOG;
            case NORTH_SOUTH_STRIPPED_BLACK_WOOD_LOG -> EAST_WEST_STRIPPED_BLACK_WOOD_LOG;
            case EAST_WEST_STRIPPED_BLACK_WOOD_LOG -> NORTH_SOUTH_STRIPPED_BLACK_WOOD_LOG;

            case NORTH_FURNACE -> EAST_FURNACE;
            case EAST_FURNACE -> SOUTH_FURNACE;
            case SOUTH_FURNACE -> WEST_FURNACE;
            case WEST_FURNACE -> NORTH_FURNACE;

            default -> block;
        };
    }

    public static short mirrorBaseBlockZ(short block) {
        return switch (block & BASE_BLOCK_MASK) {
            case NORTH_FURNACE -> SOUTH_FURNACE;
            case SOUTH_FURNACE -> NORTH_FURNACE;
            default -> block;
        };
    }

    public static short mirrorBaseBlockX(short block) {
        return switch (block & BASE_BLOCK_MASK) {
            case WEST_FURNACE -> EAST_FURNACE;
            case EAST_FURNACE -> WEST_FURNACE;
            default -> block;
        };
    }

    public static short getTransformedNonStandardBlock(short block, int transform) {
        if ((transform & MIRROR_X) != 0) block = switch (block) {
            case WEST_CREATOR_HEAD -> EAST_CREATOR_HEAD;
            case EAST_CREATOR_HEAD -> WEST_CREATOR_HEAD;
            default -> block;
        };
        if ((transform & MIRROR_Z) != 0) block = switch (block) {
            case NORTH_CREATOR_HEAD -> SOUTH_CREATOR_HEAD;
            case SOUTH_CREATOR_HEAD -> NORTH_CREATOR_HEAD;
            default -> block;
        };
        if ((transform & ROTATE_90) != 0) block = switch (block) {
            case NORTH_CREATOR_HEAD -> EAST_CREATOR_HEAD;
            case EAST_CREATOR_HEAD -> SOUTH_CREATOR_HEAD;
            case SOUTH_CREATOR_HEAD -> WEST_CREATOR_HEAD;
            case WEST_CREATOR_HEAD -> NORTH_CREATOR_HEAD;
            default -> block;
        };
        return block;
    }

    private static final Structure[] oakTrees = new Structure[4];
    private static final Structure[] spruceTrees = new Structure[4];
    private static final Structure[] darkOakTrees = new Structure[4];
    private static final Structure[] pineTrees = new Structure[4];
    private static final Structure[] redwoodTrees = new Structure[4];
    private static final Structure[] blackWoodTrees = new Structure[4];

    private static final Structure[][] structures = {oakTrees, spruceTrees, darkOakTrees, pineTrees, redwoodTrees, blackWoodTrees};
}
