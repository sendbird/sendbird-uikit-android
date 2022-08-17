package com.sendbird.uikit.model;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Configurations of Message.
 */
public class MessageUIConfig {
    @NonNull
    private final TextUIConfig myEditedTextMarkUIConfig;
    @NonNull
    private final TextUIConfig otherEditedTextMarkUIConfig;
    @NonNull
    private final TextUIConfig myMentionUIConfig;
    @NonNull
    private final TextUIConfig otherMentionUIConfig;
    @NonNull
    private final TextUIConfig myMessageTextUIConfig;
    @NonNull
    private final TextUIConfig otherMessageTextUIConfig;
    @NonNull
    private final TextUIConfig mySentAtTextUIConfig;
    @NonNull
    private final TextUIConfig otherSentAtTextUIConfig;
    @NonNull
    private final TextUIConfig myNicknameTextUIConfig;
    @NonNull
    private final TextUIConfig otherNicknameTextUIConfig;
    @NonNull
    private final TextUIConfig operatorNicknameTextUIConfig;
    @Nullable
    private ColorStateList linkedTextColor;
    @Nullable
    private Drawable myMessageBackground;
    @Nullable
    private Drawable otherMessageBackground;
    @Nullable
    private Drawable myReactionListBackground;
    @Nullable
    private Drawable otherReactionListBackground;
    @Nullable
    private Drawable myOgtagBackground;
    @Nullable
    private Drawable otherOgtagBackground;

    /**
     * Constructor
     */
    public MessageUIConfig() {
        this.myMessageTextUIConfig = new TextUIConfig.Builder().build();
        this.otherMessageTextUIConfig = new TextUIConfig.Builder().build();
        this.mySentAtTextUIConfig = new TextUIConfig.Builder().build();
        this.otherSentAtTextUIConfig = new TextUIConfig.Builder().build();
        this.myEditedTextMarkUIConfig = new TextUIConfig.Builder().build();
        this.otherEditedTextMarkUIConfig = new TextUIConfig.Builder().build();
        this.myMentionUIConfig = new TextUIConfig.Builder().build();
        this.otherMentionUIConfig = new TextUIConfig.Builder().build();
        this.myNicknameTextUIConfig = new TextUIConfig.Builder().build();
        this.otherNicknameTextUIConfig = new TextUIConfig.Builder().build();
        this.operatorNicknameTextUIConfig = new TextUIConfig.Builder().build();
    }

    /**
     * Returns UI configuration of edited message that I sent.
     * If the message is edited, UIKit add an extra text, like, "(Edited)".
     * This extra text's UI configurations will be returned.
     *
     * @return the UI configuration of edited message mark.
     * @since 3.0.0
     */
    @NonNull
    public TextUIConfig getMyEditedTextMarkUIConfig() {
        return myEditedTextMarkUIConfig;
    }

    /**
     * Returns UI configuration of edited message that the others sent.
     * If the message is edited, UIKit add an extra text, like, "(Edited)".
     * This extra text's UI configurations will be returned.
     *
     * @return the UI configuration of edited message mark.
     * @since 3.0.0
     */
    @NonNull
    public TextUIConfig getOtherEditedTextMarkUIConfig() {
        return otherEditedTextMarkUIConfig;
    }

    /**
     * Returns UI configuration of mentioned message that I sent.
     *
     * @return the UI configuration of mentioned message.
     * @since 3.0.0
     */
    @NonNull
    public TextUIConfig getMyMentionUIConfig() {
        return myMentionUIConfig;
    }

    /**
     * Returns UI configuration of mentioned message that the others sent.
     *
     * @return the UI configuration of mentioned message.
     * @since 3.0.0
     */
    @NonNull
    public TextUIConfig getOtherMentionUIConfig() {
        return otherMentionUIConfig;
    }

    /**
     * Returns UI configuration of message text that I sent.
     *
     * @return the UI configuration of message text.
     * @since 3.1.1
     */
    @NonNull
    public TextUIConfig getMyMessageTextUIConfig() {
        return myMessageTextUIConfig;
    }

    /**
     * Returns UI configuration of message text that the others sent.
     *
     * @return the UI configuration of message text.
     * @since 3.1.1
     */
    @NonNull
    public TextUIConfig getOtherMessageTextUIConfig() {
        return otherMessageTextUIConfig;
    }

    /**
     * Returns UI configuration of message sentAt text that the I sent.
     *
     * @return the UI configuration of message sentAt text.
     * @since 3.1.1
     */
    @NonNull
    public TextUIConfig getMySentAtTextUIConfig() {
        return mySentAtTextUIConfig;
    }

    /**
     * Returns UI configuration of message sentAt text that the others sent.
     *
     * @return the UI configuration of message sentAt text.
     * @since 3.1.1
     */
    @NonNull
    public TextUIConfig getOtherSentAtTextUIConfig() {
        return otherSentAtTextUIConfig;
    }

    /**
     * Returns UI configuration of sender nickname that the I sent.
     *
     * @return the UI configuration of sender nickname.
     * @since 3.1.1
     */
    @NonNull
    public TextUIConfig getMyNicknameTextUIConfig() {
        return myNicknameTextUIConfig;
    }

    /**
     * Returns UI configuration of sender nickname that the others sent.
     *
     * @return the UI configuration of sender nickname.
     * @since 3.1.1
     */
    @NonNull
    public TextUIConfig getOtherNicknameTextUIConfig() {
        return otherNicknameTextUIConfig;
    }

    /**
     * Returns UI configuration of sender nickname that the operator sent.
     *
     * @return the UI configuration of sender nickname.
     * @since 3.1.1
     */
    @NonNull
    public TextUIConfig getOperatorNicknameTextUIConfig() {
        return operatorNicknameTextUIConfig;
    }

    /**
     * Returns UI configuration of message background that the I sent.
     *
     * @return the UI configuration of message background.
     * @since 3.1.1
     */
    @Nullable
    public Drawable getMyMessageBackground() {
        return myMessageBackground;
    }

    /**
     * Sets UI configuration of message background that the I sent.
     *
     * @since 3.1.1
     */
    public void setMyMessageBackground(@NonNull Drawable myMessageBackground) {
        this.myMessageBackground = myMessageBackground;
    }

    /**
     * Returns UI configuration of message background that the others sent.
     *
     * @return the UI configuration of message background.
     * @since 3.1.1
     */
    @Nullable
    public Drawable getOtherMessageBackground() {
        return otherMessageBackground;
    }

    /**
     * Sets UI configuration of message background that the others sent.
     *
     * @since 3.1.1
     */
    public void setOtherMessageBackground(@NonNull Drawable otherMessageBackground) {
        this.otherMessageBackground = otherMessageBackground;
    }

    /**
     * Returns UI configuration of message reaction list background that the I sent.
     *
     * @return the UI configuration of message reaction list background.
     * @since 3.1.1
     */
    @Nullable
    public Drawable getMyReactionListBackground() {
        return myReactionListBackground;
    }

    /**
     * Sets UI configuration of message reaction list background that the I sent.
     *
     * @since 3.1.1
     */
    public void setMyReactionListBackground(@NonNull Drawable myReactionListBackground) {
        this.myReactionListBackground = myReactionListBackground;
    }

    /**
     * Returns UI configuration of message reaction list background that the others sent.
     *
     * @return the UI configuration of message reaction list background.
     * @since 3.1.1
     */
    @Nullable
    public Drawable getOtherReactionListBackground() {
        return otherReactionListBackground;
    }

    /**
     * Sets UI configuration of message reaction list background that the others sent.
     *
     * @since 3.1.1
     */
    public void setOtherReactionListBackground(@NonNull Drawable otherReactionListBackground) {
        this.otherReactionListBackground = otherReactionListBackground;
    }

    /**
     * Returns UI configuration of ogtag message background that the I sent.
     *
     * @return the UI configuration of ogtag message background.
     * @since 3.1.1
     */
    @Nullable
    public Drawable getMyOgtagBackground() {
        return myOgtagBackground;
    }

    /**
     * Sets UI configuration of ogtag message background that the I sent.
     *
     * @since 3.1.1
     */
    public void setMyOgtagBackground(@NonNull Drawable myOgtagBackground) {
        this.myOgtagBackground = myOgtagBackground;
    }

    /**
     * Returns UI configuration of ogtag message background that the others sent.
     *
     * @return the UI configuration of ogtag message background.
     * @since 3.1.1
     */
    @Nullable
    public Drawable getOtherOgtagBackground() {
        return otherOgtagBackground;
    }

    /**
     * Sets UI configuration of ogtag message background that the others sent.
     *
     * @since 3.1.1
     */
    public void setOtherOgtagBackground(@NonNull Drawable otherOgtagBackground) {
        this.otherOgtagBackground = otherOgtagBackground;
    }

    /**
     * Returns the UI configuration of the linked text color in the message text.
     *
     * @return  the UI configuration of the linked text color.
     * @since 3.1.1
     */
    @Nullable
    public ColorStateList getLinkedTextColor() {
        return linkedTextColor;
    }

    /**
     * Sets the UI configuration of the linked text color in the message text.
     *
     * @param linkedTextColor   the UI configuration of the linked text color.
     * @since 3.1.1
     */
    public void setLinkedTextColor(@NonNull ColorStateList linkedTextColor) {
        this.linkedTextColor = linkedTextColor;
    }
}
