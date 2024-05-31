package com.MBEv2.core;

import com.MBEv2.test.GameLogic;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.LinkedList;

import static com.MBEv2.core.utils.Constants.RENDER_DISTANCE_XZ;
import static com.MBEv2.core.utils.Constants.RENDER_DISTANCE_Y;

public class ChunkGenerator {

    private final GameLogic gameLogic;
    private final Thread thread;

    private boolean shouldExecute = false;

    public ChunkGenerator(GameLogic gameLogic) {
        this.gameLogic = gameLogic;

        thread = new Thread(this::run);
    }

    private void run() {
        while (EngineManager.isRunning) {
            if (!shouldExecute) {
                thread.interrupt();
                continue;
            }

            shouldExecute = false;

            Vector3f originalCameraPosition = gameLogic.getPlayer().getCamera().getPosition();

            Vector3f cameraPosition = new Vector3f().set(originalCameraPosition);

            int chunkX = (int) Math.floor(cameraPosition.x) >> 5;
            int chunkY = (int) Math.floor(cameraPosition.y) >> 5;
            int chunkZ = (int) Math.floor(cameraPosition.z) >> 5;

            LinkedList<Chunk> toMeshChunks = loadChunks(chunkX, chunkY, chunkZ);
            unloadChunks(chunkX, chunkY, chunkZ);
            meshChunks(toMeshChunks);
        }
    }

    private LinkedList<Chunk> loadChunks(int chunkX, int chunkY, int chunkZ) {
        LinkedList<Chunk> toMeshChunks = new LinkedList<>();

        for (int x = -RENDER_DISTANCE_XZ; x < RENDER_DISTANCE_XZ; x++)
            for (int y = -RENDER_DISTANCE_Y; y < RENDER_DISTANCE_Y; y++)
                for (int z = -RENDER_DISTANCE_XZ; z < RENDER_DISTANCE_XZ; z++) {

                    long id = GameLogic.getChunkId(chunkX + x, chunkY + y, chunkZ + z);

                    if (!Chunk.world.containsKey(id)) {
                        Chunk chunk = new Chunk(chunkX + x, chunkY + y, chunkZ + z);
                        Chunk.storeChunk(chunk);
                        chunk.setMeshed(true);
                        chunk.setLoaded(true);
                        toMeshChunks.add(chunk);
                    } else if (!Chunk.getChunk(id).isMeshed()) {
                        Chunk chunk = Chunk.getChunk(id);
                        chunk.setMeshed(true);
                        chunk.setLoaded(true);
                        toMeshChunks.add(chunk);
                    }
                }


        return toMeshChunks;
    }

    private void unloadChunks(int chunkX, int chunkY, int chunkZ) {
        LinkedList<Chunk> toUnloadChunks = new LinkedList<>();

        for (Iterator<Chunk> iterator = Chunk.world.values().iterator(); iterator.hasNext(); ) {
            Chunk chunk = iterator.next();
            if (!chunk.isLoaded()) continue;

            if (Math.abs(chunk.X - chunkX) > RENDER_DISTANCE_XZ + 2 || Math.abs(chunk.Z - chunkZ) > RENDER_DISTANCE_XZ + 2 || Math.abs(chunk.Y - chunkY) > RENDER_DISTANCE_Y + 2) {
                toUnloadChunks.add(chunk);

                if (!chunk.isModified())
                    iterator.remove();
            }
        }
        gameLogic.setToUnloadChunks(toUnloadChunks);
    }

    private void meshChunks(LinkedList<Chunk> toMeshChunks) {
        for (Chunk chunk : toMeshChunks) {
            if (!chunk.isGenerated())
                Chunk.generateChunk(chunk);
            chunk.generateMesh();
            if (chunk.getVertices().length != 0 || chunk.getTransparentVertices().length != 0)
                gameLogic.addToBufferChunk(chunk);
        }
    }

    public void continueRunning() {
        shouldExecute = true;
        if (!thread.isAlive())
            thread.start();
    }

    public void start() {
        thread.start();
    }
}
