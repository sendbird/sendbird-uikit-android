package com.sendbird.uikit.customsample.openchannel.livestream;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.params.FileMessageCreateParams;
import com.sendbird.android.params.UserMessageCreateParams;
import com.sendbird.android.params.UserMessageUpdateParams;
import com.sendbird.uikit.consts.KeyboardDisplayType;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.models.CustomMessageType;
import com.sendbird.uikit.customsample.openchannel.CustomOpenChannelMessageListAdapter;
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

    @NonNull
    @Override
    protected OpenChannelModule onCreateModule(@NonNull Bundle args) {
        return new OpenChannelModule(requireContext(), new OpenChannelModule.Params(requireContext(), R.style.AppThemeCustom_Sendbird));
    }

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
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull OpenChannelModule module, @NonNull OpenChannelViewModel viewModel) {
        super.onBeforeReady(status, module, viewModel);
        getModule().getMessageInputComponent().setOnInputTextChangedListener((s, start, before, count) -> inputText = s.toString());
        getModule().getMessageInputComponent().setOnInputLeftButtonClickListener(v -> showMessageTypeDialog());
        if (viewModel.getChannel() != null) {
            getModule().getMessageListComponent().setAdapter(new CustomOpenChannelMessageListAdapter(viewModel.getChannel(), true));
        }
    }

    private CustomMessageType customMessageType = CustomMessageType.NONE;

    @Override
    protected void onBeforeSendUserMessage(@NonNull UserMessageCreateParams params) {
        super.onBeforeSendUserMessage(params);
        params.setCustomType(customMessageType.getValue());
        params.setData(null);
        params.setMentionedUserIds(null);
        params.setMentionedUsers(null);
        params.setMetaArrays(null);
        params.setParentMessageId(0);
        params.setPushNotificationDeliveryOption(null);
        params.setTranslationTargetLanguages(null);
    }

    @Override
    protected void onBeforeSendFileMessage(@NonNull FileMessageCreateParams params) {
        super.onBeforeSendFileMessage(params);
        params.setCustomType(customMessageType.getValue());
        params.setData(null);
        params.setMentionedUserIds(null);
        params.setMentionedUsers(null);
        params.setMetaArrays(null);
        params.setParentMessageId(0);
        params.setPushNotificationDeliveryOption(null);
    }

    @Override
    protected void onBeforeUpdateUserMessage(@NonNull UserMessageUpdateParams params) {
        super.onBeforeUpdateUserMessage(params);
        params.setCustomType(customMessageType.getValue());
        params.setData(null);
        params.setMentionedUserIds(null);
        params.setMentionedUsers(null);
    }

    @Override
    protected void sendUserMessage(@NonNull UserMessageCreateParams params) {
        super.sendUserMessage(params);
    }

    @Override
    protected void sendFileMessage(@NonNull Uri uri) {
        super.sendFileMessage(uri);
    }

    @Override
    protected void updateUserMessage(long messageId, @NonNull UserMessageUpdateParams params) {
        super.updateUserMessage(messageId, params);
    }

    @Override
    protected void deleteMessage(@NonNull BaseMessage message) {
        super.deleteMessage(message);
    }

    @Override
    protected void resendMessage(@NonNull BaseMessage message) {
        super.resendMessage(message);
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
