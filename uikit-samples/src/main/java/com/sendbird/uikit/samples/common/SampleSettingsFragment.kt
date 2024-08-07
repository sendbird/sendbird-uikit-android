package com.sendbird.uikit.samples.common

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.sendbird.android.SendbirdChat.autoBackgroundDetection
import com.sendbird.android.SendbirdChat.currentUser
import com.sendbird.android.SendbirdChat.getDoNotDisturb
import com.sendbird.android.SendbirdChat.setDoNotDisturb
import com.sendbird.android.SendbirdChat.updateCurrentUserInfo
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.CompletionHandler
import com.sendbird.android.handler.DoNotDisturbHandler
import com.sendbird.android.params.UserUpdateParams
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.consts.DialogEditTextParams
import com.sendbird.uikit.interfaces.OnEditTextResultListener
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit.model.DialogListItem
import com.sendbird.uikit.modules.components.StateHeaderComponent
import com.sendbird.uikit.samples.R
import com.sendbird.uikit.samples.common.consts.StringSet
import com.sendbird.uikit.samples.common.extensions.getDrawable
import com.sendbird.uikit.samples.common.extensions.isUsingDarkTheme
import com.sendbird.uikit.samples.common.preferences.PreferenceUtils
import com.sendbird.uikit.samples.common.widgets.WaitingDialog
import com.sendbird.uikit.samples.databinding.FragmentSampleSettingsBinding
import com.sendbird.uikit.samples.databinding.ViewCustomMenuTextButtonBinding
import com.sendbird.uikit.utils.ContextUtils
import com.sendbird.uikit.utils.DialogUtils
import com.sendbird.uikit.utils.FileUtils
import com.sendbird.uikit.utils.IntentUtils
import com.sendbird.uikit.utils.PermissionUtils
import com.sendbird.uikit.utils.TextUtils
import java.io.File
import java.util.Locale
import java.util.TimeZone

/**
 * Displays a settings screen.
 */
class SampleSettingsFragment : Fragment() {
    private lateinit var binding: FragmentSampleSettingsBinding
    private val headerComponent = StateHeaderComponent()
    private var mediaUri: Uri? = null
    private val appSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (context == null) return@registerForActivityResult
            val hasPermission = PermissionUtils.hasPermissions(requireContext(), *REQUIRED_PERMISSIONS)
            if (hasPermission) {
                showMediaSelectDialog()
            }
        }
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (PermissionUtils.getNotGrantedPermissions(it).isEmpty()) {
                showMediaSelectDialog()
            }
        }
    private val takeCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            autoBackgroundDetection = true
            val resultCode = result.resultCode
            if (resultCode != Activity.RESULT_OK || context == null) return@registerForActivityResult
            val mediaUri = mediaUri
            if (mediaUri != null) {
                val file = FileUtils.uriToFile(requireContext().applicationContext, mediaUri)
                updateUserProfileImage(file)
            }
        }
    private val getContentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            autoBackgroundDetection = true
            val intent = result.data
            val resultCode = result.resultCode
            if (resultCode != Activity.RESULT_OK || intent == null || context == null) return@registerForActivityResult
            val mediaUri = intent.data
            if (mediaUri != null) {
                val file = FileUtils.uriToFile(requireContext().applicationContext, mediaUri)
                updateUserProfileImage(file)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SendbirdUIKit.connect { _, _ -> initPage() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSampleSettingsBinding.inflate(inflater, container, false)
        headerComponent.params.title = getString(R.string.text_settings_header_title)
        headerComponent.params.setUseLeftButton(false)
        headerComponent.params.rightButtonText = getString(R.string.text_settings_header_edit_button)
        val header =
            headerComponent.onCreateView(requireContext(), inflater, binding.headerComponent, savedInstanceState)
        binding.headerComponent.addView(header)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.sample_settings_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        ViewCustomMenuTextButtonBinding.inflate(layoutInflater).apply {
            val isDark = PreferenceUtils.themeMode.isUsingDarkTheme()
            text.setText(R.string.text_settings_header_edit_button)
            text.setBackgroundResource(if (isDark) R.drawable.sb_button_uncontained_background_dark else R.drawable.sb_button_uncontained_background_light)
            text.setTextColor(
                ResourcesCompat.getColor(
                    resources,
                    if (isDark) R.color.primary_light else R.color.primary_main,
                    null
                )
            )
            val editMenuItem = menu.findItem(R.id.action_edit_profile)
            root.setOnClickListener { onOptionsItemSelected(editMenuItem) }
            editMenuItem.actionView = root
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_edit_profile) {
            Logger.d("++ edit button clicked")
            showEditProfileDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        autoBackgroundDetection = true
    }

    private fun requestPermission(permissions: Array<String>) {
        if (context == null || activity == null) return
        // 1. check permission
        val hasPermission = PermissionUtils.hasPermissions(requireContext(), *permissions)
        if (hasPermission) {
            showMediaSelectDialog()
            return
        }

        // 2. determine whether rationale popup should show
        val deniedList = PermissionUtils.getExplicitDeniedPermissionList(requireActivity(), *permissions)
        if (deniedList.isNotEmpty()) {
            showPermissionRationalePopup(deniedList[0])
            return
        }
        // 3. request permission
        permissionLauncher.launch(permissions)
    }

    private fun showPermissionRationalePopup(permission: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(com.sendbird.uikit.R.string.sb_text_dialog_permission_title))
        builder.setMessage(getPermissionGuideMessage(requireContext(), permission))
        builder.setPositiveButton(com.sendbird.uikit.R.string.sb_text_go_to_settings) { _: DialogInterface?, _: Int ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse("package:" + requireContext().packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            appSettingLauncher.launch(intent)
        }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(ContextCompat.getColor(requireContext(), com.sendbird.uikit.R.color.secondary_main))
    }

    private fun initPage() {
        if (context == null) {
            return
        }

        with(binding) {
            val isDarkTheme = PreferenceUtils.themeMode.isUsingDarkTheme()
            background.setBackgroundResource(if (isDarkTheme) R.color.background_600 else R.color.background_50)
            tvNickname.setTextColor(
                ResourcesCompat.getColor(
                    resources,
                    if (isDarkTheme) R.color.ondark_text_high_emphasis else R.color.onlight_text_high_emphasis,
                    null
                )
            )
            idDivider.setBackgroundResource(if (isDarkTheme) R.drawable.sb_line_divider_dark else R.drawable.sb_line_divider_light)
            tvUserIdBadge.setTextColor(
                ResourcesCompat.getColor(
                    resources,
                    if (isDarkTheme) R.color.ondark_text_mid_emphasis else R.color.onlight_text_mid_emphasis,
                    null
                )
            )
            tvUserId.setTextColor(
                ResourcesCompat.getColor(
                    resources,
                    if (isDarkTheme) R.color.ondark_text_high_emphasis else R.color.onlight_text_high_emphasis,
                    null
                )
            )
            profileDivider.setBackgroundResource(if (isDarkTheme) R.drawable.sb_line_divider_dark else R.drawable.sb_line_divider_light)
            itemDarkTheme.setBackgroundResource(if (isDarkTheme) R.drawable.selector_rectangle_dark600 else R.drawable.selector_rectangle_light)
            tvDarkThemeName.setTextColor(
                ResourcesCompat.getColor(
                    resources,
                    if (isDarkTheme) R.color.ondark_text_high_emphasis else R.color.onlight_text_high_emphasis,
                    null
                )
            )
            darkThemeDivider.setBackgroundResource(if (isDarkTheme) R.drawable.sb_line_divider_dark else R.drawable.sb_line_divider_light)
            itemDisturb.setBackgroundResource(if (isDarkTheme) R.drawable.selector_rectangle_dark600 else R.drawable.selector_rectangle_light)
            tvDisturbName.setTextColor(
                ResourcesCompat.getColor(
                    resources,
                    if (isDarkTheme) R.color.ondark_text_high_emphasis else R.color.onlight_text_high_emphasis,
                    null
                )
            )
            disturbDivider.setBackgroundResource(if (isDarkTheme) R.drawable.sb_line_divider_dark else R.drawable.sb_line_divider_light)
            itemHome.setBackgroundResource(if (isDarkTheme) R.drawable.selector_rectangle_dark600 else R.drawable.selector_rectangle_light)
            tvHomeName.setTextColor(
                ResourcesCompat.getColor(
                    resources,
                    if (isDarkTheme) R.color.ondark_text_high_emphasis else R.color.onlight_text_high_emphasis,
                    null
                )
            )
            signOutDivider.setBackgroundResource(if (isDarkTheme) R.drawable.sb_line_divider_dark else R.drawable.sb_line_divider_light)
            val switchTrackTint =
                if (isDarkTheme) com.sendbird.uikit.R.color.sb_switch_track_dark else com.sendbird.uikit.R.color.sb_switch_track_light
            val switchThumbTint =
                if (isDarkTheme) com.sendbird.uikit.R.color.sb_switch_thumb_dark else com.sendbird.uikit.R.color.sb_switch_thumb_light
            binding.itemDarkTheme.setOnClickListener {
                Logger.d("++ dark theme clicked")
                updateDarkTheme()
            }

            val useHeader = arguments?.getBoolean(StringSet.SETTINGS_USE_HEADER, true) ?: true
            val useDoNotDisturb = arguments?.getBoolean(StringSet.SETTINGS_USE_DO_NOT_DISTURB, true) ?: true
            this@SampleSettingsFragment.headerComponent.rootView?.visibility =
                if (useHeader) View.VISIBLE else View.GONE
            this@SampleSettingsFragment.headerComponent.setOnRightButtonClickListener {
                Logger.d("++ edit button clicked")
                showEditProfileDialog()
            }
            currentUser?.let {
                loadUserProfileUrl(it.profileUrl)
                binding.tvUserId.text = it.userId
                binding.tvNickname.text = it.nickname
            } ?: run {
                loadUserProfileUrl(PreferenceUtils.profileUrl)
                binding.tvUserId.text = PreferenceUtils.userId
                binding.tvNickname.text = PreferenceUtils.nickname
            }
            val iconTint = if (SendbirdUIKit.isDarkMode()) R.color.background_700 else R.color.background_50
            val themeBackgroundTint = if (SendbirdUIKit.isDarkMode()) R.color.background_300 else R.color.background_400
            binding.ivDarkThemeIcon.setImageDrawable(
                requireContext().getDrawable(R.drawable.icon_theme, iconTint)
            )
            binding.ivDarkThemeIcon.background =
                requireContext().getDrawable(R.drawable.shape_oval, themeBackgroundTint)
            binding.scDarkThemeSwitch.trackTintList = AppCompatResources.getColorStateList(
                requireContext(), switchTrackTint
            )
            binding.scDarkThemeSwitch.thumbTintList = AppCompatResources.getColorStateList(
                requireContext(), switchThumbTint
            )
            binding.scDarkThemeSwitch.isChecked = isDarkTheme
            SendbirdUIKit.setDefaultThemeMode(if (isDarkTheme) SendbirdUIKit.ThemeMode.Dark else SendbirdUIKit.ThemeMode.Light)
            binding.scDarkThemeSwitch.setOnClickListener {
                Logger.d("++ dark theme clicked")
                updateDarkTheme()
            }
            val disturbBackgroundTint = if (SendbirdUIKit.isDarkMode()) R.color.secondary_light else R.color.secondary_main
            binding.ivDisturbIcon.setImageDrawable(
                requireContext().getDrawable(R.drawable.icon_notifications_filled, iconTint)
            )
            binding.ivDisturbIcon.background =
                requireContext().getDrawable(R.drawable.shape_oval, disturbBackgroundTint)
            binding.itemDisturb.visibility = if (useDoNotDisturb) View.VISIBLE else View.GONE
            if (useDoNotDisturb) {
                binding.itemDisturb.setOnClickListener {
                    Logger.d("++ disturb clicked")
                    updateDoNotDisturb()
                }
                binding.scDisturbSwitch.trackTintList = AppCompatResources.getColorStateList(
                    requireContext(), switchTrackTint
                )
                binding.scDisturbSwitch.thumbTintList = AppCompatResources.getColorStateList(
                    requireContext(), switchThumbTint
                )
                getDoNotDisturb(DoNotDisturbHandler { b: Boolean, _: Int, _: Int, _: Int, _: Int, _: String?, _: SendbirdException? ->
                    PreferenceUtils.doNotDisturb = b
                    if (isActive) {
                        binding.scDisturbSwitch.isChecked = PreferenceUtils.doNotDisturb
                    }
                })
                binding.scDisturbSwitch.setOnClickListener {
                    Logger.d("++ disturb clicked")
                    updateDoNotDisturb()
                }
            }
            val homeBackgroundTint = SendbirdUIKit.getDefaultThemeMode().errorColorResId
            binding.ivHomeIcon.setImageDrawable(
                requireContext().getDrawable(R.drawable.icon_leave, iconTint)
            )
            binding.ivHomeIcon.background = requireContext().getDrawable(R.drawable.shape_oval, homeBackgroundTint)
            binding.itemHome.setOnClickListener {
                Logger.d("++ home clicked")
                activity?.finish()
            }
        }
    }

    private fun showEditProfileDialog() {
        if (context == null) return
        val items = arrayOf(
            DialogListItem(R.string.text_settings_change_user_nickname),
            DialogListItem(R.string.text_settings_change_user_profile_image)
        )
        DialogUtils.showListBottomDialog(requireContext(), items) { _: View?, _: Int, item: DialogListItem ->
            val key = item.key
            if (key == R.string.text_settings_change_user_nickname) {
                Logger.dev("change user nickname")
                val listener = OnEditTextResultListener { result: String ->
                    if (TextUtils.isEmpty(result)) {
                        return@OnEditTextResultListener
                    }
                    updateUserNickname(result)
                }
                val params =
                    DialogEditTextParams(getString(com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_name_hint))
                params.enableSingleLine = true
                DialogUtils.showInputDialog(
                    requireContext(),
                    getString(R.string.text_settings_change_user_nickname),
                    params, listener,
                    getString(com.sendbird.uikit.R.string.sb_text_button_save), null,
                    getString(com.sendbird.uikit.R.string.sb_text_button_cancel), null
                )
            } else if (key == R.string.text_settings_change_user_profile_image) {
                Logger.dev("change user profile")
                if (context == null) return@showListBottomDialog
                val hasPermission = PermissionUtils.hasPermissions(requireContext(), *REQUIRED_PERMISSIONS)
                if (hasPermission) {
                    showMediaSelectDialog()
                    return@showListBottomDialog
                }
                requestPermission(REQUIRED_PERMISSIONS)
            }
        }
    }

    private fun updateUserNickname(nickname: String) {
        if (context == null) return
        WaitingDialog.show(requireContext())
        val params = UserUpdateParams()
        params.nickname = nickname
        SendbirdUIKit.updateUserInfo(params) { e: SendbirdException? ->
            WaitingDialog.dismiss()
            if (e != null) {
                Logger.e(e)
                ContextUtils.toastError(context, R.string.text_error_update_user_information)
                return@updateUserInfo
            }
            PreferenceUtils.nickname = nickname
            if (isActive) {
                binding.tvNickname.text = nickname
            }
        }
    }

    private fun updateUserProfileImage(profileImage: File) {
        if (context == null) return
        WaitingDialog.show(requireContext())
        val params = UserUpdateParams()
        params.profileImageFile = profileImage
        updateCurrentUserInfo(params, CompletionHandler { e: SendbirdException? ->
            WaitingDialog.dismiss()
            if (e != null) {
                Logger.e(e)
                ContextUtils.toastError(context, R.string.text_error_update_user_information)
                return@CompletionHandler
            }
            val currentUser = currentUser
            if (currentUser != null) {
                val profileUrl = currentUser.profileUrl
                PreferenceUtils.profileUrl = profileUrl
                loadUserProfileUrl(profileUrl)
            }
        })
    }

    private fun updateDarkTheme() {
        val themeMode = if (SendbirdUIKit.isDarkMode()) SendbirdUIKit.ThemeMode.Light else SendbirdUIKit.ThemeMode.Dark
        SendbirdUIKit.setDefaultThemeMode(themeMode)
        PreferenceUtils.themeMode = themeMode
        binding.scDarkThemeSwitch.isChecked = themeMode.isUsingDarkTheme()
        if (activity != null) {
            requireActivity().finish()
            startActivity(requireActivity().intent)
        }
    }

    private fun updateDoNotDisturb() {
        binding.scDisturbSwitch.isChecked = !PreferenceUtils.doNotDisturb
        Logger.d("update do not disturb : " + !PreferenceUtils.doNotDisturb)
        setDoNotDisturb(
            !PreferenceUtils.doNotDisturb,
            0,
            0,
            23,
            59,
            TimeZone.getDefault().id,
            CompletionHandler { e: SendbirdException? ->
                if (e != null) {
                    ContextUtils.toastError(context, R.string.text_error_update_do_not_disturb)
                    if (isActive) {
                        binding.scDisturbSwitch.isChecked = PreferenceUtils.doNotDisturb
                    }
                    return@CompletionHandler
                }
                Logger.d("update do not disturb on callback : " + !PreferenceUtils.doNotDisturb)
                PreferenceUtils.doNotDisturb = !PreferenceUtils.doNotDisturb
            })
    }

    private fun showMediaSelectDialog() {
        if (context == null) return
        val items = arrayOf(
            DialogListItem(com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_camera),
            DialogListItem(com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_gallery)
        )
        DialogUtils.showListDialog(
            requireContext(),
            getString(com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image),
            items
        ) { _: View?, _: Int, item: DialogListItem ->
            try {
                val key = item.key
                if (key == com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_camera) {
                    takeCamera()
                } else if (key == com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_gallery) {
                    takePhoto()
                }
            } catch (e: Exception) {
                Logger.e(e)
            }
        }
    }

    private fun takeCamera() {
        autoBackgroundDetection = false
        mediaUri = FileUtils.createImageFileUri(requireContext())
        val uri = mediaUri ?: return
        val intent = IntentUtils.getCameraIntent(requireContext(), uri)
        if (IntentUtils.hasIntent(requireContext(), intent)) {
            takeCameraLauncher.launch(intent)
        }
    }

    private fun takePhoto() {
        autoBackgroundDetection = false
        val intent = IntentUtils.getImageGalleryIntent()
        getContentLauncher.launch(intent)
    }

    private val isActive: Boolean
        get() {
            val isDeactivated = isRemoving || isDetached || context == null
            return !isDeactivated
        }

    private fun loadUserProfileUrl(url: String) {
        if (isActive) {
            Glide.with(requireContext())
                .load(url)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(
                    requireContext().getDrawable(
                        R.drawable.icon_user,
                        SendbirdUIKit.getDefaultThemeMode().monoTintResId
                    )
                )
                .into(binding.ivProfileView)
        }
    }

    companion object {
        private val REQUIRED_PERMISSIONS = PermissionUtils.CAMERA_PERMISSION
        private fun getPermissionGuideMessage(context: Context, permission: String): String {
            val textResId: Int = if (Manifest.permission.CAMERA == permission) {
                com.sendbird.uikit.R.string.sb_text_need_to_allow_permission_camera
            } else {
                com.sendbird.uikit.R.string.sb_text_need_to_allow_permission_storage
            }
            return String.format(Locale.US, context.getString(textResId), ContextUtils.getApplicationName(context))
        }
    }
}
