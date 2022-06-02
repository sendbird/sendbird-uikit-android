package com.sendbird.uikit.customsample.groupchannel.components.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.sendbird.android.GroupChannel;
import com.sendbird.uikit.activities.adapter.ChannelListAdapter;
import com.sendbird.uikit.activities.viewholder.BaseViewHolder;
import com.sendbird.uikit.customsample.groupchannel.components.viewholders.CustomChannelViewHolder;
import com.sendbird.uikit.customsample.databinding.ViewCustomChannelHolderBinding;

/**
 * Implements the customized <code>ChannelListAdapter</code> to adapt the customized channel list items.
 */
public class CustomChannelListAdapter extends ChannelListAdapter {
    @NonNull
    @Override
    public BaseViewHolder<GroupChannel> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomChannelViewHolder(ViewCustomChannelHolderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<GroupChannel> holder, int position) {
        super.onBindViewHolder(holder, position);
    }
}
