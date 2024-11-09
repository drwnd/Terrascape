package com.MBEv2.core;

import com.MBEv2.dataStorage.FileManager;
import com.MBEv2.dataStorage.Structure;
import com.MBEv2.entity.entities.Entity;
import com.MBEv2.entity.particles.Particle;
import com.MBEv2.generation.WorldGeneration;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import static com.MBEv2.utils.Constants.*;

public class EngineManager {

    public static int currentFrameRate;

    private static boolean isRunning = false;

    private WindowManager window;
    private SoundManager sound;
    private GLFWErrorCallback errorCallback;

    public void init() throws Exception {
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

    public void run() {
        isRunning = true;
        long lastTime = 0;
        long lastFrameRateUpdateTime = 0;
        long lastGTTime = 0;
        int frames = 0;
        long tick = 0;

        while (isRunning) {
            long currentTime = System.nanoTime();
            long passedTime = currentTime - lastTime;
            lastTime = currentTime;

            update(20 * passedTime / NANOSECONDS_PER_SECOND);
            render((currentTime - lastGTTime) / NANOSECONDS_PER_SECOND);
            frames++;
            if (window.windowShouldClose()) stop();

            if (currentTime - lastFrameRateUpdateTime > NANOSECONDS_PER_SECOND * 0.25f) {
                lastFrameRateUpdateTime = currentTime;
                currentFrameRate = frames * 4;
                frames = 0;
            }
            if (currentTime - lastGTTime > NANOSECONDS_PER_SECOND * 0.05f) {
                lastGTTime = currentTime;
                updateGT(tick);
                input();
                tick++;
            }
        }
    }

    public void stop() {
        isRunning = false;
    }

    public void input() {
        GameLogic.input();
    }

    private void render(float timeSinceLastTick) {
        GameLogic.render(timeSinceLastTick);
        window.update();
    }

    private void update(float passedTicks) {
        GameLogic.update(passedTicks);
    }

    private void updateGT(long tick) {
        GameLogic.updateGT(tick);
    }

    public void cleanUp() {
        GameLogic.cleanUp();
        window.cleanUp();
        sound.cleanUp();
        errorCallback.free();
        GLFW.glfwTerminate();
    }
}
