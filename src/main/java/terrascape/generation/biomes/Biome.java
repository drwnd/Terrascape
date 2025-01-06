package terrascape.generation.biomes;

import terrascape.generation.GenerationData;

public abstract class Biome {

    public abstract boolean placeBlock(int inChunkX, int inChunkY, int inChunkZ, GenerationData data);

    public abstract void genSurroundingStructures(int inChunkX, int inChunkY, int inChunkZ, GenerationData data);

    public int getSpecialHeight(int totalX, int totalZ, GenerationData data) {
        return 0;
    }
}