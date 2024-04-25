package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.sendbird.uikit.R
import com.sendbird.uikit.internal.extensions.ERR_MESSAGE_TEMPLATE_UNKNOWN
import com.sendbird.uikit.internal.extensions.createFallbackViewParams
import com.sendbird.uikit.internal.model.template_messages.Params
import com.sendbird.uikit.internal.model.template_messages.TemplateViewGenerator
import com.sendbird.uikit.internal.model.template_messages.ViewLifecycleHandler
import com.sendbird.uikit.internal.ui.widgets.RoundCornerLayout
import com.sendbird.uikit.internal.utils.TemplateViewCachePool

internal class MessageTemplateView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    autoAdjustHeightWhenInvisible: Boolean = true,
) : RoundCornerLayout(context, attrs, defStyle, autoAdjustHeightWhenInvisible) {
    init {
        this.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        this.radius = 0f

        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
    }

    @Synchronized
    internal fun draw(
        params: Params,
        cacheKey: String? = null,
        viewCachePool: TemplateViewCachePool? = null,
        onViewCreated: ViewLifecycleHandler? = null,
        onChildViewCreated: ViewLifecycleHandler? = null
    ) {
        if (this.childCount > 0) {
            this.removeAllViews()
        }

        if (cacheKey != null) {
            val cachedView = viewCachePool?.getScrappedView(cacheKey)
            if (cachedView != null) {
                this.addView(cachedView)
                return
            }
        }

        val view = try {
            TemplateViewGenerator.inflateViews(context, params, onViewCreated, onChildViewCreated)
        } catch (e: Exception) {
            val errorMessage = context.getString(R.string.sb_text_template_message_fallback_error).format(ERR_MESSAGE_TEMPLATE_UNKNOWN)
            val fallbackParams = context.createFallbackViewParams(errorMessage)
            TemplateViewGenerator.inflateViews(context, fallbackParams, onViewCreated, onChildViewCreated)
        }
        if (cacheKey != null) viewCachePool?.cacheView(cacheKey, view)
        this.addView(view)
    }
}
