package com.MBEv2.core;

import com.MBEv2.core.entity.Target;
import com.MBEv2.core.utils.Utils;
import org.joml.Vector3f;

import static com.MBEv2.core.utils.Constants.*;

public class Block {

    private static final int[] NON_STANDARD_BLOCK_TYPE = new int[STANDARD_BLOCKS_THRESHOLD];
    private static final byte[][] NON_STANDARD_BLOCK_TEXTURE_INDICES = new byte[STANDARD_BLOCKS_THRESHOLD][1];
    private static final byte[][] STANDARD_BLOCK_TEXTURE_INDICES = new byte[AMOUNT_OF_STANDARD_BLOCKS][1];
    private static final int[] NON_STANDARD_BLOCK_PROPERTIES = new int[STANDARD_BLOCKS_THRESHOLD];
    private static final int[] STANDARD_BLOCK_PROPERTIES = new int[AMOUNT_OF_STANDARD_BLOCKS];

//    private static final byte[] old_BLOCK_TYPE_OCCLUSION_DATA = new byte[TOTAL_AMOUNT_OF_BLOCK_TYPES];
//    private static final byte[] old_BLOCK_TYPE_DATA = new byte[TOTAL_AMOUNT_OF_BLOCK_TYPES];

    public static final long[][] BLOCK_TYPE_OCCLUSION_DATA = new long[TOTAL_AMOUNT_OF_BLOCK_TYPES][0];
    public static final byte[] BLOCK_TYPE_DATA = new byte[TOTAL_AMOUNT_OF_BLOCK_TYPES];

    private static final byte[][] BLOCK_TYPE_XYZ_SUB_DATA = new byte[TOTAL_AMOUNT_OF_BLOCK_TYPES][0];
    private static final byte[][] BLOCK_TYPE_UV_SUB_DATA = new byte[TOTAL_AMOUNT_OF_BLOCK_TYPES][0];
    public static final int[][] NORMALS = {{0, 0, 1}, {0, 1, 0}, {1, 0, 0}, {0, 0, -1}, {0, -1, 0}, {-1, 0, 0}};

    public static final int[][] CORNERS_OF_SIDE = {{1, 0, 5, 4}, {2, 0, 3, 1}, {3, 1, 7, 5}, {2, 3, 6, 7}, {6, 4, 7, 5}, {2, 0, 6, 4}};

    public static boolean occludes(short toTestBlock, short occludingBlock, int side, int x, int y, int z, int aabbIndex) {
        int occludingBlockType = getBlockType(occludingBlock);
        if (occludingBlockType == AIR_TYPE) return false;
        if (isLeaveType(occludingBlock)) {
            if (isLeaveType(toTestBlock)) return side > 2;
            return false;
        }
        if (isGlassType(occludingBlock)) return isGlassType(toTestBlock);

        long toTestOcclusionData = BLOCK_TYPE_OCCLUSION_DATA[getBlockType(toTestBlock)][side + aabbIndex];
        byte blockTypeData = BLOCK_TYPE_DATA[occludingBlockType];
        int occludingSide = (side + 3) % 6;
        long occludingOcclusionData = getBlockTypeOcclusionData(occludingBlock, occludingSide);

        if ((blockTypeData & 3) == OCCLUDES_ALL) {
            if (toTestOcclusionData == 0) return false;
            return (toTestOcclusionData | occludingOcclusionData) == occludingOcclusionData;
        }

        if ((blockTypeData & 3) == OCCLUDES_DYNAMIC_SELF)
            return dynamicOcclusion(toTestBlock, occludingBlock, side, x, y, z);
        return true;
    }

    public static boolean dynamicOcclusion(short toTestBlock, short occludingBlock, int side, int x, int y, int z) {
        if (toTestBlock != occludingBlock) return false;
        if (side == TOP || side == BOTTOM) return true;

        int[] normal = NORMALS[side];
        short blockAboveToTestBlock = Chunk.getBlockInWorld(x, y + 1, z);
        short blockAboveOccludingBlock = Chunk.getBlockInWorld(x + normal[0], y + 1, z + normal[2]);

        int blockAboveToTestBlockType = getBlockType(blockAboveToTestBlock);
        int blockAboveOccludingBlockType = getBlockType(blockAboveOccludingBlock);

        if (getBlockTypeOcclusionData(blockAboveToTestBlock, BOTTOM) == -1 && getBlockTypeOcclusionData(blockAboveOccludingBlock, BOTTOM) != -1)
            return false;
        if ((blockAboveOccludingBlockType == LIQUID_TYPE) == (blockAboveToTestBlockType == LIQUID_TYPE))
            return true;
        if (getBlockTypeOcclusionData(blockAboveOccludingBlock, BOTTOM) == -1 && blockAboveToTestBlockType == LIQUID_TYPE)
            return true;
        return blockAboveToTestBlockType != LIQUID_TYPE;
    }

    public static int getTextureIndex(short block, int side) {
        byte[] blockTextureIndices;
        if ((block & 0xFFFF) < STANDARD_BLOCKS_THRESHOLD)
            blockTextureIndices = NON_STANDARD_BLOCK_TEXTURE_INDICES[block & 0xFFFF];
        else blockTextureIndices = STANDARD_BLOCK_TEXTURE_INDICES[(block & 0xFFFF) >> BLOCK_TYPE_BITS];
        return blockTextureIndices[side >= blockTextureIndices.length ? 0 : side];
    }

    public static boolean entityIntersectsBlock(float minX, float maxX, float minY, float maxY, float minZ, float maxZ, int blockX, int blockY, int blockZ, short block) {

        int blockProperties = getBlockProperties(block);
        if ((blockProperties & NO_COLLISION) != 0) return false;

        int blockType = getBlockType(block);
        byte[] blockXYZSubData = BLOCK_TYPE_XYZ_SUB_DATA[blockType];

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

    public static short getToPlaceBlock(short toPlaceBlock, int primaryCameraDirection, int primaryXZDirection, Target target) {
        if (toPlaceBlock < STANDARD_BLOCKS_THRESHOLD) {
            if (toPlaceBlock == FRONT_CREATOR_HEAD) {
                if (primaryXZDirection == BACK) return FRONT_CREATOR_HEAD;
                if (primaryXZDirection == FRONT) return BACK_CREATOR_HEAD;
                if (primaryXZDirection == RIGHT) return LEFT_CREATOR_HEAD;
                if (primaryXZDirection == LEFT) return RIGHT_CREATOR_HEAD;
            }
            return toPlaceBlock;
        }
        int blockType = toPlaceBlock & BLOCK_TYPE_MASK;
        int baseBlock = toPlaceBlock & BASE_BLOCK_MASK;

        int toPlaceBlockType = getToPlaceBlockType(blockType, primaryCameraDirection, target);
        primaryCameraDirection %= 3;

        if (baseBlock == UP_DOWN_OAK_LOG) {
            if (primaryCameraDirection == FRONT) return (short) (FRONT_BACK_OAK_LOG | toPlaceBlockType);
            if (primaryCameraDirection == TOP) return (short) (UP_DOWN_OAK_LOG | toPlaceBlockType);
            return (short) (LEFT_RIGHT_OAK_LOG | toPlaceBlockType);
        }
        if (baseBlock == UP_DOWN_STRIPPED_OAK_LOG) {
            if (primaryCameraDirection == FRONT) return (short) (FRONT_BACK_STRIPPED_OAK_LOG | toPlaceBlockType);
            if (primaryCameraDirection == TOP) return (short) (UP_DOWN_STRIPPED_OAK_LOG | toPlaceBlockType);
            return (short) (LEFT_RIGHT_STRIPPED_OAK_LOG | toPlaceBlockType);
        }

        if (baseBlock == UP_DOWN_SPRUCE_LOG) {
            if (primaryCameraDirection == FRONT) return (short) (FRONT_BACK_SPRUCE_LOG | toPlaceBlockType);
            if (primaryCameraDirection == TOP) return (short) (UP_DOWN_SPRUCE_LOG | toPlaceBlockType);
            return (short) (LEFT_RIGHT_SPRUCE_LOG | toPlaceBlockType);
        }
        if (baseBlock == UP_DOWN_STRIPPED_SPRUCE_LOG) {
            if (primaryCameraDirection == FRONT) return (short) (FRONT_BACK_STRIPPED_SPRUCE_LOG | toPlaceBlockType);
            if (primaryCameraDirection == TOP) return (short) (UP_DOWN_STRIPPED_SPRUCE_LOG | toPlaceBlockType);
            return (short) (LEFT_RIGHT_STRIPPED_SPRUCE_LOG | toPlaceBlockType);
        }

        if (baseBlock == UP_DOWN_DARK_OAK_LOG) {
            if (primaryCameraDirection == FRONT) return (short) (FRONT_BACK_DARK_OAK_LOG | toPlaceBlockType);
            if (primaryCameraDirection == TOP) return (short) (UP_DOWN_DARK_OAK_LOG | toPlaceBlockType);
            return (short) (LEFT_RIGHT_DARK_OAK_LOG | toPlaceBlockType);
        }
        if (baseBlock == UP_DOWN_STRIPPED_DARK_OAK_LOG) {
            if (primaryCameraDirection == FRONT) return (short) (FRONT_BACK_STRIPPED_DARK_OAK_LOG | toPlaceBlockType);
            if (primaryCameraDirection == TOP) return (short) (UP_DOWN_STRIPPED_DARK_OAK_LOG | toPlaceBlockType);
            return (short) (LEFT_RIGHT_STRIPPED_DARK_OAK_LOG | toPlaceBlockType);
        }

        if (baseBlock == UP_DOWN_PINE_LOG) {
            if (primaryCameraDirection == FRONT) return (short) (FRONT_BACK_PINE_LOG | toPlaceBlockType);
            if (primaryCameraDirection == TOP) return (short) (UP_DOWN_PINE_LOG | toPlaceBlockType);
            return (short) (LEFT_RIGHT_PINE_LOG | toPlaceBlockType);
        }
        if (baseBlock == UP_DOWN_STRIPPED_PINE_LOG) {
            if (primaryCameraDirection == FRONT) return (short) (FRONT_BACK_STRIPPED_PINE_LOG | toPlaceBlockType);
            if (primaryCameraDirection == TOP) return (short) (UP_DOWN_STRIPPED_PINE_LOG | toPlaceBlockType);
            return (short) (LEFT_RIGHT_STRIPPED_PINE_LOG | toPlaceBlockType);
        }

        if (baseBlock == UP_DOWN_REDWOOD_LOG) {
            if (primaryCameraDirection == FRONT) return (short) (FRONT_BACK_REDWOOD_LOG | toPlaceBlockType);
            if (primaryCameraDirection == TOP) return (short) (UP_DOWN_REDWOOD_LOG | toPlaceBlockType);
            return (short) (LEFT_RIGHT_REDWOOD_LOG | toPlaceBlockType);
        }
        if (baseBlock == UP_DOWN_STRIPPED_REDWOOD_LOG) {
            if (primaryCameraDirection == FRONT) return (short) (FRONT_BACK_STRIPPED_REDWOOD_LOG | toPlaceBlockType);
            if (primaryCameraDirection == TOP) return (short) (UP_DOWN_STRIPPED_REDWOOD_LOG | toPlaceBlockType);
            return (short) (LEFT_RIGHT_STRIPPED_REDWOOD_LOG | toPlaceBlockType);
        }

        if (baseBlock == UP_DOWN_BLACK_WOOD_LOG) {
            if (primaryCameraDirection == FRONT) return (short) (FRONT_BACK_BLACK_WOOD_LOG | toPlaceBlockType);
            if (primaryCameraDirection == TOP) return (short) (UP_DOWN_BLACK_WOOD_LOG | toPlaceBlockType);
            return (short) (LEFT_RIGHT_BLACK_WOOD_LOG | toPlaceBlockType);
        }
        if (baseBlock == UP_DOWN_STRIPPED_BLACK_WOOD_LOG) {
            if (primaryCameraDirection == FRONT) return (short) (FRONT_BACK_STRIPPED_BLACK_WOOD_LOG | toPlaceBlockType);
            if (primaryCameraDirection == TOP) return (short) (UP_DOWN_STRIPPED_BLACK_WOOD_LOG | toPlaceBlockType);
            return (short) (LEFT_RIGHT_STRIPPED_BLACK_WOOD_LOG | toPlaceBlockType);
        }

        if (baseBlock == FRONT_FURNACE) {
            if (primaryXZDirection == FRONT) return (short) (BACK_FURNACE | toPlaceBlockType);
            if (primaryXZDirection == BACK) return (short) (FRONT_FURNACE | toPlaceBlockType);
            if (primaryXZDirection == RIGHT) return (short) (LEFT_FURNACE | toPlaceBlockType);
            return (short) (RIGHT_FURNACE | toPlaceBlockType);
        }

        return (short) (baseBlock | toPlaceBlockType);
    }

    private static int getToPlaceBlockType(int blockType, int primaryCameraDirection, Target target) {

        if (blockType == BOTTOM_BACK_STAIR) {
            Vector3f inBlockPosition = target.inBlockPosition();
            double x = Utils.fraction(inBlockPosition.x);
            double y = Utils.fraction(inBlockPosition.y);
            double z = Utils.fraction(inBlockPosition.z);

            if (primaryCameraDirection == FRONT) {
                if (y < x && y < 1.0 - x) return BOTTOM_FRONT_STAIR;
                if (y > x && y < 1.0 - x) return FRONT_LEFT_STAIR;
                if (y > x && y > 1.0 - x) return TOP_FRONT_STAIR;
                return FRONT_RIGHT_STAIR;
            }
            if (primaryCameraDirection == BACK) {
                if (y < x && y < 1.0 - x) return BOTTOM_BACK_STAIR;
                if (y > x && y < 1.0 - x) return BACK_LEFT_STAIR;
                if (y > x && y > 1.0 - x) return TOP_BACK_STAIR;
                return BACK_RIGHT_STAIR;
            }
            if (primaryCameraDirection == BOTTOM) {
                if (x < z && x < 1.0 - z) return BOTTOM_LEFT_STAIR;
                if (x > z && x < 1.0 - z) return BOTTOM_BACK_STAIR;
                if (x > z && x > 1.0 - z) return BOTTOM_RIGHT_STAIR;
                return BOTTOM_FRONT_STAIR;
            }
            if (primaryCameraDirection == TOP) {
                if (x < z && x < 1.0 - z) return TOP_LEFT_STAIR;
                if (x > z && x < 1.0 - z) return TOP_BACK_STAIR;
                if (x > z && x > 1.0 - z) return TOP_RIGHT_STAIR;
                return TOP_FRONT_STAIR;
            }
            if (primaryCameraDirection == RIGHT) {
                if (y < z && y < 1.0 - z) return BOTTOM_RIGHT_STAIR;
                if (y > z && y < 1.0 - z) return BACK_RIGHT_STAIR;
                if (y > z && y > 1.0 - z) return TOP_RIGHT_STAIR;
                return FRONT_RIGHT_STAIR;
            }
            if (primaryCameraDirection == LEFT) {
                if (y < z && y < 1.0 - z) return BOTTOM_LEFT_STAIR;
                if (y > z && y < 1.0 - z) return BACK_LEFT_STAIR;
                if (y > z && y > 1.0 - z) return TOP_LEFT_STAIR;
                return FRONT_LEFT_STAIR;
            }
        }
        if (blockType == THIN_BOTTOM_BACK_STAIR) {
            Vector3f inBlockPosition = target.inBlockPosition();
            double x = Utils.fraction(inBlockPosition.x);
            double y = Utils.fraction(inBlockPosition.y);
            double z = Utils.fraction(inBlockPosition.z);

            if (primaryCameraDirection == FRONT) {
                if (y < x && y < 1.0 - x) return THIN_BOTTOM_FRONT_STAIR;
                if (y > x && y < 1.0 - x) return THIN_FRONT_LEFT_STAIR;
                if (y > x && y > 1.0 - x) return THIN_TOP_FRONT_STAIR;
                return THIN_FRONT_RIGHT_STAIR;
            }
            if (primaryCameraDirection == BACK) {
                if (y < x && y < 1.0 - x) return THIN_BOTTOM_BACK_STAIR;
                if (y > x && y < 1.0 - x) return THIN_BACK_LEFT_STAIR;
                if (y > x && y > 1.0 - x) return THIN_TOP_BACK_STAIR;
                return THIN_BACK_RIGHT_STAIR;
            }
            if (primaryCameraDirection == BOTTOM) {
                if (x < z && x < 1.0 - z) return THIN_BOTTOM_LEFT_STAIR;
                if (x > z && x < 1.0 - z) return THIN_BOTTOM_BACK_STAIR;
                if (x > z && x > 1.0 - z) return THIN_BOTTOM_RIGHT_STAIR;
                return THIN_BOTTOM_FRONT_STAIR;
            }
            if (primaryCameraDirection == TOP) {
                if (x < z && x < 1.0 - z) return THIN_TOP_LEFT_STAIR;
                if (x > z && x < 1.0 - z) return THIN_TOP_BACK_STAIR;
                if (x > z && x > 1.0 - z) return THIN_TOP_RIGHT_STAIR;
                return THIN_TOP_FRONT_STAIR;
            }
            if (primaryCameraDirection == RIGHT) {
                if (y < z && y < 1.0 - z) return THIN_BOTTOM_RIGHT_STAIR;
                if (y > z && y < 1.0 - z) return THIN_BACK_RIGHT_STAIR;
                if (y > z && y > 1.0 - z) return THIN_TOP_RIGHT_STAIR;
                return THIN_FRONT_RIGHT_STAIR;
            }
            if (primaryCameraDirection == LEFT) {
                if (y < z && y < 1.0 - z) return THIN_BOTTOM_LEFT_STAIR;
                if (y > z && y < 1.0 - z) return THIN_BACK_LEFT_STAIR;
                if (y > z && y > 1.0 - z) return THIN_TOP_LEFT_STAIR;
                return THIN_FRONT_LEFT_STAIR;
            }
        }
        if (blockType == THICK_BOTTOM_BACK_STAIR) {
            Vector3f inBlockPosition = target.inBlockPosition();
            double x = Utils.fraction(inBlockPosition.x);
            double y = Utils.fraction(inBlockPosition.y);
            double z = Utils.fraction(inBlockPosition.z);

            if (primaryCameraDirection == FRONT) {
                if (y < x && y < 1.0 - x) return THICK_BOTTOM_FRONT_STAIR;
                if (y > x && y < 1.0 - x) return THICK_FRONT_LEFT_STAIR;
                if (y > x && y > 1.0 - x) return THICK_TOP_FRONT_STAIR;
                return THICK_FRONT_RIGHT_STAIR;
            }
            if (primaryCameraDirection == BACK) {
                if (y < x && y < 1.0 - x) return THICK_BOTTOM_BACK_STAIR;
                if (y > x && y < 1.0 - x) return THICK_BACK_LEFT_STAIR;
                if (y > x && y > 1.0 - x) return THICK_TOP_BACK_STAIR;
                return THICK_BACK_RIGHT_STAIR;
            }
            if (primaryCameraDirection == BOTTOM) {
                if (x < z && x < 1.0 - z) return THICK_BOTTOM_LEFT_STAIR;
                if (x > z && x < 1.0 - z) return THICK_BOTTOM_BACK_STAIR;
                if (x > z && x > 1.0 - z) return THICK_BOTTOM_RIGHT_STAIR;
                return THICK_BOTTOM_FRONT_STAIR;
            }
            if (primaryCameraDirection == TOP) {
                if (x < z && x < 1.0 - z) return THICK_TOP_LEFT_STAIR;
                if (x > z && x < 1.0 - z) return THICK_TOP_BACK_STAIR;
                if (x > z && x > 1.0 - z) return THICK_TOP_RIGHT_STAIR;
                return THICK_TOP_FRONT_STAIR;
            }
            if (primaryCameraDirection == RIGHT) {
                if (y < z && y < 1.0 - z) return THICK_BOTTOM_RIGHT_STAIR;
                if (y > z && y < 1.0 - z) return THICK_BACK_RIGHT_STAIR;
                if (y > z && y > 1.0 - z) return THICK_TOP_RIGHT_STAIR;
                return THICK_FRONT_RIGHT_STAIR;
            }
            if (primaryCameraDirection == LEFT) {
                if (y < z && y < 1.0 - z) return THICK_BOTTOM_LEFT_STAIR;
                if (y > z && y < 1.0 - z) return THICK_BACK_LEFT_STAIR;
                if (y > z && y > 1.0 - z) return THICK_TOP_LEFT_STAIR;
                return THICK_FRONT_LEFT_STAIR;
            }
        }

        primaryCameraDirection %= 3;
        int addend = getToPlaceBlockAddend(primaryCameraDirection, target);

        if (blockType == BOTTOM_SLAB) return SLABS[primaryCameraDirection + addend];
        if (blockType == BOTTOM_PLATE) return PLATES[primaryCameraDirection + addend];
        if (blockType == BOTTOM_SOCKET) return SOCKETS[primaryCameraDirection + addend];
        if (blockType == FRONT_BACK_WALL) return WALLS[primaryCameraDirection];
        if (blockType == UP_DOWN_POST) return POSTS[primaryCameraDirection];
        if (blockType == UP_DOWN_FENCE_FRONT_RIGHT) return FENCES[primaryCameraDirection];
        return blockType;
    }

    public static short getInInventoryBlockEquivalent(short block) {
        if (block == AIR || block == OUT_OF_WORLD) return AIR;
        if (block < STANDARD_BLOCKS_THRESHOLD) {
            if (block >= FRONT_CREATOR_HEAD && block <= LEFT_CREATOR_HEAD) return FRONT_CREATOR_HEAD;
            return block;
        }
        int blockType = block & BLOCK_TYPE_MASK;
        int baseBlock = block & BASE_BLOCK_MASK;

        switch (blockType) {
            case FULL_BLOCK -> {
                return (short) (baseBlock | FULL_BLOCK);
            }
            case PLAYER_HEAD -> {
                return (short) (baseBlock | PLAYER_HEAD);
            }
            case FRONT_SLAB, TOP_SLAB, RIGHT_SLAB, BACK_SLAB, BOTTOM_SLAB, LEFT_SLAB -> {
                return (short) (baseBlock | BOTTOM_SLAB);
            }
            case FRONT_PLATE, TOP_PLATE, RIGHT_PLATE, BACK_PLATE, BOTTOM_PLATE, LEFT_PLATE -> {
                return (short) (baseBlock | BOTTOM_PLATE);
            }
            case FRONT_SOCKET, TOP_SOCKET, RIGHT_SOCKET, BACK_SOCKET, BOTTOM_SOCKET, LEFT_SOCKET -> {
                return (short) (baseBlock | BOTTOM_SOCKET);
            }
            case FRONT_BACK_WALL, UP_DOWN_WALL, LEFT_RIGHT_WALL -> {
                return (short) (baseBlock | FRONT_BACK_WALL);
            }
            case UP_DOWN_POST, FRONT_BACK_POST, LEFT_RIGHT_POST -> {
                return (short) (baseBlock | UP_DOWN_POST);
            }
            case BOTTOM_FRONT_STAIR, BOTTOM_RIGHT_STAIR, BOTTOM_BACK_STAIR, BOTTOM_LEFT_STAIR, TOP_FRONT_STAIR,
                 TOP_RIGHT_STAIR, TOP_BACK_STAIR, TOP_LEFT_STAIR, FRONT_RIGHT_STAIR, FRONT_LEFT_STAIR, BACK_RIGHT_STAIR,
                 BACK_LEFT_STAIR -> {
                return (short) (baseBlock | BOTTOM_BACK_STAIR);
            }
            case THIN_BOTTOM_FRONT_STAIR, THIN_BOTTOM_RIGHT_STAIR, THIN_BOTTOM_BACK_STAIR, THIN_BOTTOM_LEFT_STAIR,
                 THIN_TOP_FRONT_STAIR, THIN_TOP_RIGHT_STAIR, THIN_TOP_BACK_STAIR, THIN_TOP_LEFT_STAIR,
                 THIN_FRONT_RIGHT_STAIR, THIN_FRONT_LEFT_STAIR, THIN_BACK_RIGHT_STAIR, THIN_BACK_LEFT_STAIR -> {
                return (short) (baseBlock | THIN_BOTTOM_BACK_STAIR);
            }
            case THICK_BOTTOM_FRONT_STAIR, THICK_BOTTOM_RIGHT_STAIR, THICK_BOTTOM_BACK_STAIR, THICK_BOTTOM_LEFT_STAIR,
                 THICK_TOP_FRONT_STAIR, THICK_TOP_RIGHT_STAIR, THICK_TOP_BACK_STAIR, THICK_TOP_LEFT_STAIR,
                 THICK_FRONT_RIGHT_STAIR, THICK_FRONT_LEFT_STAIR, THICK_BACK_RIGHT_STAIR, THICK_BACK_LEFT_STAIR -> {
                return (short) (baseBlock | THICK_BOTTOM_BACK_STAIR);
            }
            //Not scuffed at all
            case UP_DOWN_FENCE, UP_DOWN_FENCE_FRONT, UP_DOWN_FENCE_RIGHT, UP_DOWN_FENCE_FRONT_RIGHT, UP_DOWN_FENCE_BACK,
                 UP_DOWN_FENCE_FRONT_BACK, UP_DOWN_FENCE_RIGHT_BACK, UP_DOWN_FENCE_FRONT_RIGHT_BACK, UP_DOWN_FENCE_LEFT,
                 UP_DOWN_FENCE_FRONT_LEFT, UP_DOWN_FENCE_RIGHT_LEFT, UP_DOWN_FENCE_FRONT_RIGHT_LEFT,
                 UP_DOWN_FENCE_BACK_LEFT, UP_DOWN_FENCE_FRONT_BACK_LEFT, UP_DOWN_FENCE_RIGHT_BACK_LEFT,
                 UP_DOWN_FENCE_FRONT_RIGHT_BACK_LEFT, FRONT_BACK_FENCE, FRONT_BACK_FENCE_UP, FRONT_BACK_FENCE_RIGHT,
                 FRONT_BACK_FENCE_UP_RIGHT, FRONT_BACK_FENCE_DOWN, FRONT_BACK_FENCE_UP_DOWN,
                 FRONT_BACK_FENCE_RIGHT_DOWN, FRONT_BACK_FENCE_UP_RIGHT_DOWN, FRONT_BACK_FENCE_LEFT,
                 FRONT_BACK_FENCE_UP_LEFT, FRONT_BACK_FENCE_RIGHT_LEFT, FRONT_BACK_FENCE_UP_RIGHT_LEFT,
                 FRONT_BACK_FENCE_DOWN_LEFT, FRONT_BACK_FENCE_UP_DOWN_LEFT, FRONT_BACK_FENCE_RIGHT_DOWN_LEFT,
                 FRONT_BACK_FENCE_UP_RIGHT_DOWN_LEFT, LEFT_RIGHT_FENCE, LEFT_RIGHT_FENCE_FRONT, LEFT_RIGHT_FENCE_UP,
                 LEFT_RIGHT_FENCE_FRONT_UP, LEFT_RIGHT_FENCE_BACK, LEFT_RIGHT_FENCE_FRONT_BACK,
                 LEFT_RIGHT_FENCE_UP_BACK, LEFT_RIGHT_FENCE_FRONT_UP_BACK, LEFT_RIGHT_FENCE_DOWN,
                 LEFT_RIGHT_FENCE_FRONT_DOWN, LEFT_RIGHT_FENCE_UP_DOWN, LEFT_RIGHT_FENCE_FRONT_UP_DOWN,
                 LEFT_RIGHT_FENCE_BACK_DOWN, LEFT_RIGHT_FENCE_FRONT_BACK_DOWN, LEFT_RIGHT_FENCE_UP_BACK_DOWN,
                 LEFT_RIGHT_FENCE_FRONT_UP_BACK_DOWN -> {
                return (short) (baseBlock | UP_DOWN_FENCE_FRONT_RIGHT);
            }

        }
        return AIR;
    }

    public static int getToPlaceBlockAddend(int primaryCameraDirection, Target target) {
        Vector3f inBlockPosition = target.inBlockPosition();

        if (primaryCameraDirection == FRONT) return Utils.fraction(inBlockPosition.z) > 0.5f ? 0 : 3;
        if (primaryCameraDirection == TOP) return Utils.fraction(inBlockPosition.y) > 0.5f ? 0 : 3;
//        if (primaryCameraDirection == RIGHT)
        return Utils.fraction(inBlockPosition.x) > 0.5f ? 0 : 3;
    }

    public static int getSmartBlockType(short block, int x, int y, int z) {
        int blockType = getBlockType(block);
        if (isFrontBackFenceType(blockType)) {
            int index = 0;
            short adjacentBlock;
            long adjacentMask;

            adjacentBlock = Chunk.getBlockInWorld(x, y + 1, z);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, BOTTOM);
            if ((adjacentMask | BLOCK_TYPE_OCCLUSION_DATA[LEFT_RIGHT_WALL][TOP]) == adjacentMask || isFrontBackFenceType(getBlockType(adjacentBlock)))
                index |= 1;

            adjacentBlock = Chunk.getBlockInWorld(x + 1, y, z);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, LEFT);
            if ((adjacentMask | BLOCK_TYPE_OCCLUSION_DATA[UP_DOWN_WALL][RIGHT]) == adjacentMask || isFrontBackFenceType(getBlockType(adjacentBlock)))
                index |= 2;

            adjacentBlock = Chunk.getBlockInWorld(x, y - 1, z);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, TOP);
            if ((adjacentMask | BLOCK_TYPE_OCCLUSION_DATA[LEFT_RIGHT_WALL][BOTTOM]) == adjacentMask || isFrontBackFenceType(getBlockType(adjacentBlock)))
                index |= 4;

            adjacentBlock = Chunk.getBlockInWorld(x - 1, y, z);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, RIGHT);
            if ((adjacentMask | BLOCK_TYPE_OCCLUSION_DATA[UP_DOWN_WALL][LEFT]) == adjacentMask || isFrontBackFenceType(getBlockType(adjacentBlock)))
                index |= 8;

            return FRONT_BACK_FENCE + index;
        }
        if (isUpDownFenceType(blockType)) {
            int index = 0;
            short adjacentBlock;
            long adjacentMask;

            adjacentBlock = Chunk.getBlockInWorld(x, y, z + 1);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, BACK);
            if ((adjacentMask | BLOCK_TYPE_OCCLUSION_DATA[LEFT_RIGHT_WALL][FRONT]) == adjacentMask || isUpDownFenceType(getBlockType(adjacentBlock)))
                index |= 1;

            adjacentBlock = Chunk.getBlockInWorld(x + 1, y, z);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, LEFT);
            if ((adjacentMask | BLOCK_TYPE_OCCLUSION_DATA[FRONT_BACK_WALL][RIGHT]) == adjacentMask || isUpDownFenceType(getBlockType(adjacentBlock)))
                index |= 2;

            adjacentBlock = Chunk.getBlockInWorld(x, y, z - 1);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, FRONT);
            if ((adjacentMask | BLOCK_TYPE_OCCLUSION_DATA[LEFT_RIGHT_WALL][BACK]) == adjacentMask || isUpDownFenceType(getBlockType(adjacentBlock)))
                index |= 4;

            adjacentBlock = Chunk.getBlockInWorld(x - 1, y, z);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, RIGHT);
            if ((adjacentMask | BLOCK_TYPE_OCCLUSION_DATA[FRONT_BACK_WALL][LEFT]) == adjacentMask || isUpDownFenceType(getBlockType(adjacentBlock)))
                index |= 8;

            return UP_DOWN_FENCE + index;
        }
        if (isLeftRightFenceType(blockType)) {
            int index = 0;
            short adjacentBlock;
            long adjacentMask;

            adjacentBlock = Chunk.getBlockInWorld(x, y, z + 1);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, BACK);
            if ((adjacentMask | BLOCK_TYPE_OCCLUSION_DATA[UP_DOWN_WALL][FRONT]) == adjacentMask || isLeftRightFenceType(getBlockType(adjacentBlock)))
                index |= 1;

            adjacentBlock = Chunk.getBlockInWorld(x, y + 1, z);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, BOTTOM);
            if ((adjacentMask | BLOCK_TYPE_OCCLUSION_DATA[FRONT_BACK_WALL][TOP]) == adjacentMask || isLeftRightFenceType(getBlockType(adjacentBlock)))
                index |= 2;

            adjacentBlock = Chunk.getBlockInWorld(x, y, z - 1);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, FRONT);
            if ((adjacentMask | BLOCK_TYPE_OCCLUSION_DATA[UP_DOWN_WALL][BACK]) == adjacentMask || isLeftRightFenceType(getBlockType(adjacentBlock)))
                index |= 4;

            adjacentBlock = Chunk.getBlockInWorld(x, y - 1, z);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, TOP);
            if ((adjacentMask | BLOCK_TYPE_OCCLUSION_DATA[FRONT_BACK_WALL][BOTTOM]) == adjacentMask || isLeftRightFenceType(getBlockType(adjacentBlock)))
                index |= 8;

            return LEFT_RIGHT_FENCE + index;
        }

        return blockType;
    }

    public static void updateSmartBlock(int x, int y, int z) {
        short block = Chunk.getBlockInWorld(x, y, z);
        int blockType = getBlockType(block);
        if ((BLOCK_TYPE_DATA[blockType] & SMART_BLOCK_TYPE) == 0) return;

        int expectedBlockType = getSmartBlockType(block, x, y, z);
        if (expectedBlockType == blockType) return;

        int chunkX = x >> CHUNK_SIZE_BITS;
        int chunkY = y >> CHUNK_SIZE_BITS;
        int chunkZ = z >> CHUNK_SIZE_BITS;

        Chunk chunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
        if (chunk == null) return;

        int inChunkX = x & CHUNK_SIZE_MASK;
        int inChunkY = y & CHUNK_SIZE_MASK;
        int inChunkZ = z & CHUNK_SIZE_MASK;

        chunk.placeBlock(inChunkX, inChunkY, inChunkZ, (short) (block & BASE_BLOCK_MASK | expectedBlockType));
    }

    public static byte getBlockTypeData(short block) {
        return BLOCK_TYPE_DATA[getBlockType(block)];
    }

    public static boolean hasAmbientOcclusion(short block, short referenceBlock) {
        if (block == AIR) return false;
        if (isLeaveType(block)) return false;
        if (isGlassType(block)) return false;
        int blockType = getBlockType(block);
        if (blockType == LIQUID_TYPE) return false;
        if (blockType != FULL_BLOCK && blockType == (referenceBlock & BLOCK_TYPE_MASK)) return false;
        return (getBlockProperties(block) & LIGHT_EMITTING) == 0;
    }

    public static int getBlockProperties(short block) {
        if ((block & 0xFFFF) < STANDARD_BLOCKS_THRESHOLD) return NON_STANDARD_BLOCK_PROPERTIES[block & 0xFFFF];
        return STANDARD_BLOCK_PROPERTIES[(block & 0xFFFF) >> BLOCK_TYPE_BITS];
    }

    public static byte getSubX(int blockType, int side, int corner, int subDataAddend) {
        if (BLOCK_TYPE_XYZ_SUB_DATA[blockType].length == 0) return 0;
        return BLOCK_TYPE_XYZ_SUB_DATA[blockType][(CORNERS_OF_SIDE[side][corner] & 1) + subDataAddend];
    }

    public static byte getSubY(int blockType, int side, int corner, int subDataAddend) {
        if (BLOCK_TYPE_XYZ_SUB_DATA[blockType].length == 0) return 0;
        return BLOCK_TYPE_XYZ_SUB_DATA[blockType][(CORNERS_OF_SIDE[side][corner] < 4 ? MAX_Y : MIN_Y) + subDataAddend];
    }

    public static byte getSubZ(int blockType, int side, int corner, int subDataAddend) {
        if (BLOCK_TYPE_XYZ_SUB_DATA[blockType].length == 0) return 0;
        return BLOCK_TYPE_XYZ_SUB_DATA[blockType][((CORNERS_OF_SIDE[side][corner] & 3) < 2 ? MAX_Z : MIN_Z) + subDataAddend];
    }

    public static byte getSubU(int blockType, int side, int corner, int subDataAddend) {
        if (BLOCK_TYPE_UV_SUB_DATA[blockType].length != 0)
            return (byte) -BLOCK_TYPE_UV_SUB_DATA[blockType][(side << 3) + (corner << 1) + subDataAddend * 48];

        return switch (side) {
            case FRONT, BOTTOM -> getSubX(blockType, side, corner, subDataAddend);
            case BACK -> (byte) -getSubX(blockType, side, corner, subDataAddend);
            case LEFT -> getSubZ(blockType, side, corner, subDataAddend);
            default -> (byte) -getSubZ(blockType, side, corner, subDataAddend);
        };
    }

    public static byte getSubV(int blockType, int side, int corner, int subDataAddend) {
        if (BLOCK_TYPE_UV_SUB_DATA[blockType].length != 0)
            return BLOCK_TYPE_UV_SUB_DATA[blockType][(side << 3) + (corner << 1) + 1 + subDataAddend * 48];

        return switch (side) {
            case FRONT, BACK, RIGHT, LEFT -> (byte) -getSubY(blockType, side, corner, subDataAddend);
            case BOTTOM -> (byte) -getSubZ(blockType, side, corner, subDataAddend);
            default -> getSubX(blockType, side, corner, subDataAddend);
        };
    }

    public static long getBlockTypeOcclusionData(short block, int side) {
        long max = 0;
        long[] occlusionData = BLOCK_TYPE_OCCLUSION_DATA[getBlockType(block)];
        for (int aabbIndex = 0; aabbIndex < occlusionData.length; aabbIndex += 6)
            max |= occlusionData[aabbIndex + side];
        return max;
    }

    public static byte[] getXYZSubData(short block) {
        return BLOCK_TYPE_XYZ_SUB_DATA[getBlockType(block)];
    }

    public static int getBlockType(short block) {
        if ((block & 0xFFFF) < STANDARD_BLOCKS_THRESHOLD) return NON_STANDARD_BLOCK_TYPE[block & 0xFFFF];
        return block & BLOCK_TYPE_MASK;
    }

    public static boolean isLeaveType(short block) {
        int baseBlock = block & BASE_BLOCK_MASK;
        return baseBlock >= OAK_LEAVES && baseBlock <= BLACK_WOOD_LEAVES;
    }

    public static boolean isGlassType(short block) {
        int baseBlock = block & BASE_BLOCK_MASK;
        return baseBlock == GLASS;
    }

    public static boolean isUpDownFenceType(int blockType) {
        return blockType >= UP_DOWN_FENCE && blockType <= UP_DOWN_FENCE_FRONT_RIGHT_BACK_LEFT;
    }

    public static boolean isFrontBackFenceType(int blockType) {
        return blockType >= FRONT_BACK_FENCE && blockType <= FRONT_BACK_FENCE_UP_RIGHT_DOWN_LEFT;
    }

    public static boolean isLeftRightFenceType(int blockType) {
        return blockType >= LEFT_RIGHT_FENCE && blockType <= LEFT_RIGHT_FENCE_FRONT_UP_BACK_DOWN;
    }

    public static int getFaceCount(int blockType) {
        return BLOCK_TYPE_XYZ_SUB_DATA[blockType].length;
    }

    private static void intiBlockTextures() {

        NON_STANDARD_BLOCK_TEXTURE_INDICES[TORCH] = new byte[]{TORCH_TEXTURE};
        NON_STANDARD_BLOCK_TEXTURE_INDICES[FRONT_CREATOR_HEAD] = new byte[]{CREATOR_HEAD_FRONT_TEXTURE, CREATOR_HEAD_TOP_TEXTURE, CREATOR_HEAD_RIGHT_TEXTURE, CREATOR_HEAD_BACK_TEXTURE, CREATOR_HEAD_BOTTOM_TEXTURE, CREATOR_HEAD_LEFT_TEXTURE};
        NON_STANDARD_BLOCK_TEXTURE_INDICES[RIGHT_CREATOR_HEAD] = new byte[]{CREATOR_HEAD_LEFT_TEXTURE, CREATOR_HEAD_TOP_TEXTURE, CREATOR_HEAD_FRONT_TEXTURE, CREATOR_HEAD_RIGHT_TEXTURE, ROTATED_CREATOR_HEAD_BOTTOM_TEXTURE, CREATOR_HEAD_BACK_TEXTURE};
        NON_STANDARD_BLOCK_TEXTURE_INDICES[BACK_CREATOR_HEAD] = new byte[]{CREATOR_HEAD_BACK_TEXTURE, CREATOR_HEAD_TOP_TEXTURE, CREATOR_HEAD_LEFT_TEXTURE, CREATOR_HEAD_FRONT_TEXTURE, CREATOR_HEAD_BOTTOM_TEXTURE, CREATOR_HEAD_RIGHT_TEXTURE};
        NON_STANDARD_BLOCK_TEXTURE_INDICES[LEFT_CREATOR_HEAD] = new byte[]{CREATOR_HEAD_RIGHT_TEXTURE, CREATOR_HEAD_TOP_TEXTURE, CREATOR_HEAD_BACK_TEXTURE, CREATOR_HEAD_LEFT_TEXTURE, ROTATED_CREATOR_HEAD_BOTTOM_TEXTURE, CREATOR_HEAD_FRONT_TEXTURE};
        NON_STANDARD_BLOCK_TEXTURE_INDICES[CACTUS] = new byte[]{CACTUS_SIDE_TEXTURE, CACTUS_TOP_TEXTURE, CACTUS_SIDE_TEXTURE, CACTUS_SIDE_TEXTURE, CACTUS_TOP_TEXTURE, CACTUS_SIDE_TEXTURE};
        NON_STANDARD_BLOCK_TEXTURE_INDICES[WATER] = new byte[]{WATER_TEXTURE};
        NON_STANDARD_BLOCK_TEXTURE_INDICES[LAVA] = new byte[]{LAVA_TEXTURE};

        STANDARD_BLOCK_TEXTURE_INDICES[GRASS >> BLOCK_TYPE_BITS] = new byte[]{GRASS_SIDE_TEXTURE, GRASS_TOP_TEXTURE, GRASS_SIDE_TEXTURE, GRASS_SIDE_TEXTURE, DIRT_TEXTURE, GRASS_SIDE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[DIRT >> BLOCK_TYPE_BITS] = new byte[]{DIRT_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[STONE >> BLOCK_TYPE_BITS] = new byte[]{STONE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[MUD >> BLOCK_TYPE_BITS] = new byte[]{MUD_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[ANDESITE >> BLOCK_TYPE_BITS] = new byte[]{ANDESITE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[SNOW >> BLOCK_TYPE_BITS] = new byte[]{SNOW_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[SAND >> BLOCK_TYPE_BITS] = new byte[]{SAND_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[STONE_BRICK >> BLOCK_TYPE_BITS] = new byte[]{STONE_BRICK_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[SLATE >> BLOCK_TYPE_BITS] = new byte[]{SLATE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[GLASS >> BLOCK_TYPE_BITS] = new byte[]{GLASS_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[COBBLESTONE >> BLOCK_TYPE_BITS] = new byte[]{COBBLESTONE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[GRAVEL >> BLOCK_TYPE_BITS] = new byte[]{GRAVEL_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[POLISHED_STONE >> BLOCK_TYPE_BITS] = new byte[]{POLISHED_STONE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[COURSE_DIRT >> BLOCK_TYPE_BITS] = new byte[]{COURSE_DIRT_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[CHISELED_STONE >> BLOCK_TYPE_BITS] = new byte[]{CHISELED_STONE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[CHISELED_POLISHED_STONE >> BLOCK_TYPE_BITS] = new byte[]{CHISELED_POLISHED_STONE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[ICE >> BLOCK_TYPE_BITS] = new byte[]{ICE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[CLAY >> BLOCK_TYPE_BITS] = new byte[]{CLAY_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[MOSS >> BLOCK_TYPE_BITS] = new byte[]{MOSS_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[HEAVY_ICE >> BLOCK_TYPE_BITS] = new byte[]{HEAVY_ICE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[CHISELED_SLATE >> BLOCK_TYPE_BITS] = new byte[]{CHISELED_SLATE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[COAL_ORE >> BLOCK_TYPE_BITS] = new byte[]{COAL_ORE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[IRON_ORE >> BLOCK_TYPE_BITS] = new byte[]{IRON_ORE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[DIAMOND_ORE >> BLOCK_TYPE_BITS] = new byte[]{DIAMOND_ORE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[CRACKED_ANDESITE >> BLOCK_TYPE_BITS] = new byte[]{CRACKED_ANDESITE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[COBBLED_SLATE >> BLOCK_TYPE_BITS] = new byte[]{COBBLED_SLATE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[SLATE_BRICKS >> BLOCK_TYPE_BITS] = new byte[]{SLATE_BRICKS_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[POLISHED_SLATE >> BLOCK_TYPE_BITS] = new byte[]{POLISHED_SLATE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[SANDSTONE >> BLOCK_TYPE_BITS] = new byte[]{SANDSTONE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[POLISHED_SANDSTONE >> BLOCK_TYPE_BITS] = new byte[]{POLISHED_SANDSTONE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[OAK_PLANKS >> BLOCK_TYPE_BITS] = new byte[]{OAK_PLANKS_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[SPRUCE_PLANKS >> BLOCK_TYPE_BITS] = new byte[]{SPRUCE_PLANKS_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[DARK_OAK_PLANKS >> BLOCK_TYPE_BITS] = new byte[]{DARK_OAK_PLANKS_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[PINE_PLANKS >> BLOCK_TYPE_BITS] = new byte[]{PINE_PLANKS_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[REDWOOD_PLANKS >> BLOCK_TYPE_BITS] = new byte[]{REDWOOD_PLANKS_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[BLACK_WOOD_PLANKS >> BLOCK_TYPE_BITS] = new byte[]{BLACK_WOOD_PLANKS_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[OAK_LEAVES >> BLOCK_TYPE_BITS] = new byte[]{OAK_LEAVES_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[SPRUCE_LEAVES >> BLOCK_TYPE_BITS] = new byte[]{SPRUCE_LEAVES_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[DARK_OAK_LEAVES >> BLOCK_TYPE_BITS] = new byte[]{DARK_OAK_LEAVES_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[PINE_LEAVES >> BLOCK_TYPE_BITS] = new byte[]{PINE_LEAVES_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[REDWOOD_LEAVES >> BLOCK_TYPE_BITS] = new byte[]{REDWOOD_LEAVES_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[BLACK_WOOD_LEAVES >> BLOCK_TYPE_BITS] = new byte[]{BLACK_WOOD_LEAVES_TEXTURE};

        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(UP_DOWN_OAK_LOG) >> BLOCK_TYPE_BITS] = new byte[]{OAK_LOG_TEXTURE, OAK_LOG_TOP_TEXTURE, OAK_LOG_TEXTURE, OAK_LOG_TEXTURE, OAK_LOG_TOP_TEXTURE, OAK_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(FRONT_BACK_OAK_LOG) >> BLOCK_TYPE_BITS] = new byte[]{OAK_LOG_TOP_TEXTURE, ROTATED_OAK_LOG_TEXTURE, ROTATED_OAK_LOG_TEXTURE, OAK_LOG_TOP_TEXTURE, OAK_LOG_TEXTURE, ROTATED_OAK_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(LEFT_RIGHT_OAK_LOG) >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_OAK_LOG_TEXTURE, OAK_LOG_TEXTURE, OAK_LOG_TOP_TEXTURE, ROTATED_OAK_LOG_TEXTURE, ROTATED_OAK_LOG_TEXTURE, OAK_LOG_TOP_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(UP_DOWN_STRIPPED_OAK_LOG) >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_OAK_LOG_TEXTURE, STRIPPED_OAK_LOG_TOP_TEXTURE, STRIPPED_OAK_LOG_TEXTURE, STRIPPED_OAK_LOG_TEXTURE, STRIPPED_OAK_LOG_TOP_TEXTURE, STRIPPED_OAK_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(FRONT_BACK_STRIPPED_OAK_LOG) >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_OAK_LOG_TOP_TEXTURE, ROTATED_STRIPPED_OAK_LOG_TEXTURE, ROTATED_STRIPPED_OAK_LOG_TEXTURE, STRIPPED_OAK_LOG_TOP_TEXTURE, STRIPPED_OAK_LOG_TEXTURE, ROTATED_STRIPPED_OAK_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(LEFT_RIGHT_STRIPPED_OAK_LOG) >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_STRIPPED_OAK_LOG_TEXTURE, STRIPPED_OAK_LOG_TEXTURE, STRIPPED_OAK_LOG_TOP_TEXTURE, ROTATED_STRIPPED_OAK_LOG_TEXTURE, ROTATED_STRIPPED_OAK_LOG_TEXTURE, STRIPPED_OAK_LOG_TOP_TEXTURE};

        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(UP_DOWN_SPRUCE_LOG) >> BLOCK_TYPE_BITS] = new byte[]{SPRUCE_LOG_TEXTURE, SPRUCE_LOG_TOP_TEXTURE, SPRUCE_LOG_TEXTURE, SPRUCE_LOG_TEXTURE, SPRUCE_LOG_TOP_TEXTURE, SPRUCE_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(FRONT_BACK_SPRUCE_LOG) >> BLOCK_TYPE_BITS] = new byte[]{SPRUCE_LOG_TOP_TEXTURE, ROTATED_SPRUCE_LOG_TEXTURE, ROTATED_SPRUCE_LOG_TEXTURE, SPRUCE_LOG_TOP_TEXTURE, SPRUCE_LOG_TEXTURE, ROTATED_SPRUCE_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(LEFT_RIGHT_SPRUCE_LOG) >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_SPRUCE_LOG_TEXTURE, SPRUCE_LOG_TEXTURE, SPRUCE_LOG_TOP_TEXTURE, ROTATED_SPRUCE_LOG_TEXTURE, ROTATED_SPRUCE_LOG_TEXTURE, SPRUCE_LOG_TOP_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(UP_DOWN_STRIPPED_SPRUCE_LOG) >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_SPRUCE_LOG_TEXTURE, STRIPPED_SPRUCE_LOG_TOP_TEXTURE, STRIPPED_SPRUCE_LOG_TEXTURE, STRIPPED_SPRUCE_LOG_TEXTURE, STRIPPED_SPRUCE_LOG_TOP_TEXTURE, STRIPPED_SPRUCE_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(FRONT_BACK_STRIPPED_SPRUCE_LOG) >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_SPRUCE_LOG_TOP_TEXTURE, ROTATED_STRIPPED_SPRUCE_LOG_TEXTURE, ROTATED_STRIPPED_SPRUCE_LOG_TEXTURE, STRIPPED_SPRUCE_LOG_TOP_TEXTURE, STRIPPED_SPRUCE_LOG_TEXTURE, ROTATED_STRIPPED_SPRUCE_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(LEFT_RIGHT_STRIPPED_SPRUCE_LOG) >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_STRIPPED_SPRUCE_LOG_TEXTURE, STRIPPED_SPRUCE_LOG_TEXTURE, STRIPPED_SPRUCE_LOG_TOP_TEXTURE, ROTATED_STRIPPED_SPRUCE_LOG_TEXTURE, ROTATED_STRIPPED_SPRUCE_LOG_TEXTURE, STRIPPED_SPRUCE_LOG_TOP_TEXTURE};

        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(UP_DOWN_DARK_OAK_LOG) >> BLOCK_TYPE_BITS] = new byte[]{DARK_OAK_LOG_TEXTURE, DARK_OAK_LOG_TOP_TEXTURE, DARK_OAK_LOG_TEXTURE, DARK_OAK_LOG_TEXTURE, DARK_OAK_LOG_TOP_TEXTURE, DARK_OAK_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(FRONT_BACK_DARK_OAK_LOG) >> BLOCK_TYPE_BITS] = new byte[]{DARK_OAK_LOG_TOP_TEXTURE, ROTATED_DARK_OAK_LOG_TEXTURE, ROTATED_DARK_OAK_LOG_TEXTURE, DARK_OAK_LOG_TOP_TEXTURE, DARK_OAK_LOG_TEXTURE, ROTATED_DARK_OAK_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(LEFT_RIGHT_DARK_OAK_LOG) >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_DARK_OAK_LOG_TEXTURE, DARK_OAK_LOG_TEXTURE, DARK_OAK_LOG_TOP_TEXTURE, ROTATED_DARK_OAK_LOG_TEXTURE, ROTATED_DARK_OAK_LOG_TEXTURE, DARK_OAK_LOG_TOP_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(UP_DOWN_STRIPPED_DARK_OAK_LOG) >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_DARK_OAK_LOG_TEXTURE, STRIPPED_DARK_OAK_LOG_TOP_TEXTURE, STRIPPED_DARK_OAK_LOG_TEXTURE, STRIPPED_DARK_OAK_LOG_TEXTURE, STRIPPED_DARK_OAK_LOG_TOP_TEXTURE, STRIPPED_DARK_OAK_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(FRONT_BACK_STRIPPED_DARK_OAK_LOG) >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_DARK_OAK_LOG_TOP_TEXTURE, ROTATED_STRIPPED_DARK_OAK_LOG_TEXTURE, ROTATED_STRIPPED_DARK_OAK_LOG_TEXTURE, STRIPPED_DARK_OAK_LOG_TOP_TEXTURE, STRIPPED_DARK_OAK_LOG_TEXTURE, ROTATED_STRIPPED_DARK_OAK_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(LEFT_RIGHT_STRIPPED_DARK_OAK_LOG) >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_STRIPPED_DARK_OAK_LOG_TEXTURE, STRIPPED_DARK_OAK_LOG_TEXTURE, STRIPPED_DARK_OAK_LOG_TOP_TEXTURE, ROTATED_STRIPPED_DARK_OAK_LOG_TEXTURE, ROTATED_STRIPPED_DARK_OAK_LOG_TEXTURE, STRIPPED_DARK_OAK_LOG_TOP_TEXTURE};

        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(UP_DOWN_PINE_LOG) >> BLOCK_TYPE_BITS] = new byte[]{PINE_LOG_TEXTURE, PINE_LOG_TOP_TEXTURE, PINE_LOG_TEXTURE, PINE_LOG_TEXTURE, PINE_LOG_TOP_TEXTURE, PINE_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(FRONT_BACK_PINE_LOG) >> BLOCK_TYPE_BITS] = new byte[]{PINE_LOG_TOP_TEXTURE, ROTATED_PINE_LOG_TEXTURE, ROTATED_PINE_LOG_TEXTURE, PINE_LOG_TOP_TEXTURE, PINE_LOG_TEXTURE, ROTATED_PINE_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(LEFT_RIGHT_PINE_LOG) >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_PINE_LOG_TEXTURE, PINE_LOG_TEXTURE, PINE_LOG_TOP_TEXTURE, ROTATED_PINE_LOG_TEXTURE, ROTATED_PINE_LOG_TEXTURE, PINE_LOG_TOP_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(UP_DOWN_STRIPPED_PINE_LOG) >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_PINE_LOG_TEXTURE, STRIPPED_PINE_LOG_TOP_TEXTURE, STRIPPED_PINE_LOG_TEXTURE, STRIPPED_PINE_LOG_TEXTURE, STRIPPED_PINE_LOG_TOP_TEXTURE, STRIPPED_PINE_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(FRONT_BACK_STRIPPED_PINE_LOG) >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_PINE_LOG_TOP_TEXTURE, ROTATED_STRIPPED_PINE_LOG_TEXTURE, ROTATED_STRIPPED_PINE_LOG_TEXTURE, STRIPPED_PINE_LOG_TOP_TEXTURE, STRIPPED_PINE_LOG_TEXTURE, ROTATED_STRIPPED_PINE_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(LEFT_RIGHT_STRIPPED_PINE_LOG) >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_STRIPPED_PINE_LOG_TEXTURE, STRIPPED_PINE_LOG_TEXTURE, STRIPPED_PINE_LOG_TOP_TEXTURE, ROTATED_STRIPPED_PINE_LOG_TEXTURE, ROTATED_STRIPPED_PINE_LOG_TEXTURE, STRIPPED_PINE_LOG_TOP_TEXTURE};

        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(UP_DOWN_REDWOOD_LOG) >> BLOCK_TYPE_BITS] = new byte[]{REDWOOD_LOG_TEXTURE, REDWOOD_LOG_TOP_TEXTURE, REDWOOD_LOG_TEXTURE, REDWOOD_LOG_TEXTURE, REDWOOD_LOG_TOP_TEXTURE, REDWOOD_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(FRONT_BACK_REDWOOD_LOG) >> BLOCK_TYPE_BITS] = new byte[]{REDWOOD_LOG_TOP_TEXTURE, ROTATED_REDWOOD_LOG_TEXTURE, ROTATED_REDWOOD_LOG_TEXTURE, REDWOOD_LOG_TOP_TEXTURE, REDWOOD_LOG_TEXTURE, ROTATED_REDWOOD_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(LEFT_RIGHT_REDWOOD_LOG) >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_REDWOOD_LOG_TEXTURE, REDWOOD_LOG_TEXTURE, REDWOOD_LOG_TOP_TEXTURE, ROTATED_REDWOOD_LOG_TEXTURE, ROTATED_REDWOOD_LOG_TEXTURE, REDWOOD_LOG_TOP_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(UP_DOWN_STRIPPED_REDWOOD_LOG) >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_REDWOOD_LOG_TEXTURE, STRIPPED_REDWOOD_LOG_TOP_TEXTURE, STRIPPED_REDWOOD_LOG_TEXTURE, STRIPPED_REDWOOD_LOG_TEXTURE, STRIPPED_REDWOOD_LOG_TOP_TEXTURE, STRIPPED_REDWOOD_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(FRONT_BACK_STRIPPED_REDWOOD_LOG) >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_REDWOOD_LOG_TOP_TEXTURE, ROTATED_STRIPPED_REDWOOD_LOG_TEXTURE, ROTATED_STRIPPED_REDWOOD_LOG_TEXTURE, STRIPPED_REDWOOD_LOG_TOP_TEXTURE, STRIPPED_REDWOOD_LOG_TEXTURE, ROTATED_STRIPPED_REDWOOD_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(LEFT_RIGHT_STRIPPED_REDWOOD_LOG) >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_STRIPPED_REDWOOD_LOG_TEXTURE, STRIPPED_REDWOOD_LOG_TEXTURE, STRIPPED_REDWOOD_LOG_TOP_TEXTURE, ROTATED_STRIPPED_REDWOOD_LOG_TEXTURE, ROTATED_STRIPPED_REDWOOD_LOG_TEXTURE, STRIPPED_REDWOOD_LOG_TOP_TEXTURE};

        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(UP_DOWN_BLACK_WOOD_LOG) >> BLOCK_TYPE_BITS] = new byte[]{BLACK_WOOD_LOG_TEXTURE, BLACK_WOOD_LOG_TOP_TEXTURE, BLACK_WOOD_LOG_TEXTURE, BLACK_WOOD_LOG_TEXTURE, BLACK_WOOD_LOG_TOP_TEXTURE, BLACK_WOOD_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(FRONT_BACK_BLACK_WOOD_LOG) >> BLOCK_TYPE_BITS] = new byte[]{BLACK_WOOD_LOG_TOP_TEXTURE, ROTATED_BLACK_WOOD_LOG_TEXTURE, ROTATED_BLACK_WOOD_LOG_TEXTURE, BLACK_WOOD_LOG_TOP_TEXTURE, BLACK_WOOD_LOG_TEXTURE, ROTATED_BLACK_WOOD_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(LEFT_RIGHT_BLACK_WOOD_LOG) >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_BLACK_WOOD_LOG_TEXTURE, BLACK_WOOD_LOG_TEXTURE, BLACK_WOOD_LOG_TOP_TEXTURE, ROTATED_BLACK_WOOD_LOG_TEXTURE, ROTATED_BLACK_WOOD_LOG_TEXTURE, BLACK_WOOD_LOG_TOP_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(UP_DOWN_STRIPPED_BLACK_WOOD_LOG) >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_BLACK_WOOD_LOG_TEXTURE, STRIPPED_BLACK_WOOD_LOG_TOP_TEXTURE, STRIPPED_BLACK_WOOD_LOG_TEXTURE, STRIPPED_BLACK_WOOD_LOG_TEXTURE, STRIPPED_BLACK_WOOD_LOG_TOP_TEXTURE, STRIPPED_BLACK_WOOD_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(FRONT_BACK_STRIPPED_BLACK_WOOD_LOG) >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_BLACK_WOOD_LOG_TOP_TEXTURE, ROTATED_STRIPPED_BLACK_WOOD_LOG_TEXTURE, ROTATED_STRIPPED_BLACK_WOOD_LOG_TEXTURE, STRIPPED_BLACK_WOOD_LOG_TOP_TEXTURE, STRIPPED_BLACK_WOOD_LOG_TEXTURE, ROTATED_STRIPPED_BLACK_WOOD_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(LEFT_RIGHT_STRIPPED_BLACK_WOOD_LOG) >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_STRIPPED_BLACK_WOOD_LOG_TEXTURE, STRIPPED_BLACK_WOOD_LOG_TEXTURE, STRIPPED_BLACK_WOOD_LOG_TOP_TEXTURE, ROTATED_STRIPPED_BLACK_WOOD_LOG_TEXTURE, ROTATED_STRIPPED_BLACK_WOOD_LOG_TEXTURE, STRIPPED_BLACK_WOOD_LOG_TOP_TEXTURE};

        STANDARD_BLOCK_TEXTURE_INDICES[BLACK >> BLOCK_TYPE_BITS] = new byte[]{BLACK_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[WHITE >> BLOCK_TYPE_BITS] = new byte[]{WHITE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[CYAN >> BLOCK_TYPE_BITS] = new byte[]{CYAN_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[MAGENTA >> BLOCK_TYPE_BITS] = new byte[]{MAGENTA_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[YELLOW >> BLOCK_TYPE_BITS] = new byte[]{YELLOW_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[BLUE >> BLOCK_TYPE_BITS] = new byte[]{BLUE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[GREEN >> BLOCK_TYPE_BITS] = new byte[]{GREEN_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[RED >> BLOCK_TYPE_BITS] = new byte[]{RED_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[CRAFTING_TABLE >> BLOCK_TYPE_BITS] = new byte[]{CRAFTING_TABLE_SIDE_TEXTURE_1, CRAFTING_TABLE_TOP_TEXTURE, CRAFTING_TABLE_SIDE_TEXTURE_2, CRAFTING_TABLE_SIDE_TEXTURE_1, SPRUCE_PLANKS_TEXTURE, CRAFTING_TABLE_SIDE_TEXTURE_2};
        STANDARD_BLOCK_TEXTURE_INDICES[TNT >> BLOCK_TYPE_BITS] = new byte[]{TNT_SIDE_TEXTURE, TNT_TOP_TEXTURE, TNT_SIDE_TEXTURE, TNT_SIDE_TEXTURE, TNT_BOTTOM_TEXTURE, TNT_SIDE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[MOSSY_COBBLESTONE >> BLOCK_TYPE_BITS] = new byte[]{MOSSY_COBBLESTONE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[MOSSY_CRACKED_ANDESITE >> BLOCK_TYPE_BITS] = new byte[]{MOSSY_CRACKED_ANDESITE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[OBSIDIAN >> BLOCK_TYPE_BITS] = new byte[]{OBSIDIAN_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[MOSSY_OBSIDIAN >> BLOCK_TYPE_BITS] = new byte[]{MOSSY_OBSIDIAN_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[MOSSY_GRAVEL >> BLOCK_TYPE_BITS] = new byte[]{MOSSY_GRAVEL_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[MOSSY_DIRT >> BLOCK_TYPE_BITS] = new byte[]{MOSSY_DIRT_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[MOSSY_POLISHED_SLATE >> BLOCK_TYPE_BITS] = new byte[]{MOSSY_POLISHED_SLATE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[MOSSY_CHISELED_SLATE >> BLOCK_TYPE_BITS] = new byte[]{MOSSY_CHISELED_SLATE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[MOSSY_SLATE_BRICKS >> BLOCK_TYPE_BITS] = new byte[]{MOSSY_SLATE_BRICKS_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[MOSSY_COBBLED_SLATE >> BLOCK_TYPE_BITS] = new byte[]{MOSSY_COBBLED_SLATE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[MOSSY_SLATE >> BLOCK_TYPE_BITS] = new byte[]{MOSSY_SLATE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[MOSSY_CHISELED_STONE >> BLOCK_TYPE_BITS] = new byte[]{MOSSY_CHISELED_STONE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[MOSSY_CHISELED_POLISHED_STONE >> BLOCK_TYPE_BITS] = new byte[]{MOSSY_CHISELED_POLISHED_STONE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[MOSSY_POLISHED_STONE >> BLOCK_TYPE_BITS] = new byte[]{MOSSY_POLISHED_STONE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[MOSSY_STONE_BRICK >> BLOCK_TYPE_BITS] = new byte[]{MOSSY_STONE_BRICK_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[MOSSY_ANDESITE >> BLOCK_TYPE_BITS] = new byte[]{MOSSY_ANDESITE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[MOSSY_STONE >> BLOCK_TYPE_BITS] = new byte[]{MOSSY_STONE_TEXTURE};

        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(FRONT_FURNACE) >> BLOCK_TYPE_BITS] = new byte[]{FURNACE_FRONT_TEXTURE, FURNACE_TOP_TEXTURE, FURNACE_SIDE_TEXTURE, FURNACE_SIDE_TEXTURE, FURNACE_BOTTOM_TEXTURE, FURNACE_SIDE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(BACK_FURNACE) >> BLOCK_TYPE_BITS] = new byte[]{FURNACE_SIDE_TEXTURE, FURNACE_TOP_TEXTURE, FURNACE_SIDE_TEXTURE, FURNACE_FRONT_TEXTURE, FURNACE_BOTTOM_TEXTURE, FURNACE_SIDE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(RIGHT_FURNACE) >> BLOCK_TYPE_BITS] = new byte[]{FURNACE_SIDE_TEXTURE, FURNACE_TOP_TEXTURE, FURNACE_FRONT_TEXTURE, FURNACE_SIDE_TEXTURE, FURNACE_BOTTOM_TEXTURE, FURNACE_SIDE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[Short.toUnsignedInt(LEFT_FURNACE) >> BLOCK_TYPE_BITS] = new byte[]{FURNACE_SIDE_TEXTURE, FURNACE_TOP_TEXTURE, FURNACE_SIDE_TEXTURE, FURNACE_SIDE_TEXTURE, FURNACE_BOTTOM_TEXTURE, FURNACE_FRONT_TEXTURE};

    }

    private static void initBlockProperties() {
        NON_STANDARD_BLOCK_PROPERTIES[AIR] = NO_COLLISION | REPLACEABLE;
        NON_STANDARD_BLOCK_PROPERTIES[WATER] = NO_COLLISION | REPLACEABLE | BLAST_RESISTANT;
        NON_STANDARD_BLOCK_PROPERTIES[LAVA] = LIGHT_EMITTING | NO_COLLISION | REPLACEABLE | BLAST_RESISTANT;
        NON_STANDARD_BLOCK_PROPERTIES[TORCH] = LIGHT_EMITTING | NO_COLLISION;

        STANDARD_BLOCK_PROPERTIES[WHITE >> BLOCK_TYPE_BITS] = LIGHT_EMITTING;
        STANDARD_BLOCK_PROPERTIES[MOSSY_OBSIDIAN >> BLOCK_TYPE_BITS] = BLAST_RESISTANT;
        STANDARD_BLOCK_PROPERTIES[OBSIDIAN >> BLOCK_TYPE_BITS] = BLAST_RESISTANT;
        STANDARD_BLOCK_PROPERTIES[TNT >> BLOCK_TYPE_BITS] = INTERACTABLE;
        STANDARD_BLOCK_PROPERTIES[CRAFTING_TABLE >> BLOCK_TYPE_BITS] = INTERACTABLE;
        STANDARD_BLOCK_PROPERTIES[Short.toUnsignedInt(FRONT_FURNACE) >> BLOCK_TYPE_BITS] = INTERACTABLE;
        STANDARD_BLOCK_PROPERTIES[Short.toUnsignedInt(BACK_FURNACE) >> BLOCK_TYPE_BITS] = INTERACTABLE;
        STANDARD_BLOCK_PROPERTIES[Short.toUnsignedInt(RIGHT_FURNACE) >> BLOCK_TYPE_BITS] = INTERACTABLE;
        STANDARD_BLOCK_PROPERTIES[Short.toUnsignedInt(LEFT_FURNACE) >> BLOCK_TYPE_BITS] = INTERACTABLE;
    }

    private static void initNonStandardBlockTypes() {
        NON_STANDARD_BLOCK_TYPE[AIR] = AIR_TYPE;
        NON_STANDARD_BLOCK_TYPE[OUT_OF_WORLD] = FULL_BLOCK;

        NON_STANDARD_BLOCK_TYPE[WATER] = LIQUID_TYPE;
        NON_STANDARD_BLOCK_TYPE[LAVA] = LIQUID_TYPE;
        NON_STANDARD_BLOCK_TYPE[CACTUS] = CACTUS_TYPE;
        NON_STANDARD_BLOCK_TYPE[FRONT_CREATOR_HEAD] = PLAYER_HEAD;
        NON_STANDARD_BLOCK_TYPE[RIGHT_CREATOR_HEAD] = PLAYER_HEAD;
        NON_STANDARD_BLOCK_TYPE[BACK_CREATOR_HEAD] = PLAYER_HEAD;
        NON_STANDARD_BLOCK_TYPE[LEFT_CREATOR_HEAD] = PLAYER_HEAD;
        NON_STANDARD_BLOCK_TYPE[TORCH] = TORCH_TYPE;
    }

    private static void initBlockTypeData() {

        BLOCK_TYPE_DATA[LEAVE_TYPE] = OCCLUDES_SELF;
        BLOCK_TYPE_DATA[GLASS_TYPE] = OCCLUDES_SELF;
        BLOCK_TYPE_DATA[LIQUID_TYPE] = (byte) (DYNAMIC_SHAPE_MASK | OCCLUDES_DYNAMIC_SELF);
        BLOCK_TYPE_DATA[CACTUS_TYPE] = (byte) (DYNAMIC_SHAPE_MASK | OCCLUDES_ALL);

        for (int upDownFenceType = UP_DOWN_FENCE; upDownFenceType <= UP_DOWN_FENCE_FRONT_RIGHT_BACK_LEFT; upDownFenceType++)
            BLOCK_TYPE_DATA[upDownFenceType] = SMART_BLOCK_TYPE | OCCLUDES_ALL;
        for (int leftRightFenceType = LEFT_RIGHT_FENCE; leftRightFenceType <= LEFT_RIGHT_FENCE_FRONT_UP_BACK_DOWN; leftRightFenceType++)
            BLOCK_TYPE_DATA[leftRightFenceType] = SMART_BLOCK_TYPE | OCCLUDES_ALL;
        for (int frontBackFenceType = FRONT_BACK_FENCE; frontBackFenceType <= FRONT_BACK_FENCE_UP_RIGHT_DOWN_LEFT; frontBackFenceType++)
            BLOCK_TYPE_DATA[frontBackFenceType] = SMART_BLOCK_TYPE | OCCLUDES_ALL;

        for (int blockType = 0; blockType < TOTAL_AMOUNT_OF_BLOCK_TYPES; blockType++) {
            byte[] XYZSubData = BLOCK_TYPE_XYZ_SUB_DATA[blockType];
            long[] occlusionData = new long[XYZSubData.length];

            for (int aabbIndex = 0; aabbIndex < XYZSubData.length; aabbIndex += 6) {
                if (XYZSubData[MIN_X + aabbIndex] == 0)
                    fillOcclusionBits(occlusionData, LEFT + aabbIndex, XYZSubData[MIN_Z + aabbIndex], XYZSubData[MAX_Z + aabbIndex], XYZSubData[MIN_Y + aabbIndex], XYZSubData[MAX_Y + aabbIndex]);
                if (XYZSubData[MAX_X + aabbIndex] == 0)
                    fillOcclusionBits(occlusionData, RIGHT + aabbIndex, XYZSubData[MIN_Z + aabbIndex], XYZSubData[MAX_Z + aabbIndex], XYZSubData[MIN_Y + aabbIndex], XYZSubData[MAX_Y + aabbIndex]);

                if (XYZSubData[MIN_Y + aabbIndex] == 0)
                    fillOcclusionBits(occlusionData, BOTTOM + aabbIndex, XYZSubData[MIN_Z + aabbIndex], XYZSubData[MAX_Z + aabbIndex], XYZSubData[MIN_X + aabbIndex], XYZSubData[MAX_X + aabbIndex]);
                if (XYZSubData[MAX_Y + aabbIndex] == 0)
                    fillOcclusionBits(occlusionData, TOP + aabbIndex, XYZSubData[MIN_Z + aabbIndex], XYZSubData[MAX_Z + aabbIndex], XYZSubData[MIN_X + aabbIndex], XYZSubData[MAX_X + aabbIndex]);

                if (XYZSubData[MIN_Z + aabbIndex] == 0)
                    fillOcclusionBits(occlusionData, BACK + aabbIndex, XYZSubData[MIN_X + aabbIndex], XYZSubData[MAX_X + aabbIndex], XYZSubData[MIN_Y + aabbIndex], XYZSubData[MAX_Y + aabbIndex]);
                if (XYZSubData[MAX_Z + aabbIndex] == 0)
                    fillOcclusionBits(occlusionData, FRONT + aabbIndex, XYZSubData[MIN_X + aabbIndex], XYZSubData[MAX_X + aabbIndex], XYZSubData[MIN_Y + aabbIndex], XYZSubData[MAX_Y + aabbIndex]);
            }
            BLOCK_TYPE_OCCLUSION_DATA[blockType] = occlusionData;
        }
    }

    private static void fillOcclusionBits(long[] occlusionData, int index, int minX, int maxX, int minY, int maxY) {
        minX = minX >> 1;
        maxX = (16 + maxX) >> 1;
        minY = minY >> 1;
        maxY = (16 + maxY) >> 1;

        long value = 0;
        for (int x = minX; x < maxX; x++)
            for (int y = minY; y < maxY; y++) value |= 1L << y * 8 + x;

        occlusionData[index] = value;
    }

    private static void initXYZSubData() {
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE] = new byte[]{4, -4, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_FRONT] = new byte[]{4, -4, 0, 0, 4, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_RIGHT] = new byte[]{4, 0, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_FRONT_RIGHT] = new byte[]{4, -4, 0, 0, 4, 0, 12, 0, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_BACK] = new byte[]{4, -4, 0, 0, 0, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_FRONT_BACK] = new byte[]{4, -4, 0, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_RIGHT_BACK] = new byte[]{4, 0, 0, 0, 4, -4, 4, -4, 0, 0, 0, -12};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_FRONT_RIGHT_BACK] = new byte[]{4, -4, 0, 0, 0, 0, 12, 0, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_LEFT] = new byte[]{0, -4, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_FRONT_LEFT] = new byte[]{4, -4, 0, 0, 4, 0, 0, -12, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_RIGHT_LEFT] = new byte[]{0, 0, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_FRONT_RIGHT_LEFT] = new byte[]{0, 0, 0, 0, 4, -4, 4, -4, 0, 0, 12, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_BACK_LEFT] = new byte[]{4, -4, 0, 0, 0, -4, 0, -12, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_FRONT_BACK_LEFT] = new byte[]{4, -4, 0, 0, 0, 0, 0, -12, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_RIGHT_BACK_LEFT] = new byte[]{0, 0, 0, 0, 4, -4, 4, -4, 0, 0, 0, -12};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_FRONT_RIGHT_BACK_LEFT] = new byte[]{0, 0, 0, 0, 4, -4, 4, -4, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_RIGHT_FENCE] = new byte[]{0, 0, 4, -4, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_RIGHT_FENCE_FRONT] = new byte[]{0, 0, 4, -4, 4, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_RIGHT_FENCE_UP] = new byte[]{0, 0, 4, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_RIGHT_FENCE_FRONT_UP] = new byte[]{0, 0, 4, -4, 4, 0, 0, 0, 12, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_RIGHT_FENCE_BACK] = new byte[]{0, 0, 4, -4, 0, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_RIGHT_FENCE_FRONT_BACK] = new byte[]{0, 0, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_RIGHT_FENCE_UP_BACK] = new byte[]{0, 0, 4, 0, 4, -4, 0, 0, 4, -4, 0, -12};
        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_RIGHT_FENCE_FRONT_UP_BACK] = new byte[]{0, 0, 4, -4, 0, 0, 0, 0, 12, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_RIGHT_FENCE_DOWN] = new byte[]{0, 0, 0, -4, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_RIGHT_FENCE_FRONT_DOWN] = new byte[]{0, 0, 4, -4, 4, 0, 0, 0, 0, -12, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_RIGHT_FENCE_UP_DOWN] = new byte[]{0, 0, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_RIGHT_FENCE_FRONT_UP_DOWN] = new byte[]{0, 0, 0, 0, 4, -4, 0, 0, 4, -4, 12, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_RIGHT_FENCE_BACK_DOWN] = new byte[]{0, 0, 4, -4, 0, -4, 0, 0, 0, -12, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_RIGHT_FENCE_FRONT_BACK_DOWN] = new byte[]{0, 0, 4, -4, 0, 0, 0, 0, 0, -12, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_RIGHT_FENCE_UP_BACK_DOWN] = new byte[]{0, 0, 0, 0, 4, -4, 0, 0, 4, -4, 0, -12};
        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_RIGHT_FENCE_FRONT_UP_BACK_DOWN] = new byte[]{0, 0, 0, 0, 4, -4, 0, 0, 4, -4, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_BACK_FENCE] = new byte[]{4, -4, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_BACK_FENCE_UP] = new byte[]{4, -4, 4, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_BACK_FENCE_RIGHT] = new byte[]{4, 0, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_BACK_FENCE_UP_RIGHT] = new byte[]{4, -4, 4, 0, 0, 0, 12, 0, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_BACK_FENCE_DOWN] = new byte[]{4, -4, 0, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_BACK_FENCE_UP_DOWN] = new byte[]{4, -4, 0, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_BACK_FENCE_RIGHT_DOWN] = new byte[]{4, 0, 4, -4, 0, 0, 4, -4, 0, -12, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_BACK_FENCE_UP_RIGHT_DOWN] = new byte[]{4, -4, 0, 0, 0, 0, 12, 0, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_BACK_FENCE_LEFT] = new byte[]{0, -4, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_BACK_FENCE_UP_LEFT] = new byte[]{4, -4, 4, 0, 0, 0, 0, -12, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_BACK_FENCE_RIGHT_LEFT] = new byte[]{0, 0, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_BACK_FENCE_UP_RIGHT_LEFT] = new byte[]{0, 0, 4, -4, 0, 0, 4, -4, 12, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_BACK_FENCE_DOWN_LEFT] = new byte[]{4, -4, 0, -4, 0, 0, 0, -12, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_BACK_FENCE_UP_DOWN_LEFT] = new byte[]{4, -4, 0, 0, 0, 0, 0, -12, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_BACK_FENCE_RIGHT_DOWN_LEFT] = new byte[]{0, 0, 4, -4, 0, 0, 4, -4, 0, -12, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_BACK_FENCE_UP_RIGHT_DOWN_LEFT] = new byte[]{0, 0, 4, -4, 0, 0, 4, -4, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[TORCH_TYPE] = new byte[]{7, -7, 0, -4, 7, -7};

        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_RIGHT_STAIR] = new byte[]{8, 0, 0, 0, 0, -8, 0, 0, 0, 0, 8, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_LEFT_STAIR] = new byte[]{0, -8, 0, 0, 0, -8, 0, 0, 0, 0, 8, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[BACK_RIGHT_STAIR] = new byte[]{8, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, -8};
        BLOCK_TYPE_XYZ_SUB_DATA[BACK_LEFT_STAIR] = new byte[]{0, -8, 0, 0, 8, 0, 0, 0, 0, 0, 0, -8};
        BLOCK_TYPE_XYZ_SUB_DATA[BOTTOM_FRONT_STAIR] = new byte[]{0, 0, 0, -8, 0, 0, 0, 0, 8, 0, 8, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[BOTTOM_RIGHT_STAIR] = new byte[]{0, 0, 0, -8, 0, 0, 8, 0, 8, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[BOTTOM_BACK_STAIR] = new byte[]{0, 0, 0, -8, 0, 0, 0, 0, 8, 0, 0, -8};
        BLOCK_TYPE_XYZ_SUB_DATA[BOTTOM_LEFT_STAIR] = new byte[]{0, 0, 0, -8, 0, 0, 0, -8, 8, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[TOP_FRONT_STAIR] = new byte[]{0, 0, 8, 0, 0, 0, 0, 0, 0, -8, 8, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[TOP_RIGHT_STAIR] = new byte[]{0, 0, 8, 0, 0, 0, 8, 0, 0, -8, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[TOP_BACK_STAIR] = new byte[]{0, 0, 8, 0, 0, 0, 0, 0, 0, -8, 0, -8};
        BLOCK_TYPE_XYZ_SUB_DATA[TOP_LEFT_STAIR] = new byte[]{0, 0, 8, 0, 0, 0, 0, -8, 0, -8, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[THIN_FRONT_RIGHT_STAIR] = new byte[]{12, 0, 0, 0, 0, -4, 0, 0, 0, 0, 12, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THIN_FRONT_LEFT_STAIR] = new byte[]{0, -12, 0, 0, 0, -4, 0, 0, 0, 0, 12, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THIN_BACK_RIGHT_STAIR] = new byte[]{12, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, -12};
        BLOCK_TYPE_XYZ_SUB_DATA[THIN_BACK_LEFT_STAIR] = new byte[]{0, -12, 0, 0, 4, 0, 0, 0, 0, 0, 0, -12};
        BLOCK_TYPE_XYZ_SUB_DATA[THIN_BOTTOM_FRONT_STAIR] = new byte[]{0, 0, 0, -12, 0, 0, 0, 0, 4, 0, 12, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THIN_BOTTOM_RIGHT_STAIR] = new byte[]{0, 0, 0, -12, 0, 0, 12, 0, 4, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THIN_BOTTOM_BACK_STAIR] = new byte[]{0, 0, 0, -12, 0, 0, 0, 0, 4, 0, 0, -12};
        BLOCK_TYPE_XYZ_SUB_DATA[THIN_BOTTOM_LEFT_STAIR] = new byte[]{0, 0, 0, -12, 0, 0, 0, -12, 4, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THIN_TOP_FRONT_STAIR] = new byte[]{0, 0, 12, 0, 0, 0, 0, 0, 0, -4, 12, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THIN_TOP_RIGHT_STAIR] = new byte[]{0, 0, 12, 0, 0, 0, 12, 0, 0, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THIN_TOP_BACK_STAIR] = new byte[]{0, 0, 12, 0, 0, 0, 0, 0, 0, -4, 0, -12};
        BLOCK_TYPE_XYZ_SUB_DATA[THIN_TOP_LEFT_STAIR] = new byte[]{0, 0, 12, 0, 0, 0, 0, -12, 0, -4, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[THICK_FRONT_RIGHT_STAIR] = new byte[]{4, 0, 0, 0, 0, -12, 0, 0, 0, 0, 4, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THICK_FRONT_LEFT_STAIR] = new byte[]{0, -4, 0, 0, 0, -12, 0, 0, 0, 0, 4, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THICK_BACK_RIGHT_STAIR] = new byte[]{4, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[THICK_BACK_LEFT_STAIR] = new byte[]{0, -4, 0, 0, 12, 0, 0, 0, 0, 0, 0, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[THICK_BOTTOM_FRONT_STAIR] = new byte[]{0, 0, 0, -4, 0, 0, 0, 0, 12, 0, 4, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THICK_BOTTOM_RIGHT_STAIR] = new byte[]{0, 0, 0, -4, 0, 0, 4, 0, 12, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THICK_BOTTOM_BACK_STAIR] = new byte[]{0, 0, 0, -4, 0, 0, 0, 0, 12, 0, 0, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[THICK_BOTTOM_LEFT_STAIR] = new byte[]{0, 0, 0, -4, 0, 0, 0, -4, 12, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THICK_TOP_FRONT_STAIR] = new byte[]{0, 0, 4, 0, 0, 0, 0, 0, 0, -12, 4, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THICK_TOP_RIGHT_STAIR] = new byte[]{0, 0, 4, 0, 0, 0, 4, 0, 0, -12, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THICK_TOP_BACK_STAIR] = new byte[]{0, 0, 4, 0, 0, 0, 0, 0, 0, -12, 0, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[THICK_TOP_LEFT_STAIR] = new byte[]{0, 0, 4, 0, 0, 0, 0, -4, 0, -12, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[CACTUS_TYPE] = new byte[]{1, -1, 0, 0, 1, -1};

        BLOCK_TYPE_XYZ_SUB_DATA[PLAYER_HEAD] = new byte[]{4, -4, 0, -8, 4, -4};

        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_WALL] = new byte[]{0, 0, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_BACK_WALL] = new byte[]{0, 0, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_RIGHT_WALL] = new byte[]{4, -4, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_RIGHT_POST] = new byte[]{0, 0, 4, -4, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_BACK_POST] = new byte[]{4, -4, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_POST] = new byte[]{4, -4, 0, 0, 4, -4};

        BLOCK_TYPE_XYZ_SUB_DATA[BOTTOM_SLAB] = new byte[]{0, 0, 0, -8, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[TOP_SLAB] = new byte[]{0, 0, 8, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_SLAB] = new byte[]{0, 0, 0, 0, 8, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[BACK_SLAB] = new byte[]{0, 0, 0, 0, 0, -8};
        BLOCK_TYPE_XYZ_SUB_DATA[RIGHT_SLAB] = new byte[]{8, 0, 0, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_SLAB] = new byte[]{0, -8, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[BOTTOM_SOCKET] = new byte[]{0, 0, 0, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[TOP_SOCKET] = new byte[]{0, 0, 4, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_SOCKET] = new byte[]{0, 0, 0, 0, 4, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[BACK_SOCKET] = new byte[]{0, 0, 0, 0, 0, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[RIGHT_SOCKET] = new byte[]{4, 0, 0, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_SOCKET] = new byte[]{0, -4, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[BOTTOM_PLATE] = new byte[]{0, 0, 0, -12, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[TOP_PLATE] = new byte[]{0, 0, 12, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_PLATE] = new byte[]{0, 0, 0, 0, 12, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[BACK_PLATE] = new byte[]{0, 0, 0, 0, 0, -12};
        BLOCK_TYPE_XYZ_SUB_DATA[RIGHT_PLATE] = new byte[]{12, 0, 0, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_PLATE] = new byte[]{0, -12, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[FULL_BLOCK] = new byte[]{0, 0, 0, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[LEAVE_TYPE] = new byte[]{0, 0, 0, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[GLASS_TYPE] = new byte[]{0, 0, 0, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[LIQUID_TYPE] = new byte[]{0, 0, 0, 0, 0, 0};
    }

    private static void initUVSubData() {
        BLOCK_TYPE_UV_SUB_DATA[PLAYER_HEAD] = new byte[]{4, 4, -4, 4, 4, -4, -4, -4, 4, 4, -4, 4, 4, -4, -4, -4, 4, 4, -4, 4, 4, -4, -4, -4, 4, 4, -4, 4, 4, -4, -4, -4, -4, -4, -4, 4, 4, -4, 4, 4, -4, 4, 4, 4, -4, -4, 4, -4};
        BLOCK_TYPE_UV_SUB_DATA[TORCH_TYPE] = new byte[]{7, 4, -7, 4, 7, 0, -7, 0, 7, 4, -7, 4, 7, -10, -7, -10, 7, 4, -7, 4, 7, 0, -7, 0, 7, 4, -7, 4, 7, 0, -7, 0, -7, -7, -7, 7, 7, -7, 7, 7, -7, 4, 7, 4, -7, 0, 7, 0};
    }

    //I don't know how to use JSON-Files, so just ignore it
    public static void init() {
        intiBlockTextures();
        initBlockProperties();
        initNonStandardBlockTypes();
        initXYZSubData();
        initUVSubData();
        initBlockTypeData();
    }
}
