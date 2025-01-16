package terrascape.utils;

import terrascape.dataStorage.Chunk;
import terrascape.server.BlockEvent;

import java.util.ArrayList;

import static terrascape.utils.Constants.*;

public class EventQueue extends ArrayQueue<BlockEvent> {

    public final long tick;

    public EventQueue(int capacity, long tick) {
        super(capacity);
        this.tick = tick;
    }

    public boolean hasEventScheduledAt(int x, int y, int z) {
        for (int index = headPointer; index != tailPointer; index = incIndex(index)) {
            BlockEvent event = (BlockEvent) elements[index];
            if (event.x() == x && event.y() == y && event.z() == z) return true;
        }
        return false;
    }

    public void removeEventsInChunk(Chunk chunk, ArrayList<BlockEvent> removedEvents) {
        synchronized (this) {
            for (int index = headPointer; index != tailPointer; index = incIndex(index)) {
                BlockEvent event = (BlockEvent) elements[index];
                if (event == null) continue;
                if (event.x() >> CHUNK_SIZE_BITS == chunk.X && event.y() >> CHUNK_SIZE_BITS == chunk.Y && event.z() >> CHUNK_SIZE_BITS == chunk.Z) {
                    removedEvents.add(event);
                    elements[index] = null;
                }
            }
        }
    }
}
