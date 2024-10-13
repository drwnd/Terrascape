package com.MBEv2.core;

import com.MBEv2.core.entity.entities.Entity;
import com.MBEv2.core.entity.particles.Particle;
import com.MBEv2.test.GameLogic;
import com.MBEv2.test.Launcher;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;

import static com.MBEv2.core.utils.Constants.*;

public class EngineManager {

    public static float FRAME_RATE;
    public static int currentFrameRate;

    public static boolean isRunning = false;

    private WindowManager window;
    private SoundManager sound;
    private GLFWErrorCallback errorCallback;

    public void init() throws Exception {
        GLFW.glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        FileManager.init();
        FileManager.loadSettings(true);
        window = Launcher.getWindow();
        window.init();
        sound = Launcher.getSound();
        sound.init();
        Block.init();
        Entity.initAll();
        Particle.initAll();
        GameLogic.init();

        if (window.isvSync()) {
            GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            if (vidMode == null) throw new AssertionError();
            FRAME_RATE = vidMode.refreshRate();
        } else FRAME_RATE = Float.MAX_VALUE;
    }

    public void run() {
        isRunning = true;
        long lastTime = 0;
        long lastFrameRateUpdateTime = 0;
        long lastGTTime = 0;
        int frames = 0;
        long tick = 0L;

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
