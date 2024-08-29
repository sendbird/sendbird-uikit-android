package com.sendbird.uikit.activities.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.message.Emoji;
import com.sendbird.android.message.Reaction;
import com.sendbird.uikit.interfaces.EmojiReactionHandler;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.model.MessageListUIParams;

import java.util.List;

/**
 * A ViewHolder describes an item view and Message about its place within the RecyclerView.
 */
public abstract class GroupChannelMessageViewHolder extends MessageViewHolder implements EmojiReactionHandler {
    public GroupChannelMessageViewHolder(@NonNull View view) {
        super(view);
    }

    public GroupChannelMessageViewHolder(@NonNull View view, @NonNull MessageListUIParams messageListUIParams) {
        super(view, messageListUIParams);
    }

    /**
     * Sets message reaction data.
     *
     * @param reactionList List of reactions which the message has.
     * @param emojiReactionClickListener The callback to be invoked when the emoji reaction is clicked and held.
     * @param emojiReactionLongClickListener The callback to be invoked when the emoji reaction is long clicked and held.
     * @param moreButtonClickListener The callback to be invoked when the emoji reaction more button is clicked and held.
     * since 1.1.0
     */
    abstract public void setEmojiReaction(@NonNull List<Reaction> reactionList,
                                          @Nullable OnItemClickListener<String> emojiReactionClickListener,
                                          @Nullable OnItemLongClickListener<String> emojiReactionLongClickListener,
                                          @Nullable View.OnClickListener moreButtonClickListener);

    /**
     * Sets message reaction data with total emoji count allowed for the message.
     *
     * @param reactionList                   List of reactions which the message has.
     * @param totalEmojiList                 The total list of emojis allowed for this message. This value is used to compare whether `add` button should be displayed from the reactions view. Defaults to {@link com.sendbird.uikit.model.EmojiManager.getAllEmojis()}.
     * @param emojiReactionClickListener     The callback to be invoked when the emoji reaction is clicked and held.
     * @param emojiReactionLongClickListener The callback to be invoked when the emoji reaction is long clicked and held.
     * @param moreButtonClickListener        The callback to be invoked when the emoji reaction more button is clicked and held.
     * @since 3.20.0
     */
    public void setEmojiReaction(@NonNull List<Reaction> reactionList,
                                 @NonNull List<Emoji> totalEmojiList,
                                 @Nullable OnItemClickListener<String> emojiReactionClickListener,
                                 @Nullable OnItemLongClickListener<String> emojiReactionLongClickListener,
                                 @Nullable View.OnClickListener moreButtonClickListener) {
        setEmojiReaction(reactionList, emojiReactionClickListener, emojiReactionLongClickListener, moreButtonClickListener);
    }
}
