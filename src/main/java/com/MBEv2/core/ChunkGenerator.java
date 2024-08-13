package com.MBEv2.core;

import com.MBEv2.core.utils.Utils;
import com.MBEv2.test.GameLogic;
import org.joml.Vector3f;
import org.joml.Vector4i;

import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static com.MBEv2.core.utils.Constants.*;

public class ChunkGenerator {

    private final ThreadPoolExecutor executor;

    private final LinkedList<Vector4i> blockChanges;

    private final GenerationStarter generationStarter;

    private final Thread starterThread;


    public ChunkGenerator() {
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(NUMBER_OF_GENERATION_THREADS);
        blockChanges = new LinkedList<>();
        generationStarter = new GenerationStarter(blockChanges, executor);
        starterThread = new Thread(generationStarter);
        starterThread.start();
    }

    public void start() {
        Vector3f playerPosition = GameLogic.getPlayer().getCamera().getPosition();
        int playerX = Utils.floor(playerPosition.x) >> CHUNK_SIZE_BITS;
        int playerY = Utils.floor(playerPosition.y) >> CHUNK_SIZE_BITS;
        int playerZ = Utils.floor(playerPosition.z) >> CHUNK_SIZE_BITS;
        generationStarter.restart(NONE, playerX, playerY, playerZ);
        synchronized (starterThread) {
            starterThread.notify();
        }
    }

    public void restart(int direction) {
        Vector3f playerPosition = GameLogic.getPlayer().getCamera().getPosition();
        int playerX = Utils.floor(playerPosition.x) >> CHUNK_SIZE_BITS;
        int playerY = Utils.floor(playerPosition.y) >> CHUNK_SIZE_BITS;
        int playerZ = Utils.floor(playerPosition.z) >> CHUNK_SIZE_BITS;
        generationStarter.restart(direction, playerX, playerY, playerZ);
        synchronized (starterThread) {
            starterThread.notify();
        }
    }

    public void addBlockChange(Vector4i blockChange) {
        blockChanges.add(blockChange);
    }

    public void cleanUp() {
        generationStarter.stop();
        synchronized (starterThread) {
            starterThread.notify();
        }
        executor.getQueue().clear();
        executor.shutdown();
    }

    static class Generator implements Runnable {

        private final int x, y, z;
        private final double[][] heightMap, temperatureMap, humidityMap, erosionMap, featureMap;

        public Generator(int x, int y, int z, double[][] heightMap, double[][] temperatureMap, double[][] humidityMap, double[][] erosionMap, double[][] featureMap) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.heightMap = heightMap;
            this.temperatureMap = temperatureMap;
            this.humidityMap = humidityMap;
            this.erosionMap = erosionMap;
            this.featureMap = featureMap;
        }

        @Override
        public void run() {
            Chunk chunk = Chunk.getChunk(x, y, z);
            WorldGeneration.generate(chunk, heightMap, temperatureMap, humidityMap, erosionMap, featureMap);
        }
    }

    static class MeshHandler implements Runnable {

        private final Chunk chunk;

        public MeshHandler(Chunk chunk) {
            this.chunk = chunk;
        }

        @Override
        public void run() {
            if (!chunk.hasPropagatedBlockLight()) {
                chunk.propagateBlockLight();
                chunk.setHasPropagatedBlockLight();
            }
            meshChunk();
        }

        private void meshChunk() {
            chunk.generateMesh();
            boolean shouldBuffer = false;
            for (int side = 0; side < 6; side++) {
                if (chunk.getVertices(side).length != 0) {
                    shouldBuffer = true;
                    break;
                }
            }
            if (shouldBuffer || chunk.getTransparentVertices().length != 0)
                GameLogic.addToBufferChunk(chunk);
        }
    }

    class GenerationStarter implements Runnable {

        private final ThreadPoolExecutor executor;
        private final LinkedList<Vector4i> changes;
        private int travelDirection;
        private int playerX, playerY, playerZ;

        private boolean shouldFinish = true;
        private boolean shouldExecute = true;
        private boolean shouldRestart = false;

        public GenerationStarter(LinkedList<Vector4i> changes, ThreadPoolExecutor executor) {
            this.changes = changes;
            this.executor = executor;
        }

        public void restart(int travelDirection, int playerX, int playerY, int playerZ) {
            shouldRestart = true;
            shouldFinish = false;
            this.travelDirection = travelDirection;
            this.playerX = playerX;
            this.playerY = playerY;
            this.playerZ = playerZ;
        }

        @Override
        public void run() {
            while (shouldExecute) {
                try {
                    synchronized (starterThread) {
                        if (!shouldRestart) starterThread.wait();
                        shouldRestart = false;
                    }
                }catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                shouldFinish = true;

                executor.getQueue().clear();
                unloadChunks(playerX, playerY, playerZ);
                handleBlockChanges();
                handleSkyLight(travelDirection, playerX, playerY, playerZ);
                submitTasks(playerX, playerY, playerZ);
            }
        }

        private void unloadChunks(int playerX, int playerY, int playerZ) {
            for (Chunk chunk : Chunk.getWorld()) {
                if (chunk == null)
                    continue;

                if (Math.abs(chunk.getX() - playerX) <= RENDER_DISTANCE_XZ + 2 && Math.abs(chunk.getZ() - playerZ) <= RENDER_DISTANCE_XZ + 2 && Math.abs(chunk.getY() - playerY) <= RENDER_DISTANCE_Y + 2)
                    continue;

                chunk.clearMesh();
                GameLogic.addToUnloadChunk(chunk);

                if (chunk.isModified())
                    Chunk.putSavedChunk(chunk);

                Chunk.setNull(chunk.getIndex());
            }
        }

        private void handleBlockChanges() {
            synchronized (changes) {
                while (!changes.isEmpty()) {

                    Vector4i blockChange = changes.removeFirst();
                    int x = blockChange.x;
                    int y = blockChange.y;
                    int z = blockChange.z;
                    short previousBlock = (short) blockChange.w;

                    short block = Chunk.getBlockInWorld(x, y, z);

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

        private void handleSkyLight(int travelDirection, int playerX, int playerY, int playerZ) {
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
                for (int chunkX = playerX - RENDER_DISTANCE_XZ - 2; chunkX <= playerX + RENDER_DISTANCE_XZ + 2; chunkX++)
                    for (int chunkZ = playerZ - RENDER_DISTANCE_XZ - 2; chunkZ <= playerZ + RENDER_DISTANCE_XZ + 2; chunkZ++) {
                        LightLogic.propagateChunkSkyLight(chunkX << CHUNK_SIZE_BITS, ((playerY - RENDER_DISTANCE_Y + 1) << CHUNK_SIZE_BITS) - 1, chunkZ << CHUNK_SIZE_BITS);
                    }
            }
        }

        private void submitTasks(int playerX, int playerY, int playerZ) {
            submitChunkColumnGeneration(playerX, playerY, playerZ);
            for (int ring = 1; ring <= RENDER_DISTANCE_XZ && shouldFinish; ring++) {
                for (int x = -ring; x < ring && shouldFinish; x++)
                    submitChunkColumnGeneration(x + playerX, playerY, ring + playerZ);
                for (int z = ring; z > -ring && shouldFinish; z--)
                    submitChunkColumnGeneration(ring + playerX, playerY, z + playerZ);
                for (int x = ring; x > -ring && shouldFinish; x--)
                    submitChunkColumnGeneration(x + playerX, playerY, -ring + playerZ);
                for (int z = -ring; z < ring && shouldFinish; z++)
                    submitChunkColumnGeneration(-ring + playerX, playerY, z + playerZ);

                if (ring == 1 && shouldFinish) {
                    submitChunkColumnMeshing(playerX, playerY, playerZ);
                    continue;
                }
                int meshRing = ring - 1;
                for (int x = -meshRing; x < meshRing && shouldFinish; x++)
                    submitChunkColumnMeshing(x + playerX, playerY, meshRing + playerZ);
                for (int z = meshRing; z > -meshRing && shouldFinish; z--)
                    submitChunkColumnMeshing(meshRing + playerX, playerY, z + playerZ);
                for (int x = meshRing; x > -meshRing && shouldFinish; x--)
                    submitChunkColumnMeshing(x + playerX, playerY, -meshRing + playerZ);
                for (int z = -meshRing; z < meshRing && shouldFinish; z++)
                    submitChunkColumnMeshing(-meshRing + playerX, playerY, z + playerZ);
            }
        }

        private void submitChunkColumnGeneration(int x, int playerY, int z) {
            double[][] heightMap = WorldGeneration.heightMap(x, z);
            double[][] temperatureMap = WorldGeneration.temperatureMap(x, z);
            double[][] humidityMap = WorldGeneration.humidityMap(x, z);
            double[][] erosionMap = WorldGeneration.erosionMap(x, z);
            double[][] featureMap = WorldGeneration.featureMap(x, z);

            for (int y = RENDER_DISTANCE_Y + playerY + 1; y >= -RENDER_DISTANCE_Y + playerY - 1 && shouldFinish; y--) {
                final long expectedId = GameLogic.getChunkId(x, y, z);
                Chunk chunk = Chunk.getChunk(x, y, z);

                if (chunk == null) {
                    if (Chunk.containsSavedChunk(expectedId))
                        chunk = Chunk.removeSavedChunk(expectedId);
                    else
                        chunk = new Chunk(x, y, z);

                    Chunk.storeChunk(chunk);
                    if (!chunk.isGenerated() && shouldExecute)
                        executor.submit(new Generator(x, y, z, heightMap, temperatureMap, humidityMap, erosionMap, featureMap));
                } else if (chunk.getId() != expectedId) {
                    GameLogic.addToUnloadChunk(chunk);

                    if (chunk.isModified())
                        Chunk.putSavedChunk(chunk);

                    if (Chunk.containsSavedChunk(expectedId))
                        chunk = Chunk.removeSavedChunk(expectedId);
                    else
                        chunk = new Chunk(x, y, z);

                    Chunk.storeChunk(chunk);
                    if (!chunk.isGenerated() && shouldExecute)
                        executor.submit(new Generator(x, y, z, heightMap, temperatureMap, humidityMap, erosionMap, featureMap));
                }
            }
        }

        private void submitChunkColumnMeshing(int x, int playerY, int z) {
            LightLogic.setChunkColumnSkyLight(x << CHUNK_SIZE_BITS, ((playerY + RENDER_DISTANCE_Y + 1) << CHUNK_SIZE_BITS) - 1, z << CHUNK_SIZE_BITS);

            for (int y = RENDER_DISTANCE_Y + playerY; y >= -RENDER_DISTANCE_Y + playerY && shouldFinish; y--) {
                Chunk chunk = Chunk.getChunk(x, y, z);
                if (chunk == null)
                    continue;
                if (chunk.isMeshed())
                    continue;
                if (!shouldExecute)
                    return;
                executor.submit(new MeshHandler(chunk));
            }
        }

        public void stop() {
            shouldFinish = false;
            shouldExecute = false;
        }
    }
}
