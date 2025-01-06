package terrascape.generation.biomes;

import static terrascape.generation.WorldGeneration.*;
import static terrascape.utils.Constants.CHUNK_SIZE_BITS;
import static terrascape.utils.Constants.SAND;
import static terrascape.utils.Settings.SEED;

import terrascape.generation.GenerationData;
import terrascape.generation.OpenSimplex2S;
import terrascape.utils.Utils;

public class ColdOcean extends Biome {
    @Override
    public boolean placeBlock(int inChunkX, int inChunkY, int inChunkZ, GenerationData data) {
        int totalX = data.chunk.X << CHUNK_SIZE_BITS | inChunkX;
        int totalY = data.chunk.Y << CHUNK_SIZE_BITS | inChunkY;
        int totalZ = data.chunk.Z << CHUNK_SIZE_BITS | inChunkZ;

        int iceHeight = Math.min(data.specialHeight, WATER_LEVEL - data.height);
        if (totalY > WATER_LEVEL - iceHeight && totalY <= WATER_LEVEL + (iceHeight >> 1)) {
            data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingIceType(totalX, totalY, totalZ));
            return true;
        }
        if (totalY > data.height) return false;

        int sandHeight = (int) (data.feature * 4.0) + WATER_LEVEL - 5;
        int floorBlockDepth = 3 - (data.steepness >> 1) + (int) (data.feature * 4.0);

        if (totalY < data.height - floorBlockDepth) return false;   // Stone placed by caller
        if (totalY > sandHeight) data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
        else data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, getColdOceanFloorBlock(totalX, totalY, totalZ));
        return true;
    }

    @Override
    public void genSurroundingStructures(int inChunkX, int inChunkY, int inChunkZ, GenerationData data) {

    }

    @Override
    public int getSpecialHeight(int totalX, int totalZ, GenerationData data) {
        double iceBergNoise = OpenSimplex2S.noise3_ImproveXY(SEED ^ 0xF90C1662F77EE4DFL, totalX * ICE_BERG_FREQUENCY, totalZ * ICE_BERG_FREQUENCY, 0.0);
        double icePlainNoise = OpenSimplex2S.noise3_ImproveXY(SEED ^ 0x649C844EA835C9A7L, totalX * ICE_BERG_FREQUENCY, totalZ * ICE_BERG_FREQUENCY, 0.0);
        if (iceBergNoise > ICE_BERG_THRESHOLD + 0.1) return (int) (ICE_BERG_HEIGHT + (icePlainNoise * 4.0));
        if (iceBergNoise > ICE_BERG_THRESHOLD)
            return (int) (Utils.smoothInOutQuad(iceBergNoise, ICE_BERG_THRESHOLD, ICE_BERG_THRESHOLD + 0.1) * ICE_BERG_HEIGHT + (icePlainNoise * 4.0));
        if (icePlainNoise > ICE_PLANE_THRESHOLD) return 1;
        return data.feature > 0.98 ? 1 : 0;
    }
}
