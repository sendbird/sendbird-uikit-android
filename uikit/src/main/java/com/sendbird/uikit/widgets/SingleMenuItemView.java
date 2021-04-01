package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.databinding.SbViewSingleMenuItemBinding;
import com.sendbird.uikit.utils.DrawableUtils;

public class SingleMenuItemView extends FrameLayout {
    private SbViewSingleMenuItemBinding binding;

    public SingleMenuItemView(Context context) {
        this(context, null);
    }

    public SingleMenuItemView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.sb_single_menu_item_style);
    }

    public SingleMenuItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SingleMenuItemView, defStyle, 0);
        try {
            this.binding = SbViewSingleMenuItemBinding.inflate(LayoutInflater.from(getContext()));
            addView(binding.getRoot(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            String name = a.getString(R.styleable.SingleMenuItemView_sb_menu_item_name);
            int itemBackground = a.getResourceId(R.styleable.SingleMenuItemView_sb_menu_item_background, R.drawable.selector_rectangle_light);
            int nicknameAppearance = a.getResourceId(R.styleable.SingleMenuItemView_sb_menu_item_name_appearance, R.style.SendbirdSubtitle2OnLight01);
            int iconResId = a.getResourceId(R.styleable.SingleMenuItemView_sb_menu_item_icon, 0);
            int iconTintResId = a.getResourceId(R.styleable.SingleMenuItemView_sb_menu_item_icon_tint, R.color.primary_300);
            int type = a.getInteger(R.styleable.SingleMenuItemView_sb_menu_item_type, 0);
            binding.tvName.setTextAppearance(context, nicknameAppearance);
            binding.tvName.setEllipsize(TextUtils.TruncateAt.END);
            binding.tvName.setMaxLines(1);

            binding.vgMenuItem.setBackgroundResource(itemBackground);

            boolean useDarkTheme = SendBirdUIKit.isDarkMode();
            int nextTint = useDarkTheme ? R.color.ondark_01 : R.color.onlight_01;
            int divider = useDarkTheme ? R.drawable.sb_line_divider_dark : R.drawable.sb_line_divider_light;
            int switchTrackTint = useDarkTheme ? R.color.sb_switch_track_dark : R.color.sb_switch_track_light;
            int switchThumbTint = useDarkTheme ? R.color.sb_switch_thumb_dark : R.color.sb_switch_thumb_light;
            binding.divider.setBackgroundResource(divider);

            if (!TextUtils.isEmpty(name)) {
                setName(name);
            }

            if (iconResId > 0) {
                setIcon(iconResId);
            }

            setIconTint(iconTintResId);
            switch (type) {
                case 0: // next
                    int nextResId = a.getResourceId(R.styleable.SingleMenuItemView_sb_menu_item_action_drawable, R.drawable.icon_chevron_right);
                    binding.ivNext.setImageResource(nextResId);
                    binding.scSwitch.setVisibility(View.GONE);
                    binding.ivNext.setVisibility(View.VISIBLE);
                    binding.ivNext.setImageDrawable(DrawableUtils.setTintList(binding.ivNext.getDrawable(),
                            AppCompatResources.getColorStateList(context, nextTint)));
                    break;
                case 1: // switcher
                    binding.scSwitch.setVisibility(View.VISIBLE);
                    binding.ivNext.setVisibility(View.GONE);

                    binding.scSwitch.setTrackTintList(AppCompatResources.getColorStateList(context, switchTrackTint));
                    binding.scSwitch.setThumbTintList(AppCompatResources.getColorStateList(context, switchThumbTint));
                    break;
            }

//            int moreTint = SendBirdUIKit.isDarkMode() ? R.color.ondark_01 : R.color.onlight_01;
//            binding.ivAction.setImageDrawable(DrawableUtils.setTintList(binding.ivAction.getDrawable(),
//                    AppCompatResources.getColorStateList(context, moreTint)));
        } finally {
            a.recycle();
        }
    }

    public View getLayout() {
        return this;
    }

    public SbViewSingleMenuItemBinding getBinding() {
        return binding;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener listener) {
        binding.vgMenuItem.setOnClickListener(listener);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener listener) {
        binding.vgMenuItem.setOnLongClickListener(listener);
    }

    public void useActionMenu(boolean use) {
        binding.vgAction.setVisibility(use ? View.VISIBLE : View.GONE);
    }

    public void setOnActionMenuClickListener(OnClickListener listener) {
        binding.scSwitch.setOnClickListener(listener);
        binding.ivNext.setOnClickListener(listener);
    }

    public void setIcon(@DrawableRes int resId) {
        binding.ivIcon.setImageResource(resId);
    }

    public void setIconTint(@ColorRes int tintResId) {
        binding.ivIcon.setImageDrawable(DrawableUtils.setTintList(binding.ivIcon.getDrawable(), AppCompatResources.getColorStateList(binding.ivIcon.getContext(), tintResId)));
    }

    public void setName(@NonNull String name) {
        binding.tvName.setText(name);
    }

    public void useDivider(boolean useDivider) {
        binding.divider.setVisibility(useDivider ? View.VISIBLE : View.GONE);
    }

    public void setChecked(boolean checked) {
        binding.scSwitch.setChecked(checked);
    }
}
