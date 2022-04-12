package com.sendbird.uikit.customsample.groupchannel.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.groupchannel.components.CustomSelectUserHeaderComponent;
import com.sendbird.uikit.customsample.groupchannel.components.adapters.CustomPromoteOperatorListAdapter;
import com.sendbird.uikit.fragments.PromoteOperatorFragment;
import com.sendbird.uikit.model.ReadyStatus;
import com.sendbird.uikit.modules.PromoteOperatorModule;
import com.sendbird.uikit.modules.components.SelectUserHeaderComponent;
import com.sendbird.uikit.vm.PromoteOperatorViewModel;

public class CustomPromoteOperatorsFragment extends PromoteOperatorFragment {
    @NonNull
    @Override
    protected PromoteOperatorModule onCreateModule(@NonNull Bundle args) {
        PromoteOperatorModule module = super.onCreateModule(args);
        module.setHeaderComponent(new CustomSelectUserHeaderComponent());
        return module;
    }

    @Override
    protected void onConfigureParams(@NonNull PromoteOperatorModule module, @NonNull Bundle args) {
        super.onConfigureParams(module, args);
        SelectUserHeaderComponent.Params headerParams = module.getHeaderComponent().getParams();
        if (isFragmentAlive()) {
            headerParams.setTitle(requireContext().getString(R.string.sb_text_header_select_members));
        }
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull PromoteOperatorModule module, @NonNull PromoteOperatorViewModel viewModel) {
        super.onBeforeReady(status, module, viewModel);
        module.getPromoteOperatorListComponent().setAdapter(new CustomPromoteOperatorListAdapter());
    }
}
