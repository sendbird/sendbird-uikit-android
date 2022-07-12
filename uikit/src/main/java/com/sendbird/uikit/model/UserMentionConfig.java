package com.sendbird.uikit.model;

import androidx.annotation.NonNull;

import com.sendbird.uikit.consts.StringSet;

/**
 * Describes a configuration of mention for user.
 *
 * @since 3.0.0
 */
public class UserMentionConfig extends MentionConfig {
    private final String trigger;
    private final int maxMentionCount;
    private int maxSuggestionCount;
    private long debounceTime;
    private String delimiter;

    private UserMentionConfig(@NonNull String trigger, int maxMentionCount) {
        super();
        this.trigger = trigger;
        this.maxMentionCount = maxMentionCount;
    }

    /**
     * Returns trigger text for mention.
     *
     * @return A text of trigger.
     * @since 3.0.0
     */
    @NonNull
    public String getTrigger() {
        return trigger;
    }

    /**
     * Returns max mention count.
     * Up to 10 users mentioned in the message will be notified.
     *
     * @return A mentioned user counts to be able to mention.
     * @since 3.0.0
     */
    public int getMaxMentionCount() {
        return maxMentionCount;
    }

    /**
     * Returns maximum suggestion count.
     *
     * @return Maximum suggestion count.
     * @since 3.0.0
     */
    public int getMaxSuggestionCount() {
        return maxSuggestionCount;
    }

    /**
     * Returns the time from now to delay execution.
     *
     * @return The time from now to delay execution
     * @since 3.0.0
     */
    public long getDebounceTime() {
        return debounceTime;
    }

    /**
     * Returns mention delimiter string.
     *
     * @return mention delimiter string.
     * @since 3.0.0
     */
    @NonNull
    public String getDelimiter() {
        return delimiter;
    }

    public static class Builder {
        private int maxMentionCount = 10;
        private int maxSuggestionCount = 15;

        /**
         * Constructor
         *
         * @since 3.0.0
         */
        public Builder() {
        }

        /**
         * Sets maximum mention count.
         *
         * @param maxMentionCount maximum mention count
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.0.0
         */
        @NonNull
        public Builder setMaxMentionCount(int maxMentionCount) {
            this.maxMentionCount = maxMentionCount;
            return this;
        }

        /**
         * Sets the maximum user suggestion count.
         * It can be set up to 15.
         *
         * @param maxSuggestionCount Maximum user suggestion count.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.0.0
         */
        @NonNull
        public Builder setMaxSuggestionCount(int maxSuggestionCount) {
            this.maxSuggestionCount = Math.min(maxSuggestionCount, 15);
            return this;
        }

        /**
         * Creates an {@link UserMentionConfig} with the arguments supplied to this builder.
         *
         * @return The {@link UserMentionConfig}.
         * @since 3.0.0
         */
        @NonNull
        public UserMentionConfig build() {
            UserMentionConfig params = new UserMentionConfig(StringSet._AT, maxMentionCount);
            params.delimiter = " ";
            params.maxSuggestionCount = maxSuggestionCount;
            params.debounceTime = 300L;
            return params;
        }
    }
}
