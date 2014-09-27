package com.nknytk.home_recorder_client;

import android.content.Context;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by nknytk on 14/09/23.
 */
public class MikeViewTask extends AsyncTask<Void, Void, Void> {
    Context context;
    Spinner spinner;
    String serverIP;
    ArrayList<String> deviceList = new ArrayList<String>();

    protected MikeViewTask(Context activityContext,Spinner deviceSelector, String serverip) {
        context = activityContext;
        spinner = deviceSelector;
        serverIP = serverip;
    }

    @Override
    protected Void doInBackground(Void... v) {
        getDeviceList();
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (String device: deviceList) adapter.add(device);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
    }

    private void getDeviceList() {
        String url = Common.getURL(serverIP, "/mike/devicelist", null);
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
}
