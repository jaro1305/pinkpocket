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

    public static void main(String[] args) {
        CircularBuffer<Double> buffer = new CircularBuffer<Double>(3);
//        buffer.set_capacity(5);
        buffer.push_back(1D);
        System.out.println("front after 1 " + buffer.front + " " + buffer.back + " " + buffer.list + " " + buffer.front());

        buffer.push_back(2D);
        System.out.println("front after 2 " + buffer.front + " " + buffer.back + " " + buffer.list + " " + buffer.front());
        buffer.push_back(3D);
        System.out.println("front after 3 " + buffer.front + " " + buffer.back + " " + buffer.list + " " + buffer.front());

        buffer.push_back(4D);
        System.out.println("front after 4 " + buffer.front + " " + buffer.back + " " + buffer.list + " " + buffer.front());

//        System.out.println(buffer.front());
//        System.out.println(buffer.front());

        buffer.push_back(3D);
        buffer.push_back(4D);
        buffer.push_back(5D);
        buffer.push_back(6D);
        buffer.push_back(7D);

        System.out.println(buffer.front());
        System.out.println(buffer.front());

        buffer.push_back(8D);

        System.out.println(buffer.front());
        System.out.println(buffer.front());
    }

}
