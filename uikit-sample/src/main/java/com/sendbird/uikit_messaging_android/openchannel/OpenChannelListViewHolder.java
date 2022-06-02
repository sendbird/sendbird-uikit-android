package com.sendbird.uikit_messaging_android.openchannel;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.OpenChannel;

/**
 * Base ViewHolder to bind <code>OpenChannel</code> data.
 */
abstract public class OpenChannelListViewHolder extends RecyclerView.ViewHolder {
    public OpenChannelListViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    abstract protected void bind(@Nullable OpenChannel openChannel);
}
