package com.sendbird.uikit.customsample.fcm;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.sendbird.android.SendbirdChat;
import com.sendbird.android.push.SendbirdPushHandler;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.consts.StringSet;
import com.sendbird.uikit.customsample.groupchannel.GroupChannelMainActivity;
import com.sendbird.uikit.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Concrete implementation of a sendbird push handler.
 */
public class MyFirebaseMessagingService extends SendbirdPushHandler {

    private static final String TAG = "MyFirebaseMsgService";
    private static final AtomicReference<String> pushToken = new AtomicReference<>();

    @Override
    public boolean isUniquePushToken() {
        return false;
    }

    @Override
    public void onNewToken(@Nullable String token) {
        Log.i(TAG, "onNewToken(" + token + ")");
        pushToken.set(token);
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(@Nullable Context context, @Nullable RemoteMessage remoteMessage) {
        if (context == null || remoteMessage == null) return;
        Logger.d("From: " + remoteMessage.getFrom());
        if (remoteMessage.getData().size() > 0) {
            Logger.d( "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Logger.d( "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        try {
            if (remoteMessage.getData().containsKey(StringSet.sendbird)) {
                String jsonStr = remoteMessage.getData().get(StringSet.sendbird);
                SendbirdChat.markAsDelivered(remoteMessage.getData());
                if (jsonStr != null) {
                    sendNotification(context, new JSONObject(jsonStr));
                }
            }
        } catch (JSONException e) {
            Logger.e(e);
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param sendBird JSONObject payload from FCM
     */
    public static void sendNotification(@NonNull Context context, @NonNull JSONObject sendBird) throws JSONException {
        String message = sendBird.getString(StringSet.message);
        JSONObject channel = sendBird.getJSONObject(StringSet.channel);
        String channelUrl = channel.getString(StringSet.channel_url);

        String senderName = context.getString(R.string.app_name);
        if (sendBird.has(StringSet.sender)) {
            JSONObject sender = sendBird.getJSONObject(StringSet.sender);
            senderName = sender.getString(StringSet.name);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        final String CHANNEL_ID = StringSet.CHANNEL_ID;
        if (Build.VERSION.SDK_INT >= 26) {  // Build.VERSION_CODES.O
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, StringSet.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        Intent intent = GroupChannelMainActivity.newRedirectToChannelIntent(context, channelUrl);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent pendingIntent = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M ?
                PendingIntent.getActivity(context, channelUrl.hashCode() /* Request code */, intent, PendingIntent.FLAG_IMMUTABLE) :
                PendingIntent.getActivity(context, channelUrl.hashCode() /* Request code */, intent, 0);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_push_lollipop)
                .setColor(ContextCompat.getColor(context, R.color.primary_300))  // small icon background color
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_push_oreo))
                .setContentTitle(senderName)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent);
        notificationBuilder.setContentText(message);

        notificationManager.notify(String.valueOf(System.currentTimeMillis()), 0, notificationBuilder.build());
    }
}