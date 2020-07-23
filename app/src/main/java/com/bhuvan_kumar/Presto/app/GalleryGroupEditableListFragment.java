package com.bhuvan_kumar.Presto.app;

import android.os.Bundle;

 import androidx.annotation.Nullable;import com.bhuvan_kumar.Presto.R;
import com.bhuvan_kumar.Presto.adapter.ImageListAdapter;
import com.bhuvan_kumar.Presto.widget.GroupEditableListAdapter;

import java.util.Map;

/**
 * created by: Bk
 * date: 30.03.2018 18:15
 */
abstract public class GalleryGroupEditableListFragment<T extends GroupEditableListAdapter.GroupShareable, V extends GroupEditableListAdapter.GroupViewHolder, E extends GroupEditableListAdapter<T, V>>
        extends GroupEditableListFragment<T, V, E>
{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setDefaultGroupingCriteria(ImageListAdapter.MODE_GROUP_BY_NOTHING);
    }

    @Override
    public void onGroupingOptions(Map<String, Integer> options)
    {
        super.onGroupingOptions(options);

        options.put(getString(R.string.text_groupByNothing), ImageListAdapter.MODE_GROUP_BY_NOTHING);
        options.put(getString(R.string.text_groupByDate), ImageListAdapter.MODE_GROUP_BY_DATE);
        options.put(getString(R.string.text_groupByAlbum), ImageListAdapter.MODE_GROUP_BY_ALBUM);
    }
}
