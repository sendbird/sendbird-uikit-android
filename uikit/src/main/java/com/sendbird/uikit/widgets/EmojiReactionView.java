package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sendbird.android.Reaction;
import com.sendbird.uikit.R;
import com.sendbird.uikit.databinding.SbViewEmojiReactionComponentBinding;
import com.sendbird.uikit.model.EmojiManager;
import com.sendbird.uikit.utils.DrawableUtils;

public class EmojiReactionView extends FrameLayout {
    private SbViewEmojiReactionComponentBinding binding;

    private int emojiFailedDrawableRes;
    private int emojiFailedDrawableResTint;

    public EmojiReactionView(@NonNull Context context) {
        this(context, null);
    }

    public EmojiReactionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_emoji_reaction_style);
    }

    public EmojiReactionView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EmojiReaction, defStyleAttr, R.style.Widget_SendBird_Emoji);

        try {
            binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.sb_view_emoji_reaction_component, this, true);

            int backgroundRes = a.getResourceId(R.styleable.EmojiReaction_sb_emoji_reaction_background, R.drawable.sb_emoji_reaction_background_light);
            int textStyle = a.getResourceId(R.styleable.EmojiReaction_sb_emoji_reaction_text_appearance, R.style.SendbirdCaption4OnLight01);
            emojiFailedDrawableRes = a.getResourceId(R.styleable.EmojiReaction_sb_emoji_failed_src, R.drawable.icon_question);
            emojiFailedDrawableResTint = a.getResourceId(R.styleable.EmojiReaction_sb_emoji_failed_src_tint, R.color.onlight_03);

            binding.getRoot().setBackgroundResource(backgroundRes);
            binding.tvCount.setTextAppearance(context, textStyle);
        } finally {
            a.recycle();
        }
    }

    public void setBackgroundResource(int backgroundResource) {
        if (binding != null) {
            binding.getRoot().setBackgroundResource(backgroundResource);
        }
    }

    public void setCount(int count) {
        if (binding == null) {
            return;
        }

        if (count <= 0) {
            binding.empty.setVisibility(GONE);
            binding.tvCount.setVisibility(GONE);
        } else {
            binding.empty.setVisibility(VISIBLE);
            binding.tvCount.setVisibility(VISIBLE);

            String countText = count > 99 ? getContext()
                    .getString(R.string.sb_text_channel_reaction_count_max) : String.valueOf(count);
            binding.tvCount.setText(countText);
        }
    }

    public void setImageDrawable(Drawable drawable) {
        if (binding != null) {
            binding.ivEmoji.setImageDrawable(drawable);
        }
    }

    public void setEmojiUrl(String emojiUrl) {
        if (binding != null) {
            int overrideSize = getResources()
                    .getDimensionPixelSize(R.dimen.sb_size_38);

            Glide.with(binding.ivEmoji)
                    .load(emojiUrl)
                    .override(overrideSize, overrideSize)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(DrawableUtils.setTintList(getContext(), emojiFailedDrawableRes, emojiFailedDrawableResTint))
                    .placeholder(DrawableUtils.setTintList(getContext(), emojiFailedDrawableRes, emojiFailedDrawableResTint))
                    .into(binding.ivEmoji);
        }
    }

    public View getLayout() {
        return binding.getRoot();
    }

    public SbViewEmojiReactionComponentBinding getBinding() {
        return binding;
    }

    public void drawReaction(Reaction reaction) {
        if (reaction == null || reaction.getUserIds() == null) {
            return;
        }
        setCount(reaction.getUserIds().size());
        setEmojiUrl(EmojiManager.getInstance().getEmojiUrl(reaction.getKey()));
    }

    @BindingAdapter("reaction")
    public static void drawReaction(@NonNull EmojiReactionView view, Reaction reaction) {
        view.drawReaction(reaction);
    }
}
