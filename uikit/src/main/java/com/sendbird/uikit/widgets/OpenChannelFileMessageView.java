package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
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
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.databinding.SbViewOpenChannelFileMessageComponentBinding;
import com.sendbird.uikit.utils.DateUtils;
import com.sendbird.uikit.utils.DrawableUtils;
import com.sendbird.uikit.utils.ViewUtils;

public class OpenChannelFileMessageView extends OpenChannelMessageView {
    private SbViewOpenChannelFileMessageComponentBinding binding;
    private int nicknameAppearance;
    private int operatorAppearance;
    private int marginLeftEmpty;
    private int marginLeftNor;

    @Override
    public SbViewOpenChannelFileMessageComponentBinding getBinding() {
        return binding;
    }

    @Override
    public View getLayout() {
        return binding.getRoot();
    }

    public OpenChannelFileMessageView(Context context) {
        this(context, null);
    }

    public OpenChannelFileMessageView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.sb_open_channel_message_file_style);
    }

    public OpenChannelFileMessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageView, defStyle, 0);
        try {
            this.binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.sb_view_open_channel_file_message_component, this, true);
            int timeAppearance = a.getResourceId(R.styleable.MessageView_sb_message_time_text_appearance, R.style.SendbirdCaption4OnLight03);
            int contentBackground = a.getResourceId(R.styleable.MessageView_sb_message_background, R.drawable.selector_open_channel_message_bg_light);
            int messageAppearance = a.getResourceId(R.styleable.MessageView_sb_message_text_appearance, R.style.SendbirdBody3OnLight01);
            nicknameAppearance = a.getResourceId(R.styleable.MessageView_sb_message_sender_name_text_appearance, R.style.SendbirdCaption1OnLight02);
            operatorAppearance = a.getResourceId(R.styleable.MessageView_sb_message_operator_name_text_appearance, R.style.SendbirdCaption1Secondary300);

            binding.tvSentAt.setTextAppearance(context, timeAppearance);
            binding.tvNickname.setTextAppearance(context, nicknameAppearance);
            binding.contentPanel.setBackgroundResource(contentBackground);
            binding.tvFileName.setTextAppearance(context, messageAppearance);
            binding.tvFileName.setPaintFlags(binding.tvFileName.getPaintFlags()|Paint.UNDERLINE_TEXT_FLAG);

            marginLeftEmpty = getResources().getDimensionPixelSize(R.dimen.sb_size_40);
            marginLeftNor = getResources().getDimensionPixelSize(R.dimen.sb_size_12);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void drawMessage(OpenChannel channel, BaseMessage message, MessageGroupType messageGroupType) {
        final FileMessage fileMessage = (FileMessage) message;

        binding.tvFileName.setText(fileMessage.getName());
        binding.ivStatus.drawStatus(message, channel);
        if (channel.isOperator(message.getSender())) {
            binding.tvNickname.setTextAppearance(getContext(), operatorAppearance);
        } else {
            binding.tvNickname.setTextAppearance(getContext(), nicknameAppearance);
        }

        int backgroundTint = SendBirdUIKit.isDarkMode() ? R.color.background_600 : R.color.background_50;
        int iconTint = SendBirdUIKit.getDefaultThemeMode().getPrimaryTintResId();
        int inset = (int) getContext().getResources().getDimension(R.dimen.sb_size_12);
        if ((fileMessage.getType().toLowerCase().startsWith(StringSet.audio))) {
            Drawable background = DrawableUtils.setTintList(getContext(), R.drawable.sb_rounded_rectangle_corner_24, backgroundTint);
            Drawable icon = DrawableUtils.setTintList(getContext(), R.drawable.icon_file_audio, iconTint);
            binding.ivIcon.setImageDrawable(DrawableUtils.createLayerIcon(background, icon, inset));
        } else {
            Drawable background = DrawableUtils.setTintList(getContext(), R.drawable.sb_rounded_rectangle_corner_24, backgroundTint);
            Drawable icon = DrawableUtils.setTintList(getContext(), R.drawable.icon_file_document, iconTint);
            binding.ivIcon.setImageDrawable(DrawableUtils.createLayerIcon(background, icon, inset));
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
