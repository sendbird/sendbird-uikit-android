package com.sendbird.uikit.vm;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.FileMessageParams;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.MessageCollection;
import com.sendbird.android.MessageListParams;
import com.sendbird.android.MessagePayloadFilter;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;
import com.sendbird.android.UserMessageParams;
import com.sendbird.android.handlers.CollectionEventSource;
import com.sendbird.android.handlers.GroupChannelContext;
import com.sendbird.android.handlers.MessageCollectionHandler;
import com.sendbird.android.handlers.MessageCollectionInitHandler;
import com.sendbird.android.handlers.MessageCollectionInitPolicy;
import com.sendbird.android.handlers.MessageContext;
import com.sendbird.android.handlers.Traceable;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.consts.MessageLoadState;
import com.sendbird.uikit.consts.ReplyType;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.interfaces.AuthenticateHandler;
import com.sendbird.uikit.interfaces.OnCompleteHandler;
import com.sendbird.uikit.interfaces.OnPagedDataLoader;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.FileInfo;
import com.sendbird.uikit.model.MessageList;
import com.sendbird.uikit.utils.Available;
import com.sendbird.uikit.widgets.StatusFrameView;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ViewModel preparing and managing data related with the list of messages in a channel
 *
 * @since 3.0.0
 */
public class ChannelViewModel extends BaseViewModel implements OnPagedDataLoader<List<BaseMessage>> {
    @NonNull
    private final String ID_CHANNEL_EVENT_HANDLER = "ID_CHANNEL_EVENT_HANDLER" + System.currentTimeMillis();
    @NonNull
    private final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_GROUP_CHAT" + System.currentTimeMillis();
    @NonNull
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    @NonNull
    private final MutableLiveData<ChannelMessageData> messageList = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<List<User>> typingMembers = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<GroupChannel> channelUpdated = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<String> channelDeleted = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<List<BaseMessage>> messagesDeleted = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<MessageLoadState> messageLoadState = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<StatusFrameView.Status> statusFrame = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> hugeGapDetected = new MutableLiveData<>();
    @NonNull
    private final MessageListParams messageListParams;
    @Nullable
    private GroupChannel channel;
    @NonNull
    private final String channelUrl;
    @NonNull
    private final MessageList cachedMessages = new MessageList();
    @Nullable
    private MessageCollection collection;
    @Nullable
    private MessageCollectionHandler handler;
    private boolean needToLoadMessageCache = true;

    /**
     * Class that holds message data in a channel.
     *
     * @since 3.0.0
     */
    public static class ChannelMessageData {
        final List<BaseMessage> messages;
        final String traceName;

        ChannelMessageData(@Nullable String traceName, @NonNull List<BaseMessage> messages) {
            this.traceName = traceName;
            this.messages = messages;
        }

        /**
         * Returns a list of messages for the current channel.
         *
         * @return A list of the latest messages on the current channel
         * @since 3.0.0
         */
        @NonNull
        public List<BaseMessage> getMessages() {
            return messages;
        }

        /**
         * Returns data indicating how the message list was updated.
         *
         * @return The String that traces the path of the message list
         * @since 3.0.0
         */
        @Nullable
        public String getTraceName() {
            return traceName;
        }
    }

    /**
     * Constructor
     *
     * @param channelUrl The URL of a channel this view model is currently associated with
     * @param messageListParams Parameters required to retrieve the message list from this view model
     * @since 3.0.0
     */
    public ChannelViewModel(@NonNull String channelUrl, @Nullable MessageListParams messageListParams) {
        super();
        this.channel = null;
        this.channelUrl = channelUrl;
        this.messageListParams = messageListParams == null ? createMessageListParams() : messageListParams;
        this.messageListParams.setReverse(true);

        SendBird.addChannelHandler(ID_CHANNEL_EVENT_HANDLER, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel channel, BaseMessage message) {
                if (ChannelViewModel.this.channel != null && channel.getUrl().equals(channelUrl) && hasNext()) {
                    markAsRead();
                    notifyDataSetChanged(new MessageContext(CollectionEventSource.EVENT_MESSAGE_RECEIVED, BaseMessage.SendingStatus.SUCCEEDED));
                }
            }
        });

        SendBird.addConnectionHandler(CONNECTION_HANDLER_ID, new SendBird.ConnectionHandler() {
            @Override
            public void onReconnectStarted() {
            }

            @Override
            public void onReconnectSucceeded() {
                loadLatestMessagesForCache();
            }

            @Override
            public void onReconnectFailed() {
            }
        });
    }

    /**
     * Checks if the message with {@code messageId} is in the message list which this view model manages.
     *
     * @param messageId ID of the message you want to check
     * @return {@code true} if the message in in the message list, {@code false} otherwise
     * @since 3.0.0
     */
    public boolean hasMessageById(long messageId) {
        return cachedMessages.getById(messageId) != null;
    }

    /**
     * Retrieves messages created at {@code createdAt} from the message list which this view model manages.
     *
     * @param createdAt The timestamp messages were created
     * @return The list of messages created at {@code createdAt}
     * @since 3.0.0
     */
    @NonNull
    public List<BaseMessage> getMessagesByCreatedAt(long createdAt) {
        return cachedMessages.getByCreatedAt(createdAt);
    }

    // Do not call loadInitial inside this function.
    private synchronized void initMessageCollection(final long startingPoint) {
        Logger.i(">> ChannelViewModel::initMessageCollection()");
        if (this.channel == null) return;
        if (this.collection != null) {
            disposeMessageCollection();
        }
        this.collection = new MessageCollection.Builder(channel, messageListParams)
                .setStartingPoint(startingPoint)
                .build();
        this.collection.setMessageCollectionHandler(new MessageCollectionHandler() {
            @UiThread
            @Override
            public void onMessagesAdded(@NonNull MessageContext context, @NonNull GroupChannel channel, @NonNull List<BaseMessage> messages) {
                Logger.d(">> ChannelViewModel::onMessagesAdded() from=%s", context.getCollectionEventSource());
                if (messages.isEmpty()) return;

                if (context.getMessagesSendingStatus() == BaseMessage.SendingStatus.SUCCEEDED || context.getMessagesSendingStatus() == BaseMessage.SendingStatus.NONE) {
                    cachedMessages.addAll(messages);
                    notifyDataSetChanged(context);
                } else if (context.getMessagesSendingStatus() == BaseMessage.SendingStatus.PENDING) {
                    notifyDataSetChanged(StringSet.ACTION_PENDING_MESSAGE_ADDED);
                }

                switch (context.getCollectionEventSource()) {
                    case EVENT_MESSAGE_RECEIVED:
                    case EVENT_MESSAGE_SENT:
                    case MESSAGE_FILL:
                        markAsRead();
                        break;
                }
                if (ChannelViewModel.this.handler != null) {
                    ChannelViewModel.this.handler.onMessagesAdded(context, channel, messages);
                }
            }

            @UiThread
            @Override
            public void onMessagesUpdated(@NonNull MessageContext context, @NonNull GroupChannel channel, @NonNull List<BaseMessage> messages) {
                Logger.d(">> ChannelViewModel::onMessagesUpdated() from=%s", context.getCollectionEventSource());
                if (messages.isEmpty()) return;

                if (context.getMessagesSendingStatus() == BaseMessage.SendingStatus.SUCCEEDED) {
                    // if the source was MESSAGE_SENT, we should remove the message from the pending message datasource.
                    if (context.getCollectionEventSource() == CollectionEventSource.EVENT_MESSAGE_SENT) {
                        PendingMessageRepository.getInstance().clearAllFileInfo(messages);
                        cachedMessages.addAll(messages);
                    } else {
                        cachedMessages.updateAll(messages);
                    }
                    notifyDataSetChanged(context);
                } else if (context.getMessagesSendingStatus() == BaseMessage.SendingStatus.PENDING) {
                    notifyDataSetChanged(StringSet.ACTION_PENDING_MESSAGE_ADDED);
                } else if (context.getMessagesSendingStatus() == BaseMessage.SendingStatus.FAILED) {
                    notifyDataSetChanged(StringSet.ACTION_FAILED_MESSAGE_ADDED);
                } else if (context.getMessagesSendingStatus() == BaseMessage.SendingStatus.CANCELED) {
                    notifyDataSetChanged(StringSet.ACTION_FAILED_MESSAGE_ADDED);
                }

                if (ChannelViewModel.this.handler != null) {
                    ChannelViewModel.this.handler.onMessagesUpdated(context, channel, messages);
                }
            }

            @UiThread
            @Override
            public void onMessagesDeleted(@NonNull MessageContext context, @NonNull GroupChannel channel, @NonNull List<BaseMessage> messages) {
                Logger.d(">> ChannelViewModel::onMessagesDeleted() from=%s", context.getCollectionEventSource());
                if (messages.isEmpty()) return;


                if (context.getMessagesSendingStatus() == BaseMessage.SendingStatus.SUCCEEDED) {
                    // Remove the succeeded message from the succeeded message datasource.
                    cachedMessages.deleteAll(messages);
                    notifyDataSetChanged(context);
                } else if (context.getMessagesSendingStatus() == BaseMessage.SendingStatus.PENDING) {
                    // Remove the pending message from the pending message datasource.
                    notifyDataSetChanged(StringSet.ACTION_PENDING_MESSAGE_REMOVED);
                } else if (context.getMessagesSendingStatus() == BaseMessage.SendingStatus.FAILED) {
                    // Remove the failed message from the pending message datasource.
                    notifyDataSetChanged(StringSet.ACTION_FAILED_MESSAGE_REMOVED);
                }

                notifyMessagesDeleted(messages);
                if (ChannelViewModel.this.handler != null) {
                    ChannelViewModel.this.handler.onMessagesDeleted(context, channel, messages);
                }
            }

            @UiThread
            @Override
            public void onChannelDeleted(@NonNull GroupChannelContext context, @NonNull String channelUrl) {
                Logger.d(">> ChannelViewModel::onChannelDeleted() from=%s", context.getCollectionEventSource());
                notifyChannelDeleted(channelUrl);
                if (ChannelViewModel.this.handler != null) {
                    ChannelViewModel.this.handler.onChannelDeleted(context, channelUrl);
                }
            }

            @UiThread
            @Override
            public void onHugeGapDetected() {
                Logger.d(">> ChannelViewModel::onHugeGapDetected()");
                notifyHugeGapDetected();
                if (ChannelViewModel.this.handler != null) {
                    ChannelViewModel.this.handler.onHugeGapDetected();
                }
            }

            @Override
            public void onChannelUpdated(@NonNull GroupChannelContext context, @NonNull GroupChannel channel) {
                Logger.d(">> ChannelViewModel::onChannelUpdated() from=%s, url=%s", context.getCollectionEventSource(), channel.getUrl());

                switch (context.getCollectionEventSource()) {
                    case EVENT_TYPING_STATUS_UPDATED:
                        final List<User> typingUsers = channel.getTypingUsers();
                        if (typingUsers.size() > 0) {
                            typingMembers.setValue(typingUsers);
                        } else {
                            typingMembers.setValue(null);
                        }
                        break;
                    case EVENT_DELIVERY_RECEIPT_UPDATED:
                    case EVENT_READ_RECEIPT_UPDATED:
                        notifyDataSetChanged(context);
                        break;
                    default:
                        break;
                }

                notifyChannelDataChanged();
                if (ChannelViewModel.this.handler != null) {
                    ChannelViewModel.this.handler.onChannelUpdated(context, channel);
                }
            }
        });
        Logger.i(">> ChannelViewModel::initMessageCollection() collection=%s", collection);
        loadLatestMessagesForCache();
    }

    // If the collection starts with a starting point value, not MAX_VALUE,
    // the message should be requested the newest messages at once because there may be no new messages in the cache
    private void loadLatestMessagesForCache() {
        if (!needToLoadMessageCache || (this.collection != null && this.collection.getStartingPoint() == Long.MAX_VALUE)) return;
        if (channel == null) return;
        final MessageCollection syncCollection = new MessageCollection.Builder(channel, new MessageListParams()).build();
        syncCollection.loadPrevious((messages, e) -> {
            if (e == null) {
                needToLoadMessageCache = false;
            }
            syncCollection.dispose();
        });
    }

    private synchronized void disposeMessageCollection() {
        Logger.i(">> ChannelViewModel::disposeMessageCollection()");
        if (this.collection != null) {
            this.collection.setMessageCollectionHandler(null);
            this.collection.dispose();
        }
    }

    /**
     * Registers a handler for the message collection managed by this view model.
     *
     * @param handler {@link MessageCollectionHandler} to be registered in this view model
     * @since 3.0.0
     */
    public void setMessageCollectionHandler(@Nullable MessageCollectionHandler handler) {
        this.handler = handler;
    }

    /**
     * Returns LiveData that can be observed if the channel has been updated.
     *
     * @return LiveData holding the updated {@code GroupChannel}
     * @since 3.0.0
     */
    @NonNull
    public LiveData<GroupChannel> onChannelUpdated() {
        return channelUpdated;
    }

    /**
     * Returns LiveData that can be observed if huge gaps are detected within the collection this view model managed.
     *
     * @return LiveData holding whether huge gaps are detected
     * @since 3.0.0
     */
    @NonNull
    public LiveData<Boolean> getHugeGapDetected() {
        return hugeGapDetected;
    }

    /**
     * Returns LiveData that can be observed if the channel has been deleted.
     *
     * @return LiveData holding the URL of the deleted {@code GroupChannel}
     * @since 3.0.0
     */
    @NonNull
    public LiveData<String> onChannelDeleted() {
        return channelDeleted;
    }

    /**
     * Returns LiveData that can be observed if the messages has been deleted in the collection this view model managed.
     *
     * @return LiveData holding the list of deleted messages
     * @since 3.0.0
     */
    @NonNull
    public LiveData<List<BaseMessage>> onMessagesDeleted() {
        return messagesDeleted;
    }

    /**
     * Returns {@code GroupChannel}. If the authentication failed, {@code null} is returned.
     *
     * @return {@code GroupChannel} this view model is currently associated with
     * @since 3.0.0
     */
    @Nullable
    public GroupChannel getChannel() {
        return channel;
    }

    /**
     * Returns URL of GroupChannel.
     *
     * @return The URL of a channel this view model is currently associated with
     * @since 3.0.0
     */
    @NonNull
    public String getChannelUrl() {
        return channelUrl;
    }

    /**
     * Returns parameters required to retrieve the message list from this view model
     *
     * @return {@link MessageListParams} used in this view model
     * @since 3.0.0
     */
    @NonNull
    public MessageListParams getMessageListParams() {
        return messageListParams;
    }

    /**
     * Returns LiveData that can be observed for the list of messages.
     *
     * @return LiveData holding the latest {@link ChannelMessageData}
     * @since 3.0.0
     */
    @NonNull
    public LiveData<ChannelMessageData> getMessageList() {
        return messageList;
    }

    /**
     * Returns LiveData that can be observed for members who are typing in the channel associated with this view model.
     *
     * @return LiveData holding members who are typing
     * @since 3.0.0
     */
    @NonNull
    public LiveData<List<User>> getTypingMembers() {
        return typingMembers;
    }

    /**
     * Returns LiveData that can be observed for the state of loading messages.
     *
     * @return LiveData holding {@link MessageLoadState} for this view model
     * @since 3.0.0
     */
    @NonNull
    public LiveData<MessageLoadState> getMessageLoadState() {
        return messageLoadState;
    }

    /**
     * Returns LiveData that can be observed for the status of the result of fetching the message list.
     * When the message list is fetched successfully, the status is {@link StatusFrameView.Status#NONE}.
     *
     * @return The Status for the message list
     * @since 3.0.0
     */
    @NonNull
    public LiveData<StatusFrameView.Status> getStatusFrame() {
        return statusFrame;
    }

    @Override
    public boolean hasNext() {
        return collection == null || collection.hasNext();
    }

    @Override
    public boolean hasPrevious() {
        return collection == null || collection.hasPrevious();
    }

    /**
     * Returns the timestamp that is the starting point when the message list is fetched initially.
     *
     * @return The timestamp as the starting point
     * @since 3.0.0
     */
    public long getStartingPoint() {
        return collection != null ? collection.getStartingPoint() : Long.MAX_VALUE;
    }

    @UiThread
    private synchronized void notifyChannelDataChanged() {
        Logger.d(">> ChannelViewModel::notifyChannelDataChanged()");
        channelUpdated.setValue(channel);
    }

    @UiThread
    private synchronized void notifyDataSetChanged(@NonNull String traceName) {
        Logger.d(">> ChannelViewModel::notifyDataSetChanged(), size = %s, action=%s, hasNext=%s", cachedMessages.size(), traceName, hasNext());
        if (collection == null) return;
        final List<BaseMessage> copiedList = cachedMessages.toList();
        if (!hasNext()) {
            copiedList.addAll(0, collection.getPendingMessages());
            copiedList.addAll(0, collection.getFailedMessages());
        }
        if (copiedList.size() == 0) {
            statusFrame.setValue(StatusFrameView.Status.EMPTY);
        } else {
            statusFrame.setValue(StatusFrameView.Status.NONE);
            messageList.setValue(new ChannelMessageData(traceName, copiedList));
        }
    }

    @UiThread
    private synchronized void notifyDataSetChanged(@NonNull Traceable trace) {
        notifyDataSetChanged(trace.getTraceName());
    }

    @UiThread
    private synchronized void notifyChannelDeleted(@NonNull String channelUrl) {
        channelDeleted.setValue(channelUrl);
    }

    @UiThread
    private synchronized void notifyMessagesDeleted(@NonNull List<BaseMessage> deletedMessages) {
        messagesDeleted.setValue(deletedMessages);
    }

    @UiThread
    private synchronized void notifyHugeGapDetected() {
        hugeGapDetected.setValue(true);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Logger.dev("-- onCleared ChannelViewModel");
        SendBird.removeChannelHandler(ID_CHANNEL_EVENT_HANDLER);
        SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID);
        disposeMessageCollection();
        SendBird.removeChannelHandler(ID_CHANNEL_EVENT_HANDLER);
        SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID);
        worker.shutdownNow();
    }

    private void markAsRead() {
        Logger.dev("markAsRead");
        if (channel != null) channel.markAsRead(null);
    }

    /**
     * Sets whether the current user is typing.
     *
     * @param isTyping {@code true} if the current user is typing, {@code false} otherwise
     */
    public void setTyping(boolean isTyping) {
        if (channel != null) {
            if (isTyping) {
                channel.startTyping();
            } else {
                channel.endTyping();
            }
        }
    }

    /**
     * Sends a text message to the channel.
     *
     * @param params Parameters to be applied to the message
     * @since 3.0.0
     */
    public void sendUserMessage(@NonNull UserMessageParams params) {
        Logger.i("++ request send message : %s", params);
        if (channel != null) {
            channel.sendUserMessage(params, (message, e) -> {
                if (e != null) {
                    Logger.e(e);
                    return;
                }
                Logger.i("++ sent message : %s", message);
            });
        }
    }

    /**
     * Sends a file message to the channel.
     *
     * @param params Parameters to be applied to the message
     * @param fileInfo File information to send to the channel
     * @since 3.0.0
     */
    public void sendFileMessage(@NonNull FileMessageParams params, @NonNull FileInfo fileInfo) {
        Logger.i("++ request send file message : %s", params);
        if (channel != null) {
            FileMessage pendingFileMessage = channel.sendFileMessage(params, (message, ee) -> {
                if (ee != null) {
                    Logger.e(ee);
                    return;
                }
                Logger.i("++ sent message : %s", message);
            });
            if (pendingFileMessage != null) {
                PendingMessageRepository.getInstance().addFileInfo(pendingFileMessage, fileInfo);
            }
        }

    }

    /**
     * Resends a message to the channel.
     *
     * @param message Message to resend
     * @param handler Callback handler called when this method is completed
     * @since 3.0.0
     */
    public void resendMessage(@NonNull BaseMessage message, @Nullable OnCompleteHandler handler) {
        if (channel == null) return;
        if (message instanceof UserMessage) {
            channel.resendMessage((UserMessage) message, (message12, e) -> {
                if (handler != null) handler.onComplete(e);
                Logger.i("__ resent message : %s", message12);
            });
        } else if (message instanceof FileMessage) {
            FileInfo info = PendingMessageRepository.getInstance().getFileInfo(message);
            final File file = info == null ? null : info.getFile();
            channel.resendMessage((FileMessage) message, file, (message1, e1) -> {
                if (handler != null) handler.onComplete(e1);
                Logger.i("__ resent file message : %s", message1);
            });
        }
    }

    /**
     * Updates a text message with {@code messageId}.
     *
     * @param messageId ID of message to be updated
     * @param params Parameters to be applied to the message
     * @param handler Callback handler called when this method is completed
     * @since 3.0.0
     */
    public void updateUserMessage(long messageId, @NonNull UserMessageParams params, @Nullable OnCompleteHandler handler) {
        if (channel == null) return;
        channel.updateUserMessage(messageId, params, (message, e) -> {
            if (handler != null) handler.onComplete(e);
            Logger.i("++ updated message : %s", message);
        });
    }

    /**
     * Deletes a message.
     *
     * @param message Message to be deleted
     * @param handler Callback handler called when this method is completed
     * @since 3.0.0
     */
    public void deleteMessage(@NonNull BaseMessage message, @Nullable OnCompleteHandler handler) {
        if (channel == null) return;
        final BaseMessage.SendingStatus status = message.getSendingStatus();
        if (status == BaseMessage.SendingStatus.SUCCEEDED) {
            channel.deleteMessage(message, e -> {
                if (handler != null) handler.onComplete(e);
                Logger.i("++ deleted message : %s", message);
            });
        } else if (status == BaseMessage.SendingStatus.FAILED) {
            if (collection != null) {
                collection.removeFailedMessages(Collections.singletonList(message), (requestIds, e) -> {
                    if (handler != null) handler.onComplete(e);
                    Logger.i("++ deleted message : %s", message);
                    notifyDataSetChanged(StringSet.ACTION_FAILED_MESSAGE_REMOVED);
                    if (message instanceof FileMessage) {
                        PendingMessageRepository.getInstance().clearFileInfo((FileMessage) message);
                    }
                });
            }
        }
    }

    /**
     * Adds the reaction with {@code key} if the current user doesn't add it, otherwise the reaction will be deleted
     *
     * @param view View displaying the reaction with {@code key}
     * @param message Message to which the reaction will be applieds
     * @param key Key of reaction
     * @param handler Callback handler called when this method is completed
     * @since 3.0.0
     */
    public void toggleReaction(@NonNull View view, @NonNull BaseMessage message, @NonNull String key, @Nullable OnCompleteHandler handler) {
        if (channel == null) return;
        if (!view.isSelected()) {
            Logger.i("__ add reaction : %s", key);
            channel.addReaction(message, key, (reactionEvent, e) -> {
                if (handler != null) {
                    Logger.e(e);
                    handler.onComplete(e);
                }
            });
        } else {
            Logger.i("__ delete reaction : %s", key);
            channel.deleteReaction(message, key, (reactionEvent, e) -> {
                if (handler != null) {
                    Logger.e(e);
                    handler.onComplete(e);
                }
            });
        }
    }

    /**
     * Requests the list of <code>BaseMessage</code>s for the first time.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getMessageList()}.
     *
     * @param startingPoint Timestamp that is the starting point when the message list is fetched
     * @since 3.0.0
     */
    @UiThread
    public synchronized boolean loadInitial(final long startingPoint) {
        Logger.d(">> ChannelViewModel::loadInitial() startingPoint=%s", startingPoint);
        initMessageCollection(startingPoint);
        if (collection == null) {
            Logger.d("-- channel instance is null. an authenticate process must be proceed first");
            return false;
        }

        messageLoadState.postValue(MessageLoadState.LOAD_STARTED);
        cachedMessages.clear();
        collection.initialize(MessageCollectionInitPolicy.CACHE_AND_REPLACE_BY_API, new MessageCollectionInitHandler() {
            @Override
            public void onCacheResult(@Nullable List<BaseMessage> cachedList, @Nullable SendBirdException e) {
                if (e == null && cachedList != null && cachedList.size() > 0) {
                    cachedMessages.addAll(cachedList);
                    notifyDataSetChanged(StringSet.ACTION_INIT_FROM_CACHE);
                }
            }

            @Override
            public void onApiResult(@Nullable List<BaseMessage> apiResultList, @Nullable SendBirdException e) {
                if (e == null && apiResultList != null) {
                    cachedMessages.clear();
                    cachedMessages.addAll(apiResultList);
                    notifyDataSetChanged(StringSet.ACTION_INIT_FROM_REMOTE);
                    if (apiResultList.size() > 0) {
                        markAsRead();
                    }
                }
                messageLoadState.postValue(MessageLoadState.LOAD_ENDED);
            }
        });
        return true;
    }

    /**
     * Requests the list of <code>BaseMessage</code>s when the page goes to the previous.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getMessageList()}.
     *
     * @return Returns the list of <code>BaseMessage</code>s if no error occurs
     * @throws Exception Throws exception if getting the message list are failed
     * @since 3.0.0
     */
    @WorkerThread
    @NonNull
    @Override
    public List<BaseMessage> loadPrevious() throws Exception {
        if (!hasPrevious() || collection == null) return Collections.emptyList();
        Logger.i(">> ChannelViewModel::loadPrevious()");

        final AtomicReference<List<BaseMessage>> result = new AtomicReference<>();
        final AtomicReference<Exception> error = new AtomicReference<>();
        final CountDownLatch lock = new CountDownLatch(1);

        messageLoadState.postValue(MessageLoadState.LOAD_STARTED);
        collection.loadPrevious((messages, e) -> {
            Logger.d("++ privious size = %s", messages == null ? 0 : messages.size());
            try {
                if (e == null) {
                    if (messages != null) {
                        cachedMessages.addAll(messages);
                    }
                    result.set(messages);
                    notifyDataSetChanged(StringSet.ACTION_PREVIOUS);
                }
                error.set(e);
            } finally {
                lock.countDown();
            }
        });
        lock.await();

        messageLoadState.postValue(MessageLoadState.LOAD_ENDED);
        if (error.get() != null) throw error.get();
        return result.get();
    }

    /**
     * Requests the list of <code>BaseMessage</code>s when the page goes to the next.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getMessageList()}.
     *
     * @return Returns the list of <code>BaseMessage</code>s if no error occurs
     * @throws Exception Throws exception if getting the message list are failed
     * @since 3.0.0
     */
    @WorkerThread
    @NonNull
    @Override
    public List<BaseMessage> loadNext() throws Exception {
        if (!hasNext() || collection == null) return Collections.emptyList();

        Logger.i(">> ChannelViewModel::loadNext()");
        final AtomicReference<List<BaseMessage>> result = new AtomicReference<>();
        final AtomicReference<Exception> error = new AtomicReference<>();
        final CountDownLatch lock = new CountDownLatch(1);

        messageLoadState.postValue(MessageLoadState.LOAD_STARTED);
        collection.loadNext((messages, e) -> {
            try {
                if (e == null) {
                    cachedMessages.addAll(messages);
                    result.set(messages);
                    notifyDataSetChanged(StringSet.ACTION_NEXT);
                }
                error.set(e);
            } finally {
                lock.countDown();
            }
        });
        lock.await();

        messageLoadState.postValue(MessageLoadState.LOAD_ENDED);
        if (error.get() != null) throw error.get();
        return result.get();
    }

    /**
     * Tries to connect Sendbird Server and retrieve a channel instance.
     *
     * @param handler Callback notifying the result of authentication
     * @since 3.0.0
     */
    @Override
    public void authenticate(@NonNull AuthenticateHandler handler) {
        connect((user, e) -> {
            if (user != null) {
                GroupChannel.getChannel(channelUrl, (channel, e1) -> {
                    ChannelViewModel.this.channel = channel;
                    if (e1 != null) {
                        handler.onAuthenticationFailed();
                    } else {
                        handler.onAuthenticated();
                    }
                });
            } else {
                handler.onAuthenticationFailed();
            }
        });
    }

    /**
     * Creates params for the message list when loading the message list.
     *
     * @return {@link MessageListParams} to be used when loading the message list
     * @since 3.0.0
     */
    @NonNull
    public MessageListParams createMessageListParams() {
        final MessageListParams messageListParams = new MessageListParams();
        messageListParams.setReverse(true);
        if (SendbirdUIKit.getReplyType() == ReplyType.QUOTE_REPLY) {
            messageListParams.setReplyTypeFilter(com.sendbird.android.ReplyTypeFilter.ONLY_REPLY_TO_CHANNEL);
            messageListParams.setMessagePayloadFilter(new MessagePayloadFilter.Builder()
                    .setIncludeParentMessageInfo(true)
                    .setIncludeThreadInfo(true)
                    .setIncludeReactions(Available.isSupportReaction())
                    .build());
        } else {
            messageListParams.setReplyTypeFilter(com.sendbird.android.ReplyTypeFilter.NONE);
            messageListParams.setMessagePayloadFilter(new MessagePayloadFilter.Builder()
                    .setIncludeThreadInfo(true)
                    .setIncludeReactions(Available.isSupportReaction())
                    .build());
        }
        return messageListParams;
    }
}
