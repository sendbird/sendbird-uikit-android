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

import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.databinding.SbViewToastBinding;

public class ToastView extends FrameLayout {
    private SbViewToastBinding binding;

    public ToastView(@NonNull Context context) {
        this(context, null);
    }

    public ToastView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToastView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ToastView, defStyleAttr, 0);
        try {
            this.binding = SbViewToastBinding.inflate(LayoutInflater.from(getContext()), this, true);
            int background = a.getResourceId(R.styleable.ToastView_sb_toast_background, R.drawable.sb_toast_background_light);
            int textAppearance = a.getResourceId(R.styleable.ToastView_sb_toast_text_appearance, R.style.SendbirdBody3OnDark01);

            int successTint = SendbirdUIKit.isDarkMode() ? R.color.secondary_500 : R.color.secondary_200;
            int errorTint = SendbirdUIKit.isDarkMode() ? R.color.error_300 : R.color.error_200;
            binding.toastPanel.setBackgroundResource(background);
            binding.toastPanel.getBackground().setAlpha(163);
            binding.tvToastText.setTextAppearance(context, textAppearance);
            binding.ivSuccess.setImageTintList(AppCompatResources.getColorStateList(context, successTint));
            binding.ivError.setImageTintList(AppCompatResources.getColorStateList(context, errorTint));
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
