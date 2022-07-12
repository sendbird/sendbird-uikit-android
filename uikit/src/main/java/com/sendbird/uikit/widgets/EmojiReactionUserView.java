package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.user.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.databinding.SbViewEmojiReactionUserComponentBinding;
import com.sendbird.uikit.utils.UserUtils;

import java.util.ArrayList;
import java.util.List;

public class EmojiReactionUserView extends FrameLayout {
    private SbViewEmojiReactionUserComponentBinding binding;

    public EmojiReactionUserView(@NonNull Context context) {
        this(context, null);
    }

    public EmojiReactionUserView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_widget_emoji_message);
    }

    public EmojiReactionUserView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EmojiReactionUser, defStyle, 0);
        try {
            this.binding = SbViewEmojiReactionUserComponentBinding.inflate(LayoutInflater.from(getContext()));
            addView(binding.getRoot(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            int nicknameAppearance = a.getResourceId(R.styleable.EmojiReactionUser_sb_emoji_reaction_user_nickname_appearance, R.style.SendbirdSubtitle2OnLight01);

            binding.tvNickname.setTextAppearance(context, nicknameAppearance);
            binding.tvNickname.setEllipsize(TextUtils.TruncateAt.END);
            binding.tvNickname.setMaxLines(1);
        } finally {
            a.recycle();
        }
    }

    @NonNull
    public View getLayout() {
        return this;
    }

    @NonNull
    public SbViewEmojiReactionUserComponentBinding getBinding() {
        return binding;
    }

    public void drawUser(@Nullable User user) {
        final Context context = binding.ivUserCover.getContext();
        final String nickname = UserUtils.getDisplayName(context, user);
        final List<String> urls = new ArrayList<>();

        if (user != null) {
            urls.add(user.getProfileUrl());
        }

        binding.tvNickname.setText(nickname);
        binding.ivUserCover.loadImages(urls);

        if (user != null && user.getUserId().equals(SendbirdChat.getCurrentUser().getUserId())) {
            String meBadge = context.getResources().getString(R.string.sb_text_user_list_badge_me);
            final Spannable spannable = new SpannableString(meBadge);
            int badgeAppearance = SendbirdUIKit.isDarkMode() ? R.style.SendbirdSubtitle2OnDark02 : R.style.SendbirdSubtitle2OnLight02;
            spannable.setSpan(new TextAppearanceSpan(context, badgeAppearance),
                    0, meBadge.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.tvNickname.append(spannable);
        }
    }
}
