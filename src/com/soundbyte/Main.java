package com.soundbyte;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by jaro on 6/8/13.
 */
public class Main {

    public static void main(String args[]) {
        //DBGM(RAND_MAX);
        int numSamples = 100;

        // the () at the end = zero init like calloc
        // * this was wrapped in boost::scoped_array
        float[] lDataIn =  new float[numSamples];
        float[] rDataIn =  new float[numSamples];
        float[] lDataOut = new float[numSamples];
        float[] rDataOut = new float[numSamples];

        lDataIn[0] = rDataIn[0] = 1.0f; // make impulse
        //std::generate_n(&lDataIn[0], numSamples, gen_rand(10.f,11.f));

        // Make the data look like 2D C-style arrays (the process mathod requires data in this format)
        // ..this fits with other audio APIs like VST, even if it is a bit of an eye-bleeder
        float[] plDataIn = lDataIn;
        float[] plDataOut = lDataOut;
        float in2D[] = plDataIn;
        float out2D[] = plDataOut;

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
            AidAlgo myAlgo = new AidAlgo(leftPars, rightPars, sharedPars, null); //Supply with identical LR pars


            myAlgo.processSampleBlock(in2D, out2D, numSamples);
            showData(lDataOut, rDataOut, numSamples);

            // Now change a parameter in one channel, process and see the new output
            assert (!rightPars.setParam("OutputGain_dB", 6.f));  // Returns a bool letting you know if the parameter you're trying to set exists. Need to replace with exception.
            myAlgo.processSampleBlock(in2D, out2D, numSamples);
            showData(lDataOut, rDataOut, numSamples);
        }
        System.out.println("\n\n Demo 1 END \n\n");

        // Use some randome numbers for the left channel instead ...
//        std::generate_n ( & lDataIn[0], numSamples, gen_rand(-1.f, 1.f));
        generateSin(lDataIn, numSamples);
        generateSin(rDataIn, numSamples);
//        generate_n (lDataIn, numSamples, -1.f, 1.f);

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
            leftPars.setParam("OutputGain_dB", 6.f);
            leftPars.setParam("InputGain_dB", 6.f);
            leftPars.setParam("Band_3_Gain_dB", 10.f);
            sharedPars.setParam("NumBands", 4.f);
            AidAlgo myAlgo = new AidAlgo(leftPars, sharedPars, myMutex); //Supply with identical LR pars
            myAlgo.processSampleBlock(in2D, out2D, numSamples);
            showData(lDataOut, rDataOut, numSamples);
        }
        System.out.println("\n\n Demo 2 END \n\n");

    }

    private static void generateSin(float[] v, int numSamples) {
        for (int i = 0; i < numSamples; i++) {
            v[i] = (float)Math.sin(0.2D * i);
        }
    }

    private static void generate_n(double[] v, int numSamples, float min, float max) {
        for (int i = 0; i < numSamples; i++) {
            double rnd = (Math.random() * (Math.abs(min) + Math.abs(max))) - Math.abs(min);
            v[i] = rnd;
        }

    }

    static void showData(float[] L, float[] R, int numel) {
        float totalL = 0;
        float totalR = 0;
        for (int nn = 0; nn < numel; ++nn) {
            System.out.println("L[" + nn + "]=" + L[nn] + "  ----   " + "R[" + nn + "]=" + R[nn]);
            totalL += L[nn];
            totalR += R[nn];
        }
        System.out.println("totals : " + totalR + " " + totalL);
    }

}