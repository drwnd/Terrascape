package terrascape.server;

import org.joml.Vector3f;
import terrascape.dataStorage.Chunk;
import terrascape.entity.entities.FallingBlockEntity;
import terrascape.utils.EventQueue;

import static terrascape.utils.Constants.*;

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
                case SMART_BLOCK_EVENT -> Block.updateSmartBlock(event.x, event.y, event.z);
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

    private static void addCorrectEvents(int x, int y, int z) {
        short block = Chunk.getBlockInWorld(x, y, z);
        int properties = Block.getBlockProperties(block);
        byte blockTypeData = Block.getBlockTypeData(block);

        if ((blockTypeData & SMART_BLOCK_TYPE) != 0)
            add(new BlockEvent(x, y, z, SMART_BLOCK_EVENT), EngineManager.getTick());
        if ((properties & HAS_GRAVITY) != 0)
            add(new BlockEvent(x, y, z, GRAVITY_BLOCK_FALL_EVENT), EngineManager.getTick() + 1);
        if (Block.isWaterLogged(block))
            add(new BlockEvent(x, y, z, WATER_FLOW_EVENT), EngineManager.getTick() + 4);
        if (Block.isLavaBlock(block))
            add(new BlockEvent(x, y, z, LAVA_FLOW_EVENT), EngineManager.getTick() + 8);
        if ((properties & REQUIRES_SUPPORT) != 0)
            add(new BlockEvent(x, y, z, UNSUPPORTED_BLOCK_BREAK_EVENT), EngineManager.getTick() + 1);
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

        FallingBlockEntity entity = new FallingBlockEntity(
                new Vector3f(event.x + 0.5f, event.y + 0.5f, event.z + 0.5f),
                new Vector3f(0.0f, 0.0f, 0.0f));
        GameLogic.spawnEntity(entity);
        GameLogic.placeBlock(AIR, event.x, event.y, event.z, false);
    }

    private static final ArrayList<EventQueue> events = new ArrayList<>();
}
