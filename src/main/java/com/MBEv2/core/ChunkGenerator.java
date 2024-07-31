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

    private GenerationStarter previousGenerationStarter;


    public ChunkGenerator() {
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(NUMBER_OF_GENERATION_THREADS);
        blockChanges = new LinkedList<>();
    }

    public void start() {
        Vector3f playerPosition = GameLogic.getPlayer().getCamera().getPosition();
        int playerX = Utils.floor(playerPosition.x) >> CHUNK_SIZE_BITS;
        int playerY = Utils.floor(playerPosition.y) >> CHUNK_SIZE_BITS;
        int playerZ = Utils.floor(playerPosition.z) >> CHUNK_SIZE_BITS;
        previousGenerationStarter = new GenerationStarter(blockChanges, NONE, playerX, playerY, playerZ, executor);
        new Thread(previousGenerationStarter).start();
    }

    public void restart(int direction) {
        Vector3f playerPosition = GameLogic.getPlayer().getCamera().getPosition();
        int playerX = Utils.floor(playerPosition.x) >> CHUNK_SIZE_BITS;
        int playerY = Utils.floor(playerPosition.y) >> CHUNK_SIZE_BITS;
        int playerZ = Utils.floor(playerPosition.z) >> CHUNK_SIZE_BITS;
        previousGenerationStarter.stop();
        previousGenerationStarter = new GenerationStarter(blockChanges, direction, playerX, playerY, playerZ, executor);
        new Thread(previousGenerationStarter).start();
    }

    public void addBlockChange(Vector4i blockChange) {
        blockChanges.add(blockChange);
    }

    public void cleanUp() {
        previousGenerationStarter.stop();
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
            if (chunk.getVertices().length != 0 || chunk.getTransparentVertices().length != 0)
                GameLogic.addToBufferChunk(chunk);
        }
    }

    static class GenerationStarter implements Runnable {

        private final ThreadPoolExecutor executor;
        private final LinkedList<Vector4i> changes;
        private final int travelDirection;
        private final int playerX, playerY, playerZ;

        private boolean shouldFinish = true;

        public GenerationStarter(LinkedList<Vector4i> changes, int travelDirection, int playerX, int playerY, int playerZ, ThreadPoolExecutor executor) {
            this.executor = executor;
            this.changes = changes;
            this.travelDirection = travelDirection;
            this.playerX = playerX;
            this.playerY = playerY;
            this.playerZ = playerZ;
        }

        @Override
        public void run() {
//            long time = System.nanoTime();
//            System.out.println(System.nanoTime() - time);
            executor.getQueue().clear();
            unloadChunks();
            handleBlockChanges();
            handleSkyLight();
            submitTasks();
        }

        private void unloadChunks() {
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

        private void handleSkyLight() {
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

        private void submitTasks() {
            submitChunkColumnGeneration(playerX, playerZ);
            for (int ring = 1; ring <= RENDER_DISTANCE_XZ && shouldFinish; ring++) {
                for (int x = -ring; x < ring && shouldFinish; x++)
                    submitChunkColumnGeneration(x + playerX, ring + playerZ);
                for (int z = ring; z > -ring && shouldFinish; z--)
                    submitChunkColumnGeneration(ring + playerX, z + playerZ);
                for (int x = ring; x > -ring && shouldFinish; x--)
                    submitChunkColumnGeneration(x + playerX, -ring + playerZ);
                for (int z = -ring; z < ring && shouldFinish; z++)
                    submitChunkColumnGeneration(-ring + playerX, z + playerZ);

                if (ring == 1 && shouldFinish) {
                    submitChunkColumnMeshing(playerX, playerZ);
                    continue;
                }
                int meshRing = ring - 1;
                for (int x = -meshRing; x < meshRing && shouldFinish; x++)
                    submitChunkColumnMeshing(x + playerX, meshRing + playerZ);
                for (int z = meshRing; z > -meshRing && shouldFinish; z--)
                    submitChunkColumnMeshing(meshRing + playerX, z + playerZ);
                for (int x = meshRing; x > -meshRing && shouldFinish; x--)
                    submitChunkColumnMeshing(x + playerX, -meshRing + playerZ);
                for (int z = -meshRing; z < meshRing && shouldFinish; z++)
                    submitChunkColumnMeshing(-meshRing + playerX, z + playerZ);
            }
        }

        private void submitChunkColumnGeneration(int x, int z) {
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
                    if (!chunk.isGenerated())
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
                    if (!chunk.isGenerated())
                        executor.submit(new Generator(x, y, z, heightMap, temperatureMap, humidityMap, erosionMap, featureMap));
                }
            }
        }

        private void submitChunkColumnMeshing(int x, int z) {
            LightLogic.setChunkColumnSkyLight(x << CHUNK_SIZE_BITS, ((playerY + RENDER_DISTANCE_Y + 1) << CHUNK_SIZE_BITS) - 1, z << CHUNK_SIZE_BITS);

            for (int y = RENDER_DISTANCE_Y + playerY; y >= -RENDER_DISTANCE_Y + playerY && shouldFinish; y--) {
                Chunk chunk = Chunk.getChunk(x, y, z);
                if (chunk == null)
                    continue;
                if (chunk.isMeshed())
                    continue;
                executor.submit(new MeshHandler(chunk));
            }
        }

        public void stop() {
            shouldFinish = false;
        }
    }
}
