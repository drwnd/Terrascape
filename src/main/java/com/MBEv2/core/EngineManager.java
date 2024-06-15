package com.MBEv2.core;

import static com.MBEv2.core.utils.Constants.*;
import com.MBEv2.test.GameLogic;
import com.MBEv2.test.Launcher;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

public class EngineManager {

    public static final double NANOSECOND = 1_000_000_000;
    public static final float FRAME_RATE = 1000F;

    private static int fps;

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
        int frames = 0;
        long frameCounter = 0;
        long lastTime = System.nanoTime();
        double unprocessedTime = 0;

        while (isRunning) {
            boolean render = false;
            long startTime = System.nanoTime();
            long passedTime = startTime - lastTime;
            lastTime = startTime;

            unprocessedTime += passedTime / NANOSECOND;
            frameCounter += passedTime;

            input((float) (passedTime / NANOSECOND));

            float frameTime = 1 / FRAME_RATE;
            while (unprocessedTime > frameTime) {
                render = true;
                unprocessedTime -= frameTime;

                if (window.windowShouldClose())
                    stop();

                if (frameCounter >= NANOSECOND) {
                    setFps(frames);
                    window.setTitle(TITLE + " FPS: " + getFps());
                    frames = 0;
                    frameCounter = 0;
                }
            }

            if (render) {
                update();
                render();
                frames++;
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

    public static int getFps() {
        return fps;
    }

    public static void setFps(int fps) {
        EngineManager.fps = fps;
    }
}
