package com.sendbird.uikit.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.log.Logger;
import com.sendbird.uikit.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class MessageList {
    enum Order {
        ASC,
        DESC,
        ;
    }

    private final Order order;
    @NonNull
    private final TreeSet<BaseMessage> messages;
    private final Map<String, BaseMessage> timelineMap = new ConcurrentHashMap<>();

    private static BaseMessage createTimelineMessage(@NonNull BaseMessage anchorMessage) {
        return new TimelineMessage(anchorMessage);
    }

    public MessageList() {
        this(MessageList.Order.DESC);
    }

    public MessageList(@NonNull final Order order) {
        this.order = order;
        this.messages = new TreeSet<>((o1, o2) -> {
            if (o1.getCreatedAt() > o2.getCreatedAt()) {
                return order == Order.DESC ? -1 : 1;
            } else if (o1.getCreatedAt() < o2.getCreatedAt()) {
                return order == Order.DESC ? 1 : -1;
            }
            return 0;
        });
    }

    public List<BaseMessage> toList() {
        return new ArrayList<>(messages);
    }

    public void clear() {
        messages.clear();
        timelineMap.clear();
    }

    public int size() {
        return messages.size();
    }

    public synchronized void add(@NonNull BaseMessage message) {
        Logger.d(">> MessageList::addAll()");

        final long createdAt = message.getCreatedAt();
        final String dateStr = DateUtils.getDateString(createdAt);
        BaseMessage timeline = timelineMap.get(dateStr);
        // create new timeline message if not exists
        if (timeline == null) {
            timeline = createTimelineMessage(message);
            messages.add(timeline);
            timelineMap.put(dateStr, timeline);

            messages.remove(message);
            messages.add(BaseMessage.clone(message));
            return;
        }

        // remove previous timeline message if it exists.
        final long timelineCreatedAt = timeline.getCreatedAt();
        if (timelineCreatedAt > createdAt) {
            messages.remove(timeline);
            final BaseMessage newTimeline = createTimelineMessage(message);
            timelineMap.put(dateStr, newTimeline);
            messages.add(newTimeline);
        }

        messages.remove(message);
        messages.add(BaseMessage.clone(message));
    }

    public void addAll(@NonNull List<BaseMessage> messages) {
        Logger.d(">> MessageList::addAll()");
        if (messages.isEmpty()) return;

        for (BaseMessage message : messages) {
            add(message);
        }
    }

    public synchronized boolean delete(@NonNull BaseMessage message) {
        Logger.d(">> MessageList::deleteMessage()");

        boolean removed = messages.remove(message);
        if (removed) {
            final long createdAt = message.getCreatedAt();
            final String dateStr = DateUtils.getDateString(createdAt);
            final BaseMessage timeline = timelineMap.get(dateStr);
            if (timeline == null) return true;

            // check below item.
            final BaseMessage lower = messages.lower(message);
            if (lower != null && DateUtils.hasSameDate(createdAt, lower.getCreatedAt())) {
                return true;
            }

            // check above item.
            final BaseMessage higer = messages.higher(message);
            if (higer != null && DateUtils.hasSameDate(createdAt, higer.getCreatedAt())) {
                if (!timeline.equals(higer)) {
                    return true;
                }
            }

            if (timelineMap.remove(dateStr) != null) {
                messages.remove(timeline);
            }
        }
        return removed;
    }

    public void deleteAll(@NonNull List<BaseMessage> messages) {
        Logger.d(">> MessageList::deleteAllMessages() size = %s", messages.size());
        if (messages.isEmpty()) return;

        for (BaseMessage message : messages) {
            delete(message);
        }
    }

    public synchronized BaseMessage deleteByMessageId(long msgId) {
        BaseMessage removedMessage = null;
        if (messages.size() > 0) {
            for (BaseMessage message : messages) {
                if (message.getMessageId() == msgId) {
                    removedMessage = message;
                    messages.remove(message);
                    break;
                }
            }
        }
        return removedMessage;
    }

    public boolean deleteAllByRequestId(@NonNull List<BaseMessage> messages) {
        Logger.d(">> MessageList::deleteAllByRequestId() size = %s", messages.size());
        boolean deleted = false;
        if (messages.isEmpty()) return false;

        for (BaseMessage message : messages) {
            BaseMessage removedMessage = deleteByRequestId(message.getRequestId());
            if (removedMessage != null && !deleted) {
                deleted = true;
            }
        }
        return deleted;
    }

    public synchronized BaseMessage deleteByRequestId(@NonNull String requestId) {
        BaseMessage removedMessage = null;
        if (messages.size() > 0) {
            for (BaseMessage message : messages) {
                if (message.getRequestId().equals(requestId)) {
                    removedMessage = message;
                    messages.remove(message);
                    break;
                }
            }
        }
        return removedMessage;
    }

    public synchronized void update(@NonNull BaseMessage message) {
        Logger.d(">> MessageList::updateMessage()");
        messages.remove(message);
        messages.add(BaseMessage.clone(message));
    }

    public void updateAll(@NonNull List<BaseMessage> messages) {
        Logger.d(">> MessageList::updateAllMessages() size=%s", messages.size());
        if (messages.isEmpty()) return;

        for (BaseMessage message : messages) {
            update(message);
        }
    }

    /**
     * @return the latest message.
     */
    @Nullable
    public BaseMessage getLatestMessage() {
        if (messages.isEmpty()) return null;
        return order == Order.DESC ? messages.first() : messages.last();
    }

    /**
     * @return the oldest message.
     */
    @Nullable
    public BaseMessage getOldestMessage() {
        if (messages.isEmpty()) return null;
        return order == Order.DESC ? messages.last() : messages.first();
    }

    public synchronized BaseMessage getById(long messageId) {
        for (BaseMessage message : messages) {
            if (message.getMessageId() == messageId) {
                return message;
            }
        }
        return null;
    }

    public synchronized List<BaseMessage> getByCreatedAt(long createdAt) {
        if (createdAt == 0L) return Collections.emptyList();

        final List<BaseMessage> results = new ArrayList<>();
        for (BaseMessage message : messages) {
            if (message.getCreatedAt() == createdAt) {
                results.add(message);
            }
        }
        return results;
    }
}
