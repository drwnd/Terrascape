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

            final int chunkX = Utils.floor(cameraPosition.x) >> CHUNK_SIZE_BITS;
            final int chunkY = Utils.floor(cameraPosition.y) >> CHUNK_SIZE_BITS;
            final int chunkZ = Utils.floor(cameraPosition.z) >> CHUNK_SIZE_BITS;

            unloadChunks(chunkX, chunkY, chunkZ);
            loadChunks(chunkX, chunkY, chunkZ);
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

    private void loadChunks(final int chunkX, final int chunkY, final int chunkZ) {
        loadChunkColumn(chunkX, chunkY, chunkZ);
        for (int ring = 1; ring <= RENDER_DISTANCE_XZ && shouldFinish; ring++) {
            for (int x = -ring; x < ring && shouldFinish; x++) loadChunkColumn(x + chunkX, chunkY, ring + chunkZ);
            for (int z = ring; z > -ring && shouldFinish; z--) loadChunkColumn(ring + chunkX, chunkY, z + chunkZ);
            for (int x = ring; x > -ring && shouldFinish; x--) loadChunkColumn(x + chunkX, chunkY, -ring + chunkZ);
            for (int z = -ring; z < ring && shouldFinish; z++) loadChunkColumn(-ring + chunkX, chunkY, z + chunkZ);
        }
    }

    private void loadChunkColumn(int x, int chunkY, int z) {
        double[][] heightMap = GameLogic.heightMap(x, z);
        int[][] stoneMap = GameLogic.stoneMap(x, z, heightMap);
        double[][] featureMap = GameLogic.featureMap(x, z);
        byte[][] treeMap = GameLogic.treeMap(x, z, heightMap, stoneMap, featureMap);

        for (int y = -RENDER_DISTANCE_Y + chunkY; y <= RENDER_DISTANCE_Y + chunkY && shouldFinish; y++) {

            final long expectedId = GameLogic.getChunkId(x, y, z);
            final int index = GameLogic.getChunkIndex(x, y, z);
            Chunk chunk = Chunk.getChunk(index);

            if (chunk == null) {
                if (Chunk.containsSavedChunk(expectedId))
                    chunk = Chunk.removeSavedChunk(expectedId);
                else
                    chunk = new Chunk(x, y, z);

                Chunk.storeChunk(chunk);
                meshChunk(chunk);
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
                meshChunk(chunk);
                if (chunk.notGenerated())
                    chunk.generate(heightMap, stoneMap, featureMap, treeMap);

            } else if (!chunk.isMeshed()) {
                meshChunk(chunk);
                if (chunk.notGenerated())
                    chunk.generate(heightMap, stoneMap, featureMap, treeMap);
            }
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
