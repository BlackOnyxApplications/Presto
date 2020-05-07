package com.bhuvan_kumar.Presto.ui;

import com.genonbeta.android.framework.util.Interrupter;

/**
 * created by: Bk
 * date: 16/04/18 22:41
 */
public interface UITask
{
    void updateTaskStarted(final Interrupter interrupter);

    void updateTaskStopped();
}
