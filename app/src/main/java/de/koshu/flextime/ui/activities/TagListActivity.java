package de.koshu.flextime.ui.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import de.koshu.flextime.R;
import de.koshu.flextime.data.DataManager;
import io.realm.Realm;


public class TagListActivity extends AppCompatActivity {
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_taglist);

        realm = DataManager.getManager().getRealm();

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Tag bearbeiten");
    }

    public Realm getRealm(){
        return realm;
    }
}