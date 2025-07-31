package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.sendbird.android.channel.ChannelType
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.MultipleFilesMessage
import com.sendbird.android.message.SendingStatus
import com.sendbird.uikit.R
import com.sendbird.uikit.activities.PhotoViewActivity
import com.sendbird.uikit.consts.MessageGroupType
import com.sendbird.uikit.databinding.SbViewMyMultipleFilesMessageComponentBinding
import com.sendbird.uikit.interfaces.OnItemClickListener
import com.sendbird.uikit.internal.extensions.isNewLineMessage
import com.sendbird.uikit.internal.extensions.toContextThemeWrapper
import com.sendbird.uikit.model.MessageListUIParams
import com.sendbird.uikit.model.configurations.ChannelConfig
import com.sendbird.uikit.utils.DrawableUtils
import com.sendbird.uikit.utils.ViewUtils

internal class MyMultipleFilesMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_my_multiple_files_message
) : GroupChannelMessageView(context, attrs, defStyle) {
    override val binding: SbViewMyMultipleFilesMessageComponentBinding
    override val layout: View
        get() = binding.root
    private val sentAtAppearance: Int

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_MultipleFiles, defStyle, 0)
        try {
            binding =
                SbViewMyMultipleFilesMessageComponentBinding.inflate(
                    LayoutInflater.from(
                        context.toContextThemeWrapper(
                            defStyle
                        )
                    ),
                    this, true
                )
            sentAtAppearance = a.getResourceId(
                R.styleable.MessageView_MultipleFiles_sb_message_time_text_appearance,
                R.style.SendbirdCaption4OnLight03
            )
            val messageBackground =
                a.getResourceId(
                    R.styleable.MessageView_MultipleFiles_sb_message_me_background,
                    R.drawable.sb_shape_chat_bubble
                )
            val messageBackgroundTint =
                a.getColorStateList(R.styleable.MessageView_MultipleFiles_sb_message_me_background_tint)
            val emojiReactionListBackground = a.getResourceId(
                R.styleable.MessageView_MultipleFiles_sb_message_emoji_reaction_list_background,
                R.drawable.sb_shape_chat_bubble_reactions_light
            )
            binding.contentPanel.background =
                DrawableUtils.setTintList(context, messageBackground, messageBackgroundTint)
            binding.emojiReactionListBackground.setBackgroundResource(emojiReactionListBackground)
            binding.rvMultipleFilesMessage.setOnLongClickListener { binding.contentPanel.performLongClick() }
        } finally {
            a.recycle()
        }
    }

    override fun drawMessage(channel: GroupChannel, message: BaseMessage, params: MessageListUIParams) {
        if (message !is MultipleFilesMessage) return
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
            it.mySentAtTextUIConfig.mergeFromTextAppearance(context, sentAtAppearance)
            val background = it.myMessageBackground
            val reactionBackground = it.myReactionListBackground
            background?.let { binding.contentPanel.background = background }
            reactionBackground?.let { binding.emojiReactionListBackground.background = reactionBackground }
        }

        ViewUtils.drawSentAt(binding.tvSentAt, message, messageUIConfig)
        ViewUtils.drawReactionEnabled(binding.rvEmojiReactionList, channel, params.channelConfig)

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
        binding.rvMultipleFilesMessage.bind(message)

        binding.rvMultipleFilesMessage.onItemClickListener = OnItemClickListener { _, index, _ ->
            if (message.sendingStatus == SendingStatus.SUCCEEDED) {
                val intent =
                    PhotoViewActivity.newIntent(context, ChannelType.GROUP, message, index)
                context.startActivity(intent)
            } else {
                binding.rvMultipleFilesMessage.performClick()
            }
        }
    }
}
