package com.bioaid;

/**
 * Created by jaro on 6/8/13.
 */
public class MOCsim extends AlgoProcessor {
    ParameterContextModel shared_pars_ref;

    CircularBuffer<Float> circBuff_ptr;

    int index; //Lexical-cast this to get parameters
    float sampleRate, tc, latency, factor, thresh_dB, thresh_pa;
    boolean isStereo;

    boolean isSample2of2;
    float stereoAccumulator;

    OnePoleFilter MOCfilt = new OnePoleFilter();

    void calculateMOCresponse(float meanMOCpa) {
        // THIS IS THE OLD INEFFICIENT VERSION REQUIRING LOTS OF LIN<->LOG DOMAIN CONVERSION
        // HOWEVER, IT IS NECESSARY BECAUSE OF THE FILTER POSITION
        float meanMOCdB = Utils.pa2dbspl(meanMOCpa);
        meanMOCdB = Math.max(meanMOCdB - thresh_dB, 0.0f) * factor;
        meanMOCdB = MOCfilt.process(meanMOCdB);
        circBuff_ptr.push_back(Utils.db2lin(-meanMOCdB));

        // THIS IS THE NEW VERSION WITH LESS LIN<->LOG DOMAIN CONVERSION
        // SADLY, IT CANNOT BE USED BECAUSE THE SIGNAL MUST BE FILTERED IN dB DOMAIN
        // circBuff_ptr->push_back(
        //                         MOCfilt.process(
        //                         powf(   std::min(thresh_pa/meanMOCpa, 1.0f), factor   )   )  );
    }


    MOCsim(ParameterContextModel _shared_pars, int _index) {
        shared_pars_ref = _shared_pars;
        circBuff_ptr = new CircularBuffer<Float>(441);
        index = _index;
        sampleRate = 44100.0f;
        tc = 0.050f;
        latency = 0.010f;
        factor = 0.5f;
        thresh_dB = 25.0f;
        thresh_pa = Utils.dbspl2pa(thresh_dB);
        isStereo = false;
        isSample2of2 = false;
        stereoAccumulator = 0.0f;
        updatePars();

        // The MOC only needs to know about the stuff shared between both chans
        // The object itself is shared between chans and so this fact should not change!
        cS = shared_pars_ref.addSubscriber(this);

    }


    public void updatePars() {
        tc = shared_pars_ref.getParam("Band_" + index + "_MOCtc", tc);
        factor = shared_pars_ref.getParam("Band_" + index + "_MOCfactor", factor);
        latency = shared_pars_ref.getParam("Band_" + index + "_MOClatency", latency);
        sampleRate = shared_pars_ref.getParam("SampleRate", sampleRate);

        isStereo = (shared_pars_ref.getParam("IsStereo") > 0.5f); //Logical statement to get bool from float

        thresh_dB = shared_pars_ref.getParam("Band_" + index + "_MOCthreshold_dBspl", thresh_dB);
        thresh_pa = Utils.dbspl2pa(thresh_dB);

        MOCfilt.initOnePoleCoeffs(tc, 1.f / sampleRate);

        int bufferSamples = (int) (1 + Math.floor(latency * sampleRate)); // Add 1 to prevent a buffersize of zero
        circBuff_ptr.set_capacity(bufferSamples);
        for (int nn = 0; nn < bufferSamples; ++nn) {
            circBuff_ptr.push_back(1.0f); // populate with ones
        }
    }

    float process(float sigIn) {
        return sigIn * circBuff_ptr.front();
    }

    void pumpSample(float dataSample) {
        //% Before calculating attenuation for MOC loop, add a tiny DC
        //% offset to the incoming sample. This is so that Pa values of
        //% zero do not cause a value of -1000 dB SPL to be fed to the
        //% attenuation calculator. Attenuation is thresholded after
        //% filtering, so any preceding zeros in a lab-based simulation
        //% would cause the internal MOC level to drop incredibly low. At
        //% the onset of a stimulus, it takes the internal MOC vale a
        //% long time to recover. The effects of the MOC are only visible
        //% once the internal value has exceeded threshold, so zeros
        //% preceding the stimulus would increase the MOC latency. The
        //% small DC offset rectifies this problem. However, one should
        //% note that the MOC latency is intrinsically linked to the MOC
        //% time constant because of this architecture.
        dataSample += 2e-05;

        if (isStereo) {
            if (isSample2of2) {
                stereoAccumulator += Math.abs(dataSample);//utils::pa2dbspl(dataSample);
                calculateMOCresponse(stereoAccumulator / 2.f);
                //calculateMOCresponse(utils::pa2dbspl(stereoAccumulator/2.f));
            } else {
                //stereoAccumulator = utils::pa2dbspl(dataSample);
                stereoAccumulator = (float)Math.abs(dataSample);
            }
            isSample2of2 = !isSample2of2;
        } else {
            //calculateMOCresponse(utils::pa2dbspl(dataSample));
            calculateMOCresponse((float)Math.abs(dataSample));
        }
    }
}