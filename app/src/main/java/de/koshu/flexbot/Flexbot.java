package de.koshu.flexbot;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

public class Flexbot extends Application {
    private static Flexbot instance;
    public static Flexbot get() { return instance; }

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
        instance = this;
    }
}
