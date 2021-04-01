package com.sendbird.uikit.activities.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.uikit.activities.viewholder.BaseViewHolder;
import com.sendbird.uikit.databinding.SbViewUserPreviewBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.interfaces.OnUserSelectChangedListener;
import com.sendbird.uikit.interfaces.UserInfo;
import com.sendbird.uikit.log.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

/**
 * Adapters provide a binding from a {@link UserInfo} set to views that are displayed
 * within a {@link RecyclerView}.
 */
public class UserListAdapter extends BaseAdapter<UserInfo, BaseViewHolder<UserInfo>> {
    private List<UserInfo> userList;
    private List<String> disabledUserList;
    private final List<String> selectedUserList = new ArrayList<>();
    private OnItemClickListener<UserInfo> listener;
    private OnItemLongClickListener<UserInfo> longClickListener;
    private OnUserSelectChangedListener userSelectChangedListener;

    /**
     * Constructor
     */
    public UserListAdapter() {
        setHasStableIds(true);
    }

    /**
     * Constructor
     *
     * @param listener The listener performing when the {@link BaseViewHolder<UserInfo>} is clicked.
     */
    public UserListAdapter(OnItemClickListener<UserInfo> listener) {
        setHasStableIds(true);
        this.listener = listener;
    }

    /**
     * Called when RecyclerView needs a new {@link BaseViewHolder<UserInfo>} of the given type to represent
     * an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new {@link BaseViewHolder<UserInfo>} that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(BaseViewHolder, int)
     */
    @NonNull
    @Override
    public BaseViewHolder<UserInfo> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserPreviewHolder(SbViewUserPreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link BaseViewHolder#itemView} to reflect the item at the given
     * position.
     *
     * @param holder   The {@link BaseViewHolder<UserInfo>} which should be updated to represent
     *                 the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<UserInfo> holder, int position) {
        UserInfo userInfo = getItem(position);
        holder.bind(userInfo);
    }

    /**
     * Returns the {@link UserInfo} in the data set held by the adapter.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The {@link UserInfo} to retrieve the position of in this adapter.
     */
    @Override
    public UserInfo getItem(int position) {
        return userList.get(position);
    }

    /**
     * Returns the {@link List<UserInfo>} in the data set held by the adapter.
     *
     * @return The {@link List<UserInfo>} in this adapter.
     */
    @Override
    public List<UserInfo> getItems() {
        return userList != null ? Collections.unmodifiableList(userList) : null;
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
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
     */
    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    /**
     * Sets the {@link List<UserInfo>} to be displayed.
     *
     * @param userList list to be displayed
     */
    public void setItems(List<UserInfo> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    /**
     * Register a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     *
     * @param listener The callback that will run
     */
    public void setOnItemClickListener(@Nullable OnItemClickListener<UserInfo> listener) {
        this.listener = listener;
    }

    /**
     * Register a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked and held.
     *
     * @param listener The callback that will run
     */
    public void setOnItemLongClickListener(@Nullable OnItemLongClickListener<UserInfo> listener) {
        this.longClickListener = listener;
    }

    /**
     * Register a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     *
     * @param userSelectChangedListener The callback that will run
     */
    public void setOnUserSelectChangedListener(OnUserSelectChangedListener userSelectChangedListener) {
        this.userSelectChangedListener = userSelectChangedListener;
    }

    /**
     * Sets the members who can't select in the member list.
     *
     * @param disabledUserList The members who can't select in the member list.
     */
    public void setDisabledUserList(List<String> disabledUserList) {
        this.disabledUserList = disabledUserList;
    }

    protected boolean isDisabled(UserInfo userInfo) {
        return disabledUserList != null && disabledUserList.contains(userInfo.getUserId());
    }

    public List<String> getSelectedUserList() {
        return selectedUserList;
    }

    private boolean isSelected(String userId) {
        return selectedUserList.contains(userId);
    }

    private class UserPreviewHolder extends BaseViewHolder<UserInfo> {
        private final SbViewUserPreviewBinding binding;

        UserPreviewHolder(@NonNull SbViewUserPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.userViewHolder.setOnClickListener(v -> {
                int userPosition = getAdapterPosition();
                if (userPosition != NO_POSITION) {
                    UserInfo userInfo = getItem(userPosition);
                    boolean isSelected = isSelected(userInfo.getUserId());
                    Logger.d("++ isSelected : %s, userName : %s", isSelected, userInfo.getNickname());
                    if (!isSelected) {
                        selectedUserList.add(userInfo.getUserId());
                    } else {
                        selectedUserList.remove(userInfo.getUserId());
                    }

                    if (listener != null) {
                        listener.onItemClick(v, userPosition, userInfo);
                    }
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

            binding.userViewHolder.setOnSelectedStateChangedListener((buttonView, isSelected) -> {
                int userPosition = getAdapterPosition();
                if (userPosition != NO_POSITION) {
                    if (userSelectChangedListener != null) {
                        userSelectChangedListener.onUserSelectChanged(selectedUserList, !isSelected);
                    }
                }
            });
        }

        @Override
        public void bind(UserInfo userInfo) {
            if (userInfo == null) {
                return;
            }

            binding.setUserInfo(userInfo);
            binding.setEnabled(!isDisabled(userInfo));
            binding.setSelected(isSelected(userInfo.getUserId()) || isDisabled(userInfo));
            binding.executePendingBindings();
        }
    }
}
