package terrascape.utils;

public final class Settings {
    public static final int IS_MOUSE_BUTTON = 0x80000000;
    public static final int IS_KEYBOARD_BUTTON = 0;

    //Change to whatever you want
//        public static final long SEED = new Random().nextLong();
    public static long SEED;

    // Values read from file
    public static float FOV;
    public static float GUI_SIZE;
    public static float MOUSE_SENSITIVITY;
    public static float REACH;
    public static float TEXT_SIZE;
    public static int RENDER_DISTANCE_XZ;
    public static int RENDER_DISTANCE_Y;

    public static int MOVE_FORWARD_BUTTON;
    public static int MOVE_BACK_BUTTON;
    public static int MOVE_RIGHT_BUTTON;
    public static int MOVE_LEFT_BUTTON;

    public static int JUMP_BUTTON;
    public static int SPRINT_BUTTON;
    public static int SNEAK_BUTTON;
    public static int CRAWL_BUTTON;
    public static int FLY_FAST_BUTTON;

    public static int HOT_BAR_SLOT_1;
    public static int HOT_BAR_SLOT_2;
    public static int HOT_BAR_SLOT_3;
    public static int HOT_BAR_SLOT_4;
    public static int HOT_BAR_SLOT_5;
    public static int HOT_BAR_SLOT_6;
    public static int HOT_BAR_SLOT_7;
    public static int HOT_BAR_SLOT_8;
    public static int HOT_BAR_SLOT_9;

    public static int DESTROY_BUTTON;
    public static int USE_BUTTON;
    public static int PICK_BLOCK_BUTTON;

    public static int OPEN_INVENTORY_BUTTON;
    public static int OPEN_DEBUG_MENU_BUTTON;
    public static int TOGGLE_X_RAY_BUTTON;
    public static int TOGGLE_NO_CLIP_BUTTON;
    public static int USE_OCCLUSION_CULLING_BUTTON;
    public static int SET_POSITION_1_BUTTON;
    public static int SET_POSITION_2_BUTTON;
    public static int RELOAD_SETTINGS_BUTTON;
    public static int ZOOM_BUTTON;
    public static float AUDIO_GAIN;
    public static float STEP_GAIN;
    public static float PLACE_GAIN;
    public static float DIG_GAIN;
    public static float MISCELLANEOUS_GAIN;
    public static float INVENTORY_GAIN;
    public static int RELOAD_SHADERS_BUTTON;

    // Calculated values
    public static boolean SCROLL_HOT_BAR;

    public static int TEXT_CHAR_SIZE_X;
    public static int TEXT_CHAR_SIZE_Y;
    public static int TEXT_LINE_SPACING;

    public static int RENDERED_WORLD_WIDTH;
    public static int RENDERED_WORLD_HEIGHT;

    private Settings() { }
}
