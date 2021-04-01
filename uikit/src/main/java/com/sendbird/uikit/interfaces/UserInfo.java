package com.sendbird.uikit.interfaces;

/**
 * Interface definition for a information of user.
 */
public interface UserInfo {
    /**
     * Provides the identifier of the user.
     *
     * @return the identifier of the user.
     */
    String getUserId();

    /**
     * Provides the nickname of the user.
     *
     * @return the nickname of the user.
     */
    String getNickname();

    /**
     * Provides the profile url of the user.
     *
     * @return the profile url of the user.
     */
    String getProfileUrl();
}
