package com.sendbird.uikit.activities.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;

import com.sendbird.android.Reaction;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;

import java.util.List;

/**
 * A ViewHolder describes an item view and Message about its place within the RecyclerView.
 */
public abstract class GroupChannelMessageViewHolder extends MessageViewHolder {
    public GroupChannelMessageViewHolder(View view) {
        super(view);
    }

    GroupChannelMessageViewHolder(@NonNull ViewDataBinding binding, boolean useMessageGroupUI) {
        super(binding, useMessageGroupUI);
    }

    /**
     * Sets message reaction data.
     *
     * @param reactionList List of reactions which the message has.
     * @param emojiReactionClickListener The callback to be invoked when the emoji reaction is clicked and held.
     * @param emojiReactionLongClickListener The callback to be invoked when the emoji reaction is long clicked and held.
     * @param moreButtonClickListener The callback to be invoked when the emoji reaction more button is clicked and held.
     * @since 1.1.0
     */
    abstract public void setEmojiReaction(List<Reaction> reactionList,
                                          OnItemClickListener<String> emojiReactionClickListener,
                                          OnItemLongClickListener<String> emojiReactionLongClickListener,
                                          View.OnClickListener moreButtonClickListener);
}

