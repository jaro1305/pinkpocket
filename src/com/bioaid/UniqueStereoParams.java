package com.bioaid;

import java.util.concurrent.locks.Lock;

/**
 * Created by jaro on 6/8/13.
 */
public class UniqueStereoParams extends ParameterContextModel {

    private final int MAX_NUM_BANDS_EVER = 50;

    public UniqueStereoParams(Lock _pMyMutex) {
        super(_pMyMutex);
        populateDefaultPars();
    }

    @Override
    public void populateDefaultPars() {
        paramMap.clear();
        // Units are linear unless specifically qualified by the "_dB" suffix
        paramMap.put("InputGain_dB", 0.f);
        paramMap.put("OutputGain_dB", 0.f);

        paramMap.put("ARthreshold_dBSPL", 110.f);
        paramMap.put("ARtc", 0.006f);
        paramMap.put("ARlatency", 0.005f);

        for (int nn = 0; nn < MAX_NUM_BANDS_EVER/*(int)getParam("NumBands")*/; ++nn) {
            paramMap.put("Band_" + (nn) + "_InstantaneousCmpThreshold_dBspl", 65.f);
            paramMap.put("Band_" + (nn) + "_DRNLc", 0.2f);
            paramMap.put("Band_" + (nn) + "_Gain_dB", 0.0f);
        }
    }


}