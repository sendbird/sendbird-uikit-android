package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.databinding.DataBindingUtil;

import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.databinding.SbViewStatusFrameBinding;
import com.sendbird.uikit.utils.DrawableUtils;

public class StatusFrameView extends FrameLayout {

    private SbViewStatusFrameBinding binding;
    private ColorStateList iconTint;
    private int errorIcon;
    private int errorText;
    private int emptyIcon;
    private int emptyText;
    private boolean showAction;
    private int actionText;

    public StatusFrameView(@NonNull Context context) {
        this(context, null);
    }

    public StatusFrameView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_status_frame_style);
    }

    public StatusFrameView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.StatusFrameView, defStyle, 0);
        try {
            this.binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.sb_view_status_frame, this, true);
            int background = a.getResourceId(R.styleable.StatusFrameView_sb_status_frame_background, R.color.primary_100);
            int alertTextAppearance = a.getResourceId(R.styleable.StatusFrameView_sb_status_frame_text_appearance, R.style.SendbirdBody3OnLight03);
            iconTint = a.getColorStateList(R.styleable.StatusFrameView_sb_status_frame_icon_tint);
            errorIcon = a.getResourceId(R.styleable.StatusFrameView_sb_status_frame_error_icon, R.drawable.icon_error);
            errorText = a.getResourceId(R.styleable.StatusFrameView_sb_status_frame_error_text, R.string.sb_text_error_unknown);
            emptyIcon = a.getResourceId(R.styleable.StatusFrameView_sb_status_frame_empty_icon, R.drawable.icon_chat);
            emptyText = a.getResourceId(R.styleable.StatusFrameView_sb_status_frame_empty_text, R.string.sb_text_error_unknown);

            actionText = a.getResourceId(R.styleable.StatusFrameView_sb_status_frame_action_text, R.string.sb_text_button_retry);
            int actionBackground = a.getResourceId(R.styleable.StatusFrameView_sb_status_frame_action_background, R.drawable.selector_button_retry_light);
            int actionIcon = a.getResourceId(R.styleable.StatusFrameView_sb_status_frame_action_icon, R.drawable.icon_refresh);
            int actionTextAppearance = a.getResourceId(R.styleable.StatusFrameView_sb_status_frame_action_text_appearance, R.style.SendbirdButtonPrimary300);
            int actionIconTint = SendBirdUIKit.getDefaultThemeMode().getPrimaryTintResId();
            showAction = a.getBoolean(R.styleable.StatusFrameView_sb_status_frame_show_action, false);

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

    public void setErrorText(@StringRes int text) {
        this.errorText = text;
    }

    public void setErrorIcon(@DrawableRes int errorIcon) {
        this.errorIcon = errorIcon;
    }

    public void setEmptyText(@StringRes int emptyText) {
        this.emptyText = emptyText;
    }

    public void setEmptyIcon(@DrawableRes int emptyIcon) {
        this.emptyIcon = emptyIcon;
    }

    public void setIconTint(@Nullable ColorStateList iconTint) {
        this.iconTint = iconTint;
    }

    public void setShowAction(boolean showAction) {
        this.showAction = showAction;
    }

    public void setActionText(@StringRes int actionText) {
        this.actionText = actionText;
    }

    public void setStatus(Status status) {
        this.setVisibility(VISIBLE);
        binding.progressPanel.setVisibility(GONE);

        switch (status) {
            case LOADING:
                binding.progressPanel.setVisibility(VISIBLE);
                break;
            case CONNECTION_ERROR:
                setActionText(R.string.sb_text_button_retry);
                setErrorText(R.string.sb_text_error_retry_request);
                setShowAction(true);
                setAlert(errorText, errorIcon);
                break;
            case ERROR:
                setShowAction(false);
                setAlert(errorText, errorIcon);
                break;
            case EMPTY:
                setShowAction(false);
                setAlert(emptyText, emptyIcon);
                break;
            case NONE:
            default:
                this.setVisibility(GONE);
        }
    }

    public void setOnActionEventListener(@NonNull OnClickListener listener) {
        binding.actionPanel.setOnClickListener(listener);
    }

    private void setAlert(@StringRes int text, @DrawableRes int iconResId) {
        this.setVisibility(VISIBLE);
        binding.ivAlertIcon.setImageDrawable(DrawableUtils.setTintList(getContext(), iconResId, iconTint));
        binding.ivAlertText.setText(text);
        binding.tvAction.setText(actionText);
        binding.actionPanel.setVisibility(showAction ? View.VISIBLE : View.GONE);
    }
}
