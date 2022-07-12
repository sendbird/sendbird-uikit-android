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

import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.uikit.R;
import com.sendbird.uikit.databinding.SbViewHeaderBinding;

public class HeaderView extends FrameLayout {
    private final SbViewHeaderBinding binding;

    public HeaderView(@NonNull Context context) {
        this(context, null);
    }

    public HeaderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeaderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.HeaderComponent,
                defStyleAttr, 0);

        try {
            this.binding = SbViewHeaderBinding.inflate(LayoutInflater.from(getContext()), this, true);

            int background = a.getResourceId(R.styleable.HeaderComponent_sb_appbar_background, R.color.background_50);
            CharSequence titleText = a.getString(R.styleable.HeaderComponent_sb_appbar_title);
            int titleTextAppearance = a.getResourceId(R.styleable.HeaderComponent_sb_appbar_title_appearance, R.style.SendbirdH2OnLight01);
            CharSequence descText = a.getString(R.styleable.HeaderComponent_sb_appbar_description);
            int descTextAppearance = a.getResourceId(R.styleable.HeaderComponent_sb_appbar_description_appearance, R.style.SendbirdCaption2OnLight02);
            int leftButtonIconResId = a.getResourceId(R.styleable.HeaderComponent_sb_appbar_left_button_icon, 0);
            ColorStateList leftButtonTint = a.getColorStateList(R.styleable.HeaderComponent_sb_appbar_left_button_tint);
            int leftButtonBackground = a.getResourceId(R.styleable.HeaderComponent_sb_appbar_left_button_background, R.drawable.sb_button_uncontained_background_light);

            int rightButtonIconResId = a.getResourceId(R.styleable.HeaderComponent_sb_appbar_right_button_icon, 0);
            ColorStateList rightButtonTint = a.getColorStateList(R.styleable.HeaderComponent_sb_appbar_right_button_tint);
            int rightButtonBackground = a.getResourceId(R.styleable.HeaderComponent_sb_appbar_right_button_background, R.drawable.sb_button_uncontained_background_light);

            int dividerColor = a.getResourceId(R.styleable.HeaderComponent_sb_appbar_divider_color, R.color.onlight_04);

            binding.getRoot().setBackgroundResource(background);
            binding.title.setText(titleText);
            binding.title.setTextAppearance(context, titleTextAppearance);
            binding.description.setTextAppearance(context, descTextAppearance);
            binding.description.setTextSize(Dimension.DP, getResources().getDimension(R.dimen.sb_size_12));

            binding.leftButton.setImageResource(leftButtonIconResId);
            binding.leftButton.setBackgroundResource(leftButtonBackground);
            binding.leftButton.setImageTintList(leftButtonTint);

            binding.rightButton.setImageResource(rightButtonIconResId);
            binding.rightButton.setBackgroundResource(rightButtonBackground);
            binding.rightButton.setImageTintList(rightButtonTint);

            binding.elevationView.setBackgroundResource(dividerColor);
            if (descText != null) {
                binding.description.setText(descText);
            } else {
                binding.description.setVisibility(GONE);
            }

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

    @NonNull
    public ImageButton getRightButton() {
        return binding.rightButton;
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


    public void setRightButtonImageResource(@DrawableRes int drawableRes) {
        binding.rightButton.setImageResource(drawableRes);
    }

    public void setRightButtonImageDrawable(@Nullable Drawable drawable) {
        binding.rightButton.setImageDrawable(drawable);
    }

    public void setRightButtonTint(@NonNull ColorStateList tint) {
        binding.rightButton.setImageTintList(tint);
    }

    public void setOnRightButtonClickListener(@Nullable OnClickListener listener) {
        binding.rightButton.setOnClickListener(listener);
    }

    public void setUseRightButton(boolean useRightButton) {
        binding.rightButton.setVisibility(useRightButton ? VISIBLE : GONE);
    }

    @NonNull
    public TextView getDescriptionTextView() {
        return binding.description;
    }

    @NonNull
    public ChannelCoverView getProfileView() {
        return binding.profileView;
    }
}
