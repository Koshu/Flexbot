package de.koshu.flexbot.data;

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

    public int state = STATE_RUNNING;
    public int start = -1;  //Seconds since start of Day
    public int end = -1;  //Seconds since start of Day
    public int startConfidence;
    public int endConfidence;
    public int lastPauseStart;  //Seconds since start of Day
    public long pauseDuration;  //Seconds
    public String tag = "Untagged";

    @LinkingObjects("shifts")
    public final RealmResults<Day> days = null;



    public Day getDay(){
        if(days.size() > 1){
            Log.e("Shift", "Multiple days!");
            return null;
        }

        if(days.size() == 0) return null;

        return days.get(0);
    }

    public void setPauseInMin(String s){
        setPauseInMin(Integer.valueOf(s.trim()));
    }

    public void setPauseInMin(int min){
        pauseDuration = min * 60;
    }

    public void setState(String state){
        switch(state){
            case "Running": this.state = STATE_RUNNING; break;
            case "Done": this.state = STATE_CLOSED; break;
            case "Unsure": this.state = STATE_UNSURE; break;
            case "Paused": this.state = STATE_PAUSED; break;
        }
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
        long dur = getNettoDuration();
        return String.format("%1.1fh",(float) dur/3600.0f);
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

        return true;
    }

    public boolean endShift(LocalTime time, int confidence){
        if(state == STATE_CLOSED) return false;

        if(state == STATE_PAUSED && !unpauseShift(time,confidence)) return false;

        end = time != null ? time.toSecondOfDay() : LocalTime.now().toSecondOfDay();
        endConfidence = confidence;
        state = Shift.STATE_CLOSED;

        return true;
    }

    public boolean pauseShift(LocalTime time, int confidence){
        if(state != STATE_RUNNING) return false;

        lastPauseStart = time != null ? time.toSecondOfDay() : LocalTime.now().toSecondOfDay();
        state = STATE_PAUSED;

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
    }

    public void setStartTime(LocalTime time, int confidence){
        setStartTime(time);
        startConfidence = confidence;
    }

    public void setEndTime(String s){
        setEndTime(LocalTime.parse(s.trim()));
    }

    public void setEndTime(LocalTime time){
        end = time.toSecondOfDay();
    }

    public void setEndTime(LocalTime time, int confidence){
        setEndTime(time);
        endConfidence = confidence;
    }

    public LocalTime getEndLocalTime(){
        return LocalTime.ofSecondOfDay(end);
    }

    public String getEndTimeString(){
        if(end < 0) return "--:--";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return LocalTime.ofSecondOfDay(end).format(formatter);
    }

    public String getDateString() {
        return getDay().getDateString();
    }

    public JSONObject toJSON(){
        JSONObject json = new JSONObject();

        try {
            json.put("StartTime",getStartTimeString());
            json.put("StartTimeConf",getStartConfidenceString());
            json.put("State",getStateString());
            json.put("EndTime",getEndTimeString());
            json.put("EndTimeConf", getEndConfidenceString());
            json.put("PauseDuration",getPauseDurationString());
            json.put("Tag",this.tag);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return json;
    }

    public void fromJSON(JSONObject json){
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
    }

    public String toStringCSV(){
        String s = "SHIFT; ";

        s += getDateString() + "; ";
        s += getStartTimeString() + "; ";
        s += getStartConfidenceString() + "; ";
        s += getStateString() + "; ";
        s += getEndTimeString() + "; ";
        s += getEndConfidenceString() + "; ";
        s += getPauseDurationString() + "\n";

        return s;
    }

    public boolean fromStringCSV(String csv){
        String[] split = csv.split(";");

        if(!split[0].equals("SHIFT")) return false;
        try {
            String sStartTime = split[2];
            String sStartConf = split[3];
            String sState = split[4];
            String sEndTime = split[5];
            String sEndConf = split[6];
            String sPause = split[7];

            setStartTime(sStartTime);
            setStartConfidence(sStartConf);
            setState(sState);
            setEndTime(sEndTime);
            setEndConfidence(sEndConf);
            setPauseInMin(sPause);
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
