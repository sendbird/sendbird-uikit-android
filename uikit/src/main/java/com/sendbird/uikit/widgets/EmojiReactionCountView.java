package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sendbird.android.message.Reaction;
import com.sendbird.uikit.R;
import com.sendbird.uikit.databinding.SbViewEmojiReactionCountComponentBinding;
import com.sendbird.uikit.model.EmojiManager;
import com.sendbird.uikit.utils.DrawableUtils;

public class EmojiReactionCountView extends FrameLayout {
    private SbViewEmojiReactionCountComponentBinding binding;
    private int emojiFailedDrawableRes;
    @Nullable
    private ColorStateList emojiFailedDrawableResTint;

    public EmojiReactionCountView(@NonNull Context context) {
        this(context, null);
    }

    public EmojiReactionCountView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_widget_emoji_message);
    }

    public EmojiReactionCountView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EmojiReactionCount, defStyleAttr, R.style.Widget_Sendbird_Emoji);

        try {
            binding = SbViewEmojiReactionCountComponentBinding.inflate(LayoutInflater.from(context), this, true);
            int textStyleId = a.getResourceId(R.styleable.EmojiReactionCount_sb_emoji_reaction_count_text_appearance, R.style.SendbirdButtonOnLight03);
            emojiFailedDrawableRes = a.getResourceId(R.styleable.EmojiReactionCount_sb_emoji_failed_src, R.drawable.icon_question);
            emojiFailedDrawableResTint = a.getColorStateList(R.styleable.EmojiReactionCount_sb_emoji_failed_src_tint);

            binding.tvCount.setTextAppearance(context, textStyleId);
            // letterSpacing should be 0 to use ellipsize as TextUtils.TruncateAt.MIDDLE
            binding.tvCount.setLetterSpacing(0);
            binding.tvCount.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            binding.tvCount.setSingleLine(true);
        } finally {
            a.recycle();
        }
    }

    public void setCount(int count) {
        if (binding == null) {
            return;
        }

        if (count <= 0) {
            binding.tvCount.setVisibility(GONE);
        } else {
            binding.tvCount.setVisibility(VISIBLE);

            String countText = count > 99 ? getContext()
                    .getString(R.string.sb_text_channel_reaction_count_max) : String.valueOf(count);
            binding.tvCount.setText(countText);
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
    public EmojiReactionCountView getLayout() {
        return this;
    }

    @NonNull
    public SbViewEmojiReactionCountComponentBinding getBinding() {
        return binding;
    }

    public void drawReaction(@Nullable Reaction reaction) {
        if (reaction == null || reaction.getUserIds() == null) {
            return;
        }
        setCount(reaction.getUserIds().size());
        setEmojiUrl(EmojiManager.getInstance().getEmojiUrl(reaction.getKey()));
    }
}
