package com.bioaid;

/**
 * Created by jaro on 6/8/13.
 */
public class FilterFrequencyBand extends AlgoProcessor {
    ParameterContextModel unique_pars_ref;
    ParameterContextModel shared_pars_ref;

    DRNLBrokenStick bs_ptr;

    MOCsim moc_ptr;

    int index; //Lexical cast this to get parameters

    double gain;

    double loCut, hiCut;
    double sampleRate;

    final ButterworthBpFilter inFilter = new ButterworthBpFilter();
    final ButterworthBpFilter outFilter = new ButterworthBpFilter();

    public void updatePars() {
        // Update filters if needed
        double OLDloCut = loCut;
        double OLDhiCut = hiCut;
        double OLDsampleRate = sampleRate;

        //Don't bother supplying default as function defaults to 0 which is fine for dB gain
        gain = Utils.db2lin(unique_pars_ref.getParam("Band_" + index + "_Gain_dB"));

        sampleRate = shared_pars_ref.getParam("SampleRate", sampleRate);
        loCut = shared_pars_ref.getParam("Band_" + index + "_LowBandEdge", loCut);
        hiCut = shared_pars_ref.getParam("Band_" + index + "_HighBandEdge", hiCut);

        if ((OLDloCut != loCut) ||
                (OLDhiCut != hiCut) ||
                (OLDsampleRate != sampleRate)) {
            inFilter.initCoeffs(1.f / sampleRate, loCut, hiCut);
            outFilter.initCoeffs(1.f / sampleRate, loCut, hiCut);
        }

    }

    ;


    public FilterFrequencyBand(ParameterContextModel unique_pars, ParameterContextModel shared_pars, int _index, MOCsim _moc_ptr) {
        unique_pars_ref = unique_pars;
        shared_pars_ref = shared_pars;
        bs_ptr = new DRNLBrokenStick(unique_pars, _index);
        moc_ptr = _moc_ptr;
        index = _index;
        gain = 1.0f;
        loCut = 9e3f;
        hiCut = 10e3f;
        sampleRate = 44.1e3f;

        updatePars();

        cU = unique_pars_ref.addSubscriber(this);
        cS = shared_pars_ref.addSubscriber(this);

    }

    ;


    double process(double inputSample) {
        double tmp = inFilter.process(inputSample);

        //Stuff between the filters!
        tmp = moc_ptr.process(tmp);
        tmp = bs_ptr.process(tmp);
        //End of stuff between the filters

        tmp = outFilter.process(tmp);
        moc_ptr.pumpSample(tmp); //Moc sample now pumped in to ring-buff after 2nd filtering

        return gain * tmp;
    }
}

