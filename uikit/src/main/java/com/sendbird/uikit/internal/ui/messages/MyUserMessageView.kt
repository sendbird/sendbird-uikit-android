package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.SendingStatus
import com.sendbird.android.user.User
import com.sendbird.uikit.R
import com.sendbird.uikit.consts.MessageGroupType
import com.sendbird.uikit.databinding.SbViewMyUserMessageComponentBinding
import com.sendbird.uikit.interfaces.OnItemClickListener
import com.sendbird.uikit.internal.extensions.isNewLineMessage
import com.sendbird.uikit.internal.ui.widgets.OnLinkLongClickListener
import com.sendbird.uikit.model.MessageListUIParams
import com.sendbird.uikit.model.TextUIConfig
import com.sendbird.uikit.model.configurations.ChannelConfig
import com.sendbird.uikit.utils.DrawableUtils
import com.sendbird.uikit.utils.ViewUtils

internal class MyUserMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_my_user_message
) : GroupChannelMessageView(context, attrs, defStyle) {
    override val binding: SbViewMyUserMessageComponentBinding
    override val layout: View
        get() = binding.root

    private val editedAppearance: Int
    private val mentionAppearance: Int
    private val mentionedCurrentUserUIConfig: TextUIConfig
    private val sentAtAppearance: Int
    private val messageTextAppearance: Int
    var mentionClickListener: OnItemClickListener<User>? = null

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_User, defStyle, 0)
        try {
            binding = SbViewMyUserMessageComponentBinding.inflate(LayoutInflater.from(getContext()), this, true)
            sentAtAppearance = a.getResourceId(
                R.styleable.MessageView_User_sb_message_time_text_appearance,
                R.style.SendbirdCaption4OnLight03
            )
            messageTextAppearance = a.getResourceId(
                R.styleable.MessageView_User_sb_message_me_text_appearance,
                R.style.SendbirdBody3OnDark01
            )
            val messageBackground =
                a.getResourceId(R.styleable.MessageView_User_sb_message_me_background, R.drawable.sb_shape_chat_bubble)
            val messageBackgroundTint = a.getColorStateList(R.styleable.MessageView_User_sb_message_me_background_tint)
            val emojiReactionListBackground = a.getResourceId(
                R.styleable.MessageView_User_sb_message_emoji_reaction_list_background,
                R.drawable.sb_shape_chat_bubble_reactions_light
            )
            val ogtagBackground = a.getResourceId(
                R.styleable.MessageView_User_sb_message_me_ogtag_background,
                R.drawable.sb_message_og_background
            )
            val ogtagBackgroundTint =
                a.getColorStateList(R.styleable.MessageView_User_sb_message_me_ogtag_background_tint)
            val linkTextColor = a.getColorStateList(R.styleable.MessageView_User_sb_message_me_link_text_color)
            val clickedLinkBackgroundColor = a.getResourceId(
                R.styleable.MessageView_User_sb_message_me_clicked_link_background_color,
                R.color.primary_dark
            )
            editedAppearance = a.getResourceId(
                R.styleable.MessageView_User_sb_message_my_edited_mark_text_appearance,
                R.style.SendbirdBody3OnDark02
            )
            mentionAppearance = a.getResourceId(
                R.styleable.MessageView_User_sb_message_my_mentioned_text_appearance,
                R.style.SendbirdMentionLightMe
            )
            val mentionedCurrentUserTextBackground = a.getResourceId(
                R.styleable.MessageView_User_sb_message_mentioned_current_user_text_background,
                R.color.highlight
            )
            val mentionedCurrentUserAppearance = a.getResourceId(
                R.styleable.MessageView_User_sb_message_mentioned_current_user_text_appearance,
                R.style.MentionedCurrentUserMessage
            )
            mentionedCurrentUserUIConfig = TextUIConfig.Builder(context, mentionedCurrentUserAppearance)
                .setTextBackgroundColor(ContextCompat.getColor(context, mentionedCurrentUserTextBackground))
                .build()
            binding.tvMessage.setLinkTextColor(linkTextColor)
            binding.contentPanel.background =
                DrawableUtils.setTintList(context, messageBackground, messageBackgroundTint)
            binding.emojiReactionListBackground.setBackgroundResource(emojiReactionListBackground)
            binding.ogtagBackground.background =
                DrawableUtils.setTintList(context, ogtagBackground, ogtagBackgroundTint)
            binding.ovOgtag.background =
                DrawableUtils.setTintList(context, ogtagBackground, ogtagBackgroundTint)
            binding.tvMessage.setOnClickListener { binding.contentPanel.performClick() }
            binding.tvMessage.setOnLongClickListener { binding.contentPanel.performLongClick() }
            binding.tvMessage.onLinkLongClickListener = object : OnLinkLongClickListener {
                override fun onLongClick(textView: TextView, link: String): Boolean {
                    return binding.contentPanel.performLongClick()
                }
            }
            binding.tvMessage.clickedLinkBackgroundColor = ContextCompat.getColor(context, clickedLinkBackgroundColor)
            binding.ovOgtag.setOnLongClickListener { binding.contentPanel.performLongClick() }
        } finally {
            a.recycle()
        }
    }

    override fun drawMessage(channel: GroupChannel, message: BaseMessage, params: MessageListUIParams) {
        val messageGroupType = params.messageGroupType
        val isSent = message.sendingStatus == SendingStatus.SUCCEEDED
        val enableOgTag = message.ogMetaData != null && ChannelConfig.getEnableOgTag(params.channelConfig)
        val enableMention = params.channelConfig.enableMention
        val enableReactions =
            message.reactions.isNotEmpty() && ChannelConfig.getEnableReactions(params.channelConfig, channel)
        val enableMarkdown = params.channelConfig.enableMarkdownForUserMessage
        binding.newLineView.visibility = if (message.isNewLineMessage) VISIBLE else GONE
        binding.emojiReactionListBackground.visibility = if (enableReactions) VISIBLE else GONE
        binding.rvEmojiReactionList.visibility = if (enableReactions) VISIBLE else GONE
        binding.ogtagBackground.visibility = if (enableOgTag) VISIBLE else GONE
        binding.ovOgtag.visibility = if (enableOgTag) VISIBLE else GONE
        binding.tvSentAt.visibility =
            if (isSent && (messageGroupType === MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType === MessageGroupType.GROUPING_TYPE_SINGLE)) VISIBLE else GONE
        binding.ivStatus.drawStatus(message, channel, params.shouldUseMessageReceipt())

        messageUIConfig?.let {
            it.myMessageTextUIConfig.mergeFromTextAppearance(context, messageTextAppearance)
            it.myEditedTextMarkUIConfig.mergeFromTextAppearance(context, editedAppearance)
            it.myMentionUIConfig.mergeFromTextAppearance(context, mentionAppearance)
            it.mySentAtTextUIConfig.mergeFromTextAppearance(context, sentAtAppearance)
            it.myMessageBackground?.let { background -> binding.contentPanel.background = background }
            it.myReactionListBackground?.let { reactionBackground ->
                binding.emojiReactionListBackground.background = reactionBackground
            }
            it.myOgtagBackground?.let { ogtagBackground ->
                binding.ogtagBackground.background = ogtagBackground
                binding.ovOgtag.background = ogtagBackground
            }
            it.linkedTextColor?.let { linkedTextColor -> binding.tvMessage.setLinkTextColor(linkedTextColor) }
        }

        ViewUtils.drawTextMessage(
            binding.tvMessage,
            message,
            messageUIConfig,
            enableMention,
            enableMarkdown,
            mentionedCurrentUserUIConfig,
        ) { view, position, user ->
            mentionClickListener?.onItemClick(view, position, user)
        }
        if (enableOgTag) ViewUtils.drawOgtag(binding.ovOgtag, message.ogMetaData)
        ViewUtils.drawReactionEnabled(binding.rvEmojiReactionList, channel, params.channelConfig)
        ViewUtils.drawSentAt(binding.tvSentAt, message, messageUIConfig)

        val paddingTop =
            resources.getDimensionPixelSize(if (messageGroupType === MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType === MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        val paddingBottom =
            resources.getDimensionPixelSize(if (messageGroupType === MessageGroupType.GROUPING_TYPE_HEAD || messageGroupType === MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        binding.root.setPaddingRelative(binding.root.paddingStart, paddingTop, binding.root.paddingEnd, paddingBottom)
        if (params.shouldUseQuotedView()) {
            ViewUtils.drawQuotedMessage(
                binding.quoteReplyPanel,
                channel,
                message,
                messageUIConfig?.repliedMessageTextUIConfig,
                params
            )
        } else {
            binding.quoteReplyPanel.visibility = GONE
        }
        ViewUtils.drawThreadInfo(binding.threadInfo, message, params)
    }
}
