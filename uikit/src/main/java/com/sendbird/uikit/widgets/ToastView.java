package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.widget.ImageViewCompat;
import androidx.databinding.DataBindingUtil;

import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.databinding.SbViewToastBinding;

public class ToastView extends FrameLayout {
    private SbViewToastBinding binding;

    public ToastView(@NonNull Context context) {
        this(context, null);
    }

    public ToastView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_toast_view_style);
    }

    public ToastView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ToastView, defStyleAttr, 0);
        try {
            this.binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.sb_view_toast, this, true);
            int background = a.getResourceId(R.styleable.ToastView_sb_toast_background, R.drawable.sb_toast_background_light);
            int textAppearance = a.getResourceId(R.styleable.ToastView_sb_toast_text_appearance, R.style.SendbirdBody3OnDark01);

            int successTint = SendBirdUIKit.isDarkMode() ? R.color.secondary_500 : R.color.secondary_200;
            int errorTint = SendBirdUIKit.isDarkMode() ? R.color.error_300 : R.color.error_200;
            binding.toastPanel.setBackgroundResource(background);
            binding.toastPanel.getBackground().setAlpha(163);
            binding.tvToastText.setTextAppearance(context, textAppearance);
            ImageViewCompat.setImageTintList(binding.ivSuccess, AppCompatResources.getColorStateList(context, successTint));
            ImageViewCompat.setImageTintList(binding.ivError, AppCompatResources.getColorStateList(context, errorTint));
        } finally {
            a.recycle();
        }
    }

    public void setText(@StringRes int text) {
        binding.tvToastText.setText(text);
    }

    public void setText(CharSequence text) {
        binding.tvToastText.setText(text);
    }

    public enum ToastStatus {
        SUCCESS, ERROR
    }

    public void setStatus(ToastStatus status) {
        if (status == ToastStatus.SUCCESS) {
            binding.ivError.setVisibility(GONE);
            binding.ivSuccess.setVisibility(VISIBLE);
        } else if (status == ToastStatus.ERROR) {
            binding.ivError.setVisibility(VISIBLE);
            binding.ivSuccess.setVisibility(GONE);
        }
    }
}
