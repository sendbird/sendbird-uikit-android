package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sendbird.android.Reaction;
import com.sendbird.uikit.R;
import com.sendbird.uikit.databinding.SbViewEmojiReactionCountComponentBinding;
import com.sendbird.uikit.model.EmojiManager;
import com.sendbird.uikit.utils.DrawableUtils;

public class EmojiReactionCountView extends FrameLayout {
    private SbViewEmojiReactionCountComponentBinding binding;
    private int emojiFailedDrawableRes;
    private int emojiFailedDrawableResTint;

    public EmojiReactionCountView(@NonNull Context context) {
        this(context, null);
    }

    public EmojiReactionCountView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_emoji_reaction_style);
    }

    public EmojiReactionCountView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EmojiReactionCount, defStyleAttr, R.style.Widget_SendBird_Emoji);

        try {
            binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.sb_view_emoji_reaction_count_component,this, true);
            int textStyleId = a.getResourceId(R.styleable.EmojiReactionCount_sb_emoji_reaction_count_text_appearance, R.style.SendbirdButtonOnLight03);
            emojiFailedDrawableRes = a.getResourceId(R.styleable.EmojiReactionCount_sb_emoji_failed_src, R.drawable.icon_question);
            emojiFailedDrawableResTint = a.getResourceId(R.styleable.EmojiReactionCount_sb_emoji_failed_src_tint, R.color.onlight_03);

            binding.tvCount.setTextAppearance(context, textStyleId);
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

    public EmojiReactionCountView getLayout() {
        return this;
    }

    public SbViewEmojiReactionCountComponentBinding getBinding() {
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
    public static void drawReaction(EmojiReactionCountView view, Reaction reaction) {
        view.drawReaction(reaction);
    }
}
