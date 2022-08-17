package com.sendbird.uikit.widgets;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.OGMetaData;
import com.sendbird.uikit.R;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.databinding.SbViewOpenChannelUserMessageComponentBinding;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.utils.IntentUtils;
import com.sendbird.uikit.utils.MessageUtils;
import com.sendbird.uikit.utils.ViewUtils;

public class OpenChannelUserMessageView extends OpenChannelMessageView {
    private final SbViewOpenChannelUserMessageComponentBinding binding;
    private final int messageAppearance;
    private final int operatorAppearance;
    private final int sentAtAppearance;
    private final int nicknameAppearance;
    private final int editedAppearance;
    private final int marginLeftEmpty;
    private final int marginLeftNor;

    @NonNull
    @Override
    public SbViewOpenChannelUserMessageComponentBinding getBinding() {
        return binding;
    }

    public OpenChannelUserMessageView(@NonNull Context context) {
        this(context, null);
    }

    public OpenChannelUserMessageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_widget_user_message);
    }

    public OpenChannelUserMessageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageView, defStyle, 0);
        try {
            this.binding = SbViewOpenChannelUserMessageComponentBinding.inflate(LayoutInflater.from(getContext()), this, true);
            sentAtAppearance = a.getResourceId(R.styleable.MessageView_sb_message_time_text_appearance, R.style.SendbirdCaption4OnLight03);
            messageAppearance = a.getResourceId(R.styleable.MessageView_sb_message_text_appearance, R.style.SendbirdBody3OnLight01);
            int contentBackground = a.getResourceId(R.styleable.MessageView_sb_message_background, R.drawable.selector_rectangle_light);
            int linkTextColor = a.getResourceId(R.styleable.MessageView_sb_message_link_text_color, R.color.ondark_01);
            int ogtagBackground = a.getResourceId(R.styleable.MessageView_sb_message_ogtag_background, R.drawable.selector_open_channel_message_bg_light);
            nicknameAppearance = a.getResourceId(R.styleable.MessageView_sb_message_sender_name_text_appearance, R.style.SendbirdCaption1OnLight02);
            operatorAppearance = a.getResourceId(R.styleable.MessageView_sb_message_operator_name_text_appearance, R.style.SendbirdCaption1Secondary300);
            editedAppearance = a.getResourceId(R.styleable.MessageView_sb_message_edited_mark_text_appearance, R.style.SendbirdBody3OnLight02);

            binding.ogTag.setBackgroundResource(ogtagBackground);
            binding.tvMessage.setLinkTextColor(context.getResources().getColor(linkTextColor));
            binding.tvMessage.setClickedLinkTextColor(context.getResources().getColor(linkTextColor));
            binding.contentPanel.setBackgroundResource(contentBackground);

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

    @NonNull
    @Override
    public View getLayout() {
        return binding.getRoot();
    }

    @Override
    public void drawMessage(@NonNull OpenChannel channel, @NonNull BaseMessage message, @NonNull MessageGroupType messageGroupType) {
        if (messageUIConfig != null) {
            messageUIConfig.getMyEditedTextMarkUIConfig().mergeFromTextAppearance(getContext(), editedAppearance);
            messageUIConfig.getOtherEditedTextMarkUIConfig().mergeFromTextAppearance(getContext(), editedAppearance);
            messageUIConfig.getMyMessageTextUIConfig().mergeFromTextAppearance(getContext(), messageAppearance);
            messageUIConfig.getOtherMessageTextUIConfig().mergeFromTextAppearance(getContext(), messageAppearance);
            messageUIConfig.getMySentAtTextUIConfig().mergeFromTextAppearance(getContext(), sentAtAppearance);
            messageUIConfig.getOtherSentAtTextUIConfig().mergeFromTextAppearance(getContext(), sentAtAppearance);
            messageUIConfig.getMyNicknameTextUIConfig().mergeFromTextAppearance(getContext(), nicknameAppearance);
            messageUIConfig.getOtherNicknameTextUIConfig().mergeFromTextAppearance(getContext(), nicknameAppearance);
            messageUIConfig.getOperatorNicknameTextUIConfig().mergeFromTextAppearance(getContext(), operatorAppearance);
            final boolean isMine = MessageUtils.isMine(message);
            final Drawable background = isMine ? messageUIConfig.getMyMessageBackground() : messageUIConfig.getOtherMessageBackground();
            final Drawable ogtagBackground = isMine ? messageUIConfig.getMyOgtagBackground() : messageUIConfig.getOtherOgtagBackground();
            final ColorStateList linkedTextColor = messageUIConfig.getLinkedTextColor();
            if (background != null) binding.contentPanel.setBackground(background);
            if (ogtagBackground != null) binding.ogTag.setBackground(ogtagBackground);
            if (linkedTextColor != null) {
                binding.tvMessage.setLinkTextColor(linkedTextColor);
                binding.tvMessage.setClickedLinkTextColor(linkedTextColor.getDefaultColor());
            }
        }
        ViewUtils.drawTextMessage(binding.tvMessage, message, messageUIConfig);

        binding.ogTag.drawOgtag(message.getOgMetaData());
        binding.ivStatus.drawStatus(message, channel);

        if (messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE || messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD) {
            binding.ivProfileView.setVisibility(View.VISIBLE);
            binding.tvNickname.setVisibility(View.VISIBLE);
            binding.tvSentAt.setVisibility(View.VISIBLE);

            ViewUtils.drawSentAt(binding.tvSentAt, message, messageUIConfig);
            ViewUtils.drawNickname(binding.tvNickname, message, messageUIConfig, channel.isOperator(message.getSender()));
            ViewUtils.drawProfile(binding.ivProfileView, message);

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.tvMessage.getLayoutParams();
            params.leftMargin = marginLeftNor;
            binding.tvMessage.setLayoutParams(params);
        } else {
            binding.ivProfileView.setVisibility(View.GONE);
            binding.tvNickname.setVisibility(View.GONE);
            binding.tvSentAt.setVisibility(View.INVISIBLE);

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
