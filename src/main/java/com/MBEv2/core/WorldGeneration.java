package com.MBEv2.core;

import com.MBEv2.test.GameLogic;

import java.util.Random;

import static com.MBEv2.core.utils.Constants.*;

public class WorldGeneration {

    public static void generate(double[][] heightMap, int[][] stoneMap, double[][] featureMap, byte[][] treeMap, Chunk chunk) {
        if (chunk.isGenerated()) return;
        chunk.setGenerated();
        for (int x = 0; x < CHUNK_SIZE; x++) {
            int totalX = x + (chunk.getX() << CHUNK_SIZE_BITS);
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int totalZ = z + (chunk.getZ() << CHUNK_SIZE_BITS);

                int height = (int) heightMap[x][z];
                int stoneHeight = stoneMap[x][z];
                int snowHeight = (int) (featureMap[x][z] * 8) + SNOW_LEVEL;
                int sandHeight = (int) (Math.abs(featureMap[x][z] * 4)) + WATER_LEVEL - 1;
                int treeValue = treeMap[x][z];

                boolean oakTree = treeValue == OAK_TREE_VALUE;
                if (oakTree) oakTree = isOutsideCave(totalX, height, totalZ);
                boolean spruceTree = treeValue == SPRUCE_TREE_VALUE;
                if (spruceTree) spruceTree = isOutsideCave(totalX, height, totalZ);
                boolean darkOakTree = treeValue == DARK_OAK_TREE_VALUE;
                if (darkOakTree) darkOakTree = isOutsideCave(totalX, height, totalZ);

                for (int y = 0; y < CHUNK_SIZE; y++) {
                    int totalY = y + (chunk.getY() << CHUNK_SIZE_BITS);

                    if (isOutsideCave(totalX, totalY, totalZ)) {
                        if (totalY >= snowHeight && (totalY == stoneHeight || totalY == stoneHeight - 1))
                            chunk.storeSave(x, y, z, SNOW);
                        else if (totalY <= stoneHeight)
                            chunk.storeSave(x, y, z, getGeneratingStoneType(totalX, totalY, totalZ));
                        else if (totalY < height - 5)
                            chunk.storeSave(x, y, z, getGeneratingStoneType(totalX, totalY, totalZ));
                        else if (totalY <= height && height <= sandHeight + 2 && totalY <= sandHeight + 2 && totalY >= sandHeight - 2)
                            chunk.storeSave(x, y, z, SAND);
                        else if (totalY == height && totalY > WATER_LEVEL) chunk.storeSave(x, y, z, GRASS);
                        else if (totalY <= height)
                            chunk.storeSave(x, y, z, height <= WATER_LEVEL ? getGeneratingMudType(totalX, totalY, totalZ) : DIRT);
                        else if (totalY <= WATER_LEVEL) chunk.storeSave(x, y, z, WATER);
                    } else if (totalY <= WATER_LEVEL) chunk.storeSave(x, y, z, WATER);

                    if (oakTree && totalY < height + OAK_TREE.length && totalY >= height) for (int i = 0; i < 5; i++)
                        for (int j = 0; j < 5; j++)
                            chunk.storeTreeBlock(x + i - 2, y, z + j - 2, OAK_TREE[totalY - height][i][j]);

                    else if (spruceTree && totalY < height + SPRUCE_TREE.length && totalY >= height)
                        for (int i = 0; i < 7; i++)
                            for (int j = 0; j < 7; j++)
                                chunk.storeTreeBlock(x + i - 3, y, z + j - 3, SPRUCE_TREE[totalY - height][i][j]);

                    else if (darkOakTree && totalY < height + DARK_OAK_TREE.length && totalY >= height)
                        for (int i = 0; i < 7; i++)
                            for (int j = 0; j < 7; j++)
                                chunk.storeTreeBlock(x + i - 3, y, z + j - 3, DARK_OAK_TREE[totalY - height][i][j]);
                }
            }
        }
    }

    public static void generateChunk(Chunk chunk) {
        if (chunk.isGenerated()) return;
        double[][] heightMap = heightMap(chunk.getX(), chunk.getZ());
        int[][] stoneMap = stoneMap(chunk.getX(), chunk.getZ(), heightMap);
        double[][] featureMap = featureMap(chunk.getX(), chunk.getZ());
        byte[][] treeMap = treeMap(chunk.getX(), chunk.getZ(), heightMap, stoneMap, featureMap);
        generate(heightMap, stoneMap, featureMap, treeMap, chunk);
    }

    public static byte getGeneratingStoneType(int x, int y, int z) {
        double noise = OpenSimplex2S.noise3_ImproveXY(SEED, x * STONE_TYPE_FREQUENCY, y * STONE_TYPE_FREQUENCY, z * STONE_TYPE_FREQUENCY);
        if (Math.abs(noise) < ANDESITE_THRESHOLD) return ANDESITE;
        if (noise > SLATE_THRESHOLD) return SLATE;
        return STONE;
    }

    public static byte getGeneratingMudType(int x, int y, int z) {
        double noise = OpenSimplex2S.noise3_ImproveXY(SEED, x * MUD_TYPE_FREQUENCY, y * MUD_TYPE_FREQUENCY, z * MUD_TYPE_FREQUENCY);
        if (Math.abs(noise) < GRAVEL_THRESHOLD) return GRAVEL;
        if (noise > CLAY_THRESHOLD) return CLAY;
        if (noise < SAND_THRESHOLD) return SAND;
        return MUD;
    }

    public static double[][] heightMap(int x, int z) {
        double[][] heightMap = new double[CHUNK_SIZE][CHUNK_SIZE];

        for (int mapX = 0; mapX < CHUNK_SIZE; mapX++) {
            for (int mapZ = 0; mapZ < CHUNK_SIZE; mapZ++) {
                int currentX = (x << CHUNK_SIZE_BITS) + mapX;
                int currentZ = (z << CHUNK_SIZE_BITS) + mapZ;
                double value = WATER_LEVEL;
                value += OpenSimplex2S.noise3_ImproveXY(SEED, currentX * HEIGHT_MAP_FREQUENCY / 4, currentZ * HEIGHT_MAP_FREQUENCY / 4, 0.0) * 50;
                value += OpenSimplex2S.noise3_ImproveXY(SEED + 1, currentX * HEIGHT_MAP_FREQUENCY, currentZ * HEIGHT_MAP_FREQUENCY, 0.0) * 25;
                value += OpenSimplex2S.noise3_ImproveXY(SEED + 2, currentX * HEIGHT_MAP_FREQUENCY * 2, currentZ * HEIGHT_MAP_FREQUENCY * 2, 0.0) * 12;
                value += OpenSimplex2S.noise3_ImproveXY(SEED + 3, currentX * HEIGHT_MAP_FREQUENCY * 4, currentZ * HEIGHT_MAP_FREQUENCY * 4, 0.0) * 6;
                value += OpenSimplex2S.noise3_ImproveXY(SEED + 4, currentX * HEIGHT_MAP_FREQUENCY * 8, currentZ * HEIGHT_MAP_FREQUENCY * 8, 0.0) * 3;
                heightMap[mapX][mapZ] = value;
            }
        }
        return heightMap;
    }

    public static int[][] stoneMap(int x, int z, double[][] heightMap) {
        int[][] stoneMap = new int[CHUNK_SIZE][CHUNK_SIZE];

        for (int mapX = 0; mapX < CHUNK_SIZE; mapX++) {
            for (int mapZ = 0; mapZ < CHUNK_SIZE; mapZ++) {
                int currentX = (x << CHUNK_SIZE_BITS) + mapX;
                int currentZ = (z << CHUNK_SIZE_BITS) + mapZ;

                double biomes = OpenSimplex2S.noise3_ImproveXY(SEED + 1 << 16, currentX * STONE_MAP_FREQUENCY, currentZ * STONE_MAP_FREQUENCY, 0.0);
                biomes = Math.max(0, biomes);
                stoneMap[mapX][mapZ] = (int) (heightMap[mapX][mapZ] * (biomes + 1)) * (biomes != 0 ? 1 : 0);

            }
        }
        return stoneMap;
    }

    public static double[][] featureMap(int x, int z) {
        double[][] featureMap = new double[CHUNK_SIZE][CHUNK_SIZE];
        Random random = new Random(GameLogic.getChunkId(x, 0, z));

        for (int mapX = 0; mapX < CHUNK_SIZE; mapX++)
            for (int mapZ = 0; mapZ < CHUNK_SIZE; mapZ++)
                featureMap[mapX][mapZ] = random.nextDouble();
        return featureMap;
    }

    public static byte[][] treeMap(int x, int z, double[][] heightMap, int[][] stoneMap, double[][] featureMap) {
        byte[][] treeMap = new byte[CHUNK_SIZE][CHUNK_SIZE];
        for (int mapX = 2; mapX < CHUNK_SIZE - 2; mapX++) {
            for (int mapZ = 2; mapZ < CHUNK_SIZE - 2; mapZ++) {

                if (treeMap[mapX][mapZ] != 0)
                    continue;
                if (stoneMap[mapX][mapZ] != 0)
                    continue;
                if (heightMap[mapX][mapZ] <= WATER_LEVEL)
                    continue;
                int sandHeight = (int) (Math.abs(featureMap[mapX][mapZ] * 4)) + WATER_LEVEL;
                if (heightMap[mapX][mapZ] <= sandHeight + 2)
                    continue;

                int currentX = (x << CHUNK_SIZE_BITS) + mapX;
                int currentZ = (z << CHUNK_SIZE_BITS) + mapZ;

                double value = OpenSimplex2S.noise3_ImproveXY(SEED + 1 << 32, currentX, currentZ, 0.0);
                if (value > TREE_THRESHOLD) {
                    for (int i = -1; i <= 1; i++)
                        for (int j = -1; j <= 1; j++)
                            treeMap[mapX + i][mapZ + j] = -1;
                    treeMap[mapX][mapZ] = OAK_TREE_VALUE;
                } else {
                    boolean toCloseToChunkEdge = mapX == 2 || mapX == CHUNK_SIZE - 3 || mapZ == 2 || mapZ == CHUNK_SIZE - 3;
                    if (value < -TREE_THRESHOLD) {
                        if (toCloseToChunkEdge)
                            continue;
                        for (int i = -1; i <= 1; i++)
                            for (int j = -1; j <= 1; j++)
                                treeMap[mapX + i][mapZ + j] = -1;
                        treeMap[mapX][mapZ] = SPRUCE_TREE_VALUE;
                    } else if (value < 0.005 && value > -0.005) {
                        if (toCloseToChunkEdge)
                            continue;
                        for (int i = -1; i <= 2; i++)
                            for (int j = -1; j <= 2; j++)
                                treeMap[mapX + i][mapZ + j] = -1;
                        treeMap[mapX][mapZ] = DARK_OAK_TREE_VALUE;
                    }
                }
            }
        }
        return treeMap;
    }

    public static boolean isOutsideCave(int x, int y, int z) {
        if (y < BLOB_CAVE_MIN_Y || y > BLOB_CAVE_MAX_Y)
            return true;
        y -= CAVE_HEIGHT;
        double modifier = (y * y * CAVE_HEIGHT_BIAS);

        double blobCaveValue = OpenSimplex2S.noise3_ImproveXY(SEED, x * BLOB_CAVE_FREQUENCY, y * BLOB_CAVE_FREQUENCY, z * BLOB_CAVE_FREQUENCY) / modifier;

        return blobCaveValue < BLOB_CAVE_THRESHOLD;
    }
}
