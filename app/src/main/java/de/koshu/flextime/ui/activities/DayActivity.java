package de.koshu.flextime.ui.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import de.koshu.flextime.R;
import de.koshu.flextime.data.DataManager;
import io.realm.Realm;


public class DayActivity extends AppCompatActivity {
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_day);

        realm = DataManager.getManager().getRealm();

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Tag");

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Created Event", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Bundle bundle = getIntent().getExtras();

        if(bundle != null) {
            Fragment dayFragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
            dayFragment.setArguments(bundle);
        }
    }

    public Realm getRealm(){
        return realm;
    }
}