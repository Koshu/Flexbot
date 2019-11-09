package de.koshu.flexbot.data;

import java.util.Date;

import io.realm.RealmObject;

public class Event extends RealmObject {
    public String type = "None";
    public String info = "None";
    public String source = "None";
    public String tag = "UNTAGGED";
    public boolean filtered = false;

    public Date date = new Date();

    public boolean equals(String source, String type, String info){
        if(this.source.equals(source) && this.type.equals(type) && this.info.equals(info)){
            return true;
        } else {
            return false;
        }
    }

    public Event clone(){
        Event event = new Event();
        event.type = this.type;
        event.info = this.info;
        event.source = this.source;
        event.tag = this.tag;
        event.filtered = this.filtered;

        return event;
    }
}
