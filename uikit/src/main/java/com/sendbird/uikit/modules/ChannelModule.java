package com.sendbird.uikit.modules;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.view.ContextThemeWrapper;

import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.interfaces.LoadingDialogHandler;
import com.sendbird.uikit.modules.components.ChannelHeaderComponent;
import com.sendbird.uikit.modules.components.MessageInputComponent;
import com.sendbird.uikit.modules.components.MessageListComponent;
import com.sendbird.uikit.modules.components.StatusComponent;

/**
 * A module for channel. This module is composed of a header, list, input, and status.
 * All composed components are created when the module is created. After than those components can replace.
 *
 * <ul>
 *  <li>Header component is {@link ChannelHeaderComponent} and you can set custom header component by {@link #setHeaderComponent(ChannelHeaderComponent)}
 *  <li>List component is {@link MessageListComponent} and you can set custom list component by {@link #setMessageListComponent(MessageListComponent)}
 *  <li>Input component is {@link MessageInputComponent} and you can set custom input component by {@link #setInputComponent(MessageInputComponent)}
 *  <li>Status component is {@link StatusComponent} and you can set custom status component by {@link #setStatusComponent(StatusComponent)}
 * </ul>
 *
 * @since 3.0.0
 */
public class ChannelModule extends BaseModule {
    @NonNull
    private final Params params;
    @NonNull
    private ChannelHeaderComponent headerComponent;
    @NonNull
    private MessageListComponent messageListComponent;
    @NonNull
    private MessageInputComponent inputComponent;
    @NonNull
    private StatusComponent statusComponent;
    @Nullable
    private LoadingDialogHandler loadingDialogHandler;

    /**
     * Constructor
     *
     * @param context The {@code Context} this module is currently associated with
     * @since 3.0.0
     */
    public ChannelModule(@NonNull Context context) {
        this(context, new Params(context));
    }

    /**
     * Constructor
     *
     * @param context The {@code Context} this module is currently associated with
     * @param params  The Parameter object that can customize a default Fragment.
     * @since 3.0.0
     */
    public ChannelModule(@NonNull Context context, @NonNull Params params) {
        this.params = params;
        this.headerComponent = new ChannelHeaderComponent();
        this.messageListComponent = new MessageListComponent();
        this.inputComponent = new MessageInputComponent();
        this.statusComponent = new StatusComponent();
    }

    @Override
    @NonNull
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @Nullable Bundle args) {
        if (args != null) params.apply(context, args);

        final Context moduleContext = new ContextThemeWrapper(context, params.getTheme());
        final LinearLayout parent = new LinearLayout(context);
        parent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        parent.setOrientation(LinearLayout.VERTICAL);

        final TypedValue values = new TypedValue();
        if (params.shouldUseHeader()) {
            moduleContext.getTheme().resolveAttribute(R.attr.sb_component_header, values, true);
            final Context headerThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
            final LayoutInflater headerInflater = inflater.cloneInContext(headerThemeContext);
            final View header = this.headerComponent.onCreateView(headerThemeContext, headerInflater, parent, args);
            parent.addView(header);
        }

        final FrameLayout bodyContainer = new FrameLayout(context);
        bodyContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
        parent.addView(bodyContainer);

        moduleContext.getTheme().resolveAttribute(R.attr.sb_component_list, values, true);
        final Context listThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
        final LayoutInflater listInflater = inflater.cloneInContext(listThemeContext);
        final View messageListLayout = messageListComponent.onCreateView(listThemeContext, listInflater, bodyContainer, args);
        bodyContainer.addView(messageListLayout);

        moduleContext.getTheme().resolveAttribute(R.attr.sb_component_status, values, true);
        final Context statusThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
        final LayoutInflater statusInflater = inflater.cloneInContext(statusThemeContext);
        final View statusLayout = statusComponent.onCreateView(statusThemeContext, statusInflater, bodyContainer, args);
        bodyContainer.addView(statusLayout);

        moduleContext.getTheme().resolveAttribute(R.attr.sb_component_channel_message_input, values, true);
        final Context inputThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
        final LayoutInflater inputInflater = inflater.cloneInContext(inputThemeContext);
        final View inputLayout = inputComponent.onCreateView(inputThemeContext, inputInflater, parent, args);
        parent.addView(inputLayout);
        return parent;
    }

    /**
     * Sets the handler for the loading dialog.
     *
     * @param loadingDialogHandler Loading dialog handler to be used in this module
     * @since 3.0.0
     */
    public void setOnLoadingDialogHandler(@Nullable LoadingDialogHandler loadingDialogHandler) {
        this.loadingDialogHandler = loadingDialogHandler;
    }

    /**
     * Returns the handler for loading dialog.
     *
     * @return Loading dialog handler to be used in this module
     * @since 3.0.0
     */
    @Nullable
    public LoadingDialogHandler getLoadingDialogHandler() {
        return loadingDialogHandler;
    }

    /**
     * Sets a custom channel header component.
     *
     * @param component The channel header component to be used in this module
     * @since 3.0.0
     */
    public <T extends ChannelHeaderComponent> void setHeaderComponent(@NonNull T component) {
        this.headerComponent = component;
    }

    /**
     * Sets a custom message list component.
     *
     * @param component The message list component to be used in this module
     * @since 3.0.0
     */
    public <T extends MessageListComponent> void setMessageListComponent(@NonNull T component) {
        this.messageListComponent = component;
    }

    /**
     * Sets a custom message input component.
     *
     * @param component The message input component to be used in this module
     * @since 3.0.0
     */
    public <T extends MessageInputComponent> void setInputComponent(@NonNull T component) {
        this.inputComponent = component;
    }

    /**
     * Sets a custom status component.
     *
     * @param component The status component to be used in this module
     * @since 3.0.0
     */
    public <T extends StatusComponent> void setStatusComponent(@NonNull T component) {
        this.statusComponent = component;
    }

    /**
     * Returns the channel header component.
     *
     * @return The channel header component of this module
     * @since 3.0.0
     */
    @NonNull
    public ChannelHeaderComponent getHeaderComponent() {
        return headerComponent;
    }

    /**
     * Returns the message list component.
     *
     * @return The message list component of this module
     * @since 3.0.0
     */
    @NonNull
    public MessageListComponent getMessageListComponent() {
        return messageListComponent;
    }

    /**
     * Returns the message input component.
     *
     * @return The message input component of this module
     * @since 3.0.0
     */
    @NonNull
    public MessageInputComponent getMessageInputComponent() {
        return inputComponent;
    }

    /**
     * Returns the status component.
     *
     * @return The status component of this module
     * @since 3.0.0
     */
    @NonNull
    public StatusComponent getStatusComponent() {
        return statusComponent;
    }

    /**
     * Returns a collection of parameters applied to this module.
     *
     * @return {@link Params} applied to this module.
     * @since 3.0.0
     */
    @NonNull
    public Params getParams() {
        return params;
    }

    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * @since 3.0.0
     */
    public boolean shouldShowLoadingDialog() {
        if (loadingDialogHandler != null && loadingDialogHandler.shouldShowLoadingDialog()) {
            return true;
        }
        // Do nothing on the channel.
        return false;
    }

    /**
     * It will be called when the loading dialog needs dismissing.
     *
     * @since 3.0.0
     */
    public void shouldDismissLoadingDialog() {
        if (loadingDialogHandler != null) {
            loadingDialogHandler.shouldDismissLoadingDialog();
        }
    }

    public static class Params extends BaseModule.Params {
        /**
         * Constructor
         *
         * @param context The {@code Context} this module is currently associated with
         * @since 3.0.0
         */
        public Params(@NonNull Context context) {
            this(context, SendbirdUIKit.getDefaultThemeMode());
        }

        /**
         * Constructor
         *
         * @param context   The {@code Context} this module is currently associated with
         * @param themeMode The theme of Sendbird UIKit to be applied to this module
         * @since 3.0.0
         */
        public Params(@NonNull Context context, @NonNull SendbirdUIKit.ThemeMode themeMode) {
            super(context, themeMode, R.attr.sb_module_channel);
        }

        /**
         * Constructor
         *
         * @param context    The {@code Context} this module is currently associated with
         * @param themeResId The theme resource ID to be applied to this module
         * @since 3.0.0
         */
        public Params(@NonNull Context context, @StyleRes int themeResId) {
            super(context, themeResId, R.attr.sb_module_channel);
        }
    }
}
