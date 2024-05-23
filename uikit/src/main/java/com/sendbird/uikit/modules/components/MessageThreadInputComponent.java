package com.sendbird.uikit.modules.components;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.uikit.R;
import com.sendbird.uikit.widgets.MessageInputView;
import com.sendbird.uikit.widgets.StatusFrameView;

/**
 * This class creates and performs a view corresponding the message thread input area in Sendbird UIKit.
 *
 * since 3.3.0
 */
public class MessageThreadInputComponent extends MessageInputComponent {
    @NonNull
    private BaseMessage parentMessage;

    @NonNull
    @Override
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle args) {
        // set initial hint text
        // The reason why initialize here is that making hint text needs Context and parentMessage.
        final String defaultHintText = parentMessage.getThreadInfo().getReplyCount() > 0 ?
            context.getString(R.string.sb_text_channel_input_reply_to_thread_hint) :
            context.getString(R.string.sb_text_channel_input_reply_in_thread_hint);
        getParams().setInputHint(defaultHintText);
        return super.onCreateView(context, inflater, parent, args);
    }

    /**
     * Constructor
     *
     * @param parentMessage The parent message of thread.
     * since 3.3.0
     */
    public MessageThreadInputComponent(@NonNull BaseMessage parentMessage) {
        super(new Params());
        this.parentMessage = parentMessage;
    }

    /**
     * Returns a collection of parameters applied to this component.
     *
     * @return {@code Params} applied to this component
     * since 3.3.0
     */
    @NonNull
    @Override
    public Params getParams() {
        return (Params) super.getParams();
    }

    /**
     * Notifies this component that the parent message has been changed.
     *
     * @param channel The latest group channel
     * @param parentMessage The latest parent message
     * since 3.3.0
     */
    public void notifyParentMessageUpdated(@NonNull GroupChannel channel, @NonNull BaseMessage parentMessage) {
        if (!(getRootView() instanceof MessageInputView)) return;
        final MessageInputView inputView = (MessageInputView) this.getRootView();

        this.parentMessage = parentMessage;
        setHintMessageTextInternal(inputView, channel);
    }

    /**
     * Notifies this component that the status has been changed.
     *
     * @param channel The latest group channel
     * @param status The latest status
     * since 3.3.0
     *
     * @deprecated 3.17.0
     */
    @Deprecated
    public void notifyStatusUpdated(@NonNull GroupChannel channel, @NonNull StatusFrameView.Status status) {
        // Do nothing any more since 3.17.0
    }

    @Override
    String getHintText(@NonNull MessageInputView inputView, boolean isMuted, boolean isFrozen) {
        final MessageInputView.Mode mode = inputView.getInputMode();
        // set hint
        final Context context = inputView.getContext();
        String hintText = getParams().getHintText() != null ? getParams().getHintText() : null;
        if (isMuted) {
            hintText = context.getString(R.string.sb_text_channel_input_text_hint_muted);
        } else if (isFrozen) {
            hintText = context.getString(R.string.sb_text_channel_input_text_hint_frozen);
        } else if (MessageInputView.Mode.EDIT == mode) {
            hintText = context.getString(R.string.sb_text_channel_input_text_hint);
        }
        return hintText;
    }

    /**
     * A collection of parameters, which can be applied to a default View. The values of params are not dynamically applied at runtime.
     * Params cannot be created directly, and it is automatically created together when components are created.
     * <p><b>Since the onCreateView configuring View uses the values of the set Params, we recommend that you set up for Params before the onCreateView is called.</b></p>
     *
     * @see #getParams()
     * since 3.3.0
     */
    public static class Params extends MessageInputComponent.Params {}
}
