package de.koshu.flexbot.automation;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import de.koshu.flexbot.Helper;
import de.koshu.flexbot.data.DataManager;

public class MaintenanceWorker extends Worker {
    private Context context;

    public MaintenanceWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        this.context = context;
    }

    public static void activateLogWorker(){
        PeriodicWorkRequest logWorkerRequest = new PeriodicWorkRequest
                .Builder(MaintenanceWorker.class, 24, TimeUnit.HOURS)
                .build();

        WorkManager.getInstance().enqueueUniquePeriodicWork("FLEXBOTWORKER", ExistingPeriodicWorkPolicy.KEEP, logWorkerRequest);
    }

    @NonNull
    @Override
    public Result doWork() {
        cleanBackups();
        createBackup();
        return null;
    }

    private void createBackup(){
        DataManager dataManager = DataManager.getNotSingletonManager();

        Calendar c = Calendar.getInstance();
        String time = new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(c.getTime());

        String filename=time+".json.gz";

        File path= new File(context.getFilesDir(), "backup");
        path.mkdirs();

        File file = new File(path, filename);

        String jsonString = dataManager.exportToJSON().toString();

        try {
            Helper.writeToFile(file, Helper.compress(jsonString));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private void cleanBackups(){
        File path= new File(context.getFilesDir(), "backup");
        path.mkdirs();

        File[] files = path.listFiles();
        long now = System.currentTimeMillis();

        for(File f : files){
            if(now - f.lastModified() > 14*24*60*60*1000){
                f.delete();
            }
        }
    }
}
