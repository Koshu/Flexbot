package de.koshu.flexbot.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import de.koshu.flexbot.R;
import de.koshu.flexbot.data.Shift;

public abstract class ShiftNotificationService extends Service {
    private static final String TAG = "ShiftNotificationService";

    private static final String NOTIFICATION_CHANNEL_ID = "flexbotNot";

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

    public static final String ACTION_PAUSESHIFT = "ACTION_PAUSESHIFT";
    public static final String ACTION_UNPAUSESHIFT = "ACTION_UNPAUSESHIFT";
    public static final String ACTION_STARTSHIFT = "ACTION_STARTSHIFT";
    public static final String ACTION_STOPSHIFT = "ACTION_STOPSHIFT";

    /* Used to build and start foreground service. */
    protected void startForegroundService() {
        Log.d(TAG, "Start foreground service.");
        createNotificationChannel();
        startForeground(1, getNotification(null));
    }

    protected void stopForegroundService() {
        Log.d(TAG, "Stop foreground service.");

        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();
    }

    protected void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "PosTracker Service";
            String description = "The one and only";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "My foreground service onCreate().");
    }

    protected void updateNotification(Shift shift){
        Notification notification = getNotification(shift);

        NotificationManager mNotificationManager = getSystemService(NotificationManager.class);
        mNotificationManager.notify(1, notification);
    }

    protected Notification getNotification(Shift shift){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        // Create an explicit intent for an Activity in your app
        //Intent intent = new Intent(this, OverviewActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        builder
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.briefcase_clock);
                //.setContentIntent(pendingIntent);

        if(shift != null){
            builder
                    .setContentTitle("Shift running")
                    .setContentText("Started at " + shift.getStartTimeString());

            Intent pauseIntent = new Intent(this, ShiftService.class);
            pauseIntent.setAction(ACTION_PAUSESHIFT);
            PendingIntent pendingPauseIntent = PendingIntent.getService(this, 0, pauseIntent, 0);

            builder.addAction(new NotificationCompat.Action(
                    R.drawable.pause, "PAUSE",
                    pendingPauseIntent));

            Intent stopIntent = new Intent(this, ShiftService.class);
            stopIntent.setAction(ACTION_STOPSHIFT);
            PendingIntent pendingStopIntent = PendingIntent.getService(this, 0, stopIntent, 0);

            builder.addAction(new NotificationCompat.Action(
                    R.drawable.stop, "STOP",
                    pendingStopIntent));
        } else {
            builder.setContentTitle("No shift running.");

            Intent startIntent = new Intent(this, ShiftService.class);
            startIntent.setAction(ACTION_STARTSHIFT);
            PendingIntent pendingStartIntent = PendingIntent.getService(this, 0, startIntent, 0);

            builder.addAction(new NotificationCompat.Action(
                    R.drawable.play, "START",
                    pendingStartIntent));

        }

        return builder.build();
    }
}
