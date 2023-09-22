package com.sendbird.uikit.model;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.TypefaceSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.FontRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.sendbird.uikit.internal.model.TypefaceSpanEx;

/**
 * A configurations of text.
 * This provides a way of applying {@link com.google.android.material.resources.TextAppearance} into this class's properties.
 * A background color property is not applied automatically because background attributes is not a property of {@link com.google.android.material.resources.TextAppearance}.
 */
public class TextUIConfig implements Parcelable {
    public static final int UNDEFINED_RESOURCE_ID = -1;
    @ColorInt
    private int textBackgroundColor;
    @ColorInt
    private int textColor;
    private int textStyle;
    // pixel size
    private int textSize;
    @Nullable
    private String familyName;
    @FontRes
    private int customFontRes;

    private TextUIConfig(@ColorInt int textColor, @ColorInt int textBackgroundColor, int textStyle, int textSize, @Nullable String familyName, @FontRes int customFontRes) {
        this.textColor = textColor;
        this.textBackgroundColor = textBackgroundColor;
        this.textStyle = textStyle;
        this.textSize = textSize;
        this.familyName = familyName;
        this.customFontRes = customFontRes;
    }

    /**
     * Constructor
     */
    protected TextUIConfig(@NonNull Parcel in) {
        textBackgroundColor = in.readInt();
        textColor = in.readInt();
        textStyle = in.readInt();
        textSize = in.readInt();
        familyName = in.readString();
        customFontRes = in.readInt();
    }

    /**
     * Returns a value of background color int.
     *
     * @return A background color int value.
     * since 3.0.0
     */
    @ColorInt
    public int getTextBackgroundColor() {
        return textBackgroundColor;
    }

    /**
     * Returns a value of text color int.
     *
     * @return A text color int value.
     * since 3.0.0
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
     * @return A text style of text.
     * since 3.0.0
     */
    public int getTextStyle() {
        return textStyle;
    }

    /**
     * Returns a value of text size int.
     *
     * @return A text size int value.
     * since 3.1.1
     */
    public int getTextSize() {
        return textSize;
    }

    /**
     * Returns a value of text typeface family.
     *
     * @return A typeface family name value.
     * since 3.1.1
     */
    @Nullable
    public String getFamilyName() {
        return familyName;
    }

    /**
     * Returns a custom font res ID.
     *
     * @return A custom font resource ID.
     * since 3.2.1
     */
    public int getCustomFontRes() {
        return customFontRes;
    }

    /**
     * Apply values in the given {@link TextUIConfig} into this.
     *
     * @param config A {@link TextUIConfig} to apply.
     * @return This TextUIConfig object that applied with given data.
     * since 3.0.0
     */
    @NonNull
    public TextUIConfig apply(@NonNull TextUIConfig config) {
        if (config.getTextBackgroundColor() != UNDEFINED_RESOURCE_ID) {
            this.textBackgroundColor = config.getTextBackgroundColor();
        }

        if (config.getTextColor() != UNDEFINED_RESOURCE_ID) {
            this.textColor = config.getTextColor();
        }
        if (config.getTextStyle() != UNDEFINED_RESOURCE_ID) {
            this.textStyle = config.getTextStyle();
        }
        if (config.getTextSize() != UNDEFINED_RESOURCE_ID) {
            this.textSize = config.getTextSize();
        }
        if (config.getFamilyName() != null) {
            this.familyName = config.getFamilyName();
        }
        if (config.getCustomFontRes() != UNDEFINED_RESOURCE_ID) {
            this.customFontRes = config.getCustomFontRes();
        }
        return this;
    }

    /**
     * Apply values into given whole text.
     *
     * @param text A text to apply.
     * since 3.2.1
     */
    @NonNull
    public SpannableString apply(@NonNull Context context, @NonNull String text) {
        final SpannableString spannableString = new SpannableString(text);
        bind(context, spannableString, 0, text.length());
        return spannableString;
    }

    /**
     * Apply values into Spannable text.
     *
     * @param spannable A spannable text to apply.
     * @param start A starting position to apply value.
     * @param end An end position to apply value.
     * since 3.0.0
     */
    public void bind(@NonNull Context context, @NonNull Spannable spannable, int start, int end) {
        if (this.textBackgroundColor != UNDEFINED_RESOURCE_ID) {
            spannable.setSpan(new BackgroundColorSpan(textBackgroundColor), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        if (this.textColor != UNDEFINED_RESOURCE_ID) {
            spannable.setSpan(new ForegroundColorSpan(textColor), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // typeface style starts from 0.(Typeface.Normal = 0)
        if (textStyle != UNDEFINED_RESOURCE_ID) {
            spannable.setSpan(new StyleSpan(textStyle), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (textSize != UNDEFINED_RESOURCE_ID) {
            spannable.setSpan(new AbsoluteSizeSpan(textSize), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (this.familyName != null) {
            spannable.setSpan(new TypefaceSpan(familyName), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (this.customFontRes != UNDEFINED_RESOURCE_ID) {
            try {
                Typeface font = ResourcesCompat.getFont(context, customFontRes);
                if (font != null) {
                    spannable.setSpan(new TypefaceSpanEx(this.familyName != null ? this.familyName : "", font), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } catch (Resources.NotFoundException ignore) {
            }
        }
    }

    /**
     * Merge attributes into this instance's values by given {@link com.google.android.material.resources.TextAppearance}.
     *
     * @param context the UI context.
     * @param textAppearance A TextAppearance to apply.
     * since 3.0.0
     */
    public void mergeFromTextAppearance(@NonNull Context context, @StyleRes int textAppearance) {
        mergeFromTextAppearance(context, textAppearance, 0);
    }

    /**
     * Merge attributes into this instance's values by given {@link com.google.android.material.resources.TextAppearance} and background color.
     * @param context the context of view.
     * @param textAppearance A TextAppearance to apply.
     * @param textBackgroundColor A background color to apply.
     * since 3.0.0
     */
    // A Background attribute of text is not included in TextAppearance.
    public void mergeFromTextAppearance(@NonNull Context context, @StyleRes int textAppearance, @ColorRes int textBackgroundColor) {
        if (this.textBackgroundColor == UNDEFINED_RESOURCE_ID && textBackgroundColor != 0) {
            this.textBackgroundColor = ContextCompat.getColor(context, textBackgroundColor);
        }

        if (textAppearance == 0) return;
        final TextAppearanceSpan span = new TextAppearanceSpan(context, textAppearance);
        if (this.textSize == UNDEFINED_RESOURCE_ID) {
            this.textSize = span.getTextSize();
        }
        if (this.textStyle == UNDEFINED_RESOURCE_ID) {
            this.textStyle = span.getTextStyle();
        }
        if (this.textColor == UNDEFINED_RESOURCE_ID) {
            this.textColor = span.getTextColor() != null ? span.getTextColor().getDefaultColor() : UNDEFINED_RESOURCE_ID;
        }
        if (this.familyName == null) {
            this.familyName = span.getFamily();
        }
    }

    /**
     * Generates typeface from text style.
     *
     * @return The typeface instance
     * since 3.1.1
     */
    @NonNull
    public Typeface generateTypeface() {
        String familyName = this.familyName != null ? this.familyName : "";
        Typeface typeface = Typeface.create(familyName, Typeface.NORMAL);
        if (this.textStyle >= 0) {
            switch (this.textStyle) {
                case Typeface.NORMAL:
                    typeface = Typeface.create(familyName, Typeface.NORMAL);
                    break;
                case Typeface.BOLD:
                    typeface = Typeface.create(familyName, Typeface.BOLD);
                    break;
                case Typeface.ITALIC:
                    typeface = Typeface.create(familyName, Typeface.ITALIC);
                    break;
                case Typeface.BOLD_ITALIC:
                    typeface = Typeface.create(familyName, Typeface.BOLD_ITALIC);
                    break;
            }
        }
        return typeface;
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
        dest.writeInt(textStyle);
        dest.writeInt(textSize);
        dest.writeString(familyName);
        dest.writeInt(customFontRes);
    }

    public static class Builder {
        @ColorInt
        private int textBackgroundColor = -1;
        @ColorInt
        private int textColor = -1;
        private int textStyle = -1;
        // pixel size
        private int textSize = -1;
        @Nullable
        private String familyName;
        @FontRes
        private int customFontRes;

        /**
         * Constructor
         *
         * since 3.1.1
         */
        public Builder() {}

        /**
         * Constructor
         *
         * @param context The context for text appearance
         * @param textAppearanceRes The value of text appearance resource
         * since 3.1.1
         */
        public Builder(@NonNull Context context, @StyleRes int textAppearanceRes) {
            if (textAppearanceRes != 0) {
                final TextAppearanceSpan span = new TextAppearanceSpan(context, textAppearanceRes);
                this.textColor = span.getTextColor() != null ? span.getTextColor().getDefaultColor() : UNDEFINED_RESOURCE_ID;
                this.textStyle = span.getTextStyle() != 0 ? span.getTextStyle() : UNDEFINED_RESOURCE_ID;
                this.textSize = span.getTextSize();
                this.familyName = span.getFamily();
            }
        }

        /**
         * Sets the background color of text
         *
         * @param textBackgroundColor The value of background color int
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.1
         */
        @NonNull
        public Builder setTextBackgroundColor(@ColorInt int textBackgroundColor) {
            this.textBackgroundColor = textBackgroundColor;
            return this;
        }

        /**
         * Sets the color of text
         *
         * @param textColor The value of text color int
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.1
         */
        @NonNull
        public Builder setTextColor(@ColorInt int textColor) {
            this.textColor = textColor;
            return this;
        }

        /**
         * Sets a value of {@link android.graphics.Typeface}.
         * <li>Typeface.NORMAL</li>
         * <li>Typeface.BOLD</li>
         * <li>Typeface.ITALIC</li>
         * <li>Typeface.BOLD_ITALIC</li>
         *
         * @param textStyle The value of text style
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.1
         */
        @NonNull
        public Builder setTextStyle(int textStyle) {
            this.textStyle = textStyle;
            return this;
        }

        /**
         * Sets the size of text
         *
         * @param textSize The value of pixel size
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.1
         */
        @NonNull
        public Builder setTextSize(int textSize) {
            this.textSize = textSize;
            return this;
        }

        /**
         * Sets the typeface family name of text
         *
         * @param familyName The value of typeface family name
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.1
         */
        @NonNull
        public Builder setFamilyName(@Nullable String familyName) {
            this.familyName = familyName;
            return this;
        }

        /**
         * Sets the custom font resource id of text
         *
         * @param customFontRes The value of custom font
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.1
         */
        @NonNull
        public Builder setCustomFontRes(int customFontRes) {
            this.customFontRes = customFontRes;
            return this;
        }

        /**
         * Builds an {@link TextUIConfig} with the properties supplied to this builder.
         *
         * @return The {@link TextUIConfig} from this builder instance.
         * since 3.1.1
         */
        @NonNull
        public TextUIConfig build() {
            return new TextUIConfig(this.textColor, this.textBackgroundColor, this.textStyle, this.textSize, this.familyName, this.customFontRes);
        }
    }
}

