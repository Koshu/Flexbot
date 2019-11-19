package de.koshu.flextime.data;

import java.util.Date;

import io.realm.RealmObject;

public class Condition extends RealmObject {
    public String type = "None";
    public String info = "None";
    public String source = "None";
    public Date date = new Date();
}
