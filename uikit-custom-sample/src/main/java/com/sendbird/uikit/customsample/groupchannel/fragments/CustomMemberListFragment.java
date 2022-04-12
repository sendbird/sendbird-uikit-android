package com.sendbird.uikit.customsample.groupchannel.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.groupchannel.components.CustomUserTypedHeaderComponent;
import com.sendbird.uikit.customsample.groupchannel.components.adapters.CustomMemberListAdapter;
import com.sendbird.uikit.fragments.MemberListFragment;
import com.sendbird.uikit.model.ReadyStatus;
import com.sendbird.uikit.modules.MemberListModule;
import com.sendbird.uikit.modules.components.HeaderComponent;
import com.sendbird.uikit.vm.MemberListViewModel;

public class CustomMemberListFragment extends MemberListFragment {
    @NonNull
    @Override
    protected MemberListModule onCreateModule(@NonNull Bundle args) {
        MemberListModule module = super.onCreateModule(args);
        module.setHeaderComponent(new CustomUserTypedHeaderComponent());
        return module;
    }

    @Override
    protected void onConfigureParams(@NonNull MemberListModule module, @NonNull Bundle args) {
        super.onConfigureParams(module, args);
        HeaderComponent.Params headerParams = module.getHeaderComponent().getParams();
        if (isFragmentAlive()) {
            headerParams.setTitle(requireContext().getString(R.string.sb_text_header_member_list));
            headerParams.setLeftButtonIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_arrow_left, null));
            headerParams.setRightButtonIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_plus, null));
        }
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull MemberListModule module, @NonNull MemberListViewModel viewModel) {
        super.onBeforeReady(status, module, viewModel);
        module.getMemberListComponent().setAdapter(new CustomMemberListAdapter());
    }
}
