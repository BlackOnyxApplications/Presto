package com.bhuvan_kumar.Presto.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
 import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bhuvan_kumar.Presto.app.EditableListFragment;
import com.bhuvan_kumar.Presto.app.EditableListFragmentImpl;
import com.bhuvan_kumar.Presto.app.GroupEditableListFragment;
import com.bhuvan_kumar.Presto.ui.callback.IconSupport;
import com.bhuvan_kumar.Presto.ui.callback.TitleSupport;
import com.bhuvan_kumar.Presto.util.AppUtils;
import com.bhuvan_kumar.Presto.R;
import com.bhuvan_kumar.Presto.activity.ViewTransferActivity;
import com.bhuvan_kumar.Presto.adapter.TransferGroupListAdapter;
import com.bhuvan_kumar.Presto.database.AccessDatabase;
import com.bhuvan_kumar.Presto.service.CommunicationService;
import com.bhuvan_kumar.Presto.widget.GroupEditableListAdapter;
import com.genonbeta.android.database.SQLQuery;
import com.genonbeta.android.framework.widget.PowerfulActionMode;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * created by: Bk
 * date: 10.11.2017 00:15
 */

public class TransferGroupListFragment
        extends GroupEditableListFragment<TransferGroupListAdapter.PreloadedGroup, GroupEditableListAdapter.GroupViewHolder, TransferGroupListAdapter>
        implements IconSupport, TitleSupport
{
    private UnifiedNativeAd nativeAd;
    private Runnable mTicker = null;
    private SQLQuery.Select mSelect;
    private IntentFilter mFilter = new IntentFilter();
    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (AccessDatabase.ACTION_DATABASE_CHANGE.equals(intent.getAction())
                    && intent.hasExtra(AccessDatabase.EXTRA_TABLE_NAME)
                    && (intent.getStringExtra(AccessDatabase.EXTRA_TABLE_NAME).equals(AccessDatabase.TABLE_TRANSFERGROUP)
                    || intent.getStringExtra(AccessDatabase.EXTRA_TABLE_NAME).equals(AccessDatabase.TABLE_TRANSFER)
            ))
                refreshList();
            else if (CommunicationService.ACTION_TASK_RUNNING_LIST_CHANGE.equals(intent.getAction())
                    && intent.hasExtra(CommunicationService.EXTRA_TASK_LIST_RUNNING)) {
                getAdapter().updateActiveList(intent.getLongArrayExtra(CommunicationService.EXTRA_TASK_LIST_RUNNING));
                refreshList();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setFilteringSupported(true);
        setDefaultOrderingCriteria(TransferGroupListAdapter.MODE_SORT_ORDER_DESCENDING);
        setDefaultSortingCriteria(TransferGroupListAdapter.MODE_SORT_BY_DATE);
        setDefaultGroupingCriteria(TransferGroupListAdapter.MODE_GROUP_BY_DATE);
        setDefaultSelectionCallback(new SelectionCallback(this));
        setUseDefaultPaddingDecoration(true);
        setUseDefaultPaddingDecorationSpaceForEdges(true);
        setDefaultPaddingDecorationSize(getResources().getDimension(R.dimen.padding_list_content_parent_layout));
    }

    private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {

        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);

    }

    private void refreshAd(View view) {
        Context context = getActivity();
        if(context!=null) {
            try {
                List<Fragment> fragments = getFragmentManager() != null ? getFragmentManager().getFragments() : new ArrayList<Fragment>();
                if((fragments.size() > 1) && isAdded()) {
                    AdLoader.Builder builder = new AdLoader.Builder(getActivity(), getString(R.string.transfer_ad_unit_id));
                    builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                        @Override
                        public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                            if (nativeAd != null) {
                                nativeAd.destroy();
                            }
                            nativeAd = unifiedNativeAd;
                            if (isAdded()){
                                FrameLayout frameLayout = view.findViewById(R.id.fl_adplaceholder);
                                UnifiedNativeAdView adView = (UnifiedNativeAdView) getLayoutInflater()
                                        .inflate(R.layout.home_page_custom_ad, null);
                                populateUnifiedNativeAdView(unifiedNativeAd, adView);
                                frameLayout.removeAllViews();
                                frameLayout.addView(adView);
                            }
                        }
                    });

                    AdLoader adLoader = builder.withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(int errorCode) {
                            Log.e(getTag(), "errorCode: " + errorCode);
                        }
                    }).build();
                    adLoader.loadAd(new AdRequest.Builder().build());
                }
            } catch (Exception e){
                Log.e(TAG, e.toString());
            }
        }
    }

    @Override
    protected RecyclerView onListView(View mainContainer, ViewGroup listViewContainer)
    {
        View adaptedView = getLayoutInflater().inflate(R.layout.layout_transfer_group_list, null, false);
        ((ViewGroup) mainContainer).addView(adaptedView);

        MobileAds.initialize(getActivity(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        try {

            if(isAdded()) refreshAd(adaptedView);
        }
        catch (Exception ex){
            Log.e(getTag(), " " + ex.toString());
        }

        return super.onListView(mainContainer, (FrameLayout) adaptedView.findViewById(R.id.fragmentContainer));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        setEmptyImage(R.drawable.ic_share_white_24dp);
        setEmptyText(getString(R.string.text_listEmptyTransfer));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        mFilter.addAction(AccessDatabase.ACTION_DATABASE_CHANGE);
        mFilter.addAction(CommunicationService.ACTION_TASK_RUNNING_LIST_CHANGE);

        if (getSelect() == null)
            setSelect(new SQLQuery.Select(AccessDatabase.TABLE_TRANSFERGROUP));
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getActivity().registerReceiver(mReceiver, mFilter);

        AppUtils.startForegroundService(getActivity(), new Intent(getActivity(), CommunicationService.class)
                .setAction(CommunicationService.ACTION_REQUEST_TASK_RUNNING_LIST_CHANGE));
    }

    @Override
    public void onPause()
    {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public void onSortingOptions(Map<String, Integer> options)
    {
        options.put(getString(R.string.text_sortByDate), TransferGroupListAdapter.MODE_SORT_BY_DATE);
        options.put(getString(R.string.text_sortBySize), TransferGroupListAdapter.MODE_SORT_BY_SIZE);
    }

    @Override
    public void onGroupingOptions(Map<String, Integer> options)
    {
        options.put(getString(R.string.text_groupByNothing), TransferGroupListAdapter.MODE_GROUP_BY_NOTHING);
        options.put(getString(R.string.text_groupByDate), TransferGroupListAdapter.MODE_GROUP_BY_DATE);
    }

    @Override
    public TransferGroupListAdapter onAdapter()
    {
        final AppUtils.QuickActions<GroupEditableListAdapter.GroupViewHolder> quickActions = new AppUtils.QuickActions<GroupEditableListAdapter.GroupViewHolder>()
        {
            @Override
            public void onQuickActions(final GroupEditableListAdapter.GroupViewHolder clazz)
            {
                if (!clazz.isRepresentative()) {
                    registerLayoutViewClicks(clazz);

                    clazz.getView().findViewById(R.id.layout_image).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if (getSelectionConnection() != null)
                                getSelectionConnection().setSelected(clazz.getAdapterPosition());
                        }
                    });
                }
            }
        };

        return new TransferGroupListAdapter(getActivity(), AppUtils.getDatabase(getContext()))
        {
            @NonNull
            @Override
            public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                return AppUtils.quickAction(super.onCreateViewHolder(parent, viewType), quickActions);
            }
        }.setSelect(getSelect());
    }

    @Override
    public boolean onDefaultClickAction(GroupEditableListAdapter.GroupViewHolder holder)
    {
        try {
            ViewTransferActivity.startInstance(getActivity(), getAdapter().getItem(holder).groupId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int getIconRes()
    {
        return R.drawable.ic_swap_vert_white_24dp;
    }

    @Override
    public CharSequence getTitle(Context context)
    {
        return context.getString(R.string.text_transfers);
    }

    public SQLQuery.Select getSelect()
    {
        return mSelect;
    }

    public TransferGroupListFragment setSelect(SQLQuery.Select select)
    {
        mSelect = select;
        return this;
    }

    private static class SelectionCallback extends EditableListFragment.SelectionCallback<TransferGroupListAdapter.PreloadedGroup>
    {
        public SelectionCallback(EditableListFragmentImpl<TransferGroupListAdapter.PreloadedGroup> fragment)
        {
            super(fragment);
        }

        @Override
        public boolean onPrepareActionMenu(Context context, PowerfulActionMode actionMode)
        {
            super.onPrepareActionMenu(context, actionMode);
            return true;
        }

        @Override
        public boolean onCreateActionMenu(Context context, PowerfulActionMode actionMode, Menu menu)
        {
            super.onCreateActionMenu(context, actionMode, menu);
            actionMode.getMenuInflater().inflate(R.menu.action_mode_group, menu);
            return true;
        }

        @Override
        public boolean onActionMenuItemSelected(Context context, PowerfulActionMode actionMode, MenuItem item)
        {
            int id = item.getItemId();

            ArrayList<TransferGroupListAdapter.PreloadedGroup> selectionList = new ArrayList<>(getFragment().getSelectionConnection().getSelectedItemList());

            if (id == R.id.action_mode_group_delete)
                AppUtils.getDatabase(getFragment().getContext())
                        .removeAsynchronous(getFragment().getActivity(), selectionList);
            else if (id == R.id.action_mode_group_serve_on_web
                    || id == R.id.action_mode_group_hide_on_web) {
                boolean success = false;

                for (TransferGroupListAdapter.PreloadedGroup group : selectionList) {
                    group.isServedOnWeb = group.index.outgoingCount > 0
                            && id == R.id.action_mode_group_serve_on_web;

                    if (group.isServedOnWeb)
                        success = true;
                }

                AppUtils.getDatabase(getFragment().getContext()).update(selectionList);

                if (success)
                    AppUtils.startWebShareActivity(getFragment().getActivity(), true);
            } else
                return super.onActionMenuItemSelected(context, actionMode, item);

            return true;
        }
    }
}