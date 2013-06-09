package com.soundbyte;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * Created by jaro on 6/8/13.
 */


public abstract class ParameterContextModel {

    Map<String, Float> paramMap = new HashMap<String, Float>();
    List<ParamContextClient> listeners = new ArrayList<ParamContextClient>();

    abstract void populateDefaultPars();

    NullCheckingScopedLock pMyMutex; //option lock applied before sending signals

    public ParameterContextModel(Lock _pMyMutex) { //cTor taking optional mutex
        pMyMutex = new NullCheckingScopedLock(_pMyMutex);
    }

    ParamContextClient addSubscriber(ParamContextClient slot) {
        listeners.add(slot);
        return slot;
    }

    float getParam(String key) {
        return getParam(key, 0F); // was null in C++
    }

    float getParam(String key, float defaultVal) {
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

            pMyMutex.lock(); //Lock this as short as possible
            try {
                for (ParamContextClient listener : listeners) {
                    listener.updatePars();
                }
            } finally {
                pMyMutex.unlock();
            }
        }

        if (isNewKey) {
            return true; //Returning true indicates the key doesnt exist (this can be interpreted as an error or just ignored).
        } else {
            return false; //End of function reached without error (again, the return can be ignored)
        }

    }
}
