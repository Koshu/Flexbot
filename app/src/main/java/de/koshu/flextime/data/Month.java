package de.koshu.flextime.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import de.koshu.flextime.LocalizationHelper;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.LinkingObjects;

public class Month extends RealmObject {
    private int monthInt;
    private int yearInt;

    private float overtime = 0.0f;
    private int vacationDays = 0;
    private int sickDays = 0;
    private float paidOvertime = 0.0f;

    private Year year;
    @LinkingObjects("monthObject")
    private final RealmResults<Day> days = null;

    //CONSTRUCTORS
    public Month(){
    }

    public Month(Year year, int monthInt){
        this.year = year;
        this.yearInt = year.getYearInt();
        this.monthInt = monthInt;
    }

    //INFORMATION FLOW
    public void update(){
        float newOvertime = 0.0f;
        int newSickDays = 0;
        int newVacationDays = 0;

        for(Day day : days){
            newOvertime += day.getOvertime();

            if(day.getType() == DayTypes.SICK){
                newSickDays++;
            }else if(day.getType() == DayTypes.VACATION){
                newVacationDays++;
            }

        }

        overtime = newOvertime;
        sickDays = newSickDays;
        vacationDays = newVacationDays;

        year.update();
    }

    //HELPER FUNCTIONS
    public Day getDay(int date){
        return days.where().equalTo("dayInt",date).findFirst();
    }

    public Day getOrAddDay(int date){
        Day day = getDay(date);
        if(day != null) return day;

        day = getRealm().copyToRealm(new Day(this, date));

        return day;
    }



    public String getMonthString(){
        return LocalizationHelper.getMonthName(yearInt, monthInt);
    }

    // GETTER AND SETTER FUNCTIONS
    public int getMonthInt(){
        return monthInt;
    }

    public Year getYear(){
        return year;
    }

    public int getYearInt(){
        return yearInt;
    }

    public float getOvertime(){
        return overtime;
    }

    public String getOvertimeString(){
        return LocalizationHelper.floatToHourString(getOvertime(),true);
    }

    public float getRestOvertime(){
        return overtime - paidOvertime;
    }

    public String getRestOvertimeString(){
        return LocalizationHelper.floatToHourString(getRestOvertime(),true);
    }

    public float getPaidOvertime(){
        return paidOvertime;
    }

    public String getPaidOvertimeString(){
        return LocalizationHelper.floatToHourString(getPaidOvertime(),true);
    }

    public int getVacationDays(){
        return vacationDays;
    }

    public String getVacationDaysString(){
        return String.format(Locale.GERMANY,"%d",getVacationDays());
    }

    public int getSickDays(){
        return sickDays;
    }

    public String getSickDaysString(){
        return String.format(Locale.GERMANY,"%d",getSickDays());
    }

    public RealmResults<Day> getDays(){
        return days;
    }
    //CONVERT FUNCTIONS
    public JSONObject toJSON(){
        JSONObject json = new JSONObject();

        try {
            json.put("monthInt",monthInt);

            JSONArray jsonArr = new JSONArray();
            for(Day d : days){
                jsonArr.put(d.toJSON());
            }

            json.put("days", jsonArr);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return json;
    }

    public void fromJSONV1(JSONObject json){
        try {
            monthInt = json.getInt("monthInt");

            JSONArray arr = json.getJSONArray("days");

            for(int i=0; i < arr.length(); i++){
                JSONObject j = arr.getJSONObject(i);
                Day day = getOrAddDay(j.getInt("dayInt"));
                day.fromJSONV1(j);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        update();
    }
}
