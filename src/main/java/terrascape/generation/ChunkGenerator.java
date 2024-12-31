package terrascape.generation;

import terrascape.server.*;
import terrascape.dataStorage.Chunk;
import terrascape.dataStorage.FileManager;
import terrascape.dataStorage.HeightMap;
import terrascape.utils.Utils;
import terrascape.server.GameLogic;
import org.joml.Vector3f;
import org.joml.Vector4i;

import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static terrascape.utils.Constants.*;
import static terrascape.utils.Settings.*;

public class ChunkGenerator {

    private final ThreadPoolExecutor executor;
    private final LinkedList<Vector4i> blockChanges;

    private final GenerationStarter generationStarter;

    private final Thread starterThread;

    private boolean shouldFinish = true;
    private boolean shouldExecute = true;
    private boolean shouldRestart = false;

    public ChunkGenerator() {
        blockChanges = new LinkedList<>();
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(NUMBER_OF_GENERATION_THREADS);
        generationStarter = new GenerationStarter();
        starterThread = new Thread(generationStarter);
    }

    public void start() {
        Vector3f playerPosition = GameLogic.getPlayer().getCamera().getPosition();
        int playerX = Utils.floor(playerPosition.x) >> CHUNK_SIZE_BITS;
        int playerY = Utils.floor(playerPosition.y) >> CHUNK_SIZE_BITS;
        int playerZ = Utils.floor(playerPosition.z) >> CHUNK_SIZE_BITS;
        starterThread.start();
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
        synchronized (executor) {
            executor.getQueue().clear();
        }
        generationStarter.restart(direction, playerX, playerY, playerZ);
        synchronized (starterThread) {
            starterThread.notify();
        }
    }

    public void waitUntilHalt() {
        executor.getQueue().clear();
        executor.shutdown();
        generationStarter.halt();

        try {
            //noinspection ResultOfMethodCallIgnored
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            System.err.println("Crashed when awaiting termination");
            e.printStackTrace();
        }
        synchronized (starterThread) {
            starterThread.notify();
            try {
                starterThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void addBlockChange(Vector4i blockChange) {
        synchronized (blockChanges) {
            blockChanges.add(blockChange);
        }
    }

    public void cleanUp() {
        generationStarter.stop();
        synchronized (starterThread) {
            starterThread.notify();
        }
        executor.getQueue().clear();
        executor.shutdown();
    }

    class Generator implements Runnable {

        private final int chunkX, playerY, chunkZ;

        public Generator(int chunkX, int playerY, int chunkZ) {
            this.chunkX = chunkX;
            this.playerY = playerY;
            this.chunkZ = chunkZ;
        }

        @Override
        public void run() {
            HeightMap heightMapObject = Chunk.getHeightMap(chunkX, chunkZ);

            if (heightMapObject == null || heightMapObject.chunkX != chunkX || heightMapObject.chunkZ != chunkZ) {
                heightMapObject = FileManager.getHeightMap(chunkX, chunkZ);
                if (heightMapObject == null)
                    heightMapObject = new HeightMap(new int[CHUNK_SIZE * CHUNK_SIZE], chunkX, chunkZ);
                Chunk.setHeightMap(heightMapObject, GameLogic.getHeightMapIndex(chunkX, chunkZ));
            }
            int[] intHeightMap = heightMapObject.map;

            GenerationData generationData = new GenerationData(chunkX, chunkZ);
            boolean hasGenerated = false;

            for (int chunkY = playerY + RENDER_DISTANCE_Y + 1; chunkY >= playerY - RENDER_DISTANCE_Y - 1 && shouldFinish; chunkY--) {
                try {
                    final long expectedId = GameLogic.getChunkId(chunkX, chunkY, chunkZ);
                    Chunk chunk = Chunk.getChunk(chunkX, chunkY, chunkZ);

                    if (chunk == null) {
                        chunk = FileManager.getChunk(expectedId);
                        if (chunk == null) chunk = new Chunk(chunkX, chunkY, chunkZ);
                        else WorldGeneration.generateSurroundingChunkStructureBlocks(chunk, generationData);

                        Chunk.storeChunk(chunk);
                    } else if (chunk.id != expectedId) {
                        System.err.println("found chunk has wrong id");
                        GameLogic.addToUnloadChunk(chunk);

                        chunk = FileManager.getChunk(expectedId);
                        if (chunk == null) chunk = new Chunk(chunkX, chunkY, chunkZ);
                        else WorldGeneration.generateSurroundingChunkStructureBlocks(chunk, generationData);

                        Chunk.storeChunk(chunk);
                    }
                    if (!chunk.isGenerated()) {
                        WorldGeneration.generate(chunk, generationData);
                        hasGenerated = true;
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    System.err.println("Generator:");
                    System.err.println(exception.getClass());
                    System.err.println(chunkX + " " + chunkY + " " + chunkZ);
                }
            }

            if (!hasGenerated) return;

            for (int x = chunkX << CHUNK_SIZE_BITS, maxX = x + CHUNK_SIZE; x < maxX; x++)
                for (int z = chunkZ << CHUNK_SIZE_BITS, maxZ = z + CHUNK_SIZE; z < maxZ; z++) {

                    int height = intHeightMap[(x & CHUNK_SIZE_MASK) << CHUNK_SIZE_BITS | (z & CHUNK_SIZE_MASK)];
                    int y = (playerY + RENDER_DISTANCE_Y << CHUNK_SIZE_BITS) + CHUNK_SIZE - 1;

                    if (height >= y) continue;

                    for (; y > height; y--) {
                        short block = Chunk.getBlockInWorld(x, y, z);
                        if (block == AIR || block == OUT_OF_WORLD) continue;

                        intHeightMap[(x & CHUNK_SIZE_MASK) << CHUNK_SIZE_BITS | (z & CHUNK_SIZE_MASK)] = y;
                        break;
                    }
                }
        }

    }

    class MeshHandler implements Runnable {

        private final int chunkX, playerY, chunkZ, travelDirection;

        public MeshHandler(int chunkX, int playerY, int chunkZ, int travelDirection) {
            this.chunkX = chunkX;
            this.playerY = playerY;
            this.chunkZ = chunkZ;
            this.travelDirection = travelDirection;
        }

        @Override
        public void run() {
            try {
                if (travelDirection == BOTTOM) handleSkyLightBottom();
                handleSkyLightTop();
            } catch (Exception e) {
                System.err.println(e.getClass());
                e.printStackTrace();
            }

            MeshGenerator meshGenerator = new MeshGenerator();

            for (int chunkY = playerY + RENDER_DISTANCE_Y; chunkY >= playerY - RENDER_DISTANCE_Y && shouldFinish; chunkY--) {
                try {
                    Chunk chunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
                    if (chunk == null) {
                        System.err.println("to mesh chunk is null");
                        System.err.println(chunkX + " " + chunkY + " " + chunkZ);
                        continue;
                    }
                    if (!chunk.isGenerated()) {
                        System.err.println("to mesh chunk hasn't been generated");
                        System.err.println(chunkX + " " + chunkY + " " + chunkZ);
                        WorldGeneration.generate(chunk);
                    }
                    if (!chunk.hasPropagatedBlockLight()) {
                        chunk.propagateBlockLight();
                        chunk.setHasPropagatedBlockLight();
                    }
                    if (chunk.isMeshed()) continue;
                    meshChunk(meshGenerator, chunk);

                } catch (Exception exception) {
                    System.err.println("Meshing:");
                    System.err.println(exception.getClass());
                    exception.printStackTrace();
                    System.err.println(chunkX + " " + chunkY + " " + chunkZ);
                }
            }
        }

        private void meshChunk(MeshGenerator meshGenerator, Chunk chunk) {
            meshGenerator.setChunk(chunk);
            meshGenerator.generateMesh();
            GameLogic.addToBufferChunk(chunk);
        }

        private void handleSkyLightBottom() {
            int minY = Integer.MAX_VALUE;
            for (int chunkY = 0; chunkY < RENDERED_WORLD_HEIGHT; chunkY++) {
                Chunk chunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
                if (chunk == null || !chunk.isGenerated()) continue;
                if (chunk.Y < minY) minY = chunk.Y;
            }
            if (minY == Integer.MAX_VALUE) return;

            LightLogic.propagateChunkSkyLight(chunkX, minY, chunkZ);
        }

        private void handleSkyLightTop() {
            int maxY = Integer.MIN_VALUE;
            for (int chunkY = RENDERED_WORLD_HEIGHT; chunkY >= 0; chunkY--) {
                Chunk chunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
                if (chunk == null || !chunk.isGenerated()) continue;
                if (chunk.Y > maxY) maxY = chunk.Y;
            }
            if (maxY == Integer.MIN_VALUE) return;

            LightLogic.setChunkColumnSkyLight(chunkX, maxY, chunkZ);
        }
    }

    class GenerationStarter implements Runnable {

        private int travelDirection;
        private int playerX, playerY, playerZ;

        public void restart(int travelDirection, int playerX, int playerY, int playerZ) {
            shouldRestart = true;
            shouldFinish = false;
            this.travelDirection = travelDirection;
            this.playerX = playerX;
            this.playerY = playerY;
            this.playerZ = playerZ;
        }

        public void halt() {
            shouldExecute = false;
            shouldFinish = false;
            shouldRestart = false;
        }

        @Override
        public void run() {
            while (shouldExecute) {
                try {
                    synchronized (starterThread) {
                        if (!shouldRestart) starterThread.wait();
                        shouldRestart = false;
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                shouldFinish = true;

                executor.getQueue().clear();
                GameLogic.unloadChunks(playerX, playerY, playerZ);
                handleBlockChanges();
                submitTasks(playerX, playerY, playerZ, travelDirection);
            }
        }

        private void handleBlockChanges() {
            synchronized (blockChanges) {
                while (!blockChanges.isEmpty()) {

                    Vector4i blockChange = blockChanges.removeFirst();
                    int x = blockChange.x;
                    int y = blockChange.y;
                    int z = blockChange.z;
                    short previousBlock = (short) (blockChange.w >> 16 & 0xFFFF);
                    short block = (short) (blockChange.w & 0xFFFF);
                    if (Chunk.getBlockInWorld(x, y, z) != block) continue;

                    int blockLightLevel = Block.getBlockProperties(block) & LIGHT_EMITTING;
                    int previousBlockLightLevel = Block.getBlockProperties(previousBlock) & LIGHT_EMITTING;
                    boolean blockEmitsLight = blockLightLevel != 0;
                    boolean previousBlockEmitsLight = previousBlockLightLevel != 0;

                    if (blockEmitsLight && previousBlockEmitsLight) {
                        if (blockLightLevel > previousBlockLightLevel)
                            LightLogic.setBlockLight(x, y, z, blockLightLevel);
                        else if (previousBlockLightLevel > blockLightLevel) {
                            LightLogic.dePropagateBlockLight(x, y, z);
                            LightLogic.setBlockLight(x, y, z, blockLightLevel);
                        }
                    } else if (blockEmitsLight) {
                        if (Chunk.getBlockLightInWorld(x, y, z) > blockLightLevel) LightLogic.dePropagateBlockLight(x, y, z);
                        LightLogic.setBlockLight(x, y, z, blockLightLevel);
                    } else if (block == AIR) {
                        if (previousBlockEmitsLight) LightLogic.dePropagateBlockLight(x, y, z);
                        else LightLogic.setBlockLight(x, y, z, LightLogic.getMaxSurroundingBlockLight(x, y, z) - 1);
                    } else
                        LightLogic.dePropagateBlockLight(x, y, z);

                    if (block == AIR)
                        LightLogic.setSkyLight(x, y, z, LightLogic.getMaxSurroundingSkyLight(x, y, z) - 1);
                    else LightLogic.dePropagateSkyLight(x, y, z);
                }
            }
        }

        private void submitTasks(int playerX, int playerY, int playerZ, int travelDirection) {
            if (shouldFinish && !executor.isShutdown() && columnRequiresGeneration(playerX, playerY, playerZ))
                executor.submit(new Generator(playerX, playerY, playerZ));
            for (int ring = 1; ring <= RENDER_DISTANCE_XZ + 1 && shouldFinish; ring++) {

                for (int chunkX = -ring; chunkX < ring && shouldFinish && !executor.isShutdown(); chunkX++)
                    if (columnRequiresGeneration(chunkX + playerX, playerY, ring + playerZ))
                        executor.submit(new Generator(chunkX + playerX, playerY, ring + playerZ));

                for (int chunkZ = ring; chunkZ > -ring && shouldFinish && !executor.isShutdown(); chunkZ--)
                    if (columnRequiresGeneration(ring + playerX, playerY, chunkZ + playerZ))
                        executor.submit(new Generator(ring + playerX, playerY, chunkZ + playerZ));

                for (int chunkX = ring; chunkX > -ring && shouldFinish && !executor.isShutdown(); chunkX--)
                    if (columnRequiresGeneration(chunkX + playerX, playerY, -ring + playerZ))
                        executor.submit(new Generator(chunkX + playerX, playerY, -ring + playerZ));

                for (int chunkZ = -ring; chunkZ < ring && shouldFinish && !executor.isShutdown(); chunkZ++)
                    if (columnRequiresGeneration(-ring + playerX, playerY, chunkZ + playerZ))
                        executor.submit(new Generator(-ring + playerX, playerY, chunkZ + playerZ));

                if (ring == 1 && shouldFinish && !executor.isShutdown()) {
                    if (columnRequiresMeshing(playerX, playerY, playerZ))
                        executor.submit(new MeshHandler(playerX, playerY, playerZ, travelDirection));
                    continue;
                }
                int meshRing = ring - 1;
                for (int chunkX = -meshRing; chunkX < meshRing && shouldFinish && !executor.isShutdown(); chunkX++)
                    if (columnRequiresMeshing(chunkX + playerX, playerY, meshRing + playerZ))
                        executor.submit(new MeshHandler(chunkX + playerX, playerY, meshRing + playerZ, travelDirection));

                for (int chunkZ = meshRing; chunkZ > -meshRing && shouldFinish && !executor.isShutdown(); chunkZ--)
                    if (columnRequiresMeshing(meshRing + playerX, playerY, chunkZ + playerZ))
                        executor.submit(new MeshHandler(meshRing + playerX, playerY, chunkZ + playerZ, travelDirection));

                for (int chunkX = meshRing; chunkX > -meshRing && shouldFinish && !executor.isShutdown(); chunkX--)
                    if (columnRequiresMeshing(chunkX + playerX, playerY, -meshRing + playerZ))
                        executor.submit(new MeshHandler(chunkX + playerX, playerY, -meshRing + playerZ, travelDirection));

                for (int chunkZ = -meshRing; chunkZ < meshRing && shouldFinish && !executor.isShutdown(); chunkZ++)
                    if (columnRequiresMeshing(-meshRing + playerX, playerY, chunkZ + playerZ))
                        executor.submit(new MeshHandler(-meshRing + playerX, playerY, chunkZ + playerZ, travelDirection));
            }
        }

        private boolean columnRequiresGeneration(int chunkX, int playerY, int chunkZ) {
            for (int chunkY = playerY + RENDER_DISTANCE_Y + 1; chunkY >= playerY - RENDER_DISTANCE_Y - 1 && shouldFinish; chunkY--) {
                Chunk chunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
                if (chunk == null || !chunk.isGenerated()) return true;
            }
            return false;
        }

        private boolean columnRequiresMeshing(int chunkX, int playerY, int chunkZ) {
            for (int chunkY = playerY + RENDER_DISTANCE_Y; chunkY >= playerY - RENDER_DISTANCE_Y && shouldFinish; chunkY--) {
                Chunk chunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
                if (chunk == null || !chunk.isMeshed()) return true;
            }
            return false;
        }

        public void stop() {
            shouldFinish = false;
            shouldExecute = false;
        }
    }
}
