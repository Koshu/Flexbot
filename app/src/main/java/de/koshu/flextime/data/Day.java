package de.koshu.flextime.data;

import android.content.Context;

import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.TextStyle;

import java.util.Locale;

import de.koshu.flextime.R;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

public class Day extends RealmObject {
    public int date;
    public int month;
    public int year;
    public int type = 0;
    public float requiredWorkHours;

    public RealmList<Shift> shifts;

    //HELPER FUNCTIONS
    public DayOfWeek getDayOfWeek(){
        LocalDate ld = getLocalDate();

        return ld.getDayOfWeek();
    }

    public LocalDate getLocalDate(){
        return LocalDate.of(year,month,date);
    }

    public String getDayOfWeekString(){
        return getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.GERMANY);
    }

    public String getDayOfWeekStringShort(){
        return getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.GERMANY);
    }

    public void setDate(LocalDate lDate) {
        this.date = lDate.getDayOfMonth();
        this.month = lDate.getMonthValue();
        this.year = lDate.getYear();

        if (type == DayTypes.WORKDAY || type == DayTypes.RESTDAY){
            if (isWeekend()) {
                setType(DayTypes.RESTDAY);
            } else {
                setType(DayTypes.WORKDAY);
            }
        }
    }

    public boolean isWeekend(){
        DayOfWeek dow = getDayOfWeek();

        if(dow == DayOfWeek.SATURDAY) return true;
        if(dow == DayOfWeek.SUNDAY) return true;

        return false;
    }

    public String getMonthString(){
        LocalDate ld = getLocalDate();

        return ld.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMANY);
    }

    public String getDateString(){
        LocalDate lDate = getLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return lDate.format(formatter);
    }

    public void setDate(String date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate lDate = LocalDate.parse(date.trim(),formatter);

        this.month = lDate.getMonthValue();
        this.year = lDate.getYear();
        this.date = lDate.getDayOfMonth();
    }

    public float getOvertime(){
        return getCumulatedNettoDuration() - getRequiredWork();
    }

    public float getRequiredWork(){
        return requiredWorkHours;
    }

    public Shift startNewShift(LocalTime time, int confidence) {
        Shift newShift = getRealm().createObject(Shift.class);
        newShift.startShift(time,confidence);

        shifts.add(newShift);

        return newShift;
    }

    public float getCumulatedNettoDuration(){
        float duration = 0;

        for(Shift s : shifts){
            duration += s.getNettoDuration()/3600.0f;
        }

        return duration;
    }

    public boolean isLastDayOfMonth(){
        Realm realm = getRealm();

        Day otherDay = realm.where(Day.class)
                .equalTo("month",month)
                .equalTo("year", year)
                .greaterThan("date",date)
                .findFirst();

        return otherDay == null;
    }

    public boolean isFirstDayOfMonth(){
        Realm realm = getRealm();

        Day otherDay = realm.where(Day.class)
                .equalTo("month",month)
                .equalTo("year", year)
                .lessThan("date",date)
                .findFirst();

        return otherDay == null;
    }

    public int getIcon(){
        return DayTypes.getIconID(type);
    }

    public int getIconColor(Context context){
        return DayTypes.getColor(context,type);
    }

    public int getProgColor(Context context){
        float overtime = getOvertime();

        if(overtime < -3){
            return ContextCompat.getColor(context,R.color.colorRed);
        } else if(overtime < -2) {
            return ContextCompat.getColor(context,R.color.colorDeepOrange);
        } else if(overtime < -1) {
            return ContextCompat.getColor(context, R.color.colorOrange);
        } else if(overtime < 0) {
            return ContextCompat.getColor(context, R.color.colorYellow);
        } else if(overtime < 1) {
            return ContextCompat.getColor(context, R.color.colorLime);
        } else if(overtime < 2) {
            return ContextCompat.getColor(context, R.color.colorLightGreen);
        } else if(overtime < 3) {
            return ContextCompat.getColor(context, R.color.colorGreen);
        } else {
            return ContextCompat.getColor(context, R.color.colorTeal);
        }
    }

    public void setType(int type){
        this.type = type;
        this.requiredWorkHours = DayTypes.getHours(type);
    }

    public void setType(String type){
        setType(DayTypes.getValue(type));
    }

    public String getTypeString(){
        return DayTypes.getName(type);
    }

    public Shift addEmptyShift(){
        Shift shift = getRealm().createObject(Shift.class);

        LocalTime l1 = LocalTime.of(9,0);
        LocalTime l2 = LocalTime.of(17,0);

        shift.setStartTime(l1, Shift.CONFIDENCE_MANUAL);
        shift.setEndTime(l2, Shift.CONFIDENCE_MANUAL);

        shifts.add(shift);

        return shift;
    }

    public String toStringCSV(){
        String s = "DAY; ";

        s += getDateString() + "; ";
        s += getTypeString() + "; ";
        s += requiredWorkHours + "\n";

        for(Shift shift : shifts){
            s += shift.toStringCSV();
        }

        return s;
    }

    public boolean fromStringCSV(String csv){
        String[] split = csv.split(";");

        if(!split[0].equals("DAY")) return false;
        try {
            String sDate = split[1];
            String sType = split[2];
            String sHours = split[3];

            setDate(sDate);
            setType(sType);
            requiredWorkHours = Float.valueOf(sHours.trim());
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public JSONObject toJSON(){
        JSONObject json = new JSONObject();

        try {
            json.put("Date",getDateString());
            json.put("Type",getTypeString());
            json.put("RequiredWork",requiredWorkHours);

            JSONArray jsonArr = new JSONArray();
            for(Shift shift : shifts){
                jsonArr.put(shift.toJSON());
            }

            json.put("Shifts", jsonArr);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return json;
    }

    public void fromJSON(JSONObject json){
        try {
            setDate(json.getString("Date"));
            setType(json.getString("Type"));
            requiredWorkHours = (float) json.getDouble("RequiredWork");

            JSONArray shiftsArr = json.getJSONArray("Shifts");

            for(int i=0; i < shiftsArr.length(); i++){
                JSONObject shiftJSON = shiftsArr.getJSONObject(i);
                Shift shift = addEmptyShift();
                shift.fromJSON(shiftJSON);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
