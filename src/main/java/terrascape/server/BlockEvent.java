package terrascape.server;

import org.joml.Vector3f;
import terrascape.dataStorage.Chunk;
import terrascape.entity.Target;
import terrascape.entity.entities.FallingBlockEntity;
import terrascape.entity.entities.TNT_Entity;
import terrascape.player.SoundManager;
import terrascape.player.WindowManager;
import terrascape.utils.EventQueue;

import static terrascape.utils.Constants.*;
import static terrascape.utils.Settings.*;

import java.util.ArrayList;
import java.util.Iterator;

public record BlockEvent(int x, int y, int z, byte type) {

    public static final byte GRAVITY_BLOCK_FALL_EVENT = 1;
    public static final byte WATER_FLOW_EVENT = 2;
    public static final byte LAVA_FLOW_EVENT = 3;
    public static final byte SMART_BLOCK_EVENT = 4;
    public static final byte UNSUPPORTED_BLOCK_BREAK_EVENT = 5;

    public static void add(BlockEvent event, long tick) {
        if (tick < EngineManager.getTick()) System.err.println("Event scheduled in the past");

        for (EventQueue queue : events)
            if (queue.tick == tick) {
                if (!queue.hasEventScheduledAt(event.x, event.y, event.z)) queue.enqueue(event);
                return;
            }

        EventQueue queue = new EventQueue(10, tick);
        queue.enqueue(event);
        events.add(queue);
    }

    public static void execute(long tick) {
        EventQueue queue = null;

        for (Iterator<EventQueue> iterator = events.iterator(); iterator.hasNext(); ) {
            EventQueue currentQueue = iterator.next();
            if (currentQueue.tick == tick) {
                queue = currentQueue;
                break;
            } else if (currentQueue.tick < tick) {
                System.err.println(currentQueue.size() + " Events were missed");
                iterator.remove();
            }
        }

        if (queue == null) return;

        while (!queue.isEmpty()) {
            BlockEvent event = queue.dequeue();
            switch (event.type) {
                case SMART_BLOCK_EVENT -> executeSmartBlockEvent(event);
                case GRAVITY_BLOCK_FALL_EVENT -> executeGravityBlockFallEvent(event);
                case WATER_FLOW_EVENT -> executeWaterFlowEvent(event);
                case LAVA_FLOW_EVENT -> executeLavaFlowEvent(event);
                case UNSUPPORTED_BLOCK_BREAK_EVENT -> executeUnsupportedBlockBreakEvent(event);
            }
        }
        events.remove(queue);
    }

    public static int getAmountOfScheduledEvents(long currentTick) {
        int sum = 0;
        for (EventQueue currentQueue : events)
            if (currentQueue.tick >= currentTick) sum += currentQueue.size();
        return sum;
    }

    public static void updateSurrounding(int x, int y, int z) {
        addCorrectEvents(x, y, z);
        addCorrectEvents(x + 1, y, z);
        addCorrectEvents(x - 1, y, z);
        addCorrectEvents(x, y + 1, z);
        addCorrectEvents(x, y - 1, z);
        addCorrectEvents(x, y, z + 1);
        addCorrectEvents(x, y, z - 1);
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
            TNT_Entity.spawnTNTEntity(target.position(), 80);
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

            flickDoors(x, y, z);

            switch (doorType) {
                case NORTH_WEST_DOOR_NORTH, NORTH_WEST_DOOR_WEST -> {
                    if (Block.isDoorType(Chunk.getBlockInWorld(x - 1, y, z))) flickDoors(x - 1, y, z);
                    if (Block.isDoorType(Chunk.getBlockInWorld(x, y, z - 1))) flickDoors(x, y, z - 1);
                }
                case NORTH_EAST_DOOR_NORTH, NORTH_EAST_DOOR_EAST -> {
                    if (Block.isDoorType(Chunk.getBlockInWorld(x + 1, y, z))) flickDoors(x + 1, y, z);
                    if (Block.isDoorType(Chunk.getBlockInWorld(x, y, z - 1))) flickDoors(x, y, z - 1);
                }
                case SOUTH_WEST_DOOR_SOUTH, SOUTH_WEST_DOOR_WEST -> {
                    if (Block.isDoorType(Chunk.getBlockInWorld(x - 1, y, z))) flickDoors(x - 1, y, z);
                    if (Block.isDoorType(Chunk.getBlockInWorld(x, y, z + 1))) flickDoors(x, y, z + 1);
                }
                case SOUTH_EAST_DOOR_SOUTH, SOUTH_EAST_DOOR_EAST -> {
                    if (Block.isDoorType(Chunk.getBlockInWorld(x, y, z + 1))) flickDoors(x, y, z + 1);
                    if (Block.isDoorType(Chunk.getBlockInWorld(x + 1, y, z))) flickDoors(x + 1, y, z);
                }
            }
            return true;
        }
        return false;
    }

    private static void executeSmartBlockEvent(BlockEvent event) {
        short block = Chunk.getBlockInWorld(event.x, event.y, event.z);
        if ((Block.getBlockTypeData(block) & SMART_BLOCK_TYPE) == 0) return;
        int blockType = Block.getBlockType(block);
        int water_logged = block & WATER_LOGGED_MASK;

        int expectedBlockType = getSmartBlockType(block, event.x, event.y, event.z);
        if (expectedBlockType == blockType) return;

        int chunkX = event.x >> CHUNK_SIZE_BITS;
        int chunkY = event.y >> CHUNK_SIZE_BITS;
        int chunkZ = event.z >> CHUNK_SIZE_BITS;

        Chunk chunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
        if (chunk == null) return;

        int inChunkX = event.x & CHUNK_SIZE_MASK;
        int inChunkY = event.y & CHUNK_SIZE_MASK;
        int inChunkZ = event.z & CHUNK_SIZE_MASK;

        chunk.placeBlock(inChunkX, inChunkY, inChunkZ, (short) (block & BASE_BLOCK_MASK | water_logged | expectedBlockType));
    }

    private static int getSmartBlockType(short block, int x, int y, int z) {
        int blockType = Block.getBlockType(block);
        int waterLogged = (block & 0xFFFF) > STANDARD_BLOCKS_THRESHOLD ? block & WATER_LOGGED_MASK : 0;

        if (Block.isNorthSouthFenceType(blockType)) {
            int index = 0;
            short adjacentBlock;
            long adjacentMask;

            adjacentBlock = Chunk.getBlockInWorld(x, y + 1, z);
            adjacentMask = Block.isLiquidType(Block.getBlockType(adjacentBlock)) ? 0L : Block.getBlockOcclusionData(adjacentBlock, BOTTOM);
            if (((adjacentMask & Block.getBlockTypeOcclusionData(EAST_WEST_WALL, TOP)) != 0 || Block.isNorthSouthFenceType(Block.getBlockType(adjacentBlock))))
                index |= 1;

            adjacentBlock = Chunk.getBlockInWorld(x + 1, y, z);
            adjacentMask = Block.isLiquidType(Block.getBlockType(adjacentBlock)) ? 0L : Block.getBlockOcclusionData(adjacentBlock, EAST);
            if (((adjacentMask & Block.getBlockTypeOcclusionData(UP_DOWN_WALL, WEST)) != 0 || Block.isNorthSouthFenceType(Block.getBlockType(adjacentBlock))))
                index |= 2;

            adjacentBlock = Chunk.getBlockInWorld(x, y - 1, z);
            adjacentMask = Block.isLiquidType(Block.getBlockType(adjacentBlock)) ? 0L : Block.getBlockOcclusionData(adjacentBlock, TOP);
            if (((adjacentMask & Block.getBlockTypeOcclusionData(EAST_WEST_WALL, BOTTOM)) != 0 || Block.isNorthSouthFenceType(Block.getBlockType(adjacentBlock))))
                index |= 4;

            adjacentBlock = Chunk.getBlockInWorld(x - 1, y, z);
            adjacentMask = Block.isLiquidType(Block.getBlockType(adjacentBlock)) ? 0L : Block.getBlockOcclusionData(adjacentBlock, WEST);
            if (((adjacentMask & Block.getBlockTypeOcclusionData(UP_DOWN_WALL, EAST)) != 0 || Block.isNorthSouthFenceType(Block.getBlockType(adjacentBlock))))
                index |= 8;

            return NORTH_SOUTH_FENCE + index | waterLogged;
        }
        if (Block.isUpDownFenceType(blockType)) {
            int index = 0;
            short adjacentBlock;
            long adjacentMask;

            adjacentBlock = Chunk.getBlockInWorld(x, y, z + 1);
            adjacentMask = Block.isLiquidType(Block.getBlockType(adjacentBlock)) ? 0L : Block.getBlockOcclusionData(adjacentBlock, SOUTH);
            if (((adjacentMask & Block.getBlockTypeOcclusionData(EAST_WEST_WALL, NORTH)) != 0 || Block.isUpDownFenceType(Block.getBlockType(adjacentBlock))))
                index |= 1;

            adjacentBlock = Chunk.getBlockInWorld(x + 1, y, z);
            adjacentMask = Block.isLiquidType(Block.getBlockType(adjacentBlock)) ? 0L : Block.getBlockOcclusionData(adjacentBlock, EAST);
            if (((adjacentMask & Block.getBlockTypeOcclusionData(NORTH_SOUTH_WALL, WEST)) != 0 || Block.isUpDownFenceType(Block.getBlockType(adjacentBlock))))
                index |= 2;

            adjacentBlock = Chunk.getBlockInWorld(x, y, z - 1);
            adjacentMask = Block.isLiquidType(Block.getBlockType(adjacentBlock)) ? 0L : Block.getBlockOcclusionData(adjacentBlock, NORTH);
            if (((adjacentMask & Block.getBlockTypeOcclusionData(EAST_WEST_WALL, SOUTH)) != 0 || Block.isUpDownFenceType(Block.getBlockType(adjacentBlock))))
                index |= 4;

            adjacentBlock = Chunk.getBlockInWorld(x - 1, y, z);
            adjacentMask = Block.isLiquidType(Block.getBlockType(adjacentBlock)) ? 0L : Block.getBlockOcclusionData(adjacentBlock, WEST);
            if (((adjacentMask & Block.getBlockTypeOcclusionData(NORTH_SOUTH_WALL, EAST)) != 0 || Block.isUpDownFenceType(Block.getBlockType(adjacentBlock))))
                index |= 8;

            return UP_DOWN_FENCE + index | waterLogged;
        }
        if (Block.isEastWestFenceType(blockType)) {
            int index = 0;
            short adjacentBlock;
            long adjacentMask;

            adjacentBlock = Chunk.getBlockInWorld(x, y, z + 1);
            adjacentMask = Block.isLiquidType(Block.getBlockType(adjacentBlock)) ? 0L : Block.getBlockOcclusionData(adjacentBlock, SOUTH);
            if (((adjacentMask & Block.getBlockTypeOcclusionData(UP_DOWN_WALL, NORTH)) != 0 || Block.isEastWestFenceType(Block.getBlockType(adjacentBlock))))
                index |= 1;

            adjacentBlock = Chunk.getBlockInWorld(x, y + 1, z);
            adjacentMask = Block.isLiquidType(Block.getBlockType(adjacentBlock)) ? 0L : Block.getBlockOcclusionData(adjacentBlock, BOTTOM);
            if (((adjacentMask & Block.getBlockTypeOcclusionData(NORTH_SOUTH_WALL, TOP)) != 0 || Block.isEastWestFenceType(Block.getBlockType(adjacentBlock))))
                index |= 2;

            adjacentBlock = Chunk.getBlockInWorld(x, y, z - 1);
            adjacentMask = Block.isLiquidType(Block.getBlockType(adjacentBlock)) ? 0L : Block.getBlockOcclusionData(adjacentBlock, NORTH);
            if (((adjacentMask & Block.getBlockTypeOcclusionData(UP_DOWN_WALL, SOUTH)) != 0 || Block.isEastWestFenceType(Block.getBlockType(adjacentBlock))))
                index |= 4;

            adjacentBlock = Chunk.getBlockInWorld(x, y - 1, z);
            adjacentMask = Block.isLiquidType(Block.getBlockType(adjacentBlock)) ? 0L : Block.getBlockOcclusionData(adjacentBlock, TOP);
            if (((adjacentMask & Block.getBlockTypeOcclusionData(NORTH_SOUTH_WALL, BOTTOM)) != 0 || Block.isEastWestFenceType(Block.getBlockType(adjacentBlock))))
                index |= 8;

            return EAST_WEST_FENCE + index | waterLogged;
        }
        return blockType | waterLogged;
    }

    private static void addCorrectEvents(int x, int y, int z) {
        short block = Chunk.getBlockInWorld(x, y, z);
        int properties = Block.getBlockProperties(block);
        byte blockTypeData = Block.getBlockTypeData(block);

        if ((blockTypeData & SMART_BLOCK_TYPE) != 0)
            add(new BlockEvent(x, y, z, SMART_BLOCK_EVENT), EngineManager.getTick());
        if ((properties & HAS_GRAVITY) != 0)
            add(new BlockEvent(x, y, z, GRAVITY_BLOCK_FALL_EVENT), EngineManager.getTick() + 1);
        if (Block.isWaterLogged(block)) add(new BlockEvent(x, y, z, WATER_FLOW_EVENT), EngineManager.getTick() + 4);
        if (Block.isLavaBlock(block)) add(new BlockEvent(x, y, z, LAVA_FLOW_EVENT), EngineManager.getTick() + 8);
        if ((properties & REQUIRES_BOTTOM_SUPPORT) != 0 || (properties & REQUIRES_AND_SIDE_SUPPORT) != 0)
            add(new BlockEvent(x, y, z, UNSUPPORTED_BLOCK_BREAK_EVENT), EngineManager.getTick() + 1);
    }

    private static void flickDoors(int x, int y, int z) {
        short currentBlock = Chunk.getBlockInWorld(x, y, z);
        Vector3f position = GameLogic.getPlayer().getCamera().getPosition();
        Launcher.getSound().playRandomSound(Block.getFootstepsSound(currentBlock), position.x, position.y, position.z, 0.0f, 0.0f, 0.0f, MISCELLANEOUS_GAIN);
        int currentY = y;
        int doorType = currentBlock & BLOCK_TYPE_MASK;
        int nextDoorType = Block.getOpenClosedDoorType(doorType);

        while (Block.isDoorType(currentBlock) && (currentBlock & BLOCK_TYPE_MASK) == doorType) {
            int baseBlock = currentBlock & (BASE_BLOCK_MASK | WATER_LOGGED_MASK);
            GameLogic.placeBlock((short) (baseBlock | nextDoorType), x, currentY, z, false);
            currentY--;
            currentBlock = Chunk.getBlockInWorld(x, currentY, z);
        }
        currentY = y + 1;
        currentBlock = Chunk.getBlockInWorld(x, currentY, z);
        while (Block.isDoorType(currentBlock) && (currentBlock & BLOCK_TYPE_MASK) == doorType) {
            int baseBlock = currentBlock & (BASE_BLOCK_MASK | WATER_LOGGED_MASK);
            GameLogic.placeBlock((short) (baseBlock | nextDoorType), x, currentY, z, false);
            currentY++;
            currentBlock = Chunk.getBlockInWorld(x, currentY, z);
        }
    }

    private static void executeUnsupportedBlockBreakEvent(BlockEvent event) {
        short block = Chunk.getBlockInWorld(event.x, event.y, event.z);
        if (Block.isSupported(block, event.x, event.y, event.z)) return;

        GameLogic.placeBlock(AIR, event.x, event.y, event.z, true);
    }

    private static void executeWaterFlowEvent(BlockEvent event) {
        short block = Chunk.getBlockInWorld(event.x, event.y, event.z);
        short blockBelow = Chunk.getBlockInWorld(event.x, event.y - 1, event.z);
        if (!Block.isWaterLogged(block)) return;

        if (!Block.isWaterSupported(block, event.x, event.y, event.z)) {
            short nextWaterLevel = block == FLOWING_WATER_LEVEL_1 ? AIR : (short) (block + 1);
            GameLogic.placeBlock(nextWaterLevel, event.x, event.y, event.z, false);
        }

        if (!Block.isWaterBlock(block)) {
            short nextWaterLevel = FLOWING_WATER_LEVEL_7;

            short adjacentBlock = Chunk.getBlockInWorld(event.x + 1, event.y, event.z);
            if (Block.canWaterFlow(block, adjacentBlock, blockBelow, EAST) && Block.getBlockOcclusionData(block, WEST) != -1L)
                GameLogic.placeBlock(nextWaterLevel, event.x + 1, event.y, event.z, false);

            adjacentBlock = Chunk.getBlockInWorld(event.x - 1, event.y, event.z);
            if (Block.canWaterFlow(block, adjacentBlock, blockBelow, WEST) && Block.getBlockOcclusionData(block, EAST) != -1L)
                GameLogic.placeBlock(nextWaterLevel, event.x - 1, event.y, event.z, false);

            adjacentBlock = Chunk.getBlockInWorld(event.x, event.y, event.z + 1);
            if (Block.canWaterFlow(block, adjacentBlock, blockBelow, SOUTH) && Block.getBlockOcclusionData(block, NORTH) != -1L)
                GameLogic.placeBlock(nextWaterLevel, event.x, event.y, event.z + 1, false);

            adjacentBlock = Chunk.getBlockInWorld(event.x, event.y, event.z - 1);
            if (Block.canWaterFlow(block, adjacentBlock, blockBelow, NORTH) && Block.getBlockOcclusionData(block, SOUTH) != -1L)
                GameLogic.placeBlock(nextWaterLevel, event.x, event.y, event.z - 1, false);

            if (Block.canWaterFlow(block, blockBelow, blockBelow, TOP) && Block.getBlockOcclusionData(block, BOTTOM) != -1L)
                GameLogic.placeBlock(FLOWING_WATER_LEVEL_8, event.x, event.y - 1, event.z, false);
        } else {

            if (block != FLOWING_WATER_LEVEL_1) {
                short nextWaterLevel = block == WATER_SOURCE ? FLOWING_WATER_LEVEL_7 : (short) (block + 1);

                short adjacentBlock = Chunk.getBlockInWorld(event.x + 1, event.y, event.z);
                if (Block.canWaterFlow(block, adjacentBlock, blockBelow, EAST))
                    GameLogic.placeBlock(nextWaterLevel, event.x + 1, event.y, event.z, false);

                adjacentBlock = Chunk.getBlockInWorld(event.x - 1, event.y, event.z);
                if (Block.canWaterFlow(block, adjacentBlock, blockBelow, WEST))
                    GameLogic.placeBlock(nextWaterLevel, event.x - 1, event.y, event.z, false);

                adjacentBlock = Chunk.getBlockInWorld(event.x, event.y, event.z + 1);
                if (Block.canWaterFlow(block, adjacentBlock, blockBelow, SOUTH))
                    GameLogic.placeBlock(nextWaterLevel, event.x, event.y, event.z + 1, false);

                adjacentBlock = Chunk.getBlockInWorld(event.x, event.y, event.z - 1);
                if (Block.canWaterFlow(block, adjacentBlock, blockBelow, NORTH))
                    GameLogic.placeBlock(nextWaterLevel, event.x, event.y, event.z - 1, false);
            }

            if (Block.canWaterFlow(block, blockBelow, blockBelow, TOP))
                GameLogic.placeBlock(FLOWING_WATER_LEVEL_8, event.x, event.y - 1, event.z, false);
        }
    }

    private static void executeLavaFlowEvent(BlockEvent event) {
        short block = Chunk.getBlockInWorld(event.x, event.y, event.z);
        short blockBelow = Chunk.getBlockInWorld(event.x, event.y - 1, event.z);
        if (!Block.isLavaBlock(block)) return;

        if (!Block.isLavaSupported(block, event.x, event.y, event.z)) {
            short nextLavaLevel = block == FLOWING_LAVA_LEVEL_1 ? AIR : (short) (block + 1);
            GameLogic.placeBlock(nextLavaLevel, event.x, event.y, event.z, false);
        }


        if (block != FLOWING_LAVA_LEVEL_1) {
            short nextLavaLevel = block == LAVA_SOURCE ? FLOWING_LAVA_LEVEL_3 : (short) (block + 1);

            short adjacentBlock = Chunk.getBlockInWorld(event.x + 1, event.y, event.z);
            if (Block.canLavaFlow(block, adjacentBlock, blockBelow, EAST))
                GameLogic.placeBlock(nextLavaLevel, event.x + 1, event.y, event.z, false);

            adjacentBlock = Chunk.getBlockInWorld(event.x - 1, event.y, event.z);
            if (Block.canLavaFlow(block, adjacentBlock, blockBelow, WEST))
                GameLogic.placeBlock(nextLavaLevel, event.x - 1, event.y, event.z, false);

            adjacentBlock = Chunk.getBlockInWorld(event.x, event.y, event.z + 1);
            if (Block.canLavaFlow(block, adjacentBlock, blockBelow, SOUTH))
                GameLogic.placeBlock(nextLavaLevel, event.x, event.y, event.z + 1, false);

            adjacentBlock = Chunk.getBlockInWorld(event.x, event.y, event.z - 1);
            if (Block.canLavaFlow(block, adjacentBlock, blockBelow, NORTH))
                GameLogic.placeBlock(nextLavaLevel, event.x, event.y, event.z - 1, false);
        }

        if (Block.canLavaFlow(block, blockBelow, blockBelow, TOP))
            GameLogic.placeBlock(FLOWING_LAVA_LEVEL_4, event.x, event.y - 1, event.z, false);

    }

    private static void executeGravityBlockFallEvent(BlockEvent event) {
        short block = Chunk.getBlockInWorld(event.x, event.y, event.z);
        if ((Block.getBlockProperties(block) & HAS_GRAVITY) == 0) return;
        if ((block & 0xFFFF) < STANDARD_BLOCKS_THRESHOLD || (block & BLOCK_TYPE_MASK) != FULL_BLOCK) return;
        if ((Block.getBlockProperties(Chunk.getBlockInWorld(event.x, event.y - 1, event.z)) & REPLACEABLE) == 0) return;

        FallingBlockEntity entity = new FallingBlockEntity(new Vector3f(event.x + 0.5f, event.y + 0.5f, event.z + 0.5f), new Vector3f(0.0f, 0.0f, 0.0f));
        GameLogic.spawnEntity(entity);
        GameLogic.placeBlock(AIR, event.x, event.y, event.z, false);
    }

    private static final ArrayList<EventQueue> events = new ArrayList<>();
}
