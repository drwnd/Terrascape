package terrascape.generation;

import terrascape.entity.OpaqueModel;
import terrascape.server.Block;
import terrascape.dataStorage.Chunk;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Random;

import static terrascape.utils.Constants.*;

public final class MeshGenerator {

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
        worldCoordinate = chunk.getWorldCoordinate();
    }

    public void generateMesh() {
        chunk.setMeshed(true);

        chunk.generateSurroundingChunks();
        if (chunk.getLightLength() != 1) chunk.optimizeLightStorage();
        if (chunk.getBlockLength() != 1) chunk.optimizeBlockStorage();
        chunk.generateOcclusionCullingData();

        if (chunk.getBlockLength() == 1 && chunk.getSaveBlock(0) == AIR) return;

        @SuppressWarnings("unchecked") ArrayList<Integer>[] vertexLists = new ArrayList[OpaqueModel.FACE_TYPE_COUNT];
        for (int index = 0; index < vertexLists.length; index++) vertexLists[index] = new ArrayList<>();


        ArrayList<Integer> waterVerticesList = new ArrayList<>();

        for (blockX = 0; blockX < CHUNK_SIZE; blockX++)
            for (blockZ = 0; blockZ < CHUNK_SIZE; blockZ++)
                for (blockY = 0; blockY < CHUNK_SIZE; blockY++) {

                    block = chunk.getSaveBlock(blockX, blockY, blockZ);
                    properties = Block.getBlockProperties(block);

                    int blockType = Block.getBlockType(block);
                    if (blockType == AIR_TYPE) continue;

                    if (blockType == FLOWER_TYPE) {
                        list = vertexLists[OpaqueModel.DECORATION_FACES_INDEX];
                        addFlowerToList();
                        continue;
                    }
                    if (blockType == VINE_TYPE) {
                        list = vertexLists[OpaqueModel.DECORATION_FACES_INDEX];
                        addVineToList();
                        continue;
                    }

                    int faceCount = Block.getFaceCount(blockType);

                    if (!Block.isWaterBlock(block)) addOpaqueBlock(faceCount, vertexLists);
                    if (Block.isWaterLogged(block)) addWaterBlock(waterVerticesList);
                }

        int[] waterVertices = new int[waterVerticesList.size()];
        for (int i = 0, size = waterVerticesList.size(); i < size; i++)
            waterVertices[i] = waterVerticesList.get(i);
        chunk.setWaterVertices(waterVertices);

        int totalVertexCount = 0, verticesIndex = 0;
        for (ArrayList<Integer> vertexList : vertexLists) totalVertexCount += vertexList.size();
        int[] vertexCounts = new int[vertexLists.length];
        int[] opaqueVertices = new int[totalVertexCount];

        for (int index = 0; index < vertexLists.length; index++) {
            ArrayList<Integer> vertexList = vertexLists[index];
            vertexCounts[index] = (int) (vertexList.size() * 0.75);
            for (int vertex : vertexList) opaqueVertices[verticesIndex++] = vertex;
        }

        chunk.setOpaqueVertices(opaqueVertices);
        chunk.setVertexCounts(vertexCounts);
    }

    private void addOpaqueBlock(int faceCount, ArrayList<Integer>[] vertexLists) {
        for (aabbIndex = 0; aabbIndex < faceCount; aabbIndex += 6)
            for (side = 0; side < 6; side++) {
                byte[] normal = Block.NORMALS[side];
                short occludingBlock = chunk.getBlock(blockX + normal[0], blockY + normal[1], blockZ + normal[2]);
                if (occludesOpaque(block, occludingBlock, side, aabbIndex, worldCoordinate.x | blockX, worldCoordinate.y | blockY, worldCoordinate.z | blockZ))
                    continue;

                int texture = Block.getTextureIndex(block, side);

                int u = texture & 15;
                int v = texture >> 4 & 15;

                if (Block.isLeaveType(block))
                    addFoliageSideToList(vertexLists[OpaqueModel.FOLIAGE_FACES_OFFSET + side], u, v);
                else if ((properties & HAS_ASKEW_FACES) != 0)
                    addSideToList(u, v, vertexLists[OpaqueModel.ASKEW_FACES_INDEX]);
                else addSideToList(u, v, vertexLists[side]);
            }
    }

    private void addWaterBlock(ArrayList<Integer> waterVerticesList) {
        for (side = 0; side < 6; side++) {
            if (!Block.isWaterBlock(block) && Block.getBlockOcclusionData(block, side) == -1L) continue;

            byte[] normal = Block.NORMALS[side];
            short occludingBlock = chunk.getBlock(blockX + normal[0], blockY + normal[1], blockZ + normal[2]);

            if (occludesWater(block, occludingBlock, side, worldCoordinate.x | blockX, worldCoordinate.y | blockY, worldCoordinate.z | blockZ))
                continue;

            addWaterSideToList(waterVerticesList);
        }
    }


    private void addSideToList(int u, int v, ArrayList<Integer> list) {
        this.list = list;
        int adder1, adder2;

        switch (side) {
            case NORTH:
                adder1 = (properties & ROTATE_NORTH_TEXTURE) == 0 ? 1 : 0;
                adder2 = 1 - adder1;
                addVertexToList(blockX + 1, blockY + 1, blockZ + 1, u + adder1, v + adder2, 0);
                addVertexToList(blockX, blockY + 1, blockZ + 1, u, v, 1);
                addVertexToList(blockX + 1, blockY, blockZ + 1, u + 1, v + 1, 2);
                addVertexToList(blockX, blockY, blockZ + 1, u + adder2, v + adder1, 3);
                break;
            case TOP:
                adder1 = (properties & ROTATE_TOP_TEXTURE) == 0 ? 1 : 0;
                adder2 = 1 - adder1;
                addVertexToList(blockX, blockY + 1, blockZ, u + adder1, v + adder2, 0);
                addVertexToList(blockX, blockY + 1, blockZ + 1, u, v, 1);
                addVertexToList(blockX + 1, blockY + 1, blockZ, u + 1, v + 1, 2);
                addVertexToList(blockX + 1, blockY + 1, blockZ + 1, u + adder2, v + adder1, 3);
                break;
            case WEST:
                adder1 = (properties & ROTATE_WEST_TEXTURE) == 0 ? 1 : 0;
                adder2 = 1 - adder1;
                addVertexToList(blockX + 1, blockY + 1, blockZ, u + adder1, v + adder2, 0);
                addVertexToList(blockX + 1, blockY + 1, blockZ + 1, u, v, 1);
                addVertexToList(blockX + 1, blockY, blockZ, u + 1, v + 1, 2);
                addVertexToList(blockX + 1, blockY, blockZ + 1, u + adder2, v + adder1, 3);
                break;
            case SOUTH:
                adder1 = (properties & ROTATE_SOUTH_TEXTURE) == 0 ? 1 : 0;
                adder2 = 1 - adder1;
                addVertexToList(blockX, blockY + 1, blockZ, u + adder1, v + adder2, 0);
                addVertexToList(blockX + 1, blockY + 1, blockZ, u, v, 1);
                addVertexToList(blockX, blockY, blockZ, u + 1, v + 1, 2);
                addVertexToList(blockX + 1, blockY, blockZ, u + adder2, v + adder1, 3);
                break;
            case BOTTOM:
                adder1 = (properties & ROTATE_BOTTOM_TEXTURE) == 0 ? 1 : 0;
                adder2 = 1 - adder1;
                addVertexToList(blockX + 1, blockY, blockZ + 1, u + adder1, v + adder2, 3);
                addVertexToList(blockX, blockY, blockZ + 1, u, v, 1);
                addVertexToList(blockX + 1, blockY, blockZ, u + 1, v + 1, 2);
                addVertexToList(blockX, blockY, blockZ, u + adder2, v + adder1, 0);
                break;
            case EAST:
                adder1 = (properties & ROTATE_EAST_TEXTURE) == 0 ? 1 : 0;
                adder2 = 1 - adder1;
                addVertexToList(blockX, blockY + 1, blockZ + 1, u + adder1, v + adder2, 1);
                addVertexToList(blockX, blockY + 1, blockZ, u, v, 0);
                addVertexToList(blockX, blockY, blockZ + 1, u + 1, v + 1, 3);
                addVertexToList(blockX, blockY, blockZ, u + adder2, v + adder1, 2);
                break;
        }
    }

    private void addVertexToList(int inChunkX, int inChunkY, int inChunkZ, int u, int v, int corner) {
        int x = worldCoordinate.x + inChunkX;
        int y = worldCoordinate.y + inChunkY;
        int z = worldCoordinate.z + inChunkZ;

        int blockType = Block.getBlockType(block);
        int subX = Block.getSubX(blockType, side, corner, aabbIndex);
        int subY = Block.getSubY(blockType, side, corner, aabbIndex);
        int subZ = Block.getSubZ(blockType, side, corner, aabbIndex);

        int skyLight = getVertexSkyLightInWorld(x, y, z, subX, subY, subZ);
        int blockLight = getVertexBlockLightInWorld(x, y, z, subX, subY, subZ);

        if ((Block.getBlockTypeData(block) & DYNAMIC_SHAPE_MASK) != 0) {
            addVertexToListDynamic(inChunkX, inChunkY, inChunkZ, u, v, skyLight, blockLight, corner);
            return;
        }

        int subU;
        int subV;
        if ((properties & 1 << side + 11) == 0) {
            subU = Block.getSubU(blockType, side, corner, aabbIndex);
            subV = Block.getSubV(blockType, side, corner, aabbIndex);
        } else {
            subV = Block.getSubU(blockType, side, corner, aabbIndex);
            subU = Block.getSubV(blockType, side, corner, aabbIndex);
        }

        int ambientOcclusionLevel = getAmbientOcclusionLevel(inChunkX, inChunkY, inChunkZ, subX, subY, subZ);
        list.add(packData1(ambientOcclusionLevel, (inChunkX << 4) + subX + 15, (inChunkY << 4) + subY + 15, (inChunkZ << 4) + subZ + 15));
        list.add(packData2(skyLight, blockLight, (u << 4) + subU + 15, (v << 4) + subV + 15));
    }

    private void addVertexToListDynamic(int inChunkX, int inChunkY, int inChunkZ, int u, int v, int skyLight, int blockLight, int corner) {
        int x = worldCoordinate.x + inChunkX;
        int z = worldCoordinate.z + inChunkZ;

        int subX = 0;
        int subY = 0;
        int subZ = 0;
        int subU = 0;
        int subV = 0;

        if (Block.isLavaBlock(block)) {
            short blockAbove = chunk.getBlock(blockX, blockY + 1, blockZ);
            if (side == TOP) {
                if (Block.getBlockOcclusionData(blockAbove, BOTTOM) != -1L || block != LAVA_SOURCE) {
                    subY = getVertexLavaLevelInWorld(x, blockY + worldCoordinate.y, z) - 16;
                }
            } else if (side != BOTTOM) {
                if ((corner == 0 || corner == 1)) { // Top corners
                    if (!Block.isLavaBlock(blockAbove) && (Block.getBlockOcclusionData(blockAbove, BOTTOM) != -1L || block != LAVA_SOURCE)) {
                        int lavaLevel = getVertexLavaLevelInWorld(x, blockY + worldCoordinate.y, z);
                        subY = lavaLevel - 16;
                        subV = -lavaLevel + 16;
                    }
                } else { // Bottom corners
                    byte[] normal = Block.NORMALS[side];
                    short adjacentBlock = chunk.getBlock(blockX + normal[0], blockY, blockZ + normal[2]);

                    if (Block.isLavaBlock(adjacentBlock) && (Block.isLavaBlock(blockAbove) || Block.getBlockOcclusionData(blockAbove, BOTTOM) == -1L)) {
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
                case NORTH, SOUTH -> subZ = Block.getSubZ(blockType, side, corner, 0);

                case WEST, EAST -> subX = Block.getSubX(blockType, side, corner, 0);
            }
        }

        int ambientOcclusionLevel = getAmbientOcclusionLevel(inChunkX, inChunkY, inChunkZ, subX, subY, subZ);
        list.add(packData1(ambientOcclusionLevel, (inChunkX << 4) + subX + 15, (inChunkY << 4) + subY + 15, (inChunkZ << 4) + subZ + 15));
        list.add(packData2(skyLight, blockLight, (u << 4) + subU + 15, (v << 4) + subV + 15));
    }


    private void addWaterSideToList(ArrayList<Integer> list) {
        this.list = list;
        int u = (byte) 64 & 15;
        int v = (byte) 64 >> 4 & 15;

        switch (side) {
            case NORTH:
                addWaterVertexToList(blockX + 1, blockY + 1, blockZ + 1, u + 1, v, 0, Block.SIDES_WITH_CORNER[1]);
                addWaterVertexToList(blockX, blockY + 1, blockZ + 1, u, v, 1, Block.SIDES_WITH_CORNER[0]);
                addWaterVertexToList(blockX + 1, blockY, blockZ + 1, u + 1, v + 1, 2, Block.SIDES_WITH_CORNER[5]);
                addWaterVertexToList(blockX, blockY, blockZ + 1, u, v + 1, 3, Block.SIDES_WITH_CORNER[4]);
                break;
            case TOP:
                addWaterVertexToList(blockX, blockY + 1, blockZ, u + 1, v, 0, Block.SIDES_WITH_CORNER[2]);
                addWaterVertexToList(blockX, blockY + 1, blockZ + 1, u, v, 1, Block.SIDES_WITH_CORNER[0]);
                addWaterVertexToList(blockX + 1, blockY + 1, blockZ, u + 1, v + 1, 2, Block.SIDES_WITH_CORNER[3]);
                addWaterVertexToList(blockX + 1, blockY + 1, blockZ + 1, u, v + 1, 3, Block.SIDES_WITH_CORNER[1]);
                break;
            case WEST:
                addWaterVertexToList(blockX + 1, blockY + 1, blockZ, u + 1, v, 0, Block.SIDES_WITH_CORNER[3]);
                addWaterVertexToList(blockX + 1, blockY + 1, blockZ + 1, u, v, 1, Block.SIDES_WITH_CORNER[1]);
                addWaterVertexToList(blockX + 1, blockY, blockZ, u + 1, v + 1, 2, Block.SIDES_WITH_CORNER[7]);
                addWaterVertexToList(blockX + 1, blockY, blockZ + 1, u, v + 1, 3, Block.SIDES_WITH_CORNER[5]);
                break;
            case SOUTH:
                addWaterVertexToList(blockX, blockY + 1, blockZ, u + 1, v, 0, Block.SIDES_WITH_CORNER[2]);
                addWaterVertexToList(blockX + 1, blockY + 1, blockZ, u, v, 1, Block.SIDES_WITH_CORNER[3]);
                addWaterVertexToList(blockX, blockY, blockZ, u + 1, v + 1, 2, Block.SIDES_WITH_CORNER[6]);
                addWaterVertexToList(blockX + 1, blockY, blockZ, u, v + 1, 3, Block.SIDES_WITH_CORNER[7]);
                break;
            case BOTTOM:
                addWaterVertexToList(blockX + 1, blockY, blockZ + 1, u + 1, v, 3, Block.SIDES_WITH_CORNER[5]);
                addWaterVertexToList(blockX, blockY, blockZ + 1, u, v, 1, Block.SIDES_WITH_CORNER[4]);
                addWaterVertexToList(blockX + 1, blockY, blockZ, u + 1, v + 1, 2, Block.SIDES_WITH_CORNER[7]);
                addWaterVertexToList(blockX, blockY, blockZ, u, v + 1, 0, Block.SIDES_WITH_CORNER[6]);
                break;
            case EAST:
                addWaterVertexToList(blockX, blockY + 1, blockZ + 1, u + 1, v, 1, Block.SIDES_WITH_CORNER[0]);
                addWaterVertexToList(blockX, blockY + 1, blockZ, u, v, 0, Block.SIDES_WITH_CORNER[2]);
                addWaterVertexToList(blockX, blockY, blockZ + 1, u + 1, v + 1, 3, Block.SIDES_WITH_CORNER[4]);
                addWaterVertexToList(blockX, blockY, blockZ, u, v + 1, 2, Block.SIDES_WITH_CORNER[6]);
                break;
        }
    }

    private void addWaterVertexToList(int inChunkX, int inChunkY, int inChunkZ, int u, int v, int corner, byte[] adjacentSides) {
        int x = worldCoordinate.x + inChunkX;
        int y = worldCoordinate.y + inChunkY;
        int z = worldCoordinate.z + inChunkZ;

        int subX = 0;
        int subY = 0;
        int subZ = 0;
        int subU = 0;
        int subV = 0;

        boolean shouldSimulateWaves = false;
        short blockAbove = chunk.getBlock(blockX, blockY + 1, blockZ);

        if (side == TOP) {
            if (Block.getBlockOcclusionData(blockAbove, BOTTOM) != -1L || !Block.isWaterSource(block)) {
                subY = getVertexWaterLevelInWorld(x, blockY + worldCoordinate.y, z) - 16;
                shouldSimulateWaves = true;
            }
        } else if (side != BOTTOM) {
            if ((corner == 0 || corner == 1)) { // Top corners
                if (Block.isWaterLogged(blockAbove)) shouldSimulateWaves = true;
                else if (Block.getBlockOcclusionData(blockAbove, BOTTOM) != -1L || !Block.isWaterSource(block)) {
                    shouldSimulateWaves = true;
                    int waterLevel = getVertexWaterLevelInWorld(x, blockY + worldCoordinate.y, z);
                    subY = waterLevel - 16;
                    subV = -waterLevel + 16;
                }
            } else { // Bottom corners
                byte[] normal = Block.NORMALS[side];
                short adjacentBlock = chunk.getBlock(blockX + normal[0], blockY, blockZ + normal[2]);
                short blockBelow = chunk.getBlock(blockX, blockY - 1, blockZ);

                if (Block.isWaterLogged(adjacentBlock) && (Block.isWaterLogged(blockAbove) || Block.getBlockOcclusionData(blockAbove, BOTTOM) == -1L)) {
                    subY = 14;
                    subV = -14;
                    shouldSimulateWaves = true;
                }
                if (Block.isWaterLogged(blockBelow) || Block.getBlockOcclusionData(blockBelow, TOP) != -1)
                    shouldSimulateWaves = true;
            }
        } else {
            short blockBelow = chunk.getBlock(blockX, blockY - 1, blockZ);
            shouldSimulateWaves = Block.getBlockOcclusionData(blockBelow, TOP) != -1L;
        }

        if (!Block.isWaterBlock(block) && Block.isWaterLogged(block))
            for (int side : adjacentSides) {
                if (Block.getBlockOcclusionData(block, side) != -1L) continue;
                shouldSimulateWaves = false;
                break;
            }

        if (inChunkX == 0 || inChunkX == CHUNK_SIZE || inChunkZ == 0 || inChunkZ == CHUNK_SIZE)
            shouldSimulateWaves = false;

        int skyLight = getVertexSkyLightInWorld(x, y, z, subX, subY, subZ);
        int blockLight = getVertexBlockLightInWorld(x, y, z, subX, subY, subZ);

        int ambientOcclusionLevel = getAmbientOcclusionLevel(inChunkX, inChunkY, inChunkZ, subX, subY, subZ);
        list.add(packData1(ambientOcclusionLevel, (inChunkX << 4) + subX + 15, (inChunkY << 4) + subY + 15, (inChunkZ << 4) + subZ + 15));
        list.add(packWaterData(shouldSimulateWaves ? 1 : 0, skyLight, blockLight, (u << 4) + subU + 15, (v << 4) + subV + 15));
    }


    private void addFoliageSideToList(ArrayList<Integer> list, int u, int v) {
        this.list = list;
        int adder1, adder2;

        switch (side) {
            case NORTH:
                adder1 = (properties & ROTATE_NORTH_TEXTURE) != 0 ? 0 : 1;
                adder2 = 1 - adder1;
                addFoliageVertexToList(blockX + 1, blockY + 1, blockZ + 1, u + adder1, v + adder2, 0);
                addFoliageVertexToList(blockX, blockY + 1, blockZ + 1, u, v, 1);
                addFoliageVertexToList(blockX + 1, blockY, blockZ + 1, u + 1, v + 1, 2);
                addFoliageVertexToList(blockX, blockY, blockZ + 1, u + adder2, v + adder1, 3);
                break;
            case TOP:
                adder1 = (properties & ROTATE_TOP_TEXTURE) != 0 ? 0 : 1;
                adder2 = 1 - adder1;
                addFoliageVertexToList(blockX, blockY + 1, blockZ, u + adder1, v + adder2, 0);
                addFoliageVertexToList(blockX, blockY + 1, blockZ + 1, u, v, 1);
                addFoliageVertexToList(blockX + 1, blockY + 1, blockZ, u + 1, v + 1, 2);
                addFoliageVertexToList(blockX + 1, blockY + 1, blockZ + 1, u + adder2, v + adder1, 3);
                break;
            case WEST:
                adder1 = (properties & ROTATE_WEST_TEXTURE) != 0 ? 0 : 1;
                adder2 = 1 - adder1;
                addFoliageVertexToList(blockX + 1, blockY + 1, blockZ, u + adder1, v + adder2, 0);
                addFoliageVertexToList(blockX + 1, blockY + 1, blockZ + 1, u, v, 1);
                addFoliageVertexToList(blockX + 1, blockY, blockZ, u + 1, v + 1, 2);
                addFoliageVertexToList(blockX + 1, blockY, blockZ + 1, u + adder2, v + adder1, 3);
                break;
            case SOUTH:
                adder1 = (properties & ROTATE_SOUTH_TEXTURE) != 0 ? 0 : 1;
                adder2 = 1 - adder1;
                addFoliageVertexToList(blockX, blockY + 1, blockZ, u + adder1, v + adder2, 0);
                addFoliageVertexToList(blockX + 1, blockY + 1, blockZ, u, v, 1);
                addFoliageVertexToList(blockX, blockY, blockZ, u + 1, v + 1, 2);
                addFoliageVertexToList(blockX + 1, blockY, blockZ, u + adder2, v + adder1, 3);
                break;
            case BOTTOM:
                adder1 = (properties & ROTATE_BOTTOM_TEXTURE) != 0 ? 0 : 1;
                adder2 = 1 - adder1;
                addFoliageVertexToList(blockX + 1, blockY, blockZ + 1, u + adder1, v + adder2, 3);
                addFoliageVertexToList(blockX, blockY, blockZ + 1, u, v, 1);
                addFoliageVertexToList(blockX + 1, blockY, blockZ, u + 1, v + 1, 2);
                addFoliageVertexToList(blockX, blockY, blockZ, u + adder2, v + adder1, 0);
                break;
            case EAST:
                adder1 = (properties & ROTATE_EAST_TEXTURE) != 0 ? 0 : 1;
                adder2 = 1 - adder1;
                addFoliageVertexToList(blockX, blockY + 1, blockZ + 1, u + adder1, v + adder2, 1);
                addFoliageVertexToList(blockX, blockY + 1, blockZ, u, v, 0);
                addFoliageVertexToList(blockX, blockY, blockZ + 1, u + 1, v + 1, 3);
                addFoliageVertexToList(blockX, blockY, blockZ, u + adder2, v + adder1, 2);
                break;
        }
    }

    private void addFoliageVertexToList(int inChunkX, int inChunkY, int inChunkZ, int u, int v, int corner) {
        int x = worldCoordinate.x + inChunkX;
        int y = worldCoordinate.y + inChunkY;
        int z = worldCoordinate.z + inChunkZ;

        int blockType = Block.getBlockType(block);
        int subX = Block.getSubX(blockType, side, corner, aabbIndex);
        int subY = Block.getSubY(blockType, side, corner, aabbIndex);
        int subZ = Block.getSubZ(blockType, side, corner, aabbIndex);

        int skyLight = getVertexSkyLightInWorld(x, y, z, subX, subY, subZ);
        int blockLight = getVertexBlockLightInWorld(x, y, z, subX, subY, subZ);

        if ((Block.getBlockTypeData(block) & DYNAMIC_SHAPE_MASK) != 0) {
            addVertexToListDynamic(inChunkX, inChunkY, inChunkZ, u, v, skyLight, blockLight, corner);
            return;
        }

        int subU;
        int subV;
        if ((properties & 1 << side + 11) == 0) {
            subU = Block.getSubU(blockType, side, corner, aabbIndex);
            subV = Block.getSubV(blockType, side, corner, aabbIndex);
        } else {
            subV = Block.getSubU(blockType, side, corner, aabbIndex);
            subU = Block.getSubV(blockType, side, corner, aabbIndex);
        }

        int ambientOcclusionLevel = getAmbientOcclusionLevel(inChunkX, inChunkY, inChunkZ, subX, subY, subZ);
        list.add(packData1(ambientOcclusionLevel, (inChunkX << 4) + subX + 15, (inChunkY << 4) + subY + 15, (inChunkZ << 4) + subZ + 15));
        list.add(packFoliageData(1, skyLight, blockLight, (u << 4) + subU + 15, (v << 4) + subV + 15));
    }

    private void addFlowerToList() {
        int texture = Block.getTextureIndex(block, side);

        int u = texture & 15;
        int v = texture >> 4 & 15;

        int x = worldCoordinate.x + blockX;
        int y = worldCoordinate.y + blockY;
        int z = worldCoordinate.z + blockZ;

        int skyLight = Chunk.getSkyLightInWorld(x, y, z);
        int blockLight = Chunk.getBlockLightInWorld(x, y, z);

        side = TOP;

        random.setSeed((long) (z * 5135.64843) << 24 | (long) (x * 18941.484138) << 5);
        int randomX = (int) (random.nextDouble() * 8 - 4);
        int randomZ = (int) (random.nextDouble() * 8 - 4);
        int bottomWindMultiplier = Block.getBlockOcclusionData(Chunk.getBlockInWorld(x, y - 1, z), TOP) == -1 ? 0 : 1;
        int topWindMultiplier = 1;

        list.add(packData1(0, (blockX << 4) + 3 + 15 + randomX, (blockY + 1 << 4) + 15, (blockZ << 4) + 3 + 15 + randomZ));
        list.add(packFoliageData(topWindMultiplier, skyLight, blockLight, (u << 4) + 15, (v << 4) + 15));
        list.add(packData1(0, (blockX << 4) + 3 + 15 + randomX, (blockY << 4) + 15, (blockZ << 4) + 3 + 15 + randomZ));
        list.add(packFoliageData(bottomWindMultiplier, skyLight, blockLight, (u << 4) + 15, (v + 1 << 4) + 15));
        list.add(packData1(0, (blockX << 4) + 13 + 15 + randomX, (blockY + 1 << 4) + 15, (blockZ << 4) + 13 + 15 + randomZ));
        list.add(packFoliageData(topWindMultiplier, skyLight, blockLight, (u + 1 << 4) + 15, (v << 4) + 15));
        list.add(packData1(0, (blockX << 4) + 13 + 15 + randomX, (blockY << 4) + 15, (blockZ << 4) + 13 + 15 + randomZ));
        list.add(packFoliageData(bottomWindMultiplier, skyLight, blockLight, (u + 1 << 4) + 15, (v + 1 << 4) + 15));

        list.add(packData1(0, (blockX << 4) + 3 + 15 + randomX, (blockY + 1 << 4) + 15, (blockZ << 4) + 13 + 15 + randomZ));
        list.add(packFoliageData(topWindMultiplier, skyLight, blockLight, (u << 4) + 15, (v << 4) + 15));
        list.add(packData1(0, (blockX << 4) + 3 + 15 + randomX, (blockY << 4) + 15, (blockZ << 4) + 13 + 15 + randomZ));
        list.add(packFoliageData(bottomWindMultiplier, skyLight, blockLight, (u << 4) + 15, (v + 1 << 4) + 15));
        list.add(packData1(0, (blockX << 4) + 13 + 15 + randomX, (blockY + 1 << 4) + 15, (blockZ << 4) + 3 + 15 + randomZ));
        list.add(packFoliageData(topWindMultiplier, skyLight, blockLight, (u + 1 << 4) + 15, (v << 4) + 15));
        list.add(packData1(0, (blockX << 4) + 13 + 15 + randomX, (blockY << 4) + 15, (blockZ << 4) + 3 + 15 + randomZ));
        list.add(packFoliageData(bottomWindMultiplier, skyLight, blockLight, (u + 1 << 4) + 15, (v + 1 << 4) + 15));
    }

    private void addVineToList() {
        int texture = Block.getTextureIndex(block, side);

        int u = texture & 15;
        int v = texture >> 4 & 15;

        int x = worldCoordinate.x + blockX;
        int y = worldCoordinate.y + blockY;
        int z = worldCoordinate.z + blockZ;

        int skyLight = Chunk.getSkyLightInWorld(x, y, z);
        int blockLight = Chunk.getBlockLightInWorld(x, y, z);

        if (Block.getBlockOcclusionData(chunk.getBlock(blockX, blockY - 1, blockZ), TOP) == -1L) {
            side = TOP;
            list.add(packData1(0, (blockX << 4) + 15, (blockY << 4) + 1 + 15, (blockZ << 4) + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u << 4) + 15, (v << 4) + 15));
            list.add(packData1(0, (blockX << 4) + 15, (blockY << 4) + 1 + 15, (blockZ + 1 << 4) + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u << 4) + 15, (v + 1 << 4) + 15));
            list.add(packData1(0, (blockX + 1 << 4) + 15, (blockY << 4) + 1 + 15, (blockZ << 4) + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u + 1 << 4) + 15, (v << 4) + 15));
            list.add(packData1(0, (blockX + 1 << 4) + 15, (blockY << 4) + 1 + 15, (blockZ + 1 << 4) + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u + 1 << 4) + 15, (v + 1 << 4) + 15));
        }
        if (Block.getBlockOcclusionData(chunk.getBlock(blockX, blockY + 1, blockZ), BOTTOM) == -1L) {
            side = BOTTOM;
            list.add(packData1(0, (blockX << 4) + 15, (blockY + 1 << 4) - 1 + 15, (blockZ << 4) + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u << 4) + 15, (v << 4) + 15));
            list.add(packData1(0, (blockX << 4) + 15, (blockY + 1 << 4) - 1 + 15, (blockZ + 1 << 4) + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u << 4) + 15, (v + 1 << 4) + 15));
            list.add(packData1(0, (blockX + 1 << 4) + 15, (blockY + 1 << 4) - 1 + 15, (blockZ << 4) + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u + 1 << 4) + 15, (v << 4) + 15));
            list.add(packData1(0, (blockX + 1 << 4) + 15, (blockY + 1 << 4) - 1 + 15, (blockZ + 1 << 4) + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u + 1 << 4) + 15, (v + 1 << 4) + 15));
        }
        if (Block.getBlockOcclusionData(chunk.getBlock(blockX - 1, blockY, blockZ), WEST) == -1L) {
            side = WEST;
            list.add(packData1(0, (blockX << 4) + 1 + 15, (blockY << 4) + 15, (blockZ << 4) + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u << 4) + 15, (v << 4) + 15));
            list.add(packData1(0, (blockX << 4) + 1 + 15, (blockY << 4) + 15, (blockZ + 1 << 4) + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u << 4) + 15, (v + 1 << 4) + 15));
            list.add(packData1(0, (blockX << 4) + 1 + 15, (blockY + 1 << 4) + 15, (blockZ << 4) + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u + 1 << 4) + 15, (v << 4) + 15));
            list.add(packData1(0, (blockX << 4) + 1 + 15, (blockY + 1 << 4) + 15, (blockZ + 1 << 4) + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u + 1 << 4) + 15, (v + 1 << 4) + 15));
        }
        if (Block.getBlockOcclusionData(chunk.getBlock(blockX + 1, blockY, blockZ), EAST) == -1L) {
            side = EAST;
            list.add(packData1(0, (blockX + 1 << 4) - 1 + 15, (blockY << 4) + 15, (blockZ << 4) + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u << 4) + 15, (v << 4) + 15));
            list.add(packData1(0, (blockX + 1 << 4) - 1 + 15, (blockY << 4) + 15, (blockZ + 1 << 4) + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u << 4) + 15, (v + 1 << 4) + 15));
            list.add(packData1(0, (blockX + 1 << 4) - 1 + 15, (blockY + 1 << 4) + 15, (blockZ << 4) + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u + 1 << 4) + 15, (v << 4) + 15));
            list.add(packData1(0, (blockX + 1 << 4) - 1 + 15, (blockY + 1 << 4) + 15, (blockZ + 1 << 4) + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u + 1 << 4) + 15, (v + 1 << 4) + 15));
        }
        if (Block.getBlockOcclusionData(chunk.getBlock(blockX, blockY, blockZ - 1), NORTH) == -1L) {
            side = NORTH;
            list.add(packData1(0, (blockX << 4) + 15, (blockY << 4) + 15, (blockZ << 4) + 1 + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u << 4) + 15, (v << 4) + 15));
            list.add(packData1(0, (blockX << 4) + 15, (blockY + 1 << 4) + 15, (blockZ << 4) + 1 + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u << 4) + 15, (v + 1 << 4) + 15));
            list.add(packData1(0, (blockX + 1 << 4) + 15, (blockY << 4) + 15, (blockZ << 4) + 1 + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u + 1 << 4) + 15, (v << 4) + 15));
            list.add(packData1(0, (blockX + 1 << 4) + 15, (blockY + 1 << 4) + 15, (blockZ << 4) + 1 + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u + 1 << 4) + 15, (v + 1 << 4) + 15));
        }
        if (Block.getBlockOcclusionData(chunk.getBlock(blockX, blockY, blockZ + 1), SOUTH) == -1L) {
            side = SOUTH;
            list.add(packData1(0, (blockX << 4) + 15, (blockY << 4) + 15, (blockZ + 1 << 4) - 1 + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u << 4) + 15, (v << 4) + 15));
            list.add(packData1(0, (blockX << 4) + 15, (blockY + 1 << 4) + 15, (blockZ + 1 << 4) - 1 + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u << 4) + 15, (v + 1 << 4) + 15));
            list.add(packData1(0, (blockX + 1 << 4) + 15, (blockY << 4) + 15, (blockZ + 1 << 4) - 1 + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u + 1 << 4) + 15, (v << 4) + 15));
            list.add(packData1(0, (blockX + 1 << 4) + 15, (blockY + 1 << 4) + 15, (blockZ + 1 << 4) - 1 + 15));
            list.add(packFoliageData(1, skyLight, blockLight, (u + 1 << 4) + 15, (v + 1 << 4) + 15));
        }
    }

    private int packData1(int ambientOcclusionLevel, int inChunkX, int inChunkY, int inChunkZ) {
        return ambientOcclusionLevel << 30 | inChunkX << 20 | inChunkY << 10 | inChunkZ;
    }

    private int packData2(int skyLight, int blockLight, int u, int v) {
        return side << 26 | skyLight << 22 | blockLight << 18 | u << 9 | v;
    }

    private int packWaterData(int waveMultiplier, int skyLight, int blockLight, int u, int v) {
        return waveMultiplier << 29 | side << 26 | skyLight << 22 | blockLight << 18 | u << 9 | v;
    }

    private int packFoliageData(int windMultiplier, int skyLight, int blockLight, int u, int v) {
        return windMultiplier << 29 | side << 26 | skyLight << 22 | blockLight << 18 | u << 9 | v;
    }

    private int getAmbientOcclusionLevel(int inChunkX, int inChunkY, int inChunkZ, int subX, int subY, int subZ) {
        int level = 0;
        int x = worldCoordinate.x + inChunkX;
        int y = worldCoordinate.y + inChunkY;
        int z = worldCoordinate.z + inChunkZ;
        int startX = 0, startY = 0, startZ = 0;

        switch (side) {
            case NORTH -> {
                startX = subX == 0 ? x - 1 : subX < 0 ? --x : x;
                startY = subY == 0 ? y - 1 : subY < 0 ? --y : y;
                startZ = subZ == 0 ? z : --z;
            }
            case TOP -> {
                startX = subX == 0 ? x - 1 : subX < 0 ? --x : x;
                startY = subY == 0 ? y : --y;
                startZ = subZ == 0 ? z - 1 : subZ < 0 ? --z : z;
            }
            case WEST -> {
                startX = subX == 0 ? x : --x;
                startY = subY == 0 ? y - 1 : subY < 0 ? --y : y;
                startZ = subZ == 0 ? z - 1 : subZ < 0 ? --z : z;
            }
            case SOUTH -> {
                startX = subX == 0 ? x - 1 : subX < 0 ? --x : x;
                startY = subY == 0 ? y - 1 : subY < 0 ? --y : y;
                startZ = subZ != 0 ? z : --z;
            }
            case BOTTOM -> {
                startX = subX == 0 ? x - 1 : subX < 0 ? --x : x;
                startY = subY != 0 ? y : --y;
                startZ = subZ == 0 ? z - 1 : subZ < 0 ? --z : z;
            }
            case EAST -> {
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

    private int getVertexBlockLightInWorld(int x, int y, int z, int subX, int subY, int subZ) {
        byte max = 0;
        int startX, startY, startZ;

        switch (side) {
            case NORTH -> {
                startX = x - 1;
                startY = y - 1;
                startZ = subZ == 0 ? z : --z;
            }
            case SOUTH -> {
                startX = x - 1;
                startY = y - 1;
                startZ = subZ == 0 ? --z : z;
            }
            case TOP -> {
                startX = x - 1;
                startZ = z - 1;
                startY = subY == 0 ? y : --y;
            }
            case BOTTOM -> {
                startX = x - 1;
                startZ = z - 1;
                startY = subY == 0 ? --y : y;
            }
            case WEST -> {
                startY = y - 1;
                startZ = z - 1;
                startX = subX == 0 ? x : --x;
            }
            default -> { // EAST
                startY = y - 1;
                startZ = z - 1;
                startX = subX == 0 ? --x : x;
            }
        }

        for (int lightX = startX; lightX <= x; lightX++)
            for (int lightY = startY; lightY <= y; lightY++)
                for (int lightZ = startZ; lightZ <= z; lightZ++) {
                    byte currentBlockLight = Chunk.getBlockLightInWorld(lightX, lightY, lightZ);
                    if (max < currentBlockLight) max = currentBlockLight;
                }
        return max;
    }

    private int getVertexSkyLightInWorld(int x, int y, int z, int subX, int subY, int subZ) {
        byte max = 0;
        int startX, startY, startZ;

        switch (side) {
            case NORTH -> {
                startX = x - 1;
                startY = y - 1;
                startZ = subZ == 0 ? z : --z;
            }
            case SOUTH -> {
                startX = x - 1;
                startY = y - 1;
                startZ = subZ == 0 ? --z : z;
            }
            case TOP -> {
                startX = x - 1;
                startZ = z - 1;
                startY = subY == 0 ? y : --y;
            }
            case BOTTOM -> {
                startX = x - 1;
                startZ = z - 1;
                startY = subY == 0 ? --y : y;
            }
            case WEST -> {
                startY = y - 1;
                startZ = z - 1;
                startX = subX == 0 ? x : --x;
            }
            default -> { // EAST
                startY = y - 1;
                startZ = z - 1;
                startX = subX == 0 ? --x : x;
            }
        }

        for (int lightX = startX; lightX <= x; lightX++)
            for (int lightY = startY; lightY <= y; lightY++)
                for (int lightZ = startZ; lightZ <= z; lightZ++) {
                    byte currentBlockLight = Chunk.getSkyLightInWorld(lightX, lightY, lightZ);
                    if (max < currentBlockLight) max = currentBlockLight;
                }
        return max;
    }

    private static int getVertexWaterLevelInWorld(int x, int blockY, int z) {
        int max = 0;

        for (int blockX = x - 1; blockX <= x; blockX++)
            for (int blockZ = z - 1; blockZ <= z; blockZ++) {

                int currentWaterLevel = Block.getWaterLevel(blockX, blockY, blockZ);
                if (currentWaterLevel > max) max = currentWaterLevel;
            }
        return max;
    }

    private static int getVertexLavaLevelInWorld(int x, int blockY, int z) {
        int max = 0;

        for (int blockX = x - 1; blockX <= x; blockX++)
            for (int blockZ = z - 1; blockZ <= z; blockZ++) {

                int currentLavaLevel = Block.getLavaLevel(blockX, blockY, blockZ);
                if (currentLavaLevel > max) max = currentLavaLevel;
            }
        return max;
    }

    private static boolean occludesOpaque(short toTestBlock, short occludingBlock, int side, int aabbIndex, int x, int y, int z) {
        if (Block.getXYZSubData(toTestBlock)[Block.sideToMinMaxXYZ(side) + aabbIndex] != 0)
            return Block.getBlockOcclusionData(toTestBlock, side) == -1L;
        int occludingBlockType = Block.getBlockType(occludingBlock);
        if (Block.isLiquidType(occludingBlockType)) {
            if (Block.isWaterLogged(occludingBlock)) return false;
            if (!Block.isLiquidType(Block.getBlockType(toTestBlock))) return false;
            return occludesLava(toTestBlock, occludingBlock, side, x, y, z);
        }
        if (occludingBlockType == AIR_TYPE) return false;

        long toTestOcclusionData = Block.getBlockOcclusionData(toTestBlock, side);
        int occludingSide = (side + 3) % 6;
        long occludingOcclusionData = Block.getBlockOcclusionData(occludingBlock, occludingSide);

        if (Block.isGlassType(occludingBlock))
            return Block.isGlassType(toTestBlock) && (toTestOcclusionData | occludingOcclusionData) == occludingOcclusionData && toTestOcclusionData != 0L;
        if (Block.isLeaveType(occludingBlock)) {
            if (Block.isLeaveType(toTestBlock))
                return (x + y + z & 1) == 0 && (toTestOcclusionData | occludingOcclusionData) == occludingOcclusionData && toTestOcclusionData != 0L;
            return false;
        }

        if (toTestOcclusionData == 0)
            return occludingBlockType == CARPET && Block.getBlockType(toTestBlock) == CARPET;   // Hotfix for carpets
        return (toTestOcclusionData | occludingOcclusionData) == occludingOcclusionData;
    }

    private static boolean occludesWater(short toTestBlock, short occludingBlock, int side, int x, int y, int z) {
        if (Block.getBlockType(occludingBlock) == AIR_TYPE) return false;
        if (Block.isLeaveType(occludingBlock) || Block.isGlassType(occludingBlock) && !Block.isWaterLogged(occludingBlock))
            return false;
        if (occludingBlock == LAVA_SOURCE) return false;
        if (side == TOP)
            return Block.isWaterLogged(occludingBlock) || Block.getBlockOcclusionData(occludingBlock, BOTTOM) == -1L && Block.isWaterSource(toTestBlock);
        if (side == BOTTOM)
            return Block.isWaterLogged(occludingBlock) || Block.getBlockOcclusionData(occludingBlock, TOP) == -1L;
        if (!Block.isWaterLogged(occludingBlock))
            return Block.getBlockOcclusionData(occludingBlock, (side + 3) % 6) == -1;

        byte[] normal = Block.NORMALS[side];
        short blockAboveToTestBlock = Chunk.getBlockInWorld(x, y + 1, z);
        short blockAboveOccludingBlock = Chunk.getBlockInWorld(x + normal[0], y + 1, z + normal[2]);

        boolean toTestBlockUp = toTestBlock == FLOWING_WATER_LEVEL_8 || Block.isWaterSource(toTestBlock) && (Block.isWaterLogged(blockAboveToTestBlock) || Block.getBlockOcclusionData(blockAboveToTestBlock, BOTTOM) == -1L);
        boolean occludingBlockUp = occludingBlock == FLOWING_WATER_LEVEL_8 || Block.isWaterSource(occludingBlock) && (Block.isWaterLogged(blockAboveOccludingBlock) || Block.getBlockOcclusionData(blockAboveOccludingBlock, BOTTOM) == -1L);

        return !toTestBlockUp || occludingBlockUp;
    }

    private static boolean occludesLava(short toTestBlock, short occludingBlock, int side, int x, int y, int z) {
        if (Block.getBlockType(occludingBlock) == AIR_TYPE) return false;
        if (occludingBlock == WATER_SOURCE) return false;
        if (side == TOP || side == BOTTOM) return true;
        if (!Block.isLavaBlock(occludingBlock))
            return Block.getBlockOcclusionData(occludingBlock, (side + 3) % 6) == -1;

        byte[] normal = Block.NORMALS[side];
        short blockAboveToTestBlock = Chunk.getBlockInWorld(x, y + 1, z);
        short blockAboveOccludingBlock = Chunk.getBlockInWorld(x + normal[0], y + 1, z + normal[2]);

        boolean toTestBlockUp = toTestBlock == LAVA_SOURCE && (blockAboveToTestBlock == LAVA_SOURCE || Block.getBlockOcclusionData(blockAboveToTestBlock, BOTTOM) == -1L);
        boolean occludingBlockUp = blockAboveOccludingBlock == LAVA_SOURCE || Block.getBlockOcclusionData(blockAboveOccludingBlock, BOTTOM) == -1L;

        return !(toTestBlockUp && !occludingBlockUp);
    }

    private Chunk chunk;
    private Vector3i worldCoordinate;

    private int blockX, blockY, blockZ;
    private int side, aabbIndex, properties;
    private short block;
    private ArrayList<Integer> list;
    private final Random random = new Random();
}
