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
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.databinding.SbViewMemberListItemBinding;
import com.sendbird.uikit.utils.DrawableUtils;
import com.sendbird.uikit.utils.ViewUtils;

public class MemberPreview extends FrameLayout {
    private SbViewMemberListItemBinding binding;

    public MemberPreview(Context context) {
        this(context, null);
    }

    public MemberPreview(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.sb_member_preview_style);
    }

    public MemberPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MemberPreview, defStyle, 0);
        try {
            this.binding = SbViewMemberListItemBinding.inflate(LayoutInflater.from(getContext()));
            addView(binding.getRoot(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            int nicknameAppearance = a.getResourceId(R.styleable.MemberPreview_sb_member_preview_nickname_appearance, R.style.SendbirdSubtitle2OnLight01);
            int descAppearance = a.getResourceId(R.styleable.MemberPreview_sb_member_preview_description_appearance, R.style.SendbirdBody2OnLight02);
            int actionMenuBgResId = a.getResourceId(R.styleable.MemberPreview_sb_member_preview_action_menu_background, R.drawable.sb_button_uncontained_background_light);
            binding.tvNickname.setTextAppearance(context, nicknameAppearance);
            binding.tvNickname.setEllipsize(TextUtils.TruncateAt.END);
            binding.tvNickname.setMaxLines(1);
            binding.tvDescription.setTextAppearance(context, descAppearance);

            binding.ivAction.setBackgroundResource(actionMenuBgResId);
            int moreTint = SendBirdUIKit.isDarkMode() ? R.color.sb_selector_icon_more_color_dark : R.color.sb_selector_icon_more_color_light;
            binding.ivAction.setImageDrawable(DrawableUtils.setTintList(binding.ivAction.getDrawable(),
                    AppCompatResources.getColorStateList(context, moreTint)));

            Drawable muteDrawable = DrawableUtils.createOvalIcon(context,
                    SendBirdUIKit.getDefaultThemeMode().getPrimaryTintResId(),
                    255 / 2,
                    R.drawable.icon_mute,
                    R.color.background_50);
            binding.ivProfileOverlay.setImageDrawable(muteDrawable);
        } finally {
            a.recycle();
        }
    }

    public View getLayout() {
        return this;
    }

    public SbViewMemberListItemBinding getBinding() {
        return binding;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener listener) {
        binding.vgMemberItem.setOnClickListener(listener);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener listener) {
        binding.vgMemberItem.setOnLongClickListener(listener);
    }

    public void useActionMenu(boolean use) {
        binding.ivAction.setVisibility(use ? View.VISIBLE : View.GONE);
    }

    public void setOnActionMenuClickListener(OnClickListener listener) {
        binding.ivAction.setOnClickListener(listener);
    }

    public void setOnProfileClickListener(OnClickListener listener) {
        binding.ivProfile.setOnClickListener(listener);
    }

    public void setDescription(CharSequence text) {
        binding.tvDescription.setText(text);
    }

    public void setName(CharSequence name) {
        binding.tvNickname.setText(name);
    }

    public void setImageFromUrl(String url) {
        ViewUtils.drawProfile(binding.ivProfile, url);
    }

    public void setVisibleOverlay(int visiblility) {
        binding.ivProfileOverlay.setVisibility(visiblility);
    }

    public void enableActionMenu(boolean enabled) {
        binding.ivAction.setEnabled(enabled);
    }

    public static void drawMember(@NonNull MemberPreview preview, @NonNull Member member) {
        Context context = preview.getContext();
        boolean isOperatorMember = member.getRole() == Member.Role.OPERATOR;
        boolean isMe = member.getUserId().equals(SendBird.getCurrentUser().getUserId());
        String nickname = TextUtils.isEmpty(member.getNickname()) ? context.getString(R.string.sb_text_channel_list_title_unknown) : member.getNickname();
        preview.setName(nickname);

        String description = isOperatorMember ? context.getString(R.string.sb_text_operator) : "";
        preview.setDescription(description);
        preview.setImageFromUrl(member.getProfileUrl());
        preview.enableActionMenu(!isMe);
        preview.setVisibleOverlay(member.isMuted() ? View.VISIBLE : View.GONE);

        if (isMe) {
            String meBadge = nickname + context.getResources().getString(R.string.sb_text_user_list_badge_me);
            final Spannable spannable = new SpannableString(meBadge);
            int badgeAppearance = SendBirdUIKit.isDarkMode() ? R.style.SendbirdSubtitle2OnDark02 : R.style.SendbirdSubtitle2OnLight02;
            int originLen = nickname.length();
            spannable.setSpan(new TextAppearanceSpan(context, badgeAppearance),
                    originLen, meBadge.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            preview.setName(spannable);
        }
    }

    public static void drawMemberFromUser(@NonNull MemberPreview preview, @NonNull User user) {
        Context context = preview.getContext();
        boolean isMe = user.getUserId().equals(SendBird.getCurrentUser().getUserId());
        String nickname = TextUtils.isEmpty(user.getNickname()) ? context.getString(R.string.sb_text_channel_list_title_unknown) : user.getNickname();
        preview.setName(nickname);

        preview.setDescription("");
        preview.setImageFromUrl(user.getProfileUrl());
        preview.enableActionMenu(!isMe);

        if (isMe) {
            String meBadge = nickname + context.getResources().getString(R.string.sb_text_user_list_badge_me);
            final Spannable spannable = new SpannableString(meBadge);
            int badgeAppearance = SendBirdUIKit.isDarkMode() ? R.style.SendbirdSubtitle2OnDark02 : R.style.SendbirdSubtitle2OnLight02;
            int originLen = nickname.length();
            spannable.setSpan(new TextAppearanceSpan(context, badgeAppearance),
                    originLen, meBadge.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            preview.setName(spannable);
        }
    }
}
