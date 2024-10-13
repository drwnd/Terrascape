package com.MBEv2.core.utils;

public class Constants {

    //Recommended to not change, but I can't stop you
    public static final String TITLE = "Minecräft Bad Edition v2";
    public static final float Z_NEAR = 0.01f;
    public static final float Z_FAR = 1000.0f;
    public static final float TIME_SPEED = 0.00008333f;
    public static final float NANOSECONDS_PER_SECOND = 1_000_000_000;

    //DO NOT CHANGE THESE VALUES (like really, it will crash)
    public static final int CHUNK_SIZE_BITS = 5;
    public static final int CHUNK_SIZE = 1 << CHUNK_SIZE_BITS;
    public static final int CHUNK_SIZE_MASK = CHUNK_SIZE - 1;
    public static final int MAX_CHUNKS_XZ = 0x7FFFFFF;
    public static final int MAX_CHUNKS_Y = 0x3FF;
    public static final int ENTITY_CLUSTER_SIZE_BITS = 3;
    public static final int IN_CHUNK_ENTITY_CLUSTER_MASK = 3;
    public static final int ENTITY_CLUSTER_TO_CHUNK_BITS = CHUNK_SIZE_BITS - ENTITY_CLUSTER_SIZE_BITS;

    //Change based on computing power
    public static final int MAX_CHUNKS_TO_BUFFER_PER_FRAME = 15;

    public static final int NUMBER_OF_GENERATION_THREADS = 4;
    public static final int MAX_OCCLUSION_CULLING_DAMPER = 6;

    public static final float MAX_SOUND_DISTANCE = 30.0f * 30.0f;

    //Other useful stuff
    public static final int MAX_BLOCK_LIGHT_VALUE = 15;
    public static final int MAX_SKY_LIGHT_VALUE = 15;

    //Indices for the sides of blocks
    public static final int FRONT = 0;
    public static final int TOP = 1;
    public static final int RIGHT = 2;
    public static final int BACK = 3;
    public static final int BOTTOM = 4;
    public static final int LEFT = 5;
    public static final int NONE = 6;

    //BLOCK_TYPE_OCCLUSION_DATA
    public static final int OCCLUDES_ALL = 0;
    public static final int OCCLUDES_SELF = 1;
    public static final int OCCLUDES_DYNAMIC_SELF = 3;

    //BLOCK_TYPE_DATA
    public static final int SMART_BLOCK_TYPE = 64;
    public static final int DYNAMIC_SHAPE_MASK = 128;

    //BLOCK_XYZ_SUB_DATA
    public static final int MIN_X = 0;
    public static final int MAX_X = 1;
    public static final int MIN_Y = 2;
    public static final int MAX_Y = 3;
    public static final int MIN_Z = 4;
    public static final int MAX_Z = 5;

    //BLOCK_PROPERTIES
    public static final int LIGHT_EMITTING = 1;
    public static final int NO_COLLISION = 2;
    public static final int INTERACTABLE = 4;
    public static final int REPLACEABLE = 8;
    public static final int BLAST_RESISTANT = 16;

    //Indices for information on block types
    public static final byte FULL_BLOCK = 0;

    public static final byte BOTTOM_SLAB = 1;
    public static final byte TOP_SLAB = 2;
    public static final byte FRONT_SLAB = 3;
    public static final byte BACK_SLAB = 4;
    public static final byte RIGHT_SLAB = 5;
    public static final byte LEFT_SLAB = 6;
    public static final byte[] SLABS = new byte[]{FRONT_SLAB, TOP_SLAB, RIGHT_SLAB, BACK_SLAB, BOTTOM_SLAB, LEFT_SLAB};

    public static final byte UP_DOWN_POST = 7;
    public static final byte FRONT_BACK_POST = 8;
    public static final byte LEFT_RIGHT_POST = 9;
    public static final byte[] POSTS = new byte[]{FRONT_BACK_POST, UP_DOWN_POST, LEFT_RIGHT_POST};

    public static final byte UP_DOWN_WALL = 10;
    public static final byte FRONT_BACK_WALL = 11;
    public static final byte LEFT_RIGHT_WALL = 12;
    public static final byte[] WALLS = new byte[]{FRONT_BACK_WALL, UP_DOWN_WALL, LEFT_RIGHT_WALL};

    public static final byte FRONT_PLATE = 13;
    public static final byte TOP_PLATE = 14;
    public static final byte RIGHT_PLATE = 15;
    public static final byte BACK_PLATE = 16;
    public static final byte BOTTOM_PLATE = 17;
    public static final byte LEFT_PLATE = 18;
    public static final byte[] PLATES = new byte[]{FRONT_PLATE, TOP_PLATE, RIGHT_PLATE, BACK_PLATE, BOTTOM_PLATE, LEFT_PLATE};

    public static final byte BOTTOM_FRONT_STAIR = 19;
    public static final byte BOTTOM_BACK_STAIR = 20;
    public static final byte BOTTOM_RIGHT_STAIR = 21;
    public static final byte BOTTOM_LEFT_STAIR = 22;
    public static final byte TOP_FRONT_STAIR = 23;
    public static final byte TOP_BACK_STAIR = 24;
    public static final byte TOP_RIGHT_STAIR = 25;
    public static final byte TOP_LEFT_STAIR = 26;
    public static final byte FRONT_RIGHT_STAIR = 27;
    public static final byte FRONT_LEFT_STAIR = 28;
    public static final byte BACK_RIGHT_STAIR = 29;
    public static final byte BACK_LEFT_STAIR = 30;

    public static final byte PLAYER_HEAD = 31;
    public static final byte CACTUS_TYPE = 32;
    public static final byte AIR_TYPE = 33;
    public static final byte LIQUID_TYPE = 34;
    public static final byte LEAVE_TYPE = 35;
    public static final byte GLASS_TYPE = 36;
    public static final byte TORCH_TYPE = 37;

    public static final byte FRONT_SOCKET = 38;
    public static final byte TOP_SOCKET = 39;
    public static final byte RIGHT_SOCKET = 40;
    public static final byte BACK_SOCKET = 41;
    public static final byte BOTTOM_SOCKET = 42;
    public static final byte LEFT_SOCKET = 43;
    public static final byte[] SOCKETS = new byte[]{FRONT_SOCKET, TOP_SOCKET, RIGHT_SOCKET, BACK_SOCKET, BOTTOM_SOCKET, LEFT_SOCKET};

    public static final byte THICK_BOTTOM_FRONT_STAIR = 44;
    public static final byte THICK_BOTTOM_BACK_STAIR = 45;
    public static final byte THICK_BOTTOM_RIGHT_STAIR = 46;
    public static final byte THICK_BOTTOM_LEFT_STAIR = 47;
    public static final byte THICK_TOP_FRONT_STAIR = 48;
    public static final byte THICK_TOP_BACK_STAIR = 49;
    public static final byte THICK_TOP_RIGHT_STAIR = 50;
    public static final byte THICK_TOP_LEFT_STAIR = 51;
    public static final byte THICK_FRONT_RIGHT_STAIR = 52;
    public static final byte THICK_FRONT_LEFT_STAIR = 53;
    public static final byte THICK_BACK_RIGHT_STAIR = 54;
    public static final byte THICK_BACK_LEFT_STAIR = 55;

    public static final byte THIN_BOTTOM_FRONT_STAIR = 56;
    public static final byte THIN_BOTTOM_BACK_STAIR = 57;
    public static final byte THIN_BOTTOM_RIGHT_STAIR = 58;
    public static final byte THIN_BOTTOM_LEFT_STAIR = 59;
    public static final byte THIN_TOP_FRONT_STAIR = 60;
    public static final byte THIN_TOP_BACK_STAIR = 61;
    public static final byte THIN_TOP_RIGHT_STAIR = 62;
    public static final byte THIN_TOP_LEFT_STAIR = 63;
    public static final byte THIN_FRONT_RIGHT_STAIR = 64;
    public static final byte THIN_FRONT_LEFT_STAIR = 65;
    public static final byte THIN_BACK_RIGHT_STAIR = 66;
    public static final byte THIN_BACK_LEFT_STAIR = 67;

    public static final byte UP_DOWN_FENCE = 68;
    public static final byte UP_DOWN_FENCE_FRONT = 69;
    public static final byte UP_DOWN_FENCE_RIGHT = 70;
    public static final byte UP_DOWN_FENCE_FRONT_RIGHT = 71;
    public static final byte UP_DOWN_FENCE_BACK = 72;
    public static final byte UP_DOWN_FENCE_FRONT_BACK = 73;
    public static final byte UP_DOWN_FENCE_RIGHT_BACK = 74;
    public static final byte UP_DOWN_FENCE_FRONT_RIGHT_BACK = 75;
    public static final byte UP_DOWN_FENCE_LEFT = 76;
    public static final byte UP_DOWN_FENCE_FRONT_LEFT = 77;
    public static final byte UP_DOWN_FENCE_RIGHT_LEFT = 78;
    public static final byte UP_DOWN_FENCE_FRONT_RIGHT_LEFT = 79;
    public static final byte UP_DOWN_FENCE_BACK_LEFT = 80;
    public static final byte UP_DOWN_FENCE_FRONT_BACK_LEFT = 81;
    public static final byte UP_DOWN_FENCE_RIGHT_BACK_LEFT = 82;
    public static final byte UP_DOWN_FENCE_FRONT_RIGHT_BACK_LEFT = 83;

    public static final byte FRONT_BACK_FENCE = 84;
    public static final byte FRONT_BACK_FENCE_UP = 85;
    public static final byte FRONT_BACK_FENCE_RIGHT = 86;
    public static final byte FRONT_BACK_FENCE_UP_RIGHT = 87;
    public static final byte FRONT_BACK_FENCE_DOWN = 88;
    public static final byte FRONT_BACK_FENCE_UP_DOWN = 89;
    public static final byte FRONT_BACK_FENCE_RIGHT_DOWN = 90;
    public static final byte FRONT_BACK_FENCE_UP_RIGHT_DOWN = 91;
    public static final byte FRONT_BACK_FENCE_LEFT = 92;
    public static final byte FRONT_BACK_FENCE_UP_LEFT = 93;
    public static final byte FRONT_BACK_FENCE_RIGHT_LEFT = 94;
    public static final byte FRONT_BACK_FENCE_UP_RIGHT_LEFT = 95;
    public static final byte FRONT_BACK_FENCE_DOWN_LEFT = 96;
    public static final byte FRONT_BACK_FENCE_UP_DOWN_LEFT = 97;
    public static final byte FRONT_BACK_FENCE_RIGHT_DOWN_LEFT = 98;
    public static final byte FRONT_BACK_FENCE_UP_RIGHT_DOWN_LEFT = 99;

    public static final byte LEFT_RIGHT_FENCE = 100;
    public static final byte LEFT_RIGHT_FENCE_FRONT = 101;
    public static final byte LEFT_RIGHT_FENCE_UP = 102;
    public static final byte LEFT_RIGHT_FENCE_FRONT_UP = 103;
    public static final byte LEFT_RIGHT_FENCE_BACK = 104;
    public static final byte LEFT_RIGHT_FENCE_FRONT_BACK = 105;
    public static final byte LEFT_RIGHT_FENCE_UP_BACK = 106;
    public static final byte LEFT_RIGHT_FENCE_FRONT_UP_BACK = 107;
    public static final byte LEFT_RIGHT_FENCE_DOWN = 108;
    public static final byte LEFT_RIGHT_FENCE_FRONT_DOWN = 109;
    public static final byte LEFT_RIGHT_FENCE_UP_DOWN = 110;
    public static final byte LEFT_RIGHT_FENCE_FRONT_UP_DOWN = 111;
    public static final byte LEFT_RIGHT_FENCE_BACK_DOWN = 112;
    public static final byte LEFT_RIGHT_FENCE_FRONT_BACK_DOWN = 113;
    public static final byte LEFT_RIGHT_FENCE_UP_BACK_DOWN = 114;
    public static final byte LEFT_RIGHT_FENCE_FRONT_UP_BACK_DOWN = 115;
    public static final byte[] FENCES = new byte[]{FRONT_BACK_FENCE, UP_DOWN_FENCE, LEFT_RIGHT_FENCE};

    public static final byte FLOWER_TYPE = 116;

    public static final byte[] TO_PLACE_BLOCK_TYPES = new byte[]{FULL_BLOCK, PLAYER_HEAD, BOTTOM_SOCKET, BOTTOM_SLAB, BOTTOM_PLATE, FRONT_BACK_WALL, UP_DOWN_POST, THICK_BOTTOM_BACK_STAIR, BOTTOM_BACK_STAIR, THIN_BOTTOM_BACK_STAIR, UP_DOWN_FENCE_FRONT_RIGHT};
    public static final int BLOCK_TYPE_BITS = 8;
    public static final int BLOCK_TYPE_MASK = (1 << BLOCK_TYPE_BITS) - 1;
    public static final int BASE_BLOCK_MASK = -1 << BLOCK_TYPE_BITS;
    public static final int STANDARD_BLOCKS_THRESHOLD = 1 << BLOCK_TYPE_BITS;

    public static final int TOTAL_AMOUNT_OF_BLOCK_TYPES = 117;

    //Non standard block, aka blocks without blockTypes
    public static final short AIR = 0;
    public static final short OUT_OF_WORLD = 1;
    public static final short WATER = 2;
    public static final short LAVA = 3;
    public static final short CACTUS = 4;
    public static final short FRONT_CREATOR_HEAD = 5;
    public static final short BACK_CREATOR_HEAD = 6;
    public static final short RIGHT_CREATOR_HEAD = 7;
    public static final short LEFT_CREATOR_HEAD = 8;
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
    public static final short[] TO_PLACE_NON_STANDARD_BLOCKS = new short[]{WATER, LAVA, CACTUS, FRONT_CREATOR_HEAD, TORCH, TALL_GRASS, RED_TULIP, YELLOW_TULIP, ORANGE_TULIP, MAGENTA_TULIP, ROSE, HYACINTH, DRISLY, SHRUB, SUGAR_CANE};

    //Standard blocks, aka blocks with blockTypes
    public static final short GRASS = (short) (1 << BLOCK_TYPE_BITS);
    public static final short DIRT = (short) (2 << BLOCK_TYPE_BITS);
    public static final short STONE = (short) (3 << BLOCK_TYPE_BITS);
    public static final short STONE_BRICK = (short) (4 << BLOCK_TYPE_BITS);
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
    public static final short MOSSY_STONE_BRICK = (short) (68 << BLOCK_TYPE_BITS);
    public static final short MOSSY_POLISHED_STONE = (short) (69 << BLOCK_TYPE_BITS);
    public static final short MOSSY_CHISELED_POLISHED_STONE = (short) (70 << BLOCK_TYPE_BITS);
    public static final short MOSSY_CHISELED_STONE = (short) (71 << BLOCK_TYPE_BITS);
    public static final short MOSSY_SLATE = (short) (72 << BLOCK_TYPE_BITS);
    public static final short MOSSY_COBBLED_SLATE = (short) (73 << BLOCK_TYPE_BITS);
    public static final short MOSSY_SLATE_BRICKS = (short) (74 << BLOCK_TYPE_BITS);
    public static final short MOSSY_CHISELED_SLATE = (short) (75 << BLOCK_TYPE_BITS);
    public static final short MOSSY_POLISHED_SLATE = (short) (76 << BLOCK_TYPE_BITS);
    public static final short MOSSY_DIRT = (short) (77 << BLOCK_TYPE_BITS);
    public static final short MOSSY_GRAVEL = (short) (78 << BLOCK_TYPE_BITS);
    public static final short MOSSY_OBSIDIAN = (short) (79 << BLOCK_TYPE_BITS);
    public static final short MOSSY_CRACKED_ANDESITE = (short) (80 << BLOCK_TYPE_BITS);
    public static final short MOSSY_COBBLESTONE = (short) (81 << BLOCK_TYPE_BITS);
    public static final short FRONT_FURNACE = (short) (82 << BLOCK_TYPE_BITS);


    public static final int AMOUNT_OF_TO_PLACE_STANDARD_BLOCKS = 83;

    public static final short FRONT_BACK_OAK_LOG = (short) (511 << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_OAK_LOG = (short) (510 << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_STRIPPED_OAK_LOG = (short) (509 << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_STRIPPED_OAK_LOG = (short) (508 << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_SPRUCE_LOG = (short) (507 << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_SPRUCE_LOG = (short) (506 << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_STRIPPED_SPRUCE_LOG = (short) (505 << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_STRIPPED_SPRUCE_LOG = (short) (504 << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_DARK_OAK_LOG = (short) (503 << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_DARK_OAK_LOG = (short) (502 << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_STRIPPED_DARK_OAK_LOG = (short) (501 << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_STRIPPED_DARK_OAK_LOG = (short) (500 << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_PINE_LOG = (short) (499 << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_PINE_LOG = (short) (498 << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_STRIPPED_PINE_LOG = (short) (497 << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_STRIPPED_PINE_LOG = (short) (496 << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_REDWOOD_LOG = (short) (495 << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_REDWOOD_LOG = (short) (494 << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_STRIPPED_REDWOOD_LOG = (short) (493 << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_STRIPPED_REDWOOD_LOG = (short) (492 << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_BLACK_WOOD_LOG = (short) (491 << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_BLACK_WOOD_LOG = (short) (490 << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_STRIPPED_BLACK_WOOD_LOG = (short) (489 << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_STRIPPED_BLACK_WOOD_LOG = (short) (488 << BLOCK_TYPE_BITS);
    public static final short BACK_FURNACE = (short) (487 << BLOCK_TYPE_BITS);
    public static final short RIGHT_FURNACE = (short) (486 << BLOCK_TYPE_BITS);
    public static final short LEFT_FURNACE = (short) (485 << BLOCK_TYPE_BITS);

    public static final int AMOUNT_OF_STANDARD_BLOCKS = 512;

    //Texture indices
    public static final byte GRASS_TOP_TEXTURE = 1;
    public static final byte DIRT_TEXTURE = 2;
    public static final byte STONE_TEXTURE = 3;
    public static final byte OAK_LOG_TEXTURE = 4;
    public static final byte SPRUCE_LOG_TEXTURE = 5;
    public static final byte DARK_OAK_LOG_TEXTURE = 6;
    public static final byte GRASS_SIDE_TEXTURE = 17;
    public static final byte MUD_TEXTURE = 18;
    public static final byte ANDESITE_TEXTURE = 19;
    public static final byte OAK_LOG_TOP_TEXTURE = 20;
    public static final byte SPRUCE_LOG_TOP_TEXTURE = 21;
    public static final byte DARK_OAK_LOG_TOP_TEXTURE = 22;
    public static final byte SNOW_TEXTURE = 33;
    public static final byte SAND_TEXTURE = 34;
    public static final byte STONE_BRICK_TEXTURE = 35;
    public static final byte STRIPPED_OAK_LOG_TEXTURE = 36;
    public static final byte STRIPPED_SPRUCE_LOG_TEXTURE = 37;
    public static final byte STRIPPED_DARK_OAK_LOG_TEXTURE = 38;
    public static final byte SLATE_TEXTURE = 49;
    public static final byte GLASS_TEXTURE = 50;
    public static final byte COBBLESTONE_TEXTURE = 51;
    public static final byte STRIPPED_OAK_LOG_TOP_TEXTURE = 52;
    public static final byte STRIPPED_SPRUCE_LOG_TOP_TEXTURE = 53;
    public static final byte STRIPPED_DARK_OAK_LOG_TOP_TEXTURE = 54;
    public static final byte WATER_TEXTURE = 65;
    public static final byte GRAVEL_TEXTURE = 66;
    public static final byte POLISHED_STONE_TEXTURE = 67;
    public static final byte OAK_PLANKS_TEXTURE = 68;
    public static final byte SPRUCE_PLANKS_TEXTURE = 69;
    public static final byte DARK_OAK_PLANKS_TEXTURE = 70;
    public static final byte COURSE_DIRT_TEXTURE = 81;
    public static final byte CHISELED_STONE_TEXTURE = 82;
    public static final byte CHISELED_POLISHED_STONE_TEXTURE = 83;
    public static final byte OAK_LEAVES_TEXTURE = 84;
    public static final byte SPRUCE_LEAVES_TEXTURE = 85;
    public static final byte DARK_OAK_LEAVES_TEXTURE = 86;
    public static final byte ICE_TEXTURE = 97;
    public static final byte CLAY_TEXTURE = 98;
    public static final byte MOSS_TEXTURE = 99;
    public static final byte ROTATED_OAK_LOG_TEXTURE = 100;
    public static final byte ROTATED_SPRUCE_LOG_TEXTURE = 101;
    public static final byte ROTATED_DARK_OAK_LOG_TEXTURE = 102;
    public static final byte HEAVY_ICE_TEXTURE = 113;
    public static final byte CACTUS_SIDE_TEXTURE = 114;
    public static final byte ROTATED_STRIPPED_OAK_LOG_TEXTURE = 116;
    public static final byte ROTATED_STRIPPED_SPRUCE_LOG_TEXTURE = 117;
    public static final byte ROTATED_STRIPPED_DARK_OAK_LOG_TEXTURE = 118;
    public static final byte CHISELED_SLATE_TEXTURE = -127;
    public static final byte LAVA_TEXTURE = -126;
    public static final byte CREATOR_HEAD_RIGHT_TEXTURE = -122;
    public static final byte CREATOR_HEAD_FRONT_TEXTURE = -123;
    public static final byte CREATOR_HEAD_LEFT_TEXTURE = -124;
    public static final byte COAL_ORE_TEXTURE = -111;
    public static final byte IRON_ORE_TEXTURE = -110;
    public static final byte DIAMOND_ORE_TEXTURE = -109;
    public static final byte CREATOR_HEAD_TOP_TEXTURE = -108;
    public static final byte CREATOR_HEAD_BACK_TEXTURE = -107;
    public static final byte CREATOR_HEAD_BOTTOM_TEXTURE = -106;
    public static final byte ROTATED_CREATOR_HEAD_BOTTOM_TEXTURE = -90;
    public static final byte CACTUS_TOP_TEXTURE = -91;
    public static final byte RED_TEXTURE = -1;
    public static final byte GREEN_TEXTURE = -2;
    public static final byte BLUE_TEXTURE = -3;
    public static final byte YELLOW_TEXTURE = -4;
    public static final byte MAGENTA_TEXTURE = -5;
    public static final byte CYAN_TEXTURE = -6;
    public static final byte WHITE_TEXTURE = -7;
    public static final byte BLACK_TEXTURE = -8;
    public static final byte EXPLOSION_TEXTURE = -9;
    public static final byte COBBLED_SLATE_TEXTURE = 115;
    public static final byte SLATE_BRICKS_TEXTURE = -125;
    public static final byte POLISHED_SANDSTONE_TEXTURE = -93;
    public static final byte SANDSTONE_TEXTURE = -94;
    public static final byte POLISHED_SLATE_TEXTURE = -95;
    public static final byte CRACKED_ANDESITE_TEXTURE = -92;
    public static final byte PINE_LOG_TEXTURE = 7;
    public static final byte PINE_LOG_TOP_TEXTURE = 23;
    public static final byte STRIPPED_PINE_LOG_TEXTURE = 39;
    public static final byte STRIPPED_PINE_LOG_TOP_TEXTURE = 55;
    public static final byte PINE_PLANKS_TEXTURE = 71;
    public static final byte PINE_LEAVES_TEXTURE = 87;
    public static final byte ROTATED_PINE_LOG_TEXTURE = 103;
    public static final byte ROTATED_STRIPPED_PINE_LOG_TEXTURE = 119;
    public static final byte REDWOOD_LOG_TEXTURE = 8;
    public static final byte REDWOOD_LOG_TOP_TEXTURE = 24;
    public static final byte STRIPPED_REDWOOD_LOG_TEXTURE = 40;
    public static final byte STRIPPED_REDWOOD_LOG_TOP_TEXTURE = 56;
    public static final byte REDWOOD_PLANKS_TEXTURE = 72;
    public static final byte REDWOOD_LEAVES_TEXTURE = 88;
    public static final byte ROTATED_REDWOOD_LOG_TEXTURE = 104;
    public static final byte ROTATED_STRIPPED_REDWOOD_LOG_TEXTURE = 120;
    public static final byte BLACK_WOOD_LOG_TEXTURE = 9;
    public static final byte BLACK_WOOD_LOG_TOP_TEXTURE = 25;
    public static final byte STRIPPED_BLACK_WOOD_LOG_TEXTURE = 41;
    public static final byte STRIPPED_BLACK_WOOD_LOG_TOP_TEXTURE = 57;
    public static final byte BLACK_WOOD_PLANKS_TEXTURE = 73;
    public static final byte BLACK_WOOD_LEAVES_TEXTURE = 89;
    public static final byte ROTATED_BLACK_WOOD_LOG_TEXTURE = 105;
    public static final byte ROTATED_STRIPPED_BLACK_WOOD_LOG_TEXTURE = 121;
    public static final byte TNT_TOP_TEXTURE = -121;
    public static final byte FURNACE_TOP_TEXTURE = -120;
    public static final byte TNT_SIDE_TEXTURE = -105;
    public static final byte FURNACE_SIDE_TEXTURE = -104;
    public static final byte FURNACE_FRONT_TEXTURE = -103;
    public static final byte TNT_BOTTOM_TEXTURE = -89;
    public static final byte FURNACE_BOTTOM_TEXTURE = -88;
    public static final byte TORCH_TEXTURE = -79;
    public static final byte CRAFTING_TABLE_TOP_TEXTURE = -78;
    public static final byte CRAFTING_TABLE_SIDE_TEXTURE_1 = -77;
    public static final byte CRAFTING_TABLE_SIDE_TEXTURE_2 = -76;
    public static final byte OBSIDIAN_TEXTURE = -75;
    //    public static final byte WINE_TEXTURE = -74;
    public static final byte MOSSY_COBBLESTONE_TEXTURE = -31;
    public static final byte MOSSY_CRACKED_ANDESITE_TEXTURE = -30;
    public static final byte MOSSY_OBSIDIAN_TEXTURE = -29;
    public static final byte MOSSY_GRAVEL_TEXTURE = -28;
    public static final byte MOSSY_DIRT_TEXTURE = -27;
    public static final byte MOSSY_POLISHED_SLATE_TEXTURE = -26;
    public static final byte MOSSY_CHISELED_SLATE_TEXTURE = -25;
    public static final byte MOSSY_SLATE_BRICKS_TEXTURE = -24;
    public static final byte MOSSY_COBBLED_SLATE_TEXTURE = -23;
    public static final byte MOSSY_SLATE_TEXTURE = -22;
    public static final byte MOSSY_CHISELED_STONE_TEXTURE = -21;
    public static final byte MOSSY_CHISELED_POLISHED_STONE_TEXTURE = -20;
    public static final byte MOSSY_POLISHED_STONE_TEXTURE = -19;
    public static final byte MOSSY_STONE_BRICK_TEXTURE = -18;
    public static final byte MOSSY_ANDESITE_TEXTURE = -17;
    public static final byte MOSSY_STONE_TEXTURE = -16;
    public static final byte TALL_GRASS_TEXTURE = -63;
    public static final byte RED_TULIP_TEXTURE = -62;
    public static final byte YELLOW_TULIP_TEXTURE = -61;
    public static final byte ORANGE_TULIP_TEXTURE = -60;
    public static final byte MAGENTA_TULIP_TEXTURE = -59;
    public static final byte ROSE_TEXTURE = -58;
    public static final byte HYACINTH_TEXTURE = -57;
    public static final byte DRISLY_TEXTURE = -56;
    public static final byte SHRUB_TEXTURE = -55;
    public static final byte SUGAR_CANE_TEXTURE = -54;

    //Just pretend it doesn't exist
    public static final float[] SKY_BOX_VERTICES;

    public static final int[] SKY_BOX_INDICES;

    public static final float[] SKY_BOX_TEXTURE_COORDINATES;

    public static final float[] GUI_ELEMENT_TEXTURE_COORDINATES;

    public static final float[] OVERLAY_VERTICES;

    public static final short[][][] OAK_TREE;

    public static final short[][][] SPRUCE_TREE;

    public static final short[][][] DARK_OAK_TREE;

    public static final byte[] OCCLUSION_CULLING_LARGER_SIDE_OFFSETS = new byte[]{0, 1, 3, 6, 10};

    //No like actually, this doesn't exist! Trust me. please...
    static {
        //NOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
        //I WARNED YOU!!!
        //WHY DIDN'T YOU LISTEN!??!!?

        OAK_TREE = new short[][][]{{{AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, DIRT, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, UP_DOWN_OAK_LOG, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, UP_DOWN_OAK_LOG, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, AIR}, {OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES}, {OAK_LEAVES, OAK_LEAVES, UP_DOWN_OAK_LOG, OAK_LEAVES, OAK_LEAVES}, {OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES}, {AIR, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, AIR}}, {

                {AIR, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, AIR}, {AIR, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES}, {OAK_LEAVES, OAK_LEAVES, UP_DOWN_OAK_LOG, OAK_LEAVES, OAK_LEAVES}, {OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, AIR}, {AIR, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, AIR}}, {

                {AIR, AIR, OAK_LEAVES, AIR, AIR}, {AIR, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, AIR}, {OAK_LEAVES, OAK_LEAVES, UP_DOWN_OAK_LOG, OAK_LEAVES, AIR}, {AIR, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, AIR}, {AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR}, {AIR, OAK_LEAVES, OAK_LEAVES, AIR, AIR}, {AIR, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, AIR}, {AIR, AIR, OAK_LEAVES, OAK_LEAVES, AIR}, {AIR, AIR, AIR, AIR, AIR}}};

        SPRUCE_TREE = new short[][][]{{{AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, DIRT, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, UP_DOWN_SPRUCE_LOG, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, UP_DOWN_SPRUCE_LOG, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR}, {SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES}, {SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES}, {SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, UP_DOWN_SPRUCE_LOG, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES}, {SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES}, {SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES}, {AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, SPRUCE_LEAVES, AIR, AIR, AIR}, {AIR, AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR, AIR}, {AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, UP_DOWN_SPRUCE_LOG, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR}, {AIR, AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR, AIR}, {AIR, AIR, AIR, SPRUCE_LEAVES, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR, AIR}, {AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR}, {SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES}, {SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, UP_DOWN_SPRUCE_LOG, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES}, {SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES}, {AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR}, {AIR, AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, SPRUCE_LEAVES, AIR, AIR, AIR}, {AIR, AIR, SPRUCE_LEAVES, UP_DOWN_SPRUCE_LOG, SPRUCE_LEAVES, AIR, AIR}, {AIR, AIR, AIR, SPRUCE_LEAVES, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR, AIR}, {AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR}, {AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, UP_DOWN_SPRUCE_LOG, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR}, {AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR}, {AIR, AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, SPRUCE_LEAVES, AIR, AIR, AIR}, {AIR, AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR, AIR}, {AIR, AIR, AIR, SPRUCE_LEAVES, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, SPRUCE_LEAVES, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}};

        DARK_OAK_TREE = new short[][][]{{{AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, DIRT, DIRT, AIR, AIR, AIR}, {AIR, AIR, DIRT, DIRT, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, UP_DOWN_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, AIR, AIR, AIR}, {AIR, AIR, UP_DOWN_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, UP_DOWN_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, AIR, AIR, AIR}, {AIR, AIR, UP_DOWN_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR, AIR}, {DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR}, {DARK_OAK_LEAVES, UP_DOWN_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR}, {DARK_OAK_LEAVES, DARK_OAK_LEAVES, UP_DOWN_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR}, {AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, UP_DOWN_DARK_OAK_LOG, DARK_OAK_LEAVES, AIR, AIR}, {AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR}, {DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, UP_DOWN_DARK_OAK_LOG, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR}, {DARK_OAK_LEAVES, UP_DOWN_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR}, {DARK_OAK_LEAVES, DARK_OAK_LEAVES, UP_DOWN_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR}, {DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, UP_DOWN_DARK_OAK_LOG, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR}, {AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR}, {AIR, AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR, AIR}}, {
                {AIR, AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR, AIR}, {AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, UP_DOWN_DARK_OAK_LOG, DARK_OAK_LEAVES, AIR, AIR}, {DARK_OAK_LEAVES, UP_DOWN_DARK_OAK_LOG, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR}, {DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR}, {AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, UP_DOWN_DARK_OAK_LOG, DARK_OAK_LEAVES, AIR, AIR}, {AIR, AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR, AIR}, {AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR}, {AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR}, {AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR}, {AIR, AIR, AIR, DARK_OAK_LEAVES, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR, AIR}, {AIR, AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}, {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}};

        SKY_BOX_VERTICES = new float[]{
                -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f,
                1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f,
                -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f,
                -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
        SKY_BOX_INDICES = new int[]{
                0, 2, 1, 3, 1, 2,
                4, 5, 6, 7, 6, 5,
                8, 9, 10, 11, 10, 9,
                12, 14, 13, 15, 13, 14,
                16, 18, 17, 19, 17, 18,
                20, 21, 22, 23, 22, 21};
        SKY_BOX_TEXTURE_COORDINATES = new float[]{
                1.0f, 2 / 3f, 0.75f, 2 / 3f, 1.0f, 1 / 3f, 0.75f, 1 / 3f,
                0.25f, 2 / 3f, 0.5f, 2 / 3f, 0.25f, 1 / 3f, 0.5f, 1 / 3f,
                0.25f, 1.0f, 0.5f, 1.0f, 0.25f, 2 / 3f, 0.5f, 2 / 3f,
                0.25f, 0.0f, 0.5f, 0.0f, 0.25f, 1 / 3f, 0.5f, 1 / 3f,
                0.0f, 2 / 3f, 0.0f, 1 / 3f, 0.25f, 2 / 3f, 0.25f, 1 / 3f,
                0.75f, 2 / 3f, 0.75f, 1 / 3f, 0.5f, 2 / 3f, 0.5f, 1 / 3f};
        GUI_ELEMENT_TEXTURE_COORDINATES = new float[]{
                0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f};
        OVERLAY_VERTICES = new float[]{
                -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f,
                -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f};
    }
}
