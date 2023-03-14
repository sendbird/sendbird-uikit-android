package com.sendbird.uikit.activities.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.DiffUtil;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.FileMessage;
import com.sendbird.android.message.UserMessage;
import com.sendbird.android.user.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.activities.viewholder.BaseViewHolder;
import com.sendbird.uikit.databinding.SbViewChannelPreviewBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.utils.ChannelUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * ChannelListAdapter provides a binding from a {@link GroupChannel} type data set to views that are displayed within a RecyclerView.
 */
public class ChannelListAdapter extends BaseAdapter<GroupChannel, BaseViewHolder<GroupChannel>> {
    @NonNull
    private final List<GroupChannel> channelList = new ArrayList<>();
    @NonNull
    private List<ChannelInfo> cachedChannelList = new ArrayList<>();
    @Nullable
    private OnItemClickListener<GroupChannel> listener;
    @Nullable
    private OnItemLongClickListener<GroupChannel> longClickListener;

    /**
     * Constructor
     */
    public ChannelListAdapter() {
        setHasStableIds(true);
    }

    /**
     * Constructor
     *
     * @param listener The listener performing when the {@link ChannelPreviewHolder} is clicked.
     */
    public ChannelListAdapter(@Nullable OnItemClickListener<GroupChannel> listener) {
        setHasStableIds(true);
        setOnItemClickListener(listener);
    }

    /**
     * Called when RecyclerView needs a new {@link BaseViewHolder<GroupChannel>} of the given type to represent
     * an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new {@link BaseViewHolder<GroupChannel>} that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(BaseViewHolder, int)
     */
    @NonNull
    @Override
    public BaseViewHolder<GroupChannel> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final TypedValue values = new TypedValue();
        parent.getContext().getTheme().resolveAttribute(R.attr.sb_component_list, values, true);
        final Context contextWrapper = new ContextThemeWrapper(parent.getContext(), values.resourceId);
        return new ChannelPreviewHolder(SbViewChannelPreviewBinding.inflate(LayoutInflater.from(contextWrapper), parent, false));
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link BaseViewHolder#itemView} to reflect the item at the given
     * position.
     *
     * @param holder The {@link BaseViewHolder<GroupChannel>} which should be updated to represent
     *               the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<GroupChannel> holder, int position) {
        final GroupChannel channel = getItem(position);
        holder.bind(channel);

        holder.itemView.setOnClickListener(v -> {
            int channelPosition = holder.getBindingAdapterPosition();
            if (channelPosition != NO_POSITION && listener != null) {
                listener.onItemClick(v, channelPosition, getItem(channelPosition));
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            int channelPosition = holder.getBindingAdapterPosition();
            if (channelPosition != NO_POSITION && longClickListener != null) {
                longClickListener.onItemLongClick(v, channelPosition, getItem(channelPosition));
                return true;
            }
            return false;
        });
    }

    /**
     * Register a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     *
     * @param listener The callback that will run
     */
    public void setOnItemClickListener(@Nullable OnItemClickListener<GroupChannel> listener) {
        this.listener = listener;
    }

    /**
     * Returns a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     *
     * @return {@code OnItemClickListener<GroupChannel>} to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     * @since 3.0.0
     */
    @Nullable
    public OnItemClickListener<GroupChannel> getOnItemClickListener() {
        return listener;
    }

    /**
     * Register a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked and held.
     *
     * @param listener The callback that will run
     */
    public void setOnItemLongClickListener(@Nullable OnItemLongClickListener<GroupChannel> listener) {
        this.longClickListener = listener;
    }

    /**
     * Returns a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked and held.
     *
     * @return {@code OnItemLongClickListener<GroupChannel>} to be invoked when the {@link BaseViewHolder#itemView} is clicked and held.
     * @since 3.0.0
     */
    @Nullable
    public OnItemLongClickListener<GroupChannel> getOnItemLongClickListener() {
        return longClickListener;
    }

    /**
     * Returns the {@link List<GroupChannel>} in the data set held by the adapter.
     *
     * @return The {@link List<GroupChannel>} in this adapter.
     */
    @Override
    @NonNull
    public List<GroupChannel> getItems() {
        return Collections.unmodifiableList(channelList);
    }

    /**
     * Returns the {@link GroupChannel} in the data set held by the adapter.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The {@link GroupChannel} to retrieve the position of in this adapter.
     */
    @NonNull
    @Override
    public GroupChannel getItem(int position) {
        return channelList.get(position);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return channelList.size();
    }

    /**
     * Return hashcode for the item at <code>position</code>.
     *
     * @param position Adapter position to query
     * @return the stable ID of the item at position
     */
    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    /**
     * Sets the {@link List<GroupChannel>} to be displayed.
     *
     * @param channelList list to be displayed
     */
    public void setItems(@NonNull List<GroupChannel> channelList) {
        final List<ChannelInfo> newChannelInfo = ChannelInfo.toChannelInfoList(channelList);
        final ChannelDiffCallback diffCallback = new ChannelDiffCallback(this.cachedChannelList, newChannelInfo);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.channelList.clear();
        this.channelList.addAll(channelList);
        this.cachedChannelList = newChannelInfo;
        diffResult.dispatchUpdatesTo(this);
    }

    private static class ChannelPreviewHolder extends BaseViewHolder<GroupChannel> {
        @NonNull
        private final SbViewChannelPreviewBinding binding;

        ChannelPreviewHolder(@NonNull SbViewChannelPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.binding.channelPreview.setUseTypingIndicator(SendbirdUIKit.isUsingChannelListTypingIndicators());
            this.binding.channelPreview.setUseMessageReceiptStatus(SendbirdUIKit.isUsingChannelListMessageReceiptStatus());
            this.binding.channelPreview.setUseUnreadMentionCount(SendbirdUIKit.isUsingUserMention());
        }

        @Override
        public void bind(@NonNull GroupChannel channel) {
            binding.channelPreview.drawChannel(channel);
        }
    }

    static class ChannelInfo {
        @NonNull
        private final String channelUrl;
        private final long createdAt;
        private final int memberCount;
        @Nullable
        private final BaseMessage lastMessage;
        @NonNull
        private final String channelName;
        @Nullable
        private final String coverImageUrl;
        private final int coverImageHash;
        @Nullable
        private final GroupChannel.PushTriggerOption pushTriggerOption;
        private final int unreadMessageCount;
        private final int unreadMentionCount;
        private final boolean isFrozen;
        @NonNull
        private List<User> typingMembers = new ArrayList<>();
        private int unReadMemberCount;
        private int unDeliveredMemberCount;

        ChannelInfo(@NonNull GroupChannel channel) {
            this.channelUrl = channel.getUrl();
            this.createdAt = channel.getCreatedAt();
            this.memberCount = channel.getMemberCount();
            this.lastMessage = channel.getLastMessage();
            this.channelName = channel.getName();
            this.coverImageUrl = channel.getCoverUrl();
            this.pushTriggerOption = channel.getMyPushTriggerOption();
            this.unreadMessageCount = channel.getUnreadMessageCount();
            this.unreadMentionCount = channel.getUnreadMentionCount();
            this.coverImageHash = toUrlsHash(channel);
            this.isFrozen = channel.isFrozen();
            if (SendbirdUIKit.isUsingChannelListTypingIndicators()) {
                this.typingMembers = channel.getTypingUsers();
            }
            if (SendbirdUIKit.isUsingChannelListMessageReceiptStatus()) {
                if (channel.getLastMessage() != null) {
                    this.unReadMemberCount = channel.getUnreadMemberCount(channel.getLastMessage());
                    this.unDeliveredMemberCount = channel.getUndeliveredMemberCount(channel.getLastMessage());
                }
            }
        }

        @NonNull
        String getChannelUrl() {
            return channelUrl;
        }

        long getCreatedAt() {
            return createdAt;
        }

        int getMemberCount() {
            return memberCount;
        }

        @Nullable
        BaseMessage getLastMessage() {
            return lastMessage;
        }

        @Nullable
        GroupChannel.PushTriggerOption getPushTriggerOption() {
            return pushTriggerOption;
        }

        int getUnreadMessageCount() {
            return unreadMessageCount;
        }

        int getUnreadMentionCount() {
            return unreadMentionCount;
        }

        @NonNull
        String getChannelName() {
            return channelName;
        }

        @Nullable
        String getCoverImageUrl() {
            return coverImageUrl;
        }

        int getCoverImageHash() {
            return coverImageHash;
        }

        boolean isFrozen() {
            return isFrozen;
        }

        int getUnDeliveredMemberCount() {
            return unDeliveredMemberCount;
        }

        int getUnReadMemberCount() {
            return unReadMemberCount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChannelInfo that = (ChannelInfo) o;

            if (createdAt != that.createdAt) return false;
            if (memberCount != that.memberCount) return false;
            if (coverImageHash != that.coverImageHash) return false;
            if (unreadMessageCount != that.unreadMessageCount) return false;
            if (unreadMentionCount != that.unreadMentionCount) return false;
            if (isFrozen != that.isFrozen) return false;

            if (SendbirdUIKit.isUsingChannelListMessageReceiptStatus()) {
                if (unReadMemberCount != that.unReadMemberCount) return false;
                if (unDeliveredMemberCount != that.unDeliveredMemberCount) return false;
            }
            if (!channelUrl.equals(that.channelUrl)) return false;
            if (!Objects.equals(lastMessage, that.lastMessage))
                return false;
            if (!channelName.equals(that.channelName)) return false;
            if (!Objects.equals(coverImageUrl, that.coverImageUrl))
                return false;
            if (pushTriggerOption != that.pushTriggerOption) return false;

            if (lastMessage != null && that.getLastMessage() != null) {
                if (lastMessage instanceof UserMessage) {
                    if (!lastMessage.getMessage().equals(that.getLastMessage().getMessage())) {
                        return false;
                    }
                } else if (lastMessage instanceof FileMessage) {
                    if (!((FileMessage) lastMessage).getName().equals(((FileMessage)that.getLastMessage()).getName())) {
                        return false;
                    }
                }
            }
            if (SendbirdUIKit.isUsingChannelListTypingIndicators()) {
                return typingMembers.equals(that.typingMembers);
            }
            return true;
        }

        @Override
        public int hashCode() {
            int result = channelUrl.hashCode();
            result = 31 * result + (int) (createdAt ^ (createdAt >>> 32));
            result = 31 * result + memberCount;
            result = 31 * result + (lastMessage != null ? lastMessage.hashCode() : 0);
            result = 31 * result + channelName.hashCode();
            result = 31 * result + (coverImageUrl != null ? coverImageUrl.hashCode() : 0);
            result = 31 * result + coverImageHash;
            result = 31 * result + (pushTriggerOption != null ? pushTriggerOption.hashCode() : 0);
            result = 31 * result + unreadMessageCount;
            result = 31 * result + unreadMentionCount;
            result = 31 * result + (isFrozen ? 1 : 0);

            if (SendbirdUIKit.isUsingChannelListTypingIndicators()) {
                result = 31 * result + typingMembers.hashCode();
            }

            if (SendbirdUIKit.isUsingChannelListMessageReceiptStatus()) {
                result = 31 * result + unReadMemberCount;
                result = 31 * result + unDeliveredMemberCount;
            }
            return result;
        }

        @NonNull
        @Override
        public String toString() {
            return "ChannelInfo{" +
                    "channelUrl='" + channelUrl + '\'' +
                    ", createdAt=" + createdAt +
                    ", memberCount=" + memberCount +
                    ", lastMessage=" + lastMessage +
                    ", channelName='" + channelName + '\'' +
                    ", coverImageUrl='" + coverImageUrl + '\'' +
                    ", coverImageHash=" + coverImageHash +
                    ", pushTriggerOption=" + pushTriggerOption +
                    ", unreadMessageCount=" + unreadMessageCount +
                    ", unreadMentionCount=" + unreadMentionCount +
                    ", isFrozen=" + isFrozen +
                    ", typingMembers=" + typingMembers +
                    ", unReadMemberCount=" + unReadMemberCount +
                    ", unDeliveredMemberCount=" + unDeliveredMemberCount +
                    '}';
        }

        @NonNull
        static List<ChannelInfo> toChannelInfoList(@NonNull List<GroupChannel> channelList) {
            List<ChannelInfo> results = new ArrayList<>();
            for (GroupChannel channel : channelList) {
                results.add(new ChannelInfo(channel));
            }
            return results;
        }

        static int toUrlsHash(@NonNull GroupChannel channel) {
            List<String> urls = ChannelUtils.makeProfileUrlsFromChannel(channel);
            StringBuilder imageUrlSum = new StringBuilder();
            for (String url : urls) {
                imageUrlSum.append(url);
            }
            return imageUrlSum.toString().hashCode();
        }
    }
}
