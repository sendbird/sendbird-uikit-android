package com.sendbird.uikit.internal.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import com.sendbird.uikit.internal.extensions.intToDp
import com.sendbird.uikit.internal.interfaces.ViewRoundable

internal open class RoundCornerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val autoAdjustHeightWhenInvisible: Boolean = true
) : LinearLayout(context, attrs, defStyleAttr), ViewRoundable {
    private val rectF: RectF = RectF()
    private val path: Path = Path()
    override var radius: Float = 0F
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setWillNotDraw(false)
    }

    override fun setRadiusIntSize(radius: Int) {
        this.radius = context.resources.intToDp(radius).toFloat()
        invalidate()
    }

    final override fun setBorder(borderWidth: Int, @ColorInt borderColor: Int) {
        borderPaint.color = borderColor
        borderPaint.strokeWidth = context.resources.intToDp(borderWidth).toFloat()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // In the Android platform, even if a view is not drawn on the screen due to left and right views, its height value exists.
        // In the template message syntax, the views that are not drawn have to hide.
        // onSizeChanged() and onLayout() do not update the view even if the visibility changes, so the status of the view must be updated once again.
        // Logger.i("-- parent view's width=${(parent as View).width}, x=$x, measureWidth=$width, visible=$visibility")
        if (autoAdjustHeightWhenInvisible) {
            val visibility = if (x <= -width || x >= (parent as View).width) GONE else VISIBLE
            post {
                this.visibility = visibility
            }
        }
    }

    override fun draw(canvas: Canvas) {
        rectF.set(0f, 0f, width.toFloat(), height.toFloat())
        var save: Int? = null
        if (radius > 0) {
            path.reset()
            path.addRoundRect(rectF, radius, radius, Path.Direction.CW)
            save = canvas.save()
            canvas.clipPath(path)
        }

        val hasBorder = borderPaint.strokeWidth > 0
        val halfBorder: Float = borderPaint.strokeWidth / 2
        if (radius > 0 || hasBorder) {
            rectF.inset(halfBorder, halfBorder)
        }

        super.draw(canvas)
        save?.let { canvas.restoreToCount(it) }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        val hasBorder = borderPaint.strokeWidth > 0
        if (hasBorder) {
            canvas.drawRoundRect(rectF, radius, radius, borderPaint)
        }
    }
}
