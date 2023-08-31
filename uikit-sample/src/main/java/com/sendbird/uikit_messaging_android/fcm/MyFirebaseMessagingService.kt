package com.sendbird.uikit_messaging_android.fcm

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.RemoteMessage
import com.sendbird.android.SendbirdChat.markAsDelivered
import com.sendbird.android.push.SendbirdPushHandler
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit_messaging_android.R
import com.sendbird.uikit_messaging_android.consts.StringSet
import com.sendbird.uikit_messaging_android.groupchannel.GroupChannelMainActivity.Companion.newRedirectToChannelIntent
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicReference

/**
 * Concrete implementation of a sendbird push handler.
 */
class MyFirebaseMessagingService : SendbirdPushHandler() {
    override val isUniquePushToken: Boolean
        get() = false

    override fun onNewToken(newToken: String?) {
        Logger.i("{${TAG} onNewToken($newToken)")
        pushToken.set(newToken)
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(context: Context, remoteMessage: RemoteMessage) {
        Logger.d("From: " + remoteMessage.from)
        if (remoteMessage.data.isNotEmpty()) {
            Logger.d("Message data payload: " + remoteMessage.data)
        }

        // Check if message contains a notification payload.
        Logger.d("Message Notification Body: " + remoteMessage.notification?.body)
        try {
            if (remoteMessage.data.containsKey(StringSet.sendbird)) {
                val jsonStr = remoteMessage.data[StringSet.sendbird]
                markAsDelivered(remoteMessage.data)
                if (jsonStr == null) return
                sendNotification(context, JSONObject(jsonStr))
            }
        } catch (e: JSONException) {
            Logger.e(e)
        }
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
        private val pushToken = AtomicReference<String?>()

        /**
         * Create and show a simple notification containing the received FCM message.
         *
         * @param sendBird JSONObject payload from FCM
         */
        @Throws(JSONException::class)
        fun sendNotification(context: Context, sendBird: JSONObject) {
            val message = sendBird.getString(StringSet.message)
            val channel = sendBird.getJSONObject(StringSet.channel)
            val channelUrl = channel.getString(StringSet.channel_url)
            val messageId = sendBird.getLong(StringSet.message_id)
            var senderName = context.getString(R.string.app_name)
            if (sendBird.has(StringSet.sender)) {
                val sender = sendBird.getJSONObject(StringSet.sender)
                senderName = sender.getString(StringSet.name)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val CHANNEL_ID = StringSet.CHANNEL_ID
            if (Build.VERSION.SDK_INT >= 26) {  // Build.VERSION_CODES.O
                val mChannel =
                    NotificationChannel(CHANNEL_ID, StringSet.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
                notificationManager.createNotificationChannel(mChannel)
            }
            val intent = newRedirectToChannelIntent(context, channelUrl, messageId)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            @SuppressLint("UnspecifiedImmutableFlag") val pendingIntent =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.getActivity(
                    context,
                    messageId.toInt(),
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                ) else PendingIntent.getActivity(context, messageId.toInt(), intent, 0)
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_push_lollipop)
                .setColor(ContextCompat.getColor(context, R.color.primary_300)) // small icon background color
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.icon_push_oreo))
                .setContentTitle(senderName)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
            notificationBuilder.setContentText(message)
            notificationManager.notify(System.currentTimeMillis().toString(), 0, notificationBuilder.build())
        }
    }
}
