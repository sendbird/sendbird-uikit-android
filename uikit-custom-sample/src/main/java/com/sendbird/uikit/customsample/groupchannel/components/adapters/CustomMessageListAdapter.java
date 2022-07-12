package com.sendbird.uikit.customsample.groupchannel.components.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.UserMessage;
import com.sendbird.uikit.activities.adapter.MessageListAdapter;
import com.sendbird.uikit.activities.viewholder.MessageViewHolder;
import com.sendbird.uikit.customsample.consts.StringSet;
import com.sendbird.uikit.customsample.databinding.ViewEmojiMessageMeHolderBinding;
import com.sendbird.uikit.customsample.databinding.ViewEmojiMessageOtherHolderBinding;
import com.sendbird.uikit.customsample.databinding.ViewHighlightMessageMeHolderBinding;
import com.sendbird.uikit.customsample.databinding.ViewHighlightMessageOtherHolderBinding;
import com.sendbird.uikit.customsample.groupchannel.components.viewholders.EmojiMessageMeViewHolder;
import com.sendbird.uikit.customsample.groupchannel.components.viewholders.EmojiMessageOtherViewHolder;
import com.sendbird.uikit.customsample.groupchannel.components.viewholders.HighlightMessageMeViewHolder;
import com.sendbird.uikit.customsample.groupchannel.components.viewholders.HighlightMessageOtherViewHolder;
import com.sendbird.uikit.utils.MessageUtils;

/**
 * Implements the customized <code>MessageListAdapter</code> to adapt the customized message items.
 */
public class CustomMessageListAdapter extends MessageListAdapter {

    public static final int VIEW_HIGHLIGHT_MESSAGE_ME_TYPE = 1001;
    public static final int VIEW_HIGHLIGHT_MESSAGE_OTHER_TYPE = 1002;
    public static final int VIEW_EMOJI_MESSAGE_ME_TYPE = 1003;
    public static final int VIEW_EMOJI_MESSAGE_OTHER_TYPE = 1004;

    public CustomMessageListAdapter(@NonNull GroupChannel channel, boolean useMessageGroupUI) {
        super(channel, useMessageGroupUI);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // TODO: Create the custom ViewHolder and return it.
        // Create your custom ViewHolder or call super.onCreateViewHolder() if you want to use the default.
        if (viewType == VIEW_HIGHLIGHT_MESSAGE_ME_TYPE) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new HighlightMessageMeViewHolder(ViewHighlightMessageMeHolderBinding.inflate(inflater, parent, false));
        } else if (viewType == VIEW_HIGHLIGHT_MESSAGE_OTHER_TYPE) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new HighlightMessageOtherViewHolder(ViewHighlightMessageOtherHolderBinding.inflate(inflater, parent, false));
        } else if (viewType == VIEW_EMOJI_MESSAGE_ME_TYPE) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new EmojiMessageMeViewHolder(ViewEmojiMessageMeHolderBinding.inflate(inflater, parent, false));
        } else if (viewType == VIEW_EMOJI_MESSAGE_OTHER_TYPE) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new EmojiMessageOtherViewHolder(ViewEmojiMessageOtherHolderBinding.inflate(inflater, parent, false));
        } else {
            return super.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        // You must call the super. You can use methods that MessageViewHolder provides
        super.onBindViewHolder(holder, position);
        // TODO: Bind the custom ViewHolder
    }

    @Override
    public int getItemViewType(int position) {
        BaseMessage message = getItem(position);

        String customType = message.getCustomType();

        if (!TextUtils.isEmpty(customType) &&
                customType.equals(StringSet.emoji_type) &&
                message instanceof UserMessage) {
            if (MessageUtils.isMine(message)) {
                return VIEW_EMOJI_MESSAGE_ME_TYPE;
            } else {
                return VIEW_EMOJI_MESSAGE_OTHER_TYPE;
            }
        } else if (!TextUtils.isEmpty(customType) &&
                customType.equals(StringSet.highlight) &&
                message instanceof UserMessage) {
            if (MessageUtils.isMine(message)) {
                return VIEW_HIGHLIGHT_MESSAGE_ME_TYPE;
            } else {
                return VIEW_HIGHLIGHT_MESSAGE_OTHER_TYPE;
            }
        }

        return super.getItemViewType(position);
    }
}

