package com.sendbird.uikit.customsample.groupchannel.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.groupchannel.components.CustomUserTypedHeaderComponent;
import com.sendbird.uikit.fragments.ParticipantListFragment;
import com.sendbird.uikit.modules.ParticipantListModule;
import com.sendbird.uikit.modules.components.HeaderComponent;

/**
 * Implements the customized <code>ParticipantListFragment</code>.
 */
public class CustomParticipantsListFragment extends ParticipantListFragment {
    @NonNull
    @Override
    protected ParticipantListModule onCreateModule(@NonNull Bundle args) {
        ParticipantListModule module = super.onCreateModule(args);
        module.setHeaderComponent(new CustomUserTypedHeaderComponent());
        return module;
    }

    @Override
    protected void onConfigureParams(@NonNull ParticipantListModule module, @NonNull Bundle args) {
        super.onConfigureParams(module, args);
        HeaderComponent.Params headerParams = module.getHeaderComponent().getParams();
        if (isFragmentAlive()) {
            headerParams.setTitle(requireContext().getString(R.string.sb_text_header_participants));
            headerParams.setLeftButtonIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_arrow_left, null));
            headerParams.setUseRightButton(false);
        }
    }
}
