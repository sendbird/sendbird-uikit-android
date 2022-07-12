package com.sendbird.uikit.customsample.groupchannel.components.adapters;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.message.BaseMessage;
import com.sendbird.uikit.activities.adapter.MessageSearchAdapter;
import com.sendbird.uikit.activities.viewholder.BaseViewHolder;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.interfaces.OnItemClickListener;

/**
 * Implements the customized <code>MessageSearchAdapter</code> to adapt the customized search list items.
 */
public class CustomMessageSearchAdapter extends MessageSearchAdapter {
    @Nullable
    private OnItemClickListener<BaseMessage> itemClickListener;

    @NonNull
    @Override
    public BaseViewHolder<BaseMessage> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchedMessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_custom_searched_message_holder, parent, false));
    }

    @Override
    public void setOnItemClickListener(@Nullable OnItemClickListener<BaseMessage> listener) {
        itemClickListener = listener;
    }

    private class SearchedMessageViewHolder extends BaseViewHolder<BaseMessage> {
        @NonNull
        private final TextView nickname;
        @NonNull
        private final TextView message;
        @NonNull
        private final TextView sentAt;

        /**
         * Constructor
         *
         * @param itemView View to be displayed.
         */
        public SearchedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            nickname = itemView.findViewById(R.id.tvSender);
            message = itemView.findViewById(R.id.tvMessage);
            sentAt = itemView.findViewById(R.id.tvSentAt);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != NO_POSITION && itemClickListener != null) {
                    BaseMessage message = getItem(position);
                    itemClickListener.onItemClick(v, position, message);
                }
            });
        }

        @Override
        public void bind(@NonNull BaseMessage message) {
            nickname.setText(message.getSender().getNickname());
            this.message.setText(message.getMessage());
            sentAt.setText(DateUtils.formatDateTime(itemView.getContext(), message.getCreatedAt(), DateUtils.FORMAT_SHOW_TIME));
        }
    }
}
