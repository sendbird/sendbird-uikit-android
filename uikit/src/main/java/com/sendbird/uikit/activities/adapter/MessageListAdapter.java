package com.sendbird.uikit.activities.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.DiffUtil;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.Reaction;
import com.sendbird.uikit.R;
import com.sendbird.uikit.activities.viewholder.GroupChannelMessageViewHolder;
import com.sendbird.uikit.activities.viewholder.MessageType;
import com.sendbird.uikit.activities.viewholder.MessageViewHolder;
import com.sendbird.uikit.activities.viewholder.MessageViewHolderFactory;
import com.sendbird.uikit.interfaces.OnEmojiReactionClickListener;
import com.sendbird.uikit.interfaces.OnEmojiReactionLongClickListener;
import com.sendbird.uikit.interfaces.OnIdentifiableItemClickListener;
import com.sendbird.uikit.interfaces.OnIdentifiableItemLongClickListener;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnMessageListUpdateHandler;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.MessageUIConfig;
import com.sendbird.uikit.utils.ReactionUtils;
import com.sendbird.uikit.utils.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MessageListAdapter provides a binding from a {@link BaseMessage} type data set to views that are displayed within a RecyclerView.
 */
public class MessageListAdapter extends BaseMessageAdapter<BaseMessage, MessageViewHolder> {
    @NonNull
    private List<BaseMessage> messageList = new ArrayList<>();
    @Nullable
    private GroupChannel channel;
    @Nullable
    private OnEmojiReactionClickListener emojiReactionClickListener;
    @Nullable
    private OnEmojiReactionLongClickListener emojiReactionLongClickListener;
    @Nullable
    private OnItemClickListener<BaseMessage> emojiReactionMoreButtonClickListener;

    @Nullable
    private OnIdentifiableItemClickListener<BaseMessage> listItemClickListener;
    @Nullable
    private OnIdentifiableItemLongClickListener<BaseMessage> listItemLongClickListener;
    private final boolean useMessageGroupUI;
    private final boolean useReverseLayout;
    @Nullable
    private MessageUIConfig messageUIConfig;

    // the worker must be a single thread.
    @NonNull
    private final ExecutorService differWorker = Executors.newSingleThreadExecutor();
    @NonNull
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Constructor
     *
     * @param useMessageGroupUI <code>true</code> if the message group UI is used, <code>false</code> otherwise.
     * @since 3.0.0
     */
    public MessageListAdapter(boolean useMessageGroupUI) {
        this(null, useMessageGroupUI);
    }

    /**
     * Constructor
     *
     * @param channel The {@link GroupChannel} that contains the data needed for this adapter
     */
    public MessageListAdapter(@Nullable GroupChannel channel) {
        this(channel, true);
    }

    /**
     * Constructor
     *
     * @param channel The {@link GroupChannel} that contains the data needed for this adapter
     * @param useMessageGroupUI <code>true</code> if the message group UI is used, <code>false</code> otherwise.
     * @since 2.2.0
     */
    public MessageListAdapter(@Nullable GroupChannel channel, boolean useMessageGroupUI) {
        this(channel, useMessageGroupUI, true);
    }

    /**
     * Constructor
     *
     * @param channel The {@link GroupChannel} that contains the data needed for this adapter
     * @param useMessageGroupUI <code>true</code> if the message group UI is used, <code>false</code> otherwise.
     * @param useReverseLayout  <code>true</code> if the message list is reversed, <code>false</code> otherwise.
     * @since 3.2.2
     */
    public MessageListAdapter(@Nullable GroupChannel channel, boolean useMessageGroupUI, boolean useReverseLayout) {
        if (channel != null) this.channel = GroupChannel.clone(channel);
        this.useMessageGroupUI = useMessageGroupUI;
        this.useReverseLayout = useReverseLayout;
        setHasStableIds(true);
    }

    /**
     * Called when RecyclerView needs a new {@link MessageViewHolder} of the given type to represent
     * an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new {@link MessageViewHolder} that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(MessageViewHolder, int)
     */
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final TypedValue values = new TypedValue();
        parent.getContext().getTheme().resolveAttribute(R.attr.sb_component_list, values, true);
        final Context contextWrapper = new ContextThemeWrapper(parent.getContext(), values.resourceId);
        LayoutInflater inflater = LayoutInflater.from(contextWrapper);
        MessageViewHolder viewHolder = MessageViewHolderFactory.createViewHolder(inflater,
                parent,
                MessageType.from(viewType),
                useMessageGroupUI);

        viewHolder.setMessageUIConfig(messageUIConfig);

        final Map<String, View> views = viewHolder.getClickableViewMap();
        for (Map.Entry<String, View> entry : views.entrySet()) {
            final String identifier = entry.getKey();
            entry.getValue().setOnClickListener(v -> {
                int messagePosition = viewHolder.getBindingAdapterPosition();
                if (messagePosition != NO_POSITION) {
                    if (listItemClickListener != null) {
                        listItemClickListener.onIdentifiableItemClick(v, identifier, messagePosition, getItem(messagePosition));
                    }
                }
            });

            entry.getValue().setOnLongClickListener(v -> {
                int messagePosition = viewHolder.getBindingAdapterPosition();
                if (messagePosition != NO_POSITION) {
                    if (listItemLongClickListener != null) {
                        listItemLongClickListener.onIdentifiableItemLongClick(v, identifier, messagePosition, getItem(messagePosition));
                    }

                    return true;
                }
                return false;
            });
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            final Object lastPayload = payloads.get(payloads.size() - 1);
            if (lastPayload instanceof Animation) {
                final Animation animation = (Animation) lastPayload;
                holder.itemView.startAnimation(animation);
            }
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link MessageViewHolder#itemView} to reflect the item at the given
     * position.
     *
     * @param holder The {@link MessageViewHolder} which should be updated to represent
     *               the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, final int position) {
        BaseMessage prev = null;
        BaseMessage next = null;
        BaseMessage current = getItem(position);

        int itemCount = getItemCount();
        if (position < itemCount - 1) {
            prev = getItem(position + 1);
        }

        if (position > 0) {
            next = getItem(position - 1);
        }

        if (ReactionUtils.useReaction(channel) && holder instanceof GroupChannelMessageViewHolder) {
            GroupChannelMessageViewHolder groupChannelHolder = (GroupChannelMessageViewHolder) holder;
            List<Reaction> reactionList = current.getReactions();
            groupChannelHolder.setEmojiReaction(reactionList, (view, reactionPosition, reactionKey) -> {
                int messagePosition = holder.getBindingAdapterPosition();
                if (messagePosition != NO_POSITION && emojiReactionClickListener != null) {
                    emojiReactionClickListener.onEmojiReactionClick(
                            view,
                            reactionPosition,
                            getItem(messagePosition),
                            reactionKey
                    );
                }
            }, (view, reactionPosition, reactionKey) -> {
                int messagePosition = groupChannelHolder.getBindingAdapterPosition();
                if (messagePosition != NO_POSITION && emojiReactionLongClickListener != null) {
                    emojiReactionLongClickListener.onEmojiReactionLongClick(
                            view,
                            reactionPosition,
                            getItem(messagePosition),
                            reactionKey
                    );
                }
            }, v -> {
                int messagePosition = groupChannelHolder.getBindingAdapterPosition();
                if (messagePosition != NO_POSITION && emojiReactionMoreButtonClickListener != null) {
                    emojiReactionMoreButtonClickListener.onItemClick(
                            v,
                            messagePosition,
                            getItem(messagePosition)
                    );
                }
            });
        }

        if (channel != null) {
            holder.onBindViewHolder(channel, prev, current, next, useReverseLayout);
        }
    }

    /**
     * Sets the configurations of the message's properties to highlight text.
     *
     * @param messageUIConfig the configurations of the message's properties to highlight text.
     * @see com.sendbird.uikit.model.TextUIConfig
     * @since 3.0.0
     */
    public void setMessageUIConfig(@Nullable MessageUIConfig messageUIConfig) {
        this.messageUIConfig = messageUIConfig;
    }

    /**
     * Returns the configurations of the message's properties to highlight text.
     *
     * @return the configurations of the message's properties to highlight text.
     * @since 3.0.0
     */
    @Nullable
    public MessageUIConfig getMessageUIConfig() {
        return this.messageUIConfig;
    }

    /**
     * Return the view type of the {@link MessageViewHolder} at <code>position</code> for the purposes
     * of view recycling.
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at <code>position</code>.
     * @see MessageViewHolderFactory#getViewType(BaseMessage)
     */
    @Override
    public int getItemViewType(int position) {
        BaseMessage message = getItem(position);
        return MessageViewHolderFactory.getViewType(message);
    }

    /**
     * Return ID for the message at <code>position</code>.
     *
     * @param position Adapter position to query
     * @return the stable ID of the item at position
     */
    @Override
    public long getItemId(int position) {
        BaseMessage item = getItem(position);

        // When itemId of the pending message and the sent message are the same,
        // there is no flickering.
        // (The hashcode of the message is different from pending and sent)
        if (TextUtils.isEmpty(item.getRequestId())) {
            return item.getMessageId();
        } else {
            try {
                return Long.parseLong(item.getRequestId());
            } catch (Exception e) {
                return item.getMessageId();
            }
        }
    }

    /**
     * Sets channel that related with a list of messages
     *
     * @param channel {@code GroupChannel} that related with a list of messages
     */
    public void setChannel(@NonNull GroupChannel channel) {
        this.channel = GroupChannel.clone(channel);
    }

    /**
     * Sets the {@link List<BaseMessage>} to be displayed.
     *
     * @param messageList list to be displayed
     * @since 2.2.0
     */
    public void setItems(@NonNull final GroupChannel channel, @NonNull final List<BaseMessage> messageList, @Nullable OnMessageListUpdateHandler callback) {
        final GroupChannel copiedChannel = GroupChannel.clone(channel);
        final List<BaseMessage> copiedMessage = Collections.unmodifiableList(messageList);
        differWorker.submit(() -> {
            final CountDownLatch lock = new CountDownLatch(1);
            final MessageDiffCallback diffCallback = new MessageDiffCallback(MessageListAdapter.this.channel, channel, MessageListAdapter.this.messageList, messageList, useMessageGroupUI, useReverseLayout);
            final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

            mainHandler.post(() -> {
                try {
                    MessageListAdapter.this.messageList = copiedMessage;
                    MessageListAdapter.this.channel = copiedChannel;
                    diffResult.dispatchUpdatesTo(MessageListAdapter.this);
                    if (callback != null) {
                        callback.onListUpdated(messageList);
                    }
                } finally {
                    lock.countDown();
                }
            });
            lock.await();
            return true;
        });
    }

    /**
     * Animates the view holder with the corresponding message id.
     *
     * @param animation Animation you want to apply to the view holder
     * @param messageId Message id of the view holder to be applied
     */
    public void startAnimation(@NonNull Animation animation, long messageId) {
        BaseMessage target = null;
        int position = -1;

        final List<BaseMessage> copied = new ArrayList<>(messageList);
        for (int i = 0; i < copied.size(); i++) {
            BaseMessage message = copied.get(i);
            if (message.getMessageId() == messageId) {
                target = message;
                position = i;
                break;
            }
        }

        if (target != null) {
            startAnimation(animation, position);
        }
    }

    /**
     * Animates the view holder with the corresponding position.
     *
     * @param animation Animation you want to apply to the view holder
     * @param position Position of the view holder to be applied
     */
    public void startAnimation(@NonNull Animation animation, int position) {
        Logger.d(">> MessageListAdapter::startAnimation(), position=%s", position);
        notifyItemChanged(position, animation);
    }

    @Override
    public void onViewRecycled(@NonNull MessageViewHolder holder) {
        final View view = holder.itemView;
        if (view.getAnimation() != null) {
            view.getAnimation().cancel();
        }
    }

    /**
     * Register a callback to be invoked when the {@link MessageViewHolder#itemView} is clicked.
     *
     * @param listener The callback that will run
     * @since 2.2.0
     */
    public void setOnListItemClickListener(@Nullable OnIdentifiableItemClickListener<BaseMessage> listener) {
        this.listItemClickListener = listener;
    }

    /**
     * Returns a callback to be invoked when the {@link MessageViewHolder#itemView} is clicked
     *
     * @return {@code OnIdentifiableItemClickListener<BaseMessage>} to be invoked when the {@link MessageViewHolder#itemView} is clicked.
     * @since 3.0.0
     */
    @Nullable
    public OnIdentifiableItemClickListener<BaseMessage> getOnListItemClickListener() {
        return listItemClickListener;
    }

    /**
     * Register a callback to be invoked when the {@link MessageViewHolder#itemView} is long clicked and held.
     *
     * @param listener The callback that will run
     * @since 2.2.0
     */
    public void setOnListItemLongClickListener(@Nullable OnIdentifiableItemLongClickListener<BaseMessage> listener) {
        this.listItemLongClickListener = listener;
    }

    /**
     * Returns a callback to be invoked when the {@link MessageViewHolder#itemView} is long clicked and held.
     *
     * @return {@code OnIdentifiableItemLongClickListener<BaseMessage>} to be invoked when the {@link MessageViewHolder#itemView} is long clicked and held.
     * @since 3.0.0
     */
    @Nullable
    public OnIdentifiableItemLongClickListener<BaseMessage> getOnListItemLongClickListener() {
        return listItemLongClickListener;
    }

    /**
     * Register a callback to be invoked when the emoji reaction is clicked.
     *
     * @param listener The callback that will run
     * @since 1.1.0
     */
    public void setEmojiReactionClickListener(@Nullable OnEmojiReactionClickListener listener) {
        this.emojiReactionClickListener = listener;
    }

    /**
     * Returns a callback to be invoked when the emoji reaction is clicked.
     *
     * @return {@code OnEmojiReactionClickListener} to be invoked when the emoji reaction is clicked.
     * @since 3.0.0
     */
    @Nullable
    public OnEmojiReactionClickListener getEmojiReactionClickListener() {
        return emojiReactionClickListener;
    }

    /**
     * Register a callback to be invoked when the emoji reaction is long clicked and held.
     *
     * @param listener The callback that will run
     * @since 1.1.0
     */
    public void setEmojiReactionLongClickListener(@Nullable OnEmojiReactionLongClickListener listener) {
        this.emojiReactionLongClickListener = listener;
    }

    /**
     * Returns a callback to be invoked when the emoji reaction is long clicked and held.
     *
     * @return {@code OnEmojiReactionLongClickListener} to be invoked when the emoji reaction is long clicked and held.
     * @since 3.0.0
     */
    @Nullable
    public OnEmojiReactionLongClickListener getEmojiReactionLongClickListener() {
        return emojiReactionLongClickListener;
    }

    /**
     * Register a callback to be invoked when the emoji reaction more button is clicked.
     *
     * @param listener The callback that will run
     * @since 1.1.0
     */
    public void setEmojiReactionMoreButtonClickListener(@Nullable OnItemClickListener<BaseMessage> listener) {
        this.emojiReactionMoreButtonClickListener = listener;
    }

    /**
     * Returns a callback to be invoked when the emoji reaction more button is clicked.
     *
     * @return {OnItemClickListener<BaseMessage>} to be invoked when the emoji reaction more button is clicked.
     * @since 3.0.0
     */
    @Nullable
    public OnItemClickListener<BaseMessage> getEmojiReactionMoreButtonClickListener() {
        return emojiReactionMoreButtonClickListener;
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return messageList.size();
    }

    /**
     * Returns the {@link BaseMessage} in the data set held by the adapter.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The {@link BaseMessage} to retrieve the position of in this adapter.
     */
    @NonNull
    @Override
    public BaseMessage getItem(int position) {
        return messageList.get(position);
    }

    /**
     * Returns the {@link List<BaseMessage>} in the data set held by the adapter.
     *
     * @return The {@link List<BaseMessage>} in this adapter.
     */
    @NonNull
    @Override
    public List<BaseMessage> getItems() {
        return Collections.unmodifiableList(messageList);
    }
}
