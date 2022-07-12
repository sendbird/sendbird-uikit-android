package com.sendbird.uikit.customsample.groupchannel.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.sendbird.uikit.customsample.groupchannel.components.CustomChannelSettingsHeaderComponent;
import com.sendbird.uikit.customsample.groupchannel.components.CustomChannelSettingsMenuComponent;
import com.sendbird.uikit.fragments.ChannelSettingsFragment;
import com.sendbird.uikit.model.ReadyStatus;
import com.sendbird.uikit.modules.ChannelSettingsModule;
import com.sendbird.uikit.vm.ChannelSettingsViewModel;

/**
 * Implements the customized <code>ChannelSettingsFragment</code>.
 */
public class CustomChannelSettingsFragment extends ChannelSettingsFragment {
    @NonNull
    @Override
    protected ChannelSettingsModule onCreateModule(@NonNull Bundle args) {
        ChannelSettingsModule module = super.onCreateModule(args);
        module.setHeaderComponent(new CustomChannelSettingsHeaderComponent());
        module.setChannelSettingsMenuComponent(new CustomChannelSettingsMenuComponent());
        return module;
    }

    @Override
    protected void onReady(@NonNull ReadyStatus status, @NonNull ChannelSettingsModule module, @NonNull ChannelSettingsViewModel viewModel) {
        super.onReady(status, module, viewModel);

        if (viewModel.getChannel() != null) {
            module.getChannelSettingsMenuComponent().notifyChannelChanged(viewModel.getChannel());
        }
    }
}
