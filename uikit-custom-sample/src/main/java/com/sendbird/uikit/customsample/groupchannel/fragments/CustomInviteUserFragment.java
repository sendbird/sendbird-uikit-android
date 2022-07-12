package com.sendbird.uikit.customsample.groupchannel.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.groupchannel.components.CustomSelectUserHeaderComponent;
import com.sendbird.uikit.customsample.groupchannel.components.adapters.CustomInviteUserListAdapter;
import com.sendbird.uikit.fragments.InviteUserFragment;
import com.sendbird.uikit.model.ReadyStatus;
import com.sendbird.uikit.modules.InviteUserModule;
import com.sendbird.uikit.modules.components.SelectUserHeaderComponent;
import com.sendbird.uikit.vm.InviteUserViewModel;

/**
 * Implements the customized <code>InviteUserFragment</code>.
 */
public class CustomInviteUserFragment extends InviteUserFragment {
    @NonNull
    @Override
    protected InviteUserModule onCreateModule(@NonNull Bundle args) {
        InviteUserModule module = super.onCreateModule(args);
        module.setHeaderComponent(new CustomSelectUserHeaderComponent());
        return module;
    }

    @Override
    protected void onConfigureParams(@NonNull InviteUserModule module, @NonNull Bundle args) {
        super.onConfigureParams(module, args);
        SelectUserHeaderComponent.Params headerParams = module.getHeaderComponent().getParams();
        if (isFragmentAlive()) {
            headerParams.setTitle(requireContext().getString(R.string.sb_text_header_invite_member));
        }
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull InviteUserModule module, @NonNull InviteUserViewModel viewModel) {
        super.onBeforeReady(status, module, viewModel);
        module.getInviteUserListComponent().setAdapter(new CustomInviteUserListAdapter());
    }
}
