package com.soundbyte;

import android.content.SharedPreferences;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by jaro on 6/8/13.
 */
public class BioAidFilterService {

    public static final String OUTPUT_GAIN_DB = "OutputGain_dB";
    public static final String INPUT_GAIN_DB = "InputGain_dB";
    public static final String NUMER_OF_BANDS = "NumBands";

//    public static final String BAND_3_GAIN_DB = "Band_3_Gain_dB";

    private final AidAlgo myAlgo;
    private final SharedStereoParams sharedPars;
    private final UniqueStereoParams leftPars;

    public BioAidFilterService() {
        Lock myMutex = null;// new ReentrantLock();
        sharedPars = new SharedStereoParams(myMutex);
        leftPars = new UniqueStereoParams(myMutex);
        leftPars.setParam("OutputGain_dB", 2.f);
        leftPars.setParam("InputGain_dB", 3.f);
//        leftPars.setParam("Band_3_Gain_dB", 10.f);
        sharedPars.setParam("NumBands", 4.f);
        // mono only
        myAlgo = new AidAlgo(leftPars, sharedPars, myMutex); //Supply with identical LR pars
    }

    public void setPreferences(SharedPreferences preferences) {
        sharedPars.setParam("NumBands", preferences.getInt(NUMER_OF_BANDS, 4));
        sharedPars.setParam("OutputGain_dB", preferences.getInt(OUTPUT_GAIN_DB, 1));
        sharedPars.setParam("InputGain_dB", preferences.getInt(INPUT_GAIN_DB, 1));
    }

    public void processBlock(float[] inMono, float[] outMono, int samplesCount) {
        myAlgo.processSampleBlock(inMono, outMono, samplesCount);
    }

}
