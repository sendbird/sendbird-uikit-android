package com.sendbird.uikit.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class SpannableStringBuilder {
    private final Context context;
    private CharSequence origin;

    public SpannableStringBuilder(@NonNull Context context) {
        this(context, "");
    }

    public SpannableStringBuilder(@NonNull Context context, @NonNull CharSequence origin) {
        this.context = context;
        this.origin = origin;
    }

    public SpannableStringBuilder(@NonNull Context context, @StringRes int textResId) {
        this.context = context;
        this.origin = context.getString(textResId);
    }

    @NonNull
    public SpannableStringBuilder addTextColorSpan(@StringRes int text, @ColorRes int color) {
        return addTextColorSpan(context.getString(text), color);
    }

    @NonNull
    public SpannableStringBuilder addTextColorSpan(@NonNull CharSequence text, @ColorRes int color) {
        SpannableString newValue = new SpannableString(text);
        newValue.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, color)), 0, newValue.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        origin = TextUtils.concat(origin, newValue);
        return this;
    }

    @NonNull
    public SpannableStringBuilder addUnderlineSpan(@NonNull Context context, @StringRes int text) {
        return addUnderlineSpan(context.getString(text));
    }

    @NonNull
    public SpannableStringBuilder addUnderlineSpan(@NonNull CharSequence text) {
        SpannableString newValue = new SpannableString(text);
        newValue.setSpan(new UnderlineSpan(), 0, newValue.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        origin = TextUtils.concat(origin, newValue);
        return this;
    }

    @NonNull
    public SpannableStringBuilder addStrikeSpan(@StringRes int text) {
        return addStrikeSpan(context.getString(text));
    }

    @NonNull
    public SpannableStringBuilder addStrikeSpan(@NonNull CharSequence text) {
        SpannableString newValue = new SpannableString(text);
        newValue.setSpan(new StrikethroughSpan(), 0, newValue.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        origin = TextUtils.concat(origin, newValue);
        return this;
    }

    @NonNull
    public SpannableStringBuilder setStrikeSpan(int start, int end) {
        SpannableString newValue = new SpannableString(origin);
        newValue.setSpan(new StrikethroughSpan(), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        origin = TextUtils.concat("", newValue);
        return this;
    }

    @NonNull
    public SpannableStringBuilder addSizeSpan(@NonNull CharSequence text, float proportion) {
        SpannableString newValue = new SpannableString(text);
        newValue.setSpan(new RelativeSizeSpan(proportion), 0, newValue.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        origin = TextUtils.concat(origin, newValue);
        return this;
    }

    @NonNull
    public SpannableStringBuilder setSizeSpan(float proportion, int start, int end) {
        SpannableString newValue = new SpannableString(origin);
        newValue.setSpan(new RelativeSizeSpan(proportion), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        origin = TextUtils.concat("", newValue);
        return this;
    }

    @NonNull
    public SpannableStringBuilder addImageSpan(@NonNull CharSequence text, @DrawableRes int imgResId) {
        SpannableString newValue = new SpannableString(origin);
        newValue.setSpan(new CenteredImageSpan(context, imgResId), 0, text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        origin = TextUtils.concat("", newValue);
        return this;
    }

    @NonNull
    public SpannableStringBuilder addImageSpan(@NonNull CharSequence text, @NonNull Drawable drawable) {
        SpannableString newValue = new SpannableString(origin);
        newValue.setSpan(new CenteredImageSpan(context, drawable), 0, text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        origin = TextUtils.concat("", newValue);
        return this;
    }

    @NonNull
    public SpannableStringBuilder addStyleSpan(@StringRes int text, int typeFace) {
        return addStyleSpan(context.getString(text), typeFace);
    }

    @NonNull
    public SpannableStringBuilder addStyleSpan(@NonNull CharSequence text, int typeFace) {
        SpannableString newValue = new SpannableString(text);
        newValue.setSpan(new StyleSpan(typeFace), 0, newValue.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        origin = TextUtils.concat(origin, newValue);
        return this;
    }

    @NonNull
    public SpannableStringBuilder addHighlightTextSpan(@NonNull String text, @NonNull String highlightText, @ColorRes int highlightBackgroundColor, @ColorRes int highlightForegroundColor) {
        int start = text.toLowerCase().indexOf(highlightText.toLowerCase());
        if (start >= 0) {
            SpannableString newValue = new SpannableString(text);
            newValue.setSpan(new BackgroundColorSpan(ContextCompat.getColor(context, highlightBackgroundColor)), start, start + highlightText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            newValue.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, highlightForegroundColor)), start, start + highlightText.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            origin = TextUtils.concat("", newValue);
        }
        return this;
    }

    @NonNull
    public SpannableStringBuilder addText(@StringRes int text){
        return addText(context.getString(text));
    }

    @NonNull
    public SpannableStringBuilder addText(@NonNull CharSequence text) {
        origin = TextUtils.concat(origin, text);
        return this;
    }

    public int length() {
        return origin.length();
    }

    @NonNull
    public CharSequence build() {
        return origin;
    }

    private static class CenteredImageSpan extends ImageSpan {
        private WeakReference<Drawable> drawableRef;

        public CenteredImageSpan(Context context, final int drawableRes) {
            super(context, drawableRes);
        }

        public CenteredImageSpan(Context context, final Drawable drawable) {
            super(drawable);
        }

        @Override
        public int getSize(@NonNull Paint paint, CharSequence text,
                           int start, int end,
                           Paint.FontMetricsInt fm) {
            Drawable d = getCachedDrawable();
            Rect rect = d.getBounds();

            if (fm != null) {
                Paint.FontMetricsInt pfm = paint.getFontMetricsInt();
                // keep it the same as paint's fm
                fm.ascent = pfm.ascent;
                fm.descent = pfm.descent;
                fm.top = pfm.top;
                fm.bottom = pfm.bottom;
            }

            return rect.right;
        }

        @Override
        public void draw(@NonNull Canvas canvas, CharSequence text,
                         int start, int end, float x,
                         int top, int y, int bottom, @NonNull Paint paint) {
            Drawable b = getCachedDrawable();
            canvas.save();

            int drawableHeight = b.getIntrinsicHeight();
            int fontAscent = paint.getFontMetricsInt().ascent;
            int fontDescent = paint.getFontMetricsInt().descent;
            int transY = bottom - b.getBounds().bottom +  // align bottom to bottom
                    (drawableHeight - fontDescent + fontAscent) / 2;  // align center to center

            canvas.translate(x, transY);
            b.draw(canvas);
            canvas.restore();
        }

        // Redefined locally because it is a private member from DynamicDrawableSpan
        private Drawable getCachedDrawable() {
            WeakReference<Drawable> wr = drawableRef;
            Drawable d = null;

            if (wr != null)
                d = wr.get();

            if (d == null) {
                d = getDrawable();
                drawableRef = new WeakReference<>(d);
            }

            return d;
        }
    }
}
