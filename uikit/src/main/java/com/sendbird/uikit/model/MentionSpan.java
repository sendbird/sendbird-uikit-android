package com.sendbird.uikit.model;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.sendbird.android.message.MentionType;
import com.sendbird.android.user.User;

/**
 * The text with a MentionSpan attached will be bold and clickable.
 * MentionSpan provides the User data that is relevant to the marked-up text.
 *
 * since 3.0.0
 */
public class MentionSpan extends MetricAffectingSpan {
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
    final MentionType mentionType;
    @NonNull
    final Context context;

    /**
     * Constructor
     *
     * @param context The {@code Context} this spannable is currently associated with
     * @param trigger The text to trigger mention
     * @param value The text to be mentioned
     * @param user The User relevant to this mention-spanned text
     * @param uiConfig The mention ui config.
     * since 3.0.0
     */
    public MentionSpan(@NonNull Context context, @NonNull String trigger, @NonNull String value, @NonNull User user, @NonNull TextUIConfig uiConfig) {
        this(context, MentionType.USERS, trigger, value, user, uiConfig);
    }

    /**
     * Constructor
     *
     * @param context The {@code Context} this spannable is currently associated with
     * @param trigger The text to trigger mention
     * @param value The text to be mentioned
     * @param user The User relevant to this mention-spanned text
     * @param uiConfig The mention ui config.
     * @param mentionedCurrentUserUIConfig The mention ui config if current user is mentioned
     * since 3.0.0
     */
    public MentionSpan(@NonNull Context context, @NonNull String trigger, @NonNull String value, @NonNull User user, @NonNull TextUIConfig uiConfig, @Nullable TextUIConfig mentionedCurrentUserUIConfig) {
        this(context, MentionType.USERS, trigger, value, user, uiConfig, mentionedCurrentUserUIConfig);
    }

    /**
     * Constructor
     *
     * @param context The {@code Context} this spannable is currently associated with
     * @param mentionType The type of mention to be applied for this mention-spanned text
     * @param trigger The text to trigger mention
     * @param value The text to be mentioned
     * @param user The User relevant to this mention-spanned text
     * @param uiConfig The mention ui config.
     * since 3.0.0
     */
    public MentionSpan(@NonNull Context context, @NonNull MentionType mentionType, @NonNull String trigger, @NonNull String value, @NonNull User user, @NonNull TextUIConfig uiConfig) {
        this(context, mentionType, trigger, value, user, uiConfig, null);
    }

    /**
     * Constructor
     *
     * @param context The {@code Context} this spannable is currently associated with
     * @param mentionType The type of mention to be applied for this mention-spanned text
     * @param trigger The text to trigger mention
     * @param value The text to be mentioned
     * @param user The User relevant to this mention-spanned text
     * @param uiConfig The mention ui config.
     * @param mentionedCurrentUserUIConfig The mention ui config if current user is mentioned
     * since 3.0.0
     */
    public MentionSpan(@NonNull Context context, @NonNull MentionType mentionType, @NonNull String trigger, @NonNull String value, @NonNull User user, @NonNull TextUIConfig uiConfig, @Nullable TextUIConfig mentionedCurrentUserUIConfig) {
        this.context = context;
        this.mentionType = mentionType;
        this.trigger = trigger;
        this.value = value;
        this.mentionedUser = user;
        this.uiConfig = uiConfig;
        this.mentionedCurrentUserUIConfig = mentionedCurrentUserUIConfig;
    }

    @Override
    public void updateDrawState(@NonNull TextPaint paint) {
        applyMentionTextPaint(context, uiConfig, mentionedCurrentUserUIConfig, paint);
    }

    @Override
    public void updateMeasureState(@NonNull TextPaint paint) {
        applyMentionTextPaint(context, uiConfig, mentionedCurrentUserUIConfig, paint);
    }

    private static void applyMentionTextPaint(@NonNull Context context, @NonNull TextUIConfig uiConfig, @Nullable TextUIConfig mentionedCurrentUserUIConfig, @NonNull TextPaint to) {
        apply(context, uiConfig, to);
        if (mentionedCurrentUserUIConfig != null) {
            // if mentioned current user exists, this color has priority.
            apply(context, mentionedCurrentUserUIConfig, to);
        }
        to.setUnderlineText(false);
    }

    private static void apply(@NonNull Context context, @NonNull TextUIConfig from, @NonNull TextPaint to) {
        if (from.getTextColor() != TextUIConfig.UNDEFINED_RESOURCE_ID) {
            to.setColor(from.getTextColor());
        }
        if (from.getTextStyle() != TextUIConfig.UNDEFINED_RESOURCE_ID) {
            to.setTypeface(from.generateTypeface());
        }
        if (from.getTextSize() != TextUIConfig.UNDEFINED_RESOURCE_ID) {
            to.setTextSize(from.getTextSize());
        }
        if (from.getTextBackgroundColor() != TextUIConfig.UNDEFINED_RESOURCE_ID) {
            to.bgColor = from.getTextBackgroundColor();
        }

        if (from.getCustomFontRes() != TextUIConfig.UNDEFINED_RESOURCE_ID) {
            try {
                Typeface font = ResourcesCompat.getFont(context, from.getCustomFontRes());
                if (font != null) {
                    to.setTypeface(font);
                }
            } catch (Resources.NotFoundException ignore) {
            }
        }
    }

    /**
     * Returns the mentioned text.
     *
     * @return The mentioned text of this spanned text
     * since 3.0.0
     */
    @NonNull
    public String getValue() {
        return value;
    }

    /**
     * Returns the trigger text.
     *
     * @return The trigger text of this spanned text
     * since 3.0.0
     */
    @NonNull
    public String getTrigger() {
        return trigger;
    }

    /**
     * Returns the text to be displayed by combining trigger and value.
     *
     * @return The text to be displayed from this markup object
     * since 3.0.0
     */
    @NonNull
    public String getDisplayText() {
        return trigger + value;
    }

    /**
     * Returns the template text to be used as a mention data.
     *
     * @return The text to be used as a mention data from this markup object
     * since 3.0.0
     */
    @NonNull
    public String getTemplateText() {
        return trigger + "{" + mentionedUser.getUserId() + "}";
    }

    /**
     * Returns the User relevant to this spanned text
     *
     * @return The User data relevant to this markup object
     * since 3.0.0
     */
    @NonNull
    public User getMentionedUser() {
        return mentionedUser;
    }

    /**
     * Returns the length of the text to be displayed.
     *
     * @return The length of the displayed text of this markup object
     * since 3.0.0
     */
    public int getLength() {
        return getDisplayText().length();
    }
}
