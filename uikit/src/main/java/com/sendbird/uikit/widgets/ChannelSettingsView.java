package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.DataBindingUtil;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.databinding.SbViewChannelSettingsBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.utils.Available;
import com.sendbird.uikit.utils.ChannelUtils;
import com.sendbird.uikit.utils.DrawableUtils;

public class ChannelSettingsView extends FrameLayout {
    /**
     * Represents all channel setting menu.
     *
     * @since 1.2.0
     */
    public enum ChannelSettingMenu {
        MODERATIONS, NOTIFICATIONS, MEMBERS, LEAVE_CHANNEL, SEARCH_IN_CHANNEL
    }

    private SbViewChannelSettingsBinding binding;
    private OnItemClickListener<ChannelSettingMenu> listener;

    public ChannelSettingsView(@NonNull Context context) {
        this(context, null);
    }

    public ChannelSettingsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_channel_settings_style);
    }

    public ChannelSettingsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ChannelSettings, defStyle, 0);
        try {
            this.binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.sb_view_channel_settings, this, true);
            int background = a.getResourceId(R.styleable.ChannelSettings_sb_channel_settings_background, R.color.background_50);
            int itemBackground = a.getResourceId(R.styleable.ChannelSettings_sb_channel_settings_item_background, R.drawable.selector_rectangle_light);
            int nameAppearance = a.getResourceId(R.styleable.ChannelSettings_sb_channel_settings_name_appearance, R.style.SendbirdSubtitle1OnLight01);
            int itemAppearance = a.getResourceId(R.styleable.ChannelSettings_sb_channel_settings_item_appearance, R.style.SendbirdSubtitle2OnLight01);;
            int descAppearance = a.getResourceId(R.styleable.ChannelSettings_sb_channel_settings_description_appearance, R.style.SendbirdBody2OnLight02);;
            boolean useDarkTheme = SendBirdUIKit.isDarkMode();

            int nextTint = useDarkTheme ? R.color.ondark_01 : R.color.onlight_01;
            int switchTrackTint = useDarkTheme ? R.color.sb_switch_track_dark : R.color.sb_switch_track_light;
            int switchThumbTint = useDarkTheme ? R.color.sb_switch_thumb_dark : R.color.sb_switch_thumb_light;
            int divider = useDarkTheme ? R.drawable.sb_line_divider_dark : R.drawable.sb_line_divider_light;
            ColorStateList iconTint = SendBirdUIKit.getDefaultThemeMode().getPrimaryTintColorStateList(context);
            ColorStateList iconLeaveTint = AppCompatResources.getColorStateList(context, useDarkTheme ? R.color.error_200 : R.color.error_300);

            this.setBackgroundResource(background);
            binding.ivNotiIcon.setImageDrawable(DrawableUtils.setTintList(binding.ivNotiIcon.getDrawable(), iconTint));
            binding.ivMembersIcon.setImageDrawable(DrawableUtils.setTintList(binding.ivMembersIcon.getDrawable(), iconTint));
            binding.ivModerationIcon.setImageDrawable(DrawableUtils.setTintList(binding.ivModerationIcon.getDrawable(), iconTint));
            binding.ivSearchIcon.setImageDrawable(DrawableUtils.setTintList(binding.ivSearchIcon.getDrawable(), iconTint));
            binding.ivLeaveIcon.setImageDrawable(DrawableUtils.setTintList(binding.ivLeaveIcon.getDrawable(), iconLeaveTint));

            binding.ivModerationNext.setImageDrawable(DrawableUtils.setTintList(binding.ivNext.getDrawable(),
                    AppCompatResources.getColorStateList(context, nextTint)));
            binding.ivNext.setImageDrawable(DrawableUtils.setTintList(binding.ivNext.getDrawable(),
                    AppCompatResources.getColorStateList(context, nextTint)));
            binding.scSwitch.setTrackTintList(AppCompatResources.getColorStateList(context, switchTrackTint));
            binding.scSwitch.setThumbTintList(AppCompatResources.getColorStateList(context, switchThumbTint));

            binding.moderationItem.setBackgroundResource(itemBackground);
            binding.notiItem.setBackgroundResource(itemBackground);
            binding.membersItem.setBackgroundResource(itemBackground);
            binding.leaveItem.setBackgroundResource(itemBackground);
            binding.searchItem.setBackgroundResource(itemBackground);

            binding.tvModerationName.setTextAppearance(context, itemAppearance);
            binding.tvNotiName.setTextAppearance(context, itemAppearance);
            binding.tvMembersName.setTextAppearance(context, itemAppearance);
            binding.tvLeaveName.setTextAppearance(context, itemAppearance);
            binding.tvSearcTitle.setTextAppearance(context, itemAppearance);

            binding.tvChannelName.setTextAppearance(context, nameAppearance);
            binding.tvMemberCount.setTextAppearance(context, descAppearance);

            binding.divider0.setBackgroundResource(divider);
            binding.divider1.setBackgroundResource(divider);
            binding.divider2.setBackgroundResource(divider);
            binding.divider3.setBackgroundResource(divider);
            binding.divider4.setBackgroundResource(divider);
            binding.divider5.setBackgroundResource(divider);

            binding.notiItem.setOnClickListener((v -> {
                if (listener != null) {
                    listener.onItemClick(v, 0, ChannelSettingMenu.NOTIFICATIONS);
                }
            }));

            binding.scSwitch.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(v, 0, ChannelSettingMenu.NOTIFICATIONS);
                }
            });

            binding.membersItem.setOnClickListener((v -> {
                if (listener != null) {
                    listener.onItemClick(v, 0, ChannelSettingMenu.MEMBERS);
                }
            }));

            binding.leaveItem.setOnClickListener((v -> {
                if (listener != null) {
                    listener.onItemClick(v, 0, ChannelSettingMenu.LEAVE_CHANNEL);
                }
            }));

            binding.moderationItem.setOnClickListener((v -> {
                if (listener != null) {
                    listener.onItemClick(v, 0, ChannelSettingMenu.MODERATIONS);
                }
            }));

            binding.searchItem.setOnClickListener((v -> {
                if (listener != null) {
                    listener.onItemClick(v, 0, ChannelSettingMenu.SEARCH_IN_CHANNEL);
                }
            }));
        } finally {
            a.recycle();
        }
    }

    public void setOnItemClickListener(OnItemClickListener<ChannelSettingMenu> listener) {
        this.listener = listener;
    }

    public ChannelSettingsView getLayout() {
        return this;
    }

    public SbViewChannelSettingsBinding getBinding() {
        return binding;
    }

    public void drawSettingsView(@NonNull GroupChannel channel) {
        binding.tvChannelName.setText(ChannelUtils.makeTitleText(getContext(), channel));
        ChannelUtils.loadChannelCover(binding.ccvChannelImage, channel);
        binding.tvMemberCount.setText(ChannelUtils.makeMemberCountText(channel.getMemberCount()));
        GroupChannel.PushTriggerOption pushTriggerOption = channel.getMyPushTriggerOption();
        binding.scSwitch.setChecked(pushTriggerOption != GroupChannel.PushTriggerOption.OFF);

        binding.moderationItem.setVisibility(channel.getMyRole() == Member.Role.OPERATOR ? View.VISIBLE : View.GONE);
        binding.searchItem.setVisibility(Available.isSupportMessageSearch() ? View.VISIBLE : View.GONE);
    }
}
