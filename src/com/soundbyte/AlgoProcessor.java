package com.soundbyte;

/**
 * Created by jaro on 6/8/13.
 */
public abstract class AlgoProcessor extends ParamContextClient {
    abstract float process(float inputSample);
}