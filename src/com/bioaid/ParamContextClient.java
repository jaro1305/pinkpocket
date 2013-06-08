package com.bioaid;

/**
 * Created by jaro on 6/8/13.
 */
public class ParamContextClient {
    // These are the slots for signal change notifications for
    // (S)hared and (U)nique parameters.
    // Each subclass can choose whether or not to register with the signals
    // depending on whether it needs to update its internal parameters.
    ParamContextClient cS, cU;
//    boost::signals2::scoped_connection cS, cU;

    // We cannot put these references in the superclass as some components only need one
    // or the other.
    //cParameterContextModel& unique_pars_ref;
    //cParameterContextModel& shared_pars_ref;

    // Every subclass should have an individualised method to store its own parameters
    public void updatePars() {

    }

};


