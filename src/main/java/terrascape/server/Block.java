package terrascape.server;

import terrascape.dataStorage.Chunk;
import terrascape.entity.Target;
import terrascape.player.SoundManager;
import terrascape.utils.Utils;
import org.joml.Vector3f;

import static terrascape.utils.Constants.*;

public class Block {

    public static final byte[][] NORMALS = {{0, 0, 1}, {0, 1, 0}, {1, 0, 0}, {0, 0, -1}, {0, -1, 0}, {-1, 0, 0}};
    public static final byte[][] CORNERS_OF_SIDE = {{1, 0, 5, 4}, {2, 0, 3, 1}, {3, 1, 7, 5}, {2, 3, 6, 7}, {6, 4, 7, 5}, {2, 0, 6, 4}};
    public static final byte[][] SIDES_WITH_CORNER = {{0, 1, 5}, {0, 1, 2}, {1, 3, 5}, {1, 2, 3}, {0, 4, 5}, {0, 2, 4}, {3, 4, 5}, {2, 3, 4}};

    public static int getTextureIndex(short block, int side) {
        byte[] blockTextureIndices;
        if ((block & 0xFFFF) < STANDARD_BLOCKS_THRESHOLD)
            blockTextureIndices = NON_STANDARD_BLOCK_TEXTURE_INDICES[block & 0xFFFF];
        else blockTextureIndices = STANDARD_BLOCK_TEXTURE_INDICES[(block & 0xFFFF) >> BLOCK_TYPE_BITS];
        return blockTextureIndices[side >= blockTextureIndices.length ? 0 : side];
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
            case UP_DOWN_BASALT -> {
                if (side == NORTH)
                    return (short) (NORTH_SOUTH_BASALT | toPlaceBlockType);
                if (side == TOP) return (short) (UP_DOWN_BASALT | toPlaceBlockType);
                return (short) (EAST_WEST_BASALT | toPlaceBlockType);
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
            case NORTH_WEST_DOOR_NORTH -> {
                if (target.inBlockPosition().x > 0.5f && target.inBlockPosition().z > 0.5f)
                    yield NORTH_WEST_DOOR_NORTH;
                if (target.inBlockPosition().x > 0.5f && target.inBlockPosition().z <= 0.5f)
                    yield SOUTH_WEST_DOOR_SOUTH;
                if (target.inBlockPosition().x <= 0.5f && target.inBlockPosition().z > 0.5f)
                    yield NORTH_EAST_DOOR_NORTH;
                yield SOUTH_EAST_DOOR_SOUTH;
            }
            default -> blockType;
        };
    }

    public static short getInInventoryBlockEquivalent(short block) {
        if (block == AIR || block == OUT_OF_WORLD) return AIR;
        if ((block & 0xFFFF) < STANDARD_BLOCKS_THRESHOLD) {
            if (block >= NORTH_CREATOR_HEAD && block <= EAST_CREATOR_HEAD) return NORTH_CREATOR_HEAD;
            return block;
        }
        int baseBlock = getInInventoryBaseBlock(block);

        return (short) (baseBlock | getInInventoryBlockType(block));
    }

    private static int getInInventoryBaseBlock(short block) {
        return switch (block & BASE_BLOCK_MASK) {
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
    }

    private static int getInInventoryBlockType(short block) {
        return switch (block & BLOCK_TYPE_MASK) {
            case FULL_BLOCK -> FULL_BLOCK;
            case TOP_PLAYER_HEAD, BOTTOM_PLAYER_HEAD, NORTH_PLAYER_HEAD, SOUTH_PLAYER_HEAD, WEST_PLAYER_HEAD,
                 EAST_PLAYER_HEAD -> BOTTOM_PLAYER_HEAD;
            case NORTH_SLAB, TOP_SLAB, WEST_SLAB, SOUTH_SLAB, BOTTOM_SLAB, EAST_SLAB -> BOTTOM_SLAB;
            case NORTH_PLATE, TOP_PLATE, WEST_PLATE, SOUTH_PLATE, BOTTOM_PLATE, EAST_PLATE -> BOTTOM_PLATE;

            case NORTH_SOCKET, TOP_SOCKET, WEST_SOCKET, SOUTH_SOCKET, BOTTOM_SOCKET, EAST_SOCKET -> BOTTOM_SOCKET;
            case NORTH_SOUTH_WALL, UP_DOWN_WALL, EAST_WEST_WALL -> NORTH_SOUTH_WALL;
            case UP_DOWN_POST, NORTH_SOUTH_POST, EAST_WEST_POST -> UP_DOWN_POST;
            case BOTTOM_NORTH_STAIR, BOTTOM_WEST_STAIR, BOTTOM_SOUTH_STAIR, BOTTOM_EAST_STAIR, TOP_NORTH_STAIR,
                 TOP_WEST_STAIR, TOP_SOUTH_STAIR, TOP_EAST_STAIR, NORTH_WEST_STAIR, NORTH_EAST_STAIR, SOUTH_WEST_STAIR,
                 SOUTH_EAST_STAIR -> BOTTOM_SOUTH_STAIR;
            case THIN_BOTTOM_NORTH_STAIR, THIN_BOTTOM_WEST_STAIR, THIN_BOTTOM_SOUTH_STAIR, THIN_BOTTOM_EAST_STAIR,
                 THIN_TOP_NORTH_STAIR, THIN_TOP_WEST_STAIR, THIN_TOP_SOUTH_STAIR, THIN_TOP_EAST_STAIR,
                 THIN_NORTH_WEST_STAIR, THIN_NORTH_EAST_STAIR, THIN_SOUTH_WEST_STAIR, THIN_SOUTH_EAST_STAIR ->
                    THIN_BOTTOM_SOUTH_STAIR;
            case THICK_BOTTOM_NORTH_STAIR, THICK_BOTTOM_WEST_STAIR, THICK_BOTTOM_SOUTH_STAIR, THICK_BOTTOM_EAST_STAIR,
                 THICK_TOP_NORTH_STAIR, THICK_TOP_WEST_STAIR, THICK_TOP_SOUTH_STAIR, THICK_TOP_EAST_STAIR,
                 THICK_NORTH_WEST_STAIR, THICK_NORTH_EAST_STAIR, THICK_SOUTH_WEST_STAIR, THICK_SOUTH_EAST_STAIR ->
                    THICK_BOTTOM_SOUTH_STAIR;
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
                 EAST_WEST_FENCE_NORTH_UP_SOUTH_DOWN -> UP_DOWN_FENCE_NORTH_WEST;
            case NORTH_WEST_DOOR_NORTH, NORTH_WEST_DOOR_WEST, NORTH_EAST_DOOR_NORTH, NORTH_EAST_DOOR_EAST,
                 SOUTH_WEST_DOOR_SOUTH, SOUTH_WEST_DOOR_WEST, SOUTH_EAST_DOOR_SOUTH, SOUTH_EAST_DOOR_EAST ->
                    NORTH_WEST_DOOR_NORTH;
            case CARPET -> CARPET;
            default -> 0; // Unreachable
        };
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

    public static byte getBlockTypeData(short block) {
        return BLOCK_TYPE_DATA[getBlockType(block)];
    }

    public static boolean hasAmbientOcclusion(short block, short referenceBlock) {
        if (block == AIR) return false;
        if (isLeaveType(block)) return false;
        if (isGlassType(block)) return false;
        int blockType = getBlockType(block);
        if (isLiquidType(blockType) || blockType == FLOWER_TYPE) return false;
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

    public static long getBlockOcclusionData(short block, int side) {
        return BLOCK_TYPE_OCCLUSION_DATA[getBlockType(block)][side];
    }

    public static long getBlockTypeOcclusionData(int blockType, int side) {
        return BLOCK_TYPE_OCCLUSION_DATA[blockType][side];
    }

    public static byte[] getXYZSubData(short block) {
        return BLOCK_TYPE_XYZ_SUB_DATA[getBlockType(block)];
    }

    public static int getBlockType(short block) {
        if ((block & 0xFFFF) < STANDARD_BLOCKS_THRESHOLD) return NON_STANDARD_BLOCK_TYPE[block & 0xFFFF];
        return block & BLOCK_TYPE_MASK;
    }

    public static boolean isWaterLogged(short block) {
        return isWaterBlock(block) || (block & 0xFFFF) > STANDARD_BLOCKS_THRESHOLD && (block & WATER_LOGGED_MASK) != 0;
    }

    public static boolean isWaterBlock(short block) {
        return block >= WATER_SOURCE && block <= FLOWING_WATER_LEVEL_1;
    }

    public static int getWaterLevel(int x, int y, int z) {
        short block = Chunk.getBlockInWorld(x, y, z);
        if (isWaterLogged(block) && !isWaterBlock(block)) {
            short blockAbove = Chunk.getBlockInWorld(x, y + 1, z);
            return Block.isWaterLogged(blockAbove) ? 16 : 14;
        }
        return switch (block) {
            case FLOWING_WATER_LEVEL_8 -> 16;
            case WATER_SOURCE, FLOWING_WATER_LEVEL_7 -> 14;
            case FLOWING_WATER_LEVEL_6 -> 12;
            case FLOWING_WATER_LEVEL_5 -> 10;
            case FLOWING_WATER_LEVEL_4 -> 8;
            case FLOWING_WATER_LEVEL_3 -> 6;
            case FLOWING_WATER_LEVEL_2 -> 4;
            case FLOWING_WATER_LEVEL_1 -> 2;
            default -> 0;
        };
    }

    public static boolean canWaterFlow(short sourceBlock, short targetBlock, short blockBelow, int entrySide) {
        if (isLavaBlock(targetBlock)) return false;
        if (sourceBlock == FLOWING_WATER_LEVEL_1 && entrySide != TOP) return false;
        if (blockBelow == FLOWING_WATER_LEVEL_8 || blockBelow == WATER_SOURCE || Block.getBlockOcclusionData(blockBelow, TOP) == 0L && entrySide != TOP)
            return false;
        if (isWaterBlock(targetBlock))
            return targetBlock > sourceBlock + 1 || entrySide == TOP && targetBlock != WATER_SOURCE && targetBlock != FLOWING_WATER_LEVEL_8;
        if (entrySide == TOP && (Block.getBlockProperties(blockBelow) & REPLACEABLE) != 0)
            return true;

        return (getBlockProperties(targetBlock) & REPLACEABLE) != 0;
    }

    public static boolean isWaterSupported(short block, int x, int y, int z) {
        if (block == WATER_SOURCE || !isWaterBlock(block)) return true;
        short blockAbove = Chunk.getBlockInWorld(x, y + 1, z);
        if (isWaterLogged(blockAbove) && (isWaterBlock(blockAbove) || Block.getBlockOcclusionData(blockAbove, BOTTOM) != -1L))
            return true;

        short adjacentBlock = Chunk.getBlockInWorld(x + 1, y, z);
        if (!isWaterBlock(adjacentBlock) && isWaterLogged(adjacentBlock) && Block.getBlockOcclusionData(adjacentBlock, EAST) != -1L
                || isWaterBlock(adjacentBlock) && adjacentBlock < block)
            return true;

        adjacentBlock = Chunk.getBlockInWorld(x - 1, y, z);
        if (!isWaterBlock(adjacentBlock) && isWaterLogged(adjacentBlock) && Block.getBlockOcclusionData(adjacentBlock, WEST) != -1L
                || isWaterBlock(adjacentBlock) && adjacentBlock < block)
            return true;

        adjacentBlock = Chunk.getBlockInWorld(x, y, z + 1);
        if (!isWaterBlock(adjacentBlock) && isWaterLogged(adjacentBlock) && Block.getBlockOcclusionData(adjacentBlock, SOUTH) != -1L
                || isWaterBlock(adjacentBlock) && adjacentBlock < block)
            return true;

        adjacentBlock = Chunk.getBlockInWorld(x, y, z - 1);
        return !isWaterBlock(adjacentBlock) && isWaterLogged(adjacentBlock) && Block.getBlockOcclusionData(adjacentBlock, NORTH) != -1L
                || isWaterBlock(adjacentBlock) && adjacentBlock < block;
    }

    public static boolean isWaterSource(short block) {
        return block == WATER_SOURCE || isWaterLogged(block) && !isWaterBlock(block);
    }

    public static boolean isLavaBlock(short block) {
        return block >= LAVA_SOURCE && block <= FLOWING_LAVA_LEVEL_1;
    }

    public static int getLavaLevel(int x, int y, int z) {
        short block = Chunk.getBlockInWorld(x, y, z);
        return switch (block) {
            case FLOWING_LAVA_LEVEL_4 -> 16;
            case LAVA_SOURCE -> 14;
            case FLOWING_LAVA_LEVEL_3 -> 10;
            case FLOWING_LAVA_LEVEL_2 -> 6;
            case FLOWING_LAVA_LEVEL_1 -> 2;
            default -> 0;
        };
    }

    public static boolean canLavaFlow(short sourceBlock, short targetBlock, short blockBelow, int entrySide) {
        if (isWaterBlock(targetBlock)) return false;
        if (sourceBlock == FLOWING_LAVA_LEVEL_1 && entrySide != TOP) return false;
        if (blockBelow == FLOWING_LAVA_LEVEL_4 || blockBelow == LAVA_SOURCE || Block.getBlockOcclusionData(blockBelow, TOP) == 0L && entrySide != TOP)
            return false;
        if (isLavaBlock(targetBlock))
            return targetBlock > sourceBlock + 1 || entrySide == TOP && targetBlock != LAVA_SOURCE && targetBlock != FLOWING_LAVA_LEVEL_4;
        if (entrySide == TOP && (Block.getBlockProperties(blockBelow) & REPLACEABLE) != 0)
            return true;

        return (getBlockProperties(targetBlock) & REPLACEABLE) != 0;
    }

    public static boolean isLavaSupported(short block, int x, int y, int z) {
        if (block == LAVA_SOURCE) return true;
        short blockAbove = Chunk.getBlockInWorld(x, y + 1, z);
        if (isLavaBlock(blockAbove)) return true;

        short adjacentBlock = Chunk.getBlockInWorld(x + 1, y, z);
        if (isLavaBlock(adjacentBlock) && adjacentBlock < block) return true;

        adjacentBlock = Chunk.getBlockInWorld(x - 1, y, z);
        if (isLavaBlock(adjacentBlock) && adjacentBlock < block) return true;

        adjacentBlock = Chunk.getBlockInWorld(x, y, z + 1);
        if (isLavaBlock(adjacentBlock) && adjacentBlock < block) return true;

        adjacentBlock = Chunk.getBlockInWorld(x, y, z - 1);
        return isLavaBlock(adjacentBlock) && adjacentBlock < block;
    }

    public static boolean isLiquidType(int blockType) {
        return blockType >= LIQUID_TYPE && blockType <= LIQUID_LEVEL_8;
    }

    public static boolean isLeaveType(short block) {
        int baseBlock = block & BASE_BLOCK_MASK;
        return baseBlock >= OAK_LEAVES && baseBlock <= BLACK_WOOD_LEAVES || getBlockType(block) == VINE_TYPE;
    }

    public static boolean isGlassType(short block) {
        int baseBlock = block & BASE_BLOCK_MASK;
        return baseBlock == GLASS;
    }

    public static boolean isUpDownFenceType(int blockType) {
        return blockType >= UP_DOWN_FENCE && blockType <= UP_DOWN_FENCE_NORTH_WEST_SOUTH_EAST;
    }

    public static boolean isNorthSouthFenceType(int blockType) {
        return blockType >= NORTH_SOUTH_FENCE && blockType <= NORTH_SOUTH_FENCE_UP_WEST_DOWN_EAST;
    }

    public static boolean isEastWestFenceType(int blockType) {
        return blockType >= EAST_WEST_FENCE && blockType <= EAST_WEST_FENCE_NORTH_UP_SOUTH_DOWN;
    }

    public static int getFaceCount(int blockType) {
        return BLOCK_TYPE_XYZ_SUB_DATA[blockType].length;
    }

    public static int sideToMinMaxXYZ(int side) {
        return switch (side) {
            case NORTH -> MAX_Z;
            case SOUTH -> MIN_Z;
            case WEST -> MAX_X;
            case EAST -> MIN_X;
            case TOP -> MAX_Y;
            case BOTTOM -> MIN_Y;
            default -> -1;
        };
    }

    public static int minMaxXYZToSide(int minMaxXYZ) {
        return switch (minMaxXYZ) {
            case MAX_Z -> NORTH;
            case MIN_Z -> SOUTH;
            case MAX_X -> WEST;
            case MIN_X -> EAST;
            case MAX_Y -> TOP;
            case MIN_Y -> BOTTOM;
            default -> -1;
        };
    }

    public static boolean isSupported(short block, int x, int y, int z) {
        int properties = getBlockProperties(block);
        if ((properties & REQUIRES_BOTTOM_SUPPORT) != 0) {
            short blochBelow = Chunk.getBlockInWorld(x, y - 1, z);
            if (blochBelow == block) return true;
            return getBlockOcclusionData(blochBelow, TOP) == -1L;
        } else if ((properties & REQUIRES_AND_SIDE_SUPPORT) != 0) {
            if (getBlockOcclusionData(Chunk.getBlockInWorld(x, y, z - 1), NORTH) == -1L) return true;
            if (getBlockOcclusionData(Chunk.getBlockInWorld(x, y, z + 1), SOUTH) == -1L) return true;
            if (getBlockOcclusionData(Chunk.getBlockInWorld(x, y - 1, z), TOP) == -1L) return true;
            if (getBlockOcclusionData(Chunk.getBlockInWorld(x, y + 1, z), BOTTOM) == -1L) return true;
            if (getBlockOcclusionData(Chunk.getBlockInWorld(x - 1, y, z), WEST) == -1L) return true;
            return getBlockOcclusionData(Chunk.getBlockInWorld(x + 1, y, z), EAST) == -1L;
        }
        return true;
    }

    public static boolean isInteractable(short block) {
        return (getBlockProperties(block) & INTERACTABLE) != 0 || (getBlockTypeData(block) & INTERACTABLE) != 0;
    }

    public static boolean isDoorType(short block) {
        return (block & 0xFFFF) > STANDARD_BLOCKS_THRESHOLD && (block & BLOCK_TYPE_MASK) >= NORTH_WEST_DOOR_NORTH && (block & BLOCK_TYPE_MASK) <= SOUTH_EAST_DOOR_EAST;
    }

    public static int getOpenClosedDoorType(int doorType) {
        return switch (doorType) {
            case NORTH_WEST_DOOR_NORTH -> NORTH_WEST_DOOR_WEST;
            case NORTH_WEST_DOOR_WEST -> NORTH_WEST_DOOR_NORTH;
            case NORTH_EAST_DOOR_NORTH -> NORTH_EAST_DOOR_EAST;
            case NORTH_EAST_DOOR_EAST -> NORTH_EAST_DOOR_NORTH;
            case SOUTH_WEST_DOOR_SOUTH -> SOUTH_WEST_DOOR_WEST;
            case SOUTH_WEST_DOOR_WEST -> SOUTH_WEST_DOOR_SOUTH;
            case SOUTH_EAST_DOOR_SOUTH -> SOUTH_EAST_DOOR_EAST;
            case SOUTH_EAST_DOOR_EAST -> SOUTH_EAST_DOOR_SOUTH;
            default -> -1;
        };
    }

    public static void setBlockTypeName(int index, String name) {
        BLOCK_TYPE_NAMES[index] = name;
    }

    public static void setNonStandardBlockName(int index, String name) {
        NON_STANDARD_BLOCK_NAMES[index] = name;
    }

    public static void setStandardBlockName(int index, String name) {
        STANDARD_BLOCK_NAMES[index] = name;
    }

    public static void setFullBlockTypeName(int index, String name) {
        FULL_BLOCK_TYPE_NAMES[index] = name;
    }

    public static String getBlockName(short block) {
        block = getInInventoryBlockEquivalent(block);
        if ((block & 0xFFFF) < STANDARD_BLOCKS_THRESHOLD) return NON_STANDARD_BLOCK_NAMES[block];

        int blockTypeIndex = 0, targetBlockType = getInInventoryBlockType(block);
        for (; blockTypeIndex < TO_PLACE_BLOCK_TYPES.length; blockTypeIndex++)
            if (TO_PLACE_BLOCK_TYPES[blockTypeIndex] == targetBlockType) break;

        String name = STANDARD_BLOCK_NAMES[((block & 0xFFFF) >> BLOCK_TYPE_BITS) - 1];
        if (getBlockType(block) != FULL_BLOCK) name += " " + BLOCK_TYPE_NAMES[blockTypeIndex];
        return name;
    }

    public static String getFullBlockName(short block) {
        if ((block & 0xFFFF) < STANDARD_BLOCKS_THRESHOLD) return NON_STANDARD_BLOCK_NAMES[block];
        return STANDARD_BLOCK_NAMES[((block & 0xFFFF) >> BLOCK_TYPE_BITS) - 1] + " " + FULL_BLOCK_TYPE_NAMES[block & BLOCK_TYPE_MASK];
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

        BLOCK_TYPE_DATA[LIQUID_TYPE] = DYNAMIC_SHAPE_MASK;
        BLOCK_TYPE_DATA[LIQUID_LEVEL_8] = DYNAMIC_SHAPE_MASK;
        BLOCK_TYPE_DATA[LIQUID_LEVEL_7] = DYNAMIC_SHAPE_MASK;
        BLOCK_TYPE_DATA[LIQUID_LEVEL_6] = DYNAMIC_SHAPE_MASK;
        BLOCK_TYPE_DATA[LIQUID_LEVEL_5] = DYNAMIC_SHAPE_MASK;
        BLOCK_TYPE_DATA[LIQUID_LEVEL_4] = DYNAMIC_SHAPE_MASK;
        BLOCK_TYPE_DATA[LIQUID_LEVEL_3] = DYNAMIC_SHAPE_MASK;
        BLOCK_TYPE_DATA[LIQUID_LEVEL_2] = DYNAMIC_SHAPE_MASK;
        BLOCK_TYPE_DATA[LIQUID_LEVEL_1] = DYNAMIC_SHAPE_MASK;
        BLOCK_TYPE_DATA[CACTUS_TYPE] = DYNAMIC_SHAPE_MASK;
        BLOCK_TYPE_DATA[NORTH_WEST_DOOR_NORTH] = INTERACTABLE;
        BLOCK_TYPE_DATA[NORTH_WEST_DOOR_WEST] = INTERACTABLE;
        BLOCK_TYPE_DATA[NORTH_EAST_DOOR_NORTH] = INTERACTABLE;
        BLOCK_TYPE_DATA[NORTH_EAST_DOOR_EAST] = INTERACTABLE;
        BLOCK_TYPE_DATA[SOUTH_WEST_DOOR_SOUTH] = INTERACTABLE;
        BLOCK_TYPE_DATA[SOUTH_WEST_DOOR_WEST] = INTERACTABLE;
        BLOCK_TYPE_DATA[SOUTH_EAST_DOOR_SOUTH] = INTERACTABLE;
        BLOCK_TYPE_DATA[SOUTH_EAST_DOOR_EAST] = INTERACTABLE;

        for (int upDownFenceType = UP_DOWN_FENCE; upDownFenceType <= UP_DOWN_FENCE_NORTH_WEST_SOUTH_EAST; upDownFenceType++)
            BLOCK_TYPE_DATA[upDownFenceType] = SMART_BLOCK_TYPE;
        for (int EASTWESTFenceType = EAST_WEST_FENCE; EASTWESTFenceType <= EAST_WEST_FENCE_NORTH_UP_SOUTH_DOWN; EASTWESTFenceType++)
            BLOCK_TYPE_DATA[EASTWESTFenceType] = SMART_BLOCK_TYPE;
        for (int NORTHSOUTHFenceType = NORTH_SOUTH_FENCE; NORTHSOUTHFenceType <= NORTH_SOUTH_FENCE_UP_WEST_DOWN_EAST; NORTHSOUTHFenceType++)
            BLOCK_TYPE_DATA[NORTHSOUTHFenceType] = SMART_BLOCK_TYPE;

        for (int blockType = 0; blockType < TOTAL_AMOUNT_OF_BLOCK_TYPES; blockType++) {
            byte[] XYZSubData = BLOCK_TYPE_XYZ_SUB_DATA[blockType];
            long[] occlusionData = new long[6];

            for (int aabbIndex = 0; aabbIndex < XYZSubData.length; aabbIndex += 6) {
                if (XYZSubData[MIN_X + aabbIndex] == 0)
                    occlusionData[EAST] |= fillOcclusionBits(XYZSubData[MIN_Z + aabbIndex], XYZSubData[MAX_Z + aabbIndex], XYZSubData[MIN_Y + aabbIndex], XYZSubData[MAX_Y + aabbIndex]);
                if (XYZSubData[MAX_X + aabbIndex] == 0)
                    occlusionData[WEST] |= fillOcclusionBits(XYZSubData[MIN_Z + aabbIndex], XYZSubData[MAX_Z + aabbIndex], XYZSubData[MIN_Y + aabbIndex], XYZSubData[MAX_Y + aabbIndex]);

                if (XYZSubData[MIN_Y + aabbIndex] == 0)
                    occlusionData[BOTTOM] |= fillOcclusionBits(XYZSubData[MIN_Z + aabbIndex], XYZSubData[MAX_Z + aabbIndex], XYZSubData[MIN_X + aabbIndex], XYZSubData[MAX_X + aabbIndex]);
                if (XYZSubData[MAX_Y + aabbIndex] == 0)
                    occlusionData[TOP] |= fillOcclusionBits(XYZSubData[MIN_Z + aabbIndex], XYZSubData[MAX_Z + aabbIndex], XYZSubData[MIN_X + aabbIndex], XYZSubData[MAX_X + aabbIndex]);

                if (XYZSubData[MIN_Z + aabbIndex] == 0)
                    occlusionData[SOUTH] |= fillOcclusionBits(XYZSubData[MIN_X + aabbIndex], XYZSubData[MAX_X + aabbIndex], XYZSubData[MIN_Y + aabbIndex], XYZSubData[MAX_Y + aabbIndex]);
                if (XYZSubData[MAX_Z + aabbIndex] == 0)
                    occlusionData[NORTH] |= fillOcclusionBits(XYZSubData[MIN_X + aabbIndex], XYZSubData[MAX_X + aabbIndex], XYZSubData[MIN_Y + aabbIndex], XYZSubData[MAX_Y + aabbIndex]);
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
        BLOCK_TYPE_XYZ_SUB_DATA[PATH_TYPE] = new byte[]{0, 0, 0, -1, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[FULL_BLOCK] = new byte[]{0, 0, 0, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[LIQUID_TYPE] = new byte[]{0, 0, 0, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[VINE_TYPE] = new byte[]{1, -1, 1, -1, 1, -1};
        BLOCK_TYPE_XYZ_SUB_DATA[CARPET] = new byte[]{0, 0, 0, -15, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[TOP_PLAYER_HEAD] = new byte[]{4, -4, 8, 0, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[BOTTOM_PLAYER_HEAD] = new byte[]{4, -4, 0, -8, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_PLAYER_HEAD] = new byte[]{4, -4, 4, -4, 8, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[SOUTH_PLAYER_HEAD] = new byte[]{4, -4, 4, -4, 0, -8};
        BLOCK_TYPE_XYZ_SUB_DATA[WEST_PLAYER_HEAD] = new byte[]{8, 0, 4, -4, 4, -4};
        BLOCK_TYPE_XYZ_SUB_DATA[EAST_PLAYER_HEAD] = new byte[]{0, -8, 4, -4, 4, -4};

        BLOCK_TYPE_XYZ_SUB_DATA[LIQUID_LEVEL_1] = new byte[]{0, 0, 0, -14, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[LIQUID_LEVEL_2] = new byte[]{0, 0, 0, -12, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[LIQUID_LEVEL_3] = new byte[]{0, 0, 0, -10, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[LIQUID_LEVEL_4] = new byte[]{0, 0, 0, -8, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[LIQUID_LEVEL_5] = new byte[]{0, 0, 0, -6, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[LIQUID_LEVEL_6] = new byte[]{0, 0, 0, -4, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[LIQUID_LEVEL_7] = new byte[]{0, 0, 0, -2, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[LIQUID_LEVEL_8] = new byte[]{0, 0, 0, 0, 0, 0};

        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_WEST_DOOR_NORTH] = new byte[]{0, 0, 0, 0, 12, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_WEST_DOOR_WEST] = new byte[]{12, 0, 0, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_EAST_DOOR_NORTH] = new byte[]{0, 0, 0, 0, 12, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[NORTH_EAST_DOOR_EAST] = new byte[]{0, -12, 0, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[SOUTH_WEST_DOOR_SOUTH] = new byte[]{0, 0, 0, 0, 0, -12};
        BLOCK_TYPE_XYZ_SUB_DATA[SOUTH_WEST_DOOR_WEST] = new byte[]{12, 0, 0, 0, 0, 0};
        BLOCK_TYPE_XYZ_SUB_DATA[SOUTH_EAST_DOOR_SOUTH] = new byte[]{0, 0, 0, 0, 0, -12};
        BLOCK_TYPE_XYZ_SUB_DATA[SOUTH_EAST_DOOR_EAST] = new byte[]{0, -12, 0, 0, 0, 0};

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

    private static void setDirectionalBlock(short upDownBlock, short northSouthBlock, short eastWestBlock, byte topTexture, byte sideTexture, int[] stepSounds, int[] digSounds) {
        STANDARD_BLOCK_TEXTURE_INDICES[(upDownBlock & 0xFFFF) >> BLOCK_TYPE_BITS] = new byte[]{sideTexture, topTexture, sideTexture, sideTexture, topTexture, sideTexture};
        STANDARD_BLOCK_TEXTURE_INDICES[(northSouthBlock & 0xFFFF) >> BLOCK_TYPE_BITS] = new byte[]{topTexture, sideTexture, sideTexture, topTexture, sideTexture, sideTexture};
        STANDARD_BLOCK_TEXTURE_INDICES[(eastWestBlock & 0xFFFF) >> BLOCK_TYPE_BITS] = new byte[]{sideTexture, sideTexture, topTexture, sideTexture, sideTexture, topTexture};

        STANDARD_BLOCK_DIG_SOUNDS[(upDownBlock & 0xFFFF) >> BLOCK_TYPE_BITS] = digSounds;
        STANDARD_BLOCK_DIG_SOUNDS[(northSouthBlock & 0xFFFF) >> BLOCK_TYPE_BITS] = digSounds;
        STANDARD_BLOCK_DIG_SOUNDS[(eastWestBlock & 0xFFFF) >> BLOCK_TYPE_BITS] = digSounds;

        STANDARD_BLOCK_STEP_SOUNDS[(upDownBlock & 0xFFFF) >> BLOCK_TYPE_BITS] = stepSounds;
        STANDARD_BLOCK_STEP_SOUNDS[(northSouthBlock & 0xFFFF) >> BLOCK_TYPE_BITS] = stepSounds;
        STANDARD_BLOCK_STEP_SOUNDS[(eastWestBlock & 0xFFFF) >> BLOCK_TYPE_BITS] = stepSounds;

        STANDARD_BLOCK_PROPERTIES[(upDownBlock & 0xFFFF) >> BLOCK_TYPE_BITS] = 0;
        STANDARD_BLOCK_PROPERTIES[(northSouthBlock & 0xFFFF) >> BLOCK_TYPE_BITS] = ROTATE_TOP_TEXTURE | ROTATE_WEST_TEXTURE | ROTATE_EAST_TEXTURE;
        STANDARD_BLOCK_PROPERTIES[(eastWestBlock & 0xFFFF) >> BLOCK_TYPE_BITS] = ROTATE_NORTH_TEXTURE | ROTATE_SOUTH_TEXTURE | ROTATE_BOTTOM_TEXTURE;
    }

    private static void initNonStandardBlocks() {
        SoundManager sound = Launcher.getSound();

        setNonStandardBlockData(AIR, NO_COLLISION | REPLACEABLE, null, null, AIR_TYPE, null);
        setNonStandardBlockData(OUT_OF_WORLD, 0, null, null, FULL_BLOCK, null);
        setNonStandardBlockData(NORTH_CREATOR_HEAD, 0, sound.digWood, sound.stepWood, BOTTOM_PLAYER_HEAD, new byte[]{(byte) -124, (byte) -109, (byte) -123, (byte) -108, (byte) -107, (byte) -125});
        setNonStandardBlockData(WEST_CREATOR_HEAD, 0, sound.digWood, sound.stepWood, BOTTOM_PLAYER_HEAD, new byte[]{(byte) -125, (byte) -109, (byte) -124, (byte) -123, (byte) -91, (byte) -108});
        setNonStandardBlockData(SOUTH_CREATOR_HEAD, 0, sound.digWood, sound.stepWood, BOTTOM_PLAYER_HEAD, new byte[]{(byte) -108, (byte) -109, (byte) -125, (byte) -124, (byte) -107, (byte) -123});
        setNonStandardBlockData(EAST_CREATOR_HEAD, 0, sound.digWood, sound.stepWood, BOTTOM_PLAYER_HEAD, new byte[]{(byte) -123, (byte) -109, (byte) -108, (byte) -125, (byte) -91, (byte) -124});
        setNonStandardBlockData(TORCH, NO_COLLISION | LIGHT_LEVEL_15 | REQUIRES_BOTTOM_SUPPORT, sound.digWood, sound.stepWood, TORCH_TYPE, new byte[]{(byte) -80});
        setNonStandardBlockData(TALL_GRASS, NO_COLLISION | REPLACEABLE | REQUIRES_BOTTOM_SUPPORT, sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) -64});
        setNonStandardBlockData(RED_TULIP, NO_COLLISION | REPLACEABLE | REQUIRES_BOTTOM_SUPPORT, sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) -63});
        setNonStandardBlockData(YELLOW_TULIP, NO_COLLISION | REPLACEABLE | REQUIRES_BOTTOM_SUPPORT, sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) -62});
        setNonStandardBlockData(ORANGE_TULIP, NO_COLLISION | REPLACEABLE | REQUIRES_BOTTOM_SUPPORT, sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) -61});
        setNonStandardBlockData(MAGENTA_TULIP, NO_COLLISION | REPLACEABLE | REQUIRES_BOTTOM_SUPPORT, sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) -60});
        setNonStandardBlockData(ROSE, NO_COLLISION | REPLACEABLE | REQUIRES_BOTTOM_SUPPORT, sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) -59});
        setNonStandardBlockData(HYACINTH, NO_COLLISION | REPLACEABLE | REQUIRES_BOTTOM_SUPPORT, sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) -58});
        setNonStandardBlockData(DRISLY, NO_COLLISION | REPLACEABLE | REQUIRES_BOTTOM_SUPPORT, sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) 0xD0});
        setNonStandardBlockData(SHRUB, NO_COLLISION | REPLACEABLE | REQUIRES_BOTTOM_SUPPORT, sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) 0xD1});
        setNonStandardBlockData(SUGAR_CANE, NO_COLLISION | REQUIRES_BOTTOM_SUPPORT, sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) 0xD2});
        setNonStandardBlockData(PATH_BLOCK, 0, sound.digGrass, sound.stepGrass, PATH_TYPE, new byte[]{(byte) -72, (byte) -88, (byte) -72, (byte) -72, (byte) 1, (byte) -72});
        setNonStandardBlockData(BLACK_ROSE, NO_COLLISION | REPLACEABLE | REQUIRES_BOTTOM_SUPPORT, sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) 0xD3});
        setNonStandardBlockData(FLIELEN, NO_COLLISION | REPLACEABLE | REQUIRES_BOTTOM_SUPPORT, sound.digFoliage, sound.stepFoliage, FLOWER_TYPE, new byte[]{(byte) 0xD4});
        setNonStandardBlockData(CACTUS, REQUIRES_BOTTOM_SUPPORT, sound.digWood, sound.stepWood, CACTUS_TYPE, new byte[]{(byte) 113, (byte) -92, (byte) 113, (byte) 113, (byte) -92, (byte) 113});
        setNonStandardBlockData(VINES, REQUIRES_AND_SIDE_SUPPORT | REPLACEABLE | NO_COLLISION, sound.digFoliage, sound.stepFoliage, VINE_TYPE, new byte[]{(byte) 0xB5});
        setNonStandardBlockData(GLOW_LICHEN, REQUIRES_AND_SIDE_SUPPORT | REPLACEABLE | NO_COLLISION | LIGHT_LEVEL_7, sound.digFoliage, sound.stepFoliage, VINE_TYPE, new byte[]{(byte) 0xB7});

        setNonStandardBlockData(WATER_SOURCE, NO_COLLISION | REPLACEABLE | BLAST_RESISTANT, sound.splash, sound.splash, LIQUID_TYPE, new byte[]{(byte) 64});
        setNonStandardBlockData(FLOWING_WATER_LEVEL_1, NO_COLLISION | REPLACEABLE | BLAST_RESISTANT, sound.splash, sound.splash, LIQUID_LEVEL_1, new byte[]{(byte) 64});
        setNonStandardBlockData(FLOWING_WATER_LEVEL_2, NO_COLLISION | REPLACEABLE | BLAST_RESISTANT, sound.splash, sound.splash, LIQUID_LEVEL_2, new byte[]{(byte) 64});
        setNonStandardBlockData(FLOWING_WATER_LEVEL_3, NO_COLLISION | REPLACEABLE | BLAST_RESISTANT, sound.splash, sound.splash, LIQUID_LEVEL_3, new byte[]{(byte) 64});
        setNonStandardBlockData(FLOWING_WATER_LEVEL_4, NO_COLLISION | REPLACEABLE | BLAST_RESISTANT, sound.splash, sound.splash, LIQUID_LEVEL_4, new byte[]{(byte) 64});
        setNonStandardBlockData(FLOWING_WATER_LEVEL_5, NO_COLLISION | REPLACEABLE | BLAST_RESISTANT, sound.splash, sound.splash, LIQUID_LEVEL_5, new byte[]{(byte) 64});
        setNonStandardBlockData(FLOWING_WATER_LEVEL_6, NO_COLLISION | REPLACEABLE | BLAST_RESISTANT, sound.splash, sound.splash, LIQUID_LEVEL_6, new byte[]{(byte) 64});
        setNonStandardBlockData(FLOWING_WATER_LEVEL_7, NO_COLLISION | REPLACEABLE | BLAST_RESISTANT, sound.splash, sound.splash, LIQUID_LEVEL_7, new byte[]{(byte) 64});
        setNonStandardBlockData(FLOWING_WATER_LEVEL_8, NO_COLLISION | REPLACEABLE | BLAST_RESISTANT, sound.splash, sound.splash, LIQUID_LEVEL_8, new byte[]{(byte) 64});
        setNonStandardBlockData(LAVA_SOURCE, NO_COLLISION | REPLACEABLE | BLAST_RESISTANT | LIGHT_LEVEL_15, sound.lavaPop, sound.lavaPop, LIQUID_TYPE, new byte[]{(byte) -127});
        setNonStandardBlockData(FLOWING_LAVA_LEVEL_1, NO_COLLISION | REPLACEABLE | BLAST_RESISTANT | LIGHT_LEVEL_15, sound.lavaPop, sound.lavaPop, LIQUID_LEVEL_1, new byte[]{(byte) -127});
        setNonStandardBlockData(FLOWING_LAVA_LEVEL_2, NO_COLLISION | REPLACEABLE | BLAST_RESISTANT | LIGHT_LEVEL_15, sound.lavaPop, sound.lavaPop, LIQUID_LEVEL_3, new byte[]{(byte) -127});
        setNonStandardBlockData(FLOWING_LAVA_LEVEL_3, NO_COLLISION | REPLACEABLE | BLAST_RESISTANT | LIGHT_LEVEL_15, sound.lavaPop, sound.lavaPop, LIQUID_LEVEL_5, new byte[]{(byte) -127});
        setNonStandardBlockData(FLOWING_LAVA_LEVEL_4, NO_COLLISION | REPLACEABLE | BLAST_RESISTANT | LIGHT_LEVEL_15, sound.lavaPop, sound.lavaPop, LIQUID_LEVEL_8, new byte[]{(byte) -127});
    }

    private static void initStandardBlocks() {
        SoundManager sound = Launcher.getSound();

        setStandardBlockData(GRASS, 0, sound.digGrass, sound.stepGrass, new byte[]{(byte) 0xBA, (byte) 0xAA, (byte) 0xBA, (byte) 0xBA, (byte) 1, (byte) 0xBA});
        setStandardBlockData(DIRT, 0, sound.digGrass, sound.stepDirt, (byte) 1);
        setStandardBlockData(STONE, 0, sound.digStone, sound.stepStone, (byte) 2);
        setStandardBlockData(STONE_BRICKS, 0, sound.digStone, sound.stepStone, (byte) 34);
        setStandardBlockData(COBBLESTONE, 0, sound.digStone, sound.stepStone, (byte) 50);
        setStandardBlockData(CHISELED_STONE, 0, sound.digStone, sound.stepStone, (byte) 81);
        setStandardBlockData(POLISHED_STONE, 0, sound.digStone, sound.stepStone, (byte) 66);
        setStandardBlockData(CHISELED_POLISHED_STONE, 0, sound.digStone, sound.stepStone, (byte) 82);
        setStandardBlockData(MUD, 0, sound.digGrass, sound.stepDirt, (byte) 17);
        setStandardBlockData(ANDESITE, 0, sound.digStone, sound.stepStone, (byte) 18);
        setStandardBlockData(SNOW, 0, sound.digSnow, sound.stepSnow, (byte) 32);
        setStandardBlockData(SAND, HAS_GRAVITY, sound.digSand, sound.stepSand, (byte) 33);
        setStandardBlockData(SANDSTONE, 0, sound.digStone, sound.stepStone, (byte) -95);
        setStandardBlockData(POLISHED_SANDSTONE, 0, sound.digStone, sound.stepStone, (byte) -94);
        setStandardBlockData(SLATE, 0, sound.digStone, sound.stepStone, (byte) 48);
        setStandardBlockData(CHISELED_SLATE, 0, sound.digStone, sound.stepStone, (byte) -128);
        setStandardBlockData(COBBLED_SLATE, 0, sound.digStone, sound.stepStone, (byte) 114);
        setStandardBlockData(SLATE_BRICKS, 0, sound.digStone, sound.stepStone, (byte) -126);
        setStandardBlockData(POLISHED_SLATE, 0, sound.digStone, sound.stepStone, (byte) -96);
        setStandardBlockData(GLASS, 0, sound.digGlass, sound.stepGlass, (byte) 49);
        setStandardBlockData(GRAVEL, HAS_GRAVITY, sound.digGravel, sound.stepGravel, (byte) 65);
        setStandardBlockData(COURSE_DIRT, 0, sound.digGrass, sound.stepDirt, (byte) 80);
        setStandardBlockData(CLAY, 0, sound.digGrass, sound.stepDirt, (byte) 97);
        setStandardBlockData(MOSS, 0, sound.digFoliage, sound.stepFoliage, (byte) 98);
        setStandardBlockData(ICE, 0, sound.digIce, sound.stepGlass, (byte) 96);
        setStandardBlockData(HEAVY_ICE, 0, sound.digIce, sound.stepGlass, (byte) 112);
        setStandardBlockData(COAL_ORE, 0, sound.digStone, sound.stepStone, (byte) -112);
        setStandardBlockData(IRON_ORE, 0, sound.digStone, sound.stepStone, (byte) -111);
        setStandardBlockData(DIAMOND_ORE, 0, sound.digStone, sound.stepStone, (byte) -110);

        setDirectionalBlock(UP_DOWN_OAK_LOG, NORTH_SOUTH_OAK_LOG, EAST_WEST_OAK_LOG, (byte) 19, (byte) 3, sound.digWood, sound.stepWood);
        setDirectionalBlock(UP_DOWN_STRIPPED_OAK_LOG, NORTH_SOUTH_STRIPPED_OAK_LOG, EAST_WEST_STRIPPED_OAK_LOG, (byte) 51, (byte) 35, sound.digWood, sound.stepWood);
        setDirectionalBlock(UP_DOWN_SPRUCE_LOG, NORTH_SOUTH_SPRUCE_LOG, EAST_WEST_SPRUCE_LOG, (byte) 20, (byte) 4, sound.digWood, sound.stepWood);
        setDirectionalBlock(UP_DOWN_STRIPPED_SPRUCE_LOG, NORTH_SOUTH_STRIPPED_SPRUCE_LOG, EAST_WEST_STRIPPED_SPRUCE_LOG, (byte) 52, (byte) 36, sound.digWood, sound.stepWood);
        setDirectionalBlock(UP_DOWN_DARK_OAK_LOG, NORTH_SOUTH_DARK_OAK_LOG, EAST_WEST_DARK_OAK_LOG, (byte) 21, (byte) 5, sound.digWood, sound.stepWood);
        setDirectionalBlock(UP_DOWN_STRIPPED_DARK_OAK_LOG, NORTH_SOUTH_STRIPPED_DARK_OAK_LOG, EAST_WEST_STRIPPED_DARK_OAK_LOG, (byte) 53, (byte) 37, sound.digWood, sound.stepWood);
        setDirectionalBlock(UP_DOWN_PINE_LOG, NORTH_SOUTH_PINE_LOG, EAST_WEST_PINE_LOG, (byte) 22, (byte) 6, sound.digWood, sound.stepWood);
        setDirectionalBlock(UP_DOWN_STRIPPED_PINE_LOG, NORTH_SOUTH_STRIPPED_PINE_LOG, EAST_WEST_STRIPPED_PINE_LOG, (byte) 54, (byte) 38, sound.digWood, sound.stepWood);
        setDirectionalBlock(UP_DOWN_REDWOOD_LOG, NORTH_SOUTH_REDWOOD_LOG, EAST_WEST_REDWOOD_LOG, (byte) 23, (byte) 7, sound.digWood, sound.stepWood);
        setDirectionalBlock(UP_DOWN_STRIPPED_REDWOOD_LOG, NORTH_SOUTH_STRIPPED_REDWOOD_LOG, EAST_WEST_STRIPPED_REDWOOD_LOG, (byte) 55, (byte) 39, sound.digWood, sound.stepWood);
        setDirectionalBlock(UP_DOWN_BLACK_WOOD_LOG, NORTH_SOUTH_BLACK_WOOD_LOG, EAST_WEST_BLACK_WOOD_LOG, (byte) 24, (byte) 8, sound.digWood, sound.stepWood);
        setDirectionalBlock(UP_DOWN_STRIPPED_BLACK_WOOD_LOG, NORTH_SOUTH_STRIPPED_BLACK_WOOD_LOG, EAST_WEST_STRIPPED_BLACK_WOOD_LOG, (byte) 56, (byte) 40, sound.digWood, sound.stepWood);
        setDirectionalBlock(UP_DOWN_BASALT, NORTH_SOUTH_BASALT, EAST_WEST_BASALT, (byte) 0x8A, (byte) 0x89, sound.stepStone, sound.digStone);

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
        setStandardBlockData(MOSSY_STONE_BRICKS, 0, sound.digStone, sound.stepStone, (byte) -19);
        setStandardBlockData(MOSSY_POLISHED_STONE, 0, sound.digStone, sound.stepStone, (byte) -20);
        setStandardBlockData(MOSSY_CHISELED_POLISHED_STONE, 0, sound.digStone, sound.stepStone, (byte) -21);
        setStandardBlockData(MOSSY_CHISELED_STONE, 0, sound.digStone, sound.stepStone, (byte) -22);
        setStandardBlockData(MOSSY_SLATE, 0, sound.digStone, sound.stepStone, (byte) -23);
        setStandardBlockData(MOSSY_COBBLED_SLATE, 0, sound.digStone, sound.stepStone, (byte) -24);
        setStandardBlockData(MOSSY_SLATE_BRICKS, 0, sound.digStone, sound.stepStone, (byte) -25);
        setStandardBlockData(MOSSY_CHISELED_SLATE, 0, sound.digStone, sound.stepStone, (byte) -26);
        setStandardBlockData(MOSSY_POLISHED_SLATE, 0, sound.digStone, sound.stepStone, (byte) -27);
        setStandardBlockData(MOSSY_POLISHED_SANDSTONE, 0, sound.digStone, sound.stepStone, (byte) 0xE4);
        setStandardBlockData(MOSSY_SANDSTONE, 0, sound.digStone, sound.stepStone, (byte) 0xE3);
        setStandardBlockData(MOSSY_OBSIDIAN, BLAST_RESISTANT, sound.digStone, sound.stepStone, (byte) 0xE2);
        setStandardBlockData(MOSSY_CRACKED_ANDESITE, 0, sound.digStone, sound.stepStone, (byte) 0xE1);
        setStandardBlockData(MOSSY_COBBLESTONE, 0, sound.digStone, sound.stepStone, (byte) 0xE0);
        setStandardBlockData(SEA_LIGHT, LIGHT_LEVEL_15, sound.digGlass, sound.stepGlass, (byte) -120);
        setStandardBlockData(PODZOL, 0, sound.digGrass, sound.stepGrass, new byte[]{(byte) 0xB9, (byte) 0xA9, (byte) 0xB9, (byte) 0xB9, (byte) 0x01, (byte) 0xB9});
        setStandardBlockData(RED_SAND, HAS_GRAVITY, sound.digSand, sound.stepSand, (byte) 0xBE);
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
        setStandardBlockData(MOSSY_SANDSTONE_BRICKS, 0, sound.digStone, sound.stepStone, (byte) 0xF0);
        setStandardBlockData(MOSSY_RED_SANDSTONE, 0, sound.digStone, sound.stepStone, (byte) 0xF1);
        setStandardBlockData(MOSSY_RED_POLISHED_SANDSTONE, 0, sound.digStone, sound.stepStone, (byte) 0xF2);
        setStandardBlockData(MOSSY_RED_SANDSTONE_BRICKS, 0, sound.digStone, sound.stepStone, (byte) 0xF3);
        setStandardBlockData(SANDSTONE_BRICKS, 0, sound.digStone, sound.stepStone, (byte) 0xB6);
        setStandardBlockData(RED_SANDSTONE_BRICKS, 0, sound.digStone, sound.stepStone, (byte) 0xBF);
        setStandardBlockData(BLACKSTONE, 0, sound.digStone, sound.stepStone, (byte) 0x73);
        setStandardBlockData(BLACKSTONE_BRICKS, 0, sound.digStone, sound.stepStone, (byte) 0x74);
        setStandardBlockData(POLISHED_BLACKSTONE, 0, sound.digStone, sound.stepStone, (byte) 0x75);
        setStandardBlockData(COAL_BLOCK, 0, sound.digStone, sound.stepStone, (byte) 0x99);
        setStandardBlockData(IRON_BLOCK, 0, sound.digStone, sound.stepStone, (byte) 0x9A);
        setStandardBlockData(DIAMOND_BLOCK, 0, sound.digStone, sound.stepStone, (byte) 0x9B);

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

    private static final int[] NON_STANDARD_BLOCK_TYPE = new int[AMOUNT_OF_NON_STANDARD_BLOCKS];
    private static final byte[][] NON_STANDARD_BLOCK_TEXTURE_INDICES = new byte[AMOUNT_OF_NON_STANDARD_BLOCKS][1];
    private static final byte[][] STANDARD_BLOCK_TEXTURE_INDICES = new byte[AMOUNT_OF_STANDARD_BLOCKS][1];
    private static final int[] NON_STANDARD_BLOCK_PROPERTIES = new int[AMOUNT_OF_NON_STANDARD_BLOCKS];
    private static final int[] STANDARD_BLOCK_PROPERTIES = new int[AMOUNT_OF_STANDARD_BLOCKS];

    private static final int[][] NON_STANDARD_BLOCK_DIG_SOUNDS = new int[AMOUNT_OF_NON_STANDARD_BLOCKS][0];
    private static final int[][] STANDARD_BLOCK_DIG_SOUNDS = new int[AMOUNT_OF_STANDARD_BLOCKS][0];
    private static final int[][] NON_STANDARD_BLOCK_STEP_SOUNDS = new int[AMOUNT_OF_NON_STANDARD_BLOCKS][0];
    private static final int[][] STANDARD_BLOCK_STEP_SOUNDS = new int[AMOUNT_OF_STANDARD_BLOCKS][0];

    private static final String[] NON_STANDARD_BLOCK_NAMES = new String[AMOUNT_OF_NON_STANDARD_BLOCKS];
    private static final String[] STANDARD_BLOCK_NAMES = new String[256];
    private static final String[] BLOCK_TYPE_NAMES = new String[TO_PLACE_BLOCK_TYPES.length];
    private static final String[] FULL_BLOCK_TYPE_NAMES = new String[128]; // Amount of standard block types

    private static final long[][] BLOCK_TYPE_OCCLUSION_DATA = new long[TOTAL_AMOUNT_OF_BLOCK_TYPES][0];
    private static final byte[] BLOCK_TYPE_DATA = new byte[TOTAL_AMOUNT_OF_BLOCK_TYPES];

    private static final byte[][] BLOCK_TYPE_XYZ_SUB_DATA = new byte[TOTAL_AMOUNT_OF_BLOCK_TYPES][0];
    private static final byte[][] BLOCK_TYPE_UV_SUB_DATA = new byte[TOTAL_AMOUNT_OF_BLOCK_TYPES][0];
}
