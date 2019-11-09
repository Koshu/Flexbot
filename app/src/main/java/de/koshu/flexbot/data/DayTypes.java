package de.koshu.flexbot.data;

import android.content.Context;

import androidx.core.content.ContextCompat;

import de.koshu.flexbot.R;

public class DayTypes {
    public final static int WORKDAY = 0;
    public final static int RESTDAY = 1;
    public final static int HOLIDAY = 2;
    public final static int SICK = 3;
    public final static int VACATION = 4;
    public final static int OVERTIMEDEPLETING = 5;

    public static String[] dayStrings = {
            "Arbeitstag",
            "Ruhetag",
            "Feiertag",
            "Krank",
            "Urlaub",
            "Überstundenabbau"
    };

    private static float[] reqHours = {
            8.0f,
            0.0f,
            0.0f,
            0.0f,
            0.0f,
            8.0f
    };

    private static int[] colors = {
            R.color.colorBlueGray,
            R.color.colorGray,
            R.color.colorOrange,
            R.color.colorGreen,
            R.color.colorYellow,
            R.color.colorRed
    };

    private static int[] icons = {
            R.drawable.coffee,
            R.drawable.hotel,
            R.drawable.party_popper,
            R.drawable.heart_pulse,
            R.drawable.beach,
            R.drawable.help_circle_outline
    };

    public static String getName(int i){
        return dayStrings[i];
    }

    public static float getHours(int i){
        return reqHours[i];
    }

    public static int getValue(String s){
        for(int i = 0; i < dayStrings.length; i++){
            if(dayStrings[i].equals(s.trim())) return i;
        }

        return -1;
    }

    public static int getColor(Context context, int i){
        if(i < 0 || i >= colors.length) return ContextCompat.getColor(context,R.color.colorRed);

        return ContextCompat.getColor(context, colors[i]);
    }

    public static int getIconID(int i){
        if(i < 0 || i >= icons.length) return R.drawable.help_circle_outline;

        return icons[i];
    }
}
