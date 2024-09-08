package com.MBEv2.core.entity;

import com.MBEv2.core.*;

import static com.MBEv2.core.utils.Constants.*;
import static com.MBEv2.core.utils.Settings.*;

import com.MBEv2.core.entity.entities.TNT_Entity;
import com.MBEv2.core.utils.Transformation;
import com.MBEv2.core.utils.Utils;
import com.MBEv2.test.GameLogic;
import com.MBEv2.test.Launcher;
import org.joml.*;
import org.lwjgl.glfw.GLFW;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;

public class Player {
    //Movement
    public static final float AIR_FRICTION = 0.91f;
    public static final float FALL_FRICTION = 0.98f;
    public static final float WATER_FRICTION = 0.4f;
    public static final float GROUND_FRICTION = 0.546f;
    public static final float FLY_FRICTION = 0.8f;

    public static final float MOVEMENT_SPEED = 0.098f;
    public static final float IN_AIR_SPEED = 0.2f;
    public static final float[] MOVEMENT_STATE_SPEED = new float[]{MOVEMENT_SPEED, 0.0294f, MOVEMENT_SPEED * 0.25f};
    public static final float FLY_SPEED = 0.06f;

    public static final float JUMP_STRENGTH = 0.42f;
    public static final float SWIM_STRENGTH = 0.26f;
    public static final float GRAVITY_ACCELERATION = 0.08f;
    public static final float MAX_STEP_HEIGHT = 0.6f;

    //Movement state indices
    public static final int WALKING = 0;
    public static final int CROUCHING = 1;
    public static final int CRAWLING = 2;
    public static final int SWIMMING = 3;

    //Collision box size
    public static final float HALF_PLAYER_WIDTH = 0.23f;
    public static final float PLAYER_HEAD_OFFSET = 0.08f;
    public static final float[] PLAYER_FEET_OFFSETS = new float[]{1.65f, 1.4f, 0.4f, 0.4f};


    private final RenderManager renderer;
    private final WindowManager window;
    private final Camera camera;
    private final MouseInput mouseInput;

    private final Vector3f velocity;
    private long[] visibleChunks;

    private final ArrayList<GUIElement> GUIElements = new ArrayList<>();
    private final ArrayList<GUIElement> hotBarElements = new ArrayList<>();
    private final ArrayList<GUIElement> inventoryElements = new ArrayList<>();
    private GUIElement hotBarSelectionIndicator;
    private final Texture atlas;

    private long spaceButtonPressTime;
    private boolean spaceButtonPressed = false;
    private long useButtonPressTime = -1, destroyButtonPressTime = -1;
    private boolean useButtonWasJustPressed = false, destroyButtonWasJustPressed = false;

    private float inventoryScroll;

    //Debug
    private boolean debugScreenOpen;
    private boolean noClip;
    private boolean usingOcclusionCulling = true;
    private final Vector3i pos1, pos2;

    private boolean isFling;
    private boolean inInventory;
    private short[] hotBar = new short[9];
    private int selectedHotBarSlot = 0;

    private int movementState = WALKING;
    private boolean isGrounded = false;

    public Player() throws Exception {
        atlas = new Texture(ObjectLoader.loadTexture("textures/atlas256.png"));
        window = Launcher.getWindow();
        renderer = new RenderManager(this);
        camera = new Camera();
        mouseInput = new MouseInput(this);

        velocity = new Vector3f(0, 0, 0);
        camera.setPosition(0.5f, WorldGeneration.getHeightMapValue(0, 0) + 3, 0.5f);
        pos1 = new Vector3i();
        pos2 = new Vector3i();
        visibleChunks = new long[(RENDERED_WORLD_WIDTH * RENDERED_WORLD_HEIGHT * RENDERED_WORLD_WIDTH >> 6) + 1];
    }

    public void init() throws Exception {
        Texture skyBoxTexture1 = new Texture(ObjectLoader.loadTexture("textures/706c5e1da58f47ad6e18145165caf55d.png"));
        Texture skyBoxTexture2 = new Texture(ObjectLoader.loadTexture("textures/82984-skybox-blue-atmosphere-sky-space-hd-image-free-png.png"));
        SkyBox skyBox = ObjectLoader.loadSkyBox(SKY_BOX_VERTICES, SKY_BOX_TEXTURE_COORDINATES, SKY_BOX_INDICES, camera.getPosition());
        skyBox.setTexture(skyBoxTexture1, skyBoxTexture2);
        renderer.processSkyBox(skyBox);

        loadGUIElements();

        GUIElement inventoryOverlay = ObjectLoader.loadGUIElement(OVERLAY_VERTICES, GUI_ELEMENT_TEXTURE_COORDINATES, new Vector2f(0.0f, 0.0f));
        inventoryOverlay.setTexture(new Texture(ObjectLoader.loadTexture("textures/InventoryOverlay.png")));
        renderer.setInventoryOverlay(inventoryOverlay);

        updateHotBarElements();
        generateInventoryElements(inventoryElements);

        mouseInput.init();

        GLFW.glfwSetKeyCallback(window.getWindow(), (window, key, scancode, action, mods) -> {
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) {
                if (!isInInventory()) GLFW.glfwSetWindowShouldClose(window, true);
                else toggleInventory();
                return;
            }
            handleNonMovementInputs(key | IS_KEYBOARD_BUTTON, action);
        });
    }

    public void loadGUIElements() throws Exception {
        GUIElement crossHair = ObjectLoader.loadGUIElement(GameLogic.getCrossHairVertices(), GUI_ELEMENT_TEXTURE_COORDINATES, new Vector2f(0.0f, 0.0f));
        crossHair.setTexture(new Texture(ObjectLoader.loadTexture("textures/CrossHair.png")));
        GUIElements.addFirst(crossHair);

        GUIElement hotBarGUIElement = ObjectLoader.loadGUIElement(GameLogic.getHotBarVertices(), GUI_ELEMENT_TEXTURE_COORDINATES, new Vector2f(0.0f, 0.0f));
        hotBarGUIElement.setTexture(new Texture(ObjectLoader.loadTexture("textures/HotBar.png")));
        GUIElements.add(1, hotBarGUIElement);

        hotBarSelectionIndicator = ObjectLoader.loadGUIElement(GameLogic.getHotBarSelectionIndicatorVertices(), GUI_ELEMENT_TEXTURE_COORDINATES, new Vector2f(0, 0));
        hotBarSelectionIndicator.setTexture(new Texture(ObjectLoader.loadTexture("textures/HotBarSelectionIndicator.png")));
        setSelectedHotBarSlot(0);
    }

    public void reloadGUIElements() throws Exception {
        GUIElement crossHair = GUIElements.removeFirst();
        ObjectLoader.removeVAO(crossHair.getVao());
        ObjectLoader.removeVBO(crossHair.getVbo1());
        ObjectLoader.removeVBO(crossHair.getVbo2());

        GUIElement hotBar = GUIElements.removeFirst();
        ObjectLoader.removeVAO(hotBar.getVao());
        ObjectLoader.removeVBO(hotBar.getVbo1());
        ObjectLoader.removeVBO(hotBar.getVbo2());

        ObjectLoader.removeVAO(hotBarSelectionIndicator.getVao());
        ObjectLoader.removeVBO(hotBarSelectionIndicator.getVbo1());
        ObjectLoader.removeVBO(hotBarSelectionIndicator.getVbo2());

        loadGUIElements();
    }

    public void update(float passedTime) {
        moveCameraHandleCollisions(velocity.x * passedTime, velocity.y * passedTime, velocity.z * passedTime);

        mouseInput.input();
        Vector2f rotVec = mouseInput.getDisplayVec();
        if (!inInventory) camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY);
    }

    public void input() {
        Vector3f position = camera.getPosition();
        boolean isInWater = collidesWithBlock(position.x, position.y, position.z, movementState, WATER);
        Vector3f velocity = new Vector3f(0.0f, 0.0f, 0.0f);

        handleInputMovementStateChange(position);
        handleIsFlyingChange();

        if (isFling) handleInputFling(velocity);
        else if (isInWater) handleInputSwimming(velocity);
        else handleInputWalking(velocity);
        if (inInventory) handleInventoryHotkeys();

        normalizeVelocity(velocity);
        addVelocityChange(velocity);

        handleDestroyUsePickBlockInput();
    }

    private void handleInputMovementStateChange(Vector3f position) {
        if (inInventory) return;

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
        else if (movementState == SWIMMING && !collidesWithBlock(position.x, position.y, position.z, SWIMMING, WATER))
            movementState = CRAWLING;
    }

    private void handleIsFlyingChange() {
        long currentTime = System.nanoTime();
        if (window.isKeyPressed(JUMP_BUTTON)) {
            if (!spaceButtonPressed) {
                spaceButtonPressed = true;
                if (currentTime - spaceButtonPressTime < 300_000_000) isFling = !isFling;
                spaceButtonPressTime = currentTime;
            }
        } else if (spaceButtonPressed) spaceButtonPressed = false;

    }

    private void handleInputFling(Vector3f velocity) {
        if (inInventory) {
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

        if (inInventory) {
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

            if (window.isKeyPressed(MOVE_LEFT_BUTTON)) {
                float acceleration = SWIM_STRENGTH * accelerationModifier;
                velocity.x -= acceleration;
            }
            if (window.isKeyPressed(MOVE_RIGHT_BUTTON)) {
                float acceleration = SWIM_STRENGTH * accelerationModifier;
                velocity.x += acceleration;
            }
            applyGravity();
        }

        long currentTime = System.nanoTime();
        if (window.isKeyPressed(JUMP_BUTTON)) if (isGrounded) {
            this.velocity.y = JUMP_STRENGTH;
            isGrounded = false;
            spaceButtonPressTime = currentTime;
        } else velocity.y += SWIM_STRENGTH * 0.65f;

        if (window.isKeyPressed(SNEAK_BUTTON)) velocity.y -= SWIM_STRENGTH * 0.65f;
    }

    private void handleInputWalking(Vector3f velocity) {
        if (inInventory) {
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
        }
    }

    private void handleInventoryHotkeys() {
        if (window.isKeyPressed(HOT_BAR_SLOT_1)) hotBar[0] = getHoveredOverInventoryBlock();
        else if (window.isKeyPressed(HOT_BAR_SLOT_2)) hotBar[1] = getHoveredOverInventoryBlock();
        else if (window.isKeyPressed(HOT_BAR_SLOT_3)) hotBar[2] = getHoveredOverInventoryBlock();
        else if (window.isKeyPressed(HOT_BAR_SLOT_4)) hotBar[3] = getHoveredOverInventoryBlock();
        else if (window.isKeyPressed(HOT_BAR_SLOT_5)) hotBar[4] = getHoveredOverInventoryBlock();
        else if (window.isKeyPressed(HOT_BAR_SLOT_6)) hotBar[5] = getHoveredOverInventoryBlock();
        else if (window.isKeyPressed(HOT_BAR_SLOT_7)) hotBar[6] = getHoveredOverInventoryBlock();
        else if (window.isKeyPressed(HOT_BAR_SLOT_8)) hotBar[7] = getHoveredOverInventoryBlock();
        else if (window.isKeyPressed(HOT_BAR_SLOT_9)) hotBar[8] = getHoveredOverInventoryBlock();
        updateHotBarElements();
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

    private void handleDestroyUsePickBlockInput() {
        if (inInventory) return;
        boolean useButtonWasJustPressed = this.useButtonWasJustPressed;
        boolean destroyButtonWasJustPressed = this.destroyButtonWasJustPressed;
        this.useButtonWasJustPressed = false;
        this.destroyButtonWasJustPressed = false;
        long currentTime = System.nanoTime();

        handleDestroy(currentTime, destroyButtonWasJustPressed);

        handleUse(currentTime, useButtonWasJustPressed);

        handlePickBlock();
    }

    private void handleDestroy(long currentTime, boolean destroyButtonWasJustPressed) {
        if ((destroyButtonPressTime == -1 || currentTime - destroyButtonPressTime <= 300_000_000) && !destroyButtonWasJustPressed)
            return;
        Target target = Target.getTarget(camera.getPosition(), camera.getDirection());
        if (target != null) GameLogic.placeBlock(AIR, target.position().x, target.position().y, target.position().z);
    }

    private void handleUse(long currentTime, boolean useButtonWasJustPressed) {
        if (((useButtonPressTime == -1 || currentTime - useButtonPressTime <= 300_000_000) && !useButtonWasJustPressed))
            return;

        Vector3f cameraDirection = camera.getDirection();
        Vector3f cameraPosition = camera.getPosition();
        Target target = Target.getTarget(cameraPosition, cameraDirection);
        if (target == null) return;

        short selectedBlock = hotBar[selectedHotBarSlot];

        if ((Block.getBlockProperties(target.block()) & INTERACTABLE) != 0) if (interactWithBlock(target)) return;

        if (hotBar[selectedHotBarSlot] == AIR) return;
        short toPlaceBlock;
        short inventoryBlock = Block.getInInventoryBlockEquivalent(target.block());
        if (window.isKeyPressed(SPRINT_BUTTON) && (inventoryBlock & BLOCK_TYPE_MASK) == (selectedBlock & BLOCK_TYPE_MASK)) {
            toPlaceBlock = (short) (selectedBlock & BASE_BLOCK_MASK | target.block() & BLOCK_TYPE_MASK);
        } else
            toPlaceBlock = Block.getToPlaceBlock(selectedBlock, camera.getPrimaryDirection(cameraDirection), camera.getPrimaryXZDirection(cameraDirection), target);

        final float minX = cameraPosition.x - HALF_PLAYER_WIDTH;
        final float maxX = cameraPosition.x + HALF_PLAYER_WIDTH;
        final float minY = cameraPosition.y - PLAYER_FEET_OFFSETS[movementState];
        final float maxY = cameraPosition.y + PLAYER_HEAD_OFFSET;
        final float minZ = cameraPosition.z - HALF_PLAYER_WIDTH;
        final float maxZ = cameraPosition.z + HALF_PLAYER_WIDTH;
        Vector3i position = target.position();
        int[] normal = Block.NORMALS[target.side()];
        int x = position.x + normal[0];
        int y = position.y + normal[1];
        int z = position.z + normal[2];
        if (hasCollision() && Block.entityIntersectsBlock(minX, maxX, minY, maxY, minZ, maxZ, x, y, z, toPlaceBlock))
            return;

        if ((Block.getBlockProperties(Chunk.getBlockInWorld(x, y, z)) & REPLACEABLE) != 0)
            GameLogic.placeBlock(toPlaceBlock, x, y, z);
    }

    private boolean interactWithBlock(Target target) {
        if (window.isKeyPressed(SNEAK_BUTTON)) return false;
        if (target.block() == hotBar[selectedHotBarSlot]) return false;

        if (target.block() == CRAFTING_TABLE) {
            System.out.println("You interacted with a crafting table");
            return true;
        } else if (target.block() == TNT) {
            TNT_Entity.spawnTNTEntity(target.position(), 0);
            return true;
        }
        return false;
    }

    private void handlePickBlock() {
        if (!window.isKeyPressed(PICK_BLOCK_BUTTON)) return;

        Target target = Target.getTarget(camera.getPosition(), camera.getDirection());
        if (target == null) return;

        short block = Chunk.getBlockInWorld(target.position().x, target.position().y, target.position().z);
        short inInventoryBlock = Block.getInInventoryBlockEquivalent(block);

        boolean hasPlacedBlock = false;
        for (int hotBarSlot = 0; hotBarSlot < hotBar.length; hotBarSlot++) {
            if (hotBar[hotBarSlot] != inInventoryBlock) continue;
            hasPlacedBlock = true;
            setSelectedHotBarSlot(hotBarSlot);
            break;
        }
        if (!hasPlacedBlock) for (int hotBarSlot = 0; hotBarSlot < hotBar.length; hotBarSlot++) {
            if (hotBar[hotBarSlot] != AIR) continue;
            hotBar[hotBarSlot] = inInventoryBlock;
            setSelectedHotBarSlot(hotBarSlot);
            hasPlacedBlock = true;
            break;
        }
        if (!hasPlacedBlock) hotBar[selectedHotBarSlot] = inInventoryBlock;

        updateHotBarElements();
    }

    public void handleNonMovementInputs(int button, int action) {
        if (button == DESTROY_BUTTON) if (action == GLFW.GLFW_PRESS) {
            destroyButtonPressTime = System.nanoTime();
            destroyButtonWasJustPressed = true;
        } else {
            destroyButtonPressTime = -1;
        }
        else if (button == USE_BUTTON) if (action == GLFW.GLFW_PRESS) {
            useButtonPressTime = System.nanoTime();
            useButtonWasJustPressed = true;
        } else {
            useButtonPressTime = -1;
        }
        else if (button == HOT_BAR_SLOT_1 && action == GLFW.GLFW_PRESS) setSelectedHotBarSlot(0);
        else if (button == HOT_BAR_SLOT_2 && action == GLFW.GLFW_PRESS) setSelectedHotBarSlot(1);
        else if (button == HOT_BAR_SLOT_3 && action == GLFW.GLFW_PRESS) setSelectedHotBarSlot(2);
        else if (button == HOT_BAR_SLOT_4 && action == GLFW.GLFW_PRESS) setSelectedHotBarSlot(3);
        else if (button == HOT_BAR_SLOT_5 && action == GLFW.GLFW_PRESS) setSelectedHotBarSlot(4);
        else if (button == HOT_BAR_SLOT_6 && action == GLFW.GLFW_PRESS) setSelectedHotBarSlot(5);
        else if (button == HOT_BAR_SLOT_7 && action == GLFW.GLFW_PRESS) setSelectedHotBarSlot(6);
        else if (button == HOT_BAR_SLOT_8 && action == GLFW.GLFW_PRESS) setSelectedHotBarSlot(7);
        else if (button == HOT_BAR_SLOT_9 && action == GLFW.GLFW_PRESS) setSelectedHotBarSlot(8);

        else if (button == OPEN_INVENTORY_BUTTON && action == GLFW.GLFW_PRESS) toggleInventory();
        else if (button == OPEN_DEBUG_MENU_BUTTON && action == GLFW.GLFW_PRESS) debugScreenOpen = !debugScreenOpen;

        else if (button == TOGGLE_NO_CLIP_BUTTON && action == GLFW.GLFW_PRESS) noClip = !noClip;
        else if (button == TOGGLE_X_RAY_BUTTON && action == GLFW.GLFW_PRESS) renderer.setXRay(!renderer.isxRay());
        else if (button == SET_POSITION_1_BUTTON && action == GLFW.GLFW_PRESS) {
            Vector3f cameraPosition = camera.getPosition();
            pos1.x = Utils.floor(cameraPosition.x);
            pos1.y = Utils.floor(cameraPosition.y);
            pos1.z = Utils.floor(cameraPosition.z);
        } else if (button == SET_POSITION_2_BUTTON && action == GLFW.GLFW_PRESS) {
            Vector3f cameraPosition = camera.getPosition();
            pos2.x = Utils.floor(cameraPosition.x);
            pos2.y = Utils.floor(cameraPosition.y);
            pos2.z = Utils.floor(cameraPosition.z);

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
                        short block = Chunk.getBlockInWorld(minX + j, minY + i, minZ + k);
                        System.out.print(block + " ,");
                    }
                    System.out.println("},");
                }
                System.out.print("},");
            }
            System.out.print("}");
        } else if (button == USE_OCCLUSION_CULLING_BUTTON && action == GLFW.GLFW_PRESS) {
            usingOcclusionCulling = !usingOcclusionCulling;
            if (!usingOcclusionCulling) Arrays.fill(visibleChunks, -1);
        } else if (button == RELOAD_SETTINGS_BUTTON && action == GLFW.GLFW_PRESS) {
            try {
                FileManager.loadSettings(false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setSelectedHotBarSlot(int slot) {
        selectedHotBarSlot = slot;
        hotBarSelectionIndicator.setPosition(new Vector2f((slot - 4) * 40 * GUI_SIZE / Launcher.getWindow().getWidth(), 0.0f));
    }

    private short getHoveredOverInventoryBlock() {
        double[] xPos = new double[1];
        double[] yPos = new double[1];
        GLFW.glfwGetCursorPos(window.getWindow(), xPos, yPos);
        double x = xPos[0] / window.getWidth() - 0.5;
        double y = yPos[0] / window.getHeight() - 0.5;

        y += inventoryScroll;

        if (y < -0.5f + GUI_SIZE * 0.04f) {
            if (y < -0.5) return 0;
            if (x < 0.5f - (TO_PLACE_NON_STANDARD_BLOCKS.length + 1) * 0.02f * GUI_SIZE) return 0;
            int value = (int) ((0.5 - 0.01 * GUI_SIZE - x) / (0.02 * GUI_SIZE));
            value = Math.min(TO_PLACE_NON_STANDARD_BLOCKS.length - 1, Math.max(value, 0));
            return TO_PLACE_NON_STANDARD_BLOCKS[value];
        }
        if (x < 0.5f - (TO_PLACE_BLOCK_TYPES.length + 1) * 0.02f * GUI_SIZE) return 0;
        if (y > -0.5f + GUI_SIZE * 0.04f * (AMOUNT_OF_TO_PLACE_STANDARD_BLOCKS)) return 0;

        int valueX = (int) ((0.5 - 0.01 * GUI_SIZE - x) / (0.02 * GUI_SIZE));
        valueX = Math.min(TO_PLACE_BLOCK_TYPES.length - 1, Math.max(valueX, 0));

        int valueY = (int) ((y - 0.005 * GUI_SIZE + 0.5) / (0.04 * GUI_SIZE));
        valueY = Math.min(AMOUNT_OF_TO_PLACE_STANDARD_BLOCKS - 1, Math.max(valueY, 1));

        return (short) (valueY << BLOCK_TYPE_BITS | TO_PLACE_BLOCK_TYPES[valueX]);
    }

    private void applyGravity() {
        velocity.y -= GRAVITY_ACCELERATION;
    }

    private void toggleInventory() {
        inInventory = !inInventory;
        GLFW.glfwSetInputMode(window.getWindow(), GLFW.GLFW_CURSOR, inInventory ? GLFW.GLFW_CURSOR_NORMAL : GLFW.GLFW_CURSOR_DISABLED);
    }

    private void moveCameraHandleCollisions(float x, float y, float z) {
        Vector3f position = new Vector3f(camera.getPosition());
        Vector3f oldPosition = new Vector3f(position);
        position.add(x, y, z);

        boolean xFirst = collidesWithBlock(position.x, oldPosition.y, oldPosition.z, movementState);
        boolean zFirst = collidesWithBlock(oldPosition.x, oldPosition.y, position.z, movementState);
        boolean xAndZ = collidesWithBlock(position.x, oldPosition.y, position.z, movementState);
        float requiredStepHeight = getRequiredStepHeight(position.x, position.y, position.z, movementState);
        boolean canAutoStep = (isGrounded && !isFling || movementState == SWIMMING) && requiredStepHeight <= MAX_STEP_HEIGHT;

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
            if (y < 0.0f) isFling = false;
        } else if ((movementState == CROUCHING || movementState == CRAWLING) && isGrounded && y <= 0.0f && collidesWithBlock(oldPosition.x, position.y - 0.0625f, oldPosition.z, movementState)) {
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
        if (movementState == SWIMMING && y > 0.0f && !collidesWithBlock(position.x, position.y, position.z, SWIMMING, WATER)) {
            position.y = oldPosition.y;
            velocity.y = 0.0f;
        }

        if (collidesWithBlock(position.x, position.y, position.z, movementState)) {
            position.x = oldPosition.x;
            position.y = oldPosition.y;
            position.z = oldPosition.z;
            velocity.set(0.0f, 0.0f, 0.0f);
        }

        if (position.y != oldPosition.y) isGrounded = false;

        if (Utils.floor(oldPosition.x) >> CHUNK_SIZE_BITS != Utils.floor(position.x) >> CHUNK_SIZE_BITS)
            GameLogic.restartGenerator(position.x > oldPosition.x ? FRONT : BACK);

        else if (Utils.floor(oldPosition.y) >> CHUNK_SIZE_BITS != Utils.floor(position.y) >> CHUNK_SIZE_BITS)
            GameLogic.restartGenerator(position.y > oldPosition.y ? TOP : BOTTOM);

        else if (Utils.floor(oldPosition.z) >> CHUNK_SIZE_BITS != Utils.floor(position.z) >> CHUNK_SIZE_BITS)
            GameLogic.restartGenerator(position.z > oldPosition.z ? RIGHT : LEFT);

        camera.setPosition(position.x, position.y, position.z);
    }

    public boolean collidesWithBlock(float x, float y, float z, int movementState) {
        if (noClip) return false;

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

                    if (hasCollision() && Block.entityIntersectsBlock(minX, maxX, minY, maxY, minZ, maxZ, blockX, blockY, blockZ, block))
                        return true;
                }
        return false;
    }

    public boolean collidesWithBlock(float x, float y, float z, int movementState, short block) {
        if (noClip) return false;

        final float minX = x - HALF_PLAYER_WIDTH;
        final float maxX = x + HALF_PLAYER_WIDTH;
        final float minY = y - PLAYER_FEET_OFFSETS[movementState];
        final float maxY = y + PLAYER_HEAD_OFFSET;
        final float minZ = z - HALF_PLAYER_WIDTH;
        final float maxZ = z + HALF_PLAYER_WIDTH;

        for (int blockX = Utils.floor(minX), maxBlockX = Utils.floor(maxX); blockX <= maxBlockX; blockX++)
            for (int blockY = Utils.floor(minY), maxBlockY = Utils.floor(maxY); blockY <= maxBlockY; blockY++)
                for (int blockZ = Utils.floor(minZ), maxBlockZ = Utils.floor(maxZ); blockZ <= maxBlockZ; blockZ++)
                    if (Chunk.getBlockInWorld(blockX, blockY, blockZ) == block) return true;
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

    public void render() {
        Vector3f cameraPosition = camera.getPosition();
        final int chunkX = Utils.floor(cameraPosition.x) >> CHUNK_SIZE_BITS;
        final int chunkY = Utils.floor(cameraPosition.y) >> CHUNK_SIZE_BITS;
        final int chunkZ = Utils.floor(cameraPosition.z) >> CHUNK_SIZE_BITS;

        Matrix4f projectionMatrix = window.getProjectionMatrix();
        Matrix4f viewMatrix = Transformation.getViewMatrix(camera);
        Matrix4f projectionViewMatrix = new Matrix4f();
        projectionMatrix.mul(viewMatrix, projectionViewMatrix);
        FrustumIntersection frustumIntersection = new FrustumIntersection(projectionViewMatrix);

        if (usingOcclusionCulling) calculateVisibleChunks(chunkX, chunkY, chunkZ);

        renderChunkColumn(chunkX, chunkZ, chunkX, chunkY, chunkZ, frustumIntersection);
        for (int ring = 1; ring <= RENDER_DISTANCE_XZ + 2; ring++) {
            for (int x = -ring; x < ring; x++)
                renderChunkColumn(x + chunkX, ring + chunkZ, chunkX, chunkY, chunkZ, frustumIntersection);
            for (int z = ring; z > -ring; z--)
                renderChunkColumn(ring + chunkX, z + chunkZ, chunkX, chunkY, chunkZ, frustumIntersection);
            for (int x = ring; x > -ring; x--)
                renderChunkColumn(x + chunkX, -ring + chunkZ, chunkX, chunkY, chunkZ, frustumIntersection);
            for (int z = -ring; z < ring; z++)
                renderChunkColumn(-ring + chunkX, z + chunkZ, chunkX, chunkY, chunkZ, frustumIntersection);
        }

        for (GUIElement GUIElement : GUIElements)
            renderer.processGUIElement(GUIElement);

        for (GUIElement GUIElement : hotBarElements)
            renderer.processGUIElement(GUIElement);

        renderer.processGUIElement(hotBarSelectionIndicator);

        renderer.setHeadUnderWater(Chunk.getBlockInWorld(Utils.floor(cameraPosition.x), Utils.floor(cameraPosition.y), Utils.floor(cameraPosition.z)) == WATER);

        if (inInventory) for (GUIElement element : inventoryElements) renderer.processGUIElement(element);
    }

    private void renderChunkColumn(int chunkX, int chunkZ, int cameraX, int cameraY, int cameraZ, FrustumIntersection frustumIntersection) {
        for (int chunkY = cameraY + RENDER_DISTANCE_Y + 2; chunkY >= cameraY - RENDER_DISTANCE_Y - 2; chunkY--) {
            Chunk chunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
            if (chunk == null) continue;
            int chunkIndex = chunk.getIndex();
            if ((visibleChunks[chunkIndex >> 6] & 1L << (chunkIndex & 63)) == 0) continue;

            Vector3f position = new Vector3f(chunk.getWorldCoordinate());
            int intersectionType = frustumIntersection.intersectAab(position, new Vector3f(position.x + CHUNK_SIZE, position.y + CHUNK_SIZE, position.z + CHUNK_SIZE));
            if (intersectionType != FrustumIntersection.INTERSECT && intersectionType != FrustumIntersection.INSIDE)
                continue;

            if (chunk.getWaterModel() != null) renderer.processWaterModel(chunk.getWaterModel());
            if (chunk.getFoliageModel() != null) renderer.processFoliageModel(chunk.getFoliageModel());

            if (chunk.getChunkX() >= cameraX && chunk.getModel(LEFT) != null)
                renderer.processModel(chunk.getModel(LEFT));
            if (chunk.getChunkX() <= cameraX && chunk.getModel(RIGHT) != null)
                renderer.processModel(chunk.getModel(RIGHT));

            if (chunk.getChunkY() >= cameraY && chunk.getModel(BOTTOM) != null)
                renderer.processModel(chunk.getModel(BOTTOM));
            if (chunk.getChunkY() <= cameraY && chunk.getModel(TOP) != null) renderer.processModel(chunk.getModel(TOP));

            if (chunk.getChunkZ() >= cameraZ && chunk.getModel(BACK) != null)
                renderer.processModel(chunk.getModel(BACK));
            if (chunk.getChunkZ() <= cameraZ && chunk.getModel(FRONT) != null)
                renderer.processModel(chunk.getModel(FRONT));
        }
    }

    private void calculateVisibleChunks(int chunkX, int chunkY, int chunkZ) {
        Arrays.fill(visibleChunks, 0);
        int chunkIndex = GameLogic.getChunkIndex(chunkX, chunkY, chunkZ);

        visibleChunks[chunkIndex >> 6] = visibleChunks[chunkIndex >> 6] | 1L << (chunkIndex & 63);

        fillVisibleChunks(chunkX, chunkY, chunkZ + 1, BACK, 1 << FRONT, 0);
        fillVisibleChunks(chunkX, chunkY, chunkZ - 1, FRONT, 1 << BACK, 0);

        fillVisibleChunks(chunkX, chunkY + 1, chunkZ, BOTTOM, 1 << TOP, 0);
        fillVisibleChunks(chunkX, chunkY - 1, chunkZ, TOP, 1 << BOTTOM, 0);

        fillVisibleChunks(chunkX + 1, chunkY, chunkZ, LEFT, 1 << RIGHT, 0);
        fillVisibleChunks(chunkX - 1, chunkY, chunkZ, RIGHT, 1 << LEFT, 0);
    }

    private void fillVisibleChunks(int chunkX, int chunkY, int chunkZ, int entrySide, int traveledDirections, int damper) {
        if (damper >= MAX_OCCLUSION_CULLING_DAMPER) return;
        int chunkIndex = GameLogic.getChunkIndex(chunkX, chunkY, chunkZ);
        Chunk chunk = Chunk.getChunk(chunkIndex);
        if (chunk == null) return;

        if ((visibleChunks[chunkIndex >> 6] & 1L << (chunkIndex & 63)) != 0) return;
        visibleChunks[chunkIndex >> 6] |= 1L << (chunkIndex & 63);
        damper += chunk.getOcclusionCullingDamper();

        if (chunk.readOcclusionCullingSidePair(entrySide, FRONT) && (traveledDirections & 1 << BACK) == 0)
            fillVisibleChunks(chunkX, chunkY, chunkZ + 1, BACK, traveledDirections | 1 << FRONT, damper);
        if (chunk.readOcclusionCullingSidePair(entrySide, BACK) && (traveledDirections & 1 << FRONT) == 0)
            fillVisibleChunks(chunkX, chunkY, chunkZ - 1, FRONT, traveledDirections | 1 << BACK, damper);

        if (chunk.readOcclusionCullingSidePair(entrySide, TOP) && (traveledDirections & 1 << BOTTOM) == 0)
            fillVisibleChunks(chunkX, chunkY + 1, chunkZ, BOTTOM, traveledDirections | 1 << TOP, damper);
        if (chunk.readOcclusionCullingSidePair(entrySide, BOTTOM) && (traveledDirections & 1 << TOP) == 0)
            fillVisibleChunks(chunkX, chunkY - 1, chunkZ, TOP, traveledDirections | 1 << BOTTOM, damper);

        if (chunk.readOcclusionCullingSidePair(entrySide, RIGHT) && (traveledDirections & 1 << LEFT) == 0)
            fillVisibleChunks(chunkX + 1, chunkY, chunkZ, LEFT, traveledDirections | 1 << RIGHT, damper);
        if (chunk.readOcclusionCullingSidePair(entrySide, LEFT) && (traveledDirections & 1 << RIGHT) == 0)
            fillVisibleChunks(chunkX - 1, chunkY, chunkZ, RIGHT, traveledDirections | 1 << LEFT, damper);
    }

    public void updateHotBarElements() {
        for (GUIElement element : hotBarElements) {
            if (element == null) continue;
            ObjectLoader.removeVAO(element.getVao());
            ObjectLoader.removeVBO(element.getVbo1());
            ObjectLoader.removeVBO(element.getVbo2());
        }
        hotBarElements.clear();

        int width = window.getWidth();
        int height = window.getHeight();

        for (int i = 0; i < hotBar.length; i++) {
            short block = hotBar[i];

            int textureIndexFront = Block.getTextureIndex(block, FRONT) - 1;
            int textureIndexTop = Block.getTextureIndex(block, TOP) - 1;
            int textureIndexLeft = Block.getTextureIndex(block, LEFT) - 1;
            float[] textureCoordinates = GameLogic.getBlockDisplayTextureCoordinates(textureIndexFront, textureIndexTop, textureIndexLeft, block);
            float xOffset = (40.0f * i - 165 + 4) * GUI_SIZE / width;
            float yOffset = -0.5f + 4.0f * GUI_SIZE / height;
            GUIElement element = ObjectLoader.loadGUIElement(GameLogic.getBlockDisplayVertices(block), textureCoordinates, new Vector2f(xOffset, yOffset));
            element.setTexture(atlas);
            hotBarElements.add(element);
        }
    }

    private void generateInventoryElements(ArrayList<GUIElement> elements) {
        for (int i = 0; i < TO_PLACE_NON_STANDARD_BLOCKS.length; i++) {
            short block = TO_PLACE_NON_STANDARD_BLOCKS[i];
            float[] vertices = GameLogic.getBlockDisplayVertices(block);

            int textureIndexFront = Block.getTextureIndex(block, FRONT) - 1;
            int textureIndexTop = Block.getTextureIndex(block, TOP) - 1;
            int textureIndexLeft = Block.getTextureIndex(block, LEFT) - 1;
            float[] textureCoordinates = GameLogic.getBlockDisplayTextureCoordinates(textureIndexFront, textureIndexTop, textureIndexLeft, block);
            GUIElement element = ObjectLoader.loadGUIElement(vertices, textureCoordinates, new Vector2f(0.5f - (i + 1) * 0.02f * GUI_SIZE, 0.5f - GUI_SIZE * 0.04f));
            element.setTexture(atlas);
            elements.add(element);
        }
        for (int baseBlock = 1; baseBlock < AMOUNT_OF_TO_PLACE_STANDARD_BLOCKS; baseBlock++) {
            for (int blockTypeIndex = 0; blockTypeIndex < TO_PLACE_BLOCK_TYPES.length; blockTypeIndex++) {
                int blockType = TO_PLACE_BLOCK_TYPES[blockTypeIndex];
                short block = (short) (baseBlock << BLOCK_TYPE_BITS | blockType);
                float[] vertices = GameLogic.getBlockDisplayVertices(block);

                int textureIndexFront = Block.getTextureIndex(block, FRONT) - 1;
                int textureIndexTop = Block.getTextureIndex(block, TOP) - 1;
                int textureIndexLeft = Block.getTextureIndex(block, LEFT) - 1;
                float[] textureCoordinates = GameLogic.getBlockDisplayTextureCoordinates(textureIndexFront, textureIndexTop, textureIndexLeft, block);
                GUIElement element = ObjectLoader.loadGUIElement(vertices, textureCoordinates, new Vector2f(0.5f - (blockTypeIndex + 1) * 0.02f * GUI_SIZE, 0.5f - GUI_SIZE * 0.04f * (1 + baseBlock)));
                element.setTexture(atlas);
                elements.add(element);
            }
        }
    }

    public RenderManager getRenderer() {
        return renderer;
    }

    public Camera getCamera() {
        return camera;
    }

    public boolean hasCollision() {
        return !noClip;
    }

    public boolean isInInventory() {
        return inInventory;
    }

    public ArrayList<GUIElement> getInventoryElements() {
        return inventoryElements;
    }

    public void addToInventoryScroll(float value) {
        inventoryScroll += value;
    }

    public boolean isDebugScreenOpen() {
        return debugScreenOpen;
    }

    public boolean isFling() {
        return isFling;
    }

    public int getSelectedHotBarSlot() {
        return selectedHotBarSlot;
    }

    public int getMovementState() {
        return movementState;
    }


    public void setMovementState(int movementState) {
        this.movementState = movementState;
    }

    public void setFling(boolean fling) {
        isFling = fling;
    }

    public short[] getHotBar() {
        return hotBar;
    }

    public void setHotBar(short[] hotBar) {
        this.hotBar = hotBar;
        updateHotBarElements();
    }

    public void setVisibleChunks(long[] visibleChunks) {
        this.visibleChunks = visibleChunks;
    }
}
