package com.bhuvan_kumar.Presto.callback;

import com.bhuvan_kumar.Presto.object.NetworkDevice;

import java.util.List;

public interface OnDeviceSelectedListener
{
    void onDeviceSelected(NetworkDevice.Connection connection, List<NetworkDevice.Connection> availableInterfaces);
}
