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
import com.sendbird.uikit.widgets.OtherImageFileMessageView;

import java.util.List;

public class OtherImageFileMessageViewHolder extends OtherMessageViewHolder {
    private final View clickableView;
    private final EmojiReactionListView emojiReactionListView;

    OtherImageFileMessageViewHolder(@NonNull ViewDataBinding binding, boolean useMessageGroupUI) {
        super(binding, useMessageGroupUI);
        clickableView = ((OtherImageFileMessageView) binding.getRoot()).getBinding().ivThumbnailOveray;
        emojiReactionListView = ((OtherImageFileMessageView) binding.getRoot()).getBinding().rvEmojiReactionList;
    }

    @Override
    public View getProfileView() {
        return ((OtherImageFileMessageView) binding.getRoot()).getBinding().ivProfileView;
    }

    @Override
    public void bind(BaseChannel channel, @NonNull BaseMessage message, MessageGroupType messageGroupType) {
        binding.setVariable(BR.channel, channel);
        binding.setVariable(BR.message, message);
        binding.setVariable(BR.messageGroupType, messageGroupType);
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
