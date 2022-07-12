package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.sendbird.uikit.R;
import com.sendbird.uikit.databinding.SbViewStateHeaderBinding;

public class StateHeaderView extends FrameLayout {
    private final SbViewStateHeaderBinding binding;

    public StateHeaderView(@NonNull Context context) {
        this(context, null);
    }

    public StateHeaderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StateHeaderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.HeaderComponent,
                defStyleAttr, 0);

        try {
            this.binding = SbViewStateHeaderBinding.inflate(LayoutInflater.from(getContext()), this, true);

            int background = a.getResourceId(R.styleable.HeaderComponent_sb_appbar_background, R.color.background_50);
            CharSequence titleText = a.getString(R.styleable.HeaderComponent_sb_appbar_title);
            int titleTextAppearance = a.getResourceId(R.styleable.HeaderComponent_sb_appbar_title_appearance, R.style.SendbirdH2OnLight01);
            int leftButtonIconResId = a.getResourceId(R.styleable.HeaderComponent_sb_appbar_left_button_icon, 0);
            ColorStateList leftButtonTint = a.getColorStateList(R.styleable.HeaderComponent_sb_appbar_left_button_tint);
            int leftButtonBackground = a.getResourceId(R.styleable.HeaderComponent_sb_appbar_left_button_background, R.drawable.sb_button_uncontained_background_light);

            CharSequence rightButtonText = a.getString(R.styleable.HeaderComponent_sb_appbar_right_button_text);
            int rightButtonTextAppearance = a.getResourceId(R.styleable.HeaderComponent_sb_appbar_right_button_text_appearance, R.style.SendbirdButtonPrimary300);
            ColorStateList rightButtonTextColor = a.getColorStateList(R.styleable.HeaderComponent_sb_appbar_right_button_text_color);
            int rightButtonBackground = a.getResourceId(R.styleable.HeaderComponent_sb_appbar_right_button_background, R.drawable.sb_button_uncontained_background_light);
            int dividerColor = a.getResourceId(R.styleable.HeaderComponent_sb_appbar_divider_color, R.color.onlight_04);

            binding.root.setBackgroundResource(background);
            binding.title.setText(titleText);
            binding.title.setTextAppearance(context, titleTextAppearance);
            binding.leftButton.setImageResource(leftButtonIconResId);
            binding.leftButton.setBackgroundResource(leftButtonBackground);
            binding.leftButton.setImageTintList(leftButtonTint);

            binding.rightButton.setText(rightButtonText);
            binding.rightButton.setTextAppearance(context, rightButtonTextAppearance);
            if (rightButtonTextColor != null) {
                binding.rightButton.setTextColor(rightButtonTextColor);
            }
            binding.rightButton.setBackgroundResource(rightButtonBackground);

            binding.elevationView.setBackgroundResource(dividerColor);
        } finally {
            a.recycle();
        }
    }

    @NonNull
    public TextView getTitleTextView() {
        return binding.title;
    }

    @NonNull
    public ImageButton getLeftButton() {
        return binding.leftButton;
    }

    public void setLeftButtonImageResource(@DrawableRes int drawableRes) {
        binding.leftButton.setImageResource(drawableRes);
    }

    public void setLeftButtonImageDrawable(@Nullable Drawable drawable) {
        binding.leftButton.setImageDrawable(drawable);
    }

    public void setLeftButtonTint(@Nullable ColorStateList tint) {
        binding.leftButton.setImageTintList(tint);
    }

    public void setOnLeftButtonClickListener(@Nullable OnClickListener listener) {
        binding.leftButton.setOnClickListener(listener);
    }

    public void setUseLeftButton(boolean useLeftButton) {
        binding.leftButton.setVisibility(useLeftButton ? VISIBLE : GONE);
    }

    @NonNull
    public TextView getRightButton() {
        return binding.rightButton;
    }

    public void setRightButtonText(@StringRes int testResId) {
        binding.rightButton.setText(testResId);
    }

    public void setRightButtonText(@Nullable String text) {
        binding.rightButton.setText(text);
    }

    public void setOnRightButtonClickListener(@Nullable OnClickListener listener) {
        binding.rightButton.setOnClickListener(listener);
    }

    public void setEnabledRightButton(boolean enabled) {
        binding.rightButton.setEnabled(enabled);
    }

    public void setUseRightButton(boolean useRightButton) {
        binding.rightButton.setVisibility(useRightButton ? VISIBLE : GONE);
    }
}
