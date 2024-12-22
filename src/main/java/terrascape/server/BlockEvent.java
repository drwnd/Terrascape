package terrascape.server;

import org.joml.Vector3f;
import terrascape.dataStorage.Chunk;
import terrascape.entity.entities.FallingBlockEntity;
import terrascape.utils.EventQueue;

import static terrascape.utils.Constants.*;

import java.util.ArrayList;
import java.util.Iterator;

public record BlockEvent(int x, int y, int z, byte type) {

    public static final byte CHECK_EVENT = 0;
    public static final byte GRAVITY_BLOCK_FALL_EVENT = 1;
    public static final byte WATER_FLOW_EVENT = 2;
    public static final byte LAVA_FLOW_EVENT = 3;
    public static final byte SMART_BLOCK_EVENT = 4;

    public static void add(BlockEvent event, long tick) {
        if (tick < EngineManager.getTick()) {
            System.err.println("Event scheduled in the past");
            throw new RuntimeException(String.valueOf(EngineManager.getTick() - tick));
        }

        for (EventQueue<BlockEvent> queue : events)
            if (queue.tick == tick) {
                queue.enqueue(event);
                return;
            }

        EventQueue<BlockEvent> queue = new EventQueue<>(10, tick);
        queue.enqueue(event);
        events.add(queue);
    }

    public static void execute(long tick) {
        EventQueue<BlockEvent> queue = null;

        for (Iterator<EventQueue<BlockEvent>> iterator = events.iterator(); iterator.hasNext(); ) {
            EventQueue<BlockEvent> currentQueue = iterator.next();
            if (currentQueue.tick == tick) {
                queue = currentQueue;
                break;
            } else if (currentQueue.tick < tick) {
                System.out.println(currentQueue.size() + " Events were missed");
                iterator.remove();
            }
        }

        if (queue == null) return;

        while (!queue.isEmpty()) {
            BlockEvent event = queue.dequeue();
            switch (event.type) {
                case CHECK_EVENT -> executeCheckEvent(event);
                case SMART_BLOCK_EVENT -> Block.updateSmartBlock(event.x, event.y, event.z);
                case GRAVITY_BLOCK_FALL_EVENT -> executeGravityBlockFallEvent(event);
            }
        }
        events.remove(queue);
    }

    public static void updateSurrounding(int x, int y, int z) {
        add(new BlockEvent(x + 1, y, z, CHECK_EVENT), EngineManager.getTick());
        add(new BlockEvent(x - 1, y, z, CHECK_EVENT), EngineManager.getTick());
        add(new BlockEvent(x, y + 1, z, CHECK_EVENT), EngineManager.getTick());
        add(new BlockEvent(x, y - 1, z, CHECK_EVENT), EngineManager.getTick());
        add(new BlockEvent(x, y, z + 1, CHECK_EVENT), EngineManager.getTick());
        add(new BlockEvent(x, y, z - 1, CHECK_EVENT), EngineManager.getTick());
    }

    private static void executeCheckEvent(BlockEvent event) {
        short block = Chunk.getBlockInWorld(event.x, event.y, event.z);
        if ((Block.getBlockTypeData(block) & SMART_BLOCK_TYPE) != 0) Block.updateSmartBlock(event.x, event.y, event.z);
        else if ((Block.getBlockProperties(block) & HAS_GRAVITY) != 0)
            add(new BlockEvent(event.x, event.y, event.z, GRAVITY_BLOCK_FALL_EVENT), EngineManager.getTick() + 1);
    }

    private static void executeGravityBlockFallEvent(BlockEvent event) {
        short block = Chunk.getBlockInWorld(event.x, event.y, event.z);
        if ((block & 0xFFFF) < STANDARD_BLOCKS_THRESHOLD || (block & BLOCK_TYPE_MASK) != FULL_BLOCK) return;
        if (Block.getBlockOcclusionData(Chunk.getBlockInWorld(event.x, event.y - 1, event.z), TOP) != 0L) return;

        FallingBlockEntity entity = new FallingBlockEntity(
                new Vector3f(event.x + 0.5f, event.y + 0.5f, event.z + 0.5f),
                new Vector3f(0.0f, 0.0f, 0.0f));
        GameLogic.spawnEntity(entity);
        GameLogic.placeBlock(AIR, event.x, event.y, event.z, false);
    }

    private static final ArrayList<EventQueue<BlockEvent>> events = new ArrayList<>();
}
