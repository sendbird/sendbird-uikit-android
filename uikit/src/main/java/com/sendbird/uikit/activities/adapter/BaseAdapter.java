package com.sendbird.uikit.activities.adapter;

import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.uikit.activities.viewholder.BaseViewHolder;

import java.util.List;

abstract class BaseAdapter<T, VH extends BaseViewHolder<T>> extends RecyclerView.Adapter<VH> {
    abstract public T getItem(int position);
    abstract public List<T> getItems();
}
