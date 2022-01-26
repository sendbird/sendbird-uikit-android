package com.sendbird.uikit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.FileMessageParams;
import com.sendbird.android.MessageListParams;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdError;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;
import com.sendbird.android.UserMessageParams;
import com.sendbird.uikit.R;
import com.sendbird.uikit.consts.MessageLoadState;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.FileInfo;
import com.sendbird.uikit.model.MessageList;
import com.sendbird.uikit.utils.ReactionUtils;
import com.sendbird.uikit.widgets.PagerRecyclerView;
import com.sendbird.uikit.widgets.StatusFrameView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class OpenChannelViewModel extends BaseViewModel implements LifecycleObserver, PagerRecyclerView.Pageable<List<BaseMessage>> {
    private static final int DEFAULT_MESSAGE_LOAD_SIZE = 40;
    private final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_OPEN_CHAT" + System.currentTimeMillis();
    private final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_OPEN_CHANNEL_CHAT" + System.currentTimeMillis();
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<BaseMessage>> messageList = new MutableLiveData<>();
    private final MessageList messageCollection = new MessageList();
    private final MutableLiveData<OpenChannel> isChannelChanged = new MutableLiveData<>();
    private final MutableLiveData<Boolean> channelDeleted = new MutableLiveData<>();
    private final MutableLiveData<Long> messageDeleted = new MutableLiveData<>();
    private final MessageListParams messageListParams;
    private final MutableLiveData<MessageLoadState> messageLoadState = new MutableLiveData<>();
    private final MutableLiveData<StatusFrameView.Status> statusFrame = new MutableLiveData<>();
    private final OpenChannel channel;
    private final Observer<BaseMessage> pendingStatusObserver;

    private boolean hasPrevious = true;

    OpenChannelViewModel(@NonNull OpenChannel openChannel, @Nullable MessageListParams params) {
        super();
        this.channel = openChannel;
        this.messageListParams = params != null ? params : new MessageListParams();
        this.messageListParams.setReverse(true);
        this.messageListParams.setNextResultSize(0);
        this.messageListParams.setIncludeReactions(ReactionUtils.useReaction(openChannel));
        if (messageListParams.getPreviousResultSize() <= 0) {
            messageListParams.setPreviousResultSize(DEFAULT_MESSAGE_LOAD_SIZE);
        }

        this.pendingStatusObserver = message -> {
            Logger.d("__ pending message events, message = %s", message.getMessage());
            if (message.getChannelUrl().equals(channel.getUrl())) {
                final BaseMessage.SendingStatus sendingStatus = message.getSendingStatus();
                Logger.i("__ pending status of message is changed, pending status = %s ", sendingStatus);
                if (sendingStatus == BaseMessage.SendingStatus.SUCCEEDED) {
                    messageCollection.add(message);
                }
                notifyDataSetChanged();
            }
        };
        PendingMessageRepository.getInstance().addPendingMessageStatusChanged(pendingStatusObserver);
        registerChannelHandler();
    }

    private boolean isCurrentChannel(@NonNull String channelUrl) {
        return channelUrl.equals(channel.getUrl());
    }

    public LiveData<OpenChannel> isChannelChanged() {
        return isChannelChanged;
    }

    public LiveData<Boolean> getChannelDeleted() {
        return channelDeleted;
    }

    public LiveData<Long> getMessageDeleted() {
        return messageDeleted;
    }

    public OpenChannel getChannel() {
        return channel;
    }

    public LiveData<List<BaseMessage>> getMessageList() {
        return messageList;
    }

    public LiveData<StatusFrameView.Status> getStatusFrame() {
        return statusFrame;
    }

    public LiveData<MessageLoadState> getMessageLoadState() {
        return messageLoadState;
    }

    private void registerChannelHandler() {
        SendBird.addConnectionHandler(CONNECTION_HANDLER_ID, new SendBird.ConnectionHandler() {
            @Override
            public void onReconnectStarted() {
            }

            @Override
            public void onReconnectSucceeded() {
                // In preparation for the change of channel information, we have to call refresh of the channel.
                if (channel != null) {
                    channel.enter(e1 -> {
                        if (e1 != null) {
                            channelDeleted.postValue(true);
                            return;
                        }
                        channel.refresh(e -> {
                            if (e != null) {
                                Logger.dev(e);
                                // already left this channel at the other device.
                                if (e.getCode() == SendBirdError.ERR_NON_AUTHORIZED) {
                                    channelDeleted.postValue(true);
                                    return;
                                }
                            } else {
                                isChannelChanged.postValue(channel);
                            }
                            requestChangeLogs(channel);
                        });
                    });
                }
            }

            @Override
            public void onReconnectFailed() {
            }
        });

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                if (!messageListParams.belongsTo(baseMessage)) return;

                if (isCurrentChannel(baseChannel.getUrl())) {
                    Logger.i(">> ChannelFragnemt::onMessageReceived(%s)", baseMessage.getMessageId());
                    messageCollection.add(baseMessage);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onUserEntered(OpenChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onUserEntered()");
                    Logger.d("++ joind user : " + user);
                    notifyDataSetChanged();
                    isChannelChanged.postValue(channel);
                }
            }

            @Override
            public void onUserExited(OpenChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onUserLeft()");
                    Logger.d("++ left user : " + user);
                    notifyDataSetChanged();
                    isChannelChanged.postValue(channel);
                }
            }

            @Override
            public void onMessageDeleted(BaseChannel baseChannel, long msgId) {
                if (isCurrentChannel(baseChannel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onMessageDeleted()");
                    Logger.d("++ deletedMessage : " + msgId);
                    messageCollection.deleteByMessageId(msgId);
                    notifyDataSetChanged();
                    messageDeleted.postValue(msgId);
                }
            }

            @Override
            public void onMessageUpdated(BaseChannel baseChannel, BaseMessage updatedMessage) {
                if (isCurrentChannel(baseChannel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onMessageUpdated()");
                    Logger.d("++ updatedMessage : " + updatedMessage.getMessageId());
                    if (!messageListParams.belongsTo(updatedMessage)) {
                        final long msgId = updatedMessage.getMessageId();
                        messageCollection.deleteByMessageId(msgId);
                        messageDeleted.postValue(msgId);
                    } else {
                        messageCollection.update(updatedMessage);
                    }
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onChannelChanged(BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onChannelChanged()");
                    isChannelChanged.postValue((OpenChannel) channel);
                }
            }

            @Override
            public void onChannelDeleted(String channelUrl, BaseChannel.ChannelType channelType) {
                if (isCurrentChannel(channelUrl)) {
                    Logger.i(">> OpenChannelViewModel::onChannelDeleted()");
                    Logger.d("++ deleted channel url : " + channelUrl);
                    // will have to finish activity
                    channelDeleted.postValue(true);
                }
            }

            @Override
            public void onChannelFrozen(BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onChannelFrozen(%s)", channel.isFrozen());
                    notifyDataSetChanged();
                    isChannelChanged.postValue((OpenChannel) channel);
                }
            }

            @Override
            public void onChannelUnfrozen(BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onChannelUnfrozen(%s)", channel.isFrozen());
                    notifyDataSetChanged();
                    isChannelChanged.postValue((OpenChannel) channel);
                }
            }

            @Override
            public void onOperatorUpdated(BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onOperatorUpdated()");
                    Logger.i("++ Am I an operator : " + ((OpenChannel) channel).isOperator(SendBird.getCurrentUser()));
                    notifyDataSetChanged();
                    isChannelChanged.postValue((OpenChannel) channel);
                }
            }

            @Override
            public void onUserBanned(BaseChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl()) &&
                        user.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                    Logger.i(">> OpenChannelViewModel::onUserBanned()");
                    channelDeleted.postValue(true);
                }
            }

            @Override
            public void onUserMuted(BaseChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onUserMuted()");
                    isChannelChanged.postValue((OpenChannel) channel);
                }
            }

            @Override
            public void onUserUnmuted(BaseChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onUserUnmuted()");
                    isChannelChanged.postValue((OpenChannel) channel);
                }
            }

            @Override
            public void onChannelParticipantCountChanged(List<OpenChannel> channels) {
                com.sendbird.android.log.Logger.i(">> OpenChannelViewModel::onChannelParticipantCountChanged()");
                if (channels != null && !channels.isEmpty()) {
                    for (OpenChannel channel : channels) {
                        if (isCurrentChannel(channel.getUrl())) {
                            isChannelChanged.postValue(channel);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onMentionReceived(BaseChannel channel, BaseMessage message) {
                com.sendbird.android.log.Logger.i(">> MessageCollection::onMentionReceived()");
                if (isCurrentChannel(channel.getUrl())) {
                    isChannelChanged.postValue((OpenChannel) channel);
                }
            }
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private void onResume() {
        requestChangeLogs(channel);
    }

    public void load() {
        worker.execute(() -> {
            try {
                loadPrevious();
            } catch (Exception e) {
                Logger.w(e);
            }
        });
    }

    private List<BaseMessage> loadPrevious(long ts) throws Exception {
        Logger.dev(">> ChannelViewModel::loadPrevious()");

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<List<BaseMessage>> result = new AtomicReference<>();
        final AtomicReference<Exception> error = new AtomicReference<>();

        channel.getMessagesByTimestamp(ts, messageListParams, (messages, e) -> {
            try {
                if (e != null) {
                    error.set(e);
                    return;
                }
                result.set(messages);
            } finally {
                latch.countDown();
            }
        });
        latch.await();

        if (error.get() != null) throw error.get();
        final List<BaseMessage> newMessageList = result.get();
        Logger.i("++ load previous result size : " + newMessageList.size());
        return newMessageList;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Logger.i("-- onCleared ChannelViewModel");
        SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID);
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
        worker.shutdownNow();
        PendingMessageRepository.getInstance().removePendingMessageStatusObserver(pendingStatusObserver);
    }

    private void notifyDataSetChanged() {
        List<BaseMessage> currentList = messageCollection.toList();
        currentList.addAll(0, PendingMessageRepository.getInstance().getPendingMessageList(channel.getUrl()));
        if (currentList.size() == 0) {
            statusFrame.postValue(StatusFrameView.Status.EMPTY);
        } else {
            statusFrame.postValue(StatusFrameView.Status.NONE);
            messageList.postValue(currentList);
        }
    }

    private void requestChangeLogs(@NonNull BaseChannel channel) {
        String channelUrl = channel.getUrl();
        int cacheMessageSize = messageCollection.size();
        BaseMessage lastMessage = messageCollection.getLatestMessage();
        long lastSyncTs = cacheMessageSize > 0 && lastMessage != null ? lastMessage.getCreatedAt() : 0;
        Logger.dev("++ change logs channel url = %s, lastSyncTs = %s", channelUrl, lastSyncTs);

        if (lastSyncTs > 0) {
            MessageChangeLogsPager pager = new MessageChangeLogsPager(channel, lastSyncTs, messageListParams);
            pager.load(new MessageChangeLogsPager.MessageChangeLogsResultHandler() {
                @Override
                public void onError(SendBirdException e) {
                    Logger.e(e);
                }

                @Override
                public void onResult(List<BaseMessage> added, List<BaseMessage> updated, List<Long> deletedIds) {
                    for (long deletedId : deletedIds) {
                        BaseMessage deletedMessage = messageCollection.getById(deletedId);
                        if (deletedMessage != null) {
                            messageCollection.delete(deletedMessage);
                        }
                    }
                    List<BaseMessage> filteredAdded = new ArrayList<>();
                    for (BaseMessage addedMessage : added) {
                        if (messageListParams.belongsTo(addedMessage)) {
                            filteredAdded.add(addedMessage);
                        }
                    }
                    List<BaseMessage> filteredUpdated = new ArrayList<>();
                    for (BaseMessage updatedMessage : updated) {
                        if (messageListParams.belongsTo(updatedMessage)) {
                            filteredUpdated.add(updatedMessage);
                        }
                    }
                    Logger.i("++ channel message change logs result >> deleted message size : %s, current message size : %s, added message size : %s", deletedIds.size(), messageCollection.size(), filteredAdded.size());
                    Logger.i("++ updated Message size : %s", filteredUpdated.size());
                    messageCollection.updateAll(filteredUpdated);
                    if (filteredAdded.size() > 0) {
                        messageCollection.addAll(filteredAdded);
                    }
                    Logger.i("++ merged message size : %s", messageCollection.size());
                    boolean changed = filteredAdded.size() > 0 || filteredUpdated.size() > 0 || deletedIds.size() > 0;
                    Logger.dev("++ changeLogs updated : %s", changed);

                    if (changed) {
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }

    public void sendUserMessage(@NonNull UserMessageParams params) {
        Logger.i("++ request send message : %s", params);
        final String channelUrl = channel.getUrl();
        UserMessage pendingUserMessage = channel.sendUserMessage(params, (message, e) -> {
            if (e != null) {
                Logger.e(e);
                PendingMessageRepository.getInstance().updatePendingMessage(channelUrl, message);
                return;
            }

            if (messageListParams.belongsTo(message)) {
                Logger.i("++ sent message : %s", message);
                PendingMessageRepository.getInstance().removePendingMessage(channelUrl, message);
            }
        });
        if (pendingUserMessage != null) {
            if (messageListParams.belongsTo(pendingUserMessage)) {
                PendingMessageRepository.getInstance().addPendingMessage(channelUrl, pendingUserMessage);
            } else {
                errorToast.postValue(R.string.sb_text_error_message_filtered);
            }
        }
    }

    public void sendFileMessage(@NonNull FileMessageParams params, @NonNull FileInfo fileInfo) {
        Logger.i("++ request send file message : %s", params);
        final String channelUrl = channel.getUrl();
        FileMessage pendingFileMessage = channel.sendFileMessage(params, (message, ee) -> {
            if (ee != null) {
                Logger.e(ee);
                if (message != null) {
                    PendingMessageRepository.getInstance().updatePendingMessage(channelUrl, message);
                }
                if (ee.getMessage() != null) {
                    errorToast.postValue(R.string.sb_text_error_send_message);
                }
                return;
            }

            if (messageListParams.belongsTo(message)) {
                Logger.i("++ sent message : %s", message);
                //if (file.exists()) file.deleteOnExit();
                PendingMessageRepository.getInstance().removePendingMessage(channelUrl, message);
            }
        });

        if (pendingFileMessage != null) {
            if (messageListParams.belongsTo(pendingFileMessage)) {
                PendingMessageRepository.getInstance().addPendingMessage(channelUrl, pendingFileMessage);
                PendingMessageRepository.getInstance().addFileInfo(pendingFileMessage, fileInfo);
            } else {
                errorToast.postValue(R.string.sb_text_error_message_filtered);
            }
        }
    }

    public void resendMessage(@NonNull BaseMessage message) {
        final String channelUrl = channel.getUrl();
        if (message instanceof UserMessage) {
            UserMessage pendingMessage = channel.resendMessage((UserMessage) message, (message12, e) -> {
                if (e != null) {
                    Logger.e(e);
                    errorToast.postValue(R.string.sb_text_error_resend_message);
                    PendingMessageRepository.getInstance().updatePendingMessage(channelUrl, message12);
                    return;
                }

                Logger.i("__ resent message : %s", message12);
                PendingMessageRepository.getInstance().removePendingMessage(channelUrl, message12);
            });
            PendingMessageRepository.getInstance().updatePendingMessage(channelUrl, pendingMessage);
        } else if (message instanceof FileMessage) {
            FileInfo info = PendingMessageRepository.getInstance().getFileInfo(message);
            Logger.d("++ file info=%s", info);
            final File file = info == null ? null : info.getFile();
            FileMessage pendingMessage = channel.resendMessage((FileMessage) message, file, (message1, e1) -> {
                if (e1 != null) {
                    Logger.e(e1);
                    errorToast.postValue(R.string.sb_text_error_resend_message);
                    PendingMessageRepository.getInstance().updatePendingMessage(channelUrl, message1);
                    return;
                }

                Logger.i("__ resent file message : %s", message1);
                //if (file.exists()) file.deleteOnExit();
                PendingMessageRepository.getInstance().removePendingMessage(channelUrl, message1);
            });
            PendingMessageRepository.getInstance().updatePendingMessage(channelUrl, pendingMessage);
        }
    }

    public void updateUserMessage(long messageId, @NonNull UserMessageParams params) {
        channel.updateUserMessage(messageId, params, (message, e) -> {
            if (e != null) {
                errorToast.postValue(R.string.sb_text_error_update_user_message);
                return;
            }

            Logger.i("++ updated message : %s", message);
            messageCollection.update(message);
            notifyDataSetChanged();
        });
    }

    public void deleteMessage(@NonNull BaseMessage message) {
        if (message.getSendingStatus() == BaseMessage.SendingStatus.SUCCEEDED) {
            channel.deleteMessage(message, e -> {
                if (e != null) {
                    errorToast.postValue(R.string.sb_text_error_delete_message);
                    return;
                }

                Logger.i("++ deleted message : %s", message);
                messageDeleted.postValue(message.getMessageId());
                messageCollection.delete(message);
                notifyDataSetChanged();
            });
        } else {
            PendingMessageRepository.getInstance().removePendingMessage(message.getChannelUrl(), message);
        }
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public boolean hasPrevious() {
        return hasPrevious;
    }

    @Override
    public List<BaseMessage> loadPrevious() throws Exception {
        if (!hasPrevious()) return Collections.emptyList();

        List<BaseMessage> newMessageList;
        try {
            messageLoadState.postValue(MessageLoadState.LOAD_STARTED);
            int cacheMessageSize = messageCollection.size();
            BaseMessage oldMessage = messageCollection.getOldestMessage();
            long ts = cacheMessageSize > 0 && oldMessage != null ? oldMessage.getCreatedAt() : Long.MAX_VALUE;
            newMessageList = loadPrevious(ts);
            Logger.i("++ load previous message list : " + newMessageList);
            messageCollection.addAll(newMessageList);
            hasPrevious = newMessageList.size() >= messageListParams.getPreviousResultSize();
            return newMessageList;
        } catch (Exception e) {
            Logger.w(e);
            throw e;
        } finally {
            notifyDataSetChanged();
            messageLoadState.postValue(MessageLoadState.LOAD_ENDED);
        }
    }

    @Override
    public List<BaseMessage> loadNext() {
        return Collections.emptyList();
    }
}
