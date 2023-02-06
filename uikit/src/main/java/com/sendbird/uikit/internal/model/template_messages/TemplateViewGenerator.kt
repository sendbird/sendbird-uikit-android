package com.sendbird.uikit.internal.model.template_messages

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.sendbird.uikit.internal.ui.widgets.Box
import com.sendbird.uikit.internal.ui.widgets.Image
import com.sendbird.uikit.internal.ui.widgets.ImageButton
import com.sendbird.uikit.internal.ui.widgets.Text
import com.sendbird.uikit.internal.ui.widgets.TextButton

internal typealias ViewLifecycleHandler = (view: View, viewParams: ViewParams) -> Unit

internal object TemplateViewGenerator {
    fun generateView(
        context: Context,
        viewParams: ViewParams,
        orientation: Orientation,
        onViewCreated: ViewLifecycleHandler? = null
    ): View {
        return when (viewParams) {
            is BoxViewParams -> createBoxView(context, viewParams, orientation, onViewCreated)
            is ImageViewParams -> createImageView(context, viewParams, orientation, onViewCreated)
            is TextViewParams -> createTextView(context, viewParams, orientation, onViewCreated)
            is ButtonViewParams -> createButtonView(context, viewParams, orientation, onViewCreated)
            is ImageButtonViewParams -> createImageButtonView(context, viewParams, orientation, onViewCreated)
        }
    }

    private fun createTextView(
        context: Context,
        params: TextViewParams,
        orientation: Orientation,
        onViewCreated: ViewLifecycleHandler? = null
    ): View {
        return Text(context).apply {
            onViewCreated?.invoke(this, params)
            apply(params, orientation)
        }
    }

    private fun createImageView(
        context: Context,
        params: ImageViewParams,
        orientation: Orientation,
        onViewCreated: ViewLifecycleHandler? = null
    ): View {
        return Image(context).apply {
            onViewCreated?.invoke(this, params)
            apply(params, orientation)
        }
    }

    private fun createButtonView(
        context: Context,
        params: ButtonViewParams,
        orientation: Orientation,
        onViewCreated: ViewLifecycleHandler? = null
    ): View {
        return TextButton(context).apply {
            onViewCreated?.invoke(this, params)
            apply(params, orientation)
        }
    }

    private fun createImageButtonView(
        context: Context,
        params: ImageButtonViewParams,
        orientation: Orientation,
        onViewCreated: ViewLifecycleHandler? = null
    ): View {
        return ImageButton(context).apply {
            onViewCreated?.invoke(this, params)
            apply(params, orientation)
        }
    }

    private fun createBoxView(
        context: Context,
        params: BoxViewParams,
        orientation: Orientation,
        onViewCreated: ViewLifecycleHandler? = null
    ): ViewGroup {
        return Box(context).apply {
            onViewCreated?.invoke(this, params)
            apply(params, orientation)
            params.items?.forEach {
                addView(
                    generateView(
                        context,
                        it,
                        params.orientation,
                        onViewCreated
                    )
                )
            }
        }
    }
}
