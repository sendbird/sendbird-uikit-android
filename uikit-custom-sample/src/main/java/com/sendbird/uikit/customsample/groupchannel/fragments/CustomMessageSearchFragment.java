package com.sendbird.uikit.customsample.groupchannel.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.GroupChannel;
import com.sendbird.uikit.customsample.groupchannel.components.CustomMessageSearchHeaderComponent;
import com.sendbird.uikit.customsample.groupchannel.components.adapters.CustomMessageSearchAdapter;
import com.sendbird.uikit.fragments.MessageSearchFragment;
import com.sendbird.uikit.model.ReadyStatus;
import com.sendbird.uikit.modules.MessageSearchModule;
import com.sendbird.uikit.modules.components.MessageSearchHeaderComponent;
import com.sendbird.uikit.vm.MessageSearchViewModel;

public class CustomMessageSearchFragment extends MessageSearchFragment {
    @NonNull
    @Override
    protected MessageSearchModule onCreateModule(@NonNull Bundle args) {
        MessageSearchModule module = super.onCreateModule(args);
        module.setHeaderComponent(new CustomMessageSearchHeaderComponent());
        return module;
    }

    @Override
    protected void onConfigureParams(@NonNull MessageSearchModule module, @NonNull Bundle args) {
        super.onConfigureParams(module, args);
    }

    @Override
    protected void onBindHeaderComponent(@NonNull MessageSearchHeaderComponent headerComponent, @NonNull MessageSearchViewModel viewModel, @Nullable GroupChannel channel) {
        super.onBindHeaderComponent(headerComponent, viewModel, channel);
        if (headerComponent instanceof CustomMessageSearchHeaderComponent) {
            CustomMessageSearchHeaderComponent customHeader = (CustomMessageSearchHeaderComponent) getModule().getHeaderComponent();
            customHeader.setCancelButtonClickListener(v -> shouldActivityFinish());
        }
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull MessageSearchModule module, @NonNull MessageSearchViewModel viewModel) {
        super.onBeforeReady(status, module, viewModel);
        module.getMessageListComponent().setAdapter(new CustomMessageSearchAdapter());
    }
}
