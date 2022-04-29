package com.sendbird.uikit.model;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.BaseMessageParams;
import com.sendbird.android.User;
import com.sendbird.uikit.log.Logger;

/**
 * The text with a MentionSpan attached will be bold and clickable.
 * MentionSpan provides the User data that is relevant to the marked-up text.
 *
 * @since 3.0.0
 */
public class MentionSpan extends ClickableSpan {
    private final int UNDEFINED = -1;
    @NonNull
    private final String trigger;
    @NonNull
    private final String value;
    @NonNull
    private final User mentionedUser;
    @NonNull
    private final TextUIConfig uiConfig;

    @NonNull
    final BaseMessageParams.MentionType mentionType;
    @Nullable
    private OnMentionClickListener listener;

    /**
     * Interface to be invoked when the mention-spanned text is clicked.
     *
     * @since 3.0.0
     */
    public interface OnMentionClickListener {
        void onClicked(@NonNull MentionSpan span);
    }

    /**
     * Constructor
     *
     * @param trigger The text to trigger mention
     * @param value The text to be mentioned
     * @param user The User relevant to this mention-spanned text
     * @since 3.0.0
     */
    public MentionSpan(@NonNull String trigger, @NonNull String value, @NonNull User user, @NonNull TextUIConfig uiConfig) {
        this(BaseMessageParams.MentionType.USERS, trigger, value, user, uiConfig);
    }

    /**
     * Constructor
     *
     * @param mentionType The type of mention to be applied for this mention-spanned text
     * @param trigger The text to trigger mention
     * @param value The text to be mentioned
     * @param user The User relevant to this mention-spanned text
     * @since 3.0.0
     */
    public MentionSpan(@NonNull BaseMessageParams.MentionType mentionType, @NonNull String trigger, @NonNull String value, @NonNull User user, @NonNull TextUIConfig uiConfig) {
        this.mentionType = mentionType;
        this.trigger = trigger;
        this.value = value;
        this.mentionedUser = user;
        this.uiConfig = uiConfig;
    }

    /**
     * Sets the callback to be invoked when the mention-spanned text is clicked.
     *
     * @param listener The callback that will run
     * @since 3.0.0
     */
    public void setOnMentionClickListener(@NonNull OnMentionClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(@NonNull View widget) {
        Logger.d("++ onClicked mention()");
        if (listener != null) listener.onClicked(this);
    }

    @Override
    public void updateDrawState(@NonNull final TextPaint tp) {
        int typefaceStyle = uiConfig.getTypefaceStyle();
        if (typefaceStyle >= 0) {
            switch (typefaceStyle) {
                case Typeface.NORMAL:
                    tp.setTypeface(Typeface.create(tp.getTypeface(), Typeface.NORMAL));
                    break;
                case Typeface.BOLD:
                    tp.setTypeface(Typeface.create(tp.getTypeface(), Typeface.BOLD));
                    break;
                case Typeface.ITALIC:
                    tp.setTypeface(Typeface.create(tp.getTypeface(), Typeface.ITALIC));
                    break;
                case Typeface.BOLD_ITALIC:
                    tp.setTypeface(Typeface.create(tp.getTypeface(), Typeface.BOLD_ITALIC));
                    break;
            }
        } else {
            tp.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        }
        if (uiConfig.getTextColor() != UNDEFINED) {
            tp.setColor(this.uiConfig.getTextColor());
        }
        if (uiConfig.getTextBackgroundColor() != UNDEFINED) {
            tp.bgColor = uiConfig.getTextBackgroundColor();
        }
        tp.setUnderlineText(false);
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
