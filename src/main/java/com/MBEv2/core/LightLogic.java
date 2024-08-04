package com.MBEv2.core;

import org.joml.Vector4i;

import java.util.ArrayList;
import java.util.LinkedList;

import static com.MBEv2.core.utils.Constants.*;

public class LightLogic {

    public static void setBlockLight(int x, int y, int z, int blockLight) {
        if (blockLight <= 0)
            return;
        LinkedList<Vector4i> toPlaceLights = new LinkedList<>();
        toPlaceLights.add(new Vector4i(x, y, z, blockLight));
        setBlockLight(toPlaceLights);
    }

    public static void setBlockLight(LinkedList<Vector4i> toPlaceLights) {
        boolean onFirstIteration = true;
        while (!toPlaceLights.isEmpty()) {
            Vector4i toPlaceLight = toPlaceLights.removeFirst();
            int x = toPlaceLight.x;
            int y = toPlaceLight.y;
            int z = toPlaceLight.z;
            int currentBlockLight = toPlaceLight.w;

            Chunk chunk = Chunk.getChunk(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS);
            if (chunk == null) continue;

            int index = (x & CHUNK_SIZE - 1) << CHUNK_SIZE_BITS * 2 | (z & CHUNK_SIZE - 1) << CHUNK_SIZE_BITS | (y & CHUNK_SIZE - 1);

            if (chunk.getSaveBlockLight(index) >= currentBlockLight && !onFirstIteration) continue;

            chunk.storeSaveBlockLight(index, currentBlockLight);
            chunk.setMeshed(false);
            unMeshNextChunkIfNecessary(x & CHUNK_SIZE - 1, y & CHUNK_SIZE - 1, z & CHUNK_SIZE - 1, chunk);
            chunk.setModified();

            byte nextBlockLight = (byte) (currentBlockLight - 1);
            if (nextBlockLight <= 0) continue;
            short currentBlock = chunk.getSaveBlock(index);

            short nextBlock = Chunk.getBlockInWorld(x + 1, y, z);
            if (Chunk.getBlockLightInWorld(x + 1, y, z) < nextBlockLight && canLightTravel(nextBlock, LEFT, currentBlock, RIGHT))
                toPlaceLights.add(new Vector4i(x + 1, y, z, nextBlockLight));
            nextBlock = Chunk.getBlockInWorld(x - 1, y, z);
            if (Chunk.getBlockLightInWorld(x - 1, y, z) < nextBlockLight && canLightTravel(nextBlock, RIGHT, currentBlock, LEFT))
                toPlaceLights.add(new Vector4i(x - 1, y, z, nextBlockLight));

            nextBlock = Chunk.getBlockInWorld(x, y + 1, z);
            if (Chunk.getBlockLightInWorld(x, y + 1, z) < nextBlockLight && canLightTravel(nextBlock, BOTTOM, currentBlock, TOP))
                toPlaceLights.add(new Vector4i(x, y + 1, z, nextBlockLight));
            nextBlock = Chunk.getBlockInWorld(x, y - 1, z);
            if (Chunk.getBlockLightInWorld(x, y - 1, z) < nextBlockLight && canLightTravel(nextBlock, TOP, currentBlock, BOTTOM))
                toPlaceLights.add(new Vector4i(x, y - 1, z, nextBlockLight));

            nextBlock = Chunk.getBlockInWorld(x, y, z + 1);
            if (Chunk.getBlockLightInWorld(x, y, z + 1) < nextBlockLight && canLightTravel(nextBlock, BACK, currentBlock, FRONT))
                toPlaceLights.add(new Vector4i(x, y, z + 1, nextBlockLight));
            nextBlock = Chunk.getBlockInWorld(x, y, z - 1);
            if (Chunk.getBlockLightInWorld(x, y, z - 1) < nextBlockLight && canLightTravel(nextBlock, FRONT, currentBlock, BACK))
                toPlaceLights.add(new Vector4i(x, y, z - 1, nextBlockLight));

            onFirstIteration = false;
        }
    }

    public static void dePropagateBlockLight(int x, int y, int z) {
        ArrayList<Vector4i> toRePropagate = new ArrayList<>();
        LinkedList<Vector4i> toDePropagate = new LinkedList<>();
        toDePropagate.add(new Vector4i(x, y, z, Chunk.getBlockLightInWorld(x, y, z) + 1));

        dePropagateBlockLight(toRePropagate, toDePropagate);

        for (Vector4i vec : toRePropagate)
            setBlockLight(vec.x, vec.y, vec.z, vec.w);
    }

    public static void dePropagateBlockLight(ArrayList<Vector4i> toRePropagate, LinkedList<Vector4i> toDePropagate) {
        boolean onFirstIteration = true;
        while (!toDePropagate.isEmpty()) {
            Vector4i position = toDePropagate.removeFirst();
            int x = position.x;
            int y = position.y;
            int z = position.z;
            int lastBlockLight = position.w;

            Chunk chunk = Chunk.getChunk(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS);
            if (chunk == null) continue;

            byte currentBlockLight = chunk.getSaveBlockLight(x & CHUNK_SIZE - 1, y & CHUNK_SIZE - 1, z & CHUNK_SIZE - 1);
            if (currentBlockLight == 0) continue;

            if (currentBlockLight >= lastBlockLight) {
                Vector4i nextPosition = new Vector4i(x, y, z, currentBlockLight);
                if (notContainsToRePropagatePosition(toRePropagate, nextPosition))
                    toRePropagate.add(nextPosition);
                continue;
            }

            int index = (x & CHUNK_SIZE - 1) << CHUNK_SIZE_BITS * 2 | (z & CHUNK_SIZE - 1) << CHUNK_SIZE_BITS | (y & CHUNK_SIZE - 1);
            chunk.removeBlockLight(index);
            chunk.setMeshed(false);
            unMeshNextChunkIfNecessary(x & CHUNK_SIZE - 1, y & CHUNK_SIZE - 1, z & CHUNK_SIZE - 1, chunk);
            chunk.setModified();
            short currentBlock = onFirstIteration ? AIR : chunk.getSaveBlock(index);

            short nextBlock = Chunk.getBlockInWorld(x + 1, y, z);
            if (canLightTravel(nextBlock, LEFT, currentBlock, RIGHT))
                toDePropagate.add(new Vector4i(x + 1, y, z, currentBlockLight));
            nextBlock = Chunk.getBlockInWorld(x - 1, y, z);
            if (canLightTravel(nextBlock, RIGHT, currentBlock, LEFT))
                toDePropagate.add(new Vector4i(x - 1, y, z, currentBlockLight));

            nextBlock = Chunk.getBlockInWorld(x, y + 1, z);
            if (canLightTravel(nextBlock, BOTTOM, currentBlock, TOP))
                toDePropagate.add(new Vector4i(x, y + 1, z, currentBlockLight));
            nextBlock = Chunk.getBlockInWorld(x, y - 1, z);
            if (canLightTravel(nextBlock, TOP, currentBlock, BOTTOM))
                toDePropagate.add(new Vector4i(x, y - 1, z, currentBlockLight));

            nextBlock = Chunk.getBlockInWorld(x, y, z + 1);
            if (canLightTravel(nextBlock, BACK, currentBlock, FRONT))
                toDePropagate.add(new Vector4i(x, y, z + 1, currentBlockLight));
            nextBlock = Chunk.getBlockInWorld(x, y, z - 1);
            if (canLightTravel(nextBlock, FRONT, currentBlock, BACK))
                toDePropagate.add(new Vector4i(x, y, z - 1, currentBlockLight));

            onFirstIteration = false;
        }
    }

    public static byte getMaxSurroundingBlockLight(int x, int y, int z) {
        byte max = 0;
        short currentBlock = Chunk.getBlockInWorld(x, y, z);

        byte toTest = Chunk.getBlockLightInWorld(x + 1, y, z);
        short nextBlock = Chunk.getBlockInWorld(x + 1, y, z);
        if (max < toTest && canLightTravel(nextBlock, LEFT, currentBlock, RIGHT)) max = toTest;
        toTest = Chunk.getBlockLightInWorld(x - 1, y, z);
        nextBlock = Chunk.getBlockInWorld(x - 1, y, z);
        if (max < toTest && canLightTravel(nextBlock, RIGHT, currentBlock, LEFT)) max = toTest;

        toTest = Chunk.getBlockLightInWorld(x, y + 1, z);
        nextBlock = Chunk.getBlockInWorld(x, y + 1, z);
        if (max < toTest && canLightTravel(nextBlock, BOTTOM, currentBlock, TOP)) max = toTest;
        toTest = Chunk.getBlockLightInWorld(x, y - 1, z);
        nextBlock = Chunk.getBlockInWorld(x, y - 1, z);
        if (max < toTest && canLightTravel(nextBlock, TOP, currentBlock, BOTTOM)) max = toTest;

        toTest = Chunk.getBlockLightInWorld(x, y, z + 1);
        nextBlock = Chunk.getBlockInWorld(x, y, z + 1);
        if (max < toTest && canLightTravel(nextBlock, BACK, currentBlock, FRONT)) max = toTest;
        toTest = Chunk.getBlockLightInWorld(x, y, z - 1);
        nextBlock = Chunk.getBlockInWorld(x, y, z - 1);
        if (max < toTest && canLightTravel(nextBlock, FRONT, currentBlock, BACK)) max = toTest;
        return max;
    }


    public static void propagateChunkSkyLight(final int x, final int y, final int z) {
        LinkedList<Vector4i> toPlaceLights = new LinkedList<>();

        for (int totalX = x, maxX = x + CHUNK_SIZE; totalX < maxX; totalX++)
            for (int totalZ = z, maxZ = z + CHUNK_SIZE; totalZ < maxZ; totalZ++) {
                short block = Chunk.getBlockInWorld(totalX, y, totalZ);
                short blockAbove = Chunk.getBlockInWorld(totalX, y + 1, totalZ);
                if (!canLightTravel(block, TOP, blockAbove, BOTTOM))
                    continue;

                toPlaceLights.add(new Vector4i(totalX, y, totalZ, Chunk.getSkyLightInWorld(totalX, y + 1, totalZ)));
            }

        setSkyLight(toPlaceLights);
    }

    public static void setChunkColumnSkyLight(final int x, final int y, final int z) {
        LinkedList<Vector4i> toPlaceLights = new LinkedList<>();

        for (int totalX = x, maxX = x + CHUNK_SIZE; totalX < maxX; totalX++)
            for (int totalZ = z, maxZ = z + CHUNK_SIZE; totalZ < maxZ; totalZ++) {
                if (Chunk.getSkyLightInWorld(totalX, y, totalZ) == MAX_SKY_LIGHT_VALUE)
                    continue;

                toPlaceLights.add(new Vector4i(totalX, y, totalZ, MAX_SKY_LIGHT_VALUE));
            }

        setSkyLight(toPlaceLights);
    }

    public static void setSkyLight(int x, int y, int z, int skyLight) {
        if (skyLight <= 0)
            return;
        LinkedList<Vector4i> toPlaceLights = new LinkedList<>();
        toPlaceLights.add(new Vector4i(x, y, z, skyLight));
        setSkyLight(toPlaceLights);
    }

    public static void setSkyLight(LinkedList<Vector4i> toPlaceLights) {
        boolean onFirstIteration = toPlaceLights.size() == 1;
        while (!toPlaceLights.isEmpty()) {
            Vector4i toPlaceLight = toPlaceLights.removeFirst();
            int x = toPlaceLight.x;
            int y = toPlaceLight.y;
            int z = toPlaceLight.z;
            int currentSkyLight = toPlaceLight.w;

            if (currentSkyLight <= 0)
                continue;

            Chunk chunk = Chunk.getChunk(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS);
            if (chunk == null) continue;

            int index = (x & CHUNK_SIZE - 1) << CHUNK_SIZE_BITS * 2 | (z & CHUNK_SIZE - 1) << CHUNK_SIZE_BITS | (y & CHUNK_SIZE - 1);

            if (chunk.getSaveSkyLight(index) >= currentSkyLight && !onFirstIteration) continue;

            chunk.storeSaveSkyLight(index, currentSkyLight);
            chunk.setMeshed(false);
            unMeshNextChunkIfNecessary(x & CHUNK_SIZE - 1, y & CHUNK_SIZE - 1, z & CHUNK_SIZE - 1, chunk);

            byte nextSkyLight = (byte) (currentSkyLight - 1);
            short currentBlock = chunk.getSaveBlock(index);

            short nextBlock = Chunk.getBlockInWorld(x + 1, y, z);
            if (Chunk.getSkyLightInWorld(x + 1, y, z) < nextSkyLight && canLightTravel(nextBlock, LEFT, currentBlock, RIGHT))
                toPlaceLights.add(new Vector4i(x + 1, y, z, nextSkyLight));
            nextBlock = Chunk.getBlockInWorld(x - 1, y, z);
            if (Chunk.getSkyLightInWorld(x - 1, y, z) < nextSkyLight && canLightTravel(nextBlock, RIGHT, currentBlock, LEFT))
                toPlaceLights.add(new Vector4i(x - 1, y, z, nextSkyLight));

            nextBlock = Chunk.getBlockInWorld(x, y + 1, z);
            if (Chunk.getSkyLightInWorld(x, y + 1, z) < nextSkyLight && canLightTravel(nextBlock, BOTTOM, currentBlock, TOP))
                toPlaceLights.add(new Vector4i(x, y + 1, z, nextSkyLight));

            nextBlock = Chunk.getBlockInWorld(x, y - 1, z);
            if (Block.isLeaveType(nextBlock) || Block.getBlockType(nextBlock) == WATER_TYPE)
                currentSkyLight--;
            if (Chunk.getSkyLightInWorld(x, y - 1, z) < currentSkyLight && canLightTravel(nextBlock, TOP, currentBlock, BOTTOM))
                toPlaceLights.add(new Vector4i(x, y - 1, z, currentSkyLight));

            nextBlock = Chunk.getBlockInWorld(x, y, z + 1);
            if (Chunk.getSkyLightInWorld(x, y, z + 1) < nextSkyLight && canLightTravel(nextBlock, BACK, currentBlock, FRONT))
                toPlaceLights.add(new Vector4i(x, y, z + 1, nextSkyLight));
            nextBlock = Chunk.getBlockInWorld(x, y, z - 1);
            if (Chunk.getSkyLightInWorld(x, y, z - 1) < nextSkyLight && canLightTravel(nextBlock, FRONT, currentBlock, BACK))
                toPlaceLights.add(new Vector4i(x, y, z - 1, nextSkyLight));

            onFirstIteration = false;
        }
    }

    public static void dePropagateSkyLight(int x, int y, int z) {
        ArrayList<Vector4i> toRePropagate = new ArrayList<>();
        LinkedList<Vector4i> toDePropagate = new LinkedList<>();
        toDePropagate.add(new Vector4i(x, y, z, Chunk.getSkyLightInWorld(x, y, z) + 1));

        dePropagateSkyLight(toRePropagate, toDePropagate);

        for (Vector4i vec : toRePropagate)
            setSkyLight(vec.x, vec.y, vec.z, vec.w);
    }

    public static void dePropagateSkyLight(ArrayList<Vector4i> toRePropagate, LinkedList<Vector4i> toDePropagate) {
        boolean onFirstIteration = true;
        while (!toDePropagate.isEmpty()) {
            Vector4i position = toDePropagate.removeFirst();
            int x = position.x;
            int y = position.y;
            int z = position.z;
            int lastSkyLight = position.w;

            Chunk chunk = Chunk.getChunk(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS);
            if (chunk == null) continue;

            byte currentSkyLight = chunk.getSaveSkyLight(x & CHUNK_SIZE - 1, y & CHUNK_SIZE - 1, z & CHUNK_SIZE - 1);
            if (currentSkyLight == 0) continue;

            if (currentSkyLight >= lastSkyLight) {
                Vector4i nextPosition = new Vector4i(x, y, z, currentSkyLight);
                if (notContainsToRePropagatePosition(toRePropagate, nextPosition))
                    toRePropagate.add(nextPosition);
                continue;
            }

            int index = (x & CHUNK_SIZE - 1) << CHUNK_SIZE_BITS * 2 | (z & CHUNK_SIZE - 1) << CHUNK_SIZE_BITS | (y & CHUNK_SIZE - 1);
            chunk.removeSkyLight(index);
            chunk.setMeshed(false);
            unMeshNextChunkIfNecessary(x & CHUNK_SIZE - 1, y & CHUNK_SIZE - 1, z & CHUNK_SIZE - 1, chunk);
            short currentBlock = onFirstIteration ? AIR : chunk.getSaveBlock(index);

            short nextBlock = Chunk.getBlockInWorld(x + 1, y, z);
            if (canLightTravel(nextBlock, LEFT, currentBlock, RIGHT))
                toDePropagate.add(new Vector4i(x + 1, y, z, currentSkyLight));
            nextBlock = Chunk.getBlockInWorld(x - 1, y, z);
            if (canLightTravel(nextBlock, RIGHT, currentBlock, LEFT))
                toDePropagate.add(new Vector4i(x - 1, y, z, currentSkyLight));

            nextBlock = Chunk.getBlockInWorld(x, y + 1, z);
            if (canLightTravel(nextBlock, BOTTOM, currentBlock, TOP))
                toDePropagate.add(new Vector4i(x, y + 1, z, currentSkyLight));
            nextBlock = Chunk.getBlockInWorld(x, y - 1, z);
            if (canLightTravel(nextBlock, TOP, currentBlock, BOTTOM))
                toDePropagate.add(new Vector4i(x, y - 1, z, currentSkyLight + 1));

            nextBlock = Chunk.getBlockInWorld(x, y, z + 1);
            if (canLightTravel(nextBlock, BACK, currentBlock, FRONT))
                toDePropagate.add(new Vector4i(x, y, z + 1, currentSkyLight));
            nextBlock = Chunk.getBlockInWorld(x, y, z - 1);
            if (canLightTravel(nextBlock, FRONT, currentBlock, BACK))
                toDePropagate.add(new Vector4i(x, y, z - 1, currentSkyLight));

            onFirstIteration = false;
        }
    }

    public static byte getMaxSurroundingSkyLight(int x, int y, int z) {
        byte max = 0;
        short currentBlock = Chunk.getBlockInWorld(x, y, z);

        byte toTest = Chunk.getSkyLightInWorld(x + 1, y, z);
        short nextBlock = Chunk.getBlockInWorld(x + 1, y, z);
        if (max < toTest && canLightTravel(nextBlock, LEFT, currentBlock, RIGHT)) max = toTest;
        toTest = Chunk.getSkyLightInWorld(x - 1, y, z);
        nextBlock = Chunk.getBlockInWorld(x - 1, y, z);
        if (max < toTest && canLightTravel(nextBlock, RIGHT, currentBlock, LEFT)) max = toTest;

        toTest = (byte) (Chunk.getSkyLightInWorld(x, y + 1, z) + 1);
        nextBlock = Chunk.getBlockInWorld(x, y + 1, z);
        if (max < toTest && canLightTravel(nextBlock, BOTTOM, currentBlock, TOP)) max = toTest;
        toTest = Chunk.getSkyLightInWorld(x, y - 1, z);
        nextBlock = Chunk.getBlockInWorld(x, y - 1, z);
        if (max < toTest && canLightTravel(nextBlock, TOP, currentBlock, BOTTOM)) max = toTest;

        toTest = Chunk.getSkyLightInWorld(x, y, z + 1);
        nextBlock = Chunk.getBlockInWorld(x, y, z + 1);
        if (max < toTest && canLightTravel(nextBlock, BACK, currentBlock, FRONT)) max = toTest;
        toTest = Chunk.getSkyLightInWorld(x, y, z - 1);
        nextBlock = Chunk.getBlockInWorld(x, y, z - 1);
        if (max < toTest && canLightTravel(nextBlock, FRONT, currentBlock, BACK)) max = toTest;
        return max;
    }


    private static boolean notContainsToRePropagatePosition(ArrayList<Vector4i> toRePropagate, Vector4i position) {
        for (Vector4i vec2 : toRePropagate)
            if (position.equals(vec2.x, vec2.y, vec2.z, vec2.w))
                return false;
        return true;
    }

    public static boolean canLightTravel(short destinationBlock, int enterSide, short originBlock, int exitSide) {
        int originBlockType = Block.getBlockType(originBlock);
        int originBlockProperties = Block.getBlockProperties(originBlock);
        boolean canExit = Block.getBlockTypeOcclusionData(originBlock, exitSide) == 0 || (originBlockProperties & LIGHT_EMITTING_MASK) != 0 || originBlockType == WATER_TYPE ||
                Block.isGlassType(originBlock) || Block.isLeaveType(originBlock);
        if (!canExit) return false;

        int destinationBlockType = Block.getBlockType(destinationBlock);
        int destinationBlockProperties = Block.getBlockProperties(destinationBlock);
        return Block.getBlockTypeOcclusionData(destinationBlock, enterSide) == 0 || (destinationBlockProperties & LIGHT_EMITTING_MASK) != 0 || destinationBlockType == WATER_TYPE ||
                Block.isGlassType(destinationBlock) || Block.isLeaveType(destinationBlock);
    }

    private static void unMeshNextChunkIfNecessary(int x, int y, int z, Chunk chunk) {
        if (x == 0) unMeshChunk(chunk.getX() - 1, chunk.getY(), chunk.getZ());
        else if (x == CHUNK_SIZE - 1) unMeshChunk(chunk.getX() + 1, chunk.getY(), chunk.getZ());

        if (y == 0) unMeshChunk(chunk.getX(), chunk.getY() - 1, chunk.getZ());
        else if (y == CHUNK_SIZE - 1) unMeshChunk(chunk.getX(), chunk.getY() + 1, chunk.getZ());

        if (z == 0) unMeshChunk(chunk.getX(), chunk.getY(), chunk.getZ() - 1);
        else if (z == CHUNK_SIZE - 1) unMeshChunk(chunk.getX(), chunk.getY(), chunk.getZ() + 1);
    }

    private static void unMeshChunk(int chunkX, int chunkY, int ChunkZ) {
        Chunk chunk = Chunk.getChunk(chunkX, chunkY, ChunkZ);
        if (chunk != null)
            chunk.setMeshed(false);
    }
}
