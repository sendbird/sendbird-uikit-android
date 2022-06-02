package com.sendbird.uikit.model;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;

import com.sendbird.uikit.R;

/**
 * A configurations of text.
 * This provides a way of applying {@link com.google.android.material.resources.TextAppearance} into this class's properties.
 * A background color property is not applied automatically because background attributes is not a property of {@link com.google.android.material.resources.TextAppearance}.
 */
public class TextUIConfig implements Parcelable {
    private final int UNDEFINED_RESOURCE_ID = -1;
    @ColorInt
    private int textBackgroundColor;
    @ColorInt
    private int textColor;
    private int typefaceStyle;

    /**
     * Constructor
     */
    public TextUIConfig() {
        this(-1, -1, -1);
    }

    /**
     * Constructor
     */
    public TextUIConfig(@ColorInt int textColor) {
        this(textColor, -1, -1);
    }

    /**
     * Constructor
     */
    public TextUIConfig(@ColorInt int textColor, int typefaceStyle) {
        this(textColor, -1, typefaceStyle);
    }

    /**
     * Constructor
     */
    public TextUIConfig(@ColorInt int textColor, @ColorInt int textBackgroundColor, int typefaceStyle) {
        this.textColor = textColor;
        this.textBackgroundColor = textBackgroundColor;
        this.typefaceStyle = typefaceStyle;
    }

    /**
     * Constructor
     */
    protected TextUIConfig(@NonNull Parcel in) {
        textBackgroundColor = in.readInt();
        textColor = in.readInt();
        typefaceStyle = in.readInt();
    }

    /**
     * Returns a value of background color int.
     *
     * @return A background color int value.
     * @since 3.0.0
     */
    @ColorInt
    public int getTextBackgroundColor() {
        return textBackgroundColor;
    }

    /**
     * Returns a value of text color int.
     *
     * @return A text color int value.
     * @since 3.0.0
     */
    @ColorInt
    public int getTextColor() {
        return textColor;
    }

    /**
     * Returns a value of {@link android.graphics.Typeface}.
     * <li>Typeface.NORMAL</li>
     * <li>Typeface.BOLD</li>
     * <li>Typeface.ITALIC</li>
     * <li>Typeface.BOLD_ITALIC</li>
     *
     * @return A Typeface value of text.
     * @since 3.0.0
     */
    public int getTypefaceStyle() {
        return typefaceStyle;
    }

    /**
     * Apply values in the given {@link TextUIConfig} into this.
     *
     * @param config A {@link TextUIConfig} to apply.
     * @return This TextUIConfig object that applied with given data.
     * @since 3.0.0
     */
    @NonNull
    public TextUIConfig apply(@NonNull TextUIConfig config) {
        if (this.textBackgroundColor == UNDEFINED_RESOURCE_ID) {
            this.textBackgroundColor = config.getTextBackgroundColor();
        }
        if (this.textColor == UNDEFINED_RESOURCE_ID) {
            this.textColor = config.getTextColor();
        }

        if (this.typefaceStyle == UNDEFINED_RESOURCE_ID) {
            this.typefaceStyle = config.getTypefaceStyle();
        }
        return this;
    }

    /**
     * Apply values into Spannable text.
     *
     * @param spannable A spannable text to apply.
     * @param start A starting position to apply value.
     * @param end An end position to apply value.
     * @since 3.0.0
     */
    public void bind(@NonNull Spannable spannable, int start, int end) {
        if (this.textBackgroundColor != UNDEFINED_RESOURCE_ID) {
            spannable.setSpan(new BackgroundColorSpan(textBackgroundColor), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        if (this.textColor != UNDEFINED_RESOURCE_ID) {
            spannable.setSpan(new ForegroundColorSpan(textColor), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // typeface style starts from 0.(Typeface.Normal = 0)
        if (typefaceStyle != UNDEFINED_RESOURCE_ID) {
            spannable.setSpan(new StyleSpan(typefaceStyle), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * Merge attributes into this instance's values by given {@link com.google.android.material.resources.TextAppearance}.
     *
     * @param context the UI context.
     * @param textAppearance A TextAppearance to apply.
     * @since 3.0.0
     */
    public void mergeFromTextAppearance(@NonNull Context context, @StyleRes int textAppearance) {
        mergeFromTextAppearance(context, textAppearance, 0);
    }

    /**
     * Merge attributes into this instance's values by given {@link com.google.android.material.resources.TextAppearance} and background color.
     * @param context the context of view.
     * @param textAppearance A TextAppearance to apply.
     * @param textBackgroundColor A background color to apply.
     * @since 3.0.0
     */
    // A Background attribute of text is not included in TextAppearance.
    public void mergeFromTextAppearance(@NonNull Context context, @StyleRes int textAppearance, @ColorRes int textBackgroundColor) {
        if (textAppearance == 0) return;
        final TypedArray a = context.getTheme().obtainStyledAttributes(textAppearance, R.styleable.TextAppearance);
        try {
            if (this.textColor == UNDEFINED_RESOURCE_ID) {
                int textColorResId = a.getResourceId(R.styleable.TextAppearance_android_textColor, UNDEFINED_RESOURCE_ID);
                if (textColorResId != UNDEFINED_RESOURCE_ID) {
                    this.textColor = ContextCompat.getColor(context, textColorResId);
                }
            }
            if (this.typefaceStyle == UNDEFINED_RESOURCE_ID) {
                this.typefaceStyle = a.getInt(R.styleable.TextAppearance_android_textStyle, this.typefaceStyle);
            }
            if (this.textBackgroundColor == UNDEFINED_RESOURCE_ID && textBackgroundColor != 0) {
                this.textBackgroundColor = ContextCompat.getColor(context, textBackgroundColor);
            }
        } finally {
            a.recycle();
        }
    }

    public static final Creator<TextUIConfig> CREATOR = new Creator<TextUIConfig>() {
        @Override
        public TextUIConfig createFromParcel(Parcel in) {
            return new TextUIConfig(in);
        }

        @Override
        public TextUIConfig[] newArray(int size) {
            return new TextUIConfig[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(textBackgroundColor);
        dest.writeInt(textColor);
        dest.writeInt(typefaceStyle);
    }
}

