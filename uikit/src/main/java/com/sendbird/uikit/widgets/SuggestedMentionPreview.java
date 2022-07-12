package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.user.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.databinding.SbViewSuggestedMentionListItemBinding;
import com.sendbird.uikit.utils.UserUtils;
import com.sendbird.uikit.utils.ViewUtils;

public class SuggestedMentionPreview extends FrameLayout {
    private final SbViewSuggestedMentionListItemBinding binding;
    private final int nicknameTextAppearance;
    private final int emptyNicknameTextAppearance;

    public SuggestedMentionPreview(@NonNull Context context) {
        this(context, null);
    }

    public SuggestedMentionPreview(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_widget_suggested_mention_preview);
    }

    public SuggestedMentionPreview(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.UserPreview, defStyle, 0);
        try {
            this.binding = SbViewSuggestedMentionListItemBinding.inflate(LayoutInflater.from(getContext()));
            addView(binding.getRoot(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            int background = a.getResourceId(R.styleable.UserPreview_sb_member_preview_background, R.drawable.selector_rectangle_light);
            int descAppearance = a.getResourceId(R.styleable.UserPreview_sb_member_preview_description_appearance, R.style.SendbirdBody2OnLight02);
            this.nicknameTextAppearance = a.getResourceId(R.styleable.UserPreview_sb_member_preview_nickname_appearance, R.style.SendbirdSubtitle2OnLight01);
            this.emptyNicknameTextAppearance = a.getResourceId(R.styleable.UserPreview_sb_mention_empty_nickname_appearance, R.style.SendbirdBody2OnLight03);
            binding.getRoot().setBackgroundResource(background);
            binding.tvNickname.setEllipsize(TextUtils.TruncateAt.END);
            binding.tvNickname.setMaxLines(1);
            binding.tvDescription.setTextAppearance(context, descAppearance);
        } finally {
            a.recycle();
        }
    }

    @NonNull
    public View getLayout() {
        return this;
    }

    @NonNull
    public SbViewSuggestedMentionListItemBinding getBinding() {
        return binding;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener listener) {
        binding.vgMemberItem.setOnClickListener(listener);
    }

    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener listener) {
        binding.vgMemberItem.setOnLongClickListener(listener);
    }

    public void setOnProfileClickListener(@Nullable OnClickListener listener) {
        binding.ivProfile.setOnClickListener(listener);
    }

    public void setDescription(@Nullable CharSequence text) {
        binding.tvDescription.setText(text);
    }

    public void setName(@Nullable CharSequence name) {
        binding.tvNickname.setText(name);
    }

    public void setImageFromUrl(@Nullable String url) {
        ViewUtils.drawProfile(binding.ivProfile, url);
    }

    public void drawUser(@NonNull User user, boolean showUserId) {
        final Context context = getContext();
        final String nickname = UserUtils.getDisplayName(getContext(), user);

        if (TextUtils.isEmpty(user.getNickname())) {
            binding.tvNickname.setTextAppearance(context, emptyNicknameTextAppearance);
        } else {
            binding.tvNickname.setTextAppearance(context, nicknameTextAppearance);
        }
        setName(nickname);

        if (showUserId) {
            final String description = user.getUserId();
            setDescription(description);
        }
        setImageFromUrl(user.getProfileUrl());
    }
}
