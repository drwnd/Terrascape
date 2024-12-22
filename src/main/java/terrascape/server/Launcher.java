package terrascape.server;

import terrascape.dataStorage.FileManager;
import terrascape.player.SoundManager;
import terrascape.player.WindowManager;

import static terrascape.utils.Constants.*;

public class Launcher {

    private static WindowManager window;
    private static SoundManager sound;

    public static void main(String[] args) {
        window = new WindowManager(TITLE, 0, 0, true, true);
        sound = new SoundManager();

        try {
            EngineManager.init();

            EngineManager.run();

            EngineManager.cleanUp();

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
