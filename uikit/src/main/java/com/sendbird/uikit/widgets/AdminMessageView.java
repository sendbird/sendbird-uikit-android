package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.message.BaseMessage;
import com.sendbird.uikit.R;
import com.sendbird.uikit.databinding.SbViewAdminMessageComponentBinding;

public class AdminMessageView extends BaseMessageView {
    private final SbViewAdminMessageComponentBinding binding;

    @NonNull
    @Override
    public SbViewAdminMessageComponentBinding getBinding() {
        return binding;
    }

    @NonNull
    @Override
    public View getLayout() {
        return binding.getRoot();
    }

    public AdminMessageView(@NonNull Context context) {
        this(context, null);
    }

    public AdminMessageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_widget_admin_message);
    }

    public AdminMessageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageView_Admin, defStyle, 0);
        try {
            this.binding = SbViewAdminMessageComponentBinding.inflate(LayoutInflater.from(getContext()), this, true);
            int textAppearance = a.getResourceId(R.styleable.MessageView_Admin_sb_admin_message_text_appearance, R.style.SendbirdCaption2OnLight02);
            int backgroundResourceId = a.getResourceId(R.styleable.MessageView_Admin_sb_admin_message_background, android.R.color.transparent);

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
