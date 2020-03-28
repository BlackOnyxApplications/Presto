package com.bhuvan_kumar.Presto.ui.callback;

import com.bhuvan_kumar.Presto.object.NetworkDevice;

/**
 * created by: Bk
 * date: 16/04/18 03:18
 */
public interface NetworkDeviceSelectedListener
{
    boolean onNetworkDeviceSelected(NetworkDevice networkDevice, NetworkDevice.Connection connection);

    boolean isListenerEffective();
}
