package com.black_onyx.Presto.ui.callback;

import com.black_onyx.Presto.object.NetworkDevice;

/**
 * created by: Bk
 * date: 16/04/18 03:18
 */
public interface NetworkDeviceSelectedListener
{
    boolean onNetworkDeviceSelected(NetworkDevice networkDevice, NetworkDevice.Connection connection);

    boolean isListenerEffective();
}
