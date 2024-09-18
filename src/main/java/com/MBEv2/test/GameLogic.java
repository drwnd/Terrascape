package com.MBEv2.test;

import com.MBEv2.core.*;
import com.MBEv2.core.entity.*;
import com.MBEv2.core.utils.Utils;
import org.joml.Vector3f;
import org.joml.Vector4i;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;


import static com.MBEv2.core.utils.Constants.*;
import static com.MBEv2.core.utils.Settings.*;

public class GameLogic {

    private static final LinkedList<Chunk> toBufferChunks = new LinkedList<>();
    private static final LinkedList<Chunk> toUnloadChunks = new LinkedList<>();
    private static ChunkGenerator generator;

    private static Player player;
    private static final LinkedList<Entity> entities = new LinkedList<>();
    private static final ArrayList<Entity> toSpawnEntities = new ArrayList<>();

    private static byte generatorRestartScheduled = 0;

    public static void init() throws Exception {

        player = FileManager.loadGameState();

        startGenerator();
    }

    public static void spawnEntity(Entity entity) {
        toSpawnEntities.add(entity);
    }

    private static void addEntityToLists(Entity entity) {
        Vector3f position = entity.getPosition();
        int x = Utils.floor(position.x);
        int y = Utils.floor(position.y);
        int z = Utils.floor(position.z);

        Chunk chunk = Chunk.getChunk(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS);
        if (chunk == null) return;

        int inChunkX = x & CHUNK_SIZE_MASK;
        int inChunkY = y & CHUNK_SIZE_MASK;
        int inChunkZ = z & CHUNK_SIZE_MASK;

        int entityClusterIndex = getEntityClusterIndex(inChunkX >> ENTITY_CLUSTER_SIZE_BITS, inChunkY >> ENTITY_CLUSTER_SIZE_BITS, inChunkZ >> ENTITY_CLUSTER_SIZE_BITS);
        chunk.getEntityCluster(entityClusterIndex).add(entity);
        entities.add(entity);
    }

    public static void restartGenerator(int direction) {
        generatorRestartScheduled = (byte) (0x80 | direction);
    }

    public static void restartGeneratorNow(int direction) {
        generatorRestartScheduled = 0;
        generator.restart(direction);
    }

    public static void startGenerator() {
        generator = new ChunkGenerator();
        generator.start();
    }

    public static void haltChunkGenerator() {
        generator.waitUntilHalt();
    }

    public static void placeBlock(short block, int x, int y, int z, boolean highImportance) {
        int chunkX = x >> CHUNK_SIZE_BITS;
        int chunkY = y >> CHUNK_SIZE_BITS;
        int chunkZ = z >> CHUNK_SIZE_BITS;

        int inChunkX = x & CHUNK_SIZE_MASK;
        int inChunkY = y & CHUNK_SIZE_MASK;
        int inChunkZ = z & CHUNK_SIZE_MASK;

        Chunk chunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
        if (chunk == null) return;

        int baseBlock = block & BASE_BLOCK_MASK;
        if ((Block.getBlockTypeData(block) & SMART_BLOCK_TYPE) != 0) {
            block = (short) (baseBlock | Block.getSmartBlockType(block, x, y, z));
        }

        short previousBlock = chunk.getSaveBlock(inChunkX, inChunkY, inChunkZ);
        if (previousBlock == block) return;

        chunk.placeBlock(inChunkX, inChunkY, inChunkZ, block);
        chunk.setModified();

        Block.updateSmartBlock(x + 1, y, z);
        Block.updateSmartBlock(x - 1, y, z);
        Block.updateSmartBlock(x, y + 1, z);
        Block.updateSmartBlock(x, y - 1, z);
        Block.updateSmartBlock(x, y, z + 1);
        Block.updateSmartBlock(x, y, z - 1);

        int minX = chunkX, maxX = chunkX;
        int minY = chunkY, maxY = chunkY;
        int minZ = chunkZ, maxZ = chunkZ;

        if (inChunkX == 0) minX = chunkX - 1;
        else if (inChunkX == CHUNK_SIZE - 1) maxX = chunkX + 1;
        if (inChunkY == 0) minY = chunkY - 1;
        else if (inChunkY == CHUNK_SIZE - 1) maxY = chunkY + 1;
        if (inChunkZ == 0) minZ = chunkZ - 1;
        else if (inChunkZ == CHUNK_SIZE - 1) maxZ = chunkZ + 1;

        addBlockChange(x, y, z, previousBlock, block);

        for (chunkX = minX; chunkX <= maxX; chunkX++)
            for (chunkY = minY; chunkY <= maxY; chunkY++)
                for (chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
                    Chunk toMeshChunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
                    if (toMeshChunk == null) continue;
                    toMeshChunk.setMeshed(false);
                }
        if (highImportance) restartGeneratorNow(NONE);
        else restartGenerator(NONE);
    }

    public static void addBlockChange(int x, int y, int z, short previousBlock, short currentBlock) {
        generator.addBlockChange(new Vector4i(x, y, z, previousBlock << 16 | currentBlock));
    }

    public static void bufferChunkMesh(Chunk chunk) {
        for (int side = 0; side < 6; side++) {
            Model oldSideModel = chunk.getModel(side);
            if (chunk.getVertices(side) != null && chunk.getVertices(side).length != 0) {
                Model newModel = ObjectLoader.loadModel(chunk.getVertices(side), chunk.getWorldCoordinate());
                chunk.setModel(newModel, side);
            } else chunk.setModel(null, side);

            if (oldSideModel != null) {
                ObjectLoader.removeVAO(oldSideModel.getVao());
                ObjectLoader.removeVBO(oldSideModel.getVbo());
            }
        }

        Model oldWaterModel = chunk.getWaterModel();
        if (chunk.getWaterVertices() != null && chunk.getWaterVertices().length != 0) {
            Model newWaterModel = ObjectLoader.loadModel(chunk.getWaterVertices(), chunk.getWorldCoordinate());
            chunk.setWaterModel(newWaterModel);
        } else chunk.setWaterModel(null);

        if (oldWaterModel != null) {
            ObjectLoader.removeVAO(oldWaterModel.getVao());
            ObjectLoader.removeVBO(oldWaterModel.getVbo());
        }

        Model oldFoliageModel = chunk.getFoliageModel();
        if (chunk.getFoliageVertices() != null && chunk.getFoliageVertices().length != 0) {
            Model newFoliageModel = ObjectLoader.loadModel(chunk.getFoliageVertices(), chunk.getWorldCoordinate());
            chunk.setFoliageModel(newFoliageModel);
        } else chunk.setFoliageModel(null);

        if (oldFoliageModel != null) {
            ObjectLoader.removeVAO(oldFoliageModel.getVao());
            ObjectLoader.removeVBO(oldFoliageModel.getVbo());
        }

        chunk.clearMesh();
    }

    public static void update(float passedTicks) {
        synchronized (toUnloadChunks) {
            while (!toUnloadChunks.isEmpty()) {
                Chunk chunk = toUnloadChunks.removeFirst();
                if (chunk != null) {
                    deleteChunkMeshBuffers(chunk);
                    Chunk.removeToGenerateBlocks(chunk.id);
                }
            }
        }

        synchronized (toBufferChunks) {
            for (int i = 0; i < MAX_CHUNKS_TO_BUFFER_PER_FRAME && !toBufferChunks.isEmpty(); i++) {
                Chunk chunk = toBufferChunks.removeFirst();
                bufferChunkMesh(chunk);
            }
        }
        player.update(passedTicks);
    }

    public static void updateGT() {
        player.getRenderer().incrementTime();

        for (Entity entity : toSpawnEntities) addEntityToLists(entity);
        toSpawnEntities.clear();
        for (Iterator<Entity> iterator = entities.iterator(); iterator.hasNext(); ) {
            Entity entity = iterator.next();
            Vector3f position = entity.getPosition();

            int clusterX = Utils.floor(position.x) >> ENTITY_CLUSTER_SIZE_BITS;
            int clusterY = Utils.floor(position.y) >> ENTITY_CLUSTER_SIZE_BITS;
            int clusterZ = Utils.floor(position.z) >> ENTITY_CLUSTER_SIZE_BITS;

            entity.update();

            int newClusterX = Utils.floor(position.x) >> ENTITY_CLUSTER_SIZE_BITS;
            int newClusterY = Utils.floor(position.y) >> ENTITY_CLUSTER_SIZE_BITS;
            int newClusterZ = Utils.floor(position.z) >> ENTITY_CLUSTER_SIZE_BITS;

            if (clusterX != newClusterX || clusterY != newClusterY || clusterZ != newClusterZ) {
                int oldIndex = GameLogic.getEntityClusterIndex(
                        clusterX & IN_CHUNK_ENTITY_CLUSTER_MASK,
                        clusterY & IN_CHUNK_ENTITY_CLUSTER_MASK,
                        clusterZ & IN_CHUNK_ENTITY_CLUSTER_MASK);
                int newIndex = GameLogic.getEntityClusterIndex(
                        newClusterX & IN_CHUNK_ENTITY_CLUSTER_MASK,
                        newClusterY & IN_CHUNK_ENTITY_CLUSTER_MASK,
                        newClusterZ & IN_CHUNK_ENTITY_CLUSTER_MASK);

                int chunkX = clusterX >> ENTITY_CLUSTER_TO_CHUNK_BITS;
                int chunkY = clusterY >> ENTITY_CLUSTER_TO_CHUNK_BITS;
                int chunkZ = clusterZ >> ENTITY_CLUSTER_TO_CHUNK_BITS;
                Chunk chunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
                if (chunk == null) continue;

                chunk.getEntityCluster(oldIndex).remove(entity);

                chunkX = newClusterX >> ENTITY_CLUSTER_TO_CHUNK_BITS;
                chunkY = newClusterY >> ENTITY_CLUSTER_TO_CHUNK_BITS;
                chunkZ = newClusterZ >> ENTITY_CLUSTER_TO_CHUNK_BITS;

                chunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
                if (chunk == null) continue;
                chunk.getEntityCluster(newIndex).add(entity);
            }

            if (entity.isDead()) {
                iterator.remove();

                int x = Utils.floor(position.x);
                int y = Utils.floor(position.y);
                int z = Utils.floor(position.z);

                Chunk chunk = Chunk.getChunk(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS);
                if (chunk == null) continue;

                int inChunkX = x & CHUNK_SIZE_MASK;
                int inChunkY = y & CHUNK_SIZE_MASK;
                int inChunkZ = z & CHUNK_SIZE_MASK;

                int entityClusterIndex = getEntityClusterIndex(inChunkX >> ENTITY_CLUSTER_SIZE_BITS, inChunkY >> ENTITY_CLUSTER_SIZE_BITS, inChunkZ >> ENTITY_CLUSTER_SIZE_BITS);
                chunk.getEntityCluster(entityClusterIndex).remove(entity);
            }
        }

        if (generatorRestartScheduled != 0) {
            generator.restart(generatorRestartScheduled & 0xF);
            generatorRestartScheduled = 0;
        }
    }

    public static void unloadChunks(int playerX, int playerY, int playerZ) {
        for (Chunk chunk : Chunk.getWorld()) {
            if (chunk == null) continue;

            if (Math.abs(chunk.X - playerX) <= RENDER_DISTANCE_XZ + 2 && Math.abs(chunk.Z - playerZ) <= RENDER_DISTANCE_XZ + 2 && Math.abs(chunk.Y - playerY) <= RENDER_DISTANCE_Y + 2)
                continue;

            if (Math.abs(chunk.Y - playerY) < RENDER_DISTANCE_Y + 2)
                Arrays.fill(Chunk.getHeightMap(chunk.X, chunk.Z), Integer.MIN_VALUE);

            chunk.clearMesh();
            addToUnloadChunk(chunk);

            if (chunk.isModified()) FileManager.saveChunk(chunk);

            Chunk.setNull(chunk.getIndex());
        }
    }

    public static void unloadChunks() {
        Vector3f position = player.getCamera().getPosition();
        int playerX = Utils.floor(position.x) >> CHUNK_SIZE_BITS;
        int playerY = Utils.floor(position.y) >> CHUNK_SIZE_BITS;
        int playerZ = Utils.floor(position.z) >> CHUNK_SIZE_BITS;
        unloadChunks(playerX, playerY, playerZ);
    }

    public static void deleteChunkMeshBuffers(Chunk chunk) {
        for (int side = 0; side < 6; side++) {
            Model sideModel = chunk.getModel(side);
            if (sideModel == null) continue;

            ObjectLoader.removeVAO(sideModel.getVao());
            ObjectLoader.removeVBO(sideModel.getVbo());
            chunk.setModel(null, side);
        }

        Model waterModel = chunk.getWaterModel();
        if (waterModel != null) {
            ObjectLoader.removeVAO(waterModel.getVao());
            ObjectLoader.removeVBO(waterModel.getVbo());
            chunk.setWaterModel(null);
        }

        Model foliageModel = chunk.getFoliageModel();
        if (foliageModel != null) {
            ObjectLoader.removeVAO(foliageModel.getVao());
            ObjectLoader.removeVBO(foliageModel.getVbo());
            chunk.setFoliageModel(null);
        }
    }

    public static void input() {
        player.input();
    }

    public static void render(float timeSinceLastTick) {
        WindowManager window = Launcher.getWindow();

        if (window.isResize()) {
            GL11.glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResize(true);
        }
        RenderManager renderer = player.getRenderer();
        player.render();
        renderer.render(player.getCamera(), timeSinceLastTick);
    }

    public static void addToBufferChunk(Chunk chunk) {
        synchronized (toBufferChunks) {
            if (!toBufferChunks.contains(chunk)) toBufferChunks.add(chunk);
        }
    }

    public static void addToUnloadChunk(Chunk chunk) {
        synchronized (toUnloadChunks) {
            toUnloadChunks.add(chunk);
        }
    }

    public static long getChunkId(int chunkX, int chunkY, int chunkZ) {
        return (long) (chunkX & MAX_CHUNKS_XZ) << 37 | (long) (chunkY & MAX_CHUNKS_Y) << 27 | (chunkZ & MAX_CHUNKS_XZ);
    }

    public static int getChunkIndex(int chunkX, int chunkY, int chunkZ) {

        chunkX = (chunkX % RENDERED_WORLD_WIDTH);
        if (chunkX < 0) chunkX += RENDERED_WORLD_WIDTH;

        chunkY = (chunkY % RENDERED_WORLD_HEIGHT);
        if (chunkY < 0) chunkY += RENDERED_WORLD_HEIGHT;

        chunkZ = (chunkZ % RENDERED_WORLD_WIDTH);
        if (chunkZ < 0) chunkZ += RENDERED_WORLD_WIDTH;

        return (chunkX * RENDERED_WORLD_HEIGHT + chunkY) * RENDERED_WORLD_WIDTH + chunkZ;
    }

    public static int getHeightMapIndex(int chunkX, int chunkZ) {

        chunkX = (chunkX % RENDERED_WORLD_WIDTH);
        if (chunkX < 0) chunkX += RENDERED_WORLD_WIDTH;

        chunkZ = (chunkZ % RENDERED_WORLD_WIDTH);
        if (chunkZ < 0) chunkZ += RENDERED_WORLD_WIDTH;

        return chunkX * RENDERED_WORLD_WIDTH + chunkZ;
    }

    public static int getEntityClusterIndex(int clusterX, int clusterY, int clusterZ) {
        return clusterX << 4 | clusterZ << 2 | clusterY;
    }

    public static Player getPlayer() {
        return player;
    }

    public static void cleanUp() {
        player.getRenderer().cleanUp();
        ObjectLoader.cleanUp();
        generator.cleanUp();
        FileManager.saveGameState();
        FileManager.saveAllModifiedChunks();
    }
}
