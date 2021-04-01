package com.sendbird.uikit.activities.viewholder;

import androidx.annotation.NonNull;

import com.sendbird.android.Emoji;
import com.sendbird.uikit.BR;
import com.sendbird.uikit.databinding.SbViewEmojiBinding;

public class EmojiViewHolder extends BaseViewHolder<Emoji> {
    private final SbViewEmojiBinding binding;

    public EmojiViewHolder(@NonNull SbViewEmojiBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    @Override
    public void bind(Emoji item) {
        binding.setVariable(BR.emoji, item);
        binding.executePendingBindings();
    }
}
