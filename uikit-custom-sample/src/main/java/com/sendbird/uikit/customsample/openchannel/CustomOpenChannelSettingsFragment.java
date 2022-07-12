package com.sendbird.uikit.customsample.openchannel;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.fragments.OpenChannelSettingsFragment;
import com.sendbird.uikit.modules.OpenChannelSettingsModule;

/**
 * Implements the customized <code>OpenChannelSettingsFragment</code>
 */
public class CustomOpenChannelSettingsFragment extends OpenChannelSettingsFragment {
    @NonNull
    @Override
    protected OpenChannelSettingsModule onCreateModule(@NonNull Bundle args) {
        return new OpenChannelSettingsModule(requireContext(), new OpenChannelSettingsModule.Params(requireContext(), R.style.AppThemeCustom_Sendbird));
    }
}
