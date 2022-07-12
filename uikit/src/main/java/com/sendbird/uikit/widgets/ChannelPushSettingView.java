package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.uikit.R;
import com.sendbird.uikit.databinding.SbViewChannelPushSettingBinding;

public class ChannelPushSettingView extends FrameLayout {
    private final SbViewChannelPushSettingBinding binding;
    private OnClickListener pushAllClickListener;
    private OnClickListener mentionsOnlyClickListener;

    public ChannelPushSettingView(@NonNull Context context) {
        this(context, null);
    }

    public ChannelPushSettingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChannelPushSettingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ChannelPushSettings, defStyle, 0);
        try {
            this.binding = SbViewChannelPushSettingBinding.inflate(LayoutInflater.from(getContext()));
            addView(binding.getRoot(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            int background = a.getResourceId(R.styleable.ChannelPushSettings_sb_channel_push_settings_background, R.color.background_50);
            int optionBackground = a.getResourceId(R.styleable.ChannelPushSettings_sb_channel_push_settings_option_item_background, R.drawable.selector_rectangle_light);
            int titleAppearance = a.getResourceId(R.styleable.ChannelPushSettings_sb_channel_push_settings_title_appearance, R.style.SendbirdSubtitle2OnLight01);
            int descriptionAppearance = a.getResourceId(R.styleable.ChannelPushSettings_sb_channel_push_settings_description_appearance, R.style.SendbirdBody3OnLight02);
            int optionAppearance = a.getResourceId(R.styleable.ChannelPushSettings_sb_channel_push_option_item_appearance, R.style.SendbirdBody3OnLight01);
            int dividerColor = a.getResourceId(R.styleable.ChannelPushSettings_sb_channel_push_option_item_divider_color, R.color.onlight_04);
            int switchTrackTint = a.getResourceId(R.styleable.ChannelPushSettings_sb_channel_switch_track_tint, R.color.sb_switch_track_light);
            int switchThumbTint = a.getResourceId(R.styleable.ChannelPushSettings_sb_channel_switch_thumb_tint, R.color.sb_switch_thumb_light);
            int radioButtonBackground = a.getResourceId(R.styleable.ChannelPushSettings_sb_channel_radio_button_background, R.drawable.selector_radio_button_light);

            binding.rootView.setBackgroundResource(background);
            binding.tvTitle.setTextAppearance(context, titleAppearance);
            binding.tvDescription.setTextAppearance(context, descriptionAppearance);
            binding.tvOptionAll.setTextAppearance(context, optionAppearance);
            binding.all.setBackgroundResource(radioButtonBackground);
            binding.tvOptionMentionsOnly.setTextAppearance(context, optionAppearance);
            binding.mentionsOnly.setBackgroundResource(radioButtonBackground);
            binding.scSwitch.setTrackTintList(AppCompatResources.getColorStateList(context, switchTrackTint));
            binding.scSwitch.setThumbTintList(AppCompatResources.getColorStateList(context, switchThumbTint));
            binding.vgOptionAll.setBackgroundResource(optionBackground);
            binding.vgMentionsOnly.setBackgroundResource(optionBackground);
            binding.divider1.setBackgroundResource(dividerColor);
            binding.divider2.setBackgroundResource(dividerColor);
            binding.divider3.setBackgroundResource(dividerColor);

            binding.all.setOnClickListener(this::onSubmenuClicked);
            binding.vgOptionAll.setOnClickListener(this::onSubmenuClicked);
            binding.mentionsOnly.setOnClickListener(this::onSubmenuClicked);
            binding.vgMentionsOnly.setOnClickListener(this::onSubmenuClicked);
            binding.scSwitch.setOnClickListener(this::onSubmenuClicked);
        } finally {
            a.recycle();
        }
    }

    @NonNull
    public SbViewChannelPushSettingBinding getBinding() {
        return binding;
    }

    public void notifyChannelPushOptionChanged(@NonNull GroupChannel.PushTriggerOption option) {
        binding.vgOptionContainer.setVisibility(ViewGroup.VISIBLE);
        switch (option) {
            case OFF:
                binding.scSwitch.setChecked(false);
                binding.vgOptionContainer.setVisibility(ViewGroup.GONE);
                break;
            case ALL:
            case DEFAULT:
                binding.scSwitch.setChecked(true);
                binding.all.setChecked(true);
                binding.mentionsOnly.setChecked(false);
                break;
            case MENTION_ONLY:
                binding.scSwitch.setChecked(true);
                binding.all.setChecked(false);
                binding.mentionsOnly.setChecked(true);
                break;
        }
    }

    public void setTitle(@NonNull CharSequence title) {
        binding.tvTitle.setText(title);
    }

    public void setDescription(@NonNull CharSequence description) {
        binding.tvDescription.setText(description);
    }

    public void setOnPushOptionAllClickListener(@NonNull OnClickListener pushAllClickListener) {
        this.pushAllClickListener = pushAllClickListener;
    }

    public void setOnPushOptionMentionsOnlyClickListener(@NonNull OnClickListener mentionsOnlyClickListener) {
        this.mentionsOnlyClickListener = mentionsOnlyClickListener;
    }

    public void setOnSwitchButtonClickListener(@NonNull OnClickListener listener) {
        binding.scSwitch.setOnClickListener(listener);
    }

    private void onSubmenuClicked(@NonNull View view) {
        int id = view.getId();
        if (id == R.id.vgOptionAll || id == R.id.all) {
            if (pushAllClickListener != null) pushAllClickListener.onClick(view);
        } else if (id == R.id.vgMentionsOnly || id == R.id.mentionsOnly) {
            if (mentionsOnlyClickListener != null) mentionsOnlyClickListener.onClick(view);
        }
    }
}
