package com.sendbird.uikit.internal.model.template_messages

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.interfaces.OnNotificationTemplateActionHandler
import com.sendbird.uikit.internal.extensions.intToDp
import com.sendbird.uikit.internal.model.notifications.NotificationThemeMode
import com.sendbird.uikit.internal.model.template_messages.TemplateViewGenerator.backgroundColor
import com.sendbird.uikit.internal.model.template_messages.TemplateViewGenerator.descTextColor
import com.sendbird.uikit.internal.model.template_messages.TemplateViewGenerator.titleColor
import com.sendbird.uikit.model.Action
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.json.JSONObject

const val FILL_PARENT = 0
const val WRAP_CONTENT = 1

@Serializable
internal data class ActionData(
    val type: ActionType = ActionType.Web,
    val data: String,
    val alterData: String? = null
) {
    fun register(
        view: View,
        message: BaseMessage,
        onNotificationTemplateActionHandler: OnNotificationTemplateActionHandler?
    ) {
        onNotificationTemplateActionHandler?.let { callback ->
            view.setOnClickListener {
                callback.onHandleAction(it, Action.from(this@ActionData), message)
            }
        }
    }

    companion object {
        fun create(
            type: String = KeySet.web,
            data: String,
            customData: String? = null
        ): JsonObject {
            return buildJsonObject {
                put(KeySet.type, type)
                put(KeySet.data, data)
                customData?.let { put(KeySet.alterData, data) }
            }
        }
    }
}

@Serializable
internal data class SizeSpec(
    val type: SizeType,
    @SerialName(KeySet.value)
    private val _value: Int
) {
    internal val value: Int
        get() = when (type) {
            SizeType.Fixed -> _value
            SizeType.Flex -> {
                when (_value) {
                    FILL_PARENT -> ViewGroup.LayoutParams.MATCH_PARENT
                    WRAP_CONTENT -> ViewGroup.LayoutParams.WRAP_CONTENT
                    else -> _value
                }
            }
        }

    fun getWeight(): Float {
        return when (type) {
            SizeType.Fixed -> 0F
            SizeType.Flex -> {
                when (_value) {
                    FILL_PARENT -> 1F
                    WRAP_CONTENT -> 0F
                    else -> 0F
                }
            }
        }
    }
}

@Serializable
internal data class Align(
    private val horizontal: HorizontalAlign = HorizontalAlign.Left,
    private val vertical: VerticalAlign = VerticalAlign.Top
) {
    val gravity: Int
        get() = horizontal.value or vertical.value

    companion object {
        fun create(
            horizontal: String = KeySet.left,
            vertical: String = KeySet.top
        ): JsonObject {
            return buildJsonObject {
                put(KeySet.horizontal, horizontal)
                put(KeySet.vertical, vertical)
            }
        }
    }
}

@Serializable
internal data class MetaData(
    val pixelWidth: Int,
    val pixelHeight: Int
)

@Serializable
internal data class Params(
    val version: Int,
    val body: Body
)

@Serializable
internal data class Body(
    val items: List<ViewParams>
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator(KeySet.type)
internal sealed class ViewParams {
    abstract val type: ViewType
    abstract val action: ActionData?
    abstract val width: SizeSpec
    abstract val height: SizeSpec
    abstract val viewStyle: ViewStyle

    private fun getWeight(orientation: Orientation): Float {
        return when (orientation) {
            Orientation.Row -> {
                width.getWeight()
            }
            Orientation.Column -> {
                height.getWeight()
            }
        }
    }

    fun applyLayoutParams(
        context: Context,
        layoutParams: ViewGroup.LayoutParams,
        orientation: Orientation
    ): ViewGroup.LayoutParams {
        val resources = context.resources
        layoutParams.width = if (width.type == SizeType.Fixed) resources.intToDp(width.value) else width.value
        layoutParams.height = if (height.type == SizeType.Fixed) resources.intToDp(height.value) else height.value
        if (layoutParams is LinearLayout.LayoutParams) {
            layoutParams.weight = getWeight(orientation)
        }
        return layoutParams
    }
}

@Serializable
@SerialName(KeySet.box)
internal data class BoxViewParams(
    override val type: ViewType,
    override val action: ActionData? = null,
    override val width: SizeSpec = SizeSpec(SizeType.Flex, FILL_PARENT),
    override val height: SizeSpec = SizeSpec(SizeType.Flex, WRAP_CONTENT),
    override val viewStyle: ViewStyle = ViewStyle(),
    val align: Align = Align(),
    @SerialName(KeySet.layout)
    val orientation: Orientation = Orientation.Row,
    val items: List<ViewParams>? = null
) : ViewParams()

@Serializable
@SerialName(KeySet.text)
internal data class TextViewParams(
    override val type: ViewType,
    override val action: ActionData? = null,
    override val width: SizeSpec = SizeSpec(SizeType.Flex, FILL_PARENT),
    override val height: SizeSpec = SizeSpec(SizeType.Flex, WRAP_CONTENT),
    override val viewStyle: ViewStyle = ViewStyle(),
    val align: Align = Align(),
    val text: String,
    val maxTextLines: Int? = null,
    val textStyle: TextStyle = TextStyle()
) : ViewParams()

@Serializable
@SerialName(KeySet.image)
internal data class ImageViewParams(
    override val type: ViewType,
    override val action: ActionData? = null,
    override val width: SizeSpec = SizeSpec(SizeType.Flex, FILL_PARENT),
    override val height: SizeSpec = SizeSpec(SizeType.Flex, WRAP_CONTENT),
    override val viewStyle: ViewStyle = ViewStyle(),
    val imageUrl: String,
    val metaData: MetaData? = null,
    val imageStyle: ImageStyle = ImageStyle()
) : ViewParams()

@Serializable
@SerialName(KeySet.textButton)
internal data class ButtonViewParams(
    override val type: ViewType,
    override val action: ActionData? = null,
    override val width: SizeSpec = SizeSpec(SizeType.Flex, FILL_PARENT),
    override val height: SizeSpec = SizeSpec(SizeType.Flex, WRAP_CONTENT),
    override val viewStyle: ViewStyle = ViewStyle(),
    val text: String,
    val maxTextLines: Int = 1,
    val textStyle: TextStyle? = null
) : ViewParams()

@Serializable
@SerialName(KeySet.imageButton)
internal data class ImageButtonViewParams(
    override val type: ViewType,
    override val action: ActionData? = null,
    override val width: SizeSpec = SizeSpec(SizeType.Flex, FILL_PARENT),
    override val height: SizeSpec = SizeSpec(SizeType.Flex, WRAP_CONTENT),
    override val viewStyle: ViewStyle = ViewStyle(),
    val imageUrl: String,
    val metaData: MetaData? = null,
    val imageStyle: ImageStyle = ImageStyle()
) : ViewParams()

@Serializable
@SerialName(KeySet.carouselView)
internal data class CarouselViewParams(
    override val type: ViewType,
    override val action: ActionData? = null,
    override val width: SizeSpec = SizeSpec(SizeType.Flex, FILL_PARENT),
    override val height: SizeSpec = SizeSpec(SizeType.Flex, WRAP_CONTENT),
    override val viewStyle: ViewStyle = ViewStyle(),
    val items: List<Params>,
    val spacing: Int = 10
) : ViewParams()

internal object TemplateParamsCreator {
    @JvmStatic
    @Throws(Exception::class)
    internal fun createDataTemplateViewParams(
        dataTemplate: String,
        themeMode: NotificationThemeMode
    ): Params {
        val textParams = mutableListOf<ViewParams>().apply {
            val json = JSONObject(dataTemplate)
            json.keys().forEach { key ->
                add(
                    TextViewParams(
                        type = ViewType.Text,
                        textStyle = TextStyle(
                            size = 14,
                            color = themeMode.descTextColor
                        ),
                        text = "$key : ${json.getString(key)}"
                    )
                )
            }
        }
        return Params(
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
                        items = textParams
                    ),
                )
            )
        )
    }

    @JvmStatic
    fun createDefaultViewParam(
        message: BaseMessage,
        defaultFallbackTitle: String,
        defaultFallbackDescription: String,
        themeMode: NotificationThemeMode
    ): Params {
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
        return Params(
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
    fun createMessageTemplateDefaultViewParam(
        message: String,
        defaultFallbackTitle: String,
        defaultFallbackDescription: String
    ): Params {
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
        return Params(
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
                            radius = 16
                        ),
                        items = textList
                    ),
                )
            )
        )
    }
}
