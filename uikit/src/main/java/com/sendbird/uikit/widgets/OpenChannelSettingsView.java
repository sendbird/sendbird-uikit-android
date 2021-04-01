package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.DataBindingUtil;

import com.sendbird.android.OpenChannel;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.databinding.SbViewOpenChannelSettingsBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.utils.ChannelUtils;
import com.sendbird.uikit.utils.DrawableUtils;
import com.sendbird.uikit.utils.TextUtils;

public class OpenChannelSettingsView extends FrameLayout {
    /**
     * Represents all channel setting menu.
     *
     * @since 2.0.0
     */
    public enum OpenChannelSettingMenu {
        PARTICIPANTS, DELETE_CHANNEL
    }

    private SbViewOpenChannelSettingsBinding binding;
    private OnItemClickListener<OpenChannelSettingMenu> listener;

    public OpenChannelSettingsView(@NonNull Context context) {
        this(context, null);
    }

    public OpenChannelSettingsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_open_channel_settings_style);
    }

    public OpenChannelSettingsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ChannelSettings, defStyle, 0);
        try {
            this.binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.sb_view_open_channel_settings, this, true);
            int background = a.getResourceId(R.styleable.ChannelSettings_sb_channel_settings_background, R.color.background_50);
            int itemBackground = a.getResourceId(R.styleable.ChannelSettings_sb_channel_settings_item_background, R.drawable.selector_rectangle_light);
            int nameAppearance = a.getResourceId(R.styleable.ChannelSettings_sb_channel_settings_name_appearance, R.style.SendbirdSubtitle1OnLight01);
            int itemAppearance = a.getResourceId(R.styleable.ChannelSettings_sb_channel_settings_item_appearance, R.style.SendbirdSubtitle2OnLight01);;
            int descAppearance = a.getResourceId(R.styleable.ChannelSettings_sb_channel_settings_description_appearance, R.style.SendbirdBody2OnLight02);;
            int infoTitleAppearance = a.getResourceId(R.styleable.ChannelSettings_sb_channel_settings_description_appearance, R.style.SendbirdBody2OnLight02);;
            int infoContentAppearance = a.getResourceId(R.styleable.ChannelSettings_sb_channel_settings_description_appearance, R.style.SendbirdBody2OnLight02);;
            boolean useDarkTheme = SendBirdUIKit.isDarkMode();

            int nextTint = useDarkTheme ? R.color.ondark_01 : R.color.onlight_01;
            int divider = useDarkTheme ? R.drawable.sb_line_divider_dark : R.drawable.sb_line_divider_light;
            int iconTint = SendBirdUIKit.getDefaultThemeMode().getPrimaryTintResId();
            int deleteTint = useDarkTheme ? R.color.error_200 : R.color.error_300;

            this.setBackgroundResource(background);
            binding.ivParticipantsIcon.setImageDrawable(DrawableUtils.setTintList(binding.ivParticipantsIcon.getDrawable(),
                    AppCompatResources.getColorStateList(context, iconTint)));
            binding.ivNext.setImageDrawable(DrawableUtils.setTintList(binding.ivNext.getDrawable(),
                    AppCompatResources.getColorStateList(context, nextTint)));
            binding.ivChannelDeleteIcon.setImageDrawable(DrawableUtils.setTintList(binding.ivChannelDeleteIcon.getDrawable(),
                    AppCompatResources.getColorStateList(context, deleteTint)));

            binding.participantsItem.setBackgroundResource(itemBackground);
            binding.channelDeleteItem.setBackgroundResource(itemBackground);

            binding.tvParticipantsTitle.setTextAppearance(context, itemAppearance);
            binding.tvChannelDeleteTitle.setTextAppearance(context, itemAppearance);

            binding.tvChannelName.setTextAppearance(context, nameAppearance);
            binding.tvParticipantsCount.setTextAppearance(context, descAppearance);
            binding.tvInformationTitle.setTextAppearance(context, infoTitleAppearance);
            binding.tvInformationContent.setTextAppearance(context, infoContentAppearance);

            binding.divider0.setBackgroundResource(divider);
            binding.divider1.setBackgroundResource(divider);
            binding.divider2.setBackgroundResource(divider);
            binding.divider3.setBackgroundResource(divider);

            binding.participantsItem.setOnClickListener((v -> {
                if (listener != null) {
                    listener.onItemClick(v, 0, OpenChannelSettingMenu.PARTICIPANTS);
                }
            }));

            binding.channelDeleteItem.setOnClickListener((v -> {
                if (listener != null) {
                    listener.onItemClick(v, 0, OpenChannelSettingMenu.DELETE_CHANNEL);
                }
            }));

        } finally {
            a.recycle();
        }
    }

    public void setOnItemClickListener(OnItemClickListener<OpenChannelSettingMenu> listener) {
        this.listener = listener;
    }

    public OpenChannelSettingsView getLayout() {
        return this;
    }

    public SbViewOpenChannelSettingsBinding getBinding() {
        return binding;
    }

    public void drawSettingsView(@NonNull OpenChannel channel) {
        String channelName = channel.getName();
        binding.tvChannelName.setText(TextUtils.isEmpty(channelName) ? "" : channelName);
        binding.tvInformationContent.setText(channel.getUrl());
        ChannelUtils.loadChannelCover(binding.ccvChannelImage, channel);
        binding.tvParticipantsCount.setText(ChannelUtils.makeMemberCountText(channel.getParticipantCount()));
    }
}
