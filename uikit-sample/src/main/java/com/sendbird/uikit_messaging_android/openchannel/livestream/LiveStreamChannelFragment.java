package com.sendbird.uikit_messaging_android.openchannel.livestream;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.sendbird.uikit.consts.KeyboardDisplayType;
import com.sendbird.uikit.fragments.OpenChannelFragment;
import com.sendbird.uikit.model.ReadyStatus;
import com.sendbird.uikit.modules.OpenChannelModule;
import com.sendbird.uikit.modules.components.OpenChannelHeaderComponent;
import com.sendbird.uikit.modules.components.OpenChannelMessageInputComponent;
import com.sendbird.uikit.vm.OpenChannelViewModel;

/**
 * Displays an open channel screen used for live stream.
 */
public class LiveStreamChannelFragment extends OpenChannelFragment {
    private String inputText;

    @Override
    protected void onConfigureParams(@NonNull OpenChannelModule module, @NonNull Bundle args) {
        super.onConfigureParams(module, args);
        final String creatorName = args.getString("DESCRIPTION");
        this.inputText = args.getString("INPUT_TEXT");

        OpenChannelModule.Params moduleParams = module.getParams();
        moduleParams.setUseOverlayMode(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
        moduleParams.setUseHeader(true);

        OpenChannelHeaderComponent.Params headerParams = module.getHeaderComponent().getParams();
        headerParams.setDescription(creatorName);

        OpenChannelMessageInputComponent.Params inputParams = module.getMessageInputComponent().getParams();
        inputParams.setInputText(inputText);
        inputParams.setKeyboardDisplayType(KeyboardDisplayType.Dialog);
    }

    @Override
    public void onReady(@NonNull ReadyStatus status, @NonNull OpenChannelModule module, @NonNull OpenChannelViewModel viewModel) {
        super.onReady(status, module, viewModel);
        getModule().getMessageInputComponent().setOnInputTextChangedListener((s, start, before, count) -> inputText = s.toString());
    }

    @NonNull
    @Override
    protected String getChannelUrl() {
        final Bundle args = getArguments() == null ? new Bundle() : getArguments();
        return args.getString("CHANNEL_URL", "");
    }
}
