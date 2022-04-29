package com.sendbird.uikit.model;

import androidx.annotation.NonNull;

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
    private final TextUIConfig searchedTextUIConfig;

    /**
     * Constructor
     */
    public MessageUIConfig() {
        this.myEditedTextMarkUIConfig = new TextUIConfig();
        this.otherEditedTextMarkUIConfig = new TextUIConfig();
        this.myMentionUIConfig = new TextUIConfig();
        this.otherMentionUIConfig = new TextUIConfig();
        this.searchedTextUIConfig = new TextUIConfig();
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
     * Returns UI configuration of searched message.
     *
     * @return the UI configuration of searched message.
     * @since 3.0.0
     */
    @NonNull
    public TextUIConfig getSearchedTextUIConfig() {
        return searchedTextUIConfig;
    }
}
