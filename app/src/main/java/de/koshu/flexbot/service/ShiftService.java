package de.koshu.flexbot.service;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import de.koshu.flexbot.data.AppState;
import de.koshu.flexbot.data.DataManager;
import de.koshu.flexbot.data.Shift;
import io.realm.RealmChangeListener;


public class ShiftService extends ShiftNotificationService {
    private static final String TAG = "ShiftService";
    private DataManager dataManager;

    /* Used to build and start foreground service. */
    protected void startForegroundService() {
        super.startForegroundService();
    }

    protected void stopForegroundService() {
        Log.d(TAG, "Service gestopt");
        dataManager.endShift(null, Shift.CONFIDENCE_AUTO_NOTOK);

        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();
    }

    public ShiftService() {
        dataManager = DataManager.getManager(this);
        dataManager.getAppState().addChangeListener(new RealmChangeListener<AppState>() {
            @Override
            public void onChange(AppState appState) {
                updateNotification(appState.runningShift);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            switch (action) {
                case ACTION_START_FOREGROUND_SERVICE:
                    startForegroundService();
                    Toast.makeText(getApplicationContext(), "Service gestartet", Toast.LENGTH_LONG).show();
                    break;
                case ACTION_STOP_FOREGROUND_SERVICE:
                    stopForegroundService();
                    Toast.makeText(getApplicationContext(), "Service gestopt", Toast.LENGTH_LONG).show();
                    break;
                case ACTION_UNPAUSESHIFT: {
                    dataManager.unpauseShift(null, Shift.CONFIDENCE_MANUAL);
                    updateNotification(dataManager.getRunningShift());
                }
                break;
                case ACTION_PAUSESHIFT: {
                    dataManager.pauseShift(null, Shift.CONFIDENCE_MANUAL);
                    updateNotification(dataManager.getRunningShift());
                }
                break;
                case ACTION_STARTSHIFT: {
                    dataManager.startNewShift(null, Shift.CONFIDENCE_MANUAL);
                    updateNotification(dataManager.getRunningShift());
                }
                break;
                case ACTION_STOPSHIFT: {
                    dataManager.endShift(null, Shift.CONFIDENCE_MANUAL);
                    updateNotification(dataManager.getRunningShift());
                }
                break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}

