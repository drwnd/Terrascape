package com.MBEv2.core.utils;

import java.util.Random;

public class Constants {

    //Change to whatever you want
    public static final float TIME_SPEED = 0.00008333f;
    public static final float FOV = (float) Math.toRadians(90);
    public static final float GUI_SIZE = 1.0f;
    public static final float MOUSE_SENSITIVITY = 0.040f;
    public static final float REACH = 5.0f;

    //Recommended to not change, but I can't stop you
    public static final String TITLE = "Minecräft Bad Edition v2";
    public static final float Z_NEAR = 0.01f;
    public static final float Z_FAR = -1.0f;

    public static final float HALF_PLAYER_WIDTH = 0.23f;
    public static final float PLAYER_HEAD_OFFSET = 0.08f;
    public static final float[] PLAYER_FEET_OFFSETS = new float[]{1.65f, 1.4f, 0.4f, 0.4f};

    //Player movement
    public static final float AIR_FRICTION = 0.91f;
    public static final float FALL_FRICTION = 0.98f;
    public static final float WATER_FRICTION = 0.4f;
    public static final float GROUND_FRICTION = 0.546f;
    public static final float FLY_FRICTION = 0.8f;

    public static final float MOVEMENT_SPEED = 0.098f;
    public static final float IN_AIR_SPEED = 0.2f;
    public static final float[] MOVEMENT_STATE_SPEED = new float[]{MOVEMENT_SPEED, 0.0294f, MOVEMENT_SPEED * 0.25f};
    public static final float FLY_SPEED = 0.06f;

    public static final float JUMP_STRENGTH = 0.42f;
    public static final float SWIM_STRENGTH = 0.26f;
    public static final float GRAVITY_ACCELERATION = 0.08f;
    public static final float MAX_STEP_HEIGHT = 0.6f;

    //Movement state indices
    public static final int WALKING = 0;
    public static final int CROUCHING = 1;
    public static final int CRAWLING = 2;
    public static final int SWIMMING = 3;

    //DO NOT CHANGE THESE VALUES (like really, it will crash)
    public static final int CHUNK_SIZE_BITS = 5;
    public static final int CHUNK_SIZE = 1 << CHUNK_SIZE_BITS;
    public static final int CHUNK_SIZE_MASK = CHUNK_SIZE - 1;
    public static final int MAX_CHUNKS_XZ = 0x7FFFFFF;
    public static final int MAX_CHUNKS_Y = 0x3FF;
    public static final int MAX_BLOCK_LIGHT_VALUE = 15;
    public static final int MAX_SKY_LIGHT_VALUE = 15;

    //Change based on computing power
    public static final int REACH_ACCURACY = 100;
    public static final int MAX_CHUNKS_TO_BUFFER_PER_FRAME = 5;

    public static final int RENDER_DISTANCE_XZ = 15;
    public static final int RENDER_DISTANCE_Y = 5;

    public static final int RENDERED_WORLD_WIDTH = RENDER_DISTANCE_XZ * 2 + 5;
    public static final int RENDERED_WORLD_HEIGHT = RENDER_DISTANCE_Y * 2 + 5;

    public static final int NUMBER_OF_GENERATION_THREADS = 4;
    public static final int MAX_OCCLUSION_CULLING_DAMPER = 6;

    //World generation
    public static final long SEED = new Random().nextLong();
    //    public static final long SEED = 0;
    public static final int WATER_LEVEL = 96;
    public static final int SNOW_LEVEL = 187;
    public static final int ICE_LEVEL = 237;
    public static final double PLAINS_TREE_THRESHOLD = 0.998;
    public static final double FOREST_TREE_THRESHOLD = 0.95;
    public static final double CACTUS_THRESHOLD = 0.992;
    public static final double WASTELAND_FEATURE_THRESHOLD = 0.999;
    public static final double HEIGHT_MAP_FREQUENCY = 0.01;
    public static final double TEMPERATURE_FREQUENCY = 0.001;
    public static final double HUMIDITY_FREQUENCY = TEMPERATURE_FREQUENCY;
    public static final double EROSION_FREQUENCY = 0.001;

    public static final double MAX_TERRAIN_HEIGHT_DIFFERENCE = 50;

    public static final double MOUNTAIN_THRESHOLD = 0.3;
    public static final double OCEAN_THRESHOLD = -0.3;

    public static final double BLOB_CAVE_CAVE_HEIGHT_BIAS = 0.008;
    public static final double BLOB_CAVE_FREQUENCY = 0.008;
    public static final double BLOB_CAVE_THRESHOLD = 0.3;
    public static final double BLOB_CAVE_MAX_Y = (1 - BLOB_CAVE_THRESHOLD) / BLOB_CAVE_CAVE_HEIGHT_BIAS;

    public static final double NOODLE_CAVE_FREQUENCY = 0.01;
    public static final double NOODLE_CAVE_THRESHOLD = 0.01;
    public static final double NOODLE_CAVE_HEIGHT_BIAS = 0.004;
    public static final double NOODLE_CAVE_MAX_Y = (Math.sqrt(0.5 * NOODLE_CAVE_THRESHOLD) + 1) / NOODLE_CAVE_HEIGHT_BIAS;

    public static final double STONE_TYPE_FREQUENCY = 0.02;
    public static final double ANDESITE_THRESHOLD = 0.1;
    public static final double SLATE_THRESHOLD = 0.7;

    public static final double MUD_TYPE_FREQUENCY = 0.04;
    public static final double GRAVEL_THRESHOLD = 0.1;
    public static final double CLAY_THRESHOLD = 0.5;
    public static final double SAND_THRESHOLD = -0.5;
    public static final double MUD_THRESHOLD = -0.5;

    public static final double DIRT_TYPE_FREQUENCY = 0.05;
    public static final double COURSE_DIRT_THRESHOLD = 0.15;

    public static final double ICE_BERG_FREQUENCY = 0.025;
    public static final double ICE_BERG_THRESHOLD = 0.35;
    public static final double ICE_BERG_HEIGHT = 30;
    public static final double ICE_PLANE_THRESHOLD = 0.0;

    public static final double ICE_TYPE_FREQUENCY = 0.08;
    public static final double HEAVY_ICE_THRESHOLD = 0.6;

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
    public static final int SOLID_MASK = 64;
    public static final int DYNAMIC_SHAPE_MASK = 128;

    //BLOCK_XYZ_SUB_DATA
    public static final int MIN_X = 0;
    public static final int MAX_X = 1;
    public static final int MIN_Y = 2;
    public static final int MAX_Y = 3;
    public static final int MIN_Z = 4;
    public static final int MAX_Z = 5;

    //BLOCK_PROPERTIES
    public static final int LIGHT_EMITTING_MASK = 1;

    //Indices for information on block types
    private static int bT = 0;
    public static final int FULL_BLOCK = bT++;

    public static final int BOTTOM_SLAB = bT++;
    public static final int TOP_SLAB = bT++;
    public static final int FRONT_SLAB = bT++;
    public static final int BACK_SLAB = bT++;
    public static final int RIGHT_SLAB = bT++;
    public static final int LEFT_SLAB = bT++;
    public static final int[] SLABS = new int[]{FRONT_SLAB, TOP_SLAB, RIGHT_SLAB, BACK_SLAB, BOTTOM_SLAB, LEFT_SLAB};

    public static final int UP_DOWN_POST = bT++;
    public static final int FRONT_BACK_POST = bT++;
    public static final int LEFT_RIGHT_POST = bT++;
    public static final int[] POSTS = new int[]{FRONT_BACK_POST, UP_DOWN_POST, LEFT_RIGHT_POST};

    public static final int UP_DOWN_WALL = bT++;
    public static final int FRONT_BACK_WALL = bT++;
    public static final int LEFT_RIGHT_WALL = bT++;
    public static final int[] WALLS = new int[]{FRONT_BACK_WALL, UP_DOWN_WALL, LEFT_RIGHT_WALL};

    public static final int FRONT_PLATE = bT++;
    public static final int TOP_PLATE = bT++;
    public static final int RIGHT_PLATE = bT++;
    public static final int BACK_PLATE = bT++;
    public static final int BOTTOM_PLATE = bT++;
    public static final int LEFT_PLATE = bT++;
    public static final int[] PLATES = new int[]{FRONT_PLATE, TOP_PLATE, RIGHT_PLATE, BACK_PLATE, BOTTOM_PLATE, LEFT_PLATE};

    public static final int BOTTOM_FRONT_STAIR = bT++;
    public static final int BOTTOM_BACK_STAIR = bT++;
    public static final int BOTTOM_RIGHT_STAIR = bT++;
    public static final int BOTTOM_LEFT_STAIR = bT++;
    public static final int TOP_FRONT_STAIR = bT++;
    public static final int TOP_BACK_STAIR = bT++;
    public static final int TOP_RIGHT_STAIR = bT++;
    public static final int TOP_LEFT_STAIR = bT++;
    public static final int FRONT_RIGHT_STAIR = bT++;
    public static final int FRONT_LEFT_STAIR = bT++;
    public static final int BACK_RIGHT_STAIR = bT++;
    public static final int BACK_LEFT_STAIR = bT++;

    public static final int PLAYER_HEAD = bT++;

    public static final int[] TO_PLACE_BLOCK_TYPES = new int[]{FULL_BLOCK, PLAYER_HEAD, BOTTOM_SLAB, BOTTOM_PLATE, FRONT_BACK_WALL, UP_DOWN_POST, BOTTOM_BACK_STAIR};

    public static final int CACTUS_TYPE = bT++;
    public static final int AIR_TYPE = bT++;
    public static final int WATER_TYPE = bT++;
    public static final int LEAVE_TYPE = bT++;
    public static final int GLASS_TYPE = bT++;

    public static final int BLOCK_TYPE_BITS = 6;
    public static final int BLOCK_TYPE_MASK = (1 << BLOCK_TYPE_BITS) - 1;
    public static final int BASE_BLOCK_MASK = -1 << BLOCK_TYPE_BITS;
    public static final int STANDARD_BLOCKS_THRESHOLD = 1 << BLOCK_TYPE_BITS;

    public static final int TOTAL_AMOUNT_OF_BLOCK_TYPES = bT;

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
    public static final short[] TO_PLACE_NON_STANDARD_BLOCKS = new short[]{WATER, LAVA, CACTUS, FRONT_CREATOR_HEAD};

    //Standard blocks, aka blocks with blockTypes
    private static short b = 1;
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

    public static final int AMOUNT_OF_TO_PLACE_STANDARD_BLOCKS = b;

    public static final short FRONT_BACK_OAK_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_OAK_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_STRIPPED_OAK_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_STRIPPED_OAK_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_SPRUCE_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_SPRUCE_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_STRIPPED_SPRUCE_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_STRIPPED_SPRUCE_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_DARK_OAK_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_DARK_OAK_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_STRIPPED_DARK_OAK_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_STRIPPED_DARK_OAK_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_PINE_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_PINE_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_STRIPPED_PINE_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_STRIPPED_PINE_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_REDWOOD_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_REDWOOD_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_STRIPPED_REDWOOD_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_STRIPPED_REDWOOD_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_BLACK_WOOD_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_BLACK_WOOD_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short FRONT_BACK_STRIPPED_BLACK_WOOD_LOG = (short) (b++ << BLOCK_TYPE_BITS);
    public static final short LEFT_RIGHT_STRIPPED_BLACK_WOOD_LOG = (short) (b++ << BLOCK_TYPE_BITS);

    public static final int AMOUNT_OF_STANDARD_BLOCKS = b;


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
    public static final byte CREATOR_HEAD_RIGHT_TEXTURE = -124;
    public static final byte CREATOR_HEAD_FRONT_TEXTURE = -123;
    public static final byte CREATOR_HEAD_LEFT_TEXTURE = -122;
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
