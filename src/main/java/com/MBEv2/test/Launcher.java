package com.MBEv2.test;

import com.MBEv2.core.*;

import static com.MBEv2.core.utils.Constants.*;

public class Launcher {

    private static WindowManager window;
    private static GameLogic game;

    public static void main(String[] args) {
        EngineManager engine;
        window = new WindowManager(TITLE, 0, 0, true);
        engine = new EngineManager();
        game = new GameLogic();

        Block.init();
        generateWorld();

        try {
            engine.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateWorld() {

        for (int x = -RENDER_DISTANCE_XZ - 1; x <= RENDER_DISTANCE_XZ + 1; x++) {
            for (int z = -RENDER_DISTANCE_XZ - 1; z <= RENDER_DISTANCE_XZ + 1; z++) {

                double[][] heightMap = GameLogic.heightMap(x, z);
                int[][] stoneMap = GameLogic.stoneMap(x, z, heightMap);
                double[][] featureMap = GameLogic.featureMap(x, z);
                byte[][] treeMap = GameLogic.treeMap(x, z,heightMap, stoneMap, featureMap);

                for (int y = -1; y < RENDER_DISTANCE_Y * 2 + 1; y++) {
                    Chunk chunk = new Chunk(x, y, z);
                    chunk.generate(heightMap, stoneMap, featureMap, treeMap);
                    Chunk.storeChunk(chunk);
                }
            }
        }
    }

    public static WindowManager getWindow() {
        return window;
    }

    public static GameLogic getGame() {
        return game;
    }
}
