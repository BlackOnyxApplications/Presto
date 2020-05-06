package com.black_onyx.Presto.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.black_onyx.Presto.ui.callback.NetworkDeviceSelectedListener;
import com.black_onyx.Presto.R;
import com.black_onyx.Presto.object.NetworkDevice;
import com.black_onyx.Presto.service.CommunicationService;

/**
 * created by: Bk
 * date: 3/11/19 7:43 PM
 */
public class CustomNetworkDeviceListFragment extends NetworkDeviceListFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(false);
        setFilteringSupported(false);
        setUseDefaultPaddingDecoration(false);
        setUseDefaultPaddingDecorationSpaceForEdges(false);

        if (isScreenLarge())
            setDefaultViewingGridSize(4, 5);
        else if (isScreenNormal())
            setDefaultViewingGridSize(3, 4);
        else
            setDefaultViewingGridSize(2, 3);

        setDeviceSelectedListener(new NetworkDeviceSelectedListener()
        {
            @Override
            public boolean onNetworkDeviceSelected(NetworkDevice networkDevice, NetworkDevice.Connection connection)
            {
                if (getContext() != null) {
                    getContext().sendBroadcast(new Intent(CommunicationService.ACTION_DEVICE_ACQUAINTANCE)
                            .putExtra(CommunicationService.EXTRA_DEVICE_ID, networkDevice.deviceId)
                            .putExtra(CommunicationService.EXTRA_CONNECTION_ADAPTER_NAME, connection.adapterName));

                    return true;
                }

                return false;
            }

            @Override
            public boolean isListenerEffective()
            {
                return true;
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        getListView().setNestedScrollingEnabled(true);
        setDividerVisible(false);

        if (getContext() != null) {
            float padding = getContext().getResources().getDimension(R.dimen.short_content_width_padding);

            getListView().setClipToPadding(false);
            getListView().setPadding((int) padding, 0, (int) padding, 0);
        }
    }
}
