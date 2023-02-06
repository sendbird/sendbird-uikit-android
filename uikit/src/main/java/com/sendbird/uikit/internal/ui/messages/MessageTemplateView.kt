package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.R
import com.sendbird.uikit.interfaces.OnMessageTemplateActionHandler
import com.sendbird.uikit.internal.model.template_messages.ContentMode
import com.sendbird.uikit.internal.model.template_messages.Orientation
import com.sendbird.uikit.internal.model.template_messages.TemplateViewGenerator
import com.sendbird.uikit.internal.model.template_messages.ViewParams
import com.sendbird.uikit.internal.ui.widgets.Box
import com.sendbird.uikit.internal.ui.widgets.Image
import com.sendbird.uikit.internal.ui.widgets.ImageButton
import com.sendbird.uikit.internal.ui.widgets.Text
import com.sendbird.uikit.internal.ui.widgets.TextButton

internal class MessageTemplateView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_message_template
) : LinearLayout(context, attrs, defStyle) {

    private val defaultMessageTemplateStyle: DefaultMessageTemplateStyle

    init {
        this.orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_MessageTemplate, defStyle, 0)
        try {
            val textAppearanceInTextView = a.getResourceId(
                R.styleable.MessageView_MessageTemplate_sb_message_textview_text_appearance,
                R.style.SendbirdBody3OnLight01
            )
            val buttonBackground = a.getResourceId(
                R.styleable.MessageView_MessageTemplate_sb_message_button_background,
                R.color.onlight_04
            )
            val buttonTextAppearance = a.getResourceId(
                R.styleable.MessageView_MessageTemplate_sb_message_button_text_appearance,
                R.style.SendbirdButtonPrimary300
            )
            val buttonRadius = a.getDimensionPixelSize(
                R.styleable.MessageView_MessageTemplate_sb_message_button_radius,
                context.resources.getDimensionPixelSize(R.dimen.sb_size_6)
            )

            defaultMessageTemplateStyle = DefaultMessageTemplateStyle(
                textAppearanceInTextView,
                buttonBackground,
                buttonTextAppearance,
                buttonRadius
            )
        } finally {
            a.recycle()
        }
    }

    internal fun inflateViews(
        params: List<ViewParams>,
        message: BaseMessage,
        onMessageTemplateActionHandler: OnMessageTemplateActionHandler? = null
    ) {
        params.forEach {
            addView(createView(it, message, onMessageTemplateActionHandler))
        }
    }

    private fun createView(
        viewParams: ViewParams,
        message: BaseMessage,
        onMessageTemplateActionHandler: OnMessageTemplateActionHandler? = null
    ): View {
        return TemplateViewGenerator.generateView(
            context,
            viewParams,
            Orientation.Column,
            onViewCreated = { view, params ->
                params.action?.register(view, onMessageTemplateActionHandler, message)
                when (view) {
                    is Text -> defaultMessageTemplateStyle.apply(view)
                    is TextButton -> defaultMessageTemplateStyle.apply(view)
                    is Image -> view.scaleType = ContentMode.FitCenter.scaleType
                    is ImageButton -> {}
                    is Box -> {}
                }
            },
        )
    }
}

internal class DefaultMessageTemplateStyle(
    private val textAppearanceInTextView: Int,
    private val buttonBackground: Int,
    private val buttonTextAppearance: Int,
    private val buttonRadius: Int
) {

    fun apply(textView: Text) {
        textView.setTextAppearance(textAppearanceInTextView)
    }

    fun apply(textButton: TextButton) {
        textButton.setBackgroundResource(buttonBackground)
        textButton.setTextAppearance(buttonTextAppearance)
        textButton.radius = buttonRadius.toFloat()
    }
}
