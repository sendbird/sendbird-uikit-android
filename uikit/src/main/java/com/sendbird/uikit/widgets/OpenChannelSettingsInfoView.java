package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.OpenChannel;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.databinding.SbViewOpenChannelSettingsInfoBinding;
import com.sendbird.uikit.utils.ChannelUtils;
import com.sendbird.uikit.utils.TextUtils;

public class OpenChannelSettingsInfoView extends FrameLayout {

    private final SbViewOpenChannelSettingsInfoBinding binding;

    public OpenChannelSettingsInfoView(@NonNull Context context) {
        this(context, null);
    }

    public OpenChannelSettingsInfoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OpenChannelSettingsInfoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ChannelSettings, defStyleAttr, 0);
        try {
            this.binding = SbViewOpenChannelSettingsInfoBinding.inflate(LayoutInflater.from(getContext()), this, true);
            int background = a.getResourceId(R.styleable.ChannelSettings_sb_channel_settings_background, R.color.background_50);
            int nameAppearance = a.getResourceId(R.styleable.ChannelSettings_sb_channel_settings_name_appearance, R.style.SendbirdSubtitle1OnLight01);
            int infoTitleAppearance = a.getResourceId(R.styleable.ChannelSettings_sb_channel_settings_description_appearance, R.style.SendbirdBody2OnLight02);
            int infoContentAppearance = a.getResourceId(R.styleable.ChannelSettings_sb_channel_settings_description_appearance, R.style.SendbirdBody2OnLight02);
            int divider = SendbirdUIKit.isDarkMode() ? R.drawable.sb_line_divider_dark : R.drawable.sb_line_divider_light;

            this.setBackgroundResource(background);
            binding.tvChannelName.setTextAppearance(context, nameAppearance);
            binding.tvInformationTitle.setTextAppearance(context, infoTitleAppearance);
            binding.tvInformationContent.setTextAppearance(context, infoContentAppearance);
            binding.divider.setBackgroundResource(divider);
            binding.divider1.setBackgroundResource(divider);
        } finally {
            a.recycle();
        }
    }

    @NonNull
    public OpenChannelSettingsInfoView getLayout() {
        return this;
    }

    @NonNull
    public SbViewOpenChannelSettingsInfoBinding getBinding() {
        return binding;
    }

    public void drawOpenChannelSettingsInfoView(@Nullable OpenChannel channel) {
        if (channel == null) return;
        String channelName = channel.getName();
        binding.tvChannelName.setText(TextUtils.isEmpty(channelName) ? "" : channelName);
        ChannelUtils.loadChannelCover(binding.ccvChannelImage, channel);
        binding.tvInformationContent.setText(channel.getUrl());
    }
}
