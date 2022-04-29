package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.uikit.R;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.databinding.SbViewMyFileMessageComponentBinding;
import com.sendbird.uikit.utils.DateUtils;
import com.sendbird.uikit.utils.DrawableUtils;
import com.sendbird.uikit.utils.ViewUtils;

public class MyFileMessageView extends GroupChannelMessageView {
    private final SbViewMyFileMessageComponentBinding binding;
    private final int searchedTextBackground;
    private final int searchedMessageAppearance;

    @NonNull
    @Override
    public SbViewMyFileMessageComponentBinding getBinding() {
        return binding;
    }

    @NonNull
    @Override
    public View getLayout() {
        return binding.getRoot();
    }

    public MyFileMessageView(@NonNull Context context) {
        this(context, null);
    }

    public MyFileMessageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_widget_my_file_message);
    }

    public MyFileMessageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageView_File, defStyle, 0);
        try {
            this.binding = SbViewMyFileMessageComponentBinding.inflate(LayoutInflater.from(getContext()), this, true);
            int timeAppearance = a.getResourceId(R.styleable.MessageView_File_sb_message_time_text_appearance, R.style.SendbirdCaption4OnLight03);
            int messageAppearance = a.getResourceId(R.styleable.MessageView_File_sb_message_me_text_appearance, R.style.SendbirdBody3OnDark01);
            int messageBackground = a.getResourceId(R.styleable.MessageView_File_sb_message_me_background, R.drawable.sb_shape_chat_bubble);
            ColorStateList messageBackgroundTint = a.getColorStateList(R.styleable.MessageView_File_sb_message_me_background_tint);
            int emojiReactionListBackground = a.getResourceId(R.styleable.MessageView_File_sb_message_emoji_reaction_list_background, R.drawable.sb_shape_chat_bubble_reactions_light);
            this.searchedTextBackground = a.getResourceId(R.styleable.MessageView_User_sb_message_searched_text_background, R.color.highlight);
            this.searchedMessageAppearance = a.getResourceId(R.styleable.MessageView_User_sb_message_searched_text_appearance, R.style.SendbirdSearchedMessage);

            binding.tvSentAt.setTextAppearance(context, timeAppearance);
            binding.tvFileName.setTextAppearance(context, messageAppearance);
            binding.tvFileName.setPaintFlags(binding.tvFileName.getPaintFlags()|Paint.UNDERLINE_TEXT_FLAG);
            binding.contentPanelWithReactions.setBackground(DrawableUtils.setTintList(context, messageBackground, messageBackgroundTint));
            binding.emojiReactionListBackground.setBackgroundResource(emojiReactionListBackground);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void drawMessage(@NonNull GroupChannel channel, @NonNull BaseMessage message, @NonNull MessageGroupType messageGroupType) {
        final FileMessage fileMessage = (FileMessage) message;
        boolean sendingState = message.getSendingStatus() == BaseMessage.SendingStatus.SUCCEEDED;
        boolean hasReaction = message.getReactions() != null && message.getReactions().size() > 0;

        binding.emojiReactionListBackground.setVisibility(hasReaction ? View.VISIBLE : View.GONE);
        binding.rvEmojiReactionList.setVisibility(hasReaction ? View.VISIBLE : View.GONE);
        binding.tvSentAt.setVisibility((sendingState && (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE)) ? View.VISIBLE : View.GONE);
        binding.tvSentAt.setText(DateUtils.formatTime(getContext(), message.getCreatedAt()));
        binding.ivStatus.drawStatus(message, channel);

        CharSequence text = fileMessage.getName();
        if (messageUIConfig != null) {
            messageUIConfig.getSearchedTextUIConfig().mergeFromTextAppearance(getContext(), searchedMessageAppearance, searchedTextBackground);
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
