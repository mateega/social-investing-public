package com.example.project;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class PushNotificationService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        String title =  message.getData().get("title");
        String body =  message.getData().get("body");
        String userId = message.getData().get("user");
        String deepLink = message.getData().get("deepLink");
        String groupId = message.getData().get("groupId");

        final String CHANNEL_ID = "NOTIFICATIONS";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "MyNotification", NotificationManager.IMPORTANCE_HIGH);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        if (!userId.equals(currentUserId)) {
            Intent intent = new Intent(this, MainActivity.class);

            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(deepLink));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("deepLink", deepLink);
            intent.putExtra("groupId", groupId);

            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

            // create the notification object
            Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.logo)
                    .setAutoCancel(true);
            // send the notification through the channel to be displayed
            NotificationManagerCompat.from(this).notify(1, notification.build());
        }
    }
}