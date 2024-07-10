package com.MBEv2.test;

import com.MBEv2.core.*;
import com.MBEv2.core.entity.*;
import org.joml.Vector3i;
import org.joml.Vector4i;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;


import static com.MBEv2.core.utils.Constants.*;

public class GameLogic {

    private static Texture atlas;
    private static final LinkedList<Chunk> toBufferChunks = new LinkedList<>();
    private static final LinkedList<Chunk> toUnloadChunks = new LinkedList<>();
    private static ChunkGenerator generator;

    private static Player player;

    public static void init() throws Exception {

        generator = new ChunkGenerator();

        atlas = new Texture(ObjectLoader.loadTexture("textures/atlas256.png"));

        player = new Player(atlas);
        player.init();

        player.getRenderer().init();
        generator.continueRunning(NONE);
    }

    public static void loadUnloadChunks(int direction) {
        generator.continueRunning(direction);
    }

    public static void placeBlock(byte block, Vector3i position) {
        if (position == null)
            return;
        if (Chunk.getBlockInWorld(position.x, position.y, position.z) == block)
            return;

        int chunkX = position.x >> CHUNK_SIZE_BITS;
        int chunkY = position.y >> CHUNK_SIZE_BITS;
        int chunkZ = position.z >> CHUNK_SIZE_BITS;

        int inChunkX = position.x & CHUNK_SIZE - 1;
        int inChunkY = position.y & CHUNK_SIZE - 1;
        int inChunkZ = position.z & CHUNK_SIZE - 1;

        Chunk chunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
        byte previousBlock = chunk.getSaveBlock(inChunkX, inChunkY, inChunkZ);

        chunk.storeSave(inChunkX, inChunkY, inChunkZ, block);
        chunk.setModified();

        synchronized (generator.getChanges()){
            generator.addChange(new Vector4i(position.x, position.y, position.z, previousBlock));
        }

        int minX = chunkX, maxX = chunkX;
        int minY = chunkY, maxY = chunkY;
        int minZ = chunkZ, maxZ = chunkZ;

        if (inChunkX == 0)
            minX--;
        else if (inChunkX == CHUNK_SIZE - 1)
            maxX++;
        if (inChunkY == 0)
            minY--;
        else if (inChunkY == CHUNK_SIZE - 1)
            maxY++;
        if (inChunkZ == 0)
            minZ--;
        else if (inChunkZ == CHUNK_SIZE - 1)
            maxZ++;

        for (int x = minX; x <= maxX; x++)
            for (int y = minY; y <= maxY; y++)
                for (int z = minZ; z <= maxZ; z++) {
                    Chunk toMeshChunk = Chunk.getChunk(x, y, z);
                    if (toMeshChunk != null)
                        toMeshChunk.setMeshed(false);
                }
        generator.continueRunning(NONE);
    }

    public static void bufferChunkMesh(Chunk chunk) {
        if (chunk.getVertices() != null && chunk.getVertices().length != 0) {
            Model model = ObjectLoader.loadModel(chunk.getVertices(), chunk.getWorldCoordinate());
            model.setTexture(atlas);
            chunk.setModel(model);
        } else
            chunk.setModel(null);

        if (chunk.getTransparentVertices() != null && chunk.getTransparentVertices().length != 0) {
            Model transparentModel = ObjectLoader.loadModel(chunk.getTransparentVertices(), chunk.getWorldCoordinate());
            transparentModel.setTexture(atlas);
            chunk.setTransparentModel(transparentModel);
        } else
            chunk.setTransparentModel(null);

        chunk.clearMesh();
    }

    public static void update() {
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
                if (chunk != null)
                    deleteChunkMeshBuffers(chunk);
            }
        }
        player.update();
    }

    public static void deleteChunkMeshBuffers(Chunk chunk) {
        if (chunk.getModel() != null) {
            ObjectLoader.removeVAO(chunk.getModel().getVao());
            ObjectLoader.removeVBO(chunk.getModel().getVbo());
            chunk.setModel(null);
        }
        if (chunk.getTransparentModel() != null) {
            ObjectLoader.removeVAO(chunk.getTransparentModel().getVao());
            ObjectLoader.removeVBO(chunk.getTransparentModel().getVbo());
            chunk.setTransparentModel(null);
        }
    }

    public static void input(float passedTime) {
        player.input(passedTime);
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
            if (!toBufferChunks.contains(chunk))
                toBufferChunks.add(chunk);
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

        return new float[]{
                -size * GUI_SIZE / width, size * GUI_SIZE / height,
                -size * GUI_SIZE / width, -size * GUI_SIZE / height,
                size * GUI_SIZE / width, size * GUI_SIZE / height,

                -size * GUI_SIZE / width, -size * GUI_SIZE / height,
                size * GUI_SIZE / width, -size * GUI_SIZE / height,
                size * GUI_SIZE / width, size * GUI_SIZE / height
        };
    }

    public static float[] getHotBarElementVertices(int index, byte block) {
        if (block == AIR)
            return new float[]{};
        WindowManager window = Launcher.getWindow();

        final int width = window.getWidth();
        final int height = window.getHeight();

        final float xOffset = (40.0f * index - 165 + 4) * GUI_SIZE / width;
        final float yOffset = -0.5f + 4.0f * GUI_SIZE / height;

        final float sin30 = (float) Math.sin(Math.toRadians(30)) * GUI_SIZE;
        final float cos30 = (float) Math.cos(Math.toRadians(30)) * GUI_SIZE;

        byte[] XYZSubData = Block.getXYZSubData(block);

        float widthX = XYZSubData[MAX_X] - XYZSubData[MIN_X] + 16;
        float widthY = (XYZSubData[MAX_Y] - XYZSubData[MIN_Y] + 16) * GUI_SIZE;
        float widthZ = XYZSubData[MAX_Z] - XYZSubData[MIN_Z] + 16;

        //Ignorance is bliss, so be ignorant
        float value1 = yOffset + widthY / height + sin30 * widthX / height;
        float value2 = yOffset + widthY / height + sin30 * widthZ / height;
        float value7 = xOffset - cos30 * widthZ / width;
        float value3 = value7 + cos30 * widthX / width;
        float value4 = yOffset + widthY / height + sin30 * widthZ / height + sin30 * widthX / height;
        float value5 = xOffset + cos30 * widthX / width;
        float value6 = yOffset + sin30 * widthX / height;
        float value8 = yOffset + sin30 * widthZ / height;
        float value9 = yOffset + widthY / height;
        return new float[]{
                xOffset, yOffset,
                xOffset, value9,
                value5, value6,
                xOffset, value9,
                value5, value1,
                value5, value6,

                value7, value2,
                value3, value4,
                xOffset, value9,
                value3, value4,
                value5, value1,
                xOffset, value9,

                xOffset, yOffset,
                xOffset, value9,
                value7, value8,
                xOffset, value9,
                value7, value2,
                value7, value8,
        };
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

    public static void startChunkGenerator() {
        generator.start();
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
