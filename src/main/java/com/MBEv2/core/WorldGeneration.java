package com.MBEv2.core;

import com.MBEv2.core.utils.Utils;
import com.MBEv2.test.GameLogic;

import java.util.ArrayList;
import java.util.Random;

import static com.MBEv2.core.utils.Constants.*;
import static com.MBEv2.core.utils.Settings.*;

public class WorldGeneration {

    //World generation
    public static final int WATER_LEVEL = 0;
    public static final int SNOW_LEVEL = WATER_LEVEL + 91;
    public static final int ICE_LEVEL = WATER_LEVEL + 141;
    public static final int OCEAN_FLOOR_LEVEL = WATER_LEVEL - 30;
    public static final int DEEP_OCEAN_FLOOR_OFFSET = -70;
    public static final int FLATLAND_LEVEL = 30 + 15;
    public static final int RIVER_LEVEL = WATER_LEVEL - 15;

    public static final double TEMPERATURE_FREQUENCY = 0.001;
    public static final double HUMIDITY_FREQUENCY = TEMPERATURE_FREQUENCY;
    public static final double HEIGHT_MAP_FREQUENCY = 0.0025;
    public static final double EROSION_FREQUENCY = 0.001;
    public static final double CONTINENTAL_FREQUENCY = 0.00025;

    public static final double MAX_TERRAIN_HEIGHT_DIFFERENCE = 100;

    public static final double MOUNTAIN_THRESHOLD = 0.3;    // Continental
    public static final double OCEAN_THRESHOLD = -0.3;      // Continental
    public static final double FLATLAND_THRESHOLD = 0.3;    // Erosion
    public static final double RIVER_THRESHOLD = 0.2;      // Erosion

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

    public static final double PLAINS_TREE_THRESHOLD = 0.998;
    public static final double FOREST_TREE_THRESHOLD = 0.95;
    public static final double CACTUS_THRESHOLD = 0.992;
    public static final double WASTELAND_FEATURE_THRESHOLD = 0.999;

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

    public static final int DESERT = 0;
    public static final int WASTELAND = 1;
    public static final int DARK_OAK_FOREST = 2;
    public static final int SNOWY_SPRUCE_FOREST = 3;
    public static final int SNOWY_PLAINS = 4;
    public static final int SPRUCE_FOREST = 5;
    public static final int PLAINS = 6;
    public static final int OAK_FOREST = 7;
    public static final int WARM_OCEAN = 8;
    public static final int COLD_OCEAN = 9;
    public static final int OCEAN = 10;
    public static final int DRY_MOUNTAIN = 11;
    public static final int SNOWY_MOUNTAIN = 12;
    public static final int MOUNTAIN = 13;

    private static final Biome[] biomes = new Biome[14];

    public static void init() {
        biomes[DESERT] = WorldGeneration::generateDesert;
        biomes[WASTELAND] = WorldGeneration::generateWasteLand;
        biomes[DARK_OAK_FOREST] = WorldGeneration::generateDarkOakForest;
        biomes[SNOWY_SPRUCE_FOREST] = WorldGeneration::generateSnowySpruceForest;
        biomes[SNOWY_PLAINS] = WorldGeneration::generateSnowyPlains;
        biomes[SPRUCE_FOREST] = WorldGeneration::generateSpruceForest;
        biomes[PLAINS] = WorldGeneration::generatePlains;
        biomes[OAK_FOREST] = WorldGeneration::generateOakForest;
        biomes[WARM_OCEAN] = WorldGeneration::generateWarmOcean;
        biomes[COLD_OCEAN] = WorldGeneration::generateColdOcean;
        biomes[OCEAN] = WorldGeneration::generateOcean;
        biomes[DRY_MOUNTAIN] = WorldGeneration::generateDryMountain;
        biomes[SNOWY_MOUNTAIN] = WorldGeneration::generateSnowyMountain;
        biomes[MOUNTAIN] = WorldGeneration::generateMountain;
    }

    public static void generateSurroundingChunkTreeBlocks(Chunk chunk) {
        if (chunk.isGenerated()) return;
        double[][] heightMap = heightMapPadded(chunk.X, chunk.Z);
        double[][] temperatureMap = temperatureMap(chunk.X, chunk.Z);
        double[][] humidityMap = humidityMap(chunk.X, chunk.Z);
        double[][] erosionMap = erosionMapPadded(chunk.X, chunk.Z);
        double[][] featureMap = featureMap(chunk.X, chunk.Z);
        double[][] continentalMap = continentalMapPadded(chunk.X, chunk.Z);

        int[][] resultingHeightMap = getResultingHeightMap(heightMap, erosionMap, continentalMap);

        generateSurroundingChunkTreeBlocks(chunk, resultingHeightMap, temperatureMap, humidityMap, erosionMap, featureMap, continentalMap);
    }

    public static void generateSurroundingChunkTreeBlocks(Chunk chunk, int[][] heightMap, double[][] temperatureMap, double[][] humidityMap, double[][] erosionMap, double[][] featureMap, double[][] continentalMap) {
        long[] caveBitMap = generateCaveBitMap(chunk, heightMap);

        for (int inChunkX = 0; inChunkX < CHUNK_SIZE; inChunkX++)
            for (int inChunkZ = 0; inChunkZ < CHUNK_SIZE; inChunkZ++) {
                double temperature = temperatureMap[inChunkX][inChunkZ];
                double humidity = humidityMap[inChunkX][inChunkZ];
                double erosion = erosionMap[inChunkX][inChunkZ];
                double feature = featureMap[inChunkX][inChunkZ];
                double continental = continentalMap[inChunkX][inChunkZ];

                int resultingHeight = heightMap[inChunkX + 1][inChunkZ + 1];
                int biome = getBiome(temperature, humidity, erosion, continental, resultingHeight);
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
        double[][] heightMap = heightMapPadded(chunk.X, chunk.Z);
        double[][] temperatureMap = temperatureMap(chunk.X, chunk.Z);
        double[][] humidityMap = humidityMap(chunk.X, chunk.Z);
        double[][] erosionMap = erosionMapPadded(chunk.X, chunk.Z);
        double[][] featureMap = featureMap(chunk.X, chunk.Z);
        double[][] continentalMap = continentalMapPadded(chunk.X, chunk.Z);

        int[][] resultingHeightMap = getResultingHeightMap(heightMap, erosionMap, continentalMap);

        generate(chunk, resultingHeightMap, temperatureMap, humidityMap, erosionMap, featureMap, continentalMap);
    }

    public static void generate(Chunk chunk, int[][] heightMap, double[][] temperatureMap, double[][] humidityMap, double[][] erosionMap, double[][] featureMap, double[][] continentalMap) {
        if (chunk.isGenerated()) return;
        chunk.setGenerated();

        long[] caveBitMap = generateCaveBitMap(chunk, heightMap);

        for (int inChunkX = 0; inChunkX < CHUNK_SIZE; inChunkX++)
            for (int inChunkZ = 0; inChunkZ < CHUNK_SIZE; inChunkZ++) {
                double temperature = temperatureMap[inChunkX][inChunkZ];
                double humidity = humidityMap[inChunkX][inChunkZ];
                double erosion = erosionMap[inChunkX][inChunkZ];
                double feature = featureMap[inChunkX][inChunkZ];
                double continental = continentalMap[inChunkX][inChunkZ];

                int resultingHeight = heightMap[inChunkX + 1][inChunkZ + 1];
                Biome biome = biomes[getBiome(temperature, humidity, erosion, continental, resultingHeight)];
                long caveBits = caveBitMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ];

                generateBiome(biome, chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBits);
            }

        ArrayList<Integer> toGenerateBlocks = Chunk.removeToGenerateBlocks(chunk.id);
        if (toGenerateBlocks != null) {
            for (long data : toGenerateBlocks) {
                short block = (short) (data >> 16 & 0xFFFF);
                int inChunkX = (int) (data >> CHUNK_SIZE_BITS * 2 & CHUNK_SIZE_MASK);
                int inChunkY = (int) (data >> CHUNK_SIZE_BITS & CHUNK_SIZE_MASK);
                int inChunkZ = (int) (data & CHUNK_SIZE_MASK);

                int index = inChunkX << CHUNK_SIZE_BITS * 2 | inChunkZ << CHUNK_SIZE_BITS | inChunkY;
                if (chunk.getSaveBlock(index) != AIR && Block.isLeaveType(block)) continue;
                chunk.storeSave(inChunkX, inChunkY, inChunkZ, block);
            }
        }

        chunk.optimizeBlockStorage();
    }

    private static void generateBiome(Biome biome, Chunk chunk, int inChunkX, int inChunkZ, int height, double feature, long caveBits) {
        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = chunk.Y << CHUNK_SIZE_BITS | inChunkY;
            boolean placedBlock = false;

            int caveType = (int) (caveBits >> (inChunkY << 1) & 3);
            // Either there is no cave OR a thin layer separating caves form the ocean floor                                            OR everything above surface (trees)
            if (caveType == NO_CAVE || height <= WATER_LEVEL && totalY >= height - 1 && totalY <= WATER_LEVEL && caveType != WATER_CAVE || totalY > height)
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
        if (feature < threshold || totalY >= height + OAK_TREE.length || totalY < height || height <= WATER_LEVEL || (caveBits >> ((height + 1 & CHUNK_SIZE_MASK) << 1) & 3) != NO_CAVE)
            return false;
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++)
                chunk.storeTreeBlock(inChunkX + j - 2, inChunkY, inChunkZ + i - 2, OAK_TREE[totalY - height][i][j]);
        return true;
    }

    public static boolean genSpruceTree(Chunk chunk, int height, int inChunkX, int inChunkY, int inChunkZ, int totalY, double feature, double threshold, long caveBits) {
        if (feature < threshold || totalY >= height + SPRUCE_TREE.length || totalY < height || height <= WATER_LEVEL || (caveBits >> ((height + 1 & CHUNK_SIZE_MASK) << 1) & 3) != NO_CAVE)
            return false;
        for (int i = 0; i < 7; i++)
            for (int j = 0; j < 7; j++)
                chunk.storeTreeBlock(inChunkX + j - 3, inChunkY, inChunkZ + i - 3, SPRUCE_TREE[totalY - height][i][j]);
        return true;
    }

    public static boolean genDarkOakTree(Chunk chunk, int height, int inChunkX, int inChunkY, int inChunkZ, int totalY, double feature, double threshold, long caveBits) {
        if (feature < threshold || totalY >= height + DARK_OAK_TREE.length || totalY < height || height <= WATER_LEVEL || (caveBits >> ((height + 1 & CHUNK_SIZE_MASK) << 1) & 3) != NO_CAVE)
            return false;
        for (int i = 0; i < 7; i++)
            for (int j = 0; j < 7; j++)
                chunk.storeTreeBlock(inChunkX + j - 3, inChunkY, inChunkZ + i - 3, DARK_OAK_TREE[totalY - height][i][j]);
        return true;
    }

    public static boolean generateCactus(Chunk chunk, int height, int inChunkX, int inChunkY, int inChunkZ, int totalY, double feature, long caveBits) {
        if (feature < CACTUS_THRESHOLD || height <= WATER_LEVEL || totalY <= height || !(totalY < height + 1 + (feature - CACTUS_THRESHOLD) * 500) || (caveBits >> ((height + 1 & CHUNK_SIZE_MASK) << 1) & 3) != NO_CAVE)
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


    public static double[][] heightMapPadded(int chunkX, int chunkZ) {
        double[][] heightMap = new double[CHUNK_SIZE + 2][CHUNK_SIZE + 2];
        for (int mapX = 0; mapX < CHUNK_SIZE + 2; mapX++)
            for (int mapZ = 0; mapZ < CHUNK_SIZE + 2; mapZ++) {
                int currentX = (chunkX << CHUNK_SIZE_BITS) + mapX - 1;
                int currentZ = (chunkZ << CHUNK_SIZE_BITS) + mapZ - 1;
                heightMap[mapX][mapZ] = heightMapValue(currentX, currentZ);
            }
        return heightMap;
    }

    public static double heightMapValue(int totalX, int totalZ) {
        double height;
        height = OpenSimplex2S.noise3_ImproveXY(SEED - 2, totalX * HEIGHT_MAP_FREQUENCY, totalZ * HEIGHT_MAP_FREQUENCY, 0);
        height += OpenSimplex2S.noise3_ImproveXY(SEED - 1, totalX * HEIGHT_MAP_FREQUENCY * 2, totalZ * HEIGHT_MAP_FREQUENCY * 2, 0) * 0.5;
        height += OpenSimplex2S.noise3_ImproveXY(SEED, totalX * HEIGHT_MAP_FREQUENCY * 4, totalZ * HEIGHT_MAP_FREQUENCY * 4, 0) * 0.25;
        height += OpenSimplex2S.noise3_ImproveXY(SEED + 1, totalX * HEIGHT_MAP_FREQUENCY * 8, totalZ * HEIGHT_MAP_FREQUENCY * 8, 0) * 0.125;
        height += OpenSimplex2S.noise3_ImproveXY(SEED + 2, totalX * HEIGHT_MAP_FREQUENCY * 16, totalZ * HEIGHT_MAP_FREQUENCY * 16, 0) * 0.0625;
        return height;
    }

    public static double[][] temperatureMap(int chunkX, int chunkZ) {
        double[][] temperatureMap = new double[CHUNK_SIZE][CHUNK_SIZE];
        for (int mapX = 0; mapX < CHUNK_SIZE; mapX++)
            for (int mapZ = 0; mapZ < CHUNK_SIZE; mapZ++) {
                int currentX = chunkX << CHUNK_SIZE_BITS | mapX;
                int currentZ = chunkZ << CHUNK_SIZE_BITS | mapZ;
                double temperature = temperatureMapValue(currentX, currentZ);
                temperatureMap[mapX][mapZ] = temperature;
            }
        return temperatureMap;
    }

    public static double temperatureMapValue(int totalX, int totalZ) {
        double temperature;
        temperature = OpenSimplex2S.noise3_ImproveXY(SEED + 5, totalX * TEMPERATURE_FREQUENCY, totalZ * TEMPERATURE_FREQUENCY, 0) * 0.8888;
        temperature += OpenSimplex2S.noise3_ImproveXY(SEED + 6, totalX * TEMPERATURE_FREQUENCY * 50, totalZ * TEMPERATURE_FREQUENCY * 50, 0) * 0.1111;
        return temperature;
    }

    public static double[][] humidityMap(int chunkX, int chunkZ) {
        double[][] humidityMap = new double[CHUNK_SIZE][CHUNK_SIZE];
        for (int mapX = 0; mapX < CHUNK_SIZE; mapX++)
            for (int mapZ = 0; mapZ < CHUNK_SIZE; mapZ++) {
                int currentX = chunkX << CHUNK_SIZE_BITS | mapX;
                int currentZ = chunkZ << CHUNK_SIZE_BITS | mapZ;
                double humidity = humidityMapValue(currentX, currentZ);
                humidityMap[mapX][mapZ] = humidity;
            }
        return humidityMap;
    }

    public static double humidityMapValue(int totalX, int totalZ) {
        double humidity;
        humidity = OpenSimplex2S.noise3_ImproveXY(SEED + 7, totalX * HUMIDITY_FREQUENCY, totalZ * HUMIDITY_FREQUENCY, 0) * 0.8888;
        humidity += OpenSimplex2S.noise3_ImproveXY(SEED + 8, totalX * HUMIDITY_FREQUENCY * 50, totalZ * HUMIDITY_FREQUENCY * 50, 0) * 0.1111;
        return humidity;
    }

    public static double[][] erosionMapPadded(int chunkX, int chunkZ) {
        double[][] erosionMap = new double[CHUNK_SIZE + 2][CHUNK_SIZE + 2];
        for (int mapX = 0; mapX < CHUNK_SIZE + 2; mapX++)
            for (int mapZ = 0; mapZ < CHUNK_SIZE + 2; mapZ++) {
                int currentX = (chunkX << CHUNK_SIZE_BITS) + mapX - 1;
                int currentZ = (chunkZ << CHUNK_SIZE_BITS) + mapZ - 1;
                double erosion = erosionMapValue(currentX, currentZ);
                erosionMap[mapX][mapZ] = erosion;
            }
        return erosionMap;
    }

    public static double erosionMapValue(int totalX, int totalZ) {
        double erosion;
        erosion = OpenSimplex2S.noise3_ImproveXY(SEED + 9, totalX * EROSION_FREQUENCY, totalZ * EROSION_FREQUENCY, 0) * 0.9588;
        erosion += OpenSimplex2S.noise3_ImproveXY(SEED + 10, totalX * EROSION_FREQUENCY * 40, totalZ * EROSION_FREQUENCY * 40, 0) * 0.0411;
        return erosion;
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

    public static double[][] continentalMapPadded(int chunkX, int chunkZ) {
        double[][] continentalMap = new double[CHUNK_SIZE + 2][CHUNK_SIZE + 2];
        for (int mapX = 0; mapX < CHUNK_SIZE + 2; mapX++)
            for (int mapZ = 0; mapZ < CHUNK_SIZE + 2; mapZ++) {
                int currentX = (chunkX << CHUNK_SIZE_BITS) + mapX - 1;
                int currentZ = (chunkZ << CHUNK_SIZE_BITS) + mapZ - 1;
                double continental = continentalMapValue(currentX, currentZ);
                continentalMap[mapX][mapZ] = continental;
            }
        return continentalMap;
    }

    public static double continentalMapValue(int totalX, int totalZ) {
        double continental;
        continental = OpenSimplex2S.noise3_ImproveXY(SEED + 11, totalX * CONTINENTAL_FREQUENCY, totalZ * CONTINENTAL_FREQUENCY, 0) * 0.9588;
        continental += OpenSimplex2S.noise3_ImproveXY(SEED + 12, totalX * CONTINENTAL_FREQUENCY * 50, totalZ * CONTINENTAL_FREQUENCY * 50, 0) * 0.0411;
        return continental;
    }


    private static int getCaveType(int x, int y, int z) {
        double noodleCaveHeightBias = Math.max(y + 96, 0) * NOODLE_CAVE_HEIGHT_BIAS;
        double blobCaveHeightBias = Math.max(y + 96, 0) * BLOB_CAVE_CAVE_HEIGHT_BIAS;

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

                    // 4³ block is completely above the surface, ergo can't have caves
                    if (y > heightMap[inChunkX + 1][inChunkZ + 1] && y > heightMap[inChunkX + 1][inChunkZ + 4] &&
                            y > heightMap[inChunkX + 4][inChunkZ + 1] && y > heightMap[inChunkX + 4][inChunkZ + 4])
                        continue;

                    int cornerValues = 0;

                    // Compute cave values on the corners of a 4³ volume to approximate all values inside
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

        // Forces a separating layer of blocks between AIR_CAVES or LAVA_CAVES and bodies of water like Oceans and rivers
        if (chunkY <= WATER_LEVEL) // Only chunks touching water have that problem
            for (int inChunkX = 0; inChunkX < CHUNK_SIZE; inChunkX++)
                for (int inChunkZ = 0; inChunkZ < CHUNK_SIZE; inChunkZ++) {

                    int height = heightMap[inChunkX + 1][inChunkZ + 1];
                    if (chunkY > height) continue;

                    // Height map values of the 4 blocks directly adjacent to inChunkX, inChunkZ
                    int heightXMinusOne = heightMap[inChunkX][inChunkZ + 1];
                    int heightXPlusOne = heightMap[inChunkX + 2][inChunkZ + 1];
                    int heightZMinusOne = heightMap[inChunkX + 1][inChunkZ];
                    int heightZPlusOne = heightMap[inChunkX + 1][inChunkZ + 2];

                    //                  Starts at the highest value within the chunk that is also <= height
                    for (int inChunkY = height > chunkY + CHUNK_SIZE - 1 ? CHUNK_SIZE - 1 : height & CHUNK_SIZE_MASK; inChunkY >= 0; inChunkY--) {

                        int totalY = chunkY | inChunkY;
                        if (totalY > WATER_LEVEL) continue;

                        long caveBits = bitMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ];
                        byte caveType = (byte) (caveBits >> inChunkY * 2 & 3);
                        if (caveType == NO_CAVE || caveType == WATER_CAVE) continue;

                        boolean nextToWater = heightXMinusOne < totalY;
                        nextToWater = nextToWater || heightXPlusOne < totalY;
                        nextToWater = nextToWater || heightZMinusOne < totalY;
                        nextToWater = nextToWater || heightZPlusOne < totalY;

                        if (nextToWater) {
                            // Replaces the cave bits associated with inChunkY with 00 (NO_CAVE)
                            caveBits &= ~(3L << inChunkY * 2);
                            bitMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ] = caveBits;
                        } else break; // If all neighboring blocks are below the surface, so are all the blocks below
                    }
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

    public static int getResultingHeight(double height, double erosion, double continental) {
        height = (height * 0.5 + 0.5) * MAX_TERRAIN_HEIGHT_DIFFERENCE;

        double continentalModifier = getContinentalModifier(continental);
        double erosionModifier = getErosionModifier(height, erosion, continentalModifier);

        return Utils.floor(height + continentalModifier + erosionModifier) + WATER_LEVEL - 15;
    }

    private static double getContinentalModifier(double continental) {
        double continentalModifier = 0.0;
        // Mountains
        if (continental > MOUNTAIN_THRESHOLD)
            continentalModifier = (continental - MOUNTAIN_THRESHOLD) * (continental - MOUNTAIN_THRESHOLD) * 2000;
            // Normal ocean
        else if (continental < OCEAN_THRESHOLD && continental > OCEAN_THRESHOLD - 0.05)
            continentalModifier = Utils.smoothInOutQuad(-continental, -OCEAN_THRESHOLD, -OCEAN_THRESHOLD + 0.05) * OCEAN_FLOOR_LEVEL;
        else if (continental <= OCEAN_THRESHOLD - 0.05 && continental > OCEAN_THRESHOLD - 0.2)
            continentalModifier = (continental - (OCEAN_THRESHOLD - 0.05)) * 100 + OCEAN_FLOOR_LEVEL;
            // Deep Ocean
        else if (continental <= OCEAN_THRESHOLD - 0.2 && continental > OCEAN_THRESHOLD - 0.25)
            continentalModifier = Utils.smoothInOutQuad(-continental, -OCEAN_THRESHOLD + 0.2, -OCEAN_THRESHOLD + 0.25) * DEEP_OCEAN_FLOOR_OFFSET + OCEAN_FLOOR_LEVEL - 15;
        else if (continental <= OCEAN_THRESHOLD - 0.25)
            continentalModifier = (continental - (OCEAN_THRESHOLD - 0.25)) * 100 + OCEAN_FLOOR_LEVEL + DEEP_OCEAN_FLOOR_OFFSET - 15;
        return continentalModifier;
    }

    private static double getErosionModifier(double height, double erosion, double continentalModifier) {
        double erosionModifier = 0.0;
        // Elevated areas
        if (erosion < -0.25 && erosion > -0.4) erosionModifier = Utils.smoothInOutQuad(-erosion, 0.25, 0.4) * 55;
        else if (erosion <= -0.40) erosionModifier = (erosion + 0.40) * 20 + 55;
            // Flatland
        else if (erosion > FLATLAND_THRESHOLD && erosion < FLATLAND_THRESHOLD + 0.25)
            erosionModifier = -(continentalModifier + height * 0.75 - FLATLAND_LEVEL) * Utils.smoothInOutQuad(erosion, FLATLAND_THRESHOLD, FLATLAND_THRESHOLD + 0.25);
        else if (erosion >= FLATLAND_THRESHOLD + 0.25)
            erosionModifier = -height * 0.75 - continentalModifier + FLATLAND_LEVEL;
            // Rivers
        else if (Math.abs(erosion) < 0.005) erosionModifier = -height * 0.85 - continentalModifier + RIVER_LEVEL;
        else if (Math.abs(erosion) < RIVER_THRESHOLD)
            erosionModifier = -(continentalModifier + height * 0.85 - RIVER_LEVEL) * (1 - Utils.smoothInOutQuad(Math.abs(erosion), 0.005, RIVER_THRESHOLD));
        return erosionModifier;
    }

    public static int getResultingHeight(int totalX, int totalZ) {
        double height = heightMapValue(totalX, totalZ);
        double erosion = erosionMapValue(totalX, totalZ);
        double continental = continentalMapValue(totalX, totalZ);

        return getResultingHeight(height, erosion, continental);
    }

    public static int[][] getResultingHeightMap(double[][] heightMap, double[][] erosionMap, double[][] continentalMap) {
        int[][] resultingHeightMap = new int[heightMap.length][heightMap.length];
        for (int mapX = 0; mapX < heightMap.length; mapX++)
            for (int mapZ = 0; mapZ < heightMap.length; mapZ++)
                resultingHeightMap[mapX][mapZ] = WorldGeneration.getResultingHeight(heightMap[mapX][mapZ], erosionMap[mapX][mapZ], continentalMap[mapX][mapZ]);

        return resultingHeightMap;
    }


    public static int getBiome(double temperature, double humidity, double erosion, double continental, int resultingHeight) {
        if (resultingHeight <= WATER_LEVEL) {
            if (temperature > 0.4) return WARM_OCEAN;
            else if (temperature < -0.4) return COLD_OCEAN;
            return OCEAN;
        } else if (continental > MOUNTAIN_THRESHOLD && erosion < 0.2) {
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
