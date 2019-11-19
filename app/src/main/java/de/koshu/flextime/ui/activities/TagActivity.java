package de.koshu.flextime.ui.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import de.koshu.flextime.R;
import de.koshu.flextime.data.DataManager;
import io.realm.Realm;


public class TagActivity extends AppCompatActivity {
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tag);

        realm = DataManager.getManager().getRealm();

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Schicht-Tags");

        Bundle bundle = getIntent().getExtras();

        if(bundle != null) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
            fragment.setArguments(bundle);
        }
    }

    public Realm getRealm(){
        return realm;
    }
}