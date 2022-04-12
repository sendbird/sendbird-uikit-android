package com.sendbird.uikit.customsample.openchannel.community;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.models.CustomMessageType;
import com.sendbird.uikit.customsample.openchannel.CustomOpenChannelMessageListAdapter;
import com.sendbird.uikit.fragments.OpenChannelFragment;
import com.sendbird.uikit.model.ReadyStatus;
import com.sendbird.uikit.modules.OpenChannelModule;
import com.sendbird.uikit.modules.components.OpenChannelHeaderComponent;
import com.sendbird.uikit.vm.OpenChannelViewModel;

public class CommunityChannelFragment extends OpenChannelFragment {
    private CustomMessageType customMessageType = CustomMessageType.NONE;

    @NonNull
    @Override
    protected OpenChannelModule onCreateModule(@NonNull Bundle args) {
        return new OpenChannelModule(requireContext(), new OpenChannelModule.Params(requireContext(), R.style.AppThemeCustom_Sendbird));
    }

    @Override
    protected void onConfigureParams(@NonNull OpenChannelModule module, @NonNull Bundle args) {
        super.onConfigureParams(module, args);
        OpenChannelModule.Params params = module.getParams();
        params.setUseHeader(true);

        OpenChannelHeaderComponent.Params headerParams = module.getHeaderComponent().getParams();
        headerParams.setUseLeftButton(true);
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull OpenChannelModule module, @NonNull OpenChannelViewModel viewModel) {
        super.onBeforeReady(status, module, viewModel);
        getModule().getMessageInputComponent().setOnInputLeftButtonClickListener(v -> showMessageTypeDialog());
        if (viewModel.getChannel() != null) {
            getModule().getMessageListComponent().setAdapter(new CustomOpenChannelMessageListAdapter(viewModel.getChannel(), true));
        }
    }

    public void setCustomMessageType(@NonNull CustomMessageType customMessageType) {
        this.customMessageType = customMessageType;
    }

    @NonNull
    public CustomMessageType getCustomMessageType() {
        return customMessageType;
    }

    private void showMessageTypeDialog() {
        if (!isFragmentAlive()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Pick message type")
                .setMultiChoiceItems(new String[]{com.sendbird.uikit.customsample.consts.StringSet.highlight},
                        new boolean[]{getCustomMessageType().equals(CustomMessageType.HIGHLIGHT)},
                        (dialog, which, isChecked) -> {
                            final CustomMessageType type = isChecked ? CustomMessageType.HIGHLIGHT : CustomMessageType.NONE;
                            setCustomMessageType(type);
                        })
                .create()
                .show();
    }

    @NonNull
    @Override
    protected String getChannelUrl() {
        final Bundle args = getArguments() == null ? new Bundle() : getArguments();
        return args.getString("CHANNEL_URL", "");
    }
}
