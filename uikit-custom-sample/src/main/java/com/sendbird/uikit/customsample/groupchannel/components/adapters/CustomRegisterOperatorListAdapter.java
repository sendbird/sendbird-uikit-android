package com.sendbird.uikit.customsample.groupchannel.components.adapters;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sendbird.android.user.Member;
import com.sendbird.uikit.activities.adapter.RegisterOperatorListAdapter;
import com.sendbird.uikit.activities.viewholder.BaseViewHolder;
import com.sendbird.uikit.customsample.R;

/**
 * Implements the customized <code>RegisterOperatorListAdapter</code> to adapt the register as operators list items.
 */
public class CustomRegisterOperatorListAdapter extends RegisterOperatorListAdapter {
    @NonNull
    @Override
    public BaseViewHolder<Member> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_custom_user_holder, parent, false);
        return new UserInfoViewHolder(view);
    }

    private class UserInfoViewHolder extends BaseViewHolder<Member> {
        private final TextView nickname;
        private final CheckBox checkBox;

        /**
         * Constructor
         *
         * @param itemView View to be displayed.
         */
        public UserInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            nickname = itemView.findViewById(R.id.tvNickname);
            checkBox = itemView.findViewById(R.id.cbUser);

            itemView.setOnClickListener(v -> {
                int userPosition = getAdapterPosition();
                if (userPosition != NO_POSITION) {
                    final Member item = getItem(userPosition);
                    boolean isSelected = isSelected(item);
                    checkBox.setChecked(!isSelected);
                }
            });

            checkBox.setOnCheckedChangeListener((buttonView, isSelected) -> {
                int userPosition = getAdapterPosition();
                if (userPosition != NO_POSITION) {
                    final Member member = getItem(userPosition);

                    if (isSelected && !isDisabled(member)) {
                        selectedUserIdList.add(member.getUserId());
                    } else {
                        selectedUserIdList.remove(member.getUserId());
                    }

                    if (userSelectChangedListener != null) {
                        userSelectChangedListener.onUserSelectChanged(selectedUserIdList, !isSelected);
                    }
                }
            });
        }

        @Override
        public void bind(@NonNull Member member) {
            nickname.setText(member.getNickname());
            itemView.setEnabled(!isDisabled(member));
            checkBox.setEnabled(!isDisabled(member));
            checkBox.setChecked(isSelected(member) || isDisabled(member));
        }
    }
}

