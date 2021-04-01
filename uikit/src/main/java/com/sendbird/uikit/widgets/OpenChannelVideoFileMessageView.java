package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.OpenChannel;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.databinding.SbViewOpenChannelFileVideoMessageComponentBinding;
import com.sendbird.uikit.utils.DateUtils;
import com.sendbird.uikit.utils.ViewUtils;

public class OpenChannelVideoFileMessageView extends OpenChannelMessageView {
    private SbViewOpenChannelFileVideoMessageComponentBinding binding;
    private int nicknameAppearance;
    private int operatorAppearance;
    private int marginLeftEmpty;
    private int marginLeftNor;

    @Override
    public SbViewOpenChannelFileVideoMessageComponentBinding getBinding() {
        return binding;
    }

    @Override
    public View getLayout() {
        return binding.getRoot();
    }

    public OpenChannelVideoFileMessageView(Context context) {
        this(context, null);
    }

    public OpenChannelVideoFileMessageView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.sb_open_channel_message_file_style);
    }

    public OpenChannelVideoFileMessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageView, defStyle, 0);
        try {
            this.binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.sb_view_open_channel_file_video_message_component, this, true);
            int timeAppearance = a.getResourceId(R.styleable.MessageView_sb_message_time_text_appearance, R.style.SendbirdCaption4OnLight03);
            int contentBackground = a.getResourceId(R.styleable.MessageView_sb_message_background, R.drawable.selector_open_channel_message_bg_light);
            nicknameAppearance = a.getResourceId(R.styleable.MessageView_sb_message_sender_name_text_appearance, R.style.SendbirdCaption1OnLight02);
            operatorAppearance = a.getResourceId(R.styleable.MessageView_sb_message_operator_name_text_appearance, R.style.SendbirdCaption1Secondary300);

            binding.tvSentAt.setTextAppearance(context, timeAppearance);
            binding.tvNickname.setTextAppearance(context, nicknameAppearance);
            binding.contentPanel.setBackgroundResource(contentBackground);

            int bg = SendBirdUIKit.isDarkMode() ? R.drawable.sb_shape_image_message_background_dark : R.drawable.sb_shape_image_message_background;
            binding.ivThumbnail.setBackgroundResource(bg);

            marginLeftEmpty = getResources().getDimensionPixelSize(R.dimen.sb_size_40);
            marginLeftNor = getResources().getDimensionPixelSize(R.dimen.sb_size_12);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void drawMessage(OpenChannel channel, BaseMessage message, MessageGroupType messageGroupType) {
        FileMessage fileMessage = (FileMessage) message;

        binding.ivThumbnail.setRadius(getResources().getDimensionPixelSize(R.dimen.sb_size_8));
        ViewUtils.drawThumbnail(binding.ivThumbnail, fileMessage);
        ViewUtils.drawThumbnailIcon(binding.ivThumbnailIcon, fileMessage);
        binding.ivStatus.drawStatus(message, channel);
        if (channel.isOperator(message.getSender())) {
            binding.tvNickname.setTextAppearance(getContext(), operatorAppearance);
        } else {
            binding.tvNickname.setTextAppearance(getContext(), nicknameAppearance);
        }

        if (messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE || messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD) {
            binding.ivProfileView.setVisibility(View.VISIBLE);
            binding.tvNickname.setVisibility(View.VISIBLE);
            binding.tvSentAt.setVisibility(View.VISIBLE);
            binding.tvSentAt.setText(DateUtils.formatTime(getContext(), message.getCreatedAt()));

            ViewUtils.drawNickname(binding.tvNickname, message);
            ViewUtils.drawProfile(binding.ivProfileView, message);

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.contentPanel.getLayoutParams();
            params.leftMargin = marginLeftNor;
            binding.contentPanel.setLayoutParams(params);
        } else {
            binding.ivProfileView.setVisibility(View.GONE);
            binding.tvNickname.setVisibility(View.GONE);
            binding.tvSentAt.setVisibility(View.GONE);

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.contentPanel.getLayoutParams();
            params.leftMargin = marginLeftEmpty;
            binding.contentPanel.setLayoutParams(params);
        }
    }
}
