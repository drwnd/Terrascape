package com.MBEv2.core;

import com.MBEv2.core.entity.entities.Entity;
import com.MBEv2.core.entity.Model;
import com.MBEv2.test.GameLogic;
import org.joml.Vector3i;

import java.util.*;

import static com.MBEv2.core.utils.Constants.*;

public class Chunk {

    private static Chunk[] world;
    private static short[] occlusionCullingData;
    private static HeightMap[] heightMaps;
    private static final HashMap<Long, ArrayList<Integer>> toGenerateBlocks = new HashMap<>();

    private short[] blocks;
    private byte[] light;
    @SuppressWarnings("unchecked")
    private final LinkedList<Entity>[] entityClusters = new LinkedList[64];

    private int[][] vertices = new int[6][0];
    private int[] waterVertices = new int[0];
    private int[] foliageVertices = new int[0];

    public final int X, Y, Z;
    public final long id;
    private final Vector3i worldCoordinate;
    private int index;

    private boolean isMeshed = false;
    private boolean isGenerated = false;
    private boolean isModified = false;
    private boolean hasPropagatedBlockLight = false;
    private boolean saved = false;

    private final Model[] model;
    private Model foliageModel;
    private Model waterModel;

//    private short occlusionCullingData;

    public Chunk(int x, int y, int z) {
        this.X = x;
        this.Y = y;
        this.Z = z;
        worldCoordinate = new Vector3i(X << CHUNK_SIZE_BITS, Y << CHUNK_SIZE_BITS, Z << CHUNK_SIZE_BITS);

        blocks = new short[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];
        light = new byte[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];

        id = GameLogic.getChunkId(X, Y, Z);
        index = GameLogic.getChunkIndex(X, Y, Z);
        model = new Model[6];

        for (int i = 0; i < entityClusters.length; i++) entityClusters[i] = new LinkedList<>();
    }

    public Chunk(int x, int y, int z, short[] blocks) {
        this.X = x;
        this.Y = y;
        this.Z = z;
        worldCoordinate = new Vector3i(X << CHUNK_SIZE_BITS, Y << CHUNK_SIZE_BITS, Z << CHUNK_SIZE_BITS);

        this.blocks = blocks;
        light = new byte[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];

        id = GameLogic.getChunkId(X, Y, Z);
        index = GameLogic.getChunkIndex(X, Y, Z);
        model = new Model[6];

        for (int i = 0; i < entityClusters.length; i++) entityClusters[i] = new LinkedList<>();
    }

    public void optimizeBlockStorage() {
        short firstBlock = blocks[0];
        for (short block : blocks)
            if (block != firstBlock) return;

        blocks = new short[]{firstBlock};
//        System.out.println("Optimized block Storage! Block: " + firstBlock);
    }

    public void optimizeLightStorage() {
        byte firstLight = light[0];
        for (byte light : light)
            if (light != firstLight) return;

        light = new byte[]{firstLight};
//        System.out.println("Optimized light Storage! + BlockLight: " + getSaveBlockLight(0) + " SkyLight: " + getSaveSkyLight(0));
    }

    public short setOcclusionCullingSidePair(int side1, int side2, short occlusionCullingData) {
        if (side1 == side2) return occlusionCullingData;
        int largerSide = Math.max(side1, side2);
        int smallerSide = Math.min(side1, side2);

        occlusionCullingData = (short) (occlusionCullingData | 1 << OCCLUSION_CULLING_LARGER_SIDE_OFFSETS[largerSide - 1] + smallerSide);
        return occlusionCullingData;
    }

    public static boolean readOcclusionCullingSidePair(int side1, int side2, short occlusionCullingData) {
        if (side1 == side2) return false;
        int largerSide = Math.max(side1, side2);
        int smallerSide = Math.min(side1, side2);

        return (occlusionCullingData & 1 << OCCLUSION_CULLING_LARGER_SIDE_OFFSETS[largerSide - 1] + smallerSide) != 0;
    }

    public void generateOcclusionCullingData() {
        short occlusionCullingData = 0;

        int[] visitedBlocks = new int[CHUNK_SIZE * CHUNK_SIZE];
        ArrayList<Integer> toVisitBlockIndexes = new ArrayList<>();

        if (blocks.length == 1) {
            if (Block.getBlockType(blocks[0]) != FULL_BLOCK) occlusionCullingData = 0x7FFF;
            for (byte light : light)
                if (light != 0) {
                    Chunk.occlusionCullingData[index] = occlusionCullingData;
                    return;
                }
            Chunk.occlusionCullingData[index] = (short) (occlusionCullingData | (short) 0x8000);
            return;
        }

        for (int blockIndex = 0; blockIndex < blocks.length; blockIndex++) {
            if ((visitedBlocks[blockIndex >> CHUNK_SIZE_BITS] & (1 << (blockIndex & CHUNK_SIZE_MASK))) != 0) continue;

            toVisitBlockIndexes.add(blockIndex);
            byte visitedSides = 0;

            while (!toVisitBlockIndexes.isEmpty()) {
                int floodFillBlockIndex = toVisitBlockIndexes.removeLast();

                if ((visitedBlocks[floodFillBlockIndex >> CHUNK_SIZE_BITS] & (1 << (floodFillBlockIndex & CHUNK_SIZE_MASK))) != 0)
                    continue;
                visitedBlocks[floodFillBlockIndex >> CHUNK_SIZE_BITS] |= 1 << (floodFillBlockIndex & CHUNK_SIZE_MASK);

                if (Block.getBlockType(blocks[floodFillBlockIndex]) == FULL_BLOCK) continue;

                int nextBlockIndex = floodFillBlockIndex - (1 << CHUNK_SIZE_BITS * 2);
                if (floodFillBlockIndex >> CHUNK_SIZE_BITS * 2 == 0) visitedSides = (byte) (visitedSides | 1 << LEFT);
                else if ((visitedBlocks[nextBlockIndex >> CHUNK_SIZE_BITS] & (1 << (nextBlockIndex & CHUNK_SIZE_MASK))) == 0)
                    toVisitBlockIndexes.add(nextBlockIndex);

                nextBlockIndex = floodFillBlockIndex + (1 << CHUNK_SIZE_BITS * 2);
                if (floodFillBlockIndex >> CHUNK_SIZE_BITS * 2 == CHUNK_SIZE - 1)
                    visitedSides = (byte) (visitedSides | 1 << RIGHT);
                else if ((visitedBlocks[nextBlockIndex >> CHUNK_SIZE_BITS] & (1 << (nextBlockIndex & CHUNK_SIZE_MASK))) == 0)
                    toVisitBlockIndexes.add(nextBlockIndex);

                nextBlockIndex = floodFillBlockIndex - (1 << CHUNK_SIZE_BITS);
                if ((floodFillBlockIndex >> CHUNK_SIZE_BITS & CHUNK_SIZE_MASK) == 0)
                    visitedSides = (byte) (visitedSides | 1 << BACK);
                else if ((visitedBlocks[nextBlockIndex >> CHUNK_SIZE_BITS] & (1 << (nextBlockIndex & CHUNK_SIZE_MASK))) == 0)
                    toVisitBlockIndexes.add(nextBlockIndex);

                nextBlockIndex = (floodFillBlockIndex) + (1 << CHUNK_SIZE_BITS);
                if ((floodFillBlockIndex >> CHUNK_SIZE_BITS & CHUNK_SIZE_MASK) == CHUNK_SIZE - 1)
                    visitedSides = (byte) (visitedSides | 1 << FRONT);
                else if ((visitedBlocks[nextBlockIndex >> CHUNK_SIZE_BITS] & (1 << (nextBlockIndex & CHUNK_SIZE_MASK))) == 0)
                    toVisitBlockIndexes.add(nextBlockIndex);

                nextBlockIndex = floodFillBlockIndex - 1;
                if ((floodFillBlockIndex & CHUNK_SIZE_MASK) == 0) visitedSides = (byte) (visitedSides | 1 << BOTTOM);
                else if ((visitedBlocks[nextBlockIndex >> CHUNK_SIZE_BITS] & (1 << (nextBlockIndex & CHUNK_SIZE_MASK))) == 0)
                    toVisitBlockIndexes.add(nextBlockIndex);

                nextBlockIndex = floodFillBlockIndex + 1;
                if ((floodFillBlockIndex & CHUNK_SIZE_MASK) == CHUNK_SIZE - 1)
                    visitedSides = (byte) (visitedSides | 1 << TOP);
                else if ((visitedBlocks[nextBlockIndex >> CHUNK_SIZE_BITS] & (1 << (nextBlockIndex & CHUNK_SIZE_MASK))) == 0)
                    toVisitBlockIndexes.add(nextBlockIndex);
            }

            for (int side1 = 0; side1 < 6; side1++) {
                if ((visitedSides & 1 << side1) == 0) continue;
                for (int side2 = side1 + 1; side2 < 6; side2++) {
                    if ((visitedSides & 1 << side2) != 0)
                        occlusionCullingData = setOcclusionCullingSidePair(side1, side2, occlusionCullingData);
                }
            }
        }

        for (byte light : light)
            if (light != 0) {
                Chunk.occlusionCullingData[index] = occlusionCullingData;
                return;
            }
        occlusionCullingData |= (short) 0x8000;
        Chunk.occlusionCullingData[index] = occlusionCullingData;
    }

    public void propagateBlockLight() {
        for (int inChunkX = 0; inChunkX < CHUNK_SIZE; inChunkX++)
            for (int inChunkZ = 0; inChunkZ < CHUNK_SIZE; inChunkZ++)
                for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++)

                    if ((Block.getBlockProperties(getSaveBlock(inChunkX, inChunkY, inChunkZ)) & LIGHT_EMITTING) != 0)
                        LightLogic.setBlockLight(worldCoordinate.x | inChunkX, worldCoordinate.y | inChunkY, worldCoordinate.z | inChunkZ, MAX_BLOCK_LIGHT_VALUE);
    }

    public void generateSurroundingChunks() {
        for (int chunkX = X - 1; chunkX <= X + 1; chunkX++)
            for (int chunkY = Y - 1; chunkY <= Y + 1; chunkY++)
                for (int chunkZ = Z - 1; chunkZ <= Z + 1; chunkZ++) {

                    long expectedId = GameLogic.getChunkId(chunkX, chunkY, chunkZ);
                    int index = GameLogic.getChunkIndex(chunkX, chunkY, chunkZ);
                    Chunk chunk = getChunk(index);

                    if (chunk == null) {
                        System.out.println("surrounding Chunk is null");
                        chunk = FileManager.getChunk(expectedId);
                        if (chunk == null) chunk = new Chunk(chunkX, chunkY, chunkZ);
                        else WorldGeneration.generateSurroundingChunkTreeBlocks(chunk);

                        storeChunk(chunk);
                        if (!chunk.isGenerated) WorldGeneration.generate(chunk);

                    } else if (chunk.id != expectedId) {
                        System.out.println("surrounding Chunk is not correct");
                        GameLogic.addToUnloadChunk(chunk);

                        chunk = FileManager.getChunk(expectedId);
                        if (chunk == null) chunk = new Chunk(chunkX, chunkY, chunkZ);
                        else WorldGeneration.generateSurroundingChunkTreeBlocks(chunk);

                        Chunk.storeChunk(chunk);
                        if (!chunk.isGenerated) WorldGeneration.generate(chunk);

                    } else if (!chunk.isGenerated) {
                        System.out.println("surrounding Chunk is not generated");
                        WorldGeneration.generate(chunk);
                    }
                }
    }

    public short getBlock(int inChunkX, int inChunkY, int inChunkZ) {
        if (inChunkX < 0) {
            Chunk neighbor = getChunk(X - 1, Y, Z);
            if (neighbor == null) return OUT_OF_WORLD;
            return neighbor.getSaveBlock(CHUNK_SIZE + inChunkX, inChunkY, inChunkZ);
        } else if (inChunkX >= CHUNK_SIZE) {
            Chunk neighbor = getChunk(X + 1, Y, Z);
            if (neighbor == null) return OUT_OF_WORLD;
            return neighbor.getSaveBlock(inChunkX - CHUNK_SIZE, inChunkY, inChunkZ);
        }
        if (inChunkY < 0) {
            Chunk neighbor = getChunk(X, Y - 1, Z);
            if (neighbor == null) return OUT_OF_WORLD;
            return neighbor.getSaveBlock(inChunkX, CHUNK_SIZE + inChunkY, inChunkZ);
        } else if (inChunkY >= CHUNK_SIZE) {
            Chunk neighbor = getChunk(X, Y + 1, Z);
            if (neighbor == null) {
                return OUT_OF_WORLD;
            }
            return neighbor.getSaveBlock(inChunkX, inChunkY - CHUNK_SIZE, inChunkZ);
        }
        if (inChunkZ < 0) {
            Chunk neighbor = getChunk(X, Y, Z - 1);
            if (neighbor == null) return OUT_OF_WORLD;
            return neighbor.getSaveBlock(inChunkX, inChunkY, CHUNK_SIZE + inChunkZ);
        } else if (inChunkZ >= CHUNK_SIZE) {
            Chunk neighbor = getChunk(X, Y, Z + 1);
            if (neighbor == null) return OUT_OF_WORLD;
            return neighbor.getSaveBlock(inChunkX, inChunkY, inChunkZ - CHUNK_SIZE);
        }

        return getSaveBlock(inChunkX, inChunkY, inChunkZ);
    }

    public short getSaveBlock(int inChunkX, int inChunkY, int inChunkZ) {
        int index = inChunkX << CHUNK_SIZE_BITS * 2 | inChunkZ << CHUNK_SIZE_BITS | inChunkY;
        return blocks[blocks.length <= index ? 0 : index];
    }

    public short getSaveBlock(int index) {
        return blocks[blocks.length <= index ? 0 : index];
    }

    public static short getBlockInWorld(int x, int y, int z) {
        Chunk chunk = world[GameLogic.getChunkIndex(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS)];
        if (chunk == null || !chunk.isGenerated) return OUT_OF_WORLD;
        return chunk.getSaveBlock(x & CHUNK_SIZE_MASK, y & CHUNK_SIZE_MASK, z & CHUNK_SIZE_MASK);
    }

    public void placeBlock(int inChunkX, int inChunkY, int inChunkZ, short block) {
        if (blocks.length == 1 && blocks[0] == block) return;
        if (blocks.length == 1) {
            short oldBlock = blocks[0];
            blocks = new short[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];
            Arrays.fill(blocks, oldBlock);
        }
        storeSave(inChunkX, inChunkY, inChunkZ, block);
        setModified();

        HeightMap heightMapObject = getHeightMap(X, Z);
        heightMapObject.setModified(true);
        int[] heightMap = heightMapObject.map;
        int totalY = worldCoordinate.y | inChunkY;

        if (totalY > heightMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ] && block != AIR)
            heightMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ] = totalY;

        else if (totalY == heightMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ] && block == AIR) {
            int totalX = worldCoordinate.x | inChunkX;
            int totalZ = worldCoordinate.z | inChunkZ;
            while (getBlockInWorld(totalX, totalY, totalZ) == AIR) totalY--;
            heightMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ] = totalY;
        }
    }

    public void storeSave(int inChunkX, int inChunkY, int inChunkZ, short block) {
        blocks[inChunkX << CHUNK_SIZE_BITS * 2 | inChunkZ << CHUNK_SIZE_BITS | inChunkY] = block;
    }

    public void storeTreeBlock(int inChunkX, int inChunkY, int inChunkZ, short block) {
        if (block == AIR) return;

        int x = worldCoordinate.x + inChunkX;
        int y = worldCoordinate.y + inChunkY;
        int z = worldCoordinate.z + inChunkZ;

        Chunk chunk = getChunk(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS);

        if (chunk == this) {
            inChunkX = x & CHUNK_SIZE_MASK;
            inChunkY = y & CHUNK_SIZE_MASK;
            inChunkZ = z & CHUNK_SIZE_MASK;

            int index = inChunkX << CHUNK_SIZE_BITS * 2 | inChunkZ << CHUNK_SIZE_BITS | inChunkY;
            if (blocks[index] != AIR && Block.isLeaveType(block)) return;
            blocks[index] = block;

            int[] heightMap = getHeightMap(X, Z).map;
            if (y > heightMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ])
                heightMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ] = y;
        } else storeSurroundingChunkTreeBlock(inChunkX, inChunkY, inChunkZ, block);
    }

    public void storeSurroundingChunkTreeBlock(int inChunkX, int inChunkY, int inChunkZ, short block) {
        if (block == AIR) return;

        int x = worldCoordinate.x + inChunkX;
        int y = worldCoordinate.y + inChunkY;
        int z = worldCoordinate.z + inChunkZ;

        inChunkX = x & CHUNK_SIZE_MASK;
        inChunkY = y & CHUNK_SIZE_MASK;
        inChunkZ = z & CHUNK_SIZE_MASK;

        Chunk chunk = getChunk(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS);
        if (chunk == this) return;

        if (chunk == null) {
            long id = GameLogic.getChunkId(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS);
            synchronized (toGenerateBlocks) {
                ArrayList<Integer> toPlaceBlocks = toGenerateBlocks.computeIfAbsent(id, ignored -> new ArrayList<>());
                toPlaceBlocks.add((int) block << 16 | inChunkX << CHUNK_SIZE_BITS * 2 | inChunkY << CHUNK_SIZE_BITS | inChunkZ);
            }
        } else {
            if (chunk.saved) return;
            if (chunk.getSaveBlock(inChunkX, inChunkY, inChunkZ) != AIR && Block.isLeaveType(block)) return;
            short[] blocks = chunk.blocks;
            if (blocks.length == 1) chunk.blocks = new short[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, block);

            int[] heightMap = getHeightMap(chunk.X, chunk.Z).map;
            if (y > heightMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ])
                heightMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ] = y;
        }
    }

    public static byte getLightInWorld(int x, int y, int z) {
        Chunk chunk = world[GameLogic.getChunkIndex(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS)];
        if (chunk == null || !chunk.isGenerated) return 0;
        return chunk.getSaveLight(x & CHUNK_SIZE_MASK, y & CHUNK_SIZE_MASK, z & CHUNK_SIZE_MASK);
    }

    public byte getSaveLight(int inChunkX, int inChunkY, int inChunkZ) {
        int index = inChunkX << CHUNK_SIZE_BITS * 2 | inChunkZ << CHUNK_SIZE_BITS | inChunkY;
        return light[light.length <= index ? 0 : index];
    }

    public static byte getBlockLightInWorld(int x, int y, int z) {
        Chunk chunk = world[GameLogic.getChunkIndex(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS)];
        if (chunk == null || !chunk.isGenerated) return 0;
        return chunk.getSaveBlockLight(x & CHUNK_SIZE_MASK, y & CHUNK_SIZE_MASK, z & CHUNK_SIZE_MASK);
    }

    public byte getSaveBlockLight(int inChunkX, int inChunkY, int inChunkZ) {
        int index = inChunkX << CHUNK_SIZE_BITS * 2 | inChunkZ << CHUNK_SIZE_BITS | inChunkY;
        return (byte) (light[light.length <= index ? 0 : index] & 15);
    }

    public byte getSaveBlockLight(int index) {
        return (byte) (light[light.length <= index ? 0 : index] & 15);
    }

    public void storeSaveBlockLight(int index, int blockLight) {
        if (light.length == 1) {
            byte oldLight = light[0];
            light = new byte[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];
            Arrays.fill(light, oldLight);
        }
        byte oldLight = light[index];
        light[index] = (byte) (oldLight & 240 | blockLight);
    }

    public void removeBlockLight(int index) {
        if (light.length == 1) {
            byte oldLight = light[0];
            light = new byte[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];
            Arrays.fill(light, oldLight);
        }
        light[index] &= (byte) 240;
    }

    public static byte getSkyLightInWorld(int x, int y, int z) {
        Chunk chunk = world[GameLogic.getChunkIndex(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS)];
        if (chunk == null || !chunk.isGenerated) return 0;
        return chunk.getSaveSkyLight(x & CHUNK_SIZE_MASK, y & CHUNK_SIZE_MASK, z & CHUNK_SIZE_MASK);
    }

    public byte getSaveSkyLight(int inChunkX, int inChunkY, int inChunkZ) {
        int index = inChunkX << CHUNK_SIZE_BITS * 2 | inChunkZ << CHUNK_SIZE_BITS | inChunkY;
        return (byte) (light[light.length <= index ? 0 : index] >> 4 & 15);
    }

    public byte getSaveSkyLight(int index) {
        return (byte) (light[light.length <= index ? 0 : index] >> 4 & 15);
    }

    public void removeSkyLight(int index) {
        if (light.length == 1) {
            byte oldLight = light[0];
            light = new byte[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];
            Arrays.fill(light, oldLight);
        }
        light[index] &= 15;
    }

    public void storeSaveSkyLight(int index, int skyLight) {
        if (light.length == 1) {
            byte oldLight = light[0];
            light = new byte[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];
            Arrays.fill(light, oldLight);
        }
        byte oldLight = light[index];
        light[index] = (byte) (skyLight << 4 | oldLight & 15);
    }

    public static Chunk getChunk(int chunkX, int chunkY, int chunkZ) {
        return world[GameLogic.getChunkIndex(chunkX, chunkY, chunkZ)];
    }

    public static Chunk getChunk(int index) {
        return world[index];
    }

    public static void storeChunk(Chunk chunk) {
        world[chunk.getIndex()] = chunk;
    }

    public static void setNull(int index) {
        world[index] = null;
        occlusionCullingData[index] = (short) 0;
    }

    public static HeightMap getHeightMap(int chunkX, int chunkZ) {
        return heightMaps[GameLogic.getHeightMapIndex(chunkX, chunkZ)];
    }

    public int[] getVertices(int side) {
        return vertices[side];
    }

    public int[] getWaterVertices() {
        return waterVertices;
    }

    public int[] getFoliageVertices() {
        return foliageVertices;
    }

    public Vector3i getWorldCoordinate() {
        return worldCoordinate;
    }

    public void clearMesh() {
        vertices = new int[6][0];
        waterVertices = new int[0];
        foliageVertices = new int[0];
    }

    public Model getModel(int side) {
        return model[side];
    }

    public void setModel(Model model, int side) {
        this.model[side] = model;
    }

    public Model getFoliageModel() {
        return foliageModel;
    }

    public void setFoliageModel(Model foliageModel) {
        this.foliageModel = foliageModel;
    }

    public Model getWaterModel() {
        return waterModel;
    }

    public void setWaterModel(Model waterModel) {
        this.waterModel = waterModel;
    }

    public int getIndex() {
        return index;
    }

    public boolean isMeshed() {
        return isMeshed;
    }

    public void setMeshed(boolean meshed) {
        isMeshed = meshed;
    }

    public boolean isGenerated() {
        return isGenerated;
    }

    public void setGenerated() {
        isGenerated = true;
    }

    public boolean isModified() {
        return isModified;
    }

    public void setModified() {
        isModified = true;
    }

    public static Chunk[] getWorld() {
        return world;
    }

    public static HeightMap[] getHeightMaps() {
        return heightMaps;
    }

    public static ArrayList<Integer> removeToGenerateBlocks(long id) {
        synchronized (toGenerateBlocks) {
            return toGenerateBlocks.remove(id);
        }
    }

    public boolean hasPropagatedBlockLight() {
        return hasPropagatedBlockLight;
    }

    public void setHasPropagatedBlockLight() {
        hasPropagatedBlockLight = true;
    }

    public static int getOcclusionCullingDamper(short occlusionCullingData) {
        return occlusionCullingData >> 15 & 1;
    }

    public static short getOcclusionCullingData(int index) {
        return occlusionCullingData[index];
    }

    public boolean isBlockOptimized() {
        return blocks.length == 1;
    }

    public boolean isLightOptimized() {
        return light.length == 1;
    }

    public int getLightLength() {
        return light.length;
    }

    public int getBlockLength() {
        return blocks.length;
    }

    public short[] getBlocks() {
        return blocks;
    }

//    public byte[] getLight() {
//        return light;
//    }

    public void setSaved() {
        this.saved = true;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public static void setWorld(Chunk[] world) {
        Chunk.world = world;
    }

    public static void setOcclusionCullingData(short[] occlusionCullingData) {
        Chunk.occlusionCullingData = occlusionCullingData;
    }

    public static void setHeightMaps(HeightMap[] heightMaps) {
        Chunk.heightMaps = heightMaps;
    }

    public static void setHeightMap(HeightMap heightMap, int index) {
        heightMaps[index] = heightMap;
    }

    public void setWaterVertices(int[] waterVertices) {
        this.waterVertices = waterVertices;
    }

    public void setFoliageVertices(int[] foliageVertices) {
        this.foliageVertices = foliageVertices;
    }

    public void setVertices(int[] vertices, int side) {
        this.vertices[side] = vertices;
    }

    public LinkedList<Entity> getEntityCluster(int entityClusterIndex) {
        return entityClusters[entityClusterIndex];
    }

    public LinkedList<Entity>[] getEntityClusters() {
        return entityClusters;
    }

    public static LinkedList<Entity> getEntityCluster(int clusterX, int clusterY, int clusterZ) {
        Chunk chunk = getChunk(clusterX >> ENTITY_CLUSTER_TO_CHUNK_BITS, clusterY >> ENTITY_CLUSTER_TO_CHUNK_BITS, clusterZ >> ENTITY_CLUSTER_TO_CHUNK_BITS);
        if (chunk == null) return null;
        int index = GameLogic.getEntityClusterIndex(clusterX & ENTITY_CLUSTER_SIZE_BITS, clusterY & ENTITY_CLUSTER_SIZE_BITS, clusterZ & ENTITY_CLUSTER_SIZE_BITS);
        return chunk.getEntityCluster(index);
    }
}
