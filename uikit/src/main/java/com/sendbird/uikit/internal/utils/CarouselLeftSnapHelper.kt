package com.sendbird.uikit.internal.utils

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

internal class CarouselLeftSnapHelper : LinearSnapHelper() {
    private var horizontalHelper: OrientationHelper? = null
    private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        val helper = horizontalHelper
        return if (helper == null) {
            OrientationHelper.createHorizontalHelper(layoutManager)
        } else {
            if (helper.layoutManager !== layoutManager) {
                OrientationHelper.createHorizontalHelper(layoutManager).also {
                    horizontalHelper = it
                }
            } else {
                helper
            }
        }
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
        if (layoutManager !is LinearLayoutManager) return null
        if (layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount - 1) {
            return null
        }

        return findLeftClosestView(layoutManager, getHorizontalHelper(layoutManager))
    }

    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray? {
        if (layoutManager.canScrollVertically()) return super.calculateDistanceToFinalSnap(layoutManager, targetView)
        val out = IntArray(2)
        out[0] = distanceToLeft(targetView, getHorizontalHelper(layoutManager))
        out[1] = 0 // vertical position always zero
        return out
    }

    private fun distanceToLeft(targetView: View, orientationHelper: OrientationHelper): Int {
        val childLeft: Int = orientationHelper.getDecoratedStart(targetView)
        val containerLeft: Int = orientationHelper.startAfterPadding
        return childLeft - containerLeft
    }

    private fun findLeftClosestView(
        layoutManager: RecyclerView.LayoutManager,
        helper: OrientationHelper
    ): View? {
        val childCount = layoutManager.childCount
        if (childCount == 0) {
            return null
        }
        var closestChild: View? = null
        var absClosest = Int.MAX_VALUE
        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i) ?: continue
            val distanceToLeft = distanceToLeft(child, helper)
            val absDistance = abs(distanceToLeft)
            if (absDistance < absClosest) {
                absClosest = absDistance
                closestChild = child
            }
        }

        return closestChild
    }
}
