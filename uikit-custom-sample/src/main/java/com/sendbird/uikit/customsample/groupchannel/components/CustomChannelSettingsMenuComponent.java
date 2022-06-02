package com.sendbird.uikit.customsample.groupchannel.components;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.modules.components.ChannelSettingsMenuComponent;

/**
 * Implements the customized <code>ChannelSettingsMenuComponent</code>.
 */
public class CustomChannelSettingsMenuComponent extends ChannelSettingsMenuComponent {
    private View view;

    public CustomChannelSettingsMenuComponent() {
        super();
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.view_custom_channel_settings_menu, parent, false);
        view.findViewById(R.id.tvModeration).setOnClickListener(v -> onMenuClicked(v, ChannelSettingsMenuComponent.Menu.MODERATIONS));
        view.findViewById(R.id.tvMemberList).setOnClickListener(v -> onMenuClicked(v, ChannelSettingsMenuComponent.Menu.MEMBERS));
        view.findViewById(R.id.tvLeaveChannel).setOnClickListener(this::showDeleteDialog);
        return view;
    }

    private void showDeleteDialog(@NonNull View v) {
        new AlertDialog.Builder(v.getContext())
                .setTitle(R.string.text_dialog_leave_channel_title)
                .setPositiveButton(R.string.sb_text_button_delete, (dialog, which) -> onMenuClicked(v, ChannelSettingsMenuComponent.Menu.LEAVE_CHANNEL))
                .setNegativeButton(R.string.sb_text_button_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void notifyChannelChanged(@NonNull GroupChannel channel) {
        super.notifyChannelChanged(channel);
        if (channel.getMyRole() == Member.Role.OPERATOR) {
            view.findViewById(R.id.moderationPanel).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.moderationPanel).setVisibility(View.GONE);
        }
    }
}
