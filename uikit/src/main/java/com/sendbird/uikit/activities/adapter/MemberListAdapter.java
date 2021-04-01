package com.sendbird.uikit.activities.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.Member;
import com.sendbird.uikit.activities.viewholder.BaseViewHolder;
import com.sendbird.uikit.databinding.SbViewMemberPreviewBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.widgets.MemberPreview;

import java.util.Collections;
import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

/**
 * Adapters provide a binding from a {@link Member} set to views that are displayed
 * within a {@link RecyclerView}.
 */
public class MemberListAdapter extends BaseAdapter<Member, BaseViewHolder<Member>> {
    private List<Member> members;
    private OnItemClickListener<Member> listener;
    private OnItemLongClickListener<Member> longClickListener;
    private OnItemClickListener<Member> actionItemClickListener;
    private Member.Role myRole = Member.Role.NONE;
    private OnItemClickListener<Member> profileClickListener;

    /**
     * Called when RecyclerView needs a new {@link BaseViewHolder <Member>} of the given type to represent
     * an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new {@link BaseViewHolder<Member>} that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(BaseViewHolder, int)
     */
    @NonNull
    @Override
    public BaseViewHolder<Member> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MemberPreviewHolder(SbViewMemberPreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<Member> holder, int position) {
        holder.bind(getItem(position));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return members == null ? 0 : members.size();
    }

    /**
     * Returns the {@link Member} in the data set held by the adapter.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The {@link Member} to retrieve the position of in this adapter.
     */
    @Override
    public Member getItem(int position) {
        return members != null ? members.get(position) : null;
    }

    /**
     * Returns the {@link List<Member>} in the data set held by the adapter.
     *
     * @return The {@link List<Member>} in this adapter.
     */
    @Override
    public List<Member> getItems() {
        return members != null ? Collections.unmodifiableList(members) : null;
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
    public void setOnItemClickListener(@Nullable OnItemClickListener<Member> listener) {
        this.listener = listener;
    }

    /**
     * Register a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked and held.
     *
     * @param listener The callback that will run
     */
    public void setOnItemLongClickListener(@Nullable OnItemLongClickListener<Member> listener) {
        this.longClickListener = listener;
    }

    public void setOnActionItemClickListener(@Nullable OnItemClickListener<Member> listener) {
        this.actionItemClickListener = listener;
    }

    /**
     * Register a callback to be invoked when the profile view is clicked.
     *
     * @param profileClickListener The callback that will run
     * @since 1.2.2
     */
    public void setOnProfileClickListener(OnItemClickListener<Member> profileClickListener) {
        this.profileClickListener = profileClickListener;
    }

    /**
     * Sets the {@link List<Member>} to be displayed.
     *
     * @param memberList list to be displayed
     */
    public void setItems(List<Member> memberList, Member.Role myRole) {
        this.members = memberList;
        this.myRole = myRole;
        notifyDataSetChanged();
    }

    private class MemberPreviewHolder extends BaseViewHolder<Member> {
        private final SbViewMemberPreviewBinding binding;

        MemberPreviewHolder(@NonNull SbViewMemberPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.memberViewHolder.setOnClickListener(v -> {
                int userPosition = getAdapterPosition();
                if (userPosition != NO_POSITION && listener != null) {
                    Member member = getItem(userPosition);
                    listener.onItemClick(v, userPosition, member);
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
                    Member member = getItem(userPosition);
                    actionItemClickListener.onItemClick(v, userPosition, member);
                }
            });

            binding.memberViewHolder.setOnProfileClickListener(v -> {
                int userPosition = getAdapterPosition();
                if (userPosition != NO_POSITION && profileClickListener != null) {
                    Member member = getItem(userPosition);
                    profileClickListener.onItemClick(v, userPosition, member);
                }
            });
        }

        @Override
        public void bind(Member member) {
            binding.memberViewHolder.useActionMenu(myRole == Member.Role.OPERATOR && actionItemClickListener != null);
            MemberPreview.drawMember(binding.memberViewHolder, member);
            binding.executePendingBindings();
        }
    }
}
