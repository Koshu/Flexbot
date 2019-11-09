package de.koshu.flexbot;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

public class Flexbot extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
    }
}
