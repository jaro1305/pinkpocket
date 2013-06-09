package com.bioaid;

import static java.lang.Math.log10;
import static java.lang.Math.pow;
import static java.lang.Math.abs;

/**
 * Created by jaro on 6/8/13.
 */
public class Utils {


    private static final float LIN_OFFSET = 1e-20f;  // = -400 dB to protect from -Inf if log of zero attempted

    //---- STATIC METHODS -- (These should all be templatized)
    static float lin2db(float linVal) {
        return (float)(20.0f * log10(abs(linVal) + LIN_OFFSET));
    }

    ;

    static float db2lin(float dbVal) {
        return (float)(pow(10.0, dbVal / 20.0f) - LIN_OFFSET);
    }

    ;

    static float pa2dbspl(float paVal) {
        return 20.0f * (float)log10(LIN_OFFSET + abs(paVal) / 20e-6f);
    }

    ;

    static float dbspl2pa(float dbsplVal) {
        return 20e-6f * (float)pow(10.0f, dbsplVal / 20.0f) - LIN_OFFSET;
    }

    ;

    // static void DBG(const std::string x)
    //Problem with this version is that you need to lexical_cast to show values (see macro version above)
    //{
    //#ifdef DEBUG
    //    std::cout << x << std::endl; //VS2008 does not like this
    //#endif
    //}

    // This code maps a value within one range to a vlue in another scale
//    template<typename T >
//
//    static T mapVal(T ipVal,
//                    T ipMin, T ipMax,
//                    T opMin, T opMax) {
//        //ip params
//        const T ipRange = ipMax - ipMin;
//        const T ipFract = (ipVal - ipMin) / ipRange;
//
//        //op params
//        const T opRange = opMax - opMin;
//        return opMin + ipFract * opRange;
//    }
//
//    // vis helper
//    void showData(float[] L, float[] R, int numel) {
//        for (int nn = 0; nn < numel; ++nn)
//            System.out.println("L[" + nn + "]=" + L[nn] + "  ----   " + "R[" + nn + "]=" + R[nn]);
//    }
//
//    //Random float range generating functor
////call with std::generate_n(x.begin(), num_items, gen_rand(min,max));
////or with std::generate_n(std::back_inserter(x), num_items, gen_rand(min,max));
//    struct gen_rand {
//        float range, factor, norm;
//        public:
//        gen_rand( float mi = 0.f, float ma = 1.f) {
//
//            range(ma - mi), factor(range / RAND_MAX), norm(mi) {
//        }
//
//    float operator()
//    ()
//
//    {
//        return Math.random() * factor + norm;
//    }
//};


}