package com.bioaid;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by jaro on 6/8/13.
 */
public class NullCheckingScopedLock {

    Lock pMutex;

    NullCheckingScopedLock(Lock _pMutex)
//    : pMutex( _pMutex)
    {
//        this.pMutex = _pMutex;
//        //A reinterpret_cast from void is used to that this is the only place in the code that directly
//        //refers to boost::mutex. Allows non-boost, but boost compatible mutex useage.
////        pMutex = reinterpret_cast < boost::mutex* > ( _pMutex );
//        if( pMutex != null)
//        {
//            pMutex.lock();
//        }
    }

// unlocking in destructor
//{
//    if( pMutex != null) {
//        pMutex->unlock();
//        DBGM("Mutex unlock")
//    }
//}

};
