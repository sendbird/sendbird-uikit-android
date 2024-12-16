package com.sendbird.uikit.internal.model.template_messages

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import android.widget.ProgressBar
import com.sendbird.android.message.BaseMessage
import com.sendbird.message.template.consts.Orientation
import com.sendbird.message.template.consts.SizeType
import com.sendbird.message.template.consts.TemplateTheme
import com.sendbird.message.template.consts.ViewType
import com.sendbird.message.template.model.Body
import com.sendbird.message.template.model.BoxViewParams
import com.sendbird.message.template.model.Margin
import com.sendbird.message.template.model.Padding
import com.sendbird.message.template.model.SizeSpec
import com.sendbird.message.template.model.TemplateParams
import com.sendbird.message.template.model.TextStyle
import com.sendbird.message.template.model.TextViewParams
import com.sendbird.message.template.model.ViewParams
import com.sendbird.message.template.model.ViewStyle
import com.sendbird.message.template.model.WRAP_CONTENT
import com.sendbird.uikit.R
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.internal.extensions.intToDp
import com.sendbird.uikit.internal.model.notifications.NotificationThemeMode
import com.sendbird.uikit.utils.DrawableUtils
import org.json.JSONObject

internal object TemplateParamsCreator {
    @JvmStatic
    @Throws(Exception::class)
    internal fun createDataTemplateViewParams(
        dataTemplate: String,
        themeMode: TemplateTheme
    ): TemplateParams {
        val textParams = mutableListOf<ViewParams>().apply {
            val json = JSONObject(dataTemplate)
            json.keys().forEach { key ->
                add(
                    TextViewParams(
                        type = ViewType.Text,
                        textStyle = TextStyle(
                            size = 14,
                            color = Color.parseColor(if (themeMode == TemplateTheme.Light) "#70000000" else "#70FFFFFF")
                        ),
                        text = "$key : ${json.getString(key)}"
                    )
                )
            }
        }
        return TemplateParams(
            version = 1,
            body = Body(
                items = listOf(
                    BoxViewParams(
                        type = ViewType.Box,
                        orientation = Orientation.Column,
                        viewStyle = ViewStyle(
                            backgroundColor = Color.parseColor(if (themeMode == TemplateTheme.Light) "#EEEEEE" else "#2C2C2C"),
                            padding = Padding(
                                12, 12, 12, 12
                            ),
                            radius = 8
                        ),
                        items = textParams
                    ),
                )
            )
        )
    }

    @JvmStatic
    internal fun createDefaultViewParam(
        message: BaseMessage,
        defaultFallbackTitle: String,
        defaultFallbackDescription: String,
        themeMode: NotificationThemeMode
    ): TemplateParams {
        val hasFallbackMessage = message.message.isNotEmpty()
        val textList = mutableListOf(
            TextViewParams(
                type = ViewType.Text,
                textStyle = TextStyle(
                    size = 14,
                    color = themeMode.titleColor
                ),
                text = message.message.takeIf { it.isNotEmpty() } ?: defaultFallbackTitle
            )
        )

        if (!hasFallbackMessage) {
            textList.add(
                TextViewParams(
                    type = ViewType.Text,
                    textStyle = TextStyle(
                        size = 14,
                        color = themeMode.descTextColor
                    ),
                    text = defaultFallbackDescription
                )
            )
        }
        return TemplateParams(
            version = 1,
            body = Body(
                items = listOf(
                    BoxViewParams(
                        type = ViewType.Box,
                        orientation = Orientation.Column,
                        viewStyle = ViewStyle(
                            backgroundColor = themeMode.backgroundColor,
                            padding = Padding(
                                12, 12, 12, 12
                            ),
                            radius = 8
                        ),
                        items = textList
                    ),
                )
            )
        )
    }

    @JvmStatic
    internal fun createMessageTemplateDefaultViewParam(
        message: String,
        defaultFallbackTitle: String,
        defaultFallbackDescription: String
    ): TemplateParams {
        val hasFallbackMessage = message.isNotEmpty()
        val textList = mutableListOf(
            TextViewParams(
                type = ViewType.Text,
                width = SizeSpec(SizeType.Flex, WRAP_CONTENT),
                height = SizeSpec(SizeType.Flex, WRAP_CONTENT),
                textStyle = TextStyle(
                    size = 14,
                    color = NotificationThemeMode.Default.titleColor
                ),
                text = message.takeIf { it.isNotEmpty() } ?: defaultFallbackTitle,
            )
        )

        if (!hasFallbackMessage) {
            textList.add(
                TextViewParams(
                    type = ViewType.Text,
                    width = SizeSpec(SizeType.Flex, WRAP_CONTENT),
                    height = SizeSpec(SizeType.Flex, WRAP_CONTENT),
                    textStyle = TextStyle(
                        size = 14,
                        color = NotificationThemeMode.Default.descTextColor
                    ),
                    text = defaultFallbackDescription
                )
            )
        }
        return TemplateParams(
            version = 1,
            body = Body(
                items = listOf(
                    BoxViewParams(
                        type = ViewType.Box,
                        orientation = Orientation.Column,
                        width = SizeSpec(SizeType.Flex, WRAP_CONTENT),
                        height = SizeSpec(SizeType.Flex, WRAP_CONTENT),
                        viewStyle = ViewStyle(
                            backgroundColor = NotificationThemeMode.Default.backgroundColor,
                            padding = Padding(
                                6, 6, 12, 12
                            ),
                            margin = Margin(
                                0, 0, 50, 0
                            ),
                            radius = 16
                        ),
                        items = textList
                    ),
                )
            )
        )
    }

    internal fun createTemplateMessageLoadingView(context: Context): View {
        val height = context.resources.getDimensionPixelSize(R.dimen.sb_template_message_loading_view_height)
        return FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
            setBackgroundColor(Color.TRANSPARENT)
            addView(
                ProgressBar(context).apply {
                    val size = resources.intToDp(42)
                    layoutParams = FrameLayout.LayoutParams(
                        size, size, Gravity.CENTER
                    )
                    val loading = DrawableUtils.setTintList(
                        context,
                        R.drawable.sb_progress,
                        ColorStateList.valueOf(NotificationThemeMode.Default.spinnerColor)
                    )
                    this.indeterminateDrawable = loading
                }
            )
        }
    }

    internal fun createNotificationLoadingView(
        context: Context,
        isChatNotification: Boolean,
        themeMode: NotificationThemeMode,
    ): View {
        return FrameLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                resources.intToDp(if (isChatNotification) 274 else 294),
            )
            addView(
                ProgressBar(context).apply {
                    val size = resources.intToDp(36)
                    layoutParams = LayoutParams(
                        size, size, Gravity.CENTER
                    )
                    val loading = DrawableUtils.setTintList(
                        context,
                        R.drawable.sb_progress,
                        ColorStateList.valueOf(themeMode.spinnerColor)
                    )
                    this.indeterminateDrawable = loading
                }
            )
        }
    }

    internal fun createFallbackViewParams(context: Context, message: BaseMessage): TemplateParams {
        return createFallbackViewParams(context, message.message)
    }

    internal fun createFallbackViewParams(context: Context, message: String): TemplateParams {
        return TemplateParamsCreator.createMessageTemplateDefaultViewParam(
            message,
            context.getString(R.string.sb_text_template_message_fallback_title),
            context.getString(R.string.sb_text_template_message_fallback_description),
        )
    }

    private val NotificationThemeMode.backgroundColor: Int
        get() {
            val color = when (this) {
                NotificationThemeMode.Light -> "#EEEEEE"
                NotificationThemeMode.Dark -> "#2C2C2C"
                NotificationThemeMode.Default -> if (SendbirdUIKit.getDefaultThemeMode() == SendbirdUIKit.ThemeMode.Light) "#EEEEEE" else "#2C2C2C"
            }
            return Color.parseColor(color)
        }

    private val NotificationThemeMode.titleColor: Int
        get() {
            val color = when (this) {
                NotificationThemeMode.Light -> "#E0000000"
                NotificationThemeMode.Dark -> "#E0FFFFFF"
                NotificationThemeMode.Default -> if (SendbirdUIKit.getDefaultThemeMode() == SendbirdUIKit.ThemeMode.Light) "#E0000000" else "#E0FFFFFF"
            }
            return Color.parseColor(color)
        }

    private val NotificationThemeMode.descTextColor: Int
        get() {
            val color = when (this) {
                NotificationThemeMode.Light -> "#70000000"
                NotificationThemeMode.Dark -> "#70FFFFFF"
                NotificationThemeMode.Default -> if (SendbirdUIKit.getDefaultThemeMode() == SendbirdUIKit.ThemeMode.Light) "#70000000" else "#70FFFFFF"
            }
            return Color.parseColor(color)
        }

    private val NotificationThemeMode.spinnerColor: Int
        get() {
            val color = when (this) {
                NotificationThemeMode.Light -> "#70000000"
                NotificationThemeMode.Dark -> "#70FFFFFF"
                NotificationThemeMode.Default -> if (SendbirdUIKit.getDefaultThemeMode() == SendbirdUIKit.ThemeMode.Light) "#70000000" else "#70FFFFFF"
            }
            return Color.parseColor(color)
        }
}
