package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.uikit.R;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.databinding.SbViewMyFileMessageComponentBinding;
import com.sendbird.uikit.utils.DateUtils;
import com.sendbird.uikit.utils.DrawableUtils;
import com.sendbird.uikit.utils.SpannableStringBuilder;
import com.sendbird.uikit.utils.ViewUtils;

public class MyFileMessageView extends GroupChannelMessageView {
    private SbViewMyFileMessageComponentBinding binding;

    @Override
    public SbViewMyFileMessageComponentBinding getBinding() {
        return binding;
    }

    @Override
    public View getLayout() {
        return binding.getRoot();
    }

    public MyFileMessageView(Context context) {
        this(context, null);
    }

    public MyFileMessageView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.sb_message_file_style);
    }

    public MyFileMessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageView_File, defStyle, 0);
        try {
            this.binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.sb_view_my_file_message_component, this, true);
            int timeAppearance = a.getResourceId(R.styleable.MessageView_File_sb_message_time_text_appearance, R.style.SendbirdCaption4OnLight03);
            int messageAppearance = a.getResourceId(R.styleable.MessageView_File_sb_message_me_text_appearance, R.style.SendbirdBody3OnDark01);
            int messageBackground = a.getResourceId(R.styleable.MessageView_File_sb_message_me_background, R.drawable.sb_shape_chat_bubble);
            int messageBackgroundTint = a.getResourceId(R.styleable.MessageView_File_sb_message_me_background_tint, R.color.sb_message_me_tint_light);
            int emojiReactionListBackground = a.getResourceId(R.styleable.MessageView_File_sb_message_emoji_reaction_list_background, R.drawable.sb_shape_chat_bubble_reactions_light);
            this.highlightBackgroundColor = a.getResourceId(R.styleable.MessageView_User_sb_message_highlight_background_color, R.color.highlight);
            this.highlightForegroundColor = a.getResourceId(R.styleable.MessageView_User_sb_message_highlight_foreground_color, R.color.background_600);

            binding.tvSentAt.setTextAppearance(context, timeAppearance);
            binding.tvFileName.setTextAppearance(context, messageAppearance);
            binding.tvFileName.setPaintFlags(binding.tvFileName.getPaintFlags()|Paint.UNDERLINE_TEXT_FLAG);
            binding.contentPanelWithReactions.setBackground(DrawableUtils.setTintList(getContext(), messageBackground, messageBackgroundTint).mutate());
            binding.emojiReactionListBackground.setBackgroundResource(emojiReactionListBackground);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void drawMessage(GroupChannel channel, BaseMessage message, MessageGroupType messageGroupType) {
        final FileMessage fileMessage = (FileMessage) message;
        boolean sendingState = message.getSendingStatus() == BaseMessage.SendingStatus.SUCCEEDED;
        boolean hasReaction = message.getReactions() != null && message.getReactions().size() > 0;

        binding.emojiReactionListBackground.setVisibility(hasReaction ? View.VISIBLE : View.GONE);
        binding.rvEmojiReactionList.setVisibility(hasReaction ? View.VISIBLE : View.GONE);
        binding.tvSentAt.setVisibility((sendingState && (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE)) ? View.VISIBLE : View.GONE);
        binding.tvSentAt.setText(DateUtils.formatTime(getContext(), message.getCreatedAt()));
        binding.ivStatus.drawStatus(message, channel);

        CharSequence text = fileMessage.getName();
        if (highlightMessageInfo != null && highlightMessageInfo.getMessageId() == message.getMessageId() && highlightMessageInfo.getUpdatedAt() == message.getUpdatedAt()) {
            SpannableStringBuilder builder = new SpannableStringBuilder(getContext(), text);
            builder.addHighlightTextSpan(text.toString(), text.toString(), highlightBackgroundColor, highlightForegroundColor);
            text = builder.build();
        }
        binding.tvFileName.setText(text);

        ViewUtils.drawReactionEnabled(binding.rvEmojiReactionList, channel);
        ViewUtils.drawFileIcon(binding.ivIcon, fileMessage);

        int paddingTop = getResources().getDimensionPixelSize((messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) ? R.dimen.sb_size_1 : R.dimen.sb_size_8);
        int paddingBottom = getResources().getDimensionPixelSize((messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) ? R.dimen.sb_size_1 : R.dimen.sb_size_8);
        binding.root.setPadding(binding.root.getPaddingLeft(), paddingTop, binding.root.getPaddingRight(), paddingBottom);

        ViewUtils.drawQuotedMessage(binding.quoteReplyPanel, message);
    }
}
