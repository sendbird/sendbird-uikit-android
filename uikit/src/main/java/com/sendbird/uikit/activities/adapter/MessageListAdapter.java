package com.sendbird.uikit.activities.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.uikit.activities.viewholder.GroupChannelMessageViewHolder;
import com.sendbird.uikit.activities.viewholder.MessageType;
import com.sendbird.uikit.activities.viewholder.MessageViewHolder;
import com.sendbird.uikit.activities.viewholder.MessageViewHolderFactory;
import com.sendbird.uikit.activities.viewholder.OtherMessageViewHolder;
import com.sendbird.uikit.interfaces.OnEmojiReactionClickListener;
import com.sendbird.uikit.interfaces.OnEmojiReactionLongClickListener;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.model.HighlightMessageInfo;
import com.sendbird.uikit.utils.ReactionUtils;
import com.sendbird.uikit.utils.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

/**
 * Adapters provide a binding from a {@link BaseMessage} set to views that are displayed
 * within a {@link RecyclerView}.
 */
public class MessageListAdapter extends BaseMessageAdapter<BaseMessage, MessageViewHolder> {
    private final List<BaseMessage> messageList = new ArrayList<>();
    private GroupChannel channel;
    private OnItemClickListener<BaseMessage> profileClickListener;
    private OnItemClickListener<BaseMessage> listener;
    private OnItemLongClickListener<BaseMessage> longClickListener;
    private OnEmojiReactionClickListener emojiReactionClickListener;
    private OnEmojiReactionLongClickListener emojiReactionLongClickListener;
    private OnItemClickListener<BaseMessage> emojiReactionMoreButtonClickListener;
    private final boolean useMessageGroupUI;
    private HighlightMessageInfo highlight;

    /**
     * Constructor
     */
    public MessageListAdapter() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param channel The {@link GroupChannel} that contains the data needed for this adapter
     */
    public MessageListAdapter(GroupChannel channel) {
        this(channel, null);
    }

    /**
     * Constructor
     *
     * @param channel The {@link GroupChannel} that contains the data needed for this adapter
     * @param listener The listener performing when the {@link MessageViewHolder} is clicked.
     */
    public MessageListAdapter(GroupChannel channel, OnItemClickListener<BaseMessage> listener) {
        this(channel, listener, null);
    }

    /**
     * Constructor
     *
     * @param channel The {@link GroupChannel} that contains the data needed for this adapter
     * @param listener The listener performing when the {@link MessageViewHolder} is clicked.
     * @param longClickListener The listener performing when the {@link MessageViewHolder} is long clicked.
     */
    public MessageListAdapter(GroupChannel channel, OnItemClickListener<BaseMessage> listener, OnItemLongClickListener<BaseMessage> longClickListener) {
        this (channel, listener, longClickListener, true);
    }

    /**
     * Constructor
     *
     * @param channel The {@link GroupChannel} that contains the data needed for this adapter
     * @param listener The listener performing when the {@link MessageViewHolder} is clicked.
     * @param longClickListener The listener performing when the {@link MessageViewHolder} is long clicked.
     * @param useMessageGroupUI <code>true</code> if the message group UI is used, <code>false</code> otherwise.
     * @since 1.2.1
     */
    public MessageListAdapter(GroupChannel channel, OnItemClickListener<BaseMessage> listener, OnItemLongClickListener<BaseMessage> longClickListener, boolean useMessageGroupUI) {
        this.channel = channel != null ? GroupChannel.clone(channel) : null;
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
        MessageViewHolder viewHolder = MessageViewHolderFactory.createViewHolder(inflater,
                parent,
                MessageType.from(viewType),
                useMessageGroupUI);

        viewHolder.setHighlightInfo(highlight);
        return viewHolder;
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

            if (ReactionUtils.useReaction(channel) && holder instanceof GroupChannelMessageViewHolder) {
                GroupChannelMessageViewHolder groupChannelHolder = (GroupChannelMessageViewHolder) holder;
                groupChannelHolder.setEmojiReaction(current.getReactions(), (view, reactionPosition, reactionKey) -> {
                    int messagePosition = holder.getAdapterPosition();
                    if (messagePosition != NO_POSITION && emojiReactionClickListener != null) {
                        emojiReactionClickListener.onEmojiReactionClick(
                                view,
                                reactionPosition,
                                getItem(messagePosition),
                                reactionKey
                        );
                    }
                }, (view, reactionPosition, reactionKey) -> {
                    int messagePosition = groupChannelHolder.getAdapterPosition();
                    if (messagePosition != NO_POSITION && emojiReactionLongClickListener != null) {
                        emojiReactionLongClickListener.onEmojiReactionLongClick(
                                view,
                                reactionPosition,
                                getItem(messagePosition),
                                reactionKey
                        );
                    }
                }, v -> {
                    int messagePosition = groupChannelHolder.getAdapterPosition();
                    if (messagePosition != NO_POSITION && emojiReactionMoreButtonClickListener != null) {
                        emojiReactionMoreButtonClickListener.onItemClick(
                                v,
                                messagePosition,
                                getItem(messagePosition)
                        );
                    }
                });
            }
        }

        if (holder instanceof OtherMessageViewHolder) {
            View profileView = ((OtherMessageViewHolder) holder).getProfileView();
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
     * Sets the information of the message to highlight.
     *
     * @param highlightInfo The information of the message to highlight.
     * @since 2.1.0
     */
    public void setHighlightInfo(@Nullable HighlightMessageInfo highlightInfo) {
        this.highlight = highlightInfo;
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

    public void setChannel(@NonNull GroupChannel channel) {
        this.channel = GroupChannel.clone(channel);
    }

    /**
     * Sets the {@link List<BaseMessage>} to be displayed.
     *
     * @param messageList list to be displayed
     */
    public void setItems(GroupChannel channel, List<BaseMessage> messageList) {
        final MessageDiffCallback diffCallback = new MessageDiffCallback(this.channel, channel, this.messageList, messageList, useMessageGroupUI);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.messageList.clear();
        this.messageList.addAll(messageList);
        this.channel = GroupChannel.clone(channel);
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
     * Register a callback to be invoked when the emoji reaction is clicked and held.
     *
     * @param listener The callback that will run
     * @since 1.1.0
     */
    public void setEmojiReactionClickListener(@Nullable OnEmojiReactionClickListener listener) {
        this.emojiReactionClickListener = listener;
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
     * Register a callback to be invoked when the emoji reaction more button is clicked and held.
     *
     * @param listener The callback that will run
     * @since 1.1.0
     */
    public void setEmojiReactionMoreButtonClickListener(@Nullable OnItemClickListener<BaseMessage> listener) {
        this.emojiReactionMoreButtonClickListener = listener;
    }

    /**
     * Register a callback to be invoked when the profile view is clicked.
     *
     * @param profileClickListener The callback that will run
     * @since 1.2.2
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