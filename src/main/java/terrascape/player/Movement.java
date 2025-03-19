package terrascape.player;

import org.joml.Vector2f;
import org.joml.Vector3f;
import terrascape.dataStorage.Chunk;

import terrascape.entity.entities.Entity;
import terrascape.entity.particles.BlockSprayParticle;
import terrascape.server.Block;
import terrascape.server.ServerLogic;
import terrascape.server.Launcher;
import terrascape.utils.Utils;

import static terrascape.utils.Constants.*;
import static terrascape.utils.Settings.*;

public final class Movement {

    // Movement state indices
    public static final int WALKING = 0;
    public static final int CROUCHING = 1;
    public static final int CRAWLING = 2;
    public static final int SWIMMING = 3;

    // Collision box size
    public static final float HALF_PLAYER_WIDTH = 0.23f;
    public static final float PLAYER_HEAD_OFFSET = 0.08f;
    public static final float[] PLAYER_FEET_OFFSETS = new float[]{1.65f, 1.4f, 0.4f, 0.4f};
    public static final float BLOCK_SPRAY_SPEED = 0.095f;

    public Movement(Player player) {
        this.player = player;
        window = Launcher.getWindow();
        sound = Launcher.getSound();
        camera = player.getCamera();
        velocity = new Vector3f();
    }

    public void move() {
        Vector3f position = camera.getPosition();
        boolean isInWater = collidesWithWater(position.x, position.y, position.z, movementState);
        Vector3f velocity = new Vector3f(0.0f, 0.0f, 0.0f);

        handleInputMovementStateChange(position);
        handleIsFlyingChange();

        if (isFlying) handleInputFling(velocity);
        else if (isInWater) handleInputSwimming(velocity);
        else handleInputWalking(velocity);

        normalizeVelocity(velocity);
        addVelocityChange(velocity);
    }

    void moveCameraHandleCollisions(float x, float y, float z) {
        Vector3f position = new Vector3f(camera.getPosition());
        Vector3f oldPosition = new Vector3f(position);
        position.add(x, y, z);

        moveXYZ(x, y, z, position, oldPosition);
        handleNonCollisionStopping(y, oldPosition, position);
        restartGeneratorIfNecessary(oldPosition, position);

        camera.setPosition(position.x, position.y, position.z);
        if (isGrounded && y < -BLOCK_SPRAY_SPEED) {
            Vector3f particlePosition = new Vector3f(position.x, position.y - PLAYER_FEET_OFFSETS[movementState], position.z);
            ServerLogic.addParticle(new BlockSprayParticle(particlePosition, getStandingBlock()));
        }
        if (position.y != oldPosition.y) isGrounded = false;
    }

    boolean collidesWithWater(float x, float y, float z, int movementState) {
        if (!player.hasCollision()) return false;

        final float minX = x - HALF_PLAYER_WIDTH;
        final float maxX = x + HALF_PLAYER_WIDTH;
        final float minY = y - PLAYER_FEET_OFFSETS[movementState];
        final float maxY = y + PLAYER_HEAD_OFFSET;
        final float minZ = z - HALF_PLAYER_WIDTH;
        final float maxZ = z + HALF_PLAYER_WIDTH;

        for (int blockX = Utils.floor(minX), maxBlockX = Utils.floor(maxX); blockX <= maxBlockX; blockX++)
            for (int blockY = Utils.floor(minY), maxBlockY = Utils.floor(maxY); blockY <= maxBlockY; blockY++)
                for (int blockZ = Utils.floor(minZ), maxBlockZ = Utils.floor(maxZ); blockZ <= maxBlockZ; blockZ++)
                    if (Block.isWaterLogged(Chunk.getBlockInWorld(blockX, blockY, blockZ))) return true;
        return false;
    }

    public short getStandingBlock() {
        Vector3f position = camera.getPosition();

        final float minX = position.x - HALF_PLAYER_WIDTH;
        final float maxX = position.x + HALF_PLAYER_WIDTH;
        final float minY = position.y - PLAYER_FEET_OFFSETS[movementState] - 0.125f;
        final float maxY = position.y - PLAYER_FEET_OFFSETS[movementState] + 0.125f;
        final float minZ = position.z - HALF_PLAYER_WIDTH;
        final float maxZ = position.z + HALF_PLAYER_WIDTH;

        for (int blockX = Utils.floor(minX), maxBlockX = Utils.floor(maxX); blockX <= maxBlockX; blockX++)
            for (int blockY = Utils.floor(minY), maxBlockY = Utils.floor(maxY); blockY <= maxBlockY; blockY++)
                for (int blockZ = Utils.floor(minZ), maxBlockZ = Utils.floor(maxZ); blockZ <= maxBlockZ; blockZ++) {

                    short block = Chunk.getBlockInWorld(blockX, blockY, blockZ);

                    if (Entity.entityIntersectsBlock(minX, maxX, minY, maxY, minZ, maxZ, blockX, blockY, blockZ, block))
                        return block;
                }
        return AIR;
    }

    public int getMovementState() {
        return movementState;
    }

    public boolean isGrounded() {
        return isGrounded;
    }

    public boolean isFlying() {
        return isFlying;
    }

    public boolean isTouchingWater() {
        return touchingWater;
    }

    public void setTouchingWater(boolean touchingWater) {
        this.touchingWater = touchingWater;
    }

    public void setMovementState(int movementState) {
        this.movementState = movementState;
    }

    public void setFlying(boolean flying) {
        isFlying = flying;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public void setVelocity(float x, float y, float z) {
        velocity.set(x, y, z);
    }


    private void handleInputMovementStateChange(Vector3f position) {
        if (player.isInInventory()) return;

        if (window.isKeyPressed(SNEAK_BUTTON)) {
            if (movementState == WALKING) {
                camera.movePosition(0.0f, -0.25f, 0.0f);
                movementState = CROUCHING;
            }

        } else if (movementState == CROUCHING) {
            if (!collidesWithBlock(position.x, position.y + 0.25f, position.z, WALKING)) {
                camera.movePosition(0.0f, 0.25f, 0.0f);
                movementState = WALKING;
            } else if (!collidesWithBlock(position.x, position.y, position.z, WALKING)) movementState = WALKING;
        }

        if (window.isKeyPressed(CRAWL_BUTTON)) {
            if (movementState == WALKING) camera.movePosition(0.0f, -1.25f, 0.0f);
            else if (movementState == CROUCHING) camera.movePosition(0.0f, -1.0f, 0.0f);
            movementState = CRAWLING;

        } else if (movementState == CRAWLING) {
            if (!collidesWithBlock(position.x, position.y + 1.25f, position.z, WALKING)) {
                camera.movePosition(0.0f, 1.25f, 0.0f);
                movementState = WALKING;
            } else if (!collidesWithBlock(position.x, position.y + 1.0f, position.z, CROUCHING)) {
                camera.movePosition(0.0f, 1.0f, 0.0f);
                movementState = CROUCHING;
            } else if (!collidesWithBlock(position.x, position.y, position.z, WALKING)) movementState = WALKING;
            else if (!collidesWithBlock(position.x, position.y, position.z, CROUCHING)) movementState = CROUCHING;
        }

        if (movementState == SWIMMING && !window.isKeyPressed(SPRINT_BUTTON)) movementState = CRAWLING;
        else if (movementState == SWIMMING && !collidesWithWater(position.x, position.y, position.z, SWIMMING))
            movementState = CRAWLING;
    }

    private void handleIsFlyingChange() {
        long currentTime = System.nanoTime();
        if (window.isKeyPressed(JUMP_BUTTON)) {
            if (!spaceButtonPressed) {
                spaceButtonPressed = true;
                if (currentTime - spaceButtonPressTime < 300_000_000) isFlying = !isFlying;
                spaceButtonPressTime = currentTime;
            }
        } else if (spaceButtonPressed) spaceButtonPressed = false;

    }

    private void handleInputFling(Vector3f velocity) {
        if (player.isInInventory()) {
            this.velocity.mul(FLY_FRICTION);
            return;
        }

        float movementSpeedModifier = 1.0f;

        if (window.isKeyPressed(SPRINT_BUTTON)) movementSpeedModifier *= 2.5f;
        if (window.isKeyPressed(FLY_FAST_BUTTON)) movementSpeedModifier *= 5.0f;

        if (window.isKeyPressed(MOVE_FORWARD_BUTTON)) {
            velocity.z -= FLY_SPEED * movementSpeedModifier;
        }
        if (window.isKeyPressed(MOVE_BACK_BUTTON)) {
            velocity.z += FLY_SPEED;
        }

        if (window.isKeyPressed(MOVE_LEFT_BUTTON)) {
            velocity.x -= FLY_SPEED;
        }
        if (window.isKeyPressed(MOVE_RIGHT_BUTTON)) {
            velocity.x += FLY_SPEED;
        }

        if (window.isKeyPressed(JUMP_BUTTON)) velocity.y += FLY_SPEED;

        if (window.isKeyPressed(SNEAK_BUTTON)) velocity.y -= FLY_SPEED;

        this.velocity.mul(FLY_FRICTION);
    }

    private void handleInputSwimming(Vector3f velocity) {
        this.velocity.mul(WATER_FRICTION);

        if (player.isInInventory()) {
            applyGravity();
            return;
        }

        float accelerationModifier = SWIM_STRENGTH;

        if (window.isKeyPressed(SPRINT_BUTTON) && window.isKeyPressed(MOVE_FORWARD_BUTTON)) {
            Vector2f cameraRotation = camera.getRotation();
            float acceleration = SWIM_STRENGTH * accelerationModifier * 2.5f;
            velocity.z -= (float) (acceleration * Math.cos(Math.toRadians(cameraRotation.x)));
            velocity.y -= (float) (acceleration * Math.sin(Math.toRadians(cameraRotation.x)));
            if (movementState != SWIMMING) {
                if (movementState == WALKING) camera.movePosition(0.0f, -1.25f, 0.0f);
                else if (movementState == CROUCHING) camera.movePosition(0.0f, -1.0f, 0.0f);
                movementState = SWIMMING;
            }
        } else {
            if (window.isKeyPressed(MOVE_FORWARD_BUTTON)) {
                float acceleration = SWIM_STRENGTH * accelerationModifier;
                velocity.z -= acceleration;
            }
            if (window.isKeyPressed(MOVE_BACK_BUTTON)) {
                float acceleration = SWIM_STRENGTH * accelerationModifier;
                velocity.z += acceleration;
            }
            applyGravity();
        }
        if (window.isKeyPressed(MOVE_LEFT_BUTTON)) {
            float acceleration = SWIM_STRENGTH * accelerationModifier;
            velocity.x -= acceleration;
        }
        if (window.isKeyPressed(MOVE_RIGHT_BUTTON)) {
            float acceleration = SWIM_STRENGTH * accelerationModifier;
            velocity.x += acceleration;
        }

        long currentTime = System.nanoTime();
        if (window.isKeyPressed(JUMP_BUTTON)) {
            if (isGrounded) {
                this.velocity.y = JUMP_STRENGTH;
                isGrounded = false;
                spaceButtonPressTime = currentTime;
            } else velocity.y += SWIM_STRENGTH * 0.65f;
        }

        if (window.isKeyPressed(SNEAK_BUTTON)) velocity.y -= SWIM_STRENGTH * 0.65f;
    }

    private void handleInputWalking(Vector3f velocity) {
        if (player.isInInventory()) {
            float friction = isGrounded ? GROUND_FRICTION : AIR_FRICTION;
            applyGravity();
            this.velocity.mul(friction, FALL_FRICTION, friction);
            return;
        }

        float movementSpeedModifier = 1.0f;
        float accelerationModifier = isGrounded ? 1.0f : IN_AIR_SPEED;
        float jumpingAddend = 0.0f;
        long currentTime = System.nanoTime();

        if (movementState == WALKING && window.isKeyPressed(SPRINT_BUTTON)) {
            movementSpeedModifier *= 1.3f;
            if (window.isKeyPressed(JUMP_BUTTON) && isGrounded && currentTime - spaceButtonPressTime > 300_000_000) {
                jumpingAddend = 0.04f;
            }
        }

        if (window.isKeyPressed(MOVE_FORWARD_BUTTON)) {
            float acceleration = (MOVEMENT_STATE_SPEED[movementState] + jumpingAddend) * movementSpeedModifier * accelerationModifier;
            velocity.z -= acceleration;
        }
        if (window.isKeyPressed(MOVE_BACK_BUTTON)) {
            float acceleration = MOVEMENT_STATE_SPEED[movementState] * accelerationModifier;
            velocity.z += acceleration;
        }

        if (window.isKeyPressed(MOVE_LEFT_BUTTON)) {
            float acceleration = MOVEMENT_STATE_SPEED[movementState] * accelerationModifier;
            velocity.x -= acceleration;
        }
        if (window.isKeyPressed(MOVE_RIGHT_BUTTON)) {
            float acceleration = MOVEMENT_STATE_SPEED[movementState] * accelerationModifier;
            velocity.x += acceleration;
        }

        float friction = isGrounded ? GROUND_FRICTION : AIR_FRICTION;
        applyGravity();
        this.velocity.mul(friction, FALL_FRICTION, friction);

        if (window.isKeyPressed(JUMP_BUTTON) && isGrounded) {
            this.velocity.y = JUMP_STRENGTH;
            isGrounded = false;
            spaceButtonPressTime = currentTime;

            Vector3f position = camera.getPosition();
            sound.playRandomSound(Block.getFootstepsSound(getStandingBlock()), position.x, position.y, position.z, this.velocity.x, this.velocity.y, this.velocity.z, STEP_GAIN * 1.5f);
        }
    }

    private void normalizeVelocity(Vector3f velocity) {
        float maxSpeed = Math.max(Math.abs(velocity.x), Math.abs(velocity.z));
        float normalizer = maxSpeed / (float) Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        if (isGrounded && !Float.isNaN(normalizer) && Float.isFinite(normalizer)) {
            velocity.x *= normalizer;
            velocity.z *= normalizer;
        }
    }

    private void addVelocityChange(Vector3f velocity) {
        Vector2f rotation = camera.getRotation();

        if (velocity.z != 0) {
            this.velocity.x -= (float) Math.sin(Math.toRadians(rotation.y)) * velocity.z;
            this.velocity.z += (float) Math.cos(Math.toRadians(rotation.y)) * velocity.z;
        }
        if (velocity.x != 0) {
            this.velocity.x -= (float) Math.sin(Math.toRadians(rotation.y - 90)) * velocity.x;
            this.velocity.z += (float) Math.cos(Math.toRadians(rotation.y - 90)) * velocity.x;
        }
        this.velocity.y += velocity.y;
    }

    private void applyGravity() {
        velocity.y -= GRAVITY_ACCELERATION;
    }

    private static void restartGeneratorIfNecessary(Vector3f oldPosition, Vector3f position) {
        if (Utils.floor(oldPosition.x) >> CHUNK_SIZE_BITS != Utils.floor(position.x) >> CHUNK_SIZE_BITS)
            ServerLogic.restartGenerator(position.x > oldPosition.x ? NORTH : SOUTH);

        else if (Utils.floor(oldPosition.y) >> CHUNK_SIZE_BITS != Utils.floor(position.y) >> CHUNK_SIZE_BITS)
            ServerLogic.restartGenerator(position.y > oldPosition.y ? TOP : BOTTOM);

        else if (Utils.floor(oldPosition.z) >> CHUNK_SIZE_BITS != Utils.floor(position.z) >> CHUNK_SIZE_BITS)
            ServerLogic.restartGenerator(position.z > oldPosition.z ? WEST : EAST);
    }

    private void handleNonCollisionStopping(float y, Vector3f oldPosition, Vector3f position) {
        // Not falling of an edge when sneaking or crawling
        if ((movementState == CROUCHING || movementState == CRAWLING) && isGrounded && y <= 0.0f && collidesWithBlock(oldPosition.x, position.y - 0.0625f, oldPosition.z, movementState)) {
            boolean onEdgeX = !collidesWithBlock(position.x, position.y - 0.5625f, oldPosition.z, movementState);
            boolean onEdgeZ = !collidesWithBlock(oldPosition.x, position.y - 0.5625f, position.z, movementState);

            if (onEdgeX) {
                position.x = oldPosition.x;
                position.y = oldPosition.y;
                velocity.x = 0.0f;
                velocity.y = 0.0f;
            }
            if (onEdgeZ) {
                position.z = oldPosition.z;
                position.y = oldPosition.y;
                velocity.z = 0.0f;
                velocity.y = 0.0f;
            }
        }
        // Not swimming out of the water
        if (movementState == SWIMMING && y > 0.0f && !collidesWithWater(position.x, position.y, position.z, SWIMMING)) {
            position.y = oldPosition.y;
            velocity.y = 0.0f;
        }
    }

    private void moveXYZ(float x, float y, float z, Vector3f position, Vector3f oldPosition) {
        boolean xFirst = collidesWithBlock(position.x, oldPosition.y, oldPosition.z, movementState);
        boolean zFirst = collidesWithBlock(oldPosition.x, oldPosition.y, position.z, movementState);
        boolean xAndZ = collidesWithBlock(position.x, oldPosition.y, position.z, movementState);
        float requiredStepHeight = getRequiredStepHeight(position.x, position.y, position.z, movementState);
        boolean canAutoStep = (isGrounded && !isFlying || movementState == SWIMMING) && requiredStepHeight <= MAX_STEP_HEIGHT;

        if ((xFirst || xAndZ) && (zFirst || xAndZ) && canAutoStep && !collidesWithBlock(position.x, oldPosition.y + requiredStepHeight, position.z, movementState)) {
            position.y += requiredStepHeight;
            oldPosition.y += requiredStepHeight;
        } else if ((xFirst || xAndZ) && (zFirst || xAndZ)) {
            if (xFirst && xAndZ) {
                position.x = oldPosition.x;
                velocity.x = 0.0f;
            } else {
                position.z = oldPosition.z;
                velocity.z = 0.0f;
            }

            if (zFirst && xAndZ) {
                position.z = oldPosition.z;
                velocity.z = 0.0f;
            } else {
                position.x = oldPosition.x;
                velocity.x = 0.0f;
            }

            if (!(xFirst && xAndZ) && !(zFirst && xAndZ)) if (Math.abs(x) > Math.abs(z)) position.x += x;
            else position.z += z;
        }

        if (collidesWithBlock(position.x, position.y, position.z, movementState)) {
            position.y = oldPosition.y;
            isGrounded = y < 0.0f;
            velocity.y = 0.0f;
            if (y < 0.0f) isFlying = false;
        }
    }

    private boolean collidesWithBlock(float x, float y, float z, int movementState) {
        if (!player.hasCollision()) return false;

        final float minX = x - HALF_PLAYER_WIDTH;
        final float maxX = x + HALF_PLAYER_WIDTH;
        final float minY = y - PLAYER_FEET_OFFSETS[movementState];
        final float maxY = y + PLAYER_HEAD_OFFSET;
        final float minZ = z - HALF_PLAYER_WIDTH;
        final float maxZ = z + HALF_PLAYER_WIDTH;

        for (int blockX = Utils.floor(minX), maxBlockX = Utils.floor(maxX); blockX <= maxBlockX; blockX++)
            for (int blockY = Utils.floor(minY), maxBlockY = Utils.floor(maxY); blockY <= maxBlockY; blockY++)
                for (int blockZ = Utils.floor(minZ), maxBlockZ = Utils.floor(maxZ); blockZ <= maxBlockZ; blockZ++) {

                    short block = Chunk.getBlockInWorld(blockX, blockY, blockZ);

                    if (Entity.entityIntersectsBlock(minX, maxX, minY, maxY, minZ, maxZ, blockX, blockY, blockZ, block))
                        return true;
                }
        return false;
    }

    private float getRequiredStepHeight(float x, float y, float z, int movementState) {
        final float minX = x - HALF_PLAYER_WIDTH;
        final float maxX = x + HALF_PLAYER_WIDTH;
        final float minY = y - PLAYER_FEET_OFFSETS[movementState];
        final float maxY = y + PLAYER_HEAD_OFFSET;
        final float minZ = z - HALF_PLAYER_WIDTH;
        final float maxZ = z + HALF_PLAYER_WIDTH;

        float requiredStepHeight = 0.0f;

        for (int blockX = Utils.floor(minX), maxPlayerX = Utils.floor(maxX); blockX <= maxPlayerX; blockX++)
            for (int blockY = Utils.floor(minY), maxPlayerY = Utils.floor(maxY); blockY <= maxPlayerY; blockY++)
                for (int blockZ = Utils.floor(minZ), maxPlayerZ = Utils.floor(maxZ); blockZ <= maxPlayerZ; blockZ++) {

                    short block = Chunk.getBlockInWorld(blockX, blockY, blockZ);

                    int blockType = Block.getBlockType(block);
                    byte[] blockXYZSubData = Block.getXYZSubData(block);
                    if (blockXYZSubData.length == 0 || blockType == LIQUID_TYPE) continue;

                    for (int aabbIndex = 0; aabbIndex < blockXYZSubData.length; aabbIndex += 6) {
                        float minBlockX = blockX + blockXYZSubData[MIN_X + aabbIndex] * 0.0625f;
                        float maxBlockX = 1 + blockX + blockXYZSubData[MAX_X + aabbIndex] * 0.0625f;
                        float minBlockY = blockY + blockXYZSubData[MIN_Y + aabbIndex] * 0.0625f;
                        float maxBlockY = 1 + blockY + blockXYZSubData[MAX_Y + aabbIndex] * 0.0625f;
                        float minBlockZ = blockZ + blockXYZSubData[MIN_Z + aabbIndex] * 0.0625f;
                        float maxBlockZ = 1 + blockZ + blockXYZSubData[MAX_Z + aabbIndex] * 0.0625f;

                        if (minX < maxBlockX && maxX > minBlockX && minY < maxBlockY && maxY > minBlockY && minZ < maxBlockZ && maxZ > minBlockZ) {
                            float thisBlockStepHeight = maxBlockY - minY;
                            requiredStepHeight = Math.max(requiredStepHeight, thisBlockStepHeight);
                        }
                    }
                }
        return requiredStepHeight;
    }

    private long spaceButtonPressTime;
    private boolean spaceButtonPressed = false;

    private int movementState = WALKING;
    private boolean isGrounded = false;
    private boolean isFlying;
    private boolean touchingWater;

    private final WindowManager window;
    private final SoundManager sound;
    private final Player player;
    private final Camera camera;
    private final Vector3f velocity;

    private static final float FLY_FRICTION = 0.8f;

    private static final float MOVEMENT_SPEED = 0.098f;
    private static final float IN_AIR_SPEED = 0.2f;
    private static final float[] MOVEMENT_STATE_SPEED = new float[]{MOVEMENT_SPEED, 0.0294f, MOVEMENT_SPEED * 0.25f};
    private static final float FLY_SPEED = 0.06f;

    private static final float JUMP_STRENGTH = 0.42f;
    private static final float SWIM_STRENGTH = 0.26f;
    private static final float MAX_STEP_HEIGHT = 0.6f;
}
