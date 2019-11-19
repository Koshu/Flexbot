package de.koshu.flextime.data;

import io.realm.RealmObject;

public class AppState extends RealmObject {
    public Day runningDay;
    public Shift runningShift;
    public String currentGeoLocation = "";
    public String currentWifiLocation = "";
}
