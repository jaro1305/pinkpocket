package com.bioaid;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by jaro on 6/8/13.
 */
public class CircularBuffer<T> {

    ArrayList<T> list = new ArrayList<T>();
    int capacity;
    int front = 0;
    int back = 0;
    boolean empty = true;

    public CircularBuffer(int size) {
        capacity = size;
        for (int i = 0; i < capacity; i++) {
            list.add(null);
        }
    }

    public void set_capacity(int capacity) {
        for (int i = this.capacity; i < capacity; i++) {
            list.add(null);
        }
        this.capacity = capacity;
    }

    public void push_back(T v) {
        list.set(back, v);
        back = (back + 1) % capacity;
        if (back == front && empty) {
            front = (front + 1) % capacity;
        }
        empty = false;
    }

    public T front() {
        return list.get(front);
    }

}
