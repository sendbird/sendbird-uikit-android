package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.viewbinding.ViewBinding
import com.sendbird.android.message.BaseMessage

abstract class BaseQuotedMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    abstract val layout: View
    abstract val binding: ViewBinding
    abstract fun drawQuotedMessage(message: BaseMessage?)
}
