package com.MBEv2.core;

import com.MBEv2.test.GameLogic;
import org.joml.Vector3f;

import java.util.LinkedList;

import static com.MBEv2.core.utils.Constants.*;

public class ChunkGenerator {

    private Thread thread;

    private boolean shouldExecute = false;

    public ChunkGenerator() {
        thread = new Thread(this::run);
    }

    private void run() {
        while (EngineManager.isRunning) {
            if (!shouldExecute)
                break;
            shouldExecute = false;

            Vector3f cameraPosition = GameLogic.getPlayer().getCamera().getPosition();

            int chunkX = (int) Math.floor(cameraPosition.x) >> 5;
            int chunkY = (int) Math.floor(cameraPosition.y) >> 5;
            int chunkZ = (int) Math.floor(cameraPosition.z) >> 5;

            LinkedList<Chunk> toMeshChunks = loadChunks(chunkX, chunkY, chunkZ);
            meshChunks(toMeshChunks);
        }
    }

    private LinkedList<Chunk> loadChunks(int chunkX, int chunkY, int chunkZ) {
        LinkedList<Chunk> toMeshChunks = new LinkedList<>();

        for (int x = -RENDER_DISTANCE_XZ; x <= RENDER_DISTANCE_XZ; x++)
            for (int y = -RENDER_DISTANCE_Y; y <= RENDER_DISTANCE_Y; y++)
                for (int z = -RENDER_DISTANCE_XZ; z <= RENDER_DISTANCE_XZ; z++) {

                    long expectedId = GameLogic.getChunkId(chunkX + x, chunkY + y, chunkZ + z);
                    int index = GameLogic.getChunkIndex(chunkX + x, chunkY + y, chunkZ + z);
                    Chunk chunk = Chunk.getChunk(index);

                    if (chunk == null) {
                        chunk = new Chunk(chunkX + x, chunkY + y, chunkZ + z);
                        Chunk.storeChunk(chunk);
                        chunk.setMeshed(true);
                        toMeshChunks.add(chunk);

                    } else if (chunk.getId() != expectedId) {
                        GameLogic.addToUnloadModel(chunk.getModel());
                        GameLogic.addToUnloadModel(chunk.getTransparentModel());

                        chunk = new Chunk(chunkX + x, chunkY + y, chunkZ + z);
                        Chunk.storeChunk(chunk);
                        Chunk.generateChunk(chunk);
                        chunk.setMeshed(true);
                        toMeshChunks.add(chunk);

                    } else if (!chunk.isMeshed()) {
                        chunk.setMeshed(true);
                        toMeshChunks.add(chunk);
                    }
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
        if (thread.isAlive())
            return;
        thread = new Thread(this::run);
        thread.start();
    }

    public void start() {
        thread.start();
    }

    public void cleanUp() {
        shouldExecute = false;
    }
}
