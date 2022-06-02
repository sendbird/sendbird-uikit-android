package com.sendbird.uikit.customsample.groupchannel.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.groupchannel.components.CustomSelectUserHeaderComponent;
import com.sendbird.uikit.customsample.groupchannel.components.adapters.CustomCreateChannelUserListAdapter;
import com.sendbird.uikit.fragments.CreateChannelFragment;
import com.sendbird.uikit.model.ReadyStatus;
import com.sendbird.uikit.modules.CreateChannelModule;
import com.sendbird.uikit.modules.components.SelectUserHeaderComponent;
import com.sendbird.uikit.vm.CreateChannelViewModel;

/**
 * Implements the customized <code>CreateChannelFragment</code>.
 */
public class CustomCreateChannelFragment extends CreateChannelFragment {
    @NonNull
    @Override
    protected CreateChannelModule onCreateModule(@NonNull Bundle args) {
        CreateChannelModule module = super.onCreateModule(args);
        module.setHeaderComponent(new CustomSelectUserHeaderComponent());
        return module;
    }

    @Override
    protected void onConfigureParams(@NonNull CreateChannelModule module, @NonNull Bundle args) {
        super.onConfigureParams(module, args);
        SelectUserHeaderComponent.Params headerParams = module.getHeaderComponent().getParams();
        if (isFragmentAlive()) {
            headerParams.setTitle(requireContext().getString(R.string.sb_text_header_create_channel));
        }
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull CreateChannelModule module, @NonNull CreateChannelViewModel viewModel) {
        super.onBeforeReady(status, module, viewModel);
        module.getUserListComponent().setAdapter(new CustomCreateChannelUserListAdapter());
    }
}
