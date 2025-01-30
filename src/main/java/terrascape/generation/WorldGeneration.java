package terrascape.generation;

import terrascape.generation.biomes.*;
import terrascape.server.Block;
import terrascape.dataStorage.Chunk;
import terrascape.dataStorage.Structure;
import terrascape.utils.Utils;

import java.util.ArrayList;
import java.util.Random;

import static terrascape.utils.Constants.*;
import static terrascape.utils.Settings.*;

public final class WorldGeneration {

    public static final int NO_CAVE = 0;
    public static final int WATER_LEVEL = 0;
    public static final int SNOW_LEVEL = WATER_LEVEL + 91;
    public static final int ICE_LEVEL = WATER_LEVEL + 141;

    public static final double ICE_BERG_FREQUENCY = 0.025;
    public static final double ICE_BERG_THRESHOLD = 0.45;
    public static final double ICE_BERG_HEIGHT = 8;
    public static final double ICE_PLANE_THRESHOLD = 0.3;

    public static final double MESA_PILLAR_THRESHOLD = 0.55;
    public static final double MESA_PILLAR_FREQUENCY = 0.03;
    public static final int MESA_PILLAR_HEIGHT = 25;

    public static final double PLAINS_TREE_THRESHOLD = 0.01;
    public static final double FOREST_TREE_THRESHOLD = 0;
    public static final double REDWOOD_FOREST_TREE_THRESHOLD = 0.5;
    public static final double TALL_GRASS_THRESHOLD = 0.25;
    public static final double SHRUB_THRESHOLD = 0.03;
    public static final double FLOWER_THRESHOLD = 0.26;
    public static final double SPARSE_FLOWER_THRESHOLD = 0.01;

    public static void init() {
        BIOMES[DESERT] = new Desert();
        BIOMES[WASTELAND] = new Wasteland();
        BIOMES[DARK_OAK_FOREST] = new DarkOakForest();
        BIOMES[SNOWY_SPRUCE_FOREST] = new SnowySpruceForest();
        BIOMES[SNOWY_PLAINS] = new SnowyPlains();
        BIOMES[SPRUCE_FOREST] = new SpruceForest();
        BIOMES[PLAINS] = new Plains();
        BIOMES[OAK_FOREST] = new OakForest();
        BIOMES[WARM_OCEAN] = new WarmOcean();
        BIOMES[COLD_OCEAN] = new ColdOcean();
        BIOMES[OCEAN] = new Ocean();
        BIOMES[DRY_MOUNTAIN] = new DryMountain();
        BIOMES[SNOWY_MOUNTAIN] = new SnowyMountain();
        BIOMES[MOUNTAIN] = new Mountain();
        BIOMES[MESA] = new Mesa();
        BIOMES[CORRODED_MESA] = new CorrodedMesa();
        BIOMES[BEACH] = new Beach();
        BIOMES[PINE_FOREST] = new PineForest();
        BIOMES[REDWOOD_FOREST] = new RedwoodForest();
        BIOMES[BLACK_WOOD_FOREST] = new BlackWoodForest();
    }

    public static void generateSurroundingChunkStructureBlocks(Chunk chunk) {
        if (chunk.isGenerated()) return;
        generateSurroundingChunkStructureBlocks(chunk, new GenerationData(chunk.X, chunk.Z));
    }

    public static void generateSurroundingChunkStructureBlocks(Chunk chunk, GenerationData generationData) {
        generationData.setChunk(chunk);

        for (int inChunkX = 0; inChunkX < CHUNK_SIZE; inChunkX++)
            for (int inChunkZ = 0; inChunkZ < CHUNK_SIZE; inChunkZ++) {
                generationData.set(inChunkX, inChunkZ);
                int biomeIndex = getBiome(generationData);
                generationData.setBiome(inChunkX, inChunkZ, BIOMES[biomeIndex]);

                for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
                    int totalY = chunk.getWorldCoordinate().y | inChunkY;
                    if (totalY < generationData.height) continue;
                    Biome biome = BIOMES[biomeIndex];
                    biome.genSurroundingStructures(inChunkX, inChunkY, inChunkZ, generationData);
                }
            }
    }

    public static void generate(Chunk chunk) {
        if (chunk.isGenerated()) {
            Chunk.removeToGenerateBlocks(chunk.ID);
            return;
        }
        generate(chunk, new GenerationData(chunk.X, chunk.Z));
    }

    public static void generate(Chunk chunk, GenerationData generationData) {
        if (chunk.isGenerated()) {
            Chunk.removeToGenerateBlocks(chunk.ID);
            return;
        }
        chunk.setGenerated();

        generationData.setChunk(chunk);

        for (int inChunkX = 0; inChunkX < CHUNK_SIZE; inChunkX++)
            for (int inChunkZ = 0; inChunkZ < CHUNK_SIZE; inChunkZ++) {

                generationData.set(inChunkX, inChunkZ);
                Biome biome = BIOMES[getBiome(generationData)];
                generationData.setBiome(inChunkX, inChunkZ, biome);

                generateBiome(biome, inChunkX, inChunkZ, generationData);
            }

        genOres(generationData);

        ArrayList<Integer> toGenerateBlocks = Chunk.removeToGenerateBlocks(chunk.ID);
        if (toGenerateBlocks != null) {
            for (int data : toGenerateBlocks) {
                short block = (short) (data >> 16 & 0xFFFF);
                int inChunkX = data >> CHUNK_SIZE_BITS * 2 & CHUNK_SIZE_MASK;
                int inChunkY = data >> CHUNK_SIZE_BITS & CHUNK_SIZE_MASK;
                int inChunkZ = data & CHUNK_SIZE_MASK;

                int index = inChunkX << CHUNK_SIZE_BITS * 2 | inChunkZ << CHUNK_SIZE_BITS | inChunkY;
                if (chunk.getSaveBlock(index) != AIR && Block.isLeaveType(block)) continue;
                chunk.storeSave(inChunkX, inChunkY, inChunkZ, block);
            }
        }

        chunk.optimizeBlockStorage();
    }

    private static void generateBiome(Biome biome, int inChunkX, int inChunkZ, GenerationData data) {
        for (int inChunkY = 0; inChunkY < CHUNK_SIZE; inChunkY++) {
            int totalY = data.chunk.Y << CHUNK_SIZE_BITS | inChunkY;
            boolean placedBlock = false;

            int caveType = (int) (data.caveBits >> (inChunkY << 1) & 3);
            // Either there is no cave OR a thin layer separating caves form the ocean floor OR everything above surface (trees)
            if (caveType == NO_CAVE || data.height <= WATER_LEVEL && totalY >= data.height - 1 && totalY <= WATER_LEVEL && caveType != WATER_CAVE || totalY > data.height) {
                // Attempting to place biome specific blocks and features
                placedBlock = biome.placeBlock(inChunkX, inChunkY, inChunkZ, data);

                // Placing stone beneath surface blocks
                if (!placedBlock && totalY <= data.height) {
                    int totalX = data.chunk.X << CHUNK_SIZE_BITS | inChunkX;
                    int totalZ = data.chunk.Z << CHUNK_SIZE_BITS | inChunkZ;
                    data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingStoneType(totalX, totalY, totalZ));
                }

            } else if (caveType == WATER_CAVE && totalY <= WATER_LEVEL)
                data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER_SOURCE);

            else if (caveType == LAVA_CAVE && totalY <= WATER_LEVEL)
                data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, LAVA_SOURCE);

            // Filling Oceans with water
            if (totalY > data.height && totalY <= WATER_LEVEL && !placedBlock)
                data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, WATER_SOURCE);
        }
    }


    public static boolean genTree(int inChunkX, int inChunkY, int inChunkZ, double threshold, byte name, GenerationData data) {
        Structure tree = Structure.getStructureVariation(name, inChunkX, data.height, inChunkZ);
        int totalY = data.chunk.Y << CHUNK_SIZE_BITS | inChunkY;

        if (treeCannotSpawn(data.chunk.X << CHUNK_SIZE_BITS | inChunkX, totalY, data.chunk.Z << CHUNK_SIZE_BITS | inChunkZ, threshold, tree.lengthY(), data))
            return false;

        byte transform = Structure.getStructureTransform(data.feature, threshold, 1.0);
        int xOffset = tree.lengthX() >> 1;
        int zOffset = tree.lengthZ() >> 1;

        for (int i = 0; i < tree.lengthZ(); i++)
            for (int j = 0; j < tree.lengthX(); j++) {
                short block = tree.get(j, totalY - data.height, i, transform);
                data.chunk.storeStructureBlock(inChunkX + j - xOffset, inChunkY, inChunkZ + i - zOffset, block);
            }
        return true;
    }

    public static boolean genCactus(int inChunkX, int inChunkY, int inChunkZ, int totalY, GenerationData data) {
        if (data.feature < CACTUS_THRESHOLD || data.height <= WATER_LEVEL || totalY == data.height || !(totalY < data.height + 1 + (data.feature - CACTUS_THRESHOLD) * 500))
            return false;

        if (featureCannotSpawn(inChunkX, inChunkY, inChunkZ, data)) return false;

        data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, CACTUS);
        return true;
    }

    public static boolean genSugarcane(int inChunkX, int inChunkY, int inChunkZ, int totalY, GenerationData data) {
        if (data.feature < SUGARCANE_THRESHOLD || data.height != WATER_LEVEL || totalY == data.height || !(totalY < data.height + 1 + (data.feature - SUGARCANE_THRESHOLD) * 100))
            return false;

        if (featureCannotSpawn(inChunkX, inChunkY, inChunkZ, data)) return false;

        data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, SUGAR_CANE);
        return true;
    }


    public static boolean genFeature(int inChunkX, int inChunkY, int inChunkZ, double threshold, short block, GenerationData data) {
        if (data.feature > threshold || data.height <= WATER_LEVEL || ((data.chunk.Y << CHUNK_SIZE_BITS) | inChunkY) != data.height + 1) return false;

        if (featureCannotSpawn(inChunkX, inChunkY, inChunkZ, data)) return false;

        data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, block);
        return true;
    }

    public static void genSurroundingTree(int inChunkX, int inChunkY, int inChunkZ, double threshold, byte name, GenerationData data) {
        Structure tree = Structure.getStructureVariation(name, inChunkX, data.height, inChunkZ);
        int totalY = data.chunk.Y << CHUNK_SIZE_BITS | inChunkY;

        if (treeCannotSpawn(data.chunk.X << CHUNK_SIZE_BITS | inChunkX, totalY, data.chunk.Z << CHUNK_SIZE_BITS | inChunkZ, threshold, tree.lengthY(), data))
            return;

        byte transform = Structure.getStructureTransform(data.feature, threshold, 1.0);
        int xOffset = tree.lengthX() >> 1;
        int zOffset = tree.lengthZ() >> 1;

        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++) {
                short block = tree.get(j, totalY - data.height, i, transform);
                data.chunk.storeSurroundingChunkStructureBlock(inChunkX + j - xOffset, inChunkY, inChunkZ + i - zOffset, block);
            }
    }

    private static boolean treeCannotSpawn(int totalX, int totalY, int totalZ, double threshold, int treeHeight, GenerationData data) {
        if (!data.treeAllowed || data.feature <= threshold || totalY >= data.height + treeHeight || data.height <= WATER_LEVEL || data.steepness > 1)
            return true;
        if (data.height >= (totalY & CHUNK_COORDINATE_MASK))
            return (data.caveBits >> ((data.height & CHUNK_SIZE_MASK) << 1) & 3) != NO_CAVE;
        return getCaveType(totalX, data.height, totalZ) != NO_CAVE;
    }

    private static boolean featureCannotSpawn(int inChunkX, int inChunkY, int inChunkZ, GenerationData data) {
        if (data.chunk.getSaveBlock(inChunkX, inChunkY, inChunkZ) != AIR || data.steepness > 5) return true;

        int totalX = data.chunk.X << CHUNK_SIZE_BITS | inChunkX;
        int totalY = data.chunk.Y << CHUNK_SIZE_BITS | inChunkY;
        int totalZ = data.chunk.Z << CHUNK_SIZE_BITS | inChunkZ;

        if (data.height >= (totalY & CHUNK_COORDINATE_MASK))
            return (data.caveBits >> ((data.height & CHUNK_SIZE_MASK) << 1) & 3) != NO_CAVE;
        return getCaveType(totalX, data.height, totalZ) != NO_CAVE;
    }


    public static int getCaveType(int x, int y, int z) {
        double noodleCaveHeightBias = Math.max(y + 96, 0) * NOODLE_CAVE_HEIGHT_BIAS;
        double blobCaveHeightBias = Math.max(y + 96, 0) * BLOB_CAVE_CAVE_HEIGHT_BIAS;

        // Air cave
        double noise1 = OpenSimplex2S.noise3_ImproveXY(SEED ^ 0xBD5FA5026D01E1B3L, x * AIR_NOODLE_CAVE_FREQUENCY, y * AIR_NOODLE_CAVE_FREQUENCY, z * AIR_NOODLE_CAVE_FREQUENCY) + noodleCaveHeightBias;
        double noise2 = OpenSimplex2S.noise3_ImproveXY(SEED ^ 0x943983332AB965CCL, x * AIR_NOODLE_CAVE_FREQUENCY, y * AIR_NOODLE_CAVE_FREQUENCY, z * AIR_NOODLE_CAVE_FREQUENCY) + noodleCaveHeightBias;

        double noodleCaveValue = noise1 * noise1 + noise2 * noise2;

        double blobCaveNoise = OpenSimplex2S.noise3_ImproveXY(SEED ^ 0x16D362B27590AD29L, x * AIR_BLOB_CAVE_FREQUENCY, y * AIR_BLOB_CAVE_FREQUENCY, z * AIR_BLOB_CAVE_FREQUENCY) * 0.5555;
        blobCaveNoise += OpenSimplex2S.noise3_ImproveXY(SEED ^ 0x2072C0CE89069903L, x * 0.02, y * 0.02, z * 0.02) * 0.4444;
        blobCaveNoise -= blobCaveHeightBias;

        boolean insideCave = blobCaveNoise > AIR_BLOB_CAVE_THRESHOLD || noodleCaveValue < AIR_NOODLE_CAVE_THRESHOLD;

        if ((blobCaveNoise > AIR_BLOB_CAVE_THRESHOLD - 0.05 || noodleCaveValue < AIR_NOODLE_CAVE_THRESHOLD + 0.009) && !insideCave)
            return NO_CAVE;
        if (insideCave) return AIR_CAVE;

        // Water cave
        noise1 = OpenSimplex2S.noise3_ImproveXY(SEED ^ 0xF5C0E4D935C4B35BL, x * WATER_NOODLE_CAVE_FREQUENCY, y * WATER_NOODLE_CAVE_FREQUENCY, z * WATER_NOODLE_CAVE_FREQUENCY) + noodleCaveHeightBias;
        noise2 = OpenSimplex2S.noise3_ImproveXY(SEED ^ 0x20CF705EBBF22073L, x * WATER_NOODLE_CAVE_FREQUENCY, y * WATER_NOODLE_CAVE_FREQUENCY, z * WATER_NOODLE_CAVE_FREQUENCY) + noodleCaveHeightBias;

        noodleCaveValue = noise1 * noise1 + noise2 * noise2;

        blobCaveNoise = OpenSimplex2S.noise3_ImproveXY(SEED ^ 0xBE793D143F2B660AL, x * WATER_BLOB_CAVE_FREQUENCY, y * WATER_BLOB_CAVE_FREQUENCY, z * WATER_BLOB_CAVE_FREQUENCY) * 0.5555;
        blobCaveNoise += OpenSimplex2S.noise3_ImproveXY(SEED ^ 0x75B1F9C543265E72L, x * 0.02, y * 0.02, z * 0.02) * 0.4444;
        blobCaveNoise -= blobCaveHeightBias;

        insideCave = blobCaveNoise > WATER_BLOB_CAVE_THRESHOLD || noodleCaveValue < WATER_NOODLE_CAVE_THRESHOLD;

        if ((blobCaveNoise > WATER_BLOB_CAVE_THRESHOLD - 0.05 || noodleCaveValue < WATER_NOODLE_CAVE_THRESHOLD + 0.009) && !insideCave)
            return NO_CAVE;
        if (insideCave) return WATER_CAVE;

        // Lava cave
        noise1 = OpenSimplex2S.noise3_ImproveXY(SEED ^ 0x27BC3182C4129D96L, x * LAVA_NOODLE_CAVE_FREQUENCY, y * LAVA_NOODLE_CAVE_FREQUENCY, z * LAVA_NOODLE_CAVE_FREQUENCY) + noodleCaveHeightBias;
        noise2 = OpenSimplex2S.noise3_ImproveXY(SEED ^ 0xE445979F9FEE7DAAL, x * LAVA_NOODLE_CAVE_FREQUENCY, y * LAVA_NOODLE_CAVE_FREQUENCY, z * LAVA_NOODLE_CAVE_FREQUENCY) + noodleCaveHeightBias;

        noodleCaveValue = noise1 * noise1 + noise2 * noise2;

        insideCave = noodleCaveValue < LAVA_NOODLE_CAVE_THRESHOLD;

        if (insideCave) return LAVA_CAVE;

        return NO_CAVE;
    }

    public static long[] generateCaveBitMap(Chunk chunk, int[][] heightMap) {
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
                    if (y > heightMap[inChunkX + 1][inChunkZ + 1] && y > heightMap[inChunkX + 1][inChunkZ + 4] && y > heightMap[inChunkX + 4][inChunkZ + 1] && y > heightMap[inChunkX + 4][inChunkZ + 4])
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


    private static void genOres(GenerationData generationData) {
        Random random = new Random(generationData.chunk.ID);
        genCoalOres(generationData, random);
        genIronOres(generationData, random);
        genDiamondOres(generationData, random);
    }

    private static void genCoalOres(GenerationData data, Random random) {
        int amountOfVeins = (int) (data.humidity + 1) * 4 + 4;

        for (int i = 0; i < amountOfVeins; i++) {
            int inChunkX = random.nextInt(CHUNK_SIZE);
            int inChunkY = random.nextInt(CHUNK_SIZE);
            int inChunkZ = random.nextInt(CHUNK_SIZE);

            genOreVein(data, inChunkX, inChunkY, inChunkZ, (byte) 20, COAL_ORE, random);
        }
    }

    private static void genIronOres(GenerationData data, Random random) {
        int amountOfVeins = (int) ((data.continental + 1) * 4 + 4);

        for (int i = 0; i < amountOfVeins; i++) {
            int inChunkX = random.nextInt(CHUNK_SIZE);
            int inChunkY = random.nextInt(CHUNK_SIZE);
            int inChunkZ = random.nextInt(CHUNK_SIZE);

            genOreVein(data, inChunkX, inChunkY, inChunkZ, (byte) 10, IRON_ORE, random);
        }
    }

    private static void genDiamondOres(GenerationData data, Random random) {
        if (data.chunk.Y >= -3) return;

        int amountOfVeins = 4;

        for (int i = 0; i < amountOfVeins; i++) {
            int inChunkX = random.nextInt(CHUNK_SIZE);
            int inChunkY = random.nextInt(CHUNK_SIZE);
            int inChunkZ = random.nextInt(CHUNK_SIZE);

            genOreVein(data, inChunkX, inChunkY, inChunkZ, (byte) 5, DIAMOND_ORE, random);
        }
    }

    private static void genOreVein(GenerationData data, int inChunkX, int inChunkY, int inChunkZ, byte oreCount, short ore, Random random) {

        double distance = 1;
        while (oreCount-- > 0) {
            int oreX = (int) (inChunkX + random.nextDouble() * distance - distance * 0.5);
            int oreY = (int) (inChunkY + random.nextDouble() * distance - distance * 0.5);
            int oreZ = (int) (inChunkZ + random.nextDouble() * distance - distance * 0.5);

            if (!Chunk.isValidPosition(oreX, oreY, oreZ)) continue;
            if ((data.chunk.Y << CHUNK_SIZE_BITS) + oreY >= data.getHeight(oreX, oreZ)) continue;
            if (data.getCaveBits(oreX, oreZ) >> (oreY << 1) != NO_CAVE) continue;
            distance += 0.25;

            data.chunk.storeSave(oreX, oreY, oreZ, ore);
        }
    }


    private static short getGeneratingStoneType(int x, int y, int z) {
        double noise = OpenSimplex2S.noise3_ImproveXY(SEED ^ 0x1FCA4F81678D9EFEL, x * STONE_TYPE_FREQUENCY, y * STONE_TYPE_FREQUENCY, z * STONE_TYPE_FREQUENCY);
        double dither = ((x * 0x8D2DD55FDBC32B66L) ^ (y * 0xACE124B15269BF3EL) ^ (z * 0x70A0A3D560EE6D5CL)) * 5.0842021724855044E-21;
        noise += dither;
        if (Math.abs(noise) < ANDESITE_THRESHOLD) return ANDESITE;
        if (noise > SLATE_THRESHOLD) return SLATE;
        if (noise < BLACKSTONE_THRESHOLD) return BLACKSTONE;
        return STONE;
    }

    public static short getOceanFloorBlock(int x, int y, int z) {
        double noise = OpenSimplex2S.noise3_ImproveXY(SEED ^ 0x30CD70827706B4C0L, x * MUD_TYPE_FREQUENCY, y * MUD_TYPE_FREQUENCY, z * MUD_TYPE_FREQUENCY);
        if (Math.abs(noise) < GRAVEL_THRESHOLD) return GRAVEL;
        if (noise > CLAY_THRESHOLD) return CLAY;
        if (noise < SAND_THRESHOLD) return SAND;
        return MUD;
    }

    public static short getWarmOceanFloorBlocK(int x, int y, int z) {
        double noise = OpenSimplex2S.noise3_ImproveXY(SEED ^ 0xEB26D0A3459AAA03L, x * MUD_TYPE_FREQUENCY, y * MUD_TYPE_FREQUENCY, z * MUD_TYPE_FREQUENCY);
        if (Math.abs(noise) < GRAVEL_THRESHOLD) return GRAVEL;
        if (noise > CLAY_THRESHOLD) return CLAY;
        if (noise < MUD_THRESHOLD) return MUD;
        return SAND;
    }

    public static short getColdOceanFloorBlock(int x, int y, int z) {
        double noise = OpenSimplex2S.noise3_ImproveXY(SEED ^ 0x7A182AB93793E000L, x * MUD_TYPE_FREQUENCY, y * MUD_TYPE_FREQUENCY, z * MUD_TYPE_FREQUENCY);
        if (Math.abs(noise) < GRAVEL_THRESHOLD) return GRAVEL;
        if (noise > CLAY_THRESHOLD) return CLAY;
        if (noise < MUD_THRESHOLD) return MUD;
        return GRAVEL;
    }

    public static short getGeneratingDirtType(int x, int y, int z) {
        double noise = OpenSimplex2S.noise3_ImproveXY(SEED ^ 0xF88966EA665D953EL, x * DIRT_TYPE_FREQUENCY, y * DIRT_TYPE_FREQUENCY, z * DIRT_TYPE_FREQUENCY);
        if (Math.abs(noise) < COURSE_DIRT_THRESHOLD) return COURSE_DIRT;
        return DIRT;
    }

    public static short getGeneratingIceType(int x, int y, int z) {
        double noise = OpenSimplex2S.noise3_ImproveXY(SEED ^ 0xD6744EFC8D01AEFCL, x * ICE_TYPE_FREQUENCY, y * ICE_TYPE_FREQUENCY, z * ICE_TYPE_FREQUENCY);
        if (noise > HEAVY_ICE_THRESHOLD) return HEAVY_ICE;
        return ICE;
    }

    public static short getGeneratingTerracottaType(int terracottaIndex) {
        return switch (terracottaIndex) {
            case 3, 6, 10, 11, 15 -> RED_TERRACOTTA;
            case 2, 8, 12 -> YELLOW_TERRACOTTA;
            default -> TERRACOTTA;
        };
    }

    public static short getGeneratingGrassType(int x, int z, GenerationData data) {
        double noise = OpenSimplex2S.noise2(SEED ^ 0xEFB13EFD3B5AC7A7L, x * GRASS_TYPE_FREQUENCY, z * GRASS_TYPE_FREQUENCY);
        noise += data.feature * 0.4 - 0.2;
        if (Math.abs(noise) < MOSS_THRESHOLD) return MOSS;
        return GRASS;
    }


    public static int getResultingHeight(double height, double erosion, double continental, double river, double ridge) {
        height = (height * 0.5 + 0.5) * MAX_TERRAIN_HEIGHT_DIFFERENCE;

        double continentalModifier = getContinentalModifier(continental, ridge);
        double erosionModifier = getErosionModifier(height, erosion, continentalModifier);
        double riverModifier = getRiverModifier(height, continentalModifier, erosionModifier, river);

        return Utils.floor(height + continentalModifier + erosionModifier + riverModifier) + WATER_LEVEL - 15;
    }

    private static double getContinentalModifier(double continental, double ridge) {
        double continentalModifier = 0.0;
        // Mountains
        if (continental > MOUNTAIN_THRESHOLD)
            continentalModifier = (continental - MOUNTAIN_THRESHOLD) * (continental - MOUNTAIN_THRESHOLD) * ridge * 3000;
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
        return erosionModifier;
    }

    private static double getRiverModifier(double height, double continentalModifier, double erosionModifier, double river) {
        double riverModifier = 0.0;
        if (Math.abs(river) < 0.005)
            riverModifier = -height * 0.85 - continentalModifier - erosionModifier + RIVER_LEVEL;
        else if (Math.abs(river) < RIVER_THRESHOLD)
            riverModifier = -(continentalModifier + erosionModifier + height * 0.85 - RIVER_LEVEL) * (1 - Utils.smoothInOutQuad(Math.abs(river), 0.005, RIVER_THRESHOLD));
        return riverModifier;
    }

    public static int getResultingHeight(int totalX, int totalZ) {
        double height = GenerationData.heightMapValue(totalX, totalZ);
        double erosion = GenerationData.erosionMapValue(totalX, totalZ);
        double continental = GenerationData.continentalMapValue(totalX, totalZ);
        double river = GenerationData.riverMapValue(totalX, totalZ);
        double ridge = GenerationData.ridgeMapValue(totalX, totalZ);

        return getResultingHeight(height, erosion, continental, river, ridge);
    }

    public static int[][] getResultingHeightMap(double[][] heightMap, double[][] erosionMap, double[][] continentalMap, double[][] riverMap, double[][] ridgeMap) {
        int[][] resultingHeightMap = new int[heightMap.length][heightMap.length];
        for (int mapX = 0; mapX < heightMap.length; mapX++)
            for (int mapZ = 0; mapZ < heightMap.length; mapZ++) {

                double height = heightMap[mapX][mapZ];
                double erosion = erosionMap[mapX][mapZ];
                double continental = continentalMap[mapX][mapZ];
                double river = riverMap[mapX][mapZ];
                double ridge = ridgeMap[mapX][mapZ];

                resultingHeightMap[mapX][mapZ] = getResultingHeight(height, erosion, continental, river, ridge);
            }

        return resultingHeightMap;
    }


    private static int getBiome(GenerationData data) {
        int beachHeight = WATER_LEVEL + (int) (data.feature * 4.0) + 4;
        double dither = data.feature * 0.08f - 0.04f;
        double temperature = data.temperature + dither;
        double humidity = data.humidity + dither;
        double erosion = data.erosion + dither;
        double continental = data.continental + dither;

        if (data.height < WATER_LEVEL) {
            if (temperature > 0.33) return WARM_OCEAN;
            else if (temperature < -0.33) return COLD_OCEAN;
            return OCEAN;
        }
        if (data.height < beachHeight) {
            return BEACH;
        }
        if (continental > MOUNTAIN_THRESHOLD && erosion < 0.425) {
            if (temperature > 0.33) return DRY_MOUNTAIN;
            else if (temperature < -0.33) return SNOWY_MOUNTAIN;
            return MOUNTAIN;
        }

        if (temperature > 0.33) {
            if (temperature > 0.45 && humidity < -0.3) return CORRODED_MESA;
            if (temperature > 0.55 && humidity < 0.15) return MESA;
            if (humidity < 0.15) return DESERT;
            if (humidity > 0.5 && temperature > 0.5) return BLACK_WOOD_FOREST;
            if (humidity > 0.4 && temperature > 0.4) return DARK_OAK_FOREST;
            return WASTELAND;
        }
        if (humidity > 0.33) {
            if (temperature > -0.1) return REDWOOD_FOREST;
            if (temperature > -0.4) return SPRUCE_FOREST;
            return SNOWY_SPRUCE_FOREST;
        }
        if (humidity < 0.0 && temperature > -0.25) return PLAINS;
        if (humidity > -0.33 && temperature > -0.33) return OAK_FOREST;
        if (humidity < -0.33 && temperature > -0.5) return PINE_FOREST;
        return SNOWY_PLAINS;

    }

    private static final int OCEAN_FLOOR_LEVEL = WATER_LEVEL - 30;
    private static final int DEEP_OCEAN_FLOOR_OFFSET = -70;
    private static final int FLATLAND_LEVEL = 30 + 15;
    private static final int RIVER_LEVEL = WATER_LEVEL - 15;

    private static final double MAX_TERRAIN_HEIGHT_DIFFERENCE = 100;

    private static final double MOUNTAIN_THRESHOLD = 0.3;    // Continental
    private static final double OCEAN_THRESHOLD = -0.3;      // Continental
    private static final double FLATLAND_THRESHOLD = 0.3;    // Erosion
    private static final double RIVER_THRESHOLD = 0.1;       // Erosion

    private static final double BLOB_CAVE_CAVE_HEIGHT_BIAS = 0.008;
    private static final double NOODLE_CAVE_HEIGHT_BIAS = 0.004;

    private static final double AIR_BLOB_CAVE_FREQUENCY = 0.006;
    private static final double AIR_BLOB_CAVE_THRESHOLD = 0.36;
    private static final double AIR_NOODLE_CAVE_FREQUENCY = 0.008;
    private static final double AIR_NOODLE_CAVE_THRESHOLD = 0.008;

    private static final double WATER_BLOB_CAVE_FREQUENCY = 0.004;
    private static final double WATER_BLOB_CAVE_THRESHOLD = 0.45;
    private static final double WATER_NOODLE_CAVE_FREQUENCY = 0.005;
    private static final double WATER_NOODLE_CAVE_THRESHOLD = 0.005;

    private static final double LAVA_NOODLE_CAVE_FREQUENCY = 0.002;
    private static final double LAVA_NOODLE_CAVE_THRESHOLD = 0.0012;

    private static final int AIR_CAVE = 1;
    private static final int WATER_CAVE = 2;
    private static final int LAVA_CAVE = 3;

    private static final double CACTUS_THRESHOLD = 0.992;
    private static final double SUGARCANE_THRESHOLD = 0.96;

    private static final double STONE_TYPE_FREQUENCY = 0.02;
    private static final double ANDESITE_THRESHOLD = 0.1;
    private static final double SLATE_THRESHOLD = 0.7;
    private static final double BLACKSTONE_THRESHOLD = -0.7;

    private static final double MUD_TYPE_FREQUENCY = 0.04;
    private static final double GRAVEL_THRESHOLD = 0.1;
    private static final double CLAY_THRESHOLD = 0.5;
    private static final double SAND_THRESHOLD = -0.5;
    private static final double MUD_THRESHOLD = -0.5;

    private static final double DIRT_TYPE_FREQUENCY = 0.05;
    private static final double COURSE_DIRT_THRESHOLD = 0.15;

    private static final double GRASS_TYPE_FREQUENCY = 0.025;
    private static final double MOSS_THRESHOLD = 0.3;

    private static final double ICE_TYPE_FREQUENCY = 0.08;
    private static final double HEAVY_ICE_THRESHOLD = 0.6;

    private static final int DESERT = 0;
    private static final int WASTELAND = 1;
    private static final int DARK_OAK_FOREST = 2;
    private static final int SNOWY_SPRUCE_FOREST = 3;
    private static final int SNOWY_PLAINS = 4;
    private static final int SPRUCE_FOREST = 5;
    private static final int PLAINS = 6;
    private static final int OAK_FOREST = 7;
    private static final int WARM_OCEAN = 8;
    private static final int COLD_OCEAN = 9;
    private static final int OCEAN = 10;
    private static final int DRY_MOUNTAIN = 11;
    private static final int SNOWY_MOUNTAIN = 12;
    private static final int MOUNTAIN = 13;
    private static final int MESA = 14;
    private static final int CORRODED_MESA = 15;
    private static final int BEACH = 16;
    private static final int PINE_FOREST = 17;
    private static final int REDWOOD_FOREST = 18;
    private static final int BLACK_WOOD_FOREST = 19;

    private static final Biome[] BIOMES = new Biome[20];

    private WorldGeneration() { }
}