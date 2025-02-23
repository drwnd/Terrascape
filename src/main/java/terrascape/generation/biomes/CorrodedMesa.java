package terrascape.generation.biomes;

import terrascape.generation.GenerationData;
import terrascape.generation.OpenSimplex2S;

import static terrascape.generation.WorldGeneration.*;
import static terrascape.utils.Constants.*;
import static terrascape.utils.Constants.RED_SAND;
import static terrascape.utils.Settings.SEED;

public final class CorrodedMesa extends Biome {
    @Override
    public boolean placeBlock(int inChunkX, int inChunkY, int inChunkZ, GenerationData data) {
        int totalY = data.chunk.Y << CHUNK_SIZE_BITS | inChunkY;

        int pillarHeight = data.specialHeight;
        int floorBlockDepth = 3 - (data.steepness >> 1) + (int) (data.feature * 4.0);
        if (pillarHeight != 0 && totalY >= data.height - floorBlockDepth) {
            if (totalY > data.height + pillarHeight) return false;
            data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingTerracottaType(totalY & 15));
            return true;
        }

        if (totalY >= data.height) {
            boolean placedBlock;
            placedBlock = genCactus(inChunkX, inChunkY, inChunkZ, totalY, data);
            placedBlock |= genFeature(inChunkX, inChunkY, inChunkZ, SHRUB_THRESHOLD, SHRUB, data);
            if (placedBlock) return true;
        }
        if (totalY > data.height) return false;

        if (totalY < data.height - floorBlockDepth - 5) return false;   // Stone placed by caller
        if (totalY < data.height - floorBlockDepth) data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, RED_SANDSTONE);
        else data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, RED_SAND);
        return true;
    }

    @Override
    public void genSurroundingStructures(int inChunkX, int inChunkY, int inChunkZ, GenerationData data) {

    }

    @Override
    public int getSpecialHeight(int totalX, int totalZ, GenerationData data) {
        // Cave at surface lightLevel prevents pillar generation
        if (data.chunk.Y << CHUNK_SIZE_BITS <= data.height && data.chunk.Y + 1 << CHUNK_SIZE_BITS > data.height) {
            if (data.caveBits >> ((data.height & CHUNK_SIZE_MASK) << 1) != NO_CAVE) return 0;
        } else if (getCaveType(totalX, data.height, totalZ) != NO_CAVE) return 0;

        double noise = OpenSimplex2S.noise2(SEED ^ 0xDF860F2E2A604A17L, totalX * MESA_PILLAR_FREQUENCY, totalZ * MESA_PILLAR_FREQUENCY);
        if (Math.abs(noise) > MESA_PILLAR_THRESHOLD) return MESA_PILLAR_HEIGHT;
        return 0;
    }
}
