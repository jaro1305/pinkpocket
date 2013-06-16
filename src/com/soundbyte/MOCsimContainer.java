package com.soundbyte;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jaro on 6/8/13.
 */
public class MOCsimContainer extends ParamContextClient {
    //MOC related objects are freaks that do not have shared parameters
    ParameterContextModel parameterContextModel;
    private int numBands;

    List<MOCsim> MOCbands = new ArrayList<MOCsim>();


    public MOCsimContainer(ParameterContextModel parameterContextModel) {
        this.parameterContextModel = parameterContextModel;
        numBands = 6;
        initBandVector();
        updatePars();

        //This object only needs updates if the number of channels change
        cS = parameterContextModel.addSubscriber(this);
    }

    public void updatePars() {
        //Reset vector of channel objects if needed
        int oldNumBands = numBands;
        numBands = (int) parameterContextModel.getParam("NumBands", numBands);

        if (oldNumBands != numBands) {
            initBandVector();
        }
    }

    void initBandVector() {
        MOCbands.clear();

        for (int index = 0; index < numBands; index++) {
            MOCsim ptr = new MOCsim(parameterContextModel, index);
            MOCbands.add(ptr);
        }
    }


    MOCsim getMOCsim_ptr(int index) {
        return MOCbands.get(index);
    }

}