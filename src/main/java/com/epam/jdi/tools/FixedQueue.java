package com.epam.jdi.tools;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class FixedQueue<T> {
    private Queue<T> queue;
    private int limit;

    public FixedQueue(int limit) {
        this.limit = limit > 0 ? limit : 0;
        queue = new LinkedList<>();
    }
    public void push(T element) {
        if (limit == 0)
            return;
        if (queue.size() >= limit) {
            queue.remove();
        }
        queue.add(element);
    }
    public List<T> values() {
        return queue.stream().collect(Collectors.toList());
    }
    public void clear() {
        queue.clear();
    }
    public int size() {
        if (limit == 0)
            return 0;
        return queue.size();
    }
    public T peek() {
        if (limit == 0)
            return null;
        T element = queue.peek();
        queue.remove();
        return element;
    }
    public T observe() {
        if (limit == 0)
            return null;
        return queue.peek();
    }
}
