package com.MBEv2.core;

import com.MBEv2.core.entity.GUIElement;
import com.MBEv2.core.entity.Player;
import com.MBEv2.test.Launcher;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

import static com.MBEv2.core.utils.Settings.*;

public class MouseInput {

    private final Vector2f previousPos, currentPos;
    private Vector2f displayVec;
    private final Player player;

    public MouseInput(Player player) {
        this.player = player;
        previousPos = new Vector2f(0, 0);
        currentPos = new Vector2f(0, 0);
        displayVec = new Vector2f();
    }

    public void init() {
        GLFW.glfwSetCursorPosCallback(Launcher.getWindow().getWindow(), (window, xPos, yPos) -> {
            currentPos.x = (float) xPos;
            currentPos.y = (float) yPos;
        });

        GLFW.glfwSetMouseButtonCallback(Launcher.getWindow().getWindow(), (window, button, action, mods) ->
                player.handleNonMovementInputs(button | IS_MOUSE_BUTTON, action));

        GLFW.glfwSetScrollCallback(Launcher.getWindow().getWindow(), (window, xPos, yPos) -> {
            if (!player.isInInventory()) return;
            ArrayList<GUIElement> inventoryElements = player.getInventoryElements();
            float scrollValue = (float) yPos * -0.05f;
            player.addToInventoryScroll(scrollValue);
            for (GUIElement element : inventoryElements) {
                element.getPosition().add(0.0f, scrollValue);
            }
        });

        GLFW.glfwSetInputMode(Launcher.getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
    }

    public void input() {
        float x = currentPos.x - previousPos.x;
        float y = currentPos.y - previousPos.y;

        displayVec.y = x;
        displayVec.x = y;

        previousPos.x = currentPos.x;
        previousPos.y = currentPos.y;
    }

    public Vector2f getDisplayVec() {
        Vector2f returns = displayVec;
        displayVec = new Vector2f();
        return returns;
    }
}
