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
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.widget.CompoundButtonCompat;
import androidx.databinding.BindingAdapter;

import com.sendbird.android.SendBird;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.databinding.SbViewUserListItemBinding;
import com.sendbird.uikit.interfaces.UserInfo;
import com.sendbird.uikit.utils.ChannelUtils;

public class UserPreview extends FrameLayout {
    private SbViewUserListItemBinding binding;
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;
    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;

    public UserPreview(Context context) {
        this(context, null);
    }

    public UserPreview(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.sb_user_preview_style);
    }

    public UserPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.UserPreview, defStyle, 0);
        try {
            this.binding = SbViewUserListItemBinding.inflate(LayoutInflater.from(getContext()));
            addView(binding.getRoot(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            int nicknameAppearance = a.getResourceId(R.styleable.UserPreview_sb_user_preview_nickname_appearance, R.style.SendbirdSubtitle2OnLight01);
            boolean useSelection = a.getBoolean(R.styleable.UserPreview_sb_user_preview_use_selection, false);
            binding.cbUserPreview.setVisibility(useSelection ? View.VISIBLE : View.GONE);

            binding.tvNickname.setTextAppearance(context, nicknameAppearance);
            binding.tvNickname.setEllipsize(TextUtils.TruncateAt.END);
            binding.tvNickname.setMaxLines(1);

            int checkBoxTint = SendBirdUIKit.isDarkMode() ? R.color.sb_checkbox_tint_dark : R.color.sb_checkbox_tint_light;
            CompoundButtonCompat.setButtonTintList(binding.cbUserPreview, AppCompatResources.getColorStateList(context, checkBoxTint));

            binding.vgUserItem.setOnClickListener(v -> {
                binding.cbUserPreview.toggle();
                if (clickListener != null) {
                    clickListener.onClick(v);
                }

                if (onCheckedChangeListener != null) {
                    onCheckedChangeListener.onCheckedChanged(binding.cbUserPreview, !isSelected());
                }
            });

            binding.cbUserPreview.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onClick(v);
                }

                if (onCheckedChangeListener != null) {
                    onCheckedChangeListener.onCheckedChanged(binding.cbUserPreview, !isSelected());
                }
            });

            binding.vgUserItem.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onLongClick(v);
                }
                return false;
            });
        } finally {
            a.recycle();
        }
    }

    public View getLayout() {
        return this;
    }

    public SbViewUserListItemBinding getBinding() {
        return binding;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    public void setOnLongClickListener(View.OnLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setOnSelectedStateChangedListener(CompoundButton.OnCheckedChangeListener listener) {
        this.onCheckedChangeListener = listener;
    }

    public boolean isSelected() {
        return this.binding.cbUserPreview.isChecked();
    }

    public void setUserSelected(boolean isSelected) {
        this.binding.cbUserPreview.setChecked(isSelected);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        binding.vgUserItem.setEnabled(enabled);
        binding.cbUserPreview.setEnabled(enabled);
        binding.tvNickname.setEnabled(enabled);
    }

    public void drawUser(@NonNull UserInfo userInfo, boolean isSelected, boolean isEnabled) {
        String nickname = TextUtils.isEmpty(userInfo.getNickname()) ? getContext().getString(R.string.sb_text_channel_list_title_unknown) : userInfo.getNickname();
        binding.tvNickname.setText(nickname);
        ChannelUtils.loadImage(binding.ivUserCover, userInfo.getProfileUrl());

        if (userInfo.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
            String meBadge = getResources().getString(R.string.sb_text_user_list_badge_me);
            final Spannable spannable = new SpannableString(meBadge);
            int badgeAppearance = SendBirdUIKit.isDarkMode() ? R.style.SendbirdSubtitle2OnDark02 : R.style.SendbirdSubtitle2OnLight02;
            spannable.setSpan(new TextAppearanceSpan(getContext(), badgeAppearance),
                    0, meBadge.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.tvNickname.append(spannable);
        }

        setUserSelected(isSelected);
        setEnabled(isEnabled);
    }

    @BindingAdapter({"userInfo", "isSelected", "isEnabled"})
    public static void drawUser(UserPreview userPreview, UserInfo userInfo, boolean isSelected, boolean isEnabled) {
        userPreview.drawUser(userInfo, isSelected, isEnabled);
    }
}
