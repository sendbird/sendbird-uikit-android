# UIKit Sample

UIKit is a collection of pre-built UI components that devlopers can use to quickly implement user-friendly chat interfaces.
This sample consists of four use cases of UIKit.
You can navigate to each use case through the four menus that appear when you run it.
For more details, please refer to the README file of each item.

- Basic Usage - A sample that contains the basic usage of UIKit. Please check the [README](src/main/java/com/sendbird/uikit/samples/basic/README.md) for details.
- Customizations - Examples that have applied customization to UIKit. Please check the [README](src/main/java/com/sendbird/uikit/samples/customization/README.md) for details.
- AI Chatbot: You can chat with the AI Chatbot running on the Sendbird platform.
It is disabled by default, To enable it, please check the [README](src/main/java/com/sendbird/uikit/samples/aichatbot/README.md) for details.
- Sendbird Notification: You can try out Sendbird's Notification product in this sample.
  It is disabled by default. To enable it, please check the [README](src/main/java/com/sendbird/uikit/samples/notification/README.md) for details.
  Caution: This sample is not related to push notifications. It is a demonstration of Sendbird's Notification product.

## Prerequisites
- Android Studio Giraffe
- JDK 11

## Sendbird Application ID

To streamline the implementation process, a sample Application ID has been provided for codes in this repository. However, you need a unique Sendbird Application ID to properly initialize the Chat SDK and enable its features in your production application. Sendbird Application ID can be found in the Overview page on [Sendbird Dashboard](https://dashbaord.sendbird.com). To learn more about how and when to use the Application ID, see our documentation on [initialization](https://sendbird.com/docs/chat/uikit/v3/android/introduction/send-first-message#2-before-you-start).

## How to run
The following series of steps illustrate how to run the sample app.
1. Clone the repository.
```
git clone git@github.com:sendbird/sendbird-uikit-android.git
```
2. Open a directory in Android Studio.
3. Change the run configuration to "uikit-samples".
<img src="images/run.png" alt="Run configuration">
4. Run the sample app.

## Security

When a new Sendbird application is created in [Sendbird Dashboard](https://dashbaord.sendbird.com), the default security settings are set permissive to simplify running samples and implementing your first code.

When launching a production application, make sure to review the security settings beforehand in **Settings > Application > Security** on the dashbaord and set **Access token permission** to **Deny login** because **Read & Write** is not secure and will allow a new user to be automatically created in the SDK if none exists. Ensure that users are authenticated with a Sendbird generated Session Token. Also review the **Access Control** lists. Most apps will want to disable **"Allow retrieving user list"** as that could expose sensitivie information such as usage numbers.
