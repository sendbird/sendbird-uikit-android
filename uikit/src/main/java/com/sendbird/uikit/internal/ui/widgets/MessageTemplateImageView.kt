package com.sendbird.uikit.internal.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import com.sendbird.uikit.internal.RoundableView
import com.sendbird.uikit.internal.extensions.intToDp

internal open class MessageTemplateImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr), RoundableView {
    private var targetWidth: Int = 0
    private var targetHeight: Int = 0
    private lateinit var rectF: RectF
    private val path: Path = Path()
    private var strokePaint: Paint? = null
    override var radius: Float = 0F

    fun setSize(width: Int, height: Int) {
        targetWidth = width
        targetHeight = height
        requestLayout()
    }

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
        rectF = RectF(0f, 0f, w.toFloat(), h.toFloat())
        resetPath()
    }

    override fun draw(canvas: Canvas) {
        val save = canvas.save()
        canvas.clipPath(path)
        super.draw(canvas)
        strokePaint?.let { canvas.drawRoundRect(rectF, radius, radius, it) }
        canvas.restoreToCount(save)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val save = canvas.save()
        canvas.clipPath(path)
        super.dispatchDraw(canvas)
        strokePaint?.let { canvas.drawRoundRect(rectF, radius, radius, it) }
        canvas.restoreToCount(save)
    }

    private fun resetPath() {
        path.reset()
        path.addRoundRect(rectF, radius, radius, Path.Direction.CW)
        path.close()
    }
}
