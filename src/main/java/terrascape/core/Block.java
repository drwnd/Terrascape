package terrascape.core;

import terrascape.dataStorage.Chunk;
import terrascape.entity.Target;
import terrascape.utils.Utils;
import org.joml.Vector3f;

import static terrascape.utils.Constants.*;

public class Block {

    public static final int[][] NORMALS = {{0, 0, 1}, {0, 1, 0}, {1, 0, 0}, {0, 0, -1}, {0, -1, 0}, {-1, 0, 0}};
    public static final int[][] CORNERS_OF_SIDE = {{1, 0, 5, 4}, {2, 0, 3, 1}, {3, 1, 7, 5}, {2, 3, 6, 7}, {6, 4, 7, 5}, {2, 0, 6, 4}};

    public static boolean occludes(short toTestBlock, short occludingBlock, int side, int x, int y, int z, int aabbIndex) {
        int occludingBlockType = getBlockType(occludingBlock);
        if (occludingBlockType == AIR_TYPE) return false;

        long toTestOcclusionData = BLOCK_TYPE_OCCLUSION_DATA[getBlockType(toTestBlock)][side + aabbIndex];
        byte blockTypeData = BLOCK_TYPE_DATA[occludingBlockType];
        int occludingSide = (side + 3) % 6;
        long occludingOcclusionData = getBlockTypeOcclusionData(occludingBlock, occludingSide);

        if (isGlassType(occludingBlock))
            return isGlassType(toTestBlock) && (toTestOcclusionData | occludingOcclusionData) == occludingOcclusionData && toTestOcclusionData != 0L;
        if (isLeaveType(occludingBlock)) {
            if (isLeaveType(toTestBlock))
                return side > 2 && (toTestOcclusionData | occludingOcclusionData) == occludingOcclusionData && toTestOcclusionData != 0L;
            return false;
        }

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
        if ((blockAboveOccludingBlockType == LIQUID_TYPE) == (blockAboveToTestBlockType == LIQUID_TYPE)) return true;
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
        if ((toPlaceBlock & 0xFFFF) < STANDARD_BLOCKS_THRESHOLD) {
            if (toPlaceBlock == NORTH_CREATOR_HEAD) {
                if (primaryXZDirection == SOUTH) return NORTH_CREATOR_HEAD;
                if (primaryXZDirection == NORTH) return SOUTH_CREATOR_HEAD;
                if (primaryXZDirection == WEST) return EAST_CREATOR_HEAD;
                if (primaryXZDirection == EAST) return WEST_CREATOR_HEAD;
            }
            return toPlaceBlock;
        }
        int blockType = toPlaceBlock & BLOCK_TYPE_MASK;
        int baseBlock = toPlaceBlock & BASE_BLOCK_MASK;

        int toPlaceBlockType = getToPlaceBlockType(blockType, primaryCameraDirection, target);
        int side = target.side() % 3;

        switch (baseBlock) {
            case UP_DOWN_OAK_LOG -> {
                if (side == NORTH) return (short) (NORTH_SOUTH_OAK_LOG | toPlaceBlockType);
                if (side == TOP) return (short) (UP_DOWN_OAK_LOG | toPlaceBlockType);
                return (short) (EAST_WEST_OAK_LOG | toPlaceBlockType);
            }
            case UP_DOWN_STRIPPED_OAK_LOG -> {
                if (side == NORTH) return (short) (NORTH_SOUTH_STRIPPED_OAK_LOG | toPlaceBlockType);
                if (side == TOP) return (short) (UP_DOWN_STRIPPED_OAK_LOG | toPlaceBlockType);
                return (short) (EAST_WEST_STRIPPED_OAK_LOG | toPlaceBlockType);
            }
            case UP_DOWN_SPRUCE_LOG -> {
                if (side == NORTH) return (short) (NORTH_SOUTH_SPRUCE_LOG | toPlaceBlockType);
                if (side == TOP) return (short) (UP_DOWN_SPRUCE_LOG | toPlaceBlockType);
                return (short) (EAST_WEST_SPRUCE_LOG | toPlaceBlockType);
            }
            case UP_DOWN_STRIPPED_SPRUCE_LOG -> {
                if (side == NORTH) return (short) (NORTH_SOUTH_STRIPPED_SPRUCE_LOG | toPlaceBlockType);
                if (side == TOP) return (short) (UP_DOWN_STRIPPED_SPRUCE_LOG | toPlaceBlockType);
                return (short) (EAST_WEST_STRIPPED_SPRUCE_LOG | toPlaceBlockType);
            }
            case UP_DOWN_DARK_OAK_LOG -> {
                if (side == NORTH) return (short) (NORTH_SOUTH_DARK_OAK_LOG | toPlaceBlockType);
                if (side == TOP) return (short) (UP_DOWN_DARK_OAK_LOG | toPlaceBlockType);
                return (short) (EAST_WEST_DARK_OAK_LOG | toPlaceBlockType);
            }
            case UP_DOWN_STRIPPED_DARK_OAK_LOG -> {
                if (side == NORTH)
                    return (short) (NORTH_SOUTH_STRIPPED_DARK_OAK_LOG | toPlaceBlockType);
                if (side == TOP) return (short) (UP_DOWN_STRIPPED_DARK_OAK_LOG | toPlaceBlockType);
                return (short) (EAST_WEST_STRIPPED_DARK_OAK_LOG | toPlaceBlockType);
            }
            case UP_DOWN_PINE_LOG -> {
                if (side == NORTH) return (short) (NORTH_SOUTH_PINE_LOG | toPlaceBlockType);
                if (side == TOP) return (short) (UP_DOWN_PINE_LOG | toPlaceBlockType);
                return (short) (EAST_WEST_PINE_LOG | toPlaceBlockType);
            }
            case UP_DOWN_STRIPPED_PINE_LOG -> {
                if (side == NORTH) return (short) (NORTH_SOUTH_STRIPPED_PINE_LOG | toPlaceBlockType);
                if (side == TOP) return (short) (UP_DOWN_STRIPPED_PINE_LOG | toPlaceBlockType);
                return (short) (EAST_WEST_STRIPPED_PINE_LOG | toPlaceBlockType);
            }
            case UP_DOWN_REDWOOD_LOG -> {
                if (side == NORTH) return (short) (NORTH_SOUTH_REDWOOD_LOG | toPlaceBlockType);
                if (side == TOP) return (short) (UP_DOWN_REDWOOD_LOG | toPlaceBlockType);
                return (short) (EAST_WEST_REDWOOD_LOG | toPlaceBlockType);
            }
            case UP_DOWN_STRIPPED_REDWOOD_LOG -> {
                if (side == NORTH)
                    return (short) (NORTH_SOUTH_STRIPPED_REDWOOD_LOG | toPlaceBlockType);
                if (side == TOP) return (short) (UP_DOWN_STRIPPED_REDWOOD_LOG | toPlaceBlockType);
                return (short) (EAST_WEST_STRIPPED_REDWOOD_LOG | toPlaceBlockType);
            }
            case UP_DOWN_BLACK_WOOD_LOG -> {
                if (side == NORTH) return (short) (NORTH_SOUTH_BLACK_WOOD_LOG | toPlaceBlockType);
                if (side == TOP) return (short) (UP_DOWN_BLACK_WOOD_LOG | toPlaceBlockType);
                return (short) (EAST_WEST_BLACK_WOOD_LOG | toPlaceBlockType);
            }
            case UP_DOWN_STRIPPED_BLACK_WOOD_LOG -> {
                if (side == NORTH)
                    return (short) (NORTH_SOUTH_STRIPPED_BLACK_WOOD_LOG | toPlaceBlockType);
                if (side == TOP) return (short) (UP_DOWN_STRIPPED_BLACK_WOOD_LOG | toPlaceBlockType);
                return (short) (EAST_WEST_STRIPPED_BLACK_WOOD_LOG | toPlaceBlockType);
            }
            case NORTH_FURNACE -> {
                if (primaryXZDirection == NORTH) return (short) (SOUTH_FURNACE | toPlaceBlockType);
                if (primaryXZDirection == SOUTH) return (short) (NORTH_FURNACE | toPlaceBlockType);
                if (primaryXZDirection == WEST) return (short) (EAST_FURNACE | toPlaceBlockType);
                return (short) (WEST_FURNACE | toPlaceBlockType);
            }
        }

        return (short) (baseBlock | toPlaceBlockType);
    }

    private static int getToPlaceBlockType(int blockType, int primaryCameraDirection, Target target) {

        switch (blockType) {
            case BOTTOM_SOUTH_STAIR -> {
                int side = target.side();
                Vector3f inBlockPosition = target.inBlockPosition();
                double x = Utils.fraction(inBlockPosition.x);
                double y = Utils.fraction(inBlockPosition.y);
                double z = Utils.fraction(inBlockPosition.z);

                if (side == SOUTH) {
                    if (y < x && y < 1.0 - x) return BOTTOM_NORTH_STAIR;
                    if (y > x && y < 1.0 - x) return NORTH_EAST_STAIR;
                    if (y > x && y > 1.0 - x) return TOP_NORTH_STAIR;
                    return NORTH_WEST_STAIR;
                }
                if (side == NORTH) {
                    if (y < x && y < 1.0 - x) return BOTTOM_SOUTH_STAIR;
                    if (y > x && y < 1.0 - x) return SOUTH_EAST_STAIR;
                    if (y > x && y > 1.0 - x) return TOP_SOUTH_STAIR;
                    return SOUTH_WEST_STAIR;
                }
                if (side == TOP) {
                    if (x < z && x < 1.0 - z) return BOTTOM_EAST_STAIR;
                    if (x > z && x < 1.0 - z) return BOTTOM_SOUTH_STAIR;
                    if (x > z && x > 1.0 - z) return BOTTOM_WEST_STAIR;
                    return BOTTOM_NORTH_STAIR;
                }
                if (side == BOTTOM) {
                    if (x < z && x < 1.0 - z) return TOP_EAST_STAIR;
                    if (x > z && x < 1.0 - z) return TOP_SOUTH_STAIR;
                    if (x > z && x > 1.0 - z) return TOP_WEST_STAIR;
                    return TOP_NORTH_STAIR;
                }
                if (side == EAST) {
                    if (y < z && y < 1.0 - z) return BOTTOM_WEST_STAIR;
                    if (y > z && y < 1.0 - z) return SOUTH_WEST_STAIR;
                    if (y > z && y > 1.0 - z) return TOP_WEST_STAIR;
                    return NORTH_WEST_STAIR;
                }
                if (side == WEST) {
                    if (y < z && y < 1.0 - z) return BOTTOM_EAST_STAIR;
                    if (y > z && y < 1.0 - z) return SOUTH_EAST_STAIR;
                    if (y > z && y > 1.0 - z) return TOP_EAST_STAIR;
                    return NORTH_EAST_STAIR;
                }
            }
            case THIN_BOTTOM_SOUTH_STAIR -> {
                int side = target.side();
                Vector3f inBlockPosition = target.inBlockPosition();
                double x = Utils.fraction(inBlockPosition.x);
                double y = Utils.fraction(inBlockPosition.y);
                double z = Utils.fraction(inBlockPosition.z);

                if (side == SOUTH) {
                    if (y < x && y < 1.0 - x) return THIN_BOTTOM_NORTH_STAIR;
                    if (y > x && y < 1.0 - x) return THIN_NORTH_EAST_STAIR;
                    if (y > x && y > 1.0 - x) return THIN_TOP_NORTH_STAIR;
                    return THIN_NORTH_WEST_STAIR;
                }
                if (side == NORTH) {
                    if (y < x && y < 1.0 - x) return THIN_BOTTOM_SOUTH_STAIR;
                    if (y > x && y < 1.0 - x) return THIN_SOUTH_EAST_STAIR;
                    if (y > x && y > 1.0 - x) return THIN_TOP_SOUTH_STAIR;
                    return THIN_SOUTH_WEST_STAIR;
                }
                if (side == TOP) {
                    if (x < z && x < 1.0 - z) return THIN_BOTTOM_EAST_STAIR;
                    if (x > z && x < 1.0 - z) return THIN_BOTTOM_SOUTH_STAIR;
                    if (x > z && x > 1.0 - z) return THIN_BOTTOM_WEST_STAIR;
                    return THIN_BOTTOM_NORTH_STAIR;
                }
                if (side == BOTTOM) {
                    if (x < z && x < 1.0 - z) return THIN_TOP_EAST_STAIR;
                    if (x > z && x < 1.0 - z) return THIN_TOP_SOUTH_STAIR;
                    if (x > z && x > 1.0 - z) return THIN_TOP_WEST_STAIR;
                    return THIN_TOP_NORTH_STAIR;
                }
                if (side == EAST) {
                    if (y < z && y < 1.0 - z) return THIN_BOTTOM_WEST_STAIR;
                    if (y > z && y < 1.0 - z) return THIN_SOUTH_WEST_STAIR;
                    if (y > z && y > 1.0 - z) return THIN_TOP_WEST_STAIR;
                    return THIN_NORTH_WEST_STAIR;
                }
                if (side == WEST) {
                    if (y < z && y < 1.0 - z) return THIN_BOTTOM_EAST_STAIR;
                    if (y > z && y < 1.0 - z) return THIN_SOUTH_EAST_STAIR;
                    if (y > z && y > 1.0 - z) return THIN_TOP_EAST_STAIR;
                    return THIN_NORTH_EAST_STAIR;
                }
            }
            case THICK_BOTTOM_SOUTH_STAIR -> {
                int side = target.side();
                Vector3f inBlockPosition = target.inBlockPosition();
                double x = Utils.fraction(inBlockPosition.x);
                double y = Utils.fraction(inBlockPosition.y);
                double z = Utils.fraction(inBlockPosition.z);

                if (side == SOUTH) {
                    if (y < x && y < 1.0 - x) return THICK_BOTTOM_NORTH_STAIR;
                    if (y > x && y < 1.0 - x) return THICK_NORTH_EAST_STAIR;
                    if (y > x && y > 1.0 - x) return THICK_TOP_NORTH_STAIR;
                    return THICK_NORTH_WEST_STAIR;
                }
                if (side == NORTH) {
                    if (y < x && y < 1.0 - x) return THICK_BOTTOM_SOUTH_STAIR;
                    if (y > x && y < 1.0 - x) return THICK_SOUTH_EAST_STAIR;
                    if (y > x && y > 1.0 - x) return THICK_TOP_SOUTH_STAIR;
                    return THICK_SOUTH_WEST_STAIR;
                }
                if (side == TOP) {
                    if (x < z && x < 1.0 - z) return THICK_BOTTOM_EAST_STAIR;
                    if (x > z && x < 1.0 - z) return THICK_BOTTOM_SOUTH_STAIR;
                    if (x > z && x > 1.0 - z) return THICK_BOTTOM_WEST_STAIR;
                    return THICK_BOTTOM_NORTH_STAIR;
                }
                if (side == BOTTOM) {
                    if (x < z && x < 1.0 - z) return THICK_TOP_EAST_STAIR;
                    if (x > z && x < 1.0 - z) return THICK_TOP_SOUTH_STAIR;
                    if (x > z && x > 1.0 - z) return THICK_TOP_WEST_STAIR;
                    return THICK_TOP_NORTH_STAIR;
                }
                if (side == EAST) {
                    if (y < z && y < 1.0 - z) return THICK_BOTTOM_WEST_STAIR;
                    if (y > z && y < 1.0 - z) return THICK_SOUTH_WEST_STAIR;
                    if (y > z && y > 1.0 - z) return THICK_TOP_WEST_STAIR;
                    return THICK_NORTH_WEST_STAIR;
                }
                if (side == WEST) {
                    if (y < z && y < 1.0 - z) return THICK_BOTTOM_EAST_STAIR;
                    if (y > z && y < 1.0 - z) return THICK_SOUTH_EAST_STAIR;
                    if (y > z && y > 1.0 - z) return THICK_TOP_EAST_STAIR;
                    return THICK_NORTH_EAST_STAIR;
                }
            }
            case BOTTOM_PLAYER_HEAD -> {
                int side = target.side();
                if (side == NORTH) return SOUTH_PLAYER_HEAD;
                if (side == TOP) return BOTTOM_PLAYER_HEAD;
                if (side == WEST) return EAST_PLAYER_HEAD;
                if (side == SOUTH) return NORTH_PLAYER_HEAD;
                if (side == BOTTOM) return TOP_PLAYER_HEAD;
                return WEST_PLAYER_HEAD;
            }
            case UP_DOWN_POST -> {
                return POSTS[target.side() % 3];
            }
            case UP_DOWN_FENCE_NORTH_WEST -> {
                return FENCES[target.side() % 3];
            }
        }

        primaryCameraDirection %= 3;
        int addend = getToPlaceBlockAddend(primaryCameraDirection, target);

        return switch (blockType) {
            case BOTTOM_SLAB -> SLABS[primaryCameraDirection + addend];
            case BOTTOM_PLATE -> PLATES[primaryCameraDirection + addend];
            case BOTTOM_SOCKET -> SOCKETS[primaryCameraDirection + addend];
            case NORTH_SOUTH_WALL -> WALLS[primaryCameraDirection];
            default -> blockType;
        };
    }

    public static short getInInventoryBlockEquivalent(short block) {
        if (block == AIR || block == OUT_OF_WORLD) return AIR;
        if ((block & 0xFFFF) < STANDARD_BLOCKS_THRESHOLD) {
            if (block >= NORTH_CREATOR_HEAD && block <= EAST_CREATOR_HEAD) return NORTH_CREATOR_HEAD;
            return block;
        }
        int blockType = block & BLOCK_TYPE_MASK;
        int baseBlock = switch (block & BASE_BLOCK_MASK) {
            case UP_DOWN_OAK_LOG, NORTH_SOUTH_OAK_LOG, EAST_WEST_OAK_LOG -> UP_DOWN_OAK_LOG;
            case UP_DOWN_STRIPPED_OAK_LOG, NORTH_SOUTH_STRIPPED_OAK_LOG, EAST_WEST_STRIPPED_OAK_LOG ->
                    UP_DOWN_STRIPPED_OAK_LOG;
            case UP_DOWN_SPRUCE_LOG, NORTH_SOUTH_SPRUCE_LOG, EAST_WEST_SPRUCE_LOG -> UP_DOWN_SPRUCE_LOG;
            case UP_DOWN_STRIPPED_SPRUCE_LOG, NORTH_SOUTH_STRIPPED_SPRUCE_LOG, EAST_WEST_STRIPPED_SPRUCE_LOG ->
                    UP_DOWN_STRIPPED_SPRUCE_LOG;
            case UP_DOWN_DARK_OAK_LOG, NORTH_SOUTH_DARK_OAK_LOG, EAST_WEST_DARK_OAK_LOG -> UP_DOWN_DARK_OAK_LOG;
            case UP_DOWN_STRIPPED_DARK_OAK_LOG, NORTH_SOUTH_STRIPPED_DARK_OAK_LOG, EAST_WEST_STRIPPED_DARK_OAK_LOG ->
                    UP_DOWN_STRIPPED_DARK_OAK_LOG;
            case UP_DOWN_PINE_LOG, NORTH_SOUTH_PINE_LOG, EAST_WEST_PINE_LOG -> UP_DOWN_PINE_LOG;
            case UP_DOWN_STRIPPED_PINE_LOG, NORTH_SOUTH_STRIPPED_PINE_LOG, EAST_WEST_STRIPPED_PINE_LOG ->
                    UP_DOWN_STRIPPED_PINE_LOG;
            case UP_DOWN_REDWOOD_LOG, NORTH_SOUTH_REDWOOD_LOG, EAST_WEST_REDWOOD_LOG -> UP_DOWN_REDWOOD_LOG;
            case UP_DOWN_STRIPPED_REDWOOD_LOG, NORTH_SOUTH_STRIPPED_REDWOOD_LOG, EAST_WEST_STRIPPED_REDWOOD_LOG ->
                    UP_DOWN_STRIPPED_REDWOOD_LOG;
            case UP_DOWN_BLACK_WOOD_LOG, NORTH_SOUTH_BLACK_WOOD_LOG, EAST_WEST_BLACK_WOOD_LOG -> UP_DOWN_BLACK_WOOD_LOG;
            case UP_DOWN_STRIPPED_BLACK_WOOD_LOG, NORTH_SOUTH_STRIPPED_BLACK_WOOD_LOG,
                 EAST_WEST_STRIPPED_BLACK_WOOD_LOG -> UP_DOWN_STRIPPED_BLACK_WOOD_LOG;

            case NORTH_FURNACE, SOUTH_FURNACE, WEST_FURNACE, EAST_FURNACE -> NORTH_FURNACE;

            default -> block & BASE_BLOCK_MASK;
        };

        switch (blockType) {
            case FULL_BLOCK -> {
                return (short) baseBlock;
            }
            case TOP_PLAYER_HEAD, BOTTOM_PLAYER_HEAD, NORTH_PLAYER_HEAD, SOUTH_PLAYER_HEAD, WEST_PLAYER_HEAD,
                 EAST_PLAYER_HEAD -> {
                return (short) (baseBlock | BOTTOM_PLAYER_HEAD);
            }
            case NORTH_SLAB, TOP_SLAB, WEST_SLAB, SOUTH_SLAB, BOTTOM_SLAB, EAST_SLAB -> {
                return (short) (baseBlock | BOTTOM_SLAB);
            }
            case NORTH_PLATE, TOP_PLATE, WEST_PLATE, SOUTH_PLATE, BOTTOM_PLATE, EAST_PLATE -> {
                return (short) (baseBlock | BOTTOM_PLATE);
            }
            case NORTH_SOCKET, TOP_SOCKET, WEST_SOCKET, SOUTH_SOCKET, BOTTOM_SOCKET, EAST_SOCKET -> {
                return (short) (baseBlock | BOTTOM_SOCKET);
            }
            case NORTH_SOUTH_WALL, UP_DOWN_WALL, EAST_WEST_WALL -> {
                return (short) (baseBlock | NORTH_SOUTH_WALL);
            }
            case UP_DOWN_POST, NORTH_SOUTH_POST, EAST_WEST_POST -> {
                return (short) (baseBlock | UP_DOWN_POST);
            }
            case BOTTOM_NORTH_STAIR, BOTTOM_WEST_STAIR, BOTTOM_SOUTH_STAIR, BOTTOM_EAST_STAIR, TOP_NORTH_STAIR,
                 TOP_WEST_STAIR, TOP_SOUTH_STAIR, TOP_EAST_STAIR, NORTH_WEST_STAIR, NORTH_EAST_STAIR, SOUTH_WEST_STAIR,
                 SOUTH_EAST_STAIR -> {
                return (short) (baseBlock | BOTTOM_SOUTH_STAIR);
            }
            case THIN_BOTTOM_NORTH_STAIR, THIN_BOTTOM_WEST_STAIR, THIN_BOTTOM_SOUTH_STAIR, THIN_BOTTOM_EAST_STAIR,
                 THIN_TOP_NORTH_STAIR, THIN_TOP_WEST_STAIR, THIN_TOP_SOUTH_STAIR, THIN_TOP_EAST_STAIR,
                 THIN_NORTH_WEST_STAIR, THIN_NORTH_EAST_STAIR, THIN_SOUTH_WEST_STAIR, THIN_SOUTH_EAST_STAIR -> {
                return (short) (baseBlock | THIN_BOTTOM_SOUTH_STAIR);
            }
            case THICK_BOTTOM_NORTH_STAIR, THICK_BOTTOM_WEST_STAIR, THICK_BOTTOM_SOUTH_STAIR, THICK_BOTTOM_EAST_STAIR,
                 THICK_TOP_NORTH_STAIR, THICK_TOP_WEST_STAIR, THICK_TOP_SOUTH_STAIR, THICK_TOP_EAST_STAIR,
                 THICK_NORTH_WEST_STAIR, THICK_NORTH_EAST_STAIR, THICK_SOUTH_WEST_STAIR, THICK_SOUTH_EAST_STAIR -> {
                return (short) (baseBlock | THICK_BOTTOM_SOUTH_STAIR);
            }
            //Not scuffed at all
            case UP_DOWN_FENCE, UP_DOWN_FENCE_NORTH, UP_DOWN_FENCE_WEST, UP_DOWN_FENCE_NORTH_WEST, UP_DOWN_FENCE_SOUTH,
                 UP_DOWN_FENCE_NORTH_SOUTH, UP_DOWN_FENCE_WEST_SOUTH, UP_DOWN_FENCE_NORTH_WEST_SOUTH,
                 UP_DOWN_FENCE_EAST,
                 UP_DOWN_FENCE_NORTH_EAST, UP_DOWN_FENCE_WEST_EAST, UP_DOWN_FENCE_NORTH_WEST_EAST,
                 UP_DOWN_FENCE_SOUTH_EAST, UP_DOWN_FENCE_NORTH_SOUTH_EAST, UP_DOWN_FENCE_WEST_SOUTH_EAST,
                 UP_DOWN_FENCE_NORTH_WEST_SOUTH_EAST, NORTH_SOUTH_FENCE, NORTH_SOUTH_FENCE_UP, NORTH_SOUTH_FENCE_WEST,
                 NORTH_SOUTH_FENCE_UP_WEST, NORTH_SOUTH_FENCE_DOWN, NORTH_SOUTH_FENCE_UP_DOWN,
                 NORTH_SOUTH_FENCE_WEST_DOWN, NORTH_SOUTH_FENCE_UP_WEST_DOWN, NORTH_SOUTH_FENCE_EAST,
                 NORTH_SOUTH_FENCE_UP_EAST, NORTH_SOUTH_FENCE_WEST_EAST, NORTH_SOUTH_FENCE_UP_WEST_EAST,
                 NORTH_SOUTH_FENCE_DOWN_EAST, NORTH_SOUTH_FENCE_UP_DOWN_EAST, NORTH_SOUTH_FENCE_WEST_DOWN_EAST,
                 NORTH_SOUTH_FENCE_UP_WEST_DOWN_EAST, EAST_WEST_FENCE, EAST_WEST_FENCE_NORTH, EAST_WEST_FENCE_UP,
                 EAST_WEST_FENCE_NORTH_UP, EAST_WEST_FENCE_SOUTH, EAST_WEST_FENCE_NORTH_SOUTH,
                 EAST_WEST_FENCE_UP_SOUTH, EAST_WEST_FENCE_NORTH_UP_SOUTH, EAST_WEST_FENCE_DOWN,
                 EAST_WEST_FENCE_NORTH_DOWN, EAST_WEST_FENCE_UP_DOWN, EAST_WEST_FENCE_NORTH_UP_DOWN,
                 EAST_WEST_FENCE_SOUTH_DOWN, EAST_WEST_FENCE_NORTH_SOUTH_DOWN, EAST_WEST_FENCE_UP_SOUTH_DOWN,
                 EAST_WEST_FENCE_NORTH_UP_SOUTH_DOWN -> {
                return (short) (baseBlock | UP_DOWN_FENCE_NORTH_WEST);
            }

        }
        return AIR;
    }

    public static int getToPlaceBlockAddend(int primaryCameraDirection, Target target) {
        Vector3f inBlockPosition = target.inBlockPosition();
        int side = target.side() % 3;

        if (primaryCameraDirection == NORTH) if (side != NORTH) return Utils.fraction(inBlockPosition.z) > 0.5f ? 0 : 3;
        else return target.side() > 2 ? 0 : 3;

        if (primaryCameraDirection == TOP) if (side != TOP) return Utils.fraction(inBlockPosition.y) > 0.5f ? 0 : 3;
        else return target.side() > 2 ? 0 : 3;

//        if (primaryCameraDirection == WEST)
        if (side != WEST) return !(Utils.fraction(inBlockPosition.x) > 0.5f) ? 3 : 0;
        else return target.side() > 2 ? 0 : 3;

    }

    public static int getSmartBlockType(short block, int x, int y, int z) {
        int blockType = getBlockType(block);
        if (isNORTHSOUTHFenceType(blockType)) {
            int index = 0;
            short adjacentBlock;
            long adjacentMask;

            adjacentBlock = Chunk.getBlockInWorld(x, y + 1, z);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, BOTTOM);
            if ((adjacentMask & BLOCK_TYPE_OCCLUSION_DATA[EAST_WEST_WALL][TOP]) != 0 || isNORTHSOUTHFenceType(getBlockType(adjacentBlock)))
                index |= 1;

            adjacentBlock = Chunk.getBlockInWorld(x + 1, y, z);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, EAST);
            if ((adjacentMask & BLOCK_TYPE_OCCLUSION_DATA[UP_DOWN_WALL][WEST]) != 0 || isNORTHSOUTHFenceType(getBlockType(adjacentBlock)))
                index |= 2;

            adjacentBlock = Chunk.getBlockInWorld(x, y - 1, z);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, TOP);
            if ((adjacentMask & BLOCK_TYPE_OCCLUSION_DATA[EAST_WEST_WALL][BOTTOM]) != 0 || isNORTHSOUTHFenceType(getBlockType(adjacentBlock)))
                index |= 4;

            adjacentBlock = Chunk.getBlockInWorld(x - 1, y, z);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, WEST);
            if ((adjacentMask & BLOCK_TYPE_OCCLUSION_DATA[UP_DOWN_WALL][EAST]) != 0 || isNORTHSOUTHFenceType(getBlockType(adjacentBlock)))
                index |= 8;

            return NORTH_SOUTH_FENCE + index;
        }
        if (isUpDownFenceType(blockType)) {
            int index = 0;
            short adjacentBlock;
            long adjacentMask;

            adjacentBlock = Chunk.getBlockInWorld(x, y, z + 1);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, SOUTH);
            if ((adjacentMask & BLOCK_TYPE_OCCLUSION_DATA[EAST_WEST_WALL][NORTH]) != 0 || isUpDownFenceType(getBlockType(adjacentBlock)))
                index |= 1;

            adjacentBlock = Chunk.getBlockInWorld(x + 1, y, z);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, EAST);
            if ((adjacentMask & BLOCK_TYPE_OCCLUSION_DATA[NORTH_SOUTH_WALL][WEST]) != 0 || isUpDownFenceType(getBlockType(adjacentBlock)))
                index |= 2;

            adjacentBlock = Chunk.getBlockInWorld(x, y, z - 1);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, NORTH);
            if ((adjacentMask & BLOCK_TYPE_OCCLUSION_DATA[EAST_WEST_WALL][SOUTH]) != 0 || isUpDownFenceType(getBlockType(adjacentBlock)))
                index |= 4;

            adjacentBlock = Chunk.getBlockInWorld(x - 1, y, z);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, WEST);
            if ((adjacentMask & BLOCK_TYPE_OCCLUSION_DATA[NORTH_SOUTH_WALL][EAST]) != 0 || isUpDownFenceType(getBlockType(adjacentBlock)))
                index |= 8;

            return UP_DOWN_FENCE + index;
        }
        if (isEASTWESTFenceType(blockType)) {
            int index = 0;
            short adjacentBlock;
            long adjacentMask;

            adjacentBlock = Chunk.getBlockInWorld(x, y, z + 1);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, SOUTH);
            if ((adjacentMask & BLOCK_TYPE_OCCLUSION_DATA[UP_DOWN_WALL][NORTH]) != 0 || isEASTWESTFenceType(getBlockType(adjacentBlock)))
                index |= 1;

            adjacentBlock = Chunk.getBlockInWorld(x, y + 1, z);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, BOTTOM);
            if ((adjacentMask & BLOCK_TYPE_OCCLUSION_DATA[NORTH_SOUTH_WALL][TOP]) != 0 || isEASTWESTFenceType(getBlockType(adjacentBlock)))
                index |= 2;

            adjacentBlock = Chunk.getBlockInWorld(x, y, z - 1);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, NORTH);
            if ((adjacentMask & BLOCK_TYPE_OCCLUSION_DATA[UP_DOWN_WALL][SOUTH]) != 0 || isEASTWESTFenceType(getBlockType(adjacentBlock)))
                index |= 4;

            adjacentBlock = Chunk.getBlockInWorld(x, y - 1, z);
            adjacentMask = getBlockTypeOcclusionData(adjacentBlock, TOP);
            if ((adjacentMask & BLOCK_TYPE_OCCLUSION_DATA[NORTH_SOUTH_WALL][BOTTOM]) != 0 || isEASTWESTFenceType(getBlockType(adjacentBlock)))
                index |= 8;

            return EAST_WEST_FENCE + index;
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
        if (blockType == LIQUID_TYPE || blockType == FLOWER_TYPE) return false;
        if (blockType != FULL_BLOCK && blockType == getBlockType(referenceBlock)) return false;
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
            case NORTH, BOTTOM -> getSubX(blockType, side, corner, subDataAddend);
            case SOUTH -> (byte) -getSubX(blockType, side, corner, subDataAddend);
            case EAST -> getSubZ(blockType, side, corner, subDataAddend);
            default -> (byte) -getSubZ(blockType, side, corner, subDataAddend);
        };
    }

    public static byte getSubV(int blockType, int side, int corner, int subDataAddend) {
        if (BLOCK_TYPE_UV_SUB_DATA[blockType].length != 0)
            return BLOCK_TYPE_UV_SUB_DATA[blockType][(side << 3) + (corner << 1) + 1 + subDataAddend * 48];

        return switch (side) {
            case NORTH, SOUTH, WEST, EAST -> (byte) -getSubY(blockType, side, corner, subDataAddend);
            case BOTTOM -> (byte) -getSubZ(blockType, side, corner, subDataAddend);
            default -> getSubX(blockType, side, corner, subDataAddend);
        };
    }

    public static int[] getDigSound(short block) {
        if ((block & 0xFFFF) < STANDARD_BLOCKS_THRESHOLD) {
            if (NON_STANDARD_BLOCK_DIG_SOUNDS[block & 0xFFFF].length != 0)
                return NON_STANDARD_BLOCK_DIG_SOUNDS[block & 0xFFFF];
        } else if (STANDARD_BLOCK_DIG_SOUNDS[(block & 0xFFFF) >> BLOCK_TYPE_BITS].length != 0)
            return STANDARD_BLOCK_DIG_SOUNDS[(block & 0xFFFF) >> BLOCK_TYPE_BITS];
        return null;
    }

    public static int[] getFootstepsSound(short block) {
        if ((block & 0xFFFF) < STANDARD_BLOCKS_THRESHOLD) {
            if (NON_STANDARD_BLOCK_STEP_SOUNDS[block & 0xFFFF].length != 0)
                return NON_STANDARD_BLOCK_STEP_SOUNDS[block & 0xFFFF];
        } else if (STANDARD_BLOCK_STEP_SOUNDS[(block & 0xFFFF) >> BLOCK_TYPE_BITS].length != 0)
            return STANDARD_BLOCK_STEP_SOUNDS[(block & 0xFFFF) >> BLOCK_TYPE_BITS];
        return null;
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
        return blockType >= UP_DOWN_FENCE && blockType <= UP_DOWN_FENCE_NORTH_WEST_SOUTH_EAST;
    }

    public static boolean isNORTHSOUTHFenceType(int blockType) {
        return blockType >= NORTH_SOUTH_FENCE && blockType <= NORTH_SOUTH_FENCE_UP_WEST_DOWN_EAST;
    }

    public static boolean isEASTWESTFenceType(int blockType) {
        return blockType >= EAST_WEST_FENCE && blockType <= EAST_WEST_FENCE_NORTH_UP_SOUTH_DOWN;
    }

    public static int getFaceCount(int blockType) {
        return BLOCK_TYPE_XYZ_SUB_DATA[blockType].length;
    }

    private static long fillOcclusionBits(int minX, int maxX, int minY, int maxY) {
        minX = minX >> 1;
        maxX = 16 + maxX >> 1;
        minY = minY >> 1;
        maxY = 16 + maxY >> 1;

        long value = 0;
        for (int x = minX; x < maxX; x++)
            for (int y = minY; y < maxY; y++) value |= 1L << (y << 3) + x;

        return value;
    }

    private static void initBlockTypeData() {

        BLOCK_TYPE_DATA[LIQUID_TYPE] = (byte) (DYNAMIC_SHAPE_MASK | OCCLUDES_DYNAMIC_SELF);
        BLOCK_TYPE_DATA[CACTUS_TYPE] = (byte) (DYNAMIC_SHAPE_MASK | OCCLUDES_ALL);

        for (int upDownFenceType = UP_DOWN_FENCE; upDownFenceType <= UP_DOWN_FENCE_NORTH_WEST_SOUTH_EAST; upDownFenceType++)
            BLOCK_TYPE_DATA[upDownFenceType] = SMART_BLOCK_TYPE | OCCLUDES_ALL;
        for (int EASTWESTFenceType = EAST_WEST_FENCE; EASTWESTFenceType <= EAST_WEST_FENCE_NORTH_UP_SOUTH_DOWN; EASTWESTFenceType++)
            BLOCK_TYPE_DATA[EASTWESTFenceType] = SMART_BLOCK_TYPE | OCCLUDES_ALL;
        for (int NORTHSOUTHFenceType = NORTH_SOUTH_FENCE; NORTHSOUTHFenceType <= NORTH_SOUTH_FENCE_UP_WEST_DOWN_EAST; NORTHSOUTHFenceType++)
            BLOCK_TYPE_DATA[NORTHSOUTHFenceType] = SMART_BLOCK_TYPE | OCCLUDES_ALL;

        for (int blockType = 0; blockType < TOTAL_AMOUNT_OF_BLOCK_TYPES; blockType++) {
            byte[] XYZSubData = BLOCK_TYPE_XYZ_SUB_DATA[blockType];
            long[] occlusionData = new long[XYZSubData.length];

            for (int aabbIndex = 0; aabbIndex < XYZSubData.length; aabbIndex += 6) {
                if (XYZSubData[MIN_X + aabbIndex] == 0)
                    occlusionData[EAST + aabbIndex] = fillOcclusionBits(XYZSubData[MIN_Z + aabbIndex], XYZSubData[MAX_Z + aabbIndex], XYZSubData[MIN_Y + aabbIndex], XYZSubData[MAX_Y + aabbIndex]);
                if (XYZSubData[MAX_X + aabbIndex] == 0)
                    occlusionData[WEST + aabbIndex] = fillOcclusionBits(XYZSubData[MIN_Z + aabbIndex], XYZSubData[MAX_Z + aabbIndex], XYZSubData[MIN_Y + aabbIndex], XYZSubData[MAX_Y + aabbIndex]);

                if (XYZSubData[MIN_Y + aabbIndex] == 0)
                    occlusionData[BOTTOM + aabbIndex] = fillOcclusionBits(XYZSubData[MIN_Z + aabbIndex], XYZSubData[MAX_Z + aabbIndex], XYZSubData[MIN_X + aabbIndex], XYZSubData[MAX_X + aabbIndex]);
                if (XYZSubData[MAX_Y + aabbIndex] == 0)
                    occlusionData[TOP + aabbIndex] = fillOcclusionBits(XYZSubData[MIN_Z + aabbIndex], XYZSubData[MAX_Z + aabbIndex], XYZSubData[MIN_X + aabbIndex], XYZSubData[MAX_X + aabbIndex]);

                if (XYZSubData[MIN_Z + aabbIndex] == 0)
                    occlusionData[SOUTH + aabbIndex] = fillOcclusionBits(XYZSubData[MIN_X + aabbIndex], XYZSubData[MAX_X + aabbIndex], XYZSubData[MIN_Y + aabbIndex], XYZSubData[MAX_Y + aabbIndex]);
                if (XYZSubData[MAX_Z + aabbIndex] == 0)
                    occlusionData[NORTH + aabbIndex] = fillOcclusionBits(XYZSubData[MIN_X + aabbIndex], XYZSubData[MAX_X + aabbIndex], XYZSubData[MIN_Y + aabbIndex], XYZSubData[MAX_Y + aabbIndex]);
            }
            BLOCK_TYPE_OCCLUSION_DATA[blockType] = occlusionData;
        }
    }

    private static void initXYZSubData() {
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE] = new byte[]{4, -4, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_NORTH] = new byte[]{4, -4, 0, 0, 4, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_WEST] = new byte[]{4, 0, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_NORTH_WEST] = new byte[]{4, -4, 0, 0, 4, 0, 12, 0, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_SOUTH] = new byte[]{4, -4, 0, 0, 0, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_NORTH_SOUTH] = new byte[]{4, -4, 0, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_WEST_SOUTH] = new byte[]{4, 0, 0, 0, 4, -4, 4, -4, 0, 0, 0, -12};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_NORTH_WEST_SOUTH] = new byte[]{4, -4, 0, 0, 0, 0, 12, 0, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_EAST] = new byte[]{0, -4, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_NORTH_EAST] = new byte[]{4, -4, 0, 0, 4, 0, 0, -12, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_WEST_EAST] = new byte[]{0, 0, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_NORTH_WEST_EAST] = new byte[]{0, 0, 0, 0, 4, -4, 4, -4, 0, 0, 12, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_SOUTH_EAST] = new byte[]{4, -4, 0, 0, 0, -4, 0, -12, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_NORTH_SOUTH_EAST] = new byte[]{4, -4, 0, 0, 0, 0, 0, -12, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_WEST_SOUTH_EAST] = new byte[]{0, 0, 0, 0, 4, -4, 4, -4, 0, 0, 0, -12};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_FENCE_NORTH_WEST_SOUTH_EAST] = new byte[]{0, 0, 0, 0, 4, -4, 4, -4, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[EAST_WEST_FENCE] = new byte[]{0, 0, 4, -4, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[EAST_WEST_FENCE_NORTH] = new byte[]{0, 0, 4, -4, 4, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[EAST_WEST_FENCE_UP] = new byte[]{0, 0, 4, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[EAST_WEST_FENCE_NORTH_UP] = new byte[]{0, 0, 4, -4, 4, 0, 0, 0, 12, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[EAST_WEST_FENCE_SOUTH] = new byte[]{0, 0, 4, -4, 0, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[EAST_WEST_FENCE_NORTH_SOUTH] = new byte[]{0, 0, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[EAST_WEST_FENCE_UP_SOUTH] = new byte[]{0, 0, 4, 0, 4, -4, 0, 0, 4, -4, 0, -12};
        BLOCK_TYPE_XYZ_SUB_DATA[EAST_WEST_FENCE_NORTH_UP_SOUTH] = new byte[]{0, 0, 4, -4, 0, 0, 0, 0, 12, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[EAST_WEST_FENCE_DOWN] = new byte[]{0, 0, 0, -4, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[EAST_WEST_FENCE_NORTH_DOWN] = new byte[]{0, 0, 4, -4, 4, 0, 0, 0, 0, -12, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[EAST_WEST_FENCE_UP_DOWN] = new byte[]{0, 0, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[EAST_WEST_FENCE_NORTH_UP_DOWN] = new byte[]{0, 0, 0, 0, 4, -4, 0, 0, 4, -4, 12, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[EAST_WEST_FENCE_SOUTH_DOWN] = new byte[]{0, 0, 4, -4, 0, -4, 0, 0, 0, -12, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[EAST_WEST_FENCE_NORTH_SOUTH_DOWN] = new byte[]{0, 0, 4, -4, 0, 0, 0, 0, 0, -12, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[EAST_WEST_FENCE_UP_SOUTH_DOWN] = new byte[]{0, 0, 0, 0, 4, -4, 0, 0, 4, -4, 0, -12};
        BLOCK_TYPE_XYZ_SUB_DATA[EAST_WEST_FENCE_NORTH_UP_SOUTH_DOWN] = new byte[]{0, 0, 0, 0, 4, -4, 0, 0, 4, -4, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_SOUTH_FENCE] = new byte[]{4, -4, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_SOUTH_FENCE_UP] = new byte[]{4, -4, 4, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_SOUTH_FENCE_WEST] = new byte[]{4, 0, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_SOUTH_FENCE_UP_WEST] = new byte[]{4, -4, 4, 0, 0, 0, 12, 0, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_SOUTH_FENCE_DOWN] = new byte[]{4, -4, 0, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_SOUTH_FENCE_UP_DOWN] = new byte[]{4, -4, 0, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_SOUTH_FENCE_WEST_DOWN] = new byte[]{4, 0, 4, -4, 0, 0, 4, -4, 0, -12, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_SOUTH_FENCE_UP_WEST_DOWN] = new byte[]{4, -4, 0, 0, 0, 0, 12, 0, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_SOUTH_FENCE_EAST] = new byte[]{0, -4, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_SOUTH_FENCE_UP_EAST] = new byte[]{4, -4, 4, 0, 0, 0, 0, -12, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_SOUTH_FENCE_WEST_EAST] = new byte[]{0, 0, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_SOUTH_FENCE_UP_WEST_EAST] = new byte[]{0, 0, 4, -4, 0, 0, 4, -4, 12, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_SOUTH_FENCE_DOWN_EAST] = new byte[]{4, -4, 0, -4, 0, 0, 0, -12, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_SOUTH_FENCE_UP_DOWN_EAST] = new byte[]{4, -4, 0, 0, 0, 0, 0, -12, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_SOUTH_FENCE_WEST_DOWN_EAST] = new byte[]{0, 0, 4, -4, 0, 0, 4, -4, 0, -12, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_SOUTH_FENCE_UP_WEST_DOWN_EAST] = new byte[]{0, 0, 4, -4, 0, 0, 4, -4, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_WEST_STAIR] = new byte[]{8, 0, 0, 0, 0, -8, 0, 0, 0, 0, 8, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_EAST_STAIR] = new byte[]{0, -8, 0, 0, 0, -8, 0, 0, 0, 0, 8, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[SOUTH_WEST_STAIR] = new byte[]{8, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, -8};
        BLOCK_TYPE_XYZ_SUB_DATA[SOUTH_EAST_STAIR] = new byte[]{0, -8, 0, 0, 8, 0, 0, 0, 0, 0, 0, -8};
        BLOCK_TYPE_XYZ_SUB_DATA[BOTTOM_NORTH_STAIR] = new byte[]{0, 0, 0, -8, 0, 0, 0, 0, 8, 0, 8, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[BOTTOM_WEST_STAIR] = new byte[]{0, 0, 0, -8, 0, 0, 8, 0, 8, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[BOTTOM_SOUTH_STAIR] = new byte[]{0, 0, 0, -8, 0, 0, 0, 0, 8, 0, 0, -8};
        BLOCK_TYPE_XYZ_SUB_DATA[BOTTOM_EAST_STAIR] = new byte[]{0, 0, 0, -8, 0, 0, 0, -8, 8, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[TOP_NORTH_STAIR] = new byte[]{0, 0, 8, 0, 0, 0, 0, 0, 0, -8, 8, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[TOP_WEST_STAIR] = new byte[]{0, 0, 8, 0, 0, 0, 8, 0, 0, -8, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[TOP_SOUTH_STAIR] = new byte[]{0, 0, 8, 0, 0, 0, 0, 0, 0, -8, 0, -8};
        BLOCK_TYPE_XYZ_SUB_DATA[TOP_EAST_STAIR] = new byte[]{0, 0, 8, 0, 0, 0, 0, -8, 0, -8, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[THIN_NORTH_WEST_STAIR] = new byte[]{12, 0, 0, 0, 0, -4, 0, 0, 0, 0, 12, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THIN_NORTH_EAST_STAIR] = new byte[]{0, -12, 0, 0, 0, -4, 0, 0, 0, 0, 12, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THIN_SOUTH_WEST_STAIR] = new byte[]{12, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, -12};
        BLOCK_TYPE_XYZ_SUB_DATA[THIN_SOUTH_EAST_STAIR] = new byte[]{0, -12, 0, 0, 4, 0, 0, 0, 0, 0, 0, -12};
        BLOCK_TYPE_XYZ_SUB_DATA[THIN_BOTTOM_NORTH_STAIR] = new byte[]{0, 0, 0, -12, 0, 0, 0, 0, 4, 0, 12, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THIN_BOTTOM_WEST_STAIR] = new byte[]{0, 0, 0, -12, 0, 0, 12, 0, 4, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THIN_BOTTOM_SOUTH_STAIR] = new byte[]{0, 0, 0, -12, 0, 0, 0, 0, 4, 0, 0, -12};
        BLOCK_TYPE_XYZ_SUB_DATA[THIN_BOTTOM_EAST_STAIR] = new byte[]{0, 0, 0, -12, 0, 0, 0, -12, 4, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THIN_TOP_NORTH_STAIR] = new byte[]{0, 0, 12, 0, 0, 0, 0, 0, 0, -4, 12, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THIN_TOP_WEST_STAIR] = new byte[]{0, 0, 12, 0, 0, 0, 12, 0, 0, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THIN_TOP_SOUTH_STAIR] = new byte[]{0, 0, 12, 0, 0, 0, 0, 0, 0, -4, 0, -12};
        BLOCK_TYPE_XYZ_SUB_DATA[THIN_TOP_EAST_STAIR] = new byte[]{0, 0, 12, 0, 0, 0, 0, -12, 0, -4, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[THICK_NORTH_WEST_STAIR] = new byte[]{4, 0, 0, 0, 0, -12, 0, 0, 0, 0, 4, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THICK_NORTH_EAST_STAIR] = new byte[]{0, -4, 0, 0, 0, -12, 0, 0, 0, 0, 4, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THICK_SOUTH_WEST_STAIR] = new byte[]{4, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[THICK_SOUTH_EAST_STAIR] = new byte[]{0, -4, 0, 0, 12, 0, 0, 0, 0, 0, 0, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[THICK_BOTTOM_NORTH_STAIR] = new byte[]{0, 0, 0, -4, 0, 0, 0, 0, 12, 0, 4, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THICK_BOTTOM_WEST_STAIR] = new byte[]{0, 0, 0, -4, 0, 0, 4, 0, 12, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THICK_BOTTOM_SOUTH_STAIR] = new byte[]{0, 0, 0, -4, 0, 0, 0, 0, 12, 0, 0, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[THICK_BOTTOM_EAST_STAIR] = new byte[]{0, 0, 0, -4, 0, 0, 0, -4, 12, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THICK_TOP_NORTH_STAIR] = new byte[]{0, 0, 4, 0, 0, 0, 0, 0, 0, -12, 4, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THICK_TOP_WEST_STAIR] = new byte[]{0, 0, 4, 0, 0, 0, 4, 0, 0, -12, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[THICK_TOP_SOUTH_STAIR] = new byte[]{0, 0, 4, 0, 0, 0, 0, 0, 0, -12, 0, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[THICK_TOP_EAST_STAIR] = new byte[]{0, 0, 4, 0, 0, 0, 0, -4, 0, -12, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_WALL] = new byte[]{0, 0, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_SOUTH_WALL] = new byte[]{0, 0, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[EAST_WEST_WALL] = new byte[]{4, -4, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[EAST_WEST_POST] = new byte[]{0, 0, 4, -4, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_SOUTH_POST] = new byte[]{4, -4, 4, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[UP_DOWN_POST] = new byte[]{4, -4, 0, 0, 4, -4};

        BLOCK_TYPE_XYZ_SUB_DATA[BOTTOM_SLAB] = new byte[]{0, 0, 0, -8, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[TOP_SLAB] = new byte[]{0, 0, 8, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_SLAB] = new byte[]{0, 0, 0, 0, 8, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[SOUTH_SLAB] = new byte[]{0, 0, 0, 0, 0, -8};
        BLOCK_TYPE_XYZ_SUB_DATA[WEST_SLAB] = new byte[]{8, 0, 0, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[EAST_SLAB] = new byte[]{0, -8, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[BOTTOM_SOCKET] = new byte[]{0, 0, 0, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[TOP_SOCKET] = new byte[]{0, 0, 4, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_SOCKET] = new byte[]{0, 0, 0, 0, 4, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[SOUTH_SOCKET] = new byte[]{0, 0, 0, 0, 0, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[WEST_SOCKET] = new byte[]{4, 0, 0, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[EAST_SOCKET] = new byte[]{0, -4, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[BOTTOM_PLATE] = new byte[]{0, 0, 0, -12, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[TOP_PLATE] = new byte[]{0, 0, 12, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_PLATE] = new byte[]{0, 0, 0, 0, 12, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[SOUTH_PLATE] = new byte[]{0, 0, 0, 0, 0, -12};
        BLOCK_TYPE_XYZ_SUB_DATA[WEST_PLATE] = new byte[]{12, 0, 0, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[EAST_PLATE] = new byte[]{0, -12, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[CACTUS_TYPE] = new byte[]{1, -1, 0, 0, 1, -1};
        BLOCK_TYPE_XYZ_SUB_DATA[TORCH_TYPE] = new byte[]{7, -7, 0, -4, 7, -7};
        BLOCK_TYPE_XYZ_SUB_DATA[FLOWER_TYPE] = new byte[]{4, -4, 0, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[PATH_TYPE] = new byte[]{0, 0, 0, -2, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[TOP_PLAYER_HEAD] = new byte[]{4, -4, 8, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[BOTTOM_PLAYER_HEAD] = new byte[]{4, -4, 0, -8, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_PLAYER_HEAD] = new byte[]{4, -4, 4, -4, 8, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[SOUTH_PLAYER_HEAD] = new byte[]{4, -4, 4, -4, 0, -8};
        BLOCK_TYPE_XYZ_SUB_DATA[WEST_PLAYER_HEAD] = new byte[]{8, 0, 4, -4, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[EAST_PLAYER_HEAD] = new byte[]{0, -8, 4, -4, 4, -4};

        BLOCK_TYPE_XYZ_SUB_DATA[FULL_BLOCK] = new byte[]{0, 0, 0, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[LIQUID_TYPE] = new byte[]{0, 0, 0, 0, 0, 0};
    }

    private static void initUVSubData() {
        BLOCK_TYPE_UV_SUB_DATA[TORCH_TYPE] = new byte[]{7, 4, -7, 4, 7, 0, -7, 0, 7, 4, -7, 4, 7, -10, -7, -10, 7, 4, -7, 4, 7, 0, -7, 0, 7, 4, -7, 4, 7, 0, -7, 0, -7, -7, -7, 7, 7, -7, 7, 7, -7, 4, 7, 4, -7, 0, 7, 0};
        BLOCK_TYPE_UV_SUB_DATA[BOTTOM_PLAYER_HEAD] = new byte[]{4, 4, -4, 4, 4, -4, -4, -4, 4, 4, -4, 4, 4, -4, -4, -4, 4, 4, -4, 4, 4, -4, -4, -4, 4, 4, -4, 4, 4, -4, -4, -4, -4, -4, -4, 4, 4, -4, 4, 4, -4, 4, 4, 4, -4, -4, 4, -4};
        BLOCK_TYPE_UV_SUB_DATA[TOP_PLAYER_HEAD] = BLOCK_TYPE_UV_SUB_DATA[BOTTOM_PLAYER_HEAD];
        BLOCK_TYPE_UV_SUB_DATA[NORTH_PLAYER_HEAD] = BLOCK_TYPE_UV_SUB_DATA[BOTTOM_PLAYER_HEAD];
        BLOCK_TYPE_UV_SUB_DATA[SOUTH_PLAYER_HEAD] = BLOCK_TYPE_UV_SUB_DATA[BOTTOM_PLAYER_HEAD];
        BLOCK_TYPE_UV_SUB_DATA[WEST_PLAYER_HEAD] = BLOCK_TYPE_UV_SUB_DATA[BOTTOM_PLAYER_HEAD];
        BLOCK_TYPE_UV_SUB_DATA[EAST_PLAYER_HEAD] = BLOCK_TYPE_UV_SUB_DATA[BOTTOM_PLAYER_HEAD];
    }

    private static void setNonStandardBlockData(short block, int properties, int[] digSounds, int[] stepSounds, int type, byte[] textures) {
        if (textures != null) NON_STANDARD_BLOCK_TEXTURE_INDICES[block & 0xFFFF] = textures;
        NON_STANDARD_BLOCK_PROPERTIES[block & 0xFFFF] = properties;
        if (digSounds != null) NON_STANDARD_BLOCK_DIG_SOUNDS[block & 0xFFFF] = digSounds;
        if (stepSounds != null) NON_STANDARD_BLOCK_STEP_SOUNDS[block & 0xFFFF] = stepSounds;
        NON_STANDARD_BLOCK_TYPE[block & 0xFFFF] = type;
    }

    private static void setStandardBlockData(short block, int properties, int[] digSounds, int[] stepSounds, byte[] textures) {
        if (textures != null) STANDARD_BLOCK_TEXTURE_INDICES[(block & 0xFFFF) >> BLOCK_TYPE_BITS] = textures;
        STANDARD_BLOCK_PROPERTIES[(block & 0xFFFF) >> BLOCK_TYPE_BITS] = properties;
        if (digSounds != null) STANDARD_BLOCK_DIG_SOUNDS[(block & 0xFFFF) >> BLOCK_TYPE_BITS] = digSounds;
        if (stepSounds != null) STANDARD_BLOCK_STEP_SOUNDS[(block & 0xFFFF) >> BLOCK_TYPE_BITS] = stepSounds;
    }

    private static void setStandardBlockData(short block, int properties, int[] digSounds, int[] stepSounds, byte texture) {
        STANDARD_BLOCK_TEXTURE_INDICES[(block & 0xFFFF) >> BLOCK_TYPE_BITS] = new byte[]{texture};
        STANDARD_BLOCK_PROPERTIES[(block & 0xFFFF) >> BLOCK_TYPE_BITS] = properties;
        if (digSounds != null) STANDARD_BLOCK_DIG_SOUNDS[(block & 0xFFFF) >> BLOCK_TYPE_BITS] = digSounds;
        if (stepSounds != null) STANDARD_BLOCK_STEP_SOUNDS[(block & 0xFFFF) >> BLOCK_TYPE_BITS] = stepSounds;
    }

    private static void setLogData(short upDownLog, short northSouthLog, short eastWestLog, byte topTexture, byte sideTexture, byte rotatedSideTexture, SoundManager sound) {
        STANDARD_BLOCK_TEXTURE_INDICES[(upDownLog & 0xFFFF) >> BLOCK_TYPE_BITS] = new byte[]{sideTexture, topTexture, sideTexture, sideTexture, topTexture, sideTexture};
        STANDARD_BLOCK_TEXTURE_INDICES[(northSouthLog & 0xFFFF) >> BLOCK_TYPE_BITS] = new byte[]{topTexture, rotatedSideTexture, rotatedSideTexture, topTexture, sideTexture, rotatedSideTexture};
        STANDARD_BLOCK_TEXTURE_INDICES[(eastWestLog & 0xFFFF) >> BLOCK_TYPE_BITS] = new byte[]{rotatedSideTexture, sideTexture, topTexture, rotatedSideTexture, rotatedSideTexture, topTexture};

        STANDARD_BLOCK_DIG_SOUNDS[(upDownLog & 0xFFFF) >> BLOCK_TYPE_BITS] = sound.digWood;
        STANDARD_BLOCK_DIG_SOUNDS[(northSouthLog & 0xFFFF) >> BLOCK_TYPE_BITS] = sound.digWood;
        STANDARD_BLOCK_DIG_SOUNDS[(eastWestLog & 0xFFFF) >> BLOCK_TYPE_BITS] = sound.digWood;

        STANDARD_BLOCK_STEP_SOUNDS[(upDownLog & 0xFFFF) >> BLOCK_TYPE_BITS] = sound.stepWood;
        STANDARD_BLOCK_STEP_SOUNDS[(northSouthLog & 0xFFFF) >> BLOCK_TYPE_BITS] = sound.stepWood;
        STANDARD_BLOCK_STEP_SOUNDS[(eastWestLog & 0xFFFF) >> BLOCK_TYPE_BITS] = sound.stepWood;

        STANDARD_BLOCK_PROPERTIES[(upDownLog & 0xFFFF) >> BLOCK_TYPE_BITS] = 0;
        STANDARD_BLOCK_PROPERTIES[(northSouthLog & 0xFFFF) >> BLOCK_TYPE_BITS] = 0;
        STANDARD_BLOCK_PROPERTIES[(eastWestLog & 0xFFFF) >> BLOCK_TYPE_BITS] = 0;
    }

    private static void initNonStandardBlocks() {
        SoundManager sound = Launcher.getSound();

        setNonStandardBlockData(AIR, NO_COLLISION | REPLACEABLE, null, null, AIR_TYPE, null);
        setNonStandardBlockData(OUT_OF_WORLD, 0, null, null, FULL_BLOCK, null);
        setNonStandardBlockData(WATER, NO_COLLISION | REPLACEABLE | BLAST_RESISTANT, sound.splash, sound.splash, LIQUID_TYPE, new byte[]{(byte) 64});
        setNonStandardBlockData(LAVA, NO_COLLISION | REPLACEABLE | BLAST_RESISTANT | LIGHT_EMITTING, sound.lavaPop, sound.lavaPop, LIQUID_TYPE, new byte[]{(byte) -127});
        setNonStandardBlockData(CACTUS, 0, sound.digWood, sound.stepWood, CACTUS_TYPE, new byte[]{(byte) 113, (byte) -92, (byte) 113, (byte) 113, (byte) -92, (byte) 113});
        setNonStandardBlockData(NORTH_CREATOR_HEAD, 0, sound.digWood, sound.stepWood, BOTTOM_PLAYER_HEAD, new byte[]{(byte) -124, (byte) -109, (byte) -123, (byte) -108, (byte) -107, (byte) -125});
        setNonStandardBlockData(WEST_CREATOR_HEAD, 0, sound.digWood, sound.stepWood, BOTTOM_PLAYER_HEAD, new byte[]{(byte) -125, (byte) -109, (byte) -124, (byte) -123, (byte) -91, (byte) -108});
        setNonStandardBlockData(SOUTH_CREATOR_HEAD, 0, sound.digWood, sound.stepWood, BOTTOM_PLAYER_HEAD, new byte[]{(byte) -108, (byte) -109, (byte) -125, (byte) -124, (byte) -107, (byte) -123});
        setNonStandardBlockData(EAST_CREATOR_HEAD, 0, sound.digWood, sound.stepWood, BOTTOM_PLAYER_HEAD, new byte[]{(byte) -123, (byte) -109, (byte) -108, (byte) -125, (byte) -91, (byte) -124});
        setNonStandardBlockData(TORCH, NO_COLLISION | LIGHT_EMITTING, sound.digWood, sound.stepWood, TORCH_TYPE, new byte[]{(byte) -80});
        setNonStandardBlockData(TALL_GRASS, NO_COLLISION | REPLACEABLE, sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) -64});
        setNonStandardBlockData(RED_TULIP, NO_COLLISION | REPLACEABLE, sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) -63});
        setNonStandardBlockData(YELLOW_TULIP, NO_COLLISION | REPLACEABLE, sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) -62});
        setNonStandardBlockData(ORANGE_TULIP, NO_COLLISION | REPLACEABLE, sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) -61});
        setNonStandardBlockData(MAGENTA_TULIP, NO_COLLISION | REPLACEABLE, sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) -60});
        setNonStandardBlockData(ROSE, NO_COLLISION | REPLACEABLE, sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) -59});
        setNonStandardBlockData(HYACINTH, NO_COLLISION | REPLACEABLE, sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) -58});
        setNonStandardBlockData(DRISLY, NO_COLLISION | REPLACEABLE, sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) 0xD0});
        setNonStandardBlockData(SHRUB, NO_COLLISION | REPLACEABLE, sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) 0xD1});
        setNonStandardBlockData(SUGAR_CANE, NO_COLLISION, sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) 0xD2});
        setNonStandardBlockData(PATH_BLOCK, 0, sound.digGrass, sound.stepGrass, PATH_TYPE, new byte[]{(byte) -72, (byte) -88, (byte) -72, (byte) -72, (byte) 1, (byte) -72});
        setNonStandardBlockData(BLACK_ROSE, NO_COLLISION | REPLACEABLE,sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) 0xD3});
        setNonStandardBlockData(FLIELEN, NO_COLLISION | REPLACEABLE,sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) 0xD4});
    }

    private static void initStandardBlocks() {
        SoundManager sound = Launcher.getSound();

        setStandardBlockData(GRASS, 0, sound.digGrass, sound.stepGrass, new byte[]{(byte) 0xBA, (byte) 0xAA, (byte) 0xBA, (byte) 0xBA, (byte) 1, (byte) 0xBA});
        setStandardBlockData(DIRT, 0, sound.digGrass, sound.stepDirt, (byte) 1);
        setStandardBlockData(STONE, 0, sound.digStone, sound.stepStone, (byte) 2);
        setStandardBlockData(STONE_BRICK, 0, sound.digStone, sound.stepStone, (byte) 34);
        setStandardBlockData(COBBLESTONE, 0, sound.digStone, sound.stepStone, (byte) 50);
        setStandardBlockData(CHISELED_STONE, 0, sound.digStone, sound.stepStone, (byte) 81);
        setStandardBlockData(POLISHED_STONE, 0, sound.digStone, sound.stepStone, (byte) 66);
        setStandardBlockData(CHISELED_POLISHED_STONE, 0, sound.digStone, sound.stepStone, (byte) 82);
        setStandardBlockData(MUD, 0, sound.digGrass, sound.stepDirt, (byte) 17);
        setStandardBlockData(ANDESITE, 0, sound.digStone, sound.stepStone, (byte) 18);
        setStandardBlockData(SNOW, 0, sound.digSnow, sound.stepSnow, (byte) 32);
        setStandardBlockData(SAND, 0, sound.digSand, sound.stepSand, (byte) 33);
        setStandardBlockData(SANDSTONE, 0, sound.digStone, sound.stepStone, (byte) -95);
        setStandardBlockData(POLISHED_SANDSTONE, 0, sound.digStone, sound.stepStone, (byte) -94);
        setStandardBlockData(SLATE, 0, sound.digStone, sound.stepStone, (byte) 48);
        setStandardBlockData(CHISELED_SLATE, 0, sound.digStone, sound.stepStone, (byte) -128);
        setStandardBlockData(COBBLED_SLATE, 0, sound.digStone, sound.stepStone, (byte) 114);
        setStandardBlockData(SLATE_BRICKS, 0, sound.digStone, sound.stepStone, (byte) -126);
        setStandardBlockData(POLISHED_SLATE, 0, sound.digStone, sound.stepStone, (byte) -96);
        setStandardBlockData(GLASS, 0, sound.digGlass, sound.stepGlass, (byte) 49);
        setStandardBlockData(GRAVEL, 0, sound.digGravel, sound.stepGravel, (byte) 65);
        setStandardBlockData(COURSE_DIRT, 0, sound.digGrass, sound.stepDirt, (byte) 80);
        setStandardBlockData(CLAY, 0, sound.digGrass, sound.stepDirt, (byte) 97);
        setStandardBlockData(MOSS, 0, sound.digFoliage, sound.stepFoliage, (byte) 98);
        setStandardBlockData(ICE, 0, sound.digIce, sound.stepGlass, (byte) 96);
        setStandardBlockData(HEAVY_ICE, 0, sound.digIce, sound.stepGlass, (byte) 112);
        setStandardBlockData(COAL_ORE, 0, sound.digStone, sound.stepStone, (byte) -112);
        setStandardBlockData(IRON_ORE, 0, sound.digStone, sound.stepStone, (byte) -111);
        setStandardBlockData(DIAMOND_ORE, 0, sound.digStone, sound.stepStone, (byte) -110);

        setLogData(UP_DOWN_OAK_LOG, NORTH_SOUTH_OAK_LOG, EAST_WEST_OAK_LOG, (byte) 19, (byte) 3, (byte) 99, sound);
        setLogData(UP_DOWN_STRIPPED_OAK_LOG, NORTH_SOUTH_STRIPPED_OAK_LOG, EAST_WEST_STRIPPED_OAK_LOG, (byte) 51, (byte) 35, (byte) 115, sound);
        setLogData(UP_DOWN_SPRUCE_LOG, NORTH_SOUTH_SPRUCE_LOG, EAST_WEST_SPRUCE_LOG, (byte) 20, (byte) 4, (byte) 100, sound);
        setLogData(UP_DOWN_STRIPPED_SPRUCE_LOG, NORTH_SOUTH_STRIPPED_SPRUCE_LOG, EAST_WEST_STRIPPED_SPRUCE_LOG, (byte) 52, (byte) 36, (byte) 116, sound);
        setLogData(UP_DOWN_DARK_OAK_LOG, NORTH_SOUTH_DARK_OAK_LOG, EAST_WEST_DARK_OAK_LOG, (byte) 21, (byte) 5, (byte) 101, sound);
        setLogData(UP_DOWN_STRIPPED_DARK_OAK_LOG, NORTH_SOUTH_STRIPPED_DARK_OAK_LOG, EAST_WEST_STRIPPED_DARK_OAK_LOG, (byte) 53, (byte) 37, (byte) 117, sound);
        setLogData(UP_DOWN_PINE_LOG, NORTH_SOUTH_PINE_LOG, EAST_WEST_PINE_LOG, (byte) 22, (byte) 6, (byte) 102, sound);
        setLogData(UP_DOWN_STRIPPED_PINE_LOG, NORTH_SOUTH_STRIPPED_PINE_LOG, EAST_WEST_STRIPPED_PINE_LOG, (byte) 54, (byte) 38, (byte) 118, sound);
        setLogData(UP_DOWN_REDWOOD_LOG, NORTH_SOUTH_REDWOOD_LOG, EAST_WEST_REDWOOD_LOG, (byte) 23, (byte) 7, (byte) 103, sound);
        setLogData(UP_DOWN_STRIPPED_REDWOOD_LOG, NORTH_SOUTH_STRIPPED_REDWOOD_LOG, EAST_WEST_STRIPPED_REDWOOD_LOG, (byte) 55, (byte) 39, (byte) 119, sound);
        setLogData(UP_DOWN_BLACK_WOOD_LOG, NORTH_SOUTH_BLACK_WOOD_LOG, EAST_WEST_BLACK_WOOD_LOG, (byte) 24, (byte) 8, (byte) 104, sound);
        setLogData(UP_DOWN_STRIPPED_BLACK_WOOD_LOG, NORTH_SOUTH_STRIPPED_BLACK_WOOD_LOG, EAST_WEST_STRIPPED_BLACK_WOOD_LOG, (byte) 56, (byte) 40, (byte) 120, sound);

        setStandardBlockData(OAK_LEAVES, 0, sound.digFoliage, sound.stepFoliage, (byte) 83);
        setStandardBlockData(SPRUCE_LEAVES, 0, sound.digFoliage, sound.stepFoliage, (byte) 84);
        setStandardBlockData(DARK_OAK_LEAVES, 0, sound.digFoliage, sound.stepFoliage, (byte) 85);
        setStandardBlockData(PINE_LEAVES, 0, sound.digFoliage, sound.stepFoliage, (byte) 86);
        setStandardBlockData(REDWOOD_LEAVES, 0, sound.digFoliage, sound.stepFoliage, (byte) 87);
        setStandardBlockData(BLACK_WOOD_LEAVES, 0, sound.digFoliage, sound.stepFoliage, (byte) 88);
        setStandardBlockData(OAK_PLANKS, 0, sound.digWood, sound.stepWood, (byte) 67);
        setStandardBlockData(SPRUCE_PLANKS, 0, sound.digWood, sound.stepWood, (byte) 68);
        setStandardBlockData(DARK_OAK_PLANKS, 0, sound.digWood, sound.stepWood, (byte) 69);
        setStandardBlockData(PINE_PLANKS, 0, sound.digWood, sound.stepWood, (byte) 70);
        setStandardBlockData(REDWOOD_PLANKS, 0, sound.digWood, sound.stepWood, (byte) 71);
        setStandardBlockData(BLACK_WOOD_PLANKS, 0, sound.digWood, sound.stepWood, (byte) 72);
        setStandardBlockData(CRACKED_ANDESITE, 0, sound.digStone, sound.stepStone, (byte) -93);
        setStandardBlockData(BLACK, 0, sound.digStone, sound.stepStone, (byte) -9);
        setStandardBlockData(WHITE, 0, sound.digStone, sound.stepStone, (byte) -8);
        setStandardBlockData(CYAN, 0, sound.digStone, sound.stepStone, (byte) -7);
        setStandardBlockData(MAGENTA, 0, sound.digStone, sound.stepStone, (byte) -6);
        setStandardBlockData(YELLOW, 0, sound.digStone, sound.stepStone, (byte) -5);
        setStandardBlockData(BLUE, 0, sound.digStone, sound.stepStone, (byte) -4);
        setStandardBlockData(GREEN, 0, sound.digStone, sound.stepStone, (byte) -3);
        setStandardBlockData(RED, 0, sound.digStone, sound.stepStone, (byte) -2);
        setStandardBlockData(CRAFTING_TABLE, INTERACTABLE, sound.digWood, sound.stepWood, new byte[]{(byte) -78, (byte) -79, (byte) -77, (byte) -78, (byte) 68, (byte) -77});
        setStandardBlockData(TNT, INTERACTABLE, sound.digGrass, sound.stepGrass, new byte[]{(byte) -106, (byte) -122, (byte) -106, (byte) -106, (byte) -90, (byte) -106});
        setStandardBlockData(OBSIDIAN, BLAST_RESISTANT, sound.digStone, sound.stepStone, (byte) -76);
        setStandardBlockData(MOSSY_STONE, 0, sound.digStone, sound.stepStone, (byte) -17);
        setStandardBlockData(MOSSY_ANDESITE, 0, sound.digStone, sound.stepStone, (byte) -18);
        setStandardBlockData(MOSSY_STONE_BRICK, 0, sound.digStone, sound.stepStone, (byte) -19);
        setStandardBlockData(MOSSY_POLISHED_STONE, 0, sound.digStone, sound.stepStone, (byte) -20);
        setStandardBlockData(MOSSY_CHISELED_POLISHED_STONE, 0, sound.digStone, sound.stepStone, (byte) -21);
        setStandardBlockData(MOSSY_CHISELED_STONE, 0, sound.digStone, sound.stepStone, (byte) -22);
        setStandardBlockData(MOSSY_SLATE, 0, sound.digStone, sound.stepStone, (byte) -23);
        setStandardBlockData(MOSSY_COBBLED_SLATE, 0, sound.digStone, sound.stepStone, (byte) -24);
        setStandardBlockData(MOSSY_SLATE_BRICKS, 0, sound.digStone, sound.stepStone, (byte) -25);
        setStandardBlockData(MOSSY_CHISELED_SLATE, 0, sound.digStone, sound.stepStone, (byte) -26);
        setStandardBlockData(MOSSY_POLISHED_SLATE, 0, sound.digStone, sound.stepStone, (byte) -27);
        setStandardBlockData(MOSSY_DIRT, 0, sound.digGrass, sound.stepDirt, (byte) -28);
        setStandardBlockData(MOSSY_GRAVEL, 0, sound.digGravel, sound.stepGravel, (byte) -29);
        setStandardBlockData(MOSSY_OBSIDIAN, BLAST_RESISTANT, sound.digStone, sound.stepStone, (byte) -30);
        setStandardBlockData(MOSSY_CRACKED_ANDESITE, 0, sound.digStone, sound.stepStone, (byte) -31);
        setStandardBlockData(MOSSY_COBBLESTONE, 0, sound.digStone, sound.stepStone, (byte) -32);
        setStandardBlockData(SEA_LIGHT, LIGHT_EMITTING, sound.digGlass, sound.stepGlass, (byte) -120);
        setStandardBlockData(PODZOL, 0, sound.digGrass, sound.stepGrass, new byte[]{(byte) 0xB9, (byte) 0xA9, (byte) 0xB9, (byte) 0xB9, (byte) 0x01, (byte) 0xB9});
        setStandardBlockData(RED_SAND, 0, sound.digSand, sound.stepSand, (byte) 0xBE);
        setStandardBlockData(RED_POLISHED_SANDSTONE, 0, sound.digStone, sound.stepStone, (byte) 0xBD);
        setStandardBlockData(RED_SANDSTONE, 0, sound.digStone, sound.stepStone, (byte) 0xBC);
        setStandardBlockData(TERRACOTTA, 0, sound.digStone, sound.stepStone, (byte) 0xDF);
        setStandardBlockData(RED_TERRACOTTA, 0, sound.digStone, sound.stepStone, (byte) 0xDE);
        setStandardBlockData(GREEN_TERRACOTTA, 0, sound.digStone, sound.stepStone, (byte) 0xDD);
        setStandardBlockData(BLUE_TERRACOTTA, 0, sound.digStone, sound.stepStone, (byte) 0xDC);
        setStandardBlockData(YELLOW_TERRACOTTA, 0, sound.digStone, sound.stepStone, (byte) 0xDB);
        setStandardBlockData(MAGENTA_TERRACOTTA, 0, sound.digStone, sound.stepStone, (byte) 0xDA);
        setStandardBlockData(CYAN_TERRACOTTA, 0, sound.digStone, sound.stepStone, (byte) 0xD9);
        setStandardBlockData(WHITE_TERRACOTTA, 0, sound.digStone, sound.stepStone, (byte) 0xD8);
        setStandardBlockData(BLACK_TERRACOTTA, 0, sound.digStone, sound.stepStone, (byte) 0xD7);
        setStandardBlockData(RED_WOOL, 0, sound.digCloth, sound.stepCloth, (byte) 0xCE);
        setStandardBlockData(GREEN_WOOL, 0, sound.digCloth, sound.stepCloth, (byte) 0xCD);
        setStandardBlockData(BLUE_WOOL, 0, sound.digCloth, sound.stepCloth, (byte) 0xCC);
        setStandardBlockData(YELLOW_WOOL, 0, sound.digCloth, sound.stepCloth, (byte) 0xCB);
        setStandardBlockData(MAGENTA_WOOL, 0, sound.digCloth, sound.stepCloth, (byte) 0xCA);
        setStandardBlockData(CYAN_WOOL, 0, sound.digCloth, sound.stepCloth, (byte) 0xC9);
        setStandardBlockData(WHITE_WOOL, 0, sound.digCloth, sound.stepCloth, (byte) 0xC8);
        setStandardBlockData(BLACK_WOOL, 0, sound.digCloth, sound.stepCloth, (byte) 0xC7);

        setStandardBlockData(NORTH_FURNACE, INTERACTABLE, sound.digStone, sound.stepStone, new byte[]{(byte) -104, (byte) -121, (byte) -105, (byte) -105, (byte) -89, (byte) -105});
        setStandardBlockData(SOUTH_FURNACE, INTERACTABLE, sound.digStone, sound.stepStone, new byte[]{(byte) -105, (byte) -121, (byte) -105, (byte) -104, (byte) -89, (byte) -105});
        setStandardBlockData(WEST_FURNACE, INTERACTABLE, sound.digStone, sound.stepStone, new byte[]{(byte) -105, (byte) -121, (byte) -104, (byte) -105, (byte) -89, (byte) -105});
        setStandardBlockData(EAST_FURNACE, INTERACTABLE, sound.digStone, sound.stepStone, new byte[]{(byte) -105, (byte) -121, (byte) -105, (byte) -105, (byte) -89, (byte) -104});
    }

    //I don't know how to use JSON-Files, so just ignore it
    public static void init() {
        initNonStandardBlocks();
        initStandardBlocks();

        initXYZSubData();
        initUVSubData();
        initBlockTypeData();
    }

    private static final int[] NON_STANDARD_BLOCK_TYPE = new int[STANDARD_BLOCKS_THRESHOLD];
    private static final byte[][] NON_STANDARD_BLOCK_TEXTURE_INDICES = new byte[STANDARD_BLOCKS_THRESHOLD][1];
    private static final byte[][] STANDARD_BLOCK_TEXTURE_INDICES = new byte[AMOUNT_OF_STANDARD_BLOCKS][1];
    private static final int[] NON_STANDARD_BLOCK_PROPERTIES = new int[STANDARD_BLOCKS_THRESHOLD];
    private static final int[] STANDARD_BLOCK_PROPERTIES = new int[AMOUNT_OF_STANDARD_BLOCKS];

    private static final int[][] NON_STANDARD_BLOCK_DIG_SOUNDS = new int[STANDARD_BLOCKS_THRESHOLD][0];
    private static final int[][] STANDARD_BLOCK_DIG_SOUNDS = new int[AMOUNT_OF_STANDARD_BLOCKS][0];
    private static final int[][] NON_STANDARD_BLOCK_STEP_SOUNDS = new int[STANDARD_BLOCKS_THRESHOLD][0];
    private static final int[][] STANDARD_BLOCK_STEP_SOUNDS = new int[AMOUNT_OF_STANDARD_BLOCKS][0];

    private static final long[][] BLOCK_TYPE_OCCLUSION_DATA = new long[TOTAL_AMOUNT_OF_BLOCK_TYPES][0];
    private static final byte[] BLOCK_TYPE_DATA = new byte[TOTAL_AMOUNT_OF_BLOCK_TYPES];

    private static final byte[][] BLOCK_TYPE_XYZ_SUB_DATA = new byte[TOTAL_AMOUNT_OF_BLOCK_TYPES][0];
    private static final byte[][] BLOCK_TYPE_UV_SUB_DATA = new byte[TOTAL_AMOUNT_OF_BLOCK_TYPES][0];
}
