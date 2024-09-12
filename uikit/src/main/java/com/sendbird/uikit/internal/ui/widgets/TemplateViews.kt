package com.sendbird.uikit.internal.ui.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.uikit.R
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.internal.adapter.CarouselChildViewAdapter
import com.sendbird.uikit.internal.extensions.addRipple
import com.sendbird.uikit.internal.extensions.applyTextAlignment
import com.sendbird.uikit.internal.extensions.intToDp
import com.sendbird.uikit.internal.extensions.setAppearance
import com.sendbird.uikit.internal.extensions.setTypeface
import com.sendbird.uikit.internal.model.template_messages.BoxViewParams
import com.sendbird.uikit.internal.model.template_messages.ButtonViewParams
import com.sendbird.uikit.internal.model.template_messages.CarouselViewParams
import com.sendbird.uikit.internal.model.template_messages.ImageButtonViewParams
import com.sendbird.uikit.internal.model.template_messages.ImageViewParams
import com.sendbird.uikit.internal.model.template_messages.Orientation
import com.sendbird.uikit.internal.model.template_messages.TextViewParams
import com.sendbird.uikit.internal.model.template_messages.ViewLifecycleHandler
import com.sendbird.uikit.internal.utils.CarouselLeftSnapHelper

@SuppressLint("ViewConstructor")
internal open class Text @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RoundCornerLayout(context, attrs, defStyleAttr) {
    private val textView: TextView

    init {
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        textView = AppCompatTextView(context).apply {
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        }
        this.addView(textView)
    }

    fun apply(params: TextViewParams, orientation: Orientation) {
        params.applyLayoutParams(context, layoutParams, orientation)
        params.viewStyle.apply(this)
        params.textStyle.apply(textView)

        textView.gravity = params.align.gravity
        textView.applyTextAlignment(params.align.gravity)
        params.maxTextLines?.let { textView.maxLines = it }
        textView.text = params.text
    }

    fun setTextAppearance(textAppearance: Int) {
        textView.setAppearance(context, textAppearance)
    }
}

@SuppressLint("ViewConstructor")
internal open class Image @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MessageTemplateImageView(context, attrs, defStyleAttr) {
    init {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        scaleType = ScaleType.FIT_CENTER
    }

    fun apply(params: ImageViewParams, orientation: Orientation) {
        this.viewParams = params
        params.applyLayoutParams(context, layoutParams, orientation)
        params.metaData?.let {
            setSize(it.pixelWidth, it.pixelHeight)
        }
        params.imageStyle.apply(this)
        params.viewStyle.apply(this)
        load(params.imageUrl)
    }
}

@SuppressLint("ViewConstructor")
internal open class TextButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RoundCornerLayout(context, attrs, defStyleAttr) {
    private val textView: TextView

    init {
        // Even if action doesn't exist click ripple effect should show. (UIKit spec)
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        isClickable = true
        gravity = Gravity.CENTER
        // default button padding.
        val padding = resources.intToDp(10)
        this.setPaddingRelative(padding, padding, padding, padding)
        this.setBackgroundResource(R.drawable.sb_shape_round_rect_background_200)
        setRadiusIntSize(6)
        addRipple(background)

        textView = AppCompatTextView(context).apply {
            ellipsize = TextUtils.TruncateAt.END
            maxLines = 1
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
            setTypeface(Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, when (SendbirdUIKit.getDefaultThemeMode()) {
                SendbirdUIKit.ThemeMode.Light -> R.color.primary_300
                SendbirdUIKit.ThemeMode.Dark -> R.color.primary_200
            }))
        }
        this.addView(textView)
    }

    fun apply(params: ButtonViewParams, orientation: Orientation) {
        params.applyLayoutParams(context, layoutParams, orientation)
        params.textStyle?.apply(textView)
        params.viewStyle.apply(this, true)
        textView.maxLines = params.maxTextLines
        textView.text = params.text
        addRipple(background)
    }

    fun setTextAppearance(textAppearance: Int) {
        textView.setAppearance(context, textAppearance)
    }
}

@SuppressLint("ViewConstructor")
internal open class ImageButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MessageTemplateImageView(context, attrs, defStyleAttr) {
    init {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        scaleType = ScaleType.FIT_CENTER
        addRipple(background)
    }

    fun apply(params: ImageButtonViewParams, orientation: Orientation) {
        this.viewParams = params
        params.applyLayoutParams(context, layoutParams, orientation)
        params.metaData?.let {
            setSize(it.pixelWidth, it.pixelHeight)
        }
        params.imageStyle.apply(this)
        params.viewStyle.apply(this, true)
        load(params.imageUrl)
        addRipple(background)
    }
}

@SuppressLint("ViewConstructor")
internal open class Box @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RoundCornerLayout(context, attrs, defStyleAttr) {
    init {
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
    }

    fun apply(params: BoxViewParams, orientation: Orientation) {
        this.orientation = params.orientation.value
        params.applyLayoutParams(context, layoutParams, orientation)
        gravity = params.align.gravity
        params.viewStyle.apply(this)
    }
}

@SuppressLint("ClickableViewAccessibility")
internal class CarouselView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RoundCornerLayout(context, attrs, defStyleAttr) {
    val recyclerView: RecyclerView
    private var itemDecoration: CarouselViewItemDecoration? = null

    init {
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )

        recyclerView = object : RecyclerView(context) {
            // If padding is touched, the event should be dispatched to the parent view.
            override fun onTouchEvent(e: MotionEvent): Boolean {
                val layoutManager = this.layoutManager as? LinearLayoutManager
                layoutManager?.let {
                    val firstVisibleItemPosition = it.findFirstVisibleItemPosition()
                    val lastVisibleItemPosition = it.findLastVisibleItemPosition()
                    val isTouchEventInPadding = (e.x < paddingStart) || (e.x > width - paddingEnd)
                    if (isTouchEventInPadding && (firstVisibleItemPosition == 0 || lastVisibleItemPosition == it.itemCount - 1)) {
                        return false
                    }
                }

                return super.onTouchEvent(e)
            }
        }.apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            clipToPadding = false

            CarouselLeftSnapHelper().attachToRecyclerView(this)
        }

        this.addView(recyclerView)
    }

    fun apply(params: CarouselViewParams, orientation: Orientation, onChildViewCreated: ViewLifecycleHandler?) {
        val spaceInPixel = context.resources.intToDp(params.carouselStyle.spacing)
        itemDecoration?.let { recyclerView.removeItemDecoration(it) }
        itemDecoration = CarouselViewItemDecoration(spaceInPixel).also {
            recyclerView.addItemDecoration(it)
        }

        CarouselChildViewAdapter(context.resources.intToDp(params.carouselStyle.maxChildWidth)).apply {
            recyclerView.adapter = this
            this.onChildViewCreated = onChildViewCreated
            this.setChildTemplateParams(params.items)
        }
        params.applyLayoutParams(context, layoutParams, orientation)

        // apply ViewStyle to RecyclerView but Recycler is not a ViewRoundable instance.
        // So, we need to apply the style to parent view directly. (workaround)
        params.viewStyle.apply(recyclerView)
        setRadiusIntSize(params.viewStyle.radius ?: 0)
        setBorder(params.viewStyle.borderWidth ?: 0, params.viewStyle.borderColor ?: Color.TRANSPARENT)
    }
}
