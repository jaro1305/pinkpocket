package com.bioaid;

import com.bioaid.app.MainActivity;

/**
 * Created by jaro on 6/8/13.
 */
public class OnePoleFilter {
    public static final float FILTER_DENORMAL_PROTECTOR = 1e-20f;
    private float a1, b0, m, dn;

    void initOnePoleCoeffs(float tc, float dt) {

        float freq = ((MainActivity.AUDIO_SAMPLING_RATE-1)/1000);//44.0f;
        if ((tc / dt) < freq) // just under 1ms for 44.1 kHz
            a1 = 0.0f;
        else
            a1 = dt / tc - 1.0f;

        b0 = 1.0f + a1;
        m = 0.0f; //reset memory
        dn = FILTER_DENORMAL_PROTECTOR;
    }

    ;

    //Just a utility method for processing a chunk of samples
    //Not used in BioAid
    void process(float[] sigIn, float[] sigOut, int numel) {
        for (int nn = 0; nn < numel; ++nn) {
            sigOut[nn] = process(sigIn[nn]);
        }

    }

    float process(float sigIn) {
        m = b0 * sigIn
                - a1 * m
                + dn; //build denormal protection right in

        //Turns out that flipping the dn value in a one pole filter can still cause the filter
        //to go wacko. The dn is now constant providing a miniscule dc offset, hence commented line:
        //dn = -dn; //flip denomal remover

        return m;
    }

    ;

    //Accessor interface for external envelop follower class
    float get_a1() {
        return a1;
    }

    float get_b0() {
        return b0;
    }

}
