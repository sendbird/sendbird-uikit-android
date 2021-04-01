package com.sendbird.uikit.widgets;

import android.graphics.RectF;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.TextView;

import com.sendbird.uikit.log.Logger;

class SBLinkMovementMethod extends LinkMovementMethod {

    private OnLinkClickListener onLinkClickListener;
    private OnLinkLongClickListener onLinkLongClickListener;
    private LongPressTimer longPressTimer;
    private BackgroundColorSpan backgroundColorSpan;
    private ForegroundColorSpan foregroundColorSpan;

    private int activateTextViewHashcode = 0;
    private final RectF touchedLineBounded = new RectF();
    private boolean longPressedRegistered = false;
    private ClickableSpan prevLink = null;

    public interface OnLinkClickListener {
        /**
         * @param textView The TextView on which a click was registered.
         * @param link     The clicked link.
         * @return True if this click was handled. False to let Android handle the URL.
         */
        boolean onClick(TextView textView, String link);
    }

    public interface OnLinkLongClickListener {
        /**
         * @param textView The TextView on which a long-click was registered.
         * @param link     The long-clicked link.
         * @return True if this long-click was handled. False to let Android handle the URL (as a short-click).
         */
        boolean onLongClick(TextView textView, String link);
    }

    private static final class LongPressTimer implements Runnable {
        private OnTimerReachedListener onTimerReachedListener;

        private interface OnTimerReachedListener {
            void onTimerReached();
        }

        @Override
        public void run() {
            onTimerReachedListener.onTimerReached();
        }

        public void setOnTimerReachedListener(OnTimerReachedListener onTimerReachedListener) {
            this.onTimerReachedListener = onTimerReachedListener;
        }
    }

    private SBLinkMovementMethod() {}

    private SBLinkMovementMethod(OnLinkClickListener onLinkClickListener,
                                OnLinkLongClickListener onLinkLongClickListener,
                                int clickedLinkTextColor,
                                int clickedLinkBackgroundColor) {
        this.onLinkClickListener = onLinkClickListener;
        this.onLinkLongClickListener = onLinkLongClickListener;
        if (clickedLinkBackgroundColor != 0) this.backgroundColorSpan = new BackgroundColorSpan(clickedLinkBackgroundColor);
        if (clickedLinkTextColor != 0) this.foregroundColorSpan = new ForegroundColorSpan(clickedLinkTextColor);
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        if (activateTextViewHashcode != widget.hashCode()) {
            activateTextViewHashcode = widget.hashCode();
            widget.setAutoLinkMask(0);
        }

        int touchX = (int) event.getX();
        int touchY = (int) event.getY();

        touchX -= widget.getTotalPaddingLeft();
        touchY -= widget.getTotalPaddingTop();

        touchX += widget.getScrollX();
        touchY += widget.getScrollY();

        final Layout layout = widget.getLayout();
        final int touchedLine = layout.getLineForVertical(touchY);
        final int touchOffset = layout.getOffsetForHorizontal(touchedLine, touchX);

        touchedLineBounded.left = layout.getLineLeft(touchedLine);
        touchedLineBounded.top = layout.getLineTop(touchedLine);
        touchedLineBounded.right = layout.getLineWidth(touchedLine) + touchedLineBounded.left;
        touchedLineBounded.bottom = layout.getLineBottom(touchedLine);
        if (!touchedLineBounded.contains(touchX, touchY)) {
            clearTouchEvent(widget, buffer);
            return false;
        }

        final ClickableSpan[] links = buffer.getSpans(touchOffset, touchOffset, ClickableSpan.class);
        if (links.length <= 0 || links[0] == null) {
            clearTouchEvent(widget, buffer);
            return false;
        }

        ClickableSpan link = links[0];
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            prevLink = link;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Logger.d("ACTION_DOWN for link");
                drawClickedLink(link, buffer);
                widget.cancelLongPress();
                LongPressTimer.OnTimerReachedListener onTimerReachedListener = () -> {
                    longPressedRegistered = true;
                    widget.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    eraseClickedLink(buffer);

                    if (onLinkLongClickListener != null) {
                        onLinkLongClickListener.onLongClick(widget, buffer.toString().substring(buffer.getSpanStart(link), buffer.getSpanEnd(link)));
                    } else {
                        link.onClick(widget);
                    }
                };
                longPressTimer = new LongPressTimer();
                longPressTimer.setOnTimerReachedListener(onTimerReachedListener);
                widget.postDelayed(longPressTimer, ViewConfiguration.getLongPressTimeout());
                return true;

            case MotionEvent.ACTION_UP:
                Logger.d("ACTION_UP for link");
                if (!longPressedRegistered && link == prevLink) {
                    if (onLinkClickListener != null) {
                        onLinkClickListener.onClick(widget, buffer.toString().substring(buffer.getSpanStart(link), buffer.getSpanEnd(link)));
                    } else {
                        link.onClick(widget);
                    }
                }

                clearTouchEvent(widget, buffer);
                return true;

            case MotionEvent.ACTION_MOVE:
                if (!longPressedRegistered) {
                    drawClickedLink(link, buffer);
                }

                if (link != prevLink) {
                    clearTouchEvent(widget, buffer);
                }
                return true;
        }

        clearTouchEvent(widget, buffer);
        return false;
    }

    private void drawClickedLink(ClickableSpan clickableSpan, Spannable text) {
        int spanStart = text.getSpanStart(clickableSpan);
        int spanEnd = text.getSpanEnd(clickableSpan);
        if (backgroundColorSpan != null) {
            text.setSpan(backgroundColorSpan, spanStart, spanEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        if (foregroundColorSpan != null) {
            text.setSpan(foregroundColorSpan, spanStart, spanEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        Selection.setSelection(text, spanStart, spanEnd);
    }

    private void eraseClickedLink(Spannable text) {
        text.removeSpan(backgroundColorSpan);
        text.removeSpan(foregroundColorSpan);
        Selection.removeSelection(text);
    }

    private void clearTouchEvent(TextView widget, Spannable buffer) {
        eraseClickedLink(buffer);
        longPressedRegistered = false;
        widget.removeCallbacks(longPressTimer);
        longPressTimer = null;
    }

    public static class Builder {
        private OnLinkClickListener onLinkClickListener;
        private OnLinkLongClickListener onLinkLongClickListener;
        private int clickedLinkTextColor;
        private int clickedLinkBackgroundColor;

        public Builder setOnLinkClickListener(OnLinkClickListener onLinkClickListener) {
            this.onLinkClickListener = onLinkClickListener;
            return this;
        }

        public Builder setOnLinkLongClickListener(OnLinkLongClickListener onLinkLongClickListener) {
            this.onLinkLongClickListener = onLinkLongClickListener;
            return this;
        }

        public Builder setClickedLinkTextColor(int clickedLinkTextColor) {
            this.clickedLinkTextColor = clickedLinkTextColor;
            return this;
        }

        public Builder setClickedLinkBackgroundColor(int clickedLinkBackgroundColor) {
            this.clickedLinkBackgroundColor = clickedLinkBackgroundColor;
            return this;
        }

        public SBLinkMovementMethod create() {
            return new SBLinkMovementMethod(onLinkClickListener,
                    onLinkLongClickListener,
                    clickedLinkTextColor,
                    clickedLinkBackgroundColor);
        }
    }
}
