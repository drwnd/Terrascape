package com.MBEv2.core;

import com.MBEv2.core.entity.Player;
import com.MBEv2.core.utils.Utils;
import com.MBEv2.test.GameLogic;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static com.MBEv2.core.utils.Constants.*;

public class Camera {

    private final Vector3f position;
    private final Vector2f rotation;
    private final Player player;

    public Camera(Player player) {
        position = new Vector3f(0.0f, 0.0f, 0.0f);
        rotation = new Vector2f(0.0f, 0.0f);
        this.player = player;
    }

    public void movePosition(float x, float y, float z) {
        Vector3f oldPosition = new Vector3f(position);
        position.add(x, y, z);

        if (!player.isNoClip()) {
            int movementState = player.getMovementState();

            boolean xFirst = player.collidesWithBlock(position.x, oldPosition.y, oldPosition.z, movementState);
            boolean zFirst = player.collidesWithBlock(oldPosition.x, oldPosition.y, position.z, movementState);
            boolean xAndZ = player.collidesWithBlock(position.x, oldPosition.y, position.z, movementState);

            if ((xFirst || xAndZ) && (zFirst || xAndZ)) {
                if (xFirst && xAndZ) {
                    position.x = oldPosition.x;
                    player.setVelocityX(0.0f);
                } else {
                    position.z = oldPosition.z;
                    player.setVelocityZ(0.0f);
                }

                if (zFirst && xAndZ) {
                    position.z = oldPosition.z;
                    player.setVelocityZ(0.0f);
                } else {
                    position.x = oldPosition.x;
                    player.setVelocityX(0.0f);
                }

                if (!(xFirst && xAndZ) && !(zFirst && xAndZ))
                    if (Math.abs(x) > Math.abs(z))
                        position.x += x;
                    else
                        position.z += z;
            }

            if (player.collidesWithBlock(position.x, position.y, position.z, movementState)) {
                position.y = oldPosition.y;
                player.setGrounded(y < 0.0f);
                player.setVelocityY(0.0f);
            } else if ((movementState == CROUCHING || movementState == CRAWLING) && player.isGrounded() && y < 0.0f) {
                boolean onEdgeX = !player.collidesWithBlock(position.x, position.y - 0.0625f, oldPosition.z, movementState);
                boolean onEdgeZ = !player.collidesWithBlock(oldPosition.x, position.y - 0.0625f, position.z, movementState);

                if (onEdgeX) {
                    position.x = oldPosition.x;
                    position.y = oldPosition.y;
                    player.setVelocityX(0.0f);
                    player.setVelocityY(0.0f);
                }
                if (onEdgeZ) {
                    position.z = oldPosition.z;
                    position.y = oldPosition.y;
                    player.setVelocityZ(0.0f);
                    player.setVelocityY(0.0f);
                }
            }

            if (position.y != oldPosition.y)
                player.setGrounded(false);
        }

        if (Utils.floor(oldPosition.x) >> 5 != Utils.floor(position.x) >> 5)
            GameLogic.loadUnloadChunks();

        else if (Utils.floor(oldPosition.y) >> 5 != Utils.floor(position.y) >> 5)
            GameLogic.loadUnloadChunks();

        else if (Utils.floor(oldPosition.z) >> 5 != Utils.floor(position.z) >> 5)
            GameLogic.loadUnloadChunks();
    }

    public void movePositionNoChecks(float x, float y, float z) {
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
