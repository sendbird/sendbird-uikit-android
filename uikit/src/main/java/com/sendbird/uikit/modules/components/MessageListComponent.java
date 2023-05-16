package com.sendbird.uikit.modules.components;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.SendingStatus;
import com.sendbird.uikit.activities.adapter.MessageListAdapter;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;

/**
 * This class creates and performs a view corresponding the message list area in Sendbird UIKit.
 *
 * since 3.0.0
 */
public class MessageListComponent extends BaseMessageListComponent<MessageListAdapter> {
    @Nullable
    private OnItemClickListener<BaseMessage> quoteReplyMessageClickListener;
    @Nullable
    private OnItemLongClickListener<BaseMessage> quoteReplyMessageLongClickListener;
    @Nullable
    private OnItemClickListener<BaseMessage> threadInfoClickListener;

    /**
     * Constructor
     *
     * since 3.0.0
     */
    public MessageListComponent() {
        super(new Params(), true, true);
    }

    /**
     * Returns a collection of parameters applied to this component.
     *
     * @return {@code Params} applied to this component
     * since 3.0.0
     */
    @NonNull
    public Params getParams() {
        return (Params) super.getParams();
    }

    @Override
    public void notifyChannelChanged(@NonNull GroupChannel channel) {
        if (getAdapter() == null) {
            setAdapter(new MessageListAdapter(channel, getParams().shouldUseGroupUI()));
        }
        super.notifyChannelChanged(channel);
    }

    @Override
    protected void onListItemClicked(@NonNull View view, @NonNull String identifier, int position, @NonNull BaseMessage message) {
        final SendingStatus status = message.getSendingStatus();
        if (status == SendingStatus.PENDING) return;

        switch (identifier) {
            case StringSet.Chat:
                // ClickableViewType.Chat
                onMessageClicked(view, position, message);
                break;
            case StringSet.Profile:
                // ClickableViewType.Profile
                onMessageProfileClicked(view, position, message);
                break;
            case StringSet.QuoteReply:
                // ClickableViewType.Reply
                onQuoteReplyMessageClicked(view, position, message);
                break;
            case StringSet.ThreadInfo:
                // ClickableViewType.ThreadInfo
                onThreadInfoClicked(view, position, message);
                break;
        }
    }

    @Override
    protected void onListItemLongClicked(@NonNull View view, @NonNull String identifier, int position, @NonNull BaseMessage message) {
        switch (identifier) {
            case StringSet.Chat:
                // ClickableViewType.Chat
                onMessageLongClicked(view, position, message);
                break;
            case StringSet.Profile:
                // ClickableViewType.Profile
                onMessageProfileLongClicked(view, position, message);
                break;
            case StringSet.QuoteReply:
                // ClickableViewType.Reply
                onQuoteReplyMessageLongClicked(view, position, message);
                break;
        }
    }

    /**
     * Register a callback to be invoked when the quoted message is clicked.
     *
     * since 3.0.0
     */
    public void setOnQuoteReplyMessageClickListener(@Nullable OnItemClickListener<BaseMessage> quoteReplyMessageClickListener) {
        this.quoteReplyMessageClickListener = quoteReplyMessageClickListener;
    }

    /**
     * Register a callback to be invoked when the quoted message is long-clicked.
     *
     * @param quoteReplyMessageLongClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnQuoteReplyMessageLongClickListener(@Nullable OnItemLongClickListener<BaseMessage> quoteReplyMessageLongClickListener) {
        this.quoteReplyMessageLongClickListener = quoteReplyMessageLongClickListener;
    }

    /**
     * Called when the quoted message of the message is clicked.
     *
     * @param view     The View clicked
     * @param position The position clicked
     * @param message  The message that the clicked item displays
     * since 3.0.0
     */
    protected void onQuoteReplyMessageClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (quoteReplyMessageClickListener != null)
            quoteReplyMessageClickListener.onItemClick(view, position, message);
    }

    /**
     * Called when the quoted message of the message is long-clicked.
     *
     * @param view     The View long-clicked
     * @param position The position long-clicked
     * @param message  The message that the long-clicked item displays
     * since 3.0.0
     */
    protected void onQuoteReplyMessageLongClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (quoteReplyMessageLongClickListener != null)
            quoteReplyMessageLongClickListener.onItemLongClick(view, position, message);
    }

    /**
     * Register a callback to be invoked when the thread info is clicked.
     *
     * since 3.3.0
     */
    public void setOnThreadInfoClickListener(@Nullable OnItemClickListener<BaseMessage> threadInfoClickListener) {
        this.threadInfoClickListener = threadInfoClickListener;
    }

    /**
     * Called when the thread info of the message is clicked.
     *
     * @param view     The View clicked
     * @param position The position clicked
     * @param message  The message that the clicked item displays
     * since 3.3.0
     */
    protected void onThreadInfoClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (threadInfoClickListener != null)
            threadInfoClickListener.onItemClick(view, position, message);
    }

    /**
     * A collection of parameters, which can be applied to a default View. The values of params are not dynamically applied at runtime.
     * Params cannot be created directly, and it is automatically created together when components are created.
     * <p><b>Since the onCreateView configuring View uses the values of the set Params, we recommend that you set up for Params before the onCreateView is called.</b></p>
     *
     * @see #getParams()
     * since 3.0.0
     */
    public static class Params extends BaseMessageListComponent.Params {}
}
