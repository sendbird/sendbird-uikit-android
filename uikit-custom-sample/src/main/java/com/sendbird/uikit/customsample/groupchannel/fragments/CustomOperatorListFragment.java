package com.sendbird.uikit.customsample.groupchannel.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.groupchannel.components.CustomUserTypedHeaderComponent;
import com.sendbird.uikit.fragments.OperatorListFragment;
import com.sendbird.uikit.modules.OperatorListModule;
import com.sendbird.uikit.modules.components.HeaderComponent;

/**
 * Implements the customized <code>OperatorListFragment</code>.
 */
public class CustomOperatorListFragment extends OperatorListFragment {
    @NonNull
    @Override
    protected OperatorListModule onCreateModule(@NonNull Bundle args) {
        OperatorListModule module = super.onCreateModule(args);
        module.setHeaderComponent(new CustomUserTypedHeaderComponent());
        return module;
    }

    @Override
    protected void onConfigureParams(@NonNull OperatorListModule module, @NonNull Bundle args) {
        super.onConfigureParams(module, args);
        HeaderComponent.Params headerParams = module.getHeaderComponent().getParams();
        if (isFragmentAlive()) {
            headerParams.setTitle(requireContext().getString(R.string.sb_text_menu_operators));
            headerParams.setLeftButtonIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_arrow_left, null));
            headerParams.setRightButtonIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_plus, null));
        }
    }
}
