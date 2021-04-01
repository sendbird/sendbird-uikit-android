package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.widget.ImageViewCompat;
import androidx.databinding.DataBindingUtil;

import com.sendbird.uikit.R;
import com.sendbird.uikit.databinding.SbViewAppBarBinding;

public class AppBarView extends FrameLayout {
    private final SbViewAppBarBinding binding;
    private boolean useLeftImageButton = true;
    private boolean useRightButton = true;

    public AppBarView(Context context) {
        this(context, null);
    }

    public AppBarView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.sb_appbar_style);
    }

    public AppBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.AppBarView,
                defStyleAttr, 0);

        try {
            this.binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.sb_view_app_bar, this, true);

            CharSequence titleText = a.getString(R.styleable.AppBarView_sb_appbar_title);
            int titleTextAppearance = a.getResourceId(R.styleable.AppBarView_sb_appbar_title_appearance,
                    R.style.SendbirdH2OnLight01);
            CharSequence descText = a.getString(R.styleable.AppBarView_sb_appbar_description);
            int descTextAppearance = a.getResourceId(R.styleable.AppBarView_sb_appbar_description_appearance,
                    R.style.SendbirdCaption2OnLight02);
            CharSequence leftButtonText = a.getString(R.styleable.AppBarView_sb_appbar_left_button_text);
            int leftButtonIconResId = a.getResourceId(R.styleable.AppBarView_sb_appbar_left_button_icon, 0);
            CharSequence rightButtonText = a.getString(R.styleable.AppBarView_sb_appbar_right_button_text);
            int rightButtonIconResId = a.getResourceId(R.styleable.AppBarView_sb_appbar_right_button_icon, 0);
            int dividerColorId = a.getResourceId(R.styleable.AppBarView_sb_appbar_divider_color, R.color.onlight_04);

            int leftButtonTextAppearance = a.getResourceId(R.styleable.AppBarView_sb_appbar_left_button_text_appearance, R.style.SendbirdButtonPrimary300);
            int leftButtonTextColor = a.getResourceId(R.styleable.AppBarView_sb_appbar_left_button_text_color, R.color.sb_button_uncontained_text_color_light);
            int leftButtonTint = a.getResourceId(R.styleable.AppBarView_sb_appbar_left_button_tint, R.color.primary_300);
            int leftButtonBackground = a.getResourceId(R.styleable.AppBarView_sb_appbar_left_button_background, R.drawable.sb_button_uncontained_background_light);
            int rightButtonTextAppearance = a.getResourceId(R.styleable.AppBarView_sb_appbar_right_button_text_appearance, R.style.SendbirdButtonPrimary300);
            int rightButtonTextColor = a.getResourceId(R.styleable.AppBarView_sb_appbar_right_button_text_color, R.color.sb_button_uncontained_text_color_light);
            int rightButtonTint = a.getResourceId(R.styleable.AppBarView_sb_appbar_right_button_tint, R.color.primary_300);
            int rightButtonBackground = a.getResourceId(R.styleable.AppBarView_sb_appbar_right_button_background, R.drawable.sb_button_uncontained_background_light);

            binding.tvAppBarTitle.setText(titleText);
            binding.tvAppBarTitle.setTextAppearance(context, titleTextAppearance);
            binding.tvAppBarDesc.setTextAppearance(context, descTextAppearance);

            if (!TextUtils.isEmpty(descText)) {
                binding.tvAppBarDesc.setText(descText);
            } else {
                binding.tvAppBarDesc.setVisibility(GONE);
            }

            if (!TextUtils.isEmpty(leftButtonText)) {
                binding.ibtnLeft.setVisibility(GONE);
                binding.btnLeft.setText(leftButtonText);
            } else if (leftButtonIconResId != 0) {
                binding.btnLeft.setVisibility(GONE);
                binding.ibtnLeft.setImageResource(leftButtonIconResId);
            }

            if (!TextUtils.isEmpty(rightButtonText)) {
                binding.ibtnRight.setVisibility(GONE);
                binding.btnRight.setText(rightButtonText);
            } else if (rightButtonIconResId != 0) {
                binding.btnRight.setVisibility(GONE);
                binding.ibtnRight.setImageResource(rightButtonIconResId);
            }

            binding.elevationView.setBackgroundResource(dividerColorId);

            binding.btnLeft.setTextAppearance(context, leftButtonTextAppearance);
            binding.btnLeft.setTextColor(AppCompatResources.getColorStateList(context, leftButtonTextColor));
            binding.btnLeft.setBackgroundResource(leftButtonBackground);
            binding.btnRight.setTextAppearance(context, rightButtonTextAppearance);
            binding.btnRight.setTextColor(AppCompatResources.getColorStateList(context, rightButtonTextColor));
            binding.btnRight.setBackgroundResource(rightButtonBackground);
            binding.ibtnLeft.setBackgroundResource(leftButtonBackground);
            ImageViewCompat.setImageTintList(binding.ibtnLeft, AppCompatResources.getColorStateList(context, leftButtonTint));
            binding.ibtnRight.setBackgroundResource(rightButtonBackground);
            ImageViewCompat.setImageTintList(binding.ibtnRight, AppCompatResources.getColorStateList(context, rightButtonTint));
        } finally {
            a.recycle();
        }
    }

    public TextView getTitleTextView() {
        return binding.tvAppBarTitle;
    }

    public TextView getDescriptionTextView() {
        return binding.tvAppBarDesc;
    }

    public TextView getLeftTextButton() {
        return binding.btnLeft;
    }

    public ImageButton getLeftImageButton() {
        return binding.ibtnLeft;
    }

    public void setLeftImageButtonResource(@DrawableRes int drawableRes) {
        if (useLeftImageButton && binding != null) {
            binding.btnLeft.setVisibility(GONE);
            binding.ibtnLeft.setVisibility(VISIBLE);
            binding.ibtnLeft.setImageResource(drawableRes);
        }
    }

    public void setLeftImageButtonTint(ColorStateList tint) {
        if (useLeftImageButton && binding != null) {
            ImageViewCompat.setImageTintList(binding.ibtnLeft, tint);
        }
    }

    public void setLeftImageButtonClickListener(OnClickListener listener) {
        if (binding != null) {
            binding.ibtnLeft.setOnClickListener(listener);
        }
    }

    public void setUseLeftImageButton(boolean useLeftImageButton) {
        this.useLeftImageButton = useLeftImageButton;
        if (binding != null) {
            binding.flLeftPanel.setVisibility(useLeftImageButton ? VISIBLE : GONE);
            binding.emptyLeft.setVisibility(useLeftImageButton ? GONE : VISIBLE);
        }
    }

    public TextView getRightTextButton() {
        return binding.btnRight;
    }

    public void setRightTextButtonString(String text) {
        if (useRightButton && binding != null) {
            binding.ibtnRight.setVisibility(GONE);
            binding.btnRight.setVisibility(VISIBLE);
            binding.btnRight.setText(text);
        }
    }

    public void setRightTextButtonClickListener(OnClickListener listener) {
        if (binding != null) {
            binding.btnRight.setOnClickListener(listener);
        }
    }

    public void setRightTextButtonEnabled(boolean enabled) {
        if (binding != null) {
            binding.btnRight.setEnabled(enabled);
        }
    }

    public ImageButton getRightImageButton() {
        return binding.ibtnRight;
    }

    public void setRightImageButtonResource(@DrawableRes int drawableRes) {
        if (useRightButton && binding != null) {
            binding.btnRight.setVisibility(GONE);
            binding.ibtnRight.setVisibility(VISIBLE);
            binding.ibtnRight.setImageResource(drawableRes);
        }
    }

    public void setRightImageButtonTint(ColorStateList tint) {
        if (useRightButton && binding != null) {
            ImageViewCompat.setImageTintList(binding.ibtnRight, tint);
        }
    }

    public void setRightImageButtonClickListener(OnClickListener listener) {
        if (binding != null) {
            binding.ibtnRight.setOnClickListener(listener);
        }
    }

    public void setUseRightButton(boolean useRightButton) {
        this.useRightButton = useRightButton;
        if (binding != null) {
            binding.ibtnRight.setVisibility(useRightButton ? VISIBLE : GONE);
            binding.btnRight.setVisibility(useRightButton ? VISIBLE : GONE);
        }
    }

    public ChannelCoverView getProfileView() {
        return binding.ccvProfileView;
    }
}
