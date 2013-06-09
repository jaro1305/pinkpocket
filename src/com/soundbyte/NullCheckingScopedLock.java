package com.soundbyte;

import java.util.concurrent.locks.Lock;

/**
 * Created by jaro on 6/8/13.
 */
public class NullCheckingScopedLock {

    final Lock pMutex;

    NullCheckingScopedLock(Lock _pMutex) {
        this.pMutex = _pMutex;
    }

    public void lock() {
        if (pMutex != null) {
            pMutex.lock();
        }
    }

    public void unlock() {
        if (pMutex != null) {
            pMutex.unlock();
        }
    }
}