package com.sendbird.uikit.model;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.BaseMessageParams;
import com.sendbird.android.User;

/**
 * The text with a MentionSpan attached will be bold and clickable.
 * MentionSpan provides the User data that is relevant to the marked-up text.
 *
 * @since 3.0.0
 */
public class MentionSpan extends MetricAffectingSpan {
    private static final int UNDEFINED = -1;
    @NonNull
    private final String trigger;
    @NonNull
    private final String value;
    @NonNull
    private final User mentionedUser;
    @NonNull
    private final TextUIConfig uiConfig;
    @Nullable
    private final TextUIConfig mentionedCurrentUserUIConfig;
    @NonNull
    final BaseMessageParams.MentionType mentionType;

    /**
     * Constructor
     *
     * @param trigger The text to trigger mention
     * @param value The text to be mentioned
     * @param user The User relevant to this mention-spanned text
     * @param uiConfig The mention ui config.
     * @since 3.0.0
     */
    public MentionSpan(@NonNull String trigger, @NonNull String value, @NonNull User user, @NonNull TextUIConfig uiConfig) {
        this(BaseMessageParams.MentionType.USERS, trigger, value, user, uiConfig);
    }

    /**
     * Constructor
     *
     * @param trigger The text to trigger mention
     * @param value The text to be mentioned
     * @param user The User relevant to this mention-spanned text
     * @param uiConfig The mention ui config.
     * @param mentionedCurrentUserUIConfig The mention ui config if current user is mentioned
     * @since 3.0.0
     */
    public MentionSpan(@NonNull String trigger, @NonNull String value, @NonNull User user, @NonNull TextUIConfig uiConfig, @Nullable TextUIConfig mentionedCurrentUserUIConfig) {
        this(BaseMessageParams.MentionType.USERS, trigger, value, user, uiConfig, mentionedCurrentUserUIConfig);
    }

    /**
     * Constructor
     *
     * @param mentionType The type of mention to be applied for this mention-spanned text
     * @param trigger The text to trigger mention
     * @param value The text to be mentioned
     * @param user The User relevant to this mention-spanned text
     * @param uiConfig The mention ui config.
     * @since 3.0.0
     */
    public MentionSpan(@NonNull BaseMessageParams.MentionType mentionType, @NonNull String trigger, @NonNull String value, @NonNull User user, @NonNull TextUIConfig uiConfig) {
        this(mentionType, trigger, value, user, uiConfig, null);
    }

    /**
     * Constructor
     *
     * @param mentionType The type of mention to be applied for this mention-spanned text
     * @param trigger The text to trigger mention
     * @param value The text to be mentioned
     * @param user The User relevant to this mention-spanned text
     * @param uiConfig The mention ui config.
     * @param mentionedCurrentUserUIConfig The mention ui config if current user is mentioned
     * @since 3.0.0
     */
    public MentionSpan(@NonNull BaseMessageParams.MentionType mentionType, @NonNull String trigger, @NonNull String value, @NonNull User user, @NonNull TextUIConfig uiConfig, @Nullable TextUIConfig mentionedCurrentUserUIConfig) {
        this.mentionType = mentionType;
        this.trigger = trigger;
        this.value = value;
        this.mentionedUser = user;
        this.uiConfig = uiConfig;
        this.mentionedCurrentUserUIConfig = mentionedCurrentUserUIConfig;
    }

    private static Typeface generateTypeface(@NonNull TextUIConfig uiConfig) {
        Typeface typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
        int typefaceStyle = uiConfig.getTypefaceStyle();
        if (typefaceStyle >= 0) {
            switch (typefaceStyle) {
                case Typeface.NORMAL:
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
                    break;
                case Typeface.BOLD:
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
                    break;
                case Typeface.ITALIC:
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC);
                    break;
                case Typeface.BOLD_ITALIC:
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
                    break;
            }
        }
        return typeface;
    }

    @Override
    public void updateDrawState(@NonNull TextPaint paint) {
        applyUIConfig(paint, uiConfig);
        if (mentionedCurrentUserUIConfig != null) {
            // if mentioned current user exists, this color has priority.
            applyUIConfig(paint, mentionedCurrentUserUIConfig);
        }
    }

    @Override
    public void updateMeasureState(@NonNull TextPaint paint) {
        applyUIConfig(paint, uiConfig);
        if (mentionedCurrentUserUIConfig != null) {
            // if mentioned current user exists, this color has priority.
            applyUIConfig(paint, mentionedCurrentUserUIConfig);
        }
    }

    private static void applyUIConfig(@NonNull TextPaint paint, @NonNull TextUIConfig uiConfig) {
        applyCustomTypeFace(paint, generateTypeface(uiConfig));
        if (uiConfig.getTextColor() != UNDEFINED) {
            paint.setColor(uiConfig.getTextColor());
        }
        if (uiConfig.getTextBackgroundColor() != UNDEFINED) {
            paint.bgColor = uiConfig.getTextBackgroundColor();
        }
        paint.setUnderlineText(false);
    }

    private static void applyCustomTypeFace(@NonNull Paint paint, @NonNull Typeface tf) {
        int oldStyle;
        Typeface old = paint.getTypeface();
        if (old == null) {
            oldStyle = Typeface.BOLD;
        } else {
            oldStyle = old.getStyle();
        }

        int fake = oldStyle & ~tf.getStyle();
        if ((fake & Typeface.BOLD) != 0) {
            paint.setFakeBoldText(true);
        }

        if ((fake & Typeface.ITALIC) != 0) {
            paint.setTextSkewX(-0.25f);
        }

        paint.setTypeface(tf);
    }

    /**
     * Returns the mentioned text.
     *
     * @return The mentioned text of this spanned text
     * @since 3.0.0
     */
    @NonNull
    public String getValue() {
        return value;
    }

    /**
     * Returns the trigger text.
     *
     * @return The trigger text of this spanned text
     * @since 3.0.0
     */
    @NonNull
    public String getTrigger() {
        return trigger;
    }

    /**
     * Returns the text to be displayed by combining trigger and value.
     *
     * @return The text to be displayed from this markup object
     * @since 3.0.0
     */
    @NonNull
    public String getDisplayText() {
        return trigger + value;
    }

    /**
     * Returns the template text to be used as a mention data.
     *
     * @return The text to be used as a mention data from this markup object
     * @since 3.0.0
     */
    @NonNull
    public String getTemplateText() {
        return trigger + "{" + mentionedUser.getUserId() + "}";
    }

    /**
     * Returns the User relevant to this spanned text
     *
     * @return The User data relevant to this markup object
     * @since 3.0.0
     */
    @NonNull
    public User getMentionedUser() {
        return mentionedUser;
    }

    /**
     * Returns the length of the text to be displayed.
     *
     * @return The length of the displayed text of this markup object
     * @since 3.0.0
     */
    public int getLength() {
        return getDisplayText().length();
    }
}
