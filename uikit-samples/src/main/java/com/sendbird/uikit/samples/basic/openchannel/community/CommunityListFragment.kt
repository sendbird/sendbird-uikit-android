package com.sendbird.uikit.samples.basic.openchannel.community

import android.app.Activity
import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.sendbird.uikit.activities.CreateOpenChannelActivity
import com.sendbird.uikit.fragments.OpenChannelListFragment
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit.samples.R
import com.sendbird.uikit.samples.common.extensions.getDrawable
import com.sendbird.uikit.samples.common.extensions.isUsingDarkTheme
import com.sendbird.uikit.samples.common.preferences.PreferenceUtils
import com.sendbird.uikit.samples.databinding.ViewCustomMenuIconButtonBinding

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
        ViewCustomMenuIconButtonBinding.inflate(layoutInflater).apply {
            val themeMode = PreferenceUtils.themeMode
            val isDark = themeMode.isUsingDarkTheme()
            if (context == null) return
            val iconTint = themeMode.secondaryTintResId
            icon.setImageDrawable(requireContext().getDrawable(R.drawable.icon_create, iconTint))
            icon.setBackgroundResource(
                if (isDark) R.drawable.sb_button_uncontained_background_dark
                else R.drawable.sb_button_uncontained_background_light
            )
            root.setOnClickListener { onOptionsItemSelected(createMenuItem) }
            createMenuItem.actionView = root
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_create_channel && context != null) {
            Logger.d("++ create button clicked")
            val intent = Intent(context, CreateOpenChannelActivity::class.java)
            createChannelLauncher.launch(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }
}
