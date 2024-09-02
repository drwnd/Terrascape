package com.MBEv2.core;

import com.MBEv2.core.utils.Utils;
import com.MBEv2.test.GameLogic;

import java.util.ArrayList;
import java.util.Random;

import static com.MBEv2.core.utils.Constants.*;

public class WorldGeneration {

    //World generation
//    public static final long SEED = new Random().nextLong();
            public static final long SEED = 0;
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
    public static final double BLOB_CAVE_FREQUENCY = 0.008;
    public static final double BLOB_CAVE_THRESHOLD = 0.3;
    public static final double BLOB_CAVE_MAX_Y = (1 - BLOB_CAVE_THRESHOLD) / BLOB_CAVE_CAVE_HEIGHT_BIAS;

    public static final double NOODLE_CAVE_FREQUENCY = 0.01;
    public static final double NOODLE_CAVE_THRESHOLD = 0.01;
    public static final double NOODLE_CAVE_HEIGHT_BIAS = 0.004;
    public static final double NOODLE_CAVE_MAX_Y = (Math.sqrt(0.5 * NOODLE_CAVE_THRESHOLD) + 1) / NOODLE_CAVE_HEIGHT_BIAS;

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

    public static void generate(Chunk chunk) {
        if (chunk.isGenerated())
            return;
        double[][] heightMap = WorldGeneration.heightMap(chunk.getChunkX(), chunk.getChunkZ());
        double[][] temperatureMap = WorldGeneration.temperatureMap(chunk.getChunkX(), chunk.getChunkZ());
        double[][] humidityMap = WorldGeneration.humidityMap(chunk.getChunkX(), chunk.getChunkZ());
        double[][] erosionMap = WorldGeneration.erosionMap(chunk.getChunkX(), chunk.getChunkZ());
        double[][] featureMap = WorldGeneration.featureMap(chunk.getChunkX(), chunk.getChunkZ());

        int[][] resultingHeightMap = new int[CHUNK_SIZE][CHUNK_SIZE];
        for (int x = 0; x < CHUNK_SIZE; x++)
            for (int z = 0; z < CHUNK_SIZE; z++)
                resultingHeightMap[x][z] = getHeight(heightMap[x][z], erosionMap[x][z]);

        generate(chunk, resultingHeightMap, temperatureMap, humidityMap, erosionMap, featureMap);
    }

    public static void generate(Chunk chunk, int[][] heightMap, double[][] temperatureMap, double[][] humidityMap, double[][] erosionMap, double[][] featureMap) {
        if (chunk.isGenerated())
            return;
        chunk.setGenerated();

        int[] caveBitMap = generateCaveBitMap(chunk);

        for (int inChunkX = 0; inChunkX < CHUNK_SIZE; inChunkX++)
            for (int inChunkZ = 0; inChunkZ < CHUNK_SIZE; inChunkZ++) {
                double temperature = temperatureMap[inChunkX][inChunkZ];
                double humidity = humidityMap[inChunkX][inChunkZ];
                double erosion = erosionMap[inChunkX][inChunkZ];
                double feature = featureMap[inChunkX][inChunkZ];

                int resultingHeight = heightMap[inChunkX][inChunkZ];

                if (resultingHeight <= WATER_LEVEL)
                    generateOceans(chunk, inChunkX, inChunkZ, resultingHeight, feature, temperature, caveBitMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ]);
                else if (erosion > MOUNTAIN_THRESHOLD)
                    generateMountains(chunk, inChunkX, inChunkZ, resultingHeight, feature, temperature, caveBitMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ]);
                else if (temperature > 0.4) {
                    if (humidity < -0.4)
                        generateDesert(chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBitMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ]);
                    else if (humidity < 0.3)
                        generateWasteLand(chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBitMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ]);
                    else
                        generateDarkOakForest(chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBitMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ]);
                } else if (temperature < -0.4) {
                    if (humidity > 0.0)
                        generateSnowySpruceForest(chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBitMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ]);
                    else
                        generateSnowyPlains(chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBitMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ]);
                } else if (humidity > 0.3)
                    generateOakForest(chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBitMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ]);
                else if (humidity < -0.4)
                    generateSpruceForest(chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBitMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ]);
                else
                    generatePlains(chunk, inChunkX, inChunkZ, resultingHeight, feature, caveBitMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ]);
            }

        ArrayList<Long> toGenerateBlocks = Chunk.removeToGenerateBlocks(chunk.getId());
        if (toGenerateBlocks != null) {
            int[] intHeightMap = Chunk.getHeightMap(chunk.getChunkX(), chunk.getChunkZ());
            for (long data : toGenerateBlocks) {
                short block = (short) (data >> 48 & 0xFFFF);
                int inChunkX = (int) (data >> CHUNK_SIZE_BITS * 2 & CHUNK_SIZE_MASK);
                int inChunkY = (int) (data >> CHUNK_SIZE_BITS & CHUNK_SIZE_MASK);
                int inChunkZ = (int) (data & CHUNK_SIZE_MASK);

                int index = inChunkX << CHUNK_SIZE_BITS * 2 | inChunkZ << CHUNK_SIZE_BITS | inChunkY;
                if (chunk.getSaveBlock(index) != AIR && Block.isLeaveType(block))
                    continue;
                chunk.storeSave(inChunkX, inChunkY, inChunkZ, block);

                int y = inChunkY | chunk.getChunkY() << CHUNK_SIZE_BITS;
                if (y > intHeightMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ])
                    intHeightMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ] = y;
            }
        }

        chunk.optimizeBlockStorage();
    }

    private static void generateMountains(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature, double temperature, int caveBits) {
        if (temperature > 0.4) generateDryMountain(chunk, inChunkX, inChunkZ, height, feature, caveBits);
        else if (temperature < -0.4) generateSnowyMountain(chunk, inChunkX, inChunkZ, height, feature, caveBits);
        else generateMountain(chunk, inChunkX, inChunkZ, height, feature, caveBits);
    }

    private static void generateOceans(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature, double temperature, int caveBits) {
        if (temperature > 0.4) generateWarmOcean(chunk, inChunkX, inChunkZ, height, feature, caveBits);
        else if (temperature < -0.4) generateColdOcean(chunk, inChunkX, inChunkZ, height, feature, caveBits);
        else generateOcean(chunk, inChunkX, inChunkZ, height, feature, caveBits);
    }


    private static void generatePlains(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature, int caveBits) {
        int totalX = chunk.getChunkX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getChunkZ() << CHUNK_SIZE_BITS | inChunkZ;

        int sandHeight = Utils.floor(Math.abs(feature * 4)) + WATER_LEVEL - 1;
        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getChunkY() << CHUNK_SIZE_BITS);

            if (totalY <= height && (caveBits & 1 << inChunkY) == 0) {
                if (totalY < height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
                else if (height <= sandHeight + 2 && totalY <= sandHeight + 2 && totalY >= sandHeight - 2)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
                else if (totalY == height && totalY > WATER_LEVEL)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, GRASS);
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, height <= WATER_LEVEL ? getOceanFloorBlock(totalX, totalY, totalZ) : DIRT);
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);

            genOakTree(chunk, height, inChunkX, inChunkY, inChunkZ, totalY, feature, PLAINS_TREE_THRESHOLD, caveBits);
        }
    }

    private static void generateOakForest(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature, int caveBits) {
        int totalX = chunk.getChunkX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getChunkZ() << CHUNK_SIZE_BITS | inChunkZ;

        int sandHeight = Utils.floor(Math.abs(feature * 4)) + WATER_LEVEL - 1;
        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getChunkY() << CHUNK_SIZE_BITS);

            if (totalY <= height && (caveBits & 1 << inChunkY) == 0) {
                if (totalY < height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
                else if (height <= sandHeight + 2 && totalY <= sandHeight + 2 && totalY >= sandHeight - 2)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
                else if (totalY == height && totalY > WATER_LEVEL)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, GRASS);
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, height <= WATER_LEVEL ? getOceanFloorBlock(totalX, totalY, totalZ) : DIRT);
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);

            genOakTree(chunk, height, inChunkX, inChunkY, inChunkZ, totalY, feature, FOREST_TREE_THRESHOLD, caveBits);
        }
    }

    private static void generateSpruceForest(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature, int caveBits) {
        int totalX = chunk.getChunkX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getChunkZ() << CHUNK_SIZE_BITS | inChunkZ;

        int sandHeight = Utils.floor(Math.abs(feature * 4)) + WATER_LEVEL - 1;
        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getChunkY() << CHUNK_SIZE_BITS);

            if (totalY <= height && (caveBits & 1 << inChunkY) == 0) {
                if (totalY < height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
                else if (height <= sandHeight + 2 && totalY <= sandHeight + 2 && totalY >= sandHeight - 2)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
                else if (totalY == height && totalY > WATER_LEVEL)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, GRASS);
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, height <= WATER_LEVEL ? getOceanFloorBlock(totalX, totalY, totalZ) : DIRT);
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);

            genSpruceTree(chunk, height, inChunkX, inChunkY, inChunkZ, totalY, feature, FOREST_TREE_THRESHOLD, caveBits);
        }
    }

    private static void generateDarkOakForest(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature, int caveBits) {
        int totalX = chunk.getChunkX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getChunkZ() << CHUNK_SIZE_BITS | inChunkZ;

        int sandHeight = Utils.floor(Math.abs(feature * 4)) + WATER_LEVEL - 1;
        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getChunkY() << CHUNK_SIZE_BITS);

            if (totalY <= height && (caveBits & 1 << inChunkY) == 0) {
                if (totalY < height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
                else if (height <= sandHeight + 2 && totalY <= sandHeight + 2 && totalY >= sandHeight - 2)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
                else if (totalY == height && totalY > WATER_LEVEL)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, GRASS);
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, height <= WATER_LEVEL ? getWarmOceanFloorBlocK(totalX, totalY, totalZ) : DIRT);
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);

            genDarkOakTree(chunk, height, inChunkX, inChunkY, inChunkZ, totalY, feature, FOREST_TREE_THRESHOLD, caveBits);
        }
    }

    private static void generateDesert(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature, int caveBits) {
        int totalX = chunk.getChunkX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getChunkZ() << CHUNK_SIZE_BITS | inChunkZ;

        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getChunkY() << CHUNK_SIZE_BITS);

            if (totalY <= height && (caveBits & 1 << inChunkY) == 0) {
                if (totalY < height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, height <= WATER_LEVEL ? getWarmOceanFloorBlocK(totalX, totalY, totalZ) : SAND);
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);

            generateCactus(chunk, height, inChunkX, inChunkY, inChunkZ, totalY, feature, caveBits);
        }
    }

    private static void generateWasteLand(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature, int caveBits) {
        int totalX = chunk.getChunkX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getChunkZ() << CHUNK_SIZE_BITS | inChunkZ;

        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getChunkY() << CHUNK_SIZE_BITS);

            if (totalY <= height && (caveBits & 1 << inChunkY) == 0) {
                if (totalY < height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, height <= WATER_LEVEL ? getWarmOceanFloorBlocK(totalX, totalY, totalZ) : getGeneratingDirtType(totalX, totalY, totalZ));
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);

            generateCactus(chunk, height, inChunkX, inChunkY, inChunkZ, totalY, feature, caveBits);

            genOakTree(chunk, height, inChunkX, inChunkY, inChunkZ, totalY, feature, WASTELAND_FEATURE_THRESHOLD, caveBits);
        }
    }

    private static void generateSnowyPlains(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature, int caveBits) {
        int totalX = chunk.getChunkX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getChunkZ() << CHUNK_SIZE_BITS | inChunkZ;

        int sandHeight = Utils.floor(Math.abs(feature * 4)) + WATER_LEVEL - 1;
        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getChunkY() << CHUNK_SIZE_BITS);

            if (totalY <= height && (caveBits & 1 << inChunkY) == 0) {
                if (totalY < height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
                else if (totalY <= sandHeight + 2 && totalY >= sandHeight - 2)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
                else if (totalY == WATER_LEVEL && feature > 0.75)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, ICE);
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, height <= WATER_LEVEL ? getColdOceanFloorBlock(totalX, totalY, totalZ) : SNOW);
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);

            genSpruceTree(chunk, height, inChunkX, inChunkY, inChunkZ, totalY, feature, PLAINS_TREE_THRESHOLD, caveBits);
        }
    }

    private static void generateSnowySpruceForest(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature, int caveBits) {
        int totalX = chunk.getChunkX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getChunkZ() << CHUNK_SIZE_BITS | inChunkZ;

        int sandHeight = Utils.floor(Math.abs(feature * 4)) + WATER_LEVEL - 1;
        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getChunkY() << CHUNK_SIZE_BITS);

            if (totalY <= height && (caveBits & 1 << inChunkY) == 0) {
                if (totalY < height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
                else if (totalY <= sandHeight + 2 && totalY >= sandHeight - 2)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
                else if (totalY == WATER_LEVEL && feature > 0.75)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, ICE);
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, height <= WATER_LEVEL ? getColdOceanFloorBlock(totalX, totalY, totalZ) : SNOW);
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);

            genSpruceTree(chunk, height, inChunkX, inChunkY, inChunkZ, totalY, feature, FOREST_TREE_THRESHOLD, caveBits);
        }
    }

    private static void generateOcean(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature, int caveBits) {
        int totalX = chunk.getChunkX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getChunkZ() << CHUNK_SIZE_BITS | inChunkZ;

        int sandHeight = Utils.floor(Math.abs(feature * 4)) + WATER_LEVEL - 5;

        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getChunkY() << CHUNK_SIZE_BITS);

            if (totalY <= height && (caveBits & 1 << inChunkY) == 0) {
                if (totalY > sandHeight)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
                else if (totalY > height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getOceanFloorBlock(totalX, totalY, totalZ));
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);
        }
    }

    private static void generateWarmOcean(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature, int caveBits) {
        int totalX = chunk.getChunkX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getChunkZ() << CHUNK_SIZE_BITS | inChunkZ;

        int sandHeight = Utils.floor(Math.abs(feature * 4)) + WATER_LEVEL - 5;

        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getChunkY() << CHUNK_SIZE_BITS);

            if (totalY <= height && (caveBits & 1 << inChunkY) == 0) {
                if (totalY > sandHeight)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
                else if (totalY > height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getWarmOceanFloorBlocK(totalX, totalY, totalZ));
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);
        }
    }

    private static void generateColdOcean(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature, int caveBits) {
        int totalX = chunk.getChunkX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getChunkZ() << CHUNK_SIZE_BITS | inChunkZ;

        int sandHeight = Utils.floor(Math.abs(feature * 4)) + WATER_LEVEL - 5;
        int iceHeight = Math.min(getIceHeight(totalX, totalZ, feature), WATER_LEVEL - height);

        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getChunkY() << CHUNK_SIZE_BITS);

            if (totalY <= height && (caveBits & 1 << inChunkY) == 0) {
                if (totalY > sandHeight)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
                else if (totalY > height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getColdOceanFloorBlock(totalX, totalY, totalZ));
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);

            if (totalY > WATER_LEVEL - iceHeight && totalY < WATER_LEVEL + iceHeight)
                chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingIceType(totalX, totalY, totalZ));
        }
    }

    private static void generateMountain(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature, int caveBits) {
        int totalX = chunk.getChunkX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getChunkZ() << CHUNK_SIZE_BITS | inChunkZ;

        int snowHeight = Utils.floor(feature * 32 + SNOW_LEVEL);
        int grassHeight = Utils.floor(feature * 32) + WATER_LEVEL;

        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getChunkY() << CHUNK_SIZE_BITS);

            if (totalY <= height && (caveBits & 1 << inChunkY) == 0) {
                if (totalY > snowHeight && totalY > height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, SNOW);
                else if (totalY == height && height <= grassHeight)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, GRASS);
                else if (totalY < height && totalY > height - 5 && height <= grassHeight)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, DIRT);
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);
        }
    }

    private static void generateSnowyMountain(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature, int caveBits) {
        int totalX = chunk.getChunkX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getChunkZ() << CHUNK_SIZE_BITS | inChunkZ;

        int iceHeight = Utils.floor(feature * 32 + ICE_LEVEL);

        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getChunkY() << CHUNK_SIZE_BITS);

            if (totalY <= height && (caveBits & 1 << inChunkY) == 0) {
                if (totalY > iceHeight && totalY > height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingIceType(totalX, totalY, totalZ));
                else if (totalY > height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, SNOW);
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);
        }
    }

    private static void generateDryMountain(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature, int caveBits) {
        int totalX = chunk.getChunkX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getChunkZ() << CHUNK_SIZE_BITS | inChunkZ;

        int dirtHeight = Utils.floor(feature * 32 + WATER_LEVEL);

        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getChunkY() << CHUNK_SIZE_BITS);

            if (totalY <= height && (caveBits & 1 << inChunkY) == 0) {
                if (totalY > height - 5 && height <= dirtHeight)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingDirtType(totalX, totalY, totalZ));
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);
        }
    }


    public static void genOakTree(Chunk chunk, int height, int inChunkX, int inChunkY, int inChunkZ, int totalY, double feature, double threshold, int caveBits) {
        if (feature > threshold && totalY < height + OAK_TREE.length && totalY >= height && height > WATER_LEVEL && (caveBits & 1 << (height & CHUNK_SIZE_MASK)) == 0)
            for (int i = 0; i < 5; i++)
                for (int j = 0; j < 5; j++)
                    chunk.storeTreeBlock(inChunkX + j - 2, inChunkY, inChunkZ + i - 2, OAK_TREE[totalY - height][i][j]);
    }

    public static void genSpruceTree(Chunk chunk, int height, int inChunkX, int inChunkY, int inChunkZ, int totalY, double feature, double threshold, int caveBits) {
        if (feature > threshold && totalY < height + SPRUCE_TREE.length && totalY >= height && height > WATER_LEVEL && (caveBits & 1 << (height & CHUNK_SIZE_MASK)) == 0)
            for (int i = 0; i < 7; i++)
                for (int j = 0; j < 7; j++)
                    chunk.storeTreeBlock(inChunkX + j - 3, inChunkY, inChunkZ + i - 3, SPRUCE_TREE[totalY - height][i][j]);
    }

    public static void genDarkOakTree(Chunk chunk, int height, int inChunkX, int inChunkY, int inChunkZ, int totalY, double feature, double threshold, int caveBits) {
        if (feature > threshold && totalY < height + DARK_OAK_TREE.length && totalY >= height && height > WATER_LEVEL && (caveBits & 1 << (height & CHUNK_SIZE_MASK)) == 0)
            for (int i = 0; i < 7; i++)
                for (int j = 0; j < 7; j++)
                    chunk.storeTreeBlock(inChunkX + j - 3, inChunkY, inChunkZ + i - 3, DARK_OAK_TREE[totalY - height][i][j]);
    }

    public static void generateCactus(Chunk chunk, int height, int inChunkX, int inChunkY, int inChunkZ, int totalY, double feature, int caveBits) {
        if (feature > CACTUS_THRESHOLD && height > WATER_LEVEL && totalY > height && totalY < height + 1 + (feature - CACTUS_THRESHOLD) * 500 && (caveBits & 1 << (height & CHUNK_SIZE_MASK)) == 0)
            chunk.storeSave(inChunkX, inChunkY, inChunkZ, CACTUS);
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


    private static boolean isInsideCave(int x, int y, int z) {

        if (y <= NOODLE_CAVE_MAX_Y) {
            double noodleCaveHeightBias = Math.max(y, 0) * NOODLE_CAVE_HEIGHT_BIAS;

            double noise1 = OpenSimplex2S.noise3_ImproveXY(SEED, x * NOODLE_CAVE_FREQUENCY, y * NOODLE_CAVE_FREQUENCY, z * NOODLE_CAVE_FREQUENCY) + noodleCaveHeightBias;
            double noise2 = OpenSimplex2S.noise3_ImproveXY(SEED + 100, x * NOODLE_CAVE_FREQUENCY, y * NOODLE_CAVE_FREQUENCY, z * NOODLE_CAVE_FREQUENCY) + noodleCaveHeightBias;

            if (noise1 * noise1 + noise2 * noise2 < NOODLE_CAVE_THRESHOLD) return true;
        }

        if (y > BLOB_CAVE_MAX_Y)
            return false;

        double blobCaveHeightBias = Math.max(y, 0) * BLOB_CAVE_CAVE_HEIGHT_BIAS;

        double blobCaveNoise = OpenSimplex2S.noise3_ImproveXY(SEED + 200, x * BLOB_CAVE_FREQUENCY, y * BLOB_CAVE_FREQUENCY, z * BLOB_CAVE_FREQUENCY) * 0.5555;
        blobCaveNoise += OpenSimplex2S.noise3_ImproveXY(SEED + 300, x * 0.02, y * 0.02, z * 0.02) * 0.4444;
        blobCaveNoise -= blobCaveHeightBias;

        return blobCaveNoise > BLOB_CAVE_THRESHOLD;
    }

    private static int[] generateCaveBitMap(Chunk chunk) {
        int[] bitMap = new int[CHUNK_SIZE * CHUNK_SIZE];
        int chunkX = chunk.getChunkX() << CHUNK_SIZE_BITS;
        int chunkY = chunk.getChunkY() << CHUNK_SIZE_BITS;
        int chunkZ = chunk.getChunkZ() << CHUNK_SIZE_BITS;

        for (int x = chunkX; x < CHUNK_SIZE + chunkX; x += 4)
            for (int y = chunkY; y < CHUNK_SIZE + chunkY; y += 4)
                for (int z = chunkZ; z < CHUNK_SIZE + chunkZ; z += 4) {
                    byte cornerValues = 0;
                    if (isInsideCave(x, y, z)) cornerValues = 1;
                    if (isInsideCave(x, y, z + 3)) cornerValues |= 2;
                    if (isInsideCave(x, y + 3, z)) cornerValues |= 4;
                    if (isInsideCave(x, y + 3, z + 3)) cornerValues |= 8;
                    if (isInsideCave(x + 3, y, z)) cornerValues |= 16;
                    if (isInsideCave(x + 3, y, z + 3)) cornerValues |= 32;
                    if (isInsideCave(x + 3, y + 3, z)) cornerValues |= 64;
                    if (isInsideCave(x + 3, y + 3, z + 3)) cornerValues |= -128;

                    if (cornerValues == 0) continue;
                    int inChunkX = x & CHUNK_SIZE_MASK;
                    int inChunkY = y & CHUNK_SIZE_MASK;
                    int inChunkZ = z & CHUNK_SIZE_MASK;
                    if (cornerValues == -1) {
                        int mask = 15 << y;
                        for (int i = 0; i < 4; i++)
                            for (int j = 0; j < 4; j++)
                                bitMap[i + inChunkX << CHUNK_SIZE_BITS | inChunkZ + j] |= mask;
                    } else
                        for (int i = 0; i < 4; i++)
                            for (int j = 0; j < 4; j++)
                                for (int k = 0; k < 4; k++)
                                    if (isInsideCave(x + i, y + k, z + j))
                                        bitMap[i + inChunkX << CHUNK_SIZE_BITS | inChunkZ + j] |= 1 << inChunkY + k;
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
}
