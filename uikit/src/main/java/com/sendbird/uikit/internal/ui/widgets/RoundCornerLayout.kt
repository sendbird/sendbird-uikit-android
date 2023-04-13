package com.sendbird.uikit.internal.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import com.sendbird.uikit.internal.extensions.intToDp
import com.sendbird.uikit.internal.interfaces.ViewRoundable

internal open class RoundCornerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), ViewRoundable {
    private val rectF: RectF = RectF()
    private val path: Path = Path()
    private var strokePaint: Paint? = null
    override var radius: Float = 0F

    init {
        setBorder(0, Color.TRANSPARENT)
    }

    override fun setRadiusIntSize(radius: Int) {
        this.radius = context.resources.intToDp(radius).toFloat()
    }

    final override fun setBorder(borderWidth: Int, @ColorInt borderColor: Int) {
        if (borderWidth <= 0)
            strokePaint = null
        else {
            strokePaint = Paint().apply {
                style = Paint.Style.STROKE
                isAntiAlias = true
                strokeWidth = context.resources.intToDp(borderWidth).toFloat()
                color = borderColor
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rectF.set(0f, 0f, w.toFloat(), h.toFloat())
        resetPath()
    }

    override fun draw(canvas: Canvas) {
        val save = canvas.save()
        canvas.clipPath(path)
        super.draw(canvas)
        strokePaint?.let {
            val inlineWidth = it.strokeWidth
            rectF.set(inlineWidth / 2, inlineWidth / 2, width - inlineWidth / 2, height - inlineWidth / 2)
            canvas.drawRoundRect(rectF, radius, radius, it)
        }
        canvas.restoreToCount(save)
    }

    private fun resetPath() {
        path.reset()
        path.addRoundRect(rectF, radius, radius, Path.Direction.CW)
        path.close()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // In the Android platform, even if a view is not drawn on the screen due to left and right views, its height value exists.
        // In the template message syntax, the views that are not drawn have to hide. (spec. since v3.5.2)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        visibility = if (width == 0) GONE else VISIBLE
    }
}
