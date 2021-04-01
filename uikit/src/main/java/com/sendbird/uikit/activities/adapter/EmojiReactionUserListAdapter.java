package com.sendbird.uikit.activities.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.User;
import com.sendbird.uikit.activities.viewholder.BaseViewHolder;
import com.sendbird.uikit.databinding.SbViewEmojiReactionUserBinding;

import java.util.List;

/**
 * Adapters provide a binding from a {@link User} set to views that are displayed
 * within a {@link RecyclerView}.
 *
 * @since 1.1.0
 */
public class EmojiReactionUserListAdapter extends BaseAdapter<User, BaseViewHolder<User>> {
    private List<User> userList;

    /**
     * Constructor
     * @since 1.1.0
     */
    public EmojiReactionUserListAdapter() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param userList list to be displayed.
     * @since 1.1.0
     */
    public EmojiReactionUserListAdapter(List<User> userList) {
        setHasStableIds(true);
        this.userList = userList;
    }

    /**
     * Called when RecyclerView needs a new {@link BaseViewHolder<User>} of the given type to represent
     * an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new {@link BaseViewHolder<User>} that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(BaseViewHolder, int)
     * @since 1.1.0
     */
    @NonNull
    @Override
    public BaseViewHolder<User> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EmojiReactionUserViewHolder(SbViewEmojiReactionUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link BaseViewHolder#itemView} to reflect the item at the given
     * position.
     *
     * @param holder The {@link BaseViewHolder<User>} which should be updated to represent
     *               the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     * @since 1.1.0
     */
    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<User> holder, int position) {
        User userInfo = getItem(position);
        holder.bind(userInfo);
    }

    /**
     * Returns the {@link User} in the data set held by the adapter.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The {@link User} to retrieve the position of in this adapter.
     * @since 1.1.0
     */
    @Override
    public User getItem(int position) {
        if (userList == null) {
            return null;
        }
        return userList.get(position);
    }

    /**
     * Returns the {@link List<User>} in the data set held by the adapter.
     *
     * @return The {@link List<User>} in this adapter.
     * @since 1.1.0
     */
    @Override
    public List<User> getItems() {
        return userList;
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     * @since 1.1.0
     */
    @Override
    public int getItemCount() {
        return userList == null ? 0 : userList.size();
    }

    /**
     * Return hashcode for the item at <code>position</code>.
     *
     * @param position Adapter position to query
     * @return the stable ID of the item at position
     * @since 1.1.0
     */
    @Override
    public long getItemId(int position) {
        if (getItem(position) == null) {
            return -1;
        }
        return getItem(position).hashCode();
    }

    /**
     * Sets the {@link List<User>} to be displayed.
     *
     * @param userList list to be displayed
     * @since 1.1.0
     */
    public void setItems(List<User> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }


    private static class EmojiReactionUserViewHolder extends BaseViewHolder<User> {
        private final SbViewEmojiReactionUserBinding binding;

        EmojiReactionUserViewHolder(@NonNull SbViewEmojiReactionUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void bind(User user) {
            binding.setUser(user);
            binding.executePendingBindings();
        }
    }
}
