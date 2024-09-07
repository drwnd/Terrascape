package com.MBEv2.core.utils;

public class Constants {

    //Recommended to not change, but I can't stop you
    public static final String TITLE = "Minecräft Bad Edition v2";
    public static final float Z_NEAR = 0.01f;
    public static final float Z_FAR = 1000.0f;
    public static final float TIME_SPEED = 0.00008333f;

    //DO NOT CHANGE THESE VALUES (like really, it will crash)
    public static final int CHUNK_SIZE_BITS = 5;
    public static final int CHUNK_SIZE = 1 << CHUNK_SIZE_BITS;
    public static final int CHUNK_SIZE_MASK = CHUNK_SIZE - 1;
    public static final int MAX_CHUNKS_XZ = 0x7FFFFFF;
    public static final int MAX_CHUNKS_Y = 0x3FF;

    //Other useful stuff
    public static final int MAX_BLOCK_LIGHT_VALUE = 15;
    public static final int MAX_SKY_LIGHT_VALUE = 15;

    //Change based on computing power
    public static final int MAX_CHUNKS_TO_BUFFER_PER_FRAME = 5;

    public static final int NUMBER_OF_GENERATION_THREADS = 4;
    public static final int MAX_OCCLUSION_CULLING_DAMPER = 6;

    //Indices for the sides of blocks
    public static final int FRONT = 0;
    public static final int TOP = 1;
    public static final int RIGHT = 2;
    public static final int BACK = 3;
    public static final int BOTTOM = 4;
    public static final int LEFT = 5;
    public static final int NONE = 6;

    //OCCLUSION_DATA
    public static final int OCCLUDES_ALL = 0;
    public static final int OCCLUDES_SELF = 1;
    public static final int OCCLUDES_DYNAMIC_ALL = 2;
    public static final int OCCLUDES_DYNAMIC_SELF = 3;

    //BLOCK_DATA
//    public static final int SOLID_MASK = 64;
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

    //Indices for information on block types
    public static final int FULL_BLOCK = 0;

    public static final int BOTTOM_SLAB = 1;
    public static final int TOP_SLAB = 2;
    public static final int FRONT_SLAB = 3;
    public static final int BACK_SLAB = 4;
    public static final int RIGHT_SLAB = 5;
    public static final int LEFT_SLAB = 6;
    public static final int[] SLABS = new int[]{FRONT_SLAB, TOP_SLAB, RIGHT_SLAB, BACK_SLAB, BOTTOM_SLAB, LEFT_SLAB};

    public static final int UP_DOWN_POST = 7;
    public static final int FRONT_BACK_POST = 8;
    public static final int LEFT_RIGHT_POST = 9;
    public static final int[] POSTS = new int[]{FRONT_BACK_POST, UP_DOWN_POST, LEFT_RIGHT_POST};

    public static final int UP_DOWN_WALL = 10;
    public static final int FRONT_BACK_WALL = 11;
    public static final int LEFT_RIGHT_WALL = 12;
    public static final int[] WALLS = new int[]{FRONT_BACK_WALL, UP_DOWN_WALL, LEFT_RIGHT_WALL};

    public static final int FRONT_PLATE = 13;
    public static final int TOP_PLATE = 14;
    public static final int RIGHT_PLATE = 15;
    public static final int BACK_PLATE = 16;
    public static final int BOTTOM_PLATE = 17;
    public static final int LEFT_PLATE = 18;
    public static final int[] PLATES = new int[]{FRONT_PLATE, TOP_PLATE, RIGHT_PLATE, BACK_PLATE, BOTTOM_PLATE, LEFT_PLATE};

    public static final int BOTTOM_FRONT_STAIR = 19;
    public static final int BOTTOM_BACK_STAIR = 20;
    public static final int BOTTOM_RIGHT_STAIR = 21;
    public static final int BOTTOM_LEFT_STAIR = 22;
    public static final int TOP_FRONT_STAIR = 23;
    public static final int TOP_BACK_STAIR = 24;
    public static final int TOP_RIGHT_STAIR = 25;
    public static final int TOP_LEFT_STAIR = 26;
    public static final int FRONT_RIGHT_STAIR = 27;
    public static final int FRONT_LEFT_STAIR = 28;
    public static final int BACK_RIGHT_STAIR = 29;
    public static final int BACK_LEFT_STAIR = 30;

    public static final int PLAYER_HEAD = 31;

    public static final int[] TO_PLACE_BLOCK_TYPES = new int[]{FULL_BLOCK, PLAYER_HEAD, BOTTOM_SLAB, BOTTOM_PLATE, FRONT_BACK_WALL, UP_DOWN_POST, BOTTOM_BACK_STAIR};

    public static final int CACTUS_TYPE = 32;
    public static final int AIR_TYPE = 33;
    public static final int LIQUID_TYPE = 34;
    public static final int LEAVE_TYPE = 35;
    public static final int GLASS_TYPE = 36;
    public static final int TORCH_TYPE = 37;

    public static final int BLOCK_TYPE_BITS = 6;
    public static final int BLOCK_TYPE_MASK = (1 << BLOCK_TYPE_BITS) - 1;
    public static final int BASE_BLOCK_MASK = -1 << BLOCK_TYPE_BITS;
    public static final int STANDARD_BLOCKS_THRESHOLD = 1 << BLOCK_TYPE_BITS;

    public static final int TOTAL_AMOUNT_OF_BLOCK_TYPES = 38;

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
    public static final short[] TO_PLACE_NON_STANDARD_BLOCKS = new short[]{WATER, LAVA, CACTUS, FRONT_CREATOR_HEAD, TORCH};

    //Standard blocks, aka blocks with blockTypes
    private static int b = 1;
    public static final short GRASS = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short DIRT = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short STONE = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short STONE_BRICK = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short COBBLESTONE = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short CHISELED_STONE = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short POLISHED_STONE = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short CHISELED_POLISHED_STONE = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short MUD = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short ANDESITE = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short SNOW = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short SAND = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short SANDSTONE = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short POLISHED_SANDSTONE = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short SLATE = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short CHISELED_SLATE = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short COBBLED_SLATE = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short SLATE_BRICKS = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short POLISHED_SLATE = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short GLASS = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short GRAVEL = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short COURSE_DIRT = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short CLAY = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short MOSS = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short ICE = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short HEAVY_ICE = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short COAL_ORE = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short IRON_ORE = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short DIAMOND_ORE = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_OAK_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_STRIPPED_OAK_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_SPRUCE_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_STRIPPED_SPRUCE_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_DARK_OAK_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_STRIPPED_DARK_OAK_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_PINE_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_STRIPPED_PINE_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_REDWOOD_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_STRIPPED_REDWOOD_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_BLACK_WOOD_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short UP_DOWN_STRIPPED_BLACK_WOOD_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short OAK_LEAVES = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short SPRUCE_LEAVES = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short DARK_OAK_LEAVES = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short PINE_LEAVES = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short REDWOOD_LEAVES = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short BLACK_WOOD_LEAVES = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short OAK_PLANKS = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short SPRUCE_PLANKS = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short DARK_OAK_PLANKS = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short PINE_PLANKS = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short REDWOOD_PLANKS = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short BLACK_WOOD_PLANKS = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short CRACKED_ANDESITE = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short BLACK = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short WHITE = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short CYAN = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short MAGENTA = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short YELLOW = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short BLUE = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short GREEN = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short RED = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short CRAFTING_TABLE = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short TNT = (short) (b++ << BLOCK_TYPE_BITS);

    public static final int AMOUNT_OF_TO_PLACE_STANDARD_BLOCKS = b;
    private static int b2 = 511;

    public static final short FRONT_BACK_OAK_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_OAK_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_STRIPPED_OAK_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_STRIPPED_OAK_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_SPRUCE_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_SPRUCE_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_STRIPPED_SPRUCE_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_STRIPPED_SPRUCE_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_DARK_OAK_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_DARK_OAK_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_STRIPPED_DARK_OAK_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_STRIPPED_DARK_OAK_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_PINE_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_PINE_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_STRIPPED_PINE_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_STRIPPED_PINE_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_REDWOOD_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_REDWOOD_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_STRIPPED_REDWOOD_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_STRIPPED_REDWOOD_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_BLACK_WOOD_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_BLACK_WOOD_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_STRIPPED_BLACK_WOOD_LOG = (short) (b2-- << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_STRIPPED_BLACK_WOOD_LOG = (short) (b2-- << BLOCK_TYPE_BITS);

    public static final int AMOUNT_OF_STANDARD_BLOCKS = 1024;

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
    public static final byte TNT_SIDE_TEXTURE = -105;
    public static final byte TNT_BOTTOM_TEXTURE = -89;
    public static final byte TORCH_TEXTURE = -79;
    public static final byte CRAFTING_TABLE_TOP_TEXTURE = -78;
    public static final byte CRAFTING_TABLE_SIDE_TEXTURE_1 = -77;
    public static final byte CRAFTING_TABLE_SIDE_TEXTURE_2 = -76;

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

        OAK_TREE = new short[][][]{{
                {AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, DIRT, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, UP_DOWN_OAK_LOG, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, UP_DOWN_OAK_LOG, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, AIR},
                {OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES},
                {OAK_LEAVES, OAK_LEAVES, UP_DOWN_OAK_LOG, OAK_LEAVES, OAK_LEAVES},
                {OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES},
                {AIR, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, AIR}}, {

                {AIR, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, AIR},
                {AIR, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES},
                {OAK_LEAVES, OAK_LEAVES, UP_DOWN_OAK_LOG, OAK_LEAVES, OAK_LEAVES},
                {OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, AIR},
                {AIR, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, AIR}}, {

                {AIR, AIR, OAK_LEAVES, AIR, AIR},
                {AIR, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, AIR},
                {OAK_LEAVES, OAK_LEAVES, UP_DOWN_OAK_LOG, OAK_LEAVES, AIR},
                {AIR, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, AIR},
                {AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR},
                {AIR, OAK_LEAVES, OAK_LEAVES, AIR, AIR},
                {AIR, OAK_LEAVES, OAK_LEAVES, OAK_LEAVES, AIR},
                {AIR, AIR, OAK_LEAVES, OAK_LEAVES, AIR},
                {AIR, AIR, AIR, AIR, AIR}}};

        SPRUCE_TREE = new short[][][]{{
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, DIRT, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, UP_DOWN_SPRUCE_LOG, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, UP_DOWN_SPRUCE_LOG, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR},
                {SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES},
                {SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES},
                {SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, UP_DOWN_SPRUCE_LOG, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES},
                {SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES},
                {SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES},
                {AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, SPRUCE_LEAVES, AIR, AIR, AIR},
                {AIR, AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR, AIR},
                {AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, UP_DOWN_SPRUCE_LOG, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR},
                {AIR, AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR, AIR},
                {AIR, AIR, AIR, SPRUCE_LEAVES, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR, AIR},
                {AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR},
                {SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES},
                {SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, UP_DOWN_SPRUCE_LOG, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES},
                {SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES},
                {AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR},
                {AIR, AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, SPRUCE_LEAVES, AIR, AIR, AIR},
                {AIR, AIR, SPRUCE_LEAVES, UP_DOWN_SPRUCE_LOG, SPRUCE_LEAVES, AIR, AIR},
                {AIR, AIR, AIR, SPRUCE_LEAVES, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR, AIR},
                {AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR},
                {AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, UP_DOWN_SPRUCE_LOG, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR},
                {AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR},
                {AIR, AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, SPRUCE_LEAVES, AIR, AIR, AIR},
                {AIR, AIR, SPRUCE_LEAVES, SPRUCE_LEAVES, SPRUCE_LEAVES, AIR, AIR},
                {AIR, AIR, AIR, SPRUCE_LEAVES, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, SPRUCE_LEAVES, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}};

        DARK_OAK_TREE = new short[][][]{{
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, DIRT, DIRT, AIR, AIR, AIR},
                {AIR, AIR, DIRT, DIRT, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, UP_DOWN_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, AIR, AIR, AIR},
                {AIR, AIR, UP_DOWN_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, UP_DOWN_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, AIR, AIR, AIR},
                {AIR, AIR, UP_DOWN_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR, AIR},
                {DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR},
                {DARK_OAK_LEAVES, UP_DOWN_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR},
                {DARK_OAK_LEAVES, DARK_OAK_LEAVES, UP_DOWN_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR},
                {AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, UP_DOWN_DARK_OAK_LOG, DARK_OAK_LEAVES, AIR, AIR},
                {AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR},
                {DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, UP_DOWN_DARK_OAK_LOG, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR},
                {DARK_OAK_LEAVES, UP_DOWN_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR},
                {DARK_OAK_LEAVES, DARK_OAK_LEAVES, UP_DOWN_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR},
                {DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, UP_DOWN_DARK_OAK_LOG, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR},
                {AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR},
                {AIR, AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR, AIR}}, {

                {AIR, AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR, AIR},
                {AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, UP_DOWN_DARK_OAK_LOG, DARK_OAK_LEAVES, AIR, AIR},
                {DARK_OAK_LEAVES, UP_DOWN_DARK_OAK_LOG, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR},
                {DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR},
                {AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, UP_DOWN_DARK_OAK_LOG, DARK_OAK_LEAVES, AIR, AIR},
                {AIR, AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR, AIR},
                {AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR},
                {AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR},
                {AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR},
                {AIR, AIR, AIR, DARK_OAK_LEAVES, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}, {

                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR, AIR},
                {AIR, AIR, DARK_OAK_LEAVES, DARK_OAK_LEAVES, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR},
                {AIR, AIR, AIR, AIR, AIR, AIR, AIR}}};

        SKY_BOX_VERTICES = new float[]{
                -1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f, 1.0f,
                -1.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, 1.0f,

                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, 1.0f,
                1.0f, 1.0f, -1.0f,
                1.0f, 1.0f, 1.0f,

                -1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, 1.0f,

                -1.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, -1.0f,
                1.0f, 1.0f, 1.0f,

                -1.0f, -1.0f, -1.0f,
                -1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, 1.0f, -1.0f,

                -1.0f, -1.0f, 1.0f,
                -1.0f, 1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                1.0f, 1.0f, 1.0f
        };
        SKY_BOX_INDICES = new int[]{
                0, 2, 1,
                3, 1, 2,

                4, 5, 6,
                7, 6, 5,

                8, 9, 10,
                11, 10, 9,

                12, 14, 13,
                15, 13, 14,

                16, 18, 17,
                19, 17, 18,

                20, 21, 22,
                23, 22, 21
        };
        SKY_BOX_TEXTURE_COORDINATES = new float[]{
                1.0f, 2 / 3f,
                0.75f, 2 / 3f,
                1.0f, 1 / 3f,
                0.75f, 1 / 3f,

                0.25f, 2 / 3f,
                0.5f, 2 / 3f,
                0.25f, 1 / 3f,
                0.5f, 1 / 3f,

                0.25f, 1.0f,
                0.5f, 1.0f,
                0.25f, 2 / 3f,
                0.5f, 2 / 3f,

                0.25f, 0.0f,
                0.5f, 0.0f,
                0.25f, 1 / 3f,
                0.5f, 1 / 3f,

                0.0f, 2 / 3f,
                0.0f, 1 / 3f,
                0.25f, 2 / 3f,
                0.25f, 1 / 3f,

                0.75f, 2 / 3f,
                0.75f, 1 / 3f,
                0.5f, 2 / 3f,
                0.5f, 1 / 3f
        };
        GUI_ELEMENT_TEXTURE_COORDINATES = new float[]{
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 1.0f,

                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f
        };
        OVERLAY_VERTICES = new float[]{
                -0.5f, 0.5f,
                -0.5f, -0.5f,
                0.5f, 0.5f,

                -0.5f, -0.5f,
                0.5f, -0.5f,
                0.5f, 0.5f
        };
    }
}
