package com.MBEv2.core;

import com.MBEv2.core.entity.Player;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Camera {

    private final Vector3f position;
    private final Vector2f rotation;
    private final Player player;

    public Camera(Player player) {
        position = new Vector3f(0, 0, 0);
        rotation = new Vector2f(0, 0);
        this.player = player;
    }

    public void movePosition(float x, float y, float z) {
        Vector3f oldPosition = new Vector3f(position);

        if (z != 0) {
            position.x -= (float) Math.sin(Math.toRadians(rotation.y)) * z;
            position.z += (float) Math.cos(Math.toRadians(rotation.y)) * z;
        }
        if (x != 0) {
            position.x -= (float) Math.sin(Math.toRadians(rotation.y - 90)) * x;
            position.z += (float) Math.cos(Math.toRadians(rotation.y - 90)) * x;
        }
        position.y += y;

        int movementState = player.getMovementState();

        if (!player.isNoClip() && player.collidesWithBlock(position.x, position.y, position.z, movementState)) {
            if (player.collidesWithBlock(position.x, oldPosition.y, oldPosition.z, movementState))
                position.x = oldPosition.x;
            if (player.collidesWithBlock(oldPosition.x, position.y, oldPosition.z, movementState))
                position.y = oldPosition.y;
            if (player.collidesWithBlock(oldPosition.x, oldPosition.y, position.z, movementState))
                position.z = oldPosition.z;
            return;
        }

        if ((int) Math.floor(oldPosition.x) >> 5 != (int) Math.floor(position.x) >> 5)
            player.loadUnloadChunks();

        else if ((int) Math.floor(oldPosition.y) >> 5 != (int) Math.floor(position.y) >> 5)
            player.loadUnloadChunks();

        else if ((int) Math.floor(oldPosition.z) >> 5 != (int) Math.floor(position.z) >> 5)
            player.loadUnloadChunks();
    }

    public void movePositionNoChecks(float x, float y, float z) {
        position.x += x;
        position.y += y;
        position.z += z;
    }

    public Vector3f getDirection() {

        float rotationXRadians = (float) Math.toRadians(rotation.y);
        float rotationYRadians = (float) Math.toRadians(rotation.x);

        float x = (float) Math.sin(rotationXRadians);
        float y = (float) -Math.sin(rotationYRadians);
        float z = (float) -Math.cos(rotationXRadians);

        float v = (float) Math.sqrt(1 - y * y);

        x *= v;
        z *= v;

        return new Vector3f(x, y, z);
    }

    public int getPrimaryDirection() {
        return getPrimaryDirection(getDirection());
    }

    public int getPrimaryDirection(Vector3f cameraDirection) {
        float x = Math.abs(cameraDirection.x);
        float y = Math.abs(cameraDirection.y);
        float z = Math.abs(cameraDirection.z);
        if (x > z && x > y)
            return cameraDirection.x > 0 ? 2 : 5;
        else if (z > x && z > y)
            return cameraDirection.z > 0 ? 0 : 3;
        return cameraDirection.y > 0 ? 1 : 4;
    }

    public void setPosition(float x, float y, float z) {
        position.x = x;
        position.y = y;
        position.z = z;
    }

    public void setRotation(float x, float y) {
        rotation.x = x;
        rotation.y = y;
    }

    public void moveRotation(float x, float y) {
        rotation.x += x;
        rotation.y += y;
        rotation.x = Math.max(-90, Math.min(rotation.x, 90));
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector2f getRotation() {
        return rotation;
    }
}
