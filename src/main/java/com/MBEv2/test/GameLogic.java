package com.MBEv2.test;

import com.MBEv2.core.*;
import com.MBEv2.core.entity.*;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;
import java.util.Random;

import static com.MBEv2.core.utils.Constants.*;

public class GameLogic {

    private static Texture atlas;
    private static final LinkedList<Chunk> toBufferChunks = new LinkedList<>();
    private static final LinkedList<Chunk> toUnloadChunks = new LinkedList<>();
    private static ChunkGenerator generator;

    private static Player player;

    public static void init() throws Exception {

        generator = new ChunkGenerator();

        atlas = new Texture(ObjectLoader.loadTexture("textures/atlas256.png"));

        player = new Player(atlas);
        player.init();

        player.getRenderer().init();
        generator.continueRunning();
    }

    public static void loadUnloadChunks() {
        generator.continueRunning();
    }

    public static void placeBlock(byte block, Vector3i position) {
        if (position == null)
            return;
        if (Chunk.getBlockInWorld(position.x, position.y, position.z) == block)
            return;

        int chunkX = position.x >> CHUNK_SIZE_BITS;
        int chunkY = position.y >> CHUNK_SIZE_BITS;
        int chunkZ = position.z >> CHUNK_SIZE_BITS;

        int inChunkX = position.x & CHUNK_SIZE - 1;
        int inChunkY = position.y & CHUNK_SIZE - 1;
        int inChunkZ = position.z & CHUNK_SIZE - 1;

        Chunk chunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
        chunk.storeSave(inChunkX, inChunkY, inChunkZ, block);
        chunk.setModified();

        int minX = chunkX, maxX = chunkX;
        int minY = chunkY, maxY = chunkY;
        int minZ = chunkZ, maxZ = chunkZ;

        if (inChunkX == 0)
            minX--;
        else if (inChunkX == CHUNK_SIZE - 1)
            maxX++;
        if (inChunkY == 0)
            minY--;
        else if (inChunkY == CHUNK_SIZE - 1)
            maxY++;
        if (inChunkZ == 0)
            minZ--;
        else if (inChunkZ == CHUNK_SIZE - 1)
            maxZ++;

        for (int x = minX; x <= maxX; x++)
            for (int y = minY; y <= maxY; y++)
                for (int z = minZ; z <= maxZ; z++) {
                    Chunk toMeshChunk = Chunk.getChunk(x, y, z);
                    if (toMeshChunk != null)
                        toMeshChunk.setMeshed(false);
                }
        generator.continueRunning();
    }

    public static void bufferChunkMesh(Chunk chunk) {
        if (chunk.getVertices() != null && chunk.getVertices().length != 0) {
            Model model = ObjectLoader.loadModel(chunk.getVertices(), chunk.getWorldCoordinate());
            model.setTexture(atlas);
            chunk.setModel(model);
        } else
            chunk.setModel(null);

        if (chunk.getTransparentVertices() != null && chunk.getTransparentVertices().length != 0) {
            Model transparentModel = ObjectLoader.loadModel(chunk.getTransparentVertices(), chunk.getWorldCoordinate());
            transparentModel.setTexture(atlas);
            chunk.setTransparentModel(transparentModel);
        } else
            chunk.setTransparentModel(null);

        chunk.clearMesh();
    }

    public static void update() {
        synchronized (toBufferChunks) {
            for (int i = 0; i < MAX_CHUNKS_TO_BUFFER_PER_FRAME && !toBufferChunks.isEmpty(); i++) {
                Chunk chunk = toBufferChunks.removeFirst();
                deleteChunkMeshBuffers(chunk);
                bufferChunkMesh(chunk);
            }
        }

        synchronized (toUnloadChunks) {
            while (!toUnloadChunks.isEmpty()) {
                Chunk chunk = toUnloadChunks.removeFirst();
                if (chunk != null)
                    deleteChunkMeshBuffers(chunk);
            }
        }
        player.update();
    }

    public static void deleteChunkMeshBuffers(Chunk chunk) {
        if (chunk.getModel() != null) {
            ObjectLoader.removeVAO(chunk.getModel().getVao());
            ObjectLoader.removeVBO(chunk.getModel().getVbo());
            chunk.setModel(null);
        }
        if (chunk.getTransparentModel() != null) {
            ObjectLoader.removeVAO(chunk.getTransparentModel().getVao());
            ObjectLoader.removeVBO(chunk.getTransparentModel().getVbo());
            chunk.setTransparentModel(null);
        }
    }

    public static void input(float passedTime) {
        player.input(passedTime);
    }

    public static void render() {
        WindowManager window = Launcher.getWindow();

        if (window.isResize()) {
            GL11.glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResize(true);
        }
        player.render();
        player.getRenderer().render(player.getCamera());
    }

    public static void addToBufferChunk(Chunk chunk) {
        synchronized (toBufferChunks) {
            if (!toBufferChunks.contains(chunk))
                toBufferChunks.add(chunk);
        }
    }

    public static void addToUnloadChunk(Chunk chunk) {
        synchronized (toUnloadChunks) {
            toUnloadChunks.add(chunk);
        }
    }

    public static float[] getCrossHairVertices() {
        WindowManager window = Launcher.getWindow();

        int width = window.getWidth();
        int height = window.getHeight();
        float size = 16;

        return new float[]{
                -size * GUI_SIZE / width, size * GUI_SIZE / height,
                -size * GUI_SIZE / width, -size * GUI_SIZE / height,
                size * GUI_SIZE / width, size * GUI_SIZE / height,

                -size * GUI_SIZE / width, -size * GUI_SIZE / height,
                size * GUI_SIZE / width, -size * GUI_SIZE / height,
                size * GUI_SIZE / width, size * GUI_SIZE / height
        };
    }

    public static float[] getHotBarElementVertices(int index, byte block) {
        if (block == AIR)
            return new float[]{};
        WindowManager window = Launcher.getWindow();

        final int width = window.getWidth();
        final int height = window.getHeight();

        final float xOffset = (40.0f * index - 165 + 4) * GUI_SIZE / width;
        final float yOffset = -0.5f + 4.0f * GUI_SIZE / height;

        final float sin30 = (float) Math.sin(Math.toRadians(30)) * GUI_SIZE;
        final float cos30 = (float) Math.cos(Math.toRadians(30)) * GUI_SIZE;

        byte[] XYZSubData = Block.getXYZSubData(block);

        float widthX = XYZSubData[MAX_X] - XYZSubData[MIN_X] + 16;
        float widthY = (XYZSubData[MAX_Y] - XYZSubData[MIN_Y] + 16) * GUI_SIZE;
        float widthZ = XYZSubData[MAX_Z] - XYZSubData[MIN_Z] + 16;

        //Ignorance is bliss, so be ignorant
        float value1 = yOffset + widthY / height + sin30 * widthX / height;
        float value2 = yOffset + widthY / height + sin30 * widthZ / height;
        float value7 = xOffset - cos30 * widthZ / width;
        float value3 = value7 + cos30 * widthX / width;
        float value4 = yOffset + widthY / height + sin30 * widthZ / height + sin30 * widthX / height;
        float value5 = xOffset + cos30 * widthX / width;
        float value6 = yOffset + sin30 * widthX / height;
        float value8 = yOffset + sin30 * widthZ / height;
        float value9 = yOffset + widthY / height;
        return new float[]{
                xOffset, yOffset,
                xOffset, value9,
                value5, value6,
                xOffset, value9,
                value5, value1,
                value5, value6,

                value7, value2,
                value3, value4,
                xOffset, value9,
                value3, value4,
                value5, value1,
                xOffset, value9,

                xOffset, yOffset,
                xOffset, value9,
                value7, value8,
                xOffset, value9,
                value7, value2,
                value7, value8,
        };
    }

    public static float[] getHotBarVertices() {
        WindowManager window = Launcher.getWindow();

        int width = window.getWidth();
        int height = window.getHeight();
        float sizeX = 180;
        float sizeY = 40;

        return new float[]{

                -sizeX * GUI_SIZE / width, -0.5f,
                -sizeX * GUI_SIZE / width, sizeY * GUI_SIZE / height - 0.5f,
                sizeX * GUI_SIZE / width, -0.5f,

                -sizeX * GUI_SIZE / width, sizeY * GUI_SIZE / height - 0.5f,
                sizeX * GUI_SIZE / width, sizeY * GUI_SIZE / height - 0.5f,
                sizeX * GUI_SIZE / width, -0.5f
        };
    }

    public static double[][] heightMap(int x, int z) {
        double[][] heightMap = new double[CHUNK_SIZE][CHUNK_SIZE];

        for (int mapX = 0; mapX < CHUNK_SIZE; mapX++) {
            for (int mapZ = 0; mapZ < CHUNK_SIZE; mapZ++) {
                int currentX = (x << CHUNK_SIZE_BITS) + mapX;
                int currentZ = (z << CHUNK_SIZE_BITS) + mapZ;
                double value = WATER_LEVEL;
                value += OpenSimplex2S.noise3_ImproveXY(SEED, currentX * HEIGHT_MAP_FREQUENCY / 4, currentZ * HEIGHT_MAP_FREQUENCY / 4, 0.0) * 50;
                value += OpenSimplex2S.noise3_ImproveXY(SEED + 1, currentX * HEIGHT_MAP_FREQUENCY, currentZ * HEIGHT_MAP_FREQUENCY, 0.0) * 25;
                value += OpenSimplex2S.noise3_ImproveXY(SEED + 2, currentX * HEIGHT_MAP_FREQUENCY * 2, currentZ * HEIGHT_MAP_FREQUENCY * 2, 0.0) * 12;
                value += OpenSimplex2S.noise3_ImproveXY(SEED + 3, currentX * HEIGHT_MAP_FREQUENCY * 4, currentZ * HEIGHT_MAP_FREQUENCY * 4, 0.0) * 6;
                value += OpenSimplex2S.noise3_ImproveXY(SEED + 4, currentX * HEIGHT_MAP_FREQUENCY * 8, currentZ * HEIGHT_MAP_FREQUENCY * 8, 0.0) * 3;
                heightMap[mapX][mapZ] = value;
            }
        }
        return heightMap;
    }

    public static int[][] stoneMap(int x, int z, double[][] heightMap) {
        int[][] stoneMap = new int[CHUNK_SIZE][CHUNK_SIZE];

        for (int mapX = 0; mapX < CHUNK_SIZE; mapX++) {
            for (int mapZ = 0; mapZ < CHUNK_SIZE; mapZ++) {
                int currentX = (x << CHUNK_SIZE_BITS) + mapX;
                int currentZ = (z << CHUNK_SIZE_BITS) + mapZ;

                double biomes = OpenSimplex2S.noise3_ImproveXY(SEED + 1 << 16, currentX * STONE_MAP_FREQUENCY, currentZ * STONE_MAP_FREQUENCY, 0.0);
                biomes = Math.max(0, biomes);
                stoneMap[mapX][mapZ] = (int) (heightMap[mapX][mapZ] * (biomes + 1)) * (biomes != 0 ? 1 : 0);

            }
        }
        return stoneMap;
    }

    public static double[][] featureMap(int x, int z) {
        double[][] featureMap = new double[CHUNK_SIZE][CHUNK_SIZE];
        Random random = new Random(getChunkId(x, 0, z));

        for (int mapX = 0; mapX < CHUNK_SIZE; mapX++)
            for (int mapZ = 0; mapZ < CHUNK_SIZE; mapZ++)
                featureMap[mapX][mapZ] = random.nextDouble();
        return featureMap;
    }

    public static byte[][] treeMap(int x, int z, double[][] heightMap, int[][] stoneMap, double[][] featureMap) {
        byte[][] treeMap = new byte[CHUNK_SIZE][CHUNK_SIZE];
        for (int mapX = 2; mapX < CHUNK_SIZE - 2; mapX++) {
            for (int mapZ = 2; mapZ < CHUNK_SIZE - 2; mapZ++) {

                if (treeMap[mapX][mapZ] != 0)
                    continue;
                if (stoneMap[mapX][mapZ] != 0)
                    continue;
                if (heightMap[mapX][mapZ] <= WATER_LEVEL)
                    continue;
                int sandHeight = (int) (Math.abs(featureMap[mapX][mapZ] * 4)) + WATER_LEVEL;
                if (heightMap[mapX][mapZ] <= sandHeight + 2)
                    continue;

                int currentX = (x << CHUNK_SIZE_BITS) + mapX;
                int currentZ = (z << CHUNK_SIZE_BITS) + mapZ;

                double value = OpenSimplex2S.noise3_ImproveXY(SEED + 1 << 32, currentX, currentZ, 0.0);
                if (value > TREE_THRESHOLD) {
                    for (int i = -1; i <= 1; i++)
                        for (int j = -1; j <= 1; j++)
                            treeMap[mapX + i][mapZ + j] = -1;
                    treeMap[mapX][mapZ] = OAK_TREE_VALUE;
                } else {
                    boolean toCloseToChunkEdge = mapX == 2 || mapX == CHUNK_SIZE - 3 || mapZ == 2 || mapZ == CHUNK_SIZE - 3;
                    if (value < -TREE_THRESHOLD) {
                        if (toCloseToChunkEdge)
                            continue;
                        for (int i = -1; i <= 1; i++)
                            for (int j = -1; j <= 1; j++)
                                treeMap[mapX + i][mapZ + j] = -1;
                        treeMap[mapX][mapZ] = SPRUCE_TREE_VALUE;
                    } else if (value < 0.005 && value > -0.005) {
                        if (toCloseToChunkEdge)
                            continue;
                        for (int i = -1; i <= 2; i++)
                            for (int j = -1; j <= 2; j++)
                                treeMap[mapX + i][mapZ + j] = -1;
                        treeMap[mapX][mapZ] = DARK_OAK_TREE_VALUE;
                    }
                }
            }
        }
        return treeMap;
    }

    public static boolean isOutsideCave(int x, int y, int z) {
        if (y < BLOB_CAVE_MIN_Y || y > BLOB_CAVE_MAX_Y)
            return true;
        y -= CAVE_HEIGHT;
        double modifier = (y * y * CAVE_HEIGHT_BIAS);

        double blobCaveValue = OpenSimplex2S.noise3_ImproveXY(SEED, x * BLOB_CAVE_FREQUENCY, y * BLOB_CAVE_FREQUENCY, z * BLOB_CAVE_FREQUENCY) / modifier;

        return blobCaveValue < BLOB_CAVE_THRESHOLD;
    }

    public static long getChunkId(int x, int y, int z) {
        return (long) (x & MAX_CHUNKS_XZ) << 37 | (long) (y & MAX_CHUNKS_Y) << 27 | (z & MAX_CHUNKS_XZ);
    }

    public static int getChunkIndex(int x, int y, int z) {

        x = (x % RENDERED_WORLD_WIDTH);
        if (x < 0) x += RENDERED_WORLD_WIDTH;

        y = (y % RENDERED_WORLD_HEIGHT);
        if (y < 0) y += RENDERED_WORLD_HEIGHT;

        z = (z % RENDERED_WORLD_WIDTH);
        if (z < 0) z += RENDERED_WORLD_WIDTH;

        return (x * RENDERED_WORLD_HEIGHT + y) * RENDERED_WORLD_WIDTH + z;
    }

    public static void startChunkGenerator() {
        generator.start();
    }

    public static Player getPlayer() {
        return player;
    }

    public static void cleanUp() {
        player.getRenderer().cleanUp();
        ObjectLoader.cleanUp();
        generator.cleanUp();
    }
}
