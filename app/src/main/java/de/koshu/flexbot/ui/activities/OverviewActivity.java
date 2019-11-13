package de.koshu.flexbot.ui.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.koshu.flexbot.Helper;
import de.koshu.flexbot.R;
import de.koshu.flexbot.automation.AutomationManager;
import de.koshu.flexbot.automation.MaintenanceWorker;
import de.koshu.flexbot.data.AppState;
import de.koshu.flexbot.data.DataManager;
import de.koshu.flexbot.data.Day;
import de.koshu.flexbot.data.Shift;
import de.koshu.flexbot.service.ShiftService;
import de.koshu.flexbot.ui.fragments.DayListFragment;
import io.realm.Realm;
import io.realm.RealmChangeListener;


public class OverviewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private Realm realm;
    private DataManager dataManager;
    private AutomationManager autoManager;

    private AppState appState;

    private TextView txtState, txtStartTime, txtWorkTime, txtAllOvertime;
    private FloatingActionButton btnStart, btnStop;

    private RealmChangeListener<Day> dayListener;
    private RealmChangeListener<Shift> shiftListener;

    private DrawerLayout mDrawerLayout;

    private String shiftState = "Idle";

    private DayListFragment dayListFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        dataManager = DataManager.getManager();
        autoManager = AutomationManager.getManager();

        realm = dataManager.getRealm();

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.menu);
        ab.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        txtState = findViewById(R.id.txt_state);
        txtStartTime = findViewById(R.id.txt_startTime);
        txtWorkTime = findViewById(R.id.txt_workTime);
        txtAllOvertime = findViewById(R.id.txt_allOvertime);

        btnStop = findViewById(R.id.btn_stop);
        btnStart = findViewById(R.id.btn_start);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(shiftState){
                    case "Idle":
                        dataManager.startNewShift(null, Shift.CONFIDENCE_MANUAL);
                        break;
                    case "Running":
                        dataManager.pauseShift(null, Shift.CONFIDENCE_MANUAL);
                        break;
                    case "Paused":
                        dataManager.unpauseShift(null, Shift.CONFIDENCE_MANUAL);
                        break;
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataManager.endShift(null, Shift.CONFIDENCE_MANUAL);
            }
        });

        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("Übersicht");

        appState = dataManager.getAppState();

        dataManager.getRealm().addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm realm) {
                updateDayGui();
            }
        });


        Fragment dayListFragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    @Override
    public void onResume(){
        super.onResume();

        testPermission();

        if(!isServiceRunning()) {
            Intent intent = new Intent(OverviewActivity.this, ShiftService.class);
            intent.setAction(ShiftService.ACTION_START_FOREGROUND_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }

        updateDayGui();
        //MaintenanceWorker.activateLogWorker();
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("de.koshu.flexbot.service.ShiftService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public Realm getRealm(){
        return realm;
    }

    private void updateDayGui(){
        Shift shift = appState.runningShift;
        Day today = appState.runningDay;
        txtAllOvertime.setText(String.format("%.1fh",dataManager.getAllOvertime()));
        if(shift != null){
            txtStartTime.setText(shift.getStartTimeString());
            txtState.setText(shift.getStateString());
            shiftState = shift.getStateString();
        } else {
            txtStartTime.setText("--:--");
            txtState.setText("Idle");
            shiftState = "Idle";
            btnStop.setEnabled(false);
            btnStart.setImageResource(R.drawable.play);
        }

        switch(shiftState){
            case "Idle":
                btnStop.setEnabled(false);
                btnStart.setImageResource(R.drawable.play);
                break;
            case "Running":
                btnStop.setEnabled(true);
                btnStart.setImageResource(R.drawable.pause);
                break;
            case "Paused":
                btnStop.setEnabled(true);
                btnStart.setImageResource(R.drawable.play);
                break;
        }

        if(today == null) {
            today = dataManager.getToday();
        }

        String work = String.format("%.1f / %.1f", today.getCumulatedNettoDuration(), today.getRequiredWork());
        txtWorkTime.setText(work);

        if(dayListFragment != null) dayListFragment.update();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        mDrawerLayout.closeDrawers();

        switch(menuItem.getItemId()){
            case R.id.nav_events: {
                Intent intent = new Intent(this, EventActivity.class);
                startActivity(intent);
            } break;
            case R.id.nav_settings: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            } break;
            case R.id.nav_tags:
                Intent intent = new Intent(this, TagListActivity.class);
                startActivity(intent);
                break;
            /*case R.id.nav_export:
                exportAndSendAsCSV();
                break;*/
            case R.id.nav_exportJSON:
                exportAndSendAsJSON();
                break;
            case R.id.nav_importJSON:
                importJSON();
                break;
            /*case R.id.nav_import:
                importCSV();
                break;*/
        }
        return true;
    }

    private void exportAndSendAsCSV(){
        Calendar c = Calendar.getInstance();
        String time = new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(c.getTime());

        String filename=time+".csv";

        File path= new File(this.getFilesDir(), "csv");
        path.mkdirs();

        File file = new File(path, filename);

        dataManager.exportToCSV(file);

        Uri uri = FileProvider.getUriForFile(this,"de.koshu.postracker.fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TEXT, "Backup of work hours");

        startActivity(Intent.createChooser(intent , null));
    }

    private void exportAndSendAsJSON(){
        /*Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        //intent.putExtra(Intent.EXTRA_TITLE, "test.csv");
        //intent.setType("text/csv");
        //intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(intent , 42);*/


        Calendar c = Calendar.getInstance();
        String time = new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(c.getTime());

        String filename=time+".json.gz";

        File path= new File(this.getFilesDir(), "csv");
        path.mkdirs();

        File file = new File(path, filename);

        String jsonString = dataManager.exportToJSON().toString();

        try {
            Helper.writeToFile(file, Helper.compress(jsonString));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Uri uri = FileProvider.getUriForFile(this,"de.koshu.flexbot.fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TEXT, "Backup of work hours");

        startActivity(Intent.createChooser(intent , null));
    }

    private void importCSV(){

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");

        startActivityForResult(intent, 33);
    }

    private void importJSON(){

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/*");

        startActivityForResult(intent, 32);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode,resultCode,resultData);

        if (requestCode == 33 && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                dataManager.importCSV(getApplicationContext(), uri);
                dataManager.getToday();
            }
        }

        if (requestCode == 32 && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                try {
                    String json = Helper.decompress(Helper.readFileFromUri(getApplicationContext(), uri));
                    dataManager.importJSON(new JSONObject(json));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void testPermission(){
        final boolean locationPerm = hasPermission("android.permission.ACCESS_FINE_LOCATION");
        final Activity parAct = this;

        if(!locationPerm) {
            String[] perms = new String[]{"android.permission.ACCESS_FINE_LOCATION"};
            ActivityCompat.requestPermissions(parAct, perms, 3);

            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        }
    }

    public boolean hasPermission(String permission){
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }
}

