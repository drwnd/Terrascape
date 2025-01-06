package terrascape.generation.biomes;

import terrascape.dataStorage.Structure;
import terrascape.generation.GenerationData;

import static terrascape.generation.WorldGeneration.*;
import static terrascape.utils.Constants.*;
import static terrascape.utils.Constants.DIRT;

public class RedwoodForest extends Biome {
    @Override
    public boolean placeBlock(int inChunkX, int inChunkY, int inChunkZ, GenerationData data) {
        int totalX = data.chunk.X << CHUNK_SIZE_BITS | inChunkX;
        int totalY = data.chunk.Y << CHUNK_SIZE_BITS | inChunkY;
        int totalZ = data.chunk.Z << CHUNK_SIZE_BITS | inChunkZ;

        if (totalY >= data.height) {
            boolean placedBlock;
            placedBlock = genTree(inChunkX, inChunkY, inChunkZ, REDWOOD_FOREST_TREE_THRESHOLD, Structure.REDWOOD_TREE, data);
            placedBlock |= genFeature(inChunkX, inChunkY, inChunkZ, TALL_GRASS_THRESHOLD, TALL_GRASS, data);
            placedBlock |= genFeature(inChunkX, inChunkY, inChunkZ, FLOWER_THRESHOLD, ROSE, data);
            if (placedBlock) return true;
        }
        if (totalY > data.height) return false;

        int floorBlockDepth = 3 - (data.steepness >> 1) + (int) (data.feature * 4.0);

        if (totalY < data.height - floorBlockDepth) return false;   // Stone placed by caller
        if (totalY == data.height)
            data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, getGeneratingGrassType(totalX, totalZ, data));
        else data.chunk.storeSave(inChunkX, inChunkY, inChunkZ, DIRT);
        return true;
    }

    @Override
    public void genSurroundingStructures(int inChunkX, int inChunkY, int inChunkZ, GenerationData data) {
        genSurroundingTree(inChunkX, inChunkY, inChunkZ, REDWOOD_FOREST_TREE_THRESHOLD, Structure.REDWOOD_TREE, data);
    }
}
