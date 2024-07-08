package com.MBEv2.core;

import com.MBEv2.core.utils.Utils;
import com.MBEv2.test.GameLogic;
import org.joml.Vector3f;
import org.joml.Vector4i;

import java.util.LinkedList;

import static com.MBEv2.core.utils.Constants.*;

public class ChunkGenerator {

    private final Thread thread;

    private boolean shouldExecute = false;
    private boolean shouldFinish = true;

    private final LinkedList<Vector4i> changes;

    public ChunkGenerator() {
        thread = new Thread(this::run);
        changes = new LinkedList<>();
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

            processLightChanges();
            unloadChunks(playerX, playerY, playerZ);
            loadChunks(playerX, playerY, playerZ);
        }
    }

    public void processLightChanges() {
        synchronized (changes) {
            while (!changes.isEmpty() && shouldFinish) {
                Vector4i change = changes.removeFirst();
                int x = change.x;
                int y = change.y;
                int z = change.z;
                byte previousBlock = (byte) change.w;
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

    public void unloadChunks(final int chunkX, final int chunkY, final int chunkZ) {
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
            for (int x = -ring; x < ring && shouldFinish; x++)
                generateChunkColumn(x + playerX, playerY, ring + playerZ);
            for (int z = ring; z > -ring && shouldFinish; z--)
                generateChunkColumn(ring + playerX, playerY, z + playerZ);
            for (int x = ring; x > -ring && shouldFinish; x--)
                generateChunkColumn(x + playerX, playerY, -ring + playerZ);
            for (int z = -ring; z < ring && shouldFinish; z++)
                generateChunkColumn(-ring + playerX, playerY, z + playerZ);

            if (ring == 1) {
                meshChunkColumn(playerX, playerY, playerZ);
                continue;
            }
            int meshRing = ring - 1;
            for (int x = -meshRing; x < meshRing && shouldFinish; x++)
                meshChunkColumn(x + playerX, playerY, meshRing + playerZ);
            for (int z = meshRing; z > -meshRing && shouldFinish; z--)
                meshChunkColumn(meshRing + playerX, playerY, z + playerZ);
            for (int x = meshRing; x > -meshRing && shouldFinish; x--)
                meshChunkColumn(x + playerX, playerY, -meshRing + playerZ);
            for (int z = -meshRing; z < meshRing && shouldFinish; z++)
                meshChunkColumn(-meshRing + playerX, playerY, z + playerZ);
        }
    }

    private void generateChunkColumn(int x, int playerY, int z) {
        double[][] heightMap = WorldGeneration.heightMap(x, z);
        int[][] stoneMap = WorldGeneration.stoneMap(x, z, heightMap);
        double[][] featureMap = WorldGeneration.featureMap(x, z);
        byte[][] treeMap = WorldGeneration.treeMap(x, z, heightMap, stoneMap, featureMap);

        for (int y = RENDER_DISTANCE_Y + playerY; y >= -RENDER_DISTANCE_Y + playerY && shouldFinish; y--) {
            final long expectedId = GameLogic.getChunkId(x, y, z);
            Chunk chunk = Chunk.getChunk(x, y, z);

            if (chunk == null) {
                if (Chunk.containsSavedChunk(expectedId))
                    chunk = Chunk.removeSavedChunk(expectedId);
                else
                    chunk = new Chunk(x, y, z);

                Chunk.storeChunk(chunk);
                if (!chunk.isGenerated())
                    WorldGeneration.generate(heightMap, stoneMap, featureMap, treeMap, chunk);

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
                    WorldGeneration.generate(heightMap, stoneMap, featureMap, treeMap, chunk);
            }
        }
    }

    private void meshChunkColumn(int x, int playerY, int z) {

        LightLogic.setChunkColumnSkyLight(x << CHUNK_SIZE_BITS, (playerY + RENDER_DISTANCE_Y) << CHUNK_SIZE_BITS, z << CHUNK_SIZE_BITS);

        for (int y = RENDER_DISTANCE_Y + playerY; y >= -RENDER_DISTANCE_Y + playerY && shouldFinish; y--) {
            Chunk chunk = Chunk.getChunk(x, y, z);
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

    public LinkedList<Vector4i> getChanges() {
        return changes;
    }

    public void addChange(Vector4i change) {
        changes.add(change);
    }
}
