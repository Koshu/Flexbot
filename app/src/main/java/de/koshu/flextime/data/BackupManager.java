package de.koshu.flextime.data;

import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Looper;

import androidx.documentfile.provider.DocumentFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import de.koshu.flextime.Flextime;
import de.koshu.flextime.FileHelper;

public class BackupManager {
    private static BackupManager backupManager= null;

    private Context context;
    private DataManager dataManager;


    private BackupManager(){
        this.context = Flextime.get();

        dataManager = DataManager.getManager();
    }

    public void cleanOldBackups(int backupsDaysToKeep){
        Uri uri = getBackupUri();
        if(uri == null) return;
        if(backupsDaysToKeep == 0) return;

        DocumentFile backupFolder = DocumentFile.fromTreeUri(context,uri);

        DocumentFile[] backupFiles = backupFolder.listFiles();

        long deleteUntil = System.currentTimeMillis() - (((long)backupsDaysToKeep)*24*60*60*1000);
        for(DocumentFile file : backupFiles){
            if(file.getName().endsWith(".json.gz") && file.lastModified() < deleteUntil){
                file.delete();
            }
        }
    }

    public static BackupManager getManager(){
        if(backupManager == null){
            backupManager = new BackupManager();
        }

        return backupManager;
    }

    public DocumentFile[] getBackupList(){
        Uri uri = getBackupUri();
        if(uri == null) return null;

        DocumentFile backupFolder = DocumentFile.fromTreeUri(context,uri);

        return backupFolder.listFiles();
    }

    public void createBackup(){
        Uri uri = getBackupUri();
        if(uri == null) return;

        DocumentFile backupFolder = DocumentFile.fromTreeUri(context,uri);

        Calendar c = Calendar.getInstance();
        String time = new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(c.getTime());

        String filename=time+"_flextime_Backup.json.gz";

        DocumentFile file = backupFolder.createFile("application/tar",filename);

        String jsonString;

        if( Looper.myLooper() == Looper.getMainLooper()){
            jsonString = DataManager.getManager().exportToJSON().toString();
        } else {
            jsonString = DataManager.getNotSingletonManager().exportToJSON().toString();
        }

        try {
            FileHelper.writeToFile(context, file.getUri(), FileHelper.compress(jsonString));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public Uri getBackupUri(){
        List<UriPermission> uriList = context.getContentResolver().getPersistedUriPermissions();

        if(uriList.size() == 0){
            return null;
        } else {
            return uriList.get(0).getUri();
        }
    }

    public void removeAllPermissions(){
        List<UriPermission> uriList = context.getContentResolver().getPersistedUriPermissions();

        for(UriPermission uPerm : uriList){
            context.getContentResolver().releasePersistableUriPermission(uPerm.getUri(),Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
    }

    public void setAndTakeUri(Intent resultData){
        removeAllPermissions();

        Uri uri = resultData.getData();

        int takeFlags = resultData.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        context.getContentResolver().takePersistableUriPermission(uri, takeFlags);
    }
}
