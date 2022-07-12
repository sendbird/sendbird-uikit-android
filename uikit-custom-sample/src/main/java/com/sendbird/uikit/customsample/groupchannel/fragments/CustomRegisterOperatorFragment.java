package com.sendbird.uikit.customsample.groupchannel.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.groupchannel.components.CustomSelectUserHeaderComponent;
import com.sendbird.uikit.customsample.groupchannel.components.adapters.CustomRegisterOperatorListAdapter;
import com.sendbird.uikit.fragments.RegisterOperatorFragment;
import com.sendbird.uikit.model.ReadyStatus;
import com.sendbird.uikit.modules.RegisterOperatorModule;
import com.sendbird.uikit.modules.components.SelectUserHeaderComponent;
import com.sendbird.uikit.vm.RegisterOperatorViewModel;

/**
 * Implements the customized <code>RegisterOperatorFragment</code>.
 */
public class CustomRegisterOperatorFragment extends RegisterOperatorFragment {
    @NonNull
    @Override
    protected RegisterOperatorModule onCreateModule(@NonNull Bundle args) {
        RegisterOperatorModule module = super.onCreateModule(args);
        module.setHeaderComponent(new CustomSelectUserHeaderComponent());
        return module;
    }

    @Override
    protected void onConfigureParams(@NonNull RegisterOperatorModule module, @NonNull Bundle args) {
        super.onConfigureParams(module, args);
        SelectUserHeaderComponent.Params headerParams = module.getHeaderComponent().getParams();
        if (isFragmentAlive()) {
            headerParams.setTitle(requireContext().getString(R.string.sb_text_header_set_operators));
        }
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull RegisterOperatorModule module, @NonNull RegisterOperatorViewModel viewModel) {
        super.onBeforeReady(status, module, viewModel);
        module.getRegisterOperatorListComponent().setAdapter(new CustomRegisterOperatorListAdapter());
    }
}
