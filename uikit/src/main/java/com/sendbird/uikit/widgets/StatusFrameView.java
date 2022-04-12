package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.sendbird.uikit.R;
import com.sendbird.uikit.databinding.SbViewStatusFrameBinding;
import com.sendbird.uikit.utils.DrawableUtils;

public class StatusFrameView extends FrameLayout {

    private final SbViewStatusFrameBinding binding;
    private ColorStateList errorIconTint;
    private ColorStateList emptyIconTint;
    private ColorStateList actionIconTint;
    @Nullable
    private Drawable errorIcon;
    @Nullable
    private String errorText;
    @Nullable
    private Drawable emptyIcon;
    @Nullable
    private String emptyText;
    private boolean showAction = false;
    private int actionText;

    public StatusFrameView(@NonNull Context context) {
        this(context, null);
    }

    public StatusFrameView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusFrameView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.StatusComponent, defStyleAttr, 0);
        try {
            this.binding = SbViewStatusFrameBinding.inflate(LayoutInflater.from(getContext()), this, true);
            int background = a.getResourceId(R.styleable.StatusComponent_sb_status_frame_background, R.color.background_50);
            int alertTextAppearance = a.getResourceId(R.styleable.StatusComponent_sb_status_frame_text_appearance, R.style.SendbirdBody3OnLight03);
            actionIconTint = a.getColorStateList(R.styleable.StatusComponent_sb_status_frame_action_icon_tint);
            errorIconTint = a.getColorStateList(R.styleable.StatusComponent_sb_status_frame_error_icon_tint);
            emptyIconTint = a.getColorStateList(R.styleable.StatusComponent_sb_status_frame_empty_icon_tint);
            errorIcon = a.getDrawable(R.styleable.StatusComponent_sb_status_frame_error_icon);
            errorText = a.getString(R.styleable.StatusComponent_sb_status_frame_error_text);
            emptyIcon = a.getDrawable(R.styleable.StatusComponent_sb_status_frame_empty_icon);
            emptyText = a.getString(R.styleable.StatusComponent_sb_status_frame_empty_text);

            actionText = a.getResourceId(R.styleable.StatusComponent_sb_status_frame_action_text, R.string.sb_text_button_retry);
            int actionBackground = a.getResourceId(R.styleable.StatusComponent_sb_status_frame_action_background, R.drawable.selector_button_retry_light);
            int actionIcon = a.getResourceId(R.styleable.StatusComponent_sb_status_frame_action_icon, R.drawable.icon_refresh);
            int actionTextAppearance = a.getResourceId(R.styleable.StatusComponent_sb_status_frame_action_text_appearance, R.style.SendbirdButtonPrimary300);

            binding.ivAlertText.setTextAppearance(getContext(), alertTextAppearance);

            binding.actionPanel.setVisibility(showAction ? View.VISIBLE : View.GONE);
            binding.actionPanel.setBackgroundResource(actionBackground);
            binding.ivAction.setImageDrawable(DrawableUtils.setTintList(getContext(), actionIcon, actionIconTint));
            binding.tvAction.setText(actionText);
            binding.tvAction.setTextAppearance(context, actionTextAppearance);

            binding.frameParentPanel.setBackgroundResource(background);
            binding.progressPanel.setBackgroundResource(background);
            setStatus(Status.NONE);
        } finally {
            a.recycle();
        }
    }

    public enum Status {
        LOADING, CONNECTION_ERROR, ERROR, EMPTY, NONE
    }

    public void setErrorText(@Nullable String text) {
        this.errorText = text;
    }
    public void setErrorIcon(@Nullable Drawable errorIcon) {
        this.errorIcon = errorIcon;
    }
    public void setEmptyText(@Nullable String emptyText) {
        this.emptyText = emptyText;
    }

    public void setEmptyIcon(@Nullable Drawable emptyIcon) {
        this.emptyIcon = emptyIcon;
    }

    public void setErrorIconTint(@Nullable ColorStateList iconTint) {
        this.errorIconTint = iconTint;
    }

    public void setEmptyIconTint(@Nullable ColorStateList emptyIconTint) {
        this.emptyIconTint = emptyIconTint;
    }

    public void setActionIconTint(@Nullable ColorStateList actionIconTint) {
        this.actionIconTint = actionIconTint;
    }

    public void setShowAction(boolean showAction) {
        this.showAction = showAction;
    }

    public void setActionText(@StringRes int actionText) {
        this.actionText = actionText;
    }

    public void setStatus(@NonNull Status status) {
        this.setVisibility(VISIBLE);
        binding.progressPanel.setVisibility(GONE);

        switch (status) {
            case LOADING:
                binding.progressPanel.setVisibility(VISIBLE);
                break;
            case CONNECTION_ERROR:
                setActionText(R.string.sb_text_button_retry);
                setShowAction(true);
                setAlert(getContext().getString(R.string.sb_text_error_retry_request), errorIcon, errorIconTint);
                break;
            case ERROR:
                setShowAction(false);
                setAlert(errorText, errorIcon, errorIconTint);
                break;
            case EMPTY:
                setShowAction(false);
                setAlert(emptyText, emptyIcon, emptyIconTint);
                break;
            case NONE:
            default:
                this.setVisibility(GONE);
        }
    }

    public void setOnActionEventListener(@NonNull OnClickListener listener) {
        binding.actionPanel.setOnClickListener(listener);
    }

    private void setAlert(@Nullable String text, @Nullable Drawable icon, @Nullable ColorStateList iconTint) {
        this.setVisibility(VISIBLE);
        binding.ivAlertIcon.setImageDrawable(DrawableUtils.setTintList(icon, iconTint));
        binding.ivAlertText.setText(text);
        binding.tvAction.setText(actionText);
        binding.actionPanel.setVisibility(showAction ? View.VISIBLE : View.GONE);
        if (showAction) {
            final Drawable actionIcon = binding.ivAction.getDrawable();
            binding.ivAction.setImageDrawable(DrawableUtils.setTintList(actionIcon, actionIconTint));
        }
    }
}
