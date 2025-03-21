package terrascape.server;

import terrascape.dataStorage.Chunk;
import terrascape.utils.ArrayQueue;

import static terrascape.utils.Constants.*;
import static terrascape.utils.Settings.*;

public final class LightLogic {

    public static final byte UNKNOWN_LIGHT_LEVEL = -1;

    // BlockLight
    public static void setBlockLight(int x, int y, int z, int blockLight) {
        if (blockLight <= 0)
            return;
        ArrayQueue<LightInfo> toPlaceLights = new ArrayQueue<>(10);
        toPlaceLights.enqueue(new LightInfo(x, y, z, (byte) blockLight));
        setBlockLight(toPlaceLights);
    }

    public static void dePropagateBlockLight(int x, int y, int z) {
        ArrayQueue<LightInfo> toRePropagate = new ArrayQueue<>(10);
        ArrayQueue<LightInfo> toDePropagate = new ArrayQueue<>(10);
        toDePropagate.enqueue(new LightInfo(x, y, z, (byte) (Chunk.getBlockLightInWorld(x, y, z) + 1)));

        dePropagateBlockLight(toRePropagate, toDePropagate);

        setBlockLight(toRePropagate);
    }

    public static byte getMaxSurroundingBlockLight(int x, int y, int z) {
        byte max = 0;
        short centerBlock = Chunk.getBlockInWorld(x, y, z);

        byte toTest = Chunk.getBlockLightInWorld(x + 1, y, z);
        if (max < toTest && canLightTravel(Chunk.getBlockInWorld(x + 1, y, z), EAST, centerBlock, WEST)) max = toTest;
        toTest = Chunk.getBlockLightInWorld(x - 1, y, z);
        if (max < toTest && canLightTravel(Chunk.getBlockInWorld(x - 1, y, z), WEST, centerBlock, EAST)) max = toTest;

        toTest = Chunk.getBlockLightInWorld(x, y + 1, z);
        if (max < toTest && canLightTravel(Chunk.getBlockInWorld(x, y + 1, z), BOTTOM, centerBlock, TOP)) max = toTest;
        toTest = Chunk.getBlockLightInWorld(x, y - 1, z);
        if (max < toTest && canLightTravel(Chunk.getBlockInWorld(x, y - 1, z), TOP, centerBlock, BOTTOM)) max = toTest;

        toTest = Chunk.getBlockLightInWorld(x, y, z + 1);
        if (max < toTest && canLightTravel(Chunk.getBlockInWorld(x, y, z + 1), SOUTH, centerBlock, NORTH)) max = toTest;
        toTest = Chunk.getBlockLightInWorld(x, y, z - 1);
        if (max < toTest && canLightTravel(Chunk.getBlockInWorld(x, y, z - 1), NORTH, centerBlock, SOUTH)) max = toTest;
        return max;
    }

    private static void setBlockLight(ArrayQueue<LightInfo> toPlaceLights) {
        int ignoreChecksCounter = toPlaceLights.size();
        while (!toPlaceLights.isEmpty()) {
            LightInfo info = toPlaceLights.dequeue();
            if (info == null) continue;
            int x = info.x();
            int y = info.y();
            int z = info.z();
            int currentBlockLight = info.lightLevel();

            Chunk chunk = Chunk.getChunk(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS);
            if (chunk == null) continue;

            int index = (x & CHUNK_SIZE_MASK) << CHUNK_SIZE_BITS * 2 | (z & CHUNK_SIZE_MASK) << CHUNK_SIZE_BITS | (y & CHUNK_SIZE_MASK);

            if (chunk.getSaveBlockLight(index) >= currentBlockLight && ignoreChecksCounter <= 0) continue;

            chunk.storeSaveBlockLight(index, currentBlockLight);
            chunk.setMeshed(false);
            unMeshNextChunkIfNecessary(x & CHUNK_SIZE_MASK, y & CHUNK_SIZE_MASK, z & CHUNK_SIZE_MASK, chunk);

            byte nextBlockLight = (byte) (currentBlockLight - 1);
            if (nextBlockLight <= 0) continue;
            short currentBlock = chunk.getSaveBlock(index);

            short nextBlock = Chunk.getBlockInWorld(x + 1, y, z);
            if (Chunk.getBlockLightInWorld(x + 1, y, z) < nextBlockLight && canLightTravel(nextBlock, EAST, currentBlock, WEST))
                toPlaceLights.enqueue(new LightInfo(x + 1, y, z, nextBlockLight));
            nextBlock = Chunk.getBlockInWorld(x - 1, y, z);
            if (Chunk.getBlockLightInWorld(x - 1, y, z) < nextBlockLight && canLightTravel(nextBlock, WEST, currentBlock, EAST))
                toPlaceLights.enqueue(new LightInfo(x - 1, y, z, nextBlockLight));

            nextBlock = Chunk.getBlockInWorld(x, y + 1, z);
            if (Chunk.getBlockLightInWorld(x, y + 1, z) < nextBlockLight && canLightTravel(nextBlock, BOTTOM, currentBlock, TOP))
                toPlaceLights.enqueue(new LightInfo(x, y + 1, z, nextBlockLight));
            nextBlock = Chunk.getBlockInWorld(x, y - 1, z);
            if (Chunk.getBlockLightInWorld(x, y - 1, z) < nextBlockLight && canLightTravel(nextBlock, TOP, currentBlock, BOTTOM))
                toPlaceLights.enqueue(new LightInfo(x, y - 1, z, nextBlockLight));

            nextBlock = Chunk.getBlockInWorld(x, y, z + 1);
            if (Chunk.getBlockLightInWorld(x, y, z + 1) < nextBlockLight && canLightTravel(nextBlock, SOUTH, currentBlock, NORTH))
                toPlaceLights.enqueue(new LightInfo(x, y, z + 1, nextBlockLight));
            nextBlock = Chunk.getBlockInWorld(x, y, z - 1);
            if (Chunk.getBlockLightInWorld(x, y, z - 1) < nextBlockLight && canLightTravel(nextBlock, NORTH, currentBlock, SOUTH))
                toPlaceLights.enqueue(new LightInfo(x, y, z - 1, nextBlockLight));

            ignoreChecksCounter--;
        }
    }

    private static void dePropagateBlockLight(ArrayQueue<LightInfo> toRePropagate, ArrayQueue<LightInfo> toDePropagate) {
        boolean onFirstIteration = true;
        while (!toDePropagate.isEmpty()) {
            LightInfo info = toDePropagate.dequeue();
            if (info == null) continue;
            int x = info.x();
            int y = info.y();
            int z = info.z();
            int lastBlockLight = info.lightLevel();

            Chunk chunk = Chunk.getChunk(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS);
            if (chunk == null) continue;

            byte currentBlockLight = chunk.getSaveBlockLight(x & CHUNK_SIZE_MASK, y & CHUNK_SIZE_MASK, z & CHUNK_SIZE_MASK);
            if (currentBlockLight == 0) continue;

            if (currentBlockLight >= lastBlockLight) {
                if (currentBlockLight > 1) toRePropagate.enqueue(new LightInfo(x, y, z, currentBlockLight));
                continue;
            }

            int index = (x & CHUNK_SIZE_MASK) << CHUNK_SIZE_BITS * 2 | (z & CHUNK_SIZE_MASK) << CHUNK_SIZE_BITS | y & CHUNK_SIZE_MASK;
            chunk.removeBlockLight(index);
            chunk.setMeshed(false);
            unMeshNextChunkIfNecessary(x & CHUNK_SIZE_MASK, y & CHUNK_SIZE_MASK, z & CHUNK_SIZE_MASK, chunk);
            short currentBlock = onFirstIteration ? AIR : chunk.getSaveBlock(index);

            short nextBlock = Chunk.getBlockInWorld(x + 1, y, z);
            if (canLightTravel(nextBlock, EAST, currentBlock, WEST))
                toDePropagate.enqueue(new LightInfo(x + 1, y, z, currentBlockLight));
            nextBlock = Chunk.getBlockInWorld(x - 1, y, z);
            if (canLightTravel(nextBlock, WEST, currentBlock, EAST))
                toDePropagate.enqueue(new LightInfo(x - 1, y, z, currentBlockLight));

            nextBlock = Chunk.getBlockInWorld(x, y + 1, z);
            if (canLightTravel(nextBlock, BOTTOM, currentBlock, TOP))
                toDePropagate.enqueue(new LightInfo(x, y + 1, z, currentBlockLight));
            nextBlock = Chunk.getBlockInWorld(x, y - 1, z);
            if (canLightTravel(nextBlock, TOP, currentBlock, BOTTOM))
                toDePropagate.enqueue(new LightInfo(x, y - 1, z, currentBlockLight));

            nextBlock = Chunk.getBlockInWorld(x, y, z + 1);
            if (canLightTravel(nextBlock, SOUTH, currentBlock, NORTH))
                toDePropagate.enqueue(new LightInfo(x, y, z + 1, currentBlockLight));
            nextBlock = Chunk.getBlockInWorld(x, y, z - 1);
            if (canLightTravel(nextBlock, NORTH, currentBlock, SOUTH))
                toDePropagate.enqueue(new LightInfo(x, y, z - 1, currentBlockLight));

            onFirstIteration = false;
        }
    }

    // SkyLight
    public static void propagateChunkSkyLight(final int chunkX, final int chunkY, final int chunkZ) {
        ArrayQueue<LightInfo> toPlaceLights = new ArrayQueue<>(10);

        int x = chunkX << CHUNK_SIZE_BITS;
        int y = (chunkY << CHUNK_SIZE_BITS) + CHUNK_SIZE - 1;
        int z = chunkZ << CHUNK_SIZE_BITS;

        for (int totalY = y + (RENDERED_WORLD_HEIGHT << CHUNK_SIZE_BITS); totalY >= y; totalY -= CHUNK_SIZE) {
            for (int totalX = x, maxX = x + CHUNK_SIZE; totalX < maxX; totalX++)
                for (int totalZ = z, maxZ = z + CHUNK_SIZE; totalZ < maxZ; totalZ++) {
                    short block = Chunk.getBlockInWorld(totalX, totalY, totalZ);
                    short blockAbove = Chunk.getBlockInWorld(totalX, totalY + 1, totalZ);
                    if (!canLightTravel(block, TOP, blockAbove, BOTTOM)) continue;

                    byte skyLightAbove = Chunk.getSkyLightInWorld(totalX, totalY + 1, totalZ);
                    if (skyLightAbove == 0) continue;
                    byte skyLight = Chunk.getSkyLightInWorld(totalX, totalY, totalZ);
                    if (skyLightAbove == skyLight) continue;

                    toPlaceLights.enqueue(new LightInfo(totalX, totalY, totalZ, skyLightAbove));
                }

            setSkyLight(toPlaceLights);
        }
    }

    public static void setChunkColumnSkyLight(final int chunkX, int chunkY, final int chunkZ) {
        ArrayQueue<LightInfo> toPlaceLights = new ArrayQueue<>(10);
        int[] heightMap = Chunk.getHeightMap(chunkX, chunkZ).map;

        int x = chunkX << CHUNK_SIZE_BITS;
        int y = (chunkY << CHUNK_SIZE_BITS) + CHUNK_SIZE - 1;
        int z = chunkZ << CHUNK_SIZE_BITS;

        for (int totalY = y; totalY > y - (RENDERED_WORLD_HEIGHT << CHUNK_SIZE_BITS); totalY -= CHUNK_SIZE) {
            for (int totalX = x, maxX = x + CHUNK_SIZE; totalX < maxX; totalX++)
                for (int totalZ = z, maxZ = z + CHUNK_SIZE; totalZ < maxZ; totalZ++) {
                    if (totalY < heightMap[(totalX & CHUNK_SIZE_MASK) << CHUNK_SIZE_BITS | totalZ & CHUNK_SIZE_MASK])
                        continue;

                    if (Chunk.getSkyLightInWorld(totalX, totalY, totalZ) == MAX_SKY_LIGHT_VALUE) continue;

                    toPlaceLights.enqueue(new LightInfo(totalX, totalY, totalZ, MAX_SKY_LIGHT_VALUE));
                }

            setSkyLight(toPlaceLights);
        }
    }

    public static void computeSkyLight() {
        dePropagateSkyLight(toRePropagateSkyLight, toDePropagateSkyLight);
        setSkyLight(toRePropagateSkyLight);

        if (toDePropagateSkyLight.size() > 1000) toDePropagateSkyLight = new ArrayQueue<>(10);
        if (toRePropagateSkyLight.size() > 1000) toRePropagateSkyLight = new ArrayQueue<>(10);
    }

    public static void setSkyLight(int x, int y, int z, byte skyLight) {
        toRePropagateSkyLight.enqueue(new LightInfo(x, y, z, skyLight));
    }

    public static void dePropagateSkyLight(int x, int y, int z) {
        toDePropagateSkyLight.enqueue(new LightInfo(x, y, z, (byte) (Chunk.getSkyLightInWorld(x, y, z) + 1)));
    }

    public static byte getMaxSurroundingSkyLight(int x, int y, int z) {
        byte max = 0;
        short centerBlock = Chunk.getBlockInWorld(x, y, z);

        byte toTest = Chunk.getSkyLightInWorld(x + 1, y, z);
        if (max < toTest && canLightTravel(Chunk.getBlockInWorld(x + 1, y, z), EAST, centerBlock, WEST)) max = toTest;
        toTest = Chunk.getSkyLightInWorld(x - 1, y, z);
        if (max < toTest && canLightTravel(Chunk.getBlockInWorld(x - 1, y, z), WEST, centerBlock, EAST)) max = toTest;

        toTest = (byte) (Chunk.getSkyLightInWorld(x, y + 1, z) + 1);
        if (max < toTest && canLightTravel(Chunk.getBlockInWorld(x, y + 1, z), BOTTOM, centerBlock, TOP)) max = toTest;
        toTest = Chunk.getSkyLightInWorld(x, y - 1, z);
        if (max < toTest && canLightTravel(Chunk.getBlockInWorld(x, y - 1, z), TOP, centerBlock, BOTTOM)) max = toTest;

        toTest = Chunk.getSkyLightInWorld(x, y, z + 1);
        if (max < toTest && canLightTravel(Chunk.getBlockInWorld(x, y, z + 1), SOUTH, centerBlock, NORTH)) max = toTest;
        toTest = Chunk.getSkyLightInWorld(x, y, z - 1);
        if (max < toTest && canLightTravel(Chunk.getBlockInWorld(x, y, z - 1), NORTH, centerBlock, SOUTH)) max = toTest;

        return max;
    }

    private static void setSkyLight(ArrayQueue<LightInfo> toPlaceLights) {
        int ignoreChecksCounter = toPlaceLights.size();
        while (!toPlaceLights.isEmpty()) {
            LightInfo info = toPlaceLights.dequeue();
            if (info == null) continue;
            int x = info.x();
            int y = info.y();
            int z = info.z();
            byte currentSkyLight = info.lightLevel();
            if (currentSkyLight == UNKNOWN_LIGHT_LEVEL) {
                currentSkyLight = (byte) (getMaxSurroundingSkyLight(x, y, z) - 1);
                short currentBlock = Chunk.getBlockInWorld(x, y, z);
                if (Block.isLeaveType(currentBlock) || Block.isLiquidType(Block.getBlockType(currentBlock)))
                    currentSkyLight--;
            }

            if (currentSkyLight <= 0) continue;

            Chunk chunk = Chunk.getChunk(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS);
            if (chunk == null) continue;

            int index = (x & CHUNK_SIZE_MASK) << CHUNK_SIZE_BITS * 2 | (z & CHUNK_SIZE_MASK) << CHUNK_SIZE_BITS | y & CHUNK_SIZE_MASK;

            if (chunk.getSaveSkyLight(index) >= currentSkyLight && ignoreChecksCounter <= 0) continue;

            chunk.storeSaveSkyLight(index, currentSkyLight);
            chunk.setMeshed(false);
            unMeshNextChunkIfNecessary(x & CHUNK_SIZE_MASK, y & CHUNK_SIZE_MASK, z & CHUNK_SIZE_MASK, chunk);

            byte nextSkyLight = (byte) (currentSkyLight - 1);
            short currentBlock = chunk.getSaveBlock(index);

            short nextBlock = Chunk.getBlockInWorld(x + 1, y, z);
            if (Chunk.getSkyLightInWorld(x + 1, y, z) < nextSkyLight && canLightTravel(nextBlock, EAST, currentBlock, WEST))
                toPlaceLights.enqueue(new LightInfo(x + 1, y, z, nextSkyLight));
            nextBlock = Chunk.getBlockInWorld(x - 1, y, z);
            if (Chunk.getSkyLightInWorld(x - 1, y, z) < nextSkyLight && canLightTravel(nextBlock, WEST, currentBlock, EAST))
                toPlaceLights.enqueue(new LightInfo(x - 1, y, z, nextSkyLight));

            nextBlock = Chunk.getBlockInWorld(x, y + 1, z);
            if (Chunk.getSkyLightInWorld(x, y + 1, z) < nextSkyLight && canLightTravel(nextBlock, BOTTOM, currentBlock, TOP))
                toPlaceLights.enqueue(new LightInfo(x, y + 1, z, nextSkyLight));

            nextBlock = Chunk.getBlockInWorld(x, y - 1, z);
            if (Block.isLeaveType(nextBlock) || Block.isLiquidType(Block.getBlockType(nextBlock))) currentSkyLight--;
            if (Chunk.getSkyLightInWorld(x, y - 1, z) < currentSkyLight && canLightTravel(nextBlock, TOP, currentBlock, BOTTOM))
                toPlaceLights.enqueue(new LightInfo(x, y - 1, z, currentSkyLight));

            nextBlock = Chunk.getBlockInWorld(x, y, z + 1);
            if (Chunk.getSkyLightInWorld(x, y, z + 1) < nextSkyLight && canLightTravel(nextBlock, SOUTH, currentBlock, NORTH))
                toPlaceLights.enqueue(new LightInfo(x, y, z + 1, nextSkyLight));
            nextBlock = Chunk.getBlockInWorld(x, y, z - 1);
            if (Chunk.getSkyLightInWorld(x, y, z - 1) < nextSkyLight && canLightTravel(nextBlock, NORTH, currentBlock, SOUTH))
                toPlaceLights.enqueue(new LightInfo(x, y, z - 1, nextSkyLight));

            ignoreChecksCounter--;
        }
    }

    private static void dePropagateSkyLight(ArrayQueue<LightInfo> toRePropagate, ArrayQueue<LightInfo> toDePropagate) {
        boolean onFirstIteration = true;
        while (!toDePropagate.isEmpty()) {
            LightInfo info = toDePropagate.dequeue();
            if (info == null) continue;
            int x = info.x();
            int y = info.y();
            int z = info.z();
            int lastSkyLight = info.lightLevel();

            Chunk chunk = Chunk.getChunk(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS);
            if (chunk == null) continue;

            byte currentSkyLight = chunk.getSaveSkyLight(x & CHUNK_SIZE_MASK, y & CHUNK_SIZE_MASK, z & CHUNK_SIZE_MASK);
            if (currentSkyLight == 0) continue;

            if (currentSkyLight >= lastSkyLight) {
                if (currentSkyLight > 1) toRePropagate.enqueue(new LightInfo(x, y, z, UNKNOWN_LIGHT_LEVEL));
                continue;
            }

            int index = (x & CHUNK_SIZE_MASK) << CHUNK_SIZE_BITS * 2 | (z & CHUNK_SIZE_MASK) << CHUNK_SIZE_BITS | (y & CHUNK_SIZE_MASK);
            chunk.removeSkyLight(index);
            chunk.setMeshed(false);
            unMeshNextChunkIfNecessary(x & CHUNK_SIZE_MASK, y & CHUNK_SIZE_MASK, z & CHUNK_SIZE_MASK, chunk);
            short currentBlock = onFirstIteration ? AIR : chunk.getSaveBlock(index);

            short nextBlock = Chunk.getBlockInWorld(x + 1, y, z);
            if (canLightTravel(nextBlock, EAST, currentBlock, WEST))
                toDePropagate.enqueue(new LightInfo(x + 1, y, z, currentSkyLight));
            nextBlock = Chunk.getBlockInWorld(x - 1, y, z);
            if (canLightTravel(nextBlock, WEST, currentBlock, EAST))
                toDePropagate.enqueue(new LightInfo(x - 1, y, z, currentSkyLight));

            nextBlock = Chunk.getBlockInWorld(x, y + 1, z);
            if (canLightTravel(nextBlock, BOTTOM, currentBlock, TOP))
                toDePropagate.enqueue(new LightInfo(x, y + 1, z, currentSkyLight));
            nextBlock = Chunk.getBlockInWorld(x, y - 1, z);
            if (canLightTravel(nextBlock, TOP, currentBlock, BOTTOM))
                toDePropagate.enqueue(new LightInfo(x, y - 1, z, (byte) (currentSkyLight + 1)));

            nextBlock = Chunk.getBlockInWorld(x, y, z + 1);
            if (canLightTravel(nextBlock, SOUTH, currentBlock, NORTH))
                toDePropagate.enqueue(new LightInfo(x, y, z + 1, currentSkyLight));
            nextBlock = Chunk.getBlockInWorld(x, y, z - 1);
            if (canLightTravel(nextBlock, NORTH, currentBlock, SOUTH))
                toDePropagate.enqueue(new LightInfo(x, y, z - 1, currentSkyLight));

            onFirstIteration = false;
        }
    }

    // Util
    private static boolean canLightTravel(short destinationBlock, int enterSide, short originBlock, int exitSide) {
        int blockType;
        long occlusionData;
        boolean lightEmitting;

        occlusionData = Block.getBlockOcclusionData(originBlock, exitSide);
        lightEmitting = (Block.getBlockProperties(originBlock) & LIGHT_EMITTING) != 0;
        blockType = Block.getBlockType(originBlock);
        boolean canExit = lightEmitting || occlusionData != -1L || Block.isLiquidType(blockType) ||
                Block.isGlassType(originBlock) || Block.isLeaveType(originBlock);
        if (!canExit) return false;

        occlusionData = Block.getBlockOcclusionData(destinationBlock, enterSide);
        lightEmitting = (Block.getBlockProperties(destinationBlock) & LIGHT_EMITTING) != 0;
        blockType = Block.getBlockType(destinationBlock);
        return lightEmitting || occlusionData != -1L || Block.isLiquidType(blockType) ||
                Block.isGlassType(destinationBlock) || Block.isLeaveType(destinationBlock);
    }

    private static void unMeshNextChunkIfNecessary(int inChunkX, int inChunkY, int inChunkZ, Chunk chunk) {
        if (inChunkX == 0) {
            Chunk chunk1 = Chunk.getChunk(chunk.X - 1, chunk.Y, chunk.Z);
            if (chunk1 != null) chunk1.setMeshed(false);
        } else if (inChunkX == CHUNK_SIZE - 1) {
            Chunk chunk1 = Chunk.getChunk(chunk.X + 1, chunk.Y, chunk.Z);
            if (chunk1 != null) chunk1.setMeshed(false);
        }

        if (inChunkY == 0) {
            Chunk chunk1 = Chunk.getChunk(chunk.X, chunk.Y - 1, chunk.Z);
            if (chunk1 != null) chunk1.setMeshed(false);
        } else if (inChunkY == CHUNK_SIZE - 1) {
            Chunk chunk1 = Chunk.getChunk(chunk.X, chunk.Y + 1, chunk.Z);
            if (chunk1 != null) chunk1.setMeshed(false);
        }

        if (inChunkZ == 0) {
            Chunk chunk1 = Chunk.getChunk(chunk.X, chunk.Y, chunk.Z - 1);
            if (chunk1 != null) chunk1.setMeshed(false);
        } else if (inChunkZ == CHUNK_SIZE - 1) {
            Chunk chunk1 = Chunk.getChunk(chunk.X, chunk.Y, chunk.Z + 1);
            if (chunk1 != null) chunk1.setMeshed(false);
        }
    }

    private LightLogic() {
    }

    private static ArrayQueue<LightInfo> toDePropagateSkyLight = new ArrayQueue<>(10);
    private static ArrayQueue<LightInfo> toRePropagateSkyLight = new ArrayQueue<>(10);

    public record LightInfo(int x, int y, int z, byte lightLevel) {
    }
}
