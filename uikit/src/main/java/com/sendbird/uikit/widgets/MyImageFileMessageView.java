package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.FileMessage;
import com.sendbird.android.message.SendingStatus;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.databinding.SbViewMyFileImageMessageComponentBinding;
import com.sendbird.uikit.utils.DateUtils;
import com.sendbird.uikit.utils.DrawableUtils;
import com.sendbird.uikit.utils.ViewUtils;

public class MyImageFileMessageView extends GroupChannelMessageView {
    private final SbViewMyFileImageMessageComponentBinding binding;

    @NonNull
    @Override
    public SbViewMyFileImageMessageComponentBinding getBinding() {
        return binding;
    }

    @NonNull
    @Override
    public View getLayout() {
        return binding.getRoot();
    }

    public MyImageFileMessageView(@NonNull Context context) {
        this(context, null);
    }

    public MyImageFileMessageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_widget_my_file_message);
    }

    public MyImageFileMessageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageView_File, defStyle, 0);
        try {
            this.binding = SbViewMyFileImageMessageComponentBinding.inflate(LayoutInflater.from(getContext()), this, true);
            int timeAppearance = a.getResourceId(R.styleable.MessageView_File_sb_message_time_text_appearance, R.style.SendbirdCaption4OnLight03);
            int messageBackground = a.getResourceId(R.styleable.MessageView_File_sb_message_me_background, R.drawable.sb_shape_chat_bubble);
            ColorStateList messageBackgroundTint = a.getColorStateList(R.styleable.MessageView_File_sb_message_me_background_tint);
            int emojiReactionListBackground = a.getResourceId(R.styleable.MessageView_File_sb_message_emoji_reaction_list_background, R.drawable.sb_shape_chat_bubble_reactions_light);

            binding.tvSentAt.setTextAppearance(context, timeAppearance);
            binding.contentPanel.setBackground(DrawableUtils.setTintList(context, messageBackground, messageBackgroundTint));
            binding.emojiReactionListBackground.setBackgroundResource(emojiReactionListBackground);

            int bg = SendbirdUIKit.isDarkMode() ? R.drawable.sb_shape_image_message_background_dark : R.drawable.sb_shape_image_message_background;
            binding.ivThumbnail.setBackgroundResource(bg);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void drawMessage(@NonNull GroupChannel channel, @NonNull BaseMessage message, @NonNull MessageGroupType messageGroupType) {
        boolean sendingState = message.getSendingStatus() == SendingStatus.SUCCEEDED;
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
