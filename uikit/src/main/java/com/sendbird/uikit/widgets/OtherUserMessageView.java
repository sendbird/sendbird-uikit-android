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
import com.sendbird.android.message.SendingStatus;
import com.sendbird.uikit.R;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.databinding.SbViewOtherUserMessageComponentBinding;
import com.sendbird.uikit.model.TextUIConfig;
import com.sendbird.uikit.utils.DateUtils;
import com.sendbird.uikit.utils.DrawableUtils;
import com.sendbird.uikit.utils.MessageUtils;
import com.sendbird.uikit.utils.ViewUtils;

public class OtherUserMessageView extends GroupChannelMessageView {
    private final SbViewOtherUserMessageComponentBinding binding;
    private final int editedAppearance;
    private final int mentionAppearance;
    @NonNull
    private final TextUIConfig mentionedCurrentUserUIConfig;

    @NonNull
    @Override
    public SbViewOtherUserMessageComponentBinding getBinding() {
        return binding;
    }

    public OtherUserMessageView(@NonNull Context context) {
        this(context, null);
    }

    public OtherUserMessageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_widget_other_user_message);
    }

    public OtherUserMessageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageView_User, defStyle, 0);
        try {
            this.binding = SbViewOtherUserMessageComponentBinding.inflate(LayoutInflater.from(getContext()), this, true);
            int timeAppearance = a.getResourceId(R.styleable.MessageView_User_sb_message_time_text_appearance, R.style.SendbirdCaption4OnLight03);
            int nicknameAppearance = a.getResourceId(R.styleable.MessageView_User_sb_message_sender_name_text_appearance, R.style.SendbirdCaption1OnLight02);
            int messageAppearance = a.getResourceId(R.styleable.MessageView_User_sb_message_other_text_appearance, R.style.SendbirdBody3OnLight01);
            int messageBackground = a.getResourceId(R.styleable.MessageView_User_sb_message_other_background, R.drawable.sb_shape_chat_bubble);
            ColorStateList messageBackgroundTint = a.getColorStateList(R.styleable.MessageView_User_sb_message_other_background_tint);
            int emojiReactionListBackground = a.getResourceId(R.styleable.MessageView_User_sb_message_emoji_reaction_list_background, R.drawable.sb_shape_chat_bubble_reactions_light);
            int ogtagBackground = a.getResourceId(R.styleable.MessageView_User_sb_message_other_ogtag_background, R.drawable.sb_message_og_background);
            ColorStateList ogtagBackgroundTint = a.getColorStateList(R.styleable.MessageView_User_sb_message_other_ogtag_background_tint);
            ColorStateList linkTextColor = a.getColorStateList(R.styleable.MessageView_User_sb_message_other_link_text_color);
            int clickedLinkBackgroundColor = a.getResourceId(R.styleable.MessageView_User_sb_message_other_clicked_link_background_color, R.color.primary_100);
            this.editedAppearance = a.getResourceId(R.styleable.MessageView_User_sb_message_other_edited_mark_text_appearance, R.style.SendbirdBody3OnLight02);
            this.mentionAppearance = a.getResourceId(R.styleable.MessageView_User_sb_message_other_mentioned_text_appearance, R.style.SendbirdMentionLightOther);
            int mentionedCurrentUserTextBackground = a.getResourceId(R.styleable.MessageView_User_sb_message_mentioned_current_user_text_background, R.color.highlight);
            int mentionedCurrentUserAppearance = a.getResourceId(R.styleable.MessageView_User_sb_message_mentioned_current_user_text_appearance, R.style.MentionedCurrentUserMessage);
            this.mentionedCurrentUserUIConfig = new TextUIConfig();
            this.mentionedCurrentUserUIConfig.mergeFromTextAppearance(context, mentionedCurrentUserAppearance, mentionedCurrentUserTextBackground);

            binding.tvMessage.setTextAppearance(context, messageAppearance);
            binding.tvMessage.setLinkTextColor(linkTextColor);
            binding.contentPanel.setBackground(DrawableUtils.setTintList(context, messageBackground, messageBackgroundTint));
            binding.tvSentAt.setTextAppearance(context, timeAppearance);
            binding.tvNickname.setTextAppearance(context, nicknameAppearance);
            binding.emojiReactionListBackground.setBackgroundResource(emojiReactionListBackground);
            binding.ogtagBackground.setBackground(DrawableUtils.setTintList(context, ogtagBackground, ogtagBackgroundTint));
            binding.ovOgtag.setBackground(DrawableUtils.setTintList(context, ogtagBackground, ogtagBackgroundTint));

            binding.tvMessage.setOnClickListener(v -> binding.contentPanel.performClick());
            binding.tvMessage.setOnLongClickListener(v -> binding.contentPanel.performLongClick());
            binding.tvMessage.setOnLinkLongClickListener((v, link) -> binding.contentPanel.performLongClick());
            binding.tvMessage.setClickedLinkBackgroundColor(context.getResources().getColor(clickedLinkBackgroundColor));
            binding.ovOgtag.setOnLongClickListener(v -> binding.contentPanel.performLongClick());
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
    public void drawMessage(@NonNull GroupChannel channel, @NonNull BaseMessage message, @NonNull MessageGroupType messageGroupType) {
        boolean sendingState = message.getSendingStatus() == SendingStatus.SUCCEEDED;
        boolean hasOgTag = message.getOgMetaData() != null;
        boolean hasReaction = message.getReactions() != null && message.getReactions().size() > 0;
        boolean showProfile = messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE || messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL;
        boolean showNickname = (messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE || messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD) && !MessageUtils.hasParentMessage(message);

        binding.ivProfileView.setVisibility(showProfile ? View.VISIBLE : View.INVISIBLE);
        binding.tvNickname.setVisibility(showNickname ? View.VISIBLE : View.GONE);
        binding.emojiReactionListBackground.setVisibility(hasReaction ? View.VISIBLE : View.GONE);
        binding.rvEmojiReactionList.setVisibility(hasReaction ? View.VISIBLE : View.GONE);
        binding.ogtagBackground.setVisibility(hasOgTag ? View.VISIBLE : View.GONE);
        binding.ovOgtag.setVisibility(hasOgTag ? View.VISIBLE : View.GONE);
        binding.tvSentAt.setVisibility((sendingState && (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE)) ? View.VISIBLE : View.INVISIBLE);
        binding.tvSentAt.setText(DateUtils.formatTime(getContext(), message.getCreatedAt()));

        if (messageUIConfig != null) {
            messageUIConfig.getOtherEditedTextMarkUIConfig().mergeFromTextAppearance(getContext(), editedAppearance);
            messageUIConfig.getOtherMentionUIConfig().mergeFromTextAppearance(getContext(), mentionAppearance);
        }

        ViewUtils.drawTextMessage(binding.tvMessage, message, messageUIConfig, mentionedCurrentUserUIConfig);
        ViewUtils.drawNickname(binding.tvNickname, message);
        ViewUtils.drawOgtag(binding.ovOgtag, message.getOgMetaData());
        ViewUtils.drawReactionEnabled(binding.rvEmojiReactionList, channel);
        ViewUtils.drawProfile(binding.ivProfileView, message);

        int paddingTop = getResources().getDimensionPixelSize((messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) ? R.dimen.sb_size_1 : R.dimen.sb_size_8);
        int paddingBottom = getResources().getDimensionPixelSize((messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) ? R.dimen.sb_size_1 : R.dimen.sb_size_8);
        binding.root.setPadding(binding.root.getPaddingLeft(), paddingTop, binding.root.getPaddingRight(), paddingBottom);

        ViewUtils.drawQuotedMessage(binding.quoteReplyPanel, message);
    }
}
