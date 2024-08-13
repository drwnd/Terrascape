package com.MBEv2.test;

import com.MBEv2.core.*;
import com.MBEv2.core.entity.*;
import org.joml.Vector3i;
import org.joml.Vector4i;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;


import static com.MBEv2.core.utils.Constants.*;

public class GameLogic {

    private static final LinkedList<Chunk> toBufferChunks = new LinkedList<>();
    private static final LinkedList<Chunk> toUnloadChunks = new LinkedList<>();
    private static ChunkGenerator generator;

    private static Player player;

    public static void init() throws Exception {

        player = new Player();

        generator = new ChunkGenerator();
        player.init();

        player.getRenderer().init();
        generator.start();
    }

    public static void loadUnloadChunks(int direction) {
        generator.restart(direction);
    }

    public static void placeBlock(short block, Vector3i position) {
        if (position == null) return;
        if (Chunk.getBlockInWorld(position.x, position.y, position.z) == block) return;

        int chunkX = position.x >> CHUNK_SIZE_BITS;
        int chunkY = position.y >> CHUNK_SIZE_BITS;
        int chunkZ = position.z >> CHUNK_SIZE_BITS;

        int inChunkX = position.x & CHUNK_SIZE - 1;
        int inChunkY = position.y & CHUNK_SIZE - 1;
        int inChunkZ = position.z & CHUNK_SIZE - 1;

        Chunk chunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
        short previousBlock = chunk.getSaveBlock(inChunkX, inChunkY, inChunkZ);

        chunk.placeBlock(inChunkX, inChunkY, inChunkZ, block);
        chunk.setModified();
        if (Block.getBlockType(block) == FULL_BLOCK || Block.getBlockType(previousBlock) == FULL_BLOCK)
            chunk.setOcclusionCullingDataOutdated();

        int minX = chunkX, maxX = chunkX;
        int minY = chunkY, maxY = chunkY;
        int minZ = chunkZ, maxZ = chunkZ;

        if ((Block.getBlockProperties(block) & LIGHT_EMITTING_MASK) != 0 || (Block.getBlockProperties(previousBlock) & LIGHT_EMITTING_MASK) != 0) {
            if (inChunkX <= 15) minX = chunkX - 1;
            if (inChunkX >= CHUNK_SIZE - 16) maxX = chunkX + 1;
            if (inChunkY <= 15) minY = chunkY - 1;
            if (inChunkY >= CHUNK_SIZE - 16) maxY = chunkY + 1;
            if (inChunkZ <= 15) minZ = chunkZ - 1;
            if (inChunkZ >= CHUNK_SIZE - 16) maxZ = chunkZ + 1;
        } else {
            if (inChunkX == 0) minX = chunkX - 1;
            else if (inChunkX == CHUNK_SIZE - 1) maxX = chunkX + 1;
            if (inChunkY == 0) minY = chunkY - 1;
            else if (inChunkY == CHUNK_SIZE - 1) maxY = chunkY + 1;
            if (inChunkZ == 0) minZ = chunkZ - 1;
            else if (inChunkZ == CHUNK_SIZE - 1) maxZ = chunkZ + 1;
        }

        generator.addBlockChange(new Vector4i(position.x, position.y, position.z, previousBlock));

        for (int x = minX; x <= maxX; x++)
            for (int y = minY; y <= maxY; y++)
                for (int z = minZ; z <= maxZ; z++) {
                    Chunk toMeshChunk = Chunk.getChunk(x, y, z);
                    if (toMeshChunk == null) continue;
                    toMeshChunk.setMeshed(false);
                }
        generator.restart(NONE);
    }

    public static void bufferChunkMesh(Chunk chunk) {
        for (int side = 0; side < 6; side++) {
            if (chunk.getVertices(side) != null && chunk.getVertices(side).length != 0) {
                Model model = ObjectLoader.loadModel(chunk.getVertices(side), chunk.getWorldCoordinate());
                chunk.setModel(model, side);
            } else chunk.setModel(null, side);
        }
        if (chunk.getTransparentVertices() != null && chunk.getTransparentVertices().length != 0) {
            Model transparentModel = ObjectLoader.loadModel(chunk.getTransparentVertices(), chunk.getWorldCoordinate());
            chunk.setTransparentModel(transparentModel);
        } else chunk.setTransparentModel(null);

        chunk.clearMesh();
    }

    public static void update(float passedTime) {
        synchronized (toBufferChunks) {
            for (int i = 0; i < MAX_CHUNKS_TO_BUFFER_PER_FRAME && !toBufferChunks.isEmpty(); i++) {
                Chunk chunk = toBufferChunks.removeFirst();
                deleteChunkMeshBuffers(chunk);
                bufferChunkMesh(chunk);
            }
        }

        synchronized (toUnloadChunks) {
            while (!toUnloadChunks.isEmpty()) {
                Chunk chunk = toUnloadChunks.removeFirst();
                if (chunk != null) deleteChunkMeshBuffers(chunk);
            }
        }
        player.update(passedTime);
    }

    public static void updateGT() {
        player.getRenderer().incrementTime();
    }

    public static void deleteChunkMeshBuffers(Chunk chunk) {
        for (int side = 0; side < 6; side++) {
            Model sideModel = chunk.getModel(side);
            if (sideModel != null) {
                ObjectLoader.removeVAO(sideModel.getVao());
                ObjectLoader.removeVBO(sideModel.getVbo());
                chunk.setModel(null, side);
            }
        }
        Model transparentModel = chunk.getTransparentModel();
        if (transparentModel != null) {
            ObjectLoader.removeVAO(transparentModel.getVao());
            ObjectLoader.removeVBO(transparentModel.getVbo());
            chunk.setTransparentModel(null);
        }

    }

    public static void input() {
        player.input();
    }

    public static void render() {
        WindowManager window = Launcher.getWindow();

        if (window.isResize()) {
            GL11.glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResize(true);
        }
        player.render();
        player.getRenderer().render(player.getCamera());
    }

    public static void addToBufferChunk(Chunk chunk) {
        synchronized (toBufferChunks) {
            if (!toBufferChunks.contains(chunk)) toBufferChunks.add(chunk);
        }
    }

    public static void addToUnloadChunk(Chunk chunk) {
        synchronized (toUnloadChunks) {
            toUnloadChunks.add(chunk);
        }
    }

    public static float[] getCrossHairVertices() {
        WindowManager window = Launcher.getWindow();

        int width = window.getWidth();
        int height = window.getHeight();
        float size = 16;

        return new float[]{-size * GUI_SIZE / width, size * GUI_SIZE / height,
                -size * GUI_SIZE / width, -size * GUI_SIZE / height,
                size * GUI_SIZE / width, size * GUI_SIZE / height,

                -size * GUI_SIZE / width, -size * GUI_SIZE / height,
                size * GUI_SIZE / width, -size * GUI_SIZE / height,
                size * GUI_SIZE / width, size * GUI_SIZE / height};
    }

    public static float[] getBlockDisplayVertices(short block) {
        if (block == AIR) return new float[]{};
        WindowManager window = Launcher.getWindow();

        final int width = window.getWidth();
        final int height = window.getHeight();

        final float sin30 = (float) Math.sin(Math.toRadians(30)) * GUI_SIZE;
        final float cos30 = (float) Math.cos(Math.toRadians(30)) * GUI_SIZE;

        byte[] XYZSubData = Block.getXYZSubData(block);

        float widthX = XYZSubData[MAX_X] - XYZSubData[MIN_X] + 16;
        float widthY = (XYZSubData[MAX_Y] - XYZSubData[MIN_Y] + 16) * GUI_SIZE;
        float widthZ = XYZSubData[MAX_Z] - XYZSubData[MIN_Z] + 16;

        float rightCornersX = cos30 * widthX / width;
        float leftCornersX = -cos30 * widthZ / width;
        float backCornerX = leftCornersX + cos30 * widthX / width;

        float bottomRightCornerY = sin30 * widthX / height;
        float topRightCornerY = widthY / height + sin30 * widthX / height;
        float bottomLeftCornerY = sin30 * widthZ / height;
        float topLeftCornerY = widthY / height + sin30 * widthZ / height;
        float backCornerY = widthY / height + sin30 * widthZ / height + sin30 * widthX / height;
        float centerCornerY = widthY / height;
        return new float[]{
                0, 0,
                0, centerCornerY,
                rightCornersX, bottomRightCornerY,
                0, centerCornerY,
                rightCornersX, topRightCornerY,
                rightCornersX, bottomRightCornerY,

                leftCornersX, topLeftCornerY,
                backCornerX, backCornerY,
                0, centerCornerY,
                backCornerX, backCornerY,
                rightCornersX, topRightCornerY,
                0, centerCornerY,

                0, 0,
                0, centerCornerY,
                leftCornersX, bottomLeftCornerY,
                0, centerCornerY,
                leftCornersX, topLeftCornerY,
                leftCornersX, bottomLeftCornerY
        };
    }

    public static float[] getBlockDisplayTextureCoordinates(int textureIndexFront, int textureIndexTop, int textureIndexRight, short block) {
        if (block == AIR) return new float[]{};

        final int textureFrontX = textureIndexFront & 15;
        final int textureFrontY = (textureIndexFront >> 4) & 15;
        final float upperFrontX = (textureFrontX + Block.getSubU(block, FRONT, 0) * 0.0625f) * 0.0625f;
        final float lowerFrontX = (textureFrontX + 1 + Block.getSubU(block, FRONT, 1) * 0.0625f) * 0.0625f;
        final float upperFrontY = (textureFrontY + Block.getSubV(block, FRONT, 1) * 0.0625f) * 0.0625f;
        final float lowerFrontY = (textureFrontY + 1 + Block.getSubV(block, FRONT, 2) * 0.0625f) * 0.0625f;

        final int textureTopX = textureIndexTop & 15;
        final int textureTopY = (textureIndexTop >> 4) & 15;
        final float upperTopX = (textureTopX + Block.getSubU(block, TOP, 0) * 0.0625f) * 0.0625f;
        final float lowerTopX = (textureTopX + 1 + Block.getSubU(block, TOP, 1) * 0.0625f) * 0.0625f;
        final float upperTopY = (textureTopY + Block.getSubV(block, TOP, 1) * 0.0625f) * 0.0625f;
        final float lowerTopY = (textureTopY + 1 + Block.getSubV(block, TOP, 2) * 0.0625f) * 0.0625f;

        final int textureRightX = textureIndexRight & 15;
        final int textureRightY = (textureIndexRight >> 4) & 15;
        final float upperRightX = (textureRightX + Block.getSubU(block, RIGHT, 0) * 0.0625f) * 0.0625f;
        final float lowerRightX = (textureRightX + 1 + Block.getSubU(block, RIGHT, 1) * 0.0625f) * 0.0625f;
        final float upperRightY = (textureRightY + Block.getSubV(block, RIGHT, 1) * 0.0625f) * 0.0625f;
        final float lowerRightY = (textureRightY + 1 + Block.getSubV(block, RIGHT, 2) * 0.0625f) * 0.0625f;

        return new float[]{lowerFrontX, lowerFrontY, lowerFrontX, upperFrontY, upperFrontX, lowerFrontY,

                lowerFrontX, upperFrontY, upperFrontX, upperFrontY, upperFrontX, lowerFrontY,

                lowerTopX, lowerTopY, lowerTopX, upperTopY, upperTopX, lowerTopY,

                lowerTopX, upperTopY, upperTopX, upperTopY, upperTopX, lowerTopY,

                lowerRightX, lowerRightY, lowerRightX, upperRightY, upperRightX, lowerRightY,

                lowerRightX, upperRightY, upperRightX, upperRightY, upperRightX, lowerRightY};
    }

    public static float[] getHotBarVertices() {
        WindowManager window = Launcher.getWindow();

        int width = window.getWidth();
        int height = window.getHeight();
        float sizeX = 180;
        float sizeY = 40;

        return new float[]{
                -sizeX * GUI_SIZE / width, -0.5f,
                -sizeX * GUI_SIZE / width, sizeY * GUI_SIZE / height - 0.5f,
                sizeX * GUI_SIZE / width, -0.5f,
                -sizeX * GUI_SIZE / width, sizeY * GUI_SIZE / height - 0.5f,
                sizeX * GUI_SIZE / width, sizeY * GUI_SIZE / height - 0.5f,
                sizeX * GUI_SIZE / width, -0.5f};
    }

    public static long getChunkId(int x, int y, int z) {
        return (long) (x & MAX_CHUNKS_XZ) << 37 | (long) (y & MAX_CHUNKS_Y) << 27 | (z & MAX_CHUNKS_XZ);
    }

    public static int getChunkIndex(int x, int y, int z) {

        x = (x % RENDERED_WORLD_WIDTH);
        if (x < 0) x += RENDERED_WORLD_WIDTH;

        y = (y % RENDERED_WORLD_HEIGHT);
        if (y < 0) y += RENDERED_WORLD_HEIGHT;

        z = (z % RENDERED_WORLD_WIDTH);
        if (z < 0) z += RENDERED_WORLD_WIDTH;

        return (x * RENDERED_WORLD_HEIGHT + y) * RENDERED_WORLD_WIDTH + z;
    }

    public static Player getPlayer() {
        return player;
    }

    public static void cleanUp() {
        player.getRenderer().cleanUp();
        ObjectLoader.cleanUp();
        generator.cleanUp();
    }
}
