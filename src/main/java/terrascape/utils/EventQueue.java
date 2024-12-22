package terrascape.utils;

public class EventQueue<E> extends ArrayQueue<E> {

    public final long tick;

    public EventQueue(int capacity, long tick) {
        super(capacity);
        this.tick = tick;
    }
}
