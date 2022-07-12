package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.sendbird.uikit.R;
import com.sendbird.uikit.consts.CreatableChannelType;
import com.sendbird.uikit.databinding.SbViewSelectChannelTypeBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.utils.DrawableUtils;

public class SelectChannelTypeView extends FrameLayout {
    private final SbViewSelectChannelTypeBinding binding;
    private OnItemClickListener<CreatableChannelType> listener;

    public SelectChannelTypeView(@NonNull Context context) {
        this(context, null);
    }

    public SelectChannelTypeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_widget_select_channel_type);
    }

    public SelectChannelTypeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SelectChannelTypeView,
                defStyleAttr, 0);

        try {
            binding = SbViewSelectChannelTypeBinding.inflate(LayoutInflater.from(getContext()), this, true);

            int background = a.getResourceId(R.styleable.SelectChannelTypeView_sb_select_channel_type_background, R.color.background_50);
            int titleTextAppearance = a.getResourceId(R.styleable.SelectChannelTypeView_sb_select_channel_type_menu_title_appearance, R.style.SendbirdH1OnLight01);
            int menuBackgroundRes = a.getResourceId(R.styleable.SelectChannelTypeView_sb_select_channel_type_menu_background, R.drawable.sb_button_uncontained_background_light);
            int nameTextAppearance = a.getResourceId(R.styleable.SelectChannelTypeView_sb_select_channel_type_menu_name_appearance, R.style.SendbirdCaption2OnLight02);
            ColorStateList iconTint = a.getColorStateList(R.styleable.SelectChannelTypeView_sb_select_channel_type_menu_icon_tint);

            binding.getRoot().setBackgroundResource(background);
            binding.tvTitle.setTextAppearance(context, titleTextAppearance);

            binding.vgGroup.setBackgroundResource(menuBackgroundRes);
            binding.vgBroadcast.setBackgroundResource(menuBackgroundRes);
            binding.vgSuperGroup.setBackgroundResource(menuBackgroundRes);

            binding.tvMenuGroupChat.setTextAppearance(context, nameTextAppearance);
            binding.tvMenuSuperGroupChat.setTextAppearance(context, nameTextAppearance);
            binding.tvMenuBroadcastChant.setTextAppearance(context, nameTextAppearance);

            if (iconTint != null) {
                binding.ivIconGroup.setImageDrawable(DrawableUtils.setTintList(context, R.drawable.icon_chat, iconTint));
                binding.ivIconSuperGroup.setImageDrawable(DrawableUtils.setTintList(context, R.drawable.icon_supergroup, iconTint));
                binding.ivIconBroadcast.setImageDrawable(DrawableUtils.setTintList(context, R.drawable.icon_broadcast, iconTint));
            } else {
                binding.ivIconGroup.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.icon_chat));
                binding.ivIconSuperGroup.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.icon_supergroup));
                binding.ivIconBroadcast.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.icon_broadcast));
            }

            binding.vgGroup.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(v, 0, CreatableChannelType.Normal);
                }
            });
            binding.vgSuperGroup.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(v, 1, CreatableChannelType.Super);
                }
            });
            binding.vgBroadcast.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(v, 2, CreatableChannelType.Broadcast);
                }
            });
        } finally {
            a.recycle();
        }
    }

    public void canCreateSuperGroupChannel(boolean canCreateSuperGroupChannel) {
        this.binding.vgSuperGroup.setVisibility(canCreateSuperGroupChannel ? View.VISIBLE : View.GONE);
    }

    public void canCreateBroadcastGroupChannel(boolean canCreateBroadcastGroupChannel) {
        this.binding.vgBroadcast.setVisibility(canCreateBroadcastGroupChannel ? View.VISIBLE : View.GONE);
    }

    public void setOnItemClickListener(@Nullable OnItemClickListener<CreatableChannelType> listener) {
        this.listener = listener;
    }
}
