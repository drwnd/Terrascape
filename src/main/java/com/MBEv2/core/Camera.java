package com.MBEv2.core;

import org.joml.Vector2f;
import org.joml.Vector3f;

import static com.MBEv2.utils.Constants.*;

public class Camera {

    private final Vector3f position;
    private final Vector2f rotation;


    public Camera() {
        position = new Vector3f(0.0f, 0.0f, 0.0f);
        rotation = new Vector2f(0.0f, 0.0f);
    }

    public void movePosition(float x, float y, float z) {
        position.add(x, y, z);
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

    public int getPrimaryXZDirection(Vector3f cameraDirection){
        float x = Math.abs(cameraDirection.x);
        float z = Math.abs(cameraDirection.z);
        if (x > z)
            return cameraDirection.x > 0 ? 2 : 5;
        else
            return cameraDirection.z > 0 ? 0 : 3;
    }

    public int getPrimaryDirection(Vector3f cameraDirection) {
        float x = Math.abs(cameraDirection.x);
        float y = Math.abs(cameraDirection.y);
        float z = Math.abs(cameraDirection.z);
        if (x > z && x > y)
            return cameraDirection.x > 0.0f ? WEST : EAST;
        else if (z > x && z > y)
            return cameraDirection.z > 0.0f ? NORTH : SOUTH;
        return cameraDirection.y > 0.0f ? TOP : BOTTOM;
    }

    public void setPosition(float x, float y, float z) {
        position.x = x;
        position.y = y;
        position.z = z;
    }

    public void moveRotation(float x, float y) {
        rotation.x += x;
        rotation.y += y;
        rotation.x = Math.max(-90, Math.min(rotation.x, 90));
    }

    public void setRotation(float x, float y) {
        rotation.x = x;
        rotation.y = y;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector2f getRotation() {
        return rotation;
    }
}
