package com.sendbird.uikit.internal.ui.widgets

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min

internal class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {
    private val touchMatrix = Matrix()
    private val matrixValues = FloatArray(9)
    private val scaleDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector
    private var state = State.NONE
    private var minScale = 1f
    private var maxScale = 3f
    private var superMinScale = SUPER_MIN_MULTIPLIER * minScale
    private var superMaxScale = SUPER_MAX_MULTIPLIER * maxScale
    private var currentZoom = 1f
    private var viewWidth = 0
    private var viewHeight = 0
    private val lastTouchPoint = PointF()
    private var isRotateImageToFitScreen = false
    private var orientation = 0
    private var orientationJustChanged = false
    private var onDrawReady = false
    private enum class State {
        NONE, // No gesture
        DRAG, // Dragging/panning
        ZOOM, // Pinch zooming
        ANIMATING // Zoom animation in progress
    }

    // Animation
    private var zoomAnimator: ZoomAnimator? = null

    var isZoomEnabled = true
    var onSingleTapListener: (() -> Unit)? = null

    init {
        orientation = resources.configuration.orientation
        super.setClickable(true)
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
        gestureDetector = GestureDetector(context, GestureListener())
        imageMatrix = touchMatrix
        scaleType = ScaleType.MATRIX
        setOnTouchListener(InternalTouchListener())
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        resetZoom()
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        resetZoom()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        resetZoom()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        resetZoom()
    }

    fun resetZoom() {
        currentZoom = 1f
        fitImageToView()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Cancel any ongoing animations
        cancelAnimation()
        val newOrientation = resources.configuration.orientation
        if (newOrientation != orientation) {
            orientationJustChanged = true
            orientation = newOrientation
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Clean up animations to prevent memory leaks
        cancelAnimation()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h
        if (!onDrawReady) {
            onDrawReady = true
        }
        fitImageToView()
    }

    private fun fitImageToView() {
        drawable ?: return
        touchMatrix.reset()
        currentZoom = 1f
        val dw = if (isRotateImageToFitScreen && orientationMismatch()) drawable!!.intrinsicHeight else drawable!!.intrinsicWidth
        val dh = if (isRotateImageToFitScreen && orientationMismatch()) drawable!!.intrinsicWidth else drawable!!.intrinsicHeight
        val widthScale = viewWidth.toFloat() / dw.toFloat()
        val heightScale = viewHeight.toFloat() / dh.toFloat()
        val scale = min(widthScale, heightScale)
        if (isRotateImageToFitScreen && orientationMismatch()) {
            touchMatrix.setRotate(90f)
            touchMatrix.postTranslate(dw.toFloat(), 0f)
        }
        touchMatrix.postScale(scale, scale)
        val scaledW = scale * dw
        val scaledH = scale * dh
        val offsetX = (viewWidth - scaledW) / 2f
        val offsetY = (viewHeight - scaledH) / 2f
        touchMatrix.postTranslate(offsetX, offsetY)
        imageMatrix = touchMatrix
    }

    private fun orientationMismatch(): Boolean {
        val dw = drawable?.intrinsicWidth ?: 1
        val dh = drawable?.intrinsicHeight ?: 1
        return (viewWidth > viewHeight) != (dw > dh)
    }

    private fun setState(newState: State) {
        state = newState
    }

    private inner class InternalTouchListener : OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (drawable == null) return false
            if (!isZoomEnabled) return false

            // Cancel animation on touch down
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (state == State.ANIMATING) cancelAnimation()
            }

            // Block other gestures during animation
            if (state == State.ANIMATING) return true

            // Handle GestureDetector (double-tap)
            gestureDetector.onTouchEvent(event)

            scaleDetector.onTouchEvent(event)
            val currentPoint = PointF(event.x, event.y)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchPoint.set(currentPoint)
                    setState(State.DRAG)
                    // When image is zoomed, request parent (ViewPager2) not to intercept touch events initially
                    if (currentZoom > 1f) {
                        parent?.requestDisallowInterceptTouchEvent(true)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (state == State.DRAG) {
                        val deltaX = currentPoint.x - lastTouchPoint.x
                        val deltaY = currentPoint.y - lastTouchPoint.y

                        // Check if image can be scrolled in the drag direction
                        if (currentZoom > 1f) {
                            val canScrollX = canScrollHorizontally(deltaX)
                            val canScrollY = canScrollVertically(deltaY)

                            // Only disallow parent intercept if image can be scrolled
                            // When image reaches edge, allow parent (ViewPager2) to handle swipe
                            parent?.requestDisallowInterceptTouchEvent(canScrollX || canScrollY)
                        }

                        touchMatrix.postTranslate(
                            getFixDragTrans(deltaX, viewWidth.toFloat(), getCurrentImageWidth()),
                            getFixDragTrans(deltaY, viewHeight.toFloat(), getCurrentImageHeight())
                        )
                        fixTrans()
                        lastTouchPoint.set(currentPoint)
                        imageMatrix = touchMatrix
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    setState(State.NONE)
                    // Allow parent to intercept touch events again
                    parent?.requestDisallowInterceptTouchEvent(false)
                }
            }
            return true
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            setState(State.ZOOM)
            return true
        }
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val sf = detector.scaleFactor
            scaleImage(sf, detector.focusX, detector.focusY, true)
            return true
        }
        override fun onScaleEnd(detector: ScaleGestureDetector) {
            super.onScaleEnd(detector)
            setState(State.NONE)
            var target = currentZoom
            if (currentZoom > maxScale) {
                target = maxScale
            } else if (currentZoom < minScale) {
                target = minScale
            }
            if (target != currentZoom) {
                val df = target / currentZoom
                scaleImage(df, (viewWidth / 2f), (viewHeight / 2f), true)
            }
        }
    }

    private fun scaleImage(scaleFactor: Float, focusX: Float, focusY: Float, stretch: Boolean) {
        val orig = currentZoom
        var delta = scaleFactor
        val lower = if (stretch) superMinScale else minScale
        val upper = if (stretch) superMaxScale else maxScale
        currentZoom *= delta
        if (currentZoom > upper) {
            currentZoom = upper
            delta = upper / orig
        } else if (currentZoom < lower) {
            currentZoom = lower
            delta = lower / orig
        }
        touchMatrix.postScale(delta, delta, focusX, focusY)
        fixScaleTrans()
        imageMatrix = touchMatrix
    }

    private fun fixScaleTrans() {
        fixTrans()
        touchMatrix.getValues(matrixValues)
        val iw = getCurrentImageWidth()
        val ih = getCurrentImageHeight()
        if (iw < viewWidth) {
            matrixValues[Matrix.MTRANS_X] = (viewWidth - iw) / 2
        }
        if (ih < viewHeight) {
            matrixValues[Matrix.MTRANS_Y] = (viewHeight - ih) / 2
        }
        touchMatrix.setValues(matrixValues)
    }

    private fun fixTrans() {
        touchMatrix.getValues(matrixValues)
        val x = matrixValues[Matrix.MTRANS_X]
        val y = matrixValues[Matrix.MTRANS_Y]
        val fixX = getFixTrans(x, viewWidth.toFloat(), getCurrentImageWidth())
        val fixY = getFixTrans(y, viewHeight.toFloat(), getCurrentImageHeight())
        if (fixX != 0f || fixY != 0f) {
            touchMatrix.postTranslate(fixX, fixY)
        }
    }

    private fun getFixTrans(trans: Float, viewSize: Float, contentSize: Float): Float {
        return if (contentSize <= viewSize) {
            viewSize / 2 - (contentSize / 2 + trans)
        } else {
            if (trans > 0) {
                -trans
            } else if (trans < viewSize - contentSize) {
                (viewSize - contentSize) - trans
            } else {
                0f
            }
        }
    }

    private fun getFixDragTrans(delta: Float, viewSize: Float, contentSize: Float): Float {
        return if (contentSize <= viewSize) 0f else delta
    }

    private fun canScrollHorizontally(deltaX: Float): Boolean {
        if (currentZoom <= 1f) return false

        touchMatrix.getValues(matrixValues)
        val currentX = matrixValues[Matrix.MTRANS_X]
        val imageWidth = getCurrentImageWidth()

        if (imageWidth <= viewWidth) return false

        if (deltaX > 0) {
            return currentX < 0
        } else if (deltaX < 0) {
            val rightEdge = currentX + imageWidth
            return rightEdge > viewWidth
        }

        return false
    }

    private fun canScrollVertically(deltaY: Float): Boolean {
        if (currentZoom <= 1f) return false

        touchMatrix.getValues(matrixValues)
        val currentY = matrixValues[Matrix.MTRANS_Y]
        val imageHeight = getCurrentImageHeight()

        if (imageHeight <= viewHeight) return false

        if (deltaY > 0) {
            return currentY < 0
        } else if (deltaY < 0) {
            val bottomEdge = currentY + imageHeight
            return bottomEdge > viewHeight
        }

        return false
    }

    private fun getCurrentImageWidth(): Float {
        touchMatrix.getValues(matrixValues)
        val scaleX = matrixValues[Matrix.MSCALE_X]
        val dw = if (isRotateImageToFitScreen && orientationMismatch()) drawable?.intrinsicHeight ?: 0
        else drawable?.intrinsicWidth ?: 0
        return dw * scaleX
    }

    private fun getCurrentImageHeight(): Float {
        touchMatrix.getValues(matrixValues)
        val scaleY = matrixValues[Matrix.MSCALE_Y]
        val dh = if (isRotateImageToFitScreen && orientationMismatch()) drawable?.intrinsicWidth ?: 0
        else drawable?.intrinsicHeight ?: 0
        return dh * scaleY
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            onSingleTapListener?.invoke()
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (!isZoomEnabled || drawable == null) return false
            performDoubleTapZoom(e.x, e.y)
            return true
        }
    }

    private fun performDoubleTapZoom(focusX: Float, focusY: Float) {
        val targetZoom = getNextZoomLevel()
        animateZoomTo(targetZoom, focusX, focusY)
    }

    private fun getNextZoomLevel(): Float {
        return when {
            currentZoom < 1.5f -> 2.0f
            currentZoom < 2.5f -> 3.0f
            else -> 1.0f
        }
    }

    private fun animateZoomTo(targetZoom: Float, focusX: Float, focusY: Float) {
        zoomAnimator?.cancel()
        zoomAnimator = ZoomAnimator(currentZoom, targetZoom, focusX, focusY)
        zoomAnimator?.start()
    }

    private fun cancelAnimation() {
        zoomAnimator?.cancel()
        zoomAnimator = null
        if (state == State.ANIMATING) {
            setState(State.NONE)
        }
    }

    private inner class ZoomAnimator(
        private val startZoom: Float,
        private val targetZoom: Float,
        private val focusX: Float,
        private val focusY: Float
    ) {
        private var animator: ValueAnimator? = null

        fun start() {
            cancel()

            animator = ValueAnimator.ofFloat(startZoom, targetZoom).apply {
                duration = ANIMATION_DURATION
                interpolator = DecelerateInterpolator()
                addUpdateListener { animation ->
                    val animatedValue = animation.animatedValue as Float
                    val scaleFactor = animatedValue / currentZoom
                    scaleImage(scaleFactor, focusX, focusY, false)
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        setState(State.ANIMATING)
                    }
                    override fun onAnimationEnd(animation: Animator) {
                        setState(State.NONE)
                    }
                    override fun onAnimationCancel(animation: Animator) {
                        setState(State.NONE)
                    }
                })
                start()
            }
        }

        fun cancel() {
            animator?.cancel()
            animator = null
        }
    }

    companion object {
        private const val SUPER_MIN_MULTIPLIER = 0.75f
        private const val SUPER_MAX_MULTIPLIER = 1.25f
        private const val ANIMATION_DURATION = 300L
    }
}
