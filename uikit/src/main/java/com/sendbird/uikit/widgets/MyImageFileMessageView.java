package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.databinding.SbViewMyFileImageMessageComponentBinding;
import com.sendbird.uikit.utils.DateUtils;
import com.sendbird.uikit.utils.DrawableUtils;
import com.sendbird.uikit.utils.ViewUtils;

public class MyImageFileMessageView extends GroupChannelMessageView {
    private SbViewMyFileImageMessageComponentBinding binding;

    @Override
    public SbViewMyFileImageMessageComponentBinding getBinding() {
        return binding;
    }

    @Override
    public View getLayout() {
        return binding.getRoot();
    }

    public MyImageFileMessageView(Context context) {
        this(context, null);
    }

    public MyImageFileMessageView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.sb_message_file_style);
    }

    public MyImageFileMessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageView_File, defStyle, 0);
        try {
            this.binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.sb_view_my_file_image_message_component, this, true);
            int timeAppearance = a.getResourceId(R.styleable.MessageView_File_sb_message_time_text_appearance, R.style.SendbirdCaption4OnLight03);
            int messageBackground = a.getResourceId(R.styleable.MessageView_File_sb_message_me_background, R.drawable.sb_shape_chat_bubble);
            int messageBackgroundTint = a.getResourceId(R.styleable.MessageView_File_sb_message_me_background_tint, R.color.sb_message_me_tint_light);
            int emojiReactionListBackground = a.getResourceId(R.styleable.MessageView_File_sb_message_emoji_reaction_list_background, R.drawable.sb_shape_chat_bubble_reactions_light);

            binding.tvSentAt.setTextAppearance(context, timeAppearance);
            binding.contentPanel.setBackground(DrawableUtils.setTintList(getContext(), messageBackground, messageBackgroundTint));
            binding.emojiReactionListBackground.setBackgroundResource(emojiReactionListBackground);

            int bg = SendBirdUIKit.isDarkMode() ? R.drawable.sb_shape_image_message_background_dark : R.drawable.sb_shape_image_message_background;
            binding.ivThumbnail.setBackgroundResource(bg);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void drawMessage(GroupChannel channel, BaseMessage message, MessageGroupType messageGroupType) {
        boolean sendingState = message.getSendingStatus() == BaseMessage.SendingStatus.SUCCEEDED;
        boolean hasReaction = message.getReactions() != null && message.getReactions().size() > 0;

        binding.emojiReactionListBackground.setVisibility(hasReaction ? View.VISIBLE : View.GONE);
        binding.rvEmojiReactionList.setVisibility(hasReaction ? View.VISIBLE : View.GONE);
        binding.tvSentAt.setVisibility((sendingState && (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE)) ? View.VISIBLE : View.GONE);
        binding.tvSentAt.setText(DateUtils.formatTime(getContext(), message.getCreatedAt()));
        binding.ivStatus.drawStatus(message, channel);

        ViewUtils.drawReactionEnabled(binding.rvEmojiReactionList, channel);
        ViewUtils.drawThumbnail(binding.ivThumbnail, (FileMessage) message);
        ViewUtils.drawThumbnailIcon(binding.ivThumbnailIcon, (FileMessage) message);

        int paddingTop = getResources().getDimensionPixelSize((messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) ? R.dimen.sb_size_1 : R.dimen.sb_size_8);
        int paddingBottom = getResources().getDimensionPixelSize((messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) ? R.dimen.sb_size_1 : R.dimen.sb_size_8);
        binding.root.setPadding(binding.root.getPaddingLeft(), paddingTop, binding.root.getPaddingRight(), paddingBottom);

        ViewUtils.drawQuotedMessage(binding.quoteReplyPanel, message);
    }
}
