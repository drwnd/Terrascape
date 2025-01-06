package terrascape.generation.biomes;

import terrascape.generation.GenerationData;
import terrascape.utils.Utils;

import static terrascape.generation.WorldGeneration.*;
import static terrascape.utils.Constants.CHUNK_SIZE_BITS;
import static terrascape.utils.Constants.SNOW;

public class SnowyMountain extends Biome {
    @Override
    public boolean placeBlock(int inChunkX, int inChunkY, int inChunkZ, GenerationData data) {
        int totalX = data.chunk.X << CHUNK_SIZE_BITS | inChunkX;
        int totalY = data.chunk.Y << CHUNK_SIZE_BITS | inChunkY;
        int totalZ = data.chunk.Z << CHUNK_SIZE_BITS | inChunkZ;

        if (totalY > data.height) return false;

        int iceHeight = Utils.floor(data.feature * 32 + ICE_LEVEL);
        int floorBlockDepth = 3 - (data.steepness >> 1) + (int) (data.feature * 4.0);

        if (totalY < data.height - floorBlockDepth) return false;   // Stone placed by caller
        if (totalY > iceHeight)
            data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingIceType(totalX, totalY, totalZ));
        else data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, SNOW);
        return true;
    }

    @Override
    public void genSurroundingStructures(int inChunkX, int inChunkY, int inChunkZ, GenerationData data) {

    }
}
