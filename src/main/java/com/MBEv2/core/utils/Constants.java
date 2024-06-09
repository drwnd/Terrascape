package com.MBEv2.core.utils;

public class Constants {

    //Recommended to not change, but I can't stop you
    public static final String TITLE = "Minecräft Bad Edition v2";
    public static final float Z_NEAR = 0.01f;
    public static final float Z_FAR = -1.0f;
    public static final double TREE_THRESHOLD = 0.73;

    public static final float HALF_PLAYER_WIDTH = 0.23f;
    public static final float PLAYER_HEAD_OFFSET = 0.08f;
    public static final float[] PLAYER_FEET_OFFSETS = new float[]{1.65f, 1.4f, 0.4f};

    public static final int WALKING = 0;
    public static final int CROUCHING = 1;
    public static final int CRAWLING = 2;

    //Change to whatever you want
    public static final float TIME_SPEED = 0.000025f;
    public static final float FOV = (float) Math.toRadians(90);
    public static final float GUI_SIZE = 1.0f;
    public static final float MOUSE_SENSITIVITY = 0.040f;
    public static final float MOVEMENT_SPEED = 0.05f;
    public static final float REACH = 5.0f;
    public static final byte OAK_TREE_VALUE = 1;
    public static final byte SPRUCE_TREE_VALUE = 2;
    public static final byte DARK_OAK_TREE_VALUE = 3;

    //DO NOT CHANGE THESE VALUES
    public static final int CHUNK_SIZE = 32;
    public static final int MAX_XZ = 0x7FFFFFF;
    public static final int MAX_Y = 0x3FF;

    //Change based on computing power
    public static final int REACH_ACCURACY = 100;
    public static final int MAX_CHUNKS_TO_BUFFER_PER_FRAME = 10;

    public static final int RENDER_DISTANCE_XZ = 8;
    public static final int RENDER_DISTANCE_Y = 5;

    public static final int RENDERED_WORLD_WIDTH = RENDER_DISTANCE_XZ * 2 + 5;
    public static final int RENDERED_WORLD_HEIGHT = RENDER_DISTANCE_Y * 2 + 5;

    //World generation
    public static final long SEED = 0;
    public static final int WATER_LEVEL = 96;
    public static final int SNOW_LEVEL = 160;

    public static final byte OUT_OF_WORLD = -128;

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
    public static final byte WATER = -33;

    //Only in inventory
    public static final byte COBBLESTONE_SLAB = 20;
    public static final byte COBBLESTONE_POST = 26;
    public static final byte OAK_LOG = 4;
    public static final byte SPRUCE_LOG = 5;
    public static final byte DARK_OAK_LOG = 6;
    public static final byte STRIPPED_OAK_LOG = 36;
    public static final byte STRIPPED_SPRUCE_LOG = 37;
    public static final byte STRIPPED_DARK_OAK_LOG = 38;
    public static final byte STONE_BRICK_SLAB = 52;
    public static final byte STONE_BRICK_POST = 58;
    public static final byte GLASS_WALL = 88;
    public static final byte COBBLESTONE_WALL = 29;
    public static final byte STONE_BRICK_WALL = 61;
    public static final byte OAK_PLANKS_SLAB = -108;
    public static final byte SPRUCE_PLANKS_SLAB = -107;
    public static final byte DARK_OAK_PLANKS_SLAB = -106;

    //Only in world
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
    public static final byte[] STONE_BRICKS_SLABS = new byte[]{STONE_BRICK_FRONT_SLAB, STONE_BRICK_TOP_SLAB, STONE_BRICK_RIGHT_SLAB, STONE_BRICK_BACK_SLAB, STONE_BRICK_BOTTOM_SLAB, STONE_BRICK_LEFT_SLAB};

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

    public static final byte GLASS_UP_DOWN_WALL = 88;
    public static final byte GLASS_FRONT_BACK_WALL = 89;
    public static final byte GLASS_LEFT_RIGHT_WALL = 90;
    public static final byte[] GLASS_WALLS = new byte[]{GLASS_FRONT_BACK_WALL, GLASS_UP_DOWN_WALL, GLASS_LEFT_RIGHT_WALL};

    public static final byte COBBLESTONE_UP_DOWN_WALL = 29;
    public static final byte COBBLESTONE_FRONT_BACK_WALL = 30;
    public static final byte COBBLESTONE_LEFT_RIGHT_WALL = 31;
    public static final byte[] COBBLESTONE_WALLS = new byte[]{COBBLESTONE_FRONT_BACK_WALL, COBBLESTONE_UP_DOWN_WALL, COBBLESTONE_LEFT_RIGHT_WALL};

    public static final byte STONE_BRICK_UP_DOWN_WALL = 61;
    public static final byte STONE_BRICK_FRONT_BACK_WALL = 62;
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

    public static final int AMOUNT_OF_BLOCK_TYPES = 17;

    //Other information on stuff
    public static final int[] SIDE_MASKS;
    public static final int OCCLUDES_ALL = 0;
    public static final int OCCLUDES_SELF = 1;
    public static final int OCCLUDES_DYNAMIC_ALL = 2;
    public static final int OCCLUDES_DYNAMIC_SELF = 3;
    public static final int SOLID_MASK = 64;
    public static final int DYNAMIC_SHAPE_MASK = 128;

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
