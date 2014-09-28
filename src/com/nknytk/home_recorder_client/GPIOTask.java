package com.nknytk.home_recorder_client;

import android.content.Context;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import android.widget.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by nknytk on 14/09/28.
 */
public class GPIOTask extends AsyncTask<Void, Void, Void> {
    Context context;
    TableLayout table;
    String serverIP;
    HashMap<String, String> GPIOStatus;
    boolean toContinue = true;
    Integer interval = Common.RequestIntervalMsec;
    int WC = TableRow.LayoutParams.WRAP_CONTENT;
    int MP = TableRow.LayoutParams.MATCH_PARENT;
    int textSize = 20;

    protected GPIOTask (Context activityContext, TableLayout tableLayout,String ip) {
        context = activityContext;
        table = tableLayout;
        serverIP = ip;
    }

    @Override
    protected Void doInBackground(Void... v) {
        while (toContinue) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                break;
            }

            HashMap<String, String> currentStatus = getGPIOStatus();
            boolean same = isSame(currentStatus, GPIOStatus);
            Log.i("INFO", String.valueOf(same));
            if (same) continue;

            GPIOStatus = currentStatus;
            publishProgress();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... v) {
        table.removeAllViewsInLayout();
        if (GPIOStatus == null) return;

        table.addView(createRow("Pin", "Status"));
        for (String pin: GPIOStatus.keySet()) {
            table.addView(createRow(pin, GPIOStatus.get(pin)));
        }

        Log.i("INFO", "UI SHOUD BE UPDATED");
    }

    private TableRow createRow(String key, String value) {
        TableRow row = new TableRow(context);
        TextView pinName = new TextView(context);
        TextView pinValue = new TextView(context);

        row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
        pinName.setText(key);
        pinName.setLayoutParams(new TableRow.LayoutParams(MP, WC, 1));
        pinName.setTextSize(textSize);
        pinValue.setText(value);
        pinValue.setLayoutParams(new TableRow.LayoutParams(MP, WC, 1));
        pinValue.setTextSize(textSize);

        row.addView(pinName);
        row.addView(pinValue);
        return row;
    }

    @Override
    protected void onCancelled() {
        toContinue = false;
    }

    private HashMap<String, String> getGPIOStatus() {
        String url = Common.getURL(serverIP, "/gpio/pinstatus", "");
        byte[] result = Common.getUrlContent(url);
        if (result == null) return null;

        HashMap<String, String> currentStatus = new HashMap<String, String>();
        JsonReader jsonReader = new JsonReader(new InputStreamReader((new ByteArrayInputStream(result))));
        try {
            jsonReader.beginObject();
            while (jsonReader.hasNext()) currentStatus.put(jsonReader.nextName(), jsonReader.nextString());
            jsonReader.endObject();
        } catch (IOException e) {
        }

        return currentStatus;
    }

    private boolean isSame(HashMap<String, String> hash1, HashMap<String, String> hash2) {
        if ((hash1 == null) && (hash2 == null)) return true;
        if (((hash1 != null) && (hash2 == null)) || ((hash1 == null) && (hash2 != null))) return false;
        if (hash1.size() != hash2.size()) return false;
        for (String key: hash1.keySet()) {
            if (hash2.get(key) == null || !hash1.get(key).equals(hash2.get(key))) return false;
        }
        return true;
    }
}
