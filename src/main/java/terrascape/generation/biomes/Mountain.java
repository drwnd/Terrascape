package terrascape.generation.biomes;

import terrascape.generation.GenerationData;
import terrascape.utils.Utils;

import static terrascape.generation.WorldGeneration.*;
import static terrascape.utils.Constants.*;
import static terrascape.utils.Constants.DIRT;

public final class Mountain extends Biome {
    @Override
    public boolean placeBlock(int inChunkX, int inChunkY, int inChunkZ, GenerationData data) {
        int totalY = data.chunk.Y << CHUNK_SIZE_BITS | inChunkY;

        if (totalY > data.height) return false;

        int snowHeight = Utils.floor(data.feature * 32 + SNOW_LEVEL);
        int grassHeight = Utils.floor(data.feature * 32) + WATER_LEVEL;
        int floorBlockDepth = 3 - (data.steepness >> 1) + (int) (data.feature * 4.0);

        if (totalY > snowHeight && totalY > data.height - floorBlockDepth)
            data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, SNOW);
        else if (totalY == data.height && data.height <= grassHeight)
            data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, GRASS);
        else if (totalY < data.height && totalY > data.height - floorBlockDepth && data.height <= grassHeight)
            data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, DIRT);
        else return false;
        return true;
    }

    @Override
    public void genSurroundingStructures(int inChunkX, int inChunkY, int inChunkZ, GenerationData data) {

    }
}
