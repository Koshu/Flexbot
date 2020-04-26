package de.koshu.flextime.data;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import de.koshu.flextime.Flextime;
import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.Sort;

public class DataManager {
    private static DataManager dataManager= null;

    private Handler mHandler;
    private Realm realm;
    private Context context;
    private AppState state = null;
    private AppSettings settings = null;
    private List<DataManagerListener> changeListeners = new ArrayList<>();

    private RealmChangeListener<RealmModel> globalListener = new RealmChangeListener<RealmModel>() {
        @Override
        public void onChange(RealmModel realmModel) {
            callCallbacks();

            if(state.runningShift != null){
                startIntervalUpdater();
            } else {
                stopIntervalUpdater();
            }
        }
    };


    private DataManager(boolean withListener){
        this.context = Flextime.get();

        Realm.init(context);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(3)
                .deleteRealmIfMigrationNeeded()
                //.migration(new DataMigration())
                .build();

        realm = Realm.getInstance(config);

        state = realm.where(AppState.class).findFirst();

        if(state == null){
            realm.beginTransaction();
            state = realm.createObject(AppState.class);
            realm.commitTransaction();
        }

        if(withListener) {
            state.addChangeListener(globalListener);
            mHandler = new Handler();
        }

        settings = realm.where(AppSettings.class).findFirst();

        if(settings == null){
            realm.beginTransaction();
            settings = realm.createObject(AppSettings.class);
            realm.commitTransaction();
        }
    }

    public static DataManager getManager(){
        if(dataManager == null){
            dataManager = new DataManager(true);
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
        return new DataManager(false);
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

    public Year getYear(int yearValue){
        return getYear(yearValue,true);
    }

    public Year getYear(int yearValue, boolean createIfMissing){
        Year year = realm.where(Year.class)
                .equalTo("yearInt", yearValue)
                .findFirst();

        if(createIfMissing && year == null){
            realm.beginTransaction();
            year = new Year(yearValue);
            year = realm.copyToRealm(year);
            realm.commitTransaction();
        }

        return year;
    }

    public Year getCurrentYear(){
        LocalDate date = LocalDate.now();
        return getYear(date.getYear(),true);
    }

    public RealmResults<Year> getAllYearsOrdered(){
        String []fieldNames={"yearInt"};
        Sort sort[]={Sort.ASCENDING};
        return realm.where(Year.class).sort(fieldNames,sort).findAll();
    }

    public Month getMonth(int yearValue, int monthValue){
        Year year = getYear(yearValue);

        if(year == null) return null;

        Month month = year.getMonth(monthValue);

        if(month == null){
            realm.beginTransaction();
            month = year.getOrAddMonth(monthValue);
            realm.commitTransaction();
        }

        return month;
    }

    public Month getCurrentMonth(){
        LocalDate date = LocalDate.now();
        return getMonth(date.getYear(),date.getMonthValue());
    }

    public RealmResults<Month> getMonthsOfYear(int yearInt){
        Year year = getYear(yearInt);

        return year.getMonths();
    }

    public RealmResults<Month> getAllMonthsOrdered(){
        String []fieldNames={"yearInt","monthInt"};
        Sort sort[]={Sort.ASCENDING,Sort.ASCENDING};
        return realm.where(Month.class).sort(fieldNames,sort).findAll();
    }

    public Day getDay(LocalDate lDate) {
        Month month = getMonth(lDate.getYear(), lDate.getMonthValue());
        Day day = month.getDay(lDate.getDayOfMonth());

        if (day == null) {
            realm.beginTransaction();
            day = month.getOrAddDay(lDate.getDayOfMonth());
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

    public RealmResults<Day> getAllDaysOrdered(){
        String []fieldNames={"yearInt","monthInt","dayInt"};
        Sort sort[]={Sort.ASCENDING,Sort.ASCENDING,Sort.ASCENDING};
        return realm.where(Day.class).sort(fieldNames,sort).findAll();
    }

    public Month getFirstMonth(){
        String []fieldNames={"yearInt","monthInt"};
        Sort sort[]={Sort.ASCENDING,Sort.ASCENDING};
        Month month = realm.where(Month.class).sort(fieldNames,sort).findFirst();

        return month;
    }

    public Month getLastMonth(){
        String []fieldNames={"yearInt","monthInt"};
        Sort sort[]={Sort.DESCENDING,Sort.DESCENDING};
        Month month = realm.where(Month.class).sort(fieldNames,sort).findFirst();

        return month;
    }

    public Day getFirstDay(){
        Month month = getFirstMonth();

        String []fieldNames={"dayInt"};
        Sort sort[]={Sort.ASCENDING};
        Day day = month.getDays().where().sort(fieldNames,sort).findFirst();

        return day;
    }

    public Day getLastDay(){
        Month month = getLastMonth();

        String []fieldNames={"dayInt"};
        Sort sort[]={Sort.DESCENDING};
        Day day = month.getDays().where().sort(fieldNames,sort).findFirst();

        return day;
    }

    public RealmResults<Day> getAllDaysSorted(){
        String []fieldNames={"yearInt","monthInt","dayInt"};
        Sort sort[]={Sort.DESCENDING,Sort.DESCENDING,Sort.DESCENDING};
        RealmResults<Day> days = realm.where(Day.class).sort(fieldNames,sort).findAll();

        return days;
    }

    public RealmResults<Day> getLastDaysSorted(int numberOfDays){
        String []fieldNames={"yearInt","monthInt","dayInt"};
        Sort sort[]={Sort.DESCENDING,Sort.DESCENDING,Sort.DESCENDING};
        RealmResults<Day> days = realm.where(Day.class).sort(fieldNames,sort).limit(numberOfDays).findAll();

        return days;
    }

    public RealmResults<Day> getDaysOfMonth(int monthValue, int yearValue){
        return getMonth(yearValue,monthValue).getDays();
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
        newShift.setTag(tag);
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

    public boolean importJSON(JSONObject json){
        int version = json.optInt("backupVersion",0);

        if(version == 0) {
            return importJSONV0(json);
        } else if(version == 1) {
            return importJSONV1(json);
        } else {
            return false;
        }
    }

    public boolean importJSONV1(JSONObject json){
        realm.beginTransaction();
        realm.delete(Day.class);
        realm.delete(Shift.class);
        realm.delete(WorkTag.class);
        realm.delete(Month.class);
        realm.delete(Year.class);
        realm.commitTransaction();

        try {
            JSONObject jsonObject = json.getJSONObject("settings");

            if(jsonObject != null) {
                realm.beginTransaction();
                settings.fromJSONV1(jsonObject);
                realm.commitTransaction();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray jsonObject = json.getJSONArray("years");

            if(jsonObject != null) {
                for(int i=0; i < jsonObject.length(); i++){
                    JSONObject yearJSON = jsonObject.getJSONObject(i);

                    Year year = getYear(yearJSON.getInt("yearInt"));

                    realm.beginTransaction();
                    year.fromJSONV1(yearJSON);
                    realm.commitTransaction();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray tagsJson = json.getJSONArray("tags");

            if(tagsJson != null) {
                for(int i=0; i < tagsJson.length(); i++){
                    JSONObject tagJSON = tagsJson.getJSONObject(i);

                    realm.beginTransaction();
                    WorkTag tag = realm.createObject(WorkTag.class);
                    tag.fromJSONV1(tagJSON);
                    realm.commitTransaction();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean importJSONV0(JSONObject json){
        realm.beginTransaction();
        realm.delete(Day.class);
        realm.delete(Shift.class);
        realm.delete(WorkTag.class);
        realm.delete(Month.class);
        realm.delete(Year.class);
        realm.commitTransaction();

        try {
            JSONObject settingsJson = json.getJSONObject("Settings");

            if(settingsJson != null) {
                realm.beginTransaction();
                settings.fromJSONV0(settingsJson);
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
                    day.fromJSONV0(dayJSON);
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
                    tag.fromJSONV0(tagJson);
                    realm.commitTransaction();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return true;
    }

    public JSONObject exportToJSON(){
        JSONObject json = new JSONObject();

        try{
            json.put("date",System.currentTimeMillis()/1000);
            json.put("backupVersion",1);
            json.put("settings",settings.toJSON());

            JSONArray jsonTags = new JSONArray();

            RealmResults<WorkTag> tags = getAllTags();

            for(WorkTag t : tags) {
                jsonTags.put(t.toJSON());
            }

            json.put("tags",jsonTags);

            JSONArray jsonYears= new JSONArray();

            RealmResults<Year> years = getAllYearsOrdered();

            for(Year y : years) {
                jsonYears.put(y.toJSON());
            }

            json.put("years",jsonYears);


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
            /*RealmSchema schema = realm.getSchema();

            if(oldVersion == 0){
                schema.get("WorkTag")
                        .addField("trackMode",int.class);
            }

            if(oldVersion == 1){
                schema.get("AppSettings")
                        .addField("backupAutoEnabled",boolean.class)
                        .addField("backupsDaysToKeep",int.class);
            }*/
        }

        public int hashCode() {
            return DataMigration.class.hashCode();
        }

        public boolean equals(Object object) {
            if(object == null) {
                return false;
            }
            return object instanceof DataMigration;
        }
    }

    void startIntervalUpdater() {
        if(this != dataManager) return;
        intervalUpdater.run();
    }

    void stopIntervalUpdater() {
        if(this != dataManager) return;
        mHandler.removeCallbacks(intervalUpdater);
    }

    Runnable intervalUpdater = new Runnable() {
        @Override
        public void run() {
            try {
                callCallbacks();
            } finally {
                mHandler.postDelayed(this, 60000);
            }
        }
    };
}
