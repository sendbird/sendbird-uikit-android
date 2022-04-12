# Migration Guide

UIKit v3 beta for Android is now available. The biggest change from v2 to v3 is modularization, which allows you to build and customize views at a component level. You can execute key functions, such as list channels, using a fragment, module, and view model. Each fragment has a module that creates the view, and each module is made up of components. A fragment also has a corresponding `ViewModel` that provides the necessary data and APIs from Sendbird Chat SDK. This new architecture allows for easier and more detailed customization.

![Image|Image showing the basic concepts and architecture of modularization in UIKit for Android.](https://static.sendbird.com/docs/uikit-android-key-functions-modularization-concepts.png)

When migrating from v2 to v3, there are several breaking changes you need to remember. Since modules and view models are one of the main parts of the new architecture, you need to make changes to the existing codes in your client app. Refer to the breaking changes below.

---

## Key functions

Key functions are carried out on a screen basis, meaning each function corresponds to a single screen. In v3, a key function is composed of three main components: fragment, module, and `ViewModel`. Refer to the table below to see which key functions we provide and the components that make up each key function.

<div component="AdvancedTable" type="5B">

|Key function|Fragment|Module|Component|ViewModel|
|---|---|---|---|---|
|[List channels](/docs/uikit/v3/android/key-functions/list-channels)|ChannelListFragment|ChannelListModule|HeaderComponent<br/><br/>ChannelListComponent<br/><br/>StatusComponent|ChannelListViewModel|
|[Chat in a group channel](/docs/uikit/v3/android/key-functions/chatting-in-a-channel/chat-in-a-group-channel)|ChannelFragment|ChannelModule|ChannelHeaderComponent<br/><br/>MessageListComponent<br/><br/>MessageInputComponent<br/><br/>StatusComponent|ChannelViewModel|
|[Chat in an open channel](/docs/uikit/v3/android/key-functions/chatting-in-an-open-channel/chat-in-an-open-channel)|OpenChannelFragment|OpenChannelModule|OpenChannelHeaderComponent<br/><br/>OpenChannelMessageListComponent<br/><br/>OpenChannelMessageInputComponent<br/><br/>StatusComponent|OpenChannelViewModel|
|[Create a group channel](/docs/uikit/v3/android/key-functions/creating-a-channel/create-a-group-channel)|CreateChannelFragment|CreateChannelModule|SelectUserHeaderComponent<br/><br/>CreateChannelUserListComponent<br/><br/>StatusComponent|CreateChannelViewModel|
|[Configure group channel settings](/docs/uikit/v3/android/key-functions/configuring-channel-settings/configure-group-channel-settings)|ChannelSettingsFragment|ChannelSettingsModule|ChannelSettingsHeaderComponent<br/><br/>ChannelSettingsInfoComponent<br/><br/>ChannelSettingsMenuComponent|ChannelSettingsViewModel|
|[Configure open channel settings](/docs/uikit/v3/android/key-functions/configuring-channel-settings/configure-open-channel-settings)|OpenChannelSettingsFragment|OpenChannelSettingsModule|OpenChannelSettingsHeaderComponent<br/><br/>OpenChannelSettingsInfoComponent<br/><br/>OpenChannelSettingsMenuComponent|OpenChannelSettingsViewModel|
|[Invite users](/docs/uikit/v3/android/key-functions/invite-users)|InviteUserFragment|InviteUserModule|SelectUserHeaderComponent<br/><br/>InviteUserListComponent<br/><br/>StatusComponent|InviteUserViewModel|
|[Promote to operator](/docs/uikit/v3/android/key-functions/promote-to-operator)|PromoteOperatorFragment|PromoteOperatorModule|SelectUserHeaderComponent<br/><br/>PromoteOperatorListComponent<br/><br/>StatusComponent|PromoteOperatorViewModel|
|[List channel members](/docs/uikit/v3/android/key-functions/listing-users/list-channel-members)|MemberListFragment|MemberListModule|HeaderComponent<br/><br/>MemberListComponent<br/><br/>StatusComponent|MemberListViewModel|
|[List channel participants](/docs/uikit/v3/android/key-functions/listing-users/list-channel-participants)|ParticipantsListFragment|ParticipantListModule|HeaderComponent<br/><br/>ParticipantListComponent<br/><br/>StatusComponent|ParticipantListViewModel|
|[List banned users](/docs/uikit/v3/android/key-functions/listing-users/list-banned-users)|BannedUserListFragment|BannedUserListModule|HeaderComponent<br/><br/>BannedUserListComponent<br/><br/>StatusComponent|BannedUserListViewModel|
|[List muted members](/docs/uikit/v3/android/key-functions/listing-users/list-muted-members)|MutedMemberListFragment|MutedMemberListModule|HeaderComponent<br/><br/>MutedMemberListComponent<br/><br/>StatusComponent|MutedMemberListViewModel|
|[List operators](/docs/uikit/v3/android/key-functions/listing-users/list-operators)|OperatorListFragment|OperatorListModule|HeaderComponent<br/><br/>OperatorListComponent<br/><br/>StatusComponent|OperatorListViewModel|
|[Moderate channels and members](/docs/uikit/v3/android/key-functions/moderate-channels-and-members)|ModerationFragment|ModerationModule|HeaderComponent<br/><br/>ModerationListComponent|ModerationViewModel|
|[Search messages](/docs/uikit/v3/android/key-functions/search-messages)|MessageSearchFragment|MessageSearchModule|MessageSearchHeaderComponent<br/><br/>MessageSearchListComponent<br/><br/>StatusComponent|MessageSearchViewModel|

</div>

---

## Configuration changes

To migrate to the new version, open the `build.gradle` file at the application level. For both `Java` and `Kotlin`, add the code blocks and dependencies as follows:

```groovy
android {
	defaultConfig {
		minSdkVersion 21
	}
	buildFeatures {
		viewBinding true
	}
}
```

---

## API changes in all fragments

In v3, there are new changes to APIs that create and customize fragments. Refer to the breaking changes that apply to all fragments in the UIKit below.

### Change default setter method value

In v2, the default value of the setter method in a fragment was set to **false**. But in v3, the value has changed to **true**. For example, the default value of the `setUseHeader()` method in the `HeaderComponent` of `ChannelListFragment` was previously **false** but it's now changed to **true**. In fragments that previously didn't use a header region in v2, you must now manually change the value to **false** if you don't wish to use it in v3. When using custom fragments, call the `onConfigureParams` method to access the setter methods that were previously provided by the builder. Refer to the codes below.

<div component="AdvancedCode" title="v2">

```java
Fragment fragment = new ChannelListFragment.Builder().build();
```

</div>

<div component="AdvancedCode" title="v3">

```java
// Use non-customized fragments through the builder.
Fragment fragment = new ChannelListFragment.Builder()
	.setUseHeader(false)
	.build();

// Use custom fragments.
public class CustomChannelListFragment extends ChannelListFragment {
	@Override
	protected void onConfigureParams(@NonNull ChannelListModule module, @NonNull Bundle args) {
		final ChannelListModule.Params params = module.getParams();
		params.setUseHeader(false);
	}
}
```

</div>

### Customize theme

Starting in v3, you can't apply a custom theme to customized fragments using a `fragment.builder()` class. In order to do so, you must call the `Params` class of the fragment and set the style resource as a parameter. For non-custom fragments, you can apply a custom theme using the builder class. See the codes below.

<div component="AdvancedCode" title="v2">

```java
Fragment fragment = new ChannelListFragment.Builder(R.style.custom_theme_resource_id).build();
```

</div>

<div component="AdvancedCode" title="v3">

```java
// Apply custom theme to non-customized fragments through the builder.
Fragment fragment = new ChannelListFragment.Builder(R.style.custom_theme_resource_id).build();

// Apply custom theme to customized fragments.
public class CustomChannelListFragment extends ChannelListFragment {
	@Override
	protected ChannelListModule onCreateModule(Bundle args) {
		final ChannelListModule.Params params = new ChannelListModule.Params(context, R.string.custom_theme_resource_id);
		return new ChannelListModule(requireContext(), params);
	}
}
```

</div>

> __Note__: Go to the [Customize style resource](/docs/uikit/v3/android/customization/customize-style) page to learn more.

</div>

---

## Custom fragment changes

Unlike v2, the new version doesn't allow you to use custom fragments through `fragment.builder()` to create a view. You can only use default fragments through the builder class. See the guide below on how to build your own custom fragment.

### Create a custom fragment

1. Inherit the fragment you wish to make changes to and create a custom fragment.
2. In each `fragment.builder()` class, there are UI-related APIs such as view properties, methods, and event handlers. To customize each fragment, you must override those setter methods. Refer to the following codes on how to build a custom `ChannelListFragment` as an example.

<div component="AdvancedCode" title="v2">

```java
ChannelListFragment Fragment = new ChannelListFragment.Builder()
    .setCustomChannelListFragment(new CustomChannelListFragment())
    .setUseHeader(true)
    .setHeaderTitle("Channels")
    .setUseHeaderLeftButton(true)
    .setUseHeaderRightButton(true)
    .setHeaderLeftButtonIconResId(R.drawable.icon_arrow_left)
    .setHeaderRightButtonIconResId(R.drawable.icon_create)
    .setHeaderLeftButtonListener(leftButtonListener)
    .setHeaderRightButtonListener(rightButtonListener)
    .setChannelListAdapter(adapter)
    .setItemClickListener(itemClickListener)
    .setItemLongClickListener(itemLongClickListener)
    .setGroupChannelListQuery(query)
    .setEmptyIcon(R.drawable.icon_chat)
    .setEmptyText(R.string.text_empty)
    .build();
```

</div>

<div component="AdvancedCode" title="v3">

```java
public class CustomChannelListFragment extends ChannelListFragment {
	@Override
	protected void onConfigureParams(@NonNull ChannelListModule module, @NonNull Bundle args) {
		final HeaderComponent.Params headerParams = module.getHeaderComponent().getParams();
		headerParams.setTitle("title");
		headerParams.setUseRightButton(false);
		headerParams.setUseLeftButton(false);
		headerParams.setLeftButtonIcon(leftButtonDrawable);
		headerParams.setRightButtonIcon(rightButtonDrawable);

		final StatusComponent.Params statusParams = module.getStatusComponent().getParams();
		statusParams.setEmptyIcon(emptyDrawable);
		statusParams.setEmptyIconTint(emptyIconTint);
		statusParams.setEmptyText("empty text");
	}

	@Override
	protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull ChannelListModule module, @NonNull ChannelListViewModel viewModel) {
		super.onBeforeReady(status, module, viewModel);
		module.getChannelListComponent().setAdapter(adapter);
	}

	@Override
	protected void onBindHeaderComponent(@NonNull HeaderComponent headerComponent, @NonNull ChannelListViewModel viewModel) {
		super.onBindHeaderComponent(headerComponent, viewModel);
		headerComponent.setOnLeftButtonClickListener(leftButtonListener);
		headerComponent.setOnRightButtonClickListener(rightButtonListener);
	}

	@Override
	protected void onBindChannelListComponent(@NonNull ChannelListComponent channelListComponent, @NonNull ChannelListViewModel viewModel) {
		super.onBindChannelListComponent(channelListComponent, viewModel);
		channelListComponent.setOnItemClickListener(itemClickListener);
		channelListComponent.setOnItemLongClickListener(itemLongClickListener);
	}
}
```

</div>

#### List of methods to override

The following table shows a list of methods you must override to build a custom fragment.

<div component="AdvancedTable" title="v3" type="2B">

|Method|Description|
|---|---|
|onConfigureParams(ChannelListModule, Bundle)|Specifies the method to change the value of view properties using the parameters of the corresponding component.|
|onBeforeReady(ReadyStatus, ChannelListModule, ChannelListViewModel)|Specifies the method to set custom adapters in the component once a view has been created.|
|onBindHeaderComponent()|Specifies the method to bind event handlers to `HeaderComponent` of the channel list screen.|
|onBindChannelListComponent(ChannelListComponent, ChannelListViewModel)|Specifies the method to bind event handlers to `ChannelListComponent` of the channel list screen.|

</div>

### Set a custom fragment factory

After creating a custom fragment, follow the guide below on how to set a custom fragment factory. In UIKit v3, all activities use the `UIKitFragmentFactory` class to create a fragment. `UIKitFragmentFactory` is a global class that provides and manages all fragments used in Sendbird UIKit. While an activity creates the basic UI screen and allows the user to navigate between different screens, the fragment within the activity is what allows you to customize components and manage data.

If you wish to customize a fragment, you need to inherit the `UIKitFragmentFactory` class and override the method that creates the fragment. Then, you must return the customized fragment in order to apply the customization throughout the UIKit.

> __Note__: If you're only using fragments to build a screen in UIKit instead of using an activity, you can skip the following steps.

1. Inherit the `UIKitFragmentFactory` class to create a custom `UIKitFragmentFactory`.
2. Override the method that creates the fragment you wish to customize and return the custom fragment. When returning the fragment, the `Bundle` class containing necessary data to build a view is also returned as parameters.

<div>

```java
public class CustomFragmentFactory extends UIKitFragmentFactory {
	@Override
	public Fragment newChannelFragment(@NonNull String channelUrl, @NonNull Bundle args) {
		final CustomChannelFragment fragment = new CustomChannelFragment();
		args.putString("KEY_CHANNEL_URL", channelUrl);
		fragment.setArguments(args);
		return fragment;
	}
}
```

</div>

3. Set the custom fragment factory to `Application` using `SendbirdUIKit.setUIKitFragmentFactory(UIKitFragmentFactory)`.

```java
public class MyApplication extends Application {
	@Override
	public void onCreate() {
		SendbirdUIKit.setUIKitFragmentFactory(new CustomFragmentFactory());
	}
}
```

#### List of methods to inherit

The following table shows a list of methods you must inherit to create a new fragment in each key function.

<div component="AdvancedTable" type="3B">

|Key function|Fragment|Method|
|---|---|---|
|List channels|ChannelListFragment|Fragment newChannelListFragment(Bundle)|
|Chat in a group channel|ChannelFragment|Fragment newChannelFragment(String, Bundle)|
|Chat in an open channel|OpenChannelFragment|Fragment newOpenChannelFragment(String, Bundle)|
|Create a group channel|CreateChannelFragment|Fragment newCreateChannelFragment(CreatableChannelType, Bundle)|
|Configure group channel settings|ChannelSettingsFragment|Fragment newChannelSettingsFragment(String, Bundle)|
|Configure open channel settings|OpenChannelSettingsFragment|Fragment newOpenChannelSettingsFragment(String, Bundle)|
|Invite users|InviteUserFragment|Fragment newInviteUserFragment(String, Bundle)|
|Promote to operator|PromoteOperatorFragment|Fragment newPromoteOperatorFragment(String, Bundle)|
|List channel members|MemberListFragment|Fragment newMemberListFragment(String, Bundle)|
|List channel participants|ParticipantListFragment|Fragment newParticipantListFragment(String, Bundle)|
|List banned users|BannedUserListFragment|Fragment newBannedUserListFragment(String, Bundle)|
|List muted members|MutedMemberListFragment|Fragment newMutedMemberListFragment(String, Bundle)|
|List operators|OperatorListFragment|Fragment newOperatorListFragment(String, Bundle)|
|Moderate channels and members|ModerationFragment|Fragment newModerationFragment(String, Bundle)|
|Search messages|MessageSearchFragment|Fragment newMessageSearchFragment(String, Bundle)|

</div>

---

## API changes in each fragment

When migrating from v2 to v3, you should be aware of some changes to the setter methods in the builder class of each fragment. Refer to the codes below to see how to migrate the changed APIs for each fragment builder.

### ChannelListFragment.Builder

The following methods have changed names in v3.

<div component="AdvancedCode" title="v2">

```java
Fragment fragment = new ChannelListFragment.Builder()
	.setHeaderLeftButtonListener(leftButtonListener)
	.setHeaderRightButtonListener(rightButtonListener)
	.setItemClickListener(itemClickListener)
	.setItemLongClickListener(itemLongClickListener)
	.build();
```

</div>

<div component="AdvancedCode" title="v3">

```java
Fragment fragment = new ChannelListFragment.Builder()
	.setOnHeaderLeftButtonClickListener(leftButtonListener)
	.setOnHeaderRightButtonClickListener(rightButtonListener)
	.setOnItemClickListener(itemClickListener)
	.setOnItemLongClickListener(itemLongClickListener)
	.build();
```

</div>

### ChannelFragment.Builder

The following methods have changed names in v3.

<div component="AdvancedCode" title="v2">

```java
Fragment fragment = new ChannelFragment.Builder(channelUrl)
	.setHeaderLeftButtonListener(leftButtonListener)
	.setHeaderRightButtonListener(rightButtonListener)
	.setInputLeftButtonListener(inputClickListener)
	.setOnProfileClickListener(profileClickListener)
	.build();
```

</div>

<div component="AdvancedCode" title="v3">

```java
Fragment fragment = new ChannelFragment.Builder(channelUrl)
	.setOnHeaderLeftButtonClickListener(leftButtonListener)
	.setOnHeaderRightButtonClickListener(rightButtonListener)
	.setOnInputLeftButtonClickListener(inputClickListener)
	.setOnMessageProfileClickListener(profileClickListener)
	.build();
```

</div>

In the `ChannelFragment.Builder` class, the `setListItemClickListener` method and `setListItemLongClickListener` method have separated into individual event listener methods for each view item.

<div component="AdvancedCode" title="v2">

```java
Fragment fragment = new ChannelFragment.Builder(channelUrl)
	.setListItemClickListener(identifiableItemClickListener)
	.setListItemLongClickListener(identifiableItemLongClickListener)
	.build();

// Or use the code below.

public class CustomChannelListFragment extends ChannelListFragment {
	@Override
	public void onIdentifiableItemClick(View view, String identifier, int position, BaseMessage message) {
		switch (identifier) {
			case StringSet.Chat:
				break;
			case StringSet.Profile:
				break;
			case StringSet.QuoteReply:
				break;
		}
	}
}
```

</div>

<div component="AdvancedCode" title="v3">

```java
Fragment fragment = new ChannelFragment.Builder(channelUrl)
	.setOnMessageClickListener(messageClickListener)
	.setOnQuoteReplyMessageClickListener(quoteReplyMessageClickListener)
	.setOnMessageProfileclickListener(messageProfileClickListener)
	.setOnMessageLongClickListener(messageLongClickListener)
	.setOnQuoteReplyMessageLongClickListener(quoteReplyMessageLongClickListener)
	.setOnMessageProfileLongClickListener(messageProfileLongClickListener)
	.build();

// Or use the code below.

public class CustomChannelListFragment extends ChannelListFragment {
	@Override
	protected void onMessageClicked(@NonNull View view, int position, @NonNull BaseMessage message) {}

	@Override
	protected void onMessageProfileClicked(@NonNull View view, int position, @NonNull BaseMessage message) {}

	@Override
	protected void onQuoteReplyMessageClicked(@NonNull View view, int position, @NonNull BaseMessage message) {}
}
```

</div>

### OpenChannelFragment.Builder

The following methods have changed names in v3.

<div component="AdvancedCode" title="v2">

```java
Fragment fragment = new OpenChannelFragment.Builder(channelUrl)
	.setHeaderLeftButtonListener(leftButtonListener)
	.setHeaderRightButtonListener(rightButtonListener)
	.setInputLeftButtonListener(inputClickListener)
	.setOnProfileClickListener(profileClickListener)
	.build();
```

</div>

<div component="AdvancedCode" title="v3">

```java
 Fragment fragment = new OpenChannelFragment.Builder(channelUrl)
	.setOnHeaderLeftButtonClickListener(leftButtonListener)
	.setOnHeaderRightButtonClickListener(rightButtonListener)
	.setOnInputLeftButtonClickListener(inputClickListener)
	.setOnMessageProfileClickListener(profileClickListener)
	.build();
```

</div>

In the `OpenChannelFragment.Builder` class, the `setListItemClickListener` method and `setListItemLongClickListener` method have now been divided into individual event listener methods for each view item.

<div component="AdvancedCode" title="v2">

```java
Fragment fragment = new OpenChannelFragment.Builder(channelUrl)
	.setListItemClickListener(identifiableItemClickListener)
	.setListItemLongClickListener(identifiableItemLongClickListener)
	.build();

// Or use the code below.

public class CustomOpenChannelListFragment extends OpenChannelListFragment {
	@Override
	public void onIdentifiableItemClick(View view, String identifier, int position, BaseMessage message) {
		switch (identifier) {
			case StringSet.Chat:
				break;
			case StringSet.Profile:
				break;
		}
	}
}
```

</div>

<div component="AdvancedCode" title="v3">

```java
Fragment fragment = new OpenChannelFragment.Builder(channelUrl)
	.setOnMessageClickListener(messageClickListener)
	.setOnMessageProfileClickListener(messageProfileClickListener)
	.setOnMessageLongClickListener(messageLongClickListener)
	.setOnMessageProfileLongClickListener(messageProfileLongClickListener)
	.build();

// Or use the code below.

public class CustomOpenChannelListFragment extends OpenChannelListFragment {
	@Override
	protected void onMessageClicked(@NonNull View view, int position, @NonNull BaseMessage message) {}

	@Override
	protected void onMessageProfileClicked(@NonNull View view, int position, @NonNull BaseMessage message) {}

	@Override
	protected void onQuoteReplyMessageClicked(@NonNull View view, int position, @NonNull BaseMessage message) {}
}
```

</div>

### CreateChannelFragment.Builder

The `CreateableChannelType` property has been changed to `CreatableChannelType` and the following methods have also changed names in v3.

<div component="AdvancedCode" title="v2">

```java
Fragment fragment = new CreateChannelFragment.Builder(CreateableChannelType.Normal)
	.setHeaderLeftButtonListener(leftButtonListener)
	.setCustomUserListQueryHandler(customQueryHandler)
	.build();
```

</div>

<div component="AdvancedCode" title="v3">

```java
Fragment fragment = new CreateChannelFragment.Builder(CreatableChannelType.Normal)
	.setOnHeaderLeftButtonClickListener(leftButtonListener)
	.setCustomPagedQueryHandler(customQueryHandler)
	.build();
```

</div>

### ChannelSettingsFragment.Builder

The following methods have changed names in v3.

<div component="AdvancedCode" title="v2">

```java
Fragment fragment = new ChannelSettingsFragment.Builder(channelUrl)
	.setHeaderLeftButtonListener(leftButtonListener)
	.setMemberSettingClickListener(memberSettingClickListener)
	.setOnSettingMenuClickListener(menuClickListener)
	.build();
```

</div>

<div component="AdvancedCode" title="v3">

```java
Fragment fragment = new ChannelSettingsFragment.Builder(channelUrl)
	.setOnHeaderLeftButtonClickListener(leftButtonListener)
	.setOnMenuClickListener(menuItemClickListener)
	.build();
```

</div>

### OpenChannelSettingsFragment.Builder

The following methods have changed names in v3.

<div component="AdvancedCode" title="v2">

```java
Fragment fragment = new OpenChannelSettingsFragment.Builder(channelUrl)
	.setHeaderLeftButtonListener(leftButtonListener)
	.setMemberSettingClickListener(memberSettingClickListener)
	.setOnSettingMenuClickListener(menuClickListener)
	.build();
```

</div>

<div component="AdvancedCode" title="v3">

```java
Fragment fragment = new OpenChannelSettingsFragment.Builder(channelUrl)
	.setOnHeaderLeftButtonClickListener(leftButtonListener)
	.setOnMenuClickListener(menuItemClickListener)
	.build();
```

</div>

### InviteUserFragment.Builder

The following methods have changed names in v3.

<div component="AdvancedCode" title="v2">

```java
Fragment fragment = new InviteUserFragment.Builder(channelUrl)
	.setHeaderLeftButtonListener(leftButtonListener)
	.setUserListAdapter(userListAdapter)
	.setCustomUserListQueryHandler(userListQueryHandler)
	.build();
```

</div>

<div component="AdvancedCode" title="v3">

```java
Fragment fragment = new InviteUserFragment.Builder(channelUrl)
	.setOnHeaderLeftButtonClickListener(leftButtonListener)
	.setInviteUserListAdapter(inviteUserListAdapter)
	.setOnCustomPagedQueryHandler(customPagedQueryHandler)
	.build();
```

</div>

### PromoteOperatorFragment.Builder

The following methods have changed names in v3.

<div component="AdvancedCode" title="v2">

```java
Fragment fragment = new PromoteOperatorFragment.Builder(channelUrl)
	.setHeaderLeftButtonListener(leftButtonListener)
	.setUserListAdapter(userListAdapter)
	.setCustomUserListQueryHandler(userListQueryHandler)
	.build();
```

</div>

<div component="AdvancedCode" title="v3">

```java
Fragment fragment = new PromoteOperatorFragment.Builder(channelUrl)
	.setOnHeaderLeftButtonClickListener(leftButtonListener)
	.setPromoteOperatorListAdapter(promoteOperatorListAdapter)
	.setOnCustomPagedQueryHandler(customPagedQueryHandler)
	.build();
```

</div>

### MemberListFragment.Builder

The following methods have changed names in v3.

<div component="AdvancedCode" title="v2">

```java
Fragment fragment = new MemberListFragment.Builder(channelUrl)
	.setHeaderLeftButtonListener(leftButtonListener)
	.setHeaderRightButtonListener(rightButtonListener)
	.setItemClickListener(itemClickListener)
	.setItemLongClickListener(itemLongClickListener)
	.setActionItemClickListener(actionItemClickListener)
	.build();
```

</div>

<div component="AdvancedCode" title="v3">

```java
Fragment fragment = new MemberListFragment.Builder(channelUrl)
	.setOnHeaderLeftButtonClickListener(leftButtonListener)
	.setOnHeaderRightButtonClickListener(rightButtonListener)
	.setOnItemClickListener(itemClickListener)
	.setOnItemLongClickListener(itemLongClickListener)
	.setOnActionItemClickListener(actionItemClickListener)
	.build();
```

</div>

### ParticipantListFragment.Builder

The following methods have changed names in v3.

<div component="AdvancedCode" title="v2">

```java
Fragment fragment = new ParticipantListFragment.Builder(channelUrl)
	.setHeaderLeftButtonListener(leftButtonListener)
	.setHeaderRightButtonListener(rightButtonListener)
	.setItemClickListener(itemClickListener)
	.setItemLongClickListener(itemLongClickListener)
	.setActionItemClickListener(actionItemClickListener)
	.setUserListAdapter(userListAdapter)
	.build();
```

</div>

<div component="AdvancedCode" title="v3">

```java
Fragment fragment = new ParticipantListFragment.Builder(channelUrl)
	.setOnHeaderLeftButtonClickListener(leftButtonListener)
	.setOnHeaderRightButtonClickListener(rightButtonListener)
	.setOnItemClickListener(itemClickListener)
	.setOnItemLongClickListener(itemLongClickListener)
	.setOnActionItemClickListener(actionItemClickListener)
	.setParticipantListAdapter(participantListAdapter)
	.build();
```

</div>

### MutedMemberListFragment.Builder

The following methods have changed names in v3.

<div component="AdvancedCode" title="v2">

```java
Fragment fragment = new MutedMemberListFragment.Builder(channelUrl)
	.setHeaderLeftButtonListener(leftButtonListener)
	.setHeaderRightButtonListener(rightButtonListener)
	.setItemClickListener(itemClickListener)
	.setItemLongClickListener(itemLongClickListener)
	.setActionItemClickListener(actionItemClickListener)
	.setMemberListAdapter(memberListAdapter)
	.build();
```

</div>

<div component="AdvancedCode" title="v3">

```java
Fragment fragment = new MutedMemberListFragment.Builder(channelUrl)
	.setOnHeaderLeftButtonClickListener(leftButtonListener)
	.setOnHeaderRightButtonClickListener(rightButtonListener)
	.setOnItemClickListener(itemClickListener)
	.setOnItemLongClickListener(itemLongClickListener)
	.setOnActionItemClickListener(actionItemClickListener)
	.setMutedMemberListAdapter(mutedMemberListAdapter)
	.build();
```

</div>

### OperatorListFragment.Builder

The following methods have changed names in v3.

<div component="AdvancedCode" title="v2">

```java
Fragment fragment = new OperatorListFragment.Builder(channelUrl)
	.setHeaderLeftButtonListener(leftButtonListener)
	.setHeaderRightButtonListener(rightButtonListener)
	.setItemClickListener(itemClickListener)
	.setItemLongClickListener(itemLongClickListener)
	.setActionItemClickListener(actionItemClickListener)
	.setUserListAdapter(userListAdapter)
	.build();
```

</div>

<div component="AdvancedCode" title="v3">

```java
Fragment fragment = new OperatorListFragment.Builder(channelUrl)
	.setOnHeaderLeftButtonClickListener(leftButtonListener)
	.setOnHeaderRightButtonClickListener(rightButtonListener)
	.setOnItemClickListener(itemClickListener)
	.setOnItemLongClickListener(itemLongClickListener)
	.setOnActionItemClickListener(actionItemClickListener)
	.setOperatorListAdapter(operatorListAdapter)
	.build();
```

</div>

### ModerationFragment.Builder

The following methods have changed names in v3.

<div component="AdvancedCode" title="v2">

```java
Fragment fragment = new ModerationFragment.Builder(channelUrl)
	.setHeaderLeftButtonListener(leftButtonListener)
	.build();
```

</div>

<div component="AdvancedCode" title="v3">

```java
Fragment fragment = new ModerationFragment.Builder(channelUrl)
	.setOnHeaderLeftButtonClickListener(leftButtonListener)
	.build();
```

</div>

### MessageSearchFragment.Builder

The following methods have changed names in v3.

<div component="AdvancedCode" title="v2">

```java
Fragment fragment = new MessageSearchFragment.Builder(channelUrl)
	.setItemClickListener(itemClickListener)
	.build();
```

</div>

<div component="AdvancedCode" title="v3">

```java
Fragment fragment = new MessageSearchFragment.Builder(channelUrl)
	.setOnItemClickListener(itemClickListener)
	.build();
```

</div>