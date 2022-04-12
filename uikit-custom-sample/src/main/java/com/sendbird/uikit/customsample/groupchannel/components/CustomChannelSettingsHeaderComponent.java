package com.sendbird.uikit.customsample.groupchannel.components;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.modules.components.ChannelSettingsHeaderComponent;

public class CustomChannelSettingsHeaderComponent extends ChannelSettingsHeaderComponent {

    public CustomChannelSettingsHeaderComponent() {
        super();
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        Toolbar toolbar = new Toolbar(context);
        toolbar.setLayoutParams(new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) context.getResources().getDimension(R.dimen.sb_size_56)));
        toolbar.setBackgroundColor(context.getResources().getColor(R.color.primary_300));
        toolbar.setTitleTextAppearance(context, R.style.SendbirdH1OnDark01);
        toolbar.setSubtitleTextAppearance(context, R.style.SendbirdCaption1OnDark02);
        toolbar.setTitle(R.string.sb_text_header_channel_settings);
        toolbar.inflateMenu(R.menu.channel_settings_menu);
        toolbar.setNavigationIcon(R.drawable.icon_arrow_left);
        toolbar.setNavigationOnClickListener(this::onLeftButtonClicked);

        toolbar.getMenu().findItem(R.id.action_edit)
                .getActionView().setOnClickListener(v -> {
            Logger.d("++ edit button clicked");
            onRightButtonClicked(v);
        });
        return toolbar;
    }
}
