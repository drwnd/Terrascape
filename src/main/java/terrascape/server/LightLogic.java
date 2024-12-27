package terrascape.server;

import terrascape.dataStorage.Chunk;
import terrascape.utils.ArrayQueue;
import org.joml.Vector4i;

import static terrascape.utils.Constants.*;
import static terrascape.utils.Settings.*;

public class LightLogic {

    public static void setBlockLight(int x, int y, int z, int blockLight) {
        if (blockLight <= 0)
            return;
        ArrayQueue<Vector4i> toPlaceLights = new ArrayQueue<>(10);
        toPlaceLights.enqueue(new Vector4i(x, y, z, blockLight));
        setBlockLight(toPlaceLights);
    }

    public static void setBlockLight(ArrayQueue<Vector4i> toPlaceLights) {
        int ignoreChecksCounter = toPlaceLights.size();
        while (!toPlaceLights.isEmpty()) {
            Vector4i toPlaceLight = toPlaceLights.dequeue();
            int x = toPlaceLight.x;
            int y = toPlaceLight.y;
            int z = toPlaceLight.z;
            int currentBlockLight = toPlaceLight.w;

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
                toPlaceLights.enqueue(new Vector4i(x + 1, y, z, nextBlockLight));
            nextBlock = Chunk.getBlockInWorld(x - 1, y, z);
            if (Chunk.getBlockLightInWorld(x - 1, y, z) < nextBlockLight && canLightTravel(nextBlock, WEST, currentBlock, EAST))
                toPlaceLights.enqueue(new Vector4i(x - 1, y, z, nextBlockLight));

            nextBlock = Chunk.getBlockInWorld(x, y + 1, z);
            if (Chunk.getBlockLightInWorld(x, y + 1, z) < nextBlockLight && canLightTravel(nextBlock, BOTTOM, currentBlock, TOP))
                toPlaceLights.enqueue(new Vector4i(x, y + 1, z, nextBlockLight));
            nextBlock = Chunk.getBlockInWorld(x, y - 1, z);
            if (Chunk.getBlockLightInWorld(x, y - 1, z) < nextBlockLight && canLightTravel(nextBlock, TOP, currentBlock, BOTTOM))
                toPlaceLights.enqueue(new Vector4i(x, y - 1, z, nextBlockLight));

            nextBlock = Chunk.getBlockInWorld(x, y, z + 1);
            if (Chunk.getBlockLightInWorld(x, y, z + 1) < nextBlockLight && canLightTravel(nextBlock, SOUTH, currentBlock, NORTH))
                toPlaceLights.enqueue(new Vector4i(x, y, z + 1, nextBlockLight));
            nextBlock = Chunk.getBlockInWorld(x, y, z - 1);
            if (Chunk.getBlockLightInWorld(x, y, z - 1) < nextBlockLight && canLightTravel(nextBlock, NORTH, currentBlock, SOUTH))
                toPlaceLights.enqueue(new Vector4i(x, y, z - 1, nextBlockLight));

            ignoreChecksCounter--;
        }
    }

    public static void dePropagateBlockLight(int x, int y, int z) {
        ArrayQueue<Vector4i> toRePropagate = new ArrayQueue<>(10);
        ArrayQueue<Vector4i> toDePropagate = new ArrayQueue<>(10);
        toDePropagate.enqueue(new Vector4i(x, y, z, Chunk.getBlockLightInWorld(x, y, z) + 1));

        dePropagateBlockLight(toRePropagate, toDePropagate);

        setBlockLight(toRePropagate);
    }

    public static void dePropagateBlockLight(ArrayQueue<Vector4i> toRePropagate, ArrayQueue<Vector4i> toDePropagate) {
        boolean onFirstIteration = true;
        while (!toDePropagate.isEmpty()) {
            Vector4i position = toDePropagate.dequeue();
            int x = position.x;
            int y = position.y;
            int z = position.z;
            int lastBlockLight = position.w;

            Chunk chunk = Chunk.getChunk(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS);
            if (chunk == null) continue;

            byte currentBlockLight = chunk.getSaveBlockLight(x & CHUNK_SIZE_MASK, y & CHUNK_SIZE_MASK, z & CHUNK_SIZE_MASK);
            if (currentBlockLight == 0) continue;

            if (currentBlockLight >= lastBlockLight) {
                if (currentBlockLight > 1) {
                    Vector4i nextPosition = new Vector4i(x, y, z, currentBlockLight);
                    if (notContainsToRePropagatePosition(toRePropagate, nextPosition))
                        toRePropagate.enqueue(nextPosition);
                }
                continue;
            }

            int index = (x & CHUNK_SIZE_MASK) << CHUNK_SIZE_BITS * 2 | (z & CHUNK_SIZE_MASK) << CHUNK_SIZE_BITS | y & CHUNK_SIZE_MASK;
            chunk.removeBlockLight(index);
            chunk.setMeshed(false);
            unMeshNextChunkIfNecessary(x & CHUNK_SIZE_MASK, y & CHUNK_SIZE_MASK, z & CHUNK_SIZE_MASK, chunk);
            short currentBlock = onFirstIteration ? AIR : chunk.getSaveBlock(index);

            short nextBlock = Chunk.getBlockInWorld(x + 1, y, z);
            if (canLightTravel(nextBlock, EAST, currentBlock, WEST))
                toDePropagate.enqueue(new Vector4i(x + 1, y, z, currentBlockLight));
            nextBlock = Chunk.getBlockInWorld(x - 1, y, z);
            if (canLightTravel(nextBlock, WEST, currentBlock, EAST))
                toDePropagate.enqueue(new Vector4i(x - 1, y, z, currentBlockLight));

            nextBlock = Chunk.getBlockInWorld(x, y + 1, z);
            if (canLightTravel(nextBlock, BOTTOM, currentBlock, TOP))
                toDePropagate.enqueue(new Vector4i(x, y + 1, z, currentBlockLight));
            nextBlock = Chunk.getBlockInWorld(x, y - 1, z);
            if (canLightTravel(nextBlock, TOP, currentBlock, BOTTOM))
                toDePropagate.enqueue(new Vector4i(x, y - 1, z, currentBlockLight));

            nextBlock = Chunk.getBlockInWorld(x, y, z + 1);
            if (canLightTravel(nextBlock, SOUTH, currentBlock, NORTH))
                toDePropagate.enqueue(new Vector4i(x, y, z + 1, currentBlockLight));
            nextBlock = Chunk.getBlockInWorld(x, y, z - 1);
            if (canLightTravel(nextBlock, NORTH, currentBlock, SOUTH))
                toDePropagate.enqueue(new Vector4i(x, y, z - 1, currentBlockLight));

            onFirstIteration = false;
        }
    }

    public static byte getMaxSurroundingBlockLight(int x, int y, int z) {
        byte max = 0;
        short currentBlock = Chunk.getBlockInWorld(x, y, z);

        byte toTest = Chunk.getBlockLightInWorld(x + 1, y, z);
        short nextBlock = Chunk.getBlockInWorld(x + 1, y, z);
        if (max < toTest && canLightTravel(nextBlock, EAST, currentBlock, WEST)) max = toTest;
        toTest = Chunk.getBlockLightInWorld(x - 1, y, z);
        nextBlock = Chunk.getBlockInWorld(x - 1, y, z);
        if (max < toTest && canLightTravel(nextBlock, WEST, currentBlock, EAST)) max = toTest;

        toTest = Chunk.getBlockLightInWorld(x, y + 1, z);
        nextBlock = Chunk.getBlockInWorld(x, y + 1, z);
        if (max < toTest && canLightTravel(nextBlock, BOTTOM, currentBlock, TOP)) max = toTest;
        toTest = Chunk.getBlockLightInWorld(x, y - 1, z);
        nextBlock = Chunk.getBlockInWorld(x, y - 1, z);
        if (max < toTest && canLightTravel(nextBlock, TOP, currentBlock, BOTTOM)) max = toTest;

        toTest = Chunk.getBlockLightInWorld(x, y, z + 1);
        nextBlock = Chunk.getBlockInWorld(x, y, z + 1);
        if (max < toTest && canLightTravel(nextBlock, SOUTH, currentBlock, NORTH)) max = toTest;
        toTest = Chunk.getBlockLightInWorld(x, y, z - 1);
        nextBlock = Chunk.getBlockInWorld(x, y, z - 1);
        if (max < toTest && canLightTravel(nextBlock, NORTH, currentBlock, SOUTH)) max = toTest;
        return max;
    }


    public static void propagateChunkSkyLight(final int chunkX, final int chunkY, final int chunkZ) {
        ArrayQueue<Vector4i> toPlaceLights = new ArrayQueue<>(10);

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

                    toPlaceLights.enqueue(new Vector4i(totalX, totalY, totalZ, skyLightAbove));
                }

            setSkyLight(toPlaceLights);
        }
    }

    public static void setChunkColumnSkyLight(final int chunkX, int chunkY, final int chunkZ) {
        ArrayQueue<Vector4i> toPlaceLights = new ArrayQueue<>(10);
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

                    toPlaceLights.enqueue(new Vector4i(totalX, totalY, totalZ, MAX_SKY_LIGHT_VALUE));
                }

            setSkyLight(toPlaceLights);
        }
    }

    public static void setSkyLight(int x, int y, int z, int skyLight) {
        if (skyLight <= 0)
            return;
        ArrayQueue<Vector4i> toPlaceLights = new ArrayQueue<>(10);
        toPlaceLights.enqueue(new Vector4i(x, y, z, skyLight));
        setSkyLight(toPlaceLights);
    }

    public static void setSkyLight(ArrayQueue<Vector4i> toPlaceLights) {
        int ignoreChecksCounter = toPlaceLights.size();
        while (!toPlaceLights.isEmpty()) {
            Vector4i toPlaceLight = toPlaceLights.dequeue();
            int x = toPlaceLight.x;
            int y = toPlaceLight.y;
            int z = toPlaceLight.z;
            int currentSkyLight = toPlaceLight.w;

            if (currentSkyLight <= 0)
                continue;

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
                toPlaceLights.enqueue(new Vector4i(x + 1, y, z, nextSkyLight));
            nextBlock = Chunk.getBlockInWorld(x - 1, y, z);
            if (Chunk.getSkyLightInWorld(x - 1, y, z) < nextSkyLight && canLightTravel(nextBlock, WEST, currentBlock, EAST))
                toPlaceLights.enqueue(new Vector4i(x - 1, y, z, nextSkyLight));

            nextBlock = Chunk.getBlockInWorld(x, y + 1, z);
            if (Chunk.getSkyLightInWorld(x, y + 1, z) < nextSkyLight && canLightTravel(nextBlock, BOTTOM, currentBlock, TOP))
                toPlaceLights.enqueue(new Vector4i(x, y + 1, z, nextSkyLight));

            nextBlock = Chunk.getBlockInWorld(x, y - 1, z);
            if (Block.isLeaveType(nextBlock) || Block.getBlockType(nextBlock) == LIQUID_TYPE)
                currentSkyLight--;
            if (Chunk.getSkyLightInWorld(x, y - 1, z) < currentSkyLight && canLightTravel(nextBlock, TOP, currentBlock, BOTTOM))
                toPlaceLights.enqueue(new Vector4i(x, y - 1, z, currentSkyLight));

            nextBlock = Chunk.getBlockInWorld(x, y, z + 1);
            if (Chunk.getSkyLightInWorld(x, y, z + 1) < nextSkyLight && canLightTravel(nextBlock, SOUTH, currentBlock, NORTH))
                toPlaceLights.enqueue(new Vector4i(x, y, z + 1, nextSkyLight));
            nextBlock = Chunk.getBlockInWorld(x, y, z - 1);
            if (Chunk.getSkyLightInWorld(x, y, z - 1) < nextSkyLight && canLightTravel(nextBlock, NORTH, currentBlock, SOUTH))
                toPlaceLights.enqueue(new Vector4i(x, y, z - 1, nextSkyLight));

            ignoreChecksCounter--;
        }
    }

    public static void dePropagateSkyLight(int x, int y, int z) {
        ArrayQueue<Vector4i> toRePropagate = new ArrayQueue<>(10);
        ArrayQueue<Vector4i> toDePropagate = new ArrayQueue<>(10);
        toDePropagate.enqueue(new Vector4i(x, y, z, Chunk.getSkyLightInWorld(x, y, z) + 1));

        dePropagateSkyLight(toRePropagate, toDePropagate);

        setSkyLight(toRePropagate);
    }

    public static void dePropagateSkyLight(ArrayQueue<Vector4i> toRePropagate, ArrayQueue<Vector4i> toDePropagate) {
        boolean onFirstIteration = true;
        while (!toDePropagate.isEmpty()) {
            Vector4i position = toDePropagate.dequeue();
            int x = position.x;
            int y = position.y;
            int z = position.z;
            int lastSkyLight = position.w;

            Chunk chunk = Chunk.getChunk(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS);
            if (chunk == null) continue;

            byte currentSkyLight = chunk.getSaveSkyLight(x & CHUNK_SIZE_MASK, y & CHUNK_SIZE_MASK, z & CHUNK_SIZE_MASK);
            if (currentSkyLight == 0) continue;

            if (currentSkyLight >= lastSkyLight) {
                if (currentSkyLight > 1) {
                    Vector4i nextPosition = new Vector4i(x, y, z, currentSkyLight);
                    if (notContainsToRePropagatePosition(toRePropagate, nextPosition))
                        toRePropagate.enqueue(nextPosition);
                }
                continue;
            }

            int index = (x & CHUNK_SIZE_MASK) << CHUNK_SIZE_BITS * 2 | (z & CHUNK_SIZE_MASK) << CHUNK_SIZE_BITS | (y & CHUNK_SIZE_MASK);
            chunk.removeSkyLight(index);
            chunk.setMeshed(false);
            unMeshNextChunkIfNecessary(x & CHUNK_SIZE_MASK, y & CHUNK_SIZE_MASK, z & CHUNK_SIZE_MASK, chunk);
            short currentBlock = onFirstIteration ? AIR : chunk.getSaveBlock(index);

            short nextBlock = Chunk.getBlockInWorld(x + 1, y, z);
            if (canLightTravel(nextBlock, EAST, currentBlock, WEST))
                toDePropagate.enqueue(new Vector4i(x + 1, y, z, currentSkyLight));
            nextBlock = Chunk.getBlockInWorld(x - 1, y, z);
            if (canLightTravel(nextBlock, WEST, currentBlock, EAST))
                toDePropagate.enqueue(new Vector4i(x - 1, y, z, currentSkyLight));

            nextBlock = Chunk.getBlockInWorld(x, y + 1, z);
            if (canLightTravel(nextBlock, BOTTOM, currentBlock, TOP))
                toDePropagate.enqueue(new Vector4i(x, y + 1, z, currentSkyLight));
            nextBlock = Chunk.getBlockInWorld(x, y - 1, z);
            if (canLightTravel(nextBlock, TOP, currentBlock, BOTTOM))
                toDePropagate.enqueue(new Vector4i(x, y - 1, z, currentSkyLight + 1));

            nextBlock = Chunk.getBlockInWorld(x, y, z + 1);
            if (canLightTravel(nextBlock, SOUTH, currentBlock, NORTH))
                toDePropagate.enqueue(new Vector4i(x, y, z + 1, currentSkyLight));
            nextBlock = Chunk.getBlockInWorld(x, y, z - 1);
            if (canLightTravel(nextBlock, NORTH, currentBlock, SOUTH))
                toDePropagate.enqueue(new Vector4i(x, y, z - 1, currentSkyLight));

            onFirstIteration = false;
        }
    }

    public static byte getMaxSurroundingSkyLight(int x, int y, int z) {
        byte max = 0;
        short currentBlock = Chunk.getBlockInWorld(x, y, z);

        byte toTest = Chunk.getSkyLightInWorld(x + 1, y, z);
        short nextBlock = Chunk.getBlockInWorld(x + 1, y, z);
        if (max < toTest && canLightTravel(nextBlock, EAST, currentBlock, WEST)) max = toTest;
        toTest = Chunk.getSkyLightInWorld(x - 1, y, z);
        nextBlock = Chunk.getBlockInWorld(x - 1, y, z);
        if (max < toTest && canLightTravel(nextBlock, WEST, currentBlock, EAST)) max = toTest;

        toTest = (byte) (Chunk.getSkyLightInWorld(x, y + 1, z) + 1);
        nextBlock = Chunk.getBlockInWorld(x, y + 1, z);
        if (max < toTest && canLightTravel(nextBlock, BOTTOM, currentBlock, TOP)) max = toTest;
        toTest = Chunk.getSkyLightInWorld(x, y - 1, z);
        nextBlock = Chunk.getBlockInWorld(x, y - 1, z);
        if (max < toTest && canLightTravel(nextBlock, TOP, currentBlock, BOTTOM)) max = toTest;

        toTest = Chunk.getSkyLightInWorld(x, y, z + 1);
        nextBlock = Chunk.getBlockInWorld(x, y, z + 1);
        if (max < toTest && canLightTravel(nextBlock, SOUTH, currentBlock, NORTH)) max = toTest;
        toTest = Chunk.getSkyLightInWorld(x, y, z - 1);
        nextBlock = Chunk.getBlockInWorld(x, y, z - 1);
        if (max < toTest && canLightTravel(nextBlock, NORTH, currentBlock, SOUTH)) max = toTest;
        return max;
    }


    private static boolean notContainsToRePropagatePosition(ArrayQueue<Vector4i> toRePropagate, Vector4i position) {
//        for (Object object : toRePropagate.getElements()) {
//            if (object == null) continue;
//            Vector4i vec = (Vector4i) object;
//
//            if (vec.x == position.x && vec.y == position.y && vec.z == position.z && vec.w >= position.w)
//                return false;
//        }
//        return true;
        return toRePropagate.notContainsToRePropagatePosition(position);
    }

    public static boolean canLightTravel(short destinationBlock, int enterSide, short originBlock, int exitSide) {
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
}
