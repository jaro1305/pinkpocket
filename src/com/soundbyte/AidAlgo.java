package com.soundbyte;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by jaro on 6/8/13.
 */
public class AidAlgo {
    MOCsimContainer pMOCsimContainer;    //Abstract this away, the user need not be confused by this implementation detail
    AidStereoChannelManager pManagerL;
    AidStereoChannelManager pManagerR;

    //Mutex now defined as void ptr and cast within the function to cut down on preprocessor required if MT is disabled
    NullCheckingScopedLock pMyMutex;


    //We have dual constructors here depending on whether you want a mono or a stereo processor
    //Params cannot be const because they have subscribers added
    public AidAlgo(UniqueStereoParams _lPars,
                   UniqueStereoParams _rPars,
                   SharedStereoParams _sharedPars,
                   ReentrantLock _pMyMutex) {
        pMOCsimContainer = new MOCsimContainer(_sharedPars);
        pManagerL = new AidStereoChannelManager(_lPars, _sharedPars, pMOCsimContainer);
        pManagerR = new AidStereoChannelManager(_rPars, _sharedPars, pMOCsimContainer);
        pMyMutex = new NullCheckingScopedLock(_pMyMutex);
    }

    public AidAlgo(UniqueStereoParams _lPars,
                   SharedStereoParams _sharedPars,
                   Lock _pMyMutex) {
        pMOCsimContainer = new MOCsimContainer(_sharedPars);

        pManagerL = new AidStereoChannelManager(_lPars, _sharedPars, pMOCsimContainer);
        pManagerR = null;
        pMyMutex = new NullCheckingScopedLock(_pMyMutex);

    }

    void processSampleBlock(float[] inputChannelData,
                            float[] outputChannelData,
                            int numSamples) {

        for (int nn = 0; nn < numSamples; ++nn) {
            outputChannelData[nn] = pManagerL.process(inputChannelData[nn]);
        }
    }


}
