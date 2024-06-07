package com.sendbird.uikit.samples.customization

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.sendbird.uikit.samples.R
import com.sendbird.uikit.samples.common.extensions.cleanUpPreviousSampleSettings
import com.sendbird.uikit.samples.common.extensions.logout
import com.sendbird.uikit.samples.customization.channel.showChannelHeaderSample
import com.sendbird.uikit.samples.customization.channel.showChannelLayoutSample
import com.sendbird.uikit.samples.customization.channel.showInputMenuSample
import com.sendbird.uikit.samples.customization.channel.showMessageClickSample
import com.sendbird.uikit.samples.customization.channel.showMessageDataSample
import com.sendbird.uikit.samples.customization.channel.showMessageFilteringSample
import com.sendbird.uikit.samples.customization.channel.showMessageMenuSample
import com.sendbird.uikit.samples.customization.channel.showMessageUISample
import com.sendbird.uikit.samples.customization.channel.showNewMessageTypeSample
import com.sendbird.uikit.samples.customization.channellist.showChannelItemFilteringSample
import com.sendbird.uikit.samples.customization.channellist.showChannelItemUISample
import com.sendbird.uikit.samples.customization.channellist.showNewChannelItemTypeSample
import com.sendbird.uikit.samples.customization.channelsettings.showAppendNewCustomGroupChannelSettingsMenuSample
import com.sendbird.uikit.samples.customization.channelsettings.showCustomGroupChannelSettingsMenuSample
import com.sendbird.uikit.samples.customization.channelsettings.showHidingChannelSettingsMenuSample
import com.sendbird.uikit.samples.customization.global.showAdapterProvidersSample
import com.sendbird.uikit.samples.customization.global.showFragmentProvidersSample
import com.sendbird.uikit.samples.customization.global.showModuleProvidersSample
import com.sendbird.uikit.samples.customization.global.showViewModelProvidersSample
import com.sendbird.uikit.samples.customization.moderation.showModerationGroupChannelSample
import com.sendbird.uikit.samples.customization.moderation.showModerationOpenChannelSample
import com.sendbird.uikit.samples.customization.userlist.showCustomMemberContextMenuSample
import com.sendbird.uikit.samples.customization.userlist.showUserItemDataSourceSample
import com.sendbird.uikit.samples.customization.userlist.showUserItemFilteringSample
import com.sendbird.uikit.samples.customization.userlist.showUserItemSelectSample
import com.sendbird.uikit.samples.customization.userlist.showUserItemUISample
import com.sendbird.uikit.samples.databinding.ActivityCustomizationHomeBinding
import com.sendbird.uikit.samples.databinding.ViewCustomizationListHeaderBinding
import com.sendbird.uikit.samples.databinding.ViewCustomizationListItemBinding

class CustomizationHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCustomizationHomeBinding.inflate(layoutInflater).apply {
            setContentView(root)
            logout.setOnClickListener { logout() }
            customizationList.adapter = CustomizationListAdapter(createCustomizationList())
        }
    }

    private fun createCustomizationList() = listOf(
        // region global customization
        CustomizationItem(
            isHeader = true,
            title = getString(R.string.text_list_title_global)
        ),
        CustomizationItem(
            title = getString(R.string.text_title_fragment_providers_sample),
            description = getString(R.string.text_desc_fragment_providers_sample),
        ) { showFragmentProvidersSample(this) },
        CustomizationItem(
            title = getString(R.string.text_title_module_providers_sample),
            description = getString(R.string.text_desc_module_providers_sample),
        ) { showModuleProvidersSample(this) },
        CustomizationItem(
            title = getString(R.string.text_title_viewmodel_providers_sample),
            description = getString(R.string.text_desc_viewmodel_providers_sample),
        ) { showViewModelProvidersSample(this) },
        CustomizationItem(
            title = getString(R.string.text_title_adapter_providers_sample),
            description = getString(R.string.text_desc_adapter_providers_sample),
        ) { showAdapterProvidersSample(this) },
        // endregion

        // region channel customization
        CustomizationItem(
            isHeader = true,
            title = getString(R.string.text_list_title_channel)
        ),
        CustomizationItem(
            title = getString(R.string.text_title_message_ui_sample),
            description = getString(R.string.text_desc_message_ui_sample)
        ) { showMessageUISample(this) },
        CustomizationItem(
            title = getString(R.string.text_title_new_message_type_sample),
            description = getString(R.string.text_desc_new_message_type_sample),
        ) { showNewMessageTypeSample(this) },
        CustomizationItem(
            title = getString(R.string.text_title_message_click_sample),
            description = getString(R.string.text_desc_message_click_sample)
        ) { showMessageClickSample(this) },
        CustomizationItem(
            title = getString(R.string.text_title_message_menu_sample),
            description = getString(R.string.text_desc_message_menu_sample),
        ) { showMessageMenuSample(this) },
        CustomizationItem(
            title = getString(R.string.text_title_message_data_sample),
            description = getString(R.string.text_desc_message_data_sample)
        ) { showMessageDataSample(this) },
        CustomizationItem(
            title = getString(R.string.text_title_message_filtering_sample),
            description = getString(R.string.text_desc_message_filtering_sample),
        ) { showMessageFilteringSample(this) },
        CustomizationItem(
            title = getString(R.string.text_title_channel_layout_sample),
            description = getString(R.string.text_desc_channel_layout_sample)
        ) { showChannelLayoutSample(this) },
        CustomizationItem(
            title = getString(R.string.text_title_input_menu_sample),
            description = getString(R.string.text_desc_input_menu_sample)
        ) { showInputMenuSample(this) },
        CustomizationItem(
            title = getString(R.string.text_title_channel_header_sample),
            description = getString(R.string.text_desc_channel_header_sample),
        ) { showChannelHeaderSample(this) },
        // endregion

        // region channel list customization
        CustomizationItem(
            isHeader = true,
            title = getString(R.string.text_list_title_channel_list)
        ),
        CustomizationItem(
            title = getString(R.string.text_title_channel_item_ui_sample),
            description = getString(R.string.text_desc_channel_item_ui_sample)
        ) { showChannelItemUISample(this) },
        CustomizationItem(
            title = getString(R.string.text_title_new_channel_item_type_sample),
            description = getString(R.string.text_desc_new_channel_item_type_sample),
        ) { showNewChannelItemTypeSample(this) },
        CustomizationItem(
            title = getString(R.string.text_title_channel_item_filtering),
            description = getString(R.string.text_desc_channel_item_filtering)
        ) { showChannelItemFilteringSample(this) },
        // endregion

        // region channel settings customization
        CustomizationItem(
            isHeader = true,
            title = getString(R.string.text_list_title_channel_settings)
        ),
        CustomizationItem(
            title = getString(R.string.text_title_custom_channel_settings_menu_sample),
            description = getString(R.string.text_desc_custom_channel_settings_menu_sample)
        ) { showCustomGroupChannelSettingsMenuSample(this) },
        CustomizationItem(
            title = getString(R.string.text_title_append_new_channel_settings_menu_sample),
            description = getString(R.string.text_desc_append_new_channel_settings_menu_sample)
        ) { showAppendNewCustomGroupChannelSettingsMenuSample(this) },
        CustomizationItem(
            title = getString(R.string.text_title_hide_channel_settings_menu_sample),
            description = getString(R.string.text_desc_hide_channel_settings_menu_sample)
        ) { showHidingChannelSettingsMenuSample(this) },
        // endregion

        // region user list customization
        CustomizationItem(
            isHeader = true,
            title = getString(R.string.text_list_title_user_list)
        ),
        CustomizationItem(
            title = getString(R.string.text_title_user_item_ui_sample),
            description = getString(R.string.text_desc_user_item_ui_sample),
        ) { showUserItemUISample(this) },
        CustomizationItem(
            title = getString(R.string.text_title_user_item_select_sample),
            description = getString(R.string.text_desc_user_item_select_sample)
        ) { showUserItemSelectSample(this) },
        CustomizationItem(
            title = getString(R.string.text_title_user_item_filtering),
            description = getString(R.string.text_desc_user_item_filtering),
        ) { showUserItemFilteringSample(this) },
        CustomizationItem(
            title = getString(R.string.text_title_user_item_custom_datasource),
            description = getString(R.string.text_desc_user_item_custom_datasource),
        ) { showUserItemDataSourceSample(this) },
        CustomizationItem(
            title = getString(R.string.text_title_custom_member_context_menu),
            description = getString(R.string.text_desc_custom_member_context_menu),
        ) { showCustomMemberContextMenuSample(this) },
        // endregion

        // region user list customization
        CustomizationItem(
            isHeader = true,
            title = getString(R.string.text_title_moderation)
        ),
        CustomizationItem(
            title = getString(R.string.text_title_moderation_group_channel),
            description = getString(R.string.text_moderation_custom_group_channel_sample),
        ) { showModerationGroupChannelSample(activity = this) },
        CustomizationItem(
            title = getString(R.string.text_title_moderation_open_channel),
            description = getString(R.string.text_moderation_custom_open_channel_sample),
        ) { showModerationOpenChannelSample(activity = this) },
        // endregion
    )

    data class CustomizationItem(
        val isHeader: Boolean = false,
        val title: String,
        val description: String = "",
        var visibility: Int = View.GONE,
        val action: () -> Unit = {}
    )

    class CustomizationListAdapter(
        private val customizationList: List<CustomizationItem>
    ) : RecyclerView.Adapter<CustomizationListAdapter.CustomizationViewHolder>() {
        companion object {
            const val HEADER = 0
            const val ITEM = 1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomizationViewHolder {
            return when (viewType) {
                HEADER -> CustomizationHeaderView(
                    ViewCustomizationListHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )

                else -> CustomizationItemView(
                    ViewCustomizationListItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }

        override fun onBindViewHolder(holder: CustomizationViewHolder, position: Int) {
            holder.bind(customizationList[position])
        }

        override fun getItemCount(): Int = customizationList.size

        override fun getItemViewType(position: Int): Int = with(customizationList[position]) {
            if (isHeader) HEADER else ITEM
        }

        abstract class CustomizationViewHolder(itemView: View) : ViewHolder(itemView) {
            abstract fun bind(customizationItem: CustomizationItem)
        }

        inner class CustomizationItemView(
            private val binding: ViewCustomizationListItemBinding
        ) : CustomizationViewHolder(binding.root) {
            override fun bind(customizationItem: CustomizationItem) {
                binding.title.visibility = customizationItem.visibility
                binding.description.visibility = customizationItem.visibility
                binding.title.text = customizationItem.title
                binding.description.text = customizationItem.description
                binding.listItemLayout.setOnClickListener {
                    // Each of these settings for an existing custom can affect subsequent settings if they remain.
                    cleanUpPreviousSampleSettings()

                    // call action
                    customizationItem.action()
                }
            }
        }

        inner class CustomizationHeaderView(
            private val binding: ViewCustomizationListHeaderBinding
        ) : CustomizationViewHolder(binding.root) {
            override fun bind(customizationItem: CustomizationItem) {
                val position = absoluteAdapterPosition
                if (position == 0 || (position - 1 >= 0 && customizationList[position - 1].visibility == View.GONE)) {
                    binding.topDivider.visibility = View.GONE
                } else {
                    binding.topDivider.visibility = View.VISIBLE
                }
                if (position + 1 < customizationList.size && customizationList[position + 1].visibility == View.VISIBLE) {
                    binding.arrow.rotation = 180f
                } else {
                    binding.arrow.rotation = 0f
                }
                binding.title.text = customizationItem.title
                binding.root.setOnClickListener {
                    val pos = absoluteAdapterPosition
                    var endPosition = pos + 1
                    for (i in pos + 1 until customizationList.size) {
                        if (i >= customizationList.size || customizationList[i].isHeader) break
                        if (customizationList[i].visibility == View.VISIBLE) {
                            customizationList[i].visibility = View.GONE
                        } else {
                            customizationList[i].visibility = View.VISIBLE
                        }
                        endPosition = i
                    }
                    if (customizationList[endPosition].visibility == View.VISIBLE) {
                        binding.arrow.rotation = 180f
                    } else {
                        binding.arrow.rotation = 0f
                    }
                    notifyItemRangeChanged(pos + 1, endPosition - pos + 1)
                }
            }
        }
    }
}
