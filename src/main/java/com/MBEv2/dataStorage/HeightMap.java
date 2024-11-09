package com.MBEv2.dataStorage;

public class HeightMap {

    public final int chunkX;
    public final int chunkZ;
    public final int[] map;
    private boolean modified;

    public HeightMap(int[] map, int chunkX, int chunkZ) {
        this.map = map;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public static void setNull(int index) {
        Chunk.setHeightMap(null, index);
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }
}
