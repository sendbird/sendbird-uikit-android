package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
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
import androidx.appcompat.content.res.AppCompatResources;

import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.databinding.SbViewMemberListItemBinding;
import com.sendbird.uikit.utils.DrawableUtils;
import com.sendbird.uikit.utils.UserUtils;
import com.sendbird.uikit.utils.ViewUtils;

public class UserPreview extends FrameLayout {
    private final SbViewMemberListItemBinding binding;

    public UserPreview(@NonNull Context context) {
        this(context, null);
    }

    public UserPreview(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_widget_user_preview);
    }

    public UserPreview(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.UserPreview, defStyle, 0);
        try {
            this.binding = SbViewMemberListItemBinding.inflate(LayoutInflater.from(getContext()));
            addView(binding.getRoot(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            int background = a.getResourceId(R.styleable.UserPreview_sb_member_preview_background, R.drawable.selector_rectangle_light);
            int nicknameAppearance = a.getResourceId(R.styleable.UserPreview_sb_member_preview_nickname_appearance, R.style.SendbirdSubtitle2OnLight01);
            int descAppearance = a.getResourceId(R.styleable.UserPreview_sb_member_preview_description_appearance, R.style.SendbirdBody2OnLight02);
            int actionMenuBgResId = a.getResourceId(R.styleable.UserPreview_sb_member_preview_action_menu_background, R.drawable.sb_button_uncontained_background_light);
            binding.getRoot().setBackgroundResource(background);
            binding.tvNickname.setTextAppearance(context, nicknameAppearance);
            binding.tvNickname.setEllipsize(TextUtils.TruncateAt.END);
            binding.tvNickname.setMaxLines(1);
            binding.tvDescription.setTextAppearance(context, descAppearance);

            binding.ivAction.setBackgroundResource(actionMenuBgResId);
            int moreTint = SendbirdUIKit.isDarkMode() ? R.color.sb_selector_icon_more_color_dark : R.color.sb_selector_icon_more_color_light;
            binding.ivAction.setImageDrawable(DrawableUtils.setTintList(binding.ivAction.getDrawable(),
                    AppCompatResources.getColorStateList(context, moreTint)));

            Drawable muteDrawable = DrawableUtils.createOvalIcon(context,
                    SendbirdUIKit.getDefaultThemeMode().getPrimaryTintResId(),
                    255 / 2,
                    R.drawable.icon_mute,
                    R.color.background_50);
            binding.ivProfileOverlay.setImageDrawable(muteDrawable);
        } finally {
            a.recycle();
        }
    }

    @NonNull
    public View getLayout() {
        return this;
    }

    @NonNull
    public SbViewMemberListItemBinding getBinding() {
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

    public void useActionMenu(boolean use) {
        binding.ivAction.setVisibility(use ? View.VISIBLE : View.GONE);
    }

    public void setOnActionMenuClickListener(@Nullable OnClickListener listener) {
        binding.ivAction.setOnClickListener(listener);
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

    public void setVisibleOverlay(int visiblility) {
        binding.ivProfileOverlay.setVisibility(visiblility);
    }

    public void enableActionMenu(boolean enabled) {
        binding.ivAction.setEnabled(enabled);
    }

    public static void drawMember(@NonNull UserPreview preview, @NonNull Member member) {
        Context context = preview.getContext();
        boolean isOperatorMember = member.getRole() == Member.Role.OPERATOR;
        boolean isMe = member.getUserId().equals(SendBird.getCurrentUser().getUserId());
        final String nickname = UserUtils.getDisplayName(context, member);
        preview.setName(nickname);

        String description = isOperatorMember ? context.getString(R.string.sb_text_operator) : "";
        preview.setDescription(description);
        preview.setImageFromUrl(member.getProfileUrl());
        preview.enableActionMenu(!isMe);
        preview.setVisibleOverlay(member.isMuted() ? View.VISIBLE : View.GONE);

        if (isMe) {
            String meBadge = nickname + context.getResources().getString(R.string.sb_text_user_list_badge_me);
            final Spannable spannable = new SpannableString(meBadge);
            int badgeAppearance = SendbirdUIKit.isDarkMode() ? R.style.SendbirdSubtitle2OnDark02 : R.style.SendbirdSubtitle2OnLight02;
            int originLen = nickname.length();
            spannable.setSpan(new TextAppearanceSpan(context, badgeAppearance),
                    originLen, meBadge.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            preview.setName(spannable);
        }
    }

    public static void drawMemberFromUser(@NonNull UserPreview preview, @NonNull User user) {
        Context context = preview.getContext();
        boolean isMe = user.getUserId().equals(SendBird.getCurrentUser().getUserId());
        final String nickname = UserUtils.getDisplayName(context, user);
        preview.setName(nickname);

        preview.setDescription("");
        preview.setImageFromUrl(user.getProfileUrl());
        preview.enableActionMenu(!isMe);

        if (isMe) {
            String meBadge = nickname + context.getResources().getString(R.string.sb_text_user_list_badge_me);
            final Spannable spannable = new SpannableString(meBadge);
            int badgeAppearance = SendbirdUIKit.isDarkMode() ? R.style.SendbirdSubtitle2OnDark02 : R.style.SendbirdSubtitle2OnLight02;
            int originLen = nickname.length();
            spannable.setSpan(new TextAppearanceSpan(context, badgeAppearance),
                    originLen, meBadge.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            preview.setName(spannable);
        }
    }
}
