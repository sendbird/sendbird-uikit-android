package com.sendbird.uikit.customsample.groupchannel.components;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.widget.NestedScrollView;

import com.sendbird.android.GroupChannel;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.modules.components.ModerationListComponent;

public class CustomModerationListComponent extends ModerationListComponent {
    private View view;

    public CustomModerationListComponent() {
        super();
    }

    @Nullable
    @Override
    protected NestedScrollView getNestedScrollView() {
        return view.findViewById(R.id.scroll_layout);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.view_custom_moderation_list, parent, false);
        view.findViewById(R.id.operators).setOnClickListener(v -> onMenuItemClicked(v, ModerationMenu.OPERATORS));
        view.findViewById(R.id.banned).setOnClickListener(v -> onMenuItemClicked(v, ModerationMenu.BANNED_MEMBERS));
        view.findViewById(R.id.muted).setOnClickListener(v -> onMenuItemClicked(v, ModerationMenu.MUTED_MEMBERS));
        view.findViewById(R.id.freeze).setOnClickListener(v -> onMenuItemClicked(v, ModerationMenu.FREEZE_CHANNEL));
        return view;
    }

    @Override
    public void notifyChannelChanged(@NonNull GroupChannel channel) {
        ((SwitchCompat) view.findViewById(R.id.freezeSwitch)).setChecked(channel.isFrozen());
    }
}
