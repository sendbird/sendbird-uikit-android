package com.sendbird.uikit.customsample.groupchannel.components.adapters;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.Member;
import com.sendbird.uikit.activities.adapter.MemberListAdapter;
import com.sendbird.uikit.activities.viewholder.BaseViewHolder;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.interfaces.OnItemClickListener;

import java.util.List;

/**
 * Implements the customized <code>MemberListAdapter</code> to adapt the customized member list items.
 */
public class CustomMemberListAdapter extends MemberListAdapter {
    private OnItemClickListener<Member> actionItemClickListener;
    private OnItemClickListener<Member> profileClickListener;
    @NonNull
    private Member.Role myRole = Member.Role.NONE;

    @NonNull
    @Override
    public BaseViewHolder<Member> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_custom_user_typed_holder, parent, false);
        return new MemberPreviewHolder(v);
    }

    @Override
    public void setOnActionItemClickListener(@Nullable OnItemClickListener<Member> listener) {
        this.actionItemClickListener = listener;
    }

    @Override
    public void setOnProfileClickListener(@Nullable OnItemClickListener<Member> profileClickListener) {
        this.profileClickListener = profileClickListener;
    }

    @Override
    public void setItems(@NonNull List<Member> userList, @NonNull Member.Role myRole) {
        super.setItems(userList, myRole);
        this.myRole = myRole;
    }

    private class MemberPreviewHolder extends BaseViewHolder<Member> {
        public MemberPreviewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.findViewById(R.id.ivAction).setOnClickListener(v -> {
                int userPosition = getAdapterPosition();
                if (userPosition != NO_POSITION && actionItemClickListener != null) {
                    actionItemClickListener.onItemClick(v, userPosition, getItem(userPosition));
                }
            });

            itemView.findViewById(R.id.ivProfile).setOnClickListener(v -> {
                int userPosition = getAdapterPosition();
                if (userPosition != NO_POSITION && profileClickListener != null) {
                    profileClickListener.onItemClick(v, userPosition, getItem(userPosition));
                }
            });
        }

        @Override
        public void bind(@NonNull Member user) {
            if (myRole == Member.Role.OPERATOR && actionItemClickListener != null) {
                itemView.findViewById(R.id.ivAction).setVisibility(View.VISIBLE);
            } else {
                itemView.findViewById(R.id.ivAction).setVisibility(View.GONE);
            }
            ((TextView) itemView.findViewById(R.id.tvNickname)).setText(user.getNickname());
        }
    }
}
