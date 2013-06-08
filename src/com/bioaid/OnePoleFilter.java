package com.bioaid;

/**
 * Created by jaro on 6/8/13.
 */
public class OnePoleFilter {
    public static final double FILTER_DENORMAL_PROTECTOR = 1e-20f;
    private double a1, b0, m, dn;

    void initOnePoleCoeffs(double tc, double dt) {
        if ((tc / dt) < 44.0f) // just under 1ms for 44.1 kHz
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
    void process(double[] sigIn, double[] sigOut, int numel) {
        for (int nn = 0; nn < numel; ++nn) {
            sigOut[nn] = process(sigIn[nn]);
        }

    }

    double process(double sigIn) {
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
    double get_a1() {
        return a1;
    }

    double get_b0(){
        return b0;
    }

}
