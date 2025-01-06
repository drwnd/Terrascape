package terrascape.generation.biomes;

import terrascape.generation.GenerationData;

import static terrascape.generation.WorldGeneration.*;
import static terrascape.utils.Constants.CHUNK_SIZE_BITS;
import static terrascape.utils.Constants.SAND;

public class Ocean extends Biome {
    @Override
    public boolean placeBlock(int inChunkX, int inChunkY, int inChunkZ, GenerationData data) {
        int totalX = data.chunk.X << CHUNK_SIZE_BITS | inChunkX;
        int totalY = data.chunk.Y << CHUNK_SIZE_BITS | inChunkY;
        int totalZ = data.chunk.Z << CHUNK_SIZE_BITS | inChunkZ;

        if (totalY > data.height) return false;

        int sandHeight = (int) (data.feature * 4.0) + WATER_LEVEL - 5;
        int floorBlockDepth = 3 - (data.steepness >> 1) + (int) (data.feature * 4.0);

        if (totalY < data.height - floorBlockDepth) return false;   // Stone placed by caller
        if (totalY > sandHeight) data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, SAND);
        else data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, getOceanFloorBlock(totalX, totalY, totalZ));
        return true;
    }

    @Override
    public void genSurroundingStructures(int inChunkX, int inChunkY, int inChunkZ, GenerationData data) {

    }
}
