package com.sendbird.uikit.modules.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.uikit.R;
import com.sendbird.uikit.activities.adapter.NotificationMessageListAdapter;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.fragments.ItemAnimator;
import com.sendbird.uikit.interfaces.OnMessageListUpdateHandler;
import com.sendbird.uikit.interfaces.OnMessageTemplateActionHandler;
import com.sendbird.uikit.interfaces.OnPagedDataLoader;
import com.sendbird.uikit.internal.ui.widgets.InnerLinearLayoutManager;
import com.sendbird.uikit.internal.ui.widgets.PagerRecyclerView;
import com.sendbird.uikit.model.Action;

import java.util.List;

/**
 * This class creates and performs a view corresponding the notification message list area in Sendbird UIKit.
 *
 * @since 3.5.0
 */
public class NotificationMessageListComponent {
    @NonNull
    private final Params params;
    @Nullable
    private PagerRecyclerView pagerRecyclerView;
    @Nullable
    private NotificationMessageListAdapter adapter;
    @Nullable
    private OnMessageTemplateActionHandler onMessageTemplateActionHandler;
    @Nullable
    private OnPagedDataLoader<List<BaseMessage>> pagedDataLoader;

    /**
     * Constructor
     *
     * @since 3.5.0
     */
    public NotificationMessageListComponent() {
        this.params = new Params();
    }

    /**
     * Returns a collection of parameters applied to this component.
     *
     * @return {@code Params} applied to this component
     * @since 3.5.0
     */
    @NonNull
    public Params getParams() {
        return params;
    }

    /**
     * Returns the view created by {@link #onCreateView(Context, LayoutInflater, ViewGroup, Bundle)}.
     *
     * @return the topmost view containing this view
     * @since 3.5.0
     */
    @Nullable
    public View getRootView() {
        return this.pagerRecyclerView;
    }

    /**
     * Returns the message list adapter.
     *
     * @return The adapter applied to this list component
     * @since 3.5.0
     */
    @Nullable
    public NotificationMessageListAdapter getAdapter() {
        return adapter;
    }

    /**
     * Called after the component was created to make views.
     * <p><b>If this function is used override, {@link #getRootView()} must also be override.</b></p>
     *
     * @param context  The {@code Context} this component is currently associated with
     * @param inflater The LayoutInflater object that can be used to inflate any views in the component
     * @param parent   The ViewGroup into which the new View will be added
     * @param args     The arguments supplied when the component was instantiated, if any
     * @return Return the View for the UI.
     * @since 3.5.0
     */
    @NonNull
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle args) {
        if (args != null) params.apply(context, args);

        this.pagerRecyclerView = new PagerRecyclerView(context, null, R.attr.sb_component_list);
        pagerRecyclerView.setHasFixedSize(true);
        pagerRecyclerView.setClipToPadding(false);
        pagerRecyclerView.setThreshold(5);
        pagerRecyclerView.setUseDivider(false);
        pagerRecyclerView.setItemAnimator(new ItemAnimator());

        final LinearLayoutManager layoutManager = new InnerLinearLayoutManager(pagerRecyclerView.getContext());
        layoutManager.setReverseLayout(false);
        pagerRecyclerView.setLayoutManager(layoutManager);
        return this.pagerRecyclerView;
    }

    /**
     * Sets the paged data loader for message list.
     *
     * @param pagedDataLoader The paged data loader to be applied to this list component
     * @since 3.5.0
     */
    public void setPagedDataLoader(@NonNull OnPagedDataLoader<List<BaseMessage>> pagedDataLoader) {
        this.pagedDataLoader = pagedDataLoader;
        if (pagerRecyclerView != null)
            pagerRecyclerView.setPager(pagedDataLoader);
    }

    /**
     * Sets the message list adapter to provide child views on demand.
     * <p>When adapter is changed, all existing views are recycled back to the pool. If the pool has only one adapter, it will be cleared.</p>
     *
     * @param adapter The adapter to be applied to this list component
     * @since 3.5.0
     */
    public void setAdapter(@NonNull NotificationMessageListAdapter adapter) {
        this.adapter = adapter;
        if (pagerRecyclerView == null) return;
        if (this.adapter.getOnMessageTemplateActionHandler() == null) {
            this.adapter.setOnMessageTemplateActionHandler(this::onMessageTemplateActionClicked);
        }
        pagerRecyclerView.setAdapter(this.adapter);
    }

    /**
     * Register a callback to be invoked when the view that has an {@link com.sendbird.uikit.model.Action} data is clicked.
     * If an Action is registered in a specific view, it is called when a click event occurs.
     *
     * @param handler The callback that will run
     * @since 3.5.0
     */
    public void setOnMessageTemplateActionHandler(@Nullable OnMessageTemplateActionHandler handler) {
        this.onMessageTemplateActionHandler = handler;
    }

    /**
     * Called when the view that has an {@link com.sendbird.uikit.model.Action} data is clicked.
     *
     * @param view the view that was clicked.
     * @param action the registered Action data
     * @param message the clicked message
     * @since 3.5.0
     */
    protected void onMessageTemplateActionClicked(@NonNull View view, @NonNull Action action, @NonNull BaseMessage message) {
        if (onMessageTemplateActionHandler != null) onMessageTemplateActionHandler.onHandleAction(view, action, message);
    }

    /**
     * Handles a new channel when data has changed.
     *
     * @param channel The latest group channel
     * @since 3.5.0
     */
    public void notifyChannelChanged(@NonNull GroupChannel channel) {
        if (getAdapter() == null) {
            setAdapter(new NotificationMessageListAdapter(channel, params.shouldDisplayUserProfile));
        }
    }

    /**
     * Handles the data needed to draw the message list has changed.
     *
     * @param messageList The list of messages to be drawn
     * @param channel     The latest group channel
     * @param callback    Callback when the message list is updated
     * @since 3.5.0
     */
    public void notifyDataSetChanged(@NonNull List<BaseMessage> messageList, @NonNull GroupChannel channel, @Nullable OnMessageListUpdateHandler callback) {
        if (this.pagerRecyclerView == null) return;
        if (this.adapter != null) {
            adapter.setItems(channel, messageList, callback);
        }
    }

    /**
     * Sets the last seen timestamp to update new badge UI.
     * This value is used to compare whether a message has been newly received.
     *
     * @param lastSeenAt the timestamp last viewed by the user.
     * @since 3.5.0
     */
    @SuppressLint("NotifyDataSetChanged")
    public void notifyLastSeenUpdated(long lastSeenAt) {
        if (this.adapter != null) {
            adapter.updateLastSeenAt(lastSeenAt);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * A collection of parameters, which can be applied to a default View. The values of params are not dynamically applied at runtime.
     * Params cannot be created directly, and it is automatically created together when components are created.
     * <p><b>Since the onCreateView configuring View uses the values of the set Params, we recommend that you set up for Params before the onCreateView is called.</b></p>
     *
     * @see #getParams()
     * @since 3.5.0
     */
    public static class Params {
        private boolean shouldDisplayUserProfile = true;

        /**
         * Sets whether to display the user profile when drawing a message.
         *
         * @param shouldDisplayUserProfile <code>true</code> if the user profile is shown, <code>false</code> otherwise
         * @since 3.5.0
         */
        public void setDisplayUserProfile(boolean shouldDisplayUserProfile) {
            this.shouldDisplayUserProfile = shouldDisplayUserProfile;
        }

        /**
         * Returns whether to display the user profile when drawing a message.
         *
         * @return <code>true</code> if the user profile is shown, <code>false</code> otherwise
         * @since 3.5.0
         */
        public boolean shouldDisplayUserProfile() {
            return shouldDisplayUserProfile;
        }

        /**
         * Apply data that matches keys mapped to Params' properties.
         * {@code KEY_USE_USER_PROFILE} is mapped to {@link #setDisplayUserProfile(boolean)}
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * @since 3.5.0
         */
        @NonNull
        protected Params apply(@NonNull Context context, @NonNull Bundle args) {
            if (args.containsKey(StringSet.KEY_SHOULD_DISPLAY_USER_PROFILE)) {
                setDisplayUserProfile(args.getBoolean(StringSet.KEY_SHOULD_DISPLAY_USER_PROFILE));
            }
            return this;
        }
    }

}
