package terrascape.server;

import terrascape.dataStorage.Chunk;
import terrascape.dataStorage.FileManager;
import terrascape.dataStorage.HeightMap;
import terrascape.entity.*;
import terrascape.entity.entities.Entity;
import terrascape.entity.particles.BlockBreakParticle;
import terrascape.entity.particles.Particle;
import terrascape.generation.ChunkGenerator;
import terrascape.player.*;
import terrascape.utils.Utils;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;


import static terrascape.utils.Constants.*;
import static terrascape.utils.Settings.*;

public final class ServerLogic {

    public static void init() throws Exception {

        player = FileManager.loadPlayer();

        ChunkGenerator generator = new ChunkGenerator();
        generator.generateSurrounding(player);

        startGenerator();
    }

    public static void spawnEntity(Entity entity) {
        synchronized (TO_SPAWN_ENTITIES) {
            TO_SPAWN_ENTITIES.add(entity);
        }
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

        int entityClusterIndex = Utils.getEntityClusterIndex(inChunkX >> ENTITY_CLUSTER_SIZE_BITS, inChunkY >> ENTITY_CLUSTER_SIZE_BITS, inChunkZ >> ENTITY_CLUSTER_SIZE_BITS);
        LinkedList<Entity> entityCluster = chunk.getEntityCluster(entityClusterIndex);
        synchronized (entityCluster) {
            entityCluster.add(entity);
        }
        chunk.setModified();
        NEXT_GT_ENTITIES.add(entity);
    }

    public static void restartGenerator(int direction) {
        generatorRestartScheduled = (byte) (0x80 | direction);
    }

    public static void startGenerator() {
        generator = new ChunkGenerator();
        generator.restart(NONE);
    }

    public static void haltChunkGenerator() {
        generator.waitUntilHalt(true);
    }

    public static void placeBlock(short block, int x, int y, int z, boolean playerAction) {
        int chunkX = x >> CHUNK_SIZE_BITS;
        int chunkY = y >> CHUNK_SIZE_BITS;
        int chunkZ = z >> CHUNK_SIZE_BITS;

        int inChunkX = x & CHUNK_SIZE_MASK;
        int inChunkY = y & CHUNK_SIZE_MASK;
        int inChunkZ = z & CHUNK_SIZE_MASK;

        Chunk chunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
        if (chunk == null) return;

        short previousBlock = chunk.getSaveBlock(inChunkX, inChunkY, inChunkZ);
        if (playerAction) block = InteractionHandler.getToPlaceBlock(block, previousBlock);
        if (previousBlock == block) return;

        chunk.placeBlock(inChunkX, inChunkY, inChunkZ, block);
        BlockEvent.updateSurrounding(x, y, z);
        handleBlockChange(x, y, z, previousBlock, block);

        int minX = chunkX, maxX = chunkX;
        int minY = chunkY, maxY = chunkY;
        int minZ = chunkZ, maxZ = chunkZ;

        if (inChunkX == 0) minX = chunkX - 1;
        else if (inChunkX == CHUNK_SIZE - 1) maxX = chunkX + 1;
        if (inChunkY == 0) minY = chunkY - 1;
        else if (inChunkY == CHUNK_SIZE - 1) maxY = chunkY + 1;
        if (inChunkZ == 0) minZ = chunkZ - 1;
        else if (inChunkZ == CHUNK_SIZE - 1) maxZ = chunkZ + 1;

        for (chunkX = minX; chunkX <= maxX; chunkX++)
            for (chunkY = minY; chunkY <= maxY; chunkY++)
                for (chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
                    Chunk toMeshChunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
                    if (toMeshChunk == null) continue;
                    toMeshChunk.setMeshed(false);
                }
        restartGenerator(NONE);

        if (playerAction) {
            boolean previousBlockWaterLogged = Block.isWaterLogged(previousBlock);
            boolean newBlockWaterLogged = Block.isWaterLogged(block);

            if (block == AIR)
                addParticle(new BlockBreakParticle(new Vector3f(x + 0.5f, y + 0.625f, z + 0.5f), previousBlock));
            else if (Block.isWaterBlock(previousBlock))
                addParticle(new BlockBreakParticle(new Vector3f(x + 0.5f, y + 0.625f, z + 0.5f), WATER_SOURCE));
            else if (Block.isLavaBlock(previousBlock))
                addParticle(new BlockBreakParticle(new Vector3f(x + 0.5f, y + 0.625f, z + 0.5f), LAVA_SOURCE));
            else if (Block.isWaterBlock(block) && previousBlockWaterLogged)
                addParticle(new BlockBreakParticle(new Vector3f(x + 0.5f, y + 0.625f, z + 0.5f), previousBlock));

            SoundManager sound = Launcher.getSound();

            if (previousBlockWaterLogged || !newBlockWaterLogged) {
                sound.playRandomSound(Block.getDigSound(previousBlock), x + 0.5f, y + 0.5f, z + 0.5f, 0.0f, 0.0f, 0.0f, DIG_GAIN);
                sound.playRandomSound(Block.getFootstepsSound(block), x + 0.5f, y + 0.5f, z + 0.5f, 0.0f, 0.0f, 0.0f, STEP_GAIN);
            } else
                sound.playRandomSound(Block.getFootstepsSound(WATER_SOURCE), x + 0.5f, y + 0.5f, z + 0.5f, 0.0f, 0.0f, 0.0f, STEP_GAIN);
        }
    }

    public static void handleBlockChange(int x, int y, int z, short previousBlock, short currentBlock) {
        int blockLightLevel = Block.getBlockProperties(currentBlock) & LIGHT_EMITTING;
        int previousBlockLightLevel = Block.getBlockProperties(previousBlock) & LIGHT_EMITTING;
        boolean blockEmitsLight = blockLightLevel != 0;
        boolean previousBlockEmitsLight = previousBlockLightLevel != 0;

        if (blockEmitsLight && previousBlockEmitsLight) {
            if (blockLightLevel > previousBlockLightLevel) LightLogic.setBlockLight(x, y, z, blockLightLevel);
            else if (previousBlockLightLevel > blockLightLevel) {
                LightLogic.dePropagateBlockLight(x, y, z);
                LightLogic.setBlockLight(x, y, z, blockLightLevel);
            }
        } else if (blockEmitsLight) {
            if (Chunk.getBlockLightInWorld(x, y, z) > blockLightLevel) LightLogic.dePropagateBlockLight(x, y, z);
            LightLogic.setBlockLight(x, y, z, blockLightLevel);
        } else if (currentBlock == AIR) {
            if (previousBlockEmitsLight) LightLogic.dePropagateBlockLight(x, y, z);
            else LightLogic.setBlockLight(x, y, z, LightLogic.getMaxSurroundingBlockLight(x, y, z) - 1);
        } else LightLogic.dePropagateBlockLight(x, y, z);

        if (currentBlock == AIR) LightLogic.setSkyLight(x, y, z, LightLogic.getMaxSurroundingSkyLight(x, y, z) - 1);
        else LightLogic.dePropagateSkyLight(x, y, z);
    }

    public static void bufferChunkMesh(Chunk chunk) {
        int chunkIndex = chunk.getIndex();
        OpaqueModel oldOpaqueModel = Chunk.getOpaqueModel(chunkIndex);
        if (chunk.getOpaqueVertices() != null && chunk.getOpaqueVertices().length != 0) {
            OpaqueModel newModel = ObjectLoader.loadOpaqueModel(chunk.getOpaqueVertices(), chunk.getWorldCoordinate(), chunk.getVertexCounts());
            Chunk.setOpaqueModel(newModel, chunkIndex);
        } else Chunk.setOpaqueModel(null, chunkIndex);

        if (oldOpaqueModel != null) {
            ObjectLoader.removeVAO(oldOpaqueModel.vao);
            ObjectLoader.removeVBO(oldOpaqueModel.vbo);
        }

        WaterModel oldWaterModel = Chunk.getWaterModel(chunkIndex);
        if (chunk.getWaterVertices() != null && chunk.getWaterVertices().length != 0) {
            WaterModel newWaterModel = ObjectLoader.loadModel(chunk.getWaterVertices(), chunk.getWorldCoordinate());
            Chunk.setWaterModel(newWaterModel, chunkIndex);
        } else Chunk.setWaterModel(null, chunkIndex);

        if (oldWaterModel != null) {
            ObjectLoader.removeVAO(oldWaterModel.vao);
            ObjectLoader.removeVBO(oldWaterModel.vbo);
        }

        chunk.clearMesh();
    }

    public static void update(float passedTicks) {
        loadUnloadObjects();
        player.update(passedTicks);
    }

    public static void updateGT(long tick) {
        player.updateGT(tick);
        updateEntities();
        updateParticles();
        BlockEvent.execute(tick);

        if (generatorRestartScheduled != 0) {
            generator.restart(generatorRestartScheduled & 0xF);
            generatorRestartScheduled = 0;
        }
    }

    private static void updateEntities() {
        Vector3f playerPosition = player.getCamera().getPosition();
        int playerChunkX = Utils.floor(playerPosition.x) >> CHUNK_SIZE_BITS;
        int playerChunkY = Utils.floor(playerPosition.y) >> CHUNK_SIZE_BITS;
        int playerChunkZ = Utils.floor(playerPosition.z) >> CHUNK_SIZE_BITS;

        synchronized (TO_SPAWN_ENTITIES) {
            for (Entity entity : TO_SPAWN_ENTITIES) addEntityToLists(entity);
        }
        TO_SPAWN_ENTITIES.clear();
        synchronized (ENTITIES) {
            NEXT_GT_ENTITIES.addAll(ENTITIES);
        }

        for (Iterator<Entity> iterator = NEXT_GT_ENTITIES.iterator(); iterator.hasNext(); ) {
            Entity entity = iterator.next();
            if (entity.isTooFarFromPlayer(playerChunkX, playerChunkY, playerChunkZ)) continue;

            Vector3f position = entity.getPosition();

            int clusterX = Utils.floor(position.x) >> ENTITY_CLUSTER_SIZE_BITS;
            int clusterY = Utils.floor(position.y) >> ENTITY_CLUSTER_SIZE_BITS;
            int clusterZ = Utils.floor(position.z) >> ENTITY_CLUSTER_SIZE_BITS;

            entity.update();

            int newClusterX = Utils.floor(position.x) >> ENTITY_CLUSTER_SIZE_BITS;
            int newClusterY = Utils.floor(position.y) >> ENTITY_CLUSTER_SIZE_BITS;
            int newClusterZ = Utils.floor(position.z) >> ENTITY_CLUSTER_SIZE_BITS;

            if (clusterX != newClusterX || clusterY != newClusterY || clusterZ != newClusterZ) {
                int oldIndex = Utils.getEntityClusterIndex(clusterX & IN_CHUNK_ENTITY_CLUSTER_MASK, clusterY & IN_CHUNK_ENTITY_CLUSTER_MASK, clusterZ & IN_CHUNK_ENTITY_CLUSTER_MASK);
                int newIndex = Utils.getEntityClusterIndex(newClusterX & IN_CHUNK_ENTITY_CLUSTER_MASK, newClusterY & IN_CHUNK_ENTITY_CLUSTER_MASK, newClusterZ & IN_CHUNK_ENTITY_CLUSTER_MASK);

                int chunkX = clusterX >> ENTITY_CLUSTER_TO_CHUNK_BITS;
                int chunkY = clusterY >> ENTITY_CLUSTER_TO_CHUNK_BITS;
                int chunkZ = clusterZ >> ENTITY_CLUSTER_TO_CHUNK_BITS;
                Chunk chunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
                if (chunk == null) continue;

                LinkedList<Entity> entityCluster = chunk.getEntityCluster(oldIndex);
                synchronized (entityCluster) {
                    entityCluster.remove(entity);
                }

                chunkX = newClusterX >> ENTITY_CLUSTER_TO_CHUNK_BITS;
                chunkY = newClusterY >> ENTITY_CLUSTER_TO_CHUNK_BITS;
                chunkZ = newClusterZ >> ENTITY_CLUSTER_TO_CHUNK_BITS;

                chunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
                if (chunk == null) continue;

                entityCluster = chunk.getEntityCluster(newIndex);
                synchronized (entityCluster) {
                    entityCluster.add(entity);
                }
                chunk.setModified();
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

                int entityClusterIndex = Utils.getEntityClusterIndex(inChunkX >> ENTITY_CLUSTER_SIZE_BITS, inChunkY >> ENTITY_CLUSTER_SIZE_BITS, inChunkZ >> ENTITY_CLUSTER_SIZE_BITS);
                LinkedList<Entity> entityCluster = chunk.getEntityCluster(entityClusterIndex);
                synchronized (entityCluster) {
                    entityCluster.remove(entity);
                }
            }
        }

        synchronized (ENTITIES) {
            ENTITIES.clear();
            ENTITIES.addAll(NEXT_GT_ENTITIES);
            NEXT_GT_ENTITIES.clear();
        }
    }

    private static void updateParticles() {
        long currentTime = System.nanoTime();
        synchronized (TO_SPAWN_PARTICLES) {
            NEXT_GT_PARTICLES.addAll(TO_SPAWN_PARTICLES);
        }
        TO_SPAWN_PARTICLES.clear();
        synchronized (PARTICLES) {
            NEXT_GT_PARTICLES.addAll(PARTICLES);
        }

        NEXT_GT_PARTICLES.removeIf(particle -> currentTime > particle.getMaxAliveTime() + particle.getEmitTime());

        synchronized (PARTICLES) {
            PARTICLES.clear();
            PARTICLES.addAll(NEXT_GT_PARTICLES);
            NEXT_GT_PARTICLES.clear();
        }
    }

    public static void loadUnloadObjects() {
        synchronized (TO_UNLOAD_CHUNKS) {
            while (!TO_UNLOAD_CHUNKS.isEmpty()) {
                Chunk chunk = TO_UNLOAD_CHUNKS.removeFirst();
                deleteChunkMeshBuffers(chunk);
                Chunk.removeToGenerateBlocks(chunk.ID);
                if (chunk.isModified()) FileManager.saveChunk(chunk);
            }
        }

        synchronized (TO_UNLOAD_HEIGHT_MAPS) {
            while (!TO_UNLOAD_HEIGHT_MAPS.isEmpty()) {
                HeightMap heightMap = TO_UNLOAD_HEIGHT_MAPS.removeFirst();
                FileManager.saveHeightMap(heightMap);
            }
        }

        synchronized (TO_BUFFER_CHUNKS) {
            for (int i = 0; i < MAX_CHUNKS_TO_BUFFER_PER_FRAME && !TO_BUFFER_CHUNKS.isEmpty(); i++) {
                Chunk chunk = TO_BUFFER_CHUNKS.removeFirst();
                bufferChunkMesh(chunk);
            }
        }
    }

    public static void unloadChunks(int playerX, int playerY, int playerZ) {
        for (Chunk chunk : Chunk.getWorld()) {
            if (chunk == null) continue;

            if (Math.abs(chunk.X - playerX) <= RENDER_DISTANCE_XZ + 2 && Math.abs(chunk.Z - playerZ) <= RENDER_DISTANCE_XZ + 2 && Math.abs(chunk.Y - playerY) <= RENDER_DISTANCE_Y + 2)
                continue;

            chunk.clearMesh();
            addToUnloadChunk(chunk);

            Chunk.setNull(chunk.getIndex());
        }

        for (HeightMap heightMap : Chunk.getHeightMaps()) {
            if (heightMap == null) continue;

            if (Math.abs(heightMap.chunkX - playerX) <= RENDER_DISTANCE_XZ + 2 && Math.abs(heightMap.chunkZ - playerZ) <= RENDER_DISTANCE_XZ + 2)
                continue;

            if (heightMap.isModified()) {
                addToUnloadHeightMap(Chunk.getHeightMap(heightMap.chunkX, heightMap.chunkZ));
            }
            HeightMap.setNull(Utils.getHeightMapIndex(heightMap.chunkX, heightMap.chunkZ));
        }

        synchronized (TO_BUFFER_CHUNKS) {
            for (Iterator<Chunk> iterator = TO_BUFFER_CHUNKS.iterator(); iterator.hasNext(); ) {
                Chunk chunk = iterator.next();
                if (Math.abs(chunk.X - playerX) <= RENDER_DISTANCE_XZ + 2 && Math.abs(chunk.Z - playerZ) <= RENDER_DISTANCE_XZ + 2 && Math.abs(chunk.Y - playerY) <= RENDER_DISTANCE_Y + 2)
                    continue;

                iterator.remove();
            }
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
        int chunkIndex = chunk.getIndex();
        OpaqueModel opaqueModel = Chunk.getOpaqueModel(chunkIndex);
        if (opaqueModel != null) {
            ObjectLoader.removeVAO(opaqueModel.vao);
            ObjectLoader.removeVBO(opaqueModel.vbo);
            Chunk.setOpaqueModel(null, chunkIndex);
        }

        WaterModel waterModel = Chunk.getWaterModel(chunkIndex);
        if (waterModel != null) {
            ObjectLoader.removeVAO(waterModel.vao);
            ObjectLoader.removeVBO(waterModel.vbo);
            Chunk.setWaterModel(null, chunkIndex);
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
        player.render(timeSinceLastTick);
    }

    public static void addToBufferChunk(Chunk chunk) {
        if (chunk == null) return;
        synchronized (TO_BUFFER_CHUNKS) {
            if (!TO_BUFFER_CHUNKS.contains(chunk)) TO_BUFFER_CHUNKS.add(chunk);
        }
    }

    public static void addToUnloadChunk(Chunk chunk) {
        if (chunk == null) return;
        synchronized (TO_UNLOAD_CHUNKS) {
            TO_UNLOAD_CHUNKS.add(chunk);
        }
    }

    public static void addToUnloadHeightMap(HeightMap heightMap) {
        if (heightMap == null) return;
        synchronized (TO_UNLOAD_HEIGHT_MAPS) {
            if (TO_UNLOAD_HEIGHT_MAPS.contains(heightMap)) return;
            TO_UNLOAD_HEIGHT_MAPS.add(heightMap);
        }
    }

    public static void addParticle(Particle particle) {
        if (particle == null) return;
        synchronized (TO_SPAWN_PARTICLES) {
            TO_SPAWN_PARTICLES.add(particle);
        }
    }

    public static Player getPlayer() {
        return player;
    }

    public static void cleanUp() {
        player.cleanUp();
        ObjectLoader.cleanUp();
        generator.cleanUp();
        FileManager.savePlayer();
        FileManager.saveAllModifiedChunks();
        FileManager.saveGameState();
    }

    public static int getAmountOfToBufferChunks() {
        return TO_BUFFER_CHUNKS.size();
    }

    public static int getAmountOfEntities() {
        synchronized (ENTITIES) {
            return ENTITIES.size();
        }
    }

    public static LinkedList<Entity> getEntities() {
        return ENTITIES;
    }

    public static LinkedList<Particle> getParticles() {
        return PARTICLES;
    }

    public static int getAmountOfParticles() {
        synchronized (PARTICLES) {
            return PARTICLES.size();
        }
    }

    private static final LinkedList<Chunk> TO_BUFFER_CHUNKS = new LinkedList<>();
    private static final LinkedList<Chunk> TO_UNLOAD_CHUNKS = new LinkedList<>();
    private static final LinkedList<HeightMap> TO_UNLOAD_HEIGHT_MAPS = new LinkedList<>();
    private static ChunkGenerator generator;

    private static Player player;
    private static final LinkedList<Entity> ENTITIES = new LinkedList<>();
    private static final LinkedList<Entity> NEXT_GT_ENTITIES = new LinkedList<>();
    private static final ArrayList<Entity> TO_SPAWN_ENTITIES = new ArrayList<>();

    private static final LinkedList<Particle> PARTICLES = new LinkedList<>();
    private static final LinkedList<Particle> NEXT_GT_PARTICLES = new LinkedList<>();
    private static final LinkedList<Particle> TO_SPAWN_PARTICLES = new LinkedList<>();

    private static byte generatorRestartScheduled = 0;

    private ServerLogic() {
    }
}
