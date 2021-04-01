package com.sendbird.uikit.activities.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.OpenChannel;
import com.sendbird.uikit.activities.viewholder.MessageType;
import com.sendbird.uikit.activities.viewholder.MessageViewHolder;
import com.sendbird.uikit.activities.viewholder.MessageViewHolderFactory;
import com.sendbird.uikit.activities.viewholder.OpenChannelMessageViewHolder;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.utils.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

/**
 * Adapters provide a binding from a {@link BaseMessage} set to views that are displayed
 * within a {@link RecyclerView}. This adapter is used to be only {@link OpenChannel}.
 *
 * @since 2.0.0
 */
public class OpenChannelMessageListAdapter extends BaseMessageAdapter<BaseMessage, MessageViewHolder> {
    private final List<BaseMessage> messageList = new ArrayList<>();
    private OpenChannel channel;
    private OnItemClickListener<BaseMessage> profileClickListener;
    private OnItemClickListener<BaseMessage> listener;
    private OnItemLongClickListener<BaseMessage> longClickListener;
    private final boolean useMessageGroupUI;

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
        this(channel, null);
    }

    /**
     * Constructor
     *
     * @param channel The {@link OpenChannel} that contains the data needed for this adapter
     * @param listener The listener performing when the {@link MessageViewHolder} is clicked.
     */
    public OpenChannelMessageListAdapter(OpenChannel channel, OnItemClickListener<BaseMessage> listener) {
        this(channel, listener, null);
    }

    /**
     * Constructor
     *
     * @param channel The {@link OpenChannel} that contains the data needed for this adapter
     * @param listener The listener performing when the {@link MessageViewHolder} is clicked.
     * @param longClickListener The listener performing when the {@link MessageViewHolder} is long clicked.
     */
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
     */
    public OpenChannelMessageListAdapter(OpenChannel channel, OnItemClickListener<BaseMessage> listener, OnItemLongClickListener<BaseMessage> longClickListener, boolean useMessageGroupUI) {
        this.channel = channel != null ? OpenChannel.clone(channel) : null;
        this.listener = listener;
        this.longClickListener = longClickListener;
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
        return MessageViewHolderFactory.createOpenChannelViewHolder(inflater,
                parent,
                MessageType.from(viewType),
                useMessageGroupUI);
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

        if (holder.getClickableView() != null) {
            holder.getClickableView().setOnClickListener(v -> {
                int messagePosition = holder.getAdapterPosition();
                if (messagePosition != NO_POSITION && listener != null) {
                    listener.onItemClick(v, messagePosition, getItem(messagePosition));
                }
            });
            holder.getClickableView().setOnLongClickListener(v -> {
                int messagePosition = holder.getAdapterPosition();
                if (messagePosition != NO_POSITION && longClickListener != null) {
                    longClickListener.onItemLongClick(v, messagePosition, getItem(messagePosition));
                    return true;
                }
                return false;
            });
        }

        if (holder instanceof OpenChannelMessageViewHolder) {
            View profileView = ((OpenChannelMessageViewHolder) holder).getProfileView();
            if (profileView != null) {
                profileView.setOnClickListener(v -> {
                    int messagePosition = holder.getAdapterPosition();
                    if (messagePosition != NO_POSITION && profileClickListener != null) {
                        profileClickListener.onItemClick(v, messagePosition, getItem(messagePosition));
                    }
                });
            }
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
     */
    public void setItems(OpenChannel channel, List<BaseMessage> messageList) {
        final OpenChannelMessageDiffCallback diffCallback = new OpenChannelMessageDiffCallback(this.channel, channel, this.messageList, messageList, useMessageGroupUI);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.messageList.clear();
        this.messageList.addAll(messageList);
        this.channel = OpenChannel.clone(channel);
        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * Register a callback to be invoked when the {@link MessageViewHolder#itemView} is clicked.
     *
     * @param listener The callback that will run
     */
    public void setOnItemClickListener(@Nullable OnItemClickListener<BaseMessage> listener) {
        this.listener = listener;
    }

    /**
     * Register a callback to be invoked when the {@link MessageViewHolder#itemView} is long clicked and held.
     *
     * @param listener The callback that will run
     */
    public void setOnItemLongClickListener(@Nullable OnItemLongClickListener<BaseMessage> listener) {
        this.longClickListener = listener;
    }

    /**
     * Register a callback to be invoked when the profile view is clicked.
     *
     * @param profileClickListener The callback that will run
     */
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