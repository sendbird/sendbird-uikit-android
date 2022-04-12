package com.sendbird.uikit.activities.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.Reaction;
import com.sendbird.uikit.consts.ClickableViewIdentifier;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.databinding.SbViewOtherUserMessageBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.widgets.EmojiReactionListView;
import com.sendbird.uikit.widgets.OtherUserMessageView;

import java.util.List;
import java.util.Map;

public final class OtherUserMessageViewHolder extends GroupChannelMessageViewHolder {
    private final EmojiReactionListView emojiReactionListView;
    @NonNull
    private final OtherUserMessageView otherUserMessageView;

    OtherUserMessageViewHolder(@NonNull SbViewOtherUserMessageBinding binding, boolean useMessageGroupUI) {
        super(binding.getRoot(), useMessageGroupUI);
        otherUserMessageView = binding.otherMessageView;
        emojiReactionListView = otherUserMessageView.getBinding().rvEmojiReactionList;
        clickableViewMap.put(ClickableViewIdentifier.Chat.name(), otherUserMessageView.getBinding().contentPanel);
        clickableViewMap.put(ClickableViewIdentifier.Profile.name(), otherUserMessageView.getBinding().ivProfileView);
        clickableViewMap.put(ClickableViewIdentifier.QuoteReply.name(), otherUserMessageView.getBinding().quoteReplyPanel);
    }

    @Override
    public void bind(@NonNull BaseChannel channel, @NonNull BaseMessage message, @NonNull MessageGroupType messageGroupType) {
        otherUserMessageView.setHighlightMessageInfo(highlight);
        if (channel instanceof GroupChannel) {
            otherUserMessageView.drawMessage((GroupChannel) channel, message, messageGroupType);
        }
    }

    @Override
    public void setEmojiReaction(@NonNull List<Reaction> reactionList,
                                 @Nullable OnItemClickListener<String> emojiReactionClickListener,
                                 @Nullable OnItemLongClickListener<String> emojiReactionLongClickListener,
                                 @Nullable View.OnClickListener moreButtonClickListener) {
        emojiReactionListView.setReactionList(reactionList);
        emojiReactionListView.setEmojiReactionClickListener(emojiReactionClickListener);
        emojiReactionListView.setEmojiReactionLongClickListener(emojiReactionLongClickListener);
        emojiReactionListView.setMoreButtonClickListener(moreButtonClickListener);
    }

    @Override
    @NonNull
    public Map<String, View> getClickableViewMap() {
        return clickableViewMap;
    }
}
