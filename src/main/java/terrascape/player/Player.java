package terrascape.player;

import terrascape.server.*;
import terrascape.dataStorage.Chunk;
import terrascape.dataStorage.FileManager;
import terrascape.dataStorage.Structure;
import terrascape.entity.GUIElement;
import terrascape.entity.SkyBox;
import terrascape.entity.Target;
import terrascape.entity.Texture;
import terrascape.entity.entities.Entity;
import terrascape.entity.entities.TNT_Entity;
import terrascape.generation.WorldGeneration;
import terrascape.utils.Transformation;
import terrascape.utils.Utils;
import terrascape.server.GameLogic;
import terrascape.server.Launcher;
import org.joml.*;
import org.lwjgl.glfw.GLFW;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import static terrascape.generation.WorldGeneration.WATER_LEVEL;
import static terrascape.utils.Constants.*;
import static terrascape.utils.Settings.*;

public class Player {

    public Player() throws Exception {
        atlas = new Texture(ObjectLoader.loadTexture("textures/atlas256.png"));
        window = Launcher.getWindow();
        sound = Launcher.getSound();
        renderer = new RenderManager(this);
        camera = new Camera();
        mouseInput = new MouseInput(this);

        velocity = new Vector3f(0, 0, 0);

        int spawnX = 0;
        int spawnZ = 0;

        for (int counter = 0; counter < 100 && WorldGeneration.getResultingHeight(Utils.floor(spawnX), Utils.floor(spawnZ)) < WATER_LEVEL; counter++) {
            spawnX = Utils.floor(Math.random() * SPAWN_RADIUS * 2 - SPAWN_RADIUS);
            spawnZ = Utils.floor(Math.random() * SPAWN_RADIUS * 2 - SPAWN_RADIUS);
        }

        camera.setPosition(spawnX + 0.5f, WorldGeneration.getResultingHeight(spawnX, spawnZ) + 3, spawnZ + 0.5f);

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

        mouseInput.init();

        GLFW.glfwSetKeyCallback(window.getWindow(), (long window, int key, int scancode, int action, int mods) -> {
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) {
                if (!isInInventory()) GLFW.glfwSetWindowShouldClose(window, true);
                else toggleInventory();
                return;
            }
            handleNonMovementInputs(key | IS_KEYBOARD_BUTTON, action);

//            if (key == GLFW.GLFW_KEY_H && action == GLFW.GLFW_PRESS) {
//                Vector3f pos = camera.getPosition();
//                Chunk chunk = Chunk.getChunk(Utils.floor(pos.x) >> CHUNK_SIZE_BITS, Utils.floor(pos.y) >> CHUNK_SIZE_BITS, Utils.floor(pos.z) >> CHUNK_SIZE_BITS);
//                if (chunk == null) return;
//                Arrays.fill(chunk.getBlocks(), AIR);
//
//                for (int chunkX = chunk.X - 1; chunkX <= chunk.X + 1; chunkX++)
//                    for (int chunkY = chunk.Y - 1; chunkY <= chunk.Y + 1; chunkY++)
//                        for (int chunkZ = chunk.Z - 1; chunkZ <= chunk.Z + 1; chunkZ++) {
//                            Chunk toMeshChunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
//                            if (toMeshChunk != null) toMeshChunk.setMeshed(false);
//                        }
//                GameLogic.restartGeneratorNow(NONE);
//            }

//            if (key == GLFW.GLFW_KEY_J && action == GLFW.GLFW_RELEASE) {
//                Structure structure = Structure.testStructure;
//                if (structure == null) return;
//                Vector3f pos = camera.getPosition();
//                Chunk chunk = Chunk.getChunk(Utils.floor(pos.x) >> CHUNK_SIZE_BITS, Utils.floor(pos.y) >> CHUNK_SIZE_BITS, Utils.floor(pos.z) >> CHUNK_SIZE_BITS);
//
//                int transform = (int) (Math.random() * 8.0);
//
//                for (int y = 0; y < structure.lengthY(); y++)
//                    for (int x = 0; x < structure.lengthX(); x++)
//                        for (int z = 0; z < structure.lengthZ(); z++) {
//                            chunk.placeBlock(x, y, z, structure.get(x, y, z, transform));
//                        }
//                chunk.setMeshed(false);
//                GameLogic.restartGenerator(NONE);
//            }

            if (key == GLFW.GLFW_KEY_B && action == GLFW.GLFW_PRESS) {
                renderer.setTime(0.0f);
            }
            if (key == GLFW.GLFW_KEY_N && action == GLFW.GLFW_PRESS) {
                renderer.setTime(0.5f);
            }
            if (key == GLFW.GLFW_KEY_M && action == GLFW.GLFW_PRESS) {
                renderer.setTime(1.0f);
            }
        });
    }

    public void loadGUIElements() throws Exception {
        GUIElement crossHair = ObjectLoader.loadGUIElement(GUIElement.getCrossHairVertices(), GUI_ELEMENT_TEXTURE_COORDINATES, new Vector2f(0.0f, 0.0f));
        crossHair.setTexture(new Texture(ObjectLoader.loadTexture("textures/CrossHair.png")));
        GUIElements.addFirst(crossHair);

        GUIElement hotBarGUIElement = ObjectLoader.loadGUIElement(GUIElement.getHotBarVertices(), GUI_ELEMENT_TEXTURE_COORDINATES, new Vector2f(0.0f, 0.0f));
        hotBarGUIElement.setTexture(new Texture(ObjectLoader.loadTexture("textures/HotBar.png")));
        GUIElements.add(1, hotBarGUIElement);

        hotBarSelectionIndicator = ObjectLoader.loadGUIElement(GUIElement.getHotBarSelectionIndicatorVertices(), GUI_ELEMENT_TEXTURE_COORDINATES, new Vector2f(0, 0));
        hotBarSelectionIndicator.setTexture(new Texture(ObjectLoader.loadTexture("textures/HotBarSelectionIndicator.png")));
        setSelectedHotBarSlot(0);

        GUIElement.generateInventoryElements(inventoryElements, atlas);
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

        for (GUIElement element : inventoryElements) {
            ObjectLoader.removeVAO(element.getVao());
            ObjectLoader.removeVBO(element.getVbo1());
            ObjectLoader.removeVBO(element.getVbo2());
        }
        inventoryElements.clear();
        inventoryScroll = 0.0f;

        loadGUIElements();
    }

    public void update(float passedTicks) {
        moveCameraHandleCollisions(velocity.x * passedTicks, velocity.y * passedTicks, velocity.z * passedTicks);

        mouseInput.input();
        Vector2f rotVec = mouseInput.getDisplayVec();
        if (!inInventory) camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY);

        sound.setListenerData(this);
    }

    public void updateGT(long tick) {
        renderer.incrementTime();
        playFootstepsSounds(tick);
    }

    private void playFootstepsSounds(long tick) {
        if (movementState == SWIMMING) {
            if (tick - lastFootstepTick < 15) return;
            Vector3f position = camera.getPosition();
            sound.playRandomSound(sound.swim, position.x, position.y, position.z, 0.0f, 0.0f, 0.0f, STEP_GAIN);
            lastFootstepTick = tick;
            return;
        }

        if (!isGrounded || (!(Math.abs(velocity.x) > 0.001f) && !(Math.abs(velocity.z) > 0.001f))) return;
        if (movementState == CROUCHING || movementState == CRAWLING) return;
        if (movementState == WALKING) {
            if (window.isKeyPressed(SPRINT_BUTTON)) {
                if (tick - lastFootstepTick < 5) return;
            } else if (tick - lastFootstepTick < 10) return;
        }
        lastFootstepTick = tick;

        Vector3f position = camera.getPosition();
        float height = PLAYER_FEET_OFFSETS[movementState];
        short standingBlock = getStandingBlock();
        sound.playRandomSound(Block.getFootstepsSound(standingBlock), position.x, position.y - height, position.z, 0.0f, 0.0f, 0.0f, STEP_GAIN);
    }

    public void input() {
        Vector3f position = camera.getPosition();
        boolean isInWater = collidesWithWater(position.x, position.y, position.z, movementState);
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

        if (isInWater != touchingWater)
            sound.playRandomSound(sound.splash, position.x, position.y, position.z, 0.0f, 0.0f, 0.0f, MISCELLANEOUS_GAIN);
        touchingWater = isInWater;
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
        else if (movementState == SWIMMING && !collidesWithWater(position.x, position.y, position.z, SWIMMING))
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

            Vector3f position = camera.getPosition();
            sound.playRandomSound(Block.getFootstepsSound(getStandingBlock()), position.x, position.y, position.z, this.velocity.x, this.velocity.y, this.velocity.z, STEP_GAIN * 1.5f);
        }
    }

    private void handleInventoryHotkeys() {
        if (window.isKeyPressed(HOT_BAR_SLOT_1)) hotBar[0] = GUIElement.getHoveredOverBlock(inventoryScroll);
        else if (window.isKeyPressed(HOT_BAR_SLOT_2)) hotBar[1] = GUIElement.getHoveredOverBlock(inventoryScroll);
        else if (window.isKeyPressed(HOT_BAR_SLOT_3)) hotBar[2] = GUIElement.getHoveredOverBlock(inventoryScroll);
        else if (window.isKeyPressed(HOT_BAR_SLOT_4)) hotBar[3] = GUIElement.getHoveredOverBlock(inventoryScroll);
        else if (window.isKeyPressed(HOT_BAR_SLOT_5)) hotBar[4] = GUIElement.getHoveredOverBlock(inventoryScroll);
        else if (window.isKeyPressed(HOT_BAR_SLOT_6)) hotBar[5] = GUIElement.getHoveredOverBlock(inventoryScroll);
        else if (window.isKeyPressed(HOT_BAR_SLOT_7)) hotBar[6] = GUIElement.getHoveredOverBlock(inventoryScroll);
        else if (window.isKeyPressed(HOT_BAR_SLOT_8)) hotBar[7] = GUIElement.getHoveredOverBlock(inventoryScroll);
        else if (window.isKeyPressed(HOT_BAR_SLOT_9)) hotBar[8] = GUIElement.getHoveredOverBlock(inventoryScroll);
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
        if (target != null)
            GameLogic.placeBlock(AIR, target.position().x, target.position().y, target.position().z, true);
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
        if (window.isKeyPressed(SPRINT_BUTTON) && Block.getBlockType(inventoryBlock) == Block.getBlockType(selectedBlock)
                && (selectedBlock & 0xFFFF) >= STANDARD_BLOCKS_THRESHOLD) {

            if ((inventoryBlock & BASE_BLOCK_MASK) == (selectedBlock & BASE_BLOCK_MASK))
                toPlaceBlock = (short) (target.block() & ~WATER_LOGGED_MASK);
            else toPlaceBlock = (short) (selectedBlock & BASE_BLOCK_MASK | target.block() & BLOCK_TYPE_MASK);

        } else
            toPlaceBlock = Block.getToPlaceBlock(selectedBlock, camera.getPrimaryDirection(cameraDirection), camera.getPrimaryXZDirection(cameraDirection), target);

        final float minX = cameraPosition.x - HALF_PLAYER_WIDTH;
        final float maxX = cameraPosition.x + HALF_PLAYER_WIDTH;
        final float minY = cameraPosition.y - PLAYER_FEET_OFFSETS[movementState];
        final float maxY = cameraPosition.y + PLAYER_HEAD_OFFSET;
        final float minZ = cameraPosition.z - HALF_PLAYER_WIDTH;
        final float maxZ = cameraPosition.z + HALF_PLAYER_WIDTH;
        Vector3i position = target.position();
        int x = position.x;
        int y = position.y;
        int z = position.z;
        boolean isWaterLogging = false;

        if ((Block.getBlockProperties(Chunk.getBlockInWorld(x, y, z)) & REPLACEABLE) == 0) {

            boolean blockCanBeWaterLogged = (target.block() & 0xFFFF) > STANDARD_BLOCKS_THRESHOLD && (target.block() & BLOCK_TYPE_MASK) != FULL_BLOCK;

            if (!blockCanBeWaterLogged || selectedBlock != WATER || window.isKeyPressed(SNEAK_BUTTON)) {
                byte[] normal = Block.NORMALS[target.side()];
                x = position.x + normal[0];
                y = position.y + normal[1];
                z = position.z + normal[2];

                if (selectedBlock == WATER) {
                    short block = Chunk.getBlockInWorld(x, y, z);
                    isWaterLogging = (block & 0xFFFF) > STANDARD_BLOCKS_THRESHOLD && (block & BLOCK_TYPE_MASK) != FULL_BLOCK;
                    if (isWaterLogging) toPlaceBlock = (short) (block | WATER_LOGGED_MASK);
                }

            } else {
                isWaterLogging = true;
                toPlaceBlock = (short) (target.block() | WATER_LOGGED_MASK);
            }
        }
        if (hasCollision() && Block.playerIntersectsBlock(minX, maxX, minY, maxY, minZ, maxZ, x, y, z, toPlaceBlock)
        || Block.entityIntersectsBlock(x, y, z, toPlaceBlock))
            return;

        if (isWaterLogging || (Block.getBlockProperties(Chunk.getBlockInWorld(x, y, z)) & REPLACEABLE) != 0)
            GameLogic.placeBlock(toPlaceBlock, x, y, z, true);
    }

    private boolean interactWithBlock(Target target) {
        if (window.isKeyPressed(SNEAK_BUTTON)) return false;
        short block = target.block();

        if (block == CRAFTING_TABLE) {
            if (hotBar[selectedHotBarSlot] == CRAFTING_TABLE) return false;
            System.out.println("You interacted with a crafting table");
            return true;
        }
        if (block == TNT) {
            if (hotBar[selectedHotBarSlot] == TNT) return false;
            TNT_Entity.spawnTNTEntity(target.position(), 80);
            sound.playSound(sound.fuse, target.position().x, target.position().y, target.position().z, 0.0f, 0.0f, 0.0f, MISCELLANEOUS_GAIN);
            return true;
        }
        if (block == NORTH_FURNACE || block == WEST_FURNACE || block == SOUTH_FURNACE || block == EAST_FURNACE) {
            if (hotBar[selectedHotBarSlot] == NORTH_FURNACE) return false;
            System.out.println("You interacted with a furnace");
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
            if (hotBarSlot != selectedHotBarSlot) setSelectedHotBarSlot(hotBarSlot);
            break;
        }
        if (!hasPlacedBlock && hotBar[selectedHotBarSlot] != AIR)
            for (int hotBarSlot = 0; hotBarSlot < hotBar.length; hotBarSlot++) {
                if (hotBar[hotBarSlot] != AIR) continue;
                hotBar[hotBarSlot] = inInventoryBlock;
                if (hotBarSlot != selectedHotBarSlot) setSelectedHotBarSlot(hotBarSlot);
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
        else if (button == ZOOM_BUTTON && action == GLFW.GLFW_PRESS) {
            zoomModifier = 0.25f;
            window.updateProjectionMatrix(FOV * zoomModifier);
        } else if (button == ZOOM_BUTTON && action == GLFW.GLFW_RELEASE) window.updateProjectionMatrix(FOV);
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

            short[] blocks = new short[x * y * z];

            for (int i = 0; i < y; i++)
                for (int j = 0; j < x; j++)
                    for (int k = 0; k < z; k++) {
                        short block = Chunk.getBlockInWorld(minX + j, minY + i, minZ + k);
                        int index = i * x * z + j * z + k;
                        blocks[index] = block;
                    }

            Structure structure = new Structure(blocks, x, y, z);
            Structure.testStructure = structure;
            try {
                FileManager.saveStructure(structure, "Structures/RENAME_ME");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else if (button == USE_OCCLUSION_CULLING_BUTTON && action == GLFW.GLFW_PRESS) {
            usingOcclusionCulling = !usingOcclusionCulling;
            if (!usingOcclusionCulling) Arrays.fill(visibleChunks, -1L);
        } else if (button == RELOAD_SETTINGS_BUTTON && action == GLFW.GLFW_PRESS) {
            try {
                FileManager.loadSettings(false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setSelectedHotBarSlot(int slot) {
        if (selectedHotBarSlot == slot) return;

        selectedHotBarSlot = slot;
        hotBarSelectionIndicator.setPosition(new Vector2f((slot - 4) * 40 * GUI_SIZE / Launcher.getWindow().getWidth(), 0.0f));
        Vector3f position = camera.getPosition();
        sound.playRandomSound(Block.getFootstepsSound(hotBar[selectedHotBarSlot]), position.x, position.y, position.z, velocity.x, velocity.y, velocity.z, INVENTORY_GAIN);
    }

    public short getStandingBlock() {
        Vector3f position = camera.getPosition();
        float height = PLAYER_FEET_OFFSETS[movementState];

        int standingBlockX = Utils.floor(position.x);
        int standingBlockY = Utils.floor(position.y - height - 0.0625f);
        int standingBlockZ = Utils.floor(position.z);

        return Chunk.getBlockInWorld(standingBlockX, standingBlockY, standingBlockZ);
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
        if (movementState == SWIMMING && y > 0.0f && !collidesWithWater(position.x, position.y, position.z, SWIMMING)) {
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
            GameLogic.restartGeneratorNow(position.x > oldPosition.x ? NORTH : SOUTH);

        else if (Utils.floor(oldPosition.y) >> CHUNK_SIZE_BITS != Utils.floor(position.y) >> CHUNK_SIZE_BITS)
            GameLogic.restartGeneratorNow(position.y > oldPosition.y ? TOP : BOTTOM);

        else if (Utils.floor(oldPosition.z) >> CHUNK_SIZE_BITS != Utils.floor(position.z) >> CHUNK_SIZE_BITS)
            GameLogic.restartGeneratorNow(position.z > oldPosition.z ? WEST : EAST);

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

                    if (hasCollision() && Block.playerIntersectsBlock(minX, maxX, minY, maxY, minZ, maxZ, blockX, blockY, blockZ, block))
                        return true;
                }
        return false;
    }

    public boolean collidesWithWater(float x, float y, float z, int movementState) {
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
                    if (Block.isWaterLogged(Chunk.getBlockInWorld(blockX, blockY, blockZ))) return true;
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

        boolean headUnderWater = Block.isWaterLogged(Chunk.getBlockInWorld(Utils.floor(cameraPosition.x), Utils.floor(cameraPosition.y), Utils.floor(cameraPosition.z)));
        if (headUnderWater && !this.headUnderWater)
            sound.playRandomSound(sound.submerge, cameraPosition.x, cameraPosition.y, cameraPosition.z, 0.0f, 0.0f, 0.0f, MISCELLANEOUS_GAIN);
        else if (this.headUnderWater && !headUnderWater)
            sound.playRandomSound(sound.splash, cameraPosition.x, cameraPosition.y, cameraPosition.z, 0.0f, 0.0f, 0.0f, MISCELLANEOUS_GAIN);
        this.headUnderWater = headUnderWater;
        renderer.setHeadUnderWater(headUnderWater);

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

            if (chunk.X >= cameraX && chunk.getModel(EAST) != null) renderer.processModel(chunk.getModel(EAST));
            if (chunk.X <= cameraX && chunk.getModel(WEST) != null) renderer.processModel(chunk.getModel(WEST));

            if (chunk.Y >= cameraY && chunk.getModel(BOTTOM) != null) renderer.processModel(chunk.getModel(BOTTOM));
            if (chunk.Y <= cameraY && chunk.getModel(TOP) != null) renderer.processModel(chunk.getModel(TOP));

            if (chunk.Z >= cameraZ && chunk.getModel(SOUTH) != null) renderer.processModel(chunk.getModel(SOUTH));
            if (chunk.Z <= cameraZ && chunk.getModel(NORTH) != null) renderer.processModel(chunk.getModel(NORTH));

            for (LinkedList<Entity> entityCluster : chunk.getEntityClusters())
                for (Entity entity : entityCluster) renderer.processEntity(entity);
        }
    }

    private void calculateVisibleChunks(int chunkX, int chunkY, int chunkZ) {
        Arrays.fill(visibleChunks, 0);
        int chunkIndex = GameLogic.getChunkIndex(chunkX, chunkY, chunkZ);

        visibleChunks[chunkIndex >> 6] = visibleChunks[chunkIndex >> 6] | 1L << (chunkIndex & 63);

        fillVisibleChunks(chunkX, chunkY, chunkZ + 1, SOUTH, 1 << NORTH, 0);
        fillVisibleChunks(chunkX, chunkY, chunkZ - 1, NORTH, 1 << SOUTH, 0);

        fillVisibleChunks(chunkX, chunkY + 1, chunkZ, BOTTOM, 1 << TOP, 0);
        fillVisibleChunks(chunkX, chunkY - 1, chunkZ, TOP, 1 << BOTTOM, 0);

        fillVisibleChunks(chunkX + 1, chunkY, chunkZ, EAST, 1 << WEST, 0);
        fillVisibleChunks(chunkX - 1, chunkY, chunkZ, WEST, 1 << EAST, 0);
    }

    private void fillVisibleChunks(int chunkX, int chunkY, int chunkZ, int entrySide, int traveledDirections, int damper) {
        if (damper >= MAX_OCCLUSION_CULLING_DAMPER) return;
        int chunkIndex = GameLogic.getChunkIndex(chunkX, chunkY, chunkZ);

        short occlusionCullingData = Chunk.getOcclusionCullingData(chunkIndex);

        if ((visibleChunks[chunkIndex >> 6] & 1L << (chunkIndex & 63)) != 0) return;
        visibleChunks[chunkIndex >> 6] |= 1L << (chunkIndex & 63);
        damper += Chunk.getOcclusionCullingDamper(occlusionCullingData);

        if ((traveledDirections & 1 << SOUTH) == 0 && Chunk.readOcclusionCullingSidePair(entrySide, NORTH, occlusionCullingData))
            fillVisibleChunks(chunkX, chunkY, chunkZ + 1, SOUTH, traveledDirections | 1 << NORTH, damper);
        if ((traveledDirections & 1 << NORTH) == 0 && Chunk.readOcclusionCullingSidePair(entrySide, SOUTH, occlusionCullingData))
            fillVisibleChunks(chunkX, chunkY, chunkZ - 1, NORTH, traveledDirections | 1 << SOUTH, damper);

        if ((traveledDirections & 1 << BOTTOM) == 0 && Chunk.readOcclusionCullingSidePair(entrySide, TOP, occlusionCullingData))
            fillVisibleChunks(chunkX, chunkY + 1, chunkZ, BOTTOM, traveledDirections | 1 << TOP, damper);
        if ((traveledDirections & 1 << TOP) == 0 && Chunk.readOcclusionCullingSidePair(entrySide, BOTTOM, occlusionCullingData))
            fillVisibleChunks(chunkX, chunkY - 1, chunkZ, TOP, traveledDirections | 1 << BOTTOM, damper);

        if ((traveledDirections & 1 << EAST) == 0 && Chunk.readOcclusionCullingSidePair(entrySide, WEST, occlusionCullingData))
            fillVisibleChunks(chunkX + 1, chunkY, chunkZ, EAST, traveledDirections | 1 << WEST, damper);
        if ((traveledDirections & 1 << WEST) == 0 && Chunk.readOcclusionCullingSidePair(entrySide, EAST, occlusionCullingData))
            fillVisibleChunks(chunkX - 1, chunkY, chunkZ, WEST, traveledDirections | 1 << EAST, damper);
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
            float xOffset = (40.0f * i - 165 + 4) * GUI_SIZE / width;
            float yOffset = -0.5f + 4.0f * GUI_SIZE / height;
            GUIElement element;

            short block = hotBar[i];
            if (Block.getBlockType(block) == FLOWER_TYPE) {
                int textureIndex = Block.getTextureIndex(block, 0);
                float[] textureCoordinates = GUIElement.getFlatDisplayTextureCoordinates(textureIndex);
                element = ObjectLoader.loadGUIElement(GUIElement.getFlatDisplayVertices(), textureCoordinates, new Vector2f(xOffset, yOffset));
            } else {
                int textureIndexFront = Block.getTextureIndex(block, NORTH);
                int textureIndexTop = Block.getTextureIndex(block, TOP);
                int textureIndexLeft = Block.getTextureIndex(block, EAST);
                float[] textureCoordinates = GUIElement.getBlockDisplayTextureCoordinates(textureIndexFront, textureIndexTop, textureIndexLeft, block);
                element = ObjectLoader.loadGUIElement(GUIElement.getBlockDisplayVertices(block), textureCoordinates, new Vector2f(xOffset, yOffset));
            }

            element.setTexture(atlas);
            hotBarElements.add(element);
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

    public void updateInventoryScroll(float value) {
        float maxScroll = GUI_SIZE * 0.04f * (1 + AMOUNT_OF_TO_PLACE_STANDARD_BLOCKS) - 1.0f;

        if (inventoryScroll + value < 0.0f) value = -inventoryScroll;
        if (inventoryScroll + value > maxScroll) value = maxScroll - inventoryScroll;

        inventoryScroll += value;

        for (GUIElement element : inventoryElements) {
            element.getPosition().add(0.0f, value);
        }
    }

    public boolean isDebugScreenOpen() {
        return debugScreenOpen;
    }

    public boolean isFling() {
        return isFling;
    }

    public boolean isGrounded() {
        return isGrounded;
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

    public Vector3f getVelocity() {
        return velocity;
    }

    public float getInventoryScroll() {
        return inventoryScroll;
    }

    public void cleanUp() {
        renderer.cleanUp();
    }

    public void changeZoomModifier(float multiplier) {
        zoomModifier = Math.min(1.0f, zoomModifier * multiplier);
        window.updateProjectionMatrix(FOV * zoomModifier);
    }

    // Movement
    private static final float FLY_FRICTION = 0.8f;

    private static final float MOVEMENT_SPEED = 0.098f;
    private static final float IN_AIR_SPEED = 0.2f;
    private static final float[] MOVEMENT_STATE_SPEED = new float[]{MOVEMENT_SPEED, 0.0294f, MOVEMENT_SPEED * 0.25f};
    private static final float FLY_SPEED = 0.06f;

    private static final float JUMP_STRENGTH = 0.42f;
    private static final float SWIM_STRENGTH = 0.26f;
    private static final float MAX_STEP_HEIGHT = 0.6f;

    // Movement state indices
    private static final int WALKING = 0;
    private static final int CROUCHING = 1;
    private static final int CRAWLING = 2;
    private static final int SWIMMING = 3;

    // Collision box size
    private static final float HALF_PLAYER_WIDTH = 0.23f;
    private static final float PLAYER_HEAD_OFFSET = 0.08f;
    private static final float[] PLAYER_FEET_OFFSETS = new float[]{1.65f, 1.4f, 0.4f, 0.4f};


    private final RenderManager renderer;
    private final WindowManager window;
    private final SoundManager sound;
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

    private float inventoryScroll = 0;
    private float zoomModifier = 1.0f;
    private int movementState = WALKING;
    private boolean isGrounded = false;
    private boolean isFling;
    private boolean inInventory;
    private boolean headUnderWater, touchingWater;
    private short[] hotBar = new short[9];
    private int selectedHotBarSlot = -1; // No idea but when it's 0 there is a bug but anything else works
    private long lastFootstepTick = 0;

    // Debug
    private boolean debugScreenOpen;
    private boolean noClip;
    private boolean usingOcclusionCulling = true;
    private final Vector3i pos1, pos2;
}
