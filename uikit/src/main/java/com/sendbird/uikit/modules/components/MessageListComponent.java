package com.sendbird.uikit.modules.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.SendingStatus;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.activities.adapter.MessageListAdapter;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.fragments.ItemAnimator;
import com.sendbird.uikit.interfaces.OnEmojiReactionClickListener;
import com.sendbird.uikit.interfaces.OnEmojiReactionLongClickListener;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.interfaces.OnMessageListUpdateHandler;
import com.sendbird.uikit.interfaces.OnPagedDataLoader;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.MessageUIConfig;
import com.sendbird.uikit.model.TextUIConfig;
import com.sendbird.uikit.model.TimelineMessage;
import com.sendbird.uikit.widgets.MessageRecyclerView;
import com.sendbird.uikit.widgets.PagerRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class creates and performs a view corresponding the message list area in Sendbird UIKit.
 *
 * @since 3.0.0
 */
@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "unused"})
public class MessageListComponent {
    @NonNull
    private final AtomicInteger tooltipMessageCount = new AtomicInteger();
    @NonNull
    private final Params params;
    @Nullable
    private MessageRecyclerView messageRecyclerView;

    @Nullable
    private MessageListAdapter adapter;

    @Nullable
    private OnItemClickListener<BaseMessage> messageClickListener;
    @Nullable
    private OnItemClickListener<BaseMessage> messageProfileClickListener;
    @Nullable
    private OnItemClickListener<BaseMessage> quoteReplyMessageClickListener;
    @Nullable
    private OnItemLongClickListener<BaseMessage> messageLongClickListener;
    @Nullable
    private OnItemLongClickListener<BaseMessage> messageProfileLongClickListener;
    @Nullable
    private OnItemLongClickListener<BaseMessage> quoteReplyMessageLongClickListener;
    @Nullable
    private OnEmojiReactionClickListener emojiReactionClickListener;
    @Nullable
    private OnEmojiReactionLongClickListener emojiReactionLongClickListener;
    @Nullable
    private OnItemClickListener<BaseMessage> emojiReactionMoreButtonClickListener;
    @Nullable
    private OnPagedDataLoader<List<BaseMessage>> pagedDataLoader;
    @Nullable
    private View.OnClickListener tooltipClickListener;
    @Nullable
    private View.OnClickListener scrollBottomButtonClickListener;

    /**
     * Constructor
     *
     * @since 3.0.0
     */
    public MessageListComponent() {
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
     * Sets the message list  adapter to provide child views on demand. The default is {@code new MessageListAdapter()}.
     * <p>When adapter is changed, all existing views are recycled back to the pool. If the pool has only one adapter, it will be cleared.</p>
     *
     * @param adapter The adapter to be applied to this list component
     * @since 3.0.0
     */
    public <T extends MessageListAdapter> void setAdapter(@NonNull T adapter) {
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
        if (messageRecyclerView == null) return;
        messageRecyclerView.getRecyclerView().setAdapter(this.adapter);
    }

    /**
     * Returns the message list adapter.
     *
     * @return The adapter applied to this list component
     * @since 3.0.0
     */
    @Nullable
    public MessageListAdapter getAdapter() {
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

        this.messageRecyclerView.getTooltipView().setOnClickListener(this::onMessageTooltipClicked);
        this.messageRecyclerView.getScrollBottomView().setOnClickListener(this::onScrollBottomButtonClicked);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext());
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        if (adapter == null) adapter = new MessageListAdapter(params.useGroupUI);
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
            tooltipMessageCount.set(0);
            messageListView.hideNewMessageTooltip();
            messageListView.hideScrollBottomButton();
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
        if (this.adapter == null) {
            Logger.d("_________ return scrollToFoundPosition");
            return 0;
        }

        final List<BaseMessage> messageList = this.adapter.getItems();
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

        final long firstMessageTs = list.get(size - 1).getCreatedAt();
        final long lastMessageTs = list.get(0).getCreatedAt();
        final long maxMessageTs = Math.max(lastMessageTs, firstMessageTs);
        final long minMessageTs = Math.min(lastMessageTs, firstMessageTs);
        int position = 0;
        if (createdAt >= minMessageTs && createdAt <= maxMessageTs) {
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
        } else if (createdAt >= maxMessageTs) {
            position = lastMessageTs == maxMessageTs ? 0 : (size - 1);
        } else {
            position = firstMessageTs == minMessageTs ? (size - 1) : 0;
        }

        layoutManager.scrollToPositionWithOffset(position, offset);
        return position;
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

    /**
     * Register a callback to be invoked when the tooltip view is clicked.
     *
     * @param tooltipClickListener The callback that will run
     * @since 3.0.0
     */
    public void setOnTooltipClickListener(@Nullable View.OnClickListener tooltipClickListener) {
        this.tooltipClickListener = tooltipClickListener;
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
     * Sets the paged data loader for message list.
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
     * Register a callback to be invoked when the quoted message is clicked.
     *
     * @param quoteReplyMessageClickListener The callback that will run
     * @since 3.0.0
     */
    public void setOnQuoteReplyMessageClickListener(@Nullable OnItemClickListener<BaseMessage> quoteReplyMessageClickListener) {
        this.quoteReplyMessageClickListener = quoteReplyMessageClickListener;
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
     * Register a callback to be invoked when the quoted message is long-clicked.
     *
     * @param quoteReplyMessageLongClickListener The callback that will run
     * @since 3.0.0
     */
    public void setOnQuoteReplyMessageLongClickListener(@Nullable OnItemLongClickListener<BaseMessage> quoteReplyMessageLongClickListener) {
        this.quoteReplyMessageLongClickListener = quoteReplyMessageLongClickListener;
    }

    /**
     * Register a callback to be invoked when the emoji reaction of the message is clicked.
     *
     * @param emojiReactionClickListener The callback that will run
     * @since 3.0.0
     */
    public void setOnEmojiReactionClickListener(@Nullable OnEmojiReactionClickListener emojiReactionClickListener) {
        this.emojiReactionClickListener = emojiReactionClickListener;
    }

    /**
     * Register a callback to be invoked when the emoji reaction of the message is long-clicked.
     *
     * @param emojiReactionLongClickListener The callback that will run
     * @since 3.0.0
     */
    public void setOnEmojiReactionLongClickListener(@Nullable OnEmojiReactionLongClickListener emojiReactionLongClickListener) {
        this.emojiReactionLongClickListener = emojiReactionLongClickListener;
    }

    /**
     * Register a callback to be invoked when the button to see more emojis on the message is clicked.
     *
     * @param emojiReactionMoreButtonClickListener The callback that will run
     * @since 3.0.0
     */
    public void setOnEmojiReactionMoreButtonClickListener(@Nullable OnItemClickListener<BaseMessage> emojiReactionMoreButtonClickListener) {
        this.emojiReactionMoreButtonClickListener = emojiReactionMoreButtonClickListener;
    }

    private void onListItemClicked(@NonNull View view, @NonNull String identifier, int position, @NonNull BaseMessage message) {
        final SendingStatus status = message.getSendingStatus();
        if (status == SendingStatus.PENDING) return;

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
                onQuoteReplyMessageClicked(view, position, message);
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
                onQuoteReplyMessageLongClicked(view, position, message);
                break;
        }
    }

    private void needToCheckScrollBottomButton(@NonNull MessageRecyclerView messageListView) {
        messageListView.postDelayed(() -> {
            if (!isScrollOnTheBottom(messageListView.getRecyclerView())) {
                messageListView.showScrollBottomButton();
            } else {
                messageListView.hideScrollBottomButton();
            }
        }, 200);
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
     * Animates the message on the message list.
     *
     * @param message Message to be animated
     * @since 3.0.0
     */
    public void animateMessage(@NonNull BaseMessage message) {
        if (messageRecyclerView == null) return;
        messageRecyclerView.postDelayed(() -> startAnimationForReplyMessage(messageRecyclerView.getContext(), message.getMessageId()), 200);
    }

    /**
     * Moves the screen to the focused message, based on the created timestamp of message.
     *
     * @param viewPoint            The created timestamp of the message you want to focus on
     * @param shouldAnimateMessage {@code true} animate the message after focusing on it
     * @since 3.0.0
     */
    public void moveToFocusedMessage(long viewPoint, @Nullable BaseMessage shouldAnimateMessage) {
        Logger.d(">> MessageListComponent::moveToFocusedMessage(), startingPoint=%s", viewPoint);
        if (messageRecyclerView == null) return;
        int scrollPosition = scrollToViewPointIfPossible(viewPoint, messageRecyclerView);
        if (scrollPosition > 0) {
            needToCheckScrollBottomButton(messageRecyclerView);
        }

        if (shouldAnimateMessage != null) {
            animateMessage(shouldAnimateMessage);
        }
    }

    /**
     * After the messages are updated, calculate the current position and re-calculate the position of the scroll.
     *
     * @param scrollToBottomIfPossible Whether to scroll to the bottom when there are more messages at the bottom
     * @since 3.0.0
     */
    public void notifyMessagesFilled(boolean scrollToBottomIfPossible) {
        if (messageRecyclerView == null) return;
        int firstVisibleItemPosition = messageRecyclerView.getRecyclerView().findFirstVisibleItemPosition();

        if (firstVisibleItemPosition == 0 && !hasNextMessages() && scrollToBottomIfPossible) {
            scrollToBottom();
        }
    }

    /**
     * After receiving the message from another user, handle the necessary tasks at this component.
     *
     * @param showTooltipIfPossible Whether to show the tooltip when new messages are received
     * @since 3.0.0
     */
    public void notifyOtherMessageReceived(boolean showTooltipIfPossible) {
        if (messageRecyclerView == null) return;
        int firstVisibleItemPosition = messageRecyclerView.getRecyclerView().findFirstVisibleItemPosition();

        if (firstVisibleItemPosition > 0 || showTooltipIfPossible) {
            messageRecyclerView.showNewMessageTooltip(getTooltipMessage(messageRecyclerView.getContext(), tooltipMessageCount.incrementAndGet()));
            return;
        }
        if (!hasNextMessages()) {
            if (firstVisibleItemPosition == 0) {
                scrollToBottom();
            }
        }
    }

    /**
     * Handles the data needed to draw the message list has changed.
     *
     * @param messageList The list of messages to be drawn
     * @param channel     The latest group channel
     * @param callback    Callback when the message list is updated
     * @since 3.0.0
     */
    public void notifyDataSetChanged(@NonNull List<BaseMessage> messageList, @NonNull GroupChannel channel, @Nullable OnMessageListUpdateHandler callback) {
        if (messageRecyclerView == null) return;
        final MessageListAdapter adapter = this.adapter;
        if (adapter != null) {
            adapter.setItems(channel, messageList, callback);
        }
    }

    /**
     * Handles a new channel when data has changed.
     *
     * @param channel The latest group channel
     * @since 3.0.0
     */
    public void notifyChannelChanged(@NonNull GroupChannel channel) {
        if (messageRecyclerView == null) return;
        drawFrozenBanner(channel.isFrozen());
    }

    /**
     * Returns the text on the tooltip.
     *
     * @param context The {@code Context} this view is currently associated with
     * @param count   Number of new messages
     * @return Text to be shown on the tooltip
     * @since 3.0.0
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
     * Called when the quoted message of the message is clicked.
     *
     * @param view     The View clicked
     * @param position The position clicked
     * @param message  The message that the clicked item displays
     * @since 3.0.0
     */
    protected void onQuoteReplyMessageClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (quoteReplyMessageClickListener != null)
            quoteReplyMessageClickListener.onItemClick(view, position, message);
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
     * Called when the quoted message of the message is long-clicked.
     *
     * @param view     The View long-clicked
     * @param position The position long-clicked
     * @param message  The message that the long-clicked item displays
     * @since 3.0.0
     */
    protected void onQuoteReplyMessageLongClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (quoteReplyMessageLongClickListener != null)
            quoteReplyMessageLongClickListener.onItemLongClick(view, position, message);
    }

    /**
     * Called when the emoji reaction of the message is clicked.
     *
     * @param view        The view that was clicked
     * @param position    The position that was clicked
     * @param message     The message that was clicked
     * @param reactionKey The reaction key that was clicked
     * @since 3.0.0
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
     * @since 3.0.0
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
     * @since 3.0.0
     */
    protected void onEmojiReactionMoreButtonClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (emojiReactionMoreButtonClickListener != null)
            emojiReactionMoreButtonClickListener.onItemClick(view, position, message);
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
     * Called when the tooltip view is clicked.
     *
     * @param view The view that was clicked
     * @since 3.0.0
     */
    protected void onMessageTooltipClicked(@NonNull View view) {
        if (tooltipClickListener != null) tooltipClickListener.onClick(view);
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

        private long initialStartingPoint = Long.MAX_VALUE;

        @NonNull
        private final MessageUIConfig messageUIConfig;

        /**
         * Constructor
         *
         * @since 3.0.0
         */
        protected Params() {
            this.messageUIConfig = new MessageUIConfig();
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
         * Sets the timestamp to load the messages with.
         *
         * @param startTimeMillis The timestamp to load initially
         * @since 3.0.0
         */
        public void setInitialStartingPoint(long startTimeMillis) {
            this.initialStartingPoint = startTimeMillis;
        }

        /**
         * Returns the timestamp to load the messages with.
         *
         * @return The timestamp to load initially
         * @since 3.0.0
         */
        public long getInitialStartingPoint() {
            return initialStartingPoint;
        }

        /**
         * Sets the UI configuration of mentioned text.
         *
         * @param configSentFromMe     the UI configuration of mentioned text in the message that was sent from me.
         * @param configSentFromOthers the UI configuration of mentioned text in the message that was sent from others.
         * @since 3.0.0
         */
        public void setMentionUIConfig(@Nullable TextUIConfig configSentFromMe, @Nullable TextUIConfig configSentFromOthers) {
            if (configSentFromMe != null) this.messageUIConfig.getMyMentionUIConfig().apply(configSentFromMe);
            if (configSentFromOthers != null) this.messageUIConfig.getOtherMentionUIConfig().apply(configSentFromOthers);
        }

        /**
         * Sets the UI configuration of searched text.
         *
         * @param configSentFromMe       the UI configuration of edited text mark in the message that was sent from me.
         * @param configSentFromOthers   the UI configuration of edited text mark in the message that was sent from others.
         * @since 3.0.0
         */
        public void setEditedTextMarkUIConfig(@Nullable TextUIConfig configSentFromMe, @Nullable TextUIConfig configSentFromOthers) {
            if (configSentFromMe != null) this.messageUIConfig.getMyEditedTextMarkUIConfig().apply(configSentFromMe);
            if (configSentFromOthers != null) this.messageUIConfig.getOtherEditedTextMarkUIConfig().apply(configSentFromOthers);
        }

        /**
         * Apply data that matches keys mapped to Params' properties.
         * {@code KEY_STARTING_POINT} is mapped to {@link #setInitialStartingPoint(long)}
         * {@code KEY_USE_USER_PROFILE} is mapped to {@link #setUseUserProfile(boolean)}
         * {@code KEY_USE_MESSAGE_GROUP_UI} is mapped to {@link #setUseMessageGroupUI(boolean)}
         * {@code KEY_MENTION_UI_CONFIG_SENT_FROM_ME} and {@code KEY_MENTION_UI_CONFIG_SENT_FROM_OTHERS} are mapped to {@link #setMentionUIConfig(TextUIConfig, TextUIConfig)}
         * {@code KEY_EDITED_MARK_UI_CONFIG_SENT_FROM_ME} and {@code KEY_EDITED_MARK_UI_CONFIG_SENT_FROM_OTHERS} are mapped to {@link #setEditedTextMarkUIConfig(TextUIConfig, TextUIConfig)}
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * @since 3.0.0
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
            return this;
        }
    }
}
