package com.bioaid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by jaro on 6/8/13.
 */
public class CircularBuffer<T> {

    LinkedList<T> list = new LinkedList<T>();
    int capacity;

    public CircularBuffer(int size) {
        capacity = size;
    }

    public void set_capacity(int capacity) {
        this.capacity = capacity;
    }

    public void push_back(T v) {
        list.add(v);
        if (list.size() > capacity)
            list.remove(0);
    }

    public T front() {
        return list.get(0);
    }

    public static void main(String[] args) {
            CircularBuffer<Integer> cb = new CircularBuffer<Integer>(3);
            //cb.set_capacity(5);
            cb.push_back(1);
            System.out.println("front after 1 " + cb.front());
            cb.push_back(2);
            System.out.println("front after 2 " + cb.front());
            cb.push_back(3);
            System.out.println("front after 3 " + cb.front());
//            std::cout << "0 " << cb[0] << std::endl;
//            std::cout << "1 " << cb[1] << std::endl;
//            std::cout << "2 " << cb[2] << std::endl;

            cb.push_back(4);
            System.out.println("front after 4 " + cb.front());
//            std::cout << "0 " << cb[0] << std::endl;
//            std::cout << "1 " << cb[1] << std::endl;
//            std::cout << "2 " << cb[2] << std::endl;

            System.out.println(cb.front());
            System.out.println(cb.front());

            cb.push_back(3);
            cb.push_back(4);
            cb.push_back(5);
            cb.push_back(6);
            cb.push_back(7);

            System.out.println(cb.front());
            System.out.println(cb.front());

            cb.push_back(8);

            System.out.println(cb.front());
            System.out.println(cb.front());

    }

}
