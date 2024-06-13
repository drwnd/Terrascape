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
    private boolean tPressed = false, zPressed = false;
    private final Vector3i pos1, pos2;

    private final byte[][] hotBars = {
            {GRASS, DIRT, STONE, MUD, SNOW, SAND, STONE_BRICKS, COBBLESTONE, GLASS},
            {OAK_LOG, SPRUCE_LOG, DARK_OAK_LOG, STRIPPED_OAK_LOG, STRIPPED_SPRUCE_LOG, STRIPPED_DARK_OAK_LOG, AIR, AIR, AIR},
            {OAK_PLANKS, SPRUCE_PLANKS, DARK_OAK_PLANKS, OAK_LEAVES, SPRUCE_LEAVES, DARK_OAK_LEAVES, OAK_PLANKS_SLAB, SPRUCE_PLANKS_SLAB, DARK_OAK_PLANKS_SLAB},
            {ANDESITE, WATER, COBBLESTONE_SLAB, STONE_BRICK_SLAB, COBBLESTONE_POST, STONE_BRICK_POST, COBBLESTONE_WALL, STONE_BRICK_WALL, AIR}};
    private int selectedHotBar = 0;
    private int selectedHotBarSlot = 0;

    private int movementState = WALKING;
    private boolean isGrounded = false;

    public Player(Texture atlas) {
        this.atlas = atlas;
        window = Launcher.getWindow();
        renderer = new RenderManager();
        camera = new Camera(this);
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
        camera.movePosition(velocity.x, velocity.y, velocity.z);

        Vector2f rotVec = mouseInput.getDisplayVec();
        camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY);

        Vector3f cP = camera.getPosition();
        renderer.setHeadUnderWater(Chunk.getBlockInWorld(Utils.floor(cP.x), Utils.floor(cP.y), Utils.floor(cP.z)) == WATER);

        long currentTime = System.nanoTime();

        if (leftButtonPressTime != -1) if (currentTime - leftButtonPressTime > 300_000_000 || leftButtonWasJustPressed)
            GameLogic.placeBlock(AIR, getTarget(0, camera.getDirection()));

        if (rightButtonPressTime != -1)
            if (currentTime - rightButtonPressTime > 300_000_000 || rightButtonWasJustPressed)
                if (hotBars[selectedHotBar][selectedHotBarSlot] != AIR) {

                    Vector3f cD = camera.getDirection();
                    byte toPlaceBlock = hotBars[selectedHotBar][selectedHotBarSlot];

                    GameLogic.placeBlock(Block.getToPlaceBlock(toPlaceBlock, camera.getPrimaryDirection(cD)), getTarget(1, cD));
                }
    }

    public void input(float passedTime) {
        mouseInput.input();
        rightButtonPressTime = mouseInput.getRightButtonPressTime();
        leftButtonPressTime = mouseInput.getLeftButtonPressTime();
        rightButtonWasJustPressed = mouseInput.wasRightButtonJustPressed();
        leftButtonWasJustPressed = mouseInput.wasLeftButtonJustPressed();

        float movementSpeedModifier = 1.0f;
        Vector3f position = camera.getPosition();
        boolean isInWater = collidesWithBlock(position.x, position.y, position.z, movementState, WATER);
        Vector2f rotation = camera.getRotation();
        Vector3f velocity = new Vector3f(0.0f, 0.0f, 0.0f);
        float accelerationModifier = isInWater ? SWIM_STRENGTH : isGrounded ? 1.0f : IN_AIR_SPEED;

        if (movementState == CROUCHING)
            movementSpeedModifier *= 0.5f;
        else if (movementState == CRAWLING)
            movementSpeedModifier *= 0.35f;
        else if (window.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL))
            movementSpeedModifier *= 1.5f;

        if (window.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT)) {
            if (movementState == WALKING) {
                camera.movePositionNoChecks(0.0f, -0.25f, 0.0f);
                movementState = CROUCHING;
            }
            if (isInWater) {
                velocity.y -= SWIM_STRENGTH * passedTime * movementSpeedModifier * 7.5f;
            }

        } else if (movementState == CROUCHING) {
            if (!collidesWithBlock(position.x, position.y, position.z, WALKING))
                movementState = WALKING;
            else if (!collidesWithBlock(position.x, position.y + 0.25f, position.z, WALKING)) {
                camera.movePositionNoChecks(0.0f, 0.25f, 0.0f);
                movementState = WALKING;
            }
        }

        if (window.isKeyPressed(GLFW.GLFW_KEY_CAPS_LOCK)) {
            if (movementState == WALKING)
                camera.movePositionNoChecks(0.0f, -1.25f, 0.0f);
            else if (movementState == CROUCHING)
                camera.movePositionNoChecks(0.0f, -1.0f, 0.0f);
            movementState = CRAWLING;

        } else if (movementState == CRAWLING) {
            if (!collidesWithBlock(position.x, position.y, position.z, WALKING))
                movementState = WALKING;
            else if (!collidesWithBlock(position.x, position.y, position.z, CROUCHING))
                movementState = CROUCHING;
            else if (!collidesWithBlock(position.x, position.y + 1.25f, position.z, WALKING)) {
                camera.movePositionNoChecks(0.0f, 1.25f, 0.0f);
                movementState = WALKING;
            } else if (!collidesWithBlock(position.x, position.y + 1.0f, position.z, CROUCHING)) {
                camera.movePositionNoChecks(0.0f, 1.0f, 0.0f);
                movementState = CROUCHING;
            }
        }

        if (isInWater) {
            this.velocity.mul((float) Math.pow(WATER_FRICTION, passedTime));
        } else {
            float friction = isGrounded ? 0.0f : (float) Math.pow(AIR_FRICTION, passedTime);
            this.velocity.mul(friction, (float) Math.pow(FALL_FRICTION, passedTime), friction);
        }

        long currentTime = System.nanoTime();
        if (window.isKeyPressed(GLFW.GLFW_KEY_SPACE))
            if (isGrounded && currentTime - spaceButtonPressTime > 300_000_000) {
                this.velocity.y = JUMP_STRENGTH;
                isGrounded = false;
                spaceButtonPressTime = currentTime;
            } else if (isInWater) {
                velocity.y += SWIM_STRENGTH * passedTime * movementSpeedModifier * 7.5f;
                isGrounded = false;
            }

        if (window.isKeyPressed(GLFW.GLFW_KEY_W)) {
            float acceleration = MOVEMENT_STATE_SPEED[movementState] * movementSpeedModifier * accelerationModifier * passedTime;
            velocity.z -= acceleration;
        }
        movementSpeedModifier = Math.min(1.0f, movementSpeedModifier);
        if (window.isKeyPressed(GLFW.GLFW_KEY_S)) {
            float acceleration = MOVEMENT_STATE_SPEED[movementState] * movementSpeedModifier * accelerationModifier * passedTime;
            velocity.z += acceleration;
        }

        if (window.isKeyPressed(GLFW.GLFW_KEY_A)) {
            float acceleration = MOVEMENT_STATE_SPEED[movementState] * movementSpeedModifier * accelerationModifier * passedTime;
            velocity.x -= acceleration;
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_D)) {
            float acceleration = MOVEMENT_STATE_SPEED[movementState] * movementSpeedModifier * accelerationModifier * passedTime;
            velocity.x += acceleration;
        }

        float maxSpeed = Math.max(Math.abs(velocity.x), Math.abs(velocity.z));
        float normalizer = maxSpeed / (float) Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        if (isGrounded && !Float.isNaN(normalizer) && Float.isFinite(normalizer)) {
            velocity.x *= normalizer;
            velocity.z *= normalizer;
        }

        if (velocity.z != 0) {
            this.velocity.x -= (float) Math.sin(Math.toRadians(rotation.y)) * velocity.z;
            this.velocity.z += (float) Math.cos(Math.toRadians(rotation.y)) * velocity.z;
        }
        if (velocity.x != 0) {
            this.velocity.x -= (float) Math.sin(Math.toRadians(rotation.y - 90)) * velocity.x;
            this.velocity.z += (float) Math.cos(Math.toRadians(rotation.y - 90)) * velocity.x;
        }
        this.velocity.y += velocity.y;
        this.velocity.y = clampVelocity(this.velocity.y - GRAVITY_ACCELERATION * passedTime, MAX_FALL_SPEED);

        if (window.isKeyPressed(GLFW.GLFW_KEY_Q)) selectedHotBarSlot = 0;
        else if (window.isKeyPressed(GLFW.GLFW_KEY_2)) selectedHotBarSlot = 1;
        else if (window.isKeyPressed(GLFW.GLFW_KEY_3)) selectedHotBarSlot = 2;
        else if (window.isKeyPressed(GLFW.GLFW_KEY_4)) selectedHotBarSlot = 3;
        else if (window.isKeyPressed(GLFW.GLFW_KEY_5)) selectedHotBarSlot = 4;
        else if (window.isKeyPressed(GLFW.GLFW_KEY_R)) selectedHotBarSlot = 5;
        else if (window.isKeyPressed(GLFW.GLFW_KEY_F)) selectedHotBarSlot = 6;
        else if (mouseInput.isMouseButton5IsPressed()) selectedHotBarSlot = 7;
        else if (mouseInput.isMouseButton4IsPressed()) selectedHotBarSlot = 8;
        else if
        (window.isKeyPressed(GLFW.GLFW_KEY_UP) && !UPArrowPressed) {
            selectedHotBar = (selectedHotBar + 1) % hotBars.length;
            updateHotBarElements();
            UPArrowPressed = true;
        } else if
        (window.isKeyPressed(GLFW.GLFW_KEY_DOWN) && !DOWNArrowPressed) {
            selectedHotBar = (selectedHotBar - 1 + hotBars.length) % hotBars.length;
            updateHotBarElements();
            DOWNArrowPressed = true;
        } else if
        (window.isKeyPressed(GLFW.GLFW_KEY_G) && !gKeyPressed) {
            noClip = !noClip;
            gKeyPressed = true;
        } else if
        (window.isKeyPressed(GLFW.GLFW_KEY_T) && !tPressed) {
            Vector3f cameraPosition = camera.getPosition();
            pos1.x = (int) Math.floor(cameraPosition.x);
            pos1.y = (int) Math.floor(cameraPosition.y);
            pos1.z = (int) Math.floor(cameraPosition.z);
            tPressed = true;
        } else if
        (window.isKeyPressed(GLFW.GLFW_KEY_Y) && !zPressed) {
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

        if (UPArrowPressed && !window.isKeyPressed(GLFW.GLFW_KEY_UP)) UPArrowPressed = false;
        if (DOWNArrowPressed && !window.isKeyPressed(GLFW.GLFW_KEY_DOWN)) DOWNArrowPressed = false;
        if (gKeyPressed && !window.isKeyPressed(GLFW.GLFW_KEY_G)) gKeyPressed = false;
        if (tPressed && !window.isKeyPressed(GLFW.GLFW_KEY_T)) tPressed = false;
        if (zPressed && !window.isKeyPressed(GLFW.GLFW_KEY_Y)) zPressed = false;
    }

    private Vector3i getTarget(int action, Vector3f cD) {
        final int placing = 1;

        Vector3f cP = camera.getPosition();     //cameraPosition
        float interval = REACH / REACH_ACCURACY;
        int i = 0;
        byte block = OUT_OF_WORLD, previousBlock = OUT_OF_WORLD;

        for (; i < REACH_ACCURACY; i++) {
            previousBlock = block;

            double x = cP.x + i * interval * cD.x;
            double y = cP.y + i * interval * cD.y;
            double z = cP.z + i * interval * cD.z;

            block = Chunk.getBlockInWorld((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
            if (Block.intersectsBlock(x, y, z, block)) break;
        }
        if (block == AIR || block == OUT_OF_WORLD || block == WATER) return null;

        if (action == placing) {
            i--;
            if (previousBlock == block) while (previousBlock == block) {
                double x = cP.x + i * interval * cD.x;
                double y = cP.y + i * interval * cD.y;
                double z = cP.z + i * interval * cD.z;

                block = Chunk.getBlockInWorld((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
                i--;
            }
            else if (previousBlock != AIR && previousBlock != WATER) return null;
        }
        double x = Math.floor(cP.x + i * interval * cD.x);
        double y = Math.floor(cP.y + i * interval * cD.y);
        double z = Math.floor(cP.z + i * interval * cD.z);
        Vector3i target = new Vector3i((int) x, (int) y, (int) z);

        final float minX = cP.x - HALF_PLAYER_WIDTH;
        final float maxX = cP.x + HALF_PLAYER_WIDTH;
        final float minY = cP.y - PLAYER_FEET_OFFSETS[movementState];
        final float maxY = cP.y + PLAYER_HEAD_OFFSET;
        final float minZ = cP.z - HALF_PLAYER_WIDTH;
        final float maxZ = cP.z + HALF_PLAYER_WIDTH;

        byte toPlaceBlock = Block.getToPlaceBlock(hotBars[selectedHotBar][selectedHotBarSlot], camera.getPrimaryDirection());

        if (action == placing && Block.playerIntersectsBlock(minX, maxX, minY, maxY, minZ, maxZ, target.x, target.y, target.z, toPlaceBlock))
            return null;

        return target;
    }

    public void render() {
        for (Chunk chunk : Chunk.getWorld()) {
            if (chunk == null)
                continue;

            if (chunk.getModel() != null)
                renderer.processModel(chunk.getModel());

            if (chunk.getTransparentModel() != null)
                renderer.processTransparentModel(chunk.getTransparentModel());
        }

        for (GUIElement GUIElement : GUIElements)
            renderer.processGUIElement(GUIElement);

        for (GUIElement GUIElement : hotBarElements)
            renderer.processGUIElement(GUIElement);
    }

    public boolean collidesWithBlock(float x, float y, float z, int movementState) {
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

                    if (Block.playerIntersectsBlock(minX, maxX, minY, maxY, minZ, maxZ, blockX, blockY, blockZ, block))
                        return true;
                }
        return false;
    }

    public boolean collidesWithBlock(float x, float y, float z, int movementState, byte block) {
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
            final int textureIndex = Block.getTextureIndex(hotBars[selectedHotBar][i], 0) - 1;
            final float[] textureCoordinates = getTextureCoordinates(textureIndex);
            GUIElement element = ObjectLoader.loadGUIElement(GameLogic.getHotBarElementVertices(i), textureCoordinates);
            element.setTexture(atlas);
            hotBarElements.add(element);
        }
    }

    private static float[] getTextureCoordinates(int textureIndex) {
        final int textureX = textureIndex & 15;
        final int textureY = (textureIndex >> 4) & 15;

        final float upperX = textureX * 0.0625f;
        final float lowerX = (textureX + 1) * 0.0625f;
        final float upperY = textureY * 0.0625f;
        final float lowerY = (textureY + 1) * 0.0625f;

        return new float[]{
                lowerX, lowerY,
                lowerX, upperY,
                upperX, lowerY,

                lowerX, upperY,
                upperX, upperY,
                upperX, lowerY};
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
