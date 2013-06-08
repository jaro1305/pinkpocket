package com.bioaid;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by jaro on 6/8/13.
 */
public class FilterBank extends AlgoProcessor {
    ParameterContextModel unique_pars_ref;
    ParameterContextModel shared_pars_ref;

    int numBands;

    MOCsimContainer MOCsim_ref;

//    typedef boost::shared_ptr<cFilterFrequencyBand> band_ptr; //must be shared if we stuff into vector
//    std::vector<band_ptr> chans;
    List<FilterFrequencyBand> chans = new ArrayList<FilterFrequencyBand>();

    public void updatePars() {
        //Reset vector of channel objects if needed
        int OLDnumBands = numBands;
        numBands = (int) shared_pars_ref.getParam("NumBands", (float) numBands);

        if (OLDnumBands != numBands) {
            initBandVector();
        }
    }

    ;


    void initBandVector() {
        chans.clear();
        //Don't lock here either as it is called from update pars!
        for (int index = 0; index < numBands; index++) {
            FilterFrequencyBand ptr = new FilterFrequencyBand(unique_pars_ref, shared_pars_ref, index, MOCsim_ref.getMOCsim_ptr(index));
            chans.add(ptr);
        }
    }

    public FilterBank(ParameterContextModel unique_pars, ParameterContextModel shared_pars, MOCsimContainer _MOCsim_ref) {
        unique_pars_ref = unique_pars;

        shared_pars_ref = shared_pars;

        numBands = 0; //Must be initialized with 0 as it refers to MOCsim objects. If you default it to 6 and there are only 3 MOCsim objects then you'll point to garbage and crash.

        this.MOCsim_ref = _MOCsim_ref;
        initBandVector();
        updatePars();

        //This object only needs to know about changes to the numer of channels, so it only needs to know about shared_pars
        cS = shared_pars.addSubscriber(this);

    }

    double process(double inputSample) {
        double accumulatedOutput = 0.f;
        for (int index = 0; index < numBands; index++) {
            accumulatedOutput += chans.get(index).process(inputSample);
        }
        return accumulatedOutput;
    }

}
