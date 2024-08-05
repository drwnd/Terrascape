package com.MBEv2.core;

import static com.MBEv2.core.utils.Constants.*;

import com.MBEv2.test.GameLogic;
import com.MBEv2.test.Launcher;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;

public class EngineManager {

    public static final float NANOSECONDS_PER_SECOND = 1_000_000_000;
    public static float FRAME_RATE;

    public static boolean isRunning;

    private WindowManager window;
    private GLFWErrorCallback errorCallback;

    private void init() throws Exception {
        GLFW.glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        window = Launcher.getWindow();
        window.init();
        GameLogic.init();
        if (window.isvSync()) {
            GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            assert vidMode != null;
            FRAME_RATE = vidMode.refreshRate();
        } else FRAME_RATE = Float.MAX_VALUE;
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
        long lastTime = 0;
        long lastFrameRateUpdateTime = 0;
        long lastInputTime = 0;
        int frames = 0;

        while (isRunning) {
            long currentTime = System.nanoTime();
            long passedTime = currentTime - lastTime;
            lastTime = currentTime;

            update(20 * passedTime / NANOSECONDS_PER_SECOND);
            render();
            frames++;
            if (window.windowShouldClose())
                stop();

            if (currentTime - lastFrameRateUpdateTime > NANOSECONDS_PER_SECOND * 0.25f) {
                lastFrameRateUpdateTime = currentTime;
                window.setTitle(TITLE + " FPS: " + frames * 4);
                frames = 0;
            }
            if (currentTime - lastInputTime > NANOSECONDS_PER_SECOND * 0.05f) {
                lastInputTime = currentTime;
                input();
            }
        }
        cleanUp();
    }

    public void stop() {
        isRunning = false;
    }

    public void input() {
        GameLogic.input();
    }

    private void render() {
        GameLogic.render();
        window.update();
    }

    private void update(float passedTime) {
        GameLogic.update(passedTime);
    }

    private void cleanUp() {
        GameLogic.cleanUp();
        window.cleanUp();
        errorCallback.free();
        GLFW.glfwTerminate();
    }
}
