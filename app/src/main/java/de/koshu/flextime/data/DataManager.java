package de.koshu.flextime.data;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.koshu.flextime.Flextime;
import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.RealmSchema;
import io.realm.Sort;

public class DataManager {
    private static DataManager dataManager= null;

    private Realm realm;
    private Context context;
    private AppState state = null;
    private AppSettings settings = null;
    private List<DataManagerListener> changeListeners = new ArrayList<>();

    private RealmChangeListener<RealmModel> globalListener = new RealmChangeListener<RealmModel>() {
        @Override
        public void onChange(RealmModel realmModel) {
            callCallbacks();
        }
    };

    private DataManager(){
        this.context = Flextime.get();

        Realm.init(context);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(1)
                .migration(new DataMigration())
                .build();

        realm = Realm.getInstance(config);

        state = realm.where(AppState.class).findFirst();

        if(state == null){
            realm.beginTransaction();
            state = realm.createObject(AppState.class);
            realm.commitTransaction();
        }

        state.addChangeListener(globalListener);

        settings = realm.where(AppSettings.class).findFirst();

        if(settings == null){
            realm.beginTransaction();
            settings = realm.createObject(AppSettings.class);
            realm.commitTransaction();
        }
    }

    public static DataManager getManager(){
        if(dataManager == null){
            dataManager = new DataManager();
        }

        return dataManager;
    }

    public void addChangeListener(DataManagerListener listener){
        changeListeners.add(listener);
    }

    public void removeChangeListener(DataManagerListener listener){
        changeListeners.remove(listener);
    }

    private void callCallbacks(){
        for(DataManagerListener listener : changeListeners){
            listener.onChange();
        }
    }

    public static DataManager getNotSingletonManager(){
        return new DataManager();
    }

    public Realm getRealm(){
        return realm;
    }

    public AppSettings getSettings(){ return settings;}

    //////////////DAYS
    public Day getToday() {
        Day today = getDay(LocalDate.now());

        return today;
    }

    public Day getDay(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate ld = LocalDate.parse(date.trim(), formatter);
        return getDay(ld);
    }

    public Day getDay(LocalDate lDate) {
        Day day = realm.where(Day.class)
                .equalTo("date", lDate.getDayOfMonth())
                .equalTo("month", lDate.getMonthValue())
                .equalTo("year", lDate.getYear())
                .findFirst();

        if (day == null) {
            realm.beginTransaction();
            day = realm.createObject(Day.class);
            day.setDate(lDate);
            realm.commitTransaction();

            Day firstDay = getFirstDay();

            if(!day.getLocalDate().equals(firstDay.getLocalDate())){
                LocalDate dayBefore = day.getLocalDate().minusDays(1);

                getDay(dayBefore);
            }

            Day lastDay = getLastDay();

            if(!day.getLocalDate().equals(lastDay.getLocalDate())){
                LocalDate dayAfter = day.getLocalDate().plusDays(1);

                getDay(dayAfter);
            }
        }

        return day;
    }

    public Day getFirstDay(){
        String []fieldNames={"year","month","date"};
        Sort sort[]={Sort.ASCENDING,Sort.ASCENDING,Sort.ASCENDING};
        Day day = realm.where(Day.class).sort(fieldNames,sort).findFirst();

        return day;
    }

    public Day getLastDay(){
        String []fieldNames={"year","month","date"};
        Sort sort[]={Sort.DESCENDING,Sort.DESCENDING,Sort.DESCENDING};
        Day day = realm.where(Day.class).sort(fieldNames,sort).findFirst();

        return day;
    }

    public RealmResults<Day> getAllDaysSorted(){
        String []fieldNames={"year","month","date"};
        Sort sort[]={Sort.DESCENDING,Sort.DESCENDING,Sort.DESCENDING};
        RealmResults<Day> days = realm.where(Day.class).sort(fieldNames,sort).findAll();

        return days;
    }

    public RealmResults<Day> getDaysOfMonth(int month, int year){
        RealmResults<Day> days = realm.where(Day.class)
                .equalTo("month", month)
                .equalTo("year", year)
                .findAll();

        return days;
    }
    ///////////SHIFTS
    public void startNewShift(LocalTime time, int confidence) {
        startNewShift(time,"Untagged", confidence);
    }

    public void startNewShift(LocalTime time, String tag, int confidence) {
        if (state.runningShift != null) {
            endShift(time, Shift.CONFIDENCE_AUTO_NOTOK);
        }

        Day day = getToday();

        realm.beginTransaction();
        Shift newShift = day.startNewShift(time, confidence);
        newShift.tag = tag;
        state.runningShift = newShift;
        realm.commitTransaction();

        state.runningShift.addChangeListener(globalListener);

        Toast.makeText(context, "Neue Schicht begonnen", Toast.LENGTH_SHORT).show();
    }

    public void endShift(LocalTime time, int confidence) {
        if (state.runningShift != null) {
            realm.beginTransaction();
            Shift shift = state.runningShift;
            state.runningShift.endShift(time, confidence);
            state.runningShift.removeChangeListener(globalListener);
            state.runningShift = null;

            if(shift.getDuration() < 60){
                Toast.makeText(context, "Schicht verworfen (< 1 min)", Toast.LENGTH_SHORT).show();
                shift.deleteFromRealm();
            } else {
                Toast.makeText(context, "Schicht beendet", Toast.LENGTH_SHORT).show();
            }

            realm.commitTransaction();
        }
    }

    public void pauseShift(LocalTime time, int confidence) {
        if (state.runningShift != null) {
            realm.beginTransaction();
            state.runningShift.pauseShift(time, confidence);
            realm.commitTransaction();
        }

        Toast.makeText(context, "Schicht pausiert", Toast.LENGTH_SHORT).show();
    }

    public void unpauseShift(LocalTime time, int confidence) {
        if (state.runningShift != null) {
            realm.beginTransaction();
            state.runningShift.unpauseShift(time, confidence);
            realm.commitTransaction();
        }

        Toast.makeText(context, "Schicht fortgesetzt", Toast.LENGTH_SHORT).show();
    }

    public Shift getRunningShift(){
        return state.runningShift;
    }


    //// OTHER

    public RealmResults<WorkTag> getAllTags(){
        RealmResults<WorkTag> tags = realm.where(WorkTag.class).findAll();

        return tags;
    }

    public AppState getAppState(){
        return state;
    }



    public float getAllOvertime(){
        RealmResults<Day> days = getAllDaysSorted();

        float overtime = 0.0f;

        for(Day day : days){
            overtime += day.getOvertime();
        }

        overtime += settings.startOvertime;

        return overtime;
    }

    public float getOvertimeOfMonth(int month, int year){
        RealmResults<Day> days = getDaysOfMonth(month, year);

        float overtime = 0.0f;

        for(Day day : days){
            overtime += day.getOvertime();
        }

        return overtime;
    }

    public boolean importCSVLine(String line){
        String[] split = line.split(";");

        switch(split[0]){
            case "DAY":{
                Day day = getDay(split[1]);
                realm.beginTransaction();
                day.fromStringCSV(line);
                realm.commitTransaction();
                break;}
            case "SHIFT":{
                Day day = getDay(split[1]);
                realm.beginTransaction();
                Shift shift = day.addEmptyShift();
                shift.fromStringCSV(line);
                realm.commitTransaction();
                break;}
        }

        return false;
    }

    public boolean importCSV(Context context, Uri uri){
        try {
            InputStream inputStream =
                    context.getContentResolver().openInputStream(uri);

            BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)));

            String line;

            realm.beginTransaction();
            realm.delete(Day.class);
            realm.delete(Shift.class);
            realm.commitTransaction();

            while ((line = reader.readLine()) != null) {
                importCSVLine(line);
            }
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean importJSON(JSONObject json){
        realm.beginTransaction();
        realm.delete(Day.class);
        realm.delete(Shift.class);
        realm.delete(WorkTag.class);
        realm.commitTransaction();

        try {
            JSONObject settingsJson = json.getJSONObject("Settings");

            if(settingsJson != null) {
                realm.beginTransaction();
                settings.fromJSON(settingsJson);
                realm.commitTransaction();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray daysJson = json.getJSONArray("Days");

            if(daysJson != null) {
                for(int i=0; i < daysJson.length(); i++){
                    JSONObject dayJSON = daysJson.getJSONObject(i);

                    Day day = getDay(dayJSON.getString("Date"));
                    realm.beginTransaction();
                    day.fromJSON(dayJSON);
                    realm.commitTransaction();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray tagsJson = json.getJSONArray("Tags");

            if(tagsJson != null) {
                for(int i=0; i < tagsJson.length(); i++){
                    JSONObject tagJson = tagsJson.getJSONObject(i);

                    realm.beginTransaction();
                    WorkTag tag = realm.createObject(WorkTag.class);
                    tag.fromJSON(tagJson);
                    realm.commitTransaction();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean exportToCSV(File path){
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));

            RealmResults<Day> days = getAllDaysSorted();

            for(Day d : days) {
                writer.write(d.toStringCSV());
            }

            writer.close();
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public JSONObject exportToJSON(){
        JSONObject json = new JSONObject();

        try{
            json.put("Settings",settings.toJSON());

            JSONArray jsonTags = new JSONArray();

            RealmResults<WorkTag> tags = getAllTags();

            for(WorkTag t : tags) {
                jsonTags.put(t.toJSON());
            }

            json.put("Tags",jsonTags);

            JSONArray jsonDays = new JSONArray();

            RealmResults<Day> days = getAllDaysSorted();

            for(Day d : days) {
                jsonDays.put(d.toJSON());
            }

            json.put("Days",jsonDays);


        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
        return json;
    }

    public interface DataManagerListener{
        void onChange();
    }

    private class DataMigration implements RealmMigration {
        @Override
        public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
            RealmSchema schema = realm.getSchema();

            if(oldVersion == 0){
                schema.get("WorkTag")
                        .addField("trackMode",int.class);
            }
        }
    }
}
