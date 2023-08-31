package com.sendbird.uikit_messaging_android.openchannel.community

import android.app.Activity
import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.sendbird.uikit.activities.CreateOpenChannelActivity
import com.sendbird.uikit.fragments.OpenChannelListFragment
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit_messaging_android.R
import com.sendbird.uikit_messaging_android.databinding.ViewCustomMenuIconButtonBinding
import com.sendbird.uikit_messaging_android.utils.DrawableUtils
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils

/**
 * Displays an open channel list screen used for community.
 */
class CommunityListFragment : OpenChannelListFragment() {
    private val createChannelLauncher = registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
        Logger.d("++ create channel result=%s", result.resultCode)
        if (result.resultCode == Activity.RESULT_OK) {
            onRefresh()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.community_list_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val createMenuItem = menu.findItem(R.id.action_create_channel)
        val binding = ViewCustomMenuIconButtonBinding.inflate(
            layoutInflater
        )
        val isDark = PreferenceUtils.isUsingDarkTheme
        val iconTint = if (isDark) R.color.primary_200 else R.color.primary_300
        if (context == null) return
        binding.icon.setImageDrawable(DrawableUtils.setTintList(requireContext(), R.drawable.icon_create, iconTint))
        binding.icon.setBackgroundResource(
            if (isDark) R.drawable.sb_button_uncontained_background_dark
            else R.drawable.sb_button_uncontained_background_light
        )
        val rootView: View = binding.root
        rootView.setOnClickListener { onOptionsItemSelected(createMenuItem) }
        createMenuItem.actionView = rootView
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_create_channel && activity != null) {
            Logger.d("++ create button clicked")
            val intent = Intent(activity, CreateOpenChannelActivity::class.java)
            createChannelLauncher.launch(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }
}
