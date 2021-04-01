package com.sendbird.uikit.activities.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.Member;
import com.sendbird.android.User;
import com.sendbird.uikit.activities.viewholder.BaseViewHolder;
import com.sendbird.uikit.databinding.SbViewMemberPreviewBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.widgets.MemberPreview;

import java.util.Collections;
import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

/**
 * Adapters provide a binding from a {@link User} set to views that are displayed
 * within a {@link RecyclerView}.
 */
public class UserTypeListAdapter extends BaseAdapter<User, BaseViewHolder<User>> {
    private List<User> users;
    private OnItemClickListener<User> listener;
    private OnItemLongClickListener<User> longClickListener;
    private OnItemClickListener<User> actionItemClickListener;
    private Member.Role myRole = Member.Role.NONE;
    private OnItemClickListener<User> profileClickListener;


    /**
     * Called when RecyclerView needs a new {@link BaseViewHolder <User>} of the given type to represent
     * an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new {@link BaseViewHolder<User>} that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(BaseViewHolder, int)
     */
    @NonNull
    @Override
    public BaseViewHolder<User> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserPreviewHolder(SbViewMemberPreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<User> holder, int position) {
        holder.bind(getItem(position));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return users == null ? 0 : users.size();
    }

    /**
     * Returns the {@link User} in the data set held by the adapter.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The {@link User} to retrieve the position of in this adapter.
     */
    @Override
    public User getItem(int position) {
        return users != null ? users.get(position) : null;
    }

    /**
     * Returns the {@link List<User>} in the data set held by the adapter.
     *
     * @return The {@link List<User>} in this adapter.
     */
    @Override
    public List<User> getItems() {
        return users != null ? Collections.unmodifiableList(users) : null;
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
     * Register a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     *
     * @param listener The callback that will run
     */
    public void setOnItemClickListener(@Nullable OnItemClickListener<User> listener) {
        this.listener = listener;
    }

    /**
     * Register a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked and held.
     *
     * @param listener The callback that will run
     */
    public void setOnItemLongClickListener(@Nullable OnItemLongClickListener<User> listener) {
        this.longClickListener = listener;
    }

    public void setOnActionItemClickListener(@Nullable OnItemClickListener<User> listener) {
        this.actionItemClickListener = listener;
    }

    /**
     * Register a callback to be invoked when the profile view is clicked.
     *
     * @param profileClickListener The callback that will run
     * @since 1.2.2
     */
    public void setOnProfileClickListener(OnItemClickListener<User> profileClickListener) {
        this.profileClickListener = profileClickListener;
    }


    /**
     * Sets the {@link List<User>} to be displayed.
     *
     * @param userList list to be displayed
     */
    public void setItems(List<User> userList, Member.Role myRole) {
        this.users = userList;
        this.myRole = myRole;
        notifyDataSetChanged();
    }

    private class UserPreviewHolder extends BaseViewHolder<User> {
        private final SbViewMemberPreviewBinding binding;

        UserPreviewHolder(@NonNull SbViewMemberPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.memberViewHolder.setOnClickListener(v -> {
                int userPosition = getAdapterPosition();
                if (userPosition != NO_POSITION && listener != null) {
                    User user = getItem(userPosition);
                    listener.onItemClick(v, userPosition, user);
                }
            });

            binding.memberViewHolder.setOnLongClickListener(v -> {
                int userPosition = getAdapterPosition();
                if (userPosition != NO_POSITION && longClickListener != null) {
                    longClickListener.onItemLongClick(v, userPosition, getItem(userPosition));
                    return true;
                }
                return false;
            });

            binding.memberViewHolder.setOnActionMenuClickListener(v -> {
                int userPosition = getAdapterPosition();
                if (userPosition != NO_POSITION && actionItemClickListener != null) {
                    User user = getItem(userPosition);
                    actionItemClickListener.onItemClick(v, userPosition, user);
                }
            });

            binding.memberViewHolder.setOnProfileClickListener(v -> {
                int userPosition = getAdapterPosition();
                if (userPosition != NO_POSITION && profileClickListener != null) {
                    User user = getItem(userPosition);
                    profileClickListener.onItemClick(v, userPosition, user);
                }
            });
        }

        @Override
        public void bind(User user) {
            binding.memberViewHolder.useActionMenu(myRole == Member.Role.OPERATOR && actionItemClickListener != null);
            MemberPreview.drawMemberFromUser(binding.memberViewHolder, user);
            binding.executePendingBindings();
        }
    }
}
