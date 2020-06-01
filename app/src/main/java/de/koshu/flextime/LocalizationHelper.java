package de.koshu.flextime;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.TextStyle;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class LocalizationHelper {
    public static String floatToHourString(float f, boolean withSign){
        int hours = (int) f;
        int minutes = (int) (f%1*60.0);

        String res = String.format(Locale.GERMANY,"%d:%02d",Math.abs(hours),Math.abs(minutes));

        if(withSign && f > 0.0f) {
            res = "+"+res;
        } else if(f < 0.0f){
            res = "-"+res;
        }

        return res;
    }

    public static Float hourStringToFloat(String s){
        String[] strings = s.split(Pattern.quote(":"));
        String hour,minutes;
        float hourF = 0.0f;
        float minutesF = 0.0f;

        if(strings.length == 2) {
            hour = strings[0];
            minutes = strings[1];
        } else if(strings.length == 1){
            int l = s.length();
            int split = l-2;

            if(split > 0){
                hour = s.substring(0,split);
                minutes = s.substring(split,l);
            } else {
                hour = "0";
                minutes = s;
            }
        } else {
            return 0.0f;
        }

        hourF = Float.valueOf(hour);
        minutesF = Float.valueOf(minutes)/ 60.0f;

        if (hourF < 0) {
            hourF -= minutesF;
        } else {
            hourF += minutesF;
        }

        return hourF;
    }

    public static String getMonthName(int year, int month){
        try {
            LocalDate date = LocalDate.of(year, month, 1);
            return getMonthName(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "Unkown";
        }
    }

    public static String getMonthName(LocalDate date){
        return date.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMANY);
    }
}
