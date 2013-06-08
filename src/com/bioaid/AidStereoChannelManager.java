package com.bioaid;

/**
 * Created by jaro on 6/8/13.
 */
public class AidStereoChannelManager extends AlgoProcessor {
    ParameterContextModel unique_pars_ref;
    ParameterContextModel shared_pars_ref;
    FilterBank fBank;  //Seting this const will mess with the reset
    ARsim aRsim;  //Seting this const will mess with the reset
    double inputGain, outputGain;

    public void updatePars() {
        //DBGM("Update callback in: cAidStereoChannelManager");

        inputGain = Utils.db2lin(unique_pars_ref.getParam("InputGain_dB", Utils.lin2db(inputGain)));
        outputGain = Utils.db2lin(unique_pars_ref.getParam("OutputGain_dB", Utils.lin2db(outputGain)));
    }


    public AidStereoChannelManager(ParameterContextModel unique_pars, ParameterContextModel shared_pars,
                                   MOCsimContainer MOCsim_ref) {

        unique_pars_ref = unique_pars;

        shared_pars_ref = shared_pars;

        fBank = new FilterBank(unique_pars, shared_pars, MOCsim_ref);

        aRsim = new ARsim(unique_pars, shared_pars);

        inputGain = 1.f;

        outputGain = 1.f;

        updatePars();

// This object only has gain for each stereo-chan and so it only needs to listen to unique_pars changes
            cU = unique_pars.addSubscriber(this);
            cS = unique_pars.addSubscriber(this);
        }

        double process (double input){
            double tmp = inputGain * input; //Apply the input gain
            tmp = aRsim.process(tmp); // Apply any AR compression
            System.out.println("aRsim = " + tmp);
            tmp = fBank.process(tmp); // Do sub-band processing
            System.out.println("fBank = " + tmp);

            if (aRsim.getThresh_pa() < 1000.f) //ONly bother pumping samples if AR threshold is less than about 150 dB
                aRsim.pumpSample(tmp); // Update the AR processor with current broadband output level
            return tmp * outputGain; // Apply output gain
        }
    }

    ;


