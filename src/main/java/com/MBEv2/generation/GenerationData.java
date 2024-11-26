package com.MBEv2.generation;

import com.MBEv2.dataStorage.Chunk;

import static com.MBEv2.utils.Constants.*;

public class GenerationData {

    public double temperature;
    public double humidity;
    public double feature;
    public double erosion;
    public double continental;

    public int height;
    public byte steepness;
    public long caveBits;

    public Chunk chunk;

    public GenerationData(int chunkX, int chunkZ) {
        temperatureMap = WorldGeneration.temperatureMap(chunkX, chunkZ);
        humidityMap = WorldGeneration.humidityMap(chunkX, chunkZ);
        featureMap = WorldGeneration.featureMap(chunkX, chunkZ);

        erosionMap = WorldGeneration.erosionMapPadded(chunkX, chunkZ);
        continentalMap = WorldGeneration.continentalMapPadded(chunkX, chunkZ);
        double[][] heightMap = WorldGeneration.heightMapPadded(chunkX, chunkZ);
        double[][] riverMap = WorldGeneration.riverMapPadded(chunkX, chunkZ);
        double[][] ridgeMap = WorldGeneration.ridgeMapPadded(chunkX, chunkZ);

        resultingHeightMap = WorldGeneration.getResultingHeightMap(heightMap, erosionMap, continentalMap, riverMap, ridgeMap);
        steepnessMap = WorldGeneration.steepnessMap(resultingHeightMap);
    }

    public void setChunk(Chunk chunk) {
        caveBitMap = WorldGeneration.generateCaveBitMap(chunk, resultingHeightMap);
        this.chunk = chunk;
    }

    public void set(int inChunkX, int inChunkZ) {
        int index = inChunkX << CHUNK_SIZE_BITS | inChunkZ;

        temperature = temperatureMap[index];
        humidity = humidityMap[index];
        feature = featureMap[index];
        steepness = steepnessMap[index];
        caveBits = caveBitMap[index];

        erosion = erosionMap[inChunkX + 1][inChunkZ + 1];
        continental = continentalMap[inChunkX + 1][inChunkZ + 1];
        height = resultingHeightMap[inChunkX + 1][inChunkZ + 1];
    }

    private final double[] temperatureMap;
    private final double[] humidityMap;
    private final double[] featureMap;
    private final double[][] erosionMap;
    private final double[][] continentalMap;

    private final int[][] resultingHeightMap;
    private final byte[] steepnessMap;

    private long[] caveBitMap;
}
