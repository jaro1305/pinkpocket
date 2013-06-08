package com.bioaid;

import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;

/**
 * Created by jaro on 6/8/13.
 */
public class ARsim extends AlgoProcessor {
    ParameterContextModel unique_pars_ref;
    ParameterContextModel shared_pars_ref;

    CircularBuffer<Double> circBuff_ptr;

    double sampleRate, tc, latency, thresh_pa;

    final OnePoleFilter aRfilt = new OnePoleFilter();

    public ARsim(ParameterContextModel unique_pars, ParameterContextModel shared_pars) {
        unique_pars_ref = unique_pars;
        shared_pars_ref = shared_pars;
        circBuff_ptr = new CircularBuffer(441);
        sampleRate = 44100.0f;
        tc = 0.005f;
        latency = 0.010f;
        thresh_pa = 2.f;
        updatePars();

        //Need a listener for both here as updatePars requires both

        cS = shared_pars_ref.addSubscriber(this);
        cU = unique_pars_ref.addSubscriber(this);

        aRfilt.initOnePoleCoeffs(tc, 1.f / sampleRate);
    }

    public void updatePars() {
        tc = unique_pars_ref.getParam("ARtc", tc);
        latency = unique_pars_ref.getParam("ARlatency", latency);
        sampleRate = shared_pars_ref.getParam("SampleRate", sampleRate);

        thresh_pa = Utils.dbspl2pa(unique_pars_ref.getParam("ARthreshold_dBSPL"));

        //TODO -> if statementes here
        aRfilt.initOnePoleCoeffs(tc, 1.f / sampleRate);

        int bufferSamples = 1 + (int)floor(latency * sampleRate); // Add 1 to prevent a buffersize of zero
        circBuff_ptr.set_capacity(bufferSamples);
        for (int nn = 0; nn < bufferSamples; ++nn) {
            circBuff_ptr.push_back(1.0D); // populate with ones
        }
    }


    double getThresh_pa() {
        return thresh_pa;
    }

    double process(double sigIn) {
        return sigIn / circBuff_ptr.front();
    }

    void pumpSample(double dataSample) {
        double tmp = aRfilt.process(dataSample * dataSample); // Smooth power
        tmp = sqrt(tmp) / thresh_pa; // RMS relative to threshold
        tmp = max (tmp, 1.0f); // Stop AR giving gain
        circBuff_ptr.push_back(tmp);
    }

}
