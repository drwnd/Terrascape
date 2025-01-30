package terrascape.server;

import terrascape.dataStorage.FileManager;
import terrascape.player.SoundManager;
import terrascape.player.WindowManager;

import static terrascape.utils.Constants.*;

public final class Launcher {

    private static WindowManager window;
    private static SoundManager sound;
    private static Server server;

    public static void main(String[] args) {
        window = new WindowManager(TITLE, 0, 0, true, true);
        sound = new SoundManager();
        server = new Server();

        try {
            EngineManager.init();

            EngineManager.run();

            EngineManager.cleanUp();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass());
            System.err.println(e.getMessage());

            FileManager.saveAllModifiedChunks();
            FileManager.savePlayer();
            FileManager.saveGameState();
        }
    }

    public static WindowManager getWindow() {
        return window;
    }

    public static SoundManager getSound() {
        return sound;
    }

    public static Server getServer() {
        return server;
    }

    private Launcher() { }
}
