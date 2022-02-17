# Change Log

### v2.2.4 (Fab 17, 2022) with Core SDK `v3.1.7`
* Added `setUseHeaderProfileImage(boolean)` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`.

### v2.2.3 (Jan 26, 2022) with Core SDK `v3.1.5`
* Added `List<DialogListItem> makeMessageContextMenu(BaseMessage)`,  `boolean onMessageContextMenuItemClicked(BaseMessage, View, int, DialogListItem)`, `saveFileMessage(FileMessage)` in `ChannelFragment`, `OpenChannelFragment`.
* Changed `ViewModelStoreOwner` from `Activity` to `Fragment`. 
* Improved stability.

### v2.2.2 (Dec 21, 2021) with Core SDK `v3.1.3`
* Improved stability.

### v2.2.1 (Dec 10, 2021) with Core SDK `v3.1.1`
* Improved stability.

### <strike>v2.2.0 (Nov 23, 2021) with Core SDK `v3.1.0`</strike> *DEPRECATED*
* Supported local caching.
  * Added `getInitResultHandler()` in `SendBirdUIKitAdapter`.
* Supported message threading.
  * Added `enum ReplyType { NONE, QUOTE_REPLY }`.
  * Added `setReplyType(ReplyType)` in `SendBirdUIKit`.
  * Added `getReplyType()` in `SendBirdUIKit`.
* Added `getClickableViewMap()` in `MessageViewHolder`.
* Removed `getClickableView()`, `getProfileView()` in `MessageViewHolder`.
* Added `setListItemClickListener(OnIdentifiableItemClickListener<BaseMessage>)`, `setListItemLongClickListener(OnIdentifiableItemLongClickListener<BaseMessage>)` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`.
* Deprecated `setItemClickListener(OnItemClickListener<BaseMessage>)`, `setItemLongClickListener(OnItemLongClickListener<BaseMessage>)` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`.
* Added `MessageListAdapter(GroupChannel, boolean)` in `MessageListAdapter`.
* Deprecated `MessageListAdapter(GroupChannel, OnItemClickListener<BaseMessage>)`, `MessageListAdapter(GroupChannel, OnItemClickListener<BaseMessage>, OnItemLongClickListener<BaseMessage>)`, `MessageListAdapter(GroupChannel, OnItemClickListener<BaseMessage>, OnItemLongClickListener<BaseMessage>, boolean)` in `MessageListAdapter`.
* Added `setItems(GroupChannel, List<BaseMessage>, OnMessageListUpdateHandler)` in `MessageListAdapter`.
* Deprecated `setItems(GroupChannel, List<BaseMessage>)` in `MessageListAdapter`.
* Added `OpenChannelMessageListAdapter(OpenChannel, boolean)` in `OpenChannelMessageListAdapter`.
* Deprecated `OpenChannelMessageListAdapter(OpenChannel, OnItemClickListener<BaseMessage>)`, `OpenChannelMessageListAdapter(OpenChannel, OnItemClickListener<BaseMessage>, OnItemLongClickListener<BaseMessage>)`, `OpenChannelMessageListAdapter(OpenChannel, OnItemClickListener<BaseMessage>, OnItemLongClickListener<BaseMessage>, boolean)` in `OpenChannelMessageListAdapter`.
* Added `setItems(OpenChannel, List<BaseMessage>, OnMessageListUpdateHandler)` in `OpenChannelMessageListAdapter`.
* Deprecated `setItems(OpenChannel, List<BaseMessage>)` in `OpenChannelMessageListAdapter`.
* Added `setOnListItemClickListener(OnIdentifiableItemClickListener<BaseMessage>)`, `setOnListItemLongClickListener(OnIdentifiableItemLongClickListener<BaseMessage>)` in `MessageListAdapter`, `OpenChannelMessageListAdapter`.
* Deprecated `setOnItemClickListener(OnItemClickListener<BaseMessage>)`, `setOnProfileClickListener(OnItemClickListener<BaseMessage>)`, `setOnItemLongClickListener(OnItemLongClickListener<BaseMessage>)` in `MessageListAdapter`, `OpenChannelMessageListAdapter`.

### v2.1.8 (Sep 23, 2021) with Core SDK `v3.0.172`
* Added `getMessageTootip(int count)` in `ChannelFragment`.
* Added `initFromForeground(SendBirdUIKitAdapter adapter, Context context)` in `SendBirdUIKit`.

### v2.1.7 (August 19, 2021) with Core SDK `v3.0.170`
* Added filtering logics for channel events by custom message list params.
* Added filtering logics for channel list events by custom channel list query.
* Improved handling of failed messages.

### v2.1.6 (July 20, 2021) with Core SDK `v3.0.168`
* Added `setEmptyIcon(int resId, ColorStateList tint)` in `CreateChannelFragment.Builder`, `InviteChannelFragment.Builder`, and `PromoteOperatorsFragment.Builder`.
* Added `setEmptyText(int resId)` in `CreateChannelFragment.Builder`, `InviteChannelFragment.Builder`, and `PromoteOperatorsFragment.Builder`.
* Upgraded Gradle version to `4.0.1`.

### v2.1.5 (Jun 8, 2021) with Core SDK `v3.0.166`
* Added support for [Firebase Cloud Messaging version 22.0.0](https://firebase.google.com/support/release-notes/android#messaging_v22-0-0).

### v2.1.4 (May 14, 2021) with Core SDK `v3.0.164`
* Fixed problems in API level 30.
    * Fixed camera launching issue.
    * Fixed video and file viewer launching issue.
* Upgraded Gradle version to `3.5.4`

### v2.1.3 (April 27, 2021) with Core SDK `v3.0.163`
* Targeted Android 11 (API 30).
* Supported Scoped storage.
    * The `requestLegacyExternalStorage` attribute has been deleted.
    * Used `Media Store API`.
* Improved stability.

### v2.1.2 (April 13, 2021) with Core SDK `v3.0.161`
* Added `showInputRightButtonAlways()` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`.
* From this version, it is not available on `jcenter`. This version can only be available from Sendbird's maven repository: `maven { url "https://repo.sendbird.com/public/maven" }`.

### v2.1.1 (March 30, 2021) with Core SDK `v3.0.160`
* Added `setHeaderTitle(String)` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`.
* Improved stability.
* This is the last release that will be available on `jcenter`. From the next release, SDK binary will be available from Sendbird's maven repository: `maven { url "https://repo.sendbird.com/public/maven" }`.

### v2.1.0 (March 18, 2021) with Core SDK `v3.0.159`
* Added Message Search features.
    * Added `MessageSearchActivity`, `MessageSearchFragment`, `MessageSearchAdpater` and `HighlightMessageInfo` classes.
    * Added `isSupportMessageSearch()` in `Available`.
    * Added `setHighlightMessageInfo(HighlightMessageInfo)` in `ChannelFragment.Builder`.
    * Added `setStartingPoint(long)` in `ChannelFragment.Builder`.
    * Added `setHighlightInfo(HighlightMessageInfo)` in `MessageListAdapter`.
    * Added `setHighlightInfo(HighlightMessageInfo)` in `MessageViewHolder`.
    * Added `IntentBuilder` class in `ChannelActivity`.
        * Added `setStartingPoint(long)` in `ChannelActivity.IntentBuilder`.
        * Added `setHighlightMessageInfo(HighlightMessageInfo)` in `ChannelActivity.IntentBuilder`.
        * Added `build()` in `ChannelActivity.IntentBuilder`.
* Added icon tint params methods.
    * Added `setHeaderLeftButtonIcon(int, ColorStateList)` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`, `ChannelListFragment.Builder`, `BannedListFragment.Builder`, `MemberListFragment.Builder`, `MutedMemberListFragment.Builder`, `OperatorListFragment.Builder`, `ParticipantsListFragment.Builder`, `InviteChannelFragment.Builder`, `ChannelSettingsFragment.Builder`, `OpenChannelSettingsFragment.Builder`, `CreateChannelFragment.Builder`, `ModerationFragment.Builder` and `PromoteOperatorsList.Builder`.
    * Added `setHeaderRightButtonIcon(int, ColorStateList)` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`,, `ChannelListFragment.Builder` `BannedListFragment.Builder`, `MemberListFragment.Builder`, `MutedMemberListFragment.Builder`, `OperatorListFragment.Builder`. 
    * Added `setEmptyIcon(int, ColorStateList)` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`, `ChannelListFragment.Builder`, `BannedListFragment.Builder`, `MemberListFragment.Builder`, `MutedMemberListFragment.Builder`, `OperatorListFragment.Builder`, `ParticipantsListFragment.Builder`. 
    * Added `setInputLeftButtonIcon(int, ColorStateList)` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`.
    * Added `setInputRightButtonIcon(int, ColorStateList)` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`.
* Added `setInputText(String)` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`.
* Added `setOnEditModeTextChangedListener(OnInputTextChangedListener)` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`.
* Added `setOnInputTextChangedListener(OnInputTextChangedListener)` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`.
* Deprecated `lastSeenAt` feature.
* Added IconSet.
    * `icon_done_all.png`
    * `icon_done.png`
    * `icon_emoji_more.png`
    * `icon_ban.png`
    * `icon_moderations.png`
    * `icon_mute.png`
    * `icon_question.png`
    * `icon_thumbnail_none.png`
    * `icon_notification_filled.png`
* Replaced IconSet.
    * `emoji_fail.png` -> `icon_question.png`
    * `emoji_more_large_dark.png` -> `icon_emoji_more.png`
    * `emoji_more_large_light.png` -> `icon_emoji_more.png`
    * `emoji_more_small_dark.png` -> `icon_emoji_more.png`
    * `emoji_more_small_light.png` -> `icon_emoji_more.png`
    * `icon_actions_delete.png` -> `icon_delete.png`
    * `icon_add_disabled_dark.png` -> `icon_add.png`
    * `icon_add_disabled_light.png` -> `icon_add.png`
    * `icon_avatar_broadcast_dark.png` -> `icon_broadcast.png`
    * `icon_avatar_broadcast_light.png` -> `icon_broadcast.png`
    * `icon_avatar_dark.png` -> `icon_user.png`
    * `icon_avatar_light.png` -> `icon_user.png`
    * `icon_banned.png` -> `icon_ban.png`
    * `icon_broadcast_preview.png` -> `icon_broadcast.png`
    * `icon_checkbox.png` -> Removed
    * `icon_close_dark.png` -> `icon_close.png`
    * `icon_delivered.png` -> `icon_done_all.png`
    * `icon_dummy.png` -> Removed
    * `icon_more_disabled.png` -> `icon_more.png`
    * `icon_mute_dark.png` -> `icon_mute.png`
    * `icon_mute.png` -> `icon_notification_filled.png`
    * `icon_muted.png` -> `icon_mute.png`
    * `icon_no_thumbnail_dark.png` -> `icon_thumbnail_none.png`
    * `icon_no_thumbnail_light.png` -> `icon_thumbnail_none.png`
    * `icon_read.png` -> `icon_done_all.png`
    * `icon_sent.png` -> `icon_done.png`
    * `icon_spinner_large.png` -> `icon_spinner.png`
    * `icon_success.png` -> `icon_done.png`
    * `icon_thumbnail_dark.png` -> `icon_photo.png`
    * `icon_thumbnail_light.png` -> `icon_photo.png`
    * `moderations.png` -> `icon_moderations.png`
    * `operator.png` -> `icon_operator.png`
    * `sb_default_profile_image_1.png` -> Removed
    * `sb_default_profile_image_2.png` -> Removed
    * `icon_checkbox_on.png` -> Removed
    * `icon_checkbox_off.png` -> Removed
* Removed unused drawables.
    * `selector_member_action_button.xml`
    * `selector_message_input_chooser_icon_dark.xml`
    * `selector_message_input_chooser_icon.xml`
    * `chatbubble_incoming_dark.9.png`
    * `chatbubble_incoming_light.9.png`
    * `chatbubble_incoming_pressed_dark.9.png`
    * `chatbubble_incoming_pressed_light.9.png`
    * `chatbubble_outgoing_dark.9.png`
    * `chatbubble_outgoing_light.9.png`
    * `chatbubble_outgoing_pressed_dark.9.png`
    * `chatbubble_outgoing_pressed_light.9.png`
    * `selector_my_user_message_bubble_dark.xml`
    * `selector_my_user_message_bubble_light.xml`
    * `selector_my_other_message_bubble_dark.xml`
    * `selector_my_other_message_bubble_light.xml`
    * `chatbubble_reactions_dark.xml`
    * `chatbubble_reactions_light.xml`
* Removed legacy attuributes.
    * `sb_dialog_view_left_button_text_appearance`
    * `sb_dialog_view_left_button_text_color`
    * `sb_dialog_view_left_button_background`
    * `sb_dialog_view_right_button_text_appearance`
    * `sb_dialog_view_right_button_text_color`
    * `sb_dialog_view_right_button_background`
    * `sb_dialog_view_alert_button_text_appearance`
    * `sb_dialog_view_alert_button_text_color`
    * `sb_dialog_view_alert_button_background`
    * `sb_dialog_view_cancel_button_text_appearance`
    * `sb_dialog_view_cancel_button_text_color`
    * `sb_dialog_view_cancel_button_background`    
* Changed ColorSet.
    * Changes `Primary-*` colors.
    * Changes `Secondary-*` colors.
    * Changes `Background-300` color.
    * Changes `Background-200` color.
    * Changes `Background-100` color.
    * Added `Background-50` color.
    * Added `Error-*` colors.
    * Removed `Error` color.
* Fixed `ChannelFragment` memory leak.
* Improved stability.

### v2.0.2 (January 26, 2021) with Core SDK `v3.0.156`
* Added `setEmptyIcon(int resId)` in `ChannelFragment.Builder`, `ChannelListFragment.Builder`, and `OpenChannelFragment.Builder`.
* Added `setEmptyText(int resId)` in `ChannelFragment.Builder`, `ChannelListFragment.Builder`, and `OpenChannelFragment.Builder`.

### v2.0.1 (January 11, 2021) with Core SDK `v3.0.154`
* Added `showMediaSelectDialog()` in `ChannelFragment` and `OpenChannelFragment`.
* Added `takeCamera()`, `takePhoto()`, and `takeFile()` in `ChannelFragment` and `OpenChannelFragment`.
* Added `setUseInputLeftButton(boolean useInputLeftButton)` in `ChannelFragment.Builder` and `OpenChannelFragment.Builder`.
* Support image resizing and compression
    * Added `setUseImageCompression(boolean)` and `shouldUseImageCompression()` in `SendBirdUIKit`
    * Added `setCompressQuality(int)` and `getCompressQuality()` in `SendBirdUIKit`
    * Added `setResizingSize(Pair<Integer, Integer>)` and `getResizingSize()` in `SendBirdUIKit`
* Fix compile issue on gradle 4.1.1

### v2.0.0 (December 23, 2020) with Core SDK `v3.0.153`
* Added OpenChannel features.
    * Added `OpenChannelSettingsActivity`, `ParticipantsListActivity`.
    * Added `OpenChannelFragment`, `OpenChannelSettingsFragment` and `ParticipantsListFragment`.
    * Added `onBeforeUpdateOpenChannel(OpenChannelParams params)` in `CustomParamsHandler`.
    * Added `styles_overlay.xml`.
* Added `enum KeyboardDisplayType { Plane, Dialog }`.
    * Added `setKeyboardDisplayType(KeyboardDisplayType type)` in `ChannelFragment.Builder`.
    * Added `setKeyboardDisplayType(KeyboardDisplayType type)` in `OpenChannelFragment.Builder`.
* Improved stability.

### v1.2.5 (December 10, 2020) with Core SDK `v3.0.152`
* Add opened interfaces for loading dialog
    * Added `shouldShowLoadingDialog()` in `ChannelFragment`, `MemberListFragment`, `BannedListFragment`, `OperatorListFragment`, `ModerationFragment`, and `MutedMemberListFragment`.
    * Added `shouldDismissLoadingDialog()` in `ChannelFragment`, `MemberListFragment`, `BannedListFragment`, `OperatorListFragment`, `ModerationFragment`, and `MutedMemberListFragment`.
* Improved stability.

### v1.2.4 (November 17, 2020) with Core SDK `v3.0.150`
* Disabled android.enableJetifier

### v1.2.3 (October 19, 2020) with Core SDK `v3.0.149`
* Added `setUseHeaderRightButton(boolean)` in `ChannelSettingsFragment`, `CreateChannelFragment`, `InviteChannelFragment`, `PromoteOperatorsFragment`.
* Improved stability.

### v1.2.2 (September 17, 2020) with Core SDK `v3.0.145`
* Supported user profile.
    * Added `setUseDefaultUserProfile(boolean)` in `SendBirdUIKit`
    * Added `setCustomParamsHandler(CustomParamsHandler handler)` in `SendbirdUIKit`
    * Added `setOnProfileClickListener`, `setUseUserProfile` in `ChannelFragment`, `MemberListFragment`, `BannedListFragment`, `MutedMemberListFragment`, `OperatorListFragment`.

### v1.2.1 (September 10, 2020) with Core SDK `v3.0.144`
* Supported message group UI.
* Added `setUseMessageGroupUI` in `ChannelFragment`.

### v1.2.0 (August 27, 2020) with Core SDK `v3.0.142`
* Added operator features
    * Member managing (ban/unban, mute/unmute, promote/dismiss)
    * Moderation feature for the operator
    * Channel freezing/unfreezing
    * Channel creator will be the default operator
    * Added `ModerationActivity`, `BannedListActivity`, `OperatorListActivity`, `MutedMemberListActivity`, and `PromoteOperatorsActivity`.
    * Added `ModerationFragment`, `BannedListFragment`, `OperatorListFragment`, `MutedMemberListFragment`, and `PromoteOperatorsFragment`.
* Added GroupChannel type selector.
* Added `newIntentFromCustomActivity` on each activities on UIKit
* Added `setCreateButtonText(String text)` in `InviteChannelFragment.Builder`
* Added `setInvitedButtonText(String text)` in `CreateChannelFragment.Builder`
* Deprecated `setMemberSettingClickListener(View.OnClickListener listener)` in `ChannelSettingsFragment`.
* Removed button attributes (Replaced with the button attributes of each component.)
    * `sb_button_contained_style`
    * `sb_button_uncontained_style`
    * `sb_button_warning_style`
    * `sb_button_cancel_style`
    * `sb_icon_button_style`
* Removed button styles (Replaced with the button attributes of each component.)
    * `Widget.SendBird.Button`
    * `Widget.SendBird.Button.Contained`
    * `Widget.SendBird.Button.Uncontained`
    * `Widget.SendBird.Button.Alert`
    * `Widget.SendBird.Button.Cancel`
    * `Widget.SendBird.IconButton`

### v1.1.3 (August 13, 2020) with Core SDK `v3.0.139`
* Implement OG tag messages.
* Improved stability.

### v1.1.2 (July 22, 2020)
* The following functions have been opened to send custom data.
    * Added `newIntentFromCustomActivity()` in each UIKit Activity.
* Improved stability.

### v1.1.1 (July 16, 2020) with Core SDK `v3.0.138`
* The following functions have been opened to send custom data.
    * Added `onBeforeInviteUsers()` with `userIds` in `InviteChannelFragment`.
    * Added `inviteUser()` with `userIds` in `InviteChannelFragment`.
    * Added `onNewUserInvited()` in `InviteChannelFragment`.
    * Added `setInviteButtonText()` in `InviteChannelFragment`.
    * Added `setInviteButtonEnabled()` in `InviteChannelFragment`.
    * Added `setCreateButtonText()` in `CreateChannelFragment`.
    * Added `setCreateButtonEnabled()` in `CreateChannelFragment`.
    * Added `setMemberSettingClickListener()` with `onClickListener` in `ChannelSettingsFragment.Builder`.
* Improved stability.
    
### v1.1.0 (July 10, 2020) with Core SDK `v3.0.137`
* Supports reaction feature.
    * Added `EmojiManager` class.
    * Added `EmojiListAdapter` class.
    * Added `EmojiReactionListAdapter` class.
    * Added `EmojiReactionUserListAdapter` class.
    * Added `setEmojiReactionClickListener(OnEmojiReactionClickListener emojiReactionClickListener)` in `ChannelFragment.Builder`.
    * Added `setEmojiReactionLongClickListener(OnEmojiReactionLongClickListener emojiReactionLongClickListener)` in `ChannelFragment.Builder`.
    * Added `setEmojiReactionMoreButtonClickListener(OnItemClickListener<BaseMessage> emojiReactionMoreButtonClickListener)` in `ChannelFragment.Builder`.
* Improved stability.

### v1.0.5 (June 25, 2020) with Core SDK `v3.0.136`
* Added - Unknown type message
* Added `setGroupChannelListQuery()` in `ChannelListFragment.Builder`.
* Added `setMessageListParams()` in `ChannelFragment.Builder`.

### v1.0.4 (June 14, 2020) with Core SDK `v3.0.133`
* The following functions have been opened to send custom data.
    * Added `onBeforeSendUserMessage()` with `UserMessageParams` in `ChannelFragment`.
    * Added `onBeforeUpdateUserMessage()` with `UserMessageParams` in `ChannelFragment`.
    * Added `sendUserMessage()` with `UserMessageParams` in `ChannelFragment`.
    * Added `onBeforeSendFileMessage()` with `FileMessageParams` in `ChannelFragment`.
    * Added `sendFileMessage()` with `Uri` in `ChannelFragment`.
    * Added `updateUserMessage()` with `messageId` and `UserMessageParams` in `ChannelFragment`.
    * Added `deleteMessage()` in `ChannelFragment`.
    * Added `resendMessage()` in `ChannelFragment`.
    * Added `onBeforeCreateGroupChannel()` with `GroupChannelParams` in `CreateChannelFragment`.
    * Added `createGroupChannel()` with `GroupChannelParams` in `CreateChannelFragment`.
    * Added `onNewChannelCreated()` in `CreateChannelFragment`.
    * Added `onBeforeUpdateGroupChannel()` with `GroupChannelParams` in `ChannelSettingsFragment`.
    * Added `updateGroupChannel()` with `GroupChannelParams` in `ChannelSettingsFragment`.
    * Added `leaveChannel()` in `ChannelSettingsFragment`.
    * Added `leaveChannel()` in `ChannelListFragment`.
    * Added `setCustomChannelFragment()` in `ChannelFragment.Builder`.
    * Added `setCustomChannelListFragment()` in `ChannelListFragment.Builder`.
    * Added `setCustomInviteChannelFragment()` in `InviteChannelFragment.Builder`.
    * Added `setCustomChannelSettingsFragment()` in `ChannelSettingsFragment.Builder`.
    * Added `setCustomCreateChannelFragment()` in `CreateChannelFragment.Builder`.
    * Added `setCustomMemberListFragment()` in `MemberListFragment.Builder`.
    * Added `createChannelFragment()` with `channelUrl` in `ChannelActivity`.
    * Added `createChannelListFragment()`, `createRedirectChannelActivityIntent()` in `ChannelListActivity`.
    * Added `createInviteChannelFragment()` with `channelUrl` in `InviteChannelActivity`.
    * Added `createChannelSettingsFragment()` with `channelUrl` in `ChannelSettingsActivity`.
    * Added `createCreateChannelFragment()` in `CreateChannelActivity`.
    * Added `createMemberListFragment()` with `channelUrl` in `MemberListActivity`.

### v1.0.3 (May 29, 2020) with Core SDK `v3.0.132`
* Improved stability.

### v1.0.2 (May 14, 2020)
* Added - `SendBirdUIKit.setLogLevel(LogLevel level)`
* Added - `LogLevel` in `SendBirdUIKit`
  * `ALL`, `INFO`, `WARN`, `ERROR`.

### v1.0.1 (Apr 29, 2020) with Core SDK `v3.0.129`
* Added - UIKit version information to User-Agent
* Improved stability.

### v1.0.0 (Apr 1, 2020)
* First release.
