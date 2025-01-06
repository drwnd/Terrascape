package terrascape.dataStorage;

import terrascape.entity.GUIElement;
import terrascape.player.Player;
import terrascape.server.Block;
import terrascape.server.GameLogic;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static terrascape.utils.Constants.*;
import static terrascape.utils.Settings.*;

public class FileManager {

    private static final int CHUNK_X = 0;
    private static final int CHUNK_Y = 1;
    private static final int CHUNK_Z = 2;
    private static final int BLOCKS_LENGTH = 3;

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
    private static File heightMapFile;
    private static final Map<String, Integer> keyCodes = new HashMap<>(70);

    public static void init() {
        seedFile = new File("Saves/" + SEED);
        if (!seedFile.exists()) //noinspection ResultOfMethodCallIgnored
            seedFile.mkdirs();

        heightMapFile = new File(seedFile.getPath() + "/height_maps");
        if (!heightMapFile.exists()) //noinspection ResultOfMethodCallIgnored
            heightMapFile.mkdirs();

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
        keyCodes.put("<", GLFW.GLFW_KEY_WORLD_2 | IS_KEYBOARD_BUTTON);
        keyCodes.put("LEFT_CONTROL", GLFW.GLFW_KEY_LEFT_CONTROL | IS_KEYBOARD_BUTTON);
        keyCodes.put("LEFT_ALT", GLFW.GLFW_KEY_LEFT_ALT | IS_KEYBOARD_BUTTON);
        keyCodes.put("RIGHT_SHIFT", GLFW.GLFW_KEY_RIGHT_SHIFT | IS_KEYBOARD_BUTTON);
        keyCodes.put("RIGHT_CONTROL", GLFW.GLFW_KEY_RIGHT_CONTROL | IS_KEYBOARD_BUTTON);
        keyCodes.put("RIGHT_ALT", GLFW.GLFW_KEY_RIGHT_ALT | IS_KEYBOARD_BUTTON);
        keyCodes.put("PLUS", GLFW.GLFW_KEY_RIGHT_BRACKET);
        keyCodes.put("HASHTAG", GLFW.GLFW_KEY_BACKSLASH);
        keyCodes.put("COMMA", GLFW.GLFW_KEY_COMMA);
        keyCodes.put("POINT", GLFW.GLFW_KEY_PERIOD);
        keyCodes.put("MINUS", GLFW.GLFW_KEY_SLASH);
        keyCodes.put("^", GLFW.GLFW_KEY_GRAVE_ACCENT);
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
            File chunkFile = new File(seedFile.getPath() + "/" + chunk.id);

            if (!chunkFile.exists()) //noinspection ResultOfMethodCallIgnored
                chunkFile.createNewFile();

            FileOutputStream writer = new FileOutputStream(chunkFile.getPath());
            writer.write(toByteArray(chunk.X));
            writer.write(toByteArray(chunk.Y));
            writer.write(toByteArray(chunk.Z));

            writer.write(toByteArray(chunk.getBlockLength()));

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

        int[] ints = getInts(data, 4);

        short[] blocks = getBlocks(ints[BLOCKS_LENGTH], 16, data);

        Chunk chunk = new Chunk(ints[CHUNK_X], ints[CHUNK_Y], ints[CHUNK_Z], blocks);
        chunk.setGenerated();
        chunk.setSaved();
        Chunk.removeToGenerateBlocks(chunk.id);

        return chunk;
    }

    public static void saveAllModifiedChunks() {
        for (Chunk chunk : Chunk.getWorld()) {
            if (chunk == null) continue;
            if (chunk.isModified()) saveChunk(chunk);
        }

        for (HeightMap heightMap : Chunk.getHeightMaps()) {
            if (heightMap == null) continue;
            if (heightMap.isModified()) saveHeightMap(heightMap);
        }
    }


    public static void saveHeightMap(HeightMap heightMap) {
        try {
            File mapFile = new File(heightMapFile.getPath() + "/" + GameLogic.getHeightMapIndex(heightMap.chunkX, heightMap.chunkZ));

            if (!mapFile.exists()) //noinspection ResultOfMethodCallIgnored
                mapFile.createNewFile();

            FileOutputStream writer = new FileOutputStream(mapFile.getPath());
            writer.write(toByteArray(heightMap.chunkX));
            writer.write(toByteArray(heightMap.chunkZ));

            writer.write(toByteArray(heightMap.map));

            writer.close();
        } catch (IOException e) {
            System.out.println("saveChunk");
        }
    }

    public static HeightMap getHeightMap(int chunkX, int chunkZ) {
        File mapFile = new File(heightMapFile.getPath() + "/" + GameLogic.getHeightMapIndex(chunkX, chunkZ));
        if (!mapFile.exists()) return null;

        FileInputStream reader;

        try {
            reader = new FileInputStream(mapFile.getPath());
        } catch (FileNotFoundException e) {
            System.out.println("get Height Map 1");
            return null;
        }

        byte[] data;

        try {
            data = reader.readAllBytes();
            reader.close();
        } catch (IOException e) {
            System.out.println("get Height Map 2");
            return null;
        }

        return new HeightMap(getInts(data, CHUNK_SIZE * CHUNK_SIZE), chunkX, chunkZ);
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

            writer.write(player.getMovement().getMovementState());
            writer.write(player.getSelectedHotBarSlot());
            writer.write(player.getMovement().isFling() ? 1 : 0);
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

        if (data.length == 0) return player;

        float[] floats = readGameState(data);
        byte[] playerFlags = readPlayerFlags(data);
        short[] hotBar = readHotBar(data);

        player.getRenderer().setTime(floats[TIME]);
        player.getCamera().setPosition(floats[PLAYER_X], floats[PLAYER_Y], floats[PLAYER_Z]);
        player.getCamera().setRotation(floats[PLAYER_PITCH], floats[PLAYER_YAW]);
        player.setHotBar(hotBar);
        player.getMovement().setMovementState(playerFlags[MOVEMENT_STATE]);
        player.setSelectedHotBarSlot(playerFlags[SELECTED_HOT_BAR_SLOT]);
        player.getMovement().setFling(playerFlags[IS_FLYING] == 1);

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

    public static void loadNames() throws Exception {
        File blockTypeNames = new File("textData/BlockTypeNames");
        if (!blockTypeNames.exists()) throw new FileNotFoundException("Need to have block type names file");
        BufferedReader reader = new BufferedReader(new FileReader(blockTypeNames.getPath()));
        for (int blockType = 0; blockType < TO_PLACE_BLOCK_TYPES.length; blockType++)
            Block.setBlockTypeName(blockType, reader.readLine());


        File nonStandardBlockNames = new File("textData/NonStandardBlockNames");
        if (!nonStandardBlockNames.exists()) throw new FileNotFoundException("Need to have non standard block names file");
        reader = new BufferedReader(new FileReader(nonStandardBlockNames.getPath()));
        for (int nonStandardBlock = 0; nonStandardBlock < AMOUNT_OF_NON_STANDARD_BLOCKS; nonStandardBlock++)
            Block.setNonStandardBlockName(nonStandardBlock, reader.readLine());


        File standardBlockNames = new File("textData/standardBlockNames");
        if (!standardBlockNames.exists()) throw new FileNotFoundException("Need to have standard block names file");
        reader = new BufferedReader(new FileReader(standardBlockNames.getPath()));
        for (int standardBlock = 0; standardBlock < 256; standardBlock++)
            Block.setStandardBlockName(standardBlock, reader.readLine());


        File allBlockTypeNames = new File("textData/AllBlockTypeNames");
        if (!allBlockTypeNames.exists()) throw new FileNotFoundException("Need to have all block type names file");
        reader = new BufferedReader(new FileReader(allBlockTypeNames.getPath()));
        for (int standardBlock = 0; standardBlock < 128; standardBlock++)
            Block.setFullBlockTypeName(standardBlock, reader.readLine());
    }

    public static void loadSettings(boolean initialLoad) throws Exception {
        File settings = new File("textData/Settings");
        if (!settings.exists()) throw new FileNotFoundException("Need to have settings file");

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
        SCROLL_HOT_BAR = Boolean.parseBoolean(getStingAfterColon(reader.readLine()));
        ZOOM_BUTTON = keyCodes.get(getStingAfterColon(reader.readLine()));
        AUDIO_GAIN = Float.parseFloat(getStingAfterColon(reader.readLine()));
        STEP_GAIN = Float.parseFloat(getStingAfterColon(reader.readLine()));
        PLACE_GAIN = Float.parseFloat(getStingAfterColon(reader.readLine()));
        DIG_GAIN = Float.parseFloat(getStingAfterColon(reader.readLine()));
        MISCELLANEOUS_GAIN = Float.parseFloat(getStingAfterColon(reader.readLine()));
        INVENTORY_GAIN = Float.parseFloat(getStingAfterColon(reader.readLine()));

        reader.close();

        TEXT_CHAR_SIZE_X = (int) (16 * TEXT_SIZE);
        TEXT_CHAR_SIZE_Y = (int) (24 * TEXT_SIZE);
        TEXT_LINE_SPACING = (int) (28 * TEXT_SIZE);

        if (!initialLoad && (RENDER_DISTANCE_XZ != newRenderDistanceXZ || RENDER_DISTANCE_Y != newRenderDistanceY)) {
            RENDER_DISTANCE_XZ = newRenderDistanceXZ;
            RENDER_DISTANCE_Y = newRenderDistanceY;

            GameLogic.haltChunkGenerator();
            GameLogic.unloadChunks();

            RENDERED_WORLD_WIDTH = newRenderDistanceXZ * 2 + 5;
            RENDERED_WORLD_HEIGHT = newRenderDistanceY * 2 + 5;

            Chunk[] newWorld = new Chunk[RENDERED_WORLD_WIDTH * RENDERED_WORLD_HEIGHT * RENDERED_WORLD_WIDTH];
            short[] occlusionCullingData = new short[newWorld.length];
            for (Chunk chunk : Chunk.getWorld()) {
                if (chunk == null) continue;
                int newIndex = GameLogic.getChunkIndex(chunk.X, chunk.Y, chunk.Z);

                occlusionCullingData[newIndex] = Chunk.getOcclusionCullingData(chunk.getIndex());
                newWorld[newIndex] = chunk;

                chunk.setIndex(newIndex);
            }
            Chunk.setWorld(newWorld);
            Chunk.setOcclusionCullingData(occlusionCullingData);

            HeightMap[] newHeightMaps = new HeightMap[RENDERED_WORLD_WIDTH * RENDERED_WORLD_WIDTH];
            for (HeightMap heightMap : Chunk.getHeightMaps()) {
                if (heightMap == null) continue;
                int newIndex = GameLogic.getHeightMapIndex(heightMap.chunkX, heightMap.chunkZ);
                newHeightMaps[newIndex] = heightMap;
            }
            Chunk.setHeightMaps(newHeightMaps);
            GameLogic.getPlayer().setVisibleChunks(new long[(RENDERED_WORLD_WIDTH * RENDERED_WORLD_HEIGHT * RENDERED_WORLD_WIDTH >> 6) + 1]);

            GameLogic.startGenerator();
        }

        if (initialLoad) {
            RENDER_DISTANCE_XZ = newRenderDistanceXZ;
            RENDER_DISTANCE_Y = newRenderDistanceY;
            RENDERED_WORLD_WIDTH = newRenderDistanceXZ * 2 + 5;
            RENDERED_WORLD_HEIGHT = newRenderDistanceY * 2 + 5;
            Chunk.setWorld(new Chunk[RENDERED_WORLD_WIDTH * RENDERED_WORLD_HEIGHT * RENDERED_WORLD_WIDTH]);
            Chunk.setOcclusionCullingData(new short[RENDERED_WORLD_WIDTH * RENDERED_WORLD_HEIGHT * RENDERED_WORLD_WIDTH]);
            Chunk.setHeightMaps(new HeightMap[RENDERED_WORLD_WIDTH * RENDERED_WORLD_WIDTH]);
        }
        Player player = GameLogic.getPlayer();
        if (GUI_SIZE != newGUISize) {
            GUI_SIZE = newGUISize;
            if (player != null) {
                GUIElement.reloadGUIElements(player);
                player.updateHotBarElements();
            }
        }
    }

    private static String getStingAfterColon(String string) {
        return string.substring(string.indexOf(':') + 1);
    }


    public static Structure loadStructure(String filename) throws Exception {
        File structureFile = new File(filename);
        if (!structureFile.exists()) throw new RuntimeException("This structure file doesn't exist " + filename);

        FileInputStream reader = new FileInputStream(structureFile.getPath());
        byte[] data = reader.readAllBytes();
        reader.close();

        int lengthX = data[0];
        int lengthY = data[1];
        int lengthZ = data[2];

        short[] blocks = getBlocks(lengthX * lengthY * lengthZ, 3, data);

        return new Structure(blocks, lengthX, lengthY, lengthZ);
    }

    public static void saveStructure(Structure structure, String filename) throws Exception {
        File structureFile = new File(filename);
        if (!structureFile.exists()) //noinspection ResultOfMethodCallIgnored
            structureFile.createNewFile();

        byte[] data = toByteArray(structure.blocks());

        FileOutputStream writer = new FileOutputStream(structureFile.getPath());

        writer.write(structure.lengthX());
        writer.write(structure.lengthY());
        writer.write(structure.lengthZ());
        writer.write(data);

        writer.close();
    }


    private static short[] getBlocks(int blocksLength, int startIndex, byte[] data) {
        short[] blocks = new short[blocksLength];

        for (int i = 0; i < blocksLength; i++) {
            int index = i << 1;
            short block = (short) (((int) data[startIndex + index] & 0xFF) << 8 | ((int) data[startIndex + 1 + index] & 0xFF));
            blocks[i] = block;
        }

        return blocks;
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

    public static byte[] toByteArray(int[] ints) {
        byte[] byteArray = new byte[(ints.length << 2)];

        for (int i = 0; i < ints.length; i++) {
            byteArray[i << 2] = (byte) (ints[i] >> 24 & 0xFF);
            byteArray[(i << 2) + 1] = (byte) (ints[i] >> 16 & 0xFF);
            byteArray[(i << 2) + 2] = (byte) (ints[i] >> 8 & 0xFF);
            byteArray[(i << 2) + 3] = (byte) (ints[i] & 0xFF);
        }

        return byteArray;
    }

    private static int[] getInts(byte[] bytes, int count) {
        int[] ints = new int[count];

        for (int i = 0; i < count; i++) {
            int index = i << 2;
            ints[i] = ((int) bytes[index] & 0xFF) << 24 | ((int) bytes[index + 1] & 0xFF) << 16 | ((int) bytes[index + 2] & 0xFF) << 8 | ((int) bytes[index + 3] & 0xFF);
        }

        return ints;
    }

//        public static long getSeedFileSize() {
//        return folderSize(seedFile);
//    }
//
//    public static long folderSize(File directory) {
//        File[] files = directory.listFiles();
//        if (files == null) return -1;
//
//        long length = 0;
//        for (File file : files) {
//            if (file.isFile())
//                length += file.length();
//            else
//                length += folderSize(file);
//        }
//        return length;
//    }
}
