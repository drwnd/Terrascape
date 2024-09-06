package com.MBEv2.core;

import com.MBEv2.core.entity.Player;
import com.MBEv2.test.GameLogic;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static com.MBEv2.core.utils.Constants.*;
import static com.MBEv2.core.utils.Settings.*;

public class FileManager {

    private static final int CHUNK_X = 0;
    private static final int CHUNK_Y = 1;
    private static final int CHUNK_Z = 2;
    private static final int LIGHT_LENGTH = 3;
    private static final int BLOCKS_LENGTH = 4;

    private static final int TIME = 0;
    private static final int PLAYER_X = 1;
    private static final int PLAYER_Y = 2;
    private static final int PLAYER_Z = 3;
    private static final int PLAYER_PITCH = 4;
    private static final int PLAYER_YAW = 5;
    private static final int MOVEMENT_STATE = 0;
    private static final int SELECTED_HOT_BAR_SLOT = 1;
    private static final int IS_FLYING = 2;

    private static File seedFile;
    private static final Map<String, Integer> keyCodes = new HashMap<>(70);

    public static void init() {
        seedFile = new File(System.getProperty("user.dir") + "/src/main/resources/Saves/" + SEED);
        if (!seedFile.exists()) //noinspection ResultOfMethodCallIgnored
            seedFile.mkdirs();

        keyCodes.put("LEFT_CLICK", GLFW.GLFW_MOUSE_BUTTON_LEFT | IS_MOUSE_BUTTON);
        keyCodes.put("RIGHT_CLICK", GLFW.GLFW_MOUSE_BUTTON_RIGHT | IS_MOUSE_BUTTON);
        keyCodes.put("MIDDLE_CLICK", GLFW.GLFW_MOUSE_BUTTON_MIDDLE | IS_MOUSE_BUTTON);
        keyCodes.put("MOUSE_BUTTON_4", GLFW.GLFW_MOUSE_BUTTON_4 | IS_MOUSE_BUTTON);
        keyCodes.put("MOUSE_BUTTON_5", GLFW.GLFW_MOUSE_BUTTON_5 | IS_MOUSE_BUTTON);
        keyCodes.put("MOUSE_BUTTON_6", GLFW.GLFW_MOUSE_BUTTON_6 | IS_MOUSE_BUTTON);
        keyCodes.put("MOUSE_BUTTON_7", GLFW.GLFW_MOUSE_BUTTON_7 | IS_MOUSE_BUTTON);
        keyCodes.put("MOUSE_BUTTON_8", GLFW.GLFW_MOUSE_BUTTON_8 | IS_MOUSE_BUTTON);

        keyCodes.put("0", GLFW.GLFW_KEY_0 | IS_KEYBOARD_BUTTON);
        keyCodes.put("1", GLFW.GLFW_KEY_1 | IS_KEYBOARD_BUTTON);
        keyCodes.put("2", GLFW.GLFW_KEY_2 | IS_KEYBOARD_BUTTON);
        keyCodes.put("3", GLFW.GLFW_KEY_3 | IS_KEYBOARD_BUTTON);
        keyCodes.put("4", GLFW.GLFW_KEY_4 | IS_KEYBOARD_BUTTON);
        keyCodes.put("5", GLFW.GLFW_KEY_5 | IS_KEYBOARD_BUTTON);
        keyCodes.put("6", GLFW.GLFW_KEY_6 | IS_KEYBOARD_BUTTON);
        keyCodes.put("7", GLFW.GLFW_KEY_7 | IS_KEYBOARD_BUTTON);
        keyCodes.put("8", GLFW.GLFW_KEY_8 | IS_KEYBOARD_BUTTON);
        keyCodes.put("9", GLFW.GLFW_KEY_9 | IS_KEYBOARD_BUTTON);

        keyCodes.put("F1", GLFW.GLFW_KEY_F1 | IS_KEYBOARD_BUTTON);
        keyCodes.put("F2", GLFW.GLFW_KEY_F2 | IS_KEYBOARD_BUTTON);
        keyCodes.put("F3", GLFW.GLFW_KEY_F3 | IS_KEYBOARD_BUTTON);
        keyCodes.put("F4", GLFW.GLFW_KEY_F4 | IS_KEYBOARD_BUTTON);
        keyCodes.put("F5", GLFW.GLFW_KEY_F5 | IS_KEYBOARD_BUTTON);
        keyCodes.put("F6", GLFW.GLFW_KEY_F6 | IS_KEYBOARD_BUTTON);
        keyCodes.put("F7", GLFW.GLFW_KEY_F7 | IS_KEYBOARD_BUTTON);
        keyCodes.put("F8", GLFW.GLFW_KEY_F8 | IS_KEYBOARD_BUTTON);
        keyCodes.put("F9", GLFW.GLFW_KEY_F9 | IS_KEYBOARD_BUTTON);
        keyCodes.put("F10", GLFW.GLFW_KEY_F10 | IS_KEYBOARD_BUTTON);
        keyCodes.put("F11", GLFW.GLFW_KEY_F11 | IS_KEYBOARD_BUTTON);
        keyCodes.put("F12", GLFW.GLFW_KEY_F12 | IS_KEYBOARD_BUTTON);

        keyCodes.put("TAB", GLFW.GLFW_KEY_TAB | IS_KEYBOARD_BUTTON);
        keyCodes.put("CAPS_LOCK", GLFW.GLFW_KEY_CAPS_LOCK | IS_KEYBOARD_BUTTON);
        keyCodes.put("SPACE", GLFW.GLFW_KEY_SPACE | IS_KEYBOARD_BUTTON);
        keyCodes.put("LEFT_SHIFT", GLFW.GLFW_KEY_LEFT_SHIFT | IS_KEYBOARD_BUTTON);
        keyCodes.put("LEFT_CONTROL", GLFW.GLFW_KEY_LEFT_CONTROL | IS_KEYBOARD_BUTTON);
        keyCodes.put("LEFT_ALT", GLFW.GLFW_KEY_LEFT_ALT | IS_KEYBOARD_BUTTON);
        keyCodes.put("RIGHT_SHIFT", GLFW.GLFW_KEY_RIGHT_SHIFT | IS_KEYBOARD_BUTTON);
        keyCodes.put("RIGHT_CONTROL", GLFW.GLFW_KEY_RIGHT_CONTROL | IS_KEYBOARD_BUTTON);
        keyCodes.put("RIGHT_ALT", GLFW.GLFW_KEY_RIGHT_ALT | IS_KEYBOARD_BUTTON);
        keyCodes.put("UP", GLFW.GLFW_KEY_UP | IS_KEYBOARD_BUTTON);
        keyCodes.put("LEFT", GLFW.GLFW_KEY_LEFT | IS_KEYBOARD_BUTTON);
        keyCodes.put("DOWN", GLFW.GLFW_KEY_DOWN | IS_KEYBOARD_BUTTON);
        keyCodes.put("RIGHT", GLFW.GLFW_KEY_RIGHT | IS_KEYBOARD_BUTTON);

        keyCodes.put("A", GLFW.GLFW_KEY_A | IS_KEYBOARD_BUTTON);
        keyCodes.put("B", GLFW.GLFW_KEY_B | IS_KEYBOARD_BUTTON);
        keyCodes.put("C", GLFW.GLFW_KEY_C | IS_KEYBOARD_BUTTON);
        keyCodes.put("D", GLFW.GLFW_KEY_D | IS_KEYBOARD_BUTTON);
        keyCodes.put("E", GLFW.GLFW_KEY_E | IS_KEYBOARD_BUTTON);
        keyCodes.put("F", GLFW.GLFW_KEY_F | IS_KEYBOARD_BUTTON);
        keyCodes.put("G", GLFW.GLFW_KEY_G | IS_KEYBOARD_BUTTON);
        keyCodes.put("H", GLFW.GLFW_KEY_H | IS_KEYBOARD_BUTTON);
        keyCodes.put("I", GLFW.GLFW_KEY_I | IS_KEYBOARD_BUTTON);
        keyCodes.put("J", GLFW.GLFW_KEY_J | IS_KEYBOARD_BUTTON);
        keyCodes.put("K", GLFW.GLFW_KEY_K | IS_KEYBOARD_BUTTON);
        keyCodes.put("L", GLFW.GLFW_KEY_L | IS_KEYBOARD_BUTTON);
        keyCodes.put("M", GLFW.GLFW_KEY_M | IS_KEYBOARD_BUTTON);
        keyCodes.put("N", GLFW.GLFW_KEY_N | IS_KEYBOARD_BUTTON);
        keyCodes.put("O", GLFW.GLFW_KEY_O | IS_KEYBOARD_BUTTON);
        keyCodes.put("P", GLFW.GLFW_KEY_P | IS_KEYBOARD_BUTTON);
        keyCodes.put("Q", GLFW.GLFW_KEY_Q | IS_KEYBOARD_BUTTON);
        keyCodes.put("R", GLFW.GLFW_KEY_R | IS_KEYBOARD_BUTTON);
        keyCodes.put("S", GLFW.GLFW_KEY_S | IS_KEYBOARD_BUTTON);
        keyCodes.put("T", GLFW.GLFW_KEY_T | IS_KEYBOARD_BUTTON);
        keyCodes.put("U", GLFW.GLFW_KEY_U | IS_KEYBOARD_BUTTON);
        keyCodes.put("V", GLFW.GLFW_KEY_V | IS_KEYBOARD_BUTTON);
        keyCodes.put("W", GLFW.GLFW_KEY_W | IS_KEYBOARD_BUTTON);
        keyCodes.put("X", GLFW.GLFW_KEY_X | IS_KEYBOARD_BUTTON);
        keyCodes.put("Y", GLFW.GLFW_KEY_Y | IS_KEYBOARD_BUTTON);
        keyCodes.put("Z", GLFW.GLFW_KEY_Z | IS_KEYBOARD_BUTTON);
    }

    public static void saveChunk(Chunk chunk) {
        chunk.setSaved();
        try {
            File chunkFile = new File(seedFile.getPath() + "/" + chunk.getId());

            if (!chunkFile.exists()) //noinspection ResultOfMethodCallIgnored
                chunkFile.createNewFile();

            FileOutputStream writer = new FileOutputStream(chunkFile.getPath());
            writer.write(toByteArray(chunk.getChunkX()));
            writer.write(toByteArray(chunk.getChunkY()));
            writer.write(toByteArray(chunk.getChunkZ()));

            writer.write(toByteArray(chunk.getLightLength()));
            writer.write(toByteArray(chunk.getBlockLength()));

            writer.write(chunk.getLight());
            writer.write(toByteArray(chunk.getBlocks()));

            writer.close();
        } catch (IOException e) {
            System.out.println("saveChunk");
        }
    }

    public static Chunk getChunk(long id) {
        File chunkFile = new File(seedFile.getPath() + "/" + id);
        if (!chunkFile.exists()) return null;

        FileInputStream reader;

        try {
            reader = new FileInputStream(chunkFile.getPath());
        } catch (FileNotFoundException e) {
            System.out.println("getChunk 1");
            return null;
        }

        byte[] data;

        try {
            data = reader.readAllBytes();
            reader.close();
        } catch (IOException e) {
            System.out.println("getChunk 2");
            return null;
        }

        int[] ints = getInts(data);

        byte[] light = getLight(ints[LIGHT_LENGTH], data);
        short[] blocks = getBlocks(ints[LIGHT_LENGTH], ints[BLOCKS_LENGTH], data);

        Chunk chunk = new Chunk(ints[CHUNK_X], ints[CHUNK_Y], ints[CHUNK_Z], light, blocks);
        chunk.setGenerated();
        chunk.setHasPropagatedBlockLight();
        chunk.setSaved();
        Chunk.removeToGenerateBlocks(chunk.getId());

        return chunk;
    }

    private static byte[] toByteArray(int i) {
        return new byte[]{(byte) (i >> 24 & 0xFF), (byte) (i >> 16 & 0xFF), (byte) (i >> 8 & 0xFF), (byte) (i & 0xFF)};
    }

    private static byte[] toByteArray(short[] blocks) {
        byte[] byteArray = new byte[blocks.length * 2];

        for (int i = 0; i < blocks.length; i++) {
            byteArray[i << 1] = (byte) (blocks[i] >> 8 & 0xFF);
            byteArray[(i << 1) + 1] = (byte) (blocks[i] & 0xFF);
        }

        return byteArray;
    }

    private static int[] getInts(byte[] bytes) {
        int[] ints = new int[5];

        for (int i = 0; i < ints.length; i++) {
            int index = i << 2;
            ints[i] = ((int) bytes[index] & 0xFF) << 24 | ((int) bytes[index + 1] & 0xFF) << 16 | ((int) bytes[index + 2] & 0xFF) << 8 | ((int) bytes[index + 3] & 0xFF);
        }

        return ints;
    }

    private static byte[] getLight(int lightLength, byte[] data) {
        byte[] light = new byte[lightLength];

        System.arraycopy(data, 20, light, 0, lightLength);

        return light;
    }

    private static short[] getBlocks(int lightLength, int blocksLength, byte[] data) {
        short[] blocks = new short[blocksLength];

        for (int i = 0; i < blocksLength; i++) {
            int index = i << 1;
            short block = (short) (((int) data[20 + lightLength + index] & 0xFF) << 8 | ((int) data[21 + lightLength + index] & 0xFF));
            blocks[i] = block;
        }

        return blocks;
    }

    public static void saveAllModifiedChunks() {
        for (Chunk chunk : Chunk.getWorld()) {
            if (chunk == null) continue;
            if (chunk.isModified()) saveChunk(chunk);
        }
    }


    public static void saveGameState() {
        File stateFile = new File(seedFile.getPath() + "/gameState");

        try {
            if (!stateFile.exists())
                //noinspection ResultOfMethodCallIgnored
                stateFile.createNewFile();

            FileOutputStream writer = new FileOutputStream(stateFile.getPath());

            Player player = GameLogic.getPlayer();
            writer.write(toByteArray(Float.floatToIntBits(player.getRenderer().getTime())));

            Vector3f playerPosition = player.getCamera().getPosition();
            Vector2f playerRotation = player.getCamera().getRotation();

            writer.write(toByteArray(Float.floatToIntBits(playerPosition.x)));
            writer.write(toByteArray(Float.floatToIntBits(playerPosition.y)));
            writer.write(toByteArray(Float.floatToIntBits(playerPosition.z)));

            writer.write(toByteArray(Float.floatToIntBits(playerRotation.x)));
            writer.write(toByteArray(Float.floatToIntBits(playerRotation.y)));

            writer.write(player.getMovementState());
            writer.write(player.getSelectedHotBarSlot());
            writer.write(player.isFling() ? 1 : 0);
            writer.write(toByteArray(player.getHotBar()));

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Player loadGameState() throws Exception {
        Player player;

        player = new Player();
        player.init();
        player.getRenderer().init();

        File stateFile = new File(seedFile.getPath() + "/gameState");
        if (!stateFile.exists()) return player;

        FileInputStream reader = new FileInputStream(stateFile.getPath());
        byte[] data = reader.readAllBytes();
        reader.close();

        float[] floats = readGameState(data);
        byte[] playerFlags = readPlayerFlags(data);
        short[] hotBar = readHotBar(data);

        player.getRenderer().setTime(floats[TIME]);
        player.getCamera().setPosition(floats[PLAYER_X], floats[PLAYER_Y], floats[PLAYER_Z]);
        player.getCamera().setRotation(floats[PLAYER_PITCH], floats[PLAYER_YAW]);
        player.setHotBar(hotBar);
        player.setMovementState(playerFlags[MOVEMENT_STATE]);
        player.setSelectedHotBarSlot(playerFlags[SELECTED_HOT_BAR_SLOT]);
        player.setFling(playerFlags[IS_FLYING] == 1);

        return player;
    }

    private static float[] readGameState(byte[] bytes) {
        float[] floats = new float[6];

        for (int i = 0; i < floats.length; i++) {
            int index = i << 2;
            int intFloat = ((int) bytes[index] & 0xFF) << 24 | ((int) bytes[index + 1] & 0xFF) << 16 | ((int) bytes[index + 2] & 0xFF) << 8 | ((int) bytes[index + 3] & 0xFF);
            floats[i] = Float.intBitsToFloat(intFloat);
        }

        return floats;
    }

    private static byte[] readPlayerFlags(byte[] bytes) {
        byte[] flags = new byte[3];

        System.arraycopy(bytes, 24, flags, 0, flags.length);

        return flags;
    }

    private static short[] readHotBar(byte[] bytes) {
        short[] hotBar = new short[9];

        for (int i = 0; i < hotBar.length; i++) {
            int index = i << 1;
            short block = (short) (((int) bytes[27 + index] & 0xFF) << 8 | ((int) bytes[28 + index] & 0xFF));
            hotBar[i] = block;
        }

        return hotBar;
    }

    public static long getSeedFileSize() {
        return folderSize(seedFile);
    }

    public static void loadSettings(boolean initialLoad) throws Exception {
        File settings = new File(System.getProperty("user.dir") + "/src/main/resources/Settings");
        if (!settings.exists()) {
            //noinspection ResultOfMethodCallIgnored
            settings.createNewFile();
            throw new FileNotFoundException("Need to have settings file");
        }

        BufferedReader reader = new BufferedReader(new FileReader(settings.getPath()));

        FOV = (float) Math.toRadians(Float.parseFloat(getStingAfterColon(reader.readLine())));
        float newGUISize = Float.parseFloat(getStingAfterColon(reader.readLine()));
        MOUSE_SENSITIVITY = Float.parseFloat(getStingAfterColon(reader.readLine()));
        REACH = Float.parseFloat(getStingAfterColon(reader.readLine()));
        TEXT_SIZE = Float.parseFloat(getStingAfterColon(reader.readLine()));

        int newRenderDistanceXZ = Integer.parseInt(getStingAfterColon(reader.readLine()));
        int newRenderDistanceY = Integer.parseInt(getStingAfterColon(reader.readLine()));

        MOVE_FORWARD_BUTTON = keyCodes.get(getStingAfterColon(reader.readLine()));
        MOVE_BACK_BUTTON = keyCodes.get(getStingAfterColon(reader.readLine()));
        MOVE_RIGHT_BUTTON = keyCodes.get(getStingAfterColon(reader.readLine()));
        MOVE_LEFT_BUTTON = keyCodes.get(getStingAfterColon(reader.readLine()));

        JUMP_BUTTON = keyCodes.get(getStingAfterColon(reader.readLine()));
        SPRINT_BUTTON = keyCodes.get(getStingAfterColon(reader.readLine()));
        SNEAK_BUTTON = keyCodes.get(getStingAfterColon(reader.readLine()));
        CRAWL_BUTTON = keyCodes.get(getStingAfterColon(reader.readLine()));
        FLY_FAST_BUTTON = keyCodes.get(getStingAfterColon(reader.readLine()));

        HOT_BAR_SLOT_1 = keyCodes.get(getStingAfterColon(reader.readLine()));
        HOT_BAR_SLOT_2 = keyCodes.get(getStingAfterColon(reader.readLine()));
        HOT_BAR_SLOT_3 = keyCodes.get(getStingAfterColon(reader.readLine()));
        HOT_BAR_SLOT_4 = keyCodes.get(getStingAfterColon(reader.readLine()));
        HOT_BAR_SLOT_5 = keyCodes.get(getStingAfterColon(reader.readLine()));
        HOT_BAR_SLOT_6 = keyCodes.get(getStingAfterColon(reader.readLine()));
        HOT_BAR_SLOT_7 = keyCodes.get(getStingAfterColon(reader.readLine()));
        HOT_BAR_SLOT_8 = keyCodes.get(getStingAfterColon(reader.readLine()));
        HOT_BAR_SLOT_9 = keyCodes.get(getStingAfterColon(reader.readLine()));

        DESTROY_BUTTON = keyCodes.get(getStingAfterColon(reader.readLine()));
        USE_BUTTON = keyCodes.get(getStingAfterColon(reader.readLine()));
        PICK_BLOCK_BUTTON = keyCodes.get(getStingAfterColon(reader.readLine()));

        OPEN_INVENTORY_BUTTON = keyCodes.get(getStingAfterColon(reader.readLine()));
        OPEN_DEBUG_MENU_BUTTON = keyCodes.get(getStingAfterColon(reader.readLine()));
        TOGGLE_X_RAY_BUTTON = keyCodes.get(getStingAfterColon(reader.readLine()));
        TOGGLE_NO_CLIP_BUTTON = keyCodes.get(getStingAfterColon(reader.readLine()));
        USE_OCCLUSION_CULLING_BUTTON = keyCodes.get(getStingAfterColon(reader.readLine()));
        SET_POSITION_1_BUTTON = keyCodes.get(getStingAfterColon(reader.readLine()));
        SET_POSITION_2_BUTTON = keyCodes.get(getStingAfterColon(reader.readLine()));
        RELOAD_SETTINGS_BUTTON = keyCodes.get(getStingAfterColon(reader.readLine()));

        reader.close();

        TEXT_CHAR_SIZE_X = (int) (16 * TEXT_SIZE);
        TEXT_CHAR_SIZE_Y = (int) (24 * TEXT_SIZE);
        TEXT_LINE_SPACING = (int) (28 * TEXT_SIZE);

        RENDERED_WORLD_WIDTH = newRenderDistanceXZ * 2 + 5;
        RENDERED_WORLD_HEIGHT = newRenderDistanceY * 2 + 5;

        if (!initialLoad && (RENDER_DISTANCE_XZ != newRenderDistanceXZ || RENDER_DISTANCE_Y != newRenderDistanceY)) {
            RENDER_DISTANCE_XZ = newRenderDistanceXZ;
            RENDER_DISTANCE_Y = newRenderDistanceY;

            GameLogic.haltChunkGenerator();
            GameLogic.unloadChunks();

            Chunk[] newWorld = new Chunk[RENDERED_WORLD_WIDTH * RENDERED_WORLD_HEIGHT * RENDERED_WORLD_WIDTH];
            for (Chunk chunk : Chunk.getWorld()) {
                if (chunk == null) continue;
                int newIndex = GameLogic.getChunkIndex(chunk.getChunkX(), chunk.getChunkY(), chunk.getChunkZ());
                chunk.setIndex(newIndex);
                newWorld[newIndex] = chunk;
            }
            Chunk.setWorld(newWorld);
            Chunk.setHeightMap(new int[RENDERED_WORLD_WIDTH * RENDERED_WORLD_WIDTH][CHUNK_SIZE * CHUNK_SIZE]);
            GameLogic.getPlayer().setVisibleChunks(new long[(RENDERED_WORLD_WIDTH * RENDERED_WORLD_HEIGHT * RENDERED_WORLD_WIDTH >> 6) + 1]);

            GameLogic.startGenerator();
        }
        if (initialLoad) {
            RENDER_DISTANCE_XZ = newRenderDistanceXZ;
            RENDER_DISTANCE_Y = newRenderDistanceY;
            Chunk.setWorld(new Chunk[RENDERED_WORLD_WIDTH * RENDERED_WORLD_HEIGHT * RENDERED_WORLD_WIDTH]);
            Chunk.setHeightMap(new int[RENDERED_WORLD_WIDTH * RENDERED_WORLD_WIDTH][CHUNK_SIZE * CHUNK_SIZE]);
        }
        if (GUI_SIZE != newGUISize) {
            GUI_SIZE = newGUISize;
            Player player = GameLogic.getPlayer();
            if (player != null) player.reloadGUIElements();
        }
    }

    private static String getStingAfterColon(String string) {
        return string.substring(string.indexOf(':') + 1);
    }


    public static long folderSize(File directory) {
        File[] files = directory.listFiles();
        if (files == null) return -1;

        long length = 0;
        for (File file : files) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }


}