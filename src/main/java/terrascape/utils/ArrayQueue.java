package terrascape.utils;

public class ArrayQueue<E> {

    private int headPointer = 0;
    private int tailPointer = 0;
    private Object[] elements;

    public ArrayQueue(int capacity) {
        elements = new Object[Math.max(2, capacity)];
    }

    public void enqueue(E element) {
        if ((tailPointer + 1) % elements.length == headPointer)
            grow();
        elements[tailPointer] = element;
        tailPointer = (tailPointer + 1) % elements.length;
    }

    public E dequeue() {
        E element = (E) elements[headPointer];
        elements[headPointer] = null;
        headPointer = (headPointer + 1) % elements.length;
        return element;
    }

    public E front() {
        return (E) elements[headPointer];
    }

    public boolean isEmpty() {
        return headPointer == tailPointer;
    }

    public int size() {
        return headPointer < tailPointer ? tailPointer - headPointer : elements.length - tailPointer + headPointer;
    }

    private void grow() {
        Object[] newElements = new Object[elements.length << 1];

        int index = headPointer;
        int newIndex = 0;
        while (index != tailPointer) {
            newElements[newIndex] = elements[index];
            newIndex++;
            index = (index + 1) % elements.length;
        }
        elements = newElements;
        headPointer = 0;
        tailPointer = newIndex;
    }
}