package com.sendbird.uikit.activities.viewholder;

import android.content.res.TypedArray;

import androidx.annotation.NonNull;

import com.sendbird.android.Emoji;
import com.sendbird.uikit.R;
import com.sendbird.uikit.utils.DrawableUtils;
import com.sendbird.uikit.widgets.EmojiView;

public class EmojiMoreViewHolder extends BaseViewHolder<Emoji> {
    public EmojiMoreViewHolder(@NonNull EmojiView emojiView) {
        super(emojiView);

        TypedArray a = emojiView.getContext()
                .getTheme()
                .obtainStyledAttributes(null,
                        R.styleable.Emoji,
                        R.attr.sb_emoji_reaction_style,
                        R.style.Widget_SendBird_Emoji);

        try {
            int backgroundRes = a.getResourceId(R.styleable.Emoji_sb_emoji_background,
                    R.drawable.sb_emoji_background_light);
            int moreRes = a.getResourceId(R.styleable.Emoji_sb_emoji_more_button_src,
                    R.drawable.icon_emoji_more);
            int moreResTint = a.getResourceId(R.styleable.Emoji_sb_emoji_more_button_src_tint,
                    R.color.onlight_03);

            emojiView.setBackgroundResource(backgroundRes);
            emojiView.setImageDrawable(DrawableUtils.setTintList(emojiView.getContext(), moreRes, moreResTint));
        } finally {
            a.recycle();
        }
    }

    @Override
    public void bind(Emoji item) {}
}
