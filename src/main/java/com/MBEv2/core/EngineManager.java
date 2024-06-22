package com.MBEv2.core;

import static com.MBEv2.core.utils.Constants.*;

import com.MBEv2.test.GameLogic;
import com.MBEv2.test.Launcher;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

public class EngineManager {

    public static final float NANOSECONDS_PER_SECOND = 1_000_000_000;
    public static final float FRAME_RATE = 144.0f;

    public static boolean isRunning;

    private WindowManager window;
    private GLFWErrorCallback errorCallback;

    private void init() throws Exception {
        GLFW.glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        window = Launcher.getWindow();
        window.init();
        GameLogic.init();
    }

    public void start() throws Exception {
        Block.init();
        init();
        if (isRunning)
            return;
        run();
    }

    public void run() {
        isRunning = true;
        GameLogic.startChunkGenerator();
        long lastTime = System.nanoTime();
        long frameTime = (long) (NANOSECONDS_PER_SECOND / FRAME_RATE);

        while (isRunning) {
            long startTime = System.nanoTime();
            long passedTime = startTime - lastTime;
            lastTime = startTime;

            input((float) passedTime / NANOSECONDS_PER_SECOND);
            update();
            render();
            window.setTitle(TITLE + " FPS: " + (int) (NANOSECONDS_PER_SECOND / (float) passedTime));
            if (window.windowShouldClose())
                stop();

            while (System.nanoTime() < startTime + frameTime) {

            }

        }
        cleanUp();
    }

    public void stop() {
        isRunning = false;
    }

    public void input(float passedTime) {
        GameLogic.input(passedTime);
    }

    private void render() {
        GameLogic.render();
        window.update();
    }

    private void update() {
        GameLogic.update();
    }

    private void cleanUp() {
        GameLogic.cleanUp();
        window.cleanUp();
        errorCallback.free();
        GLFW.glfwTerminate();
    }
}
