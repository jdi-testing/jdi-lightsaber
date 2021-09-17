package com.jdiai.tools;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static java.lang.Math.max;

public class FixedQueue<T> {
    protected Queue<T> queue;
    protected int limit;

    public FixedQueue(int limit) {
        this.limit = max(limit, 0);
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
        return new ArrayList<>(queue);
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
