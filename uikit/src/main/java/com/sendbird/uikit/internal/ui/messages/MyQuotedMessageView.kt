package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FileMessage
import com.sendbird.android.message.UserMessage
import com.sendbird.uikit.R
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.consts.StringSet
import com.sendbird.uikit.databinding.SbViewMyQuotedMessageBinding
import com.sendbird.uikit.internal.extensions.setAppearance
import com.sendbird.uikit.utils.DrawableUtils
import com.sendbird.uikit.utils.UserUtils
import com.sendbird.uikit.utils.ViewUtils
import java.util.Locale

internal class MyQuotedMessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.sb_widget_my_message
) : BaseQuotedMessageView(context, attrs, defStyleAttr) {
    override val binding: SbViewMyQuotedMessageBinding
    override val layout: View
        get() = binding.root

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView, defStyleAttr, 0)
        try {
            binding = SbViewMyQuotedMessageBinding.inflate(LayoutInflater.from(getContext()), this, true)
            val backgroundResId = a.getResourceId(
                R.styleable.MessageView_sb_quoted_message_me_background,
                R.drawable.sb_shape_chat_bubble
            )
            val backgroundTint = a.getColorStateList(R.styleable.MessageView_sb_quoted_message_me_background_tint)
            val titleIconId =
                a.getResourceId(R.styleable.MessageView_sb_quoted_message_me_title_icon, R.drawable.icon_reply_filled)
            val titleIconTint = a.getColorStateList(R.styleable.MessageView_sb_quoted_message_me_title_icon_tint)
            val titleTextAppearance = a.getResourceId(
                R.styleable.MessageView_sb_quoted_message_me_title_text_appearance,
                R.style.SendbirdCaption1OnLight01
            )
            val messageIconTint = a.getColorStateList(R.styleable.MessageView_sb_quoted_message_me_file_icon_tint)
            val messageTextAppearance = a.getResourceId(
                R.styleable.MessageView_sb_quoted_message_me_text_appearance,
                R.style.SendbirdCaption2OnLight03
            )
            if (backgroundTint != null) {
                binding.quoteReplyMessagePanel.background =
                    DrawableUtils.setTintList(context, backgroundResId, backgroundTint.withAlpha(0x80))
            } else {
                binding.quoteReplyMessagePanel.setBackgroundResource(backgroundResId)
            }
            binding.ivQuoteReplyIcon.setImageResource(titleIconId)
            binding.ivQuoteReplyIcon.imageTintList = titleIconTint
            binding.tvQuoteReplyTitle.setAppearance(context, titleTextAppearance)
            binding.tvQuoteReplyMessage.setAppearance(context, messageTextAppearance)
            binding.ivQuoteReplyMessageIcon.imageTintList = messageIconTint
            val bg =
                if (SendbirdUIKit.isDarkMode()) R.drawable.sb_shape_quoted_message_thumbnail_background_dark else R.drawable.sb_shape_quoted_message_thumbnail_background
            binding.ivQuoteReplyThumbnail.setBackgroundResource(bg)
        } finally {
            a.recycle()
        }
    }

    override fun drawQuotedMessage(message: BaseMessage?) {
        binding.quoteReplyPanel.visibility = GONE
        if (message == null) return
        if (message.parentMessage == null) return

        val parentMessage = message.parentMessage
        binding.quoteReplyPanel.visibility = VISIBLE
        binding.quoteReplyMessagePanel.visibility = GONE
        binding.ivQuoteReplyMessageIcon.visibility = GONE
        binding.quoteReplyThumbnailPanel.visibility = GONE
        binding.tvQuoteReplyTitle.text = String.format(
            context.getString(R.string.sb_text_replied_to),
            context.getString(R.string.sb_text_you),
            UserUtils.getDisplayName(context, parentMessage?.sender, true)
        )
        binding.ivQuoteReplyThumbnailOveray.visibility = GONE
        val requestListener: RequestListener<Drawable?> = object : RequestListener<Drawable?> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any,
                target: Target<Drawable?>,
                isFirstResource: Boolean
            ): Boolean {
                binding.ivQuoteReplyThumbnailOveray.visibility = GONE
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any,
                target: Target<Drawable?>,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                binding.ivQuoteReplyThumbnailOveray.visibility = VISIBLE
                return false
            }
        }
        when (parentMessage) {
            is UserMessage -> {
                binding.quoteReplyMessagePanel.visibility = VISIBLE
                binding.tvQuoteReplyMessage.text = parentMessage.message
                binding.tvQuoteReplyMessage.isSingleLine = false
                binding.tvQuoteReplyMessage.maxLines = 2
                binding.tvQuoteReplyMessage.ellipsize = TextUtils.TruncateAt.END
            }
            is FileMessage -> {
                val type = parentMessage.type
                binding.ivQuoteReplyThumbnail.radius = resources.getDimensionPixelSize(R.dimen.sb_size_8).toFloat()
                binding.tvQuoteReplyMessage.isSingleLine = true
                binding.tvQuoteReplyMessage.ellipsize = TextUtils.TruncateAt.MIDDLE
                if (type.lowercase(Locale.getDefault()).contains(StringSet.gif)) {
                    binding.quoteReplyThumbnailPanel.visibility = VISIBLE
                    binding.ivQuoteReplyThumbnailIcon.setImageDrawable(
                        DrawableUtils.createOvalIcon(
                            context, R.color.background_50, R.drawable.icon_gif, R.color.onlight_03
                        )
                    )
                    ViewUtils.drawQuotedMessageThumbnail(
                        binding.ivQuoteReplyThumbnail,
                        parentMessage,
                        requestListener
                    )
                } else if (type.lowercase(Locale.getDefault()).contains(StringSet.video)) {
                    binding.quoteReplyThumbnailPanel.visibility = VISIBLE
                    binding.ivQuoteReplyThumbnailIcon.setImageDrawable(
                        DrawableUtils.createOvalIcon(
                            context, R.color.background_50, R.drawable.icon_play, R.color.onlight_03
                        )
                    )
                    ViewUtils.drawQuotedMessageThumbnail(
                        binding.ivQuoteReplyThumbnail,
                        parentMessage,
                        requestListener
                    )
                } else if (type.lowercase(Locale.getDefault()).startsWith(StringSet.audio)) {
                    binding.quoteReplyMessagePanel.visibility = VISIBLE
                    binding.ivQuoteReplyMessageIcon.visibility = VISIBLE
                    binding.ivQuoteReplyMessageIcon.setImageResource(R.drawable.icon_file_audio)
                    binding.tvQuoteReplyMessage.text = parentMessage.name
                } else if (type.startsWith(StringSet.image) && !type.contains(StringSet.svg)) {
                    binding.quoteReplyThumbnailPanel.visibility = VISIBLE
                    binding.ivQuoteReplyThumbnailIcon.setImageResource(android.R.color.transparent)
                    ViewUtils.drawQuotedMessageThumbnail(
                        binding.ivQuoteReplyThumbnail,
                        parentMessage,
                        requestListener
                    )
                } else {
                    binding.quoteReplyMessagePanel.visibility = VISIBLE
                    binding.ivQuoteReplyMessageIcon.visibility = VISIBLE
                    binding.ivQuoteReplyMessageIcon.setImageResource(R.drawable.icon_file_document)
                    binding.tvQuoteReplyMessage.text = parentMessage.name
                }
            }
        }
    }
}
