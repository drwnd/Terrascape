package terrascape.player;

import static terrascape.utils.Constants.*;
import static terrascape.utils.Settings.*;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

public final class WindowManager {

    public WindowManager(String title, int width, int height, boolean vSync, boolean maximized) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.vSync = vSync;
        this.maximized = maximized;

        projectionMatrix = new Matrix4f();
    }

    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE);

        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        if (vidMode == null) throw new RuntimeException("Could not get video mode");

        if (maximized) {
            GLFW.glfwWindowHint(GLFW.GLFW_MAXIMIZED, GLFW.GLFW_TRUE);
            window = GLFW.glfwCreateWindow(vidMode.width(), vidMode.height(), title, GLFW.glfwGetPrimaryMonitor(), MemoryUtil.NULL);
        } else window = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);

        if (window == MemoryUtil.NULL) throw new RuntimeException("Failed to create GLFW window");

        GLFW.glfwSetFramebufferSizeCallback(window, (long window, int width, int height) -> {
            this.width = width;
            this.height = height;
            this.setResize(true);
            updateProjectionMatrix();
        });

        if (maximized) {
            GLFW.glfwMaximizeWindow(window);
            width = vidMode.width();
            height = vidMode.height();
        } else
            GLFW.glfwSetWindowPos(window, (vidMode.width() - width) / 2, (vidMode.height() - height) / 2);

        GLFW.glfwMakeContextCurrent(window);

        GLFW.glfwSwapInterval(vSync ? 1 : 0);

        GLFW.glfwShowWindow(window);

        GL.createCapabilities();

        GL11.glClearColor(0, 0, 0, 1);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        updateProjectionMatrix();
    }

    public void update() {
        GLFW.glfwSwapBuffers(window);
        GLFW.glfwPollEvents();
    }

    public void cleanUp() {
        GLFW.glfwDestroyWindow(window);
    }

    public boolean isKeyPressed(int keycode) {
        if ((keycode & IS_MOUSE_BUTTON) == 0) return GLFW.glfwGetKey(window, keycode & 0x7FFFFFFF) == GLFW.GLFW_PRESS;
        else return GLFW.glfwGetMouseButton(window, keycode & 0x7FFFFFFF) == GLFW.GLFW_PRESS;
    }

    public boolean windowShouldClose() {
        return GLFW.glfwWindowShouldClose(window);
    }

    public boolean isResize() {
        return resize;
    }

    public void setResize(boolean resize) {
        this.resize = resize;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getWindow() {
        return window;
    }

    public void updateProjectionMatrix() {
        float aspectRatio = (float) width / height;
        projectionMatrix.setPerspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
    }

    public void updateProjectionMatrix(float fov) {
        float aspectRatio = (float) width / height;
        projectionMatrix.setPerspective(fov, aspectRatio, Z_NEAR, Z_FAR);
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    private final String title;
    private int width, height;
    private long window;

    private boolean resize;
    private final boolean vSync, maximized;

    private final Matrix4f projectionMatrix;
}
