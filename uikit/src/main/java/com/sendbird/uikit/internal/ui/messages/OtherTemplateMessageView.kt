package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.SendingStatus
import com.sendbird.uikit.R
import com.sendbird.uikit.consts.MessageGroupType
import com.sendbird.uikit.consts.ReplyType
import com.sendbird.uikit.databinding.SbViewOtherTemplateMessageComponentBinding
import com.sendbird.uikit.interfaces.OnMessageTemplateActionHandler
import com.sendbird.uikit.internal.extensions.ERR_MESSAGE_TEMPLATE_NOT_APPLICABLE
import com.sendbird.uikit.internal.extensions.MessageTemplateContainerType
import com.sendbird.uikit.internal.extensions.createTemplateMessageLoadingView
import com.sendbird.uikit.internal.extensions.drawFeedback
import com.sendbird.uikit.internal.extensions.hasParentMessage
import com.sendbird.uikit.internal.extensions.messageTemplateContainerType
import com.sendbird.uikit.internal.extensions.saveParamsFromTemplate
import com.sendbird.uikit.internal.extensions.toContextThemeWrapper
import com.sendbird.uikit.internal.interfaces.OnFeedbackRatingClickListener
import com.sendbird.uikit.internal.model.template_messages.Params
import com.sendbird.uikit.internal.model.template_messages.ViewType
import com.sendbird.uikit.internal.extensions.createFallbackViewParams
import com.sendbird.uikit.internal.model.templates.MessageTemplateStatus
import com.sendbird.uikit.internal.extensions.messageTemplateParams
import com.sendbird.uikit.internal.extensions.messageTemplateStatus
import com.sendbird.uikit.internal.utils.TemplateViewCachePool
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit.model.MessageListUIParams
import com.sendbird.uikit.utils.MessageUtils
import com.sendbird.uikit.utils.ViewUtils

internal class OtherTemplateMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : BaseMessageView(context, attrs, defStyle) {
    override val binding: SbViewOtherTemplateMessageComponentBinding
    override val layout: View
        get() = binding.root
    private val sentAtAppearance: Int
    private val nicknameAppearance: Int
    var onFeedbackRatingClickListener: OnFeedbackRatingClickListener? = null

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView, defStyle, 0)
        try {
            binding = SbViewOtherTemplateMessageComponentBinding.inflate(
                LayoutInflater.from(context.toContextThemeWrapper(defStyle)), this, true
            )
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
        val messageGroupType = params.messageGroupType
        val isSent = message.sendingStatus == SendingStatus.SUCCEEDED
        val showProfile =
            messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE || messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL
        val showNickname =
            (messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE || messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD) &&
                (!params.shouldUseQuotedView() || !MessageUtils.hasParentMessage(message))

        binding.ivProfileView.visibility = if (showProfile) VISIBLE else INVISIBLE
        binding.tvNickname.visibility = if (showNickname) VISIBLE else GONE
        val shouldShowSentAt = isSent && (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE)
        messageUIConfig?.let {
            it.otherSentAtTextUIConfig.mergeFromTextAppearance(context, sentAtAppearance)
            it.otherNicknameTextUIConfig.mergeFromTextAppearance(context, nicknameAppearance)
            val background = it.otherMessageBackground
            if (background != null) binding.messageTemplateView.background = background
        }
        ViewUtils.drawNickname(binding.tvNickname, message, messageUIConfig, false)
        ViewUtils.drawProfile(binding.ivProfileView, message)
        ViewUtils.drawSentAt(binding.tvSentAt, message, messageUIConfig)
        ViewUtils.drawSentAt(binding.tvSentAtForWideContainer, message, messageUIConfig)
        val paddingTop =
            resources.getDimensionPixelSize(if (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        val paddingBottom =
            resources.getDimensionPixelSize(if (messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        binding.root.setPadding(binding.root.paddingLeft, paddingTop, binding.root.paddingRight, paddingBottom)
        drawTemplateView(message, viewCachePool, shouldShowSentAt, handler)

        val shouldHideFeedback = !params.channelConfig.enableFeedback ||
            (message.hasParentMessage() && params.channelConfig.replyType == ReplyType.THREAD)

        binding.feedback.drawFeedback(message, shouldHideFeedback) { _, rating ->
            onFeedbackRatingClickListener?.onFeedbackClicked(message, rating)
        }
    }

    private fun drawTemplateView(
        message: BaseMessage,
        viewCachePool: TemplateViewCachePool,
        shouldShowSentAt: Boolean,
        handler: OnMessageTemplateActionHandler?
    ) {
        Logger.d("drawTemplateView() messageId = ${message.messageId}, status = ${message.messageTemplateStatus}")
        val params = when (val status = message.messageTemplateStatus) {
            null, MessageTemplateStatus.NOT_APPLICABLE -> {
                Logger.e("MessageTemplateStatus should not be null or NOT_APPLICABLE. messageId = ${message.messageId}, status = $status")
                val errorMessage = context.getString(R.string.sb_text_template_message_fallback_error).format(ERR_MESSAGE_TEMPLATE_NOT_APPLICABLE)
                context.createFallbackViewParams(errorMessage)
            }
            MessageTemplateStatus.LOADING -> {
                changeContainerType(MessageTemplateContainerType.DEFAULT, shouldShowSentAt)
                val loadingView = context.createTemplateMessageLoadingView()
                binding.messageTemplateView.removeAllViews()
                binding.messageTemplateView.addView(loadingView)
                return
            }
            MessageTemplateStatus.CACHED -> {
                // Params could be null if it's failed to parse template (e.g. there's a parent template but no child templates)
                val params = message.messageTemplateParams ?: kotlin.run {
                    message.saveParamsFromTemplate()
                    message.messageTemplateParams
                }

                val containerType = when {
                    params == null -> MessageTemplateContainerType.DEFAULT
                    params.hasCarouselView() -> MessageTemplateContainerType.CAROUSEL
                    else -> message.messageTemplateContainerType
                }

                changeContainerType(containerType, shouldShowSentAt, params == null)
                params ?: context.createFallbackViewParams(message)
            }
            MessageTemplateStatus.FAILED_TO_PARSE, MessageTemplateStatus.FAILED_TO_FETCH -> {
                changeContainerType(MessageTemplateContainerType.DEFAULT, shouldShowSentAt)
                context.createFallbackViewParams(message)
            }
        }

        val cacheKey = "${message.messageId}_${message.messageTemplateStatus}"
        binding.messageTemplateView.draw(
            params,
            cacheKey,
            viewCachePool,
            onViewCreated = { view, params ->
                params.action?.register(view, message) { view, action, message ->
                    handler?.onHandleAction(view, action, message)
                }
            },
            onChildViewCreated = { view, params ->
                params.action?.register(view, message) { view, action, message ->
                    handler?.onHandleAction(view, action, message)
                }
            }
        )
    }

    private fun changeContainerType(type: MessageTemplateContainerType, shouldShowSentAt: Boolean, widthWrapContent: Boolean = true) {
        setContentPanelConstraintByType(type, widthWrapContent)
        val radius = when (type) {
            MessageTemplateContainerType.CAROUSEL -> 0F
            else -> context.resources.getDimensionPixelSize(R.dimen.sb_size_12).toFloat()
        }
        setContentPanelRadius(radius)
        setSentAtVisibility(type, shouldShowSentAt)
    }

    private fun setContentPanelConstraintByType(type: MessageTemplateContainerType, widthWrapContent: Boolean = true) {
        val margin = context.resources.getDimensionPixelSize(R.dimen.sb_size_12)
        val defaultWidth = if (widthWrapContent) {
            ConstraintSet.WRAP_CONTENT
        } else {
            context.resources.getDimensionPixelSize(R.dimen.sb_message_max_width)
        }

        val contentPanelId = binding.messageTemplateView.id
        binding.root.changeConstraintSet { set ->
            when (type) {
                MessageTemplateContainerType.DEFAULT -> {
                    set.constrainWidth(contentPanelId, defaultWidth)
                    set.connect(contentPanelId, ConstraintSet.START, binding.profileRightPadding.id, ConstraintSet.END, 0)
                    set.clear(contentPanelId, ConstraintSet.END)
                }
                MessageTemplateContainerType.WIDE -> {
                    set.constrainWidth(contentPanelId, 0)
                    set.connect(contentPanelId, ConstraintSet.START, binding.profileRightPadding.id, ConstraintSet.END, 0)
                    set.connect(contentPanelId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, margin)
                }
                MessageTemplateContainerType.CAROUSEL -> {
                    set.constrainWidth(contentPanelId, ConstraintSet.MATCH_CONSTRAINT)
                    set.connect(contentPanelId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
                    set.connect(contentPanelId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
                }
            }
        }
    }

    private fun setContentPanelRadius(radius: Float) {
        binding.messageTemplateView.radius = radius
    }

    private fun setSentAtVisibility(containerType: MessageTemplateContainerType, shouldShowSentAt: Boolean) {
        if (shouldShowSentAt) {
            when (containerType) {
                MessageTemplateContainerType.DEFAULT -> {
                    binding.tvSentAt.visibility = VISIBLE
                    binding.tvSentAtForWideContainer.visibility = INVISIBLE
                }
                MessageTemplateContainerType.WIDE, MessageTemplateContainerType.CAROUSEL -> {
                    binding.tvSentAt.visibility = INVISIBLE
                    binding.tvSentAtForWideContainer.visibility = VISIBLE
                }
            }
        } else {
            binding.tvSentAt.visibility = INVISIBLE
            binding.tvSentAtForWideContainer.visibility = INVISIBLE
        }
    }

    private fun ConstraintLayout.changeConstraintSet(block: (ConstraintSet) -> Unit) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(this)
        block(constraintSet)
        constraintSet.applyTo(this)
    }
}

private fun Params.hasCarouselView(): Boolean {
    return body.items.any { it.type == ViewType.CarouselView }
}
