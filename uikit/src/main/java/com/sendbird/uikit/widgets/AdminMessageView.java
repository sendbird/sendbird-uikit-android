package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;

import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseMessage;
import com.sendbird.uikit.R;
import com.sendbird.uikit.databinding.SbViewAdminMessageComponentBinding;

public class AdminMessageView extends BaseMessageView {
    private SbViewAdminMessageComponentBinding binding;

    @Override
    public SbViewAdminMessageComponentBinding getBinding() {
        return binding;
    }

    @Override
    public View getLayout() {
        return binding.getRoot();
    }

    public AdminMessageView(Context context) {
        this(context, null);
    }

    public AdminMessageView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.sb_message_admin_style);
    }

    public AdminMessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageView_Admin, defStyle, 0);
        try {
            this.binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.sb_view_admin_message_component, this, true);
            int textAppearance = a.getResourceId(R.styleable.MessageView_Admin_sb_admin_message_text_appearance, R.style.SendbirdCaption2OnLight02);
            int backgroundResourceId = a.getResourceId(R.styleable.MessageView_Admin_sb_admin_message_background, android.R.color.transparent);

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
    public static void drawMessage(AdminMessageView view, AdminMessage message) {
        view.drawMessage(message);
    }
}
