package com.sendbird.uikit.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.uikit.internal.model.template_messages.ActionData;

/**
 * Custom action data to be linked to a custom message.
 * This action data is delivered and used as it is.
 *
 * @since 3.5.0
 */
public final class Action {
    private final String type;
    private final String data;
    private final String alterData;

    /**
     * Constructor that is used only internally.
     *
     * @param type an action data type.
     * @param data a data delivered and used as it is
     * @param alterData an alternative data that can be used if data is not available
     * @since 3.5.0
     */
    public Action(@NonNull String type, @NonNull String data, @Nullable String alterData) {
        this.type = type;
        this.data = data;
        this.alterData = alterData;
    }

    /**
     * Convert ActionData to Action class. This is used only for internal.
     *
     * @param actionData The data from the given custom data filed.
     * @return Action data.
     * @since 3.5.0
     */
    @NonNull
    public static Action from(@NonNull ActionData actionData) {
        return new Action(actionData.getType().name().toLowerCase(), actionData.getData(), actionData.getAlterData());
    }

    /**
     * Returns the type of Action.
     * "web", "custom", and "uikit" are available.
     *
     * @return the type of Action.
     * @since 3.5.0
     */
    @NonNull
    public String getType() {
        return type;
    }

    /**
     * Returns action data that associated with the view.
     *
     * @return the action data associated with the view.
     * @since 3.5.0
     */
    @NonNull
    public String getData() {
        return data;
    }

    /**
     * Alternative data that can be used if data is not available
     *
     * @return the alternative data that can be used if data is not available
     * @since 3.5.0
     */
    @Nullable
    public String getAlterData() {
        return alterData;
    }

    @NonNull
    @Override
    public String toString() {
        return "Action{" +
                "type='" + type + '\'' +
                ", data='" + data + '\'' +
                ", alterData='" + alterData + '\'' +
                '}';
    }
}
