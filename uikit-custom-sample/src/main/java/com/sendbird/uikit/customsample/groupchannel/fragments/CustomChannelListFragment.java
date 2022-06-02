package com.sendbird.uikit.customsample.groupchannel.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelParams;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.groupchannel.GroupChannelMainActivity;
import com.sendbird.uikit.customsample.groupchannel.components.CustomChannelListHeaderComponent;
import com.sendbird.uikit.customsample.groupchannel.components.adapters.CustomChannelListAdapter;
import com.sendbird.uikit.fragments.ChannelListFragment;
import com.sendbird.uikit.model.ReadyStatus;
import com.sendbird.uikit.modules.ChannelListModule;
import com.sendbird.uikit.modules.components.ChannelListComponent;
import com.sendbird.uikit.modules.components.HeaderComponent;
import com.sendbird.uikit.vm.ChannelListViewModel;

/**
 * Implements the customized <code>ChannelListFragment</code>.
 */
public class CustomChannelListFragment extends ChannelListFragment {

    @NonNull
    @Override
    protected ChannelListModule onCreateModule(@NonNull Bundle args) {
        ChannelListModule module = super.onCreateModule(args);
        module.setHeaderComponent(new CustomChannelListHeaderComponent());
        return module;
    }

    @Override
    protected void onConfigureParams(@NonNull ChannelListModule module, @NonNull Bundle args) {
        super.onConfigureParams(module, args);
        module.getParams().setUseHeader(true);
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull ChannelListModule module, @NonNull ChannelListViewModel viewModel) {
        super.onBeforeReady(status, module, viewModel);
        module.getChannelListComponent().setAdapter(new CustomChannelListAdapter());
        module.getChannelListComponent().setOnItemLongClickListener((view, position, channel) -> showListContextMenu(channel));
    }

    private void showListContextMenu(@NonNull GroupChannel channel) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        CharSequence titleItem = getString(R.string.sb_text_channel_settings_change_channel_name);
        CharSequence leaveItem = getString(R.string.sb_text_channel_list_leave);
        final boolean isOff = channel.getMyPushTriggerOption() == GroupChannel.PushTriggerOption.OFF;
        CharSequence notificationItem = isOff ? getString(R.string.sb_text_channel_list_push_on) :
                getString(R.string.sb_text_channel_list_push_off);
        CharSequence[] items = {titleItem, leaveItem, notificationItem};
        builder.setItems(items, (dialog, which) -> {
            dialog.dismiss();
            if (which == 0) {
                showTitleChangeDialog(channel);
            } else if (which == 1) {
                getViewModel().leaveChannel(channel, e -> {
                    if (e == null) return;
                    Toast.makeText(requireContext(), R.string.sb_text_error_leave_channel, Toast.LENGTH_SHORT).show();
                });
                leaveChannel(channel);
            } else {
                getViewModel().setPushNotification(channel, isOff,
                        e -> {
                            if (e == null) return;
                            int errorString = isOff ? R.string.sb_text_error_push_notification_on :
                                    R.string.sb_text_error_push_notification_off;
                            Toast.makeText(requireContext(), errorString, Toast.LENGTH_SHORT).show();
                        });
            }
        });
        builder.show();
    }

    private void showTitleChangeDialog(final GroupChannel channel) {
        final EditText input = new EditText(getContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(input)
                .setTitle(R.string.sb_text_channel_settings_change_channel_name)
                .setPositiveButton(R.string.text_confirm,
                        (dialog, which) -> channel.updateChannel(new GroupChannelParams()
                                .setName(input.getText().toString()), null));
        builder.show();
    }

    @Override
    protected void onBindHeaderComponent(@NonNull HeaderComponent headerComponent, @NonNull ChannelListViewModel viewModel) {
        super.onBindHeaderComponent(headerComponent, viewModel);
        ((CustomChannelListHeaderComponent) headerComponent).setSettingsButtonClickListener(v -> {
            if (getActivity() instanceof GroupChannelMainActivity) {
                ((GroupChannelMainActivity) getActivity()).moveToSettings();
            }
        });
    }

    @Override
    protected void onBindChannelListComponent(@NonNull ChannelListComponent channelListComponent, @NonNull ChannelListViewModel viewModel) {
        super.onBindChannelListComponent(channelListComponent, viewModel);
        channelListComponent.setOnItemLongClickListener((view, position, channel)
                -> showListContextMenu(channel));
    }
}
