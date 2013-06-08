package com.bioaid;

/**
 * Created by jaro on 6/8/13.
 */
public abstract class AlgoProcessor extends ParamContextClient {
    abstract double process(double inputSample);
}