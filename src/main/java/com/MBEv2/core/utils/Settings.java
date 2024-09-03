package com.MBEv2.core.utils;

import org.lwjgl.glfw.GLFW;

public class Settings {

    //Change to whatever you want
    public static float FOV = (float) Math.toRadians(90);
    public static float GUI_SIZE = 1.0f;
    public static float MOUSE_SENSITIVITY = 0.040f;
    public static float REACH = 5.0f;
    public static float TEXT_SIZE = 1.0f;
    public static int RENDER_DISTANCE_XZ = 15;
    public static int RENDER_DISTANCE_Y = 5;

    public static final int IS_MOUSE_BUTTON = 0x80000000;
    public static final int IS_KEYBOARD_BUTTON = 0;

    public static int MOVE_FORWARD_BUTTON = GLFW.GLFW_KEY_W | IS_KEYBOARD_BUTTON;
    public static int MOVE_BACK_BUTTON = GLFW.GLFW_KEY_S | IS_KEYBOARD_BUTTON;
    public static int MOVE_RIGHT_BUTTON = GLFW.GLFW_KEY_D | IS_KEYBOARD_BUTTON;
    public static int MOVE_LEFT_BUTTON = GLFW.GLFW_KEY_A | IS_KEYBOARD_BUTTON;

    public static int JUMP_BUTTON = GLFW.GLFW_KEY_SPACE | IS_KEYBOARD_BUTTON;
    public static int SPRINT_BUTTON = GLFW.GLFW_KEY_LEFT_CONTROL | IS_KEYBOARD_BUTTON;
    public static int SNEAK_BUTTON = GLFW.GLFW_KEY_LEFT_SHIFT | IS_KEYBOARD_BUTTON;
    public static int CRAWL_BUTTON = GLFW.GLFW_KEY_CAPS_LOCK | IS_KEYBOARD_BUTTON;
    public static int FLY_FAST_BUTTON = GLFW.GLFW_KEY_TAB | IS_KEYBOARD_BUTTON;

    public static int HOT_BAR_SLOT_1 = GLFW.GLFW_KEY_Q | IS_KEYBOARD_BUTTON;
    public static int HOT_BAR_SLOT_2 = GLFW.GLFW_KEY_2 | IS_KEYBOARD_BUTTON;
    public static int HOT_BAR_SLOT_3 = GLFW.GLFW_KEY_3 | IS_KEYBOARD_BUTTON;
    public static int HOT_BAR_SLOT_4 = GLFW.GLFW_KEY_4 | IS_KEYBOARD_BUTTON;
    public static int HOT_BAR_SLOT_5 = GLFW.GLFW_KEY_5 | IS_KEYBOARD_BUTTON;
    public static int HOT_BAR_SLOT_6 = GLFW.GLFW_KEY_R | IS_KEYBOARD_BUTTON;
    public static int HOT_BAR_SLOT_7 = GLFW.GLFW_KEY_F | IS_KEYBOARD_BUTTON;
    public static int HOT_BAR_SLOT_8 = GLFW.GLFW_MOUSE_BUTTON_5 | IS_MOUSE_BUTTON;
    public static int HOT_BAR_SLOT_9 = GLFW.GLFW_MOUSE_BUTTON_4 | IS_MOUSE_BUTTON;

    //Forced like this until I fix it
//    public static int DESTROY_BUTTON = GLFW.GLFW_MOUSE_BUTTON_LEFT | IS_MOUSE_BUTTON;
//    public static int USE_BUTTON = GLFW.GLFW_MOUSE_BUTTON_RIGHT | IS_MOUSE_BUTTON;
    public static int PICK_BLOCK_BUTTON = GLFW.GLFW_MOUSE_BUTTON_3 | IS_MOUSE_BUTTON;

    public static int OPEN_INVENTORY_BUTTON = GLFW.GLFW_KEY_E | IS_KEYBOARD_BUTTON;
    public static int OPEN_DEBUG_MENU_BUTTON = GLFW.GLFW_KEY_F3 | IS_KEYBOARD_BUTTON;
    public static int TOGGLE_X_RAY_BUTTON = GLFW.GLFW_KEY_X | IS_KEYBOARD_BUTTON;
    public static int TOGGLE_NO_CLIP_BUTTON = GLFW.GLFW_KEY_G | IS_KEYBOARD_BUTTON;
    public static int USE_OCCLUSION_CULLING_BUTTON = GLFW.GLFW_KEY_C | IS_KEYBOARD_BUTTON;
    public static int SET_POSITION_1_BUTTON = GLFW.GLFW_KEY_T | IS_KEYBOARD_BUTTON;
    public static int SET_POSITION_2_BUTTON = GLFW.GLFW_KEY_Y | IS_KEYBOARD_BUTTON;

    public static final int TEXT_CHAR_SIZE_X = (int) (16 * TEXT_SIZE);
    public static final int TEXT_CHAR_SIZE_Y = (int) (24 * TEXT_SIZE);
    public static final int TEXT_LINE_SPACING = (int) (28 * TEXT_SIZE);

    public static final int RENDERED_WORLD_WIDTH = RENDER_DISTANCE_XZ * 2 + 5;
    public static final int RENDERED_WORLD_HEIGHT = RENDER_DISTANCE_Y * 2 + 5;

}
