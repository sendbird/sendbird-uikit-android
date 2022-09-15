package com.sendbird.uikit.activities.viewholder;

import android.content.res.ColorStateList;
import android.content.res.TypedArray;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.sendbird.android.message.Emoji;
import com.sendbird.uikit.R;
import com.sendbird.uikit.internal.ui.reactions.EmojiView;
import com.sendbird.uikit.utils.DrawableUtils;

public class EmojiMoreViewHolder extends BaseViewHolder<Emoji> {
    public EmojiMoreViewHolder(@NonNull EmojiView emojiView) {
        super(emojiView);

        TypedArray a = emojiView.getContext()
                .getTheme()
                .obtainStyledAttributes(null,
                        R.styleable.Emoji,
                        R.attr.sb_widget_emoji_message,
                        R.style.Widget_Sendbird_Emoji);

        try {
            int backgroundRes = a.getResourceId(R.styleable.Emoji_sb_emoji_background,
                    R.drawable.sb_emoji_background_light);
            int moreRes = a.getResourceId(R.styleable.Emoji_sb_emoji_more_button_src,
                    R.drawable.icon_emoji_more);
            ColorStateList moreResTint = a.getColorStateList(R.styleable.Emoji_sb_emoji_more_button_src_tint);

            emojiView.setBackgroundResource(backgroundRes);
            if (moreResTint != null) {
                emojiView.setImageDrawable(DrawableUtils.setTintList(emojiView.getContext(), moreRes, moreResTint));
            } else {
                emojiView.setImageDrawable(AppCompatResources.getDrawable(emojiView.getContext(), moreRes));
            }
        } finally {
            a.recycle();
        }
    }

    @Override
    public void bind(@NonNull Emoji item) {}
}
