package com.sendbird.uikit.customsample.groupchannel.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.params.FileMessageCreateParams;
import com.sendbird.android.params.UserMessageCreateParams;
import com.sendbird.android.params.UserMessageUpdateParams;
import com.sendbird.uikit.activities.MessageSearchActivity;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.customsample.groupchannel.components.CustomChannelHeaderComponent;
import com.sendbird.uikit.customsample.groupchannel.components.CustomMessageInputComponent;
import com.sendbird.uikit.customsample.groupchannel.components.adapters.CustomMessageListAdapter;
import com.sendbird.uikit.customsample.groupchannel.components.adapters.CustomSuggestedMentionListAdapter;
import com.sendbird.uikit.customsample.groupchannel.viewmodels.CustomChannelViewModel;
import com.sendbird.uikit.customsample.groupchannel.viewmodels.ViewModelFactory;
import com.sendbird.uikit.customsample.models.CustomMessageType;
import com.sendbird.uikit.fragments.ChannelFragment;
import com.sendbird.uikit.model.ReadyStatus;
import com.sendbird.uikit.modules.ChannelModule;
import com.sendbird.uikit.modules.components.ChannelHeaderComponent;
import com.sendbird.uikit.modules.components.MessageInputComponent;
import com.sendbird.uikit.vm.ChannelViewModel;
import com.sendbird.uikit.widgets.MessageInputView;

/**
 * Implements the customized <code>ChannelFragment</code>.
 */
public class CustomChannelFragment extends ChannelFragment {
    @NonNull
    private CustomMessageType customMessageType = CustomMessageType.NONE;

    @NonNull
    @Override
    protected ChannelModule onCreateModule(@NonNull Bundle args) {
        ChannelModule module = super.onCreateModule(args);
        module.setHeaderComponent(new CustomChannelHeaderComponent());
        module.setInputComponent(new CustomMessageInputComponent());
        return module;
    }

    @NonNull
    @Override
    protected ChannelViewModel onCreateViewModel() {
        final Bundle args = getArguments() == null ? new Bundle() : getArguments();
        final String channelUrl = args.getString(StringSet.KEY_CHANNEL_URL, "");
        return new ViewModelProvider(this, new ViewModelFactory(channelUrl)).get(channelUrl, CustomChannelViewModel.class);
    }

    @Override
    protected void onBeforeSendUserMessage(@NonNull UserMessageCreateParams params) {
        super.onBeforeSendUserMessage(params);
        params.setCustomType(customMessageType.getValue());
    }

    @Override
    protected void onBeforeSendFileMessage(@NonNull FileMessageCreateParams params) {
        super.onBeforeSendFileMessage(params);
        params.setCustomType(customMessageType.getValue());
    }

    @Override
    protected void onBeforeUpdateUserMessage(@NonNull UserMessageUpdateParams params) {
        super.onBeforeUpdateUserMessage(params);
        params.setCustomType(customMessageType.getValue());
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull ChannelModule module, @NonNull ChannelViewModel viewModel) {
        super.onBeforeReady(status, module, viewModel);

        final GroupChannel channel = viewModel.getChannel();
        if (channel == null) return;
        module.getMessageListComponent().setAdapter(new CustomMessageListAdapter(channel, true));
    }

    @Override
    protected void onBindMessageInputComponent(@NonNull MessageInputComponent inputComponent, @NonNull ChannelViewModel viewModel, @Nullable GroupChannel channel) {
        super.onBindMessageInputComponent(inputComponent, viewModel, channel);

        if (inputComponent instanceof CustomMessageInputComponent) {
            CustomMessageInputComponent customInput = (CustomMessageInputComponent) getModule().getMessageInputComponent();
            customInput.setMenuCameraClickListener(v -> takeCamera());
            customInput.setMenuPhotoClickListener(v -> takePhoto());
            customInput.setMenuFileClickListener(v -> takeFile());
            customInput.setHighlightCheckedListener((buttonView, isChecked) ->
                    customMessageType = isChecked ? CustomMessageType.HIGHLIGHT : CustomMessageType.NONE);
            customInput.setEmojiClickListener((view, position, url) -> {
                final UserMessageCreateParams params = new UserMessageCreateParams();
                params.setMessage(url);
                customMessageType = CustomMessageType.EMOJI;
                sendUserMessage(params);
                customInput.requestInputMode(MessageInputView.Mode.DEFAULT);
                customMessageType = CustomMessageType.NONE;
            });
            customInput.setUseSuggestedMentionListDivider(false);
            customInput.setSuggestedMentionListAdapter(new CustomSuggestedMentionListAdapter());
        }
    }

    @Override
    protected void onBindChannelHeaderComponent(@NonNull ChannelHeaderComponent headerComponent, @NonNull ChannelViewModel viewModel, @Nullable GroupChannel channel) {
        super.onBindChannelHeaderComponent(headerComponent, viewModel, channel);

        if (headerComponent instanceof CustomChannelHeaderComponent) {
            CustomChannelHeaderComponent customHeader = (CustomChannelHeaderComponent) getModule().getHeaderComponent();
            customHeader.setSearchButtonClickListener(v -> {
                if (isFragmentAlive() && channel != null) {
                    startActivity(MessageSearchActivity.newIntent(requireContext(), channel.getUrl()));
                }
            });
        }
    }
}
