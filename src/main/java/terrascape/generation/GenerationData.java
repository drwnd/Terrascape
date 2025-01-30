package terrascape.generation;

import terrascape.generation.biomes.Biome;
import terrascape.dataStorage.Chunk;
import terrascape.utils.Utils;

import java.util.Random;

import static terrascape.utils.Constants.*;
import static terrascape.utils.Settings.SEED;

public final class GenerationData {

    public double temperature;
    public double humidity;
    public double feature;
    public double erosion;
    public double continental;

    public int height, specialHeight;
    public byte steepness;
    public long caveBits;
    public boolean treeAllowed;

    public Chunk chunk;

    public GenerationData(int chunkX, int chunkZ) {
        temperatureMap = temperatureMap(chunkX, chunkZ);
        humidityMap = humidityMap(chunkX, chunkZ);
        featureMap = featureMap(chunkX, chunkZ);
        treeBitMap = treeBitMap(chunkX, chunkZ);

        erosionMap = erosionMapPadded(chunkX, chunkZ);
        continentalMap = continentalMapPadded(chunkX, chunkZ);
        double[][] heightMap = heightMapPadded(chunkX, chunkZ);
        double[][] riverMap = riverMapPadded(chunkX, chunkZ);
        double[][] ridgeMap = ridgeMapPadded(chunkX, chunkZ);

        resultingHeightMap = WorldGeneration.getResultingHeightMap(heightMap, erosionMap, continentalMap, riverMap, ridgeMap);
        steepnessMap = steepnessMap(resultingHeightMap);
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
        treeAllowed = (treeBitMap[inChunkX] >> inChunkZ & 1) == 1;

        erosion = erosionMap[inChunkX + 1][inChunkZ + 1];
        continental = continentalMap[inChunkX + 1][inChunkZ + 1];
        height = resultingHeightMap[inChunkX + 1][inChunkZ + 1];
    }

    public void setBiome(int inChunkX, int inChunkZ, Biome biome) {
        specialHeight = biome.getSpecialHeight(chunk.X << CHUNK_SIZE_BITS | inChunkX, chunk.Z << CHUNK_SIZE_BITS | inChunkZ, this);
    }

    public int getHeight(int inChunkX, int inChunkZ) {
        return resultingHeightMap[inChunkX + 1][inChunkZ + 1];
    }

    public long getCaveBits(int inChunkX, int inChunkZ) {
        return caveBitMap[inChunkX << CHUNK_SIZE_BITS | inChunkZ];
    }

    private static double[][] heightMapPadded(int chunkX, int chunkZ) {
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
        height = OpenSimplex2S.noise3_ImproveXY(SEED ^ 0x08D2BCC9BD98BBF5L, totalX * HEIGHT_MAP_FREQUENCY, totalZ * HEIGHT_MAP_FREQUENCY, 0);
        height += OpenSimplex2S.noise3_ImproveXY(SEED ^ 0xCEC793764665EF7DL, totalX * HEIGHT_MAP_FREQUENCY * 2, totalZ * HEIGHT_MAP_FREQUENCY * 2, 0) * 0.5;
        height += OpenSimplex2S.noise3_ImproveXY(SEED ^ 0xBD4957D70308DEBFL, totalX * HEIGHT_MAP_FREQUENCY * 4, totalZ * HEIGHT_MAP_FREQUENCY * 4, 0) * 0.25;
        height += OpenSimplex2S.noise3_ImproveXY(SEED ^ 0xD68F54787A92D53CL, totalX * HEIGHT_MAP_FREQUENCY * 8, totalZ * HEIGHT_MAP_FREQUENCY * 8, 0) * 0.125;
        height += OpenSimplex2S.noise3_ImproveXY(SEED ^ 0x574730707031DA54L, totalX * HEIGHT_MAP_FREQUENCY * 16, totalZ * HEIGHT_MAP_FREQUENCY * 16, 0) * 0.0625;
        return height;
    }

    private static double[] temperatureMap(int chunkX, int chunkZ) {
        double[] temperatureMap = new double[CHUNK_SIZE * CHUNK_SIZE];
        for (int mapX = 0; mapX < CHUNK_SIZE; mapX++)
            for (int mapZ = 0; mapZ < CHUNK_SIZE; mapZ++) {
                int currentX = chunkX << CHUNK_SIZE_BITS | mapX;
                int currentZ = chunkZ << CHUNK_SIZE_BITS | mapZ;
                double temperature = temperatureMapValue(currentX, currentZ);
                temperatureMap[mapX << CHUNK_SIZE_BITS | mapZ] = temperature;
            }
        return temperatureMap;
    }

    public static double temperatureMapValue(int totalX, int totalZ) {
        double temperature;
        temperature = OpenSimplex2S.noise3_ImproveXY(SEED ^ 0xADA1CE5C24C4A44FL, totalX * TEMPERATURE_FREQUENCY, totalZ * TEMPERATURE_FREQUENCY, 0) * 0.8888;
        temperature += OpenSimplex2S.noise3_ImproveXY(SEED ^ 0xEEA0CB5D51C0A447L, totalX * TEMPERATURE_FREQUENCY * 50, totalZ * TEMPERATURE_FREQUENCY * 50, 0) * 0.1111;
        return temperature;
    }

    private static double[] humidityMap(int chunkX, int chunkZ) {
        double[] humidityMap = new double[CHUNK_SIZE * CHUNK_SIZE];
        for (int mapX = 0; mapX < CHUNK_SIZE; mapX++)
            for (int mapZ = 0; mapZ < CHUNK_SIZE; mapZ++) {
                int currentX = chunkX << CHUNK_SIZE_BITS | mapX;
                int currentZ = chunkZ << CHUNK_SIZE_BITS | mapZ;
                double humidity = humidityMapValue(currentX, currentZ);
                humidityMap[mapX << CHUNK_SIZE_BITS | mapZ] = humidity;
            }
        return humidityMap;
    }

    public static double humidityMapValue(int totalX, int totalZ) {
        double humidity;
        humidity = OpenSimplex2S.noise3_ImproveXY(SEED ^ 0x41C8F1921D50DF82L, totalX * HUMIDITY_FREQUENCY, totalZ * HUMIDITY_FREQUENCY, 0) * 0.8888;
        humidity += OpenSimplex2S.noise3_ImproveXY(SEED ^ 0xB935E00850C8416EL, totalX * HUMIDITY_FREQUENCY * 50, totalZ * HUMIDITY_FREQUENCY * 50, 0) * 0.1111;
        return humidity;
    }

    private static double[][] erosionMapPadded(int chunkX, int chunkZ) {
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
        erosion = OpenSimplex2S.noise3_ImproveXY(SEED ^ 0xBEF86CF6C75F708DL, totalX * EROSION_FREQUENCY, totalZ * EROSION_FREQUENCY, 0) * 0.9588;
        erosion += OpenSimplex2S.noise3_ImproveXY(SEED ^ 0x60E4A215EA2087BCL, totalX * EROSION_FREQUENCY * 40, totalZ * EROSION_FREQUENCY * 40, 0) * 0.0411;
        return erosion;
    }

    private static double[] featureMap(int chunkX, int chunkZ) {
        double[] featureMap = new double[CHUNK_SIZE * CHUNK_SIZE];
        Random random = new Random(Utils.getChunkId(chunkX, 0, chunkZ));

        for (int mapX = 0; mapX < CHUNK_SIZE; mapX++)
            for (int mapZ = 0; mapZ < CHUNK_SIZE; mapZ++)
                featureMap[mapX << CHUNK_SIZE_BITS | mapZ] = random.nextDouble();

        return featureMap;
    }

    private static byte[] steepnessMap(int[][] heightMapPadded) {
        byte[] steepnessMap = new byte[CHUNK_SIZE * CHUNK_SIZE];

        for (int mapX = 0; mapX < CHUNK_SIZE; mapX++)
            for (int mapZ = 0; mapZ < CHUNK_SIZE; mapZ++) {
                int height = heightMapPadded[mapX + 1][mapZ + 1];
                int steepnessX = Math.max(Math.abs(height - heightMapPadded[mapX][mapZ + 1]), Math.abs(height - heightMapPadded[mapX + 2][mapZ + 1]));
                int steepnessZ = Math.max(Math.abs(height - heightMapPadded[mapX + 1][mapZ]), Math.abs(height - heightMapPadded[mapX + 1][mapZ + 2]));
                steepnessMap[mapX << CHUNK_SIZE_BITS | mapZ] = (byte) Math.max(steepnessX, steepnessZ);
            }

        return steepnessMap;
    }

    private static double[][] continentalMapPadded(int chunkX, int chunkZ) {
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
        continental = OpenSimplex2S.noise3_ImproveXY(SEED ^ 0xCF71B60E764BFC2CL, totalX * CONTINENTAL_FREQUENCY, totalZ * CONTINENTAL_FREQUENCY, 0) * 0.9588;
        continental += OpenSimplex2S.noise3_ImproveXY(SEED ^ 0x8EF1C1F90DA10C0AL, totalX * CONTINENTAL_FREQUENCY * 50, totalZ * CONTINENTAL_FREQUENCY * 50, 0) * 0.0411;
        return continental;
    }

    private static double[][] riverMapPadded(int chunkX, int chunkZ) {
        double[][] riverMap = new double[CHUNK_SIZE + 2][CHUNK_SIZE + 2];
        for (int mapX = 0; mapX < CHUNK_SIZE + 2; mapX++)
            for (int mapZ = 0; mapZ < CHUNK_SIZE + 2; mapZ++) {
                int currentX = (chunkX << CHUNK_SIZE_BITS) + mapX - 1;
                int currentZ = (chunkZ << CHUNK_SIZE_BITS) + mapZ - 1;
                double river = riverMapValue(currentX, currentZ);
                riverMap[mapX][mapZ] = river;
            }
        return riverMap;
    }

    public static double riverMapValue(int totalX, int totalZ) {
        double river;
        river = OpenSimplex2S.noise3_ImproveXY(SEED ^ 0x84D43603ED399321L, totalX * RIVER_FREQUENCY, totalZ * RIVER_FREQUENCY, 0) * 0.9588;
        river += OpenSimplex2S.noise3_ImproveXY(SEED ^ 0x7C46A6B469AC4A05L, totalX * RIVER_FREQUENCY * 50, totalZ * RIVER_FREQUENCY * 50, 0) * 0.0411;
        return river;
    }

    private static double[][] ridgeMapPadded(int chunkX, int chunkZ) {
        double[][] ridgeMap = new double[CHUNK_SIZE + 2][CHUNK_SIZE + 2];
        for (int mapX = 0; mapX < CHUNK_SIZE + 2; mapX++)
            for (int mapZ = 0; mapZ < CHUNK_SIZE + 2; mapZ++) {
                int currentX = (chunkX << CHUNK_SIZE_BITS) + mapX - 1;
                int currentZ = (chunkZ << CHUNK_SIZE_BITS) + mapZ - 1;
                double river = ridgeMapValue(currentX, currentZ);
                ridgeMap[mapX][mapZ] = river;
            }
        return ridgeMap;
    }

    public static double ridgeMapValue(int totalX, int totalZ) {
        double ridge;
        ridge = (1 - Math.abs(OpenSimplex2S.noise3_ImproveXY(SEED ^ 0xDD4D88700A5E4D7EL, totalX * RIDGE_FREQUENCY, totalZ * RIDGE_FREQUENCY, 0))) * 0.5;
        ridge += (1 - Math.abs(OpenSimplex2S.noise3_ImproveXY(SEED ^ 0x8A3E12DE957E78C5L, totalX * RIDGE_FREQUENCY * 2, totalZ * RIDGE_FREQUENCY * 2, 0))) * 0.25;
        ridge += (1 - Math.abs(OpenSimplex2S.noise3_ImproveXY(SEED ^ 0x0A8E80B850A75321L, totalX * RIDGE_FREQUENCY * 4, totalZ * RIDGE_FREQUENCY * 4, 0))) * 0.125;
        ridge += (1 - Math.abs(OpenSimplex2S.noise3_ImproveXY(SEED ^ 0x6E0744EACB517937L, totalX * RIDGE_FREQUENCY * 8, totalZ * RIDGE_FREQUENCY * 8, 0))) * 0.0625;
        return ridge;
    }

    private int[] treeBitMap(int chunkX, int chunkZ) {
        int[] treeBitMap = new int[CHUNK_SIZE];
        Random random = new Random(Utils.getChunkId(chunkX, 0, chunkZ));

        // Places 16 trees each in the central 6 x 6 of the 16 8 x 8s of a chunk
        for (int regionX = 0; regionX < CHUNK_SIZE; regionX += 8)
            for (int regionZ = 0; regionZ < CHUNK_SIZE; regionZ += 8) {
                int inRegionX = random.nextInt(1, 7);
                int inRegionZ = random.nextInt(1, 7);

                treeBitMap[regionX + inRegionX] |= 1 << regionZ + inRegionZ;
            }

        return treeBitMap;
    }

    private final double[] temperatureMap;
    private final double[] humidityMap;
    private final double[] featureMap;
    private final double[][] erosionMap;
    private final double[][] continentalMap;

    private final int[][] resultingHeightMap;
    private final byte[] steepnessMap;
    private final int[] treeBitMap;

    private long[] caveBitMap;

    private static final double TEMPERATURE_FREQUENCY = 0.001;
    private static final double HUMIDITY_FREQUENCY = TEMPERATURE_FREQUENCY;
    private static final double HEIGHT_MAP_FREQUENCY = 0.0025;
    private static final double EROSION_FREQUENCY = 0.001;
    private static final double CONTINENTAL_FREQUENCY = 0.00025;
    private static final double RIVER_FREQUENCY = 0.0005;
    private static final double RIDGE_FREQUENCY = 0.005;
}
