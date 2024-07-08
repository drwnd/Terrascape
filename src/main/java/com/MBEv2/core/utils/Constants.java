package com.MBEv2.core.utils;

public class Constants {

    //Change to whatever you want
    public static final float TIME_SPEED = 0.000025f;
    public static final float FOV = (float) Math.toRadians(90);
    public static final float GUI_SIZE = 1.0f;
    public static final float MOUSE_SENSITIVITY = 0.040f;
    public static final float MOVEMENT_SPEED = 0.5f;
    public static final float REACH = 5.0f;

    //Recommended to not change, but I can't stop you
    public static final String TITLE = "Minecräft Bad Edition v2";
    public static final float Z_NEAR = 0.01f;
    public static final float Z_FAR = -1.0f;

    public static final float HALF_PLAYER_WIDTH = 0.23f;
    public static final float PLAYER_HEAD_OFFSET = 0.08f;
    public static final float[] PLAYER_FEET_OFFSETS = new float[]{1.65f, 1.4f, 0.4f, 0.4f};

    //Player movement
    public static final float AIR_FRICTION = 0.028944f;
    public static final float FALL_FRICTION = 0.23521f;
    public static final float WATER_FRICTION = 0.000000008f;
    public static final float GROUND_FRICTION = 0.000000257f;
    public static final float FLY_FRICTION = 0.00062f;

    public static final float IN_AIR_SPEED = 0.23f;
    public static final float[] MOVEMENT_STATE_SPEED = new float[]{MOVEMENT_SPEED, MOVEMENT_SPEED * 0.375f, MOVEMENT_SPEED * 0.25f};
    public static final float MAX_FALL_SPEED = 0.4f;
    public static final float FLY_SPEED = 1.0f;

    public static final float JUMP_STRENGTH = 11.81952f;
    public static final float SWIM_STRENGTH = 0.486f;
    public static final float GRAVITY_ACCELERATION = 0.28f;
    public static final float MAX_STEP_HEIGHT = 0.6f;

    //Movement state indices
    public static final int WALKING = 0;
    public static final int CROUCHING = 1;
    public static final int CRAWLING = 2;
    public static final int SWIMMING = 3;

    //DO NOT CHANGE THESE VALUES
    public static final int CHUNK_SIZE = 32;
    public static final int CHUNK_SIZE_BITS = 5;
    public static final int MAX_CHUNKS_XZ = 0x7FFFFFF;
    public static final int MAX_CHUNKS_Y = 0x3FF;
    public static final int MAX_BLOCK_LIGHT_VALUE = 15;
    public static final int MAX_SKY_LIGHT_VALUE = 15;

    //Change based on computing power
    public static final int REACH_ACCURACY = 100;
    public static final int MAX_CHUNKS_TO_BUFFER_PER_FRAME = 10;

    public static final int RENDER_DISTANCE_XZ = 15;
    public static final int RENDER_DISTANCE_Y = 5;

    public static final int RENDERED_WORLD_WIDTH = RENDER_DISTANCE_XZ * 2 + 5;
    public static final int RENDERED_WORLD_HEIGHT = RENDER_DISTANCE_Y * 2 + 5;

    //World generation
    public static final long SEED = 0;
    public static final int WATER_LEVEL = 96;
    public static final int SNOW_LEVEL = 160;
    public static final double TREE_THRESHOLD = 0.73;
    public static final byte OAK_TREE_VALUE = 1;
    public static final byte SPRUCE_TREE_VALUE = 2;
    public static final byte DARK_OAK_TREE_VALUE = 3;
    public static final double HEIGHT_MAP_FREQUENCY = 0.01;
    public static final double STONE_MAP_FREQUENCY = 0.005;

    public static final int CAVE_HEIGHT = 10;
    public static final double CAVE_HEIGHT_BIAS = 0.0002;

    public static final double BLOB_CAVE_FREQUENCY = 0.05;
    public static final double BLOB_CAVE_THRESHOLD = 0.9;
    public static final double BLOB_CAVE_MAX_Y = CAVE_HEIGHT + 1.0 / Math.sqrt(BLOB_CAVE_THRESHOLD * CAVE_HEIGHT_BIAS);
    public static final double BLOB_CAVE_MIN_Y = CAVE_HEIGHT - 1.0 / Math.sqrt(BLOB_CAVE_THRESHOLD * CAVE_HEIGHT_BIAS);

    public static final double STONE_TYPE_FREQUENCY = 0.02;
    public static final double ANDESITE_THRESHOLD = 0.1;
    public static final double SLATE_THRESHOLD = 0.7;

    public static final double MUD_TYPE_FREQUENCY = 0.08;
    public static final double GRAVEL_THRESHOLD = 0.1;
    public static final double CLAY_THRESHOLD = 0.5;
    public static final double SAND_THRESHOLD = -0.5;

    public static final byte OUT_OF_WORLD = -1;

    //Indices for the sides of blocks
    public static final int FRONT = 0;
    public static final int TOP = 1;
    public static final int RIGHT = 2;
    public static final int BACK = 3;
    public static final int BOTTOM = 4;
    public static final int LEFT = 5;

    //Indices for information on block types
    public static final int FULL_BLOCK = 0;
    public static final int BOTTOM_SLAB = 1;
    public static final int TOP_SLAB = 2;
    public static final int FRONT_SLAB = 3;
    public static final int BACK_SLAB = 4;
    public static final int RIGHT_SLAB = 5;
    public static final int LEFT_SLAB = 6;
    public static final int UP_DOWN_POST = 7;
    public static final int FRONT_BACK_POST = 8;
    public static final int LEFT_RIGHT_POST = 9;
    public static final int WATER_TYPE = 10;
    public static final int LEAVE_TYPE = 11;
    public static final int GLASS_TYPE = 12;
    public static final int AIR_TYPE = 13;
    public static final int UP_DOWN_WALL = 14;
    public static final int FRONT_BACK_WALL = 15;
    public static final int LEFT_RIGHT_WALL = 16;
    public static final int FRONT_PLATE = 17;
    public static final int TOP_PLATE = 18;
    public static final int RIGHT_PLATE = 19;
    public static final int BACK_PLATE = 20;
    public static final int BOTTOM_PLATE = 21;
    public static final int LEFT_PLATE = 22;
    public static final int PLAYER_HEAD = 23;
    public static final int CACTUS_TYPE = 24;

    public static final int AMOUNT_OF_BLOCK_TYPES = 25;

    //Other information on stuff
    public static final int[] SIDE_MASKS;

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

    //In world and in inventory
    public static final byte AIR = 0;
    public static final byte GRASS = 1;
    public static final byte DIRT = 2;
    public static final byte STONE = 3;
    public static final byte MUD = 18;
    public static final byte ANDESITE = 19;
    public static final byte SNOW = 33;
    public static final byte SAND = 34;
    public static final byte STONE_BRICKS = 35;
    public static final byte COBBLESTONE = 51;
    public static final byte OAK_PLANKS = 68;
    public static final byte SPRUCE_PLANKS = 69;
    public static final byte DARK_OAK_PLANKS = 70;
    public static final byte GLASS = 50;
    public static final byte OAK_LEAVES = 84;
    public static final byte SPRUCE_LEAVES = 85;
    public static final byte DARK_OAK_LEAVES = 92;
    public static final byte WATER = 65;
    public static final byte SLATE = 49;
    public static final byte POLISHED_STONE = 67;
    public static final byte GRAVEL = 66;
    public static final byte COURSE_DIRT = 81;
    public static final byte CHISELED_STONE = 82;
    public static final byte CHISELED_POLISHED_STONE = 83;
    public static final byte CHISELED_SLATE = -127;
    public static final byte LAVA = -126;
    public static final byte COAL_ORE = -111;
    public static final byte IRON_ORE = -110;
    public static final byte DIAMOND_ORE = -109;
    public static final byte RED = -2;
    public static final byte GREEN = -3;
    public static final byte BLUE = -4;
    public static final byte YELLOW = -5;
    public static final byte MAGENTA = -6;
    public static final byte CYAN = -7;
    public static final byte WHITE = -8;
    public static final byte BLACK = -9;
    public static final byte BARRIER = 17;
    public static final byte MOSS = 99;
    public static final byte CLAY = 98;
    public static final byte ICE = 113;
    public static final byte HEAVY_ICE = 97;
    public static final byte CACTUS = 114;

    //Only in inventory
    public static final byte COBBLESTONE_SLAB = 20;
    public static final byte COBBLESTONE_POST = 26;
    public static final byte COBBLESTONE_PLATE = -48;
    public static final byte COBBLESTONE_WALL = 29;

    public static final byte STONE_BRICK_SLAB = 52;
    public static final byte STONE_BRICK_POST = 58;
    public static final byte STONE_BRICK_WALL = 61;
    public static final byte STONE_BRICK_PLATE = -64;

    public static final byte STONE_SLAB = -32;
    public static final byte STONE_POST = -38;
    public static final byte STONE_WALL = -22;
    public static final byte STONE_PLATE = -16;

    public static final byte POLISHED_STONE_SLAB = -96;
    public static final byte POLISHED_STONE_POST = -102;
    public static final byte POLISHED_STONE_WALL = -86;
    public static final byte POLISHED_STONE_PLATE = -80;

    public static final byte SLATE_SLAB = -128;
    public static final byte SLATE_POST = 122;
    public static final byte SLATE_WALL = -118;
    public static final byte SLATE_PLATE = -112;

    public static final byte ANDESITE_SLAB = 112;
    public static final byte ANDESITE_WALL = 106;
    public static final byte ANDESITE_POST = 74;
    public static final byte ANDESITE_PLATE = 80;

    public static final byte OAK_PLANKS_SLAB = -108;
    public static final byte OAK_PLANKS_WALL = 7;
    public static final byte OAK_PLANKS_POST = 10;
    public static final byte OAK_PLANKS_PLATE = 71;

    public static final byte SPRUCE_PLANKS_SLAB = -107;
    public static final byte SPRUCE_PLANKS_WALL = 13;
    public static final byte SPRUCE_PLANKS_POST = 39;
    public static final byte SPRUCE_PLANKS_PLATE = -72;

    public static final byte DARK_OAK_PLANKS_SLAB = -106;
    public static final byte DARK_OAK_PLANKS_WALL = 42;
    public static final byte DARK_OAK_PLANKS_POST = 45;
    public static final byte DARK_OAK_PLANKS_PLATE = -73;

    public static final byte OAK_LOG = 4;
    public static final byte SPRUCE_LOG = 5;
    public static final byte DARK_OAK_LOG = 6;
    public static final byte STRIPPED_OAK_LOG = 36;
    public static final byte STRIPPED_SPRUCE_LOG = 37;
    public static final byte STRIPPED_DARK_OAK_LOG = 38;
    public static final byte GLASS_WALL = 88;
    public static final byte CREATOR_HEAD = 16;

    //Only in world
    public static final byte FRONT_CREATOR_HEAD = 16;
    public static final byte RIGHT_CREATOR_HEAD = 32;
    public static final byte BACK_CREATOR_HEAD = 48;
    public static final byte LEFT_CREATOR_HEAD = 64;
    public static final byte[] CREATOR_HEADS = new byte[]{FRONT_CREATOR_HEAD, AIR, RIGHT_CREATOR_HEAD, BACK_CREATOR_HEAD, AIR, LEFT_CREATOR_HEAD};

    public static final byte OAK_PLANKS_FRONT_BACK_WALL = 7;
    public static final byte OAK_PLANKS_UP_DOWN_WALL = 8;
    public static final byte OAK_PLANKS_LEFT_RIGHT_WALL = 9;
    public static final byte[] OAK_PLANKS_WALLS = new byte[]{OAK_PLANKS_FRONT_BACK_WALL, OAK_PLANKS_UP_DOWN_WALL, OAK_PLANKS_LEFT_RIGHT_WALL};

    public static final byte OAK_PLANKS_UP_DOWN_POST = 10;
    public static final byte OAK_PLANKS_FRONT_BACK_POST = 11;
    public static final byte OAK_PLANKS_LEFT_RIGHT_POST = 12;
    public static final byte[] OAK_PLANKS_POSTS = new byte[]{OAK_PLANKS_FRONT_BACK_POST, OAK_PLANKS_UP_DOWN_POST, OAK_PLANKS_LEFT_RIGHT_POST};

    public static final byte OAK_PLANKS_BOTTOM_PLATE = 71;
    public static final byte OAK_PLANKS_TOP_PLATE = 103;
    public static final byte OAK_PLANKS_FRONT_PLATE = 119;
    public static final byte OAK_PLANKS_BACK_PLATE = -121;
    public static final byte OAK_PLANKS_RIGHT_PLATE = -105;
    public static final byte OAK_PLANKS_LEFT_PLATE = -89;
    public static final byte[] OAK_PLANKS_PLATES = new byte[]{OAK_PLANKS_FRONT_PLATE, OAK_PLANKS_TOP_PLATE, OAK_PLANKS_RIGHT_PLATE, OAK_PLANKS_BACK_PLATE, OAK_PLANKS_BOTTOM_PLATE, OAK_PLANKS_LEFT_PLATE};

    public static final byte SPRUCE_PLANKS_FRONT_BACK_WALL = 13;
    public static final byte SPRUCE_PLANKS_UP_DOWN_WALL = 14;
    public static final byte SPRUCE_PLANKS_LEFT_RIGHT_WALL = 15;
    public static final byte[] SPRUCE_PLANKS_WALLS = new byte[]{SPRUCE_PLANKS_FRONT_BACK_WALL, SPRUCE_PLANKS_UP_DOWN_WALL, SPRUCE_PLANKS_LEFT_RIGHT_WALL};

    public static final byte SPRUCE_PLANKS_UP_DOWN_POST = 39;
    public static final byte SPRUCE_PLANKS_FRONT_BACK_POST = 40;
    public static final byte SPRUCE_PLANKS_LEFT_RIGHT_POST = 41;
    public static final byte[] SPRUCE_PLANKS_POSTS = new byte[]{SPRUCE_PLANKS_FRONT_BACK_POST, SPRUCE_PLANKS_UP_DOWN_POST, SPRUCE_PLANKS_LEFT_RIGHT_POST};

    public static final byte SPRUCE_PLANKS_BOTTOM_PLATE = -72;
    public static final byte SPRUCE_PLANKS_TOP_PLATE = -71;
    public static final byte SPRUCE_PLANKS_FRONT_PLATE = -70;
    public static final byte SPRUCE_PLANKS_BACK_PLATE = -54;
    public static final byte SPRUCE_PLANKS_RIGHT_PLATE = -55;
    public static final byte SPRUCE_PLANKS_LEFT_PLATE = -56;
    public static final byte[] SPRUCE_PLANKS_PLATES = new byte[]{SPRUCE_PLANKS_FRONT_PLATE, SPRUCE_PLANKS_TOP_PLATE, SPRUCE_PLANKS_RIGHT_PLATE, SPRUCE_PLANKS_BACK_PLATE, SPRUCE_PLANKS_BOTTOM_PLATE, SPRUCE_PLANKS_LEFT_PLATE};

    public static final byte DARK_OAK_PLANKS_FRONT_BACK_WALL = 42;
    public static final byte DARK_OAK_PLANKS_UP_DOWN_WALL = 43;
    public static final byte DARK_OAK_PLANKS_LEFT_RIGHT_WALL = 44;
    public static final byte[] DARK_OAK_PLANKS_WALLS = new byte[]{DARK_OAK_PLANKS_FRONT_BACK_WALL, DARK_OAK_PLANKS_UP_DOWN_WALL, DARK_OAK_PLANKS_LEFT_RIGHT_WALL};

    public static final byte DARK_OAK_PLANKS_UP_DOWN_POST = 45;
    public static final byte DARK_OAK_PLANKS_FRONT_BACK_POST = 46;
    public static final byte DARK_OAK_PLANKS_LEFT_RIGHT_POST = 47;
    public static final byte[] DARK_OAK_PLANKS_POSTS = new byte[]{DARK_OAK_PLANKS_FRONT_BACK_POST, DARK_OAK_PLANKS_UP_DOWN_POST, DARK_OAK_PLANKS_LEFT_RIGHT_POST};

    public static final byte DARK_OAK_PLANKS_BOTTOM_PLATE = -73;
    public static final byte DARK_OAK_PLANKS_TOP_PLATE = -57;
    public static final byte DARK_OAK_PLANKS_FRONT_PLATE = -41;
    public static final byte DARK_OAK_PLANKS_BACK_PLATE = -25;
    public static final byte DARK_OAK_PLANKS_RIGHT_PLATE = -10;
    public static final byte DARK_OAK_PLANKS_LEFT_PLATE = -11;
    public static final byte[] DARK_OAK_PLANKS_PLATES = new byte[]{DARK_OAK_PLANKS_FRONT_PLATE, DARK_OAK_PLANKS_TOP_PLATE, DARK_OAK_PLANKS_RIGHT_PLATE, DARK_OAK_PLANKS_BACK_PLATE, DARK_OAK_PLANKS_BOTTOM_PLATE, DARK_OAK_PLANKS_LEFT_PLATE};

    public static final byte ANDESITE_BOTTOM_SLAB = 112;
    public static final byte ANDESITE_TOP_SLAB = 111;
    public static final byte ANDESITE_FRONT_SLAB = 110;
    public static final byte ANDESITE_BACK_SLAB = 109;
    public static final byte ANDESITE_RIGHT_SLAB = 108;
    public static final byte ANDESITE_LEFT_SLAB = 107;
    public static final byte[] ANDESITE_SLABS = new byte[]{ANDESITE_FRONT_SLAB, ANDESITE_TOP_SLAB, ANDESITE_RIGHT_SLAB, ANDESITE_BACK_SLAB, ANDESITE_BOTTOM_SLAB, ANDESITE_LEFT_SLAB};

    public static final byte ANDESITE_BOTTOM_PLATE = 80;
    public static final byte ANDESITE_TOP_PLATE = 79;
    public static final byte ANDESITE_FRONT_PLATE = 78;
    public static final byte ANDESITE_BACK_PLATE = 77;
    public static final byte ANDESITE_RIGHT_PLATE = 76;
    public static final byte ANDESITE_LEFT_PLATE = 75;
    public static final byte[] ANDESITE_PLATES = new byte[]{ANDESITE_FRONT_PLATE, ANDESITE_TOP_PLATE, ANDESITE_RIGHT_PLATE, ANDESITE_BACK_PLATE, ANDESITE_BOTTOM_PLATE, ANDESITE_LEFT_PLATE};

    public static final byte ANDESITE_FRONT_BACK_WALL = 106;
    public static final byte ANDESITE_UP_DOWN_WALL = 105;
    public static final byte ANDESITE_LEFT_RIGHT_WALL = 104;
    public static final byte[] ANDESITE_WALLS = new byte[]{ANDESITE_FRONT_BACK_WALL, ANDESITE_UP_DOWN_WALL, ANDESITE_LEFT_RIGHT_WALL};

    public static final byte ANDESITE_UP_DOWN_POST = 74;
    public static final byte ANDESITE_FRONT_BACK_POST = 73;
    public static final byte ANDESITE_LEFT_RIGHT_POST = 72;
    public static final byte[] ANDESITE_POSTS = new byte[]{ANDESITE_FRONT_BACK_POST, ANDESITE_UP_DOWN_POST, ANDESITE_LEFT_RIGHT_POST};

    public static final byte SLATE_BOTTOM_SLAB = -128;
    public static final byte SLATE_TOP_SLAB = 127;
    public static final byte SLATE_FRONT_SLAB = 126;
    public static final byte SLATE_BACK_SLAB = 125;
    public static final byte SLATE_RIGHT_SLAB = 124;
    public static final byte SLATE_LEFT_SLAB = 123;
    public static final byte[] SLATE_SLABS = new byte[]{SLATE_FRONT_SLAB, SLATE_TOP_SLAB, SLATE_RIGHT_SLAB, SLATE_BACK_SLAB, SLATE_BOTTOM_SLAB, SLATE_LEFT_SLAB};

    public static final byte SLATE_BOTTOM_PLATE = -112;
    public static final byte SLATE_TOP_PLATE = -113;
    public static final byte SLATE_FRONT_PLATE = -114;
    public static final byte SLATE_BACK_PLATE = -115;
    public static final byte SLATE_RIGHT_PLATE = -116;
    public static final byte SLATE_LEFT_PLATE = -117;
    public static final byte[] SLATE_PLATES = new byte[]{SLATE_FRONT_PLATE, SLATE_TOP_PLATE, SLATE_RIGHT_PLATE, SLATE_BACK_PLATE, SLATE_BOTTOM_PLATE, SLATE_LEFT_PLATE};

    public static final byte SLATE_FRONT_BACK_WALL = -118;
    public static final byte SLATE_UP_DOWN_WALL = -119;
    public static final byte SLATE_LEFT_RIGHT_WALL = -120;
    public static final byte[] SLATE_WALLS = new byte[]{SLATE_FRONT_BACK_WALL, SLATE_UP_DOWN_WALL, SLATE_LEFT_RIGHT_WALL};

    public static final byte SLATE_UP_DOWN_POST = 122;
    public static final byte SLATE_FRONT_BACK_POST = 121;
    public static final byte SLATE_LEFT_RIGHT_POST = 120;
    public static final byte[] SLATE_POSTS = new byte[]{SLATE_FRONT_BACK_POST, SLATE_UP_DOWN_POST, SLATE_LEFT_RIGHT_POST};

    public static final byte POLISHED_STONE_BOTTOM_SLAB = -96;
    public static final byte POLISHED_STONE_TOP_SLAB = -97;
    public static final byte POLISHED_STONE_FRONT_SLAB = -98;
    public static final byte POLISHED_STONE_BACK_SLAB = -99;
    public static final byte POLISHED_STONE_RIGHT_SLAB = -100;
    public static final byte POLISHED_STONE_LEFT_SLAB = -101;
    public static final byte[] POLISHED_STONE_SLABS = new byte[]{POLISHED_STONE_FRONT_SLAB, POLISHED_STONE_TOP_SLAB, POLISHED_STONE_RIGHT_SLAB, POLISHED_STONE_BACK_SLAB, POLISHED_STONE_BOTTOM_SLAB, POLISHED_STONE_LEFT_SLAB};

    public static final byte POLISHED_STONE_BOTTOM_PLATE = -80;
    public static final byte POLISHED_STONE_TOP_PLATE = -81;
    public static final byte POLISHED_STONE_FRONT_PLATE = -82;
    public static final byte POLISHED_STONE_BACK_PLATE = -83;
    public static final byte POLISHED_STONE_RIGHT_PLATE = -84;
    public static final byte POLISHED_STONE_LEFT_PLATE = -84;
    public static final byte[] POLISHED_STONE_PLATES = new byte[]{POLISHED_STONE_FRONT_PLATE, POLISHED_STONE_TOP_PLATE, POLISHED_STONE_RIGHT_PLATE, POLISHED_STONE_BACK_PLATE, POLISHED_STONE_BOTTOM_PLATE, POLISHED_STONE_LEFT_PLATE};

    public static final byte POLISHED_STONE_FRONT_BACK_WALL = -86;
    public static final byte POLISHED_STONE_UP_DOWN_WALL = -87;
    public static final byte POLISHED_STONE_LEFT_RIGHT_WALL = -88;
    public static final byte[] POLISHED_STONE_WALLS = new byte[]{POLISHED_STONE_FRONT_BACK_WALL, POLISHED_STONE_UP_DOWN_WALL, POLISHED_STONE_LEFT_RIGHT_WALL};

    public static final byte POLISHED_STONE_UP_DOWN_POST = -102;
    public static final byte POLISHED_STONE_FRONT_BACK_POST = -103;
    public static final byte POLISHED_STONE_LEFT_RIGHT_POST = -104;
    public static final byte[] POLISHED_STONE_POSTS = new byte[]{POLISHED_STONE_FRONT_BACK_POST, POLISHED_STONE_UP_DOWN_POST, POLISHED_STONE_LEFT_RIGHT_POST};

    public static final byte STONE_BOTTOM_SLAB = -32;
    public static final byte STONE_TOP_SLAB = -33;
    public static final byte STONE_FRONT_SLAB = -34;
    public static final byte STONE_BACK_SLAB = -35;
    public static final byte STONE_RIGHT_SLAB = -36;
    public static final byte STONE_LEFT_SLAB = -37;
    public static final byte[] STONE_SLABS = new byte[]{STONE_FRONT_SLAB, STONE_TOP_SLAB, STONE_RIGHT_SLAB, STONE_BACK_SLAB, STONE_BOTTOM_SLAB, STONE_LEFT_SLAB};

    public static final byte STONE_BOTTOM_PLATE = -16;
    public static final byte STONE_TOP_PLATE = -17;
    public static final byte STONE_FRONT_PLATE = -18;
    public static final byte STONE_BACK_PLATE = -19;
    public static final byte STONE_RIGHT_PLATE = -20;
    public static final byte STONE_LEFT_PLATE = -21;
    public static final byte[] STONE_PLATES = new byte[]{STONE_FRONT_PLATE, STONE_TOP_PLATE, STONE_RIGHT_PLATE, STONE_BACK_PLATE, STONE_BOTTOM_PLATE, STONE_LEFT_PLATE};

    public static final byte STONE_FRONT_BACK_WALL = -22;
    public static final byte STONE_UP_DOWN_WALL = -23;
    public static final byte STONE_LEFT_RIGHT_WALL = -24;
    public static final byte[] STONE_WALLS = new byte[]{STONE_FRONT_BACK_WALL, STONE_UP_DOWN_WALL, STONE_LEFT_RIGHT_WALL};

    public static final byte STONE_UP_DOWN_POST = -38;
    public static final byte STONE_FRONT_BACK_POST = -39;
    public static final byte STONE_LEFT_RIGHT_POST = -40;
    public static final byte[] STONE_POSTS = new byte[]{STONE_FRONT_BACK_POST, STONE_UP_DOWN_POST, STONE_LEFT_RIGHT_POST};

    public static final byte COBBLESTONE_BOTTOM_SLAB = 20;
    public static final byte COBBLESTONE_TOP_SLAB = 21;
    public static final byte COBBLESTONE_FRONT_SLAB = 22;
    public static final byte COBBLESTONE_BACK_SLAB = 23;
    public static final byte COBBLESTONE_RIGHT_SLAB = 24;
    public static final byte COBBLESTONE_LEFT_SLAB = 25;
    public static final byte[] COBBLESTONE_SLABS = new byte[]{COBBLESTONE_FRONT_SLAB, COBBLESTONE_TOP_SLAB, COBBLESTONE_RIGHT_SLAB, COBBLESTONE_BACK_SLAB, COBBLESTONE_BOTTOM_SLAB, COBBLESTONE_LEFT_SLAB};

    public static final byte STONE_BRICK_BOTTOM_SLAB = 52;
    public static final byte STONE_BRICK_TOP_SLAB = 53;
    public static final byte STONE_BRICK_FRONT_SLAB = 54;
    public static final byte STONE_BRICK_BACK_SLAB = 55;
    public static final byte STONE_BRICK_RIGHT_SLAB = 56;
    public static final byte STONE_BRICK_LEFT_SLAB = 57;
    public static final byte[] STONE_BRICK_SLABS = new byte[]{STONE_BRICK_FRONT_SLAB, STONE_BRICK_TOP_SLAB, STONE_BRICK_RIGHT_SLAB, STONE_BRICK_BACK_SLAB, STONE_BRICK_BOTTOM_SLAB, STONE_BRICK_LEFT_SLAB};

    public static final byte OAK_PLANKS_BOTTOM_SLAB = -108;
    public static final byte OAK_PLANKS_TOP_SLAB = -92;
    public static final byte OAK_PLANKS_FRONT_SLAB = -76;
    public static final byte OAK_PLANKS_BACK_SLAB = -60;
    public static final byte OAK_PLANKS_RIGHT_SLAB = -44;
    public static final byte OAK_PLANKS_LEFT_SLAB = -28;
    public static final byte[] OAK_PLANKS_SLABS = new byte[]{OAK_PLANKS_FRONT_SLAB, OAK_PLANKS_TOP_SLAB, OAK_PLANKS_RIGHT_SLAB, OAK_PLANKS_BACK_SLAB, OAK_PLANKS_BOTTOM_SLAB, OAK_PLANKS_LEFT_SLAB};

    public static final byte SPRUCE_PLANKS_BOTTOM_SLAB = -107;
    public static final byte SPRUCE_PLANKS_TOP_SLAB = -91;
    public static final byte SPRUCE_PLANKS_FRONT_SLAB = -75;
    public static final byte SPRUCE_PLANKS_BACK_SLAB = -59;
    public static final byte SPRUCE_PLANKS_RIGHT_SLAB = -43;
    public static final byte SPRUCE_PLANKS_LEFT_SLAB = -27;
    public static final byte[] SPRUCE_PLANKS_SLABS = new byte[]{SPRUCE_PLANKS_FRONT_SLAB, SPRUCE_PLANKS_TOP_SLAB, SPRUCE_PLANKS_RIGHT_SLAB, SPRUCE_PLANKS_BACK_SLAB, SPRUCE_PLANKS_BOTTOM_SLAB, SPRUCE_PLANKS_LEFT_SLAB};

    public static final byte DARK_OAK_PLANKS_BOTTOM_SLAB = -106;
    public static final byte DARK_OAK_PLANKS_TOP_SLAB = -90;
    public static final byte DARK_OAK_PLANKS_FRONT_SLAB = -74;
    public static final byte DARK_OAK_PLANKS_BACK_SLAB = -58;
    public static final byte DARK_OAK_PLANKS_RIGHT_SLAB = -42;
    public static final byte DARK_OAK_PLANKS_LEFT_SLAB = -26;
    public static final byte[] DARK_OAK_PLANKS_SLABS = new byte[]{DARK_OAK_PLANKS_FRONT_SLAB, DARK_OAK_PLANKS_TOP_SLAB, DARK_OAK_PLANKS_RIGHT_SLAB, DARK_OAK_PLANKS_BACK_SLAB, DARK_OAK_PLANKS_BOTTOM_SLAB, DARK_OAK_PLANKS_LEFT_SLAB};

    public static final byte COBBLESTONE_UP_DOWN_POST = 26;
    public static final byte COBBLESTONE_FRONT_BACK_POST = 27;
    public static final byte COBBLESTONE_LEFT_RIGHT_POST = 28;
    public static final byte[] COBBLESTONE_POSTS = new byte[]{COBBLESTONE_FRONT_BACK_POST, COBBLESTONE_UP_DOWN_POST, COBBLESTONE_LEFT_RIGHT_POST};

    public static final byte STONE_BRICK_UP_DOWN_POST = 58;
    public static final byte STONE_BRICK_FRONT_BACK_POST = 59;
    public static final byte STONE_BRICK_LEFT_RIGHT_POST = 60;
    public static final byte[] STONE_BRICK_POSTS = new byte[]{STONE_BRICK_FRONT_BACK_POST, STONE_BRICK_UP_DOWN_POST, STONE_BRICK_LEFT_RIGHT_POST};

    public static final byte GLASS_FRONT_BACK_WALL = 88;
    public static final byte GLASS_UP_DOWN_WALL = 89;
    public static final byte GLASS_LEFT_RIGHT_WALL = 90;
    public static final byte[] GLASS_WALLS = new byte[]{GLASS_FRONT_BACK_WALL, GLASS_UP_DOWN_WALL, GLASS_LEFT_RIGHT_WALL};

    public static final byte COBBLESTONE_FRONT_BACK_WALL = 29;
    public static final byte COBBLESTONE_UP_DOWN_WALL = 30;
    public static final byte COBBLESTONE_LEFT_RIGHT_WALL = 31;
    public static final byte[] COBBLESTONE_WALLS = new byte[]{COBBLESTONE_FRONT_BACK_WALL, COBBLESTONE_UP_DOWN_WALL, COBBLESTONE_LEFT_RIGHT_WALL};

    public static final byte STONE_BRICK_FRONT_BACK_WALL = 61;
    public static final byte STONE_BRICK_UP_DOWN_WALL = 62;
    public static final byte STONE_BRICK_LEFT_RIGHT_WALL = 63;
    public static final byte[] STONE_BRICK_WALLS = new byte[]{STONE_BRICK_FRONT_BACK_WALL, STONE_BRICK_UP_DOWN_WALL, STONE_BRICK_LEFT_RIGHT_WALL};

    public static final byte UP_DOWN_OAK_LOG = 4;
    public static final byte FRONT_BACK_OAK_LOG = 86;
    public static final byte LEFT_RIGHT_OAK_LOG = 100;
    public static final byte[] OAK_LOGS = new byte[]{FRONT_BACK_OAK_LOG, UP_DOWN_OAK_LOG, LEFT_RIGHT_OAK_LOG};

    public static final byte UP_DOWN_STRIPPED_OAK_LOG = 36;
    public static final byte FRONT_BACK_STRIPPED_OAK_LOG = 116;
    public static final byte LEFT_RIGHT_STRIPPED_OAK_LOG = -124;
    public static final byte[] STRIPPED_OAK_LOGS = new byte[]{FRONT_BACK_STRIPPED_OAK_LOG, UP_DOWN_STRIPPED_OAK_LOG, LEFT_RIGHT_STRIPPED_OAK_LOG};

    public static final byte UP_DOWN_SPRUCE_LOG = 5;
    public static final byte FRONT_BACK_SPRUCE_LOG = 87;
    public static final byte LEFT_RIGHT_SPRUCE_LOG = 101;
    public static final byte[] SPRUCE_LOGS = new byte[]{FRONT_BACK_SPRUCE_LOG, UP_DOWN_SPRUCE_LOG, LEFT_RIGHT_SPRUCE_LOG};

    public static final byte UP_DOWN_STRIPPED_SPRUCE_LOG = 37;
    public static final byte FRONT_BACK_STRIPPED_SPRUCE_LOG = 117;
    public static final byte LEFT_RIGHT_STRIPPED_SPRUCE_LOG = -123;
    public static final byte[] STRIPPED_SPRUCE_LOGS = new byte[]{FRONT_BACK_STRIPPED_SPRUCE_LOG, UP_DOWN_STRIPPED_SPRUCE_LOG, LEFT_RIGHT_STRIPPED_SPRUCE_LOG};

    public static final byte UP_DOWN_DARK_OAK_LOG = 6;
    public static final byte FRONT_BACK_DARK_OAK_LOG = 91;
    public static final byte LEFT_RIGHT_DARK_OAK_LOG = 102;
    public static final byte[] DARK_OAK_LOGS = new byte[]{FRONT_BACK_DARK_OAK_LOG, UP_DOWN_DARK_OAK_LOG, LEFT_RIGHT_DARK_OAK_LOG};

    public static final byte UP_DOWN_STRIPPED_DARK_OAK_LOG = 38;
    public static final byte FRONT_BACK_STRIPPED_DARK_OAK_LOG = 118;
    public static final byte LEFT_RIGHT_STRIPPED_DARK_OAK_LOG = -122;
    public static final byte[] STRIPPED_DARK_OAK_LOGS = new byte[]{FRONT_BACK_STRIPPED_DARK_OAK_LOG, UP_DOWN_STRIPPED_DARK_OAK_LOG, LEFT_RIGHT_STRIPPED_DARK_OAK_LOG};

    public static final byte COBBLESTONE_BOTTOM_PLATE = -48;
    public static final byte COBBLESTONE_TOP_PLATE = -49;
    public static final byte COBBLESTONE_FRONT_PLATE = -50;
    public static final byte COBBLESTONE_BACK_PLATE = -51;
    public static final byte COBBLESTONE_RIGHT_PLATE = -52;
    public static final byte COBBLESTONE_LEFT_PLATE = -53;
    public static final byte[] COBBLESTONE_PLATES = new byte[]{COBBLESTONE_FRONT_PLATE, COBBLESTONE_TOP_PLATE, COBBLESTONE_RIGHT_PLATE, COBBLESTONE_BACK_PLATE, COBBLESTONE_BOTTOM_PLATE, COBBLESTONE_LEFT_PLATE};

    public static final byte STONE_BRICK_BOTTOM_PLATE = -64;
    public static final byte STONE_BRICK_TOP_PLATE = -65;
    public static final byte STONE_BRICK_FRONT_PLATE = -66;
    public static final byte STONE_BRICK_BACK_PLATE = -67;
    public static final byte STONE_BRICK_RIGHT_PLATE = -68;
    public static final byte STONE_BRICK_LEFT_PLATE = -69;
    public static final byte[] STONE_BRICK_PLATES = new byte[]{STONE_BRICK_FRONT_PLATE, STONE_BRICK_TOP_PLATE, STONE_BRICK_RIGHT_PLATE, STONE_BRICK_BACK_PLATE, STONE_BRICK_BOTTOM_PLATE, STONE_BRICK_LEFT_PLATE};


    //Texture indices for multi textured blocks
    public static final byte GRASS_SIDE = 17;
    public static final byte OAK_LOG_TOP = 20;
    public static final byte SPRUCE_LOG_TOP = 21;
    public static final byte DARK_OAK_LOG_TOP = 22;
    public static final byte STRIPPED_OAK_LOG_TOP = 52;
    public static final byte STRIPPED_SPRUCE_LOG_TOP = 53;
    public static final byte STRIPPED_DARK_OAK_LOG_TOP = 54;
    public static final byte ROTATED_OAK_LOG = 100;
    public static final byte ROTATED_SPRUCE_LOG = 101;
    public static final byte ROTATED_DARK_OAK_LOG = 102;
    public static final byte ROTATED_STRIPPED_OAK_LOG = 116;
    public static final byte ROTATED_STRIPPED_SPRUCE_LOG = 117;
    public static final byte ROTATED_STRIPPED_DARK_OAK_LOG = 118;
    public static final byte CREATOR_HEAD_FRONT = -123;
    public static final byte CREATOR_HEAD_LEFT = -122;
    public static final byte CREATOR_HEAD_RIGHT = -124;
    public static final byte CREATOR_HEAD_TOP = -108;
    public static final byte CREATOR_HEAD_BACK = -107;
    public static final byte CREATOR_HEAD_BOTTOM = -106;
    public static final byte ROTATED_CREATOR_HEAD_BOTTOM = -90;
    public static final byte CACTUS_SIDE = 114;
    public static final byte CACTUS_TOP = -91;

    //Not currently in use
    //public static final float[] LIGHT = {1.0f, 1.2f, 0.9f, 0.8f, 0.6f, 1.1f};

    //Just pretend it doesn't exist
    public static final float[] SKY_BOX_VERTICES;

    public static final int[] SKY_BOX_INDICES;

    public static final float[] SKY_BOX_TEXTURE_COORDINATES;

    public static final float[] GUI_ELEMENT_TEXTURE_COORDINATES;

    public static final float[] OVERLAY_VERTICES;

    public static final byte[][][] OAK_TREE;

    public static final byte[][][] SPRUCE_TREE;

    public static final byte[][][] DARK_OAK_TREE;

    //No like actually, this doesn't exist! Trust me. please...
    static {
        //NOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
        //I WARNED YOU!!!
        //WHY DIDN'T YOU LISTEN!??!!?

        OAK_TREE = new byte[][][]{{
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

        SPRUCE_TREE = new byte[][][]{{
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

        DARK_OAK_TREE = new byte[][][]{{
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

        SIDE_MASKS = new int[]{1, 2, 4, 8, 16, 32};

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
