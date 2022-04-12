package com.sendbird.uikit.customsample.groupchannel.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.sendbird.uikit.customsample.groupchannel.components.CustomModerationHeaderComponent;
import com.sendbird.uikit.customsample.groupchannel.components.CustomModerationListComponent;
import com.sendbird.uikit.fragments.ModerationFragment;
import com.sendbird.uikit.modules.ModerationModule;

public class CustomModerationFragment extends ModerationFragment {
    @NonNull
    @Override
    protected ModerationModule onCreateModule(@NonNull Bundle args) {
        ModerationModule module = super.onCreateModule(args);
        module.setHeaderComponent(new CustomModerationHeaderComponent());
        module.setModerationListComponent(new CustomModerationListComponent());
        return module;
    }
}
