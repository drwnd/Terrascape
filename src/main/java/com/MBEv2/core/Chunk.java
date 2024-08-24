package com.MBEv2.core;

import com.MBEv2.core.entity.Model;
import com.MBEv2.test.GameLogic;
import org.joml.Vector3i;

import java.util.*;

import static com.MBEv2.core.utils.Constants.*;

public class Chunk {

    private static final Chunk[] world = new Chunk[RENDERED_WORLD_WIDTH * RENDERED_WORLD_HEIGHT * RENDERED_WORLD_WIDTH];
    private static final int[][] heightMap = new int[RENDERED_WORLD_WIDTH * RENDERED_WORLD_WIDTH][CHUNK_SIZE * CHUNK_SIZE];
    private static final HashMap<Long, Chunk> savedChunks = new HashMap<>();
    private static final HashMap<Long, ArrayList<Long>> toGenerateBlocks = new HashMap<>();

    private short[] blocks;
    private byte[] light;

    private int[][] vertices = new int[6][0];
    private int[] waterVertices;

    private final int X, Y, Z;
    private final Vector3i worldCoordinate;
    private final long id;
    private final int index;

    private boolean isMeshed = false;
    private boolean isGenerated = false;
    private boolean isModified = false;
    private boolean hasPropagatedBlockLight = false;

    private final Model[] model;
    private Model waterModel;

    private short occlusionCullingData;
    private byte occlusionCullingDamper;

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
    }

    public void optimizeBlockStorage() {
        short firstBlock = blocks[0];
        for (short block : blocks)
            if (block != firstBlock)
                return;

        blocks = new short[]{firstBlock};
//        System.out.println("Optimized block Storage! Block: " + firstBlock);
    }

    public void optimizeLightStorage() {
        byte firstLight = light[0];
        for (byte light : light)
            if (light != firstLight)
                return;

        light = new byte[]{firstLight};
//        System.out.println("Optimized light Storage! + BlockLight: " + getSaveBlockLight(0) + " SkyLight: " + getSaveSkyLight(0));
    }

    public void setOcclusionCullingSidePair(int side1, int side2) {
        if (side1 == side2) return;
        int largerSide = Math.max(side1, side2);
        int smallerSide = Math.min(side1, side2);

        occlusionCullingData = (short) (occlusionCullingData | 1 << OCCLUSION_CULLING_LARGER_SIDE_OFFSETS[largerSide - 1] + smallerSide);
    }

    public boolean readOcclusionCullingSidePair(int side1, int side2) {
        if (side1 == side2) return false;
        int largerSide = Math.max(side1, side2);
        int smallerSide = Math.min(side1, side2);

        return (occlusionCullingData & 1 << OCCLUSION_CULLING_LARGER_SIDE_OFFSETS[largerSide - 1] + smallerSide) != 0;
    }

    public void generateOcclusionCullingData() {
        occlusionCullingData = (short) 0x8000;

        int[] visitedBlocks = new int[CHUNK_SIZE * CHUNK_SIZE];
        LinkedList<Integer> toVisitBlockIndexes = new LinkedList<>();
        int maxLightValue = 0;

        if (blocks.length == 1) {
            if (Block.getBlockType(blocks[0]) != FULL_BLOCK)
                occlusionCullingData = -1;
            return;
        }

        for (int blockIndex = 0; blockIndex < blocks.length; blockIndex++) {
            if ((visitedBlocks[blockIndex >> CHUNK_SIZE_BITS] & (1 << (blockIndex & CHUNK_SIZE_MASK))) != 0) continue;

            toVisitBlockIndexes.add(blockIndex);
            byte visitedSides = 0;

            while (!toVisitBlockIndexes.isEmpty()) {
                int floodFillBlockIndex = toVisitBlockIndexes.removeFirst();

                if ((visitedBlocks[floodFillBlockIndex >> CHUNK_SIZE_BITS] & (1 << (floodFillBlockIndex & CHUNK_SIZE_MASK))) != 0)
                    continue;
                visitedBlocks[floodFillBlockIndex >> CHUNK_SIZE_BITS] |= 1 << (floodFillBlockIndex & CHUNK_SIZE_MASK);

                if (Block.getBlockType(blocks[floodFillBlockIndex]) == FULL_BLOCK) continue;
                byte light = this.light[this.light.length <= floodFillBlockIndex ? 0 : floodFillBlockIndex];
                if ((light & 15) > maxLightValue) maxLightValue = light & 15;
                if ((light >> 4 & 15) > maxLightValue) maxLightValue = light >> 4 & 15;

                int nextBlockIndex = floodFillBlockIndex - (1 << CHUNK_SIZE_BITS * 2);
                if (floodFillBlockIndex >> CHUNK_SIZE_BITS * 2 == 0)
                    visitedSides = (byte) (visitedSides | 1 << LEFT);
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
                if ((floodFillBlockIndex & CHUNK_SIZE_MASK) == 0)
                    visitedSides = (byte) (visitedSides | 1 << BOTTOM);
                else if ((visitedBlocks[nextBlockIndex >> CHUNK_SIZE_BITS] & (1 << (nextBlockIndex & CHUNK_SIZE_MASK))) == 0)
                    toVisitBlockIndexes.add(nextBlockIndex);

                nextBlockIndex = floodFillBlockIndex + 1;
                if ((floodFillBlockIndex & CHUNK_SIZE_MASK) == CHUNK_SIZE - 1)
                    visitedSides = (byte) (visitedSides | 1 << TOP);
                else if ((visitedBlocks[nextBlockIndex >> CHUNK_SIZE_BITS] & (1 << (nextBlockIndex & CHUNK_SIZE_MASK))) == 0)
                    toVisitBlockIndexes.add(nextBlockIndex);
            }

            for (int side1 = 0; side1 < 6; side1++) {
                if ((visitedSides & 1 << side1) == 0)
                    continue;
                for (int side2 = side1 + 1; side2 < 6; side2++) {
                    if ((visitedSides & 1 << side2) != 0)
                        setOcclusionCullingSidePair(side1, side2);
                }
            }
        }

        if (maxLightValue != 0) occlusionCullingDamper = 0;
        else occlusionCullingDamper = 1;
    }

    public void generateMesh() {
        isMeshed = true;
        ArrayList<ArrayList<Integer>> verticesList = new ArrayList<>(6);
        ArrayList<Integer> waterVerticesList = new ArrayList<>();
        for (int side = 0; side < 6; side++) {
            verticesList.add(new ArrayList<>());
        }

        generateSurroundingChunks();
        if ((occlusionCullingData & 0x8000) == 0)
            generateOcclusionCullingData();

        if (light.length != 1) optimizeLightStorage();

        for (int x = 0; x < CHUNK_SIZE; x++)
            for (int z = 0; z < CHUNK_SIZE; z++)
                for (int y = 0; y < CHUNK_SIZE; y++) {

                    short block = getSaveBlock(x, y, z);

                    if (Block.getBlockType(block) == AIR_TYPE) continue;

                    for (int side = 0; side < 6; side++) {

                        int[] normal = Block.NORMALS[side];
                        if (Block.occludes(block, getBlock(x + normal[0], y + normal[1], z + normal[2]), side, worldCoordinate.x | x, worldCoordinate.y | y, worldCoordinate.z | z))
                            continue;

                        int texture = Block.getTextureIndex(block, side) - 1;

                        int u = texture & 15;
                        int v = texture >> 4 & 15;

                        if (block == WATER) addWaterSideToList(x, y, z, side, waterVerticesList);
                        else addSideToList(x, y, z, u, v, side, verticesList.get(side), block);
                    }
                }

        for (int side = 0; side < 6; side++) {
            ArrayList<Integer> sideVertices = verticesList.get(side);
            vertices[side] = new int[sideVertices.size()];
            for (int i = 0, size = sideVertices.size(); i < size; i++)
                vertices[side][i] = sideVertices.get(i);
        }

        waterVertices = new int[waterVerticesList.size()];
        for (int i = 0, size = waterVerticesList.size(); i < size; i++)
            waterVertices[i] = waterVerticesList.get(i);
    }

    public void propagateBlockLight() {
        for (int x = 0; x < CHUNK_SIZE; x++)
            for (int z = 0; z < CHUNK_SIZE; z++)
                for (int y = 0; y < CHUNK_SIZE; y++)
                    if ((Block.getBlockProperties(getSaveBlock(x, y, z)) & LIGHT_EMITTING_MASK) != 0)
                        LightLogic.setBlockLight(worldCoordinate.x | x, worldCoordinate.y | y, worldCoordinate.z | z, MAX_BLOCK_LIGHT_VALUE);
    }

    public void generateSurroundingChunks() {
        for (int x = X - 1; x <= X + 1; x++)
            for (int y = Y - 1; y <= Y + 1; y++)
                for (int z = Z - 1; z <= Z + 1; z++) {
                    long expectedId = GameLogic.getChunkId(x, y, z);
                    int index = GameLogic.getChunkIndex(x, y, z);
                    Chunk chunk = getChunk(index);

                    if (chunk == null) {
                        if (containsSavedChunk(expectedId)) chunk = removeSavedChunk(expectedId);
                        else chunk = new Chunk(x, y, z);

                        storeChunk(chunk);
                        if (!chunk.isGenerated) WorldGeneration.generate(chunk);

                    } else if (chunk.getId() != expectedId) {
                        GameLogic.addToUnloadChunk(chunk);

                        if (chunk.isModified) putSavedChunk(chunk);

                        if (containsSavedChunk(expectedId)) chunk = removeSavedChunk(expectedId);
                        else chunk = new Chunk(x, y, z);

                        Chunk.storeChunk(chunk);
                        if (!chunk.isGenerated) WorldGeneration.generate(chunk);

                    } else if (!chunk.isGenerated) WorldGeneration.generate(chunk);
                }
    }

    public void addSideToList(int x, int y, int z, int u, int v, int side, ArrayList<Integer> verticesList, short block) {
        byte[] blockXYZSubData;

        switch (side) {
            case FRONT:
                blockXYZSubData = Block.getXYZSubData(block);
                for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                    addVertexToList(verticesList, x + 1, y + 1, z + 1, u, v, side, block, 0, x, y, z, aabbIndex);
                    addVertexToList(verticesList, x, y + 1, z + 1, u + 1, v, side, block, 1, x, y, z, aabbIndex);
                    addVertexToList(verticesList, x + 1, y, z + 1, u, v + 1, side, block, 2, x, y, z, aabbIndex);
                    addVertexToList(verticesList, x, y, z + 1, u + 1, v + 1, side, block, 3, x, y, z, aabbIndex);
                }
                break;
            case TOP:
                blockXYZSubData = Block.getXYZSubData(block);
                for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                    addVertexToList(verticesList, x, y + 1, z, u, v, side, block, 0, x, y, z, aabbIndex);
                    addVertexToList(verticesList, x, y + 1, z + 1, u + 1, v, side, block, 1, x, y, z, aabbIndex);
                    addVertexToList(verticesList, x + 1, y + 1, z, u, v + 1, side, block, 2, x, y, z, aabbIndex);
                    addVertexToList(verticesList, x + 1, y + 1, z + 1, u + 1, v + 1, side, block, 3, x, y, z, aabbIndex);
                }
                break;
            case RIGHT:
                blockXYZSubData = Block.getXYZSubData(block);
                for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                    addVertexToList(verticesList, x + 1, y + 1, z, u, v, side, block, 0, x, y, z, aabbIndex);
                    addVertexToList(verticesList, x + 1, y + 1, z + 1, u + 1, v, side, block, 1, x, y, z, aabbIndex);
                    addVertexToList(verticesList, x + 1, y, z, u, v + 1, side, block, 2, x, y, z, aabbIndex);
                    addVertexToList(verticesList, x + 1, y, z + 1, u + 1, v + 1, side, block, 3, x, y, z, aabbIndex);
                }
                break;
            case BACK:
                blockXYZSubData = Block.getXYZSubData(block);
                for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                    addVertexToList(verticesList, x, y + 1, z, u, v, side, block, 0, x, y, z, aabbIndex);
                    addVertexToList(verticesList, x + 1, y + 1, z, u + 1, v, side, block, 1, x, y, z, aabbIndex);
                    addVertexToList(verticesList, x, y, z, u, v + 1, side, block, 2, x, y, z, aabbIndex);
                    addVertexToList(verticesList, x + 1, y, z, u + 1, v + 1, side, block, 3, x, y, z, aabbIndex);
                }
                break;
            case BOTTOM:
                blockXYZSubData = Block.getXYZSubData(block);
                for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                    addVertexToList(verticesList, x + 1, y, z + 1, u, v, side, block, 3, x, y, z, aabbIndex);
                    addVertexToList(verticesList, x, y, z + 1, u + 1, v, side, block, 1, x, y, z, aabbIndex);
                    addVertexToList(verticesList, x + 1, y, z, u, v + 1, side, block, 2, x, y, z, aabbIndex);
                    addVertexToList(verticesList, x, y, z, u + 1, v + 1, side, block, 0, x, y, z, aabbIndex);
                }
                break;
            case LEFT:
                blockXYZSubData = Block.getXYZSubData(block);
                for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                    addVertexToList(verticesList, x, y + 1, z + 1, u, v, side, block, 1, x, y, z, aabbIndex);
                    addVertexToList(verticesList, x, y + 1, z, u + 1, v, side, block, 0, x, y, z, aabbIndex);
                    addVertexToList(verticesList, x, y, z + 1, u, v + 1, side, block, 3, x, y, z, aabbIndex);
                    addVertexToList(verticesList, x, y, z, u + 1, v + 1, side, block, 2, x, y, z, aabbIndex);
                }
                break;
        }
    }

    public void addVertexToList(ArrayList<Integer> list, int x, int y, int z, int u, int v, int side, short block, int corner, int blockX, int blockY, int blockZ, int subDataAddend) {

        int totalX = worldCoordinate.x + x;
        int totalY = worldCoordinate.y + y;
        int totalZ = worldCoordinate.z + z;

        int skyLight = getVertexSkyLightInWorld(totalX, totalY, totalZ);
        int blockLight = getVertexBlockLightInWorld(totalX, totalY, totalZ);

        if ((Block.getBlockTypeData(block) & DYNAMIC_SHAPE_MASK) != 0) {
            addVertexToListDynamic(list, x, y, z, u, v, side, skyLight, blockLight, block, corner, blockX, blockY, blockZ);
            return;
        }

        int blockType = Block.getBlockType(block);
        int subX = Block.getSubX(blockType, side, corner, subDataAddend);
        int subY = Block.getSubY(blockType, side, corner, subDataAddend);
        int subZ = Block.getSubZ(blockType, side, corner, subDataAddend);
        int subU = Block.getSubU(blockType, side, corner, subDataAddend / 6);
        int subV = Block.getSubV(blockType, side, corner, subDataAddend / 6);

        list.add(packData(getAmbientOcclusionLevel(x, y, z, side, subX, subY, subZ), (x << 4) + subX + 15, (y << 4) + subY + 15, (z << 4) + subZ + 15));
        list.add(packData(side, skyLight, blockLight, (u << 4) + subU + 15, (v << 4) + subV + 15));
    }

    public void addWaterSideToList(int x, int y, int z, int side, ArrayList<Integer> verticesList) {
        int u = WATER_TEXTURE - 1 & 15;
        int v = WATER_TEXTURE - 1 >> 4 & 15;

        switch (side) {
            case FRONT:
                addWaterVertexToList(verticesList, x + 1, y + 1, z + 1, u, v, side, 0, x, y, z);
                addWaterVertexToList(verticesList, x, y + 1, z + 1, u + 1, v, side, 1, x, y, z);
                addWaterVertexToList(verticesList, x + 1, y, z + 1, u, v + 1, side, 2, x, y, z);
                addWaterVertexToList(verticesList, x, y, z + 1, u + 1, v + 1, side, 3, x, y, z);
                break;
            case TOP:
                addWaterVertexToList(verticesList, x, y + 1, z, u, v, side, 0, x, y, z);
                addWaterVertexToList(verticesList, x, y + 1, z + 1, u + 1, v, side, 1, x, y, z);
                addWaterVertexToList(verticesList, x + 1, y + 1, z, u, v + 1, side, 2, x, y, z);
                addWaterVertexToList(verticesList, x + 1, y + 1, z + 1, u + 1, v + 1, side, 3, x, y, z);
                break;
            case RIGHT:
                addWaterVertexToList(verticesList, x + 1, y + 1, z, u, v, side, 0, x, y, z);
                addWaterVertexToList(verticesList, x + 1, y + 1, z + 1, u + 1, v, side, 1, x, y, z);
                addWaterVertexToList(verticesList, x + 1, y, z, u, v + 1, side, 2, x, y, z);
                addWaterVertexToList(verticesList, x + 1, y, z + 1, u + 1, v + 1, side, 3, x, y, z);
                break;
            case BACK:
                addWaterVertexToList(verticesList, x, y + 1, z, u, v, side, 0, x, y, z);
                addWaterVertexToList(verticesList, x + 1, y + 1, z, u + 1, v, side, 1, x, y, z);
                addWaterVertexToList(verticesList, x, y, z, u, v + 1, side, 2, x, y, z);
                addWaterVertexToList(verticesList, x + 1, y, z, u + 1, v + 1, side, 3, x, y, z);
                break;
            case BOTTOM:
                addWaterVertexToList(verticesList, x + 1, y, z + 1, u, v, side, 3, x, y, z);
                addWaterVertexToList(verticesList, x, y, z + 1, u + 1, v, side, 1, x, y, z);
                addWaterVertexToList(verticesList, x + 1, y, z, u, v + 1, side, 2, x, y, z);
                addWaterVertexToList(verticesList, x, y, z, u + 1, v + 1, side, 0, x, y, z);
                break;
            case LEFT:
                addWaterVertexToList(verticesList, x, y + 1, z + 1, u, v, side, 1, x, y, z);
                addWaterVertexToList(verticesList, x, y + 1, z, u + 1, v, side, 0, x, y, z);
                addWaterVertexToList(verticesList, x, y, z + 1, u, v + 1, side, 3, x, y, z);
                addWaterVertexToList(verticesList, x, y, z, u + 1, v + 1, side, 2, x, y, z);
                break;
        }
    }

    public void addWaterVertexToList(ArrayList<Integer> list, int x, int y, int z, int u, int v, int side, int corner, int blockX, int blockY, int blockZ) {
        int totalX = worldCoordinate.x + x;
        int totalY = worldCoordinate.y + y;
        int totalZ = worldCoordinate.z + z;

        int skyLight = getVertexSkyLightInWorld(totalX, totalY, totalZ);
        int blockLight = getVertexBlockLightInWorld(totalX, totalY, totalZ);

        int subX = 0;
        int subY = 0;
        int subZ = 0;
        int subU = 0;
        int subV = 0;

        boolean shouldSimulateWaves = false;

        if (side == TOP) {
            subY = -2;
            shouldSimulateWaves = true;
        } else if (side != BOTTOM) {
            shouldSimulateWaves = corner == 1 || corner == 0;
            short blockAbove = getBlock(blockX, blockY + 1, blockZ);
            if ((corner == 0 || corner == 1) && blockAbove != WATER && Block.getBlockTypeOcclusionData(blockAbove, BOTTOM) == 0) {
                subY = -2;
                subV = 2;
            } else if (corner == 2 || corner == 3) {
                int[] normal = Block.NORMALS[side];
                short adjacentBlock = getBlock(blockX + normal[0], blockY, blockZ + normal[2]);
                if (adjacentBlock == WATER && (blockAbove == WATER || Block.getBlockTypeOcclusionData(blockAbove, BOTTOM) != 0)) {
                    subY = 14;
                    subV = -14;
                }
            }
        }
        short blockBelow = getBlock(blockX, blockY - 1, blockZ);
        shouldSimulateWaves = shouldSimulateWaves || blockBelow == WATER;

        if (x == 0 || x == CHUNK_SIZE || z == 0 || z == CHUNK_SIZE) shouldSimulateWaves = false;

        list.add(packData(getAmbientOcclusionLevel(x, y, z, side, subX, subY, subZ), (x << 4) + subX + 15, (y << 4) + subY + 15, (z << 4) + subZ + 15));
        list.add(packWaterData(shouldSimulateWaves ? 1 : 0, side, skyLight, blockLight, (u << 4) + subU + 15, (v << 4) + subV + 15));
    }


    public void addVertexToListDynamic(ArrayList<Integer> list, int x, int y, int z, int u, int v, int side, int skyLight, int blockLight, short block, int corner, int blockX, int blockY, int blockZ) {
        int subX = 0;
        int subY = 0;
        int subZ = 0;
        int subU = 0;
        int subV = 0;

        if (Block.getBlockType(block) == LIQUID_TYPE) {
            if (side == TOP) {
                subY = -2;
            } else if (side != BOTTOM) {
                short blockAbove = getBlock(blockX, blockY + 1, blockZ);
                if ((corner == 0 || corner == 1) && blockAbove != block && Block.getBlockTypeOcclusionData(blockAbove, BOTTOM) == 0) {
                    subY = -2;
                    subV = 2;
                } else if (corner == 2 || corner == 3) {
                    int[] normal = Block.NORMALS[side];
                    short adjacentBlock = getBlock(blockX + normal[0], blockY, blockZ + normal[2]);
                    if (adjacentBlock == block && (blockAbove == block || Block.getBlockTypeOcclusionData(blockAbove, BOTTOM) != 0)) {
                        subY = 14;
                        subV = -14;
                    }
                }
            }
        } else if (Block.getBlockType(block) == CACTUS_TYPE) {
            int blockType = Block.getBlockType(block);
            switch (side) {
                case TOP, BOTTOM -> {
                    subX = Block.getSubX(blockType, side, corner, 0);
                    subZ = Block.getSubZ(blockType, side, corner, 0);
                    subU = Block.getSubU(blockType, side, corner, 0);
                    subV = Block.getSubV(blockType, side, corner, 0);
                }
                case FRONT, BACK -> subZ = Block.getSubZ(blockType, side, corner, 0);

                case RIGHT, LEFT -> subX = Block.getSubX(blockType, side, corner, 0);
            }
        }

        list.add(packData(getAmbientOcclusionLevel(x, y, z, side, subX, subY, subZ), (x << 4) + subX + 15, (y << 4) + subY + 15, (z << 4) + subZ + 15));
        list.add(packData(side, skyLight, blockLight, (u << 4) + subU + 15, (v << 4) + subV + 15));
    }

    public int packData(int ambientOcclusionLevel, int x, int y, int z) {
        return ambientOcclusionLevel << 30 | x << 20 | y << 10 | z;
    }

    public int packData(int side, int skyLight, int blockLight, int u, int v) {
        return side << 26 | skyLight << 22 | blockLight << 18 | u << 9 | v;
    }

    public int packWaterData(int waveMultiplier, int side, int skyLight, int blockLight, int u, int v) {
        return waveMultiplier << 29 | side << 26 | skyLight << 22 | blockLight << 18 | u << 9 | v;
    }

    public int getAmbientOcclusionLevel(int x, int y, int z, int side, int subX, int subY, int subZ) {

        int level = 0;
        switch (side) {
            case FRONT:
                if (subZ != 0) z--;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y - 1, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y - 1, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                break;
            case TOP:
                if (subY != 0) y--;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                break;
            case RIGHT:
                if (subX != 0) x--;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y - 1, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y - 1, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                break;
            case BACK:
                if (subZ != 0) z++;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y - 1, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y - 1, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                break;
            case BOTTOM:
                if (subY != 0) y++;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y - 1, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y - 1, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y - 1, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y - 1, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                break;
            case LEFT:
                if (subX != 0) x++;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y - 1, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockTypeData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y - 1, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                break;
        }
        return level & 3;
    }

    public short getBlock(int x, int y, int z) {
        if (x < 0) {
            Chunk neighbor = getChunk(X - 1, Y, Z);
            if (neighbor == null) return OUT_OF_WORLD;
            return neighbor.getSaveBlock(CHUNK_SIZE + x, y, z);
        } else if (x >= CHUNK_SIZE) {
            Chunk neighbor = getChunk(X + 1, Y, Z);
            if (neighbor == null) return OUT_OF_WORLD;
            return neighbor.getSaveBlock(x - CHUNK_SIZE, y, z);
        }
        if (y < 0) {
            Chunk neighbor = getChunk(X, Y - 1, Z);
            if (neighbor == null) return OUT_OF_WORLD;
            return neighbor.getSaveBlock(x, CHUNK_SIZE + y, z);
        } else if (y >= CHUNK_SIZE) {
            Chunk neighbor = getChunk(X, Y + 1, Z);
            if (neighbor == null) {
                return OUT_OF_WORLD;
            }
            return neighbor.getSaveBlock(x, y - CHUNK_SIZE, z);
        }
        if (z < 0) {
            Chunk neighbor = getChunk(X, Y, Z - 1);
            if (neighbor == null) return OUT_OF_WORLD;
            return neighbor.getSaveBlock(x, y, CHUNK_SIZE + z);
        } else if (z >= CHUNK_SIZE) {
            Chunk neighbor = getChunk(X, Y, Z + 1);
            if (neighbor == null) return OUT_OF_WORLD;
            return neighbor.getSaveBlock(x, y, z - CHUNK_SIZE);
        }

        return getSaveBlock(x, y, z);
    }

    public short getSaveBlock(int x, int y, int z) {
        int index = x << CHUNK_SIZE_BITS * 2 | z << CHUNK_SIZE_BITS | y;
        return blocks[blocks.length <= index ? 0 : index];
    }

    public short getSaveBlock(int index) {
        return blocks[blocks.length <= index ? 0 : index];
    }

    public static short getBlockInWorld(int x, int y, int z) {
        Chunk chunk = world[GameLogic.getChunkIndex(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS)];
        if (chunk == null || !chunk.isGenerated) return OUT_OF_WORLD;
        return chunk.getSaveBlock(x & CHUNK_SIZE - 1, y & CHUNK_SIZE - 1, z & CHUNK_SIZE - 1);
    }

    public void placeBlock(int x, int y, int z, short block) {
        if (blocks.length == 1 && blocks[0] == block) return;
        if (blocks.length == 1) {
            short oldBlock = blocks[0];
            blocks = new short[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];
            Arrays.fill(blocks, oldBlock);
        }
        blocks[x << CHUNK_SIZE_BITS * 2 | z << CHUNK_SIZE_BITS | y] = block;

        int[] heightMap = Chunk.heightMap[GameLogic.getHeightMapIndex(X, Z)];
        int totalY = worldCoordinate.y | y;

        if (totalY > heightMap[x << CHUNK_SIZE_BITS | z]) heightMap[x << CHUNK_SIZE_BITS | z] = totalY;

        else if (totalY == heightMap[x << CHUNK_SIZE_BITS | z] && block == AIR) {
            int totalX = worldCoordinate.x | x;
            int totalZ = worldCoordinate.z | z;
            while (getBlockInWorld(totalX, totalY, totalZ) == AIR) totalY--;
            heightMap[x << CHUNK_SIZE_BITS | z] = totalY;
        }
    }

    public void storeSave(int x, int y, int z, short block) {
        blocks[x << CHUNK_SIZE_BITS * 2 | z << CHUNK_SIZE_BITS | y] = block;
    }

    public void storeTreeBlock(int inChunkX, int inChunkY, int inChunkZ, short block) {
        if (block == AIR) return;

        int x = worldCoordinate.x + inChunkX;
        int y = worldCoordinate.y + inChunkY;
        int z = worldCoordinate.z + inChunkZ;

        Chunk chunk = getChunk(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS);

        inChunkX = x & CHUNK_SIZE_MASK;
        inChunkY = y & CHUNK_SIZE_MASK;
        inChunkZ = z & CHUNK_SIZE_MASK;

        if (chunk == this) {
            int index = inChunkX << CHUNK_SIZE_BITS * 2 | inChunkZ << CHUNK_SIZE_BITS | inChunkY;
            if (blocks[index] != AIR && Block.isLeaveType(block))
                return;
            blocks[index] = block;

            int[] heightMap = getHeightMap(X, Z);
            if (y > heightMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ])
                heightMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ] = y;
        } else if (chunk == null) {

            long id = GameLogic.getChunkId(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS);
            synchronized (toGenerateBlocks) {
                ArrayList<Long> toPlaceBlocks = toGenerateBlocks.computeIfAbsent(id, k -> new ArrayList<>());
                toPlaceBlocks.add((long) block << 48 | inChunkX << CHUNK_SIZE_BITS * 2 | inChunkY << CHUNK_SIZE_BITS | inChunkZ);
            }
        } else {
            if (chunk.getSaveBlock(inChunkX, inChunkY, inChunkZ) != AIR && Block.isLeaveType(block))
                return;
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, block);

            int[] heightMap = getHeightMap(chunk.getChunkX(), chunk.getChunkZ());
            if (y > heightMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ])
                heightMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ] = y;
        }
    }

    public static byte getBlockLightInWorld(int x, int y, int z) {
        Chunk chunk = world[GameLogic.getChunkIndex(x >> CHUNK_SIZE_BITS, y >> CHUNK_SIZE_BITS, z >> CHUNK_SIZE_BITS)];
        if (chunk == null || !chunk.isGenerated) return 0;
        return chunk.getSaveBlockLight(x & CHUNK_SIZE - 1, y & CHUNK_SIZE - 1, z & CHUNK_SIZE - 1);
    }

    public byte getSaveBlockLight(int x, int y, int z) {
        int index = x << CHUNK_SIZE_BITS * 2 | z << CHUNK_SIZE_BITS | y;
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
        return chunk.getSaveSkyLight(x & CHUNK_SIZE - 1, y & CHUNK_SIZE - 1, z & CHUNK_SIZE - 1);
    }

    public byte getSaveSkyLight(int x, int y, int z) {
        int index = x << CHUNK_SIZE_BITS * 2 | z << CHUNK_SIZE_BITS | y;
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

    public static int getVertexBlockLightInWorld(int x, int y, int z) {
        int max = 0;
        for (int x_ = x - 1; x_ <= x; x_++)
            for (int y_ = y - 1; y_ <= y; y_++)
                for (int z_ = z - 1; z_ <= z; z_++) {
                    int currentBlockLight = getBlockLightInWorld(x_, y_, z_);
                    if (max < currentBlockLight) max = currentBlockLight;
                }
        return max;
    }

    public static int getVertexSkyLightInWorld(int x, int y, int z) {
        int max = 0;
        for (int x_ = x - 1; x_ <= x; x_++)
            for (int y_ = y - 1; y_ <= y; y_++)
                for (int z_ = z - 1; z_ <= z; z_++) {
                    int currentBlockLight = getSkyLightInWorld(x_, y_, z_);
                    if (max < currentBlockLight) max = currentBlockLight;
                }
        return max;
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
    }

    public static int[] getHeightMap(int chunkX, int chunkZ) {
        return heightMap[GameLogic.getHeightMapIndex(chunkX, chunkZ)];
    }

    public int[] getVertices(int side) {
        return vertices[side];
    }

    public int[] getWaterVertices() {
        return waterVertices;
    }

    public Vector3i getWorldCoordinate() {
        return worldCoordinate;
    }

    public void clearMesh() {
        vertices = new int[6][0];
        waterVertices = new int[0];
    }

    public Model getModel(int side) {
        return model[side];
    }

    public void setModel(Model model, int side) {
        this.model[side] = model;
    }

    public Model getWaterModel() {
        return waterModel;
    }

    public void setWaterModel(Model waterModel) {
        this.waterModel = waterModel;
    }

    public long getId() {
        return id;
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

    public static boolean containsSavedChunk(long id) {
        synchronized (savedChunks) {
            return savedChunks.containsKey(id);
        }
    }

    public static void putSavedChunk(Chunk chunk) {
        synchronized (savedChunks) {
            savedChunks.put(chunk.getId(), chunk);
        }
        chunk.setMeshed(false);
    }

    public static Chunk removeSavedChunk(long id) {
        synchronized (savedChunks) {
            return savedChunks.remove(id);
        }
    }

    public static ArrayList<Long> removeToGenerateBlocks(long id) {
        synchronized (toGenerateBlocks) {
            return toGenerateBlocks.remove(id);
        }
    }

    public int getChunkX() {
        return X;
    }

    public int getChunkY() {
        return Y;
    }

    public int getChunkZ() {
        return Z;
    }

    public boolean hasPropagatedBlockLight() {
        return hasPropagatedBlockLight;
    }

    public void setHasPropagatedBlockLight() {
        hasPropagatedBlockLight = true;
    }

    public void setOcclusionCullingDataOutdated() {
        occlusionCullingData = (short) (occlusionCullingData & 0b111111111111111);
    }

    public byte getOcclusionCullingDamper() {
        return occlusionCullingDamper;
    }

    public short getOcclusionCullingData() {
        return occlusionCullingData;
    }

    public boolean isBlockOptimized() {
        return blocks.length == 1;
    }

    public boolean isLightOptimized() {
        return light.length == 1;
    }
}
