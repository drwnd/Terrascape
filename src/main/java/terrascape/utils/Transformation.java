package terrascape.utils;

import terrascape.player.Camera;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import terrascape.player.WindowManager;

public final class Transformation {

    public static Matrix4f createTransformationMatrix(Vector3f position) {
        return new Matrix4f().translate(position);
    }

    public static Matrix4f getProjectionViewMatrix(Camera camera, WindowManager window) {
        Vector3f pos = camera.getPosition();
        Vector2f rot = camera.getRotation();

        Matrix4f matrix = new Matrix4f(window.getProjectionMatrix());
        matrix.rotate((float) Math.toRadians(rot.x), X_AXIS).rotate((float) Math.toRadians(rot.y), Y_AXIS);
        matrix.translate(-pos.x, -pos.y, -pos.z);

        return matrix;
    }

    private Transformation() {
    }

    private static final Vector3f X_AXIS = new Vector3f(1.0f, 0.0f, 0.0f);
    private static final Vector3f Y_AXIS = new Vector3f(0.0f, 1.0f, 0.0f);
}
