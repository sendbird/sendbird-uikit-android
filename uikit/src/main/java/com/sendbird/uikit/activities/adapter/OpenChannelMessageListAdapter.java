package com.sendbird.uikit.activities.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.SendBird;
import com.sendbird.uikit.activities.viewholder.MessageType;
import com.sendbird.uikit.activities.viewholder.MessageViewHolder;
import com.sendbird.uikit.activities.viewholder.MessageViewHolderFactory;
import com.sendbird.uikit.consts.ClickableViewIdentifier;
import com.sendbird.uikit.interfaces.OnIdentifiableItemLongClickListener;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.interfaces.OnIdentifiableItemClickListener;
import com.sendbird.uikit.interfaces.OnMessageListUpdateHandler;
import com.sendbird.uikit.utils.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

/**
 * Adapters provide a binding from a {@link BaseMessage} set to views that are displayed
 * within a {@link RecyclerView}. This adapter is used to be only {@link OpenChannel}.
 *
 * @since 2.0.0
 */
public class OpenChannelMessageListAdapter extends BaseMessageAdapter<BaseMessage, MessageViewHolder> {
    private List<BaseMessage> messageList = new ArrayList<>();
    private OpenChannel channel;
    private OnItemClickListener<BaseMessage> profileClickListener;
    private OnItemClickListener<BaseMessage> listener;
    private OnItemLongClickListener<BaseMessage> longClickListener;
    private OnIdentifiableItemClickListener<BaseMessage> listItemClickListener;
    private OnIdentifiableItemLongClickListener<BaseMessage> listItemLongClickListener;
    private final boolean useMessageGroupUI;
    private final ExecutorService service = Executors.newSingleThreadExecutor();

    /**
     * Constructor
     */
    public OpenChannelMessageListAdapter() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param channel The {@link OpenChannel} that contains the data needed for this adapter
     */
    public OpenChannelMessageListAdapter(OpenChannel channel) {
        this(channel, true);
    }

    /**
     * Constructor
     *
     * @param channel The {@link OpenChannel} that contains the data needed for this adapter
     * @param listener The listener performing when the {@link MessageViewHolder} is clicked.
     * @deprecated As of 2.2.0, replaced by {@link OpenChannelMessageListAdapter(OpenChannel, boolean)}.
     */
    @Deprecated
    public OpenChannelMessageListAdapter(OpenChannel channel, OnItemClickListener<BaseMessage> listener) {
        this(channel, listener, null);
    }

    /**
     * Constructor
     *
     * @param channel The {@link OpenChannel} that contains the data needed for this adapter
     * @param listener The listener performing when the {@link MessageViewHolder} is clicked.
     * @param longClickListener The listener performing when the {@link MessageViewHolder} is long clicked.
     * @deprecated As of 2.2.0, replaced by {@link OpenChannelMessageListAdapter(OpenChannel, boolean)}.
     */
    @Deprecated
    public OpenChannelMessageListAdapter(OpenChannel channel, OnItemClickListener<BaseMessage> listener, OnItemLongClickListener<BaseMessage> longClickListener) {
        this (channel, listener, longClickListener, true);
    }

    /**
     * Constructor
     *
     * @param channel The {@link OpenChannel} that contains the data needed for this adapter
     * @param listener The listener performing when the {@link MessageViewHolder} is clicked.
     * @param longClickListener The listener performing when the {@link MessageViewHolder} is long clicked.
     * @param useMessageGroupUI <code>true</code> if the message group UI is used, <code>false</code> otherwise.
     * @deprecated As of 2.2.0, replaced by {@link OpenChannelMessageListAdapter(OpenChannel, boolean)}.
     */
    @Deprecated
    public OpenChannelMessageListAdapter(OpenChannel channel, OnItemClickListener<BaseMessage> listener, OnItemLongClickListener<BaseMessage> longClickListener, boolean useMessageGroupUI) {
        this(channel, useMessageGroupUI);
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    /**
     * Constructor
     *
     * @param channel The {@link OpenChannel} that contains the data needed for this adapter
     * @param useMessageGroupUI <code>true</code> if the message group UI is used, <code>false</code> otherwise.
     * @since 2.2.0
     */
    public OpenChannelMessageListAdapter(OpenChannel channel, boolean useMessageGroupUI) {
        this.channel = channel != null ? OpenChannel.clone(channel) : null;
        this.useMessageGroupUI = useMessageGroupUI;
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
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final MessageViewHolder viewHolder = MessageViewHolderFactory.createOpenChannelViewHolder(inflater,
                parent,
                MessageType.from(viewType),
                useMessageGroupUI);

        final Map<String, View> views = viewHolder.getClickableViewMap();
        if (views != null) {
            for (Map.Entry<String, View> entry : views.entrySet()) {
                final String identifier = entry.getKey();
                entry.getValue().setOnClickListener(v -> {
                    int messagePosition = viewHolder.getAdapterPosition();
                    if (messagePosition != NO_POSITION) {
                        if (listItemClickListener != null) {
                            listItemClickListener.onIdentifiableItemClick(v, identifier, messagePosition, getItem(messagePosition));
                        }

                        // for backward compatibilities
                        if (listener != null && identifier.equals(ClickableViewIdentifier.Chat.name())) {
                            listener.onItemClick(v, messagePosition, getItem(messagePosition));
                        }
                        if (profileClickListener != null && identifier.equals(ClickableViewIdentifier.Profile.name())) {
                            profileClickListener.onItemClick(v, messagePosition, getItem(messagePosition));
                        }
                    }
                });

                entry.getValue().setOnLongClickListener(v -> {
                    int messagePosition = viewHolder.getAdapterPosition();
                    if (messagePosition != NO_POSITION) {
                        if (listItemLongClickListener != null) {
                            listItemLongClickListener.onIdentifiableItemLongClick(v, identifier, messagePosition, getItem(messagePosition));
                        }

                        // for backward compatibilities
                        if (longClickListener != null && identifier.equals(ClickableViewIdentifier.Chat.name())) {
                            longClickListener.onItemLongClick(v, messagePosition, getItem(messagePosition));
                        }
                        return true;
                    }
                    return false;
                });
            }
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads != null && !payloads.isEmpty()) {
            final Object lastPayload = payloads.get(payloads.size() - 1);
            if (lastPayload instanceof Animation) {
                Animation animation = (Animation) lastPayload;
                final Map<String, View> clickableViewMap = holder.getClickableViewMap();
                if (clickableViewMap != null && !clickableViewMap.isEmpty()) {
                    final View view = clickableViewMap.get(ClickableViewIdentifier.Chat.name());
                    if (view != null) {
                        view.setAnimation(animation);
                    }
                }
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

        holder.onBindViewHolder(channel, prev, current, next);
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
     * Return hashcode for the item at <code>position</code>.
     *
     * @param position Adapter position to query
     * @return the stable ID of the item at position
     */
    @Override
    public long getItemId(int position) {
        BaseMessage item = getItem(position);
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

    public void setChannel(@NonNull OpenChannel channel) {
        this.channel = OpenChannel.clone(channel);
    }

    /**
     * Sets the {@link List<BaseMessage>} to be displayed.
     *
     * @param messageList list to be displayed
     * @deprecated As of 2.2.0, replaced by {@link OpenChannelMessageListAdapter#setItems(OpenChannel, List, OnMessageListUpdateHandler)}.
     */
    @Deprecated
    public void setItems(OpenChannel channel, List<BaseMessage> messageList) {
        final OpenChannelMessageDiffCallback diffCallback = new OpenChannelMessageDiffCallback(this.channel, channel, this.messageList, messageList, useMessageGroupUI);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.messageList.clear();
        this.messageList.addAll(messageList);
        this.channel = OpenChannel.clone(channel);
        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * Sets the {@link List<BaseMessage>} to be displayed.
     *
     * @param messageList list to be displayed
     * @since 2.2.0
     */
    public void setItems(@NonNull final OpenChannel channel, @NonNull final List<BaseMessage> messageList, @Nullable OnMessageListUpdateHandler callback) {
        final OpenChannel copiedChannel = OpenChannel.clone(channel);
        final List<BaseMessage> copiedMessage = Collections.unmodifiableList(messageList);
        service.submit(() -> {
            final CountDownLatch lock = new CountDownLatch(1);
            final OpenChannelMessageDiffCallback diffCallback = new OpenChannelMessageDiffCallback(OpenChannelMessageListAdapter.this.channel, channel, OpenChannelMessageListAdapter.this.messageList, messageList, useMessageGroupUI);
            final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

            SendBird.runOnUIThread(() -> {
                try {
                    OpenChannelMessageListAdapter.this.messageList = copiedMessage;
                    OpenChannelMessageListAdapter.this.channel = copiedChannel;
                    diffResult.dispatchUpdatesTo(OpenChannelMessageListAdapter.this);
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

    public void startAnimation(@NonNull Animation animation, int position) {
        notifyItemChanged(position, animation);
    }

    @Override
    public void onViewRecycled(@NonNull MessageViewHolder holder) {
        final Map<String, View> clickableViewMap = holder.getClickableViewMap();
        if (clickableViewMap != null && !clickableViewMap.isEmpty()) {
            final View view = clickableViewMap.get(ClickableViewIdentifier.Chat.name());
            if (view != null && view.getAnimation() != null) {
                view.getAnimation().cancel();
            }
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
     * Register a callback to be invoked when the {@link MessageViewHolder#itemView} is long clicked and held.
     *
     * @param listener The callback that will run
     * @since 2.2.0
     */
    public void setOnListItemLongClickListener(@Nullable OnIdentifiableItemLongClickListener<BaseMessage> listener) {
        this.listItemLongClickListener = listener;
    }

    /**
     * Register a callback to be invoked when the {@link MessageViewHolder#itemView} is clicked.
     *
     * @param listener The callback that will run
     * @deprecated As of 2.2.0, replaced by {@link MessageListAdapter#setOnListItemClickListener(OnIdentifiableItemClickListener)}
     */
    @Deprecated
    public void setOnItemClickListener(@Nullable OnItemClickListener<BaseMessage> listener) {
        this.listener = listener;
    }

    /**
     * Register a callback to be invoked when the {@link MessageViewHolder#itemView} is long clicked and held.
     *
     * @param listener The callback that will run
     * @deprecated As of 2.2.0, replaced by {@link MessageListAdapter#setOnListItemLongClickListener(OnIdentifiableItemLongClickListener)}
     */
    @Deprecated
    public void setOnItemLongClickListener(@Nullable OnItemLongClickListener<BaseMessage> listener) {
        this.longClickListener = listener;
    }

    /**
     * Register a callback to be invoked when the profile view is clicked.
     *
     * @param profileClickListener The callback that will run
     * @deprecated As of 2.2.0, replaced by {@link MessageListAdapter#setOnListItemClickListener(OnIdentifiableItemClickListener)}
     */
    @Deprecated
    public void setOnProfileClickListener(OnItemClickListener<BaseMessage> profileClickListener) {
        this.profileClickListener = profileClickListener;
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return messageList == null ? 0 : messageList.size();
    }

    /**
     * Returns the {@link BaseMessage} in the data set held by the adapter.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The {@link BaseMessage} to retrieve the position of in this adapter.
     */
    @Override
    public BaseMessage getItem(int position) {
        return messageList.get(position);
    }

    /**
     * Returns the {@link List<BaseMessage>} in the data set held by the adapter.
     *
     * @return The {@link List<BaseMessage>} in this adapter.
     */
    @Override
    public List<BaseMessage> getItems() {
        return messageList != null ? Collections.unmodifiableList(messageList) : null;
    }
}