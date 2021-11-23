package com.sendbird.uikit.widgets;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class PagerRecyclerView extends ThemeableRecyclerView {
    private LinearLayoutManager layoutManager;
    private final OnScrollListener scrollListener = new OnScrollListener();

    public PagerRecyclerView(@NonNull Context context) {
        super(context);
    }

    public PagerRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PagerRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setLayoutManager(LayoutManager layoutManager) {
        if (!(layoutManager instanceof LinearLayoutManager)) throw new IllegalArgumentException("LinearLayoutManager supports only.");
        this.layoutManager = (LinearLayoutManager) layoutManager;
        if (this.scrollListener != null) {
            this.scrollListener.setLayoutManager(this.layoutManager);
        }
        super.setLayoutManager(layoutManager);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Nullable
    @Override
    public LinearLayoutManager getLayoutManager() {
        return layoutManager;
    }

    public void setPager(@NonNull Pageable<?> pager) {
        this.scrollListener.setPager(pager);
        this.scrollListener.setLayoutManager(this.layoutManager);
        addOnScrollListener(this.scrollListener);
    }

    public void setThreshold(int threshold) {
        this.scrollListener.setThreshold(threshold);
    }

    public int findFirstVisibleItemPosition() {
        return layoutManager.findFirstVisibleItemPosition();
    }

    public int findLastVisibleItemPosition() {
        return layoutManager.findLastVisibleItemPosition();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeOnScrollListener(this.scrollListener);
        this.scrollListener.dispose();
    }

    public void setOnScrollEndDetectListener(@Nullable OnScrollEndDetectListener scrollEndDetectListener) {
        this.scrollListener.setOnScrollEndDetectListener(scrollEndDetectListener);
    }

    private final static class OnScrollListener extends RecyclerView.OnScrollListener {
        private int threshold = 1;
        private Pageable<?> pager;
        private LinearLayoutManager layoutManager;
        private OnScrollEndDetectListener scrollEndDetectListener;
        private final ExecutorService topLoadingWorker = Executors.newSingleThreadExecutor();
        private final ExecutorService bottomLoadingWorker = Executors.newSingleThreadExecutor();
        private final AtomicBoolean topLoading = new AtomicBoolean(false);
        private final AtomicBoolean bottomLoading = new AtomicBoolean(false);

        public OnScrollListener() {
        }

        public void setPager(@NonNull Pageable<?> pager) {
            this.pager = pager;
        }

        public void setLayoutManager(@Nullable LinearLayoutManager layoutManager) {
            this.layoutManager = layoutManager;
        }

        public void setOnScrollEndDetectListener(@Nullable OnScrollEndDetectListener scrollEndDetectListener) {
            this.scrollEndDetectListener = scrollEndDetectListener;
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
            int itemCount = layoutManager.getItemCount();

            if (!recyclerView.canScrollVertically(ScrollDirection.Bottom.getDirection())) {
                if (scrollEndDetectListener != null) {
                    scrollEndDetectListener.onScrollEnd(ScrollDirection.Bottom);
                }
            }

            if (!recyclerView.canScrollVertically(ScrollDirection.Top.getDirection())) {
                if (scrollEndDetectListener != null) {
                    scrollEndDetectListener.onScrollEnd(ScrollDirection.Top);
                }
            }

            if (pager == null) return;
            final boolean reverseLayout = layoutManager.getReverseLayout();
            final boolean topLoadMore = pager.hasPrevious() && (reverseLayout ? itemCount - lastVisibleItemPosition <= threshold : firstVisibleItemPosition <= threshold);
            if (!topLoading.get() && topLoadMore) {
                topLoading.set(true);
                topLoadingWorker.submit(() -> {
                    try {
                        pager.loadPrevious();
                    } catch (Exception ignore) {
                    } finally {
                        topLoading.set(false);
                    }
                });
            }
            //final boolean bottomLoadMore = !(reverseLayout? !pager.hasNext() : !pager.hasPrevious());
            final boolean bottomLoadMore = pager.hasNext() && (reverseLayout ? firstVisibleItemPosition <= threshold : itemCount - lastVisibleItemPosition <= threshold);
            if (!bottomLoading.get() && bottomLoadMore) {
                bottomLoading.set(true);
                bottomLoadingWorker.submit(() -> {
                    try {
                        pager.loadNext();
                    } catch (Exception ignore) {
                    } finally {
                        bottomLoading.set(false);
                    }
                });
            }
        }

        public void setThreshold(int threshold) {
            if (threshold <= 0) {
                throw new IllegalArgumentException("illegal threshold: " + threshold);
            }
            this.threshold = threshold;
        }

        public void dispose() {
            this.topLoadingWorker.shutdown();
            this.bottomLoadingWorker.shutdown();
        }
    }

    public interface Pageable<T> {
        /**
         * Synchronized function call must be used.
         */
        T loadPrevious() throws Exception;

        /**
         * Synchronized function call must be used.
         */
        T loadNext() throws Exception;

        boolean hasNext();
        boolean hasPrevious();
    }

    public interface OnScrollEndDetectListener {
        void onScrollEnd(ScrollDirection direction);
    }

    public enum ScrollDirection {
        Top(-1), Bottom(1);
        private final int value;
        ScrollDirection(int value) {
            this.value = value;
        }

        public int getDirection() {
            return value;
        }
    }
}
