package com.MBEv2.core;

import com.MBEv2.core.entity.Model;
import com.MBEv2.test.GameLogic;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import static com.MBEv2.core.utils.Constants.*;

public class Chunk {

    private static final Chunk[] world = new Chunk[RENDERED_WORLD_WIDTH * RENDERED_WORLD_HEIGHT * RENDERED_WORLD_WIDTH];
    private static final HashMap<Long, Chunk> savedChunks = new HashMap<>();

    private short[] blocks;
    private byte[] light;

    private int[] vertices;
    private int[] transparentVertices;

    private final int X, Y, Z;
    private final Vector3i worldCoordinate;
    private final long id;
    private final int index;

    private boolean isMeshed = false;
    private boolean isGenerated = false;
    private boolean isModified = false;
    private boolean hasPropagatedBlockLight = false;

    private Model model;
    private Model transparentModel;

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
        ArrayList<Integer> verticesList = new ArrayList<>();
        ArrayList<Integer> transparentVerticesList = new ArrayList<>();

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

                        if (block != WATER) {
                            addSideToList(x, y, z, u, v, side, verticesList, block);
                            continue;
                        }
                        addSideToList(x, y, z, u, v, side, transparentVerticesList, block);
                    }
                }

        vertices = new int[verticesList.size()];
        for (int i = 0, size = verticesList.size(); i < size; i++)
            vertices[i] = verticesList.get(i);

        transparentVertices = new int[transparentVerticesList.size()];
        for (int i = 0, size = transparentVerticesList.size(); i < size; i++)
            transparentVertices[i] = transparentVerticesList.get(i);
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
        int skyLight;
        int blockLight;

        switch (side) {
            case FRONT:
                skyLight = getSkyLightInWorld(worldCoordinate.x | x, worldCoordinate.y | y, (worldCoordinate.z | z) + (Block.getXYZSubData(block)[MAX_Z] == 0 ? 1 : 0));
                blockLight = getBlockLightInWorld(worldCoordinate.x | x, worldCoordinate.y | y, (worldCoordinate.z | z) + (Block.getXYZSubData(block)[MAX_Z] == 0 ? 1 : 0));
                addVertexToList(verticesList, x + 1, y + 1, z + 1, u, v, side, skyLight, blockLight, block, 0, x, y, z);
                addVertexToList(verticesList, x, y + 1, z + 1, u + 1, v, side, skyLight, blockLight, block, 1, x, y, z);
                addVertexToList(verticesList, x + 1, y, z + 1, u, v + 1, side, skyLight, blockLight, block, 2, x, y, z);
                addVertexToList(verticesList, x, y, z + 1, u + 1, v + 1, side, skyLight, blockLight, block, 3, x, y, z);
                break;
            case TOP:
                skyLight = getSkyLightInWorld(worldCoordinate.x | x, (worldCoordinate.y | y) + (Block.getXYZSubData(block)[MAX_Y] == 0 ? 1 : 0), worldCoordinate.z | z);
                blockLight = getBlockLightInWorld(worldCoordinate.x | x, (worldCoordinate.y | y) + (Block.getXYZSubData(block)[MAX_Y] == 0 ? 1 : 0), worldCoordinate.z | z);
                addVertexToList(verticesList, x, y + 1, z, u, v, side, skyLight, blockLight, block, 0, x, y, z);
                addVertexToList(verticesList, x, y + 1, z + 1, u + 1, v, side, skyLight, blockLight, block, 1, x, y, z);
                addVertexToList(verticesList, x + 1, y + 1, z, u, v + 1, side, skyLight, blockLight, block, 2, x, y, z);
                addVertexToList(verticesList, x + 1, y + 1, z + 1, u + 1, v + 1, side, skyLight, blockLight, block, 3, x, y, z);
                break;
            case RIGHT:
                skyLight = getSkyLightInWorld((worldCoordinate.x | x) + (Block.getXYZSubData(block)[MAX_X] == 0 ? 1 : 0), worldCoordinate.y | y, worldCoordinate.z | z);
                blockLight = getBlockLightInWorld((worldCoordinate.x | x) + (Block.getXYZSubData(block)[MAX_X] == 0 ? 1 : 0), worldCoordinate.y | y, worldCoordinate.z | z);
                addVertexToList(verticesList, x + 1, y + 1, z, u, v, side, skyLight, blockLight, block, 0, x, y, z);
                addVertexToList(verticesList, x + 1, y + 1, z + 1, u + 1, v, side, skyLight, blockLight, block, 1, x, y, z);
                addVertexToList(verticesList, x + 1, y, z, u, v + 1, side, skyLight, blockLight, block, 2, x, y, z);
                addVertexToList(verticesList, x + 1, y, z + 1, u + 1, v + 1, side, skyLight, blockLight, block, 3, x, y, z);
                break;
            case BACK:
                skyLight = getSkyLightInWorld(worldCoordinate.x | x, worldCoordinate.y | y, (worldCoordinate.z | z) - (Block.getXYZSubData(block)[MIN_Z] == 0 ? 1 : 0));
                blockLight = getBlockLightInWorld(worldCoordinate.x | x, worldCoordinate.y | y, (worldCoordinate.z | z) - (Block.getXYZSubData(block)[MIN_Z] == 0 ? 1 : 0));
                addVertexToList(verticesList, x, y + 1, z, u, v, side, skyLight, blockLight, block, 0, x, y, z);
                addVertexToList(verticesList, x + 1, y + 1, z, u + 1, v, side, skyLight, blockLight, block, 1, x, y, z);
                addVertexToList(verticesList, x, y, z, u, v + 1, side, skyLight, blockLight, block, 2, x, y, z);
                addVertexToList(verticesList, x + 1, y, z, u + 1, v + 1, side, skyLight, blockLight, block, 3, x, y, z);
                break;
            case BOTTOM:
                skyLight = getSkyLightInWorld(worldCoordinate.x | x, (worldCoordinate.y | y) - (Block.getXYZSubData(block)[MIN_Y] == 0 ? 1 : 0), worldCoordinate.z | z);
                blockLight = getBlockLightInWorld(worldCoordinate.x | x, (worldCoordinate.y | y) - (Block.getXYZSubData(block)[MIN_Y] == 0 ? 1 : 0), worldCoordinate.z | z);
                addVertexToList(verticesList, x + 1, y, z + 1, u, v, side, skyLight, blockLight, block, 3, x, y, z);
                addVertexToList(verticesList, x, y, z + 1, u + 1, v, side, skyLight, blockLight, block, 1, x, y, z);
                addVertexToList(verticesList, x + 1, y, z, u, v + 1, side, skyLight, blockLight, block, 2, x, y, z);
                addVertexToList(verticesList, x, y, z, u + 1, v + 1, side, skyLight, blockLight, block, 0, x, y, z);
                break;
            case LEFT:
                skyLight = getSkyLightInWorld((worldCoordinate.x | x) - (Block.getXYZSubData(block)[MIN_X] == 0 ? 1 : 0), worldCoordinate.y | y, worldCoordinate.z | z);
                blockLight = getBlockLightInWorld((worldCoordinate.x | x) - (Block.getXYZSubData(block)[MIN_X] == 0 ? 1 : 0), worldCoordinate.y | y, worldCoordinate.z | z);
                addVertexToList(verticesList, x, y + 1, z + 1, u, v, side, skyLight, blockLight, block, 1, x, y, z);
                addVertexToList(verticesList, x, y + 1, z, u + 1, v, side, skyLight, blockLight, block, 0, x, y, z);
                addVertexToList(verticesList, x, y, z + 1, u, v + 1, side, skyLight, blockLight, block, 3, x, y, z);
                addVertexToList(verticesList, x, y, z, u + 1, v + 1, side, skyLight, blockLight, block, 2, x, y, z);
                break;
        }
    }

    public void addVertexToList(ArrayList<Integer> list, int x, int y, int z, int u, int v, int side, int skyLight, int blockLight, short block, int corner, int blockX, int blockY, int blockZ) {
        if ((Block.getBlockTypeData(block) & DYNAMIC_SHAPE_MASK) != 0) {
            addVertexToListDynamic(list, x, y, z, u, v, side, skyLight, blockLight, block, corner, blockX, blockY, blockZ);
            return;
        }

        int subX = Block.getSubX(block, side, corner);
        int subY = Block.getSubY(block, side, corner);
        int subZ = Block.getSubZ(block, side, corner);
        int subU = Block.getSubU(block, side, corner);
        int subV = Block.getSubV(block, side, corner);

        list.add(packData(getAmbientOcclusionLevel(x, y, z, side, subX, subY, subZ), skyLight, blockLight, (x << 4) + subX + 15, (y << 4) + subY) + 15);
        list.add(packData(side, (u << 4) + subU + 15, (v << 4) + subV + 15, (z << 4) + subZ + 15));
    }

    public void addVertexToListDynamic(ArrayList<Integer> list, int x, int y, int z, int u, int v, int side, int skyLight, int blockLight, short block, int corner, int blockX, int blockY, int blockZ) {
        int subX = 0;
        int subY = 0;
        int subZ = 0;
        int subU = 0;
        int subV = 0;

        if (Block.getBlockType(block) == WATER_TYPE) {
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
            switch (side) {
                case TOP, BOTTOM -> {
                    subX = Block.getSubX(block, side, corner);
                    subZ = Block.getSubZ(block, side, corner);
                    subU = Block.getSubU(block, side, corner);
                    subV = Block.getSubV(block, side, corner);
                }
                case FRONT, BACK -> subZ = Block.getSubZ(block, side, corner);

                case RIGHT, LEFT -> subX = Block.getSubX(block, side, corner);
            }
        }

        list.add(packData(getAmbientOcclusionLevel(x, y, z, side, subX, subY, subZ), skyLight, blockLight, (x << 4) + subX + 15, (y << 4) + subY) + 15);
        list.add(packData(side, (u << 4) + subU + 15, (v << 4) + subV + 15, (z << 4) + subZ + 15));
    }

    public int packData(int ambientOcclusionLevel, int skyLight, int blockLight, int x, int y) {
        return ambientOcclusionLevel << 30 | skyLight << 26 | blockLight << 22 | x << 11 | y;
    }

    public int packData(int side, int u, int v, int z) {
        return side << 29 | u << 20 | v << 11 | z;
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
    }

    public void storeSave(int x, int y, int z, short block) {
        blocks[x << CHUNK_SIZE_BITS * 2 | z << CHUNK_SIZE_BITS | y] = block;
    }

    public void storeTreeBlock(int x, int y, int z, short block) {
        int index = x << CHUNK_SIZE_BITS * 2 | z << CHUNK_SIZE_BITS | y;
        if (block == AIR || blocks[index] != AIR && Block.isLeaveType(block))
            return;
        blocks[index] = block;
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

    public static Chunk getChunk(int x, int y, int z) {
        return world[GameLogic.getChunkIndex(x, y, z)];
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

    public int[] getVertices() {
        return vertices;
    }

    public int[] getTransparentVertices() {
        return transparentVertices;
    }

    public Vector3i getWorldCoordinate() {
        return worldCoordinate;
    }

    public void clearMesh() {
        vertices = new int[0];
        transparentVertices = new int[0];
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Model getTransparentModel() {
        return transparentModel;
    }

    public void setTransparentModel(Model transparentModel) {
        this.transparentModel = transparentModel;
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

    public byte[] getLight() {
        return light;
    }

    public static Chunk[] getWorld() {
        return world;
    }

    public static boolean containsSavedChunk(long id) {
        return savedChunks.containsKey(id);
    }

    public static void putSavedChunk(Chunk chunk) {
        savedChunks.put(chunk.getId(), chunk);
        chunk.setMeshed(false);
    }

    public static Chunk removeSavedChunk(long id) {
        return savedChunks.remove(id);
    }

    public int getX() {
        return X;
    }

    public int getY() {
        return Y;
    }

    public int getZ() {
        return Z;
    }

    public boolean hasPropagatedBlockLight() {
        return hasPropagatedBlockLight;
    }

    public void setHasPropagatedBlockLight() {
        hasPropagatedBlockLight = true;
    }

    public short getOcclusionCullingData() {
        return occlusionCullingData;
    }

    public void setOcclusionCullingDataOutdated() {
        occlusionCullingData = (short) (occlusionCullingData & 0b111111111111111);
    }

    public byte getOcclusionCullingDamper() {
        return occlusionCullingDamper;
    }
}
