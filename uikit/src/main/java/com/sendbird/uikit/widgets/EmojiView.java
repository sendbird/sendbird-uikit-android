package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

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
    @Nullable
    private ColorStateList emojiFailedDrawableResTint;

    public EmojiView(@NonNull Context context) {
        this(context, null);
    }

    public EmojiView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_widget_emoji_message);
    }

    public EmojiView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Emoji, defStyleAttr, R.style.Widget_Sendbird_Emoji);

        try {
            binding = SbViewEmojiComponentBinding.inflate(LayoutInflater.from(getContext()), this, true);

            backgroundResource = a.getResourceId(R.styleable.Emoji_sb_emoji_background, R.drawable.sb_emoji_background_light);
            emojiFailedDrawableRes = a.getResourceId(R.styleable.Emoji_sb_emoji_failed_src, R.drawable.icon_question);
            emojiFailedDrawableResTint = a.getColorStateList(R.styleable.Emoji_sb_emoji_failed_src_tint);

            binding.emojiPanel.setBackgroundResource(backgroundResource);
            Drawable failedDrawable;
            if (emojiFailedDrawableResTint != null) {
                failedDrawable = DrawableUtils.setTintList(getContext(), emojiFailedDrawableRes, emojiFailedDrawableResTint);
            } else {
                failedDrawable = AppCompatResources.getDrawable(getContext(), emojiFailedDrawableRes);
            }
            binding.ivEmoji.setImageDrawable(failedDrawable);
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

    public void setImageDrawable(@Nullable Drawable drawable) {
        if (binding != null) {
            binding.ivEmoji.setImageDrawable(drawable);
        }
    }

    public void setEmojiUrl(@Nullable String emojiUrl) {
        if (binding != null) {
            int overrideSize = getResources()
                    .getDimensionPixelSize(R.dimen.sb_size_38);

            Drawable failedDrawable;
            if (emojiFailedDrawableResTint != null) {
                failedDrawable = DrawableUtils.setTintList(getContext(), emojiFailedDrawableRes, emojiFailedDrawableResTint);
            } else {
                failedDrawable = AppCompatResources.getDrawable(getContext(), emojiFailedDrawableRes);
            }

            Glide.with(binding.ivEmoji)
                    .load(emojiUrl)
                    .override(overrideSize, overrideSize)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(failedDrawable)
                    .placeholder(failedDrawable)
                    .into(binding.ivEmoji);
        }
    }

    @NonNull
    public View getLayout() {
        return binding.getRoot();
    }

    @NonNull
    public SbViewEmojiComponentBinding getBinding() {
        return binding;
    }

    public void drawEmoji(@Nullable Emoji emoji) {
        if (emoji == null) {
            return;
        }
        setEmojiUrl(EmojiManager.getInstance().getEmojiUrl(emoji.getKey()));
    }
}
