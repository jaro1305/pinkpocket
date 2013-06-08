package com.bioaid;

/**
 * Created by jaro on 6/8/13.
 */
public class DRNLBrokenStick extends AlgoProcessor {

    ParameterContextModel unique_pars_ref;
    int index; //Lexical-cast this to get parameters
    double cmpThreshIN_pa, cmpThreshOUT;
    double DRNLb, DRNLc;

    public void updatePars() {
        cmpThreshIN_pa = Utils.dbspl2pa(unique_pars_ref.getParam("Band_" + index + "_InstantaneousCmpThreshold_dBspl"));
        DRNLc = unique_pars_ref.getParam("Band_" + index + "_DRNLc", DRNLc);

        DRNLb = Math.pow(cmpThreshIN_pa, 1.0f - DRNLc);
        cmpThreshOUT = Math.pow(10.0f, (1.0f / (1.0f - DRNLc)) * Math.log10(DRNLb));
    }

    public DRNLBrokenStick(ParameterContextModel _unique_pars, int _index) {

        unique_pars_ref = _unique_pars;

        index = _index;

        cmpThreshIN_pa = 0.3f;

        cmpThreshOUT = 100.f;

        DRNLb = 100.f;

        DRNLc = 0.2f;
        updatePars();

        //Only need a listener for unique pars in this object
        cU = unique_pars_ref.addSubscriber(this);
    }

    double process(double sigIn) {
        double abs_x = Math.abs(sigIn);
        if (abs_x > cmpThreshOUT)
            return Math.signum(sigIn) * DRNLb * Math.pow(abs_x, DRNLc);
        else
            return sigIn;
    }
};
