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
import com.sendbird.uikit.databinding.SbViewTimeLineMessageComponentBinding;
import com.sendbird.uikit.utils.DateUtils;

public class TimelineMessageView extends BaseMessageView {
    private final SbViewTimeLineMessageComponentBinding binding;

    @NonNull
    public SbViewTimeLineMessageComponentBinding getBinding() {
        return binding;
    }

    @NonNull
    @Override
    public View getLayout() {
        return binding.getRoot();
    }

    public TimelineMessageView(@NonNull Context context) {
        this(context, null);
    }

    public TimelineMessageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_widget_timeline_message);
    }

    public TimelineMessageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageView, defStyle, 0);
        try {
            this.binding = SbViewTimeLineMessageComponentBinding.inflate(LayoutInflater.from(getContext()), this, true);
            int textAppearance = a.getResourceId(R.styleable.MessageView_sb_message_timeline_text_appearance, R.style.SendbirdCaption1OnDark01);
            int backgroundResourceId = a.getResourceId(R.styleable.MessageView_sb_message_timeline_background, R.drawable.sb_shape_timeline_background);

            binding.tvTimeline.setTextAppearance(context, textAppearance);
            binding.tvTimeline.setBackgroundResource(backgroundResourceId);
        } finally {
            a.recycle();
        }
    }

    public void drawTimeline(@NonNull BaseMessage message) {
        binding.tvTimeline.setText(DateUtils.formatDate(message.getCreatedAt()));
    }
}
