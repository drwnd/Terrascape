package com.MBEv2.core;

import com.MBEv2.core.entity.Model;
import com.MBEv2.test.GameLogic;
import org.joml.Vector3i;

import java.util.ArrayList;

import static com.MBEv2.core.utils.Constants.*;

public class Chunk {

    private static final Chunk[] world = new Chunk[RENDERED_WORLD_WIDTH * RENDERED_WORLD_HEIGHT * RENDERED_WORLD_WIDTH];

    private final byte[] blocks;

    private int[] vertices;
    private int[] transparentVertices;

    private final int X, Y, Z;
    private final Vector3i worldCoordinate;
    private final long id;
    private final int index;

    private boolean isMeshed = false;
    private boolean isGenerated = false;
    private boolean isModified = false;

    private Model model;
    private Model transparentModel;

    public Chunk(int x, int y, int z) {
        this.X = x;
        this.Y = y;
        this.Z = z;
        worldCoordinate = new Vector3i(X << 5, Y << 5, Z << 5);
        blocks = new byte[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];
        id = GameLogic.getChunkId(X, Y, Z);
        index = GameLogic.getChunkIndex(X, Y, Z);
    }

    public void generate(double[][] heightMap, int[][] stoneMap, double[][] featureMap, byte[][] treeMap) {
        if (isGenerated)
            return;
        isGenerated = true;
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {

                int height = (int) heightMap[x][z];
                int stoneHeight = stoneMap[x][z];
                int snowHeight = (int) (featureMap[x][z] * 8) + SNOW_LEVEL;
                int sandHeight = (int) (Math.abs(featureMap[x][z] * 4)) + WATER_LEVEL;
                int tree = treeMap[x][z];
                boolean oakTree = tree == OAK_TREE_VALUE;
                boolean spruceTree = tree == SPRUCE_TREE_VALUE;
                boolean darkOakTree = tree == DARK_OAK_TREE_VALUE;

                for (int y = 0; y < CHUNK_SIZE; y++) {
                    int totalY = y + (Y << 5);

                    if (oakTree && totalY < height + OAK_TREE.length && totalY >= height)
                        for (int i = 0; i < 5; i++)
                            for (int j = 0; j < 5; j++)
                                storeTreeBlock(x + i - 2, y, z + j - 2, OAK_TREE[totalY - height][i][j]);

                    else if (spruceTree && totalY < height + SPRUCE_TREE.length && totalY >= height)
                        for (int i = 0; i < 7; i++)
                            for (int j = 0; j < 7; j++)
                                storeTreeBlock(x + i - 3, y, z + j - 3, SPRUCE_TREE[totalY - height][i][j]);

                    else if (darkOakTree && totalY < height + DARK_OAK_TREE.length && totalY >= height)
                        for (int i = 0; i < 7; i++)
                            for (int j = 0; j < 7; j++)
                                storeTreeBlock(x + i - 3, y, z + j - 3, DARK_OAK_TREE[totalY - height][i][j]);

                    else if (totalY >= snowHeight && (totalY == stoneHeight || totalY == stoneHeight - 1))
                        storeSave(x, y, z, SNOW);
                    else if (totalY <= stoneHeight)
                        storeSave(x, y, z, STONE);
                    else if (totalY < height - 5)
                        storeSave(x, y, z, STONE);
                    else if (totalY <= height && height <= sandHeight + 2 && totalY <= sandHeight + 2 && totalY >= sandHeight - 2)
                        storeSave(x, y, z, SAND);
                    else if (totalY == height && totalY > WATER_LEVEL)
                        storeSave(x, y, z, GRASS);
                    else if (totalY <= height)
                        storeSave(x, y, z, height <= WATER_LEVEL ? MUD : DIRT);
                    else if (totalY <= WATER_LEVEL)
                        storeSave(x, y, z, WATER);
                }
            }
        }
    }

    public static void generateChunk(Chunk chunk) {
        if (chunk.isGenerated)
            return;
        double[][] heightMap = GameLogic.heightMap(chunk.X, chunk.Z);
        int[][] stoneMap = GameLogic.stoneMap(chunk.X, chunk.Z, heightMap);
        double[][] featureMap = GameLogic.featureMap(chunk.X, chunk.Z);
        byte[][] treeMap = GameLogic.treeMap(chunk.X, chunk.Z, heightMap, stoneMap, featureMap);
        chunk.generate(heightMap, stoneMap, featureMap, treeMap);
    }

    public void generateMesh() {
        isMeshed = true;
        ArrayList<Integer> verticesList = new ArrayList<>();
        ArrayList<Integer> transparentVerticesList = new ArrayList<>();

        generateSurroundingChunks();

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {

                    byte block = getSaveBlock(x, y, z);

                    if (block == AIR)
                        continue;

                    for (int side = 0; side < 6; side++) {

                        int[] normal = Block.NORMALS[side];
                        if (Block.occludes(block, getBlock(x + normal[0], y + normal[1], z + normal[2]), side, worldCoordinate.x | x, worldCoordinate.y | y, worldCoordinate.z | z))
                            continue;

                        int texture = Block.getTextureIndex(block, side) - 1;

                        int u = texture & 15;
                        int v = (texture >> 4) & 15;

                        if (block != WATER) {
                            addSideToList(x, y, z, u, v, side, verticesList, block);
                            continue;
                        }
                        addSideToList(x, y, z, u, v, side, transparentVerticesList, block);
                    }
                }
            }
        }
        vertices = new int[verticesList.size()];
        for (int i = 0, size = verticesList.size(); i < size; i++)
            vertices[i] = verticesList.get(i);

        transparentVertices = new int[transparentVerticesList.size()];
        for (int i = 0, size = transparentVerticesList.size(); i < size; i++)
            transparentVertices[i] = transparentVerticesList.get(i);
    }

    public void generateSurroundingChunks() {
        generateIfNecessary(X - 1, Y - 1, Z - 1);

        generateIfNecessary(X - 1, Y - 1, Z);

        generateIfNecessary(X - 1, Y - 1, Z + 1);

        generateIfNecessary(X - 1, Y, Z - 1);

        generateIfNecessary(X - 1, Y, Z);

        generateIfNecessary(X - 1, Y, Z + 1);

        generateIfNecessary(X - 1, Y + 1, Z - 1);

        generateIfNecessary(X - 1, Y + 1, Z);

        generateIfNecessary(X - 1, Y + 1, Z + 1);

        generateIfNecessary(X, Y - 1, Z - 1);

        generateIfNecessary(X, Y - 1, Z);

        generateIfNecessary(X, Y - 1, Z + 1);

        generateIfNecessary(X, Y, Z - 1);

        generateIfNecessary(X, Y, Z + 1);

        generateIfNecessary(X, Y + 1, Z - 1);

        generateIfNecessary(X, Y + 1, Z);

        generateIfNecessary(X, Y + 1, Z + 1);

        generateIfNecessary(X + 1, Y - 1, Z - 1);

        generateIfNecessary(X + 1, Y - 1, Z);

        generateIfNecessary(X + 1, Y - 1, Z + 1);

        generateIfNecessary(X + 1, Y, Z - 1);

        generateIfNecessary(X + 1, Y, Z);

        generateIfNecessary(X + 1, Y, Z + 1);

        generateIfNecessary(X + 1, Y + 1, Z - 1);

        generateIfNecessary(X + 1, Y + 1, Z);

        generateIfNecessary(X + 1, Y + 1, Z + 1);
    }

    public void generateIfNecessary(int x, int y, int z) {
        long expectedId = GameLogic.getChunkId(x, y, z);
        int index = GameLogic.getChunkIndex(x, y, z);
        Chunk chunk = getChunk(index);

        if (chunk == null) {
            chunk = new Chunk(x, y, z);
            generateChunk(chunk);
            storeChunk(chunk);

        } else if (chunk.getId() != expectedId) {
            GameLogic.addToUnloadModel(chunk.getModel());
            GameLogic.addToUnloadModel(chunk.getTransparentModel());

            chunk = new Chunk(x, y, z);
            Chunk.storeChunk(chunk);
            generateChunk(chunk);

        } else if (!chunk.isGenerated)
            generateChunk(chunk);
    }

    public void addSideToList(int x, int y, int z, int u, int v, int side, ArrayList<Integer> verticesList, byte block) {
        int skyLight = 15;
        int blockLight = 0;

        switch (side) {
            case FRONT:
                addVertexToList(verticesList, x + 1, y + 1, z + 1, u, v, side, skyLight, blockLight, block, 0, x, y, z);
                addVertexToList(verticesList, x, y + 1, z + 1, u + 1, v, side, skyLight, blockLight, block, 1, x, y, z);
                addVertexToList(verticesList, x + 1, y, z + 1, u, v + 1, side, skyLight, blockLight, block, 2, x, y, z);
                addVertexToList(verticesList, x, y, z + 1, u + 1, v + 1, side, skyLight, blockLight, block, 3, x, y, z);
                break;
            case TOP:
                addVertexToList(verticesList, x, y + 1, z, u, v, side, skyLight, blockLight, block, 0, x, y, z);
                addVertexToList(verticesList, x, y + 1, z + 1, u + 1, v, side, skyLight, blockLight, block, 1, x, y, z);
                addVertexToList(verticesList, x + 1, y + 1, z, u, v + 1, side, skyLight, blockLight, block, 2, x, y, z);
                addVertexToList(verticesList, x + 1, y + 1, z + 1, u + 1, v + 1, side, skyLight, blockLight, block, 3, x, y, z);
                break;
            case RIGHT:
                addVertexToList(verticesList, x + 1, y + 1, z, u, v, side, skyLight, blockLight, block, 0, x, y, z);
                addVertexToList(verticesList, x + 1, y + 1, z + 1, u + 1, v, side, skyLight, blockLight, block, 1, x, y, z);
                addVertexToList(verticesList, x + 1, y, z, u, v + 1, side, skyLight, blockLight, block, 2, x, y, z);
                addVertexToList(verticesList, x + 1, y, z + 1, u + 1, v + 1, side, skyLight, blockLight, block, 3, x, y, z);
                break;
            case BACK:
                addVertexToList(verticesList, x, y + 1, z, u, v, side, skyLight, blockLight, block, 0, x, y, z);
                addVertexToList(verticesList, x + 1, y + 1, z, u + 1, v, side, skyLight, blockLight, block, 1, x, y, z);
                addVertexToList(verticesList, x, y, z, u, v + 1, side, skyLight, blockLight, block, 2, x, y, z);
                addVertexToList(verticesList, x + 1, y, z, u + 1, v + 1, side, skyLight, blockLight, block, 3, x, y, z);
                break;
            case BOTTOM:
                addVertexToList(verticesList, x + 1, y, z + 1, u, v, side, skyLight, blockLight, block, 3, x, y, z);
                addVertexToList(verticesList, x, y, z + 1, u + 1, v, side, skyLight, blockLight, block, 1, x, y, z);
                addVertexToList(verticesList, x + 1, y, z, u, v + 1, side, skyLight, blockLight, block, 2, x, y, z);
                addVertexToList(verticesList, x, y, z, u + 1, v + 1, side, skyLight, blockLight, block, 0, x, y, z);
                break;
            case LEFT:
                addVertexToList(verticesList, x, y + 1, z + 1, u, v, side, skyLight, blockLight, block, 1, x, y, z);
                addVertexToList(verticesList, x, y + 1, z, u + 1, v, side, skyLight, blockLight, block, 0, x, y, z);
                addVertexToList(verticesList, x, y, z + 1, u, v + 1, side, skyLight, blockLight, block, 3, x, y, z);
                addVertexToList(verticesList, x, y, z, u + 1, v + 1, side, skyLight, blockLight, block, 2, x, y, z);
                break;
        }
    }

    public void addVertexToList(ArrayList<Integer> list, int x, int y, int z, int u, int v, int side, int skyLight, int blockLight, byte block, int corner, int blockX, int blockY, int blockZ) {
        if ((Block.getBlockData(block) & DYNAMIC_SHAPE_MASK) != 0) {
            addVertexToListDynamic(list, x, y, z, u, v, side, skyLight, blockLight, block, corner, blockX, blockY, blockZ);
            return;
        }

        int subX = Block.getSubX(block, side, corner);
        int subY = Block.getSubY(block, side, corner);
        int subZ = Block.getSubZ(block, side, corner);
        int subU = Block.getSubU(block, side, corner);
        int subV = Block.getSubV(block, side, corner);

        list.add(packData((x << 4) + subX + 15, (y << 4) + subY + 15, (z << 4) + subZ + 15, getAmbientOcclusionLevel(x, y, z, side, subX, subY, subZ)));
        list.add(packData(side, skyLight, blockLight, (u << 4) + subU + 15, (v << 4) + subV + 15));
    }

    public void addVertexToListDynamic(ArrayList<Integer> list, int x, int y, int z, int u, int v, int side, int skyLight, int blockLight, byte block, int corner, int blockX, int blockY, int blockZ) {
        int subX = 0;
        int subY = 0;
        int subZ = 0;
        int subU = 0;
        int subV = 0;

        if (block == WATER) {
            switch (side) {
                case TOP: {
                    subY = -2;
                    subV = 2;
                    break;
                }
                case FRONT, RIGHT, BACK, LEFT: {
                    byte blockAbove = getBlock(blockX, blockY + 1, blockZ);
                    if ((corner == 0 || corner == 1) && blockAbove != WATER) {
                        subY = -2;
                        subV = 2;
                    } else if (corner == 2 || corner == 3) {
                        int[] normal = Block.NORMALS[side];
                        byte adjacentBlock = getBlock(blockX + normal[0], blockY, blockZ + normal[2]);
                        if (adjacentBlock == WATER && blockAbove == WATER) {
                            subY = 14;
                            subV = -14;
                        }
                    }
                    break;
                }
            }
        }

        list.add(packData((x << 4) + subX + 15, (y << 4) + subY + 15, (z << 4) + subZ + 15, getAmbientOcclusionLevel(x, y, z, side, subX, subY, subZ)));
        list.add(packData(side, skyLight, blockLight, (u << 4) + subU + 15, (v << 4) + subV + 15));
    }

    public int packData(int x, int y, int z, int ambientOcclusionLevel) {
        return ambientOcclusionLevel << 30 | x << 20 | y << 10 | z;
    }

    public int packData(int side, int skyLight, int blockLight, int u, int v) {
        return side << 26 | skyLight << 22 | blockLight << 18 | u << 9 | v;
    }

    public int getAmbientOcclusionLevel(int x, int y, int z, int side, int subX, int subY, int subZ) {

        int level = 0;
        switch (side) {
            case FRONT:
                if (subZ != 0)
                    return 0;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y - 1, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y - 1, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                break;
            case TOP:
                if (subY != 0)
                    return 0;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                break;
            case RIGHT:
                if (subX != 0)
                    return 0;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y - 1, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y - 1, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                break;
            case BACK:
                if (subZ != 0)
                    return 0;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y - 1, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y - 1, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                break;
            case BOTTOM:
                if (subY != 0)
                    return 0;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y - 1, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y - 1, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x, worldCoordinate.y + y - 1, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y - 1, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                break;
            case LEFT:
                if (subX != 0)
                    return 0;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y - 1, worldCoordinate.z + z)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                if ((Block.getBlockData(getBlockInWorld(worldCoordinate.x + x - 1, worldCoordinate.y + y - 1, worldCoordinate.z + z - 1)) & SOLID_MASK) != 0)
                    level++;
                break;
        }
        return level & 3;
    }

    public byte getBlock(int x, int y, int z) {
        if (x < 0) {
            Chunk neighbor = getChunk(X - 1, Y, Z);
            if (neighbor == null)
                return OUT_OF_WORLD;
            return neighbor.getSaveBlock(CHUNK_SIZE + x, y, z);
        } else if (x >= CHUNK_SIZE) {
            Chunk neighbor = getChunk(X + 1, Y, Z);
            if (neighbor == null)
                return OUT_OF_WORLD;
            return neighbor.getSaveBlock(x - CHUNK_SIZE, y, z);
        }
        if (y < 0) {
            Chunk neighbor = getChunk(X, Y - 1, Z);
            if (neighbor == null)
                return OUT_OF_WORLD;
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
            if (neighbor == null)
                return OUT_OF_WORLD;
            return neighbor.getSaveBlock(x, y, CHUNK_SIZE + z);
        } else if (z >= CHUNK_SIZE) {
            Chunk neighbor = getChunk(X, Y, Z + 1);
            if (neighbor == null)
                return OUT_OF_WORLD;
            return neighbor.getSaveBlock(x, y, z - CHUNK_SIZE);
        }

        return getSaveBlock(x, y, z);
    }

    public byte getSaveBlock(int x, int y, int z) {
        return blocks[x << 10 | y << 5 | z];
    }

    public static byte getBlockInWorld(int x, int y, int z) {
        Chunk chunk = world[GameLogic.getChunkIndex(x >> 5, y >> 5, z >> 5)];
        if (chunk == null)
            return OUT_OF_WORLD;
        return chunk.getSaveBlock(x & 31, y & 31, z & 31);
    }

    public void storeSave(int x, int y, int z, byte block) {
        blocks[x << 10 | y << 5 | z] = block;
    }

    private void storeTreeBlock(int x, int y, int z, byte block) {
        if (block == AIR || blocks[x << 10 | y << 5 | z] != AIR && Block.isLeaveType(block))
            return;
        blocks[x << 10 | y << 5 | z] = block;
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

    public static void storeChunk(Chunk chunk, int index){
        world[index] = chunk;
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
        vertices = null;
        transparentVertices = null;
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

    public boolean isModified() {
        return isModified;
    }

    public void setModified() {
        isModified = true;
    }

    public static Chunk[] getWorld() {
        return world;
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
}
