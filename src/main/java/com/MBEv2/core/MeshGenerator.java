package com.MBEv2.core;

import org.joml.Vector3i;

import java.util.ArrayList;

import static com.MBEv2.core.utils.Constants.*;

public class MeshGenerator {

    private Chunk chunk;
    private Vector3i worldCoordinate;

    private int blockX, blockY, blockZ;
    private int side;
    private short block;
    private ArrayList<Integer> list;

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
        worldCoordinate = chunk.getWorldCoordinate();
    }

    public void generateMesh() {
        chunk.setMeshed(true);
        ArrayList<ArrayList<Integer>> verticesList = new ArrayList<>(6);
        ArrayList<Integer> foliageVerticesList = new ArrayList<>();
        ArrayList<Integer> waterVerticesList = new ArrayList<>();
        for (int side = 0; side < 6; side++) verticesList.add(new ArrayList<>());

        chunk.generateSurroundingChunks();
        if (chunk.getLightLength() != 1) chunk.optimizeLightStorage();
        chunk.generateOcclusionCullingData();

        for (blockX = 0; blockX < CHUNK_SIZE; blockX++)
            for (blockZ = 0; blockZ < CHUNK_SIZE; blockZ++)
                for (blockY = 0; blockY < CHUNK_SIZE; blockY++) {

                    block = chunk.getSaveBlock(blockX, blockY, blockZ);

                    if (Block.getBlockType(block) == AIR_TYPE) continue;

                    for (side = 0; side < 6; side++) {

                        int[] normal = Block.NORMALS[side];
                        short occludingBlock = chunk.getBlock(blockX + normal[0], blockY + normal[1], blockZ + normal[2]);
                        if (Block.occludes(block, occludingBlock, side, worldCoordinate.x | blockX, worldCoordinate.y | blockY, worldCoordinate.z | blockZ))
                            continue;

                        int texture = Block.getTextureIndex(block, side) - 1;

                        int u = texture & 15;
                        int v = texture >> 4 & 15;

                        if (block == WATER) addWaterSideToList(waterVerticesList);
                        else if (Block.isLeaveType(block)) addFoliageSideToList(foliageVerticesList, u, v);
                        else addSideToList(u, v, verticesList.get(side));
                    }
                }

        for (int side = 0; side < 6; side++) {
            ArrayList<Integer> sideVertices = verticesList.get(side);
            int[] vertices = new int[sideVertices.size()];
            for (int i = 0, size = sideVertices.size(); i < size; i++)
                vertices[i] = sideVertices.get(i);
            chunk.setVertices(vertices, side);
        }

        int[] waterVertices = new int[waterVerticesList.size()];
        for (int i = 0, size = waterVerticesList.size(); i < size; i++)
            waterVertices[i] = waterVerticesList.get(i);
        chunk.setWaterVertices(waterVertices);

        int[] foliageVertices = new int[foliageVerticesList.size()];
        for (int i = 0, size = foliageVerticesList.size(); i < size; i++)
            foliageVertices[i] = foliageVerticesList.get(i);
        chunk.setFoliageVertices(foliageVertices);
    }


    public void addSideToList(int u, int v, ArrayList<Integer> list) {
        byte[] blockXYZSubData = Block.getXYZSubData(block);
        this.list = list;

        switch (side) {
            case FRONT:
                for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                    addVertexToList(blockX + 1, blockY + 1, blockZ + 1, u, v,  0, aabbIndex);
                    addVertexToList(blockX, blockY + 1, blockZ + 1, u + 1, v,  1, aabbIndex);
                    addVertexToList(blockX + 1, blockY, blockZ + 1, u, v + 1,  2, aabbIndex);
                    addVertexToList(blockX, blockY, blockZ + 1, u + 1, v + 1,  3, aabbIndex);
                }
                break;
            case TOP:
                for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                    addVertexToList(blockX, blockY + 1, blockZ, u, v,  0, aabbIndex);
                    addVertexToList(blockX, blockY + 1, blockZ + 1, u + 1, v,  1, aabbIndex);
                    addVertexToList(blockX + 1, blockY + 1, blockZ, u, v + 1,  2, aabbIndex);
                    addVertexToList(blockX + 1, blockY + 1, blockZ + 1, u + 1, v + 1,  3, aabbIndex);
                }
                break;
            case RIGHT:
                for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                    addVertexToList(blockX + 1, blockY + 1, blockZ, u, v,  0, aabbIndex);
                    addVertexToList(blockX + 1, blockY + 1, blockZ + 1, u + 1, v,  1, aabbIndex);
                    addVertexToList(blockX + 1, blockY, blockZ, u, v + 1,  2, aabbIndex);
                    addVertexToList(blockX + 1, blockY, blockZ + 1, u + 1, v + 1,  3, aabbIndex);
                }
                break;
            case BACK:
                for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                    addVertexToList(blockX, blockY + 1, blockZ, u, v,  0, aabbIndex);
                    addVertexToList(blockX + 1, blockY + 1, blockZ, u + 1, v,  1, aabbIndex);
                    addVertexToList(blockX, blockY, blockZ, u, v + 1,  2, aabbIndex);
                    addVertexToList(blockX + 1, blockY, blockZ, u + 1, v + 1,  3, aabbIndex);
                }
                break;
            case BOTTOM:
                for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                    addVertexToList(blockX + 1, blockY, blockZ + 1, u, v,  3, aabbIndex);
                    addVertexToList(blockX, blockY, blockZ + 1, u + 1, v,  1, aabbIndex);
                    addVertexToList(blockX + 1, blockY, blockZ, u, v + 1,  2, aabbIndex);
                    addVertexToList(blockX, blockY, blockZ, u + 1, v + 1,  0, aabbIndex);
                }
                break;
            case LEFT:
                for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                    addVertexToList(blockX, blockY + 1, blockZ + 1, u, v,  1, aabbIndex);
                    addVertexToList(blockX, blockY + 1, blockZ, u + 1, v,  0, aabbIndex);
                    addVertexToList(blockX, blockY, blockZ + 1, u, v + 1,  3, aabbIndex);
                    addVertexToList(blockX, blockY, blockZ, u + 1, v + 1,  2, aabbIndex);
                }
                break;
        }
    }

    public void addVertexToList(int inChunkX, int inChunkY, int inChunkZ, int u, int v, int corner, int subDataAddend) {
        int x = worldCoordinate.x + inChunkX;
        int y = worldCoordinate.y + inChunkY;
        int z = worldCoordinate.z + inChunkZ;

        int skyLight = Chunk.getVertexSkyLightInWorld(x, y, z);
        int blockLight = Chunk.getVertexBlockLightInWorld(x, y, z);

        if ((Block.getBlockTypeData(block) & DYNAMIC_SHAPE_MASK) != 0) {
            addVertexToListDynamic(inChunkX, inChunkY, inChunkZ, u, v, skyLight, blockLight,  corner);
            return;
        }

        int blockType = Block.getBlockType(block);
        int subX = Block.getSubX(blockType, side, corner, subDataAddend);
        int subY = Block.getSubY(blockType, side, corner, subDataAddend);
        int subZ = Block.getSubZ(blockType, side, corner, subDataAddend);
        int subU = Block.getSubU(blockType, side, corner, subDataAddend / 6);
        int subV = Block.getSubV(blockType, side, corner, subDataAddend / 6);

        int ambientOcclusionLevel = getAmbientOcclusionLevel(inChunkX, inChunkY, inChunkZ, subX, subY, subZ);
        list.add(packData1(ambientOcclusionLevel, (inChunkX << 4) + subX + 15, (inChunkY << 4) + subY + 15, (inChunkZ << 4) + subZ + 15));
        list.add(packData2(skyLight, blockLight, (u << 4) + subU + 15, (v << 4) + subV + 15));
    }

    public void addVertexToListDynamic(int inChunkX, int inChunkY, int inChunkZ, int u, int v, int skyLight, int blockLight, int corner) {
        int subX = 0;
        int subY = 0;
        int subZ = 0;
        int subU = 0;
        int subV = 0;

        if (Block.getBlockType(block) == LIQUID_TYPE) {
            if (side == TOP) {
                subY = -2;
            } else if (side != BOTTOM) {
                short blockAbove = chunk.getBlock(blockX, blockY + 1, blockZ);
                if ((corner == 0 || corner == 1) && blockAbove != block && Block.getBlockTypeOcclusionData(blockAbove, BOTTOM) == 0) {
                    subY = -2;
                    subV = 2;
                } else if (corner == 2 || corner == 3) {
                    int[] normal = Block.NORMALS[side];
                    short adjacentBlock = chunk.getBlock(blockX + normal[0], blockY, blockZ + normal[2]);
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
        int ambientOcclusionLevel = getAmbientOcclusionLevel(inChunkX, inChunkY, inChunkZ, subX, subY, subZ);
        list.add(packData1(ambientOcclusionLevel, (inChunkX << 4) + subX + 15, (inChunkY << 4) + subY + 15, (inChunkZ << 4) + subZ + 15));
        list.add(packData2(skyLight, blockLight, (u << 4) + subU + 15, (v << 4) + subV + 15));
    }


    public void addWaterSideToList(ArrayList<Integer> list) {
        this.list = list;
        int u = WATER_TEXTURE - 1 & 15;
        int v = WATER_TEXTURE - 1 >> 4 & 15;

        switch (side) {
            case FRONT:
                addWaterVertexToList(blockX + 1, blockY + 1, blockZ + 1, u, v, 0);
                addWaterVertexToList(blockX, blockY + 1, blockZ + 1, u + 1, v, 1);
                addWaterVertexToList(blockX + 1, blockY, blockZ + 1, u, v + 1, 2);
                addWaterVertexToList(blockX, blockY, blockZ + 1, u + 1, v + 1, 3);
                break;
            case TOP:
                addWaterVertexToList(blockX, blockY + 1, blockZ, u, v, 0);
                addWaterVertexToList(blockX, blockY + 1, blockZ + 1, u + 1, v, 1);
                addWaterVertexToList(blockX + 1, blockY + 1, blockZ, u, v + 1, 2);
                addWaterVertexToList(blockX + 1, blockY + 1, blockZ + 1, u + 1, v + 1, 3);
                break;
            case RIGHT:
                addWaterVertexToList(blockX + 1, blockY + 1, blockZ, u, v, 0);
                addWaterVertexToList(blockX + 1, blockY + 1, blockZ + 1, u + 1, v, 1);
                addWaterVertexToList(blockX + 1, blockY, blockZ, u, v + 1, 2);
                addWaterVertexToList(blockX + 1, blockY, blockZ + 1, u + 1, v + 1, 3);
                break;
            case BACK:
                addWaterVertexToList(blockX, blockY + 1, blockZ, u, v, 0);
                addWaterVertexToList(blockX + 1, blockY + 1, blockZ, u + 1, v, 1);
                addWaterVertexToList(blockX, blockY, blockZ, u, v + 1, 2);
                addWaterVertexToList(blockX + 1, blockY, blockZ, u + 1, v + 1, 3);
                break;
            case BOTTOM:
                addWaterVertexToList(blockX + 1, blockY, blockZ + 1, u, v, 3);
                addWaterVertexToList(blockX, blockY, blockZ + 1, u + 1, v, 1);
                addWaterVertexToList(blockX + 1, blockY, blockZ, u, v + 1, 2);
                addWaterVertexToList(blockX, blockY, blockZ, u + 1, v + 1, 0);
                break;
            case LEFT:
                addWaterVertexToList(blockX, blockY + 1, blockZ + 1, u, v, 1);
                addWaterVertexToList(blockX, blockY + 1, blockZ, u + 1, v, 0);
                addWaterVertexToList(blockX, blockY, blockZ + 1, u, v + 1, 3);
                addWaterVertexToList(blockX, blockY, blockZ, u + 1, v + 1, 2);
                break;
        }
    }

    public void addWaterVertexToList(int inChunkX, int inChunkY, int inChunkZ, int u, int v, int corner) {
        int x = worldCoordinate.x + inChunkX;
        int y = worldCoordinate.y + inChunkY;
        int z = worldCoordinate.z + inChunkZ;

        int skyLight = Chunk.getVertexSkyLightInWorld(x, y, z);
        int blockLight = Chunk.getVertexBlockLightInWorld(x, y, z);

        int subX = 0;
        int subY = 0;
        int subZ = 0;
        int subU = 0;
        int subV = 0;

        boolean shouldSimulateWaves;

        if (side == TOP) {
            subY = -2;
            shouldSimulateWaves = true;
        } else if (side != BOTTOM) {
            short blockAbove = chunk.getBlock(blockX, blockY + 1, blockZ);
            shouldSimulateWaves = corner == 1 || corner == 0;
            if ((corner == 0 || corner == 1) && blockAbove != WATER && Block.getBlockTypeOcclusionData(blockAbove, BOTTOM) == 0) {
                subY = -2;
                subV = 2;
            } else if (corner == 2 || corner == 3) {
                int[] normal = Block.NORMALS[side];
                short adjacentBlock = chunk.getBlock(blockX + normal[0], blockY, blockZ + normal[2]);
                if (adjacentBlock == WATER && (blockAbove == WATER || Block.getBlockTypeOcclusionData(blockAbove, BOTTOM) != 0)) {
                    subY = 14;
                    subV = -14;
                }
            }
        } else shouldSimulateWaves = true;

        short blockBelow = chunk.getBlock(blockX, blockY - 1, blockZ);
        shouldSimulateWaves = shouldSimulateWaves || blockBelow == WATER || Block.getBlockTypeOcclusionData(blockBelow, TOP) == 0;

        if (inChunkX == 0 || inChunkX == CHUNK_SIZE || inChunkZ == 0 || inChunkZ == CHUNK_SIZE)
            shouldSimulateWaves = false;

        int ambientOcclusionLevel = getAmbientOcclusionLevel(inChunkX, inChunkY, inChunkZ, subX, subY, subZ);
        list.add(packData1(ambientOcclusionLevel, (inChunkX << 4) + subX + 15, (inChunkY << 4) + subY + 15, (inChunkZ << 4) + subZ + 15));
        list.add(packWaterData(shouldSimulateWaves ? 1 : 0, skyLight, blockLight, (u << 4) + subU + 15, (v << 4) + subV + 15));
    }


    public void addFoliageSideToList(ArrayList<Integer> list, int u, int v) {
        this.list = list;
        byte[] blockXYZSubData = Block.getXYZSubData(block);

        switch (side) {
            case FRONT:
                for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                    addFoliageVertexToList(blockX + 1, blockY + 1, blockZ + 1, u, v, 0,  aabbIndex);
                    addFoliageVertexToList(blockX, blockY + 1, blockZ + 1, u + 1, v, 1,  aabbIndex);
                    addFoliageVertexToList(blockX + 1, blockY, blockZ + 1, u, v + 1, 2,  aabbIndex);
                    addFoliageVertexToList(blockX, blockY, blockZ + 1, u + 1, v + 1, 3,  aabbIndex);
                }
                break;
            case TOP:
                for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                    addFoliageVertexToList(blockX, blockY + 1, blockZ, u, v, 0,  aabbIndex);
                    addFoliageVertexToList(blockX, blockY + 1, blockZ + 1, u + 1, v, 1,  aabbIndex);
                    addFoliageVertexToList(blockX + 1, blockY + 1, blockZ, u, v + 1, 2,  aabbIndex);
                    addFoliageVertexToList(blockX + 1, blockY + 1, blockZ + 1, u + 1, v + 1, 3,  aabbIndex);
                }
                break;
            case RIGHT:
                for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                    addFoliageVertexToList(blockX + 1, blockY + 1, blockZ, u, v, 0,  aabbIndex);
                    addFoliageVertexToList(blockX + 1, blockY + 1, blockZ + 1, u + 1, v, 1,  aabbIndex);
                    addFoliageVertexToList(blockX + 1, blockY, blockZ, u, v + 1, 2,  aabbIndex);
                    addFoliageVertexToList(blockX + 1, blockY, blockZ + 1, u + 1, v + 1, 3,  aabbIndex);
                }
                break;
            case BACK:
                for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                    addFoliageVertexToList(blockX, blockY + 1, blockZ, u, v, 0,  aabbIndex);
                    addFoliageVertexToList(blockX + 1, blockY + 1, blockZ, u + 1, v, 1,  aabbIndex);
                    addFoliageVertexToList(blockX, blockY, blockZ, u, v + 1, 2,  aabbIndex);
                    addFoliageVertexToList(blockX + 1, blockY, blockZ, u + 1, v + 1, 3,  aabbIndex);
                }
                break;
            case BOTTOM:
                for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                    addFoliageVertexToList(blockX + 1, blockY, blockZ + 1, u, v, 3,  aabbIndex);
                    addFoliageVertexToList(blockX, blockY, blockZ + 1, u + 1, v, 1,  aabbIndex);
                    addFoliageVertexToList(blockX + 1, blockY, blockZ, u, v + 1, 2,  aabbIndex);
                    addFoliageVertexToList(blockX, blockY, blockZ, u + 1, v + 1, 0,  aabbIndex);
                }
                break;
            case LEFT:
                for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                    addFoliageVertexToList(blockX, blockY + 1, blockZ + 1, u, v, 1,  aabbIndex);
                    addFoliageVertexToList(blockX, blockY + 1, blockZ, u + 1, v, 0,  aabbIndex);
                    addFoliageVertexToList(blockX, blockY, blockZ + 1, u, v + 1, 3,  aabbIndex);
                    addFoliageVertexToList(blockX, blockY, blockZ, u + 1, v + 1, 2,  aabbIndex);
                }
                break;
        }
    }

    public void addFoliageVertexToList(int inChunkX, int inChunkY, int inChunkZ, int u, int v, int corner, int subDataAddend) {
        int x = worldCoordinate.x + inChunkX;
        int y = worldCoordinate.y + inChunkY;
        int z = worldCoordinate.z + inChunkZ;

        int skyLight = Chunk.getVertexSkyLightInWorld(x, y, z);
        int blockLight = Chunk.getVertexBlockLightInWorld(x, y, z);

        if ((Block.getBlockTypeData(block) & DYNAMIC_SHAPE_MASK) != 0) {
            addVertexToListDynamic(inChunkX, inChunkY, inChunkZ, u, v, skyLight, blockLight,  corner);
            return;
        }

        int blockType = Block.getBlockType(block);
        int subX = Block.getSubX(blockType, side, corner, subDataAddend);
        int subY = Block.getSubY(blockType, side, corner, subDataAddend);
        int subZ = Block.getSubZ(blockType, side, corner, subDataAddend);
        int subU = Block.getSubU(blockType, side, corner, subDataAddend / 6);
        int subV = Block.getSubV(blockType, side, corner, subDataAddend / 6);

        int ambientOcclusionLevel = getAmbientOcclusionLevel(inChunkX, inChunkY, inChunkZ, subX, subY, subZ);
        list.add(packData1(ambientOcclusionLevel, (inChunkX << 4) + subX + 15, (inChunkY << 4) + subY + 15, (inChunkZ << 4) + subZ + 15));
        list.add(packData2(skyLight, blockLight, (u << 4) + subU + 15, (v << 4) + subV + 15));
    }


    public int packData1(int ambientOcclusionLevel, int inChunkX, int inChunkY, int inChunkZ) {
        return ambientOcclusionLevel << 30 | inChunkX << 20 | inChunkY << 10 | inChunkZ;
    }

    public int packData2(int skyLight, int blockLight, int u, int v) {
        return side << 26 | skyLight << 22 | blockLight << 18 | u << 9 | v;
    }

    public int packWaterData(int waveMultiplier, int skyLight, int blockLight, int u, int v) {
        return waveMultiplier << 29 | side << 26 | skyLight << 22 | blockLight << 18 | u << 9 | v;
    }

    public int getAmbientOcclusionLevel(int inChunkX, int inChunkY, int inChunkZ, int subX, int subY, int subZ) {
        int level = 0;
        int x = worldCoordinate.x + inChunkX;
        int y = worldCoordinate.y + inChunkY;
        int z = worldCoordinate.z + inChunkZ;
        int startX = 0, startY = 0, startZ = 0;
        short block = chunk.getSaveBlock(blockX, blockY, blockZ);

        switch (side) {
            case FRONT -> {
                startX = subX == 0 ? x - 1 : subX < 0 ? --x : x;
                startY = subY == 0 ? y - 1 : subY < 0 ? --y : y;
                startZ = subZ != 0 ? --z : z;
            }
            case TOP -> {
                startX = subX == 0 ? x - 1 : subX < 0 ? --x : x;
                startY = subY != 0 ? --y : y;
                startZ = subZ == 0 ? z - 1 : subZ < 0 ? --z : z;
            }
            case RIGHT -> {
                startX = subX != 0 ? --x : x;
                startY = subY == 0 ? y - 1 : subY < 0 ? --y : y;
                startZ = subZ == 0 ? z - 1 : subZ < 0 ? --z : z;
            }
            case BACK -> {
                startX = subX == 0 ? x - 1 : subX < 0 ? --x : x;
                startY = subY == 0 ? y - 1 : subY < 0 ? --y : y;
                startZ = subZ != 0 ? z : --z;
            }
            case BOTTOM -> {
                startX = subX == 0 ? x - 1 : subX < 0 ? --x : x;
                startY = subY != 0 ? y : --y;
                startZ = subZ == 0 ? z - 1 : subZ < 0 ? --z : z;
            }
            case LEFT -> {
                startX = subX != 0 ? x : --x;
                startY = subY == 0 ? y - 1 : subY < 0 ? --y : y;
                startZ = subZ == 0 ? z - 1 : subZ < 0 ? --z : z;
            }
        }

        for (int totalX = startX; totalX <= x; totalX++)
            for (int totalZ = startZ; totalZ <= z; totalZ++)
                for (int totalY = startY; totalY <= y; totalY++) {
                    if (totalX == (worldCoordinate.x | blockX) && totalY == (worldCoordinate.y | blockY) && totalZ == (worldCoordinate.z | blockZ))
                        continue;
                    if (Block.hasAmbientOcclusion(Chunk.getBlockInWorld(totalX, totalY, totalZ), block)) level++;
                }

        return Math.min(3, level);
    }

}