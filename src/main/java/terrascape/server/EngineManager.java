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

public final class EngineManager {

    public static int currentFrameRate;

    private static WindowManager window;

    private static SoundManager sound;
    private static Server server;
    private static GLFWErrorCallback errorCallback;
    private static long tick = 0, lastGTTime;
    private static boolean entityDataUpdateIsScheduled = true;

    public static void init() throws Exception {
        GLFW.glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        WorldGeneration.init();
        FileManager.init();
        FileManager.loadSettings(true);
        FileManager.loadNames();
        Structure.init();
        window = Launcher.getWindow();
        window.init();
        sound = Launcher.getSound();
        sound.init();
        server = Launcher.getServer();
        Block.init();
        Entity.initAll();
        Particle.initAll();
        ServerLogic.init();
        FileManager.loadGameState();
    }

    public static void run() {
        server.start();

        long lastTime = System.nanoTime();
        long lastFrameRateUpdateTime = 0;
        long lastInputTime = 0;
        int frames = 0;

        while (!window.windowShouldClose()) {
            long currentTime = System.nanoTime();
            long passedTime = currentTime - lastTime;
            lastTime = currentTime;

            update(TARGET_TPS * passedTime / NANOSECONDS_PER_SECOND);
            render(Math.min(1.0f, TARGET_TPS * (currentTime - lastGTTime) / NANOSECONDS_PER_SECOND));
            frames++;

            if (currentTime - lastFrameRateUpdateTime > NANOSECONDS_PER_SECOND * 0.25f) {
                lastFrameRateUpdateTime = currentTime;
                currentFrameRate = frames * 4;
                frames = 0;
            }
            if (currentTime - lastInputTime > NANOSECONDS_PER_SECOND * (1.0f / TARGET_TPS)) {
                lastInputTime = currentTime;
                input();
            }
        }
    }

    public static void input() {
        ServerLogic.input();
    }

    private static void render(float passedTicks) {
        if (entityDataUpdateIsScheduled) {
            ServerLogic.getPlayer().updateEntityData();
            entityDataUpdateIsScheduled = false;
        }
        ServerLogic.render(passedTicks);
        long gpuTime = System.nanoTime();
        window.update();
        if (ServerLogic.getPlayer().printTimes) {
            System.out.println("gpu " + (System.nanoTime() - gpuTime));
            System.out.println("-----------------");
        }
    }

    private static void update(float passedTicks) {
        ServerLogic.update(passedTicks);
    }

    public static long getTick() {
        return tick;
    }

    public static void incTick() {
        tick++;
    }

    public static void setLastGTTime(long lastGTTime) {
        EngineManager.lastGTTime = lastGTTime;
        entityDataUpdateIsScheduled = true;
    }

    public static void cleanUp() {
        server.stop();
        ServerLogic.cleanUp();
        window.cleanUp();
        sound.cleanUp();
        errorCallback.free();
        GLFW.glfwTerminate();
    }

    public static void setTick(long tick) {
        EngineManager.tick = tick;
    }

    private EngineManager() {
    }
}
