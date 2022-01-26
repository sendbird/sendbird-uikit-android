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
import com.sendbird.uikit.R;
import com.sendbird.uikit.consts.MessageLoadState;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.FileInfo;
import com.sendbird.uikit.model.MessageList;
import com.sendbird.uikit.widgets.PagerRecyclerView;
import com.sendbird.uikit.widgets.StatusFrameView;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class ChannelViewModel extends BaseViewModel implements PagerRecyclerView.Pageable<List<BaseMessage>>, MessageCollectionHandler {
    private final String ID_CHANNEL_EVENT_HANDLER = "ID_CHANNEL_EVENT_HANDLER" + System.currentTimeMillis();
    private final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_GROUP_CHAT" + System.currentTimeMillis();
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final MutableLiveData<ChannelMessageData> messageList = new MutableLiveData<>();
    private final MutableLiveData<List<User>> typingMembers = new MutableLiveData<>();
    private final MutableLiveData<GroupChannel> isChannelChanged = new MutableLiveData<>();
    private final MutableLiveData<MessageLoadState> messageLoadState = new MutableLiveData<>();
    private final MutableLiveData<StatusFrameView.Status> statusFrame = new MutableLiveData<>();
    private final GroupChannel channel;
    private final MessageList cachedMessages = new MessageList();

    @Nullable
    private MessageCollection collection;
    private MessageCollectionHandler handler;
    private boolean needToLoadMessageCache = true;

    public static class ChannelMessageData {
        final List<BaseMessage> messages;
        final String traceName;
        ChannelMessageData(@Nullable String traceName, @NonNull List<BaseMessage> messages) {
            this.traceName = traceName;
            this.messages = messages;
        }

        public List<BaseMessage> getMessages() {
            return messages;
        }

        public String getTraceName() {
            return traceName;
        }
    }

    ChannelViewModel(@NonNull GroupChannel groupChannel) {
        super();
        this.channel = groupChannel;

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

        SendBird.addChannelHandler(ID_CHANNEL_EVENT_HANDLER, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel channel, BaseMessage message) {
                if (channel.getUrl().equals(ChannelViewModel.this.channel.getUrl()) && hasNext()) {
                    markAsRead();
                    notifyDataSetChanged(new MessageContext(CollectionEventSource.EVENT_MESSAGE_RECEIVED, BaseMessage.SendingStatus.SUCCEEDED));
                }
            }
        });
    }

    public boolean hasMessageById(long messageId) {
        return cachedMessages.getById(messageId) != null;
    }

    public List<BaseMessage> getMessagesByCreatedAt(long createdAt) {
        return cachedMessages.getByCreatedAt(createdAt);
    }

    private synchronized void initMessageCollection(final long startingPoint, @NonNull final MessageListParams params) {
        Logger.i(">> ChannelViewModel::initMessageCollection()");
        if (this.collection != null) {
            disposeMessageCollection();
        }
        this.collection = new MessageCollection.Builder(channel, params)
                .setStartingPoint(startingPoint)
                .build();
        this.collection.setMessageCollectionHandler(this);
        loadLatestMessagesForCache();
    }

    // If the collection starts with a starting point value, not MAX_VALUE,
    // the message should be requested the newest messages at once because there may be no new messages in the cache
    private void loadLatestMessagesForCache() {
        if (!needToLoadMessageCache || (this.collection != null && this.collection.getStartingPoint() == Long.MAX_VALUE)) return;
        final MessageCollection syncCollection = new MessageCollection.Builder(channel, new MessageListParams()).build();
        syncCollection.loadPrevious((messages, e) -> {
            if (e == null) {
                needToLoadMessageCache = false;
                syncCollection.dispose();
            }
        });
    }

    private synchronized void disposeMessageCollection() {
        Logger.i(">> ChannelViewModel::disposeMessageCollection()");
        if (this.collection != null) {
            this.collection.setMessageCollectionHandler(null);
            this.collection.dispose();
        }
    }

    public void setMessageCollectionHandler(@Nullable MessageCollectionHandler handler) {
        this.handler = handler;
    }

    @Nullable
    public BaseMessage getOldestMessage() {
        return cachedMessages.getOldestMessage();
    }

    @Nullable
    public BaseMessage getLatestMessage() {
        return cachedMessages.getLatestMessage();
    }

    public LiveData<GroupChannel> isChannelChanged() {
        return isChannelChanged;
    }

    public GroupChannel getChannel() {
        return channel;
    }

    public LiveData<ChannelMessageData> getMessageList() {
        return messageList;
    }

    public LiveData<List<User>> getTypingMembers() {
        return typingMembers;
    }

    public LiveData<MessageLoadState> getMessageLoadState() {
        return messageLoadState;
    }

    public LiveData<StatusFrameView.Status> getStatusFrame() {
        return statusFrame;
    }

    @Override
    public boolean hasNext() {
        if (collection == null) return false;
        return collection.hasNext();
    }

    @Override
    public boolean hasPrevious() {
        if (collection == null) return false;
        return collection.hasPrevious();
    }

    public long getStartingPoint() {
        if (collection == null) return Long.MAX_VALUE;
        return collection.getStartingPoint();
    }

    @UiThread
    private synchronized void notifyChannelDataChanged() {
        Logger.d(">> ChannelViewModel::notifyChannelDataChanged()");
        isChannelChanged.setValue(channel);
    }

    @UiThread
    private synchronized void notifyDataSetChanged(@NonNull String traceName) {
        Logger.d(">> ChannelViewModel::notifyDataSetChanged(), size = %s, action=%s", cachedMessages.size(), traceName);
        final List<BaseMessage> copiedList = cachedMessages.toList();
        if (!hasNext() && collection != null) {
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
    @Override
    public void onMessagesAdded(@NonNull MessageContext context, @NonNull GroupChannel channel, @NonNull List<BaseMessage> messages) {
        Logger.d(">> ChannelViewModel::onMessagesAdded() from=%s", context.getCollectionEventSource());
        if (messages.isEmpty()) return;

        if (context.getMessagesSendingStatus() == BaseMessage.SendingStatus.SUCCEEDED) {
            cachedMessages.addAll(messages);
            notifyDataSetChanged(context);
        }  else if (context.getMessagesSendingStatus() == BaseMessage.SendingStatus.PENDING) {
            notifyDataSetChanged(StringSet.ACTION_PENDING_MESSAGE_ADDED);
        }

        switch (context.getCollectionEventSource()) {
            case EVENT_MESSAGE_RECEIVED:
            case EVENT_MESSAGE_SENT:
            case MESSAGE_FILL:
                markAsRead();
                break;
        }
        if (this.handler != null) {
            this.handler.onMessagesAdded(context, channel, messages);
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

        if (this.handler != null) {
            this.handler.onMessagesUpdated(context, channel, messages);
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

        if (this.handler != null) {
            this.handler.onMessagesDeleted(context, channel, messages);
        }
    }

    @UiThread
    @Override
    public void onChannelDeleted(@NonNull GroupChannelContext context, @NonNull String channelUrl) {
        Logger.d(">> ChannelViewModel::onChannelDeleted() from=%s", context.getCollectionEventSource());
        if (this.handler != null) {
            this.handler.onChannelDeleted(context, channelUrl);
        }
    }

    @UiThread
    @Override
    public void onHugeGapDetected() {
        Logger.d(">> ChannelViewModel::onHugeGapDetected()");
        if (this.handler != null) {
            this.handler.onHugeGapDetected();
        }
    }

    @Override
    public void onChannelUpdated(@NonNull GroupChannelContext context, @NonNull GroupChannel channel) {
        Logger.d(">> ChannelViewModel::onChannelUpdated() from=%s, url=%s", context.getCollectionEventSource(), channel.getUrl());

        //notifyChannelDataChanged();
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
                notifyChannelDataChanged();
                break;
        }

        if (this.handler != null) {
            this.handler.onChannelUpdated(context, channel);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Logger.dev("-- onCleared ChannelViewModel");
        SendBird.removeChannelHandler(ID_CHANNEL_EVENT_HANDLER);
        SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID);
        disposeMessageCollection();
        worker.shutdownNow();
    }

    private void markAsRead() {
        Logger.dev("markAsRead");
        channel.markAsRead();
    }

    public void setTyping(boolean isTyping) {
        if (channel != null) {
            if (isTyping) {
                channel.startTyping();
            } else {
                channel.endTyping();
            }
        }
    }

    public void sendUserMessage(@NonNull UserMessageParams params) {
        Logger.i("++ request send message : %s", params);
        channel.sendUserMessage(params, (message, e) -> {
            if (e != null) {
                Logger.e(e);
                return;
            }
            Logger.i("++ sent message : %s", message);
        });
    }

    public void sendFileMessage(@NonNull FileMessageParams params, @NonNull FileInfo fileInfo) {
        Logger.i("++ request send file message : %s", params);
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

    public void resendMessage(@NonNull BaseMessage message) {
        if (message instanceof UserMessage) {
            channel.resendMessage((UserMessage) message, (message12, e) -> {
                if (e != null) {
                    Logger.e(e);
                    errorToast.setValue(R.string.sb_text_error_resend_message);
                    return;
                }
                Logger.i("__ resent message : %s", message12);
            });
        } else if (message instanceof FileMessage) {
            FileInfo info = PendingMessageRepository.getInstance().getFileInfo(message);
            final File file = info == null ? null : info.getFile();
            channel.resendMessage((FileMessage) message, file, (message1, e1) -> {
                if (e1 != null) {
                    Logger.e(e1);
                    errorToast.setValue(R.string.sb_text_error_resend_message);
                    return;
                }
                Logger.i("__ resent file message : %s", message1);
            });
        }
    }

    public void updateUserMessage(long messageId, @NonNull UserMessageParams params) {
        channel.updateUserMessage(messageId, params, (message, e) -> {
            if (e != null) {
                errorToast.setValue(R.string.sb_text_error_update_user_message);
                return;
            }

            Logger.i("++ updated message : %s", message);
        });
    }

    public void deleteMessage(@NonNull BaseMessage message) {
        final BaseMessage.SendingStatus status = message.getSendingStatus();
        if (status == BaseMessage.SendingStatus.SUCCEEDED) {
            channel.deleteMessage(message, e -> {
                if (e != null) {
                    Logger.e(e);
                    errorToast.setValue(R.string.sb_text_error_delete_message);
                    return;
                }

                Logger.i("++ deleted message : %s", message);
            });
        } else if (status == BaseMessage.SendingStatus.FAILED) {
            if (collection != null) {
                collection.removeFailedMessages(Collections.singletonList(message), (requestIds, e) -> {
                    if (e != null) {
                        Logger.e(e);
                        errorToast.setValue(R.string.sb_text_error_delete_message);
                        return;
                    }

                    Logger.i("++ deleted message : %s", message);
                    notifyDataSetChanged(StringSet.ACTION_FAILED_MESSAGE_REMOVED);
                    if (message instanceof FileMessage) {
                        PendingMessageRepository.getInstance().clearFileInfo((FileMessage) message);
                    }
                });
            }
        }
    }

    public void toggleReaction(View view, BaseMessage message, String key) {
        if (!view.isSelected()) {
            Logger.i("__ add reaction : %s", key);
            channel.addReaction(message, key, (reactionEvent, e) -> {
                if (e != null) {
                    Logger.e(e);
                    errorToast.setValue(R.string.sb_text_error_add_reaction);
                }
            });
        } else {
            Logger.i("__ delete reaction : %s", key);
            channel.deleteReaction(message, key, (reactionEvent, e) -> {
                if (e != null) {
                    Logger.e(e);
                    errorToast.setValue(R.string.sb_text_error_delete_reaction);
                }
            });
        }
    }

    @UiThread
    public synchronized void loadInitial(final long startingPoint, @NonNull final MessageListParams params) {
        initMessageCollection(startingPoint, params);

        messageLoadState.postValue(MessageLoadState.LOAD_STARTED);
        cachedMessages.clear();
        if (collection != null) {
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
                    if (e == null && apiResultList != null && apiResultList.size() > 0) {
                        cachedMessages.clear();
                        cachedMessages.addAll(apiResultList);
                        notifyDataSetChanged(StringSet.ACTION_INIT_FROM_REMOTE);
                        markAsRead();
                    }
                    messageLoadState.postValue(MessageLoadState.LOAD_ENDED);
                }
            });
        }
    }

    @WorkerThread
    @Override
    public List<BaseMessage> loadPrevious() throws Exception  {
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

    @WorkerThread
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
}
