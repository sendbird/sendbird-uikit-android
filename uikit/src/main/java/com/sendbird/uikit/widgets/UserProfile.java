package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.databinding.SbViewUserProfileBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;

public class UserProfile extends FrameLayout {

    private SbViewUserProfileBinding binding;
    private OnItemClickListener<User> listener;

    public UserProfile(Context context) {
        this(context, null);
    }

    public UserProfile(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_user_profile_style);
    }

    public UserProfile(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.UserProfile, defStyle, 0);
        try {
            this.binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.sb_view_user_profile, this, true);
            int background = a.getResourceId(R.styleable.UserProfile_sb_user_profile_background, R.color.background_50);
            int userNameAppearance = a.getResourceId(R.styleable.UserProfile_sb_user_profile_user_name_text_appearance, R.style.SendbirdH1OnLight01);
            int singleMessageButtonBg = a.getResourceId(R.styleable.UserProfile_sb_user_profile_button_background, R.drawable.selector_button_default_light);
            int singleMessageTextAppearance = a.getResourceId(R.styleable.UserProfile_sb_user_profile_button_text_appearance, R.style.SendbirdButtonOnLight01);
            int dividerColor = a.getResourceId(R.styleable.UserProfile_sb_user_profile_divider_color, R.color.onlight_04);
            int infoTitleTextAppearance = a.getResourceId(R.styleable.UserProfile_sb_user_profile_information_title_text_appearance, R.style.SendbirdBody2OnLight02);
            int infoContentTextAppearance = a.getResourceId(R.styleable.UserProfile_sb_user_profile_information_text_appearance, R.style.SendbirdBody3OnLight01);

            this.binding.parent.setBackgroundResource(background);
            this.binding.tvName.setTextAppearance(context, userNameAppearance);
            this.binding.btCreateChannel.setBackgroundResource(singleMessageButtonBg);
            this.binding.btCreateChannel.setTextAppearance(context, singleMessageTextAppearance);
            this.binding.ivDivider.setBackgroundResource(dividerColor);
            this.binding.tvTitleUserId.setTextAppearance(context, infoTitleTextAppearance);
            this.binding.tvUserId.setTextAppearance(context, infoContentTextAppearance);
        } catch (Exception e) {
            a.recycle();
        }
    }

    public void setOnItemClickListener(OnItemClickListener<User> listener) {
        this.listener = listener;
    }

    public void drawUserProfile(@NonNull User user) {
        this.binding.profileView.loadImage(user.getProfileUrl());
        this.binding.tvName.setText(user.getNickname());
        this.binding.tvUserId.setText(user.getUserId());
        setUseChannelCreateButton(isMe(user.getUserId()));
        this.binding.btCreateChannel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(v, 0, user);
            }
        });
    }

    private boolean isMe(@NonNull String userId) {
        User currentUser = SendBird.getCurrentUser();
        if (currentUser != null) {
            return userId.equals(currentUser.getUserId());
        }
        return false;
    }

    public void setUseChannelCreateButton(boolean channelCreatable) {
        this.binding.btCreateChannel.setVisibility(!channelCreatable ? View.GONE : View.VISIBLE);
    }
}
