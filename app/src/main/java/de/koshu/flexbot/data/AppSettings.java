package de.koshu.flexbot.data;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.DayOfWeek;

import io.realm.RealmObject;

public class AppSettings extends RealmObject {
    public float hoursToWork_Monday = 8.0f;
    public float hoursToWork_Tuesday = 8.0f;
    public float hoursToWork_Wednesday = 8.0f;
    public float hoursToWork_Thursday = 8.0f;
    public float hoursToWork_Friday = 8.0f;
    public float hoursToWork_Saturday = 0.0f;
    public float hoursToWork_Sunday = 0.0f;

    public float startOvertime = 0.0f;
    public int startVacationDays = 0;

    public float getHoursToWork(DayOfWeek d){
        switch(d) {
            case MONDAY: return hoursToWork_Monday;
            case TUESDAY: return hoursToWork_Tuesday;
            case WEDNESDAY: return hoursToWork_Wednesday;
            case THURSDAY: return hoursToWork_Thursday;
            case FRIDAY: return hoursToWork_Friday;
            case SATURDAY: return hoursToWork_Saturday;
            case SUNDAY: return hoursToWork_Sunday;
        }

        return -1;
    }

    public void setHoursToWork(DayOfWeek d, float hours){
        switch(d) {
            case MONDAY: hoursToWork_Monday = hours; break;
            case TUESDAY: hoursToWork_Tuesday = hours; break;
            case WEDNESDAY: hoursToWork_Wednesday = hours; break;
            case THURSDAY: hoursToWork_Thursday = hours; break;
            case FRIDAY: hoursToWork_Friday = hours; break;
            case SATURDAY: hoursToWork_Saturday = hours; break;
            case SUNDAY: hoursToWork_Sunday = hours; break;
        }
    }

    public JSONObject toJSON(){
        JSONObject json = new JSONObject();

        try {
            json.put("HoursOnMonday",hoursToWork_Monday);
            json.put("HoursOnTuesday",hoursToWork_Tuesday);
            json.put("HoursOnWednesday",hoursToWork_Wednesday);
            json.put("HoursOnThursday",hoursToWork_Thursday);
            json.put("HoursOnFriday",hoursToWork_Friday);
            json.put("HoursOnSaturday",hoursToWork_Saturday);
            json.put("HoursOnSunday",hoursToWork_Sunday);
            json.put("StartOvertime",startOvertime);
            json.put("StartVacationDays",startVacationDays);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return json;
    }

    public void fromJSON(JSONObject json){
        try {
            hoursToWork_Monday = (float) json.getDouble("HoursOnMonday");
            hoursToWork_Tuesday = (float) json.getDouble("HoursOnTuesday");
            hoursToWork_Wednesday = (float) json.getDouble("HoursOnWednesday");
            hoursToWork_Thursday = (float) json.getDouble("HoursOnThursday");
            hoursToWork_Friday = (float) json.getDouble("HoursOnFriday");
            hoursToWork_Saturday = (float) json.getDouble("HoursOnSaturday");
            hoursToWork_Sunday = (float) json.getDouble("HoursOnSunday");

            startOvertime = (float) json.getDouble("StartOvertime");
            startVacationDays = json.getInt("StartVacationDays");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
