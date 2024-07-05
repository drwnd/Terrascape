package com.MBEv2.core.entity;

import com.MBEv2.core.*;

import static com.MBEv2.core.utils.Constants.*;

import com.MBEv2.core.utils.Utils;
import com.MBEv2.test.GameLogic;
import com.MBEv2.test.Launcher;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private final RenderManager renderer;
    private final WindowManager window;
    private final Camera camera;
    private final MouseInput mouseInput;

    private final Vector3f velocity;

    private final List<GUIElement> GUIElements = new ArrayList<>();
    private final List<GUIElement> hotBarElements = new ArrayList<>();
    private final Texture atlas;

    private long rightButtonPressTime, leftButtonPressTime, spaceButtonPressTime;
    private boolean rightButtonWasJustPressed, leftButtonWasJustPressed;
    private boolean UPArrowPressed, DOWNArrowPressed;

    //Debug
    private boolean noClip, gKeyPressed;
    private boolean isFling, vKeyPressed;
    private boolean tPressed = false, zPressed = false;
    private final Vector3i pos1, pos2;

    private final byte[][] hotBars = {
            {GRASS, DIRT, SAND, MUD, SNOW, LAVA, WATER, GRAVEL, COURSE_DIRT},
            {CREATOR_HEAD, COAL_ORE, IRON_ORE, DIAMOND_ORE, GLASS, GLASS_WALL, CLAY, MOSS, AIR},
            {OAK_LOG, STRIPPED_OAK_LOG, OAK_LEAVES, OAK_PLANKS, OAK_PLANKS_SLAB, OAK_PLANKS_WALL, OAK_PLANKS_POST, OAK_PLANKS_PLATE, AIR},
            {SPRUCE_LOG, STRIPPED_SPRUCE_LOG, SPRUCE_LEAVES, SPRUCE_PLANKS, SPRUCE_PLANKS_SLAB, SPRUCE_PLANKS_WALL, SPRUCE_PLANKS_POST, SPRUCE_PLANKS_PLATE, AIR},
            {DARK_OAK_LOG, STRIPPED_DARK_OAK_LOG, DARK_OAK_LEAVES, DARK_OAK_PLANKS, DARK_OAK_PLANKS_SLAB, DARK_OAK_PLANKS_WALL, DARK_OAK_PLANKS_POST, DARK_OAK_PLANKS_PLATE, AIR},
            {COBBLESTONE, COBBLESTONE_SLAB, COBBLESTONE_PLATE, COBBLESTONE_POST, COBBLESTONE_WALL, AIR, AIR, AIR, AIR},
            {STONE, STONE_SLAB, STONE_PLATE, STONE_POST, STONE_WALL, CHISELED_STONE, AIR, AIR, AIR},
            {STONE_BRICKS, STONE_BRICK_SLAB, STONE_BRICK_PLATE, STONE_BRICK_POST, STONE_BRICK_WALL, AIR, AIR, AIR, AIR},
            {POLISHED_STONE, POLISHED_STONE_SLAB, POLISHED_STONE_PLATE, POLISHED_STONE_POST, POLISHED_STONE_WALL, CHISELED_POLISHED_STONE, AIR, AIR, AIR},
            {SLATE, SLATE_SLAB, SLATE_PLATE, SLATE_POST, SLATE_WALL, CHISELED_SLATE, AIR, AIR, AIR},
            {ANDESITE, ANDESITE_SLAB, ANDESITE_PLATE, ANDESITE_POST, ANDESITE_WALL, AIR, AIR, AIR, AIR},
            {RED, GREEN, BLUE, YELLOW, MAGENTA, CYAN, WHITE, BLACK, BARRIER}
    };
    private int selectedHotBar = 0;
    private int selectedHotBarSlot = 0;

    private int movementState = WALKING;
    private boolean isGrounded = false;

    public Player(Texture atlas) {
        this.atlas = atlas;
        window = Launcher.getWindow();
        renderer = new RenderManager();
        camera = new Camera();
        mouseInput = new MouseInput();

        velocity = new Vector3f(0, 0, 0);
        camera.setPosition(0, 100, 0);
        pos1 = new Vector3i();
        pos2 = new Vector3i();
    }

    public void init() throws Exception {
        Texture skyBoxTexture1 = new Texture(ObjectLoader.loadTexture("textures/706c5e1da58f47ad6e18145165caf55d.png"));
        Texture skyBoxTexture2 = new Texture(ObjectLoader.loadTexture("textures/82984-skybox-blue-atmosphere-sky-space-hd-image-free-png.png"));
        SkyBox skyBox = ObjectLoader.loadSkyBox(SKY_BOX_VERTICES, SKY_BOX_TEXTURE_COORDINATES, SKY_BOX_INDICES, camera.getPosition());
        skyBox.setTexture(skyBoxTexture1, skyBoxTexture2);
        renderer.processSkyBox(skyBox);

        GUIElement crossHair = ObjectLoader.loadGUIElement(GameLogic.getCrossHairVertices(), GUI_ELEMENT_TEXTURE_COORDINATES);
        crossHair.setTexture(new Texture(ObjectLoader.loadTexture("textures/CrossHair.png")));
        GUIElements.add(crossHair);

        GUIElement hotBarGUIElement = ObjectLoader.loadGUIElement(GameLogic.getHotBarVertices(), GUI_ELEMENT_TEXTURE_COORDINATES);
        hotBarGUIElement.setTexture(new Texture(ObjectLoader.loadTexture("textures/HotBar.png")));
        GUIElements.add(hotBarGUIElement);

        GUIElement waterOverly = ObjectLoader.loadGUIElement(OVERLAY_VERTICES, GUI_ELEMENT_TEXTURE_COORDINATES);
        waterOverly.setTexture(new Texture(ObjectLoader.loadTexture("textures/WaterOverlay.png")));
        renderer.setWaterOverlay(waterOverly);

        updateHotBarElements();

        mouseInput.init();
    }

    public void update() {
        moveCameraHandleCollisions(velocity.x, velocity.y, velocity.z);

        Vector2f rotVec = mouseInput.getDisplayVec();
        camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY);

        Vector3f cP = camera.getPosition();
        renderer.setHeadUnderWater(Chunk.getBlockInWorld(Utils.floor(cP.x), Utils.floor(cP.y), Utils.floor(cP.z)) == WATER);

        long currentTime = System.nanoTime();

        if (leftButtonPressTime != -1 && (currentTime - leftButtonPressTime > 300_000_000 || leftButtonWasJustPressed)) {
            Vector3f target = getTarget(0, camera.getDirection());
            if (target != null)
                GameLogic.placeBlock(AIR, new Vector3i(Utils.floor(target.x), Utils.floor(target.y), Utils.floor(target.z)));
        }

        if (rightButtonPressTime != -1 && (currentTime - rightButtonPressTime > 300_000_000 || rightButtonWasJustPressed) && hotBars[selectedHotBar][selectedHotBarSlot] != AIR) {

            Vector3f cameraDirection = camera.getDirection();
            Vector3f target = getTarget(1, cameraDirection);
            if (target != null) {
                byte selectedBlock = hotBars[selectedHotBar][selectedHotBarSlot];
                byte toPlaceBlock = Block.getToPlaceBlock(selectedBlock, camera.getPrimaryDirection(cameraDirection), camera.getPrimaryXZDirection(cameraDirection), target);

                GameLogic.placeBlock(toPlaceBlock, new Vector3i(Utils.floor(target.x), Utils.floor(target.y), Utils.floor(target.z)));
            }
        }
    }

    public void input(float passedTime) {
        mouseInput.input();
        rightButtonPressTime = mouseInput.getRightButtonPressTime();
        leftButtonPressTime = mouseInput.getLeftButtonPressTime();
        rightButtonWasJustPressed = mouseInput.wasRightButtonJustPressed();
        leftButtonWasJustPressed = mouseInput.wasLeftButtonJustPressed();

        Vector3f position = camera.getPosition();
        boolean isInWater = collidesWithBlock(position.x, position.y, position.z, movementState, WATER);
        Vector3f velocity = new Vector3f(0.0f, 0.0f, 0.0f);

        handleInputMovementStateChange(position);

        if (isFling)
            handleInputFling(velocity, passedTime);
        else if (isInWater)
            handleInputSwimming(velocity, passedTime);
        else
            handleInputWalking(velocity, passedTime);

        normalizeVelocity(velocity);
        addVelocityChange(velocity);

        handleInputHotkeys();
        handleInputDebugHotkeys();
    }

    private void handleInputWalking(Vector3f velocity, float passedTime) {
        float friction = (float) Math.pow(isGrounded ? GROUND_FRICTION : AIR_FRICTION, passedTime);
        this.velocity.mul(friction, (float) Math.pow(FALL_FRICTION, passedTime), friction);

        float movementSpeedModifier = 1.0f;
        float accelerationModifier = isGrounded ? 1.0f : IN_AIR_SPEED;

        if (movementState == WALKING && window.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL))
            movementSpeedModifier *= 1.5f;

        if (window.isKeyPressed(GLFW.GLFW_KEY_W)) {
            float acceleration = MOVEMENT_STATE_SPEED[movementState] * movementSpeedModifier * accelerationModifier * passedTime;
            velocity.z -= acceleration;
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_S)) {
            float acceleration = MOVEMENT_STATE_SPEED[movementState] * accelerationModifier * passedTime;
            velocity.z += acceleration;
        }

        if (window.isKeyPressed(GLFW.GLFW_KEY_A)) {
            float acceleration = MOVEMENT_STATE_SPEED[movementState] * accelerationModifier * passedTime;
            velocity.x -= acceleration;
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_D)) {
            float acceleration = MOVEMENT_STATE_SPEED[movementState] * accelerationModifier * passedTime;
            velocity.x += acceleration;
        }

        long currentTime = System.nanoTime();
        if (window.isKeyPressed(GLFW.GLFW_KEY_SPACE) && isGrounded && currentTime - spaceButtonPressTime > 300_000_000) {
            this.velocity.y = JUMP_STRENGTH * passedTime;
            isGrounded = false;
            spaceButtonPressTime = currentTime;
        }
        applyGravity(passedTime);
    }

    private void handleInputSwimming(Vector3f velocity, float passedTime) {
        this.velocity.mul((float) Math.pow(WATER_FRICTION, passedTime));

        float accelerationModifier = SWIM_STRENGTH * (isGrounded ? 2.0f : 1.0f);

        if (window.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL) && window.isKeyPressed(GLFW.GLFW_KEY_W)) {
            Vector2f cameraRotation = camera.getRotation();
            float acceleration = SWIM_STRENGTH * accelerationModifier * passedTime * 2.5f;
            velocity.z -= (float) (acceleration * Math.cos(Math.toRadians(cameraRotation.x)));
            velocity.y -= (float) (acceleration * Math.sin(Math.toRadians(cameraRotation.x)));
            if (movementState != SWIMMING) {
                if (movementState == WALKING)
                    camera.movePosition(0.0f, -1.25f, 0.0f);
                else if (movementState == CROUCHING)
                    camera.movePosition(0.0f, -1.0f, 0.0f);
                movementState = SWIMMING;
            }
        } else {
            if (window.isKeyPressed(GLFW.GLFW_KEY_W)) {
                float acceleration = SWIM_STRENGTH * accelerationModifier * passedTime;
                velocity.z -= acceleration;
            }
            if (window.isKeyPressed(GLFW.GLFW_KEY_S)) {
                float acceleration = SWIM_STRENGTH * accelerationModifier * passedTime;
                velocity.z += acceleration;
            }

            if (window.isKeyPressed(GLFW.GLFW_KEY_A)) {
                float acceleration = SWIM_STRENGTH * accelerationModifier * passedTime;
                velocity.x -= acceleration;
            }
            if (window.isKeyPressed(GLFW.GLFW_KEY_D)) {
                float acceleration = SWIM_STRENGTH * accelerationModifier * passedTime;
                velocity.x += acceleration;
            }
            applyGravity(passedTime);
        }

        long currentTime = System.nanoTime();
        if (window.isKeyPressed(GLFW.GLFW_KEY_SPACE))
            if (isGrounded && currentTime - spaceButtonPressTime > 300_000_000) {
                this.velocity.y = JUMP_STRENGTH * passedTime;
                isGrounded = false;
                spaceButtonPressTime = currentTime;
            } else {
                velocity.y += SWIM_STRENGTH * passedTime;
                isGrounded = false;
            }

        if (window.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT))
            velocity.y -= SWIM_STRENGTH * passedTime;
    }

    private void handleInputFling(Vector3f velocity, float passedTime) {
        this.velocity.mul((float) Math.pow(FLY_FRICTION, passedTime));

        float movementSpeedModifier = 1.0f;

        if (window.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL))
            movementSpeedModifier *= 2.5f;
        if (window.isKeyPressed(GLFW.GLFW_KEY_TAB))
            movementSpeedModifier *= 5.0f;

        if (window.isKeyPressed(GLFW.GLFW_KEY_W)) {
            float acceleration = FLY_SPEED * MOVEMENT_STATE_SPEED[movementState] * movementSpeedModifier * passedTime;
            velocity.z -= acceleration;
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_S)) {
            float acceleration = FLY_SPEED * MOVEMENT_STATE_SPEED[movementState] * passedTime;
            velocity.z += acceleration;
        }

        if (window.isKeyPressed(GLFW.GLFW_KEY_A)) {
            float acceleration = FLY_SPEED * MOVEMENT_STATE_SPEED[movementState] * passedTime;
            velocity.x -= acceleration;
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_D)) {
            float acceleration = FLY_SPEED * MOVEMENT_STATE_SPEED[movementState] * passedTime;
            velocity.x += acceleration;
        }

        if (window.isKeyPressed(GLFW.GLFW_KEY_SPACE))
            velocity.y += FLY_SPEED * MOVEMENT_SPEED * passedTime;

        if (window.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT))
            velocity.y -= FLY_SPEED * MOVEMENT_SPEED * passedTime;
    }

    private void handleInputHotkeys() {
        if (window.isKeyPressed(GLFW.GLFW_KEY_Q)) selectedHotBarSlot = 0;
        else if (window.isKeyPressed(GLFW.GLFW_KEY_2)) selectedHotBarSlot = 1;
        else if (window.isKeyPressed(GLFW.GLFW_KEY_3)) selectedHotBarSlot = 2;
        else if (window.isKeyPressed(GLFW.GLFW_KEY_4)) selectedHotBarSlot = 3;
        else if (window.isKeyPressed(GLFW.GLFW_KEY_5)) selectedHotBarSlot = 4;
        else if (window.isKeyPressed(GLFW.GLFW_KEY_R)) selectedHotBarSlot = 5;
        else if (window.isKeyPressed(GLFW.GLFW_KEY_F)) selectedHotBarSlot = 6;
        else if (mouseInput.isMouseButton5IsPressed()) selectedHotBarSlot = 7;
        else if (mouseInput.isMouseButton4IsPressed()) selectedHotBarSlot = 8;

        if (window.isKeyPressed(GLFW.GLFW_KEY_UP) && !UPArrowPressed) {
            selectedHotBar = (selectedHotBar + 1) % hotBars.length;
            updateHotBarElements();
            UPArrowPressed = true;
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_DOWN) && !DOWNArrowPressed) {
            selectedHotBar = (selectedHotBar - 1 + hotBars.length) % hotBars.length;
            updateHotBarElements();
            DOWNArrowPressed = true;
        }
        if (UPArrowPressed && !window.isKeyPressed(GLFW.GLFW_KEY_UP)) UPArrowPressed = false;
        if (DOWNArrowPressed && !window.isKeyPressed(GLFW.GLFW_KEY_DOWN)) DOWNArrowPressed = false;
    }

    private void handleInputDebugHotkeys() {
        if (window.isKeyPressed(GLFW.GLFW_KEY_G) && !gKeyPressed) {
            noClip = !noClip;
            gKeyPressed = true;
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_T) && !tPressed) {
            Vector3f cameraPosition = camera.getPosition();
            pos1.x = (int) Math.floor(cameraPosition.x);
            pos1.y = (int) Math.floor(cameraPosition.y);
            pos1.z = (int) Math.floor(cameraPosition.z);
            tPressed = true;
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_Y) && !zPressed) {
            Vector3f cameraPosition = camera.getPosition();
            pos2.x = (int) Math.floor(cameraPosition.x);
            pos2.y = (int) Math.floor(cameraPosition.y);
            pos2.z = (int) Math.floor(cameraPosition.z);
            zPressed = true;

            int x = Math.abs(pos1.x - pos2.x) + 1;
            int y = Math.abs(pos1.y - pos2.y) + 1;
            int z = Math.abs(pos1.z - pos2.z) + 1;
            int minX = Math.min(pos1.x, pos2.x);
            int minY = Math.min(pos1.y, pos2.y);
            int minZ = Math.min(pos1.z, pos2.z);

            System.out.print("{");

            for (int i = 0; i < y; i++) {
                System.out.println("{");

                for (int j = 0; j < x; j++) {
                    System.out.print("{");

                    for (int k = 0; k < z; k++) {
                        byte block = Chunk.getBlockInWorld(minX + j, minY + i, minZ + k);
                        System.out.print(block + " ,");
                    }
                    System.out.println("},");
                }
                System.out.print("},");
            }
            System.out.print("}");
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_V) && !vKeyPressed) {
            isFling = !isFling;
            vKeyPressed = true;
            if (movementState == SWIMMING)
                movementState = CRAWLING;
        }

        if (gKeyPressed && !window.isKeyPressed(GLFW.GLFW_KEY_G)) gKeyPressed = false;
        if (tPressed && !window.isKeyPressed(GLFW.GLFW_KEY_T)) tPressed = false;
        if (zPressed && !window.isKeyPressed(GLFW.GLFW_KEY_Y)) zPressed = false;
        if (vKeyPressed && !window.isKeyPressed(GLFW.GLFW_KEY_V)) vKeyPressed = false;
    }

    private void handleInputMovementStateChange(Vector3f position) {
        if (window.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT)) {
            if (movementState == WALKING) {
                camera.movePosition(0.0f, -0.25f, 0.0f);
                movementState = CROUCHING;
            }

        } else if (movementState == CROUCHING) {
            if (!collidesWithBlock(position.x, position.y + 0.25f, position.z, WALKING)) {
                camera.movePosition(0.0f, 0.25f, 0.0f);
                movementState = WALKING;
            } else if (!collidesWithBlock(position.x, position.y, position.z, WALKING))
                movementState = WALKING;
        }

        if (window.isKeyPressed(GLFW.GLFW_KEY_CAPS_LOCK)) {
            if (movementState == WALKING)
                camera.movePosition(0.0f, -1.25f, 0.0f);
            else if (movementState == CROUCHING)
                camera.movePosition(0.0f, -1.0f, 0.0f);
            movementState = CRAWLING;

        } else if (movementState == CRAWLING) {
            if (!collidesWithBlock(position.x, position.y + 1.25f, position.z, WALKING)) {
                camera.movePosition(0.0f, 1.25f, 0.0f);
                movementState = WALKING;
            } else if (!collidesWithBlock(position.x, position.y + 1.0f, position.z, CROUCHING)) {
                camera.movePosition(0.0f, 1.0f, 0.0f);
                movementState = CROUCHING;
            } else if (!collidesWithBlock(position.x, position.y, position.z, WALKING))
                movementState = WALKING;
            else if (!collidesWithBlock(position.x, position.y, position.z, CROUCHING))
                movementState = CROUCHING;
        }

        if (movementState == SWIMMING && !window.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL))
            movementState = CRAWLING;
        else if (movementState == SWIMMING && !collidesWithBlock(position.x, position.y, position.z, SWIMMING, WATER))
            movementState = CRAWLING;
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

    private void applyGravity(float passedTime) {
        this.velocity.y = clampVelocity(this.velocity.y - GRAVITY_ACCELERATION * passedTime, MAX_FALL_SPEED);
    }

    private void moveCameraHandleCollisions(float x, float y, float z) {
        Vector3f position = new Vector3f(camera.getPosition());
        Vector3f oldPosition = new Vector3f(position);
        position.add(x, y, z);

        int movementState = getMovementState();

        boolean xFirst = collidesWithBlock(position.x, oldPosition.y, oldPosition.z, movementState);
        boolean zFirst = collidesWithBlock(oldPosition.x, oldPosition.y, position.z, movementState);
        boolean xAndZ = collidesWithBlock(position.x, oldPosition.y, position.z, movementState);
        float requiredStepHeight = getRequiredStepHeight(position.x, position.y, position.z, movementState);
        boolean canAutoStep = isGrounded && !isFling && movementState != SWIMMING && requiredStepHeight <= MAX_STEP_HEIGHT;

        if ((xFirst || xAndZ) && (zFirst || xAndZ) && canAutoStep && !collidesWithBlock(position.x, oldPosition.y + requiredStepHeight, position.z, movementState)) {
            position.y += requiredStepHeight;
            oldPosition.y += requiredStepHeight;
        } else if ((xFirst || xAndZ) && (zFirst || xAndZ)) {
            if (xFirst && xAndZ) {
                position.x = oldPosition.x;
                setVelocityX(0.0f);
            } else {
                position.z = oldPosition.z;
                setVelocityZ(0.0f);
            }

            if (zFirst && xAndZ) {
                position.z = oldPosition.z;
                setVelocityZ(0.0f);
            } else {
                position.x = oldPosition.x;
                setVelocityX(0.0f);
            }

            if (!(xFirst && xAndZ) && !(zFirst && xAndZ))
                if (Math.abs(x) > Math.abs(z))
                    position.x += x;
                else
                    position.z += z;
        }

        if (collidesWithBlock(position.x, position.y, position.z, movementState)) {
            position.y = oldPosition.y;
            setGrounded(y < 0.0f);
            setVelocityY(0.0f);
            if (y < 0.0f)
                isFling = false;
        } else if ((movementState == CROUCHING || movementState == CRAWLING) && isGrounded() && y < 0.0f && collidesWithBlock(oldPosition.x, position.y, oldPosition.z, movementState)) {
            boolean onEdgeX = !collidesWithBlock(position.x, position.y - 0.0625f, oldPosition.z, movementState);
            boolean onEdgeZ = !collidesWithBlock(oldPosition.x, position.y - 0.0625f, position.z, movementState);

            if (onEdgeX) {
                position.x = oldPosition.x;
                position.y = oldPosition.y;
                setVelocityX(0.0f);
                setVelocityY(0.0f);
            }
            if (onEdgeZ) {
                position.z = oldPosition.z;
                position.y = oldPosition.y;
                setVelocityZ(0.0f);
                setVelocityY(0.0f);
            }
        }
        if (movementState == SWIMMING && y > 0.0f && !collidesWithBlock(position.x, position.y, position.z, SWIMMING, WATER)) {
            position.y = oldPosition.y;
            setVelocityY(0.0f);
        }

        if (collidesWithBlock(position.x, position.y, position.z, movementState)) {
            position.x = oldPosition.x;
            position.y = oldPosition.y;
            position.z = oldPosition.z;
            setVelocityX(0.0f);
            setVelocityY(0.0f);
            setVelocityZ(0.0f);
        }

        if (position.y != oldPosition.y)
            setGrounded(false);

        if (Utils.floor(oldPosition.x) >> CHUNK_SIZE_BITS != Utils.floor(position.x) >> CHUNK_SIZE_BITS)
            GameLogic.loadUnloadChunks();

        else if (Utils.floor(oldPosition.y) >> CHUNK_SIZE_BITS != Utils.floor(position.y) >> CHUNK_SIZE_BITS)
            GameLogic.loadUnloadChunks();

        else if (Utils.floor(oldPosition.z) >> CHUNK_SIZE_BITS != Utils.floor(position.z) >> CHUNK_SIZE_BITS)
            GameLogic.loadUnloadChunks();

        camera.setPosition(position.x, position.y, position.z);
    }

    public boolean collidesWithBlock(float x, float y, float z, int movementState) {
        if (isNoClip())
            return false;

        final float minX = x - HALF_PLAYER_WIDTH;
        final float maxX = x + HALF_PLAYER_WIDTH;
        final float minY = y - PLAYER_FEET_OFFSETS[movementState];
        final float maxY = y + PLAYER_HEAD_OFFSET;
        final float minZ = z - HALF_PLAYER_WIDTH;
        final float maxZ = z + HALF_PLAYER_WIDTH;

        for (int blockX = Utils.floor(minX), maxBlockX = Utils.floor(maxX); blockX <= maxBlockX; blockX++)
            for (int blockY = Utils.floor(minY), maxBlockY = Utils.floor(maxY); blockY <= maxBlockY; blockY++)
                for (int blockZ = Utils.floor(minZ), maxBlockZ = Utils.floor(maxZ); blockZ <= maxBlockZ; blockZ++) {

                    byte block = Chunk.getBlockInWorld(blockX, blockY, blockZ);

                    if (Block.playerIntersectsBlock(minX, maxX, minY, maxY, minZ, maxZ, blockX, blockY, blockZ, block, this))
                        return true;
                }
        return false;
    }

    public boolean collidesWithBlock(float x, float y, float z, int movementState, byte block) {
        if (isNoClip())
            return false;

        final float minX = x - HALF_PLAYER_WIDTH;
        final float maxX = x + HALF_PLAYER_WIDTH;
        final float minY = y - PLAYER_FEET_OFFSETS[movementState];
        final float maxY = y + PLAYER_HEAD_OFFSET;
        final float minZ = z - HALF_PLAYER_WIDTH;
        final float maxZ = z + HALF_PLAYER_WIDTH;

        for (int blockX = Utils.floor(minX), maxBlockX = Utils.floor(maxX); blockX <= maxBlockX; blockX++)
            for (int blockY = Utils.floor(minY), maxBlockY = Utils.floor(maxY); blockY <= maxBlockY; blockY++)
                for (int blockZ = Utils.floor(minZ), maxBlockZ = Utils.floor(maxZ); blockZ <= maxBlockZ; blockZ++)
                    if (Chunk.getBlockInWorld(blockX, blockY, blockZ) == block)
                        return true;
        return false;
    }

    public float getRequiredStepHeight(float x, float y, float z, int movementState) {
        final float minX = x - HALF_PLAYER_WIDTH;
        final float maxX = x + HALF_PLAYER_WIDTH;
        final float minY = y - PLAYER_FEET_OFFSETS[movementState];
        final float maxY = y + PLAYER_HEAD_OFFSET;
        final float minZ = z - HALF_PLAYER_WIDTH;
        final float maxZ = z + HALF_PLAYER_WIDTH;

        float requiredStepHeight = 0.0f;

        for (int blockX = Utils.floor(minX), maxBlockX = Utils.floor(maxX); blockX <= maxBlockX; blockX++)
            for (int blockY = Utils.floor(minY), maxBlockY = Utils.floor(maxY); blockY <= maxBlockY; blockY++)
                for (int blockZ = Utils.floor(minZ), maxBlockZ = Utils.floor(maxZ); blockZ <= maxBlockZ; blockZ++) {

                    float thisBlockStepHeight = 0.0f;
                    byte block = Chunk.getBlockInWorld(blockX, blockY, blockZ);

                    if (Block.playerIntersectsBlock(minX, maxX, minY, maxY, minZ, maxZ, blockX, blockY, blockZ, block, this))
                        thisBlockStepHeight = Block.getSubY(block, TOP, 0) * 0.0625f + blockY + 1 - minY;

                    requiredStepHeight = Math.max(requiredStepHeight, thisBlockStepHeight);
                }

        return requiredStepHeight;
    }

    private Vector3f getTarget(int action, Vector3f cameraDirection) {
        final int placing = 1;

        Vector3f cameraPosition = camera.getPosition();     //cameraPosition
        float interval = REACH / REACH_ACCURACY;
        int i = 0;
        byte block = OUT_OF_WORLD, previousBlock = OUT_OF_WORLD;

        for (; i < REACH_ACCURACY; i++) {
            previousBlock = block;

            float x = cameraPosition.x + i * interval * cameraDirection.x;
            float y = cameraPosition.y + i * interval * cameraDirection.y;
            float z = cameraPosition.z + i * interval * cameraDirection.z;

            block = Chunk.getBlockInWorld(Utils.floor(x), Utils.floor(y), Utils.floor(z));
            if (Block.intersectsBlock(x, y, z, block)) break;
        }
        if (Block.getBlockType(block) == AIR_TYPE || block == OUT_OF_WORLD || Block.getBlockType(block) == WATER_TYPE) return null;

        if (action == placing) {
            i--;
            if (previousBlock == block) {
                while (previousBlock == block && i >= 0) {
                    float x = cameraPosition.x + i * interval * cameraDirection.x;
                    float y = cameraPosition.y + i * interval * cameraDirection.y;
                    float z = cameraPosition.z + i * interval * cameraDirection.z;

                    block = Chunk.getBlockInWorld((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
                    i--;
                }
                int blockType = Block.getBlockType(block);
                if (blockType != AIR_TYPE && blockType != WATER_TYPE)
                    return null;
            } else if (Block.getBlockType(previousBlock) != AIR_TYPE && Block.getBlockType(previousBlock) != WATER_TYPE) return null;
        }
        float x = cameraPosition.x + i * interval * cameraDirection.x;
        float y = cameraPosition.y + i * interval * cameraDirection.y;
        float z = cameraPosition.z + i * interval * cameraDirection.z;
        Vector3f target = new Vector3f(x, y, z);

        final float minX = cameraPosition.x - HALF_PLAYER_WIDTH;
        final float maxX = cameraPosition.x + HALF_PLAYER_WIDTH;
        final float minY = cameraPosition.y - PLAYER_FEET_OFFSETS[movementState];
        final float maxY = cameraPosition.y + PLAYER_HEAD_OFFSET;
        final float minZ = cameraPosition.z - HALF_PLAYER_WIDTH;
        final float maxZ = cameraPosition.z + HALF_PLAYER_WIDTH;

        byte toPlaceBlock = Block.getToPlaceBlock(hotBars[selectedHotBar][selectedHotBarSlot], camera.getPrimaryDirection(), camera.getPrimaryXZDirection(), target);

        if (action == placing && Block.playerIntersectsBlock(minX, maxX, minY, maxY, minZ, maxZ, Utils.floor(target.x), Utils.floor(target.y), Utils.floor(target.z), toPlaceBlock, this))
            return null;

        return target;
    }

    public void render() {
        Vector3f cameraPosition = camera.getPosition();
        final int chunkX = Utils.floor(cameraPosition.x) >> CHUNK_SIZE_BITS;
        final int chunkY = Utils.floor(cameraPosition.y) >> CHUNK_SIZE_BITS;
        final int chunkZ = Utils.floor(cameraPosition.z) >> CHUNK_SIZE_BITS;

        renderChunkColumn(chunkX, chunkY, chunkZ);
        for (int ring = 1; ring <= RENDER_DISTANCE_XZ + 2; ring++) {
            for (int x = -ring; x < ring; x++) renderChunkColumn(x + chunkX, chunkY, ring + chunkZ);
            for (int z = ring; z > -ring; z--) renderChunkColumn(ring + chunkX, chunkY, z + chunkZ);
            for (int x = ring; x > -ring; x--) renderChunkColumn(x + chunkX, chunkY, -ring + chunkZ);
            for (int z = -ring; z < ring; z++) renderChunkColumn(-ring + chunkX, chunkY, z + chunkZ);
        }

        for (GUIElement GUIElement : GUIElements)
            renderer.processGUIElement(GUIElement);

        for (GUIElement GUIElement : hotBarElements)
            renderer.processGUIElement(GUIElement);
    }

    private void renderChunkColumn(int x, int cameraY, int z) {
        for (int y = RENDER_DISTANCE_Y + 2; y >= -RENDER_DISTANCE_Y - 2; y--) {
            Chunk chunk = Chunk.getChunk(x, y + cameraY, z);
            if (chunk == null)
                continue;
            if (chunk.getModel() != null)
                renderer.processModel(chunk.getModel());
            if (chunk.getTransparentModel() != null)
                renderer.processTransparentModel(chunk.getTransparentModel());
        }
    }

    private void updateHotBarElements() {
        for (GUIElement element : hotBarElements) {
            if (element == null)
                continue;
            ObjectLoader.removeVAO(element.getVao());
            ObjectLoader.removeVBO(element.getVbo1());
            ObjectLoader.removeVBO(element.getVbo2());
        }
        hotBarElements.clear();

        for (int i = 0; i < hotBars[selectedHotBar].length; i++) {
            byte block = hotBars[selectedHotBar][i];
            int textureIndexFront = Block.getTextureIndex(block, FRONT) - 1;
            int textureIndexTop = Block.getTextureIndex(block, TOP) - 1;
            int textureIndexRight = Block.getTextureIndex(block, RIGHT) - 1;
            float[] textureCoordinates = getTextureCoordinates(textureIndexFront, textureIndexTop, textureIndexRight, block);
            GUIElement element = ObjectLoader.loadGUIElement(GameLogic.getHotBarElementVertices(i, block), textureCoordinates);
            element.setTexture(atlas);
            hotBarElements.add(element);
        }
    }

    private static float[] getTextureCoordinates(int textureIndexFront, int textureIndexTop, int textureIndexRight, byte block) {
        if (block == AIR)
            return new float[]{};

        final int textureFrontX = textureIndexFront & 15;
        final int textureFrontY = (textureIndexFront >> 4) & 15;
        final float upperFrontX = (textureFrontX + Block.getSubU(block, FRONT, 0) * 0.0625f) * 0.0625f;
        final float lowerFrontX = (textureFrontX + 1 + Block.getSubU(block, FRONT, 1) * 0.0625f) * 0.0625f;
        final float upperFrontY = (textureFrontY + Block.getSubV(block, FRONT, 1) * 0.0625f) * 0.0625f;
        final float lowerFrontY = (textureFrontY + 1 + Block.getSubV(block, FRONT, 2) * 0.0625f) * 0.0625f;

        final int textureTopX = textureIndexTop & 15;
        final int textureTopY = (textureIndexTop >> 4) & 15;
        final float upperTopX = (textureTopX + Block.getSubU(block, TOP, 0) * 0.0625f) * 0.0625f;
        final float lowerTopX = (textureTopX + 1 + Block.getSubU(block, TOP, 1) * 0.0625f) * 0.0625f;
        final float upperTopY = (textureTopY + Block.getSubV(block, TOP, 1) * 0.0625f) * 0.0625f;
        final float lowerTopY = (textureTopY + 1 + Block.getSubV(block, TOP, 2) * 0.0625f) * 0.0625f;

        final int textureRightX = textureIndexRight & 15;
        final int textureRightY = (textureIndexRight >> 4) & 15;
        final float upperRightX = (textureRightX + Block.getSubU(block, RIGHT, 0) * 0.0625f) * 0.0625f;
        final float lowerRightX = (textureRightX + 1 + Block.getSubU(block, RIGHT, 1) * 0.0625f) * 0.0625f;
        final float upperRightY = (textureRightY + Block.getSubV(block, RIGHT, 1) * 0.0625f) * 0.0625f;
        final float lowerRightY = (textureRightY + 1 + Block.getSubV(block, RIGHT, 2) * 0.0625f) * 0.0625f;

        return new float[]{
                lowerFrontX, lowerFrontY,
                lowerFrontX, upperFrontY,
                upperFrontX, lowerFrontY,

                lowerFrontX, upperFrontY,
                upperFrontX, upperFrontY,
                upperFrontX, lowerFrontY,

                lowerTopX, lowerTopY,
                lowerTopX, upperTopY,
                upperTopX, lowerTopY,

                lowerTopX, upperTopY,
                upperTopX, upperTopY,
                upperTopX, lowerTopY,

                lowerRightX, lowerRightY,
                lowerRightX, upperRightY,
                upperRightX, lowerRightY,

                lowerRightX, upperRightY,
                upperRightX, upperRightY,
                upperRightX, lowerRightY};
    }

    public RenderManager getRenderer() {
        return renderer;
    }

    public Camera getCamera() {
        return camera;
    }

    public boolean isNoClip() {
        return noClip;
    }

    public int getMovementState() {
        return movementState;
    }

    public float clampVelocity(float velocity, float lowHigh) {
        return Math.max(-lowHigh, Math.min(velocity, lowHigh));
    }

    public void setVelocityX(float x) {
        velocity.x = x;
    }

    public void setVelocityY(float y) {
        velocity.y = y;
    }

    public void setVelocityZ(float z) {
        velocity.z = z;
    }

    public void setGrounded(boolean grounded) {
        isGrounded = grounded;
    }

    public boolean isGrounded() {
        return isGrounded;
    }
}
