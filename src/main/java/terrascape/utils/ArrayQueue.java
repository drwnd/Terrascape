package terrascape.utils;

import org.joml.Vector4i;

public class ArrayQueue<E> {

    protected int headPointer = 0;
    protected int tailPointer = 0;
    protected Object[] elements;

    public ArrayQueue(int capacity) {
        elements = new Object[Math.max(2, capacity)];
    }

    public void enqueue(E element) {
        if (incIndex(tailPointer) == headPointer)
            grow();
        elements[tailPointer] = element;
        tailPointer = incIndex(tailPointer);
    }

    public E dequeue() {
        E element = (E) elements[headPointer];
        elements[headPointer] = null;
        headPointer = incIndex(headPointer);
        return element;
    }

    public E front() {
        return (E) elements[headPointer];
    }

    public boolean isEmpty() {
        return headPointer == tailPointer;
    }

    public int size() {
        return headPointer <= tailPointer ? tailPointer - headPointer : elements.length + tailPointer - headPointer;
    }

    private void grow() {
        Object[] newElements = new Object[elements.length << 1];

        int index = headPointer;
        int newIndex = 0;
        while (index != tailPointer) {
            newElements[newIndex] = elements[index];
            newIndex++;
            index = incIndex(index);
        }
        elements = newElements;
        headPointer = 0;
        tailPointer = newIndex;
    }

    protected int incIndex(int index) {
        return index + 1 >= elements.length ? 0 : index + 1;
    }

    // Not part of the actual class, but it doesn't work otherwise
    // Only use if E is Vector4i, only use in LightLogic
    public boolean notContainsToRePropagatePosition(Vector4i position) {
        for (int index = headPointer; index != tailPointer; index = incIndex(index)) {
            Vector4i vec = (Vector4i) elements[index];
            if (vec.x == position.x && vec.y == position.y && vec.z == position.z && vec.w >= position.w)
                return false;
        }
        return true;
    }
}
