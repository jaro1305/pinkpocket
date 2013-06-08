package com.bioaid;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jaro on 6/8/13.
 */
public class MOCsimContainer extends ParamContextClient {
    //MOC related objects are freaks that do not have shared parameters
    ParameterContextModel shared_pars_ref;
    int numBands;

    //    typedef boost::shared_ptr<cMOCsim> MOCband_ptr; //must be shared if we stuff into vector
    //    std::vector<MOCband_ptr> MOCbands;
    List<MOCsim> MOCbands = new ArrayList<MOCsim>();


    public MOCsimContainer(ParameterContextModel shared_pars) {
        shared_pars_ref = shared_pars;
        numBands = 6;
        initBandVector();
        updatePars();

        //This object only needs updates if the number of channels change
        cS = shared_pars_ref.addSubscriber(this);
    }

    public void updatePars() {
        //Reset vector of channel objects if needed
        int oldNumBands = numBands;
        numBands = (int) shared_pars_ref.getParam("NumBands", numBands);

        if (oldNumBands != numBands) {
            initBandVector();
        }
    }

    void initBandVector() {
        MOCbands.clear();

        for (int index = 0; index < numBands; index++) {
            MOCsim ptr = new MOCsim(shared_pars_ref, index);
            MOCbands.add(ptr);
        }
    }


    MOCsim getMOCsim_ptr(int index) {
        return MOCbands.get(index);
    }

};