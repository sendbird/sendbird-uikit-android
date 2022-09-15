package com.sendbird.uikit_messaging_android.openchannel.community;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.sendbird.uikit.activities.CreateOpenChannelActivity;
import com.sendbird.uikit.fragments.OpenChannelListFragment;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit_messaging_android.R;
import com.sendbird.uikit_messaging_android.databinding.ViewCustomMenuIconButtonBinding;
import com.sendbird.uikit_messaging_android.utils.DrawableUtils;
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils;

/**
 * Displays an open channel list screen used for community.
 */
public class CommunityListFragment extends OpenChannelListFragment {
    @NonNull
    private final ActivityResultLauncher<Intent> createChannelLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        Logger.d("++ create channel result=%s", result.getResultCode());
        if (result.getResultCode() == Activity.RESULT_OK) {
            onRefresh();
        }
    });

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.community_list_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        final MenuItem createMenuItem = menu.findItem(R.id.action_create_channel);
        ViewCustomMenuIconButtonBinding binding = ViewCustomMenuIconButtonBinding.inflate(getLayoutInflater());
        boolean isDark = PreferenceUtils.isUsingDarkTheme();
        int iconTint = isDark ? R.color.primary_200 : R.color.primary_300;
        if (getContext() == null) return;
        binding.icon.setImageDrawable(DrawableUtils.setTintList(getContext(), R.drawable.icon_create, iconTint));
        binding.icon.setBackgroundResource(isDark ? R.drawable.sb_button_uncontained_background_dark : R.drawable.sb_button_uncontained_background_light);
        View rootView = binding.getRoot();
        rootView.setOnClickListener(v -> onOptionsItemSelected(createMenuItem));
        createMenuItem.setActionView(rootView);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_create_channel && getActivity() != null) {
            Logger.d("++ create button clicked");
            Intent intent = new Intent(getActivity(), CreateOpenChannelActivity.class);
            createChannelLauncher.launch(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        setHasOptionsMenu(true);
    }
}
