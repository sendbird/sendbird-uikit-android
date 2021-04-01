package com.sendbird.uikit.activities.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.GroupChannel;
import com.sendbird.uikit.activities.viewholder.BaseViewHolder;
import com.sendbird.uikit.databinding.SbViewChannelPreviewBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.utils.ChannelUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

/**
 * Adapters provide a binding from a {@link GroupChannel} set to views that are displayed
 * within a {@link RecyclerView}.
 */
public class ChannelListAdapter extends BaseAdapter<GroupChannel, BaseViewHolder<GroupChannel>> {
    private final List<GroupChannel> channelList = new ArrayList<>();
    private List<ChannelInfo> cachedChannelList = new ArrayList<>();
    private OnItemClickListener<GroupChannel> listener;
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
    public ChannelListAdapter(OnItemClickListener<GroupChannel> listener) {
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
        return new ChannelPreviewHolder(SbViewChannelPreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
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
            int channelPosition = holder.getAdapterPosition();
            if (channelPosition != NO_POSITION && listener != null) {
                listener.onItemClick(v, channelPosition, getItem(channelPosition));
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            int channelPosition = holder.getAdapterPosition();
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
     * Register a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked and held.
     *
     * @param listener The callback that will run
     */
    public void setOnItemLongClickListener(@Nullable OnItemLongClickListener<GroupChannel> listener) {
        this.longClickListener = listener;
    }

    /**
     * Returns the {@link List<GroupChannel>} in the data set held by the adapter.
     *
     * @return The {@link List<GroupChannel>} in this adapter.
     */
    @Override
    public List<GroupChannel> getItems() {
        return channelList != null ? Collections.unmodifiableList(channelList) : null;
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
        return channelList == null ? 0 : channelList.size();
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
    public void setItems(List<GroupChannel> channelList) {
        final ChannelDiffCallback diffCallback = new ChannelDiffCallback(this.cachedChannelList, channelList);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.channelList.clear();
        this.channelList.addAll(channelList);
        this.cachedChannelList = ChannelInfo.toChannelInfoList(channelList);
        diffResult.dispatchUpdatesTo(this);
    }

    private static class ChannelPreviewHolder extends BaseViewHolder<GroupChannel> {

        private final SbViewChannelPreviewBinding binding;

        ChannelPreviewHolder(SbViewChannelPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void bind(GroupChannel channel) {
            binding.setChannel(channel);
            binding.executePendingBindings();
        }
    }

    static class ChannelInfo {
        private final String channelUrl;
        private final long createdAt;
        private final int memberCount;
        private final String lastMessage;
        private final String channelName;
        private final String coverImageUrl;
        private final int coverImageHash;
        private final GroupChannel.PushTriggerOption pushTriggerOption;
        private final int unreadMessageCount;
        private final boolean isFrozen;
        ChannelInfo(@NonNull GroupChannel channel) {
            this.channelUrl = channel.getUrl();
            this.createdAt = channel.getCreatedAt();
            this.memberCount = channel.getMemberCount();
            this.lastMessage = channel.getLastMessage() != null ? channel.getLastMessage().getMessage() : "";
            this.channelName = channel.getName();
            this.coverImageUrl = channel.getCoverUrl();
            this.pushTriggerOption = channel.getMyPushTriggerOption();
            this.unreadMessageCount = channel.getUnreadMessageCount();
            this.coverImageHash = toUrlsHash(channel);
            this.isFrozen = channel.isFrozen();
        }

        public String getChannelUrl() {
            return channelUrl;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        int getMemberCount() {
            return memberCount;
        }

        String getLastMessage() {
            return lastMessage;
        }

        GroupChannel.PushTriggerOption getPushTriggerOption() {
            return pushTriggerOption;
        }

        int getUnreadMessageCount() {
            return unreadMessageCount;
        }

        String getChannelName() {
            return channelName;
        }

        String getCoverImageUrl() {
            return coverImageUrl;
        }

        int getCoverImageHash() {
            return coverImageHash;
        }

        public boolean isFrozen() {
            return isFrozen;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChannelInfo that = (ChannelInfo) o;

            if (createdAt != that.createdAt) return false;
            if (memberCount != that.memberCount) return false;
            if (unreadMessageCount != that.unreadMessageCount) return false;
            if (coverImageHash != that.coverImageHash) return false;
            if (channelUrl != null && channelUrl.equals(that.channelUrl)) return false;
            if (lastMessage != null && lastMessage.equals(that.lastMessage)) return false;
            if (channelName != null && channelName.equals(that.channelName)) return false;
            if (coverImageUrl != null && coverImageUrl.equals(that.coverImageUrl)) return false;
            if (isFrozen != that.isFrozen) return false;
            return pushTriggerOption == that.pushTriggerOption;
        }

        @Override
        public int hashCode() {
            int result = channelUrl != null ? channelUrl.hashCode() : 0;
            result = 31 * result + (int) (createdAt ^ (createdAt >>> 32));
            result = 31 * result + memberCount;
            result = 31 * result + coverImageHash;
            result = 31 * result + (lastMessage != null ? lastMessage.hashCode() : 0);
            result = 31 * result + (channelName != null ? channelName.hashCode() : 0);
            result = 31 * result + (coverImageUrl != null ? coverImageUrl.hashCode() : 0);
            result = 31 * result + (pushTriggerOption != null ? pushTriggerOption.hashCode() : 0);
            result = 31 * result + unreadMessageCount;
            result = 31 * result + (isFrozen ? 1 : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ChannelInfo{" +
                    "channelUrl='" + channelUrl + '\'' +
                    ", createdAt=" + createdAt +
                    ", coverImageHash=" + coverImageHash +
                    ", memberCount=" + memberCount +
                    ", lastMessage='" + lastMessage + '\'' +
                    ", channelName='" + channelName + '\'' +
                    ", coverImageUrl='" + coverImageUrl + '\'' +
                    ", pushTriggerOption=" + pushTriggerOption +
                    ", unreadMessageCount=" + unreadMessageCount +
                    ", isFrozen=" + isFrozen +
                    '}';
        }

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
