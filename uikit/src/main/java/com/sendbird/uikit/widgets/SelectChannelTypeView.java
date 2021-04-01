package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.databinding.DataBindingUtil;

import com.sendbird.uikit.R;
import com.sendbird.uikit.consts.CreateableChannelType;
import com.sendbird.uikit.databinding.SbViewSelectChannelTypeBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.utils.DrawableUtils;

public class SelectChannelTypeView extends FrameLayout {
    private final SbViewSelectChannelTypeBinding binding;
    private OnItemClickListener<CreateableChannelType> listener;

    public SelectChannelTypeView(Context context) {
        this(context, null);
    }

    public SelectChannelTypeView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.sb_select_channel_type_style);
    }

    public SelectChannelTypeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SelectChannelTypeView,
                defStyleAttr, 0);

        try {
            binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.sb_view_select_channel_type, this, true);

            int titleTextAppearance = a.getResourceId(R.styleable.SelectChannelTypeView_sb_select_channel_type_menu_title_appearance, R.style.SendbirdH1OnLight01);
            int menuBackgroundRes = a.getResourceId(R.styleable.SelectChannelTypeView_sb_select_channel_type_menu_background, R.drawable.sb_button_uncontained_background_light);
            int nameTextAppearance = a.getResourceId(R.styleable.SelectChannelTypeView_sb_select_channel_type_menu_name_appearance, R.style.SendbirdCaption2OnLight02);
            ColorStateList iconTint = a.getColorStateList(R.styleable.SelectChannelTypeView_sb_select_channel_type_menu_icon_tint);

            binding.tvTitle.setTextAppearance(context, titleTextAppearance);

            binding.vgGroup.setBackgroundResource(menuBackgroundRes);
            binding.vgBroadcast.setBackgroundResource(menuBackgroundRes);
            binding.vgSuperGroup.setBackgroundResource(menuBackgroundRes);

            binding.tvMenuGroupChat.setTextAppearance(context, nameTextAppearance);
            binding.tvMenuSuperGroupChat.setTextAppearance(context, nameTextAppearance);
            binding.tvMenuBroadcastChant.setTextAppearance(context, nameTextAppearance);

            binding.ivIconGroup.setImageDrawable(DrawableUtils.setTintList(context, R.drawable.icon_chat, iconTint));
            binding.ivIconSuperGroup.setImageDrawable(DrawableUtils.setTintList(context, R.drawable.icon_supergroup, iconTint));
            binding.ivIconBroadcast.setImageDrawable(DrawableUtils.setTintList(context, R.drawable.icon_broadcast, iconTint));

            binding.vgGroup.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(v, 0, CreateableChannelType.Normal);
                }
            });
            binding.vgSuperGroup.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(v, 1, CreateableChannelType.Super);
                }
            });
            binding.vgBroadcast.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(v, 2, CreateableChannelType.Broadcast);
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

    public void setOnItemClickListener(OnItemClickListener<CreateableChannelType> listener) {
        this.listener = listener;
    }
}
