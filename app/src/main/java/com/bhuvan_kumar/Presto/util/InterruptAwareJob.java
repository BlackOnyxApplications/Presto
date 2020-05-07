package com.bhuvan_kumar.Presto.util;

import com.genonbeta.android.framework.util.Interrupter;

/**
 * created by: Bk
 * date: 11.02.2018 19:37
 */

abstract public class InterruptAwareJob
{
    abstract protected void onRun();

    protected void run(Interrupter interrupter)
    {
        onRun();
        interrupter.removeClosers();
    }
}
