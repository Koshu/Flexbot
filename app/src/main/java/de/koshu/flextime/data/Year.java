package de.koshu.flextime.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.koshu.flextime.LocalizationHelper;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

public class Year extends RealmObject {
    private int yearInt;

    private float overtime = 0.0f;
    private int vacationDays = 0;
    private int sickDays = 0;
    private float paidOvertime = 0.0f;

    @LinkingObjects("year")
    private final RealmResults<Month> months = null;

    //CONSTRUCTORS
    public Year(){
    }

    public Year(int yearInt){
        this.yearInt = yearInt;
    }

    //INFORMATION FLOW
    public void update(){
        float newOvertime = 0.0f;
        float newPaidOvertime = 0.0f;
        int newSickDays = 0;
        int newVacationDays = 0;

        for(Month month : months){
            newOvertime += month.getOvertime();
            newSickDays += month.getSickDays();
            newVacationDays += month.getVacationDays();
            newPaidOvertime += month.getPaidOvertime();
        }

        overtime = newOvertime;
        sickDays = newSickDays;
        vacationDays = newVacationDays;
        paidOvertime = newPaidOvertime;
    }

    //HELPER FUNCTIONS
    public Month getMonth(int month){
        return months.where().equalTo("monthInt",month).findFirst();
    }

    public Month getOrAddMonth(int monthValue){
        Month month = getMonth(monthValue);
        if(month != null) return month;

        month = getRealm().copyToRealm(new Month(this, monthValue));

        return month;
    }

    public float getLastRestOvertime(){
        float last = 0.0f;
        Realm realm = getRealm();

        Year year = realm.where(Year.class)
                .equalTo("yearInt", yearInt-1)
                .findFirst();

        if(year != null){
            last = year.getRestOvertime();
        } else {
            AppSettings settings = realm.where(AppSettings.class).findFirst();
            last = settings.startOvertime;
        }

        return last;
    }

    public String getLastRestOvertimeString(){
        return LocalizationHelper.floatToHourString(getLastRestOvertime(),true);
    }

    public float getRestOvertime(){
        return overtime + getLastRestOvertime() - paidOvertime;
    }

    public String getRestOvertimeString(){
        return LocalizationHelper.floatToHourString(getRestOvertime(),true);
    }

    public String getOvertimeString(){
        return LocalizationHelper.floatToHourString(getOvertime(),true);
    }

    public String getPaidOvertimeString(){
        return LocalizationHelper.floatToHourString(getPaidOvertime(),true);
    }

    public String getYearString(){
        return String.valueOf(yearInt);
    }

    public String getSickDaysString(){
        return String.valueOf(sickDays);
    }

    public String getVacationDaysString(){
        return String.valueOf(vacationDays);
    }

    public int getEntitledVacationDays(){
        Realm realm = getRealm();
        AppSettings settings = realm.where(AppSettings.class).findFirst();
        //TODO: Make a setting
        return 28;
    }

    public int getRestVacationDays(){
        return getEntitledVacationDays() - vacationDays + getLastRestVacationDays();
    }

    public String getRestVacationDaysString(){
        return String.valueOf(getRestVacationDays());
    }

    public String getLastRestVacationDaysString(){
        return String.valueOf(getLastRestVacationDays());
    }

    public String getEntitledVacationDaysString(){
        return String.valueOf(getEntitledVacationDays());
    }

    public int getLastRestVacationDays(){
        int last = 0;
        Realm realm = getRealm();

        Year year = realm.where(Year.class)
                .equalTo("yearInt", yearInt-1)
                .findFirst();

        if(year != null){
            last = year.getRestVacationDays();
        } else {
            AppSettings settings = realm.where(AppSettings.class).findFirst();
            last = settings.startVacationDays;
        }

        return last;
    }

    //GETTER AND SETTER FUNCTIONS
    public int getYearInt() {
        return yearInt;
    }

    public void setYearInt(int yearInt) {
        this.yearInt = yearInt;
    }

    public float getOvertime() {
        return overtime;
    }

    public int getVacationDays() {
        return vacationDays;
    }

    public int getSickDays() {
        return sickDays;
    }

    public float getPaidOvertime(){
        return paidOvertime;
    }

    public RealmResults<Month> getMonths() {
        return months;
    }


    //IMPORT AND EXPORT FUNCTIONS
    public JSONObject toJSON(){
        JSONObject json = new JSONObject();

        try {
            json.put("yearInt",yearInt);

            JSONArray jsonArr = new JSONArray();
            for(Month m : months){
                jsonArr.put(m.toJSON());
            }

            json.put("months", jsonArr);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return json;
    }

    public void fromJSONV1(JSONObject json){
        try {
            yearInt = json.getInt("yearInt");

            JSONArray arr = json.getJSONArray("months");

            for(int i=0; i < arr.length(); i++){
                JSONObject j = arr.getJSONObject(i);
                Month month = getOrAddMonth(j.getInt("monthInt"));
                month.fromJSONV1(j);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        update();
    }
}
