package terrascape.utils;

import terrascape.server.BlockEvent;

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
}
