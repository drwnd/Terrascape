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
        if (chunk.getWaterVertices() != null && chunk.getWaterVertices().length != 0) {
            Model taterModel = ObjectLoader.loadModel(chunk.getWaterVertices(), chunk.getWorldCoordinate());
            chunk.setWaterModel(taterModel);
        } else chunk.setWaterModel(null);

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
        Model waterModel = chunk.getWaterModel();
        if (waterModel != null) {
            ObjectLoader.removeVAO(waterModel.getVao());
            ObjectLoader.removeVBO(waterModel.getVbo());
            chunk.setWaterModel(null);
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
        float[] vertices = new float[XYZSubData.length * 6];

        for (int aabbIndex = 0; aabbIndex < XYZSubData.length; aabbIndex += 6) {
            int offset = aabbIndex * 6;

            float widthX = XYZSubData[MAX_X + aabbIndex] - XYZSubData[MIN_X + aabbIndex] + 16;
            float widthY = (XYZSubData[MAX_Y + aabbIndex] - XYZSubData[MIN_Y + aabbIndex] + 16) * GUI_SIZE;
            float widthZ = XYZSubData[MAX_Z + aabbIndex] - XYZSubData[MIN_Z + aabbIndex] + 16;

            float startX = -XYZSubData[MAX_X + aabbIndex] * cos30 / width + XYZSubData[MAX_Z + aabbIndex] * cos30 / width;
            float startY = XYZSubData[MIN_Y + aabbIndex] * GUI_SIZE / height - XYZSubData[MAX_X + aabbIndex] * sin30 / height - XYZSubData[MAX_Z + aabbIndex] * sin30 / height;

            float rightCornersX = startX + cos30 * widthX / width;
            float leftCornersX = startX - cos30 * widthZ / width;
            float backCornerX = rightCornersX + leftCornersX - startX;

            float bottomRightCornerY = startY + sin30 * widthX / height;
            float topRightCornerY = startY + widthY / height + sin30 * widthX / height;
            float bottomLeftCornerY = startY + sin30 * widthZ / height;
            float topLeftCornerY = startY + widthY / height + sin30 * widthZ / height;
            float backCornerY = startY + widthY / height + sin30 * widthZ / height + sin30 * widthX / height;
            float centerCornerY = startY + widthY / height;

            vertices[offset] = startX;
            vertices[offset + 1] = startY;
            vertices[offset + 2] = startX;
            vertices[offset + 3] = centerCornerY;
            vertices[offset + 4] = rightCornersX;
            vertices[offset + 5] = bottomRightCornerY;
            vertices[offset + 6] = startX;
            vertices[offset + 7] = centerCornerY;
            vertices[offset + 8] = rightCornersX;
            vertices[offset + 9] = topRightCornerY;
            vertices[offset + 10] = rightCornersX;
            vertices[offset + 11] = bottomRightCornerY;

            vertices[offset + 12] = leftCornersX;
            vertices[offset + 13] = topLeftCornerY;
            vertices[offset + 14] = backCornerX;
            vertices[offset + 15] = backCornerY;
            vertices[offset + 16] = startX;
            vertices[offset + 17] = centerCornerY;
            vertices[offset + 18] = backCornerX;
            vertices[offset + 19] = backCornerY;
            vertices[offset + 20] = rightCornersX;
            vertices[offset + 21] = topRightCornerY;
            vertices[offset + 22] = startX;
            vertices[offset + 23] = centerCornerY;

            vertices[offset + 24] = startX;
            vertices[offset + 25] = startY;
            vertices[offset + 26] = startX;
            vertices[offset + 27] = centerCornerY;
            vertices[offset + 28] = leftCornersX;
            vertices[offset + 29] = bottomLeftCornerY;
            vertices[offset + 30] = startX;
            vertices[offset + 31] = centerCornerY;
            vertices[offset + 32] = leftCornersX;
            vertices[offset + 33] = topLeftCornerY;
            vertices[offset + 34] = leftCornersX;
            vertices[offset + 35] = bottomLeftCornerY;
        }
        return vertices;
    }

    public static float[] getBlockDisplayTextureCoordinates(int textureIndexFront, int textureIndexTop, int textureIndexRight, short block) {
        if (block == AIR) return new float[]{};
        int blockType = Block.getBlockType(block);
        byte[] XYZSubData = Block.getXYZSubData(block);
        float[] textureCoordinates = new float[XYZSubData.length * 6];

        final int textureFrontX = textureIndexFront & 15;
        final int textureFrontY = (textureIndexFront >> 4) & 15;
        final int textureTopX = textureIndexTop & 15;
        final int textureTopY = (textureIndexTop >> 4) & 15;
        final int textureRightX = textureIndexRight & 15;
        final int textureRightY = (textureIndexRight >> 4) & 15;

        for (int aabbIndex = 0; aabbIndex < XYZSubData.length; aabbIndex += 6) {
            int offset = aabbIndex * 6;
            int subDataIndex = aabbIndex / 6;

            final float upperFrontX = (textureFrontX + Block.getSubU(blockType, FRONT, 0, subDataIndex) * 0.0625f) * 0.0625f;
            final float lowerFrontX = (textureFrontX + 1 + Block.getSubU(blockType, FRONT, 1, subDataIndex) * 0.0625f) * 0.0625f;
            final float upperFrontY = (textureFrontY + Block.getSubV(blockType, FRONT, 1, subDataIndex) * 0.0625f) * 0.0625f;
            final float lowerFrontY = (textureFrontY + 1 + Block.getSubV(blockType, FRONT, 2, subDataIndex) * 0.0625f) * 0.0625f;
            textureCoordinates[offset] = lowerFrontX;
            textureCoordinates[offset + 1] = lowerFrontY;
            textureCoordinates[offset + 2] = lowerFrontX;
            textureCoordinates[offset + 3] = upperFrontY;
            textureCoordinates[offset + 4] = upperFrontX;
            textureCoordinates[offset + 5] = lowerFrontY;
            textureCoordinates[offset + 6] = lowerFrontX;
            textureCoordinates[offset + 7] = upperFrontY;
            textureCoordinates[offset + 8] = upperFrontX;
            textureCoordinates[offset + 9] = upperFrontY;
            textureCoordinates[offset + 10] = upperFrontX;
            textureCoordinates[offset + 11] = lowerFrontY;

            final float upperTopX = (textureTopX + Block.getSubU(blockType, TOP, 0, subDataIndex) * 0.0625f) * 0.0625f;
            final float lowerTopX = (textureTopX + 1 + Block.getSubU(blockType, TOP, 1, subDataIndex) * 0.0625f) * 0.0625f;
            final float upperTopY = (textureTopY + Block.getSubV(blockType, TOP, 1, subDataIndex) * 0.0625f) * 0.0625f;
            final float lowerTopY = (textureTopY + 1 + Block.getSubV(blockType, TOP, 2, subDataIndex) * 0.0625f) * 0.0625f;
            textureCoordinates[offset + 12] = lowerTopX;
            textureCoordinates[offset + 13] = lowerTopY;
            textureCoordinates[offset + 14] = lowerTopX;
            textureCoordinates[offset + 15] = upperTopY;
            textureCoordinates[offset + 16] = upperTopX;
            textureCoordinates[offset + 17] = lowerTopY;
            textureCoordinates[offset + 18] = lowerTopX;
            textureCoordinates[offset + 19] = upperTopY;
            textureCoordinates[offset + 20] = upperTopX;
            textureCoordinates[offset + 21] = upperTopY;
            textureCoordinates[offset + 22] = upperTopX;
            textureCoordinates[offset + 23] = lowerTopY;

            final float upperRightX = (textureRightX + Block.getSubU(blockType, RIGHT, 0, subDataIndex) * 0.0625f) * 0.0625f;
            final float lowerRightX = (textureRightX + 1 + Block.getSubU(blockType, RIGHT, 1, subDataIndex) * 0.0625f) * 0.0625f;
            final float upperRightY = (textureRightY + Block.getSubV(blockType, RIGHT, 1, subDataIndex) * 0.0625f) * 0.0625f;
            final float lowerRightY = (textureRightY + 1 + Block.getSubV(blockType, RIGHT, 2, subDataIndex) * 0.0625f) * 0.0625f;
            textureCoordinates[offset + 24] = lowerRightX;
            textureCoordinates[offset + 25] = lowerRightY;
            textureCoordinates[offset + 26] = lowerRightX;
            textureCoordinates[offset + 27] = upperRightY;
            textureCoordinates[offset + 28] = upperRightX;
            textureCoordinates[offset + 29] = lowerRightY;
            textureCoordinates[offset + 30] = lowerRightX;
            textureCoordinates[offset + 31] = upperRightY;
            textureCoordinates[offset + 32] = upperRightX;
            textureCoordinates[offset + 33] = upperRightY;
            textureCoordinates[offset + 34] = upperRightX;
            textureCoordinates[offset + 35] = lowerRightY;
        }
        return textureCoordinates;
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
                sizeX * GUI_SIZE / width, -0.5f
        };
    }

    public static float[] getHotBarSelectionIndicatorVertices() {
        WindowManager window = Launcher.getWindow();

        int width = window.getWidth();
        int height = window.getHeight();

        float sizeX = 24;
        float sizeY = 48;

        float yOffset = 4 * GUI_SIZE / height;

        return new float[]{
                -sizeX * GUI_SIZE / width, -0.5f - yOffset,
                -sizeX * GUI_SIZE / width, sizeY * GUI_SIZE / height - 0.5f - yOffset,
                sizeX * GUI_SIZE / width, -0.5f - yOffset,
                -sizeX * GUI_SIZE / width, sizeY * GUI_SIZE / height - 0.5f - yOffset,
                sizeX * GUI_SIZE / width, sizeY * GUI_SIZE / height - 0.5f - yOffset,
                sizeX * GUI_SIZE / width, -0.5f - yOffset
        };
    }

    public static long getChunkId(int chunkX, int chunkY, int chunkZ) {
        return (long) (chunkX & MAX_CHUNKS_XZ) << 37 | (long) (chunkY & MAX_CHUNKS_Y) << 27 | (chunkZ & MAX_CHUNKS_XZ);
    }

    public static int getChunkIndex(int chunkX, int chunkY, int chunkZ) {

        chunkX = (chunkX % RENDERED_WORLD_WIDTH);
        if (chunkX < 0) chunkX += RENDERED_WORLD_WIDTH;

        chunkY = (chunkY % RENDERED_WORLD_HEIGHT);
        if (chunkY < 0) chunkY += RENDERED_WORLD_HEIGHT;

        chunkZ = (chunkZ % RENDERED_WORLD_WIDTH);
        if (chunkZ < 0) chunkZ += RENDERED_WORLD_WIDTH;

        return (chunkX * RENDERED_WORLD_HEIGHT + chunkY) * RENDERED_WORLD_WIDTH + chunkZ;
    }

    public static int getHeightMapIndex(int chunkX, int chunkZ) {

        chunkX = (chunkX % RENDERED_WORLD_WIDTH);
        if (chunkX < 0) chunkX += RENDERED_WORLD_WIDTH;

        chunkZ = (chunkZ % RENDERED_WORLD_WIDTH);
        if (chunkZ < 0) chunkZ += RENDERED_WORLD_WIDTH;

        return chunkX * RENDERED_WORLD_WIDTH + chunkZ;
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
