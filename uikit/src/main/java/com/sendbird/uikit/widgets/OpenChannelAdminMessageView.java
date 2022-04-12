package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.BaseMessage;
import com.sendbird.uikit.R;
import com.sendbird.uikit.databinding.SbViewOpenChannelAdminMessageComponentBinding;

public class OpenChannelAdminMessageView extends BaseMessageView {
    private final SbViewOpenChannelAdminMessageComponentBinding binding;

    @NonNull
    public SbViewOpenChannelAdminMessageComponentBinding getBinding() {
        return binding;
    }

    @NonNull
    public View getLayout() {
        return binding.getRoot();
    }

    public OpenChannelAdminMessageView(@NonNull Context context) {
        this(context, null);
    }

    public OpenChannelAdminMessageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_widget_admin_message);
    }

    public OpenChannelAdminMessageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageView, defStyle, 0);
        try {
            this.binding = SbViewOpenChannelAdminMessageComponentBinding.inflate(LayoutInflater.from(getContext()), this, true);
            int textAppearance = a.getResourceId(R.styleable.MessageView_Admin_sb_admin_message_text_appearance, R.style.SendbirdCaption2OnLight02);
            int backgroundResourceId = a.getResourceId(R.styleable.MessageView_Admin_sb_admin_message_background, R.drawable.sb_shape_admin_message_background_light);

            binding.tvMessage.setTextAppearance(context, textAppearance);
            binding.tvMessage.setBackgroundResource(backgroundResourceId);
        } finally {
            a.recycle();
        }
    }

    public void drawMessage(@NonNull BaseMessage message) {
        binding.tvMessage.setText(message.getMessage());
    }
}
