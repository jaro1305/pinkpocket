package com.bioaid;

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
    Lock pMyMutex;


    //We have dual constructors here depending on whether you want a mono or a stereo processor
    //Params cannot be const because they have subscribers added
    public AidAlgo(UniqueStereoParams _lPars,
                   UniqueStereoParams _rPars,
                   SharedStereoParams _sharedPars,
                   ReentrantLock _pMyMutex) {
        pMOCsimContainer = new MOCsimContainer(_sharedPars);
        pManagerL = new AidStereoChannelManager(_lPars, _sharedPars, pMOCsimContainer);
        pManagerR = new AidStereoChannelManager(_rPars, _sharedPars, pMOCsimContainer);
        pMyMutex = _pMyMutex;
    }

    public AidAlgo(UniqueStereoParams _lPars,
                   SharedStereoParams _sharedPars,
                   Lock _pMyMutex) {
        pMOCsimContainer = new MOCsimContainer(_sharedPars);

        pManagerL = new AidStereoChannelManager(_lPars, _sharedPars, pMOCsimContainer);
        pManagerR = null;
        pMyMutex = _pMyMutex;

    }

    ;

    //cMOCsimContainer MOCsimContainer(sharedPars);

    void processSampleBlock(double[][] inputChannelData,
                            int numInputChannels,
                            double[][] outputChannelData,
                            int numOutputChannels,
                            int numSamples) {

        boolean isStereo = true;
        // No point processing stereo if there is only one output channel
        // ..or if there is only a MONO processor
        if ((numInputChannels > numOutputChannels) || (pManagerR == null)) {
            isStereo = false;
        }

        //This code sets the index for the right input channel
        //If both outputs are sharing a common input then the following loop needs to know
        int inRightIdx = 0;
        if (numInputChannels == 2)
            inRightIdx = 1;

        pMyMutex.lock();
        try {//New scope just for lock
            for (int nn = 0; nn < numSamples; ++nn) {
                outputChannelData[0][nn] = pManagerL.process(inputChannelData[0][nn]);
                if (isStereo)
                    outputChannelData[1][nn] = pManagerR.process(inputChannelData[inRightIdx][nn]);
            }
        } finally {
            pMyMutex.unlock();
        }//Mutex unlocked

        //Copy the data into both channels if we are mono
        if (!isStereo && (numOutputChannels == 2)) {
            for (int nn = 0; nn < numSamples; ++nn) {
                outputChannelData[1][nn] = outputChannelData[0][nn];
            }
        }

    }


}
