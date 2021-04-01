package com.sendbird.uikit.widgets;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.OGMetaData;
import com.sendbird.android.OpenChannel;
import com.sendbird.uikit.R;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.databinding.SbViewOpenChannelUserMessageComponentBinding;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.utils.DateUtils;
import com.sendbird.uikit.utils.IntentUtils;
import com.sendbird.uikit.utils.ViewUtils;

public class OpenChannelUserMessageView extends OpenChannelMessageView {
    private SbViewOpenChannelUserMessageComponentBinding binding;
    private int editedAppearance;
    private int operatorAppearance;
    private int nicknameAppearance;
    private int marginLeftEmpty;
    private int marginLeftNor;

    @Override
    public SbViewOpenChannelUserMessageComponentBinding getBinding() {
        return binding;
    }

    public OpenChannelUserMessageView(Context context) {
        this(context, null);
    }

    public OpenChannelUserMessageView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.sb_open_channel_message_user_style);
    }

    public OpenChannelUserMessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageView, defStyle, 0);
        try {
            this.binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.sb_view_open_channel_user_message_component, this, true);
            int timeAppearance = a.getResourceId(R.styleable.MessageView_sb_message_time_text_appearance, R.style.SendbirdCaption4OnLight03);
            int messageAppearance = a.getResourceId(R.styleable.MessageView_sb_message_text_appearance, R.style.SendbirdBody3OnLight01);
            int contentBackground = a.getResourceId(R.styleable.MessageView_sb_message_background, R.drawable.selector_rectangle_light);
            int linkTextColor = a.getResourceId(R.styleable.MessageView_sb_message_link_text_color, R.color.ondark_01);
            int ogtagBackground = a.getResourceId(R.styleable.MessageView_sb_message_ogtag_background, R.drawable.selector_open_channel_message_bg_light);
            nicknameAppearance = a.getResourceId(R.styleable.MessageView_sb_message_sender_name_text_appearance, R.style.SendbirdCaption1OnLight02);
            operatorAppearance = a.getResourceId(R.styleable.MessageView_sb_message_operator_name_text_appearance, R.style.SendbirdCaption1Secondary300);
            editedAppearance = a.getResourceId(R.styleable.MessageView_sb_message_edited_mark_text_appearance, R.style.SendbirdBody3OnLight02);

            binding.ogTag.setBackgroundResource(ogtagBackground);
            binding.tvMessage.setTextAppearance(context, messageAppearance);
            binding.tvMessage.setLinkTextColor(context.getResources().getColor(linkTextColor));
            binding.tvMessage.setClickedLinkTextColor(context.getResources().getColor(linkTextColor));
            binding.contentPanel.setBackgroundResource(contentBackground);
            binding.tvSentAt.setTextAppearance(context, timeAppearance);

            binding.tvMessage.setOnClickListener(v -> binding.contentPanel.performClick());
            binding.tvMessage.setOnLongClickListener(v -> binding.contentPanel.performLongClick());
            binding.tvMessage.setOnLinkLongClickListener((v, link) -> binding.contentPanel.performLongClick());
            binding.tvMessage.setClickedLinkBackgroundColor(context.getResources().getColor(R.color.primary_400));
            binding.ogTag.setOnLongClickListener(v -> binding.contentPanel.performLongClick());

            marginLeftEmpty = getResources().getDimensionPixelSize(R.dimen.sb_size_40);
            marginLeftNor = getResources().getDimensionPixelSize(R.dimen.sb_size_12);
        } finally {
            a.recycle();
        }
    }

    @Override
    public View getLayout() {
        return binding.getRoot();
    }

    @Override
    public void drawMessage(OpenChannel channel, BaseMessage message, MessageGroupType messageGroupType) {
        ViewUtils.drawTextMessage(binding.tvMessage, message, editedAppearance);

        binding.ogTag.drawOgtag(message.getOgMetaData());
        binding.ivStatus.drawStatus(message, channel);

        if (messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE || messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD) {
            binding.ivProfileView.setVisibility(View.VISIBLE);
            binding.tvNickname.setVisibility(View.VISIBLE);
            binding.tvSentAt.setVisibility(View.VISIBLE);
            binding.tvSentAt.setText(DateUtils.formatTime(getContext(), message.getCreatedAt()));

            if (channel.isOperator(message.getSender())) {
                binding.tvNickname.setTextAppearance(getContext(), operatorAppearance);
            } else {
                binding.tvNickname.setTextAppearance(getContext(), nicknameAppearance);
            }

            ViewUtils.drawNickname(binding.tvNickname, message);
            ViewUtils.drawProfile(binding.ivProfileView, message);

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.tvMessage.getLayoutParams();
            params.leftMargin = marginLeftNor;
            binding.tvMessage.setLayoutParams(params);
        } else {
            binding.ivProfileView.setVisibility(View.GONE);
            binding.tvNickname.setVisibility(View.GONE);
            binding.tvSentAt.setVisibility(View.GONE);

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.tvMessage.getLayoutParams();
            params.leftMargin = marginLeftEmpty;
            binding.tvMessage.setLayoutParams(params);
        }

        binding.ogTag.setOnClickListener(v -> {
            OGMetaData ogMetaData = message.getOgMetaData();
            if (ogMetaData.getUrl() == null) {
                return;
            }

            Intent intent = IntentUtils.getWebViewerIntent(ogMetaData.getUrl());
            try {
                getContext().startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Logger.e(e);
            }
        });
    }
}
