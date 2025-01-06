package terrascape.generation.biomes;

import terrascape.generation.GenerationData;
import terrascape.utils.Utils;

import static terrascape.generation.WorldGeneration.*;
import static terrascape.utils.Constants.CHUNK_SIZE_BITS;

public class DryMountain extends Biome {
    @Override
    public boolean placeBlock(int inChunkX, int inChunkY, int inChunkZ, GenerationData data) {
        int totalX = data.chunk.X << CHUNK_SIZE_BITS | inChunkX;
        int totalY = data.chunk.Y << CHUNK_SIZE_BITS | inChunkY;
        int totalZ = data.chunk.Z << CHUNK_SIZE_BITS | inChunkZ;

        if (totalY > data.height) return false;

        int dirtHeight = Utils.floor(data.feature * 32 + WATER_LEVEL);
        int floorBlockDepth = 3 - (data.steepness >> 1) + (int) (data.feature * 4.0);

        if (totalY > data.height - floorBlockDepth && data.height <= dirtHeight)
            data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingDirtType(totalX, totalY, totalZ));
        else return false;
        return true;
    }

    @Override
    public void genSurroundingStructures(int inChunkX, int inChunkY, int inChunkZ, GenerationData data) {

    }
}
