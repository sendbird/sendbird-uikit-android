package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.VisibleForTesting
import com.sendbird.android.channel.TemplateContainerOptions
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FeedbackStatus
import com.sendbird.uikit.R
import com.sendbird.uikit.consts.ReplyType
import com.sendbird.uikit.databinding.SbViewOtherTemplateMessageComponentBinding
import com.sendbird.uikit.interfaces.OnItemClickListener
import com.sendbird.uikit.interfaces.OnMessageTemplateActionHandler
import com.sendbird.uikit.internal.extensions.ERR_MESSAGE_TEMPLATE_NOT_APPLICABLE
import com.sendbird.uikit.internal.extensions.createFallbackViewParams
import com.sendbird.uikit.internal.extensions.createTemplateMessageLoadingView
import com.sendbird.uikit.internal.extensions.drawFeedback
import com.sendbird.uikit.internal.extensions.hasParentMessage
import com.sendbird.uikit.internal.extensions.isSuggestedRepliesVisible
import com.sendbird.uikit.internal.extensions.messageTemplateParams
import com.sendbird.uikit.internal.extensions.messageTemplateStatus
import com.sendbird.uikit.internal.extensions.saveParamsFromTemplate
import com.sendbird.uikit.internal.extensions.shouldShowSuggestedReplies
import com.sendbird.uikit.internal.extensions.toContextThemeWrapper
import com.sendbird.uikit.internal.interfaces.OnFeedbackRatingClickListener
import com.sendbird.uikit.internal.model.templates.MessageTemplateStatus
import com.sendbird.uikit.internal.utils.TemplateViewCachePool
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit.model.MessageListUIParams
import com.sendbird.uikit.utils.ViewUtils

internal class OtherTemplateMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : BaseMessageView(context, attrs, defStyle) {
    override val binding: SbViewOtherTemplateMessageComponentBinding = SbViewOtherTemplateMessageComponentBinding.inflate(
        LayoutInflater.from(context.toContextThemeWrapper(defStyle)), this, true
    )
    override val layout: View
        get() = binding.root
    private val sentAtAppearance: Int
    private val nicknameAppearance: Int
    var onFeedbackRatingClickListener: OnFeedbackRatingClickListener? = null
    var onSuggestedRepliesClickListener: OnItemClickListener<String>? = null

    private val suggestedRepliesViewStub: SuggestedRepliesView? by lazy {
        binding.suggestedRepliesViewStub.inflate() as? SuggestedRepliesView
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView, defStyle, 0)
        try {
            sentAtAppearance = a.getResourceId(
                R.styleable.MessageView_sb_message_time_text_appearance,
                R.style.SendbirdCaption4OnLight03
            )
            nicknameAppearance = a.getResourceId(
                R.styleable.MessageView_sb_message_sender_name_text_appearance,
                R.style.SendbirdCaption1OnLight02
            )
        } finally {
            a.recycle()
        }
    }

    fun drawMessage(message: BaseMessage, params: MessageListUIParams, viewCachePool: TemplateViewCachePool, handler: OnMessageTemplateActionHandler?) {
        val messageContainerOptions = message.templateMessageData?.containerOptions ?: TemplateContainerOptions()
        val showProfile = messageContainerOptions.profile
        val showNickname = messageContainerOptions.nickname
        val shouldShowSentAt = messageContainerOptions.time

        binding.ivProfileView.visibility = if (showProfile) VISIBLE else INVISIBLE
        binding.tvNickname.visibility = if (showNickname) VISIBLE else GONE
        binding.tvSentAt.visibility = if (shouldShowSentAt) VISIBLE else GONE

        messageUIConfig?.let {
            it.otherSentAtTextUIConfig.mergeFromTextAppearance(context, sentAtAppearance)
            it.otherNicknameTextUIConfig.mergeFromTextAppearance(context, nicknameAppearance)
            val background = it.otherMessageBackground
            if (background != null) binding.messageTemplateView.background = background
        }
        if (showNickname) ViewUtils.drawNickname(binding.tvNickname, message, messageUIConfig, false)
        if (showProfile) ViewUtils.drawProfile(binding.ivProfileView, message)
        if (shouldShowSentAt) ViewUtils.drawSentAt(binding.tvSentAt, message, messageUIConfig)
        drawTemplateView(message, viewCachePool, handler)

        val shouldShowFeedback = params.channelConfig.enableFeedback &&
            !(message.hasParentMessage() && params.channelConfig.replyType == ReplyType.THREAD)

        if (shouldShowFeedback && message.myFeedbackStatus != FeedbackStatus.NOT_APPLICABLE) {
            binding.feedback.visibility = View.VISIBLE
            binding.feedback.drawFeedback(message) { _, rating ->
                onFeedbackRatingClickListener?.onFeedbackClicked(message, rating)
            }
        } else {
            binding.feedback.visibility = View.GONE
        }

        val shouldShowSuggestedReplies = message.shouldShowSuggestedReplies
        message.isSuggestedRepliesVisible = shouldShowSuggestedReplies
        if (shouldShowSuggestedReplies) {
            suggestedRepliesViewStub?.let {
                it.visibility = View.VISIBLE
                it.drawSuggestedReplies(message, params.channelConfig.suggestedRepliesDirection)
                it.onItemClickListener = OnItemClickListener<String> { v, position, data ->
                    onSuggestedRepliesClickListener?.onItemClick(v, position, data)
                }
            }
        } else {
            suggestedRepliesViewStub?.visibility = View.GONE
        }
    }

    @VisibleForTesting
    internal fun drawTemplateView(
        message: BaseMessage,
        viewCachePool: TemplateViewCachePool,
        handler: OnMessageTemplateActionHandler?
    ) {
        Logger.d("drawTemplateView() messageId = ${message.messageId}, status = ${message.messageTemplateStatus}")
        val params = when (val status = message.messageTemplateStatus) {
            null, MessageTemplateStatus.NOT_APPLICABLE -> {
                Logger.e("MessageTemplateStatus should not be null or NOT_APPLICABLE. messageId = ${message.messageId}, status = $status")
                val errorMessage = context.getString(R.string.sb_text_template_message_fallback_error).format(
                    ERR_MESSAGE_TEMPLATE_NOT_APPLICABLE
                )
                context.createFallbackViewParams(errorMessage)
            }
            MessageTemplateStatus.FAILED_TO_PARSE, MessageTemplateStatus.FAILED_TO_FETCH -> {
                context.createFallbackViewParams(message)
            }
            MessageTemplateStatus.LOADING -> {
                val loadingView = context.createTemplateMessageLoadingView()
                binding.messageTemplateView.removeAllViews()
                binding.messageTemplateView.addView(loadingView)
                return
            }
            MessageTemplateStatus.CACHED -> {
                // Params could be null if it's failed to parse template (e.g. there's a parent template but no child templates)
                val params = message.messageTemplateParams ?: run {
                    message.saveParamsFromTemplate()
                    message.messageTemplateParams
                }

                params ?: context.createFallbackViewParams(message)
            }
        }

        val cacheKey = "${message.messageId}_${message.messageTemplateStatus}"
        binding.messageTemplateView.draw(
            params,
            cacheKey,
            viewCachePool,
            onViewCreated = { v, p ->
                p.action?.register(v, message) { view, action, message ->
                    handler?.onHandleAction(view, action, message)
                }
            },
            onChildViewCreated = { v, p ->
                p.action?.register(v, message) { view, action, message ->
                    handler?.onHandleAction(view, action, message)
                }
            }
        )
    }
}
