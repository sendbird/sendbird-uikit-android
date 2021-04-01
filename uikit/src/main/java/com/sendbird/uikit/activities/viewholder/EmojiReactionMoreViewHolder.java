package com.sendbird.uikit.activities.viewholder;

import android.content.res.TypedArray;

import androidx.annotation.NonNull;

import com.sendbird.android.Reaction;
import com.sendbird.uikit.R;
import com.sendbird.uikit.utils.DrawableUtils;
import com.sendbird.uikit.widgets.EmojiReactionView;

public class EmojiReactionMoreViewHolder extends BaseViewHolder<Reaction> {

    public EmojiReactionMoreViewHolder(@NonNull EmojiReactionView view) {
        super(view);

        TypedArray a = view.getContext()
                .getTheme()
                .obtainStyledAttributes(null,
                        R.styleable.EmojiReaction,
                        R.attr.sb_emoji_reaction_style,
                        R.style.Widget_SendBird_Emoji);

        try {
            int backgroundRes = a.getResourceId(
                    R.styleable.EmojiReaction_sb_emoji_reaction_background,
                    R.drawable.sb_emoji_reaction_background_light);
            int moreRes = a.getResourceId(R.styleable.EmojiReaction_sb_emoji_reaction_more_button_src,
                    R.drawable.icon_emoji_more);
            int moreResTint = a.getResourceId(R.styleable.EmojiReaction_sb_emoji_reaction_more_button_src_tint,
                    R.color.onlight_03);

            view.setBackgroundResource(backgroundRes);
            view.setImageDrawable(DrawableUtils.setTintList(view.getContext(), moreRes, moreResTint));
            view.setCount(0);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void bind(Reaction item) {}
}
