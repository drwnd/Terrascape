package com.MBEv2.core;

import com.MBEv2.core.utils.Utils;
import com.MBEv2.test.GameLogic;
import org.joml.Vector3f;
import org.joml.Vector4i;

import java.util.LinkedList;

import static com.MBEv2.core.utils.Constants.*;

public class ChunkGenerator {

    private final Thread genThread;
    private final Thread meshThread;

    private boolean genThreadShouldExecute = false;
    private boolean genThreadShouldFinish = true;
    private boolean meshThreadShouldExecute = true;
    private boolean meshThreadShouldFinish = true;
    private int travelDirection;

    private int maxToMeshRing = -1;

    private int playerX;
    private int playerY;
    private int playerZ;

    private final LinkedList<Vector4i> blockChanges;

    public ChunkGenerator() {
        genThread = new Thread(this::runGenThread);
        meshThread = new Thread(this::runMeshThread);
        blockChanges = new LinkedList<>();
    }

    private void runGenThread() {
        while (EngineManager.isRunning) {
            if (!genThreadShouldExecute) {
                try {
                    synchronized (genThread) {
                        genThread.wait();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (!genThreadShouldExecute)
                break;
            genThreadShouldExecute = false;
            genThreadShouldFinish = true;

            Vector3f cameraPosition = GameLogic.getPlayer().getCamera().getPosition();

            playerX = Utils.floor(cameraPosition.x) >> CHUNK_SIZE_BITS;
            playerY = Utils.floor(cameraPosition.y) >> CHUNK_SIZE_BITS;
            playerZ = Utils.floor(cameraPosition.z) >> CHUNK_SIZE_BITS;

            maxToMeshRing = -1;
            meshThreadShouldFinish = false;

            processBlockChanges();
            processSkyLight();
            unloadChunks(playerX, playerY, playerZ);
            generateChunks();
        }
    }

    private void runMeshThread() {
        while (EngineManager.isRunning) {
            try {
                synchronized (meshThread) {
                    meshThread.wait();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (!meshThreadShouldExecute)
                break;

            meshChunks();
        }
    }

    private void processSkyLight() {
        if (travelDirection == TOP)
            for (Chunk chunk : Chunk.getWorld()) {
                if (chunk == null)
                    continue;
                byte[] light = chunk.getLight();
                for (int index = 0; index < light.length; index++)
                    light[index] &= 15;
                chunk.setMeshed(false);
            }
        else if (travelDirection == BOTTOM) {
            for (int chunkX = playerX - RENDER_DISTANCE_XZ; chunkX <= playerX + RENDER_DISTANCE_XZ; chunkX++)
                for (int chunkZ = playerZ - RENDER_DISTANCE_XZ; chunkZ <= playerZ + RENDER_DISTANCE_XZ; chunkZ++) {
                    LightLogic.propagateChunkSkyLight(chunkX << CHUNK_SIZE_BITS, ((playerY - RENDER_DISTANCE_Y + 1) << CHUNK_SIZE_BITS) - 1, chunkZ << CHUNK_SIZE_BITS);
                }
        }
    }

    public void processBlockChanges() {
        synchronized (blockChanges) {
            while (!blockChanges.isEmpty()) {

                Vector4i blockChange = blockChanges.removeFirst();
                int x = blockChange.x;
                int y = blockChange.y;
                int z = blockChange.z;
                byte previousBlock = (byte) blockChange.w;

                byte block = Chunk.getBlockInWorld(x, y, z);

                boolean blockEmitsLight = (Block.getBlockProperties(block) & LIGHT_EMITTING_MASK) != 0;
                boolean previousBlockEmitsLight = (Block.getBlockProperties(previousBlock) & LIGHT_EMITTING_MASK) != 0;

                if (blockEmitsLight && !previousBlockEmitsLight)
                    LightLogic.setBlockLight(x, y, z, MAX_BLOCK_LIGHT_VALUE);
                else if (block == AIR)
                    if (previousBlockEmitsLight)
                        LightLogic.dePropagateBlockLight(x, y, z);
                    else
                        LightLogic.setBlockLight(x, y, z, LightLogic.getMaxSurroundingBlockLight(x, y, z) - 1);
                else if (!blockEmitsLight)
                    LightLogic.dePropagateBlockLight(x, y, z);

                if (block == AIR)
                    LightLogic.setSkyLight(x, y, z, LightLogic.getMaxSurroundingSkyLight(x, y, z) - 1);
                else
                    LightLogic.dePropagateSkyLight(x, y, z);
            }
        }
    }

    private void unloadChunks(final int chunkX, final int chunkY, final int chunkZ) {
        for (Chunk chunk : Chunk.getWorld()) {
            if (chunk == null)
                continue;

            if (Math.abs(chunk.getX() - chunkX) <= RENDER_DISTANCE_XZ + 2 && Math.abs(chunk.getZ() - chunkZ) <= RENDER_DISTANCE_XZ + 2 && Math.abs(chunk.getY() - chunkY) <= RENDER_DISTANCE_Y + 2)
                continue;

            chunk.clearMesh();
            GameLogic.addToUnloadChunk(chunk);

            if (chunk.isModified())
                Chunk.putSavedChunk(chunk);

            Chunk.setNull(chunk.getIndex());
        }
    }

    private void generateChunks() {
        generateChunkColumn(playerX, playerY, playerZ);
        for (int ring = 1; ring <= RENDER_DISTANCE_XZ + 1 && genThreadShouldFinish; ring++) {
//            long startTime = System.nanoTime();
            for (int x = -ring; x < ring && genThreadShouldFinish; x++)
                generateChunkColumn(x + playerX, playerY, ring + playerZ);
            for (int z = ring; z > -ring && genThreadShouldFinish; z--)
                generateChunkColumn(ring + playerX, playerY, z + playerZ);
            for (int x = ring; x > -ring && genThreadShouldFinish; x--)
                generateChunkColumn(x + playerX, playerY, -ring + playerZ);
            for (int z = -ring; z < ring && genThreadShouldFinish; z++)
                generateChunkColumn(-ring + playerX, playerY, z + playerZ);

//            System.out.println(System.nanoTime() - startTime + " generating ring " + ring);
            if (genThreadShouldFinish) {
                maxToMeshRing = ring - 1;
                meshThreadShouldFinish = true;
                synchronized (meshThread) {
                    meshThread.notify();
                }
            }
        }
    }

    private void meshChunks() {
        if (maxToMeshRing >= 0 && meshThreadShouldFinish)
            meshChunkColumn(playerX, playerY, playerZ);
        for (int meshRing = 1; meshRing <= maxToMeshRing && meshThreadShouldFinish; meshRing++) {
//            long startTime = System.nanoTime();

            for (int x = -meshRing; x < meshRing && meshThreadShouldFinish; x++)
                meshChunkColumn(x + playerX, playerY, meshRing + playerZ);
            for (int z = meshRing; z > -meshRing && meshThreadShouldFinish; z--)
                meshChunkColumn(meshRing + playerX, playerY, z + playerZ);
            for (int x = meshRing; x > -meshRing && meshThreadShouldFinish; x--)
                meshChunkColumn(x + playerX, playerY, -meshRing + playerZ);
            for (int z = -meshRing; z < meshRing && meshThreadShouldFinish; z++)
                meshChunkColumn(-meshRing + playerX, playerY, z + playerZ);

//            System.out.println(System.nanoTime() - startTime + " meshing ring " + meshRing);
        }
    }

    private void generateChunkColumn(int x, int playerY, int z) {
        double[][] heightMap = WorldGeneration.heightMap(x, z);
        double[][] temperatureMap = WorldGeneration.temperatureMap(x, z);
        double[][] humidityMap = WorldGeneration.humidityMap(x, z);
        double[][] erosionMap = WorldGeneration.erosionMap(x, z);
        double[][] featureMap = WorldGeneration.featureMap(x, z);

        for (int y = RENDER_DISTANCE_Y + playerY + 1; y >= -RENDER_DISTANCE_Y + playerY - 1 && genThreadShouldFinish; y--) {
            final long expectedId = GameLogic.getChunkId(x, y, z);
            Chunk chunk = Chunk.getChunk(x, y, z);

            if (chunk == null) {
                if (Chunk.containsSavedChunk(expectedId))
                    chunk = Chunk.removeSavedChunk(expectedId);
                else
                    chunk = new Chunk(x, y, z);

                Chunk.storeChunk(chunk);
                if (!chunk.isGenerated())
                    WorldGeneration.generate(chunk, heightMap, temperatureMap, humidityMap, erosionMap, featureMap);

            } else if (chunk.getId() != expectedId) {
                GameLogic.addToUnloadChunk(chunk);

                if (chunk.isModified())
                    Chunk.putSavedChunk(chunk);

                if (Chunk.containsSavedChunk(expectedId))
                    chunk = Chunk.removeSavedChunk(expectedId);
                else
                    chunk = new Chunk(x, y, z);

                Chunk.storeChunk(chunk);
                if (!chunk.isGenerated())
                    WorldGeneration.generate(chunk, heightMap, temperatureMap, humidityMap, erosionMap, featureMap);
            }
        }
    }

    private void meshChunkColumn(int x, int playerY, int z) {
        LightLogic.setChunkColumnSkyLight(x << CHUNK_SIZE_BITS, ((playerY + RENDER_DISTANCE_Y + 1) << CHUNK_SIZE_BITS) - 1, z << CHUNK_SIZE_BITS);

        for (int y = RENDER_DISTANCE_Y + playerY; y >= -RENDER_DISTANCE_Y + playerY && meshThreadShouldFinish; y--) {
            Chunk chunk = Chunk.getChunk(x, y, z);
            if (chunk == null)
                continue;
            if (!chunk.hasPropagatedBlockLight()) {
                chunk.propagateBlockLight();
                chunk.setHasPropagatedBlockLight();
            }
            if (!chunk.isMeshed())
                meshChunk(chunk);
        }
    }

    private void meshChunk(Chunk chunk) {
        chunk.generateMesh();
        if (chunk.getVertices().length != 0 || chunk.getTransparentVertices().length != 0)
            GameLogic.addToBufferChunk(chunk);
    }

    public void restart(int travelDirection) {
        genThreadShouldExecute = true;
        genThreadShouldFinish = false;
        meshThreadShouldFinish = false;
        this.travelDirection = travelDirection;
        synchronized (genThread) {
            genThread.notify();
        }
    }

    public void start() {
        meshThread.start();
        genThread.start();
    }

    public void cleanUp() {
        genThreadShouldExecute = false;
        genThreadShouldFinish = false;
        meshThreadShouldFinish = false;
        meshThreadShouldExecute = false;
        synchronized (genThread) {
            genThread.notify();
        }
        synchronized (meshThread) {
            meshThread.notify();
        }
    }

    public void addBlockChange(Vector4i change) {
        blockChanges.add(change);
    }
}
