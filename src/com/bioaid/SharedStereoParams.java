package com.bioaid;

import com.bioaid.app.MainActivity;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Math.pow;

/**
 * Created by jaro on 6/8/13.
 */
public class SharedStereoParams extends ParameterContextModel {

    private final int MAX_NUM_BANDS_EVER = 50;

    SharedStereoParams(Lock _pMyMutex) {
        super(_pMyMutex);
        populateDefaultPars();
    }

    @Override
    void populateDefaultPars() {
        paramMap.put("SampleRate", (float)MainActivity.AUDIO_SAMPLING_RATE);//44100.f);
        paramMap.put("IsStereo", 0.0f);
        paramMap.put("NumBands", 6.f);
        for (int nn = 0; nn < MAX_NUM_BANDS_EVER; ++nn) {

            double cf = 250.f * pow(2.f, (float) nn);
            double bw = 1.f;
            paramMap.put("Band_" + (nn) + "_LowBandEdge", (float) (cf * pow(2.f, -bw / 2.f)));
            paramMap.put("Band_" + (nn) + "_HighBandEdge", (float) (cf * pow(2.f, bw / 2.f)));

            paramMap.put("Band_" + (nn) + "_MOCtc", 0.050f);
            paramMap.put("Band_" + (nn) + "_MOCfactor", 0.8f);
            paramMap.put("Band_" + (nn) + "_MOClatency", 0.010f);
            paramMap.put("Band_" + (nn) + "_MOCthreshold_dBspl", 25.0f);
        }
    }


}