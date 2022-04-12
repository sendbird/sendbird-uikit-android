# Sendbird UIKit for Android
![Platform](https://img.shields.io/badge/platform-ANDROID-orange.svg)
![Languages](https://img.shields.io/badge/language-JAVA-orange.svg)

## Introduction

The new version of the Sendbird UIKit is now available as the beta. Compared to UIKit v2, UIKit v3 beta features a new modular architecture with more granular components that give you enhanced flexibility to customize your web and mobile apps. Check out our [migration guides](https://github.com/sendbird/sendbird-uikit-android/blob/v3-beta/changelogs/MIGRATIONGUIDE_V3.md) and go visit the [v3 branch](https://github.com/sendbird/sendbird-uikit-android/tree/v3-beta) here.

Sendbird UIKit for Android is a development kit with an user interface that enables an easy and fast integration of standard chat features into new or existing client apps. This repository houses the UIKit source code in addition to two samples as explained below. 

- **uikit** is where you can find the open source code. Check out [UIKit Open Source Guidelines](https://github.com/sendbird/sendbird-uikit-android-sources/blob/main/OPENSOURCE_GUIDELINES.md) for more information regarding our stance on open source.
- **uikit-sample** is a chat app with UIKit’s core core features in which you can see items such as push notifications, total unread message count and auto sign-in are demonstrated. When you sign in to the sample app, you will only see a list of channels rendered by the [ChannelListActivity](https://sendbird.com/docs/uikit/v1/android/guides/group-channel#2-list-channels) on the screen. 
- **uikit-custom-sample** is a chat app which contains customizable sample code for the following:  
  * An example of how you can create your own custom message type, for example, a demonstration of sending a message in highlight.
  * MessageListParams provides various options for retrieving a list of messages with `MessageListParams`
  * ChannelListQuery provides various options for retrieving a list of channels with `ChannelListQuery`
  * User list provides various options for retrieving a list of users
  * Styles, colors, fonts
  * An example of multilingual UI support. In the `/res/values-ko-rKR/strings.xml`, you can find an example written in Korean language.

### More about Sendbird UIKIT for Android

Find out more about Sendbird UIKit for Android at [UIKit for Android doc](https://sendbird.com/docs/uikit/v1/android/getting-started/about-uikit). If you need any help in resolving any issues or have questions, visit [our community](https://community.sendbird.com).

<br />

## Before getting started

This section shows you the prerequisites you need for testing Sendbird UIKit for Android sample apps.

### Requirements

The minimum requirements for UIKit for Android are:

- Android + (API level as 16 or higher) 
- Java 8
- Support androidx only 
- Gradle 4.0.1 or higher 

### Try the sample app using your data 

If you would like to try the sample app specifically fit to your usage, you can do so by replacing the default sample app ID with yours, which you can obtain by [creating your Sendbird application from the dashboard](https://docs.sendbird.com/android/quick_start#3_install_and_configure_the_chat_sdk_4_step_1_create_a_sendbird_application_from_your_dashboard). Furthermore, you could also add data of your choice on the dashboard to test. This will allow you to experience the sample app with data from your Sendbird application. 

<br />

## Getting started

This section explains the steps you need to take before testing the sample apps.

### Create a project

Go to your `Android Studio` and create a project for UIKit for Android in the **Project window** as follows:

1. In the **Welcome to Android Studio** window, click **Start a new Android Studio project**.
2. In the **Select a Project Template** window, select **Empty Activity**, and click **Next**.
3. Enter your project name in the **Name** field in the **Configure your project** window.
4. Select your language as either **Java** or **Kotlin** from the **Language** drop-down menu.
5. Enable `Use androidx.*artifacts`.
6. Select minimum API level as 16 or higher.

### Install UIKit for Android

UIKit for Android is installed via `Gradle`. Begin by opening the project's top-level `build.gradle` file and adding code blocks as below:

> Note: Add the code blocks in your root `build.gradle` file, not your module `build.gradle` file.

```gradle
buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:4.0.1'
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven { url "https://jitpack.io" }
        maven { url "https://repo.sendbird.com/public/maven" }
    }
}
```
 
Then, open the `build.gradle` file at the application level. For `Java` and `Kotlin`, add code blocks and dependencies as below:

> Note: Data binding should be enabled in your `build.gradle` file.

```gradle
apply plugin: 'com.android.application'

android {
    ...
    
    dataBinding {
        enabled = true
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    ...
    
}

dependencies {
    implementation 'com.sendbird.sdk:uikit:LATEST_VERSION'
    ...
    
}
```

After saving your `build.gradle` file, click the **Sync** button to apply all the changes. 

<br />

## UIKit features and ways to customize 

Here is an overview of a list of key components that can be customized on UIKit. All components can be called while fragments and activities are running on the Android platform. 

|Component|Desctription|
|:---:|:---|
|ChannelList|A component that shows all channels a user has joined.|
|Channel|A component that shows the current channel a user has joined. From this component, users can send or receive messages.|
|CreateChannel|A component that shows all the users in your client app so you can create a channel. Users can be selected from this component to begin chatting.|
|InviteChannel|A component that shows all the users of your client app from the current channel so you can invite other users to join. |
|ChannelSettings|A component that changes the channel information.|
|MemberList|A component that shows the list of members who have joined the current channel.|
