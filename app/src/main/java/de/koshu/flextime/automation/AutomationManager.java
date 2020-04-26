package de.koshu.flextime.automation;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import de.koshu.flextime.Flextime;
import de.koshu.flextime.R;
import de.koshu.flextime.data.AppState;
import de.koshu.flextime.data.DataManager;
import de.koshu.flextime.data.Day;
import de.koshu.flextime.data.Event;
import de.koshu.flextime.data.Shift;
import de.koshu.flextime.data.WorkTag;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class AutomationManager {
    private static AutomationManager autoManager= null;
    private static final String NOTIFICATION_CHANNEL_ID = "flextimeEvents";
    private Realm realm;
    private Context context;
    private DataManager dataManager;
    private RealmResults<WorkTag> workTags;
    public GeofenceHelper geoHelper = new GeofenceHelper(this);
    public WifiHelper wifiHelper = new WifiHelper(this);

    private AutomationManager(){
        this.context = Flextime.get();

        this.dataManager = DataManager.getManager();

        this.realm = dataManager.getRealm();

        workTags = realm.where(WorkTag.class).findAll();

        workTags.addChangeListener(new RealmChangeListener<RealmResults<WorkTag>>() {
            @Override
            public void onChange(RealmResults<WorkTag> workTags) {
                updateAndStartGeofence();
            }
        });

        updateAndStartGeofence();
        wifiHelper.start();

        createNotificationChannel();
    }

    protected void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "FlexTime Service";
            String description = "The one and only";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    protected void sendInfoNotification(String msg){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.appicon_small)
                .setContentTitle("FlexTime")
                .setContentText(msg)
                .setAutoCancel(true)
                .setTimeoutAfter(3000);

        NotificationManager mNotificationManager = context.getSystemService(NotificationManager.class);
        mNotificationManager.notify(3, builder.build());
    }

    public void updateAndStartGeofence(){
        geoHelper.clearGeofences();

        for(WorkTag tag : workTags) {
            if(tag.mode != WorkTag.MODE_MANUAL){
                geoHelper.addGeofence(tag.name,tag.geoLatitude,tag.geoLongitute);
            }
        }

        geoHelper.start();
    }

    public static AutomationManager getManager(){
        if(autoManager == null) autoManager = new AutomationManager();

        return autoManager;
    }

    public Realm getRealm(){
        return realm;
    }

    public Context getContext(){
        return context;
    }

    private void detectShiftChange(Event event){
        detectStartOfShift(event);
        detectEndOfShift(event);
    }

    private void detectStartOfShift(Event event) {
        WorkTag tag = realm.where(WorkTag.class).equalTo("name", event.tag).findFirst();

        if (tag == null || tag.mode != WorkTag.MODE_AUTO) return;

        switch (tag.trackMode) {
            case WorkTag.TRACKMODE_HYBRID: {
                if (event.source.equals("WIFI") && event.type.equals("CONNECTED")) {
                    Event lastGeoEvent = realm.where(Event.class)
                            .greaterThan("date", new Date(System.currentTimeMillis() - 30 * 60 * 1000))
                            .equalTo("source", "GEO")
                            .equalTo("type", "INSIDE")
                            .equalTo("tag", event.tag)
                            .sort("date", Sort.DESCENDING)
                            .findFirst();

                    int confidence = lastGeoEvent == null ? Shift.CONFIDENCE_AUTO_NOTOK : Shift.CONFIDENCE_AUTO_OK;

                    Day toDay = dataManager.getToday();

                    for (Shift shift : toDay.getShifts()) {
                        if (shift.getTag() != event.tag) break;
                        if (shift.getEndConfidence() == Shift.CONFIDENCE_MANUAL) break;

                        long diff = shift.getEndLocalTime().until(LocalTime.now(), ChronoUnit.SECONDS);
                        if (diff < 90 * 60) {
                            sendInfoNotification("Schicht " + shift.getTag() + " fortgeführt");
                            realm.beginTransaction();
                            shift.setEnd(-1);
                            shift.setState(Shift.STATE_RUNNING);
                            shift.setPauseDuration(shift.getPauseDuration() + diff);
                            realm.commitTransaction();
                            return;
                        }
                    }

                    sendInfoNotification("Schicht " + event.tag + " gestartet");
                    dataManager.startNewShift(null, event.tag, confidence);
                }
            } break;
            case WorkTag.TRACKMODE_WIFI: {
                if (event.source.equals("WIFI") && event.type.equals("CONNECTED")) {
                    Event lastGeoEvent = realm.where(Event.class)
                            .greaterThan("date", new Date(System.currentTimeMillis() - 30 * 60 * 1000))
                            .equalTo("source", "GEO")
                            .equalTo("type", "INSIDE")
                            .equalTo("tag", event.tag)
                            .sort("date", Sort.DESCENDING)
                            .findFirst();


                    Day toDay = dataManager.getToday();

                    for (Shift shift : toDay.getShifts()) {
                        if (shift.getTag() != event.tag) break;
                        if (shift.getEndConfidence() == Shift.CONFIDENCE_MANUAL) break;

                        long diff = shift.getEndLocalTime().until(LocalTime.now(), ChronoUnit.SECONDS);
                        if (diff < 90 * 60) {
                            sendInfoNotification("Schicht " + shift.getTag() + " fortgeführt");
                            realm.beginTransaction();
                            shift.setEnd(-1);
                            shift.setState(Shift.STATE_RUNNING);
                            shift.setPauseDuration(shift.getPauseDuration() + diff);
                            realm.commitTransaction();
                            return;
                        }
                    }

                    sendInfoNotification("Schicht " + event.tag + " gestartet");
                    dataManager.startNewShift(null, event.tag, Shift.CONFIDENCE_AUTO_OK);
                }
            } break;
        }
    }


    public void addEvent(Event event){
        if(!tagEvent(event)){
            event.filtered = true;
        } else {
            filterEvents(event);
        }

        addEventToRealm(event);

        if(!event.filtered){
            detectShiftChange(event);
        }
    }

    private boolean filterEvents(Event event){
        switch(event.source){
            case "WIFI":{
                if(event.type.equals("CONNECTED")) {
                    Event lastEvent = getLastEvent("WIFI",24);

                    if(lastEvent != null && lastEvent.tag.equals(event.tag) && lastEvent.type.equals("DISCONNECTED")){
                        realm.beginTransaction();
                        lastEvent.filtered = true;
                        realm.commitTransaction();
                        event.filtered = true;
                        break;
                    }
                }
            }break;

            case "GEO":{
                Event lastEvent = getLastEvent("GEO",12*60);

                if(lastEvent != null && lastEvent.tag.equals(event.tag) && lastEvent.type.equals(event.type)){
                    event.filtered = true;
                    break;
                }
            }break;
        }

        return false;
    }

    private void detectEndOfShift(Event event){
        WorkTag tag = realm.where(WorkTag.class).equalTo("name", event.tag).findFirst();
        if(tag == null || tag.mode != WorkTag.MODE_AUTO) return;

        switch(tag.trackMode) {
            case WorkTag.TRACKMODE_HYBRID: {
                if (event.source.equals("GEO") && event.type.equals("OUTSIDE")) {
                    Event lastWifiEvent = getLastEvent("WIFI", 25);
                    Shift shift = dataManager.getRunningShift();

                    if (shift == null) return;

                    if (tag.addAutoPause == true && shift.getPauseDuration() == 0) {
                        realm.beginTransaction();
                        shift.setPauseDuration(shift.getPauseDuration() + (int)(tag.addAutoPauseTime * 60.0f));
                        realm.commitTransaction();
                    }

                    if (lastWifiEvent == null || !lastWifiEvent.type.equals("DISCONNECTED") || !lastWifiEvent.tag.equals(event.tag)) {
                        dataManager.endShift(null, Shift.CONFIDENCE_AUTO_NOTOK);
                    } else {
                        Instant instant = Instant.ofEpochSecond(lastWifiEvent.date.getTime() / 1000);
                        LocalTime ld = instant.atZone(ZoneId.systemDefault()).toLocalTime();
                        dataManager.endShift(ld, Shift.CONFIDENCE_AUTO_OK);
                    }

                    sendInfoNotification("Schicht " + shift.getTag() + " beendet");
                }
            }
            break;

            case WorkTag.TRACKMODE_WIFI: {
                if (event.source.equals("WIFI") && event.type.equals("DISCONNECTED")) {
                    Shift shift = dataManager.getRunningShift();

                    if (shift == null) return;

                    if (tag.addAutoPause == true && shift.getPauseDuration() == 0) {
                        realm.beginTransaction();
                        shift.setPauseDuration(shift.getPauseDuration() + (int)(tag.addAutoPauseTime * 60.0f));
                        realm.commitTransaction();
                    }

                    dataManager.endShift(null, Shift.CONFIDENCE_AUTO_OK);

                    sendInfoNotification("Schicht " + shift.getTag() + " beendet");
                }
            }
            break;
        }
    }

    private boolean tagEvent(Event event){
        switch(event.source){
            case "WIFI":{
                for(WorkTag tag : workTags){
                    for(String ssid : tag.associatedWIFIs){
                        if(ssid.equals(event.info)){
                            event.tag = tag.name;
                            return true;
                        }
                    }
                }
            }break;

            case "GEO":{
                String[] fenceSplit = event.info.split(Pattern.quote("."));

                if(fenceSplit.length != 2) break;
                if(!fenceSplit[0].equals("flextime")) break;

                String fence = fenceSplit[1];

                for(WorkTag tag : workTags){
                    if(tag.name.equals(fence)){
                        event.tag = tag.name;
                        event.info = fence;
                        return true;
                    }
                }
            }break;
        }

        return false;
    }

    private Event getLastEvent(String source, int timeInMin){
        Event lastEvent = null;
        if(timeInMin > 0) {
            lastEvent = realm.where(Event.class)
                    .greaterThan("date", new Date(System.currentTimeMillis() - timeInMin * 60 * 1000))
                    .equalTo("source", source)
                    .equalTo("filtered", false)
                    .sort("date", Sort.DESCENDING)
                    .findFirst();
        } else {
            lastEvent = realm.where(Event.class)
                    .equalTo("source", source)
                    .equalTo("filtered", false)
                    .sort("date", Sort.DESCENDING)
                    .findFirst();
        }

        return lastEvent;
    }

    private Event addEventToRealm(Event event){
        realm.beginTransaction();
        final Event realmEvent = realm.copyToRealm(event);
        realm.commitTransaction();
        return realmEvent;
    }
}
