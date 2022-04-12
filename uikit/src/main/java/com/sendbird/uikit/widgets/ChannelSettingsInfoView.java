package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.GroupChannel;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.databinding.SbViewChannelSettingsInfoBinding;
import com.sendbird.uikit.utils.ChannelUtils;

public class ChannelSettingsInfoView extends FrameLayout {

    private final SbViewChannelSettingsInfoBinding binding;

    public ChannelSettingsInfoView(@NonNull Context context) {
        this(context, null);
    }

    public ChannelSettingsInfoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChannelSettingsInfoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ChannelSettings, defStyle, 0);
        try {
            this.binding = SbViewChannelSettingsInfoBinding.inflate(LayoutInflater.from(getContext()),this, true);
            int background = a.getResourceId(R.styleable.ChannelSettings_sb_channel_settings_background, R.color.background_50);
            int nameAppearance = a.getResourceId(R.styleable.ChannelSettings_sb_channel_settings_name_appearance, R.style.SendbirdSubtitle1OnLight01);
            int divider = SendbirdUIKit.isDarkMode() ? R.drawable.sb_line_divider_dark : R.drawable.sb_line_divider_light;

            this.setBackgroundResource(background);
            binding.tvChannelName.setTextAppearance(context, nameAppearance);
            binding.divider.setBackgroundResource(divider);
        } finally {
            a.recycle();
        }
    }

    @NonNull
    public ChannelSettingsInfoView getLayout() {
        return this;
    }

    @NonNull
    public SbViewChannelSettingsInfoBinding getBinding() {
        return binding;
    }

    public void drawChannelSettingsInfoView(@Nullable GroupChannel channel) {
        if (channel == null) return;
        binding.tvChannelName.setText(ChannelUtils.makeTitleText(getContext(), channel));
        ChannelUtils.loadChannelCover(binding.ccvChannelImage, channel);
    }
}
