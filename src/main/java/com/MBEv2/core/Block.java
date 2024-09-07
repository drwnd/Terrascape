package com.MBEv2.core;

import com.MBEv2.core.entity.Player;
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

    private static final byte[] BLOCK_TYPE_OCCLUSION_DATA = new byte[TOTAL_AMOUNT_OF_BLOCK_TYPES];
    private static final byte[] BLOCK_TYPE_DATA = new byte[TOTAL_AMOUNT_OF_BLOCK_TYPES];
    private static final byte[][] BLOCK_TYPE_XYZ_SUB_DATA = new byte[TOTAL_AMOUNT_OF_BLOCK_TYPES][0];
    private static final byte[][] BLOCK_TYPE_UV_SUB_DATA = new byte[TOTAL_AMOUNT_OF_BLOCK_TYPES][0];
    public static final int[][] NORMALS = {{0, 0, 1}, {0, 1, 0}, {1, 0, 0}, {0, 0, -1}, {0, -1, 0}, {-1, 0, 0}};

    public static final int[][] CORNERS_OF_SIDE = {{1, 0, 5, 4}, {2, 0, 3, 1}, {3, 1, 7, 5}, {2, 3, 6, 7}, {6, 4, 7, 5}, {2, 0, 6, 4}};

    public static boolean occludes(short toTestBlock, short occludingBlock, int side, int x, int y, int z) {
        if (isLeaveType(occludingBlock)) {
            if (isLeaveType(toTestBlock)) return side > 2;
            return false;
        }
        if (isGlassType(occludingBlock)) return isGlassType(toTestBlock);
        byte occlusionData = BLOCK_TYPE_OCCLUSION_DATA[getBlockType(occludingBlock)];
        byte blockData = BLOCK_TYPE_DATA[getBlockType(toTestBlock)];
        int occludingSide = (side + 3) % 6;
        int occlusionType = (occlusionData >> 6) & 3;
        int blockSideType = blockData & 1 << side;
        int occludingSideType = occlusionData & 1 << occludingSide;

        if ((occlusionType == OCCLUDES_ALL || occlusionType == OCCLUDES_DYNAMIC_ALL) && blockSideType != 0 && occludingSideType != 0)
            return true;

        if (occlusionType == OCCLUDES_DYNAMIC_SELF || occlusionType == OCCLUDES_DYNAMIC_ALL)
            return dynamicOcclusion(toTestBlock, occludingBlock, side, x, y, z, blockSideType, occludingSideType);

        return occlusionType == OCCLUDES_SELF && toTestBlock == occludingBlock;
    }

    public static boolean dynamicOcclusion(short toTestBlock, short occludingBlock, int side, int x, int y, int z, int blockSideType, int occludingSideType) {
        if (getBlockType(occludingBlock) == LIQUID_TYPE) {
            if (toTestBlock != occludingBlock) return false;
            if (side == TOP || side == BOTTOM) return true;

            int[] normal = NORMALS[side];
            short blockAboveToTestBlock = Chunk.getBlockInWorld(x, y + 1, z);
            short blockAboveOccludingBlock = Chunk.getBlockInWorld(x + normal[0], y + 1, z + normal[2]);

            int blockAboveToTestBlockType = getBlockType(blockAboveToTestBlock);
            int blockAboveOccludingBlockType = getBlockType(blockAboveOccludingBlock);

            if (getBlockTypeOcclusionData(blockAboveToTestBlock, BOTTOM) != 0 && getBlockTypeOcclusionData(blockAboveOccludingBlock, BOTTOM) == 0)
                return false;
            if ((blockAboveOccludingBlockType == LIQUID_TYPE) == (blockAboveToTestBlockType == LIQUID_TYPE))
                return true;
            if (getBlockTypeOcclusionData(blockAboveOccludingBlock, BOTTOM) != 0 && blockAboveToTestBlockType == LIQUID_TYPE)
                return true;
            return blockAboveToTestBlockType != LIQUID_TYPE;
        }

        if (blockSideType != 0 && occludingSideType != 0) return true;

        int occludingSide = (side + 3) % 6;
        int occludingBlockData = BLOCK_TYPE_DATA[getBlockType(occludingBlock)];
        int occludingBlockSideType = occludingBlockData & 1 << occludingSide;

        return getBlockType(toTestBlock) == getBlockType(occludingBlock) && blockSideType != 0 && occludingBlockSideType != 0;
    }

    public static int getTextureIndex(short block, int side) {
        byte[] blockTextureIndices;
        if (block < STANDARD_BLOCKS_THRESHOLD) blockTextureIndices = NON_STANDARD_BLOCK_TEXTURE_INDICES[block];
        else blockTextureIndices = STANDARD_BLOCK_TEXTURE_INDICES[block >> BLOCK_TYPE_BITS];
        return blockTextureIndices[side >= blockTextureIndices.length ? 0 : side];
    }

    public static boolean playerIntersectsBlock(float minX, float maxX, float minY, float maxY, float minZ, float maxZ, int blockX, int blockY, int blockZ, short block, Player player) {
        if (player.isNoClip()) return false;

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

        primaryCameraDirection %= 3;
        int addend = getToPlaceBlockAddend(primaryCameraDirection, target);

        if (blockType == BOTTOM_SLAB) return SLABS[primaryCameraDirection + addend];
        if (blockType == BOTTOM_PLATE) return PLATES[primaryCameraDirection + addend];
        if (blockType == FRONT_BACK_WALL) return WALLS[primaryCameraDirection];
        if (blockType == UP_DOWN_POST) return POSTS[primaryCameraDirection];
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

    public static byte getBlockTypeData(short block) {
        if (isLeaveType(block)) return BLOCK_TYPE_DATA[LEAVE_TYPE];
        if (isGlassType(block)) return BLOCK_TYPE_DATA[GLASS_TYPE];
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
        if (block < STANDARD_BLOCKS_THRESHOLD) return NON_STANDARD_BLOCK_PROPERTIES[block];
        return STANDARD_BLOCK_PROPERTIES[block >> BLOCK_TYPE_BITS];
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
        if (BLOCK_TYPE_UV_SUB_DATA[blockType].length == 0) return 0;
        return (byte) -BLOCK_TYPE_UV_SUB_DATA[blockType][(side << 3) + (corner << 1) + subDataAddend * 48];
    }

    public static byte getSubV(int blockType, int side, int corner, int subDataAddend) {
        if (BLOCK_TYPE_UV_SUB_DATA[blockType].length == 0) return 0;
        return BLOCK_TYPE_UV_SUB_DATA[blockType][(side << 3) + (corner << 1) + 1 + subDataAddend * 48];
    }

    public static int getBlockTypeOcclusionData(short block, int side) {
        if (block < STANDARD_BLOCKS_THRESHOLD)
            return BLOCK_TYPE_OCCLUSION_DATA[NON_STANDARD_BLOCK_TYPE[block]] & 1 << side;
        return BLOCK_TYPE_OCCLUSION_DATA[block & BLOCK_TYPE_MASK] & 1 << side;
    }

    public static byte[] getXYZSubData(short block) {
        return BLOCK_TYPE_XYZ_SUB_DATA[getBlockType(block)];
    }

    public static int getBlockType(short block) {
        if (block < STANDARD_BLOCKS_THRESHOLD) return NON_STANDARD_BLOCK_TYPE[block];
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

    //I don't know how to use JSON-Files, so just ignore it
    public static void init() {

        NON_STANDARD_BLOCK_TYPE[AIR] = AIR_TYPE;
        NON_STANDARD_BLOCK_PROPERTIES[AIR] = NO_COLLISION | REPLACEABLE;
        NON_STANDARD_BLOCK_TYPE[OUT_OF_WORLD] = FULL_BLOCK;

        NON_STANDARD_BLOCK_TEXTURE_INDICES[WATER] = new byte[]{WATER_TEXTURE};
        NON_STANDARD_BLOCK_TYPE[WATER] = LIQUID_TYPE;
        NON_STANDARD_BLOCK_PROPERTIES[WATER] = NO_COLLISION | REPLACEABLE;
        NON_STANDARD_BLOCK_TEXTURE_INDICES[LAVA] = new byte[]{LAVA_TEXTURE};
        NON_STANDARD_BLOCK_TYPE[LAVA] = LIQUID_TYPE;
        NON_STANDARD_BLOCK_PROPERTIES[LAVA] = LIGHT_EMITTING | NO_COLLISION | REPLACEABLE;
        NON_STANDARD_BLOCK_TEXTURE_INDICES[CACTUS] = new byte[]{CACTUS_SIDE_TEXTURE, CACTUS_TOP_TEXTURE, CACTUS_SIDE_TEXTURE, CACTUS_SIDE_TEXTURE, CACTUS_TOP_TEXTURE, CACTUS_SIDE_TEXTURE};
        NON_STANDARD_BLOCK_TYPE[CACTUS] = CACTUS_TYPE;
        NON_STANDARD_BLOCK_TEXTURE_INDICES[FRONT_CREATOR_HEAD] = new byte[]{CREATOR_HEAD_FRONT_TEXTURE, CREATOR_HEAD_TOP_TEXTURE, CREATOR_HEAD_RIGHT_TEXTURE, CREATOR_HEAD_BACK_TEXTURE, CREATOR_HEAD_BOTTOM_TEXTURE, CREATOR_HEAD_LEFT_TEXTURE};
        NON_STANDARD_BLOCK_TEXTURE_INDICES[RIGHT_CREATOR_HEAD] = new byte[]{CREATOR_HEAD_LEFT_TEXTURE, CREATOR_HEAD_TOP_TEXTURE, CREATOR_HEAD_FRONT_TEXTURE, CREATOR_HEAD_RIGHT_TEXTURE, ROTATED_CREATOR_HEAD_BOTTOM_TEXTURE, CREATOR_HEAD_BACK_TEXTURE};
        NON_STANDARD_BLOCK_TEXTURE_INDICES[BACK_CREATOR_HEAD] = new byte[]{CREATOR_HEAD_BACK_TEXTURE, CREATOR_HEAD_TOP_TEXTURE, CREATOR_HEAD_LEFT_TEXTURE, CREATOR_HEAD_FRONT_TEXTURE, CREATOR_HEAD_BOTTOM_TEXTURE, CREATOR_HEAD_RIGHT_TEXTURE};
        NON_STANDARD_BLOCK_TEXTURE_INDICES[LEFT_CREATOR_HEAD] = new byte[]{CREATOR_HEAD_RIGHT_TEXTURE, CREATOR_HEAD_TOP_TEXTURE, CREATOR_HEAD_BACK_TEXTURE, CREATOR_HEAD_LEFT_TEXTURE, ROTATED_CREATOR_HEAD_BOTTOM_TEXTURE, CREATOR_HEAD_FRONT_TEXTURE};
        NON_STANDARD_BLOCK_TYPE[FRONT_CREATOR_HEAD] = PLAYER_HEAD;
        NON_STANDARD_BLOCK_TYPE[RIGHT_CREATOR_HEAD] = PLAYER_HEAD;
        NON_STANDARD_BLOCK_TYPE[BACK_CREATOR_HEAD] = PLAYER_HEAD;
        NON_STANDARD_BLOCK_TYPE[LEFT_CREATOR_HEAD] = PLAYER_HEAD;
        NON_STANDARD_BLOCK_TYPE[TORCH] = TORCH_TYPE;
        NON_STANDARD_BLOCK_PROPERTIES[TORCH] = LIGHT_EMITTING | NO_COLLISION;
        NON_STANDARD_BLOCK_TEXTURE_INDICES[TORCH] = new byte[]{TORCH_TEXTURE};

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

        STANDARD_BLOCK_TEXTURE_INDICES[UP_DOWN_OAK_LOG >> BLOCK_TYPE_BITS] = new byte[]{OAK_LOG_TEXTURE, OAK_LOG_TOP_TEXTURE, OAK_LOG_TEXTURE, OAK_LOG_TEXTURE, OAK_LOG_TOP_TEXTURE, OAK_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[FRONT_BACK_OAK_LOG >> BLOCK_TYPE_BITS] = new byte[]{OAK_LOG_TOP_TEXTURE, ROTATED_OAK_LOG_TEXTURE, ROTATED_OAK_LOG_TEXTURE, OAK_LOG_TOP_TEXTURE, OAK_LOG_TEXTURE, ROTATED_OAK_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[LEFT_RIGHT_OAK_LOG >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_OAK_LOG_TEXTURE, OAK_LOG_TEXTURE, OAK_LOG_TOP_TEXTURE, ROTATED_OAK_LOG_TEXTURE, ROTATED_OAK_LOG_TEXTURE, OAK_LOG_TOP_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[UP_DOWN_STRIPPED_OAK_LOG >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_OAK_LOG_TEXTURE, STRIPPED_OAK_LOG_TOP_TEXTURE, STRIPPED_OAK_LOG_TEXTURE, STRIPPED_OAK_LOG_TEXTURE, STRIPPED_OAK_LOG_TOP_TEXTURE, STRIPPED_OAK_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[FRONT_BACK_STRIPPED_OAK_LOG >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_OAK_LOG_TOP_TEXTURE, ROTATED_STRIPPED_OAK_LOG_TEXTURE, ROTATED_STRIPPED_OAK_LOG_TEXTURE, STRIPPED_OAK_LOG_TOP_TEXTURE, STRIPPED_OAK_LOG_TEXTURE, ROTATED_STRIPPED_OAK_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[LEFT_RIGHT_STRIPPED_OAK_LOG >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_STRIPPED_OAK_LOG_TEXTURE, STRIPPED_OAK_LOG_TEXTURE, STRIPPED_OAK_LOG_TOP_TEXTURE, ROTATED_STRIPPED_OAK_LOG_TEXTURE, ROTATED_STRIPPED_OAK_LOG_TEXTURE, STRIPPED_OAK_LOG_TOP_TEXTURE};

        STANDARD_BLOCK_TEXTURE_INDICES[UP_DOWN_SPRUCE_LOG >> BLOCK_TYPE_BITS] = new byte[]{SPRUCE_LOG_TEXTURE, SPRUCE_LOG_TOP_TEXTURE, SPRUCE_LOG_TEXTURE, SPRUCE_LOG_TEXTURE, SPRUCE_LOG_TOP_TEXTURE, SPRUCE_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[FRONT_BACK_SPRUCE_LOG >> BLOCK_TYPE_BITS] = new byte[]{SPRUCE_LOG_TOP_TEXTURE, ROTATED_SPRUCE_LOG_TEXTURE, ROTATED_SPRUCE_LOG_TEXTURE, SPRUCE_LOG_TOP_TEXTURE, SPRUCE_LOG_TEXTURE, ROTATED_SPRUCE_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[LEFT_RIGHT_SPRUCE_LOG >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_SPRUCE_LOG_TEXTURE, SPRUCE_LOG_TEXTURE, SPRUCE_LOG_TOP_TEXTURE, ROTATED_SPRUCE_LOG_TEXTURE, ROTATED_SPRUCE_LOG_TEXTURE, SPRUCE_LOG_TOP_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[UP_DOWN_STRIPPED_SPRUCE_LOG >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_SPRUCE_LOG_TEXTURE, STRIPPED_SPRUCE_LOG_TOP_TEXTURE, STRIPPED_SPRUCE_LOG_TEXTURE, STRIPPED_SPRUCE_LOG_TEXTURE, STRIPPED_SPRUCE_LOG_TOP_TEXTURE, STRIPPED_SPRUCE_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[FRONT_BACK_STRIPPED_SPRUCE_LOG >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_SPRUCE_LOG_TOP_TEXTURE, ROTATED_STRIPPED_SPRUCE_LOG_TEXTURE, ROTATED_STRIPPED_SPRUCE_LOG_TEXTURE, STRIPPED_SPRUCE_LOG_TOP_TEXTURE, STRIPPED_SPRUCE_LOG_TEXTURE, ROTATED_STRIPPED_SPRUCE_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[LEFT_RIGHT_STRIPPED_SPRUCE_LOG >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_STRIPPED_SPRUCE_LOG_TEXTURE, STRIPPED_SPRUCE_LOG_TEXTURE, STRIPPED_SPRUCE_LOG_TOP_TEXTURE, ROTATED_STRIPPED_SPRUCE_LOG_TEXTURE, ROTATED_STRIPPED_SPRUCE_LOG_TEXTURE, STRIPPED_SPRUCE_LOG_TOP_TEXTURE};

        STANDARD_BLOCK_TEXTURE_INDICES[UP_DOWN_DARK_OAK_LOG >> BLOCK_TYPE_BITS] = new byte[]{DARK_OAK_LOG_TEXTURE, DARK_OAK_LOG_TOP_TEXTURE, DARK_OAK_LOG_TEXTURE, DARK_OAK_LOG_TEXTURE, DARK_OAK_LOG_TOP_TEXTURE, DARK_OAK_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[FRONT_BACK_DARK_OAK_LOG >> BLOCK_TYPE_BITS] = new byte[]{DARK_OAK_LOG_TOP_TEXTURE, ROTATED_DARK_OAK_LOG_TEXTURE, ROTATED_DARK_OAK_LOG_TEXTURE, DARK_OAK_LOG_TOP_TEXTURE, DARK_OAK_LOG_TEXTURE, ROTATED_DARK_OAK_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[LEFT_RIGHT_DARK_OAK_LOG >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_DARK_OAK_LOG_TEXTURE, DARK_OAK_LOG_TEXTURE, DARK_OAK_LOG_TOP_TEXTURE, ROTATED_DARK_OAK_LOG_TEXTURE, ROTATED_DARK_OAK_LOG_TEXTURE, DARK_OAK_LOG_TOP_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[UP_DOWN_STRIPPED_DARK_OAK_LOG >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_DARK_OAK_LOG_TEXTURE, STRIPPED_DARK_OAK_LOG_TOP_TEXTURE, STRIPPED_DARK_OAK_LOG_TEXTURE, STRIPPED_DARK_OAK_LOG_TEXTURE, STRIPPED_DARK_OAK_LOG_TOP_TEXTURE, STRIPPED_DARK_OAK_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[FRONT_BACK_STRIPPED_DARK_OAK_LOG >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_DARK_OAK_LOG_TOP_TEXTURE, ROTATED_STRIPPED_DARK_OAK_LOG_TEXTURE, ROTATED_STRIPPED_DARK_OAK_LOG_TEXTURE, STRIPPED_DARK_OAK_LOG_TOP_TEXTURE, STRIPPED_DARK_OAK_LOG_TEXTURE, ROTATED_STRIPPED_DARK_OAK_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[LEFT_RIGHT_STRIPPED_DARK_OAK_LOG >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_STRIPPED_DARK_OAK_LOG_TEXTURE, STRIPPED_DARK_OAK_LOG_TEXTURE, STRIPPED_DARK_OAK_LOG_TOP_TEXTURE, ROTATED_STRIPPED_DARK_OAK_LOG_TEXTURE, ROTATED_STRIPPED_DARK_OAK_LOG_TEXTURE, STRIPPED_DARK_OAK_LOG_TOP_TEXTURE};

        STANDARD_BLOCK_TEXTURE_INDICES[UP_DOWN_PINE_LOG >> BLOCK_TYPE_BITS] = new byte[]{PINE_LOG_TEXTURE, PINE_LOG_TOP_TEXTURE, PINE_LOG_TEXTURE, PINE_LOG_TEXTURE, PINE_LOG_TOP_TEXTURE, PINE_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[FRONT_BACK_PINE_LOG >> BLOCK_TYPE_BITS] = new byte[]{PINE_LOG_TOP_TEXTURE, ROTATED_PINE_LOG_TEXTURE, ROTATED_PINE_LOG_TEXTURE, PINE_LOG_TOP_TEXTURE, PINE_LOG_TEXTURE, ROTATED_PINE_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[LEFT_RIGHT_PINE_LOG >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_PINE_LOG_TEXTURE, PINE_LOG_TEXTURE, PINE_LOG_TOP_TEXTURE, ROTATED_PINE_LOG_TEXTURE, ROTATED_PINE_LOG_TEXTURE, PINE_LOG_TOP_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[UP_DOWN_STRIPPED_PINE_LOG >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_PINE_LOG_TEXTURE, STRIPPED_PINE_LOG_TOP_TEXTURE, STRIPPED_PINE_LOG_TEXTURE, STRIPPED_PINE_LOG_TEXTURE, STRIPPED_PINE_LOG_TOP_TEXTURE, STRIPPED_PINE_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[FRONT_BACK_STRIPPED_PINE_LOG >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_PINE_LOG_TOP_TEXTURE, ROTATED_STRIPPED_PINE_LOG_TEXTURE, ROTATED_STRIPPED_PINE_LOG_TEXTURE, STRIPPED_PINE_LOG_TOP_TEXTURE, STRIPPED_PINE_LOG_TEXTURE, ROTATED_STRIPPED_PINE_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[LEFT_RIGHT_STRIPPED_PINE_LOG >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_STRIPPED_PINE_LOG_TEXTURE, STRIPPED_PINE_LOG_TEXTURE, STRIPPED_PINE_LOG_TOP_TEXTURE, ROTATED_STRIPPED_PINE_LOG_TEXTURE, ROTATED_STRIPPED_PINE_LOG_TEXTURE, STRIPPED_PINE_LOG_TOP_TEXTURE};

        STANDARD_BLOCK_TEXTURE_INDICES[UP_DOWN_REDWOOD_LOG >> BLOCK_TYPE_BITS] = new byte[]{REDWOOD_LOG_TEXTURE, REDWOOD_LOG_TOP_TEXTURE, REDWOOD_LOG_TEXTURE, REDWOOD_LOG_TEXTURE, REDWOOD_LOG_TOP_TEXTURE, REDWOOD_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[FRONT_BACK_REDWOOD_LOG >> BLOCK_TYPE_BITS] = new byte[]{REDWOOD_LOG_TOP_TEXTURE, ROTATED_REDWOOD_LOG_TEXTURE, ROTATED_REDWOOD_LOG_TEXTURE, REDWOOD_LOG_TOP_TEXTURE, REDWOOD_LOG_TEXTURE, ROTATED_REDWOOD_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[LEFT_RIGHT_REDWOOD_LOG >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_REDWOOD_LOG_TEXTURE, REDWOOD_LOG_TEXTURE, REDWOOD_LOG_TOP_TEXTURE, ROTATED_REDWOOD_LOG_TEXTURE, ROTATED_REDWOOD_LOG_TEXTURE, REDWOOD_LOG_TOP_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[UP_DOWN_STRIPPED_REDWOOD_LOG >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_REDWOOD_LOG_TEXTURE, STRIPPED_REDWOOD_LOG_TOP_TEXTURE, STRIPPED_REDWOOD_LOG_TEXTURE, STRIPPED_REDWOOD_LOG_TEXTURE, STRIPPED_REDWOOD_LOG_TOP_TEXTURE, STRIPPED_REDWOOD_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[FRONT_BACK_STRIPPED_REDWOOD_LOG >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_REDWOOD_LOG_TOP_TEXTURE, ROTATED_STRIPPED_REDWOOD_LOG_TEXTURE, ROTATED_STRIPPED_REDWOOD_LOG_TEXTURE, STRIPPED_REDWOOD_LOG_TOP_TEXTURE, STRIPPED_REDWOOD_LOG_TEXTURE, ROTATED_STRIPPED_REDWOOD_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[LEFT_RIGHT_STRIPPED_REDWOOD_LOG >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_STRIPPED_REDWOOD_LOG_TEXTURE, STRIPPED_REDWOOD_LOG_TEXTURE, STRIPPED_REDWOOD_LOG_TOP_TEXTURE, ROTATED_STRIPPED_REDWOOD_LOG_TEXTURE, ROTATED_STRIPPED_REDWOOD_LOG_TEXTURE, STRIPPED_REDWOOD_LOG_TOP_TEXTURE};

        STANDARD_BLOCK_TEXTURE_INDICES[UP_DOWN_BLACK_WOOD_LOG >> BLOCK_TYPE_BITS] = new byte[]{BLACK_WOOD_LOG_TEXTURE, BLACK_WOOD_LOG_TOP_TEXTURE, BLACK_WOOD_LOG_TEXTURE, BLACK_WOOD_LOG_TEXTURE, BLACK_WOOD_LOG_TOP_TEXTURE, BLACK_WOOD_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[FRONT_BACK_BLACK_WOOD_LOG >> BLOCK_TYPE_BITS] = new byte[]{BLACK_WOOD_LOG_TOP_TEXTURE, ROTATED_BLACK_WOOD_LOG_TEXTURE, ROTATED_BLACK_WOOD_LOG_TEXTURE, BLACK_WOOD_LOG_TOP_TEXTURE, BLACK_WOOD_LOG_TEXTURE, ROTATED_BLACK_WOOD_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[LEFT_RIGHT_BLACK_WOOD_LOG >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_BLACK_WOOD_LOG_TEXTURE, BLACK_WOOD_LOG_TEXTURE, BLACK_WOOD_LOG_TOP_TEXTURE, ROTATED_BLACK_WOOD_LOG_TEXTURE, ROTATED_BLACK_WOOD_LOG_TEXTURE, BLACK_WOOD_LOG_TOP_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[UP_DOWN_STRIPPED_BLACK_WOOD_LOG >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_BLACK_WOOD_LOG_TEXTURE, STRIPPED_BLACK_WOOD_LOG_TOP_TEXTURE, STRIPPED_BLACK_WOOD_LOG_TEXTURE, STRIPPED_BLACK_WOOD_LOG_TEXTURE, STRIPPED_BLACK_WOOD_LOG_TOP_TEXTURE, STRIPPED_BLACK_WOOD_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[FRONT_BACK_STRIPPED_BLACK_WOOD_LOG >> BLOCK_TYPE_BITS] = new byte[]{STRIPPED_BLACK_WOOD_LOG_TOP_TEXTURE, ROTATED_STRIPPED_BLACK_WOOD_LOG_TEXTURE, ROTATED_STRIPPED_BLACK_WOOD_LOG_TEXTURE, STRIPPED_BLACK_WOOD_LOG_TOP_TEXTURE, STRIPPED_BLACK_WOOD_LOG_TEXTURE, ROTATED_STRIPPED_BLACK_WOOD_LOG_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[LEFT_RIGHT_STRIPPED_BLACK_WOOD_LOG >> BLOCK_TYPE_BITS] = new byte[]{ROTATED_STRIPPED_BLACK_WOOD_LOG_TEXTURE, STRIPPED_BLACK_WOOD_LOG_TEXTURE, STRIPPED_BLACK_WOOD_LOG_TOP_TEXTURE, ROTATED_STRIPPED_BLACK_WOOD_LOG_TEXTURE, ROTATED_STRIPPED_BLACK_WOOD_LOG_TEXTURE, STRIPPED_BLACK_WOOD_LOG_TOP_TEXTURE};

        STANDARD_BLOCK_TEXTURE_INDICES[BLACK >> BLOCK_TYPE_BITS] = new byte[]{BLACK_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[WHITE >> BLOCK_TYPE_BITS] = new byte[]{WHITE_TEXTURE};
        STANDARD_BLOCK_PROPERTIES[WHITE >> BLOCK_TYPE_BITS] = LIGHT_EMITTING;
        STANDARD_BLOCK_TEXTURE_INDICES[CYAN >> BLOCK_TYPE_BITS] = new byte[]{CYAN_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[MAGENTA >> BLOCK_TYPE_BITS] = new byte[]{MAGENTA_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[YELLOW >> BLOCK_TYPE_BITS] = new byte[]{YELLOW_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[BLUE >> BLOCK_TYPE_BITS] = new byte[]{BLUE_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[GREEN >> BLOCK_TYPE_BITS] = new byte[]{GREEN_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[RED >> BLOCK_TYPE_BITS] = new byte[]{RED_TEXTURE};
        STANDARD_BLOCK_TEXTURE_INDICES[CRAFTING_TABLE >> BLOCK_TYPE_BITS] = new byte[]{CRAFTING_TABLE_SIDE_TEXTURE_1, CRAFTING_TABLE_TOP_TEXTURE, CRAFTING_TABLE_SIDE_TEXTURE_2, CRAFTING_TABLE_SIDE_TEXTURE_1, SPRUCE_PLANKS_TEXTURE, CRAFTING_TABLE_SIDE_TEXTURE_2};
        STANDARD_BLOCK_PROPERTIES[CRAFTING_TABLE >> BLOCK_TYPE_BITS] = INTERACTABLE;
        STANDARD_BLOCK_TEXTURE_INDICES[TNT >> BLOCK_TYPE_BITS] = new byte[]{TNT_SIDE_TEXTURE, TNT_TOP_TEXTURE, TNT_SIDE_TEXTURE, TNT_SIDE_TEXTURE, TNT_BOTTOM_TEXTURE, TNT_SIDE_TEXTURE};
        STANDARD_BLOCK_PROPERTIES[TNT >> BLOCK_TYPE_BITS] = INTERACTABLE;

        BLOCK_TYPE_OCCLUSION_DATA[AIR_TYPE] = 0b00000000;
        BLOCK_TYPE_DATA[AIR_TYPE] = 0b00000000;

        BLOCK_TYPE_OCCLUSION_DATA[FULL_BLOCK] = 0b00111111;
        BLOCK_TYPE_DATA[FULL_BLOCK] = 0b01111111;

        BLOCK_TYPE_OCCLUSION_DATA[LEAVE_TYPE] = 0b00000000;
        BLOCK_TYPE_DATA[LEAVE_TYPE] = 0b00111111;

        BLOCK_TYPE_OCCLUSION_DATA[GLASS_TYPE] = 0b01111111;
        BLOCK_TYPE_DATA[GLASS_TYPE] = 0b00111111;

        BLOCK_TYPE_OCCLUSION_DATA[LIQUID_TYPE] = (byte) 0b11111111;
        BLOCK_TYPE_DATA[LIQUID_TYPE] = (byte) 0b10111111;

        BLOCK_TYPE_OCCLUSION_DATA[BOTTOM_SLAB] = (byte) 0b10010000;
        BLOCK_TYPE_DATA[BOTTOM_SLAB] = 0b00111101;

        BLOCK_TYPE_OCCLUSION_DATA[TOP_SLAB] = (byte) 0b10000010;
        BLOCK_TYPE_DATA[TOP_SLAB] = 0b00101111;

        BLOCK_TYPE_OCCLUSION_DATA[FRONT_SLAB] = (byte) 0b10000001;
        BLOCK_TYPE_DATA[FRONT_SLAB] = 0b00110111;

        BLOCK_TYPE_OCCLUSION_DATA[BACK_SLAB] = (byte) 0b10001000;
        BLOCK_TYPE_DATA[BACK_SLAB] = 0b00111110;

        BLOCK_TYPE_OCCLUSION_DATA[LEFT_SLAB] = (byte) 0b10100000;
        BLOCK_TYPE_DATA[LEFT_SLAB] = 0b00111011;

        BLOCK_TYPE_OCCLUSION_DATA[RIGHT_SLAB] = (byte) 0b10000100;
        BLOCK_TYPE_DATA[RIGHT_SLAB] = 0b00011111;

        BLOCK_TYPE_OCCLUSION_DATA[UP_DOWN_POST] = (byte) 0b10000000;
        BLOCK_TYPE_DATA[UP_DOWN_POST] = 0b00010010;

        BLOCK_TYPE_OCCLUSION_DATA[FRONT_BACK_POST] = (byte) 0b10000000;
        BLOCK_TYPE_DATA[FRONT_BACK_POST] = 0b00001001;

        BLOCK_TYPE_OCCLUSION_DATA[LEFT_RIGHT_POST] = (byte) 0b10000000;
        BLOCK_TYPE_DATA[LEFT_RIGHT_POST] = 0b00100100;

        BLOCK_TYPE_OCCLUSION_DATA[UP_DOWN_WALL] = (byte) 0b10000000;
        BLOCK_TYPE_DATA[UP_DOWN_WALL] = 0b00101101;

        BLOCK_TYPE_OCCLUSION_DATA[FRONT_BACK_WALL] = (byte) 0b10000000;
        BLOCK_TYPE_DATA[FRONT_BACK_WALL] = 0b00110110;

        BLOCK_TYPE_OCCLUSION_DATA[LEFT_RIGHT_WALL] = (byte) 0b10000000;
        BLOCK_TYPE_DATA[LEFT_RIGHT_WALL] = 0b00011011;

        BLOCK_TYPE_OCCLUSION_DATA[BOTTOM_PLATE] = (byte) 0b10010000;
        BLOCK_TYPE_DATA[BOTTOM_PLATE] = 0b00111101;

        BLOCK_TYPE_OCCLUSION_DATA[TOP_PLATE] = (byte) 0b10000010;
        BLOCK_TYPE_DATA[TOP_PLATE] = 0b00101111;

        BLOCK_TYPE_OCCLUSION_DATA[FRONT_PLATE] = (byte) 0b10000001;
        BLOCK_TYPE_DATA[FRONT_PLATE] = 0b00110111;

        BLOCK_TYPE_OCCLUSION_DATA[BACK_PLATE] = (byte) 0b10001000;
        BLOCK_TYPE_DATA[BACK_PLATE] = 0b00111110;

        BLOCK_TYPE_OCCLUSION_DATA[LEFT_PLATE] = (byte) 0b10100000;
        BLOCK_TYPE_DATA[LEFT_PLATE] = 0b00111011;

        BLOCK_TYPE_OCCLUSION_DATA[RIGHT_PLATE] = (byte) 0b10000100;
        BLOCK_TYPE_DATA[RIGHT_PLATE] = 0b00011111;

        BLOCK_TYPE_OCCLUSION_DATA[PLAYER_HEAD] = (byte) 0b00000000;
        BLOCK_TYPE_DATA[PLAYER_HEAD] = 0b00010000;

        BLOCK_TYPE_OCCLUSION_DATA[CACTUS_TYPE] = (byte) 0b10000000;
        BLOCK_TYPE_DATA[CACTUS_TYPE] = (byte) 0b10010010;

        BLOCK_TYPE_OCCLUSION_DATA[BOTTOM_FRONT_STAIR] = (byte) 0b10010001;
        BLOCK_TYPE_DATA[BOTTOM_FRONT_STAIR] = 0b00110101;

        BLOCK_TYPE_OCCLUSION_DATA[BOTTOM_RIGHT_STAIR] = (byte) 0b10010100;
        BLOCK_TYPE_DATA[BOTTOM_RIGHT_STAIR] = 0b00011101;

        BLOCK_TYPE_OCCLUSION_DATA[BOTTOM_BACK_STAIR] = (byte) 0b10011000;
        BLOCK_TYPE_DATA[BOTTOM_BACK_STAIR] = 0b00111100;

        BLOCK_TYPE_OCCLUSION_DATA[BOTTOM_LEFT_STAIR] = (byte) 0b10110000;
        BLOCK_TYPE_DATA[BOTTOM_LEFT_STAIR] = 0b00111001;

        BLOCK_TYPE_OCCLUSION_DATA[TOP_FRONT_STAIR] = (byte) 0b10000011;
        BLOCK_TYPE_DATA[TOP_FRONT_STAIR] = 0b00100111;

        BLOCK_TYPE_OCCLUSION_DATA[TOP_RIGHT_STAIR] = (byte) 0b10000110;
        BLOCK_TYPE_DATA[TOP_RIGHT_STAIR] = 0b00001111;

        BLOCK_TYPE_OCCLUSION_DATA[TOP_BACK_STAIR] = (byte) 0b10001010;
        BLOCK_TYPE_DATA[TOP_BACK_STAIR] = 0b00101110;

        BLOCK_TYPE_OCCLUSION_DATA[TOP_LEFT_STAIR] = (byte) 0b10100010;
        BLOCK_TYPE_DATA[TOP_LEFT_STAIR] = 0b00101011;

        BLOCK_TYPE_OCCLUSION_DATA[FRONT_RIGHT_STAIR] = (byte) 0b10000101;
        BLOCK_TYPE_DATA[FRONT_RIGHT_STAIR] = 0b00010111;

        BLOCK_TYPE_OCCLUSION_DATA[FRONT_LEFT_STAIR] = (byte) 0b10100001;
        BLOCK_TYPE_DATA[FRONT_LEFT_STAIR] = (byte) 0b00110011;

        BLOCK_TYPE_OCCLUSION_DATA[BACK_RIGHT_STAIR] = (byte) 0b10001100;
        BLOCK_TYPE_DATA[BACK_RIGHT_STAIR] = 0b00011110;

        BLOCK_TYPE_OCCLUSION_DATA[BACK_LEFT_STAIR] = (byte) 0b10101000;
        BLOCK_TYPE_DATA[BACK_LEFT_STAIR] = 0b00111010;

        BLOCK_TYPE_OCCLUSION_DATA[TORCH_TYPE] = (byte) 0b00000000;
        BLOCK_TYPE_DATA[TORCH_TYPE] = 0b00010000;

//        new byte[]{
//                0, 0, 0, 0, 0, 0, 0, 0,
//                0, 0, 0, 0, 0, 0, 0, 0,
//                0, 0, 0, 0, 0, 0, 0, 0,
//                0, 0, 0, 0, 0, 0, 0, 0,
//                0, 0, 0, 0, 0, 0, 0, 0,
//                0, 0, 0, 0, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[TORCH_TYPE] = new byte[]{7, -7, 0, -4, 7, -7};
        BLOCK_TYPE_UV_SUB_DATA[TORCH_TYPE] = new byte[]{7, 4, -7, 4, 7, 0, -7, 0, 7, 4, -7, 4, 7, -10, -7, -10, 7, 4, -7, 4, 7, 0, -7, 0, 7, 4, -7, 4, 7, 0, -7, 0, -7, -7, -7, 7, 7, -7, 7, 7, -7, 4, 7, 4, -7, 0, 7, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_RIGHT_STAIR] = new byte[]{8, 0, 0, 0, 0, -8, 0, 0, 0, 0, 8, 0};
        BLOCK_TYPE_UV_SUB_DATA[FRONT_RIGHT_STAIR] = new byte[]{0, 0, -8, 0, 0, 0, -8, 0, 8, 8, 0, 8, 8, 0, 0, 0, 0, 0, -8, 0, 0, 0, -8, 0, 8, 0, 0, 0, 8, 0, 0, 0, -8, -8, -8, 0, 0, -8, 0, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, 0, 0, -8, 0, 0, -8, 0, 0, 0, -8, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_LEFT_STAIR] = new byte[]{0, -8, 0, 0, 0, -8, 0, 0, 0, 0, 8, 0};
        BLOCK_TYPE_UV_SUB_DATA[FRONT_LEFT_STAIR] = new byte[]{0, 0, -8, 0, 0, 0, -8, 0, 8, 8, 0, 8, 8, 0, 0, 0, 0, 0, -8, 0, 0, 0, -8, 0, 8, 0, 0, 0, 8, 0, 0, 0, -8, -8, -8, 0, 0, -8, 0, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, 0, 0, -8, 0, 0, -8, 0, 0, 0, -8, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[BACK_RIGHT_STAIR] = new byte[]{8, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, -8};
        BLOCK_TYPE_UV_SUB_DATA[BACK_RIGHT_STAIR] = new byte[]{0, 0, -8, 0, 0, 0, -8, 0, 8, 8, 0, 8, 8, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 0, -8, -8, -8, 0, 0, -8, 0, 0, -8, 0, 0, 0, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, 0, 0, -8, 0, 0, 0, -8, 0, 0, 0, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 8, 0, 0, 0, 8, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[BACK_LEFT_STAIR] = new byte[]{0, -8, 0, 0, 8, 0, 0, 0, 0, 0, 0, -8};
        BLOCK_TYPE_UV_SUB_DATA[BACK_LEFT_STAIR] = new byte[]{8, 0, 0, 0, 8, 0, 0, 0, 0, 8, -8, 8, 0, 0, -8, 0, 8, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 0, 0, -8, 0, 0, 8, -8, 8, 0, -8, 0, 0, 0, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, 0, 0, -8, 0, 0, 0, -8, 0, 0, 0, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 8, 0, 0, 0, 8, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[BOTTOM_FRONT_STAIR] = new byte[]{0, 0, 0, -8, 0, 0, 0, 0, 8, 0, 8, 0};
        BLOCK_TYPE_UV_SUB_DATA[BOTTOM_FRONT_STAIR] = new byte[]{0, 8, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 8, 0, 0, 0, 0, 0, 8, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, -8, 8, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 0, 8, -8, 0, -8, 0, 0, 0, 0, 0, -8, 0, -8, 0, 0, 0, 8, 0, 0, 0, 8, -8, 0, 0, 0, -8, -8, 0, -8};

        BLOCK_TYPE_XYZ_SUB_DATA[BOTTOM_RIGHT_STAIR] = new byte[]{0, 0, 0, -8, 0, 0, 8, 0, 8, 0, 0, 0};
        BLOCK_TYPE_UV_SUB_DATA[BOTTOM_RIGHT_STAIR] = new byte[]{0, 8, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 8, 0, 0, 0, 0, 0, 8, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 8, 0, 0, 0, 0, 0, 0, -8, 0, 0, -8, -8, -8, 0, 8, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, -8, 8, 0, 0, 0, 8, -8, 0, -8, -8, 0, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, -8};

        BLOCK_TYPE_XYZ_SUB_DATA[BOTTOM_BACK_STAIR] = new byte[]{0, 0, 0, -8, 0, 0, 0, 0, 8, 0, 0, -8};
        BLOCK_TYPE_UV_SUB_DATA[BOTTOM_BACK_STAIR] = new byte[]{0, 8, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 8, 0, 0, 0, 0, 0, 8, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, -8, 0, 0, -8, 0, 0, 0, -8, 0, 0, 0, -8, 0, 0, -8, -8, -8, 0, 0, 0, 0, 0, -8, 0, -8, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 8, 0, 0, -8, 8, -8};

        BLOCK_TYPE_XYZ_SUB_DATA[BOTTOM_LEFT_STAIR] = new byte[]{0, 0, 0, -8, 0, 0, 0, -8, 8, 0, 0, 0};
        BLOCK_TYPE_UV_SUB_DATA[BOTTOM_LEFT_STAIR] = new byte[]{0, 8, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 8, 0, 0, 0, 0, 0, 8, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 8, 0, 0, 0, 0, 8, 0, 0, 0, 8, -8, 0, -8, 0, 0, 0, 0, 0, -8, 0, -8, 0, 0, 0, 0, 0, -8, 0, -8, 0, 0, -8, 0, 0, -8, -8, -8, -8, 0, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, -8};

        BLOCK_TYPE_XYZ_SUB_DATA[TOP_FRONT_STAIR] = new byte[]{0, 0, 8, 0, 0, 0, 0, 0, 0, -8, 8, 0};
        BLOCK_TYPE_UV_SUB_DATA[TOP_FRONT_STAIR] = new byte[]{0, 0, 0, 0, 0, -8, 0, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, -8, 0, 0, 0, 0, 0, -8, 0, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, -8, 0, 8, 0, 8, 0, 0, 0, 0, 8, 8, 0, 8, 8, 0, 0, 0, 8, 8, 0, 8, 8, 0, 0, 0, 0, 8, 0, 8, 0, 0, 0, 0, 0, -8, 0, 0, 0, -8, 0, 0, -8, 8, 0, 8, -8, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[TOP_RIGHT_STAIR] = new byte[]{0, 0, 8, 0, 0, 0, 8, 0, 0, -8, 0, 0};
        BLOCK_TYPE_UV_SUB_DATA[TOP_RIGHT_STAIR] = new byte[]{0, 0, 0, 0, 0, -8, 0, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, -8, 0, 0, 0, 0, 0, -8, 0, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, -8, 8, 0, 0, 0, 8, -8, 0, -8, 0, 8, 0, 8, 0, 0, 0, 0, 0, 8, 0, 8, 0, 0, 0, 0, 0, 0, -8, 0, 0, -8, -8, -8, -8, 0, -8, 0, 0, 0, 0, 0, 0, 8, 0, 8, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[TOP_BACK_STAIR] = new byte[]{0, 0, 8, 0, 0, 0, 0, 0, 0, -8, 0, -8};
        BLOCK_TYPE_UV_SUB_DATA[TOP_BACK_STAIR] = new byte[]{0, 0, 0, 0, 0, -8, 0, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, -8, 0, 0, 0, 0, 0, -8, 0, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, -8, 0, 8, 0, 8, 0, 0, 0, 0, 0, 0, -8, 0, 0, 0, -8, 0, 8, 0, 0, 0, 8, -8, 0, -8, 0, 8, 0, 8, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 8, -8, 0, 0, 0, -8, -8, 0, -8};

        BLOCK_TYPE_XYZ_SUB_DATA[TOP_LEFT_STAIR] = new byte[]{0, 0, 8, 0, 0, 0, 0, -8, 0, -8, 0, 0};
        BLOCK_TYPE_UV_SUB_DATA[TOP_LEFT_STAIR] = new byte[]{0, 0, 0, 0, 0, -8, 0, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, -8, 0, 0, 0, 0, 0, -8, 0, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, -8, 0, 0, -8, 0, 0, -8, -8, -8, 0, 8, 0, 8, 0, 0, 0, 0, 0, 8, 0, 8, 0, 0, 0, 0, 8, 0, 0, 0, 8, -8, 0, -8, -8, 0, -8, 0, 0, 0, 0, 0, 0, 8, 0, 8, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[CACTUS_TYPE] = new byte[]{1, -1, 0, 0, 1, -1};
        BLOCK_TYPE_UV_SUB_DATA[CACTUS_TYPE] = new byte[]{1, 0, -1, 0, 1, 0, -1, 0, 1, 1, -1, 1, 1, -1, -1, -1, 1, 0, -1, 0, 1, 0, -1, 0, 1, 0, -1, 0, 1, 0, -1, 0, -1, -1, -1, 1, 1, -1, 1, 1, -1, 0, 1, 0, -1, 0, 1, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[PLAYER_HEAD] = new byte[]{4, -4, 0, -8, 4, -4};
        BLOCK_TYPE_UV_SUB_DATA[PLAYER_HEAD] = new byte[]{4, 4, -4, 4, 4, -4, -4, -4, 4, 4, -4, 4, 4, -4, -4, -4, 4, 4, -4, 4, 4, -4, -4, -4, 4, 4, -4, 4, 4, -4, -4, -4, -4, -4, -4, 4, 4, -4, 4, 4, -4, 4, 4, 4, -4, -4, 4, -4};

        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_WALL] = new byte[]{0, 0, 4, -4, 0, 0};
        BLOCK_TYPE_UV_SUB_DATA[UP_DOWN_WALL] = new byte[]{0, 4, 0, 4, 0, -4, 0, -4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 4, 0, -4, 0, -4, 0, 4, 0, 4, 0, -4, 0, -4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 4, 0, -4, 0, -4};

        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_BACK_WALL] = new byte[]{0, 0, 0, 0, 4, -4};
        BLOCK_TYPE_UV_SUB_DATA[FRONT_BACK_WALL] = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 4, 0, -4, 0, 4, 0, -4, 0, 4, 0, -4, 0, 4, 0, -4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -4, 0, 4, 0, -4, 0, 4, -4, 0, 4, 0, -4, 0, 4, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_RIGHT_WALL] = new byte[]{4, -4, 0, 0, 0, 0};
        BLOCK_TYPE_UV_SUB_DATA[LEFT_RIGHT_WALL] = new byte[]{4, 0, -4, 0, 4, 0, -4, 0, 0, 4, 0, 4, 0, -4, 0, -4, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, -4, 0, 4, 0, -4, 0, -4, 0, -4, 0, 4, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_RIGHT_POST] = new byte[]{0, 0, 4, -4, 4, -4};
        BLOCK_TYPE_UV_SUB_DATA[LEFT_RIGHT_POST] = new byte[]{0, 0, 0, 0, 0, -8, 0, -8, 4, 0, -4, 0, 4, 0, -4, 0, 4, 4, -4, 4, 4, -4, -4, -4, 0, 8, 0, 8, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 8, -4, 4, 4, 4, -4, -4, 4, -4};

        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_BACK_POST] = new byte[]{4, -4, 4, -4, 0, 0};
        BLOCK_TYPE_UV_SUB_DATA[FRONT_BACK_POST] = new byte[]{4, 4, -4, 4, 4, -4, -4, -4, 0, 8, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, -8, 4, 4, -4, 4, 4, -4, -4, -4, -8, 0, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, -8};

        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_POST] = new byte[]{4, -4, 0, 0, 4, -4};
        BLOCK_TYPE_UV_SUB_DATA[UP_DOWN_POST] = new byte[]{0, 0, -8, 0, 0, 0, -8, 0, 4, 4, -4, 4, 4, -4, -4, -4, 8, 0, 0, 0, 8, 0, 0, 0, 0, 0, -8, 0, 0, 0, -8, 0, -4, -4, -4, 4, 4, -4, 4, 4, 0, 0, 8, 0, 0, 0, 8, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[BOTTOM_SLAB] = new byte[]{0, 0, 0, -8, 0, 0};
        BLOCK_TYPE_UV_SUB_DATA[BOTTOM_SLAB] = new byte[]{0, 8, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 8, 0, 0, 0, 0, 0, 8, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 8, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[TOP_SLAB] = new byte[]{0, 0, 8, 0, 0, 0};
        BLOCK_TYPE_UV_SUB_DATA[TOP_SLAB] = new byte[]{0, 0, 0, 0, 0, -8, 0, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, -8, 0, 0, 0, 0, 0, -8, 0, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, -8};

        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_SLAB] = new byte[]{0, 0, 0, 0, 8, 0};
        BLOCK_TYPE_UV_SUB_DATA[FRONT_SLAB] = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, 0, 0, -8, 0, 0, -8, 0, 0, 0, -8, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[BACK_SLAB] = new byte[]{0, 0, 0, 0, 0, -8};
        BLOCK_TYPE_UV_SUB_DATA[BACK_SLAB] = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, 0, 0, -8, 0, 0, 0, -8, 0, 0, 0, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 8, 0, 0, 0, 8, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[RIGHT_SLAB] = new byte[]{8, 0, 0, 0, 0, 0};
        BLOCK_TYPE_UV_SUB_DATA[RIGHT_SLAB] = new byte[]{0, 0, -8, 0, 0, 0, -8, 0, 0, 8, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 0, -8, 0, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_SLAB] = new byte[]{0, -8, 0, 0, 0, 0};
        BLOCK_TYPE_UV_SUB_DATA[LEFT_SLAB] = new byte[]{8, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, 0, 0, -8, 0, 0, 0, 0, 0, 8, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[BOTTOM_PLATE] = new byte[]{0, 0, 0, -12, 0, 0};
        BLOCK_TYPE_UV_SUB_DATA[BOTTOM_PLATE] = new byte[]{0, 12, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 12, 0, 0, 0, 0, 0, 12, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 12, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[TOP_PLATE] = new byte[]{0, 0, 12, 0, 0, 0};
        BLOCK_TYPE_UV_SUB_DATA[TOP_PLATE] = new byte[]{0, 0, 0, 0, 0, -12, 0, -12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -12, 0, -12, 0, 0, 0, 0, 0, -12, 0, -12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -12, 0, -12};

        BLOCK_TYPE_XYZ_SUB_DATA[FRONT_PLATE] = new byte[]{0, 0, 0, 0, 12, 0};
        BLOCK_TYPE_UV_SUB_DATA[FRONT_PLATE] = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 12, 0, 0, 0, 12, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -12, 0, 0, 0, -12, 0, 0, -12, 0, 0, 0, -12, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[BACK_PLATE] = new byte[]{0, 0, 0, 0, 0, -12};
        BLOCK_TYPE_UV_SUB_DATA[BACK_PLATE] = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -12, 0, 0, 0, -12, 0, 0, 0, -12, 0, 0, 0, -12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 12, 0, 0, 12, 0, 0, 0, 12, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[RIGHT_PLATE] = new byte[]{12, 0, 0, 0, 0, 0};
        BLOCK_TYPE_UV_SUB_DATA[RIGHT_PLATE] = new byte[]{0, 0, -12, 0, 0, 0, -12, 0, 0, 12, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 0, -12, 0, -12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[LEFT_PLATE] = new byte[]{0, -12, 0, 0, 0, 0};
        BLOCK_TYPE_UV_SUB_DATA[LEFT_PLATE] = new byte[]{12, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, -12, 0, -12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -12, 0, 0, 0, -12, 0, 0, 0, 0, 0, 12, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[FULL_BLOCK] = new byte[]{0, 0, 0, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[LEAVE_TYPE] = new byte[]{0, 0, 0, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[GLASS_TYPE] = new byte[]{0, 0, 0, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[LIQUID_TYPE] = new byte[]{0, 0, 0, -2, 0, 0};
    }
}
