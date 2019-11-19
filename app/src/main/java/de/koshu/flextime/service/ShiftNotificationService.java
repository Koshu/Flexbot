package de.koshu.flextime.service;

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

import de.koshu.flextime.R;
import de.koshu.flextime.data.Shift;

public abstract class ShiftNotificationService extends Service {
    private static final String TAG = "ShiftNotificationService";

    private static final String NOTIFICATION_CHANNEL_ID = "flextimeNot";

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
            CharSequence name = "FlexTime Service";
            String description = "The one and only";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);

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

    protected void updateControlNotification(Shift shift){
        NotificationManager mNotificationManager = getSystemService(NotificationManager.class);
        mNotificationManager.notify(1, getNotification(shift));
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
                .setSmallIcon(R.drawable.appicon_small);
                //.setContentIntent(pendingIntent);

        if(shift != null && shift.state != Shift.STATE_CLOSED && shift.state != Shift.STATE_UNSURE){
            Intent pauseIntent = new Intent(this, ShiftService.class);
            pauseIntent.setAction(ACTION_PAUSESHIFT);
            PendingIntent pendingPauseIntent = PendingIntent.getService(this, 0, pauseIntent, 0);

            Intent stopIntent = new Intent(this, ShiftService.class);
            stopIntent.setAction(ACTION_STOPSHIFT);
            PendingIntent pendingStopIntent = PendingIntent.getService(this, 0, stopIntent, 0);

            Intent unpauseIntent = new Intent(this, ShiftService.class);
            unpauseIntent.setAction(ACTION_UNPAUSESHIFT);
            PendingIntent pendingUnpauseIntent = PendingIntent.getService(this, 0, unpauseIntent, 0);


            switch(shift.state) {
                case Shift.STATE_PAUSED:
                    builder
                            .addAction(R.drawable.play, "RESUME", pendingUnpauseIntent)
                            .addAction(R.drawable.stop, "STOP", pendingStopIntent)
                            .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                    .setShowActionsInCompactView(0)
                                    .setShowActionsInCompactView(1))
                            .setContentTitle("Schicht pausiert")
                            .setContentText(shift.tag + " - pausiert seit: " + shift.getPauseStartTimeString());
                    break;
                case Shift.STATE_RUNNING:
                    builder
                            .addAction(R.drawable.pause, "PAUSE", pendingPauseIntent)
                            .addAction(R.drawable.stop, "STOP", pendingStopIntent)
                            .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                    .setShowActionsInCompactView(0)
                                    .setShowActionsInCompactView(1))
                            .setContentTitle("Schicht läuft")
                            .setContentText(shift.tag + " - gestartet um: " + shift.getStartTimeString());
                    break;
            }


        } else {
            Intent startIntent = new Intent(this, ShiftService.class);
            startIntent.setAction(ACTION_STARTSHIFT);
            PendingIntent pendingStartIntent = PendingIntent.getService(this, 0, startIntent, 0);

            builder
                    .addAction(R.drawable.play, "Start", pendingStartIntent)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0))
                    .setContentTitle("Keine Schicht")
                    .setContentText("Beginne eine Schicht");

        }

        return builder.build();
    }
}
