package com.bhuvan_kumar.Presto.fragment;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
 import androidx.annotation.Nullable;import com.bhuvan_kumar.Presto.app.GalleryGroupEditableListFragment;
import com.bhuvan_kumar.Presto.ui.callback.TitleSupport;
import com.bhuvan_kumar.Presto.util.AppUtils;
import com.bhuvan_kumar.Presto.R;
import com.bhuvan_kumar.Presto.adapter.VideoListAdapter;
import com.bhuvan_kumar.Presto.widget.GroupEditableListAdapter;

public class VideoListFragment
        extends GalleryGroupEditableListFragment<VideoListAdapter.VideoHolder, GroupEditableListAdapter.GroupViewHolder, VideoListAdapter>
        implements TitleSupport
{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setFilteringSupported(true);
        setDefaultOrderingCriteria(VideoListAdapter.MODE_SORT_ORDER_DESCENDING);
        setDefaultSortingCriteria(VideoListAdapter.MODE_GROUP_BY_ALBUM);
        setDefaultViewingGridSize(3, 4);
        setUseDefaultPaddingDecoration(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        setEmptyImage(R.drawable.ic_video_library_white_24dp);
        setEmptyText(getString(R.string.text_listEmptyVideo));
    }

    @Override
    public void onResume()
    {
        super.onResume();

        getContext().getContentResolver()
                .registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, getDefaultContentObserver());
    }

    @Override
    public void onPause()
    {
        super.onPause();

        getContext().getContentResolver()
                .unregisterContentObserver(getDefaultContentObserver());
    }

    @Override
    public VideoListAdapter onAdapter()
    {
        final AppUtils.QuickActions<GroupEditableListAdapter.GroupViewHolder> quickActions = new AppUtils.QuickActions<GroupEditableListAdapter.GroupViewHolder>()
        {
            @Override
            public void onQuickActions(final GroupEditableListAdapter.GroupViewHolder clazz)
            {
                if (!clazz.isRepresentative()) {
                    registerLayoutViewClicks(clazz);

                    View visitView = clazz.getView().findViewById(R.id.visitView);
                    visitView.setOnClickListener(
                            new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    performLayoutClickOpen(clazz);
                                }
                            });

                    visitView.setOnLongClickListener(new View.OnLongClickListener()
                    {
                        @Override
                        public boolean onLongClick(View v)
                        {
                            return performLayoutLongClick(clazz);
                        }
                    });

                    clazz.getView().findViewById(getAdapter().isGridLayoutRequested()
                            ? R.id.selectorContainer : R.id.selector)
                            .setOnClickListener(new View.OnClickListener()
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

        return new VideoListAdapter(getActivity())
        {
            @NonNull
            @Override
            public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                return AppUtils.quickAction(super.onCreateViewHolder(parent, viewType), quickActions);
            }
        };
    }

    @Override
    public boolean onDefaultClickAction(GroupEditableListAdapter.GroupViewHolder holder)
    {
        return getSelectionConnection() != null
                ? getSelectionConnection().setSelected(holder)
                : performLayoutClickOpen(holder);
    }

    @Override
    public int onGridSpanSize(int viewType, int currentSpanSize)
    {
        return viewType == VideoListAdapter.VIEW_TYPE_TITLE
                ? currentSpanSize
                : super.onGridSpanSize(viewType, currentSpanSize);
    }

    @Override
    public CharSequence getTitle(Context context)
    {
        return context.getString(R.string.text_video);
    }
}
