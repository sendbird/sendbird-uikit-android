package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.MessageForm
import com.sendbird.android.message.SendingStatus
import com.sendbird.uikit.R
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.activities.adapter.FormItemAdapter
import com.sendbird.uikit.consts.MessageGroupType
import com.sendbird.uikit.databinding.SbViewFormMessageComponentBinding
import com.sendbird.uikit.internal.extensions.setAppearance
import com.sendbird.uikit.model.MessageListUIParams
import com.sendbird.uikit.utils.DrawableUtils
import com.sendbird.uikit.utils.MessageUtils
import com.sendbird.uikit.utils.ViewUtils

internal const val MESSAGE_FORM_VERSION = 1

internal class FormMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_other_user_message
) : BaseMessageView(context, attrs, defStyle) {
    override val binding: SbViewFormMessageComponentBinding
    private val editedAppearance: Int
    private val sentAtAppearance: Int
    private val nicknameAppearance: Int
    private val messageAppearance: Int
    private val formItemAdapter: FormItemAdapter = FormItemAdapter {
        setSubmitButtonEnabled(if (messageForm?.isSubmitted == true) false else it)
    }
    private var messageForm: MessageForm? = null

    override val layout: View
        get() = binding.root

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_User, defStyle, 0)
        try {
            binding = SbViewFormMessageComponentBinding.inflate(LayoutInflater.from(getContext()), this, true)
            sentAtAppearance = a.getResourceId(
                R.styleable.MessageView_User_sb_message_time_text_appearance,
                R.style.SendbirdCaption4OnLight03
            )
            nicknameAppearance = a.getResourceId(
                R.styleable.MessageView_User_sb_message_sender_name_text_appearance,
                R.style.SendbirdCaption1OnLight02
            )
            editedAppearance = a.getResourceId(
                R.styleable.MessageView_User_sb_message_other_edited_mark_text_appearance,
                R.style.SendbirdBody3OnLight02
            )
            messageAppearance = a.getResourceId(
                R.styleable.MessageView_User_sb_message_other_text_appearance,
                R.style.SendbirdBody3OnLight01
            )
            val messageBackground = a.getResourceId(
                R.styleable.MessageView_User_sb_message_other_background,
                R.drawable.sb_shape_chat_bubble
            )
            val messageBackgroundTint =
                a.getColorStateList(R.styleable.MessageView_User_sb_message_other_background_tint)

            binding.contentPanel.background =
                DrawableUtils.setTintList(context, messageBackground, messageBackgroundTint)

            binding.rvFormItems.adapter = formItemAdapter
            binding.rvFormItems.layoutManager = LinearLayoutManager(context)
            binding.rvFormItems.addItemDecoration(
                ItemSpacingDecoration(resources.getDimensionPixelSize(R.dimen.sb_size_12))
            )
            binding.rvFormItems.itemAnimator = null
        } finally {
            a.recycle()
        }
    }

    fun drawFormMessage(message: BaseMessage, messageListUIParams: MessageListUIParams) {
        val messageGroupType = messageListUIParams.messageGroupType
        val isSent = message.sendingStatus == SendingStatus.SUCCEEDED
        val showProfile =
            messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE || messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL
        val showNickname =
            (messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE || messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD) &&
                (!messageListUIParams.shouldUseQuotedView() || !MessageUtils.hasParentMessage(message))
        val showSentAt =
            isSent && (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE)

        binding.ivProfileView.visibility = if (showProfile) VISIBLE else INVISIBLE
        binding.tvNickname.visibility = if (showNickname) VISIBLE else GONE
        binding.tvSentAt.visibility = if (showSentAt) VISIBLE else GONE

        val paddingTop =
            resources.getDimensionPixelSize(if (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        val paddingBottom =
            resources.getDimensionPixelSize(if (messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        binding.root.setPadding(binding.root.paddingLeft, paddingTop, binding.root.paddingRight, paddingBottom)

        messageUIConfig?.let {
            it.otherSentAtTextUIConfig.mergeFromTextAppearance(context, sentAtAppearance)
            it.otherNicknameTextUIConfig.mergeFromTextAppearance(context, nicknameAppearance)
            it.otherMessageBackground?.let { background -> binding.contentPanel.background = background }
            it.otherEditedTextMarkUIConfig.mergeFromTextAppearance(context, editedAppearance)
            it.otherMessageTextUIConfig.mergeFromTextAppearance(context, messageAppearance)
        }

        ViewUtils.drawNickname(binding.tvNickname, message, messageUIConfig, false)
        ViewUtils.drawProfile(binding.ivProfileView, message)
        ViewUtils.drawSentAt(binding.tvSentAt, message, messageUIConfig)

        val form = message.messageForm ?: return
        messageForm = form

        if (messageListUIParams.channelConfig.enableFormTypeMessage && form.version <= MESSAGE_FORM_VERSION) {
            binding.formEnabledLayout.visibility = VISIBLE
            binding.tvMessageFormDisabled.visibility = GONE
            formItemAdapter.setMessageForm(form)
            setSubmitButtonEnabled(!form.isSubmitted)
        } else {
            binding.tvMessageFormDisabled.setAppearance(
                context,
                if (SendbirdUIKit.isDarkMode()) R.style.SendbirdBody3OnDark03 else R.style.SendbirdBody3OnLight03
            )
            binding.formEnabledLayout.visibility = GONE
            binding.tvMessageFormDisabled.visibility = VISIBLE
        }
    }

    private fun setSubmitButtonEnabled(enabled: Boolean) {
        val isDarkMode = SendbirdUIKit.isDarkMode()
        binding.buttonSubmit.isClickable = enabled
        binding.buttonSubmit.isEnabled = enabled
        if (!enabled) {
            binding.buttonSubmit.background = if (isDarkMode) {
                ResourcesCompat.getDrawable(resources, R.drawable.sb_shape_submit_disabled_button_dark, null)
            } else {
                ResourcesCompat.getDrawable(resources, R.drawable.sb_shape_submit_disabled_button_light, null)
            }
            binding.buttonSubmit.setAppearance(
                context,
                if (isDarkMode) R.style.SendbirdButtonOnDark04 else R.style.SendbirdButtonOnLight04
            )
            if (messageForm?.isSubmitted == true) binding.buttonSubmit.text = context.getString(R.string.sb_forms_submitted_successfully)
        } else {
            binding.buttonSubmit.background = if (isDarkMode) {
                ResourcesCompat.getDrawable(resources, R.drawable.sb_shape_submit_button_dark, null)
            } else {
                ResourcesCompat.getDrawable(resources, R.drawable.sb_shape_submit_button_light, null)
            }

            binding.buttonSubmit.setAppearance(
                context,
                if (isDarkMode) R.style.SendbirdButtonOnLight01 else R.style.SendbirdButtonOnDark01
            )
            binding.buttonSubmit.text = context.getString(R.string.sb_forms_submit)
        }
    }

    fun setSubmitButtonClickListener(listener: OnClickListener?) {
        binding.buttonSubmit.setOnClickListener { view ->
            val isSubmittable = formItemAdapter.isSubmittable()
            formItemAdapter.updateValidation()
            if (!isSubmittable) {
                return@setOnClickListener
            }
            listener?.onClick(view)
        }
    }

    private class ItemSpacingDecoration(
        private val spacing: Int
    ) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            val itemCount = state.itemCount
            val isLastPosition = position == (itemCount - 1)

            with(outRect) {
                left = 0
                top = 0
                right = 0
                bottom = if (isLastPosition) 0 else spacing
            }
        }
    }
}
