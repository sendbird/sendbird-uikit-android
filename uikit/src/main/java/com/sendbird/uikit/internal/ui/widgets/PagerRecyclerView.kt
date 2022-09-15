package com.sendbird.uikit.internal.ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.uikit.interfaces.OnPagedDataLoader
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

internal class PagerRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ThemeableRecyclerView(context, attrs, defStyle) {
    private var layoutManager: LinearLayoutManager? = null
    private val onScrollListener: OnScrollListener by lazy { OnScrollListener(layoutManager) }

    override fun setLayoutManager(layoutManager: LayoutManager?) {
        require(layoutManager is LinearLayoutManager) { "LinearLayoutManager supports only." }
        this.layoutManager = layoutManager
        super.setLayoutManager(layoutManager)
    }

    override fun getLayoutManager(): LinearLayoutManager? = layoutManager

    fun setPager(pager: OnPagedDataLoader<*>) {
        onScrollListener.pager = pager
        onScrollListener.layoutManager = layoutManager
        addOnScrollListener(onScrollListener)
    }

    fun setThreshold(threshold: Int) {
        onScrollListener.setThreshold(threshold)
    }

    fun findFirstVisibleItemPosition(): Int {
        return layoutManager?.findFirstVisibleItemPosition() ?: 0
    }

    fun findLastVisibleItemPosition(): Int {
        return layoutManager?.findLastVisibleItemPosition() ?: 0
    }

    fun setOnScrollEndDetectListener(scrollEndDetectListener: OnScrollEndDetectListener?) {
        onScrollListener.scrollEndDetectListener = scrollEndDetectListener
    }

    private class OnScrollListener constructor(var layoutManager: LinearLayoutManager?) :
        RecyclerView.OnScrollListener() {
        private var threshold = 1
        var pager: OnPagedDataLoader<*>? = null
        var scrollEndDetectListener: OnScrollEndDetectListener? = null
        private val topLoadingWorker = Executors.newSingleThreadExecutor()
        private val bottomLoadingWorker = Executors.newSingleThreadExecutor()
        private val topLoading = AtomicBoolean(false)
        private val bottomLoading = AtomicBoolean(false)

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            layoutManager?.run {
                val lastVisibleItemPosition = findLastVisibleItemPosition()
                val firstVisibleItemPosition = findFirstVisibleItemPosition()
                if (!recyclerView.canScrollVertically(ScrollDirection.Bottom.direction)) {
                    scrollEndDetectListener?.onScrollEnd(ScrollDirection.Bottom)
                }
                if (!recyclerView.canScrollVertically(ScrollDirection.Top.direction)) {
                    scrollEndDetectListener?.onScrollEnd(ScrollDirection.Top)
                }
                pager?.run {
                    val topLoadMore =
                        hasPrevious() && if (reverseLayout) itemCount - lastVisibleItemPosition <= threshold else firstVisibleItemPosition <= threshold
                    if (!topLoading.get() && topLoadMore) {
                        topLoading.set(true)
                        topLoadingWorker.submit {
                            try {
                                loadPrevious()
                            } catch (ignore: Exception) {
                            } finally {
                                topLoading.set(false)
                            }
                        }
                    }

                    // final boolean bottomLoadMore = !(reverseLayout? !pager.hasNext() : !pager.hasPrevious());
                    val bottomLoadMore =
                        hasNext() && if (reverseLayout) firstVisibleItemPosition <= threshold else itemCount - lastVisibleItemPosition <= threshold
                    if (!bottomLoading.get() && bottomLoadMore) {
                        bottomLoading.set(true)
                        bottomLoadingWorker.submit {
                            try {
                                loadNext()
                            } catch (ignore: Exception) {
                            } finally {
                                bottomLoading.set(false)
                            }
                        }
                    }
                }
            }
        }

        fun setThreshold(threshold: Int) {
            require(threshold > 0) { "illegal threshold: $threshold" }
            this.threshold = threshold
        }

        fun dispose() {
            topLoadingWorker.shutdown()
            bottomLoadingWorker.shutdown()
        }
    }

    interface OnScrollEndDetectListener {
        fun onScrollEnd(direction: ScrollDirection)
    }

    enum class ScrollDirection(val direction: Int) {
        Top(-1), Bottom(1);
    }
}
