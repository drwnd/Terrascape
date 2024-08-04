package com.MBEv2.core;

import com.MBEv2.core.utils.Utils;
import com.MBEv2.test.GameLogic;

import java.util.Random;

import static com.MBEv2.core.utils.Constants.*;

public class WorldGeneration {

    public static void generate(Chunk chunk) {
        if (chunk.isGenerated())
            return;
        double[][] heightMap = WorldGeneration.heightMap(chunk.getX(), chunk.getZ());
        double[][] temperatureMap = WorldGeneration.temperatureMap(chunk.getX(), chunk.getZ());
        double[][] humidityMap = WorldGeneration.humidityMap(chunk.getX(), chunk.getZ());
        double[][] erosionMap = WorldGeneration.erosionMap(chunk.getX(), chunk.getZ());
        double[][] featureMap = WorldGeneration.featureMap(chunk.getX(), chunk.getZ());
        generate(chunk, heightMap, temperatureMap, humidityMap, erosionMap, featureMap);
    }

    public static void generate(Chunk chunk, double[][] heightMap, double[][] temperatureMap, double[][] humidityMap, double[][] erosionMap, double[][] featureMap) {
        if (chunk.isGenerated())
            return;
        chunk.setGenerated();
        for (int inChunkX = 0; inChunkX < CHUNK_SIZE; inChunkX++)
            for (int inChunkZ = 0; inChunkZ < CHUNK_SIZE; inChunkZ++) {
                double height = heightMap[inChunkX][inChunkZ];
                double temperature = temperatureMap[inChunkX][inChunkZ];
                double humidity = humidityMap[inChunkX][inChunkZ];
                double erosion = erosionMap[inChunkX][inChunkZ];
                double feature = featureMap[inChunkX][inChunkZ];

                int resultingHeight = getHeight(height, erosion);
                if (resultingHeight <= WATER_LEVEL)
                    generateOceans(chunk, inChunkX, inChunkZ, resultingHeight, feature, temperature);
                else if (erosion > MOUNTAIN_THRESHOLD)
                    generateMountains(chunk, inChunkX, inChunkZ, resultingHeight, feature, temperature);
                else if (temperature > 0.4) {
                    if (humidity < -0.4)
                        generateDesert(chunk, inChunkX, inChunkZ, resultingHeight, feature);
                    else if (humidity < 0.3)
                        generateWasteLand(chunk, inChunkX, inChunkZ, resultingHeight, feature);
                    else
                        generateDarkOakForest(chunk, inChunkX, inChunkZ, resultingHeight, feature);
                } else if (temperature < -0.4) {
                    if (humidity > 0.0)
                        generateSnowySpruceForest(chunk, inChunkX, inChunkZ, resultingHeight, feature);
                    else
                        generateSnowyPlains(chunk, inChunkX, inChunkZ, resultingHeight, feature);
                } else if (humidity > 0.3)
                    generateOakForest(chunk, inChunkX, inChunkZ, resultingHeight, feature);
                else if (humidity < -0.4)
                    generateSpruceForest(chunk, inChunkX, inChunkZ, resultingHeight, feature);
                else
                    generatePlains(chunk, inChunkX, inChunkZ, resultingHeight, feature);
            }
        chunk.optimizeBlockStorage();
    }

    private static void generateMountains(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature, double temperature) {
        if (temperature > 0.4) generateDryMountain(chunk, inChunkX, inChunkZ, height, feature);
        else if (temperature < -0.4) generateSnowyMountain(chunk, inChunkX, inChunkZ, height, feature);
        else generateMountain(chunk, inChunkX, inChunkZ, height, feature);
    }

    private static void generateOceans(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature, double temperature) {
        if (temperature > 0.4) generateWarmOcean(chunk, inChunkX, inChunkZ, height, feature);
        else if (temperature < -0.4) generateColdOcean(chunk, inChunkX, inChunkZ, height, feature);
        else generateOcean(chunk, inChunkX, inChunkZ, height, feature);
    }


    private static void generatePlains(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature) {
        int totalX = chunk.getX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getZ() << CHUNK_SIZE_BITS | inChunkZ;

        int sandHeight = Utils.floor(Math.abs(feature * 4)) + WATER_LEVEL - 1;
        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getY() << CHUNK_SIZE_BITS);

            if (totalY <= height && isOutsideCave(totalX, totalY, totalZ)) {
                if (totalY < height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
                else if (height <= sandHeight + 2 && totalY <= sandHeight + 2 && totalY >= sandHeight - 2)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
                else if (totalY == height && totalY > WATER_LEVEL)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, GRASS);
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, height <= WATER_LEVEL ? getOceanFloorBlock(totalX, totalY, totalZ) : DIRT);
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);

            genOakTree(chunk, height, inChunkX, inChunkY, inChunkZ, totalX, totalY, totalZ, feature, PLAINS_TREE_THRESHOLD);
        }
    }

    private static void generateOakForest(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature) {
        int totalX = chunk.getX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getZ() << CHUNK_SIZE_BITS | inChunkZ;

        int sandHeight = Utils.floor(Math.abs(feature * 4)) + WATER_LEVEL - 1;
        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getY() << CHUNK_SIZE_BITS);

            if (totalY <= height && isOutsideCave(totalX, totalY, totalZ)) {
                if (totalY < height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
                else if (height <= sandHeight + 2 && totalY <= sandHeight + 2 && totalY >= sandHeight - 2)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
                else if (totalY == height && totalY > WATER_LEVEL)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, GRASS);
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, height <= WATER_LEVEL ? getOceanFloorBlock(totalX, totalY, totalZ) : DIRT);
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);

            genOakTree(chunk, height, inChunkX, inChunkY, inChunkZ, totalX, totalY, totalZ, feature, FOREST_TREE_THRESHOLD);
        }
    }

    private static void generateSpruceForest(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature) {
        int totalX = chunk.getX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getZ() << CHUNK_SIZE_BITS | inChunkZ;

        int sandHeight = Utils.floor(Math.abs(feature * 4)) + WATER_LEVEL - 1;
        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getY() << CHUNK_SIZE_BITS);

            if (totalY <= height && isOutsideCave(totalX, totalY, totalZ)) {
                if (totalY < height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
                else if (height <= sandHeight + 2 && totalY <= sandHeight + 2 && totalY >= sandHeight - 2)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
                else if (totalY == height && totalY > WATER_LEVEL)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, GRASS);
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, height <= WATER_LEVEL ? getOceanFloorBlock(totalX, totalY, totalZ) : DIRT);
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);

            genSpruceTree(chunk, height, inChunkX, inChunkY, inChunkZ, totalX, totalY, totalZ, feature, FOREST_TREE_THRESHOLD);
        }
    }

    private static void generateDarkOakForest(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature) {
        int totalX = chunk.getX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getZ() << CHUNK_SIZE_BITS | inChunkZ;

        int sandHeight = Utils.floor(Math.abs(feature * 4)) + WATER_LEVEL - 1;
        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getY() << CHUNK_SIZE_BITS);

            if (totalY <= height && isOutsideCave(totalX, totalY, totalZ)) {
                if (totalY < height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
                else if (height <= sandHeight + 2 && totalY <= sandHeight + 2 && totalY >= sandHeight - 2)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
                else if (totalY == height && totalY > WATER_LEVEL)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, GRASS);
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, height <= WATER_LEVEL ? getWarmOceanFloorBlocK(totalX, totalY, totalZ) : DIRT);
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);

            genDarkOakTree(chunk, height, inChunkX, inChunkY, inChunkZ, totalX, totalY, totalZ, feature, FOREST_TREE_THRESHOLD);
        }
    }

    private static void generateDesert(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature) {
        int totalX = chunk.getX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getZ() << CHUNK_SIZE_BITS | inChunkZ;

        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getY() << CHUNK_SIZE_BITS);

            if (totalY <= height && isOutsideCave(totalX, totalY, totalZ)) {
                if (totalY < height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, height <= WATER_LEVEL ? getWarmOceanFloorBlocK(totalX, totalY, totalZ) : SAND);
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);

            if (feature > CACTUS_THRESHOLD && height > WATER_LEVEL && totalY > height && totalY < height + 1 + (feature - CACTUS_THRESHOLD) * 500 && isOutsideCave(totalX, height, totalZ))
                chunk.storeSave(inChunkX, inChunkY, inChunkZ, CACTUS);
        }
    }

    private static void generateWasteLand(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature) {
        int totalX = chunk.getX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getZ() << CHUNK_SIZE_BITS | inChunkZ;

        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getY() << CHUNK_SIZE_BITS);

            if (totalY <= height && isOutsideCave(totalX, totalY, totalZ)) {
                if (totalY < height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, height <= WATER_LEVEL ? getWarmOceanFloorBlocK(totalX, totalY, totalZ) : getGeneratingDirtType(totalX, totalY, totalZ));
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);

            if (feature > WASTELAND_FEATURE_THRESHOLD && height > WATER_LEVEL && totalY > height && totalY < height + 1 + (feature - CACTUS_THRESHOLD) * 250 && isOutsideCave(totalX, height, totalZ))
                chunk.storeSave(inChunkX, inChunkY, inChunkZ, CACTUS);

            genOakTree(chunk, height, inChunkX, inChunkY, inChunkZ, totalX, totalY, totalZ, feature, WASTELAND_FEATURE_THRESHOLD);
        }
    }

    private static void generateSnowyPlains(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature) {
        int totalX = chunk.getX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getZ() << CHUNK_SIZE_BITS | inChunkZ;

        int sandHeight = Utils.floor(Math.abs(feature * 4)) + WATER_LEVEL - 1;
        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getY() << CHUNK_SIZE_BITS);

            if (totalY <= height && isOutsideCave(totalX, totalY, totalZ)) {
                if (totalY < height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
                else if (totalY <= sandHeight + 2 && totalY >= sandHeight - 2)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
                else if (totalY == WATER_LEVEL && feature > 0.75)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, ICE);
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, height <= WATER_LEVEL ? getColdOceanFloorBlock(totalX, totalY, totalZ) : SNOW);
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);

            genSpruceTree(chunk, height, inChunkX, inChunkY, inChunkZ, totalX, totalY, totalZ, feature, PLAINS_TREE_THRESHOLD);
        }
    }

    private static void generateSnowySpruceForest(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature) {
        int totalX = chunk.getX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getZ() << CHUNK_SIZE_BITS | inChunkZ;

        int sandHeight = Utils.floor(Math.abs(feature * 4)) + WATER_LEVEL - 1;
        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getY() << CHUNK_SIZE_BITS);

            if (totalY <= height && isOutsideCave(totalX, totalY, totalZ)) {
                if (totalY < height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
                else if (totalY <= sandHeight + 2 && totalY >= sandHeight - 2)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
                else if (totalY == WATER_LEVEL && feature > 0.75)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, ICE);
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, height <= WATER_LEVEL ? getColdOceanFloorBlock(totalX, totalY, totalZ) : SNOW);
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);

            genSpruceTree(chunk, height, inChunkX, inChunkY, inChunkZ, totalX, totalY, totalZ, feature, FOREST_TREE_THRESHOLD);
        }
    }

    private static void generateOcean(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature) {
        int totalX = chunk.getX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getZ() << CHUNK_SIZE_BITS | inChunkZ;

        int sandHeight = Utils.floor(Math.abs(feature * 4)) + WATER_LEVEL - 5;

        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getY() << CHUNK_SIZE_BITS);

            if (totalY <= height && isOutsideCave(totalX, totalY, totalZ)) {
                if (totalY > sandHeight)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
                else if (totalY > height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getOceanFloorBlock(totalX, totalY, totalZ));
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);
        }
    }

    private static void generateWarmOcean(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature) {
        int totalX = chunk.getX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getZ() << CHUNK_SIZE_BITS | inChunkZ;

        int sandHeight = Utils.floor(Math.abs(feature * 4)) + WATER_LEVEL - 5;

        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getY() << CHUNK_SIZE_BITS);

            if (totalY <= height && isOutsideCave(totalX, totalY, totalZ)) {
                if (totalY > sandHeight)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
                else if (totalY > height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getWarmOceanFloorBlocK(totalX, totalY, totalZ));
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);
        }
    }

    private static void generateColdOcean(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature) {
        int totalX = chunk.getX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getZ() << CHUNK_SIZE_BITS | inChunkZ;

        int sandHeight = Utils.floor(Math.abs(feature * 4)) + WATER_LEVEL - 5;
        int iceHeight = Math.min(getIceHeight(totalX, totalZ, feature), WATER_LEVEL - height);

        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getY() << CHUNK_SIZE_BITS);

            if (totalY <= height && isOutsideCave(totalX, totalY, totalZ)) {
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

    private static void generateMountain(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature) {
        int totalX = chunk.getX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getZ() << CHUNK_SIZE_BITS | inChunkZ;

        int snowHeight = Utils.floor(feature * 32 + SNOW_LEVEL);
        int grassHeight = Utils.floor(feature * 32) + WATER_LEVEL;

        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getY() << CHUNK_SIZE_BITS);

            if (totalY <= height && isOutsideCave(totalX, totalY, totalZ)) {
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

    private static void generateSnowyMountain(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature) {
        int totalX = chunk.getX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getZ() << CHUNK_SIZE_BITS | inChunkZ;

        int iceHeight = Utils.floor(feature * 32 + ICE_LEVEL);

        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getY() << CHUNK_SIZE_BITS);

            if (totalY <= height && isOutsideCave(totalX, totalY, totalZ)) {
                if (totalY > iceHeight && totalY > height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingIceType(totalX, totalY, totalZ));
                else if (totalY > height - 5)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, SNOW);
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);
        }
    }

    private static void generateDryMountain(Chunk chunk, int inChunkX, int inChunkZ, int height, double feature) {
        int totalX = chunk.getX() << CHUNK_SIZE_BITS | inChunkX;
        int totalZ = chunk.getZ() << CHUNK_SIZE_BITS | inChunkZ;

        int dirtHeight = Utils.floor(feature * 32 + WATER_LEVEL);

        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = inChunkY + (chunk.getY() << CHUNK_SIZE_BITS);

            if (totalY <= height && isOutsideCave(totalX, totalY, totalZ)) {
                if (totalY > height - 5 && height <= dirtHeight)
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingDirtType(totalX, totalY, totalZ));
                else
                    chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
            } else if (totalY <= WATER_LEVEL) chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER);
        }
    }


    public static void genOakTree(Chunk chunk, int height, int inChunkX, int inChunkY, int inChunkZ, int totalX, int totalY, int totalZ, double feature, double threshold) {
        if (feature > threshold && totalY < height + OAK_TREE.length && totalY >= height && height > WATER_LEVEL &&
                inChunkX >= 2 && inChunkZ >= 2 && inChunkX < CHUNK_SIZE - 2 && inChunkZ < CHUNK_SIZE - 2 && isOutsideCave(totalX, height, totalZ))
            for (int i = 0; i < 5; i++)
                for (int j = 0; j < 5; j++)
                    chunk.storeTreeBlock(inChunkX + j - 2, inChunkY, inChunkZ + i - 2, OAK_TREE[totalY - height][i][j]);
    }

    public static void genSpruceTree(Chunk chunk, int height, int inChunkX, int inChunkY, int inChunkZ, int totalX, int totalY, int totalZ, double feature, double threshold) {
        if (feature > threshold && totalY < height + SPRUCE_TREE.length && totalY >= height && height > WATER_LEVEL &&
                inChunkX >= 3 && inChunkZ >= 3 && inChunkX < CHUNK_SIZE - 3 && inChunkZ < CHUNK_SIZE - 3 && isOutsideCave(totalX, height, totalZ))
            for (int i = 0; i < 7; i++)
                for (int j = 0; j < 7; j++)
                    chunk.storeTreeBlock(inChunkX + j - 3, inChunkY, inChunkZ + i - 3, SPRUCE_TREE[totalY - height][i][j]);
    }

    public static void genDarkOakTree(Chunk chunk, int height, int inChunkX, int inChunkY, int inChunkZ, int totalX, int totalY, int totalZ, double feature, double threshold) {
        if (feature > threshold && totalY < height + DARK_OAK_TREE.length && totalY >= height && height > WATER_LEVEL &&
                inChunkX >= 3 && inChunkZ >= 3 && inChunkX < CHUNK_SIZE - 3 && inChunkZ < CHUNK_SIZE - 3 && isOutsideCave(totalX, height, totalZ))
            for (int i = 0; i < 7; i++)
                for (int j = 0; j < 7; j++)
                    chunk.storeTreeBlock(inChunkX + j - 3, inChunkY, inChunkZ + i - 3, DARK_OAK_TREE[totalY - height][i][j]);
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

    private static boolean isOutsideCave(int x, int y, int z) {

        if (y <= NOODLE_CAVE_MAX_Y) {
            double noodleCaveHeightBias = Math.max(y, 0) * NOODLE_CAVE_HEIGHT_BIAS;

            double noise1 = OpenSimplex2S.noise3_ImproveXY(SEED, x * NOODLE_CAVE_FREQUENCY, y * NOODLE_CAVE_FREQUENCY, z * NOODLE_CAVE_FREQUENCY) + noodleCaveHeightBias;
            double noise2 = OpenSimplex2S.noise3_ImproveXY(SEED + 100, x * NOODLE_CAVE_FREQUENCY, y * NOODLE_CAVE_FREQUENCY, z * NOODLE_CAVE_FREQUENCY) + noodleCaveHeightBias;

            if (noise1 * noise1 + noise2 * noise2 < NOODLE_CAVE_THRESHOLD) return false;
        }

        if (y > BLOB_CAVE_MAX_Y)
            return true;

        double blobCaveHeightBias = Math.max(y, 0) * BLOB_CAVE_CAVE_HEIGHT_BIAS;

        double blobCaveNoise = OpenSimplex2S.noise3_ImproveXY(SEED + 200, x * BLOB_CAVE_FREQUENCY, y * BLOB_CAVE_FREQUENCY, z * BLOB_CAVE_FREQUENCY) * 0.5555;
        blobCaveNoise += OpenSimplex2S.noise3_ImproveXY(SEED + 300, x * 0.02, y * 0.02, z * 0.02) * 0.4444;
        blobCaveNoise -= blobCaveHeightBias;

        return blobCaveNoise < BLOB_CAVE_THRESHOLD;
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

    private static int getHeight(double height, double erosion) {
        height = height * 0.5 + 0.5;

        double modifier = 0.0;
        if (erosion > MOUNTAIN_THRESHOLD)
            modifier = (erosion - MOUNTAIN_THRESHOLD) * (erosion - MOUNTAIN_THRESHOLD) * 1000;
        else if (erosion < OCEAN_THRESHOLD)
            modifier = (erosion - OCEAN_THRESHOLD) * (erosion - OCEAN_THRESHOLD) * -1000;

        return Utils.floor(height * MAX_TERRAIN_HEIGHT_DIFFERENCE + modifier) + WATER_LEVEL - 15;
    }
}
