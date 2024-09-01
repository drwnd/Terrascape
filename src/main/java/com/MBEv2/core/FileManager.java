package com.MBEv2.core;

import java.io.*;

import static com.MBEv2.core.WorldGeneration.SEED;

public class FileManager {

    private static final int CHUNK_X = 0;
    private static final int CHUNK_Y = 1;
    private static final int CHUNK_Z = 2;
    private static final int LIGHT_LENGTH = 3;
    private static final int BLOCKS_LENGTH = 4;

    private static File seedFile;

    public static void init() {
        seedFile = new File(System.getProperty("user.dir") + "/src/main/resources/Saves/" + SEED);
        if (!seedFile.exists()) //noinspection ResultOfMethodCallIgnored
            seedFile.mkdirs();
    }

    public static void saveChunk(Chunk chunk) {
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

        int fileSize = (int) chunkFile.length();
        byte[] data = new byte[fileSize];

        try {
            if (fileSize != reader.read(data)) {
                System.out.println("Something went wrong reading the file");
                return null;
            }
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
}
