package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.content.res.ColorStateList
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FileMessage
import com.sendbird.uikit.R
import com.sendbird.uikit.consts.StringSet
import com.sendbird.uikit.databinding.SbViewMessagePreviewBinding
import com.sendbird.uikit.internal.extensions.getDisplayMessage
import com.sendbird.uikit.internal.extensions.setAppearance
import com.sendbird.uikit.utils.DateUtils
import com.sendbird.uikit.utils.DrawableUtils
import com.sendbird.uikit.utils.MessageUtils
import com.sendbird.uikit.utils.ViewUtils
import java.util.Locale

internal class MessagePreview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_message_preview
) : FrameLayout(context, attrs, defStyle) {
    private val binding: SbViewMessagePreviewBinding
    private val metaphorTintColor: ColorStateList?
    private var messageTextAppearance = 0
    private var messageFileTextAppearance = 0

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessagePreview, defStyle, 0)
        try {
            binding = SbViewMessagePreviewBinding.inflate(LayoutInflater.from(getContext()))
            addView(binding.getRoot(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val background = a.getResourceId(
                R.styleable.MessagePreview_sb_message_preview_background,
                R.drawable.selector_rectangle_light
            )
            val userNameAppearance = a.getResourceId(
                R.styleable.MessagePreview_sb_message_preview_username_text_appearance,
                R.style.SendbirdSubtitle1OnLight01
            )
            messageTextAppearance = a.getResourceId(
                R.styleable.MessagePreview_sb_message_preview_message_text_appearance,
                R.style.SendbirdBody3OnLight03
            )
            messageFileTextAppearance = a.getResourceId(
                R.styleable.MessagePreview_sb_message_preview_message_file_text_appearance,
                R.style.SendbirdBody3OnLight01
            )
            val sentAtTextAppearance = a.getResourceId(
                R.styleable.MessagePreview_sb_message_preview_sent_at_text_appearance,
                R.style.SendbirdCaption2OnLight02
            )
            val dividerColor =
                a.getResourceId(R.styleable.MessagePreview_sb_message_preview_divider_color, R.color.onlight_04)
            val metaphorBackgroundColor =
                a.getResourceId(R.styleable.MessagePreview_sb_message_preview_message_metaphor_background_color, 0)
            metaphorTintColor =
                a.getColorStateList(R.styleable.MessagePreview_sb_message_preview_message_metaphor_icon_tint_color)
            binding.root.setBackgroundResource(background)
            binding.tvUserName.setAppearance(context, userNameAppearance)
            binding.tvMessage.setAppearance(context, messageTextAppearance)
            binding.tvSentAt.setAppearance(context, sentAtTextAppearance)
            binding.ivDivider.setBackgroundResource(dividerColor)
            binding.ivIcon.background = DrawableUtils.createRoundedRectangle(
                resources.getDimension(R.dimen.sb_size_8), ContextCompat.getColor(context, metaphorBackgroundColor)
            )
        } finally {
            a.recycle()
        }
    }

    fun drawMessage(message: BaseMessage) {
        val context = binding.tvSentAt.context
        ViewUtils.drawProfile(binding.ivProfile, message)
        binding.tvUserName.text = message.sender?.nickname ?: ""
        binding.tvSentAt.text = DateUtils.formatDateTime(context, message.createdAt)
        if (message is FileMessage) {
            if (MessageUtils.isVoiceMessage(message)) {
                binding.tvMessage.apply {
                    isSingleLine = true
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    setAppearance(context, messageTextAppearance)
                    text = context.getString(R.string.sb_text_voice_message)
                }
                binding.ivIcon.visibility = GONE
            } else {
                val icon = getIconDrawable(message.type)
                binding.tvMessage.apply {
                    isSingleLine = true
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.MIDDLE
                    setAppearance(context, messageFileTextAppearance)
                }
                metaphorTintColor?.let {
                    binding.ivIcon.setImageDrawable(
                        DrawableUtils.setTintList(
                            binding.ivIcon.context,
                            icon,
                            metaphorTintColor
                        )
                    )
                } ?: binding.ivIcon.setImageDrawable(AppCompatResources.getDrawable(binding.ivIcon.context, icon))
                binding.ivIcon.setImageResource(icon)
                binding.ivIcon.visibility = VISIBLE
                binding.tvMessage.text = message.name
            }
        } else {
            binding.tvMessage.apply {
                isSingleLine = false
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END
                setAppearance(context, messageTextAppearance)
                text = message.getDisplayMessage()
            }
            binding.ivIcon.visibility = GONE
        }
    }

    private fun getIconDrawable(mimeType: String): Int {
        return if (mimeType.lowercase(Locale.getDefault()).contains(StringSet.image)) {
            if (mimeType.endsWith(StringSet.gif)) {
                R.drawable.icon_gif
            } else {
                R.drawable.icon_photo
            }
        } else if (mimeType.lowercase(Locale.getDefault()).contains(StringSet.video)) {
            R.drawable.icon_play
        } else if (mimeType.lowercase(Locale.getDefault()).contains(StringSet.audio)) {
            R.drawable.icon_file_audio
        } else {
            R.drawable.icon_file_document
        }
    }
}
