package com.sendbird.uikit.modules.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.FeedbackRating;
import com.sendbird.android.message.SendingStatus;
import com.sendbird.android.user.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.activities.adapter.BaseMessageListAdapter;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.fragments.ItemAnimator;
import com.sendbird.uikit.interfaces.OnConsumableClickListener;
import com.sendbird.uikit.interfaces.OnEmojiReactionClickListener;
import com.sendbird.uikit.interfaces.OnEmojiReactionLongClickListener;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.interfaces.OnMessageListUpdateHandler;
import com.sendbird.uikit.interfaces.OnPagedDataLoader;
import com.sendbird.uikit.internal.extensions.ChannelExtensionsKt;
import com.sendbird.uikit.internal.extensions.MessageExtensionsKt;
import com.sendbird.uikit.internal.interfaces.OnFeedbackRatingClickListener;
import com.sendbird.uikit.internal.ui.widgets.InnerLinearLayoutManager;
import com.sendbird.uikit.internal.ui.widgets.MessageRecyclerView;
import com.sendbird.uikit.internal.ui.widgets.PagerRecyclerView;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.MessageUIConfig;
import com.sendbird.uikit.model.TextUIConfig;
import com.sendbird.uikit.model.TimelineMessage;
import com.sendbird.uikit.model.configurations.ChannelConfig;
import com.sendbird.uikit.model.configurations.UIKitConfig;

import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

abstract public class BaseMessageListComponent<LA extends BaseMessageListAdapter> {
    @NonNull
    final AtomicInteger tooltipMessageCount = new AtomicInteger();
    @NonNull
    private final Params params;
    @Nullable
    MessageRecyclerView messageRecyclerView;
    private final boolean useMessageTooltip;
    private final boolean useScrollFirstButton;
    private final ItemAnimator itemAnimator = new ItemAnimator();

    @Nullable
    private LA adapter;

    @Nullable
    private OnItemClickListener<BaseMessage> messageClickListener;
    @Nullable
    private OnItemClickListener<BaseMessage> messageProfileClickListener;
    @Nullable
    private OnItemClickListener<User> messageMentionClickListener;

    @Nullable
    private OnFeedbackRatingClickListener feedbackRatingClickListener;

    @Nullable
    private OnItemLongClickListener<BaseMessage> messageLongClickListener;
    @Nullable
    private OnItemLongClickListener<BaseMessage> messageProfileLongClickListener;
    @Nullable
    private OnEmojiReactionClickListener emojiReactionClickListener;
    @Nullable
    private OnEmojiReactionLongClickListener emojiReactionLongClickListener;
    @Nullable
    private OnItemClickListener<BaseMessage> emojiReactionMoreButtonClickListener;
    @Nullable
    OnPagedDataLoader<List<BaseMessage>> pagedDataLoader;
    @Nullable
    private View.OnClickListener tooltipClickListener;
    @Nullable
    @Deprecated
    private View.OnClickListener scrollBottomButtonClickListener;
    @Nullable
    private OnConsumableClickListener scrollFirstButtonClickListener;
    @Nullable
    private View.OnClickListener unreadCountTooltipClickListener;

    BaseMessageListComponent(@NonNull Params params, boolean useMessageTooltip, boolean useScrollFirstButton) {
        this.params = params;
        this.useMessageTooltip = useMessageTooltip;
        this.useScrollFirstButton = useScrollFirstButton;
    }

    /**
     * Returns the view created by {@link #onCreateView(Context, LayoutInflater, ViewGroup, Bundle)}.
     *
     * @return the topmost view containing this view
     * since 3.0.0
     */
    @Nullable
    public View getRootView() {
        return this.messageRecyclerView;
    }

    /**
     * Returns the recycler view used in the list component by default.
     *
     * @return {@link RecyclerView} used in this component
     * since 3.0.0
     */
    @Nullable
    public RecyclerView getRecyclerView() {
        return messageRecyclerView != null ? messageRecyclerView.getRecyclerView() : null;
    }

    /**
     * Returns a collection of parameters applied to this component.
     *
     * @return {@code Params} applied to this component
     * since 3.0.0
     */
    @NonNull
    public Params getParams() {
        return params;
    }

    /**
     * Sets the message list adapter to provide child views on demand.
     * <p>When adapter is changed, all existing views are recycled back to the pool. If the pool has only one adapter, it will be cleared.</p>
     *
     * @param adapter The adapter to be applied to this list component
     * since 3.0.0
     */
    public void setAdapter(@NonNull LA adapter) {
        this.adapter = adapter;

        if (this.adapter.getMessageUIConfig() == null) {
            this.adapter.setMessageUIConfig(params.messageUIConfig);
        }
        if (this.adapter.getOnListItemClickListener() == null) {
            this.adapter.setOnListItemClickListener(this::onListItemClicked);
        }
        if (this.adapter.getOnListItemLongClickListener() == null) {
            this.adapter.setOnListItemLongClickListener(this::onListItemLongClicked);
        }
        if (this.adapter.getEmojiReactionClickListener() == null) {
            this.adapter.setEmojiReactionClickListener(this::onEmojiReactionClicked);
        }
        if (this.adapter.getEmojiReactionLongClickListener() == null) {
            this.adapter.setEmojiReactionLongClickListener(this::onEmojiReactionLongClicked);
        }
        if (this.adapter.getEmojiReactionMoreButtonClickListener() == null) {
            this.adapter.setEmojiReactionMoreButtonClickListener(this::onEmojiReactionMoreButtonClicked);
        }
        if (this.adapter.getMentionClickListener() == null) {
            this.adapter.setMentionClickListener(this::onMessageMentionClicked);
        }

        if (this.adapter.getFeedbackRatingClickListener() == null) {
            this.adapter.setFeedbackRatingClickListener(this::onFeedbackRatingClicked);
        }

        if (messageRecyclerView == null) return;
        messageRecyclerView.getRecyclerView().setAdapter(this.adapter);
    }

    /**
     * Returns the message list adapter.
     *
     * @return The adapter applied to this list component
     * since 3.0.0
     */
    @Nullable
    public LA getAdapter() {
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
     * since 3.0.0
     */
    @NonNull
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle args) {
        if (args != null) {
            params.apply(context, args);
        }

        this.messageRecyclerView = createMessageRecyclerView(context);
        final PagerRecyclerView recyclerView = this.messageRecyclerView.getRecyclerView();
        recyclerView.setHasFixedSize(true);
        recyclerView.setClipToPadding(false);
        recyclerView.setThreshold(5);
        recyclerView.setItemAnimator(itemAnimator);
        recyclerView.useReverseData();
        messageRecyclerView.setOnScrollFirstButtonClickListener(this::onScrollFirstButtonClicked);
        recyclerView.setOnScrollEndDetectListener(direction -> onScrollEndReaches(direction, messageRecyclerView));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (useScrollFirstButton && !isScrollOnTheFirst(recyclerView)) {
                    messageRecyclerView.showScrollFirstButton();
                }
            }
        });

        messageRecyclerView.getTooltipView().setOnClickListener(this::onMessageTooltipClicked);
        messageRecyclerView.getUnreadCountTooltipIcon().setOnClickListener(this::onUnreadCountTooltipClicked);

        final LinearLayoutManager layoutManager = createInnerLayoutManager(recyclerView);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        return this.messageRecyclerView;
    }

    /**
     * Scrolls to the bottom of the message list.
     *
     * since 3.0.0
     * @deprecated
     * <p> Use {@link #scrollToFirst()} instead.
     */
    @Deprecated
    public void scrollToBottom() {
        scrollToFirst();
    }

    /**
     * Scrolls to the first position of the recycler view.
     *
     * since 3.2.2
     */
    public void scrollToFirst() {
        if (messageRecyclerView == null) return;
        messageRecyclerView.getRecyclerView().stopScroll();
        messageRecyclerView.getRecyclerView().scrollToPosition(0);
        onScrollEndReaches(PagerRecyclerView.ScrollDirection.Bottom, messageRecyclerView);
    }

    /**
     * After receiving the message from another user, handle the necessary tasks at this component.
     *
     * @param showTooltipIfPossible Whether to show the tooltip when new messages are received
     * since 3.0.0
     */
    public void notifyOtherMessageReceived(boolean showTooltipIfPossible) {
        int firstVisibleItemPosition = getFirstVisibleItemPosition();

        if (useMessageTooltip && (firstVisibleItemPosition > 0 || showTooltipIfPossible)) {
            messageRecyclerView.showNewMessageTooltip(getTooltipMessage(messageRecyclerView.getContext(), tooltipMessageCount.incrementAndGet()));
            return;
        }
        scrollToFirstIfLastMessageVisible(true);
    }

    /**
     * Moves the screen to the focused message, based on the created timestamp of message.
     *
     * @param viewPoint            The created timestamp of the message you want to focus on
     * @param shouldAnimateMessage {@code true} animate the message after focusing on it
     * since 3.0.0
     */
    public void moveToFocusedMessage(long viewPoint, @Nullable BaseMessage shouldAnimateMessage) {
        Logger.d(">> BaseMessageListComponent::moveToFocusedMessage(), startingPoint=%s", viewPoint);
        if (messageRecyclerView == null) return;
        int scrollPosition = scrollToViewPointIfPossible(viewPoint, messageRecyclerView);
        if (scrollPosition > 0) {
            needToCheckScrollFirstButton(messageRecyclerView);
        }

        if (shouldAnimateMessage != null) {
            animateMessage(shouldAnimateMessage);
        }
    }

    /**
     * Register a callback to be invoked when the tooltip view is clicked.
     *
     * @param tooltipClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnTooltipClickListener(@Nullable View.OnClickListener tooltipClickListener) {
        this.tooltipClickListener = tooltipClickListener;
    }

    /**
     * Register a callback to be invoked when the unread count tooltip is clicked.
     *
     * @param unreadCountTooltipClickListener The callback that will run
     * since 3.24.0
     */
    public void setOnUnreadTooltipClickListener(@Nullable View.OnClickListener unreadCountTooltipClickListener) {
        this.unreadCountTooltipClickListener = unreadCountTooltipClickListener;
    }

    /**
     * Sets the paged data loader for message list.
     *
     * @param pagedDataLoader The paged data loader to be applied to this list component
     * since 3.0.0
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
     * since 3.0.0
     */
    public void setOnMessageClickListener(@Nullable OnItemClickListener<BaseMessage> messageClickListener) {
        this.messageClickListener = messageClickListener;
    }

    /**
     * Register a callback to be invoked when the profile view of the message is clicked.
     *
     * @param messageProfileClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnMessageProfileClickListener(@Nullable OnItemClickListener<BaseMessage> messageProfileClickListener) {
        this.messageProfileClickListener = messageProfileClickListener;
    }

    /**
     * Register a callback to be invoked when the mentioned user of the message is clicked.
     *
     * @param messageMentionClickListener The callback that will run
     * since 3.5.3
     */
    public void setOnMessageMentionClickListener(@Nullable OnItemClickListener<User> messageMentionClickListener) {
        this.messageMentionClickListener = messageMentionClickListener;
    }

    /**
     * Register a callback to be invoked when the feedback rating of the message is clicked.
     *
     * @param feedbackRatingClickListener The callback that will run
     * since 3.13.0
     */
    public void setOnFeedbackRatingClickListener(@Nullable OnFeedbackRatingClickListener feedbackRatingClickListener) {
        this.feedbackRatingClickListener = feedbackRatingClickListener;
    }

    /**
     * Register a callback to be invoked when the message is long-clicked.
     *
     * @param messageLongClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnMessageLongClickListener(@Nullable OnItemLongClickListener<BaseMessage> messageLongClickListener) {
        this.messageLongClickListener = messageLongClickListener;
    }

    /**
     * Register a callback to be invoked when the profile view of the message is long-clicked.
     *
     * @param messageProfileLongClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnMessageProfileLongClickListener(@Nullable OnItemLongClickListener<BaseMessage> messageProfileLongClickListener) {
        this.messageProfileLongClickListener = messageProfileLongClickListener;
    }

    /**
     * Register a callback to be invoked when the emoji reaction of the message is clicked.
     *
     * @param emojiReactionClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnEmojiReactionClickListener(@Nullable OnEmojiReactionClickListener emojiReactionClickListener) {
        this.emojiReactionClickListener = emojiReactionClickListener;
    }

    /**
     * Register a callback to be invoked when the emoji reaction of the message is long-clicked.
     *
     * @param emojiReactionLongClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnEmojiReactionLongClickListener(@Nullable OnEmojiReactionLongClickListener emojiReactionLongClickListener) {
        this.emojiReactionLongClickListener = emojiReactionLongClickListener;
    }

    /**
     * Register a callback to be invoked when the button to see more emojis on the message is clicked.
     *
     * @param emojiReactionMoreButtonClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnEmojiReactionMoreButtonClickListener(@Nullable OnItemClickListener<BaseMessage> emojiReactionMoreButtonClickListener) {
        this.emojiReactionMoreButtonClickListener = emojiReactionMoreButtonClickListener;
    }

    /**
     * Register a callback to be invoked when the button to scroll to the bottom is clicked.
     *
     * @param scrollBottomButtonClickListener The callback that will run
     * since 3.0.0
     * @deprecated
     * This method is no longer acceptable to invoke event.
     * <p> Use {@link #setOnScrollFirstButtonClickListener(OnConsumableClickListener)} instead.
     */
    @Deprecated
    public void setOnScrollBottomButtonClickListener(@Nullable View.OnClickListener scrollBottomButtonClickListener) {
        this.scrollBottomButtonClickListener = scrollBottomButtonClickListener;
    }

    /**
     * Register a callback to be invoked when the button to scroll to the first position is clicked.
     *
     * @param scrollFirstButtonClickListener The callback that will run
     * since 3.2.2
     */
    public void setOnScrollFirstButtonClickListener(@Nullable OnConsumableClickListener scrollFirstButtonClickListener) {
        this.scrollFirstButtonClickListener = scrollFirstButtonClickListener;
    }

    /**
     * Called when the message item is clicked.
     *
     * @param view The View clicked
     * @param identifier The identifier identifying which area is clicked
     * @param position The position clicked
     * @param message The message that the clicked item displays
     * since 3.3.0
     */
    abstract void onListItemClicked(@NonNull View view, @NonNull String identifier, int position, @NonNull BaseMessage message);

    /**
     * Called when the message item is long-clicked.
     *
     * @param view The View clicked
     * @param identifier The identifier identifying which area is long-clicked
     * @param position The position clicked
     * @param message The message that the clicked item displays
     * since 3.3.0
     */
    abstract void onListItemLongClicked(@NonNull View view, @NonNull String identifier, int position, @NonNull BaseMessage message);

    /**
     * Animates the message on the message list.
     *
     * @param message Message to be animated
     * since 3.0.0
     */
    public void animateMessage(@NonNull BaseMessage message) {
        if (messageRecyclerView == null) return;
        messageRecyclerView.postDelayed(() -> startAnimationForReplyMessage(messageRecyclerView.getContext(), message.getMessageId()), 200);
    }

    /**
     * Handles the data needed to draw the message list has changed.
     *
     * @param messageList The list of messages to be drawn
     * @param channel     The latest group channel
     * @param callback    Callback when the message list is updated
     * since 3.0.0
     */
    public void notifyDataSetChanged(@NonNull List<BaseMessage> messageList, @NonNull GroupChannel channel, @Nullable OnMessageListUpdateHandler callback) {
        if (messageRecyclerView == null) return;
        final LA adapter = this.adapter;
        if (adapter != null) {
            adapter.setItems(channel, messageList, callback);
        }
    }

    /**
     * Handles a new channel when data has changed.
     *
     * @param channel The latest group channel
     * since 3.0.0
     */
    public void notifyChannelChanged(@NonNull GroupChannel channel) {
        if (messageRecyclerView == null) return;

        if (params.shouldUseBanner()) {
            drawFrozenBanner(channel.isFrozen());
        }
        if (ChannelExtensionsKt.getContainsBot(channel)) {
            messageRecyclerView.getRecyclerView().setItemAnimator(null);
        } else {
            messageRecyclerView.getRecyclerView().setItemAnimator(itemAnimator);
        }
    }

    /**
     * After the messages are updated, calculate the current position and re-calculate the position of the scroll.
     *
     * @param scrollToFirstIfPossible Whether to scroll to the bottom when there are more messages at the bottom
     * since 3.0.0
     */
    public void notifyMessagesFilled(boolean scrollToFirstIfPossible) {
        scrollToFirstIfLastMessageVisible(scrollToFirstIfPossible);
    }

    /**
     * After updating the typing indicator, determines whether to scroll to the bottom.
     *
     * since 3.11.0
     */
    public void notifyTypingIndicatorUpdated(boolean scrollToFirstIfPossible) {
        scrollToFirstIfLastMessageVisible(scrollToFirstIfPossible);
    }

    /**
     * Checks if the new line is visible on screen and shows or hides the unread message count tooltip.
     *
     * @param channel the GroupChannel containing the messages and unread count
     * @return <code>true</code> if the new line is visible, <code>false</code> otherwise
     * since 3.24.0
     */
    public boolean updateTooltipByNewLine(GroupChannel channel) {
        if (messageRecyclerView == null) return false;

        if (isNewLineVisible(channel)) {
            messageRecyclerView.hideUnreadCountTooltip();
            return true;
        } else {
            int count = channel.getUnreadMessageCount();
            if (count == 0) {
                messageRecyclerView.hideUnreadCountTooltip();
                hideNewMessageTooltip();
            } else {
                messageRecyclerView.showUnreadCountTooltip(getUnreadCountTooltipMessage(messageRecyclerView.getContext(), count));
            }

            return false;
        }
    }

    /**
     * Returns the text on the tooltip.
     *
     * @param context The {@code Context} this view is currently associated with
     * @param count   Number of new messages
     * @return Text to be shown on the tooltip
     * since 3.0.0
     */
    @NonNull
    protected String getTooltipMessage(@NonNull Context context, int count) {
        if (messageRecyclerView == null) return "";
        String result = "";
        if (count > 1) {
            result = String.format(Locale.US, context.getString(R.string.sb_text_channel_tooltip_with_count), count);
        } else if (count == 1) {
            result = String.format(Locale.US, context.getString(R.string.sb_text_channel_tooltip), count);
        }
        return result;
    }

    /**
     * Returns the text on the unread count tooltip.
     *
     * @param context The {@code Context} this view is currently associated with
     * @param count   Number of unread message counts
     * @return Text to be shown on the unread count tooltip
     * since 3.24.0
     */
    @NonNull
    protected String getUnreadCountTooltipMessage(@NonNull Context context, int count) {
        if (messageRecyclerView == null) return "";
        String result = "";

        String countText = count > 99 ? context.getString(R.string.sb_text_channel_message_unread_count_max) : String.valueOf(count);

        if (count > 1) {
            result = String.format(Locale.US, context.getString(R.string.sb_text_channel_unread_count_tooltip_with_count), countText);
        } else if (count == 1) {
            result = String.format(Locale.US, context.getString(R.string.sb_text_channel_unread_count_tooltip), countText);
        }

        return result;
    }

    /**
     * Called when the item of the message list is clicked.
     *
     * @param view     The View clicked
     * @param position The position clicked
     * @param message  The message that the clicked item displays
     * since 3.0.0
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
     * since 3.0.0
     */
    protected void onMessageProfileClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (!params.useUserProfile) return;
        if (messageProfileClickListener != null)
            messageProfileClickListener.onItemClick(view, position, message);
    }

    /**
     * Called when the mentioned user of the message is clicked.
     *
     * @param view     The View clicked
     * @param position The position clicked
     * @param user  The mentioned user that the clicked item displays
     * since 3.5.3
     */
    protected void onMessageMentionClicked(@NonNull View view, int position, @NonNull User user) {
        if (messageMentionClickListener != null)
            messageMentionClickListener.onItemClick(view, position, user);
    }

    /**
     * Called when the feedback rating of the message is clicked.
     *
     * @param rating The clicked feedback rating
     * since 3.13.0
     */
    protected void onFeedbackRatingClicked(@NonNull BaseMessage message, @NonNull FeedbackRating rating) {
        if (feedbackRatingClickListener != null) feedbackRatingClickListener.onFeedbackClicked(message, rating);
    }

    /**
     * Called when the item of the message list is long-clicked.
     *
     * @param view     The View long-clicked
     * @param position The position long-clicked
     * @param message  The message that the long-clicked item displays
     * since 3.0.0
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
     * since 3.0.0
     */
    protected void onMessageProfileLongClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (messageProfileLongClickListener != null)
            messageProfileLongClickListener.onItemLongClick(view, position, message);
    }

    /**
     * Called when the emoji reaction of the message is clicked.
     *
     * @param view        The view that was clicked
     * @param position    The position that was clicked
     * @param message     The message that was clicked
     * @param reactionKey The reaction key that was clicked
     * since 3.0.0
     */
    protected void onEmojiReactionClicked(@NonNull View view, int position, @NonNull BaseMessage message, @NonNull String reactionKey) {
        if (emojiReactionClickListener != null)
            emojiReactionClickListener.onEmojiReactionClick(view, position, message, reactionKey);
    }

    /**
     * Called when the emoji reaction of the message is long-clicked.
     *
     * @param view        The view that was long-clicked
     * @param position    The position that was long-clicked
     * @param message     The message that was long-clicked
     * @param reactionKey The reaction key that was long-clicked
     * since 3.0.0
     */
    protected void onEmojiReactionLongClicked(@NonNull View view, int position, @NonNull BaseMessage message, @NonNull String reactionKey) {
        if (emojiReactionLongClickListener != null)
            emojiReactionLongClickListener.onEmojiReactionLongClick(view, position, message, reactionKey);
    }

    /**
     * Called when the button to see more emojis on the message is clicked.
     *
     * @param view     The view that was clicked
     * @param position The position that was clicked
     * @param message  The message that was clicked
     * since 3.0.0
     */
    protected void onEmojiReactionMoreButtonClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (emojiReactionMoreButtonClickListener != null)
            emojiReactionMoreButtonClickListener.onItemClick(view, position, message);
    }

    /**
     * Called when the tooltip view is clicked.
     *
     * @param view The view that was clicked
     * since 3.0.0
     */
    protected void onMessageTooltipClicked(@NonNull View view) {
        if (tooltipClickListener != null) tooltipClickListener.onClick(view);
    }

    /**
     * Called when the button to scroll to the bottom is clicked.
     *
     * @param view The view that was clicked
     * since 3.0.0
     * @deprecated 3.2.2
     * This method is no longer acceptable to invoke event.
     * <p> Use {@link #onScrollFirstButtonClicked(View)} instead.
     */
    @Deprecated
    protected void onScrollBottomButtonClicked(@NonNull View view) {
        if (scrollBottomButtonClickListener != null) scrollBottomButtonClickListener.onClick(view);
    }

    /**
     * Called when the button to scroll to the first position is clicked.
     *
     * @param view The view that was clicked
     * since 3.2.2
     */
    protected boolean onScrollFirstButtonClicked(@NonNull View view) {
        onScrollBottomButtonClicked(view);
        if (scrollFirstButtonClickListener != null) return scrollFirstButtonClickListener.onClick(view);
        return false;
    }

    /********************************************************************************************
     *                                      PRIVATE AREA
     *********************************************************************************************/
    private void onUnreadCountTooltipClicked(@NonNull View view) {
        if (unreadCountTooltipClickListener != null) unreadCountTooltipClickListener.onClick(view);
    }

    private boolean isNewLineVisible(GroupChannel channel) {
        if (MessageExtensionsKt.getNewLineMessageId() == null) return false;

        if (getRecyclerView() == null || adapter == null) return false;

        final List<BaseMessage> messages = adapter.getItems();
        if (messages.isEmpty()) return false;

        final LinearLayoutManager layoutManager = (LinearLayoutManager) getRecyclerView().getLayoutManager();
        if (layoutManager == null) return false;

        final int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        final int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

        if (firstVisibleItemPosition == RecyclerView.NO_POSITION
            || lastVisibleItemPosition == RecyclerView.NO_POSITION) return false;

        final long lastRead = channel.getMyLastRead();
        BaseMessage firstVisibleMessage = messages.get(firstVisibleItemPosition);
        BaseMessage lastVisibleMessage = messages.get(lastVisibleItemPosition);

        return firstVisibleMessage.getCreatedAt() >= lastRead && lastVisibleMessage.getCreatedAt() <= lastRead;
    }

    private void hideNewMessageTooltip() {
        if (messageRecyclerView == null) return;

        if (useMessageTooltip) {
            tooltipMessageCount.set(0);
            messageRecyclerView.hideNewMessageTooltip();
        }
    }

    boolean hasNextMessages() {
        return pagedDataLoader != null && pagedDataLoader.hasNext();
    }

    private void startAnimationForReplyMessage(@NonNull Context context, long targetMessageId) {
        if (this.adapter != null) {
            final Animation animation = AnimationUtils.loadAnimation(context, R.anim.shake_quoted_message);
            this.adapter.startAnimation(animation, targetMessageId);
        }
    }

    private void drawFrozenBanner(boolean isFrozen) {
        if (messageRecyclerView == null) return;
        messageRecyclerView.getBannerView().setVisibility(isFrozen ? View.VISIBLE : View.GONE);
        if (isFrozen) {
            messageRecyclerView.setBannerText(messageRecyclerView.getContext().getString(R.string.sb_text_information_channel_frozen));
        }
    }

    private int scrollToViewPointIfPossible(long viewPoint, @NonNull MessageRecyclerView messageRecyclerView) {
        int selectionPosition = 0;
        if (viewPoint >= 0) {
            int offset = messageRecyclerView.getRecyclerView().getHeight() / 2;
            selectionPosition = scrollToFoundPosition(viewPoint, offset, messageRecyclerView);
        }
        return selectionPosition;
    }

    private int scrollToFoundPosition(long createdAt, int offset, MessageRecyclerView messageRecyclerView) {
        Logger.d("_________ scrollToFoundPosition( %s )", createdAt);
        final LA adapter = getAdapter();
        if (adapter == null) {
            Logger.d("_________ return scrollToFoundPosition");
            return 0;
        }

        final List<BaseMessage> messageList = adapter.getItems();
        if (messageList.isEmpty()) {
            Logger.d("_________ return scrollToFoundPosition");
            return 0;
        }

        final List<BaseMessage> list = new ArrayList<>(messageList);
        final int size = list.size();
        final LinearLayoutManager layoutManager = messageRecyclerView.getRecyclerView().getLayoutManager();
        if (layoutManager == null) {
            return 0;
        }

        // filter failed, pending messages
        int newIndex = 0;
        for (int i = 0; i < size; i++) {
            BaseMessage message = list.get(i);
            if (message.getSendingStatus() == SendingStatus.SUCCEEDED) {
                newIndex = i;
                break;
            }
        }

        final long latestMessageTs = list.get(newIndex).getCreatedAt();
        final long oldestMessageTs = list.get(size - 1).getCreatedAt();
        int position = 0;
        if (createdAt >= oldestMessageTs && createdAt <= latestMessageTs) {
            for (int i = size - 1; i >= 0; i--) {
                BaseMessage message = list.get(i);
                if (message instanceof TimelineMessage) continue;

                long ct = message.getCreatedAt();
                //Logger.d("_________ [%s] : %s, [%s]", i, ct, message.getMessage());
                if (createdAt <= ct) {
                    Logger.d("_________ found message=%s, i=%s", message.getMessage(), i);
                    position = i;
                    break;
                }
            }
        } else if (createdAt >= latestMessageTs) {
            position = 0;
        } else {
            position = size - 1;
        }

        // To show the top of the item view, scroll to next item position with offset.
        layoutManager.scrollToPositionWithOffset(
            position + 1 >= size ? position : position + 1, offset
        );
        return position;
    }

    private void needToCheckScrollFirstButton(@NonNull MessageRecyclerView messageListView) {
        if (useScrollFirstButton) {
            messageListView.postDelayed(() -> {
                if (!isScrollOnTheFirst(messageListView.getRecyclerView())) {
                    messageListView.showScrollFirstButton();
                } else {
                    messageListView.hideScrollFirstButton();
                }
            }, 200);
        }
    }

    private boolean isScrollOnTheFirst(@NonNull RecyclerView recyclerView) {
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            return linearLayoutManager.findFirstVisibleItemPosition() <= 0;
        }
        return false;
    }

    private void onScrollEndReaches(@NonNull PagerRecyclerView.ScrollDirection direction, @NonNull MessageRecyclerView messageListView) {
        final PagerRecyclerView.ScrollDirection endDirection = PagerRecyclerView.ScrollDirection.Bottom;
        if (!hasNextMessages() && direction == endDirection) {
            hideNewMessageTooltip();
            if (useScrollFirstButton) {
                messageListView.hideScrollFirstButton();
            }
        }
    }

    private void scrollToFirstIfLastMessageVisible(boolean scrollToFirstIfPossible) {
        int firstVisibleItemPosition = getFirstVisibleItemPosition();

        if (firstVisibleItemPosition == 0 && !hasNextMessages() && scrollToFirstIfPossible) {
            scrollToFirst();
        }
    }

    // Implement the logic to handle the case when messageRecyclerView is null.
    // If messageRecyclerView is null, consider returning an appropriate default value -1.
    private int getFirstVisibleItemPosition() {
        if (messageRecyclerView == null) return -1;
        return messageRecyclerView.getRecyclerView().findFirstVisibleItemPosition();
    }

    /**
     * A collection of parameters, which can be applied to a default View. The values of params are not dynamically applied at runtime.
     * Params cannot be created directly, and it is automatically created together when components are created.
     * <p>Since the onCreateView configuring View uses the values of the set Params, we recommend that you set up for Params before the onCreateView is called.</p>
     *
     * @see #getParams()
     * since 3.0.0
     */
    public static class Params {
        private boolean useGroupUI = true;
        private boolean useUserProfile = UIKitConfig.getCommon().getEnableUsingDefaultUserProfile();
        private long initialStartingPoint = Long.MAX_VALUE;
        private boolean useBanner = true;

        @NonNull
        private final MessageUIConfig messageUIConfig;
        @NonNull
        private ChannelConfig channelConfig = UIKitConfig.getGroupChannelConfig();

        /**
         * Constructor
         *
         * since 3.0.0
         */
        protected Params() {
            this.messageUIConfig = new MessageUIConfig();
        }

        /**
         * Sets whether the message group UI is used.
         *
         * @param useMessageGroupUI <code>true</code> if the message group UI is used, <code>false</code> otherwise
         * since 3.0.0
         */
        public void setUseMessageGroupUI(boolean useMessageGroupUI) {
            this.useGroupUI = useMessageGroupUI;
        }

        /**
         * Sets whether the user profile is shown when the profile of message is clicked.
         *
         * @param useUserProfile <code>true</code> if the user profile is shown, <code>false</code> otherwise
         * since 3.0.0
         */
        public void setUseUserProfile(boolean useUserProfile) {
            this.useUserProfile = useUserProfile;
        }

        /**
         * Sets whether the message list banner is used.
         *
         * @param useBanner <code>true</code> if the message list banner is used, <code>false</code> otherwise.
         * since 3.3.0
         */
        public void setUseBanner(boolean useBanner) {
            this.useBanner = useBanner;
        }

        /**
         * Sets {@link ChannelConfig} that will be used in this component.
         * Use {@code UIKitConfig.groupChannelConfig.clone()} for the default value.
         * Example usage:
         *
         * <pre>
         * val messageListComponent = MessageListComponent()
         * messageListComponent.params.channelConfig = UIKitConfig.groupChannelConfig.clone().apply {
         *     this.enableMention = true
         * }
         * </pre>
         *
         * @param channelConfig Channel config to be used in this component.
         * since 3.6.0
         */
        public void setChannelConfig(@NonNull ChannelConfig channelConfig) {
            this.channelConfig = channelConfig;
        }

        /**
         * Returns {@link ChannelConfig} that will be used in this component.
         *
         * @return Channel config to be used in this component.
         * since 3.6.0
         */
        @NonNull
        public ChannelConfig getChannelConfig() {
            return channelConfig;
        }

        /**
         * Returns whether the user profile uses when the profile of message is clicked.
         *
         * @return <code>true</code> if the user profile is shown, <code>false</code> otherwise
         * since 3.0.0
         */
        public boolean shouldUseUserProfile() {
            return useUserProfile;
        }

        /**
         * Returns whether the message group UI is used.
         *
         * @return <code>true</code> if the message group UI is used, <code>false</code> otherwise
         * since 3.0.0
         */
        public boolean shouldUseGroupUI() {
            return useGroupUI;
        }

        /**
         * Returns whether the message list banner is used.
         *
         * @return <code>true</code> if the message list banner is used, <code>false</code> otherwise
         * since 3.0.0
         */
        public boolean shouldUseBanner() {
            return useBanner;
        }

        /**
         * Sets the timestamp to load the messages with.
         *
         * @param startTimeMillis The timestamp to load initially
         * since 3.0.0
         */
        public void setInitialStartingPoint(long startTimeMillis) {
            this.initialStartingPoint = startTimeMillis;
        }

        /**
         * Returns the timestamp to load the messages with.
         *
         * @return The timestamp to load initially
         * since 3.0.0
         */
        public long getInitialStartingPoint() {
            return initialStartingPoint;
        }

        /**
         * Sets the UI configuration of mentioned text.
         *
         * @param configSentFromMe     the UI configuration of mentioned text in the message that was sent from me.
         * @param configSentFromOthers the UI configuration of mentioned text in the message that was sent from others.
         * since 3.0.0
         */
        public void setMentionUIConfig(@Nullable TextUIConfig configSentFromMe, @Nullable TextUIConfig configSentFromOthers) {
            if (configSentFromMe != null) this.messageUIConfig.getMyMentionUIConfig().apply(configSentFromMe);
            if (configSentFromOthers != null)
                this.messageUIConfig.getOtherMentionUIConfig().apply(configSentFromOthers);
        }

        /**
         * Sets the UI configuration of edited mark text.
         *
         * @param configSentFromMe       the UI configuration of edited text mark in the message that was sent from me.
         * @param configSentFromOthers   the UI configuration of edited text mark in the message that was sent from others.
         * since 3.0.0
         */
        public void setEditedTextMarkUIConfig(@Nullable TextUIConfig configSentFromMe, @Nullable TextUIConfig configSentFromOthers) {
            if (configSentFromMe != null) this.messageUIConfig.getMyEditedTextMarkUIConfig().apply(configSentFromMe);
            if (configSentFromOthers != null)
                this.messageUIConfig.getOtherEditedTextMarkUIConfig().apply(configSentFromOthers);
        }

        /**
         * Sets the UI configuration of message text.
         *
         * @param configSentFromMe       the UI configuration of the message text that was sent from me.
         * @param configSentFromOthers   the UI configuration of the message text that was sent from others.
         * since 3.1.1
         */
        public void setMessageTextUIConfig(@Nullable TextUIConfig configSentFromMe, @Nullable TextUIConfig configSentFromOthers) {
            if (configSentFromMe != null) this.messageUIConfig.getMyMessageTextUIConfig().apply(configSentFromMe);
            if (configSentFromOthers != null)
                this.messageUIConfig.getOtherMessageTextUIConfig().apply(configSentFromOthers);
        }

        /**
         * Sets the UI configuration of message sentAt text.
         *
         * @param configSentFromMe       the UI configuration of the message sentAt text that was sent from me.
         * @param configSentFromOthers   the UI configuration of the message sentAt text that was sent from others.
         * since 3.1.1
         */
        public void setSentAtTextUIConfig(@Nullable TextUIConfig configSentFromMe, @Nullable TextUIConfig configSentFromOthers) {
            if (configSentFromMe != null) this.messageUIConfig.getMySentAtTextUIConfig().apply(configSentFromMe);
            if (configSentFromOthers != null)
                this.messageUIConfig.getOtherSentAtTextUIConfig().apply(configSentFromOthers);
        }

        /**
         * Sets the UI configuration of sender nickname text.
         *
         * @param configSentFromOthers   the UI configuration of the sender nickname text that was sent from others.
         * since 3.1.1
         */
        public void setNicknameTextUIConfig(@NonNull TextUIConfig configSentFromOthers) {
            this.messageUIConfig.getOtherNicknameTextUIConfig().apply(configSentFromOthers);
        }

        /**
         * Sets the UI configuration of the replied parent message text.
         *
         * @param configRepliedMessage the UI configuration of the replied parent message text.
         * since 3.2.1
         */
        public void setRepliedMessageTextUIConfig(@NonNull TextUIConfig configRepliedMessage) {
            this.messageUIConfig.getRepliedMessageTextUIConfig().apply(configRepliedMessage);
        }

        /**
         * Sets the UI configuration of message background drawable.
         *
         * @param drawableSentFromMe     the UI configuration of the message background that was sent from me.
         * @param drawableSentFromOthers the UI configuration of the message background that was sent from others.
         * since 3.1.1
         */
        public void setMessageBackground(@Nullable Drawable drawableSentFromMe, @Nullable Drawable drawableSentFromOthers) {
            if (drawableSentFromMe != null) this.messageUIConfig.setMyMessageBackground(drawableSentFromMe);
            if (drawableSentFromOthers != null) this.messageUIConfig.setOtherMessageBackground(drawableSentFromOthers);
        }

        /**
         * Sets the UI configuration of message reaction list background drawable.
         *
         * @param drawableSentFromMe     the UI configuration of the message reaction list background drawable that was sent from me.
         * @param drawableSentFromOthers the UI configuration of the message reaction list background drawable that was sent from others.
         * since 3.1.1
         */
        public void setReactionListBackground(@Nullable Drawable drawableSentFromMe, @Nullable Drawable drawableSentFromOthers) {
            if (drawableSentFromMe != null) this.messageUIConfig.setMyReactionListBackground(drawableSentFromMe);
            if (drawableSentFromOthers != null)
                this.messageUIConfig.setOtherReactionListBackground(drawableSentFromOthers);
        }

        /**
         * Sets the UI configuration of ogtag message background drawable.
         *
         * @param drawableSentFromMe     the UI configuration of the ogtag message background drawable that was sent from me.
         * @param drawableSentFromOthers the UI configuration of the ogtag message background drawable that was sent from others.
         * since 3.1.1
         */
        public void setOgtagBackground(@Nullable Drawable drawableSentFromMe, @Nullable Drawable drawableSentFromOthers) {
            if (drawableSentFromMe != null) this.messageUIConfig.setMyOgtagBackground(drawableSentFromMe);
            if (drawableSentFromOthers != null) this.messageUIConfig.setOtherOgtagBackground(drawableSentFromOthers);
        }

        /**
         * Sets the UI configuration of the linked text color in the message text.
         *
         * @param color the UI configuration of the linked text color.
         * since 3.1.1
         */
        public void setLinkedTextColor(@NonNull ColorStateList color) {
            this.messageUIConfig.setLinkedTextColor(color);
        }

        /**
         * Apply data that matches keys mapped to Params' properties.
         * {@code KEY_STARTING_POINT} is mapped to {@link #setInitialStartingPoint(long)}
         * {@code KEY_USE_USER_PROFILE} is mapped to {@link #setUseUserProfile(boolean)}
         * {@code KEY_USE_MESSAGE_GROUP_UI} is mapped to {@link #setUseMessageGroupUI(boolean)}
         * {@code KEY_MENTION_UI_CONFIG_SENT_FROM_ME} and {@code KEY_MENTION_UI_CONFIG_SENT_FROM_OTHERS} are mapped to {@link #setMentionUIConfig(TextUIConfig, TextUIConfig)}
         * {@code KEY_EDITED_MARK_UI_CONFIG_SENT_FROM_ME} and {@code KEY_EDITED_MARK_UI_CONFIG_SENT_FROM_OTHERS} are mapped to {@link #setEditedTextMarkUIConfig(TextUIConfig, TextUIConfig)}
         * {@code KEY_MESSAGE_TEXT_UI_CONFIG_SENT_FROM_ME} and {@code KEY_MESSAGE_TEXT_UI_CONFIG_SENT_FROM_OTHERS} are mapped to {@link #setMessageTextUIConfig(TextUIConfig, TextUIConfig)}
         * {@code KEY_SENT_AT_TEXT_UI_CONFIG_SENT_FROM_ME} and {@code KEY_SENT_AT_TEXT_UI_CONFIG_SENT_FROM_OTHERS} are mapped to {@link #setSentAtTextUIConfig(TextUIConfig, TextUIConfig)}
         * {@code KEY_NICKNAME_TEXT_UI_CONFIG_SENT_FROM_OTHERS} is mapped to {@link #setNicknameTextUIConfig(TextUIConfig)}
         * {@code KEY_REPLIED_MESSAGE_TEXT_UI_CONFIG} is mapped to {@link #setRepliedMessageTextUIConfig(TextUIConfig)}
         * {@code KEY_MESSAGE_BACKGROUND_SENT_FROM_ME} and {@code KEY_MESSAGE_BACKGROUND_SENT_FROM_OTHERS} are mapped to {@link #setMessageBackground(Drawable, Drawable)}
         * {@code KEY_REACTION_LIST_BACKGROUND_SENT_FROM_ME} and {@code KEY_REACTION_LIST_BACKGROUND_SENT_FROM_OTHERS} are mapped to {@link #setReactionListBackground(Drawable, Drawable)}
         * {@code KEY_OGTAG_BACKGROUND_SENT_FROM_ME} and {@code KEY_OGTAG_BACKGROUND_SENT_FROM_OTHERS} are mapped to {@link #setOgtagBackground(Drawable, Drawable)}
         * {@code KEY_LINKED_TEXT_COLOR} is mapped to {@link #setLinkedTextColor(ColorStateList)}
         * {@code KEY_CHANNEL_CONFIG} is mapped to {@link #setChannelConfig(ChannelConfig)}
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.0.0
         */
        @NonNull
        protected Params apply(@NonNull Context context, @NonNull Bundle args) {
            if (args.containsKey(StringSet.KEY_STARTING_POINT)) {
                setInitialStartingPoint(args.getLong(StringSet.KEY_STARTING_POINT));
            }
            if (args.containsKey(StringSet.KEY_USE_USER_PROFILE)) {
                setUseUserProfile(args.getBoolean(StringSet.KEY_USE_USER_PROFILE));
            }
            if (args.containsKey(StringSet.KEY_USE_MESSAGE_GROUP_UI)) {
                setUseMessageGroupUI(args.getBoolean(StringSet.KEY_USE_MESSAGE_GROUP_UI));
            }
            setMentionUIConfig(args.getParcelable(StringSet.KEY_MENTION_UI_CONFIG_SENT_FROM_ME), args.getParcelable(StringSet.KEY_MENTION_UI_CONFIG_SENT_FROM_OTHERS));
            setEditedTextMarkUIConfig(args.getParcelable(StringSet.KEY_EDITED_MARK_UI_CONFIG_SENT_FROM_ME), args.getParcelable(StringSet.KEY_EDITED_MARK_UI_CONFIG_SENT_FROM_OTHERS));
            setMessageTextUIConfig(args.getParcelable(StringSet.KEY_MESSAGE_TEXT_UI_CONFIG_SENT_FROM_ME), args.getParcelable(StringSet.KEY_MESSAGE_TEXT_UI_CONFIG_SENT_FROM_OTHERS));
            setSentAtTextUIConfig(args.getParcelable(StringSet.KEY_SENT_AT_TEXT_UI_CONFIG_SENT_FROM_ME), args.getParcelable(StringSet.KEY_SENT_AT_TEXT_UI_CONFIG_SENT_FROM_OTHERS));
            final TextUIConfig nicknameTextUIConfig = args.getParcelable(StringSet.KEY_NICKNAME_TEXT_UI_CONFIG_SENT_FROM_OTHERS);
            if (nicknameTextUIConfig != null) {
                setNicknameTextUIConfig(nicknameTextUIConfig);
            }
            final TextUIConfig repliedMessageTextUIConfig = args.getParcelable(StringSet.KEY_REPLIED_MESSAGE_TEXT_UI_CONFIG);
            if (repliedMessageTextUIConfig != null) {
                setRepliedMessageTextUIConfig(repliedMessageTextUIConfig);
            }

            Drawable messageBackgroundSentFromMe = null;
            Drawable messageBackgroundSentFromOthers = null;
            Drawable reactionListBackgroundSentFromMe = null;
            Drawable reactionListBackgroundSentFromOthers = null;
            Drawable ogtagBackgroundSentFromMe = null;
            Drawable ogtagBackgroundSentFromOthers = null;
            if (args.containsKey(StringSet.KEY_MESSAGE_BACKGROUND_SENT_FROM_ME)) {
                messageBackgroundSentFromMe = AppCompatResources.getDrawable(context, args.getInt(StringSet.KEY_MESSAGE_BACKGROUND_SENT_FROM_ME));
            }
            if (args.containsKey(StringSet.KEY_MESSAGE_BACKGROUND_SENT_FROM_OTHERS)) {
                messageBackgroundSentFromOthers = AppCompatResources.getDrawable(context, args.getInt(StringSet.KEY_MESSAGE_BACKGROUND_SENT_FROM_OTHERS));
            }
            if (args.containsKey(StringSet.KEY_REACTION_LIST_BACKGROUND_SENT_FROM_ME)) {
                reactionListBackgroundSentFromMe = AppCompatResources.getDrawable(context, args.getInt(StringSet.KEY_REACTION_LIST_BACKGROUND_SENT_FROM_ME));
            }
            if (args.containsKey(StringSet.KEY_REACTION_LIST_BACKGROUND_SENT_FROM_OTHERS)) {
                reactionListBackgroundSentFromOthers = AppCompatResources.getDrawable(context, args.getInt(StringSet.KEY_REACTION_LIST_BACKGROUND_SENT_FROM_OTHERS));
            }
            if (args.containsKey(StringSet.KEY_OGTAG_BACKGROUND_SENT_FROM_ME)) {
                ogtagBackgroundSentFromMe = AppCompatResources.getDrawable(context, args.getInt(StringSet.KEY_OGTAG_BACKGROUND_SENT_FROM_ME, 0));
            }
            if (args.containsKey(StringSet.KEY_OGTAG_BACKGROUND_SENT_FROM_OTHERS)) {
                ogtagBackgroundSentFromOthers = AppCompatResources.getDrawable(context, args.getInt(StringSet.KEY_OGTAG_BACKGROUND_SENT_FROM_OTHERS, 0));
            }
            setMessageBackground(messageBackgroundSentFromMe, messageBackgroundSentFromOthers);
            setReactionListBackground(reactionListBackgroundSentFromMe, reactionListBackgroundSentFromOthers);
            setOgtagBackground(ogtagBackgroundSentFromMe, ogtagBackgroundSentFromOthers);

            if (args.containsKey(StringSet.KEY_LINKED_TEXT_COLOR)) {
                final ColorStateList linkedTextColor = AppCompatResources.getColorStateList(context, args.getInt(StringSet.KEY_LINKED_TEXT_COLOR));
                if (linkedTextColor != null) setLinkedTextColor(linkedTextColor);
            }
            if (args.containsKey(StringSet.KEY_USE_MESSAGE_LIST_BANNER)) {
                setUseBanner(args.getBoolean(StringSet.KEY_USE_MESSAGE_LIST_BANNER));
            }
            if (args.containsKey(StringSet.KEY_CHANNEL_CONFIG)) {
                setChannelConfig(args.getParcelable(StringSet.KEY_CHANNEL_CONFIG));
            }

            return this;
        }

        @TestOnly
        @NonNull
        MessageUIConfig getMessageUIConfig() {
            return messageUIConfig;
        }
    }

    @VisibleForTesting
    @NonNull
    MessageRecyclerView createMessageRecyclerView(@NonNull Context context) {
        return new MessageRecyclerView(context, null, R.attr.sb_component_list);
    }

    @VisibleForTesting
    @NonNull
    InnerLinearLayoutManager createInnerLayoutManager(PagerRecyclerView recyclerView) {
        return new InnerLinearLayoutManager(recyclerView.getContext());
    }

    @TestOnly
    boolean useMessageTooltip() {
        return useMessageTooltip;
    }

    @TestOnly
    boolean useScrollFirstButton() {
        return useScrollFirstButton;
    }
}

