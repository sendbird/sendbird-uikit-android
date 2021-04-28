package com.sendbird.uikit.vm;

import android.view.View;

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
import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.android.MessageListParams;
import com.sendbird.android.Reaction;
import com.sendbird.android.ReactionEvent;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class ChannelViewModel extends BaseViewModel implements LifecycleObserver, PagerRecyclerView.Pageable<List<BaseMessage>> {
    private static final int DEFAULT_MESSAGE_LOAD_SIZE = 40;
    private final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_GROUP_CHAT" + System.currentTimeMillis();;
    private final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_CHAT" + System.currentTimeMillis();;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final ExecutorService cachedMessageChangeLogs = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<BaseMessage>> messageList = new MutableLiveData<>();
    private final ChatMessageCollection messageCollection = new ChatMessageCollection();
    private final MutableLiveData<List<User>> typingMembers = new MutableLiveData<>();
    private final MutableLiveData<GroupChannel> isChannelChanged = new MutableLiveData<>();
    private final MutableLiveData<Boolean> channelDeleted = new MutableLiveData<>();
    private final MutableLiveData<Long> messageDeleted = new MutableLiveData<>();
    private final MessageListParams messageListParams;
    private final MutableLiveData<MessageLoadState> messageLoadState = new MutableLiveData<>();
    private final MutableLiveData<StatusFrameView.Status> statusFrame = new MutableLiveData<>();
    private final MutableLiveData<BaseMessage> incomingMessage = new MutableLiveData<>();
    private final MutableLiveData<BaseMessage> newRequestedMessage = new MutableLiveData<>();
    private final List<BaseMessage> cachedMessageList = new ArrayList<>();

    private GroupChannel channel;
    private volatile boolean hasPrevious;
    private volatile boolean hasNext;
    private long startingPoint;

    ChannelViewModel(@NonNull GroupChannel groupChannel, @Nullable MessageListParams params) {
        super();
        this.channel = groupChannel;
        messageListParams = params != null ? params : new MessageListParams();
        messageListParams.setReverse(true);
        messageListParams.setIncludeReactions(ReactionUtils.useReaction(groupChannel));
    }

    private boolean isCurrentChannel(@NonNull String channelUrl) {
        return channelUrl.equals(channel.getUrl());
    }

    public LiveData<GroupChannel> isChannelChanged() {
        return isChannelChanged;
    }

    public LiveData<Boolean> getChannelDeleted() {
        return channelDeleted;
    }

    public LiveData<Long> getMessageDeleted() {
        return messageDeleted;
    }

    public GroupChannel getChannel() {
        return channel;
    }

    public LiveData<List<BaseMessage>> getMessageList() {
        return messageList;
    }

    public LiveData<BaseMessage> getIncomingMessage() {
        return incomingMessage;
    }

    public LiveData<BaseMessage> getNewRequestedMessage() {
        return newRequestedMessage;
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

    public boolean hasNext() {
        return hasNext;
    }

    public boolean hasPrevious() {
        return hasPrevious;
    }

    public long getStartingPoint() {
        return startingPoint;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private void onCreate() {
        SendBird.addConnectionHandler(CONNECTION_HANDLER_ID, new SendBird.ConnectionHandler() {
            @Override
            public void onReconnectStarted() {
            }

            @Override
            public void onReconnectSucceeded() {
                // In preparation for the change of channel information, we have to call refresh of the channel.
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
                    cachedMessageChangeLogs();
                });
            }

            @Override
            public void onReconnectFailed() {
            }
        });

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                if (isCurrentChannel(baseChannel.getUrl())) {
                    Logger.i(">> ChannelFragnemt::onMessageReceived(%s), hasNext=%s", baseMessage.getMessageId(), hasNext);
                    ChannelViewModel.this.channel = (GroupChannel) baseChannel;
                    if (hasNext) {
                        synchronized (cachedMessageList) {
                            cachedMessageList.add(baseMessage);
                        }
                    } else {
                        messageCollection.add(baseMessage);
                        notifyDataSetChanged();
                    }
                    incomingMessage.postValue(baseMessage);
                    markAsRead();
                }
            }

            @Override
            public void onUserJoined(GroupChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ChannelFragnemt::onUserJoined()");
                    Logger.d("++ joind user : " + user);
                    ChannelViewModel.this.channel = channel;
                    isChannelChanged.postValue(channel);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onUserLeft(GroupChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ChannelFragnemt::onUserLeft()");
                    Logger.d("++ left user : " + user);
                    if (channel.getMyMemberState() == Member.MemberState.NONE) {
                        channelDeleted.postValue(true);
                        return;
                    }
                    ChannelViewModel.this.channel = channel;
                    isChannelChanged.postValue(channel);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onMessageDeleted(BaseChannel baseChannel, long msgId) {
                if (isCurrentChannel(baseChannel.getUrl())) {
                    Logger.i(">> ChannelFragnemt::onMessageDeleted()");
                    Logger.d("++ deletedMessage : " + msgId);
                    ChannelViewModel.this.channel = (GroupChannel) baseChannel;
                    messageDeleted.postValue(msgId);
                    messageCollection.removeByMessageId(msgId);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onMessageUpdated(BaseChannel baseChannel, BaseMessage updatedMessage) {
                if (isCurrentChannel(baseChannel.getUrl())) {
                    Logger.i(">> ChannelFragnemt::onMessageUpdated()");
                    Logger.d("++ updatedMessage : " + updatedMessage.getMessageId());
                    ChannelViewModel.this.channel = (GroupChannel) baseChannel;
                    messageCollection.update(updatedMessage);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onReadReceiptUpdated(GroupChannel baseChannel) {
                if (isCurrentChannel(baseChannel.getUrl())) {
                    Logger.i(">> ChannelFragnemt::onReadReceiptUpdated()");
                    ChannelViewModel.this.channel = baseChannel;
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onDeliveryReceiptUpdated(GroupChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ChannelFragnemt::onDeliveryReceiptUpdated()");
                    ChannelViewModel.this.channel = channel;
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onTypingStatusUpdated(GroupChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ChannelFragnemt::onTypingStatusUpdated()");
                    List<User> typingUsers = channel.getTypingUsers();
                    if (typingUsers.size() > 0) {
                        typingMembers.postValue(typingUsers);
                    } else {
                        typingMembers.postValue(null);
                    }
                }
            }

            @Override
            public void onChannelChanged(BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ChannelFragnemt::onChannelChanged()");
                    ChannelViewModel.this.channel = (GroupChannel) channel;
                    isChannelChanged.postValue((GroupChannel) channel);
                }
            }

            @Override
            public void onChannelDeleted(String channelUrl, BaseChannel.ChannelType channelType) {
                if (isCurrentChannel(channelUrl)) {
                    Logger.i(">> ChannelFragnemt::onChannelDeleted()");
                    Logger.d("++ deleted channel url : " + channelUrl);
                    // will have to finish activity
                    channelDeleted.postValue(true);
                }
            }

            @Override
            public void onChannelFrozen(BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ChannelFragnemt::onChannelFrozen(%s)", channel.isFrozen());
                    ChannelViewModel.this.channel = (GroupChannel) channel;
                    isChannelChanged.postValue((GroupChannel) channel);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onChannelUnfrozen(BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ChannelFragnemt::onChannelUnfrozen(%s)", channel.isFrozen());
                    ChannelViewModel.this.channel = (GroupChannel) channel;
                    isChannelChanged.postValue((GroupChannel) channel);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onReactionUpdated(BaseChannel channel, ReactionEvent reactionEvent) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ChannelFragnemt::onReactionUpdated()");
                    ChannelViewModel.this.channel = (GroupChannel) channel;
                    BaseMessage message = messageCollection.get(reactionEvent.getMessageId());
                    if (message != null) {
                        message.applyReactionEvent(reactionEvent);
                        notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onOperatorUpdated(BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ChannelFragnemt::onOperatorUpdated()");
                    ChannelViewModel.this.channel = (GroupChannel) channel;
                    Logger.i("++ my role : " + ((GroupChannel) channel).getMyRole());
                    isChannelChanged.postValue((GroupChannel) channel);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onUserBanned(BaseChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl()) &&
                        user.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                    Logger.i(">> ChannelFragnemt::onUserBanned()");
                    channelDeleted.postValue(true);
                }
            }

            @Override
            public void onUserMuted(BaseChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ChannelFragnemt::onUserMuted()");
                    ChannelViewModel.this.channel = (GroupChannel) channel;
                    isChannelChanged.postValue((GroupChannel) channel);
                }
            }

            @Override
            public void onUserUnmuted(BaseChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ChannelFragnemt::onUserUnmuted()");
                    ChannelViewModel.this.channel = (GroupChannel) channel;
                    isChannelChanged.postValue((GroupChannel) channel);
                }
            }
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private void onResume() {
        Logger.i("++ channel.getMyMemberState() : " + channel.getMyMemberState());
        if (channel.getMyMemberState() == Member.MemberState.JOINED) {
            requestChangeLogs(channel);
            cachedMessageChangeLogs();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private void onDestroy() {
        SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID);
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
    }

    private List<BaseMessage> loadMessages(long ts, MessageListParams params) throws Exception {
        Logger.dev(">> ChannelViewModel::loadMessages()");

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<List<BaseMessage>> result = new AtomicReference<>();
        final AtomicReference<Exception> error = new AtomicReference<>();

        channel.getMessagesByTimestamp(ts, params, (messages, e) -> {
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
        Logger.i("++ load messages result size : " + newMessageList.size());
        return newMessageList;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Logger.dev("-- onCleared ChannelViewModel");
        worker.shutdownNow();
    }

    private void markAsRead() {
        Logger.dev("markAsRead");
        channel.markAsRead();
    }

    private void notifyDataSetChanged() {
        List<BaseMessage> currentList = messageCollection.copyToList();
        if (!hasNext) {
            currentList.addAll(0, PendingMessageRepository.getInstance().getPendingMessageList(channel.getUrl()));
        }
        if (currentList.size() == 0) {
            statusFrame.postValue(StatusFrameView.Status.EMPTY);
        } else {
            statusFrame.postValue(StatusFrameView.Status.NONE);
            messageList.postValue(currentList);
        }
    }

    private synchronized void cachedMessageChangeLogs() {
        if (!hasNext || cachedMessageList.isEmpty()) return;

        final long firstTs = cachedMessageList.get(0).getCreatedAt();
        final int size = cachedMessageList.size();
        cachedMessageChangeLogs.submit(() -> {
            long ts = cachedMessageList.get(0).getCreatedAt();
            final List<BaseMessage> results = new ArrayList<>();
            if (firstTs == ts && cachedMessageList.size() != size) {
                // same request
                return results;
            }
            MessageListParams params = messageListParams.clone();
            params.setPreviousResultSize(0);
            params.setNextResultSize(100);
            boolean hasMore;
            do {
                if (!results.isEmpty()) {
                    ts = results.get(0).getCreatedAt();
                }
                List<BaseMessage> messages = loadMessages(ts, params);
                results.addAll(0, messages);
                hasMore = messages.size() > 0;
            } while (hasMore);

            cachedMessageList.addAll(results);
            return results;
        });
    }

    private void requestChangeLogs(@NonNull BaseChannel channel) {
        String channelUrl = channel.getUrl();
        int cacheMessageSize = messageCollection.size();
        long lastSyncTs = cacheMessageSize > 0 ? messageCollection.last().getCreatedAt() : 0;
        Logger.d("++ change logs channel url = %s, lastSyncTs = %s, hasNext=%s", channelUrl, lastSyncTs, hasNext);

        if (lastSyncTs > 0) {
            MessageChangeLogsPager pager = new MessageChangeLogsPager(channel, lastSyncTs, messageListParams);
            pager.load(!hasNext, new MessageChangeLogsPager.MessageChangeLogsResultHandler() {
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
                        markAsRead();
                    }
                }
            });
        }
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
            newRequestedMessage.postValue(message);
        });
        PendingMessageRepository.getInstance().addPendingMessage(channelUrl, pendingUserMessage);
        notifyDataSetChanged();
        newRequestedMessage.postValue(pendingUserMessage);
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
            newRequestedMessage.postValue(message);
        });
        if (pendingFileMessage != null) {
            PendingMessageRepository.getInstance().addPendingMessage(channelUrl, pendingFileMessage);
            PendingMessageRepository.getInstance().addFileInfo(pendingFileMessage, fileInfo);
            notifyDataSetChanged();
            newRequestedMessage.postValue(pendingFileMessage);
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
                newRequestedMessage.postValue(message);
            });
            PendingMessageRepository.getInstance().updatePendingMessage(channelUrl, pendingMessage);
            notifyDataSetChanged();
        } else if (message instanceof FileMessage) {
            FileInfo info = PendingMessageRepository.getInstance().getFileInfo(message);
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
                newRequestedMessage.postValue(message);
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
                messageCollection.remove(message);
                messageDeleted.postValue(message.getMessageId());
                notifyDataSetChanged();
            });
        } else {
            PendingMessageRepository.getInstance().removePendingMessage(message.getChannelUrl(), message);
            notifyDataSetChanged();
        }
    }

    public void toggleReaction(View view, BaseMessage message, String key) {
        if (!view.isSelected()) {
            Logger.i("__ add reaction : %s", key);
            channel.addReaction(message, key, (reactionEvent, e) -> {
                if (e != null) {
                    Logger.e(e);
                    errorToast.postValue(R.string.sb_text_error_add_reaction);
                }
            });
        } else {
            Logger.i("__ delete reaction : %s", key);
            channel.deleteReaction(message, key, (reactionEvent, e) -> {
                if (e != null) {
                    Logger.e(e);
                    errorToast.postValue(R.string.sb_text_error_delete_reaction);
                }
            });
        }
    }

    public Map<Reaction, List<User>> getReactionUserInfo(List<Reaction> reactionList) {
        Map<Reaction, List<User>> result = new HashMap<>();
        Map<String, User> userMap = new HashMap<>();

        for (Member member : channel.getMembers()) {
            userMap.put(member.getUserId(), member);
        }

        for (Reaction reaction : reactionList) {
            List<User> userList = new ArrayList<>();
            List<String> userIds = reaction.getUserIds();
            for (String userId : userIds) {
                User user = userMap.get(userId);
                userList.add(user);
            }
            result.put(reaction, userList);
        }

        return result;
    }

    public void loadInitial(long ts) {
        worker.execute(() -> {
            try {
                Logger.i("____________ loadInitial()");
                messageLoadState.postValue(MessageLoadState.LOAD_STARTED);
                messageCollection.clear();
                Logger.i("____________ cachedMessageList size=%s,  hasNext=%s", cachedMessageList.size(), hasNext);
                if (hasNext && cachedMessageList.size() > 0 && cachedMessageList.get(0).getCreatedAt() < ts) {
                    hasNext = false;
                    hasPrevious = true;

                    messageCollection.addAll(cachedMessageList);
                    notifyDataSetChanged();
                }

                startingPoint = ts;

                MessageListParams params = messageListParams.clone();
                params.setPreviousResultSize(DEFAULT_MESSAGE_LOAD_SIZE / 2);
                params.setNextResultSize(0);
                params.setInclusive(true);
                List<BaseMessage> prevList = loadMessages(ts, params);
                hasPrevious = prevList.size() >= params.getPreviousResultSize();
                Logger.i("____________ load initial prev message list : %s, hasPrevious=%s", prevList.size(), hasPrevious);

                params.setPreviousResultSize(0);
                params.setNextResultSize(DEFAULT_MESSAGE_LOAD_SIZE / 2);
                params.setInclusive(false);
                List<BaseMessage> nextList = loadMessages(ts, params);
                prevList.addAll(nextList);

                hasNext = !(ts == Long.MAX_VALUE || (nextList.size() < params.getNextResultSize()));
                Logger.i("____________ load initial nextList message list : %s, hasNext=%s", nextList.size(), hasNext);

                if (hasNext) {
                    params = messageListParams.clone();
                    params.setPreviousResultSize(DEFAULT_MESSAGE_LOAD_SIZE);
                    List<BaseMessage> lastestMessages = loadMessages(Long.MAX_VALUE, params);
                    synchronized (cachedMessageList) {
                        cachedMessageList.clear();
                        cachedMessageList.addAll(lastestMessages);
                    }
                }

                messageCollection.clear();
                messageCollection.addAll(prevList);
                markAsRead();
            } catch (Exception e) {
                Logger.w(e);
            } finally {
                notifyDataSetChanged();
                messageLoadState.postValue(MessageLoadState.LOAD_ENDED);
            }
        });
    }

    @Override
    public List<BaseMessage> loadPrevious() throws Exception  {
        if (!hasPrevious) return Collections.emptyList();

        List<BaseMessage> newMessageList;
        try {
            Logger.i("____________ loadPrevious()");
            messageLoadState.postValue(MessageLoadState.LOAD_STARTED);
            long ts = messageCollection.first().getCreatedAt();

            MessageListParams params = messageListParams.clone();
            params.setPreviousResultSize(DEFAULT_MESSAGE_LOAD_SIZE);
            params.setNextResultSize(0);
            newMessageList = loadMessages(ts, params);
            hasPrevious = newMessageList.size() >= params.getPreviousResultSize();
            Logger.i("____________ load previous message list : %s, hasPrevious=%s", newMessageList.size(), hasPrevious);
            messageCollection.addAll(newMessageList);
        } catch (Exception e) {
            Logger.w(e);
            throw e;
        } finally {
            notifyDataSetChanged();
            messageLoadState.postValue(MessageLoadState.LOAD_ENDED);
        }
        return newMessageList;
    }

    @Override
    public List<BaseMessage> loadNext() throws Exception {
        if (!hasNext) return Collections.emptyList();

        List<BaseMessage> newMessageList;
        try {
            Logger.i("____________ loadNext()");
            messageLoadState.postValue(MessageLoadState.LOAD_STARTED);
            long ts = messageCollection.last().getCreatedAt();
            MessageListParams params = messageListParams.clone();
            params.setPreviousResultSize(0);
            params.setNextResultSize(DEFAULT_MESSAGE_LOAD_SIZE);
            params.setReverse(false);
            newMessageList = loadMessages(ts, params);
            hasNext = newMessageList.size() >= params.getNextResultSize();
            if (newMessageList.size() > 0) {
                Logger.i("____________ load next message list : %s, hasNext= %s", newMessageList.size(), hasNext);
            }
            if (!hasNext) {
                synchronized (cachedMessageList) {
                    newMessageList.addAll(cachedMessageList);
                    cachedMessageList.clear();
                }
            }
            messageCollection.addAll(newMessageList);
        } catch (Exception e) {
            Logger.w(e);
            throw e;
        } finally {
            notifyDataSetChanged();
            messageLoadState.postValue(MessageLoadState.LOAD_ENDED);
        }
        return newMessageList;
    }
}
