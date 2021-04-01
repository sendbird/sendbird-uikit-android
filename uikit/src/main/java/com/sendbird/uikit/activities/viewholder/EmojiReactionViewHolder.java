package com.sendbird.uikit.activities.viewholder;

import androidx.annotation.NonNull;

import com.sendbird.android.Reaction;
import com.sendbird.uikit.BR;
import com.sendbird.uikit.databinding.SbViewEmojiReactionBinding;

public class EmojiReactionViewHolder extends BaseViewHolder<Reaction> {
    private final SbViewEmojiReactionBinding binding;

    public EmojiReactionViewHolder(@NonNull SbViewEmojiReactionBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    @Override
    public void bind(Reaction item) {
        binding.setVariable(BR.reaction, item);
        binding.executePendingBindings();
    }
}
