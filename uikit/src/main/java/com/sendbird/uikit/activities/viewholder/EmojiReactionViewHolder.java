package com.sendbird.uikit.activities.viewholder;

import androidx.annotation.NonNull;

import com.sendbird.android.message.Reaction;
import com.sendbird.uikit.databinding.SbViewEmojiReactionBinding;

public class EmojiReactionViewHolder extends BaseViewHolder<Reaction> {
    @NonNull
    private final SbViewEmojiReactionBinding binding;

    public EmojiReactionViewHolder(@NonNull SbViewEmojiReactionBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    @Override
    public void bind(@NonNull Reaction item) {
        this.binding.emojiReactionView.drawReaction(item);
    }
}
