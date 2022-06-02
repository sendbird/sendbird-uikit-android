package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
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
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.databinding.SbViewSingleMenuItemBinding;
import com.sendbird.uikit.utils.DrawableUtils;

public class SingleMenuItemView extends FrameLayout {
    private final SbViewSingleMenuItemBinding binding;

    public enum Type {
        /**
         * A type that has an action button to redirect next page.
         */
        NEXT(0),

        /**
         * A type that has a switch button to toggle some action.
         */
        SWITCH(1),

        /**
         * A type that has no next action.
         */
        NONE(2);

        int value;
        Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        @NonNull
        public static Type from(int value) {
            for (Type type : values()) {
                if (type.value == value) {
                    return type;
                }
            }
            return NONE;
        }
    }

    public SingleMenuItemView(@NonNull Context context) {
        this(context, null);
    }

    public SingleMenuItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SingleMenuItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SingleMenuItemView, defStyle, 0);
        try {
            this.binding = SbViewSingleMenuItemBinding.inflate(LayoutInflater.from(getContext()));
            addView(binding.getRoot(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            int itemBackground = a.getResourceId(R.styleable.SingleMenuItemView_sb_menu_item_background, R.drawable.selector_rectangle_light);
            int nicknameAppearance = a.getResourceId(R.styleable.SingleMenuItemView_sb_menu_item_name_appearance, R.style.SendbirdSubtitle2OnLight01);
            int descriptionAppearance = a.getResourceId(R.styleable.SingleMenuItemView_sb_menu_item_description_appearance, R.style.SendbirdSubtitle2OnLight02);
            ColorStateList iconTintRes = a.getColorStateList(R.styleable.SingleMenuItemView_sb_menu_item_icon_tint);
            int type = a.getInteger(R.styleable.SingleMenuItemView_sb_menu_item_type, 0);
            binding.tvName.setTextAppearance(context, nicknameAppearance);
            binding.tvName.setEllipsize(TextUtils.TruncateAt.END);
            binding.tvName.setMaxLines(1);

            binding.tvDescription.setTextAppearance(context, descriptionAppearance);

            binding.vgMenuItem.setBackgroundResource(itemBackground);

            boolean useDarkTheme = SendbirdUIKit.isDarkMode();
            int nextTint = useDarkTheme ? R.color.ondark_01 : R.color.onlight_01;
            int divider = useDarkTheme ? R.drawable.sb_line_divider_dark : R.drawable.sb_line_divider_light;
            int switchTrackTint = useDarkTheme ? R.color.sb_switch_track_dark : R.color.sb_switch_track_light;
            int switchThumbTint = useDarkTheme ? R.color.sb_switch_thumb_dark : R.color.sb_switch_thumb_light;
            binding.divider.setBackgroundResource(divider);

            if (iconTintRes != null) {
                setIconTint(iconTintRes);
            }

            int nextResId = a.getResourceId(R.styleable.SingleMenuItemView_sb_menu_item_action_drawable, R.drawable.icon_chevron_right);
            binding.ivNext.setImageResource(nextResId);
            binding.ivNext.setImageDrawable(DrawableUtils.setTintList(binding.ivNext.getDrawable(),
                    AppCompatResources.getColorStateList(context, nextTint)));

            binding.scSwitch.setTrackTintList(AppCompatResources.getColorStateList(context, switchTrackTint));
            binding.scSwitch.setThumbTintList(AppCompatResources.getColorStateList(context, switchThumbTint));

            setMenuType(Type.from(type));
        } finally {
            a.recycle();
        }
    }

    @NonNull
    public View getLayout() {
        return this;
    }

    @NonNull
    public SbViewSingleMenuItemBinding getBinding() {
        return binding;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener listener) {
        binding.vgMenuItem.setOnClickListener(listener);
    }

    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener listener) {
        binding.vgMenuItem.setOnLongClickListener(listener);
    }

    public void useActionMenu(boolean use) {
        binding.vgAction.setVisibility(use ? View.VISIBLE : View.GONE);
    }

    public void setNextActionDrawable(@DrawableRes int drawableResId) {
        binding.ivNext.setImageResource(drawableResId);
    }

    public void setOnActionMenuClickListener(@NonNull OnClickListener listener) {
        binding.scSwitch.setOnClickListener(listener);
        binding.ivNext.setOnClickListener(listener);
    }

    public void setIcon(@DrawableRes int resId) {
        binding.ivIcon.setImageResource(resId);
    }

    public void setIconTint(@ColorRes int tintResId) {
        binding.ivIcon.setImageTintList(AppCompatResources.getColorStateList(binding.ivIcon.getContext(), tintResId));
    }

    public void setIconTint(@NonNull ColorStateList tint) {
        binding.ivIcon.setImageTintList(tint);
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

    public void setMenuType(@NonNull Type type) {
        if (type == Type.NEXT) {
            binding.scSwitch.setVisibility(View.GONE);
            binding.ivNext.setVisibility(View.VISIBLE);
            binding.tvDescription.setVisibility(View.VISIBLE);
        } else if (type == Type.SWITCH) {
            binding.scSwitch.setVisibility(View.VISIBLE);
            binding.ivNext.setVisibility(View.GONE);
            binding.tvDescription.setVisibility(View.GONE);
        } else {
            binding.scSwitch.setVisibility(View.GONE);
            binding.ivNext.setVisibility(View.GONE);
            binding.tvDescription.setVisibility(View.GONE);
        }
    }

    public void setDescription(@NonNull String description) {
        binding.tvDescription.setText(description);
    }
}
