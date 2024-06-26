package com.MBEv2.core;

import com.MBEv2.core.utils.Utils;
import com.MBEv2.test.GameLogic;
import org.joml.Vector3f;

import static com.MBEv2.core.utils.Constants.*;

public class ChunkGenerator {

    private final Thread thread;

    private boolean shouldExecute = false;
    private boolean shouldFinish = true;

    public ChunkGenerator() {
        thread = new Thread(this::run);
    }

    private void run() {
        while (EngineManager.isRunning) {
            if (!shouldExecute) {
                try {
                    synchronized (thread) {
                        thread.wait();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (!shouldExecute)
                break;
            shouldExecute = false;
            shouldFinish = true;

            Vector3f cameraPosition = GameLogic.getPlayer().getCamera().getPosition();

            final int playerX = Utils.floor(cameraPosition.x) >> CHUNK_SIZE_BITS;
            final int playerY = Utils.floor(cameraPosition.y) >> CHUNK_SIZE_BITS;
            final int playerZ = Utils.floor(cameraPosition.z) >> CHUNK_SIZE_BITS;

            unloadChunks(playerX, playerY, playerZ);
            loadChunks(playerX, playerY, playerZ);
        }
    }

    public static void unloadChunks(final int chunkX, final int chunkY, final int chunkZ) {
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

    private void loadChunks(final int playerX, final int playerY, final int playerZ) {
        generateChunkColumn(playerX, playerY, playerZ);
        for (int ring = 1; ring <= RENDER_DISTANCE_XZ && shouldFinish; ring++) {
            for (int x = -ring; x < ring && shouldFinish; x++) generateChunkColumn(x + playerX, playerY, ring + playerZ);
            for (int z = ring; z > -ring && shouldFinish; z--) generateChunkColumn(ring + playerX, playerY, z + playerZ);
            for (int x = ring; x > -ring && shouldFinish; x--) generateChunkColumn(x + playerX, playerY, -ring + playerZ);
            for (int z = -ring; z < ring && shouldFinish; z++) generateChunkColumn(-ring + playerX, playerY, z + playerZ);

            if (ring == 1){
                meshChunkColumn(playerX, playerY, playerZ);
                continue;
            }
            int meshRing = ring - 1;
            for (int x = -meshRing; x < meshRing && shouldFinish; x++) meshChunkColumn(x + playerX, playerY, meshRing + playerZ);
            for (int z = meshRing; z > -meshRing && shouldFinish; z--) meshChunkColumn(meshRing + playerX, playerY, z + playerZ);
            for (int x = meshRing; x > -meshRing && shouldFinish; x--) meshChunkColumn(x + playerX, playerY, -meshRing + playerZ);
            for (int z = -meshRing; z < meshRing && shouldFinish; z++) meshChunkColumn(-meshRing + playerX, playerY, z + playerZ);
        }
    }

    private void generateChunkColumn(int x, int playerY, int z) {
        double[][] heightMap = GameLogic.heightMap(x, z);
        int[][] stoneMap = GameLogic.stoneMap(x, z, heightMap);
        double[][] featureMap = GameLogic.featureMap(x, z);
        byte[][] treeMap = GameLogic.treeMap(x, z, heightMap, stoneMap, featureMap);

        for (int y = RENDER_DISTANCE_Y + playerY; y >= -RENDER_DISTANCE_Y + playerY && shouldFinish; y--) {
            final long expectedId = GameLogic.getChunkId(x, y, z);
            Chunk chunk = Chunk.getChunk(x, y, z);

            if (chunk == null) {
                if (Chunk.containsSavedChunk(expectedId))
                    chunk = Chunk.removeSavedChunk(expectedId);
                else
                    chunk = new Chunk(x, y, z);

                Chunk.storeChunk(chunk);
                if (chunk.notGenerated())
                    chunk.generate(heightMap, stoneMap, featureMap, treeMap);

            } else if (chunk.getId() != expectedId) {
                GameLogic.addToUnloadChunk(chunk);

                if (chunk.isModified())
                    Chunk.putSavedChunk(chunk);

                if (Chunk.containsSavedChunk(expectedId))
                    chunk = Chunk.removeSavedChunk(expectedId);
                else
                    chunk = new Chunk(x, y, z);

                Chunk.storeChunk(chunk);
                if (chunk.notGenerated())
                    chunk.generate(heightMap, stoneMap, featureMap, treeMap);
            }
        }
    }

    private void meshChunkColumn(int x, int playerY, int z) {
        for (int y = RENDER_DISTANCE_Y + playerY; y >= -RENDER_DISTANCE_Y + playerY && shouldFinish; y--) {
            Chunk chunk = Chunk.getChunk(x, y, z);
            if (!chunk.isMeshed())
                meshChunk(chunk);
        }
    }

    private void meshChunk(Chunk chunk) {
        chunk.generateMesh();
        if (chunk.getVertices().length != 0 || chunk.getTransparentVertices().length != 0)
            GameLogic.addToBufferChunk(chunk);
    }

    public void continueRunning() {
        shouldExecute = true;
        shouldFinish = false;
        synchronized (thread) {
            thread.notify();
        }
    }

    public void start() {
        thread.start();
    }

    public void cleanUp() {
        shouldExecute = false;
        shouldFinish = false;
        synchronized (thread) {
            thread.notify();
        }
    }
}
