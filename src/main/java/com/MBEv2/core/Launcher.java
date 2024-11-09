package com.MBEv2.core;

import com.MBEv2.dataStorage.FileManager;

import static com.MBEv2.utils.Constants.*;

public class Launcher {

    private static WindowManager window;
    private static SoundManager sound;

    public static void main(String[] args) {
        window = new WindowManager(TITLE, 0, 0, true, true);
        sound = new SoundManager();
        EngineManager engine = new EngineManager();

        try {
            engine.init();

            engine.run();

            engine.cleanUp();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getClass());
            System.out.println(e.getMessage());

            FileManager.saveAllModifiedChunks();
            FileManager.saveGameState();
        }
    }

    public static WindowManager getWindow() {
        return window;
    }

    public static SoundManager getSound() {
        return sound;
    }
}
