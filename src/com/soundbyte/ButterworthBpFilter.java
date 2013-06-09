package com.soundbyte;

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.tan;
import static java.lang.StrictMath.sqrt;

/**
 * Created by jaro on 6/8/13.
 */
public class ButterworthBpFilter {

    public static final double FILTER_DENORMAL_PROTECTOR = 1e-20f;

    double a[] = new double[5];
    double b[] = new double[5];
    double m[] = new double[5];
    double dn;

    public void initCoeffs(double dt, double fl, double fu) {
        //Use double precision during calculation and cast down to single for coeffs
        double q = Math.PI * dt * (fu - fl);
        double r = Math.PI * dt * (fu + fl);

        double N = pow(tan(q), 2.0) + sqrt(2.0) * tan(q) + 1.0;
        double M = pow(tan(q), 2.0) / N; //M after N because it depends on N
        double Oh = -cos(r) * (2.0 * sqrt(2.0) * tan(q) + 4.0) / ((cos(q)) * N);
        double P = (-2.0 * pow(tan(q), 2.0) + pow(((2.0 * cos(r)) / (cos(q))), 2.0) + 2.0) / N;
        double Q = cos(r) * (2.0 * sqrt(2.0) * tan(q) - 4.0) / (cos(q) * N);
        double R = (pow(tan(q), 2.0) - sqrt(2.0) * tan(q) + 1.0) / N;

        b[0] = M;
        b[1] = 0.0f;
        b[2] = -2.f * M;
        b[3] = 0.0f;
        b[4] = M;
        a[0] = 1.0;
        a[1] = Oh;
        a[2] = P;
        a[3] = Q;
        a[4] = R;

        m[0] = 0.0f;
        m[1] = 0.0f;
        m[2] = 0.0f;
        m[3] = 0.0f;
        m[4] = 0.0f;
        dn = FILTER_DENORMAL_PROTECTOR;
    }


    //Just a utility method for processing a chunk of samples
    //Not used in BioAid
    void process(float[] sigIn, float[] sigOut, int numel) {
        for (int nn = 0; nn < numel; ++nn) {
            sigOut[nn] = process(sigIn[nn]);
        }
    }

    float process(float sigIn) {
        float w1 = (float)(sigIn - a[1] * m[1] - a[2] * m[2] - a[3] * m[3] - a[4] * m[4] + dn);
        dn = -dn;

        float sigOut = (float)(b[1] * m[1] + b[2] * m[2] + b[3] * m[3] + b[4] * m[4] + b[0] * w1);
        m[4] = m[3];
        m[3] = m[2];
        m[2] = m[1];
        m[1] = w1;

        return sigOut;
    }

};
