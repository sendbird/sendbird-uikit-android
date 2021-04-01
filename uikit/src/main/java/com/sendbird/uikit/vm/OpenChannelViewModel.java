package com.sendbird.uikit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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
import com.sendbird.uikit.model.ChatMessageCollection;
import com.sendbird.uikit.model.FileInfo;
import com.sendbird.uikit.utils.ReactionUtils;
import com.sendbird.uikit.widgets.PagerRecyclerView;
import com.sendbird.uikit.widgets.StatusFrameView;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class OpenChannelViewModel extends BaseViewModel implements LifecycleObserver, PagerRecyclerView.Pageable<List<BaseMessage>> {
    private static final int DEFAULT_MESSAGE_LOAD_SIZE = 40;
    private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_GROUP_CHAT";
    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_CHAT";
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<BaseMessage>> messageList = new MutableLiveData<>();
    private final ChatMessageCollection messageCollection = new ChatMessageCollection();
    private final MutableLiveData<OpenChannel> isChannelChanged = new MutableLiveData<>();
    private final MutableLiveData<Boolean> channelDeleted = new MutableLiveData<>();
    private final MutableLiveData<Long> messageDeleted = new MutableLiveData<>();
    private final MessageListParams messageListParams;
    private final MutableLiveData<MessageLoadState> messageLoadState = new MutableLiveData<>();
    private final MutableLiveData<StatusFrameView.Status> statusFrame = new MutableLiveData<>();

    private OpenChannel channel;

    OpenChannelViewModel(@NonNull OpenChannel openChannel, @Nullable MessageListParams params) {
        super();
        this.channel = openChannel;
        messageListParams = params != null ? params : new MessageListParams();
        messageListParams.setReverse(true);
        messageListParams.setNextResultSize(0);
        messageListParams.setIncludeReactions(ReactionUtils.useReaction(openChannel));
        if (messageListParams.getPreviousResultSize() <= 0) {
            messageListParams.setPreviousResultSize(DEFAULT_MESSAGE_LOAD_SIZE);
        }
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

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private void onResume() {
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
                if (isCurrentChannel(baseChannel.getUrl())) {
                    Logger.i(">> ChannelFragnemt::onMessageReceived(%s)", baseMessage.getMessageId());
                    OpenChannelViewModel.this.channel = (OpenChannel) baseChannel;
                    messageCollection.add(baseMessage);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onUserEntered(OpenChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onUserEntered()");
                    Logger.d("++ joind user : " + user);
                    OpenChannelViewModel.this.channel = channel;
                    isChannelChanged.postValue(channel);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onUserExited(OpenChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onUserLeft()");
                    Logger.d("++ left user : " + user);
                    OpenChannelViewModel.this.channel = channel;
                    isChannelChanged.postValue(channel);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onMessageDeleted(BaseChannel baseChannel, long msgId) {
                if (isCurrentChannel(baseChannel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onMessageDeleted()");
                    Logger.d("++ deletedMessage : " + msgId);
                    OpenChannelViewModel.this.channel = (OpenChannel) baseChannel;
                    messageDeleted.postValue(msgId);
                    messageCollection.removeByMessageId(msgId);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onMessageUpdated(BaseChannel baseChannel, BaseMessage updatedMessage) {
                if (isCurrentChannel(baseChannel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onMessageUpdated()");
                    Logger.d("++ updatedMessage : " + updatedMessage.getMessageId());
                    OpenChannelViewModel.this.channel = (OpenChannel) baseChannel;
                    messageCollection.update(updatedMessage);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onChannelChanged(BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onChannelChanged()");
                    OpenChannelViewModel.this.channel = (OpenChannel) channel;
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
                    OpenChannelViewModel.this.channel = (OpenChannel) channel;
                    isChannelChanged.postValue((OpenChannel) channel);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onChannelUnfrozen(BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onChannelUnfrozen(%s)", channel.isFrozen());
                    OpenChannelViewModel.this.channel = (OpenChannel) channel;
                    isChannelChanged.postValue((OpenChannel) channel);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onOperatorUpdated(BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onOperatorUpdated()");
                    OpenChannelViewModel.this.channel = (OpenChannel) channel;
                    Logger.i("++ Am I an operator : " + ((OpenChannel) channel).isOperator(SendBird.getCurrentUser()));
                    isChannelChanged.postValue((OpenChannel) channel);
                    notifyDataSetChanged();
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
                    OpenChannelViewModel.this.channel = (OpenChannel) channel;
                    isChannelChanged.postValue((OpenChannel) channel);
                }
            }

            @Override
            public void onUserUnmuted(BaseChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onUserUnmuted()");
                    OpenChannelViewModel.this.channel = (OpenChannel) channel;
                    isChannelChanged.postValue((OpenChannel) channel);
                }
            }
        });

        requestChangeLogs(channel);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private void onDestroy() {
        SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID);
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
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
        Logger.dev("-- onCleared ChannelViewModel");
        worker.shutdownNow();
    }

    private void notifyDataSetChanged() {
        List<BaseMessage> currentList = messageCollection.copyToList();
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
        long lastSyncTs = cacheMessageSize > 0 ? messageCollection.last().getCreatedAt() : 0;
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
                    Logger.i("++ channel message change logs result >> deleted message size : %s, current message size : %s, added message size : %s", deletedIds.size(), messageCollection.size(), added.size());
                    for (long deletedId : deletedIds) {
                        BaseMessage deletedMessage = messageCollection.get(deletedId);
                        if (deletedMessage != null) {
                            messageCollection.remove(deletedMessage);
                        }
                    }
                    Logger.i("++ updated Message size : %s", updated.size());
                    messageCollection.updateAll(updated);
                    if (added.size() > 0) {
                        messageCollection.addAll(added);
                    }
                    Logger.i("++ merged message size : %s", messageCollection.size());
                    boolean changed = added.size() > 0 || updated.size() > 0 || deletedIds.size() > 0;
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
                notifyDataSetChanged();
                return;
            }

            Logger.i("++ sent message : %s", message);
            messageCollection.add(message);
            PendingMessageRepository.getInstance().removePendingMessage(channelUrl, message);
            notifyDataSetChanged();
        });
        PendingMessageRepository.getInstance().addPendingMessage(channelUrl, pendingUserMessage);
        notifyDataSetChanged();
    }

    public void sendFileMessage(@NonNull FileMessageParams params, @NonNull FileInfo fileInfo) {
        Logger.i("++ request send file message : %s", params);
        final String channelUrl = channel.getUrl();
        FileMessage pendingFileMessage = channel.sendFileMessage(params, (message, ee) -> {
            if (ee != null) {
                Logger.e(ee);
                if (message != null) {
                    PendingMessageRepository.getInstance().updatePendingMessage(channelUrl, message);
                    notifyDataSetChanged();
                }
                if (ee.getMessage() != null) {
                    errorToast.postValue(R.string.sb_text_error_send_message);
                }
                return;
            }

            Logger.i("++ sent message : %s", message);
            //if (file.exists()) file.deleteOnExit();
            messageCollection.add(message);
            PendingMessageRepository.getInstance().removePendingMessage(channelUrl, message);
            notifyDataSetChanged();
        });
        if (pendingFileMessage != null) {
            PendingMessageRepository.getInstance().addPendingMessage(channelUrl, pendingFileMessage);
            PendingMessageRepository.getInstance().addFileInfo(pendingFileMessage, fileInfo);
            notifyDataSetChanged();
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
                    notifyDataSetChanged();
                    return;
                }

                Logger.i("__ resent message : %s", message12);
                messageCollection.add(message12);
                PendingMessageRepository.getInstance().removePendingMessage(channelUrl, message12);
                notifyDataSetChanged();
            });
            PendingMessageRepository.getInstance().updatePendingMessage(channelUrl, pendingMessage);
            notifyDataSetChanged();
        } else if (message instanceof FileMessage) {
            FileInfo info = PendingMessageRepository.getInstance().getFileInfo(message);
            Logger.d("++ file info=%s", info);
            FileMessage pendingMessage = channel.resendMessage((FileMessage) message, info.getFile(), (message1, e1) -> {
                if (e1 != null) {
                    Logger.e(e1);
                    errorToast.postValue(R.string.sb_text_error_resend_message);
                    PendingMessageRepository.getInstance().updatePendingMessage(channelUrl, message1);
                    notifyDataSetChanged();
                    return;
                }

                Logger.i("__ resent file message : %s", message1);
                //if (file.exists()) file.deleteOnExit();
                messageCollection.add(message1);
                PendingMessageRepository.getInstance().removePendingMessage(channelUrl, message1);
                notifyDataSetChanged();
            });
            PendingMessageRepository.getInstance().updatePendingMessage(channelUrl, pendingMessage);
            notifyDataSetChanged();
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
                messageCollection.remove(message);
                notifyDataSetChanged();
            });
        } else {
            PendingMessageRepository.getInstance().removePendingMessage(message.getChannelUrl(), message);
            notifyDataSetChanged();
        }
    }

    @Override
    public List<BaseMessage> loadPrevious() throws Exception {
        List<BaseMessage> newMessageList;
        try {
            messageLoadState.postValue(MessageLoadState.LOAD_STARTED);
            int cacheMessageSize = messageCollection.size();
            long ts = cacheMessageSize > 0 ? messageCollection.first().getCreatedAt() : Long.MAX_VALUE;
            newMessageList = loadPrevious(ts);
            Logger.i("++ load previous message list : " + newMessageList);
            messageCollection.addAll(newMessageList);
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
