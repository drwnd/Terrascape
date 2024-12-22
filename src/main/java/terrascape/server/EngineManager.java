package terrascape.server;

import terrascape.dataStorage.FileManager;
import terrascape.dataStorage.Structure;
import terrascape.entity.entities.Entity;
import terrascape.entity.particles.Particle;
import terrascape.generation.WorldGeneration;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import terrascape.player.SoundManager;
import terrascape.player.WindowManager;

import static terrascape.utils.Constants.*;

public class EngineManager {

    public static int currentFrameRate;

    private static WindowManager window;
    private static SoundManager sound;
    private static GLFWErrorCallback errorCallback;
    private static long tick = 0;

    public static void init() throws Exception {
        GLFW.glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        WorldGeneration.init();
        FileManager.init();
        FileManager.loadSettings(true);
        Structure.init();
        window = Launcher.getWindow();
        window.init();
        sound = Launcher.getSound();
        sound.init();
        Block.init();
        Entity.initAll();
        Particle.initAll();
        GameLogic.init();
    }

    public static void run() {
        long lastTime = 0;
        long lastFrameRateUpdateTime = 0;
        long lastGTTime = 0;
        int frames = 0;

        while (!window.windowShouldClose()) {
            long currentTime = System.nanoTime();
            long passedTime = currentTime - lastTime;
            lastTime = currentTime;

            update(20 * passedTime / NANOSECONDS_PER_SECOND);
            render(20 * (currentTime - lastGTTime) / NANOSECONDS_PER_SECOND);
            frames++;

            if (currentTime - lastFrameRateUpdateTime > NANOSECONDS_PER_SECOND * 0.25f) {
                lastFrameRateUpdateTime = currentTime;
                currentFrameRate = frames * 4;
                frames = 0;
            }
            if (currentTime - lastGTTime > NANOSECONDS_PER_SECOND * 0.05f) {
                lastGTTime = currentTime;
                input();
                updateGT();
            }
        }
    }

    public static void input() {
        GameLogic.input();
    }

    private static void render(float passedTicks) {
        GameLogic.render(passedTicks);
        window.update();
    }

    private static void update(float passedTicks) {
        GameLogic.update(passedTicks);
    }

    private static void updateGT() {
        GameLogic.updateGT(tick);
        tick++;
    }

    public static long getTick() {
        return tick;
    }

    public static void cleanUp() {
        GameLogic.cleanUp();
        window.cleanUp();
        sound.cleanUp();
        errorCallback.free();
        GLFW.glfwTerminate();
    }
}
