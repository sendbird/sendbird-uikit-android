package com.sendbird.uikit.activities.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;

/**
 * A ViewHolder describes an item view and Message about its place within the RecyclerView.
 */
public abstract class OpenChannelMessageViewHolder extends MessageViewHolder {
    public OpenChannelMessageViewHolder(View view) {
        super(view);
    }

    OpenChannelMessageViewHolder(@NonNull ViewDataBinding binding, boolean useMessageGroupUI) {
        super(binding, useMessageGroupUI);
    }

    abstract public View getProfileView();
}

