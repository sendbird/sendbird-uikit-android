package com.sendbird.uikit.internal.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FeedbackRating
import com.sendbird.android.message.FeedbackStatus
import com.sendbird.uikit.R
import com.sendbird.uikit.consts.StringSet
import com.sendbird.uikit.internal.interfaces.OnFeedbackRatingClickListener
import com.sendbird.uikit.internal.model.notifications.NotificationThemeMode
import com.sendbird.uikit.internal.model.template_messages.Params
import com.sendbird.uikit.internal.model.template_messages.TemplateViewGenerator
import com.sendbird.uikit.utils.DrawableUtils
import com.sendbird.uikit.widgets.FeedbackView

@Suppress("DEPRECATION")
internal fun TextView.setAppearance(context: Context, res: Int) {
    if (Build.VERSION.SDK_INT < 23) {
        setTextAppearance(context, res)
    } else {
        setTextAppearance(res)
    }
}

internal fun EditText.setCursorDrawable(context: Context, res: Int) {
    ContextCompat.getDrawable(context, res)?.let {
        setCursorDrawable(it)
    }
}

@SuppressLint("DiscouragedPrivateApi")
internal fun EditText.setCursorDrawable(cursor: Drawable) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        textCursorDrawable = cursor
    } else {
        try {
            val f = TextView::class.java.getDeclaredField(StringSet.mCursorDrawableRes)
            f.isAccessible = true
            f[this] = cursor
        } catch (ignore: Throwable) {
        }
    }
}

internal fun View.loadToBackground(url: String, radius: Int = 0, useRipple: Boolean = false) {
    var builder = Glide.with(this)
        .asDrawable()
        .load(url)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
    if (radius > 0) {
        builder = builder.apply(RequestOptions().transform(RoundedCorners(context.resources.intToDp(radius))))
    }
    builder.into(object : CustomTarget<Drawable>() {
        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
            if (useRipple) this@loadToBackground.addRipple(resource) else background = resource
        }

        override fun onLoadCleared(placeholder: Drawable?) {
        }
    })
}

internal fun ImageView.load(url: String) {
    Glide.with(this)
        .asDrawable()
        .load(url)
        // If the height of the image sets as a warp, it needs to be set to a specific size because it is unnatural when scrolling.(with adjustViewBounds true)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

internal fun ImageView.loadCircle(url: String) {
    val overrideSize = resources
        .getDimensionPixelSize(R.dimen.sb_size_64)

    Glide.with(this)
        .load(url)
        .override(overrideSize, overrideSize)
        .circleCrop()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

internal fun View.addRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.colorControlHighlight, this, true)
    val color = ContextCompat.getColor(context, resourceId)
    this@addRipple.background = createRippleDrawable(color, background)
}

internal fun View.addRipple(background: Drawable?) = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.colorControlHighlight, this, true)
    val color = ContextCompat.getColor(context, resourceId)
    this@addRipple.background = createRippleDrawable(color, background)
}

internal fun View.addRipple(pressedColor: Int) {
    this@addRipple.background = createRippleDrawable(pressedColor, background)
}

private fun createRippleDrawable(pressedColor: Int, backgroundDrawable: Drawable?): RippleDrawable {
    return RippleDrawable(getPressedState(pressedColor), backgroundDrawable, null)
}

private fun getPressedState(pressedColor: Int): ColorStateList {
    return ColorStateList(arrayOf(intArrayOf()), intArrayOf(pressedColor))
}

internal fun TextView.setTypeface(textStyle: Int) {
    this.typeface = Typeface.create(this.typeface, textStyle)
}

internal fun View.setBackgroundColorAndRadius(colorStateList: ColorStateList?, radius: Float) {
    val drawable = GradientDrawable()
    drawable.shape = GradientDrawable.RECTANGLE
    drawable.cornerRadius = radius
    drawable.color = colorStateList
    background = drawable
}

internal fun View.setBackgroundColorAndRadii(colorStateList: ColorStateList?, radii: FloatArray) {
    val drawable = GradientDrawable()
    drawable.shape = GradientDrawable.RECTANGLE
    drawable.cornerRadii = radii
    drawable.color = colorStateList
    background = drawable
}

internal fun FeedbackView.drawFeedback(message: BaseMessage, shouldHideFeedback: Boolean, listener: OnFeedbackRatingClickListener?) {
    this.visibility = if (shouldHideFeedback || message.myFeedbackStatus == FeedbackStatus.NOT_APPLICABLE) {
        View.GONE
    } else {
        View.VISIBLE
    }

    this.drawFeedback(message.myFeedback)
    this.onFeedbackRatingClickListener = { feedbackRating: FeedbackRating ->
        listener?.onFeedbackClicked(message, feedbackRating)
    }
}

internal fun Context.createTemplateMessageLoadingView(): View {
    val maxWidth = resources.getDimensionPixelSize(R.dimen.sb_message_max_width)
    return FrameLayout(this).apply {
        layoutParams = FrameLayout.LayoutParams(maxWidth, maxWidth)
        setBackgroundColor(Color.TRANSPARENT)
        addView(
            ProgressBar(context).apply {
                val size = resources.intToDp(36)
                layoutParams = FrameLayout.LayoutParams(
                    size, size, Gravity.CENTER
                )
                val loading = DrawableUtils.setTintList(
                    context,
                    R.drawable.sb_progress,
                    ColorStateList.valueOf(TemplateViewGenerator.getSpinnerColor(NotificationThemeMode.Default))
                )
                this.indeterminateDrawable = loading
            }
        )
    }
}

internal fun Context.createFallbackViewParams(message: BaseMessage): Params {
    return createFallbackViewParams(message.message)
}

internal fun Context.createFallbackViewParams(message: String): Params {
    return TemplateViewGenerator.createMessageTemplateDefaultViewParam(
        message,
        this.getString(R.string.sb_text_template_message_fallback_title),
        this.getString(R.string.sb_text_template_message_fallback_description),
    )
}

internal fun TextView.applyTextAlignment(gravity: Int) {
    if ((gravity and Gravity.START) == Gravity.START) {
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
    } else if ((gravity and Gravity.END) == Gravity.END) {
        textAlignment = View.TEXT_ALIGNMENT_VIEW_END
    }
}
