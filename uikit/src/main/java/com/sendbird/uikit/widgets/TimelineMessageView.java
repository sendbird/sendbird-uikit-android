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
import com.sendbird.uikit.databinding.SbViewTimeLineMessageComponentBinding;
import com.sendbird.uikit.utils.DateUtils;

public class TimelineMessageView extends BaseMessageView {
    private SbViewTimeLineMessageComponentBinding binding;

    public SbViewTimeLineMessageComponentBinding getBinding() {
        return binding;
    }

    @Override
    public View getLayout() {
        return binding.getRoot();
    }

    public TimelineMessageView(Context context) {
        this(context, null);
    }

    public TimelineMessageView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.sb_message_timeline_style);
    }

    public TimelineMessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageView, defStyle, 0);
        try {
            this.binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.sb_view_time_line_message_component, this, true);
            int textAppearance = a.getResourceId(R.styleable.MessageView_sb_message_timeline_text_appearance, R.style.SendbirdCaption1OnDark01);
            int backgroundResourceId = a.getResourceId(R.styleable.MessageView_sb_message_timeline_background, R.drawable.sb_shape_timeline_background);

            binding.tvTimeline.setTextAppearance(context, textAppearance);
            binding.tvTimeline.setBackgroundResource(backgroundResourceId);
        } finally {
            a.recycle();
        }
    }

    public void drawTimeline(BaseMessage message) {
        binding.tvTimeline.setText(DateUtils.formatDate(message.getCreatedAt()));
    }

    @BindingAdapter("message")
    public static void drawTimeline(TimelineMessageView view, BaseMessage message) {
        view.drawTimeline(message);
    }
}
