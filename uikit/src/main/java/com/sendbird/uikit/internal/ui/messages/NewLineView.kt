package com.sendbird.uikit.internal.ui.messages

import android.view.LayoutInflater
import android.widget.FrameLayout
import com.sendbird.uikit.R
import com.sendbird.uikit.databinding.SbViewNewLineBinding
import com.sendbird.uikit.internal.extensions.setAppearance

internal class NewLineView @JvmOverloads constructor(
    context: android.content.Context,
    attrs: android.util.AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    init {
        val binding = SbViewNewLineBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_NewLineView, defStyle, 0)
        val newLineBackground = a.getResourceId(R.styleable.MessageView_NewLineView_sb_new_line_background, R.color.primary_main)
        val newLineTextAppearance = a.getResourceId(
            R.styleable.MessageView_NewLineView_sb_new_line_text_appearance,
            R.style.SendbirdCaption3Primary300
        )
        binding.vLineLeft.setBackgroundResource(newLineBackground)
        binding.vLineRight.setBackgroundResource(newLineBackground)
        binding.tvNewMessage.setAppearance(context, newLineTextAppearance)
    }
}
