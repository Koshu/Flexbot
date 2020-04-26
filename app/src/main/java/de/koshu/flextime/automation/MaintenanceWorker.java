package de.koshu.flextime.automation;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

import de.koshu.flextime.data.AppSettings;
import de.koshu.flextime.data.BackupManager;
import de.koshu.flextime.data.DataManager;


public class MaintenanceWorker extends Worker {
    private Context context;

    public MaintenanceWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        this.context = context;
    }

    public static void activate(){
        PeriodicWorkRequest logWorkerRequest = new PeriodicWorkRequest
                .Builder(MaintenanceWorker.class, 24, TimeUnit.HOURS)
                .build();

        WorkManager.getInstance().enqueueUniquePeriodicWork("FLEXTIMEWORKER", ExistingPeriodicWorkPolicy.KEEP, logWorkerRequest);
    }

    @NonNull
    @Override
    public Result doWork() {
        DataManager dataManager = DataManager.getNotSingletonManager();
        AppSettings settings = dataManager.getSettings();

        BackupManager backMan = BackupManager.getManager();

        if(settings.backupAutoEnabled) {
            backMan.createBackup();
            backMan.cleanOldBackups(settings.backupsDaysToKeep);
        }

        return Result.success();
    }

}
