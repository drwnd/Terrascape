package terrascape.entity;

import org.joml.Math;
import org.joml.Vector3i;
import org.lwjgl.PointerBuffer;

import static terrascape.utils.Constants.*;

public final class OpaqueModel {

    public static final int FACE_TYPE_COUNT = 14;
    public static final int FOLIAGE_FACES_OFFSET = 6;
    public static final int DECORATION_FACES_INDEX = 12;
    public static final int ASKEW_FACES_INDEX = 13;

    public final int vao, vbo;
    public final int X, Y, Z;

    public OpaqueModel(int vao, int[] vertexCounts, Vector3i position, int vbo) {
        this.vao = vao;
        this.vertexCounts = vertexCounts;
        X = position.x;
        Y = position.y;
        Z = position.z;
        this.vbo = vbo;
        toRenderVertexCounts = new int[FACE_TYPE_COUNT];
        indices = PointerBuffer.allocateDirect(vertexCounts.length);
        int index = 0;
        for (int vertexCount : vertexCounts) {
            indices.put(index * 4L);
            index += vertexCount;
        }
        indices.flip();
    }

    public int[] getLowDetailVertexCounts(int playerChunkX, int playerChunkY, int playerChunkZ) {
        int modelChunkX = X >> CHUNK_SIZE_BITS;
        int modelChunkY = Y >> CHUNK_SIZE_BITS;
        int modelChunkZ = Z >> CHUNK_SIZE_BITS;

        if (playerChunkX >= modelChunkX) {
            toRenderVertexCounts[WEST] = vertexCounts[WEST];
            toRenderVertexCounts[WEST + FOLIAGE_FACES_OFFSET] = vertexCounts[WEST + FOLIAGE_FACES_OFFSET];
        } else {
            toRenderVertexCounts[WEST] = 0;
            toRenderVertexCounts[WEST + FOLIAGE_FACES_OFFSET] = 0;
        }
        if (playerChunkX <= modelChunkX) {
            toRenderVertexCounts[EAST] = vertexCounts[EAST];
            toRenderVertexCounts[EAST + FOLIAGE_FACES_OFFSET] = vertexCounts[EAST + FOLIAGE_FACES_OFFSET];
        } else {
            toRenderVertexCounts[EAST] = 0;
            toRenderVertexCounts[EAST + FOLIAGE_FACES_OFFSET] = 0;
        }
        if (playerChunkY >= modelChunkY) {
            toRenderVertexCounts[TOP] = vertexCounts[TOP];
            toRenderVertexCounts[TOP + FOLIAGE_FACES_OFFSET] = vertexCounts[TOP + FOLIAGE_FACES_OFFSET];
        } else {
            toRenderVertexCounts[TOP] = 0;
            toRenderVertexCounts[TOP + FOLIAGE_FACES_OFFSET] = 0;
        }
        if (playerChunkY <= modelChunkY) {
            toRenderVertexCounts[BOTTOM] = vertexCounts[BOTTOM];
            toRenderVertexCounts[BOTTOM + FOLIAGE_FACES_OFFSET] = vertexCounts[BOTTOM + FOLIAGE_FACES_OFFSET];
        } else {
            toRenderVertexCounts[BOTTOM] = 0;
            toRenderVertexCounts[BOTTOM + FOLIAGE_FACES_OFFSET] = 0;
        }
        if (playerChunkZ >= modelChunkZ) {
            toRenderVertexCounts[NORTH] = vertexCounts[NORTH];
            toRenderVertexCounts[NORTH + FOLIAGE_FACES_OFFSET] = vertexCounts[NORTH + FOLIAGE_FACES_OFFSET];
        } else {
            toRenderVertexCounts[NORTH] = 0;
            toRenderVertexCounts[NORTH + FOLIAGE_FACES_OFFSET] = 0;
        }
        if (playerChunkZ <= modelChunkZ) {
            toRenderVertexCounts[SOUTH] = vertexCounts[SOUTH];
            toRenderVertexCounts[SOUTH + FOLIAGE_FACES_OFFSET] = vertexCounts[SOUTH + FOLIAGE_FACES_OFFSET];
        } else {
            toRenderVertexCounts[SOUTH] = 0;
            toRenderVertexCounts[SOUTH + FOLIAGE_FACES_OFFSET] = 0;
        }
        toRenderVertexCounts[DECORATION_FACES_INDEX] = 0;
        toRenderVertexCounts[ASKEW_FACES_INDEX] = vertexCounts[ASKEW_FACES_INDEX];

        return toRenderVertexCounts;
    }

    public int[] getSolidOnlyVertexCounts(int playerChunkX, int playerChunkY, int playerChunkZ) {
        int modelChunkX = X >> CHUNK_SIZE_BITS;
        int modelChunkY = Y >> CHUNK_SIZE_BITS;
        int modelChunkZ = Z >> CHUNK_SIZE_BITS;

        toRenderVertexCounts[WEST] = playerChunkX >= modelChunkX ? vertexCounts[WEST] : 0;
        toRenderVertexCounts[EAST] = playerChunkX <= modelChunkX ? vertexCounts[EAST] : 0;
        toRenderVertexCounts[TOP] = playerChunkY >= modelChunkY ? vertexCounts[TOP] : 0;
        toRenderVertexCounts[BOTTOM] = playerChunkY <= modelChunkY ? vertexCounts[BOTTOM] : 0;
        toRenderVertexCounts[NORTH] = playerChunkZ >= modelChunkZ ? vertexCounts[NORTH] : 0;
        toRenderVertexCounts[SOUTH] = playerChunkZ <= modelChunkZ ? vertexCounts[SOUTH] : 0;
        toRenderVertexCounts[ASKEW_FACES_INDEX] = vertexCounts[ASKEW_FACES_INDEX];

        for (int index = FOLIAGE_FACES_OFFSET; index <= DECORATION_FACES_INDEX; index++)
            toRenderVertexCounts[index] = 0;
        return toRenderVertexCounts;
    }

    public int[] getFoliageOnlyVertexCounts() {
        for (int index = 0; index < FOLIAGE_FACES_OFFSET; index++) toRenderVertexCounts[index] = 0;
        toRenderVertexCounts[ASKEW_FACES_INDEX] = 0;
        System.arraycopy(vertexCounts, FOLIAGE_FACES_OFFSET, toRenderVertexCounts, FOLIAGE_FACES_OFFSET, 7);
        return toRenderVertexCounts;
    }

    public int getDistanceFromPlayer(int playerChunkX, int playerChunkY, int playerChunkZ) {
        int modelChunkX = X >> CHUNK_SIZE_BITS;
        int modelChunkY = Y >> CHUNK_SIZE_BITS;
        int modelChunkZ = Z >> CHUNK_SIZE_BITS;

        int distanceX = Math.abs(modelChunkX - playerChunkX);
        int distanceY = Math.abs(modelChunkY - playerChunkY);
        int distanceZ = Math.abs(modelChunkZ - playerChunkZ);

        return Math.max(Math.max(distanceX, distanceY), distanceZ);
    }

    public int getFoliageVertexCount() {
        int vertexCount = 0;
        for (int index = FOLIAGE_FACES_OFFSET; index < vertexCounts.length; index++)
            vertexCount += vertexCounts[index];
        return vertexCount;
    }

    public int getSolidVertexCount(int[] vertexCounts) {
        int vertexCount = 0;
        for (int index = 0; index < FOLIAGE_FACES_OFFSET; index++)
            vertexCount += vertexCounts[index];
        return vertexCount;
    }

    public PointerBuffer getIndices() {
        return indices;
    }

    private final int[] vertexCounts, toRenderVertexCounts;
    private final PointerBuffer indices;
}
