package com.sendbird.uikit.activities.viewholder;

import android.content.res.ColorStateList;
import android.content.res.TypedArray;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.sendbird.android.message.Reaction;
import com.sendbird.uikit.R;
import com.sendbird.uikit.internal.ui.reactions.EmojiReactionView;
import com.sendbird.uikit.utils.DrawableUtils;

public class EmojiReactionMoreViewHolder extends BaseViewHolder<Reaction> {

    public EmojiReactionMoreViewHolder(@NonNull EmojiReactionView view) {
        super(view);

        TypedArray a = view.getContext()
                .getTheme()
                .obtainStyledAttributes(null,
                        R.styleable.EmojiReaction,
                        R.attr.sb_widget_emoji_message,
                        R.style.Widget_Sendbird_Emoji);

        try {
            int backgroundRes = a.getResourceId(
                    R.styleable.EmojiReaction_sb_emoji_reaction_background,
                    R.drawable.sb_emoji_reaction_background_light);
            int moreRes = a.getResourceId(R.styleable.EmojiReaction_sb_emoji_reaction_more_button_src,
                    R.drawable.icon_emoji_more);
            ColorStateList moreResTint = a.getColorStateList(R.styleable.EmojiReaction_sb_emoji_reaction_more_button_src_tint);

            view.setBackgroundResource(backgroundRes);
            if (moreResTint != null) {
                view.setImageDrawable(DrawableUtils.setTintList(view.getContext(), moreRes, moreResTint));
            } else {
                view.setImageDrawable(AppCompatResources.getDrawable(view.getContext(), moreRes));
            }
            view.setCount(0);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void bind(@NonNull Reaction item) {}
}
