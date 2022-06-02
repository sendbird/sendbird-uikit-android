package com.sendbird.uikit.customsample.groupchannel.components.adapters;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sendbird.uikit.activities.adapter.InviteUserListAdapter;
import com.sendbird.uikit.activities.viewholder.BaseViewHolder;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.interfaces.UserInfo;

/**
 * Implements the customized <code>InviteUserListAdapter</code> to adapt the customized user list items.
 */
public class CustomInviteUserListAdapter extends InviteUserListAdapter {
    @NonNull
    @Override
    public BaseViewHolder<UserInfo> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_custom_user_holder, parent, false);
        return new UserInfoViewHolder(view);
    }

    private class UserInfoViewHolder extends BaseViewHolder<UserInfo> {
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
                    final UserInfo item = getItem(userPosition);
                    boolean isSelected = isSelected(item);
                    checkBox.setChecked(!isSelected);
                }
            });

            checkBox.setOnCheckedChangeListener((buttonView, isSelected) -> {
                int userPosition = getAdapterPosition();
                if (userPosition != NO_POSITION) {
                    final UserInfo userInfo = getItem(userPosition);

                    if (isSelected && !isDisabled(userInfo)) {
                        selectedUserIdList.add(userInfo.getUserId());
                    } else {
                        selectedUserIdList.remove(userInfo.getUserId());
                    }

                    if (userSelectChangedListener != null) {
                        userSelectChangedListener.onUserSelectChanged(selectedUserIdList, !isSelected);
                    }
                }
            });
        }

        @Override
        public void bind(@NonNull UserInfo userInfo) {
            nickname.setText(userInfo.getNickname());
            itemView.setEnabled(!isDisabled(userInfo));
            checkBox.setEnabled(!isDisabled(userInfo));
            checkBox.setChecked(isSelected(userInfo) || isDisabled(userInfo));
        }
    }
}

