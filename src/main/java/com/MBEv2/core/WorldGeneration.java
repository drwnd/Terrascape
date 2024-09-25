package com.MBEv2.core;

import com.MBEv2.core.utils.Utils;
import com.MBEv2.test.GameLogic;

import java.util.ArrayList;
import java.util.Random;

import static com.MBEv2.core.utils.Constants.*;
import static com.MBEv2.core.utils.Settings.*;

public class WorldGeneration {

    //World generation
    public static final int WATER_LEVEL = 96;
    public static final int SNOW_LEVEL = 187;
    public static final int ICE_LEVEL = 237;
    public static final double PLAINS_TREE_THRESHOLD = 0.998;
    public static final double FOREST_TREE_THRESHOLD = 0.95;
    public static final double CACTUS_THRESHOLD = 0.992;
    public static final double WASTELAND_FEATURE_THRESHOLD = 0.999;
    public static final double HEIGHT_MAP_FREQUENCY = 0.01;
    public static final double TEMPERATURE_FREQUENCY = 0.001;
    public static final double HUMIDITY_FREQUENCY = TEMPERATURE_FREQUENCY;
    public static final double EROSION_FREQUENCY = 0.001;

    public static final double MAX_TERRAIN_HEIGHT_DIFFERENCE = 50;

    public static final double MOUNTAIN_THRESHOLD = 0.3;
    public static final double OCEAN_THRESHOLD = -0.3;

    public static final double BLOB_CAVE_CAVE_HEIGHT_BIAS = 0.008;
    public static final double NOODLE_CAVE_HEIGHT_BIAS = 0.004;

    public static final double AIR_BLOB_CAVE_FREQUENCY = 0.006;
    public static final double AIR_BLOB_CAVE_THRESHOLD = 0.36;
    public static final double AIR_NOODLE_CAVE_FREQUENCY = 0.008;
    public static final double AIR_NOODLE_CAVE_THRESHOLD = 0.008;

    public static final double WATER_BLOB_CAVE_FREQUENCY = 0.004;
    public static final double WATER_BLOB_CAVE_THRESHOLD = 0.45;
    public static final double WATER_NOODLE_CAVE_FREQUENCY = 0.005;
    public static final double WATER_NOODLE_CAVE_THRESHOLD = 0.005;

    public static final double LAVA_NOODLE_CAVE_FREQUENCY = 0.002;
    public static final double LAVA_NOODLE_CAVE_THRESHOLD = 0.0012;

    public static final int NO_CAVE = 0;
    public static final int AIR_CAVE = 1;
    public static final int WATER_CAVE = 2;
    public static final int LAVA_CAVE = 3;

    public static final double STONE_TYPE_FREQUENCY = 0.02;
    public static final double ANDESITE_THRESHOLD = 0.1;
    public static final double SLATE_THRESHOLD = 0.7;

    public static final double MUD_TYPE_FREQUENCY = 0.04;
    public static final double GRAVEL_THRESHOLD = 0.1;
    public static final double CLAY_THRESHOLD = 0.5;
    public static final double SAND_THRESHOLD = -0.5;
    public static final double MUD_THRESHOLD = -0.5;

    public static final double DIRT_TYPE_FREQUENCY = 0.05;
    public static final double COURSE_DIRT_THRESHOLD = 0.15;

    public static final double ICE_BERG_FREQUENCY = 0.025;
    public static final double ICE_BERG_THRESHOLD = 0.35;
    public static final double ICE_BERG_HEIGHT = 30;
    public static final double ICE_PLANE_THRESHOLD = 0.0;

    public static final double ICE_TYPE_FREQUENCY = 0.08;
    public static final double HEAVY_ICE_THRESHOLD = 0.6;


    public static final int DESERT = 2;
    public static final int WASTELAND = 3;
    public static final int DARK_OAK_FOREST = 4;
    public static final int SNOWY_SPRUCE_FOREST = 5;
    public static final int SNOWY_PLAINS = 6;
    public static final int SPRUCE_FOREST = 7;
    public static final int PLAINS = 8;
    public static final int OAK_FOREST = 9;
    public static final int WARM_OCEAN = 10;
    public static final int COLD_OCEAN = 11;
    public static final int OCEAN = 12;
    public static final int DRY_MOUNTAIN = 13;
    public static final int SNOWY_MOUNTAIN = 14;
    public static final int MOUNTAIN = 15;

    public static void generateSurroundingChunkTreeBlocks(Chunk chunk) {
        if (chunk.isGenerated()) return;
        double[][] heightMap = WorldGeneration.heightMap(chunk.X, chunk.Z);
        double[][] temperatureMap = WorldGeneration.temperatureMap(chunk.X, chunk.Z);
        double[][] humidityMap = WorldGeneration.humidityMap(chunk.X, chunk.Z);
        double[][] erosionMap = WorldGeneration.erosionMap(chunk.X, chunk.Z);
        double[][] featureMap = WorldGeneration.featureMap(chunk.X, chunk.Z);

        int[][] resultingHeightMap = new int[CHUNK_SIZE][CHUNK_SIZE];
        for (int x = 0; x < CHUNK_SIZE; x++)
            for (int z = 0; z < CHUNK_SIZE; z++)
                resultingHeightMap[x][z] = getHeight(heightMap[x][z], erosionMap[x][z]);

        generateSurroundingChunkTreeBlocks(chunk, resultingHeightMap, temperatureMap, humidityMap, erosionMap, featureMap);
    }

    public static void generateSurroundingChunkTreeBlocks(Chunk chunk, int[][] heightMap, double[][] temperatureMap, double[][] humidityMap, double[][] erosionMap, double[][] featureMap) {
        long[] caveBitMap = generateCaveBitMap(chunk, heightMap);

        for (int inChunkX = 0; inChunkX < CHUNK_SIZE; inChunkX++)
            for (int inChunkZ = 0; inChunkZ < CHUNK_SIZE; inChunkZ++) {
                double temperature = temperatureMap[inChunkX][inChunkZ];
                double humidity = humidityMap[inChunkX][inChunkZ];
                double erosion = erosionMap[inChunkX][inChunkZ];
                double feature = featureMap[inChunkX][inChunkZ];

                int resultingHeight = heightMap[inChunkX][inChunkZ];
                int biome = getBiome(temperature, humidity, erosion, resultingHeight);
                long caveBits = caveBitMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ];

                for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
                    int totalY = chunk.getWorldCoordinate().y | inChunkY;
                    switch (biome) {
                        case WASTELAND ->
                                genSurroundingOakTree(chunk, resultingHeight, inChunkX, inChunkY, inChunkZ, totalY, feature, WASTELAND_FEATURE_THRESHOLD, caveBits);
                        case PLAINS ->
                                genSurroundingOakTree(chunk, resultingHeight, inChunkX, inChunkY, inChunkZ, totalY, feature, PLAINS_TREE_THRESHOLD, caveBits);
                        case OAK_FOREST ->
                                genSurroundingOakTree(chunk, resultingHeight, inChunkX, inChunkY, inChunkZ, totalY, feature, FOREST_TREE_THRESHOLD, caveBits);
                        case DARK_OAK_FOREST ->
                                genSurroundingDarkOakTree(chunk, resultingHeight, inChunkX, inChunkY, inChunkZ, totalY, feature, FOREST_TREE_THRESHOLD, caveBits);
                        case SNOWY_PLAINS ->
                                genSurroundingSpruceTree(chunk, resultingHeight, inChunkX, inChunkY, inChunkZ, totalY, feature, PLAINS_TREE_THRESHOLD, caveBits);
                        case SNOWY_SPRUCE_FOREST, SPRUCE_FOREST ->
                                genSurroundingSpruceTree(chunk, resultingHeight, inChunkX, inChunkY, inChunkZ, totalY, feature, FOREST_TREE_THRESHOLD, caveBits);
                    }
                }
            }
    }


    public static void generate(Chunk chunk) {
        if (chunk.isGenerated()) return;
        double[][] heightMap = WorldGeneration.heightMap(chunk.X, chunk.Z);
        double[][] temperatureMap = WorldGeneration.temperatureMap(chunk.X, chunk.Z);
        double[][] humidityMap = WorldGeneration.humidityMap(chunk.X, chunk.Z);
        double[][] erosionMap = WorldGeneration.erosionMap(chunk.X, chunk.Z);
        double[][] featureMap = WorldGeneration.featureMap(chunk.X, chunk.Z);

        int[][] resultingHeightMap = new int[CHUNK_SIZE][CHUNK_SIZE];
        for (int x = 0; x < CHUNK_SIZE; x++)
            for (int z = 0; z < CHUNK_SIZE; z++)
                resultingHeightMap[x][z] = getHeight(heightMap[x][z], erosionMap[x][z]);

        generate(chunk, resultingHeightMap, temperatureMap, humidityMap, erosionMap, featureMap);
    }

    public static void generate(Chunk chunk, int[][] heightMap, double[][] temperatureMap, double[][] humidityMap, double[][] erosionMap, double[][] featureMap) {
        if (chunk.isGenerated()) return;
        chunk.setGenerated();

        long[] caveBitMap = generateCaveBitMap(chunk, heightMap);

        for (int inChunkX = 0; inChunkX < CHUNK_SIZE; inChunkX++)
            for (int inChunkZ = 0; inChunkZ < CHUNK_SIZE; inChunkZ++) {
                double temperature = temperatureMap[inChunkX][inChunkZ];
                double humidity = humidityMap[inChunkX][inChunkZ];
                double erosion = erosionMap[inChunkX][inChunkZ];
                double feature = featureMap[inChunkX][inChunkZ];

                int resultingHeight = heightMap[inChunkX][inChunkZ];
                int biome = getBiome(temperature, humidity, erosion, resultingHeight);
                long caveBits = caveBitMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ];

                switch (biome) {
                    case OCEAN ->
                            generateBiome(WorldGeneration::generateOcean, chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBits);
                    case WARM_OCEAN ->
                            generateBiome(WorldGeneration::generateWarmOcean, chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBits);
                    case COLD_OCEAN ->
                            generateBiome(WorldGeneration::generateColdOcean, chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBits);
                    case MOUNTAIN ->
                            generateBiome(WorldGeneration::generateMountain, chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBits);
                    case DRY_MOUNTAIN ->
                            generateBiome(WorldGeneration::generateDryMountain, chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBits);
                    case SNOWY_MOUNTAIN ->
                            generateBiome(WorldGeneration::generateSnowyMountain, chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBits);
                    case DESERT ->
                            generateBiome(WorldGeneration::generateDesert, chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBits);
                    case WASTELAND ->
                            generateBiome(WorldGeneration::generateWasteLand, chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBits);
                    case DARK_OAK_FOREST ->
                            generateBiome(WorldGeneration::generateDarkOakForest, chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBits);
                    case SNOWY_SPRUCE_FOREST ->
                            generateBiome(WorldGeneration::generateSnowySpruceForest, chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBits);
                    case SNOWY_PLAINS ->
                            generateBiome(WorldGeneration::generateSnowyPlains, chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBits);
                    case SPRUCE_FOREST ->
                            generateBiome(WorldGeneration::generateSpruceForest, chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBits);
                    case PLAINS ->
                            generateBiome(WorldGeneration::generatePlains, chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBits);
                    case OAK_FOREST ->
                            generateBiome(WorldGeneration::generateOakForest, chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBits);
                }
            }

        ArrayList<Long> toGenerateBlocks = Chunk.removeToGenerateBlocks(chunk.id);
        if (toGenerateBlocks != null) {
            int[] intHeightMap = Chunk.getHeightMap(chunk.X, chunk.Z);
            for (long data : toGenerateBlocks) {
                short block = (short) (data >> 48 & 0xFFFF);
                int inChunkX = (int) (data >> CHUNK_SIZE_BITS * 2 & CHUNK_SIZE_MASK);
                int inChunkY = (int) (data >> CHUNK_SIZE_BITS & CHUNK_SIZE_MASK);
                int inChunkZ = (int) (data & CHUNK_SIZE_MASK);

                int index = inChunkX << CHUNK_SIZE_BITS * 2 | inChunkZ << CHUNK_SIZE_BITS | inChunkY;
                if (chunk.getSaveBlock(index) != AIR && Block.isLeaveType(block)) continue;
                chunk.storeSave(inChunkX, inChunkY, inChunkZ, block);

                int y = inChunkY | chunk.Y << CHUNK_SIZE_BITS;
                if (y > intHeightMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ])
                    intHeightMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ] = y;
            }
        }

        chunk.optimizeBlockStorage();
    }

    private static void generateBiome(Biome biome, Chunk chunk, int inChunkX, int inChunkZ, int height, double feature, long caveBits) {
        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = chunk.Y << CHUNK_SIZE_BITS | inChunkY;
            boolean placedBlock = false;

            int caveType = (int) (caveBits >> (inChunkY << 1) & 3);
            if (caveType == NO_CAVE || height <= WATER_LEVEL && totalY >= height - 1 && totalY <= WATER_LEVEL)
                placedBlock = biome.placeBlock(chunk, inChunkX, inChunkY, inChunkZ, height, feature, caveBits);
            else if (caveType == WATER_CAVE && totalY <= WATER_LEVEL)
                chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);
            else if (caveType == LAVA_CAVE && totalY <= WATER_LEVEL)
                chunk.storeSave(inChunkX, inChunkY, inChunkZ, LAVA);

            if (totalY > height && totalY <= WATER_LEVEL && !placedBlock)
                chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);
        }
    }


    private static boolean generatePlains(Chunk chunk, int inChunkX, int inChunkY, int inChunkZ, int height, double feature, long caveBits) {
        int totalX = chunk.X << CHUNK_SIZE_BITS | inChunkX;
        int totalY = chunk.Y << CHUNK_SIZE_BITS | inChunkY;
        int totalZ = chunk.Z << CHUNK_SIZE_BITS | inChunkZ;

        if (totalY >= height) {
            boolean placedTreeBlock = genOakTree(chunk, height, inChunkX, inChunkY, inChunkZ, totalY, feature, PLAINS_TREE_THRESHOLD, caveBits);
            if (placedTreeBlock) return true;
        }
        if (totalY > height) return false;

        int sandHeight = Utils.floor(feature * 4.0) + WATER_LEVEL - 1;

        if (totalY < height - 5)
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
        else if (height <= sandHeight + 2 && totalY <= sandHeight + 2 && totalY >= sandHeight - 2)
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
        else if (totalY == height) chunk.storeSave(inChunkX, inChunkY, inChunkZ, GRASS);
        else chunk.storeSave(inChunkX, inChunkY, inChunkZ, DIRT);
        return true;
    }

    private static boolean generateOakForest(Chunk chunk, int inChunkX, int inChunkY, int inChunkZ, int height, double feature, long caveBits) {
        int totalX = chunk.X << CHUNK_SIZE_BITS | inChunkX;
        int totalY = chunk.Y << CHUNK_SIZE_BITS | inChunkY;
        int totalZ = chunk.Z << CHUNK_SIZE_BITS | inChunkZ;

        if (totalY >= height) {
            boolean placedTreeBlock = genOakTree(chunk, height, inChunkX, inChunkY, inChunkZ, totalY, feature, FOREST_TREE_THRESHOLD, caveBits);
            if (placedTreeBlock) return true;
        }
        if (totalY > height) return false;

        int sandHeight = Utils.floor(feature * 4) + WATER_LEVEL - 1;

        if (totalY < height - 5)
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
        else if (height <= sandHeight + 2 && totalY <= sandHeight + 2 && totalY >= sandHeight - 2)
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
        else if (totalY == height) chunk.storeSave(inChunkX, inChunkY, inChunkZ, GRASS);
        else chunk.storeSave(inChunkX, inChunkY, inChunkZ, DIRT);
        return true;
    }

    private static boolean generateSpruceForest(Chunk chunk, int inChunkX, int inChunkY, int inChunkZ, int height, double feature, long caveBits) {
        int totalX = chunk.X << CHUNK_SIZE_BITS | inChunkX;
        int totalY = chunk.Y << CHUNK_SIZE_BITS | inChunkY;
        int totalZ = chunk.Z << CHUNK_SIZE_BITS | inChunkZ;

        if (totalY >= height) {
            boolean placedTreeBlock = genSpruceTree(chunk, height, inChunkX, inChunkY, inChunkZ, totalY, feature, FOREST_TREE_THRESHOLD, caveBits);
            if (placedTreeBlock) return true;
        }
        if (totalY > height) return false;

        int sandHeight = Utils.floor(feature * 4) + WATER_LEVEL - 1;

        if (totalY < height - 5)
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
        else if (height <= sandHeight + 2 && totalY <= sandHeight + 2 && totalY >= sandHeight - 2)
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
        else if (totalY == height) chunk.storeSave(inChunkX, inChunkY, inChunkZ, GRASS);
        else chunk.storeSave(inChunkX, inChunkY, inChunkZ, DIRT);
        return true;
    }

    private static boolean generateDarkOakForest(Chunk chunk, int inChunkX, int inChunkY, int inChunkZ, int height, double feature, long caveBits) {
        int totalX = chunk.X << CHUNK_SIZE_BITS | inChunkX;
        int totalY = chunk.Y << CHUNK_SIZE_BITS | inChunkY;
        int totalZ = chunk.Z << CHUNK_SIZE_BITS | inChunkZ;

        int sandHeight = Utils.floor(Math.abs(feature * 4)) + WATER_LEVEL - 1;

        if (totalY >= height) {
            boolean placedTreeBlock = genDarkOakTree(chunk, height, inChunkX, inChunkY, inChunkZ, totalY, feature, FOREST_TREE_THRESHOLD, caveBits);
            if (placedTreeBlock) return true;
        }
        if (totalY > height) return false;

        if (totalY < height - 5)
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
        else if (height <= sandHeight + 2 && totalY <= sandHeight + 2 && totalY >= sandHeight - 2)
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
        else if (totalY == height) chunk.storeSave(inChunkX, inChunkY, inChunkZ, GRASS);
        else chunk.storeSave(inChunkX, inChunkY, inChunkZ, DIRT);
        return true;
    }

    private static boolean generateDesert(Chunk chunk, int inChunkX, int inChunkY, int inChunkZ, int height, double feature, long caveBits) {
        int totalX = chunk.X << CHUNK_SIZE_BITS | inChunkX;
        int totalY = chunk.Y << CHUNK_SIZE_BITS | inChunkY;
        int totalZ = chunk.Z << CHUNK_SIZE_BITS | inChunkZ;

        if (totalY >= height) generateCactus(chunk, height, inChunkX, inChunkY, inChunkZ, totalY, feature, caveBits);
        if (totalY > height) return false;

        if (totalY < height - 5)
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
        else chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
        return true;
    }

    private static boolean generateWasteLand(Chunk chunk, int inChunkX, int inChunkY, int inChunkZ, int height, double feature, long caveBits) {
        int totalX = chunk.X << CHUNK_SIZE_BITS | inChunkX;
        int totalY = chunk.Y << CHUNK_SIZE_BITS | inChunkY;
        int totalZ = chunk.Z << CHUNK_SIZE_BITS | inChunkZ;

        if (totalY >= height) {
            boolean placedCactusBlock = generateCactus(chunk, height, inChunkX, inChunkY, inChunkZ, totalY, feature, caveBits);
            boolean placedTreeBlock = genOakTree(chunk, height, inChunkX, inChunkY, inChunkZ, totalY, feature, WASTELAND_FEATURE_THRESHOLD, caveBits);
            if (placedCactusBlock || placedTreeBlock) return true;
        }
        if (totalY > height) return false;

        if (totalY < height - 5)
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
        else chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingDirtType(totalX, totalY, totalZ));
        return true;
    }

    private static boolean generateSnowyPlains(Chunk chunk, int inChunkX, int inChunkY, int inChunkZ, int height, double feature, long caveBits) {
        int totalX = chunk.X << CHUNK_SIZE_BITS | inChunkX;
        int totalY = chunk.Y << CHUNK_SIZE_BITS | inChunkY;
        int totalZ = chunk.Z << CHUNK_SIZE_BITS | inChunkZ;

        if (totalY >= height) {
            boolean placedTreeBlock = genSpruceTree(chunk, height, inChunkX, inChunkY, inChunkZ, totalY, feature, PLAINS_TREE_THRESHOLD, caveBits);
            if (placedTreeBlock) return true;
        }
        if (totalY > height) return false;

        int sandHeight = Utils.floor(feature * 4) + WATER_LEVEL - 1;

        if (totalY < height - 5)
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
        else if (totalY <= sandHeight + 2 && totalY >= sandHeight - 2)
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
        else chunk.storeSave(inChunkX, inChunkY, inChunkZ, SNOW);
        return true;
    }

    private static boolean generateSnowySpruceForest(Chunk chunk, int inChunkX, int inChunkY, int inChunkZ, int height, double feature, long caveBits) {
        int totalX = chunk.X << CHUNK_SIZE_BITS | inChunkX;
        int totalY = chunk.Y << CHUNK_SIZE_BITS | inChunkY;
        int totalZ = chunk.Z << CHUNK_SIZE_BITS | inChunkZ;

        if (totalY >= height) {
            boolean placedTreeBlock = genSpruceTree(chunk, height, inChunkX, inChunkY, inChunkZ, totalY, feature, FOREST_TREE_THRESHOLD, caveBits);
            if (placedTreeBlock) return true;
        }
        if (totalY > height) return false;

        int sandHeight = Utils.floor(feature * 4) + WATER_LEVEL - 1;

        if (totalY < height - 5)
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
        else if (totalY <= sandHeight + 2 && totalY >= sandHeight - 2)
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
        else chunk.storeSave(inChunkX, inChunkY, inChunkZ, SNOW);
        return true;
    }

    private static boolean generateOcean(Chunk chunk, int inChunkX, int inChunkY, int inChunkZ, int height, double feature, long ignoredCaveBits) {
        int totalX = chunk.X << CHUNK_SIZE_BITS | inChunkX;
        int totalY = chunk.Y << CHUNK_SIZE_BITS | inChunkY;
        int totalZ = chunk.Z << CHUNK_SIZE_BITS | inChunkZ;

        if (totalY > height) return false;

        int sandHeight = Utils.floor(feature * 4) + WATER_LEVEL - 5;

        if (totalY > sandHeight) chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
        else if (totalY > height - 5)
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, getOceanFloorBlock(totalX, totalY, totalZ));
        else chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
        return true;
    }

    private static boolean generateWarmOcean(Chunk chunk, int inChunkX, int inChunkY, int inChunkZ, int height, double feature, long ignoredCaveBits) {
        int totalX = chunk.X << CHUNK_SIZE_BITS | inChunkX;
        int totalY = chunk.Y << CHUNK_SIZE_BITS | inChunkY;
        int totalZ = chunk.Z << CHUNK_SIZE_BITS | inChunkZ;

        if (totalY > height) return false;

        int sandHeight = Utils.floor(feature * 4) + WATER_LEVEL - 5;

        if (totalY > sandHeight) chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
        else if (totalY > height - 5)
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, getWarmOceanFloorBlocK(totalX, totalY, totalZ));
        else chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
        return true;
    }

    private static boolean generateColdOcean(Chunk chunk, int inChunkX, int inChunkY, int inChunkZ, int height, double feature, long ignoredCaveBits) {
        int totalX = chunk.X << CHUNK_SIZE_BITS | inChunkX;
        int totalY = chunk.Y << CHUNK_SIZE_BITS | inChunkY;
        int totalZ = chunk.Z << CHUNK_SIZE_BITS | inChunkZ;

        int iceHeight = Math.min(getIceHeight(totalX, totalZ, feature), WATER_LEVEL - height);
        if (totalY > WATER_LEVEL - iceHeight && totalY < WATER_LEVEL + iceHeight) {
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingIceType(totalX, totalY, totalZ));
            return true;
        }
        if (totalY > height) return false;

        int sandHeight = Utils.floor(feature * 4) + WATER_LEVEL - 5;

        if (totalY > sandHeight) chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
        else if (totalY > height - 5)
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, getColdOceanFloorBlock(totalX, totalY, totalZ));
        else chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
        return true;
    }

    private static boolean generateMountain(Chunk chunk, int inChunkX, int inChunkY, int inChunkZ, int height, double feature, long ignoredCaveBits) {
        int totalX = chunk.X << CHUNK_SIZE_BITS | inChunkX;
        int totalY = chunk.Y << CHUNK_SIZE_BITS | inChunkY;
        int totalZ = chunk.Z << CHUNK_SIZE_BITS | inChunkZ;

        if (totalY > height) return false;

        int snowHeight = Utils.floor(feature * 32 + SNOW_LEVEL);
        int grassHeight = Utils.floor(feature * 32) + WATER_LEVEL;

        if (totalY > snowHeight && totalY > height - 5) chunk.storeSave(inChunkX, inChunkY, inChunkZ, SNOW);
        else if (totalY == height && height <= grassHeight) chunk.storeSave(inChunkX, inChunkY, inChunkZ, GRASS);
        else if (totalY < height && totalY > height - 5 && height <= grassHeight)
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, DIRT);
        else chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
        return true;
    }

    private static boolean generateSnowyMountain(Chunk chunk, int inChunkX, int inChunkY, int inChunkZ, int height, double feature, long ignoredCaveBits) {
        int totalX = chunk.X << CHUNK_SIZE_BITS | inChunkX;
        int totalY = chunk.Y << CHUNK_SIZE_BITS | inChunkY;
        int totalZ = chunk.Z << CHUNK_SIZE_BITS | inChunkZ;

        if (totalY > height) return false;

        int iceHeight = Utils.floor(feature * 32 + ICE_LEVEL);

        if (totalY > iceHeight && totalY > height - 5)
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingIceType(totalX, totalY, totalZ));
        else if (totalY > height - 5) chunk.storeSave(inChunkX, inChunkY, inChunkZ, SNOW);
        else chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
        return true;
    }

    private static boolean generateDryMountain(Chunk chunk, int inChunkX, int inChunkY, int inChunkZ, int height, double feature, long ignoredCaveBits) {
        int totalX = chunk.X << CHUNK_SIZE_BITS | inChunkX;
        int totalY = chunk.Y << CHUNK_SIZE_BITS | inChunkY;
        int totalZ = chunk.Z << CHUNK_SIZE_BITS | inChunkZ;

        if (totalY > height) return false;

        int dirtHeight = Utils.floor(feature * 32 + WATER_LEVEL);

        if (totalY > height - 5 && height <= dirtHeight)
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingDirtType(totalX, totalY, totalZ));
        else chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
        return true;
    }


    public static boolean genOakTree(Chunk chunk, int height, int inChunkX, int inChunkY, int inChunkZ, int totalY, double feature, double threshold, long caveBits) {
        if (!(feature > threshold) || totalY >= height + OAK_TREE.length || totalY < height || height <= WATER_LEVEL || (caveBits >> ((height & CHUNK_SIZE_MASK) << 1) & 3) != NO_CAVE)
            return false;
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++)
                chunk.storeTreeBlock(inChunkX + j - 2, inChunkY, inChunkZ + i - 2, OAK_TREE[totalY - height][i][j]);
        return true;
    }

    public static boolean genSpruceTree(Chunk chunk, int height, int inChunkX, int inChunkY, int inChunkZ, int totalY, double feature, double threshold, long caveBits) {
        if (!(feature > threshold) || totalY >= height + SPRUCE_TREE.length || totalY < height || height <= WATER_LEVEL || (caveBits >> ((height & CHUNK_SIZE_MASK) << 1) & 3) != NO_CAVE)
            return false;
        for (int i = 0; i < 7; i++)
            for (int j = 0; j < 7; j++)
                chunk.storeTreeBlock(inChunkX + j - 3, inChunkY, inChunkZ + i - 3, SPRUCE_TREE[totalY - height][i][j]);
        return true;
    }

    public static boolean genDarkOakTree(Chunk chunk, int height, int inChunkX, int inChunkY, int inChunkZ, int totalY, double feature, double threshold, long caveBits) {
        if (!(feature > threshold) || totalY >= height + DARK_OAK_TREE.length || totalY < height || height <= WATER_LEVEL || (caveBits >> ((height & CHUNK_SIZE_MASK) << 1) & 3) != NO_CAVE)
            return false;
        for (int i = 0; i < 7; i++)
            for (int j = 0; j < 7; j++)
                chunk.storeTreeBlock(inChunkX + j - 3, inChunkY, inChunkZ + i - 3, DARK_OAK_TREE[totalY - height][i][j]);
        return true;
    }

    public static boolean generateCactus(Chunk chunk, int height, int inChunkX, int inChunkY, int inChunkZ, int totalY, double feature, long caveBits) {
        if (!(feature > CACTUS_THRESHOLD) || height <= WATER_LEVEL || totalY <= height || !(totalY < height + 1 + (feature - CACTUS_THRESHOLD) * 500) || (caveBits >> ((height & CHUNK_SIZE_MASK) << 1) & 3) != NO_CAVE)
            return false;
        chunk.storeSave(inChunkX, inChunkY, inChunkZ, CACTUS);
        return true;
    }


    public static void genSurroundingOakTree(Chunk chunk, int height, int inChunkX, int inChunkY, int inChunkZ, int totalY, double feature, double threshold, long caveBits) {
        if (!(feature > threshold) || totalY >= height + OAK_TREE.length || totalY < height || height <= WATER_LEVEL || (caveBits >> ((height & CHUNK_SIZE_MASK) << 1) & 3) != NO_CAVE)
            return;
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++)
                chunk.storeSurroundingChunkTreeBlock(inChunkX + j - 2, inChunkY, inChunkZ + i - 2, OAK_TREE[totalY - height][i][j]);
    }

    public static void genSurroundingSpruceTree(Chunk chunk, int height, int inChunkX, int inChunkY, int inChunkZ, int totalY, double feature, double threshold, long caveBits) {
        if (!(feature > threshold) || totalY >= height + SPRUCE_TREE.length || totalY < height || height <= WATER_LEVEL || (caveBits >> ((height & CHUNK_SIZE_MASK) << 1) & 3) != NO_CAVE)
            return;
        for (int i = 0; i < 7; i++)
            for (int j = 0; j < 7; j++)
                chunk.storeSurroundingChunkTreeBlock(inChunkX + j - 3, inChunkY, inChunkZ + i - 3, SPRUCE_TREE[totalY - height][i][j]);
    }

    public static void genSurroundingDarkOakTree(Chunk chunk, int height, int inChunkX, int inChunkY, int inChunkZ, int totalY, double feature, double threshold, long caveBits) {
        if (!(feature > threshold) || totalY >= height + DARK_OAK_TREE.length || totalY < height || height <= WATER_LEVEL || (caveBits >> ((height & CHUNK_SIZE_MASK) << 1) & 3) != NO_CAVE)
            return;
        for (int i = 0; i < 7; i++)
            for (int j = 0; j < 7; j++)
                chunk.storeSurroundingChunkTreeBlock(inChunkX + j - 3, inChunkY, inChunkZ + i - 3, DARK_OAK_TREE[totalY - height][i][j]);
    }


    public static int getHeightMapValue(int totalX, int totalZ) {
        double height;
        height = OpenSimplex2S.noise3_ImproveXY(SEED, totalX * HEIGHT_MAP_FREQUENCY, totalZ * HEIGHT_MAP_FREQUENCY, 0);
        height += OpenSimplex2S.noise3_ImproveXY(SEED + 1, totalX * HEIGHT_MAP_FREQUENCY * 2, totalZ * HEIGHT_MAP_FREQUENCY * 2, 0) * 0.5;
        height += OpenSimplex2S.noise3_ImproveXY(SEED + 2, totalX * HEIGHT_MAP_FREQUENCY * 4, totalZ * HEIGHT_MAP_FREQUENCY * 4, 0) * 0.25;

        double erosion;
        erosion = OpenSimplex2S.noise3_ImproveXY(SEED + 9, totalX * EROSION_FREQUENCY, totalZ * EROSION_FREQUENCY, 0) * 0.9588;
        if (erosion > 0.0)
            erosion += OpenSimplex2S.noise3_ImproveXY(SEED + 10, totalX * EROSION_FREQUENCY * 50, totalZ * EROSION_FREQUENCY * 50, 0) * 0.0411;

        return getHeight(height * 0.5, erosion);
    }

    public static double[][] heightMap(int chunkX, int chunkZ) {
        double[][] heightMap = new double[CHUNK_SIZE][CHUNK_SIZE];
        for (int mapX = 0; mapX < CHUNK_SIZE; mapX++)
            for (int mapZ = 0; mapZ < CHUNK_SIZE; mapZ++) {
                int currentX = chunkX << CHUNK_SIZE_BITS | mapX;
                int currentZ = chunkZ << CHUNK_SIZE_BITS | mapZ;
                double height;
                height = OpenSimplex2S.noise3_ImproveXY(SEED, currentX * HEIGHT_MAP_FREQUENCY, currentZ * HEIGHT_MAP_FREQUENCY, 0);
                height += OpenSimplex2S.noise3_ImproveXY(SEED + 1, currentX * HEIGHT_MAP_FREQUENCY * 2, currentZ * HEIGHT_MAP_FREQUENCY * 2, 0) * 0.5;
                height += OpenSimplex2S.noise3_ImproveXY(SEED + 2, currentX * HEIGHT_MAP_FREQUENCY * 4, currentZ * HEIGHT_MAP_FREQUENCY * 4, 0) * 0.25;
                heightMap[mapX][mapZ] = height * 0.5;
            }
        return heightMap;
    }

    public static double[][] temperatureMap(int chunkX, int chunkZ) {
        double[][] temperatureMap = new double[CHUNK_SIZE][CHUNK_SIZE];
        for (int mapX = 0; mapX < CHUNK_SIZE; mapX++)
            for (int mapZ = 0; mapZ < CHUNK_SIZE; mapZ++) {
                int currentX = chunkX << CHUNK_SIZE_BITS | mapX;
                int currentZ = chunkZ << CHUNK_SIZE_BITS | mapZ;
                double temperature;
                temperature = OpenSimplex2S.noise3_ImproveXY(SEED + 5, currentX * TEMPERATURE_FREQUENCY, currentZ * TEMPERATURE_FREQUENCY, 0) * 0.8888;
                temperature += OpenSimplex2S.noise3_ImproveXY(SEED + 6, currentX * TEMPERATURE_FREQUENCY * 50, currentZ * TEMPERATURE_FREQUENCY * 50, 0) * 0.1111;
                temperatureMap[mapX][mapZ] = temperature;
            }
        return temperatureMap;
    }

    public static double[][] humidityMap(int chunkX, int chunkZ) {
        double[][] humidityMap = new double[CHUNK_SIZE][CHUNK_SIZE];
        for (int mapX = 0; mapX < CHUNK_SIZE; mapX++)
            for (int mapZ = 0; mapZ < CHUNK_SIZE; mapZ++) {
                int currentX = chunkX << CHUNK_SIZE_BITS | mapX;
                int currentZ = chunkZ << CHUNK_SIZE_BITS | mapZ;
                double humidity;
                humidity = OpenSimplex2S.noise3_ImproveXY(SEED + 7, currentX * HUMIDITY_FREQUENCY, currentZ * HUMIDITY_FREQUENCY, 0) * 0.8888;
                humidity += OpenSimplex2S.noise3_ImproveXY(SEED + 8, currentX * HUMIDITY_FREQUENCY * 50, currentZ * HUMIDITY_FREQUENCY * 50, 0) * 0.1111;
                humidityMap[mapX][mapZ] = humidity;
            }
        return humidityMap;
    }

    public static double[][] erosionMap(int chunkX, int chunkZ) {
        double[][] erosionMap = new double[CHUNK_SIZE][CHUNK_SIZE];
        for (int mapX = 0; mapX < CHUNK_SIZE; mapX++)
            for (int mapZ = 0; mapZ < CHUNK_SIZE; mapZ++) {
                int currentX = chunkX << CHUNK_SIZE_BITS | mapX;
                int currentZ = chunkZ << CHUNK_SIZE_BITS | mapZ;
                double erosion;
                erosion = OpenSimplex2S.noise3_ImproveXY(SEED + 9, currentX * EROSION_FREQUENCY, currentZ * EROSION_FREQUENCY, 0) * 0.9588;
                if (erosion > 0.0)
                    erosion += OpenSimplex2S.noise3_ImproveXY(SEED + 10, currentX * EROSION_FREQUENCY * 50, currentZ * EROSION_FREQUENCY * 50, 0) * 0.0411;
                erosionMap[mapX][mapZ] = erosion;
            }
        return erosionMap;
    }

    public static double[][] featureMap(int chunkX, int chunkZ) {
        double[][] featureMap = new double[CHUNK_SIZE][CHUNK_SIZE];
        Random random = new Random(GameLogic.getChunkId(chunkX, 0, chunkZ));
        for (int mapX = 0; mapX < CHUNK_SIZE; mapX++)
            for (int mapZ = 0; mapZ < CHUNK_SIZE; mapZ++) {
                featureMap[mapX][mapZ] = random.nextDouble();
            }
        return featureMap;
    }


    private static int getCaveType(int x, int y, int z) {
        double noodleCaveHeightBias = Math.max(y, 0) * NOODLE_CAVE_HEIGHT_BIAS;
        double blobCaveHeightBias = Math.max(y, 0) * BLOB_CAVE_CAVE_HEIGHT_BIAS;

        // Air cave
        double noise1 = OpenSimplex2S.noise3_ImproveXY(SEED, x * AIR_NOODLE_CAVE_FREQUENCY, y * AIR_NOODLE_CAVE_FREQUENCY, z * AIR_NOODLE_CAVE_FREQUENCY) + noodleCaveHeightBias;
        double noise2 = OpenSimplex2S.noise3_ImproveXY(SEED + 100, x * AIR_NOODLE_CAVE_FREQUENCY, y * AIR_NOODLE_CAVE_FREQUENCY, z * AIR_NOODLE_CAVE_FREQUENCY) + noodleCaveHeightBias;

        double noodleCaveValue = noise1 * noise1 + noise2 * noise2;

        double blobCaveNoise = OpenSimplex2S.noise3_ImproveXY(SEED + 200, x * AIR_BLOB_CAVE_FREQUENCY, y * AIR_BLOB_CAVE_FREQUENCY, z * AIR_BLOB_CAVE_FREQUENCY) * 0.5555;
        blobCaveNoise += OpenSimplex2S.noise3_ImproveXY(SEED + 300, x * 0.02, y * 0.02, z * 0.02) * 0.4444;
        blobCaveNoise -= blobCaveHeightBias;

        boolean insideCave = blobCaveNoise > AIR_BLOB_CAVE_THRESHOLD || noodleCaveValue < AIR_NOODLE_CAVE_THRESHOLD;

        if ((blobCaveNoise > AIR_BLOB_CAVE_THRESHOLD - 0.05 || noodleCaveValue < AIR_NOODLE_CAVE_THRESHOLD + 0.009) && !insideCave)
            return NO_CAVE;
        if (insideCave) return AIR_CAVE;

        // Water cave
        noise1 = OpenSimplex2S.noise3_ImproveXY(SEED + 400, x * WATER_NOODLE_CAVE_FREQUENCY, y * WATER_NOODLE_CAVE_FREQUENCY, z * WATER_NOODLE_CAVE_FREQUENCY) + noodleCaveHeightBias;
        noise2 = OpenSimplex2S.noise3_ImproveXY(SEED + 500, x * WATER_NOODLE_CAVE_FREQUENCY, y * WATER_NOODLE_CAVE_FREQUENCY, z * WATER_NOODLE_CAVE_FREQUENCY) + noodleCaveHeightBias;

        noodleCaveValue = noise1 * noise1 + noise2 * noise2;

        blobCaveNoise = OpenSimplex2S.noise3_ImproveXY(SEED + 600, x * WATER_BLOB_CAVE_FREQUENCY, y * WATER_BLOB_CAVE_FREQUENCY, z * WATER_BLOB_CAVE_FREQUENCY) * 0.5555;
        blobCaveNoise += OpenSimplex2S.noise3_ImproveXY(SEED + 700, x * 0.02, y * 0.02, z * 0.02) * 0.4444;
        blobCaveNoise -= blobCaveHeightBias;

        insideCave = blobCaveNoise > WATER_BLOB_CAVE_THRESHOLD || noodleCaveValue < WATER_NOODLE_CAVE_THRESHOLD;

        if ((blobCaveNoise > WATER_BLOB_CAVE_THRESHOLD - 0.05 || noodleCaveValue < WATER_NOODLE_CAVE_THRESHOLD + 0.009) && !insideCave)
            return NO_CAVE;
        if (insideCave) return WATER_CAVE;

        // Lava cave
        noise1 = OpenSimplex2S.noise3_ImproveXY(SEED + 800, x * LAVA_NOODLE_CAVE_FREQUENCY, y * LAVA_NOODLE_CAVE_FREQUENCY, z * LAVA_NOODLE_CAVE_FREQUENCY) + noodleCaveHeightBias;
        noise2 = OpenSimplex2S.noise3_ImproveXY(SEED + 900, x * LAVA_NOODLE_CAVE_FREQUENCY, y * LAVA_NOODLE_CAVE_FREQUENCY, z * LAVA_NOODLE_CAVE_FREQUENCY) + noodleCaveHeightBias;

        noodleCaveValue = noise1 * noise1 + noise2 * noise2;

        insideCave = noodleCaveValue < LAVA_NOODLE_CAVE_THRESHOLD;

        if (insideCave) return LAVA_CAVE;

        return NO_CAVE;
    }

    private static long[] generateCaveBitMap(Chunk chunk, int[][] heightMap) {
        long[] bitMap = new long[CHUNK_SIZE * CHUNK_SIZE];
        int chunkX = chunk.X << CHUNK_SIZE_BITS;
        int chunkY = chunk.Y << CHUNK_SIZE_BITS;
        int chunkZ = chunk.Z << CHUNK_SIZE_BITS;

        for (int x = chunkX; x < CHUNK_SIZE + chunkX; x += 4)
            for (int y = chunkY; y < CHUNK_SIZE + chunkY; y += 4)
                for (int z = chunkZ; z < CHUNK_SIZE + chunkZ; z += 4) {
                    int inChunkX = x & CHUNK_SIZE_MASK;
                    int inChunkY = y & CHUNK_SIZE_MASK;
                    int inChunkZ = z & CHUNK_SIZE_MASK;

                    if (y > heightMap[inChunkX][inChunkZ]) continue;

                    int cornerValues = 0;

                    cornerValues |= getCaveType(x, y, z);
                    cornerValues |= getCaveType(x, y, z + 3) << 2;
                    cornerValues |= getCaveType(x, y + 3, z) << 4;
                    cornerValues |= getCaveType(x, y + 3, z + 3) << 6;
                    cornerValues |= getCaveType(x + 3, y, z) << 8;
                    cornerValues |= getCaveType(x + 3, y, z + 3) << 10;
                    cornerValues |= getCaveType(x + 3, y + 3, z) << 12;
                    cornerValues |= getCaveType(x + 3, y + 3, z + 3) << 14;

                    if (cornerValues == 0) continue; // Only no cave
                    if (cornerValues == 0x5555) { // Only air cave
                        long mask = 0x55L << (inChunkY << 1);
                        for (int i = 0; i < 4; i++)
                            for (int j = 0; j < 4; j++)
                                bitMap[i + inChunkX << CHUNK_SIZE_BITS | inChunkZ + j] |= mask;
                    } else if (cornerValues == 0xFFFF) { // Only lava cave
                        long mask = 0xFFL << (inChunkY << 1);
                        for (int i = 0; i < 4; i++)
                            for (int j = 0; j < 4; j++)
                                bitMap[i + inChunkX << CHUNK_SIZE_BITS | inChunkZ + j] |= mask;
                    } else if (cornerValues == 0xAAAA) { // Only water cave
                        long mask = 0xAAL << (inChunkY << 1);
                        for (int i = 0; i < 4; i++)
                            for (int j = 0; j < 4; j++)
                                bitMap[i + inChunkX << CHUNK_SIZE_BITS | inChunkZ + j] |= mask;
                    } else // Mix of caves need to calculate everything
                        for (int i = 0; i < 4; i++)
                            for (int j = 0; j < 4; j++)
                                for (int k = 0; k < 4; k++)
                                    bitMap[i + inChunkX << CHUNK_SIZE_BITS | inChunkZ + j] |= (long) getCaveType(x + i, y + k, z + j) << (inChunkY + k << 1);
                }

        return bitMap;
    }

    private static short getGeneratingStoneType(int x, int y, int z) {
        double noise = OpenSimplex2S.noise3_ImproveXY(SEED, x * STONE_TYPE_FREQUENCY, y * STONE_TYPE_FREQUENCY, z * STONE_TYPE_FREQUENCY);
        if (Math.abs(noise) < ANDESITE_THRESHOLD) return ANDESITE;
        if (noise > SLATE_THRESHOLD) return SLATE;
        return STONE;
    }

    private static short getOceanFloorBlock(int x, int y, int z) {
        double noise = OpenSimplex2S.noise3_ImproveXY(SEED, x * MUD_TYPE_FREQUENCY, y * MUD_TYPE_FREQUENCY, z * MUD_TYPE_FREQUENCY);
        if (Math.abs(noise) < GRAVEL_THRESHOLD) return GRAVEL;
        if (noise > CLAY_THRESHOLD) return CLAY;
        if (noise < SAND_THRESHOLD) return SAND;
        return MUD;
    }

    private static short getWarmOceanFloorBlocK(int x, int y, int z) {
        double noise = OpenSimplex2S.noise3_ImproveXY(SEED, x * MUD_TYPE_FREQUENCY, y * MUD_TYPE_FREQUENCY, z * MUD_TYPE_FREQUENCY);
        if (Math.abs(noise) < GRAVEL_THRESHOLD) return GRAVEL;
        if (noise > CLAY_THRESHOLD) return CLAY;
        if (noise < MUD_THRESHOLD) return MUD;
        return SAND;
    }

    public static short getColdOceanFloorBlock(int x, int y, int z) {
        double noise = OpenSimplex2S.noise3_ImproveXY(SEED, x * MUD_TYPE_FREQUENCY, y * MUD_TYPE_FREQUENCY, z * MUD_TYPE_FREQUENCY);
        if (Math.abs(noise) < GRAVEL_THRESHOLD) return GRAVEL;
        if (noise > CLAY_THRESHOLD) return CLAY;
        if (noise < MUD_THRESHOLD) return MUD;
        return GRAVEL;
    }

    private static short getGeneratingDirtType(int x, int y, int z) {
        double noise = OpenSimplex2S.noise3_ImproveXY(SEED, x * DIRT_TYPE_FREQUENCY, y * DIRT_TYPE_FREQUENCY, z * DIRT_TYPE_FREQUENCY);
        if (Math.abs(noise) < COURSE_DIRT_THRESHOLD) return COURSE_DIRT;
        return DIRT;
    }

    private static short getGeneratingIceType(int x, int y, int z) {
        double noise = OpenSimplex2S.noise3_ImproveXY(SEED, x * ICE_TYPE_FREQUENCY, y * ICE_TYPE_FREQUENCY, z * ICE_TYPE_FREQUENCY);
        if (noise > HEAVY_ICE_THRESHOLD) return HEAVY_ICE;
        return ICE;
    }


    private static int getIceHeight(int x, int z, double feature) {
        double noise = OpenSimplex2S.noise3_ImproveXY(SEED, x * ICE_BERG_FREQUENCY, z * ICE_BERG_FREQUENCY, 0.0);
        if (noise > ICE_BERG_THRESHOLD) return Math.max(Utils.floor((noise - ICE_BERG_THRESHOLD) * ICE_BERG_HEIGHT), 1);
        if (noise > ICE_PLANE_THRESHOLD) return 1;
        return feature > 0.9 ? 1 : 0;
    }

    public static int getHeight(double height, double erosion) {
        height = height * 0.5 + 0.5;

        double modifier = 0.0;
        if (erosion > MOUNTAIN_THRESHOLD)
            modifier = (erosion - MOUNTAIN_THRESHOLD) * (erosion - MOUNTAIN_THRESHOLD) * 1000;
        else if (erosion < OCEAN_THRESHOLD)
            modifier = (erosion - OCEAN_THRESHOLD) * (erosion - OCEAN_THRESHOLD) * -1000;

        return Utils.floor(height * MAX_TERRAIN_HEIGHT_DIFFERENCE + modifier) + WATER_LEVEL - 15;
    }


    public static int getBiome(double temperature, double humidity, double erosion, int resultingHeight) {
        if (resultingHeight <= WATER_LEVEL) {
            if (temperature > 0.4) return WARM_OCEAN;
            else if (temperature < -0.4) return COLD_OCEAN;
            return OCEAN;
        } else if (erosion > MOUNTAIN_THRESHOLD) {
            if (temperature > 0.4) return DRY_MOUNTAIN;
            else if (temperature < -0.4) return SNOWY_MOUNTAIN;
            return MOUNTAIN;
        } else if (temperature > 0.4) {
            if (humidity < -0.4) return DESERT;
            else if (humidity < 0.3) return WASTELAND;
            else return DARK_OAK_FOREST;
        } else if (temperature < -0.4) {
            if (humidity > 0.0) return SNOWY_SPRUCE_FOREST;
            else return SNOWY_PLAINS;
        } else if (humidity > 0.3) return OAK_FOREST;
        else if (humidity < -0.4) return SPRUCE_FOREST;
        return PLAINS;
    }

    interface Biome {
        // Returns true if a block has been placed at these coordinates, false otherwise
        boolean placeBlock(Chunk chunk, int inChunkX, int inChunkY, int inChunkZ, int height, double feature, long caveBits);
    }
}
