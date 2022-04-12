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
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.OpenChannel;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.activities.adapter.MessageListAdapter;
import com.sendbird.uikit.activities.adapter.OpenChannelMessageListAdapter;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.fragments.ItemAnimator;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemEventListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.interfaces.OnMessageListUpdateHandler;
import com.sendbird.uikit.interfaces.OnPagedDataLoader;
import com.sendbird.uikit.utils.MessageUtils;
import com.sendbird.uikit.widgets.MessageRecyclerView;
import com.sendbird.uikit.widgets.PagerRecyclerView;

import java.util.List;

/**
 * This class creates and performs a view corresponding the message list area for {@code OpenChannel} in Sendbird UIKit.
 *
 * @since 3.0.0
 */
@SuppressWarnings("unused")
public class OpenChannelMessageListComponent {
    @NonNull
    private final Params params;
    @Nullable
    private MessageRecyclerView messageRecyclerView;
    @Nullable
    private OpenChannelMessageListAdapter adapter;
    @Nullable
    private OnItemClickListener<BaseMessage> messageClickListener;
    @Nullable
    private OnItemClickListener<BaseMessage> messageProfileClickListener;
    @Nullable
    private OnItemLongClickListener<BaseMessage> messageLongClickListener;
    @Nullable
    private OnItemLongClickListener<BaseMessage> messageProfileLongClickListener;
    @Nullable
    private OnPagedDataLoader<List<BaseMessage>> pagedDataLoader;
    @Nullable
    private View.OnClickListener scrollBottomButtonClickListener;
    @Nullable
    private OnItemEventListener<BaseMessage> messageInsertedListener;

    /**
     * Constructor
     *
     * @since 3.0.0
     */
    public OpenChannelMessageListComponent() {
        this.params = new Params();
    }

    /**
     * Returns the view created by {@link #onCreateView(Context, LayoutInflater, ViewGroup, Bundle)}.
     *
     * @return the topmost view containing this view
     * @since 3.0.0
     */
    @Nullable
    public View getRootView() {
        return this.messageRecyclerView;
    }

    /**
     * Returns the recycler view used in the list component by default.
     *
     * @return {@link RecyclerView} used in this component
     * @since 3.0.0
     */
    @Nullable
    public RecyclerView getRecyclerView() {
        return messageRecyclerView != null ? messageRecyclerView.getRecyclerView() : null;
    }

    /**
     * Returns a collection of parameters applied to this component.
     *
     * @return {@code Params} applied to this component
     * @since 3.0.0
     */
    @NonNull
    public Params getParams() {
        return params;
    }

    /**
     * Sets the message list adapter for {@code OpenChannel}. The default is {@code new OpenChannelMessageListAdapter()}.
     * <p>When adapter is changed, all existing views are recycled back to the pool. If the pool has only one adapter, it will be cleared.</p>
     *
     * @param adapter The adapter to be applied to this list component
     * @since 3.0.0
     */
    public <T extends OpenChannelMessageListAdapter> void setAdapter(@NonNull T adapter) {
        this.adapter = adapter;
        if (this.adapter.getOnListItemClickListener() == null) {
            this.adapter.setOnListItemClickListener(this::onListItemClicked);
        }
        if (this.adapter.getOnListItemLongClickListener() == null) {
            this.adapter.setOnListItemLongClickListener(this::onListItemLongClicked);
        }

        if (messageRecyclerView == null) return;
        this.adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (positionStart == 0) {
                    BaseMessage message = adapter.getItem(positionStart);
                    LinearLayoutManager layoutManager = messageRecyclerView.getRecyclerView().getLayoutManager();
                    if ((MessageUtils.isMine(message) ||
                            (layoutManager != null && layoutManager.findFirstVisibleItemPosition() == 0))) {
                        onMessageInserted(message);
                    }
                }
            }
        });
        messageRecyclerView.getRecyclerView().setAdapter(adapter);
    }

    /**
     * Returns the message list adapter for {@code OpenChannel}.
     *
     * @return The adapter applied to this list component
     * @since 3.0.0
     */
    @Nullable
    public OpenChannelMessageListAdapter getAdapter() {
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
     * @since 3.0.0
     */
    @NonNull
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle args) {
        if (args != null) params.apply(context, args);
        this.messageRecyclerView = new MessageRecyclerView(context, null, R.attr.sb_component_list);

        final PagerRecyclerView recyclerView = this.messageRecyclerView.getRecyclerView();
        recyclerView.setHasFixedSize(true);
        recyclerView.setClipToPadding(false);
        recyclerView.setThreshold(5);
        recyclerView.setItemAnimator(new ItemAnimator());

        recyclerView.setOnScrollEndDetectListener(direction -> onScrollEndReaches(direction, this.messageRecyclerView));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (!isScrollOnTheBottom(recyclerView)) {
                    messageRecyclerView.showScrollBottomButton();
                }
            }
        });
        this.messageRecyclerView.getScrollBottomView().setOnClickListener(this::onScrollBottomButtonClicked);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext());
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        if (adapter == null) adapter = new OpenChannelMessageListAdapter(params.useGroupUI);
        setAdapter(adapter);
        return this.messageRecyclerView;
    }

    /********************************************************************************************
     *                                      PRIVATE AREA
     *********************************************************************************************/
    private boolean hasNextMessages() {
        return pagedDataLoader != null && pagedDataLoader.hasNext();
    }

    private boolean isScrollOnTheBottom(@NonNull RecyclerView recyclerView) {
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            return linearLayoutManager.findFirstVisibleItemPosition() <= 0;
        }
        return false;
    }

    private void onScrollEndReaches(@NonNull PagerRecyclerView.ScrollDirection direction, @NonNull MessageRecyclerView messageListView) {
        if (!hasNextMessages() && direction == PagerRecyclerView.ScrollDirection.Bottom) {
            messageListView.hideScrollBottomButton();
        }
    }

    /**
     * Register a callback to be invoked when the button to scroll to the bottom is clicked.
     *
     * @param scrollBottomButtonClickListener The callback that will run
     * @since 3.0.0
     */
    public void setOnScrollBottomButtonClickListener(@Nullable View.OnClickListener scrollBottomButtonClickListener) {
        this.scrollBottomButtonClickListener = scrollBottomButtonClickListener;
    }

    /**
     * Sets the paged data loader for open channel message list.
     *
     * @param pagedDataLoader The paged data loader to be applied to this list component
     * @since 3.0.0
     */
    public void setPagedDataLoader(@NonNull OnPagedDataLoader<List<BaseMessage>> pagedDataLoader) {
        this.pagedDataLoader = pagedDataLoader;
        if (messageRecyclerView != null)
            messageRecyclerView.getRecyclerView().setPager(pagedDataLoader);
    }

    /**
     * Register a callback to be invoked when the message is clicked.
     *
     * @param messageClickListener The callback that will run
     * @since 3.0.0
     */
    public void setOnMessageClickListener(@Nullable OnItemClickListener<BaseMessage> messageClickListener) {
        this.messageClickListener = messageClickListener;
    }

    /**
     * Register a callback to be invoked when the profile view of the message is clicked.
     *
     * @param messageProfileClickListener The callback that will run
     * @since 3.0.0
     */
    public void setOnMessageProfileClickListener(@Nullable OnItemClickListener<BaseMessage> messageProfileClickListener) {
        this.messageProfileClickListener = messageProfileClickListener;
    }

    /**
     * Register a callback to be invoked when the message is long-clicked.
     *
     * @param messageLongClickListener The callback that will run
     * @since 3.0.0
     */
    public void setOnMessageLongClickListener(@Nullable OnItemLongClickListener<BaseMessage> messageLongClickListener) {
        this.messageLongClickListener = messageLongClickListener;
    }

    /**
     * Register a callback to be invoked when the profile view of the message is long-clicked.
     *
     * @param messageProfileLongClickListener The callback that will run
     * @since 3.0.0
     */
    public void setOnMessageProfileLongClickListener(@Nullable OnItemLongClickListener<BaseMessage> messageProfileLongClickListener) {
        this.messageProfileLongClickListener = messageProfileLongClickListener;
    }

    /**
     * Register a callback to be invoked when the message is inserted.
     *
     * @param messageInsertedListener The callback that will run
     * @since 3.0.0
     */
    public void setOnMessageInsertedListener(@Nullable OnItemEventListener<BaseMessage> messageInsertedListener) {
        this.messageInsertedListener = messageInsertedListener;
    }

    private void onListItemClicked(@NonNull View view, @NonNull String identifier, int position, @NonNull BaseMessage message) {
        final BaseMessage.SendingStatus status = message.getSendingStatus();
        if (status == BaseMessage.SendingStatus.PENDING) return;

        switch (identifier) {
            case StringSet.Chat:
                // ClickableViewType.Chat
                onMessageClicked(view, position, message);
                break;
            case StringSet.Profile:
                // ClickableViewType.Profile
                onMessageProfileClicked(view, position, message);
                break;
            case StringSet.QuoteReply:
                // ClickableViewType.Reply
                break;
        }
    }

    private void onListItemLongClicked(@NonNull View view, @NonNull String identifier, int position, @NonNull BaseMessage message) {
        switch (identifier) {
            case StringSet.Chat:
                // ClickableViewType.Chat
                onMessageLongClicked(view, position, message);
                break;
            case StringSet.Profile:
                // ClickableViewType.Profile
                onMessageProfileLongClicked(view, position, message);
                break;
            case StringSet.QuoteReply:
                // ClickableViewType.Reply
                break;
        }
    }

    /**
     * Scrolls to the bottom of the message list.
     *
     * @since 3.0.0
     */
    public void scrollToBottom() {
        if (messageRecyclerView == null) return;
        messageRecyclerView.getRecyclerView().stopScroll();
        messageRecyclerView.getRecyclerView().scrollToPosition(0);
        onScrollEndReaches(PagerRecyclerView.ScrollDirection.Bottom, messageRecyclerView);
    }

    /**
     * Notifies this component that the data needed to draw the message list has changed.
     *
     * @param messageList The list of messages to be drawn
     * @param channel     The latest group channel
     * @param callback    Callback when the message list is updated
     * @since 3.0.0
     */
    public void notifyDataSetChanged(@NonNull List<BaseMessage> messageList, @NonNull OpenChannel channel, @Nullable OnMessageListUpdateHandler callback) {
        if (messageRecyclerView == null) return;
        final OpenChannelMessageListAdapter adapter = this.adapter;
        if (adapter != null) {
            adapter.setItems(channel, messageList, callback);
        }
    }

    /**
     * Notifies this component that the channel data has changed.
     *
     * @param channel The latest open channel
     * @since 3.0.0
     */
    public void notifyChannelChanged(@NonNull OpenChannel channel) {
        if (messageRecyclerView == null) return;
        final boolean isFrozen = channel.isFrozen();
        messageRecyclerView.getBannerView().setVisibility(isFrozen ? View.VISIBLE : View.GONE);
        if (isFrozen) {
            messageRecyclerView.setBannerText(messageRecyclerView.getContext().getString(R.string.sb_text_information_channel_frozen));
        }
    }

    /**
     * Called when the item of the message list is clicked.
     *
     * @param view     The View clicked
     * @param position The position clicked
     * @param message  The message that the clicked item displays
     * @since 3.0.0
     */
    protected void onMessageClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (messageClickListener != null) messageClickListener.onItemClick(view, position, message);
    }

    /**
     * Called when the profile view of the message is clicked.
     *
     * @param view     The View clicked
     * @param position The position clicked
     * @param message  The message that the clicked item displays
     * @since 3.0.0
     */
    protected void onMessageProfileClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (!params.useUserProfile) return;
        if (messageProfileClickListener != null)
            messageProfileClickListener.onItemClick(view, position, message);
    }

    /**
     * Called when the item of the message list is long-clicked.
     *
     * @param view     The View long-clicked
     * @param position The position long-clicked
     * @param message  The message that the long-clicked item displays
     * @since 3.0.0
     */
    protected void onMessageLongClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (messageLongClickListener != null)
            messageLongClickListener.onItemLongClick(view, position, message);
    }

    /**
     * Called when the profile view of the message is long-clicked.
     *
     * @param view     The View long-clicked
     * @param position The position long-clicked
     * @param message  The message that the long-clicked item displays
     * @since 3.0.0
     */
    protected void onMessageProfileLongClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (messageProfileLongClickListener != null)
            messageProfileLongClickListener.onItemLongClick(view, position, message);
    }

    /**
     * Called when the button to scroll to the bottom is clicked.
     *
     * @param view The view that was clicked
     * @since 3.0.0
     */
    protected void onScrollBottomButtonClicked(@NonNull View view) {
        if (scrollBottomButtonClickListener != null) scrollBottomButtonClickListener.onClick(view);
    }

    /**
     * Called when the message is inserted.
     *
     * @param message The message that has been inserted
     * @since 3.0.0
     */
    protected void onMessageInserted(@NonNull BaseMessage message) {
        if (messageInsertedListener != null) messageInsertedListener.onItemEvent(message);
    }


    /**
     * A collection of parameters, which can be applied to a default View. The values of params are not dynamically applied at runtime.
     * Params cannot be created directly, and it is automatically created together when components are created.
     * <p>Since the onCreateView configuring View uses the values of the set Params, we recommend that you set up for Params before the onCreateView is called.</p>
     *
     * @see #getParams()
     * @since 3.0.0
     */
    public static class Params {
        private boolean useGroupUI = true;
        private boolean useUserProfile = SendbirdUIKit.shouldUseDefaultUserProfile();

        /**
         * Constructor
         *
         * @since 3.0.0
         */
        protected Params() {
        }

        /**
         * Sets whether the message group UI is used.
         *
         * @param useMessageGroupUI <code>true</code> if the message group UI is used, <code>false</code> otherwise
         * @since 3.0.0
         */
        public void setUseMessageGroupUI(boolean useMessageGroupUI) {
            this.useGroupUI = useMessageGroupUI;
        }

        /**
         * Sets whether the user profile is shown when the profile of message is clicked.
         *
         * @param useUserProfile <code>true</code> if the user profile is shown, <code>false</code> otherwise
         * @since 3.0.0
         */
        public void setUseUserProfile(boolean useUserProfile) {
            this.useUserProfile = useUserProfile;
        }

        /**
         * Returns whether the user profile uses when the profile of message is clicked.
         *
         * @return <code>true</code> if the user profile is shown, <code>false</code> otherwise
         * @since 3.0.0
         */
        @SuppressLint("KotlinPropertyAccess")
        public boolean shouldUseUserProfile() {
            return useUserProfile;
        }

        /**
         * Returns whether the message group UI is used.
         *
         * @return <code>true</code> if the message group UI is used, <code>false</code> otherwise
         * @since 3.0.0
         */
        public boolean shouldUseGroupUI() {
            return useGroupUI;
        }

        /**
         * Apply data that matches keys mapped to Params' properties.
         * {@code KEY_USE_USER_PROFILE} is mapped to {@link #setUseUserProfile(boolean)}
         * {@code KEY_USE_MESSAGE_GROUP_UI} is mapped to {@link #setUseMessageGroupUI(boolean)}
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * @since 3.0.0
         */
        @NonNull
        protected Params apply(@NonNull Context context, @NonNull Bundle args) {
            if (args.containsKey(StringSet.KEY_USE_USER_PROFILE)) {
                setUseUserProfile(args.getBoolean(StringSet.KEY_USE_USER_PROFILE));
            }
            if (args.containsKey(StringSet.KEY_USE_MESSAGE_GROUP_UI)) {
                setUseMessageGroupUI(args.getBoolean(StringSet.KEY_USE_MESSAGE_GROUP_UI));
            }
            return this;
        }
    }
}
