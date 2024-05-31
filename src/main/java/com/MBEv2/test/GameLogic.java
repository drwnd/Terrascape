package com.MBEv2.test;

import com.MBEv2.core.*;
import com.MBEv2.core.entity.*;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import static com.MBEv2.core.utils.Constants.*;

public class GameLogic {

    private final WindowManager window;
    private Texture atlas;
    private HashMap<Long, Model> chunkModels;
    private HashMap<Long, Model> transparentChunkModels;
    private LinkedList<Chunk> toBufferChunks;
    private LinkedList<Chunk> toUnloadChunks;
    private ChunkGenerator generator;

    private Player player;

    public GameLogic() {
        window = Launcher.getWindow();
    }

    public void init() throws Exception {

        chunkModels = new HashMap<>();
        transparentChunkModels = new HashMap<>();
        toBufferChunks = new LinkedList<>();
        toUnloadChunks = new LinkedList<>();
        generator = new ChunkGenerator(this);

        atlas = new Texture(ObjectLoader.loadTexture("textures/atlas256.png"));

        for (int x = -RENDER_DISTANCE_XZ; x < RENDER_DISTANCE_XZ; x++) {
            for (int y = 0; y < RENDER_DISTANCE_Y * 2; y++) {
                for (int z = -RENDER_DISTANCE_XZ; z < RENDER_DISTANCE_XZ; z++) {
                    long index = GameLogic.getChunkId(x, y, z);
                    Chunk chunk = Chunk.getChunk(index);
                    chunk.generateMesh();
                    bufferChunkMesh(chunk);
                }
            }
        }

        player = new Player(chunkModels, transparentChunkModels, this, atlas);
        player.init();

        player.getRenderer().init();

    }

    public void loadUnloadChunks() {
        generator.continueRunning();
    }

    public void placeBlock(byte block, Vector3i position) {
        if (position == null)
            return;

        int chunkX = position.x >> 5;
        int chunkY = position.y >> 5;
        int chunkZ = position.z >> 5;

        int inChunkX = position.x & 31;
        int inChunkY = position.y & 31;
        int inChunkZ = position.z & 31;

        Chunk chunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
        chunk.storeSave(inChunkX, inChunkY, inChunkZ, block);
        chunk.setModified();
        regenerateChunkMesh(chunk);

        if (inChunkX == 0) {
            Chunk neighbour = Chunk.getChunk(chunkX - 1, chunkY, chunkZ);
            if (neighbour != null)
                regenerateChunkMesh(neighbour);
        } else if (inChunkX == CHUNK_SIZE - 1) {
            Chunk neighbour = Chunk.getChunk(chunkX + 1, chunkY, chunkZ);
            if (neighbour != null)
                regenerateChunkMesh(neighbour);
        }

        if (inChunkY == 0) {
            Chunk neighbour = Chunk.getChunk(chunkX, chunkY - 1, chunkZ);
            if (neighbour != null)
                regenerateChunkMesh(neighbour);
        } else if (inChunkY == CHUNK_SIZE - 1) {
            Chunk neighbour = Chunk.getChunk(chunkX, chunkY + 1, chunkZ);
            if (neighbour != null)
                regenerateChunkMesh(neighbour);
        }

        if (inChunkZ == 0) {
            Chunk neighbour = Chunk.getChunk(chunkX, chunkY, chunkZ - 1);
            if (neighbour != null)
                regenerateChunkMesh(neighbour);
        } else if (inChunkZ == CHUNK_SIZE - 1) {
            Chunk neighbour = Chunk.getChunk(chunkX, chunkY, chunkZ + 1);
            if (neighbour != null)
                regenerateChunkMesh(neighbour);
        }
    }

    public void regenerateChunkMesh(Chunk chunk) {
        chunk.generateMesh();

        if (chunkModels.get(chunk.getId()) != null) {
            GL30.glDeleteVertexArrays(chunk.getModel().getId());
            GL20.glDeleteBuffers(chunk.getModel().getVbo());
        }

        if (transparentChunkModels.get(chunk.getId()) != null) {
            GL30.glDeleteVertexArrays(chunk.getTransparentModel().getId());
            GL20.glDeleteBuffers(chunk.getTransparentModel().getVbo());
        }

        bufferChunkMesh(chunk);
    }

    public void bufferChunkMesh(Chunk chunk) {
        if (chunk.getVertices().length != 0) {
            Model model = ObjectLoader.loadModel(chunk.getVertices(), chunk.getWorldCoordinate());
            chunk.setModel(model);
            model.setTexture(atlas);
            chunkModels.put(chunk.getId(), model);
        } else
            chunkModels.remove(chunk.getId());

        if (chunk.getTransparentVertices().length != 0) {
            Model transparentModel = ObjectLoader.loadModel(chunk.getTransparentVertices(), chunk.getWorldCoordinate());
            chunk.setTransparentModel(transparentModel);
            transparentModel.setTexture(atlas);
            transparentChunkModels.put(chunk.getId(), transparentModel);
        } else
            transparentChunkModels.remove(chunk.getId());

        chunk.clearMesh();
    }

    public void update() {
        for (int i = 0; i < MAX_CHUNKS_TO_BUFFEr_PER_FRAME && toBufferChunks.size() > 1; i++) {
            Chunk chunk = toBufferChunks.removeFirst();
            bufferChunkMesh(chunk);
        }

        player.update();

        LinkedList<Chunk> chunks = toUnloadChunks;

        for (Chunk chunk : chunks) {
            unloadChunk(chunk);
        }
        removeUnusedModels();
    }

    public void removeUnusedModels() {
        Vector3f cameraPosition = player.getCamera().getPosition();

        int chunkX = (int) Math.floor(cameraPosition.x) >> 5;
        int chunkY = (int) Math.floor(cameraPosition.y) >> 5;
        int chunkZ = (int) Math.floor(cameraPosition.z) >> 5;

        for (Iterator<Model> iterator = chunkModels.values().iterator(); iterator.hasNext(); ) {
            Model model = iterator.next();
            Vector3i worldCoordinate = model.getPosition();
            int x = worldCoordinate.x >> 5;
            int y = worldCoordinate.y >> 5;
            int z = worldCoordinate.z >> 5;

            if (Math.abs(x - chunkX) > RENDER_DISTANCE_XZ + 2 || Math.abs(z - chunkZ) > RENDER_DISTANCE_XZ + 2 || Math.abs(y - chunkY) > RENDER_DISTANCE_Y + 2) {
                iterator.remove();
                GL30.glDeleteVertexArrays(model.getId());
                GL20.glDeleteBuffers(model.getVbo());
            }
        }

        for (Iterator<Model> iterator = transparentChunkModels.values().iterator(); iterator.hasNext(); ) {
            Model transparentModel = iterator.next();
            Vector3i worldCoordinate = transparentModel.getPosition();
            int x = worldCoordinate.x >> 5;
            int y = worldCoordinate.y >> 5;
            int z = worldCoordinate.z >> 5;

            if (Math.abs(x - chunkX) > RENDER_DISTANCE_XZ + 2 || Math.abs(z - chunkZ) > RENDER_DISTANCE_XZ + 2 || Math.abs(y - chunkY) > RENDER_DISTANCE_Y + 2) {
                iterator.remove();
                GL30.glDeleteVertexArrays(transparentModel.getId());
                GL20.glDeleteBuffers(transparentModel.getVbo());
            }
        }
    }

    public void unloadChunk(Chunk chunk) {
        if (chunkModels.get(chunk.getId()) != null) {
            GL30.glDeleteVertexArrays(chunk.getModel().getId());
            GL20.glDeleteBuffers(chunk.getModel().getVbo());
        }

        if (transparentChunkModels.get(chunk.getId()) != null) {
            GL30.glDeleteVertexArrays(chunk.getTransparentModel().getId());
            GL20.glDeleteBuffers(chunk.getTransparentModel().getVbo());
        }

        chunkModels.remove(chunk.getId());
        transparentChunkModels.remove(chunk.getId());
        chunk.setLoaded(false);
        chunk.setMeshed(false);
        chunk.setModel(null);
        chunk.setTransparentModel(null);
    }

    public void input() {
        player.input();
    }

    public float[] getCrossHairVertices() {

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

    public float[] getHotBarVertices() {

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

    public float[] getHotBarElementVertices(int index) {

        int width = window.getWidth();
        int height = window.getHeight();

        float sizeX = 180;
        float sizeY = 40;

        float lowerX = (-sizeX * GUI_SIZE + 4 + 40 * index) / width;
        float upperX = (-sizeX * GUI_SIZE + 36 + 40 * index) / width;
        float lowerY = -0.5f + 4.0f / height;
        float upperY = (sizeY * GUI_SIZE - 4.0f) / height - 0.5f;
        return new float[]{
                lowerX, lowerY,
                lowerX, upperY,
                upperX, lowerY,

                lowerX, upperY,
                upperX, upperY,
                upperX, lowerY
        };
    }

    public void render() {
        if (window.isResize()) {
            GL11.glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResize(true);
        }
        player.render();
        player.getRenderer().render(player.getCamera());

    }

    public void cleanUp() {
        player.getRenderer().cleanUp();
        ObjectLoader.cleanUp();
    }

    public static double[][] heightMap(int x, int z) {
        final double FREQUENCY = 1 / 100d;
        double[][] heightMap = new double[CHUNK_SIZE][CHUNK_SIZE];

        for (int mapX = 0; mapX < CHUNK_SIZE; mapX++) {
            for (int mapZ = 0; mapZ < CHUNK_SIZE; mapZ++) {
                int currentX = (x << 5) + mapX;
                int currentZ = (z << 5) + mapZ;
                double value = WATER_LEVEL;
                value += OpenSimplex2S.noise3_ImproveXY(SEED, currentX * FREQUENCY / 4, currentZ * FREQUENCY / 4, 0.0) * 50;
                value += OpenSimplex2S.noise3_ImproveXY(SEED + 1, currentX * FREQUENCY, currentZ * FREQUENCY, 0.0) * 25;
                value += OpenSimplex2S.noise3_ImproveXY(SEED + 2, currentX * FREQUENCY * 2, currentZ * FREQUENCY * 2, 0.0) * 12;
                value += OpenSimplex2S.noise3_ImproveXY(SEED + 3, currentX * FREQUENCY * 4, currentZ * FREQUENCY * 4, 0.0) * 6;
                value += OpenSimplex2S.noise3_ImproveXY(SEED + 4, currentX * FREQUENCY * 8, currentZ * FREQUENCY * 8, 0.0) * 3;
                heightMap[mapX][mapZ] = value;
            }
        }
        return heightMap;
    }

    public static int[][] stoneMap(int x, int z, double[][] heightMap) {
        int[][] stoneMap = new int[CHUNK_SIZE][CHUNK_SIZE];

        for (int mapX = 0; mapX < CHUNK_SIZE; mapX++) {
            for (int mapZ = 0; mapZ < CHUNK_SIZE; mapZ++) {
                int currentX = (x << 5) + mapX;
                int currentZ = (z << 5) + mapZ;

                double biomes = OpenSimplex2S.noise3_ImproveXY(SEED + 1 << 16, currentX / 200d, currentZ / 200d, 0.0);
                biomes = Math.max(0, biomes);
                stoneMap[mapX][mapZ] = (int) (heightMap[mapX][mapZ] * (biomes + 1)) * (biomes != 0 ? 1 : 0);

            }
        }
        return stoneMap;
    }

    public static double[][] featureMap(int x, int z) {
        double[][] featureMap = new double[CHUNK_SIZE][CHUNK_SIZE];

        for (int mapX = 0; mapX < CHUNK_SIZE; mapX++) {
            for (int mapZ = 0; mapZ < CHUNK_SIZE; mapZ++) {
                int currentX = (x << 5) + mapX;
                int currentZ = (z << 5) + mapZ;

                double value = OpenSimplex2S.noise3_ImproveXY(SEED + 1 << 48, currentX, currentZ, 0.0);
                featureMap[mapX][mapZ] = value;
            }
        }
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

                int currentX = (x << 5) + mapX;
                int currentZ = (z << 5) + mapZ;

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

    public void addToBufferChunk(Chunk chunk) {
        toBufferChunks.add(chunk);
    }

    public void setToUnloadChunks(LinkedList<Chunk> toUnloadChunks) {
        this.toUnloadChunks = toUnloadChunks;
    }

    public static long getChunkId(int x, int y, int z) {
        return (long) (x & MAX_XZ) << 37 | (long) (y & MAX_Y) << 27 | (z & MAX_XZ);
    }

    public void startChunkGenerator() {
        generator.start();
    }

    public Player getPlayer() {
        return player;
    }
}
