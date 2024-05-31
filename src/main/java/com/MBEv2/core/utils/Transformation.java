package com.MBEv2.core.utils;

import com.MBEv2.core.Camera;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Transformation {

    public static Matrix4f createTransformationMatrix(Vector3f position) {
        Matrix4f matrix = new Matrix4f();
        matrix.identity().translate(position);
        return matrix;
    }

    public static Matrix4f getViewMatrix(Camera camera) {
        Vector3f pos = camera.getPosition();
        Vector2f rot = camera.getRotation();
        Matrix4f matrix = new Matrix4f();
        matrix.identity();
        matrix.rotate((float) Math.toRadians(rot.x), new Vector3f(1, 0, 0))
                .rotate((float) Math.toRadians(rot.y), new Vector3f(0, 1, 0));

        matrix.translate(-pos.x, -pos.y, -pos.z);
        return matrix;
    }
}
