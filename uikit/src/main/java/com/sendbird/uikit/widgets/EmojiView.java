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
import com.sendbird.android.Emoji;
import com.sendbird.uikit.R;
import com.sendbird.uikit.databinding.SbViewEmojiComponentBinding;
import com.sendbird.uikit.model.EmojiManager;
import com.sendbird.uikit.utils.DrawableUtils;

public class EmojiView extends FrameLayout {
    private SbViewEmojiComponentBinding binding;
    private int backgroundResource;
    private int emojiFailedDrawableRes;
    private int emojiFailedDrawableResTint;

    public EmojiView(@NonNull Context context) {
        this(context, null);
    }

    public EmojiView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_emoji_reaction_style);
    }

    public EmojiView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Emoji, defStyleAttr, R.style.Widget_SendBird_Emoji);

        try {
            binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.sb_view_emoji_component, this, true);

            backgroundResource = a.getResourceId(R.styleable.Emoji_sb_emoji_background, R.drawable.sb_emoji_background_light);
            emojiFailedDrawableRes = a.getResourceId(R.styleable.Emoji_sb_emoji_failed_src, R.drawable.icon_question);
            emojiFailedDrawableResTint = a.getResourceId(R.styleable.Emoji_sb_emoji_failed_src_tint, R.color.onlight_03);

            binding.emojiPanel.setBackgroundResource(backgroundResource);
            binding.ivEmoji.setImageDrawable(DrawableUtils.setTintList(getContext(), emojiFailedDrawableRes, emojiFailedDrawableResTint));
        } finally {
            a.recycle();
        }
    }

    public void setBackgroundResource(int backgroundResource) {
        this.backgroundResource = backgroundResource;
        if (binding != null) {
            binding.emojiPanel.setBackgroundResource(backgroundResource);
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

    public SbViewEmojiComponentBinding getBinding() {
        return binding;
    }

    public void drawEmoji(Emoji emoji) {
        if (emoji == null) {
            return;
        }
        setEmojiUrl(EmojiManager.getInstance().getEmojiUrl(emoji.getKey()));
    }

    @BindingAdapter("emoji")
    public static void drawEmoji(@NonNull EmojiView view, Emoji emoji) {
        view.drawEmoji(emoji);
    }
}
