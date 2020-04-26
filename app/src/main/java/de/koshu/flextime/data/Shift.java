package de.koshu.flextime.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

public class Shift extends RealmObject {
    public static final int STATE_RUNNING = 0;
    public static final int STATE_CLOSED = 1;
    public static final int STATE_UNSURE = 2;
    public static final int STATE_PAUSED = 3;

    public static final int CONFIDENCE_MANUAL = 0;
    public static final int CONFIDENCE_AUTO_OK = 1;
    public static final int CONFIDENCE_AUTO_NOTOK = 2;

    private int state = STATE_RUNNING;
    private int start = -1;  //Seconds since start of Day
    private int end = -1;  //Seconds since start of Day
    private int startConfidence;
    private int endConfidence;
    private int lastPauseStart;  //Seconds since start of Day
    private long pauseDuration = 0;  //Seconds
    private String tag = "Untagged";

    private Day day;

    //CONSTRUCTORS
    public Shift(){
    }

    public Shift(Day day){
        this.day = day;
    }

    //INFORMATION FLOW
    public void update(){
        day.update();
    }

    // GETTER AND SETTER FUNCTIONS


    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
        update();
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
        update();
    }

    public int getStartConfidence() {
        return startConfidence;
    }

    public void setStartConfidence(int startConfidence) {
        this.startConfidence = startConfidence;
        update();
    }

    public int getEndConfidence() {
        return endConfidence;
    }

    public void setEndConfidence(int endConfidence) {
        this.endConfidence = endConfidence;
        update();
    }

    public void setState(int state) {
        this.state = state;
        update();
    }

    public int getLastPauseStart() {
        return lastPauseStart;
    }

    public void setLastPauseStart(int lastPauseStart) {
        this.lastPauseStart = lastPauseStart;
        update();
    }

    public void setPauseDuration(long pauseDuration) {
        this.pauseDuration = pauseDuration;
        update();
    }

    public Day getDay(){
        return day;
    }

    public void setPauseInMin(String s){
        setPauseInMin(Integer.valueOf(s.trim()));
    }

    public void setPauseInMin(int min){
        pauseDuration = min * 60;
        update();
    }

    public String getTag(){
        return tag;
    }

    public void setTag(String tag){
        this.tag = tag;
        update();
    }

    public int getState(){
        return state;
    }

    public boolean isState(int state){
        return this.state == state;
    }

    public void setState(String state){
        switch(state){
            case "Running": this.state = STATE_RUNNING; break;
            case "Done": this.state = STATE_CLOSED; break;
            case "Unsure": this.state = STATE_UNSURE; break;
            case "Paused": this.state = STATE_PAUSED; break;
        }

        update();
    }

    public String getStateString(){
        switch(state){
            case STATE_RUNNING: return "Running";
            case STATE_CLOSED: return "Done";
            case STATE_UNSURE: return "Unsure";
            case STATE_PAUSED: return "Paused";
            default: return "Unknown";
        }
    }

    public void setStartConfidence(String conf){
        switch(conf){
            case "Manual": startConfidence = CONFIDENCE_MANUAL; break;
            case "Auto": startConfidence = CONFIDENCE_AUTO_OK; break;
            case "Auto NotOK": startConfidence = CONFIDENCE_AUTO_NOTOK; break;
        }

        update();
    }

    public String getStartConfidenceString(){
        switch(startConfidence){
            case CONFIDENCE_MANUAL: return "Manual";
            case CONFIDENCE_AUTO_OK: return "Auto";
            case CONFIDENCE_AUTO_NOTOK: return "Auto NotOK";
            default: return "Unknown";
        }
    }

    public void setEndConfidence(String conf){
        switch(conf){
            case "Manual": endConfidence = CONFIDENCE_MANUAL; break;
            case "Auto": endConfidence = CONFIDENCE_AUTO_OK; break;
            case "Auto NotOK": endConfidence = CONFIDENCE_AUTO_NOTOK; break;
        }

        update();
    }

    public String getEndConfidenceString(){
        switch(endConfidence){
            case CONFIDENCE_MANUAL: return "Manual";
            case CONFIDENCE_AUTO_OK: return "Auto";
            case CONFIDENCE_AUTO_NOTOK: return "Auto NotOK";
            default: return "Unknown";
        }
    }

    public long getPauseDuration(){
        return pauseDuration;
    }

    public String getPauseDurationString(){
        return String.valueOf(pauseDuration/60);
    }

    public long getNettoDuration(){
        return getDuration() - getPauseDuration();
    }

    public String getNettoDurationString(){
        return hoursToString(getNettoDuration()/60.0f/60.0f);
    }

    private String hoursToString(float hours){
        int h = (int)hours;
        int m = (int)(hours%1*60.0f);

        return String.format("%d:%02d",h,m);
    }

    public long getDuration(){
        if(start < 0) return 0;

        long duration = 0;
        if(end < 0){
            LocalTime lt = LocalTime.now();
            duration = lt.toSecondOfDay() - start;
        } else {
            duration = end - start;
        }

        if(duration < 0){
            duration += 24*60*60;
        }

        return duration;
    }

    public boolean startShift(LocalTime time, int confidence){
        if(state != STATE_RUNNING) return false;

        start = time != null ? time.toSecondOfDay() : LocalTime.now().toSecondOfDay();
        startConfidence = confidence;

        update();
        return true;
    }

    public boolean endShift(LocalTime time, int confidence){
        if(state == STATE_CLOSED) return false;

        if(state == STATE_PAUSED && !unpauseShift(time,confidence)) return false;

        end = time != null ? time.toSecondOfDay() : LocalTime.now().toSecondOfDay();
        endConfidence = confidence;
        state = Shift.STATE_CLOSED;

        update();
        return true;
    }

    public boolean pauseShift(LocalTime time, int confidence){
        if(state != STATE_RUNNING) return false;

        lastPauseStart = time != null ? time.toSecondOfDay() : LocalTime.now().toSecondOfDay();
        state = STATE_PAUSED;

        update();
        return true;
    }

    public boolean unpauseShift(LocalTime time, int confidence){
        if(state != STATE_PAUSED) return false;

        if(time == null){
            time = LocalTime.now();
        }

        long pauseTime = time.toSecondOfDay() - lastPauseStart;

        if(pauseTime < 0) pauseTime += 24*60*60;


        pauseDuration += pauseTime;
        lastPauseStart = -1;
        state = STATE_RUNNING;

        update();
        return true;
    }

    public LocalTime getStartLocalTime(){
        return LocalTime.ofSecondOfDay(start);
    }

    public String getStartTimeString(){
        if(start < 0) return "--:--";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return LocalTime.ofSecondOfDay(start).format(formatter);
    }

    public void setStartTime(String s){
        setStartTime(LocalTime.parse(s.trim()));
    }

    public void setStartTime(LocalTime time){
        start = time.toSecondOfDay();
        update();
    }

    public void setStartTime(LocalTime time, int confidence){
        setStartTime(time);
        startConfidence = confidence;
        update();
    }

    public void setEndTime(String s){
        try {
            setEndTime(LocalTime.parse(s.trim()));
        } catch (Exception e){
            end = -1;
        }
        update();
    }

    public void setEndTime(LocalTime time){
        end = time.toSecondOfDay();
        update();
    }

    public void setEndTime(LocalTime time, int confidence){
        setEndTime(time);
        endConfidence = confidence;
        update();
    }

    public LocalTime getEndLocalTime(){
        return LocalTime.ofSecondOfDay(end);
    }

    public String getEndTimeString(){
        if(end < 0) return "--:--";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return LocalTime.ofSecondOfDay(end).format(formatter);
    }

    public String getPauseStartTimeString(){
        if(lastPauseStart < 0) return "--:--";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return LocalTime.ofSecondOfDay(lastPauseStart).format(formatter);
    }

    public String getDateString() {
        return getDay().getDateString();
    }

    public JSONObject toJSON(){
        JSONObject json = new JSONObject();

        try {
            json.put("startTime",getStartTimeString());
            json.put("startTimeConf",getStartConfidenceString());
            json.put("state",getStateString());
            json.put("endTime",getEndTimeString());
            json.put("endTimeConf", getEndConfidenceString());
            json.put("pauseDuration",getPauseDurationString());
            json.put("tag",this.tag);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return json;
    }

    public void fromJSONV0(JSONObject json){
        try {
            setStartTime(json.getString("StartTime"));
            setStartConfidence(json.getString("StartTimeConf"));
            setState(json.getString("State"));
            setEndTime(json.getString("EndTime"));
            setEndConfidence(json.getString("EndTimeConf"));
            setPauseInMin(json.getString("PauseDuration"));
            this.tag = json.optString("Tag","Untagged");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        update();
    }
    public void fromJSONV1(JSONObject json){
        try {
            setStartTime(json.getString("startTime"));
            setStartConfidence(json.getString("startTimeConf"));
            setState(json.getString("state"));
            setEndTime(json.getString("endTime"));
            setEndConfidence(json.getString("endTimeConf"));
            setPauseInMin(json.getString("pauseDuration"));
            this.tag = json.optString("tag","Untagged");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        update();
    }

}
