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

import androidx.databinding.BindingAdapter;

import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.databinding.SbViewEmojiReactionUserComponentBinding;

import java.util.ArrayList;
import java.util.List;

public class EmoijReactionUserView extends FrameLayout {
    private SbViewEmojiReactionUserComponentBinding binding;

    public EmoijReactionUserView(Context context) {
        this(context, null);
    }

    public EmoijReactionUserView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.sb_emoji_reaction_style);
    }

    public EmoijReactionUserView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
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

    public View getLayout() {
        return this;
    }

    public SbViewEmojiReactionUserComponentBinding getBinding() {
        return binding;
    }

    public void drawUser(User user) {
        Context context = binding.ivUserCover.getContext();
        String nickname = context.getString(R.string.sb_text_channel_list_title_unknown);
        List<String> urls = new ArrayList<>();

        if (user != null) {
            nickname = TextUtils.isEmpty(user.getNickname()) ?
                    context.getString(R.string.sb_text_channel_list_title_unknown) : user.getNickname();
            urls.add(user.getProfileUrl());
        }

        binding.tvNickname.setText(nickname);
        binding.ivUserCover.loadImages(urls);

        if (user != null && user.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
            String meBadge = context.getResources().getString(R.string.sb_text_user_list_badge_me);
            final Spannable spannable = new SpannableString(meBadge);
            int badgeAppearance = SendBirdUIKit.isDarkMode() ? R.style.SendbirdSubtitle2OnDark02 : R.style.SendbirdSubtitle2OnLight02;
            spannable.setSpan(new TextAppearanceSpan(context, badgeAppearance),
                    0, meBadge.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.tvNickname.append(spannable);
        }
    }

    @BindingAdapter("user")
    public static void drawUser(EmoijReactionUserView userView, User user) {
        userView.drawUser(user);
    }
}
