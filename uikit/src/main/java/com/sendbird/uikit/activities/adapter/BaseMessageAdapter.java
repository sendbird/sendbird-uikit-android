package com.sendbird.uikit.activities.adapter;

import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.uikit.activities.viewholder.MessageViewHolder;

import java.util.List;

abstract class BaseMessageAdapter<T, VH extends MessageViewHolder> extends RecyclerView.Adapter<VH> {
    abstract public T getItem(int position);
    abstract public List<T> getItems();
    abstract public long getItemId(int position);
}
