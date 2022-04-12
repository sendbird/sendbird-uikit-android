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
import com.sendbird.uikit.databinding.SbViewOtherFileVideoMessageBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.widgets.EmojiReactionListView;
import com.sendbird.uikit.widgets.OtherVideoFileMessageView;

import java.util.List;
import java.util.Map;

public final class OtherVideoFileMessageViewHolder extends GroupChannelMessageViewHolder {
    private final EmojiReactionListView emojiReactionListView;
    @NonNull
    private final OtherVideoFileMessageView otherVideoFileMessageView;

    OtherVideoFileMessageViewHolder(@NonNull SbViewOtherFileVideoMessageBinding binding, boolean useMessageGroupUI) {
        super(binding.getRoot(), useMessageGroupUI);
        otherVideoFileMessageView = binding.otherVideoFileMessageView;
        emojiReactionListView = otherVideoFileMessageView.getBinding().rvEmojiReactionList;
        clickableViewMap.put(ClickableViewIdentifier.Chat.name(), otherVideoFileMessageView.getBinding().ivThumbnailOveray);
        clickableViewMap.put(ClickableViewIdentifier.Profile.name(), otherVideoFileMessageView.getBinding().ivProfileView);
        clickableViewMap.put(ClickableViewIdentifier.QuoteReply.name(), otherVideoFileMessageView.getBinding().quoteReplyPanel);
    }

    @Override
    public void bind(@NonNull BaseChannel channel, @NonNull BaseMessage message, @NonNull MessageGroupType messageGroupType) {
        if (channel instanceof GroupChannel) {
            otherVideoFileMessageView.drawMessage((GroupChannel) channel, message, messageGroupType);
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
