package com.sendbird.uikit.activities.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.uikit.activities.viewholder.MessageViewHolder;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.internal.extensions.MessageListAdapterExtensionsKt;
import com.sendbird.uikit.internal.interfaces.OnSubmitButtonClickListener;
import com.sendbird.uikit.internal.ui.viewholders.FormMessageViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.SuggestedRepliesViewHolder;
import com.sendbird.uikit.internal.wrappers.SendbirdUIKitImpl;
import com.sendbird.uikit.internal.wrappers.SendbirdUIKitWrapper;
import com.sendbird.uikit.model.MessageListUIParams;

/**
 * MessageListAdapter provides a binding from a {@link BaseMessage} type data set to views that are displayed within a RecyclerView.
 */
public class MessageListAdapter extends BaseMessageListAdapter {
    @Nullable
    protected OnItemClickListener<String> suggestedRepliesClickListener;

    public MessageListAdapter(boolean useMessageGroupUI) {
        this(null, useMessageGroupUI);
    }

    public MessageListAdapter(@Nullable GroupChannel channel) {
        this(channel, true);
    }

    public MessageListAdapter(@Nullable GroupChannel channel, boolean useMessageGroupUI) {
        this(channel, new MessageListUIParams.Builder()
            .setUseMessageGroupUI(useMessageGroupUI)
            .build());
    }

    public MessageListAdapter(@Nullable GroupChannel channel, @NonNull MessageListUIParams messageListUIParams) {
        this(channel, messageListUIParams, new SendbirdUIKitImpl());
    }

    @VisibleForTesting
    MessageListAdapter(@Nullable GroupChannel channel, @NonNull MessageListUIParams messageListUIParams, @NonNull SendbirdUIKitWrapper sendbirdUIKit) {
        super(channel,
            new MessageListUIParams.Builder(messageListUIParams)
                .setUseQuotedView(true)
                .build(),
            sendbirdUIKit);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof SuggestedRepliesViewHolder) {
            SuggestedRepliesViewHolder suggestedRepliesViewHolder = (SuggestedRepliesViewHolder) holder;
            suggestedRepliesViewHolder.setSuggestedRepliesClickedListener((view, pos, data) -> {
                int messagePosition = holder.getBindingAdapterPosition();
                if (messagePosition != NO_POSITION && suggestedRepliesClickListener != null) {
                    suggestedRepliesClickListener.onItemClick(view, pos, data);
                }
            });
        }

        if (holder instanceof FormMessageViewHolder) {
            FormMessageViewHolder formMessageViewHolder = (FormMessageViewHolder) holder;
            formMessageViewHolder.setOnSubmitClickListener((message, form) -> {
                final OnSubmitButtonClickListener finalListener = MessageListAdapterExtensionsKt.getSubmitButtonClickListener(this);
                if (finalListener != null) {
                    finalListener.onClicked(message, form);
                }
            });
        }
    }

    /**
     * Returns a callback to be invoked when the suggested replies is clicked.
     *
     * @return {OnItemClickListener<String>} to be invoked when the suggested replies is clicked.
     * since 3.10.0
     */
    @Nullable
    public OnItemClickListener<String> getSuggestedRepliesClickListener() {
        return suggestedRepliesClickListener;
    }

    /**
     * Register a callback to be invoked when the suggested replies is clicked.
     *
     * @param suggestedRepliesClickListener The callback to be registered.
     * since 3.10.0
     */
    public void setSuggestedRepliesClickListener(@Nullable OnItemClickListener<String> suggestedRepliesClickListener) {
        this.suggestedRepliesClickListener = suggestedRepliesClickListener;
    }
}
