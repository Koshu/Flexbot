package de.koshu.flextime;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

public class Flextime extends Application {
    private static Flextime instance;
    public static Flextime get() { return instance; }

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
        instance = this;
    }
}
