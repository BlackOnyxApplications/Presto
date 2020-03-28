package com.bhuvan_kumar.Presto.widget;

import com.bhuvan_kumar.Presto.exception.NotReadyException;
import com.bhuvan_kumar.Presto.object.Editable;
import com.genonbeta.android.framework.widget.ListAdapterImpl;

import java.util.List;

/**
 * created by: Bk
 * date: 14/04/18 00:51
 */
public interface EditableListAdapterImpl<T extends Editable> extends ListAdapterImpl<T>
{
    boolean filterItem(T item);

    T getItem(int position) throws NotReadyException;

    void notifyAllSelectionChanges();

    void notifyItemChanged(int position);

    void notifyItemRangeChanged(int positionStart, int itemCount);

    void syncSelectionList();

    void syncSelectionList(List<T> itemList);
}
