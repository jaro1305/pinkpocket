package com.bioaid;

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
        leftPars.setParam("OutputGain_dB", 6.f);
        leftPars.setParam("InputGain_dB", 6.f);
        leftPars.setParam("Band_3_Gain_dB", 10.f);
        sharedPars.setParam("NumBands", 4.f);
        // mono only
        myAlgo = new AidAlgo(leftPars, sharedPars, myMutex); //Supply with identical LR pars
    }

    public void setPreferences(SharedPreferences preferences) {
        sharedPars.setParam("NumBands", preferences.getInt(NUMER_OF_BANDS, 4));
        sharedPars.setParam("OutputGain_dB", preferences.getInt(OUTPUT_GAIN_DB, 4));
        sharedPars.setParam("InputGain_dB", preferences.getInt(INPUT_GAIN_DB, 4));
    }

    public float[] processBlock(float[] leftChannel) {
        float[][] in2d = new float[][]{leftChannel};
        float[][] out2d = new float[][]{new float[leftChannel.length]};
        if(true) {
            myAlgo.processSampleBlock(in2d, 1, out2d, 1, leftChannel.length);
        } else {
            // Just copy the array, for testing
            for(int i = 0; i < leftChannel.length; i++) {
                out2d[0][i] = leftChannel[i];
            }
        }
        return out2d[0];
    }

    public static void main(String[] args) {

        byte b1 = (byte) 0;
        byte b2 = (byte) 0;
        System.out.println(b1 + " " + b2);
        int[] arr = new int[]{b1, b2};
        double scaled = ((((arr[0] & 0xff) << 8) + arr[1] & 0xff) / (Short.MAX_VALUE * 2D)) * 2D - 1;
        System.out.println(scaled);
    }
}
