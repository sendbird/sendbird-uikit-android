package com.sendbird.uikit.customsample.openchannel.community;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;

import com.sendbird.android.channel.OpenChannel;
import com.sendbird.uikit.activities.CreateOpenChannelActivity;
import com.sendbird.uikit.activities.OpenChannelActivity;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.fragments.OpenChannelListFragment;
import com.sendbird.uikit.log.Logger;

/**
 * Displays an open channel list screen used for community.
 */
public class CommunityListFragment extends OpenChannelListFragment {
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.community_list_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        final MenuItem createMenuItem = menu.findItem(R.id.action_create_channel);
        View rootView = createMenuItem.getActionView();
        if (rootView != null) rootView.setOnClickListener(v -> onOptionsItemSelected(createMenuItem));
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_create_channel && getActivity() != null) {
            Logger.d("++ create button clicked");
            Intent intent = new Intent(getActivity(), CreateOpenChannelActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onItemClicked(@NonNull View view, int position, @NonNull OpenChannel channel) {
        startActivity(OpenChannelActivity.newIntent(requireContext(), CommunityActivity.class, channel.getUrl()));

    }

    @Override
    public void onResume() {
        super.onResume();
        setHasOptionsMenu(true);
    }
}
