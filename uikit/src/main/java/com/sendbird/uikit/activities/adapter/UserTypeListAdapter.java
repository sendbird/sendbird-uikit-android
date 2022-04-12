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

import com.sendbird.android.Member;
import com.sendbird.android.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.activities.viewholder.BaseViewHolder;
import com.sendbird.uikit.databinding.SbViewUserPreviewBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.widgets.UserPreview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adapters provides a binding from a {@link User} set to views that are displayed within a RecyclerView.
 */
public class UserTypeListAdapter<T extends User> extends BaseAdapter<T, BaseViewHolder<T>> {
    @NonNull
    final private List<T> users = new ArrayList<>();
    @Nullable
    private OnItemClickListener<T> listener;
    @Nullable
    private OnItemLongClickListener<T> longClickListener;
    @Nullable
    private OnItemClickListener<T> actionItemClickListener;
    @NonNull
    private Member.Role myRole = Member.Role.NONE;
    @Nullable
    private OnItemClickListener<T> profileClickListener;

    /**
     * Called when RecyclerView needs a new {@link BaseViewHolder <T>} of the given type to represent
     * an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new {@link BaseViewHolder<T>} that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(BaseViewHolder, int)
     */
    @NonNull
    @Override
    public BaseViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final TypedValue values = new TypedValue();
        parent.getContext().getTheme().resolveAttribute(R.attr.sb_component_list, values, true);
        final Context contextWrapper = new ContextThemeWrapper(parent.getContext(), values.resourceId);
        return new UserPreviewHolder(SbViewUserPreviewBinding.inflate(LayoutInflater.from(contextWrapper), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<T> holder, int position) {
        holder.bind(getItem(position));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * Returns the {@link User} in the data set held by the adapter.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The {@link User} to retrieve the position of in this adapter.
     */
    @Override
    @NonNull
    public T getItem(int position) {
        return users.get(position);
    }

    /**
     * Returns the {@link List<T>} in the data set held by the adapter.
     *
     * @return The {@link List<T>} in this adapter.
     */
    @Override
    @NonNull
    public List<T> getItems() {
        return Collections.unmodifiableList(users);
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
    public void setOnItemClickListener(@Nullable OnItemClickListener<T> listener) {
        this.listener = listener;
    }

    /**
     * Returns a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     *
     * @return {@code OnItemClickListener} to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     * @since 3.0.0
     */
    @Nullable
    public OnItemClickListener<T> getOnItemClickListener() {
        return listener;
    }

    /**
     * Register a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked and held.
     *
     * @param listener The callback that will run
     */
    public void setOnItemLongClickListener(@Nullable OnItemLongClickListener<T> listener) {
        this.longClickListener = listener;
    }

    /**
     * Returns a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked and held.
     *
     * @return {@code OnItemLongClickListener} to be invoked when the {@link BaseViewHolder#itemView} is clicked and held.
     * @since 3.0.0
     */
    @Nullable
    public OnItemLongClickListener<T> getOnItemLongClickListener() {
        return longClickListener;
    }

    /**
     * Register a callback to be invoked when the action view is clicked.
     *
     * @param listener The callback that will run
     */
    public void setOnActionItemClickListener(@Nullable OnItemClickListener<T> listener) {
        this.actionItemClickListener = listener;
    }

    /**
     * Returns a callback to be invoked when the action view is clicked.
     *
     * @return {@code OnItemClickListener} to be invoked when the action view is clicked.
     * @since 3.0.0
     */
    @Nullable
    public OnItemClickListener<T> getOnActionItemClickListener() {
        return actionItemClickListener;
    }

    /**
     * Register a callback to be invoked when the profile view is clicked.
     *
     * @param profileClickListener The callback that will run
     * @since 1.2.2
     */
    public void setOnProfileClickListener(@Nullable OnItemClickListener<T> profileClickListener) {
        this.profileClickListener = profileClickListener;
    }

    /**
     * Returns a callback to be invoked when the profile view is clicked.
     *
     * @return {@code OnItemClickListener} to be invoked when the profile view is clicked.
     * @since 3.0.0
     */
    @Nullable
    public OnItemClickListener<T> getOnProfileClickListener() {
        return profileClickListener;
    }

    /**
     * Sets the {@link List<T>} to be displayed.
     *
     * @param userList list to be displayed
     */
    public void setItems(@NonNull List<T> userList, @NonNull Member.Role myRole) {
        final UserTypeDiffCallback<T> diffCallback = new UserTypeDiffCallback<>(this.users, userList, this.myRole, myRole);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.users.clear();
        this.users.addAll(userList);
        this.myRole = myRole;
        diffResult.dispatchUpdatesTo(this);
    }

    private class UserPreviewHolder extends BaseViewHolder<T> {
        @NonNull
        private final SbViewUserPreviewBinding binding;

        UserPreviewHolder(@NonNull SbViewUserPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.userViewHolder.setOnClickListener(v -> {
                int userPosition = getAdapterPosition();
                if (userPosition != NO_POSITION && listener != null) {
                    listener.onItemClick(v, userPosition, getItem(userPosition));
                }
            });

            binding.userViewHolder.setOnLongClickListener(v -> {
                int userPosition = getAdapterPosition();
                if (userPosition != NO_POSITION && longClickListener != null) {
                    longClickListener.onItemLongClick(v, userPosition, getItem(userPosition));
                    return true;
                }
                return false;
            });

            binding.userViewHolder.setOnActionMenuClickListener(v -> {
                int userPosition = getAdapterPosition();
                if (userPosition != NO_POSITION && actionItemClickListener != null) {
                    actionItemClickListener.onItemClick(v, userPosition, getItem(userPosition));
                }
            });

            binding.userViewHolder.setOnProfileClickListener(v -> {
                int userPosition = getAdapterPosition();
                if (userPosition != NO_POSITION && profileClickListener != null) {
                    profileClickListener.onItemClick(v, userPosition, getItem(userPosition));
                }
            });
        }

        @Override
        public void bind(@NonNull T user) {
            binding.userViewHolder.useActionMenu(myRole == Member.Role.OPERATOR && actionItemClickListener != null);
            if (user instanceof Member) {
                UserPreview.drawMember(binding.userViewHolder, (Member) user);
            } else {
                UserPreview.drawMemberFromUser(binding.userViewHolder, user);
            }
        }
    }

    private static class UserTypeDiffCallback<T extends User> extends DiffUtil.Callback {
        @NonNull
        private final List<T> oldUserList;
        @NonNull
        private final List<T> newUserList;
        @NonNull
        private final Member.Role oldMyRole;
        @NonNull
        private final Member.Role newMyRole;

        UserTypeDiffCallback(@NonNull List<T> oldUserList, @NonNull List<T> newUserList, @NonNull Member.Role oldMyRole, @NonNull Member.Role newMyRole) {
            this.oldUserList = oldUserList;
            this.newUserList = newUserList;
            this.oldMyRole = oldMyRole;
            this.newMyRole = newMyRole;
        }

        @Override
        public int getOldListSize() {
            return oldUserList.size();
        }

        @Override
        public int getNewListSize() {
            return newUserList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            final T oldUser = oldUserList.get(oldItemPosition);
            final T newUser = newUserList.get(newItemPosition);

            return oldUser.equals(newUser) && oldMyRole.equals(newMyRole);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            final T oldUser = oldUserList.get(oldItemPosition);
            final T newUser = newUserList.get(newItemPosition);

            if (!areItemsTheSame(oldItemPosition, newItemPosition)) {
                return false;
            }

            final String oldNickname = oldUser.getNickname();
            final String newNickname = newUser.getNickname() != null ? newUser.getNickname() : "";
            if (!newNickname.equals(oldNickname)) {
                return false;
            }

            final String oldProfileUrl = oldUser.getProfileUrl();
            final String newProfileUrl = newUser.getProfileUrl() != null ? newUser.getProfileUrl() : "";

            if (newUser instanceof Member && oldUser instanceof Member) {
                final Member oldMember = (Member) oldUser;
                final Member newMember = (Member) newUser;
                if (oldMember.isMuted() != newMember.isMuted()) {
                    return false;
                }

                if (oldMember.getRole() != newMember.getRole()) {
                    return false;
                }
            }

            return newProfileUrl.equals(oldProfileUrl);
        }
    }
}
