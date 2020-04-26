package de.koshu.flextime.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;
import io.realm.RealmObject;

public class WorkTag extends RealmObject {
    public final static int MODE_MANUAL = 0;
    public final static int MODE_SEMIAUTO = 1;
    public final static int MODE_AUTO = 2;

    public final static int TRACKMODE_HYBRID = 0;
    public final static int TRACKMODE_WIFI = 1;
    public final static int TRACKMODE_GEO = 2;


    public String name = "New Tag";
    public int mode = MODE_MANUAL;
    public int trackMode = 0;

    public RealmList<String> associatedWIFIs = new RealmList<>();

    public float geoLatitude = 0.0f;
    public float geoLongitute= 0.0f;

    public boolean addAutoPause = true;
    public int addAutoPauseTime = 30;

    public void setMode(String mode){
        switch (mode){
            case "MANUAL":
                this.mode = MODE_MANUAL; break;
            case "SEMIAUTO":
                this.mode = MODE_SEMIAUTO; break;
            case "AUTO":
                this.mode = MODE_AUTO; break;
        }
    }

    public String getModeString(){
        switch (mode){
            case MODE_MANUAL:
                return "MANUAL";
            case MODE_SEMIAUTO:
                return "SEMIAUTO";
            case MODE_AUTO:
                return "AUTO";
        }

        return null;
    }

    //TODO Add trackmode to json

    public JSONObject toJSON(){
        JSONObject json = new JSONObject();

        try {
            json.put("name",name);
            json.put("mode",getModeString());
            json.put("geoLatitude",geoLatitude);
            json.put("geoLongitute",geoLongitute);
            json.put("addAutoPause",addAutoPause);
            json.put("addAutoPauseTime",addAutoPauseTime);

            JSONArray wifiArr = new JSONArray();

            for(String s : associatedWIFIs){
                wifiArr.put(s);
            }

            json.put("wifis",wifiArr);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return json;
    }

    public void fromJSONV0(JSONObject json){
        try {
            name = json.getString("Name");
            setMode(json.getString("Mode"));
            geoLatitude = (float) json.getDouble("GeoLatitude");
            geoLongitute = (float) json.getDouble("GeoLongitute");
            addAutoPause = json.getBoolean("addAutoPause");
            addAutoPauseTime = json.getInt("addAutoPauseTime");

            JSONArray wifiArr = json.getJSONArray("Wifis");

            for(int i = 0; i < wifiArr.length(); i++){
                associatedWIFIs.add(wifiArr.getString(i));;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void fromJSONV1(JSONObject json){
        try {
            name = json.getString("name");
            setMode(json.getString("mode"));
            geoLatitude = (float) json.getDouble("geoLatitude");
            geoLongitute = (float) json.getDouble("geoLongitute");
            addAutoPause = json.getBoolean("addAutoPause");
            addAutoPauseTime = json.getInt("addAutoPauseTime");

            JSONArray wifiArr = json.getJSONArray("wifis");

            for(int i = 0; i < wifiArr.length(); i++){
                associatedWIFIs.add(wifiArr.getString(i));;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
