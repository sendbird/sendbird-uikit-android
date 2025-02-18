package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.sendbird.message.template.ViewGenerator
import com.sendbird.message.template.interfaces.OnViewLifecycleHandler
import com.sendbird.message.template.consts.TemplateTheme
import com.sendbird.message.template.model.TemplateParams
import com.sendbird.uikit.R
import com.sendbird.uikit.internal.extensions.ERR_MESSAGE_TEMPLATE_UNKNOWN
import com.sendbird.uikit.internal.model.template_messages.TemplateParamsCreator
import com.sendbird.uikit.internal.ui.widgets.RoundCornerLayout
import com.sendbird.uikit.internal.utils.TemplateViewCachePool

internal class MessageTemplateView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    autoAdjustHeightWhenInvisible: Boolean = true,
) : RoundCornerLayout(context, attrs, defStyle, autoAdjustHeightWhenInvisible) {
    var maxWidth: Int = Int.MAX_VALUE
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
        params: TemplateParams,
        theme: TemplateTheme,
        cacheKey: String? = null,
        viewCachePool: TemplateViewCachePool? = null,
        onViewCreated: OnViewLifecycleHandler? = null,
        onChildViewCreated: OnViewLifecycleHandler? = null
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
            ViewGenerator.inflateViews(context, theme, params, onViewCreated, onChildViewCreated)
        } catch (e: Exception) {
            val errorMessage = context.getString(R.string.sb_text_template_message_fallback_error).format(ERR_MESSAGE_TEMPLATE_UNKNOWN)
            val fallbackParams = TemplateParamsCreator.createFallbackViewParams(context, errorMessage)
            ViewGenerator.inflateViews(context, theme, fallbackParams, onViewCreated, onChildViewCreated)
        }
        if (cacheKey != null) viewCachePool?.cacheView(cacheKey, view)
        this.addView(view)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var totalWidth = measuredWidth

        if (totalWidth > maxWidth) {
            totalWidth = maxWidth
            val newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY)
            super.onMeasure(newWidthMeasureSpec, heightMeasureSpec)
        }

        setMeasuredDimension(totalWidth, measuredHeight)
    }
}
