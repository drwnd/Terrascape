package terrascape.player;

import terrascape.entity.*;
import terrascape.entity.particles.Particle;
import terrascape.server.*;
import terrascape.dataStorage.Chunk;
import terrascape.dataStorage.FileManager;
import terrascape.dataStorage.Structure;
import terrascape.entity.entities.Entity;
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

import static terrascape.generation.WorldGeneration.WATER_LEVEL;
import static terrascape.utils.Constants.*;
import static terrascape.utils.Settings.*;

public class Player {

    public Player() {
        window = Launcher.getWindow();
        sound = Launcher.getSound();
        renderer = new RenderManager(this);
        camera = new Camera();
        mouseInput = new MouseInput(this);
        movement = new Movement(this);
        interactionHandler = new InteractionHandler(this);

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

        GUIElement.loadGUIElements(this);

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
            if (key == GLFW.GLFW_KEY_J && action == GLFW.GLFW_PRESS) {
                printTimes = !printTimes;
            }
            if (key == GLFW.GLFW_KEY_K && action == GLFW.GLFW_PRESS) {
                usingFrustumCulling = !usingFrustumCulling;
                System.out.println("frustum culling" + usingFrustumCulling);
            }
            if (key == GLFW.GLFW_KEY_L && action == GLFW.GLFW_PRESS) {
                renderingEntities = !renderingEntities;
                System.out.println("rendering entities" + renderingEntities);
            }
        });
    }

    public void update(float passedTicks) {
        Vector3f velocity = movement.getVelocity();
        movement.moveCameraHandleCollisions(velocity.x * passedTicks, velocity.y * passedTicks, velocity.z * passedTicks);

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
        int movementState = movement.getMovementState();
        if (movementState == Movement.SWIMMING) {
            if (tick - lastFootstepTick < 15) return;
            Vector3f position = camera.getPosition();
            sound.playRandomSound(sound.swim, position.x, position.y, position.z, 0.0f, 0.0f, 0.0f, STEP_GAIN);
            lastFootstepTick = tick;
            return;
        }
        Vector3f velocity = movement.getVelocity();

        if (!movement.isGrounded() || Math.abs(velocity.x) < 0.001f && Math.abs(velocity.z) < 0.001f) return;
        if (movementState == Movement.CROUCHING || movementState == Movement.CRAWLING) return;
        if (movementState == Movement.WALKING) {
            if (window.isKeyPressed(SPRINT_BUTTON)) {
                if (tick - lastFootstepTick < 5) return;
            } else if (tick - lastFootstepTick < 10) return;
        }
        lastFootstepTick = tick;

        Vector3f position = camera.getPosition();
        float height = Movement.PLAYER_FEET_OFFSETS[movementState];
        short standingBlock = movement.getStandingBlock();
        sound.playRandomSound(Block.getFootstepsSound(standingBlock), position.x, position.y - height, position.z, 0.0f, 0.0f, 0.0f, STEP_GAIN);
    }

    public void input() {
        Vector3f position = camera.getPosition();
        boolean isInWater = movement.collidesWithWater(position.x, position.y, position.z, movement.getMovementState());
        movement.move();

        interactionHandler.handleDestroyUsePickBlockInput();
        if (inInventory) handleInventoryHotkeys();

        if (isInWater != movement.isTouchingWater())
            sound.playRandomSound(sound.splash, position.x, position.y, position.z, 0.0f, 0.0f, 0.0f, MISCELLANEOUS_GAIN);
        movement.setTouchingWater(isInWater);
    }

    protected void handleInventoryHotkeys() {
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

    public void handleNonMovementInputs(int button, int action) {
        if (button == HOT_BAR_SLOT_1 && action == GLFW.GLFW_PRESS) setSelectedHotBarSlot(0);
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
            camera.setZoomModifier(0.25f);
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
        Vector3f velocity = movement.getVelocity();
        sound.playRandomSound(Block.getFootstepsSound(hotBar[selectedHotBarSlot]), position.x, position.y, position.z, velocity.x, velocity.y, velocity.z, INVENTORY_GAIN);
    }

    private void toggleInventory() {
        inInventory = !inInventory;
        GLFW.glfwSetInputMode(window.getWindow(), GLFW.GLFW_CURSOR, inInventory ? GLFW.GLFW_CURSOR_NORMAL : GLFW.GLFW_CURSOR_DISABLED);
    }

    public void render(float passedTicks) {
        long playerTime = System.nanoTime();
        Vector3f cameraPosition = camera.getPosition();
        final int playerChunkX = Utils.floor(cameraPosition.x) >> CHUNK_SIZE_BITS;
        final int playerChunkY = Utils.floor(cameraPosition.y) >> CHUNK_SIZE_BITS;
        final int playerChunkZ = Utils.floor(cameraPosition.z) >> CHUNK_SIZE_BITS;

        long occlusionCullingTime = System.nanoTime();
        if (usingOcclusionCulling) calculateOcclusionCulling(playerChunkX, playerChunkY, playerChunkZ);
        occlusionCullingTime = System.nanoTime() - occlusionCullingTime;

        long frustumCullingTime = System.nanoTime();
        if (usingFrustumCulling) calculateFrustumCulling(playerChunkX, playerChunkY, playerChunkZ);
        frustumCullingTime = System.nanoTime() - frustumCullingTime;

        long renderChunkColumnTime = System.nanoTime();
        renderChunkColumn(playerChunkX, playerChunkY, playerChunkZ);
        for (int ring = 1; ring <= RENDER_DISTANCE_XZ + 2; ring++) {
            for (int x = -ring; x < ring; x++)
                renderChunkColumn(x + playerChunkX, playerChunkY, ring + playerChunkZ);
            for (int z = ring; z > -ring; z--)
                renderChunkColumn(ring + playerChunkX, playerChunkY, z + playerChunkZ);
            for (int x = ring; x > -ring; x--)
                renderChunkColumn(x + playerChunkX, playerChunkY, -ring + playerChunkZ);
            for (int z = -ring; z < ring; z++)
                renderChunkColumn(-ring + playerChunkX, playerChunkY, z + playerChunkZ);
        }
        renderChunkColumnTime = System.nanoTime() - renderChunkColumnTime;

        for (GUIElement GUIElement : GUIElements)
            renderer.processGUIElement(GUIElement);

        for (GUIElement GUIElement : hotBarElements)
            renderer.processGUIElement(GUIElement);

        renderer.processGUIElement(hotBarSelectionIndicator);

        if (renderingEntities) {
            for (Entity entity : GameLogic.getEntities()) {
                int entityChunkX = Utils.floor(entity.getPosition().x) >> CHUNK_SIZE_BITS;
                int entityChunkY = Utils.floor(entity.getPosition().y) >> CHUNK_SIZE_BITS;
                int entityChunkZ = Utils.floor(entity.getPosition().z) >> CHUNK_SIZE_BITS;

                int entityChunkIndex = GameLogic.getChunkIndex(entityChunkX, entityChunkY, entityChunkZ);
                if ((visibleChunks[entityChunkIndex >> 6] & 1L << (entityChunkIndex & 63)) == 0) continue;

                renderer.processEntity(entity);
            }

            for (Particle particle : GameLogic.getParticles()) {
                int particleChunkX = Utils.floor(particle.getPosition().x) >> CHUNK_SIZE_BITS;
                int particleChunkY = Utils.floor(particle.getPosition().y) >> CHUNK_SIZE_BITS;
                int particleChunkZ = Utils.floor(particle.getPosition().z) >> CHUNK_SIZE_BITS;

                int particleChunkIndex = GameLogic.getChunkIndex(particleChunkX, particleChunkY, particleChunkZ);
                if ((visibleChunks[particleChunkIndex >> 6] & 1L << (particleChunkIndex & 63)) == 0) continue;

                renderer.processParticle(particle);
            }
        }

        boolean headUnderWater = Block.isWaterLogged(Chunk.getBlockInWorld(Utils.floor(cameraPosition.x), Utils.floor(cameraPosition.y), Utils.floor(cameraPosition.z)));
        if (headUnderWater && !this.headUnderWater)
            sound.playRandomSound(sound.submerge, cameraPosition.x, cameraPosition.y, cameraPosition.z, 0.0f, 0.0f, 0.0f, MISCELLANEOUS_GAIN);
        else if (this.headUnderWater && !headUnderWater)
            sound.playRandomSound(sound.splash, cameraPosition.x, cameraPosition.y, cameraPosition.z, 0.0f, 0.0f, 0.0f, MISCELLANEOUS_GAIN);
        this.headUnderWater = headUnderWater;
        renderer.setHeadUnderWater(headUnderWater);

        if (inInventory) renderInventoryElements();
        playerTime = System.nanoTime() - playerTime;

        long renderTime = System.nanoTime();
        renderer.render(camera, passedTicks);
        renderTime = System.nanoTime() - renderTime;

        if (printTimes) {
            System.out.println("player " + playerTime);
            System.out.println("occlusionCulling " + occlusionCullingTime);
            System.out.println("frustumCulling   " + frustumCullingTime);
            System.out.println("chunkColumn      " + renderChunkColumnTime);
            System.out.println("render " + renderTime);
        }
    }

    private void renderInventoryElements() {
        for (GUIElement element : inventoryElements) {
            if (element.getPosition().y > 0.55f || element.getPosition().y < -0.55f) continue;
            renderer.processGUIElement(element);
        }
        short hoveredBlock = GUIElement.getHoveredOverBlock(inventoryScroll);
        if (hoveredBlock == AIR) return;
        String name = Block.getBlockName(hoveredBlock);
        renderer.processDisplayString(new DisplayString(mouseInput.getX() - name.length() * TEXT_CHAR_SIZE_X, mouseInput.getY(), name));
    }

    private void renderChunkColumn(int chunkX, int playerChunkY, int chunkZ) {
        for (int chunkY = playerChunkY + RENDER_DISTANCE_Y + 2; chunkY >= playerChunkY - RENDER_DISTANCE_Y - 2; chunkY--) {
            Chunk chunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
            if (chunk == null) continue;
            int chunkIndex = chunk.getIndex();
            if ((visibleChunks[chunkIndex >> 6] & 1L << (chunkIndex & 63)) == 0) continue;

            if (chunk.getWaterModel() != null) renderer.processWaterModel(chunk.getWaterModel());
            if (chunk.getOpaqueModel() != null) renderer.processModel(chunk.getOpaqueModel());
        }
    }

    private void calculateFrustumCulling(int playerChunkX, int playerChunkY, int playerChunkZ) {
        Matrix4f projectionMatrix = window.getProjectionMatrix();
        Matrix4f viewMatrix = Transformation.getViewMatrix(camera);
        Matrix4f projectionViewMatrix = new Matrix4f();
        projectionMatrix.mul(viewMatrix, projectionViewMatrix);
        FrustumIntersection frustumIntersection = new FrustumIntersection(projectionViewMatrix);

        for (int chunkX = playerChunkX - RENDER_DISTANCE_XZ - 2; chunkX <= playerChunkX + RENDER_DISTANCE_XZ + 2; chunkX++)
            for (int chunkY = playerChunkY - RENDER_DISTANCE_Y - 2; chunkY <= playerChunkY + RENDER_DISTANCE_Y + 2; chunkY++)
                for (int chunkZ = playerChunkZ - RENDER_DISTANCE_XZ - 2; chunkZ <= playerChunkZ + RENDER_DISTANCE_XZ + 2; chunkZ++) {
                    int chunkIndex = GameLogic.getChunkIndex(chunkX, chunkY, chunkZ);
                    if ((visibleChunks[chunkIndex >> 6] & 1L << (chunkIndex & 63)) == 0) continue;

                    int intersectionType = frustumIntersection.intersectAab(
                            chunkX << CHUNK_SIZE_BITS, chunkY << CHUNK_SIZE_BITS, chunkZ << CHUNK_SIZE_BITS,
                            chunkX + 1 << CHUNK_SIZE_BITS, chunkY + 1 << CHUNK_SIZE_BITS, chunkZ + 1 << CHUNK_SIZE_BITS);

                    if (intersectionType == FrustumIntersection.INSIDE || intersectionType == FrustumIntersection.INTERSECT)
                        continue;

                    visibleChunks[chunkIndex >> 6] &= ~(1L << (chunkIndex & 63));
                }
    }

    private void calculateOcclusionCulling(int playerChunkX, int playerChunkY, int playerChunkZ) {
        Arrays.fill(visibleChunks, 0);
        int chunkIndex = GameLogic.getChunkIndex(playerChunkX, playerChunkY, playerChunkZ);

        visibleChunks[chunkIndex >> 6] = visibleChunks[chunkIndex >> 6] | 1L << (chunkIndex & 63);

        fillVisibleChunks(playerChunkX, playerChunkY, playerChunkZ + 1, SOUTH, 1 << NORTH, 0);
        fillVisibleChunks(playerChunkX, playerChunkY, playerChunkZ - 1, NORTH, 1 << SOUTH, 0);

        fillVisibleChunks(playerChunkX, playerChunkY + 1, playerChunkZ, BOTTOM, 1 << TOP, 0);
        fillVisibleChunks(playerChunkX, playerChunkY - 1, playerChunkZ, TOP, 1 << BOTTOM, 0);

        fillVisibleChunks(playerChunkX + 1, playerChunkY, playerChunkZ, EAST, 1 << WEST, 0);
        fillVisibleChunks(playerChunkX - 1, playerChunkY, playerChunkZ, WEST, 1 << EAST, 0);
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
            int blockType = Block.getBlockType(block);
            if (blockType == FLOWER_TYPE || blockType == VINE_TYPE) {
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

            element.setTexture(Texture.atlas);
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

    public int getSelectedHotBarSlot() {
        return selectedHotBarSlot;
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

    public float getInventoryScroll() {
        return inventoryScroll;
    }

    public void cleanUp() {
        renderer.cleanUp();
    }

    public Movement getMovement() {
        return movement;
    }

    public InteractionHandler getInteractionHandler() {
        return interactionHandler;
    }

    public ArrayList<GUIElement> getInventoryElements() {
        return inventoryElements;
    }

    public ArrayList<GUIElement> getGUIElements() {
        return GUIElements;
    }

    public GUIElement getHotBarSelectionIndicator() {
        return hotBarSelectionIndicator;
    }

    public void setHotBarSelectionIndicator(GUIElement hotBarSelectionIndicator) {
        this.hotBarSelectionIndicator = hotBarSelectionIndicator;
    }

    public void setInventoryScroll(float inventoryScroll) {
        this.inventoryScroll = inventoryScroll;
    }

    private final RenderManager renderer;
    private final WindowManager window;
    private final SoundManager sound;
    private final Camera camera;
    private final MouseInput mouseInput;
    private final Movement movement;
    private final InteractionHandler interactionHandler;

    private long[] visibleChunks;

    private final ArrayList<GUIElement> GUIElements = new ArrayList<>();
    private final ArrayList<GUIElement> hotBarElements = new ArrayList<>();
    private final ArrayList<GUIElement> inventoryElements = new ArrayList<>();
    private GUIElement hotBarSelectionIndicator;

    private float inventoryScroll = 0;
    private boolean headUnderWater;
    private short[] hotBar = new short[9];
    private int selectedHotBarSlot = -1; // No idea but when it's 0 there is a bug but anything else works
    private long lastFootstepTick = 0;
    private boolean inInventory;

    // Debug
    private boolean debugScreenOpen;
    private boolean usingOcclusionCulling = true, usingFrustumCulling = true, renderingEntities = true;
    public boolean printTimes = false;
    private boolean noClip;
    private final Vector3i pos1, pos2;
}