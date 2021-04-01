package com.sendbird.uikit.activities.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.Reaction;
import com.sendbird.uikit.BR;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.widgets.EmojiReactionListView;
import com.sendbird.uikit.widgets.OtherFileMessageView;

import java.util.List;

public class OtherFileMessageViewHolder extends OtherMessageViewHolder {
    private final View clickableView;
    private final EmojiReactionListView emojiReactionListView;

    OtherFileMessageViewHolder(@NonNull ViewDataBinding binding, boolean useMessageGroupUI) {
        super(binding, useMessageGroupUI);
        clickableView = ((OtherFileMessageView) binding.getRoot()).getBinding().contentPanelWithReactions;
        emojiReactionListView = ((OtherFileMessageView) binding.getRoot()).getBinding().rvEmojiReactionList;
    }

    @Override
    public View getProfileView() {
        return ((OtherFileMessageView) binding.getRoot()).getBinding().ivProfileView;
    }

    @Override
    public void bind(BaseChannel channel, @NonNull BaseMessage message, MessageGroupType messageGroupType) {
        binding.setVariable(BR.channel, channel);
        binding.setVariable(BR.message, message);
        binding.setVariable(BR.messageGroupType, messageGroupType);
        binding.setVariable(BR.highlightInfo, highlight);
    }

    @Override
    public View getClickableView() {
        return clickableView;
    }

    @Override
    public void setEmojiReaction(List<Reaction> reactionList,
                                 OnItemClickListener<String> emojiReactionClickListener,
                                 OnItemLongClickListener<String> emojiReactionLongClickListener,
                                 View.OnClickListener moreButtonClickListener) {
        emojiReactionListView.setReactionList(reactionList);
        emojiReactionListView.setEmojiReactionClickListener(emojiReactionClickListener);
        emojiReactionListView.setEmojiReactionLongClickListener(emojiReactionLongClickListener);
        emojiReactionListView.setMoreButtonClickListener(moreButtonClickListener);
    }
}
