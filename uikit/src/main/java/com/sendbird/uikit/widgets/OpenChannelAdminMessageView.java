package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;

import com.sendbird.android.BaseMessage;
import com.sendbird.uikit.R;
import com.sendbird.uikit.databinding.SbViewOpenChannelAdminMessageComponentBinding;

public class OpenChannelAdminMessageView extends BaseMessageView {
    private SbViewOpenChannelAdminMessageComponentBinding binding;

    public SbViewOpenChannelAdminMessageComponentBinding getBinding() {
        return binding;
    }

    public View getLayout() {
        return binding.getRoot();
    }

    public OpenChannelAdminMessageView(Context context) {
        this(context, null);
    }

    public OpenChannelAdminMessageView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.sb_open_channel_message_admin_style);
    }

    public OpenChannelAdminMessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageView, defStyle, 0);
        try {
            this.binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.sb_view_open_channel_admin_message_component, this, true);
            int textAppearance = a.getResourceId(R.styleable.MessageView_Admin_sb_admin_message_text_appearance, R.style.SendbirdCaption2OnLight02);
            int backgroundResourceId = a.getResourceId(R.styleable.MessageView_Admin_sb_admin_message_background, R.drawable.sb_shape_admin_message_background_light);

            binding.tvMessage.setTextAppearance(context, textAppearance);
            binding.tvMessage.setBackgroundResource(backgroundResourceId);
        } finally {
            a.recycle();
        }
    }

    public void drawMessage(BaseMessage message) {
        binding.tvMessage.setText(message.getMessage());
    }

    @BindingAdapter("message")
    public static void drawMessage(OpenChannelAdminMessageView view, BaseMessage message) {
        view.drawMessage(message);
    }
}
