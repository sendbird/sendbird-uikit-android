package com.sendbird.uikit.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.lifecycle.ViewModelProvider;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.params.MessageListParams;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.activities.adapter.NotificationMessageListAdapter;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.interfaces.LoadingDialogHandler;
import com.sendbird.uikit.interfaces.OnMessageTemplateActionHandler;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.Action;
import com.sendbird.uikit.model.ReadyStatus;
import com.sendbird.uikit.modules.NotificationChannelModule;
import com.sendbird.uikit.modules.components.HeaderComponent;
import com.sendbird.uikit.modules.components.NotificationMessageListComponent;
import com.sendbird.uikit.modules.components.StatusComponent;
import com.sendbird.uikit.utils.DialogUtils;
import com.sendbird.uikit.utils.IntentUtils;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit.vm.NotificationChannelViewModel;
import com.sendbird.uikit.vm.ViewModelFactory;

public class NotificationChannelFragment extends BaseModuleFragment<NotificationChannelModule, NotificationChannelViewModel> {

    @Nullable
    private View.OnClickListener headerLeftButtonClickListener;
    @Nullable
    private LoadingDialogHandler loadingDialogHandler;
    @Nullable
    private NotificationMessageListAdapter adapter;
    @Nullable
    private OnMessageTemplateActionHandler actionHandler;
    @Nullable
    private MessageListParams params;

    @NonNull
    @Override
    protected NotificationChannelModule onCreateModule(@NonNull Bundle args) {
        return new NotificationChannelModule(requireContext());
    }

    @Override
    protected void onConfigureParams(@NonNull NotificationChannelModule module, @NonNull Bundle args) {
        if (loadingDialogHandler != null) module.setOnLoadingDialogHandler(loadingDialogHandler);
    }

    @NonNull
    @Override
    protected NotificationChannelViewModel onCreateViewModel() {
        final NotificationChannelViewModel viewModel = new ViewModelProvider(this, new ViewModelFactory(getChannelUrl(), params)).get(getChannelUrl(), NotificationChannelViewModel.class);
        getLifecycle().addObserver(viewModel);
        return viewModel;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        shouldShowLoadingDialog();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        shouldDismissLoadingDialog();
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull NotificationChannelModule module, @NonNull NotificationChannelViewModel viewModel) {
        Logger.d(">> NotificationChannelFragment::onBeforeReady status=%s", status);
        module.getMessageListComponent().setPagedDataLoader(viewModel);
        if (this.adapter != null) {
            module.getMessageListComponent().setAdapter(adapter);
        }
        final GroupChannel channel = viewModel.getChannel();
        onBindHeaderComponent(module.getHeaderComponent(), viewModel, channel);
        onBindNotificationMessageListComponent(module.getMessageListComponent(), viewModel, channel);
        onBindStatusComponent(module.getStatusComponent(), viewModel, channel);
    }

    @Override
    protected void onReady(@NonNull ReadyStatus status, @NonNull NotificationChannelModule module, @NonNull NotificationChannelViewModel viewModel) {
        Logger.d(">> NotificationChannelFragment::onReady status=%s", status);
        shouldDismissLoadingDialog();
        final GroupChannel channel = viewModel.getChannel();
        if (status == ReadyStatus.ERROR || channel == null) {
            if (isFragmentAlive()) {
                toastError(R.string.sb_text_error_get_channel);
                shouldActivityFinish();
            }
            return;
        }

        module.getMessageListComponent().notifyChannelChanged(channel);
        viewModel.onChannelDeleted().observe(getViewLifecycleOwner(), channelUrl -> shouldActivityFinish());
        loadInitial();
    }

    /**
     * Called to bind events to the HeaderComponent. This is called from {@link #onBeforeReady(ReadyStatus, NotificationChannelModule, NotificationChannelViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param headerComponent The component to which the event will be bound
     * @param viewModel A view model that provides the data needed for the fragment
     * @param channel The {@code GroupChannel} that contains the data needed for this fragment
     * @since 3.5.0
     */
    protected void onBindHeaderComponent(@NonNull HeaderComponent headerComponent, @NonNull NotificationChannelViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> NotificationChannelFragment::onBindNotificationHeaderComponent()");
        headerComponent.setOnLeftButtonClickListener(headerLeftButtonClickListener != null ? headerLeftButtonClickListener : v -> shouldActivityFinish());
    }

    /**
     * Called to bind events to the NotificationMessageListComponent. This is called from {@link #onBeforeReady(ReadyStatus, NotificationChannelModule, NotificationChannelViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param listComponent The component to which the event will be bound
     * @param viewModel A view model that provides the data needed for the fragment
     * @param channel The {@code GroupChannel} that contains the data needed for this fragment
     * @since 3.5.0
     */
    protected void onBindNotificationMessageListComponent(@NonNull NotificationMessageListComponent listComponent, @NonNull NotificationChannelViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> NotificationChannelFragment::onBindNotificationMessageListComponent()");
        listComponent.setOnMessageTemplateActionHandler(actionHandler != null ? actionHandler : this::handleAction);
        viewModel.getMessageList().observeAlways(getViewLifecycleOwner(), messageData -> {
            if (!isFragmentAlive() || channel == null) return;
            listComponent.notifyDataSetChanged(messageData.getMessages(), channel, null);
        });
    }

    /**
     * Called to bind events to the StatusComponent. This is called from {@link #onBeforeReady(ReadyStatus, NotificationChannelModule, NotificationChannelViewModel)}  regardless of the value of {@link ReadyStatus}.
     *
     * @param statusComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * @param channel         The {@code GroupChannel} that contains the data needed for this fragment
     * @since 3.5.0
     */
    protected void onBindStatusComponent(@NonNull StatusComponent statusComponent, @NonNull NotificationChannelViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> NotificationChannelFragment::onBindStatusComponent()");
        viewModel.getStatusFrame().observe(getViewLifecycleOwner(), statusComponent::notifyStatusChanged);
    }

    private void handleAction(@NonNull View view, @NonNull Action action, @NonNull BaseMessage message) {
        switch (action.getType()) {
            case StringSet.web:
                handleWebAction(view, action, message);
                break;
            case StringSet.custom:
                handleCustomAction(view, action, message);
                break;
            case StringSet.uikit:
                handlePredefinedAction(view, action, message);
                break;
            default:
                break;
        }
    }

    /**
     * If an Action is registered in a specific view, it is called when a click event occurs.
     *
     * @param action the registered Action data
     * @param message a clicked message
     * @since 3.5.0
     */
    protected void handleWebAction(@NonNull View view, @NonNull Action action, @NonNull BaseMessage message) {
        Logger.d(">> NotificationChannelFragment::handleWebAction() action=%s", action);
        final Intent intent = IntentUtils.getWebViewerIntent(action.getData());
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Logger.e(e);
        }
    }

    /**
     * If an Action is registered in a specific view, it is called when a click event occurs.
     *
     * @param action the registered Action data
     * @param message a clicked message
     * @since 3.5.0
     */
    protected void handleCustomAction(@NonNull View view, @NonNull Action action, @NonNull BaseMessage message) {
        Logger.d(">> NotificationChannelFragment::handleCustomAction() action=%s", action);
        try {
            final String data = action.getData();
            if (TextUtils.isNotEmpty(data)) {
                final Uri uri = Uri.parse(data);
                Logger.d("++ uri = %s", uri);
                final String scheme = uri.getScheme();
                Logger.d("++ scheme=%s", scheme);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                boolean hasIntent = IntentUtils.hasIntent(requireContext(), intent);
                if (!hasIntent) {
                    final String alterData = action.getAlterData();
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(alterData));
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } catch (Exception e) {
            Logger.w(e);
        }
    }

    /**
     * If an Action is registered in a specific view, it is called when a click event occurs.
     *
     *
     * <ul>
     *  Support below actions.
     *  <li> delete: Request deleting message. (e.g: sendbirduikit://delete)
     * </ul>
     *
     * @param action the registered Action data
     * @param message a clicked message
     * @since 3.5.0
     */
    protected void handlePredefinedAction(@NonNull View view, @NonNull Action action, @NonNull BaseMessage message) {
        Logger.d(">> NotificationChannelFragment::handleUIKitAction() action=%s", action);
        final String data = action.getData();
        if (TextUtils.isNotEmpty(data)) {
            final Uri uri = Uri.parse(data);
            Logger.d("++ uri = %s", uri);
            final String host = uri.getHost();
            final String path = uri.getPath();
            Logger.d("++ host=%s, path=%s",host, path);
            if (StringSet.delete.equals(host)) {
                showWarningDialog(message);
            }
        }
    }

    /**
     * Request refreshing the message list.
     * Renews the channel and updates the last read time value together.
     *
     * @since 3.5.0
     */
    public void updateLastReadTimeOnCurrentChannel() {
        Logger.d(">> NotificationChannelFragment::updateLastReadTimeOnCurrentChannel()");
        if (!isFragmentAlive()) return;
        final GroupChannel channel = getViewModel().getChannel();
        if (channel != null) {
            final NotificationMessageListComponent listComponent = getModule().getMessageListComponent();
            listComponent.notifyLastSeenUpdated(getViewModel().getChannel().getMyLastRead());
        }
    }

    private void showWarningDialog(@NonNull BaseMessage message) {
        if (getContext() == null) return;
        DialogUtils.showWarningDialog(
                requireContext(),
                getString(R.string.sb_text_dialog_delete_message),
                getString(R.string.sb_text_button_delete),
                delete -> {
                    Logger.dev("delete");
                    deleteMessage(message);
                },
                getString(R.string.sb_text_button_cancel),
                cancel -> Logger.dev("cancel"));
    }

    /**
     * Delete a message
     *
     * @param message Message to delete.
     * @since 3.5.0
     */
    protected void deleteMessage(@NonNull BaseMessage message) {
        getViewModel().deleteMessage(message, e -> {
            if (e != null) toastError(R.string.sb_text_error_delete_message);
        });
    }

    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * @since 3.5.0
     */
    protected boolean shouldShowLoadingDialog() {
        return getModule().shouldShowLoadingDialog();
    }

    /**
     * It will be called when the loading dialog needs dismissing.
     *
     * @since 3.5.0
     */
    protected void shouldDismissLoadingDialog() {
        getModule().shouldDismissLoadingDialog();
    }

    private synchronized void loadInitial() {
        getViewModel().loadInitial(Long.MAX_VALUE);
    }

    /**
     * Returns the URL of the channel with the required data to use this fragment.
     *
     * @return The URL of a channel this fragment is currently associated with
     * @since 3.5.0
     */
    @NonNull
    protected String getChannelUrl() {
        final Bundle args = getArguments() == null ? new Bundle() : getArguments();
        return args.getString(StringSet.KEY_CHANNEL_URL, "");
    }

    public static class Builder {
        @NonNull
        private final Bundle bundle;
        @Nullable
        private NotificationChannelFragment customFragment;
        @Nullable
        private View.OnClickListener headerLeftButtonClickListener;
        @Nullable
        private NotificationMessageListAdapter adapter;
        @Nullable
        private LoadingDialogHandler loadingDialogHandler;
        @Nullable
        private OnMessageTemplateActionHandler actionHandler;
        @Nullable
        private MessageListParams params;

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * @since 3.5.0
         */
        public Builder(@NonNull String channelUrl) {
            this(channelUrl, SendbirdUIKit.getDefaultThemeMode());
        }

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * @param themeMode  {@link SendbirdUIKit.ThemeMode}
         * @since 3.5.0
         */
        public Builder(@NonNull String channelUrl, @NonNull SendbirdUIKit.ThemeMode themeMode) {
            this(channelUrl, themeMode.getResId());
        }

        /**
         * Constructor
         *
         * @param channelUrl       the url of the channel will be implemented.
         * @param customThemeResId the resource identifier for custom theme.
         * @since 3.5.0
         */
        public Builder(@NonNull String channelUrl, @StyleRes int customThemeResId) {
            bundle = new Bundle();
            bundle.putInt(StringSet.KEY_THEME_RES_ID, customThemeResId);
            bundle.putString(StringSet.KEY_CHANNEL_URL, channelUrl);
        }

        /**
         * Sets arguments to this fragment.
         *
         * @param args the arguments supplied when the fragment was instantiated.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.5.0
         */
        @NonNull
        public Builder withArguments(@NonNull Bundle args) {
            this.bundle.putAll(args);
            return this;
        }

        /**
         * Sets the title of the header.
         *
         * @param title text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.5.0
         */
        @NonNull
        public Builder setHeaderTitle(@NonNull String title) {
            bundle.putString(StringSet.KEY_HEADER_TITLE, title);
            return this;
        }

        /**
         * Sets the click listener on the message template view clicked.
         * Sets the click listener when the view component that has {@link com.sendbird.uikit.model.Action} is clicked
         *
         * @param handler The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.5.0
         */
        @NonNull
        public Builder setOnMessageTemplateActionHandler(@NonNull OnMessageTemplateActionHandler handler) {
            this.actionHandler = handler;
            return this;
        }

        /**
         * Sets whether the header is used.
         *
         * @param useHeader <code>true</code> if the header is used, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.5.0
         */
        @NonNull
        public Builder setUseHeader(boolean useHeader) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER, useHeader);
            return this;
        }

        /**
         * Sets whether to display the user profile when drawing a message.
         *
         * @param shouldDisplayUserProfile <code>true</code> if the user profile is shown, <code>false</code> otherwise
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.5.0
         */
        @NonNull
        public Builder setDisplayUserProfile(boolean shouldDisplayUserProfile) {
            bundle.putBoolean(StringSet.KEY_SHOULD_DISPLAY_USER_PROFILE, shouldDisplayUserProfile);
            return this;
        }

        /**
         * Sets whether the left button of the header is used.
         *
         * @param useHeaderLeftButton <code>true</code> if the left button of the header is used,
         *                            <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.5.0
         */
        @NonNull
        public Builder setUseHeaderLeftButton(boolean useHeaderLeftButton) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER_LEFT_BUTTON, useHeaderLeftButton);
            return this;
        }

        /**
         * Sets the icon on the left button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.5.0
         */
        @NonNull
        public Builder setHeaderLeftButtonIconResId(@DrawableRes int resId) {
            return setHeaderLeftButtonIcon(resId, null);
        }

        /**
         * Sets the icon on the left button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint  Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.5.0
         */
        @NonNull
        public Builder setHeaderLeftButtonIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_TINT, tint);
            return this;
        }

        /**
         * Sets the click listener on the left button of the header.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.5.0
         */
        @NonNull
        public Builder setOnHeaderLeftButtonClickListener(@NonNull View.OnClickListener listener) {
            this.headerLeftButtonClickListener = listener;
            return this;
        }

        /**
         * Sets the custom fragment. It must inherit {@link NotificationChannelFragment}.
         *
         * @param fragment custom fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.5.0
         */
        @NonNull
        public <T extends NotificationChannelFragment> Builder setCustomFragment(T fragment) {
            this.customFragment = fragment;
            return this;
        }

        /**
         * Sets the icon when the data is not exists.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.5.0
         */
        @NonNull
        public Builder setEmptyIcon(@DrawableRes int resId) {
            return setEmptyIcon(resId, null);
        }

        /**
         * Sets the icon when the data is not exists.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint  Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.5.0
         */
        @NonNull
        public Builder setEmptyIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_EMPTY_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_EMPTY_ICON_TINT, tint);
            return this;
        }

        /**
         * Sets the text when the data is not exists
         *
         * @param resId the resource identifier of text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.5.0
         */
        @NonNull
        public Builder setEmptyText(@StringRes int resId) {
            bundle.putInt(StringSet.KEY_EMPTY_TEXT_RES_ID, resId);
            return this;
        }

        /**
         * Sets the text when error occurs
         *
         * @param resId the resource identifier of text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.5.0
         */
        @NonNull
        public Builder setErrorText(@StringRes int resId) {
            bundle.putInt(StringSet.KEY_ERROR_TEXT_RES_ID, resId);
            return this;
        }

        /**
         * Sets the notification channel adapter.
         *
         * @param adapter the adapter for the notification channel.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.5.0
         */
        @NonNull
        public <T extends NotificationMessageListAdapter> Builder setNotificationMessageAdapter(T adapter) {
            this.adapter = adapter;
            return this;
        }

        /**
         * Sets the custom loading dialog handler
         *
         * @param loadingDialogHandler Interface definition for a callback to be invoked before when the loading dialog is called.
         * @see LoadingDialogHandler
         * @since 3.5.0
         */
        @NonNull
        public Builder setLoadingDialogHandler(@NonNull LoadingDialogHandler loadingDialogHandler) {
            this.loadingDialogHandler = loadingDialogHandler;
            return this;
        }

        /**
         * Sets the message list params for this channel.
         * The reverse and the nextResultSize properties in the MessageListParams are used in the UIKit. Even though you set that property it will be ignored.
         *
         * @param params The MessageListParams instance that you want to use.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.5.0
         */
        @NonNull
        public Builder setMessageListParams(@NonNull MessageListParams params) {
            this.params = params;
            return this;
        }

        /**
         * Creates an {@link NotificationChannelFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link NotificationChannelFragment} applied to the {@link Bundle}.
         * @since 3.5.0
         */
        @NonNull
        public NotificationChannelFragment build() {
            NotificationChannelFragment fragment = customFragment != null ? customFragment : new NotificationChannelFragment();
            fragment.setArguments(bundle);
            fragment.loadingDialogHandler = loadingDialogHandler;
            fragment.headerLeftButtonClickListener = headerLeftButtonClickListener;
            fragment.adapter = adapter;
            fragment.params = params;
            fragment.actionHandler = actionHandler;
            return fragment;
        }
    }
}
