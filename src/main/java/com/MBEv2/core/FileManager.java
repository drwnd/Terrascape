package com.MBEv2.core;

import com.MBEv2.core.entity.Player;
import com.MBEv2.test.GameLogic;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.*;

import static com.MBEv2.core.WorldGeneration.SEED;

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

    public static void init() {
        seedFile = new File(System.getProperty("user.dir") + "/src/main/resources/Saves/" + SEED);
        if (!seedFile.exists()) //noinspection ResultOfMethodCallIgnored
            seedFile.mkdirs();
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

    public static long folderSize(File directory) {
        File[] files = directory.listFiles();
        if(files == null) return -1;

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
