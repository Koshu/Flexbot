package de.koshu.flextime.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import de.koshu.flextime.FileHelper;
import de.koshu.flextime.R;
import de.koshu.flextime.data.AppSettings;
import de.koshu.flextime.data.BackupManager;
import de.koshu.flextime.data.DataManager;
import io.realm.Realm;

/**
 * A placeholder fragment containing a simple view.
 */
public class BackupFragment extends Fragment implements BackupListAdapter.ItemMenuListener {
    private Switch swtEnableBackup;
    private EditText txtBackupPath;
    private RecyclerView recyBackups;
    private Button btnBackupNow;
    private Uri pathUri;
    private RecyclerView.LayoutManager layoutManager;
    private BackupListAdapter backupListAdapter;
    private View view;
    private Realm realm;
    private BackupManager backupManager;
    private AppSettings appSettings;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_backup, container, false);

        swtEnableBackup = view.findViewById(R.id.swt_enableBackup);
        txtBackupPath = view.findViewById(R.id.txt_backupPath);
        btnBackupNow = view.findViewById(R.id.btn_backupNow);
        recyBackups = view.findViewById(R.id.recy_backups);

        recyBackups.setHasFixedSize(false);

        txtBackupPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDirectory();
            }
        });

        btnBackupNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backupManager.createBackup();
                updateBackupList();
            }
        });
        layoutManager = new LinearLayoutManager(getContext());
        recyBackups.setLayoutManager(layoutManager);

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        backupManager = BackupManager.getManager();
        appSettings =  DataManager.getManager().getSettings();
        realm = DataManager.getManager().getRealm();

        updateGui();
        updateBackupList();
        updateBackupUri();
    }

    @Override
    public void onPause(){
        super.onPause();

        realm.beginTransaction();
        appSettings.backupAutoEnabled = swtEnableBackup.isChecked();
        realm.commitTransaction();
    }

    public void updateGui(){
        swtEnableBackup.setChecked(appSettings.backupAutoEnabled);
    }

    public void selectDirectory() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        startActivityForResult(intent, 44);
    }

    public void updateBackupList(){
        DocumentFile[] backups = backupManager.getBackupList();

        if(backups != null) {
            backupListAdapter = new BackupListAdapter(getActivity(),this, backups);
            recyBackups.setAdapter(backupListAdapter);
        } else {
            backupListAdapter = new BackupListAdapter(getActivity(),this, null);
            recyBackups.setAdapter(backupListAdapter);
        }
    }

    public void updateBackupUri(){
        pathUri = backupManager.getBackupUri();

        if(pathUri == null){
            txtBackupPath.setText("");
            updateBackupList();
            swtEnableBackup.setEnabled(false);
            btnBackupNow.setEnabled(false);
        } else {
            txtBackupPath.setText(pathUri.toString());
            updateBackupList();
            swtEnableBackup.setEnabled(true);
            btnBackupNow.setEnabled(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode,resultCode,resultData);

        if (requestCode == 44 && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                backupManager.setAndTakeUri(resultData);
                updateBackupUri();
            }
        }
    }

    @Override
    public void onRestoreClick(DocumentFile file) {
        DataManager dataManager = DataManager.getManager();

        Uri uri = file.getUri();

        try {
            String json = FileHelper.decompress(FileHelper.readFileFromUri(getActivity().getApplicationContext(), uri));
            dataManager.importJSON(new JSONObject(json));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        dataManager.getToday();
    }

    @Override
    public void onDeleteClick(DocumentFile file) {

    }
}