package com.sendbird.uikit.activities.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.BaseMessage;
import com.sendbird.uikit.activities.viewholder.BaseViewHolder;
import com.sendbird.uikit.databinding.SbViewSearchResultPreviewBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class MessageSearchAdapter extends BaseAdapter<BaseMessage, BaseViewHolder<BaseMessage>> {
    private List<BaseMessage> items = new ArrayList<>();
    private OnItemClickListener<BaseMessage> listener;

    @NonNull
    @Override
    public BaseViewHolder<BaseMessage> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchResultViewHolder(SbViewSearchResultPreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<BaseMessage> holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public BaseMessage getItem(int position) {
        return this.items.get(position);
    }

    @Override
    public List<BaseMessage> getItems() {
        return items != null ? Collections.unmodifiableList(items) : null;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getMessageId();
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    /**
     * Sets the {@link List<BaseMessage>} to be displayed.
     *
     * @param items list to be displayed
     * @since 2.1.0
     */
    public void setItems(List<BaseMessage> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    /**
     * Register a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     *
     * @param listener The callback that will run
     * @since 2.1.0
     */
    public void setOnItemClickListener(@Nullable OnItemClickListener<BaseMessage> listener) {
        this.listener = listener;
    }

    private class SearchResultViewHolder extends BaseViewHolder<BaseMessage> {
        private final SbViewSearchResultPreviewBinding binding;

        public SearchResultViewHolder(@NonNull SbViewSearchResultPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            this.binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != NO_POSITION && listener != null) {
                    BaseMessage message = getItem(position);
                    listener.onItemClick(v, position, message);
                }
            });
        }

        @Override
        public void bind(BaseMessage message) {
            binding.setMessage(message);
            binding.executePendingBindings();
        }
    }
}
