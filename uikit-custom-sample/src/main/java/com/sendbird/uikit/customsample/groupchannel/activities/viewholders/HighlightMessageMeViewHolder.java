package com.sendbird.uikit.customsample.groupchannel.activities.viewholders;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.Reaction;
import com.sendbird.uikit.activities.viewholder.GroupChannelMessageViewHolder;
import com.sendbird.uikit.consts.ClickableViewIdentifier;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.customsample.databinding.ViewHighlightMessageMeHolderBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;

import java.util.List;
import java.util.Map;

public class HighlightMessageMeViewHolder extends GroupChannelMessageViewHolder {
    private final ViewHighlightMessageMeHolderBinding binding;

    public HighlightMessageMeViewHolder(ViewHighlightMessageMeHolderBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        clickableViewMap.put(ClickableViewIdentifier.Chat.name(), binding.tvMessage);
    }

    @Override
    public void bind(BaseChannel channel, @NonNull BaseMessage message, MessageGroupType messageGroupType) {
        Context context = binding.getRoot().getContext();
        boolean sendingState = message.getSendingStatus() == BaseMessage.SendingStatus.SUCCEEDED;

        binding.tvSentAt.setVisibility(sendingState ? View.VISIBLE : View.GONE);
        String sentAt = DateUtils.formatDateTime(context, message.getCreatedAt(), DateUtils.FORMAT_SHOW_TIME);
        binding.tvSentAt.setText(sentAt);
        binding.ivStatus.drawStatus(message, channel);
        binding.tvMessage.setText(message.getMessage());

        int paddingTop = context.getResources().getDimensionPixelSize(com.sendbird.uikit.R.dimen.sb_size_8);
        int paddingBottom = context.getResources().getDimensionPixelSize(com.sendbird.uikit.R.dimen.sb_size_8);
        binding.root.setPadding(binding.root.getPaddingLeft(), paddingTop, binding.root.getPaddingRight(), paddingBottom);
    }

    @Override
    public Map<String, View> getClickableViewMap() {
        return clickableViewMap;
    }

    @Override
    public void setEmojiReaction(List<Reaction> reactionList, OnItemClickListener<String> emojiReactionClickListener, OnItemLongClickListener<String> emojiReactionLongClickListener, View.OnClickListener moreButtonClickListener) {}
}
