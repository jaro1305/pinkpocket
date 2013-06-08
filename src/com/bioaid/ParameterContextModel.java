package com.bioaid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by jaro on 6/8/13.
 */

//typedef std::map<std::string, float> paramMap_t;

public abstract class ParameterContextModel {
//    typedef boost::signals2::signal<void(cParameterContextModel&)>paramChangeSignal_t;
//    typedef boost::signals2::connection connection_t;
//    typedef paramChangeSignal_t::slot_type paramChangeSlot_t;

    Map<String, Float> paramMap = new HashMap<String, Float>();
//    paramChangeSignal_t paramChangeSignal;
    List<ParamContextClient> listeners = new ArrayList<ParamContextClient>();

    abstract void populateDefaultPars();

    //Mutex now defined as void ptr and cast within the function to cut down on preprocessor required if MT is disabled
    Lock pMyMutex; //option lock applied before sending signals

    public ParameterContextModel(Lock _pMyMutex) { //cTor taking optional mutex
        pMyMutex = _pMyMutex;
    }


//    connection_t addSubscriber(paramChangeSlot_t slot) {
//        return paramChangeSignal.connect(slot);
//    }
    ParamContextClient addSubscriber(ParamContextClient slot) {
        listeners.add(slot);
        return slot;
    }

    double getParam(String key) {
        return getParam(key, 0F); // was null in C++
    }

    double getParam(String key, double defaultVal) {
        Float param = paramMap.get(key);
        return param == null ? defaultVal : param;
    }


    boolean isParam(String key) {
        return paramMap.containsKey(key);
    }

    boolean setParam(String key, float newVal) {
        boolean isNewKey = !isParam(key);
        boolean isNewVal = (getParam(key) != newVal); // (uses default val of function if it doesnt exist)

        if (isNewKey || isNewVal) {
            paramMap.put(key, newVal);   //Only go to the effort of updating map if something is new

            // TODO : where's the locking?
            NullCheckingScopedLock l = new NullCheckingScopedLock(pMyMutex); //Lock this as short as possible
            // TODO: not sure what's this supposed to be - looks like a listener call
            // paramChangeSignal(this); //Only go to the effort of messaging if something is new
            for (ParamContextClient listener : listeners) {
                listener.updatePars();
            }
        }

        if (isNewKey) {
            return true; //Returning true indicates the key doesnt exist (this can be interpreted as an error or just ignored).
        } else {
            return false; //End of function reached without error (again, the return can be ignored)
        }

    }


};
