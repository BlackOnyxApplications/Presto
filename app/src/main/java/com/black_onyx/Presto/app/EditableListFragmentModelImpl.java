package com.black_onyx.Presto.app;

import com.black_onyx.Presto.widget.EditableListAdapter;

/**
 * created by: Bk
 * date: 8/24/18 1:36 PM
 */
public interface EditableListFragmentModelImpl<V extends EditableListAdapter.EditableViewHolder>
{
    void setLayoutClickListener(EditableListFragment.LayoutClickListener<V> clickListener);
}
