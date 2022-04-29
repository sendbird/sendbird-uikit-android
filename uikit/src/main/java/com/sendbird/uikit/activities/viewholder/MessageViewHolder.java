package com.sendbird.uikit.activities.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.model.HighlightMessageInfo;
import com.sendbird.uikit.model.MessageUIConfig;
import com.sendbird.uikit.utils.DateUtils;
import com.sendbird.uikit.utils.MessageUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A ViewHolder describes an item view and Message about its place within the RecyclerView.
 */
public abstract class MessageViewHolder extends RecyclerView.ViewHolder {
    @Nullable
    protected HighlightMessageInfo highlight;
    @Nullable
    protected MessageUIConfig messageUIConfig;
    @NonNull
    protected Map<String, View> clickableViewMap = new ConcurrentHashMap<>();
    private boolean isNewDate = false;
    private boolean isMine = false;
    private boolean isShowProfile = false;
    private boolean useMessageGroupUI = true;

    /**
     * Constructor
     *
     * @param view View to be displayed.
     */
    public MessageViewHolder(@NonNull View view) {
        super(view);
    }

    MessageViewHolder(@NonNull View view, boolean useMessageGroupUI) {
        super(view);
        this.useMessageGroupUI = useMessageGroupUI;
    }

    public void onBindViewHolder(@NonNull BaseChannel channel, @Nullable BaseMessage prevMessage, @NonNull BaseMessage message, @Nullable BaseMessage nextMessage) {
        if (prevMessage != null) {
            this.isNewDate = !DateUtils.hasSameDate(message.getCreatedAt(), prevMessage.getCreatedAt());
        } else {
            this.isNewDate = true;
        }

        this.isMine = MessageUtils.isMine(message);
        this.isShowProfile = !isMine;

        bind(channel, message, useMessageGroupUI ?
                MessageUtils.getMessageGroupType(prevMessage, message, nextMessage) :
                MessageGroupType.GROUPING_TYPE_SINGLE);
        itemView.requestLayout();
    }

    /**
     * Sets the information of the message to highlight.
     *
     * @param highlightInfo The information of the message to highlight.
     * @since 2.1.0
     */
    public void setHighlightInfo(@Nullable HighlightMessageInfo highlightInfo) {
        this.highlight = highlightInfo;
    }

    /**
     * Sets the configurations of the message's properties to highlight text.
     *
     * @param messageUIConfig the configurations of the message's properties to highlight text.
     * @see com.sendbird.uikit.model.TextUIConfig
     * @since 3.0.0
     */
    public void setMessageUIConfig(@Nullable MessageUIConfig messageUIConfig) {
        this.messageUIConfig = messageUIConfig;
    }

    /**
     * Return whether a day has passed since the previous message was created.
     *
     * @return <code>true</code> if a day has passed since the previous message was created ,
     * <code>false</code> otherwise.
     */
    protected boolean isNewDate() {
        return isNewDate;
    }

    /**
     * Return whether the profile is visible or not.
     *
     * @return <code>true</code> if the profile is visible, <code>false</code> otherwise.
     */
    protected boolean isShowProfile() {
        return isShowProfile;
    }

    /**
     * Called whether the message is from the current user.
     *
     * @return <code>true</code> if the message is from the current user, <code>false</code> otherwise.
     */
    protected boolean isMine() {
        return isMine;
    }

    /**
     * Binds as item view and data.
     *
     * @param channel Channel used for as item view.
     * @param message Message used for as item view.
     * @param messageGroupType The type of message group UI.
     * @since 1.2.1
     */
    abstract public void bind(@NonNull BaseChannel channel, @NonNull BaseMessage message, @NonNull MessageGroupType messageGroupType);

    /**
     * Returns a Map containing views to register a click event with an identifier.
     *
     * @return A Map containing views to register a click event with an identifier.
     * @since 2.2.0
     */
    @NonNull
    abstract public Map<String, View> getClickableViewMap();
}

