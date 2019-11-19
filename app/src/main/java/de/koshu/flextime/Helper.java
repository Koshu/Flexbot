package de.koshu.flextime;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Helper {
    public static String floatToHourString(float f, boolean withSign){
        int hours = (int) f;
        int minutes = (int) (f%1*60.0);
        String res = String.format("%d:%02d",Math.abs(hours),Math.abs(minutes));

        if(f < 0.0f){
            res = "-"+res;
        } else if(withSign) {
            res = "+"+res;
        }

        return res;
    }

    public static byte[] compress(String data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
        GZIPOutputStream gzip = new GZIPOutputStream(bos);
        gzip.write(data.getBytes());
        gzip.close();
        byte[] compressed = bos.toByteArray();
        bos.close();
        return compressed;
    }

    public static String decompress(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        GZIPInputStream gzip = new GZIPInputStream(bais);
        InputStreamReader reader = new InputStreamReader(gzip);
        BufferedReader in = new BufferedReader(reader);

        String result = "";
        String line;
        while ((line = in.readLine()) != null) {
            Log.d("Test",line);
            result += line;
        }

        return result;
    }

    public static void writeToFile(File path, byte[] data) throws IOException {
        BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(path));

        writer.write(data);
        writer.flush();
        writer.close();
    }

    public static void writeToFile(File path, String data) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));

        writer.write(data);
        writer.flush();
        writer.close();
    }

    public static byte[] readFileFromUri(Context context, Uri uri){
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        try {
            InputStream inputStream =
                    context.getContentResolver().openInputStream(uri);

            BufferedInputStream reader = new BufferedInputStream(inputStream);

            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int len = 0;
            while ((len = reader.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }

        } catch(Exception e){
            e.printStackTrace();
            return null;
        }

        return byteBuffer.toByteArray();
    }
}
