package com.sendbird.uikit.vm;

import androidx.annotation.NonNull;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.MessageChangeLogsParams;
import com.sendbird.android.MessageListParams;
import com.sendbird.android.SendBirdError;
import com.sendbird.android.SendBirdException;
import com.sendbird.uikit.log.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

class MessageChangeLogsPager {
    interface MessageChangeLogsResultHandler {
        void onError(SendBirdException e);
        void onResult(List<BaseMessage> addedMessageList, List<BaseMessage> updatedMessageList, List<Long> deletedMessageIdList);
    }

    private final BaseChannel channel;
    private final long lastSyncAt;
    private final MessageChangeLogsParams params;
    private final MessageListParams messageListParams;
    MessageChangeLogsPager(@NonNull BaseChannel channel, long lastSyncAt, @NonNull MessageListParams params) {
        this.channel = channel;
        this.lastSyncAt = lastSyncAt;
        this.params = MessageChangeLogsParams.from(params);
        this.messageListParams = params.clone();
        this.messageListParams.setPreviousResultSize(0);
        this.messageListParams.setNextResultSize(100);
    }

    void load(final MessageChangeLogsResultHandler handler) {
        load(true, handler);
    }

    void load(boolean fetchAll, final MessageChangeLogsResultHandler handler) {
        Executors.newSingleThreadExecutor().execute(() -> {
            final List<BaseMessage> updatedMessageList = new ArrayList<>();
            final List<BaseMessage> addedMessageList = new ArrayList<>();
            final List<Long> deletedMessageIdList = new ArrayList<>();

            CountDownLatch lock = new CountDownLatch(1);
            final AtomicReference<String> tokenRef = new AtomicReference<>();
            final AtomicReference<SendBirdException> error = new AtomicReference<>();
            final AtomicBoolean hasMoreRef = new AtomicBoolean();

            try {
                if (channel instanceof GroupChannel) {
                    load(lastSyncAt, params, (updated, deletedMessageIds, hasMore, token, e) -> {
                        try {
                            if (e != null) {
                                error.set(e);
                                if (handler != null) {
                                    handler.onError(e);
                                }
                                return;
                            }
                            tokenRef.set(token);
                            hasMoreRef.set(hasMore);

                            updatedMessageList.addAll(updated);
                            deletedMessageIdList.addAll(deletedMessageIds);
                        } finally {
                            lock.countDown();
                        }
                    });
                    lock.await();

                    while (hasMoreRef.get() && error.get() == null) {
                        CountDownLatch moreLock = new CountDownLatch(1);
                        more(tokenRef.get(), params, (updated, deletedMessageIds, hasMore, token, e) -> {
                            try {
                                if (e != null) {
                                    error.set(e);
                                    if (handler != null) {
                                        handler.onError(e);
                                    }
                                    return;
                                }

                                tokenRef.set(token);
                                hasMoreRef.set(hasMore);

                                updatedMessageList.addAll(updated);
                                deletedMessageIdList.addAll(deletedMessageIds);
                            } finally {
                                moreLock.countDown();
                            }
                        });
                        moreLock.await();
                    }
                }

                if (error.get() != null) {
                    throw error.get();
                }

                boolean hasMore;
                long timeStamp = lastSyncAt;
                do {
                    int totalSize = addedMessageList.size();
                    if (totalSize > 0) {
                        timeStamp = addedMessageList.get(0).getCreatedAt();
                    }
                    final List<BaseMessage> list = loadAddedMessageFromTimestamp(timeStamp);
                    addedMessageList.addAll(0, list);
                    hasMore = list.size() > 0;
                } while (hasMore && fetchAll);

                if (handler != null) {
                    handler.onResult(addedMessageList, updatedMessageList, deletedMessageIdList);
                }
            } catch (SendBirdException e) {
                Logger.e(e);
                if (handler != null) {
                    handler.onError(e);
                }
            } catch (Exception e) {
                Logger.e(e);
                if (handler != null) {
                    handler.onError(new SendBirdException(e.getMessage(), SendBirdError.ERR_REQUEST_FAILED));
                }
            }
        });
    }

    private void load(long ts, MessageChangeLogsParams params, BaseChannel.GetMessageChangeLogsHandler handler) {
        this.channel.getMessageChangeLogsSinceTimestamp(ts, params, handler);
    }

    private void more(String token, MessageChangeLogsParams params, BaseChannel.GetMessageChangeLogsByTokenHandler handler) {
        this.channel.getMessageChangeLogsSinceToken(token, params, handler);
    }

    private List<BaseMessage> loadAddedMessageFromTimestamp(long ts) throws Exception {
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
        return result.get();
    }
}
