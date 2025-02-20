package terrascape.dataStorage;

import terrascape.entity.GUIElement;
import terrascape.entity.OpaqueModel;
import terrascape.entity.WaterModel;
import terrascape.entity.entities.Entity;
import terrascape.player.Player;
import terrascape.server.Block;
import terrascape.server.BlockEvent;
import terrascape.server.EngineManager;
import terrascape.server.ServerLogic;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import terrascape.utils.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static terrascape.utils.Constants.*;
import static terrascape.utils.Settings.*;

public final class FileManager {

    public static void init() {
        KEY_CODES.put("LEFT_CLICK", GLFW.GLFW_MOUSE_BUTTON_LEFT | IS_MOUSE_BUTTON);
        KEY_CODES.put("RIGHT_CLICK", GLFW.GLFW_MOUSE_BUTTON_RIGHT | IS_MOUSE_BUTTON);
        KEY_CODES.put("MIDDLE_CLICK", GLFW.GLFW_MOUSE_BUTTON_MIDDLE | IS_MOUSE_BUTTON);
        KEY_CODES.put("MOUSE_BUTTON_4", GLFW.GLFW_MOUSE_BUTTON_4 | IS_MOUSE_BUTTON);
        KEY_CODES.put("MOUSE_BUTTON_5", GLFW.GLFW_MOUSE_BUTTON_5 | IS_MOUSE_BUTTON);
        KEY_CODES.put("MOUSE_BUTTON_6", GLFW.GLFW_MOUSE_BUTTON_6 | IS_MOUSE_BUTTON);
        KEY_CODES.put("MOUSE_BUTTON_7", GLFW.GLFW_MOUSE_BUTTON_7 | IS_MOUSE_BUTTON);
        KEY_CODES.put("MOUSE_BUTTON_8", GLFW.GLFW_MOUSE_BUTTON_8 | IS_MOUSE_BUTTON);

        KEY_CODES.put("0", GLFW.GLFW_KEY_0 | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("1", GLFW.GLFW_KEY_1 | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("2", GLFW.GLFW_KEY_2 | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("3", GLFW.GLFW_KEY_3 | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("4", GLFW.GLFW_KEY_4 | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("5", GLFW.GLFW_KEY_5 | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("6", GLFW.GLFW_KEY_6 | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("7", GLFW.GLFW_KEY_7 | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("8", GLFW.GLFW_KEY_8 | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("9", GLFW.GLFW_KEY_9 | IS_KEYBOARD_BUTTON);

        KEY_CODES.put("F1", GLFW.GLFW_KEY_F1 | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("F2", GLFW.GLFW_KEY_F2 | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("F3", GLFW.GLFW_KEY_F3 | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("F4", GLFW.GLFW_KEY_F4 | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("F5", GLFW.GLFW_KEY_F5 | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("F6", GLFW.GLFW_KEY_F6 | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("F7", GLFW.GLFW_KEY_F7 | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("F8", GLFW.GLFW_KEY_F8 | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("F9", GLFW.GLFW_KEY_F9 | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("F10", GLFW.GLFW_KEY_F10 | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("F11", GLFW.GLFW_KEY_F11 | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("F12", GLFW.GLFW_KEY_F12 | IS_KEYBOARD_BUTTON);

        KEY_CODES.put("TAB", GLFW.GLFW_KEY_TAB | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("CAPS_LOCK", GLFW.GLFW_KEY_CAPS_LOCK | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("SPACE", GLFW.GLFW_KEY_SPACE | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("LEFT_SHIFT", GLFW.GLFW_KEY_LEFT_SHIFT | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("<", GLFW.GLFW_KEY_WORLD_2 | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("LEFT_CONTROL", GLFW.GLFW_KEY_LEFT_CONTROL | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("LEFT_ALT", GLFW.GLFW_KEY_LEFT_ALT | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("RIGHT_SHIFT", GLFW.GLFW_KEY_RIGHT_SHIFT | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("RIGHT_CONTROL", GLFW.GLFW_KEY_RIGHT_CONTROL | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("RIGHT_ALT", GLFW.GLFW_KEY_RIGHT_ALT | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("PLUS", GLFW.GLFW_KEY_RIGHT_BRACKET);
        KEY_CODES.put("HASHTAG", GLFW.GLFW_KEY_BACKSLASH);
        KEY_CODES.put("COMMA", GLFW.GLFW_KEY_COMMA);
        KEY_CODES.put("POINT", GLFW.GLFW_KEY_PERIOD);
        KEY_CODES.put("MINUS", GLFW.GLFW_KEY_SLASH);
        KEY_CODES.put("^", GLFW.GLFW_KEY_GRAVE_ACCENT);
        KEY_CODES.put("UP", GLFW.GLFW_KEY_UP | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("LEFT", GLFW.GLFW_KEY_LEFT | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("DOWN", GLFW.GLFW_KEY_DOWN | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("RIGHT", GLFW.GLFW_KEY_RIGHT | IS_KEYBOARD_BUTTON);

        KEY_CODES.put("A", GLFW.GLFW_KEY_A | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("B", GLFW.GLFW_KEY_B | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("C", GLFW.GLFW_KEY_C | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("D", GLFW.GLFW_KEY_D | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("E", GLFW.GLFW_KEY_E | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("F", GLFW.GLFW_KEY_F | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("G", GLFW.GLFW_KEY_G | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("H", GLFW.GLFW_KEY_H | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("I", GLFW.GLFW_KEY_I | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("J", GLFW.GLFW_KEY_J | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("K", GLFW.GLFW_KEY_K | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("L", GLFW.GLFW_KEY_L | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("M", GLFW.GLFW_KEY_M | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("N", GLFW.GLFW_KEY_N | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("O", GLFW.GLFW_KEY_O | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("P", GLFW.GLFW_KEY_P | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("Q", GLFW.GLFW_KEY_Q | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("R", GLFW.GLFW_KEY_R | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("S", GLFW.GLFW_KEY_S | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("T", GLFW.GLFW_KEY_T | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("U", GLFW.GLFW_KEY_U | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("V", GLFW.GLFW_KEY_V | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("W", GLFW.GLFW_KEY_W | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("X", GLFW.GLFW_KEY_X | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("Y", GLFW.GLFW_KEY_Y | IS_KEYBOARD_BUTTON);
        KEY_CODES.put("Z", GLFW.GLFW_KEY_Z | IS_KEYBOARD_BUTTON);
    }

    private static void loadUniversalFiles() {
        seedFile = new File("Saves/" + SEED);
        if (!seedFile.exists()) //noinspection ResultOfMethodCallIgnored
            seedFile.mkdirs();

        heightMapsFile = new File(seedFile.getPath() + "/height_maps");
        if (!heightMapsFile.exists()) //noinspection ResultOfMethodCallIgnored
            heightMapsFile.mkdirs();

        chunksFile = new File(seedFile.getPath() + "/chunks");
        if (!chunksFile.exists()) //noinspection ResultOfMethodCallIgnored
            chunksFile.mkdirs();
    }


    public static void saveChunk(Chunk chunk) {
        chunk.setSaved();
        try {
            File chunkFile = new File(chunksFile.getPath() + "/" + chunk.ID);

            if (!chunkFile.exists()) //noinspection ResultOfMethodCallIgnored
                chunkFile.mkdir();

            saveBlocks(chunk, chunkFile);
            saveEntities(chunk, chunkFile);
            saveBlockEvents(chunk, chunkFile);

        } catch (IOException e) {
            System.err.println("Error when saving chunk to file");
        }
    }

    public static Chunk getChunk(long id) {
        File chunkFile = new File(chunksFile.getPath() + "/" + id);
        if (!chunkFile.exists()) return null;

        byte[] blocksData, entityData, eventsData;
        try {
            blocksData = getBlocksData(chunkFile);
            entityData = getEntityData(chunkFile);
            eventsData = getEventsData(chunkFile);
        } catch (IOException e) {
            System.err.println("Error when reading chunk from file");
            e.printStackTrace();
            return null;
        }

        int[] ints = Utils.getInts(blocksData, 4);
        short[] blocks = Utils.getBlocks(ints[BLOCKS_LENGTH], 16, blocksData);
        LinkedList<Entity>[] entityClusters = new LinkedList[64];
        for (int entityClusterIndex = 0; entityClusterIndex < entityClusters.length; entityClusterIndex++)
            entityClusters[entityClusterIndex] = new LinkedList<>();

        int entityBytesIndex = 0;
        if (entityData != null)
            while (entityBytesIndex < entityData.length) {
                Entity entity = Entity.getFromBytes(entityData, entityBytesIndex);
                entityBytesIndex += entity.getByteSize();
                ServerLogic.spawnEntity(entity);
            }

        BlockEvent.addEventsFromBytes(eventsData);

        Chunk chunk = new Chunk(ints[CHUNK_X], ints[CHUNK_Y], ints[CHUNK_Z], blocks, entityClusters);
        chunk.setGenerated();
        chunk.setSaved();
        Chunk.removeToGenerateBlocks(chunk.ID);

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
            File mapFile = new File(heightMapsFile.getPath() + "/" + Utils.getHeightMapIndex(heightMap.chunkX, heightMap.chunkZ));

            if (!mapFile.exists()) //noinspection ResultOfMethodCallIgnored
                mapFile.createNewFile();

            FileOutputStream writer = new FileOutputStream(mapFile.getPath());
            writer.write(Utils.toByteArray(heightMap.chunkX));
            writer.write(Utils.toByteArray(heightMap.chunkZ));

            writer.write(Utils.toByteArray(heightMap.map));

            writer.close();
        } catch (IOException e) {
            System.out.println("saveChunk");
        }
    }

    public static HeightMap getHeightMap(int chunkX, int chunkZ) {
        File mapFile = new File(heightMapsFile.getPath() + "/" + Utils.getHeightMapIndex(chunkX, chunkZ));
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

        return new HeightMap(Utils.getInts(data, CHUNK_SIZE * CHUNK_SIZE), chunkX, chunkZ);
    }


    public static void savePlayer() {
        File playerFile = new File(seedFile.getPath() + "/player");

        try {
            if (!playerFile.exists())
                //noinspection ResultOfMethodCallIgnored
                playerFile.createNewFile();

            FileOutputStream writer = new FileOutputStream(playerFile.getPath());

            Player player = ServerLogic.getPlayer();
            if (player != null) {
                Vector3f playerPosition = player.getCamera().getPosition();
                Vector2f playerRotation = player.getCamera().getRotation();
                Vector3f playerVelocity = player.getMovement().getVelocity();

                writer.write(Utils.toByteArray(Float.floatToIntBits(playerPosition.x)));
                writer.write(Utils.toByteArray(Float.floatToIntBits(playerPosition.y)));
                writer.write(Utils.toByteArray(Float.floatToIntBits(playerPosition.z)));

                writer.write(Utils.toByteArray(Float.floatToIntBits(playerRotation.x)));
                writer.write(Utils.toByteArray(Float.floatToIntBits(playerRotation.y)));

                writer.write(Utils.toByteArray(Float.floatToIntBits(playerVelocity.x)));
                writer.write(Utils.toByteArray(Float.floatToIntBits(playerVelocity.y)));
                writer.write(Utils.toByteArray(Float.floatToIntBits(playerVelocity.z)));

                writer.write(player.getMovement().getMovementState());
                writer.write(player.getSelectedHotBarSlot());
                writer.write(player.getMovement().isFlying() ? 1 : 0);
                writer.write(Utils.toByteArray(player.getHotBar()));

                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static Player loadPlayer() throws Exception {
        Player player;

        player = new Player();
        player.init();
        player.getRenderer().init();

        File playerFile = new File(seedFile.getPath() + "/player");
        if (!playerFile.exists()) return player;

        FileInputStream reader = new FileInputStream(playerFile.getPath());
        byte[] data = reader.readAllBytes();
        reader.close();

        if (data.length == 0) return player;

        float[] floats = readPlayerState(data);
        byte[] playerFlags = readPlayerFlags(data);
        short[] hotBar = readHotBar(data);

        player.getCamera().setPosition(floats[PLAYER_X], floats[PLAYER_Y], floats[PLAYER_Z]);
        player.getCamera().setRotation(floats[PLAYER_PITCH], floats[PLAYER_YAW]);
        player.getMovement().setVelocity(floats[PLAYER_VELOCITY_X], floats[PLAYER_VELOCITY_Y], floats[PLAYER_VELOCITY_Z]);
        player.setHotBar(hotBar);
        player.getMovement().setMovementState(playerFlags[MOVEMENT_STATE]);
        player.setSelectedHotBarSlot(playerFlags[SELECTED_HOT_BAR_SLOT]);
        player.getMovement().setFlying(playerFlags[IS_FLYING] == 1);

        return player;
    }


    public static void loadGameState() throws IOException {
        File stateFile = new File(seedFile.getPath() + "/gameState");
        if (!stateFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            stateFile.createNewFile();
            return;
        }

        FileInputStream reader = new FileInputStream(stateFile.getPath());
        byte[] data = reader.readAllBytes();
        reader.close();

        if (data.length != 12) return;

        long currentTick = Utils.getLong(data, 0);
        float currentTime = Float.intBitsToFloat(Utils.getInt(data, 8));

        EngineManager.setTick(currentTick);
        ServerLogic.getPlayer().getRenderer().setTime(currentTime);
    }

    public static void saveGameState() {
        File stateFile = new File(seedFile.getPath() + "/gameState");
        try {
            if (!stateFile.exists())
                //noinspection ResultOfMethodCallIgnored
                stateFile.createNewFile();

            FileOutputStream writer = new FileOutputStream(stateFile.getPath());

            writer.write(Utils.toByteArray(EngineManager.getTick()));
            writer.write(Utils.toByteArray(Float.floatToIntBits(ServerLogic.getPlayer().getRenderer().getTime())));

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public static void loadNames() throws Exception {
        File blockTypeNames = new File("textData/BlockTypeNames");
        if (!blockTypeNames.exists()) throw new FileNotFoundException("Need to have block type names file");
        BufferedReader reader = new BufferedReader(new FileReader(blockTypeNames.getPath()));
        for (int blockType = 0; blockType < TO_PLACE_BLOCK_TYPES.length; blockType++)
            Block.setBlockTypeName(blockType, reader.readLine());


        File nonStandardBlockNames = new File("textData/NonStandardBlockNames");
        if (!nonStandardBlockNames.exists())
            throw new FileNotFoundException("Need to have non standard block names file");
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

        MOVE_FORWARD_BUTTON = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        MOVE_BACK_BUTTON = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        MOVE_RIGHT_BUTTON = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        MOVE_LEFT_BUTTON = KEY_CODES.get(getStingAfterColon(reader.readLine()));

        JUMP_BUTTON = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        SPRINT_BUTTON = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        SNEAK_BUTTON = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        CRAWL_BUTTON = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        FLY_FAST_BUTTON = KEY_CODES.get(getStingAfterColon(reader.readLine()));

        HOT_BAR_SLOT_1 = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        HOT_BAR_SLOT_2 = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        HOT_BAR_SLOT_3 = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        HOT_BAR_SLOT_4 = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        HOT_BAR_SLOT_5 = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        HOT_BAR_SLOT_6 = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        HOT_BAR_SLOT_7 = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        HOT_BAR_SLOT_8 = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        HOT_BAR_SLOT_9 = KEY_CODES.get(getStingAfterColon(reader.readLine()));

        DESTROY_BUTTON = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        USE_BUTTON = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        PICK_BLOCK_BUTTON = KEY_CODES.get(getStingAfterColon(reader.readLine()));

        OPEN_INVENTORY_BUTTON = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        OPEN_DEBUG_MENU_BUTTON = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        TOGGLE_X_RAY_BUTTON = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        TOGGLE_NO_CLIP_BUTTON = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        USE_OCCLUSION_CULLING_BUTTON = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        SET_POSITION_1_BUTTON = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        SET_POSITION_2_BUTTON = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        RELOAD_SETTINGS_BUTTON = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        SCROLL_HOT_BAR = Boolean.parseBoolean(getStingAfterColon(reader.readLine()));
        ZOOM_BUTTON = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        AUDIO_GAIN = Float.parseFloat(getStingAfterColon(reader.readLine()));
        STEP_GAIN = Float.parseFloat(getStingAfterColon(reader.readLine()));
        PLACE_GAIN = Float.parseFloat(getStingAfterColon(reader.readLine()));
        DIG_GAIN = Float.parseFloat(getStingAfterColon(reader.readLine()));
        MISCELLANEOUS_GAIN = Float.parseFloat(getStingAfterColon(reader.readLine()));
        INVENTORY_GAIN = Float.parseFloat(getStingAfterColon(reader.readLine()));
        RELOAD_SHADERS_BUTTON = KEY_CODES.get(getStingAfterColon(reader.readLine()));
        long seed = Long.parseLong(getStingAfterColon(reader.readLine()));

        reader.close();

        TEXT_CHAR_SIZE_X = (int) (16 * TEXT_SIZE);
        TEXT_CHAR_SIZE_Y = (int) (24 * TEXT_SIZE);
        TEXT_LINE_SPACING = (int) (28 * TEXT_SIZE);

        if (!initialLoad && (RENDER_DISTANCE_XZ != newRenderDistanceXZ || RENDER_DISTANCE_Y != newRenderDistanceY)) {
            RENDER_DISTANCE_XZ = newRenderDistanceXZ;
            RENDER_DISTANCE_Y = newRenderDistanceY;

            ServerLogic.haltChunkGenerator();
            ServerLogic.unloadChunks();
            ServerLogic.loadUnloadObjects();

            RENDERED_WORLD_WIDTH = newRenderDistanceXZ * 2 + 5;
            RENDERED_WORLD_HEIGHT = newRenderDistanceY * 2 + 5;

            Chunk[] newWorld = getNewWorld();
            short[] newOcclusionCullingDat = getNewOcclusionCullingData();
            HeightMap[] newHeightMaps = getNewHeightMaps();
            OpaqueModel[] newOpaqueModels = getNewOpaqueModels();
            WaterModel[] newWaterModels = getNewWaterModels();
            updateChunkIndices();
            Chunk.setStaticData(newWorld, newOcclusionCullingDat, newHeightMaps, newOpaqueModels, newWaterModels);

            ServerLogic.getPlayer().setVisibleChunks(new long[(RENDERED_WORLD_WIDTH * RENDERED_WORLD_HEIGHT * RENDERED_WORLD_WIDTH >> 6) + 1]);

            ServerLogic.startGenerator();
        }
        if (GUI_SIZE != newGUISize) {
            Player player = ServerLogic.getPlayer();
            GUI_SIZE = newGUISize;
            if (player != null) {
                GUIElement.reloadGUIElements(player);
                player.updateHotBarElements();
            }
        }

        if (initialLoad) {
            SEED = seed;
            loadUniversalFiles();
            RENDER_DISTANCE_XZ = newRenderDistanceXZ;
            RENDER_DISTANCE_Y = newRenderDistanceY;
            RENDERED_WORLD_WIDTH = newRenderDistanceXZ * 2 + 5;
            RENDERED_WORLD_HEIGHT = newRenderDistanceY * 2 + 5;
            Chunk.setStaticData(new Chunk[RENDERED_WORLD_WIDTH * RENDERED_WORLD_HEIGHT * RENDERED_WORLD_WIDTH],
                    new short[RENDERED_WORLD_WIDTH * RENDERED_WORLD_HEIGHT * RENDERED_WORLD_WIDTH],
                    new HeightMap[RENDERED_WORLD_WIDTH * RENDERED_WORLD_WIDTH],
                    new OpaqueModel[RENDERED_WORLD_WIDTH * RENDERED_WORLD_HEIGHT * RENDERED_WORLD_WIDTH],
                    new WaterModel[RENDERED_WORLD_WIDTH * RENDERED_WORLD_HEIGHT * RENDERED_WORLD_WIDTH]);
        }
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

        short[] blocks = Utils.getBlocks(lengthX * lengthY * lengthZ, 3, data);

        return new Structure(blocks, lengthX, lengthY, lengthZ);
    }

    public static void saveStructure(Structure structure, String filename) throws Exception {
        File structureFile = new File(filename);
        if (!structureFile.exists()) //noinspection ResultOfMethodCallIgnored
            structureFile.createNewFile();

        byte[] data = Utils.toByteArray(structure.blocks());

        FileOutputStream writer = new FileOutputStream(structureFile.getPath());

        writer.write(structure.lengthX());
        writer.write(structure.lengthY());
        writer.write(structure.lengthZ());
        writer.write(data);

        writer.close();
    }


    private static void saveEntities(Chunk chunk, File chunkFile) throws IOException {
        File entitiesFile = new File(chunkFile.getPath() + "/entities");

        if (!entitiesFile.exists()) //noinspection ResultOfMethodCallIgnored
            entitiesFile.createNewFile();

        FileOutputStream writer = new FileOutputStream(entitiesFile.getPath());

        for (int entityClusterIndex = 0; entityClusterIndex < 64; entityClusterIndex++) {
            LinkedList<Entity> entityCluster = chunk.getEntityCluster(entityClusterIndex);
            synchronized (entityCluster) {
                for (Entity entity : entityCluster) {
                    byte[] bytes = entity.toBytes();
                    writer.write(bytes);
                }
            }
        }

        writer.close();
    }

    private static void saveBlocks(Chunk chunk, File chunkFile) throws IOException {
        File blocksFile = new File(chunkFile.getPath() + "/blocks");

        if (!blocksFile.exists()) //noinspection ResultOfMethodCallIgnored
            blocksFile.createNewFile();

        FileOutputStream writer = new FileOutputStream(blocksFile.getPath());
        writer.write(Utils.toByteArray(chunk.X));
        writer.write(Utils.toByteArray(chunk.Y));
        writer.write(Utils.toByteArray(chunk.Z));
        writer.write(Utils.toByteArray(chunk.getBlockLength()));

        writer.write(Utils.toByteArray(chunk.getBlocks()));

        writer.close();
    }

    private static void saveBlockEvents(Chunk chunk, File chunkFile) throws IOException {
        File eventsFile = new File(chunkFile.getPath() + "/events");

        if (!eventsFile.exists()) //noinspection ResultOfMethodCallIgnored
            eventsFile.createNewFile();

        ArrayList<BlockEvent> events = BlockEvent.removeEventsInChunk(chunk);
        FileOutputStream writer = new FileOutputStream(eventsFile.getPath());


        for (BlockEvent event : events) {
            writer.write(event.type());
            writer.write(Utils.toByteArray(event.x()));
            writer.write(Utils.toByteArray(event.y()));
            writer.write(Utils.toByteArray(event.z()));
        }

        writer.close();
    }

    private static Chunk[] getNewWorld() {
        Chunk[] newWorld = new Chunk[RENDERED_WORLD_WIDTH * RENDERED_WORLD_HEIGHT * RENDERED_WORLD_WIDTH];
        for (Chunk chunk : Chunk.getWorld()) {
            if (chunk == null) continue;
            int newIndex = Utils.getChunkIndex(chunk.X, chunk.Y, chunk.Z);

            newWorld[newIndex] = chunk;
        }
        return newWorld;
    }

    private static short[] getNewOcclusionCullingData() {
        short[] occlusionCullingData = new short[RENDERED_WORLD_WIDTH * RENDERED_WORLD_HEIGHT * RENDERED_WORLD_WIDTH];
        for (Chunk chunk : Chunk.getWorld()) {
            if (chunk == null) continue;
            int newIndex = Utils.getChunkIndex(chunk.X, chunk.Y, chunk.Z);

            occlusionCullingData[newIndex] = Chunk.getOcclusionCullingData(chunk.getIndex());
        }
        return occlusionCullingData;
    }

    private static HeightMap[] getNewHeightMaps() {
        HeightMap[] newHeightMaps = new HeightMap[RENDERED_WORLD_WIDTH * RENDERED_WORLD_WIDTH];
        for (HeightMap heightMap : Chunk.getHeightMaps()) {
            if (heightMap == null) continue;
            int newIndex = Utils.getHeightMapIndex(heightMap.chunkX, heightMap.chunkZ);
            newHeightMaps[newIndex] = heightMap;
        }
        return newHeightMaps;
    }

    private static OpaqueModel[] getNewOpaqueModels() {
        OpaqueModel[] newOpaqueModels = new OpaqueModel[RENDERED_WORLD_WIDTH * RENDERED_WORLD_HEIGHT * RENDERED_WORLD_WIDTH];
        for (Chunk chunk : Chunk.getWorld()) {
            if (chunk == null) continue;
            int newIndex = Utils.getChunkIndex(chunk.X, chunk.Y, chunk.Z);

            newOpaqueModels[newIndex] = Chunk.getOpaqueModel(chunk.getIndex());
        }
        return newOpaqueModels;
    }

    private static WaterModel[] getNewWaterModels() {
        WaterModel[] newWaterModels = new WaterModel[RENDERED_WORLD_WIDTH * RENDERED_WORLD_HEIGHT * RENDERED_WORLD_WIDTH];
        for (Chunk chunk : Chunk.getWorld()) {
            if (chunk == null) continue;
            int newIndex = Utils.getChunkIndex(chunk.X, chunk.Y, chunk.Z);

            newWaterModels[newIndex] = Chunk.getWaterModel(chunk.getIndex());
        }
        return newWaterModels;
    }

    private static void updateChunkIndices() {
        for (Chunk chunk : Chunk.getWorld()) {
            if (chunk == null) continue;
            int newIndex = Utils.getChunkIndex(chunk.X, chunk.Y, chunk.Z);
            chunk.setIndex(newIndex);
        }
    }

    private static String getStingAfterColon(String string) {
        return string.substring(string.indexOf(':') + 1);
    }

    private static float[] readPlayerState(byte[] bytes) {
        float[] floats = new float[8];

        for (int i = 0; i < floats.length; i++) {
            int index = i << 2;
            int intFloat = ((int) bytes[index] & 0xFF) << 24 | ((int) bytes[index + 1] & 0xFF) << 16 | ((int) bytes[index + 2] & 0xFF) << 8 | ((int) bytes[index + 3] & 0xFF);
            floats[i] = Float.intBitsToFloat(intFloat);
        }

        return floats;
    }

    private static byte[] readPlayerFlags(byte[] bytes) {
        byte[] flags = new byte[3];

        System.arraycopy(bytes, 32, flags, 0, flags.length);

        return flags;
    }

    private static short[] readHotBar(byte[] bytes) {
        short[] hotBar = new short[9];

        for (int i = 0; i < hotBar.length; i++) {
            int index = i << 1;
            short block = (short) (((int) bytes[35 + index] & 0xFF) << 8 | ((int) bytes[36 + index] & 0xFF));
            hotBar[i] = block;
        }

        return hotBar;
    }

    private static byte[] getEntityData(File chunkFile) throws IOException {
        File entitiesFile = new File(chunkFile.getPath() + "/entities");
        if (!entitiesFile.exists()) return null;
        FileInputStream reader = new FileInputStream(entitiesFile.getPath());
        byte[] entityData = reader.readAllBytes();
        reader.close();
        //noinspection ResultOfMethodCallIgnored
        entitiesFile.delete();
        return entityData;
    }

    private static byte[] getEventsData(File chunkFile) throws IOException {
        File eventsFile = new File(chunkFile.getPath() + "/events");
        if (!eventsFile.exists()) return null;
        FileInputStream reader = new FileInputStream(eventsFile.getPath());
        byte[] eventsData = reader.readAllBytes();
        reader.close();
        //noinspection ResultOfMethodCallIgnored
        eventsFile.delete();
        return eventsData;
    }

    private static byte[] getBlocksData(File chunkFile) throws IOException {
        FileInputStream reader = new FileInputStream(chunkFile.getPath() + "/blocks");
        byte[] blocksData = reader.readAllBytes();
        reader.close();
        return blocksData;
    }

    private static final int CHUNK_X = 0;
    private static final int CHUNK_Y = 1;
    private static final int CHUNK_Z = 2;
    private static final int BLOCKS_LENGTH = 3;

    private static final int PLAYER_X = 0;
    private static final int PLAYER_Y = 1;
    private static final int PLAYER_Z = 2;
    private static final int PLAYER_PITCH = 3;
    private static final int PLAYER_YAW = 4;
    private static final int PLAYER_VELOCITY_X = 5;
    private static final int PLAYER_VELOCITY_Y = 6;
    private static final int PLAYER_VELOCITY_Z = 7;
    private static final int MOVEMENT_STATE = 0;
    private static final int SELECTED_HOT_BAR_SLOT = 1;
    private static final int IS_FLYING = 2;

    private static File seedFile;
    private static File heightMapsFile;
    private static File chunksFile;
    private static final Map<String, Integer> KEY_CODES = new HashMap<>(70);

    private FileManager() {
    }
}
