package com.bioaid;

import java.util.LinkedList;

/**
 * Created by jaro on 6/8/13.
 */
public class CircularBuffer<T> {

    LinkedList<T> list = new LinkedList<T>();

    public CircularBuffer(int size) {

    }

    public void set_capacity(int capacity) {
    }

    public void push_back(T v) {
        list.push(v);
    }

    public T front() {
        return list.pop();
    }

    public static void main(String[] args) {
        CircularBuffer<Double> buffer = new CircularBuffer<Double>(3);
        buffer.set_capacity(5);
        buffer.push_back(0D);
        buffer.push_back(1D);

        System.out.println(buffer.front());
        System.out.println(buffer.front());

        buffer.push_back(2D);
        buffer.push_back(3D);
        buffer.push_back(4D);
        buffer.push_back(5D);
        buffer.push_back(6D);

        System.out.println(buffer.front());
        System.out.println(buffer.front());

        buffer.push_back(7D);

        System.out.println(buffer.front());
        System.out.println(buffer.front());
    }

}
