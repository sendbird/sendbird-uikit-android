package com.sendbird.uikit.activities.viewholder;

import androidx.annotation.NonNull;

import com.sendbird.android.Emoji;
import com.sendbird.uikit.databinding.SbViewEmojiBinding;

public class EmojiViewHolder extends BaseViewHolder<Emoji> {
    @NonNull
    private final SbViewEmojiBinding binding;

    public EmojiViewHolder(@NonNull SbViewEmojiBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    @Override
    public void bind(@NonNull Emoji item) {
        binding.emojiView.drawEmoji(item);
    }
}
