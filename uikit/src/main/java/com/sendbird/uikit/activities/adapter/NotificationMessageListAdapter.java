package com.sendbird.uikit.activities.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.activities.viewholder.MessageType;
import com.sendbird.uikit.activities.viewholder.MessageViewHolderFactory;
import com.sendbird.uikit.databinding.SbViewMessageNotificationChannelBinding;
import com.sendbird.uikit.interfaces.OnMessageListUpdateHandler;
import com.sendbird.uikit.interfaces.OnMessageTemplateActionHandler;
import com.sendbird.uikit.internal.model.MessageTemplateDiffCallback;
import com.sendbird.uikit.internal.ui.viewholders.NotificationChannelMessageViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * NotificationMessageListAdapter provides a binding from a {@link BaseMessage} type data set to views that are displayed within a RecyclerView.
 */
public class NotificationMessageListAdapter extends RecyclerView.Adapter<NotificationChannelMessageViewHolder> {
    @NonNull
    private List<BaseMessage> messageList = new ArrayList<>();
    @NonNull
    private GroupChannel channel;
    @Nullable
    private OnMessageTemplateActionHandler onMessageTemplateActionHandler;
    private long prevLastSeenAt = 0L;
    private long currentLastSeenAt = 0L;
    private final boolean shouldDisplayUserProfile;

    // the worker must be a single thread.
    @NonNull
    private final ExecutorService differWorker = Executors.newSingleThreadExecutor();

    /**
     * Constructor
     *
     * @param channel The {@link GroupChannel} that contains the data needed for this adapter
     * @since 3.5.0
     */
    public NotificationMessageListAdapter(@NonNull GroupChannel channel) {
        this(channel, true);
    }

    /**
     * Constructor
     *
     * @param channel The {@link GroupChannel} that contains the data needed for this adapter
     * @param shouldDisplayUserProfile <code>true</code> if the user profile is shown, <code>false</code> otherwise
     * @since 3.5.0
     */
    public NotificationMessageListAdapter(@NonNull GroupChannel channel, boolean shouldDisplayUserProfile) {
        this.channel = GroupChannel.clone(channel);
        this.currentLastSeenAt = channel.getMyLastRead();
        this.shouldDisplayUserProfile = shouldDisplayUserProfile;
    }

    /**
     * Called when RecyclerView needs a new {@link NotificationChannelMessageViewHolder} of the given type to represent
     * an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new {@link NotificationChannelMessageViewHolder} that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(NotificationChannelMessageViewHolder, int)
     * @since 3.5.0
     */
    @NonNull
    @Override
    public NotificationChannelMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final TypedValue values = new TypedValue();
        parent.getContext().getTheme().resolveAttribute(R.attr.sb_component_list, values, true);
        final Context contextWrapper = new ContextThemeWrapper(parent.getContext(), values.resourceId);
        LayoutInflater inflater = LayoutInflater.from(contextWrapper);
        return new NotificationChannelMessageViewHolder(SbViewMessageNotificationChannelBinding.inflate(inflater, parent, false), shouldDisplayUserProfile);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link NotificationChannelMessageViewHolder#itemView} to reflect the item at the given
     * position.
     *
     * @param holder   The {@link NotificationChannelMessageViewHolder} which should be updated to represent
     *                 the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     * @since 3.5.0
     */
    @Override
    public void onBindViewHolder(@NonNull NotificationChannelMessageViewHolder holder, final int position) {
        final BaseMessage message = getItem(position);
        holder.setOnMessageTemplateActionHandler(onMessageTemplateActionHandler);
        holder.bind(channel, message, currentLastSeenAt);
    }

    /**
     * Return the view type of the {@link NotificationChannelMessageViewHolder}.
     * Notification channel always returns {@link MessageType#VIEW_TYPE_NOTIFICATION_CHANNEL_MESSAGE}
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at <code>position</code>.
     * @see MessageViewHolderFactory#getViewType(BaseMessage)
     * @since 3.5.0
     */
    @Override
    public int getItemViewType(int position) {
        return MessageType.VIEW_TYPE_NOTIFICATION_CHANNEL_MESSAGE.getValue();
    }

    /**
     * Return ID for the message at <code>position</code>.
     *
     * @param position Adapter position to query
     * @return the stable ID of the item at position
     * @since 3.5.0
     */
    @Override
    public long getItemId(int position) {
        return getItem(position).getMessageId();
    }

    /**
     * Sets the {@link List<BaseMessage>} to be displayed.
     *
     * @param messageList list to be displayed
     * @since 3.5.0
     */
    public void setItems(@NonNull final GroupChannel channel, @NonNull final List<BaseMessage> messageList, @Nullable OnMessageListUpdateHandler callback) {
        final GroupChannel copiedChannel = GroupChannel.clone(channel);
        final List<BaseMessage> copiedMessage = Collections.unmodifiableList(messageList);
        differWorker.submit(() -> {
            final CountDownLatch lock = new CountDownLatch(1);

            final MessageTemplateDiffCallback diffCallback = new MessageTemplateDiffCallback(
                    NotificationMessageListAdapter.this.messageList,
                    messageList,
                    prevLastSeenAt,
                    currentLastSeenAt
            );
            final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

            SendbirdUIKit.runOnUIThread(() -> {
                try {
                    NotificationMessageListAdapter.this.messageList = copiedMessage;
                    NotificationMessageListAdapter.this.channel = copiedChannel;
                    diffResult.dispatchUpdatesTo(NotificationMessageListAdapter.this);
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
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     * @since 3.5.0
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
     * @since 3.5.0
     */
    @NonNull
    public BaseMessage getItem(int position) {
        return messageList.get(position);
    }

    /**
     * Returns the {@link List<BaseMessage>} in the data set held by the adapter.
     *
     * @return The {@link List<BaseMessage>} in this adapter.
     * @since 3.5.0
     */
    @NonNull
    public List<BaseMessage> getItems() {
        return Collections.unmodifiableList(messageList);
    }

    /**
     * Set the current user's last read timestamp in channel.
     *
     * @param lastSeenAt the current user's last read timestamp in channel.
     * @since 3.5.0
     */
    public synchronized void updateLastSeenAt(long lastSeenAt) {
        // set the previous lastSeenAt value due to compare the message changing status.
        this.prevLastSeenAt = this.currentLastSeenAt;
        this.currentLastSeenAt = lastSeenAt;
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
     * Returns a callback to be invoked when the view that has an {@link com.sendbird.uikit.model.Action} data is clicked.
     *
     * @return a callback to be invoked when the view that has an {@link com.sendbird.uikit.model.Action} data is clicked.
     * @since 3.5.0
     */
    @Nullable
    public OnMessageTemplateActionHandler getOnMessageTemplateActionHandler() {
        return onMessageTemplateActionHandler;
    }
}
