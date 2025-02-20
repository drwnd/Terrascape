package terrascape.player;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;
import terrascape.dataStorage.Chunk;
import terrascape.entity.Target;
import terrascape.entity.entities.Entity;
import terrascape.entity.entities.TNT_Entity;
import terrascape.server.Block;
import terrascape.server.BlockEvent;
import terrascape.server.ServerLogic;
import terrascape.server.Launcher;

import static terrascape.utils.Constants.*;
import static terrascape.utils.Constants.AIR;
import static terrascape.utils.Settings.*;

public final class InteractionHandler {

    public InteractionHandler(Player player) {
        this.player = player;
        camera = player.getCamera();
        window = Launcher.getWindow();
    }

    public void handleDestroyUsePickBlockInput() {
        if (player.isInInventory()) return;
        boolean useButtonWasJustPressed = this.useButtonWasJustPressed;
        boolean destroyButtonWasJustPressed = this.destroyButtonWasJustPressed;
        this.useButtonWasJustPressed = false;
        this.destroyButtonWasJustPressed = false;
        long currentTime = System.nanoTime();

        handleDestroy(currentTime, destroyButtonWasJustPressed);

        handleUse(currentTime, useButtonWasJustPressed);

        handlePickBlock();
    }

    public void input(int button, int action) {
        if (button == DESTROY_BUTTON) {
            if (action == GLFW.GLFW_PRESS) {
                destroyButtonPressTime = System.nanoTime();
                destroyButtonWasJustPressed = true;
            } else {
                destroyButtonPressTime = -1;
            }
        } else if (button == USE_BUTTON) {
            if (action == GLFW.GLFW_PRESS) {
                useButtonPressTime = System.nanoTime();
                useButtonWasJustPressed = true;
            } else {
                useButtonPressTime = -1;
            }
        }
    }

    private void handleDestroy(long currentTime, boolean destroyButtonWasJustPressed) {
        if ((destroyButtonPressTime == -1 || currentTime - destroyButtonPressTime <= 300_000_000) && !destroyButtonWasJustPressed)
            return;
        Target target = Target.getTarget(camera.getPosition(), camera.getDirection());
        if (target != null)
            ServerLogic.placeBlock(AIR, target.position().x, target.position().y, target.position().z, true);
    }

    private void handleUse(long currentTime, boolean useButtonWasJustPressed) {
        if (((useButtonPressTime == -1 || currentTime - useButtonPressTime <= 300_000_000) && !useButtonWasJustPressed))
            return;

        Vector3f cameraDirection = camera.getDirection();
        Vector3f cameraPosition = camera.getPosition();
        Target target = Target.getTarget(cameraPosition, cameraDirection);
        if (target == null) return;

        short selectedBlock = player.getHotBar()[player.getSelectedHotBarSlot()];

        if (Block.isInteractable(target.block()))
            if (interactWithBlock(target, selectedBlock)) return;

        if (selectedBlock == AIR) return;
        short toPlaceBlock;
        short inventoryBlock = Block.getInInventoryBlockEquivalent(target.block());
        if (window.isKeyPressed(SPRINT_BUTTON) && Block.getBlockType(inventoryBlock) == Block.getBlockType(selectedBlock)
                && (selectedBlock & 0xFFFF) >= STANDARD_BLOCKS_THRESHOLD) {

            if ((inventoryBlock & BASE_BLOCK_MASK) == (selectedBlock & BASE_BLOCK_MASK))
                toPlaceBlock = (short) (target.block() & ~WATER_LOGGED_MASK);
            else toPlaceBlock = (short) (selectedBlock & BASE_BLOCK_MASK | target.block() & BLOCK_TYPE_MASK);

        } else
            toPlaceBlock = Block.getToPlaceBlock(selectedBlock, camera.getPrimaryDirection(cameraDirection), camera.getPrimaryXZDirection(cameraDirection), target);

        final float minX = cameraPosition.x - Movement.HALF_PLAYER_WIDTH;
        final float maxX = cameraPosition.x + Movement.HALF_PLAYER_WIDTH;
        final float minY = cameraPosition.y - Movement.PLAYER_FEET_OFFSETS[player.getMovement().getMovementState()];
        final float maxY = cameraPosition.y + Movement.PLAYER_HEAD_OFFSET;
        final float minZ = cameraPosition.z - Movement.HALF_PLAYER_WIDTH;
        final float maxZ = cameraPosition.z + Movement.HALF_PLAYER_WIDTH;
        Vector3i position = target.position();
        int x = position.x;
        int y = position.y;
        int z = position.z;
        boolean isWaterLogging = false;

        if ((Block.getBlockProperties(Chunk.getBlockInWorld(x, y, z)) & REPLACEABLE) == 0) {

            boolean blockCanBeWaterLogged = (target.block() & 0xFFFF) > STANDARD_BLOCKS_THRESHOLD && (target.block() & BLOCK_TYPE_MASK) != FULL_BLOCK;

            if (!blockCanBeWaterLogged || selectedBlock != WATER_SOURCE || window.isKeyPressed(SNEAK_BUTTON)) {
                byte[] normal = Block.NORMALS[target.side()];
                x = position.x + normal[0];
                y = position.y + normal[1];
                z = position.z + normal[2];

                if (selectedBlock == WATER_SOURCE) {
                    short block = Chunk.getBlockInWorld(x, y, z);
                    isWaterLogging = (block & 0xFFFF) > STANDARD_BLOCKS_THRESHOLD && (block & BLOCK_TYPE_MASK) != FULL_BLOCK;
                    if (isWaterLogging) toPlaceBlock = (short) (block | WATER_LOGGED_MASK);
                }

            } else {
                isWaterLogging = true;
                toPlaceBlock = (short) (target.block() | WATER_LOGGED_MASK);
            }
        }
        if (player.hasCollision() && Entity.entityIntersectsBlock(minX, maxX, minY, maxY, minZ, maxZ, x, y, z, toPlaceBlock)
                || Entity.entityIntersectsBlock(x, y, z, toPlaceBlock))
            return;

        if (!Block.isSupported(selectedBlock, x, y, z)) return;

        if (isWaterLogging || (Block.getBlockProperties(Chunk.getBlockInWorld(x, y, z)) & REPLACEABLE) != 0)
            ServerLogic.placeBlock(toPlaceBlock, x, y, z, true);
    }

    public static short getToPlaceBlock(short block, short previousBlock) {
        if (block == AIR && Block.isWaterLogged(previousBlock)) return WATER_SOURCE;
        if (previousBlock != WATER_SOURCE) return block;
        if ((block & 0xFFFF) < STANDARD_BLOCKS_THRESHOLD) return block;
        if ((block & BLOCK_TYPE_MASK) == FULL_BLOCK) return block;
        return (short) (block | WATER_LOGGED_MASK);
    }

    private void handlePickBlock() {
        if (!window.isKeyPressed(PICK_BLOCK_BUTTON)) return;

        Target target = Target.getTarget(camera.getPosition(), camera.getDirection());
        if (target == null) return;

        int selectedHotBarSlot = player.getSelectedHotBarSlot();
        short[] hotBar = player.getHotBar();
        short block = Chunk.getBlockInWorld(target.position().x, target.position().y, target.position().z);
        short inInventoryBlock = Block.getInInventoryBlockEquivalent(block);

        boolean hasPlacedBlock = false;
        for (int hotBarSlot = 0; hotBarSlot < hotBar.length; hotBarSlot++) {
            if (hotBar[hotBarSlot] != inInventoryBlock) continue;
            hasPlacedBlock = true;
            if (hotBarSlot != selectedHotBarSlot) player.setSelectedHotBarSlot(hotBarSlot);
            break;
        }
        if (!hasPlacedBlock && hotBar[selectedHotBarSlot] != AIR)
            for (int hotBarSlot = 0; hotBarSlot < hotBar.length; hotBarSlot++) {
                if (hotBar[hotBarSlot] != AIR) continue;
                hotBar[hotBarSlot] = inInventoryBlock;
                if (hotBarSlot != selectedHotBarSlot) player.setSelectedHotBarSlot(hotBarSlot);
                hasPlacedBlock = true;
                break;
            }
        if (!hasPlacedBlock) hotBar[selectedHotBarSlot] = inInventoryBlock;

        player.updateHotBarElements();
    }

    public static boolean interactWithBlock(Target target, short heldBlock) {
        WindowManager window = Launcher.getWindow();
        SoundManager sound = Launcher.getSound();
        if (window.isKeyPressed(SNEAK_BUTTON)) return false;
        short block = target.block();

        if (block == CRAFTING_TABLE) {
            return heldBlock != CRAFTING_TABLE;
            // TODO Crafting table UI
        }
        if (block == TNT) {
            if (heldBlock == TNT) return false;
            TNT_Entity.spawnTNTEntity(target.position(), TNT_Entity.STANDARD_TNT_FUSE);
            sound.playSound(sound.fuse, target.position().x, target.position().y, target.position().z, 0.0f, 0.0f, 0.0f, MISCELLANEOUS_GAIN);
            return true;
        }
        if (block == NORTH_FURNACE || block == WEST_FURNACE || block == SOUTH_FURNACE || block == EAST_FURNACE) {
            return heldBlock != NORTH_FURNACE;
            // TODO Furnace UI
        }
        if (Block.isDoorType(block)) {
            if (Block.getBlockType(heldBlock) == NORTH_WEST_DOOR_NORTH) return false;
            int x = target.position().x;
            int y = target.position().y;
            int z = target.position().z;
            int doorType = Block.getBlockType(target.block());

            BlockEvent.flickDoors(x, y, z);

            switch (doorType) {
                case NORTH_WEST_DOOR_NORTH, NORTH_WEST_DOOR_WEST -> {
                    if (Block.isDoorType(Chunk.getBlockInWorld(x - 1, y, z))) BlockEvent.flickDoors(x - 1, y, z);
                    if (Block.isDoorType(Chunk.getBlockInWorld(x, y, z - 1))) BlockEvent.flickDoors(x, y, z - 1);
                }
                case NORTH_EAST_DOOR_NORTH, NORTH_EAST_DOOR_EAST -> {
                    if (Block.isDoorType(Chunk.getBlockInWorld(x + 1, y, z))) BlockEvent.flickDoors(x + 1, y, z);
                    if (Block.isDoorType(Chunk.getBlockInWorld(x, y, z - 1))) BlockEvent.flickDoors(x, y, z - 1);
                }
                case SOUTH_WEST_DOOR_SOUTH, SOUTH_WEST_DOOR_WEST -> {
                    if (Block.isDoorType(Chunk.getBlockInWorld(x - 1, y, z))) BlockEvent.flickDoors(x - 1, y, z);
                    if (Block.isDoorType(Chunk.getBlockInWorld(x, y, z + 1))) BlockEvent.flickDoors(x, y, z + 1);
                }
                case SOUTH_EAST_DOOR_SOUTH, SOUTH_EAST_DOOR_EAST -> {
                    if (Block.isDoorType(Chunk.getBlockInWorld(x, y, z + 1))) BlockEvent.flickDoors(x, y, z + 1);
                    if (Block.isDoorType(Chunk.getBlockInWorld(x + 1, y, z))) BlockEvent.flickDoors(x + 1, y, z);
                }
            }
            return true;
        }
        return false;
    }


    private long useButtonPressTime = -1, destroyButtonPressTime = -1;
    private boolean useButtonWasJustPressed = false, destroyButtonWasJustPressed = false;

    private final Player player;
    private final Camera camera;
    private final WindowManager window;
}
