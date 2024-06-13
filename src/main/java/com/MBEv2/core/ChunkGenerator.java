package com.MBEv2.core;

import com.MBEv2.test.GameLogic;
import org.joml.Vector3f;

import java.util.LinkedList;

import static com.MBEv2.core.utils.Constants.*;

public class ChunkGenerator {

    private final Thread thread;

    private boolean shouldExecute = false;

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

            Vector3f cameraPosition = GameLogic.getPlayer().getCamera().getPosition();

            final int chunkX = (int) Math.floor(cameraPosition.x) >> 5;
            final int chunkY = (int) Math.floor(cameraPosition.y) >> 5;
            final int chunkZ = (int) Math.floor(cameraPosition.z) >> 5;

            unloadChunks(chunkX, chunkY, chunkZ);
            LinkedList<Chunk> toMeshChunks = loadChunks(chunkX, chunkY, chunkZ);
            meshChunks(toMeshChunks);
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

    private LinkedList<Chunk> loadChunks(final int chunkX, final int chunkY, final int chunkZ) {
        LinkedList<Chunk> toMeshChunks = new LinkedList<>();

        for (int x = -RENDER_DISTANCE_XZ + chunkX; x <= RENDER_DISTANCE_XZ + chunkX; x++)
            for (int y = -RENDER_DISTANCE_Y + chunkY; y <= RENDER_DISTANCE_Y + chunkY; y++)
                for (int z = -RENDER_DISTANCE_XZ + chunkZ; z <= RENDER_DISTANCE_XZ + chunkZ; z++) {

                    final long expectedId = GameLogic.getChunkId(x, y, z);
                    final int index = GameLogic.getChunkIndex(x, y, z);
                    Chunk chunk = Chunk.getChunk(index);

                    if (chunk == null) {
                        if (Chunk.containsSavedChunk(expectedId))
                            chunk = Chunk.removeSavedChunk(expectedId);
                        else
                            chunk = new Chunk(x, y, z);

                        Chunk.storeChunk(chunk);
                        toMeshChunks.add(chunk);

                    } else if (chunk.getId() != expectedId) {
                        GameLogic.addToUnloadChunk(chunk);

                        if (chunk.isModified())
                            Chunk.putSavedChunk(chunk);

                        if (Chunk.containsSavedChunk(expectedId))
                            chunk = Chunk.removeSavedChunk(expectedId);
                        else
                            chunk = new Chunk(x, y, z);

                        Chunk.storeChunk(chunk);
                        toMeshChunks.add(chunk);

                    } else if (!chunk.isMeshed())
                        toMeshChunks.add(chunk);
                }

        return toMeshChunks;
    }

    private void meshChunks(LinkedList<Chunk> toMeshChunks) {
        for (Chunk chunk : toMeshChunks) {
            if (!chunk.isGenerated())
                Chunk.generateChunk(chunk);
            chunk.generateMesh();
            if (chunk.getVertices().length != 0 || chunk.getTransparentVertices().length != 0)
                GameLogic.addToBufferChunk(chunk);
        }
    }

    public void continueRunning() {
        shouldExecute = true;
        synchronized (thread) {
            thread.notify();
        }
    }

    public void start() {
        thread.start();
    }

    public void cleanUp() {
        shouldExecute = false;
        synchronized (thread) {
            thread.notify();
        }
    }
}
