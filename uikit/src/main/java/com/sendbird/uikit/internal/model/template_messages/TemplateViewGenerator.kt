package com.sendbird.uikit.internal.model.template_messages

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.internal.model.notifications.NotificationThemeMode
import com.sendbird.uikit.internal.ui.widgets.Box
import com.sendbird.uikit.internal.ui.widgets.CarouselView
import com.sendbird.uikit.internal.ui.widgets.Image
import com.sendbird.uikit.internal.ui.widgets.ImageButton
import com.sendbird.uikit.internal.ui.widgets.Text
import com.sendbird.uikit.internal.ui.widgets.TextButton

internal typealias ViewLifecycleHandler = (view: View, viewParams: ViewParams) -> Unit

internal object TemplateViewGenerator {

    @Throws(RuntimeException::class)
    fun inflateViews(
        context: Context,
        params: Params,
        onViewCreated: ViewLifecycleHandler? = null,
        onChildViewCreated: ViewLifecycleHandler? = null
    ): View {
        when (params.version) {
            1, 2 -> {
                return LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.body.items.forEach {
                        addView(
                            generateView(context, it, Orientation.Column, onViewCreated, onChildViewCreated)
                        )
                    }
                }
            }
            else -> {
                throw RuntimeException("unsupported version. current version = ${params.version}")
            }
        }
    }

    private fun generateView(
        context: Context,
        viewParams: ViewParams,
        orientation: Orientation,
        onViewCreated: ViewLifecycleHandler? = null,
        onChildViewCreated: ViewLifecycleHandler? = null
    ): View {
        return when (viewParams) {
            is BoxViewParams -> createBoxView(context, viewParams, orientation, onViewCreated)
            is ImageViewParams -> createImageView(context, viewParams, orientation, onViewCreated)
            is TextViewParams -> createTextView(context, viewParams, orientation, onViewCreated)
            is ButtonViewParams -> createButtonView(context, viewParams, orientation, onViewCreated)
            is ImageButtonViewParams -> createImageButtonView(context, viewParams, orientation, onViewCreated)
            is CarouselViewParams -> createCarouselView(context, viewParams, orientation, onViewCreated, onChildViewCreated)
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

    private fun createCarouselView(
        context: Context,
        params: CarouselViewParams,
        orientation: Orientation,
        onViewCreated: ViewLifecycleHandler? = null,
        onChildViewCreated: ViewLifecycleHandler? = null
    ): ViewGroup {
        return CarouselView(context).apply {
            onViewCreated?.invoke(this, params)
            apply(params, orientation, onChildViewCreated)
        }
    }

    internal val NotificationThemeMode.backgroundColor: Int
        get() {
            val color = when (this) {
                NotificationThemeMode.Light -> "#EEEEEE"
                NotificationThemeMode.Dark -> "#2C2C2C"
                NotificationThemeMode.Default -> if (SendbirdUIKit.getDefaultThemeMode() == SendbirdUIKit.ThemeMode.Light) "#EEEEEE" else "#2C2C2C"
            }
            return Color.parseColor(color)
        }

    internal val NotificationThemeMode.titleColor: Int
        get() {
            val color = when (this) {
                NotificationThemeMode.Light -> "#E0000000"
                NotificationThemeMode.Dark -> "#E0FFFFFF"
                NotificationThemeMode.Default -> if (SendbirdUIKit.getDefaultThemeMode() == SendbirdUIKit.ThemeMode.Light) "#E0000000" else "#E0FFFFFF"
            }
            return Color.parseColor(color)
        }

    internal val NotificationThemeMode.descTextColor: Int
        get() {
            val color = when (this) {
                NotificationThemeMode.Light -> "#70000000"
                NotificationThemeMode.Dark -> "#70FFFFFF"
                NotificationThemeMode.Default -> if (SendbirdUIKit.getDefaultThemeMode() == SendbirdUIKit.ThemeMode.Light) "#70000000" else "#70FFFFFF"
            }
            return Color.parseColor(color)
        }

    internal val NotificationThemeMode.spinnerColor: Int
        get() {
            val color = when (this) {
                NotificationThemeMode.Light -> "#70000000"
                NotificationThemeMode.Dark -> "#70FFFFFF"
                NotificationThemeMode.Default -> if (SendbirdUIKit.getDefaultThemeMode() == SendbirdUIKit.ThemeMode.Light) "#70000000" else "#70FFFFFF"
            }
            return Color.parseColor(color)
        }
}
