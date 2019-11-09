package de.koshu.flexbot.automation;

import android.content.Context;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;

import java.util.Date;
import java.util.regex.Pattern;

import de.koshu.flexbot.data.DataManager;
import de.koshu.flexbot.data.Event;
import de.koshu.flexbot.data.Shift;
import de.koshu.flexbot.data.WorkTag;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class AutomationManager {
    private static AutomationManager autoManager= null;

    private Realm realm;
    private Context context;
    private DataManager dataManager;
    private RealmResults<WorkTag> workTags;
    public GeofenceHelper geoHelper = new GeofenceHelper(this);
    public WifiHelper wifiHelper = new WifiHelper(this);

    private AutomationManager(Context context, DataManager dataManager){
        this.context = context;
        this.dataManager = dataManager;
        this.realm = dataManager.getRealm();

        workTags = realm.where(WorkTag.class).findAll();


        updateAndStartGeofence();
        wifiHelper.start();


    }

    private void updateAndStartGeofence(){
        geoHelper.clearGeofences();

        for(WorkTag tag : workTags) {
            if(tag.mode != WorkTag.MODE_MANUAL){
                geoHelper.addGeofence(tag.name,tag.geoLatitude,tag.geoLongitute);
            }
        }

        geoHelper.start();
    }

    public static AutomationManager getManager(Context context, DataManager dataManager){
        if(autoManager == null){
            autoManager = new AutomationManager(context,dataManager);
        }

        return autoManager;
    }

    public static AutomationManager getManager(){
        return autoManager;
    }

    public Realm getRealm(){
        return realm;
    }

    public Context getContext(){
        return context;
    }

    private void detectShiftChange(Event event){
        if(dataManager.getRunningShift() == null){
            detectStartOfShift(event);
        } else {
            detectEndOfShift(event);
        }
    }

    private void detectStartOfShift(Event event){
        WorkTag tag = realm.where(WorkTag.class).equalTo("name", event.tag).findFirst();

        if(tag == null || tag.mode != WorkTag.MODE_AUTO) return;

        if(event.source.equals("WIFI") && event.type.equals("CONNECTED")){
            Event lastGeoEvent = realm.where(Event.class)
                    .greaterThan("date", new Date(System.currentTimeMillis() - 30 * 60 * 1000))
                    .equalTo("source", "GEO")
                    .equalTo("type", "INSIDE")
                    .equalTo("tag", event.tag)
                    .sort("date", Sort.DESCENDING)
                    .findFirst();

            if(lastGeoEvent == null){
                dataManager.startNewShift(null, event.tag, Shift.CONFIDENCE_AUTO_NOTOK);
            } else {
                dataManager.startNewShift(null, event.tag, Shift.CONFIDENCE_AUTO_OK);
            }
        }
    }

    public void addEvent(Event event){
        if(!tagEvent(event)){
            event.filtered = true;
            addEventToRealm(event);
            return;
        }

        switch(event.source){
            case "WIFI":{
                if(event.type.equals("CONNECTED")) {
                    Event lastEvent = getLastEvent("WIFI",24);

                    if(lastEvent != null && lastEvent.tag.equals(event.tag) && lastEvent.type.equals("DISCONNECTED")){
                        realm.beginTransaction();
                        lastEvent.filtered = true;
                        realm.commitTransaction();
                        event.filtered = true;
                        addEventToRealm(event);
                        break;
                    }
                }

                addEventToRealm(event);

                detectShiftChange(event);
            }break;

            case "GEO":{
                addEventToRealm(event);

                detectShiftChange(event);
            }break;
        }
    }



    private void detectEndOfShift(Event event){
        if(event.equals("GEO","OUTSIDE","flexbot.Arbeit")){
            Event lastWifiEvent = getLastEvent("WIFI", 25);

            if(lastWifiEvent == null){
                dataManager.endShift(null, Shift.CONFIDENCE_AUTO_NOTOK);
            } else {
                Instant instant = Instant.ofEpochSecond(lastWifiEvent.date.getTime()/1000);
                LocalTime ld = instant.atZone(ZoneId.systemDefault()).toLocalTime();
                dataManager.endShift(ld, Shift.CONFIDENCE_AUTO_OK);
            }
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
                if(!fenceSplit[0].equals("flexbot")) break;

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
