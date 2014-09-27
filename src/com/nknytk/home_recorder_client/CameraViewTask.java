package com.nknytk.home_recorder_client;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by nknytk on 14/09/07.
 */
public class CameraViewTask extends AsyncTask<String, Bitmap, Bitmap> {
    Context context;
    Spinner spinner;
    ImageView view;
    String serverIP;
    Integer interval = Common.RequestIntervalMsec;
    ArrayList<String> deviceList = new ArrayList<String>();
    boolean toContinue = true;
    boolean devicesAreSet = false;

    protected CameraViewTask(Context activityContext,Spinner deviceSelector, ImageView imgView, String serverip) {
        context = activityContext;
        spinner = deviceSelector;
        view = imgView;
        serverIP = serverip;
    }

    @Override
    protected Bitmap doInBackground(String... values) {
        Bitmap image = null;
        while (toContinue) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                break;
            }

            if (deviceList == null || deviceList.size() == 0) getDeviceList();
            if (deviceList == null || deviceList.size() == 0) continue;
            publishProgress(image);

            try {
                image = getImage();
            } catch (Error e) {
                image = null;
                Log.e("ERROR", e.toString());
                continue;
            }
            publishProgress(image);
        }
        return image;
    }

    @Override
    protected void onProgressUpdate(Bitmap... images) {
        // set device list to spinner if not set
        if (!devicesAreSet) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            for (String device: deviceList) adapter.add(device);
            spinner.setAdapter(adapter);
            spinner.setSelection(0);

            devicesAreSet = true;
            return;
        }

        // if spinner is set, update image
        if (images == null) {
            view.setImageResource(0);
        } else {
            view.setImageBitmap(images[0]);
        }
    }

    private Bitmap getImage() {
        String devicename = (String)spinner.getSelectedItem();
        String url = Common.getURL(serverIP, "/camera/image", Common.join("device=", devicename, ""));
        byte[] result = Common.getUrlContent(url);
        try {
            Bitmap bmp = BitmapFactory.decodeByteArray(result, 0, result.length);
            return bmp;
        } catch (Exception e) {
            Log.e("ERROR", "ERROR", e);
            return null;
        }
    }

    private void getDeviceList() {
        String url = Common.getURL(serverIP, "/camera/devicelist", null);
        byte[] result = Common.getUrlContent(url);
        if (result == null) return;


        JsonReader jsonReader = new JsonReader(new InputStreamReader((new ByteArrayInputStream(result))));
        try {
            jsonReader.beginArray();
            while (jsonReader.hasNext()) deviceList.add(jsonReader.nextString());
            jsonReader.endArray();
        } catch (IOException e) {
        }
    }

    @Override
    protected void onCancelled(){
        toContinue = false;
    }
}
