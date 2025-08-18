package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FileMessage
import com.sendbird.android.message.SendingStatus
import com.sendbird.uikit.R
import com.sendbird.uikit.consts.MessageGroupType
import com.sendbird.uikit.databinding.SbViewMyFileMessageComponentBinding
import com.sendbird.uikit.internal.extensions.isNewLineMessage
import com.sendbird.uikit.internal.extensions.setAppearance
import com.sendbird.uikit.model.MessageListUIParams
import com.sendbird.uikit.model.configurations.ChannelConfig
import com.sendbird.uikit.utils.DrawableUtils
import com.sendbird.uikit.utils.ViewUtils

internal class MyFileMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_my_file_message
) : GroupChannelMessageView(context, attrs, defStyle) {
    override val binding: SbViewMyFileMessageComponentBinding
    override val layout: View
        get() = binding.root

    private val messageTextAppearance: Int
    private val sentAtAppearance: Int

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_File, defStyle, 0)
        try {
            binding = SbViewMyFileMessageComponentBinding.inflate(LayoutInflater.from(context), this, true)
            sentAtAppearance = a.getResourceId(
                R.styleable.MessageView_File_sb_message_time_text_appearance,
                R.style.SendbirdCaption4OnLight03
            )
            messageTextAppearance = a.getResourceId(
                R.styleable.MessageView_File_sb_message_me_text_appearance,
                R.style.SendbirdBody3OnDark01
            )
            val messageBackground =
                a.getResourceId(R.styleable.MessageView_File_sb_message_me_background, R.drawable.sb_shape_chat_bubble)
            val messageBackgroundTint = a.getColorStateList(R.styleable.MessageView_File_sb_message_me_background_tint)
            val emojiReactionListBackground = a.getResourceId(
                R.styleable.MessageView_File_sb_message_emoji_reaction_list_background,
                R.drawable.sb_shape_chat_bubble_reactions_light
            )
            binding.tvSentAt.setAppearance(context, sentAtAppearance)
            binding.tvFileName.setAppearance(context, messageTextAppearance)
            binding.tvFileName.paintFlags = binding.tvFileName.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            binding.contentPanelWithReactions.background =
                DrawableUtils.setTintList(context, messageBackground, messageBackgroundTint)
            binding.emojiReactionListBackground.setBackgroundResource(emojiReactionListBackground)
        } finally {
            a.recycle()
        }
    }

    override fun drawMessage(channel: GroupChannel, message: BaseMessage, params: MessageListUIParams) {
        val fileMessage = message as FileMessage
        val isSent = message.sendingStatus == SendingStatus.SUCCEEDED
        val enableReactions =
            message.reactions.isNotEmpty() && ChannelConfig.getEnableReactions(params.channelConfig, channel)
        val messageGroupType = params.messageGroupType

        binding.newLineView.visibility = if (message.isNewLineMessage) VISIBLE else GONE
        binding.emojiReactionListBackground.visibility = if (enableReactions) VISIBLE else GONE
        binding.rvEmojiReactionList.visibility = if (enableReactions) VISIBLE else GONE
        binding.tvSentAt.visibility =
            if (isSent && (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE)) VISIBLE else GONE
        binding.ivStatus.drawStatus(message, channel, params.shouldUseMessageReceipt())

        messageUIConfig?.let {
            it.myMessageTextUIConfig.mergeFromTextAppearance(context, messageTextAppearance)
            it.mySentAtTextUIConfig.mergeFromTextAppearance(context, sentAtAppearance)
            it.myMessageBackground?.let { background -> binding.contentPanel.background = background }
            it.myReactionListBackground?.let { reactionListBackground ->
                binding.emojiReactionListBackground.background = reactionListBackground
            }
        }

        ViewUtils.drawSentAt(binding.tvSentAt, message, messageUIConfig)
        ViewUtils.drawFilename(binding.tvFileName, fileMessage, messageUIConfig)
        ViewUtils.drawReactionEnabled(binding.rvEmojiReactionList, channel, params.channelConfig)
        ViewUtils.drawFileIcon(binding.ivIcon, fileMessage)

        val paddingTop =
            resources.getDimensionPixelSize(if (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        val paddingBottom =
            resources.getDimensionPixelSize(if (messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
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
