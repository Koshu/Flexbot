package de.koshu.flexbot.ui.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import de.koshu.flexbot.R;
import de.koshu.flexbot.data.DataManager;
import de.koshu.flexbot.ui.fragments.EventListFragment;
import io.realm.Realm;


public class EventActivity extends AppCompatActivity {
    private Realm realm;
    private Switch swtFiltered;
    private EventListFragment eventFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_event);

        realm = DataManager.getManager().getRealm();

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Events");

        eventFragment = (EventListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        swtFiltered = toolbar.findViewById(R.id.swt_filtered);
        swtFiltered.setChecked(true);

        swtFiltered.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                eventFragment.setFiltered(isChecked);
            }
        });
    }

    public Realm getRealm(){
        return realm;
    }
}