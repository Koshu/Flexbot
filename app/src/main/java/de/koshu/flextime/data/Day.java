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

import de.koshu.flextime.LocalizationHelper;
import de.koshu.flextime.R;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

public class Day extends RealmObject {
    private int dayInt;
    private int monthInt;
    private int yearInt;

    private Month monthObject;

    private int type = 0;
    private float requiredWorkHours = 0.0f;
    private float workHours = 0.0f;

    @LinkingObjects("day")
    private final RealmResults<Shift> shifts = null;

    //CONSTRUCTORS
    public Day(){
    }

    public Day(Month monthObject, int dayInt){
        this.monthObject = monthObject;
        this.dayInt = dayInt;
        this.monthInt = monthObject.getMonthInt();
        this.yearInt = monthObject.getYear().getYearInt();

        if (isWeekend()) {
            setType(DayTypes.RESTDAY);
        } else {
            setType(DayTypes.WORKDAY);
        }
    }

    //INFORMATION FLOW
    public void update(){
        float newWorkHours = 0.0f;

        if(shifts !=  null) {
            for (Shift shift : shifts) {
                newWorkHours += shift.getNettoDuration() / 3600.0f;
            }
        }

        workHours = newWorkHours;

        monthObject.update();
    }

    //HELPER FUNCTIONS
    public DayOfWeek getDayOfWeek(){
        LocalDate ld = getLocalDate();

        return ld.getDayOfWeek();
    }

    public LocalDate getLocalDate(){
        return LocalDate.of(getYearInt(),getMonthInt(), dayInt);
    }

    public int getMonthInt(){
        return monthInt;
    }

    public int getYearInt(){
        return yearInt;
    }

    public String getDayOfWeekString(){
        return getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.GERMANY);
    }

    public String getDayOfWeekStringShort(){
        return getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.GERMANY);
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

    public String getDateStringShort(){
        LocalDate lDate = getLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
        return lDate.format(formatter);
    }

    public float getOvertime(){
        return workHours - requiredWorkHours;
    }

    public String getOvertimeString(){
        return LocalizationHelper.floatToHourString(getOvertime(),true);
    }

    public float getRequiredWork(){
        return requiredWorkHours;
    }

    public String getRequiredWorkString(){
        return LocalizationHelper.floatToHourString(requiredWorkHours,false);
    }

    public Shift startNewShift(LocalTime time, int confidence) {
        Shift newShift = new Shift(this);

        newShift = getRealm().copyToRealm(newShift);
        newShift.startShift(time,confidence);

        update();
        return newShift;
    }

    public void deleteShift(Shift shift){
        shift.deleteFromRealm();
        update();
    }
    public float getWorkHours(){
        return workHours;
    }

    public String getCumulatedNettoDurationString(){
        return LocalizationHelper.floatToHourString(workHours,false);
    }

    public boolean isLastDayOfMonth(){
        Day otherDay = monthObject.getDays().where()
                .greaterThan("dayInt", dayInt)
                .findFirst();

        return otherDay == null;
    }

    public boolean isFirstDayOfMonth(){
        Day otherDay = monthObject.getDays().where()
                .lessThan("dayInt", dayInt)
                .findFirst();

        return otherDay == null;
    }

    public int getDayInt() {
        return dayInt;
    }

    public Month getMonthObject() {
        return monthObject;
    }

    public float getRequiredWorkHours() {
        return requiredWorkHours;
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
        update();
    }

    public int getType(){
        return type;
    }

    public RealmResults<Shift> getShifts(){
        return shifts;
    }

    public void setType(String type){
        setType(DayTypes.getValue(type));
    }

    public String getTypeString(){
        return DayTypes.getName(type);
    }

    public Shift addEmptyShift(){
        Shift shift = new Shift(this);
        shift = getRealm().copyToRealm(shift);

        LocalTime l1 = LocalTime.of(9,0);
        LocalTime l2 = LocalTime.of(17,0);

        shift.setStartTime(l1, Shift.CONFIDENCE_MANUAL);
        shift.setEndTime(l2, Shift.CONFIDENCE_MANUAL);

        update();
        return shift;
    }

    public JSONObject toJSON(){
        JSONObject json = new JSONObject();

        try {
            json.put("dayInt",dayInt);
            json.put("type",getTypeString());
            json.put("requiredWorkHours",requiredWorkHours);

            JSONArray jsonArr = new JSONArray();
            for(Shift shift : shifts){
                jsonArr.put(shift.toJSON());
            }

            json.put("shifts", jsonArr);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return json;
    }

    public void fromJSONV0(JSONObject json){
        try {
            setType(json.getString("Type"));
            requiredWorkHours = (float) json.getDouble("RequiredWork");

            JSONArray shiftsArr = json.getJSONArray("Shifts");

            for(int i=0; i < shiftsArr.length(); i++){
                JSONObject shiftJSON = shiftsArr.getJSONObject(i);
                Shift shift = addEmptyShift();
                shift.fromJSONV0(shiftJSON);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        update();
    }

    public void fromJSONV1(JSONObject json){
        try {
            setType(json.getString("type"));
            requiredWorkHours = (float) json.getDouble("requiredWorkHours");

            JSONArray shiftsArr = json.getJSONArray("shifts");

            for(int i=0; i < shiftsArr.length(); i++){
                JSONObject shiftJSON = shiftsArr.getJSONObject(i);
                Shift shift = addEmptyShift();
                shift.fromJSONV1(shiftJSON);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        update();
    }
}
