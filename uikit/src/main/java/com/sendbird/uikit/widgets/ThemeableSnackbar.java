package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.sendbird.uikit.R;
import com.sendbird.uikit.databinding.SbViewMentionLimitAlertBinding;
import com.sendbird.uikit.utils.DrawableUtils;

import java.util.Locale;

public class ThemeableSnackbar extends FrameLayout {
    @NonNull
    private final SbViewMentionLimitAlertBinding binding;
    private Snackbar snackbar;

    public ThemeableSnackbar(@NonNull Context context) {
        this(context, null);
    }

    public ThemeableSnackbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_widget_themeable_snackbar);
    }

    public ThemeableSnackbar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(null, R.styleable.ThemeableSnackbar, defStyleAttr, 0);
        try {
            this.binding = SbViewMentionLimitAlertBinding.inflate(LayoutInflater.from(context));
            addView(binding.getRoot(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            int background = a.getResourceId(R.styleable.ThemeableSnackbar_sb_snackbar_background, R.color.background_50);
            int rooflineColor = a.getResourceId(R.styleable.ThemeableSnackbar_sb_snackbar_roof_line_color, R.color.onlight_04);
            int textAppearance = a.getResourceId(R.styleable.ThemeableSnackbar_sb_snackbar_text_appearance, R.style.SendbirdBody3OnLight02);
            int icon = a.getResourceId(R.styleable.ThemeableSnackbar_sb_snackbar_icon, R.drawable.icon_info);
            ColorStateList iconTint = a.getColorStateList(R.styleable.ThemeableSnackbar_sb_snackbar_icon_tint);

            this.binding.ivIcon.setImageResource(icon);
            if (iconTint != null) {
                this.binding.ivIcon.setImageDrawable(DrawableUtils.setTintList(binding.ivIcon.getContext(), icon, iconTint));
            }

            this.binding.tvText.setTextAppearance(context, textAppearance);
            this.binding.ivRoofLine.setBackgroundResource(rooflineColor);
            this.binding.getRoot().setBackgroundResource(background);
        } finally {
            a.recycle();
        }
    }

    public void init(@NonNull View anchorView) {
        final Snackbar snackbar = Snackbar.make(anchorView, "", Snackbar.LENGTH_INDEFINITE);
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
        snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
        snackbar.setAnchorView(anchorView);

        final Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.removeAllViews();
        snackbarLayout.setPadding(0, 0, 0, 0);
        snackbarLayout.addView(this);
        this.snackbar = snackbar;
    }

    public void setMaxMentionCount(int maxMentionCount) {
        final String alertText = String.format(Locale.US, getContext().getString(R.string.sb_text_exceed_mention_limit_count), maxMentionCount);
        this.binding.tvText.setText(alertText);
    }

    public void show() {
        if (snackbar != null && !snackbar.isShown()) {
            snackbar.show();
        }
    }

    public void dismiss() {
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
    }

    @Override
    public boolean isShown() {
        if (snackbar != null) {
            return snackbar.isShown();
        }
        return false;
    }
}
