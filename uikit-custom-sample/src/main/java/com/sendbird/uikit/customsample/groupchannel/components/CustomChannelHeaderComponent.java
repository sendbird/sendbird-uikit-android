package com.sendbird.uikit.customsample.groupchannel.components;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.sendbird.android.GroupChannel;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.modules.components.ChannelHeaderComponent;

/**
 * Implements the customized <code>ChannelHeaderComponent</code> used in <code>CustomChannelFragment</code>.
 */
public class CustomChannelHeaderComponent extends ChannelHeaderComponent {
    private Toolbar toolbar;
    @Nullable
    private View.OnClickListener searchButtonClickListener;

    public CustomChannelHeaderComponent() {
        super();
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle args) {
        toolbar = new Toolbar(context);
        toolbar.setLayoutParams(new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) context.getResources().getDimension(R.dimen.sb_size_56)));
        toolbar.setBackgroundColor(context.getResources().getColor(R.color.primary_300));
        toolbar.setTitleTextAppearance(context, R.style.SendbirdH1OnDark01);
        toolbar.setSubtitleTextAppearance(context, R.style.SendbirdCaption1OnDark02);
        toolbar.inflateMenu(R.menu.channel_menu);
        toolbar.setNavigationIcon(R.drawable.icon_arrow_left);
        toolbar.setNavigationOnClickListener(this::onLeftButtonClicked);

        toolbar.getMenu().findItem(R.id.action_settings)
                .getActionView().setOnClickListener(v -> {
            Logger.d("++ settings button clicked");
            onRightButtonClicked(v);
        });

        toolbar.getMenu().findItem(R.id.action_search)
                .getActionView().setOnClickListener(v -> {
            Logger.d("++ settings button clicked");
            if (searchButtonClickListener != null) searchButtonClickListener.onClick(v);
        });
        return toolbar;
    }

    @Override
    public void notifyChannelChanged(@NonNull GroupChannel channel) {
        toolbar.setTitle(channel.getName());
    }

    @Override
    public void notifyHeaderDescriptionChanged(@Nullable String description) {
        toolbar.setSubtitle(description);
    }

    public void setSearchButtonClickListener(@Nullable View.OnClickListener searchButtonClickListener) {
        this.searchButtonClickListener = searchButtonClickListener;
    }
}
