package com.sendbird.uikit.vm;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelChangeLogsParams;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdError;
import com.sendbird.android.SendBirdException;
import com.sendbird.uikit.log.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

class ChannelChangeLogsPager {
    interface ChannelChangeLogsResultHandler {
        void onError(SendBirdException e);
        void onResult(List<GroupChannel> updatedChannels, List<String> deletedChannelUrls);
    }

    private final AtomicLong lastSyncTs = new AtomicLong(0);
    private final GroupChannelChangeLogsParams params;
    ChannelChangeLogsPager(long lastSyncTs, GroupChannelChangeLogsParams params) {
        this.lastSyncTs.set(lastSyncTs);
        this.params = params;
    }

    void load(final ChannelChangeLogsResultHandler handler) {
        Executors.newSingleThreadExecutor().execute(() -> {
            final List<GroupChannel> updatedChannels = new ArrayList<>();
            final List<String> deletedChannelUrls = new ArrayList<>();

            CountDownLatch lock = new CountDownLatch(1);
            final AtomicReference<String> tokenRef = new AtomicReference<>();
            final AtomicReference<SendBirdException> error = new AtomicReference<>();
            final AtomicBoolean hasMoreRef = new AtomicBoolean();

            try {
                load(lastSyncTs.get(), params, (updated, deleted, hasMore, token, e) -> {
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

                        updatedChannels.addAll(updated);
                        deletedChannelUrls.addAll(deleted);
                    } finally {
                        lock.countDown();
                    }
                });
                lock.await();

                while (hasMoreRef.get() && error.get() == null) {
                    CountDownLatch moreLock = new CountDownLatch(1);
                    more(tokenRef.get(), params, (updated, deleted, hasMore, token, e) -> {
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

                            updatedChannels.addAll(updated);
                            deletedChannelUrls.addAll(deleted);
                        } finally {
                            moreLock.countDown();
                        }
                    });
                    moreLock.await();
                }

                if (error.get() != null) {
                    throw error.get();
                }
                if (handler != null) {
                    handler.onResult(updatedChannels, deletedChannelUrls);
                }
            } catch (InterruptedException e) {
                Logger.e(e);
                if (handler != null) {
                    handler.onError(new SendBirdException("timeout", SendBirdError.ERR_REQUEST_FAILED));
                }
            } catch (SendBirdException e) {
                Logger.e(e);
                if (handler != null) {
                    handler.onError(e);
                }
            }
        });
    }

    private static void load(long lastSyncTs, GroupChannelChangeLogsParams params, SendBird.GetMyGroupChannelChangeLogsHandler handler) {
        SendBird.getMyGroupChannelChangeLogsByTimestampWithParams(lastSyncTs, params, handler);
    }

    private static void more(String token, GroupChannelChangeLogsParams params, SendBird.GetMyGroupChannelChangeLogsHandler handler) {
        SendBird.getMyGroupChannelChangeLogsByTokenWithParams(token, params, handler);
    }
}
