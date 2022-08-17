package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
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
import com.sendbird.uikit.databinding.SbViewOtherFileImageMessageComponentBinding;
import com.sendbird.uikit.utils.DrawableUtils;
import com.sendbird.uikit.utils.MessageUtils;
import com.sendbird.uikit.utils.ViewUtils;

public class OtherImageFileMessageView extends GroupChannelMessageView {
    private final SbViewOtherFileImageMessageComponentBinding binding;

    private final int sentAtAppearance;
    private final int nicknameAppearance;

    @NonNull
    @Override
    public SbViewOtherFileImageMessageComponentBinding getBinding() {
        return binding;
    }

    @NonNull
    @Override
    public View getLayout() {
        return binding.getRoot();
    }

    public OtherImageFileMessageView(@NonNull Context context) {
        this(context, null);
    }

    public OtherImageFileMessageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_widget_other_file_message);
    }

    public OtherImageFileMessageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageView_File, defStyle, 0);
        try {
            this.binding = SbViewOtherFileImageMessageComponentBinding.inflate(LayoutInflater.from(getContext()), this, true);
            sentAtAppearance = a.getResourceId(R.styleable.MessageView_File_sb_message_time_text_appearance, R.style.SendbirdCaption4OnLight03);
            nicknameAppearance = a.getResourceId(R.styleable.MessageView_File_sb_message_sender_name_text_appearance, R.style.SendbirdCaption1OnLight02);
            int messageBackground = a.getResourceId(R.styleable.MessageView_File_sb_message_other_background, R.drawable.sb_shape_chat_bubble);
            ColorStateList messageBackgroundTint = a.getColorStateList(R.styleable.MessageView_File_sb_message_other_background_tint);
            int emojiReactionListBackground = a.getResourceId(R.styleable.MessageView_File_sb_message_emoji_reaction_list_background, R.drawable.sb_shape_chat_bubble_reactions_light);

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
        boolean showProfile = messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE || messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL;
        boolean showNickname = (messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE || messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD) && !MessageUtils.hasParentMessage(message);

        binding.ivProfileView.setVisibility(showProfile ? View.VISIBLE : View.INVISIBLE);
        binding.tvNickname.setVisibility(showNickname ? View.VISIBLE : View.GONE);
        binding.emojiReactionListBackground.setVisibility(hasReaction ? View.VISIBLE : View.GONE);
        binding.rvEmojiReactionList.setVisibility(hasReaction ? View.VISIBLE : View.GONE);
        binding.tvSentAt.setVisibility((sendingState && (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE)) ? View.VISIBLE : View.INVISIBLE);

        if (messageUIConfig != null) {
            messageUIConfig.getOtherSentAtTextUIConfig().mergeFromTextAppearance(getContext(), sentAtAppearance);
            messageUIConfig.getOtherNicknameTextUIConfig().mergeFromTextAppearance(getContext(), nicknameAppearance);
            final Drawable background = messageUIConfig.getOtherMessageBackground();
            final Drawable reactionBackground = messageUIConfig.getOtherReactionListBackground();
            if (background != null) binding.contentPanel.setBackground(background);
            if (reactionBackground != null) binding.emojiReactionListBackground.setBackground(reactionBackground);
        }

        ViewUtils.drawNickname(binding.tvNickname, message, messageUIConfig, false);
        ViewUtils.drawReactionEnabled(binding.rvEmojiReactionList, channel);
        ViewUtils.drawProfile(binding.ivProfileView, message);
        ViewUtils.drawThumbnail(binding.ivThumbnail, (FileMessage) message);
        ViewUtils.drawThumbnailIcon(binding.ivThumbnailIcon, (FileMessage) message);
        ViewUtils.drawSentAt(binding.tvSentAt, message, messageUIConfig);

        int paddingTop = getResources().getDimensionPixelSize((messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) ? R.dimen.sb_size_1 : R.dimen.sb_size_8);
        int paddingBottom = getResources().getDimensionPixelSize((messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) ? R.dimen.sb_size_1 : R.dimen.sb_size_8);
        binding.root.setPadding(binding.root.getPaddingLeft(), paddingTop, binding.root.getPaddingRight(), paddingBottom);

        ViewUtils.drawQuotedMessage(binding.quoteReplyPanel, message);
    }
}
