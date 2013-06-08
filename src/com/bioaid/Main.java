package com.bioaid;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by jaro on 6/8/13.
 */
public class Main {

    public static void main(String args[]) {
        //DBGM(RAND_MAX);
        int numSamples = 10;

        // the () at the end = zero init like calloc
        // * this was wrapped in boost::scoped_array
        double[] lDataIn = new double[numSamples];
        double[] rDataIn = new double[numSamples];
        double[] lDataOut = new double[numSamples];
        double[] rDataOut = new double[numSamples];

        lDataIn[0] = rDataIn[0] = 1.0f; // make impulse
        //std::generate_n(&lDataIn[0], numSamples, gen_rand(10.f,11.f));

        // Make the data look like 2D C-style arrays (the process mathod requires data in this format)
        // ..this fits with other audio APIs like VST, even if it is a bit of an eye-bleeder
        double[] plDataIn = lDataIn;
        double[] prDataIn = rDataIn;
        double[] plDataOut = lDataOut;
        double[] prDataOut = rDataOut;
        double in2D[][] = new double[][]{plDataIn, prDataIn};
        double out2D[][] = new double[][]{plDataOut, prDataOut};

        showData(lDataIn, rDataIn, numSamples);

        {
            //      _                        _
            //   __| | ___ _ __ ___   ___   / |
            //  / _` |/ _ \ '_ ` _ \ / _ \  | |
            // | (_| |  __/ | | | | | (_) | | |
            //  \__,_|\___|_| |_| |_|\___/  |_|
            // Stereo  in/out

            System.out.println("\n\n Demo 1 \n\n");

            // Creating and passing in a Mutex is useless in this (single threaded) demo, but it demostrates the syntax.
            // It is fine to just omit the arg (see demo_2).
            //boost::mutex myMutex;
            ReentrantLock myMutex = new ReentrantLock();

            SharedStereoParams sharedPars = new SharedStereoParams(myMutex);
            UniqueStereoParams leftPars = new UniqueStereoParams(myMutex);
            UniqueStereoParams rightPars = new UniqueStereoParams(myMutex);
            AidAlgo myAlgo = new AidAlgo(leftPars, rightPars, sharedPars, myMutex); //Supply with identical LR pars


            myAlgo.processSampleBlock(in2D, 2, out2D, 2, numSamples);
            showData(lDataOut, rDataOut, numSamples);

            // Now change a parameter in one channel, process and see the new output
            assert (!rightPars.setParam("OutputGain_dB", 6.f));  // Returns a bool letting you know if the parameter you're trying to set exists. Need to replace with exception.
            myAlgo.processSampleBlock(in2D, 2, out2D, 2, numSamples);
            showData(lDataOut, rDataOut, numSamples);
        }
        System.out.println("\n\n Demo 1 END \n\n");

        // Use some randome numbers for the left channel instead ...
//        std::generate_n ( & lDataIn[0], numSamples, gen_rand(-1.f, 1.f));
        generate_n (lDataIn, numSamples, -1.f, 1.f);



        showData(lDataIn, rDataIn, numSamples);

        {
            //      _                        ____
            //   __| | ___ _ __ ___   ___   |___ \
            //  / _` |/ _ \ '_ ` _ \ / _ \    __) |
            // | (_| |  __/ | | | | | (_) |  / __/
            //  \__,_|\___|_| |_| |_|\___/  |_____|
            // Mono in with dual out
            Lock myMutex = new ReentrantLock();
            System.out.println("\n\n Demo 2\n\n");
            SharedStereoParams sharedPars = new SharedStereoParams(myMutex);
            UniqueStereoParams leftPars = new UniqueStereoParams(myMutex);
            assert (!leftPars.setParam("OutputGain_dB", 6.f));
            assert (!leftPars.setParam("InputGain_dB", 6.f));
            assert (!leftPars.setParam("Band_3_Gain_dB", 10.f));
            assert (!sharedPars.setParam("NumBands", 4.f));
            AidAlgo myAlgo = new AidAlgo(leftPars, sharedPars, myMutex); //Supply with identical LR pars
            myAlgo.processSampleBlock(in2D, 1, out2D, 2, numSamples);
            showData(lDataOut, rDataOut, numSamples);
        }
        System.out.println("\n\n Demo 2 END \n\n");

    }

    private static void generate_n(double[] v, int numSamples, float min, float max) {
        for (int i=0; i < numSamples; i++) {
            double rnd = (Math.random() * (Math.abs(min) + Math.abs(max))) - Math.abs(min);
            v[i] = rnd;
        }

    }

    static void showData(double[] L, double[] R, int numel) {
        for (int nn = 0; nn < numel; ++nn)
            System.out.println( "L[" + nn + "]=" + L[nn] + "  ----   "
                    + "R[" + nn + "]=" + R[nn] + "\n");
    }

}