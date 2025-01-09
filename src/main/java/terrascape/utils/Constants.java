package terrascape.utils;

public class Constants {

    // Literally do whatever
    public static final String TITLE = "Terrascape";
    public static final float Z_NEAR = 0.088f;
    public static final float Z_FAR = Float.POSITIVE_INFINITY;
    public static final float TIME_SPEED = 0.00008333f;
    public static final float NANOSECONDS_PER_SECOND = 1_000_000_000;
    public static final float SPAWN_RADIUS = 150.0f; // More like halfSideLengthOfSpawnSquare
    public static final byte TARGET_TPS = 20;
    public static final int MAX_AMOUNT_OF_TO_RENDER_ENTITIES = 20000; // A thing with GPU memory not so much performance

    // DO NOT CHANGE THESE VALUES (like really, it will crash)
    public static final byte CHUNK_SIZE_BITS = 5;
    public static final byte CHUNK_SIZE = 1 << CHUNK_SIZE_BITS;
    public static final byte CHUNK_SIZE_MASK = CHUNK_SIZE - 1;
    public static final byte CHUNK_COORDINATE_MASK = -1 << CHUNK_SIZE_BITS;
    public static final int MAX_CHUNKS_XZ = 0x7FFFFFF;
    public static final int MAX_CHUNKS_Y = 0x3FF;
    public static final byte ENTITY_CLUSTER_SIZE_BITS = 3;
    public static final byte IN_CHUNK_ENTITY_CLUSTER_MASK = 3;
    public static final byte ENTITY_CLUSTER_TO_CHUNK_BITS = CHUNK_SIZE_BITS - ENTITY_CLUSTER_SIZE_BITS;

    //Movement
    public static final float AIR_FRICTION = 0.91f;
    public static final float FALL_FRICTION = 0.98f;
    public static final float WATER_FRICTION = 0.4f;
    public static final float GROUND_FRICTION = 0.546f;
    public static final float GRAVITY_ACCELERATION = 0.08f;

    // Change based on computing power
    public static final byte MAX_CHUNKS_TO_BUFFER_PER_FRAME = 15;

    public static final byte NUMBER_OF_GENERATION_THREADS = 3;
    public static final byte MAX_OCCLUSION_CULLING_DAMPER = 6;

    public static final float MAX_SOUND_DISTANCE = 30.0f * 30.0f;

    // Other useful stuff
    public static final byte MAX_SKY_LIGHT_VALUE = 15;

    // Indices for the sides of blocks
    /**
     * Positive Z.
     */
    public static final byte NORTH = 0;
    /**
     * Positive Y.
     */
    public static final byte TOP = 1;
    /**
     * Positive X.
     */
    public static final byte WEST = 2;
    /**
     * Negative Z.
     */
    public static final byte SOUTH = 3;
    /**
     * Negative Y.
     */
    public static final byte BOTTOM = 4;
    /**
     * Negative X.
     */
    public static final byte EAST = 5;
    public static final byte NONE = 6;

    // BLOCK_TYPE_DATA

    //    public static final int INTERACTABLE = 4;
    public static final byte SMART_BLOCK_TYPE = 64;
    public static final byte DYNAMIC_SHAPE_MASK = -128;

    // BLOCK_XYZ_SUB_DATA
    public static final byte MIN_X = 0;
    public static final byte MAX_X = 1;
    public static final byte MIN_Y = 2;
    public static final byte MAX_Y = 3;
    public static final byte MIN_Z = 4;
    public static final byte MAX_Z = 5;

    // BLOCK_PROPERTIES
    public static final int LIGHT_EMITTING = 15;
    public static final int NO_COLLISION = 16;
    public static final int INTERACTABLE = 32;
    public static final int REPLACEABLE = 64;
    public static final int BLAST_RESISTANT = 128;
    public static final int HAS_GRAVITY = 256;
    public static final int REQUIRES_BOTTOM_SUPPORT = 512;
    public static final int REQUIRES_AND_SIDE_SUPPORT = 1024;
    public static final int ROTATE_NORTH_TEXTURE = 2048;
    public static final int ROTATE_TOP_TEXTURE = 4096;
    public static final int ROTATE_WEST_TEXTURE = 8192;
    public static final int ROTATE_SOUTH_TEXTURE = 16384;
    public static final int ROTATE_BOTTOM_TEXTURE = 32768;
    public static final int ROTATE_EAST_TEXTURE = 65536;

    // Block emitted light levels
    public static final byte LIGHT_LEVEL_1 = 1;
    public static final byte LIGHT_LEVEL_2 = 2;
    public static final byte LIGHT_LEVEL_3 = 3;
    public static final byte LIGHT_LEVEL_4 = 4;
    public static final byte LIGHT_LEVEL_5 = 5;
    public static final byte LIGHT_LEVEL_6 = 6;
    public static final byte LIGHT_LEVEL_7 = 7;
    public static final byte LIGHT_LEVEL_8 = 8;
    public static final byte LIGHT_LEVEL_9 = 9;
    public static final byte LIGHT_LEVEL_10 = 10;
    public static final byte LIGHT_LEVEL_11 = 11;
    public static final byte LIGHT_LEVEL_12 = 12;
    public static final byte LIGHT_LEVEL_13 = 13;
    public static final byte LIGHT_LEVEL_14 = 14;
    public static final byte LIGHT_LEVEL_15 = 15;

    // Indices for information on block types
    public static final byte FULL_BLOCK = 0;

    public static final byte BOTTOM_SLAB = 1;
    public static final byte TOP_SLAB = 2;
    public static final byte NORTH_SLAB = 3;
    public static final byte SOUTH_SLAB = 4;
    public static final byte WEST_SLAB = 5;
    public static final byte EAST_SLAB = 6;
    public static final byte[] SLABS = new byte[]{NORTH_SLAB, TOP_SLAB, WEST_SLAB, SOUTH_SLAB, BOTTOM_SLAB, EAST_SLAB};

    public static final byte UP_DOWN_POST = 7;
    public static final byte NORTH_SOUTH_POST = 8;
    public static final byte EAST_WEST_POST = 9;
    public static final byte[] POSTS = new byte[]{NORTH_SOUTH_POST, UP_DOWN_POST, EAST_WEST_POST};

    public static final byte UP_DOWN_WALL = 10;
    public static final byte NORTH_SOUTH_WALL = 11;
    public static final byte EAST_WEST_WALL = 12;
    public static final byte[] WALLS = new byte[]{NORTH_SOUTH_WALL, UP_DOWN_WALL, EAST_WEST_WALL};

    public static final byte NORTH_PLATE = 13;
    public static final byte TOP_PLATE = 14;
    public static final byte WEST_PLATE = 15;
    public static final byte SOUTH_PLATE = 16;
    public static final byte BOTTOM_PLATE = 17;
    public static final byte EAST_PLATE = 18;
    public static final byte[] PLATES = new byte[]{NORTH_PLATE, TOP_PLATE, WEST_PLATE, SOUTH_PLATE, BOTTOM_PLATE, EAST_PLATE};

    public static final byte BOTTOM_NORTH_STAIR = 19;
    public static final byte BOTTOM_SOUTH_STAIR = 20;
    public static final byte BOTTOM_WEST_STAIR = 21;
    public static final byte BOTTOM_EAST_STAIR = 22;
    public static final byte TOP_NORTH_STAIR = 23;
    public static final byte TOP_SOUTH_STAIR = 24;
    public static final byte TOP_WEST_STAIR = 25;
    public static final byte TOP_EAST_STAIR = 26;
    public static final byte NORTH_WEST_STAIR = 27;
    public static final byte NORTH_EAST_STAIR = 28;
    public static final byte SOUTH_WEST_STAIR = 29;
    public static final byte SOUTH_EAST_STAIR = 30;

    public static final byte BOTTOM_PLAYER_HEAD = 31;
    public static final byte TOP_PLAYER_HEAD = 32;
    public static final byte NORTH_PLAYER_HEAD = 33;
    public static final byte SOUTH_PLAYER_HEAD = 34;
    public static final byte WEST_PLAYER_HEAD = 35;
    public static final byte EAST_PLAYER_HEAD = 36;
    public static final byte CARPET = 37;
    public static final byte NORTH_SOCKET = 38;
    public static final byte TOP_SOCKET = 39;
    public static final byte WEST_SOCKET = 40;
    public static final byte SOUTH_SOCKET = 41;
    public static final byte BOTTOM_SOCKET = 42;
    public static final byte EAST_SOCKET = 43;
    public static final byte[] SOCKETS = new byte[]{NORTH_SOCKET, TOP_SOCKET, WEST_SOCKET, SOUTH_SOCKET, BOTTOM_SOCKET, EAST_SOCKET};

    public static final byte THICK_BOTTOM_NORTH_STAIR = 44;
    public static final byte THICK_BOTTOM_SOUTH_STAIR = 45;
    public static final byte THICK_BOTTOM_WEST_STAIR = 46;
    public static final byte THICK_BOTTOM_EAST_STAIR = 47;
    public static final byte THICK_TOP_NORTH_STAIR = 48;
    public static final byte THICK_TOP_SOUTH_STAIR = 49;
    public static final byte THICK_TOP_WEST_STAIR = 50;
    public static final byte THICK_TOP_EAST_STAIR = 51;
    public static final byte THICK_NORTH_WEST_STAIR = 52;
    public static final byte THICK_NORTH_EAST_STAIR = 53;
    public static final byte THICK_SOUTH_WEST_STAIR = 54;
    public static final byte THICK_SOUTH_EAST_STAIR = 55;

    public static final byte THIN_BOTTOM_NORTH_STAIR = 56;
    public static final byte THIN_BOTTOM_SOUTH_STAIR = 57;
    public static final byte THIN_BOTTOM_WEST_STAIR = 58;
    public static final byte THIN_BOTTOM_EAST_STAIR = 59;
    public static final byte THIN_TOP_NORTH_STAIR = 60;
    public static final byte THIN_TOP_SOUTH_STAIR = 61;
    public static final byte THIN_TOP_WEST_STAIR = 62;
    public static final byte THIN_TOP_EAST_STAIR = 63;
    public static final byte THIN_NORTH_WEST_STAIR = 64;
    public static final byte THIN_NORTH_EAST_STAIR = 65;
    public static final byte THIN_SOUTH_WEST_STAIR = 66;
    public static final byte THIN_SOUTH_EAST_STAIR = 67;

    public static final byte UP_DOWN_FENCE = 68;
    public static final byte UP_DOWN_FENCE_NORTH = 69;
    public static final byte UP_DOWN_FENCE_WEST = 70;
    public static final byte UP_DOWN_FENCE_NORTH_WEST = 71;
    public static final byte UP_DOWN_FENCE_SOUTH = 72;
    public static final byte UP_DOWN_FENCE_NORTH_SOUTH = 73;
    public static final byte UP_DOWN_FENCE_WEST_SOUTH = 74;
    public static final byte UP_DOWN_FENCE_NORTH_WEST_SOUTH = 75;
    public static final byte UP_DOWN_FENCE_EAST = 76;
    public static final byte UP_DOWN_FENCE_NORTH_EAST = 77;
    public static final byte UP_DOWN_FENCE_WEST_EAST = 78;
    public static final byte UP_DOWN_FENCE_NORTH_WEST_EAST = 79;
    public static final byte UP_DOWN_FENCE_SOUTH_EAST = 80;
    public static final byte UP_DOWN_FENCE_NORTH_SOUTH_EAST = 81;
    public static final byte UP_DOWN_FENCE_WEST_SOUTH_EAST = 82;
    public static final byte UP_DOWN_FENCE_NORTH_WEST_SOUTH_EAST = 83;

    public static final byte NORTH_SOUTH_FENCE = 84;
    public static final byte NORTH_SOUTH_FENCE_UP = 85;
    public static final byte NORTH_SOUTH_FENCE_WEST = 86;
    public static final byte NORTH_SOUTH_FENCE_UP_WEST = 87;
    public static final byte NORTH_SOUTH_FENCE_DOWN = 88;
    public static final byte NORTH_SOUTH_FENCE_UP_DOWN = 89;
    public static final byte NORTH_SOUTH_FENCE_WEST_DOWN = 90;
    public static final byte NORTH_SOUTH_FENCE_UP_WEST_DOWN = 91;
    public static final byte NORTH_SOUTH_FENCE_EAST = 92;
    public static final byte NORTH_SOUTH_FENCE_UP_EAST = 93;
    public static final byte NORTH_SOUTH_FENCE_WEST_EAST = 94;
    public static final byte NORTH_SOUTH_FENCE_UP_WEST_EAST = 95;
    public static final byte NORTH_SOUTH_FENCE_DOWN_EAST = 96;
    public static final byte NORTH_SOUTH_FENCE_UP_DOWN_EAST = 97;
    public static final byte NORTH_SOUTH_FENCE_WEST_DOWN_EAST = 98;
    public static final byte NORTH_SOUTH_FENCE_UP_WEST_DOWN_EAST = 99;

    public static final byte EAST_WEST_FENCE = 100;
    public static final byte EAST_WEST_FENCE_NORTH = 101;
    public static final byte EAST_WEST_FENCE_UP = 102;
    public static final byte EAST_WEST_FENCE_NORTH_UP = 103;
    public static final byte EAST_WEST_FENCE_SOUTH = 104;
    public static final byte EAST_WEST_FENCE_NORTH_SOUTH = 105;
    public static final byte EAST_WEST_FENCE_UP_SOUTH = 106;
    public static final byte EAST_WEST_FENCE_NORTH_UP_SOUTH = 107;
    public static final byte EAST_WEST_FENCE_DOWN = 108;
    public static final byte EAST_WEST_FENCE_NORTH_DOWN = 109;
    public static final byte EAST_WEST_FENCE_UP_DOWN = 110;
    public static final byte EAST_WEST_FENCE_NORTH_UP_DOWN = 111;
    public static final byte EAST_WEST_FENCE_SOUTH_DOWN = 112;
    public static final byte EAST_WEST_FENCE_NORTH_SOUTH_DOWN = 113;
    public static final byte EAST_WEST_FENCE_UP_SOUTH_DOWN = 114;
    public static final byte EAST_WEST_FENCE_NORTH_UP_SOUTH_DOWN = 115;
    public static final byte[] FENCES = new byte[]{NORTH_SOUTH_FENCE, UP_DOWN_FENCE, EAST_WEST_FENCE};

    public static final byte NORTH_WEST_DOOR_NORTH = 116;
    public static final byte NORTH_WEST_DOOR_WEST = 117;
    public static final byte NORTH_EAST_DOOR_NORTH = 118;
    public static final byte NORTH_EAST_DOOR_EAST = 119;
    public static final byte SOUTH_WEST_DOOR_SOUTH = 120;
    public static final byte SOUTH_WEST_DOOR_WEST = 121;
    public static final byte SOUTH_EAST_DOOR_SOUTH = 122;
    public static final byte SOUTH_EAST_DOOR_EAST = 123;

    public static final short FLOWER_TYPE = 128;
    public static final short CACTUS_TYPE = 129;
    public static final short AIR_TYPE = 130;
    public static final short TORCH_TYPE = 131;
    public static final short PATH_TYPE = 132;
    public static final short LIQUID_TYPE = 133;
    public static final short LIQUID_LEVEL_1 = 134;
    public static final short LIQUID_LEVEL_2 = 135;
    public static final short LIQUID_LEVEL_3 = 136;
    public static final short LIQUID_LEVEL_4 = 137;
    public static final short LIQUID_LEVEL_5 = 138;
    public static final short LIQUID_LEVEL_6 = 139;
    public static final short LIQUID_LEVEL_7 = 140;
    public static final short LIQUID_LEVEL_8 = 141;
    public static final short VINE_TYPE = 142;

    public static final int TOTAL_AMOUNT_OF_BLOCK_TYPES = 143;

    public static final byte[] TO_PLACE_BLOCK_TYPES = new byte[]{FULL_BLOCK, BOTTOM_PLAYER_HEAD, BOTTOM_SOCKET, BOTTOM_SLAB, BOTTOM_PLATE, NORTH_SOUTH_WALL, UP_DOWN_POST, THICK_BOTTOM_SOUTH_STAIR, BOTTOM_SOUTH_STAIR, THIN_BOTTOM_SOUTH_STAIR, UP_DOWN_FENCE_NORTH_WEST, NORTH_WEST_DOOR_NORTH, CARPET};
    public static final byte BLOCK_TYPE_BITS = 8;
    public static final short WATER_LOGGED_MASK = (1 << BLOCK_TYPE_BITS - 1);
    public static final byte BLOCK_TYPE_MASK = (1 << BLOCK_TYPE_BITS - 1) - 1;
    public static final short BASE_BLOCK_MASK = -1 << BLOCK_TYPE_BITS;
    public static final short STANDARD_BLOCKS_THRESHOLD = 1 << BLOCK_TYPE_BITS;

    // Non standard block, aka blocks without blockTypes
    public static final short AIR = 0;
    public static final short OUT_OF_WORLD = 1;
    public static final short VINES = 2;
    public static final short GLOW_LICHEN = 3;
    public static final short CACTUS = 4;
    public static final short NORTH_CREATOR_HEAD = 5;
    public static final short SOUTH_CREATOR_HEAD = 6;
    public static final short WEST_CREATOR_HEAD = 7;
    public static final short EAST_CREATOR_HEAD = 8;
    public static final short TORCH = 9;
    public static final short TALL_GRASS = 10;
    public static final short RED_TULIP = 11;
    public static final short YELLOW_TULIP = 12;
    public static final short ORANGE_TULIP = 13;
    public static final short MAGENTA_TULIP = 14;
    public static final short ROSE = 15;
    public static final short HYACINTH = 16;
    public static final short DRISLY = 17;
    public static final short SHRUB = 18;
    public static final short SUGAR_CANE = 19;
    public static final short PATH_BLOCK = 20;
    public static final short BLACK_ROSE = 21;
    public static final short FLIELEN = 22;
    public static final short WATER_SOURCE = 23;
    public static final short FLOWING_WATER_LEVEL_8 = 24;
    public static final short FLOWING_WATER_LEVEL_7 = 25;
    public static final short FLOWING_WATER_LEVEL_6 = 26;
    public static final short FLOWING_WATER_LEVEL_5 = 27;
    public static final short FLOWING_WATER_LEVEL_4 = 28;
    public static final short FLOWING_WATER_LEVEL_3 = 29;
    public static final short FLOWING_WATER_LEVEL_2 = 30;
    public static final short FLOWING_WATER_LEVEL_1 = 31;
    public static final short LAVA_SOURCE = 32;
    public static final short FLOWING_LAVA_LEVEL_4 = 33;
    public static final short FLOWING_LAVA_LEVEL_3 = 34;
    public static final short FLOWING_LAVA_LEVEL_2 = 35;
    public static final short FLOWING_LAVA_LEVEL_1 = 36;

    public static final short[] TO_PLACE_NON_STANDARD_BLOCKS = new short[]{WATER_SOURCE, LAVA_SOURCE, CACTUS, NORTH_CREATOR_HEAD, TORCH, TALL_GRASS, RED_TULIP, YELLOW_TULIP, ORANGE_TULIP, MAGENTA_TULIP, ROSE, HYACINTH, DRISLY, SHRUB, SUGAR_CANE, PATH_BLOCK, BLACK_ROSE, FLIELEN, VINES, GLOW_LICHEN};
    public static final int AMOUNT_OF_NON_STANDARD_BLOCKS = 37;

    // Standard blocks, aka blocks with blockTypes
    public static final short GRASS = (short) (1 << BLOCK_TYPE_BITS);
    public static final short DIRT = (short) (2 << BLOCK_TYPE_BITS);
    public static final short STONE = (short) (3 << BLOCK_TYPE_BITS);
    public static final short STONE_BRICKS = (short) (4 << BLOCK_TYPE_BITS);
    public static final short COBBLESTONE = (short) (5 << BLOCK_TYPE_BITS);
    public static final short CHISELED_STONE = (short) (6 << BLOCK_TYPE_BITS);
    public static final short POLISHED_STONE = (short) (7 << BLOCK_TYPE_BITS);
    public static final short CHISELED_POLISHED_STONE = (short) (8 << BLOCK_TYPE_BITS);
    public static final short MUD = (short) (9 << BLOCK_TYPE_BITS);
    public static final short ANDESITE = (short) (10 << BLOCK_TYPE_BITS);
    public static final short SNOW = (short) (11 << BLOCK_TYPE_BITS);
    public static final short SAND = (short) (12 << BLOCK_TYPE_BITS);
    public static final short SANDSTONE = (short) (13 << BLOCK_TYPE_BITS);
    public static final short POLISHED_SANDSTONE = (short) (14 << BLOCK_TYPE_BITS);
    public static final short SLATE = (short) (15 << BLOCK_TYPE_BITS);
    public static final short CHISELED_SLATE = (short) (16 << BLOCK_TYPE_BITS);
    public static final short COBBLED_SLATE = (short) (17 << BLOCK_TYPE_BITS);
    public static final short SLATE_BRICKS = (short) (18 << BLOCK_TYPE_BITS);
    public static final short POLISHED_SLATE = (short) (19 << BLOCK_TYPE_BITS);
    public static final short GLASS = (short) (20 << BLOCK_TYPE_BITS);
    public static final short GRAVEL = (short) (21 << BLOCK_TYPE_BITS);
    public static final short COURSE_DIRT = (short) (22 << BLOCK_TYPE_BITS);
    public static final short CLAY = (short) (23 << BLOCK_TYPE_BITS);
    public static final short MOSS = (short) (24 << BLOCK_TYPE_BITS);
    public static final short ICE = (short) (25 << BLOCK_TYPE_BITS);
    public static final short HEAVY_ICE = (short) (26 << BLOCK_TYPE_BITS);
    public static final short COAL_ORE = (short) (27 << BLOCK_TYPE_BITS);
    public static final short IRON_ORE = (short) (28 << BLOCK_TYPE_BITS);
    public static final short DIAMOND_ORE = (short) (29 << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_OAK_LOG = (short) (30 << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_STRIPPED_OAK_LOG = (short) (31 << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_SPRUCE_LOG = (short) (32 << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_STRIPPED_SPRUCE_LOG = (short) (33 << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_DARK_OAK_LOG = (short) (34 << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_STRIPPED_DARK_OAK_LOG = (short) (35 << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_PINE_LOG = (short) (36 << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_STRIPPED_PINE_LOG = (short) (37 << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_REDWOOD_LOG = (short) (38 << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_STRIPPED_REDWOOD_LOG = (short) (39 << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_BLACK_WOOD_LOG = (short) (40 << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_STRIPPED_BLACK_WOOD_LOG = (short) (41 << BLOCK_TYPE_BITS);
    public static final short OAK_LEAVES = (short) (42 << BLOCK_TYPE_BITS);
    public static final short SPRUCE_LEAVES = (short) (43 << BLOCK_TYPE_BITS);
    public static final short DARK_OAK_LEAVES = (short) (44 << BLOCK_TYPE_BITS);
    public static final short PINE_LEAVES = (short) (45 << BLOCK_TYPE_BITS);
    public static final short REDWOOD_LEAVES = (short) (46 << BLOCK_TYPE_BITS);
    public static final short BLACK_WOOD_LEAVES = (short) (47 << BLOCK_TYPE_BITS);
    public static final short OAK_PLANKS = (short) (48 << BLOCK_TYPE_BITS);
    public static final short SPRUCE_PLANKS = (short) (49 << BLOCK_TYPE_BITS);
    public static final short DARK_OAK_PLANKS = (short) (50 << BLOCK_TYPE_BITS);
    public static final short PINE_PLANKS = (short) (51 << BLOCK_TYPE_BITS);
    public static final short REDWOOD_PLANKS = (short) (52 << BLOCK_TYPE_BITS);
    public static final short BLACK_WOOD_PLANKS = (short) (53 << BLOCK_TYPE_BITS);
    public static final short CRACKED_ANDESITE = (short) (54 << BLOCK_TYPE_BITS);
    public static final short BLACK = (short) (55 << BLOCK_TYPE_BITS);
    public static final short WHITE = (short) (56 << BLOCK_TYPE_BITS);
    public static final short CYAN = (short) (57 << BLOCK_TYPE_BITS);
    public static final short MAGENTA = (short) (58 << BLOCK_TYPE_BITS);
    public static final short YELLOW = (short) (59 << BLOCK_TYPE_BITS);
    public static final short BLUE = (short) (60 << BLOCK_TYPE_BITS);
    public static final short GREEN = (short) (61 << BLOCK_TYPE_BITS);
    public static final short RED = (short) (62 << BLOCK_TYPE_BITS);
    public static final short CRAFTING_TABLE = (short) (63 << BLOCK_TYPE_BITS);
    public static final short TNT = (short) (64 << BLOCK_TYPE_BITS);
    public static final short OBSIDIAN = (short) (65 << BLOCK_TYPE_BITS);
    public static final short MOSSY_STONE = (short) (66 << BLOCK_TYPE_BITS);
    public static final short MOSSY_ANDESITE = (short) (67 << BLOCK_TYPE_BITS);
    public static final short MOSSY_STONE_BRICKS = (short) (68 << BLOCK_TYPE_BITS);
    public static final short MOSSY_POLISHED_STONE = (short) (69 << BLOCK_TYPE_BITS);
    public static final short MOSSY_CHISELED_POLISHED_STONE = (short) (70 << BLOCK_TYPE_BITS);
    public static final short MOSSY_CHISELED_STONE = (short) (71 << BLOCK_TYPE_BITS);
    public static final short MOSSY_SLATE = (short) (72 << BLOCK_TYPE_BITS);
    public static final short MOSSY_COBBLED_SLATE = (short) (73 << BLOCK_TYPE_BITS);
    public static final short MOSSY_SLATE_BRICKS = (short) (74 << BLOCK_TYPE_BITS);
    public static final short MOSSY_CHISELED_SLATE = (short) (75 << BLOCK_TYPE_BITS);
    public static final short MOSSY_POLISHED_SLATE = (short) (76 << BLOCK_TYPE_BITS);
    public static final short MOSSY_SANDSTONE_BRICKS = (short) (77 << BLOCK_TYPE_BITS);
    public static final short MOSSY_RED_SANDSTONE_BRICKS = (short) (78 << BLOCK_TYPE_BITS);
    public static final short MOSSY_OBSIDIAN = (short) (79 << BLOCK_TYPE_BITS);
    public static final short MOSSY_CRACKED_ANDESITE = (short) (80 << BLOCK_TYPE_BITS);
    public static final short MOSSY_COBBLESTONE = (short) (81 << BLOCK_TYPE_BITS);
    public static final short NORTH_FURNACE = (short) (82 << BLOCK_TYPE_BITS);
    public static final short SEA_LIGHT = (short) (83 << BLOCK_TYPE_BITS);
    public static final short PODZOL = (short) (84 << BLOCK_TYPE_BITS);
    public static final short RED_SAND = (short) (85 << BLOCK_TYPE_BITS);
    public static final short RED_SANDSTONE = (short) (86 << BLOCK_TYPE_BITS);
    public static final short RED_POLISHED_SANDSTONE = (short) (87 << BLOCK_TYPE_BITS);
    public static final short TERRACOTTA = (short) (88 << BLOCK_TYPE_BITS);
    public static final short RED_TERRACOTTA = (short) (89 << BLOCK_TYPE_BITS);
    public static final short GREEN_TERRACOTTA = (short) (90 << BLOCK_TYPE_BITS);
    public static final short BLUE_TERRACOTTA = (short) (91 << BLOCK_TYPE_BITS);
    public static final short YELLOW_TERRACOTTA = (short) (92 << BLOCK_TYPE_BITS);
    public static final short MAGENTA_TERRACOTTA = (short) (93 << BLOCK_TYPE_BITS);
    public static final short CYAN_TERRACOTTA = (short) (94 << BLOCK_TYPE_BITS);
    public static final short WHITE_TERRACOTTA = (short) (95 << BLOCK_TYPE_BITS);
    public static final short BLACK_TERRACOTTA = (short) (96 << BLOCK_TYPE_BITS);
    public static final short RED_WOOL = (short) (97 << BLOCK_TYPE_BITS);
    public static final short GREEN_WOOL = (short) (98 << BLOCK_TYPE_BITS);
    public static final short BLUE_WOOL = (short) (99 << BLOCK_TYPE_BITS);
    public static final short YELLOW_WOOL = (short) (100 << BLOCK_TYPE_BITS);
    public static final short MAGENTA_WOOL = (short) (101 << BLOCK_TYPE_BITS);
    public static final short CYAN_WOOL = (short) (102 << BLOCK_TYPE_BITS);
    public static final short WHITE_WOOL = (short) (103 << BLOCK_TYPE_BITS);
    public static final short BLACK_WOOL = (short) (104 << BLOCK_TYPE_BITS);
    public static final short SANDSTONE_BRICKS = (short) (105 << BLOCK_TYPE_BITS);
    public static final short RED_SANDSTONE_BRICKS = (short) (106 << BLOCK_TYPE_BITS);
    public static final short MOSSY_SANDSTONE = (short) (107 << BLOCK_TYPE_BITS);
    public static final short MOSSY_POLISHED_SANDSTONE = (short) (108 << BLOCK_TYPE_BITS);
    public static final short MOSSY_RED_SANDSTONE = (short) (109 << BLOCK_TYPE_BITS);
    public static final short MOSSY_RED_POLISHED_SANDSTONE = (short) (110 << BLOCK_TYPE_BITS);

    public static final int AMOUNT_OF_TO_PLACE_STANDARD_BLOCKS = 111;

    public static final short NORTH_SOUTH_OAK_LOG = (short) (255 << BLOCK_TYPE_BITS);
    public static final short EAST_WEST_OAK_LOG = (short) (254 << BLOCK_TYPE_BITS);
    public static final short NORTH_SOUTH_STRIPPED_OAK_LOG = (short) (253 << BLOCK_TYPE_BITS);
    public static final short EAST_WEST_STRIPPED_OAK_LOG = (short) (252 << BLOCK_TYPE_BITS);
    public static final short NORTH_SOUTH_SPRUCE_LOG = (short) (251 << BLOCK_TYPE_BITS);
    public static final short EAST_WEST_SPRUCE_LOG = (short) (250 << BLOCK_TYPE_BITS);
    public static final short NORTH_SOUTH_STRIPPED_SPRUCE_LOG = (short) (249 << BLOCK_TYPE_BITS);
    public static final short EAST_WEST_STRIPPED_SPRUCE_LOG = (short) (248 << BLOCK_TYPE_BITS);
    public static final short NORTH_SOUTH_DARK_OAK_LOG = (short) (247 << BLOCK_TYPE_BITS);
    public static final short EAST_WEST_DARK_OAK_LOG = (short) (246 << BLOCK_TYPE_BITS);
    public static final short NORTH_SOUTH_STRIPPED_DARK_OAK_LOG = (short) (245 << BLOCK_TYPE_BITS);
    public static final short EAST_WEST_STRIPPED_DARK_OAK_LOG = (short) (244 << BLOCK_TYPE_BITS);
    public static final short NORTH_SOUTH_PINE_LOG = (short) (243 << BLOCK_TYPE_BITS);
    public static final short EAST_WEST_PINE_LOG = (short) (242 << BLOCK_TYPE_BITS);
    public static final short NORTH_SOUTH_STRIPPED_PINE_LOG = (short) (241 << BLOCK_TYPE_BITS);
    public static final short EAST_WEST_STRIPPED_PINE_LOG = (short) (240 << BLOCK_TYPE_BITS);
    public static final short NORTH_SOUTH_REDWOOD_LOG = (short) (239 << BLOCK_TYPE_BITS);
    public static final short EAST_WEST_REDWOOD_LOG = (short) (238 << BLOCK_TYPE_BITS);
    public static final short NORTH_SOUTH_STRIPPED_REDWOOD_LOG = (short) (237 << BLOCK_TYPE_BITS);
    public static final short EAST_WEST_STRIPPED_REDWOOD_LOG = (short) (236 << BLOCK_TYPE_BITS);
    public static final short NORTH_SOUTH_BLACK_WOOD_LOG = (short) (235 << BLOCK_TYPE_BITS);
    public static final short EAST_WEST_BLACK_WOOD_LOG = (short) (234 << BLOCK_TYPE_BITS);
    public static final short NORTH_SOUTH_STRIPPED_BLACK_WOOD_LOG = (short) (233 << BLOCK_TYPE_BITS);
    public static final short EAST_WEST_STRIPPED_BLACK_WOOD_LOG = (short) (232 << BLOCK_TYPE_BITS);
    public static final short SOUTH_FURNACE = (short) (231 << BLOCK_TYPE_BITS);
    public static final short WEST_FURNACE = (short) (230 << BLOCK_TYPE_BITS);
    public static final short EAST_FURNACE = (short) (229 << BLOCK_TYPE_BITS);

    public static final int AMOUNT_OF_STANDARD_BLOCKS = 256;

    // Just pretend it doesn't exist
    public static final float[] SKY_BOX_VERTICES;

    public static final int[] SKY_BOX_INDICES;

    public static final float[] SKY_BOX_TEXTURE_COORDINATES;

    public static final float[] GUI_ELEMENT_TEXTURE_COORDINATES;

    public static final float[] OVERLAY_VERTICES;

    // No like actually, this doesn't exist! Trust me. please...
    static {
        // NOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
        // I WARNED YOU!!!
        // WHY DIDN'T YOU LISTEN!??!!?
        // Ok it's actually not THAT bad... (anymore)

        SKY_BOX_VERTICES = new float[]{-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
        SKY_BOX_INDICES = new int[]{0, 2, 1, 3, 1, 2, 4, 5, 6, 7, 6, 5, 8, 9, 10, 11, 10, 9, 12, 14, 13, 15, 13, 14, 16, 18, 17, 19, 17, 18, 20, 21, 22, 23, 22, 21};
        SKY_BOX_TEXTURE_COORDINATES = new float[]{1.0f, 2 / 3f, 0.75f, 2 / 3f, 1.0f, 1 / 3f, 0.75f, 1 / 3f, 0.25f, 2 / 3f, 0.5f, 2 / 3f, 0.25f, 1 / 3f, 0.5f, 1 / 3f, 0.25f, 1.0f, 0.5f, 1.0f, 0.25f, 2 / 3f, 0.5f, 2 / 3f, 0.25f, 0.0f, 0.5f, 0.0f, 0.25f, 1 / 3f, 0.5f, 1 / 3f, 0.0f, 2 / 3f, 0.0f, 1 / 3f, 0.25f, 2 / 3f, 0.25f, 1 / 3f, 0.75f, 2 / 3f, 0.75f, 1 / 3f, 0.5f, 2 / 3f, 0.5f, 1 / 3f};
        GUI_ELEMENT_TEXTURE_COORDINATES = new float[]{0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f};
        OVERLAY_VERTICES = new float[]{-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f};
    }
}
