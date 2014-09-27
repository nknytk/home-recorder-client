package com.nknytk.home_recorder_client;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nknytk on 14/09/23.
 */
public class AvailableFeatureTask extends AsyncTask <String, String, String> {
    Context context;
    SharedPreferences preferences;
    String networkName = "";
    Integer interval = Common.RequestIntervalMsec;
    List<String[]> availableFeatures;
    LinearLayout featureContainer;
    boolean toContinue = true;
    int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
    int MP = ViewGroup.LayoutParams.MATCH_PARENT;

    protected AvailableFeatureTask(Context parentContext, LinearLayout featureContainerLayout) {
        context = parentContext;
        preferences = context.getSharedPreferences(Common.PrefKey, Context.MODE_PRIVATE);
        featureContainer = featureContainerLayout;
    }

    @Override
    protected String doInBackground(String... v) {
        while(toContinue) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                break;
            }

            String serverIP = preferences.getString(Common.join(networkName, Common.CurrentServerIP), null);
            if (serverIP == null) {
                availableFeatures = null;
                publishProgress("");
                continue;
            }

            List<String[]> features = getAvailableFeatures(serverIP);
            if (features == null) {
                availableFeatures = null;
                publishProgress("");
                continue;
            }

            boolean isChanged = false;
            if (availableFeatures == null) {
                isChanged = true;
            } else if (availableFeatures.size() != features.size()) {
                isChanged = true;
            } else {
                for (int i = 0; i < features.size(); i++) {
                    if (!availableFeatures.get(i)[0].equals(features.get(i)[0])) isChanged = true;
                }
            }

            if (isChanged) {
                availableFeatures = features;
                publishProgress("");
            }
        }

        return "";
    }

    @Override
    protected void onProgressUpdate(String... val) {
        if (availableFeatures == null) {
            for (int i = 0; i < featureContainer.getChildCount(); i++) {
                TextView childView = (TextView)featureContainer.getChildAt(i);
                childView.setText(null);
            }
            return;
        }

        // header
        TextView headerText = (TextView)featureContainer.getChildAt(0);
        headerText.setText("Server Views");

        // contents
        for (int i = 1; i < featureContainer.getChildCount(); i++) {
            TextView childView = (TextView)featureContainer.getChildAt(i);
            childView.setText(null);
            String tag = (String)childView.getTag();
            for (String[] feature: availableFeatures) {
                if (tag.equals(feature[0])) {
                    childView.setText(feature[1]);
                    childView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String clickedTag = (String)v.getTag();
                            launchActivity(clickedTag);
                        }
                    });
                    break;
                }
            }
        }
    }

    private List getAvailableFeatures(String serverIP) {
        String url = Common.getURL(serverIP, "/webfeatures/available", "");
        byte[] result = Common.getUrlContent(url);
        if (result == null || result.length == 0) return null;

        JsonReader jsonReader = new JsonReader(new InputStreamReader((new ByteArrayInputStream(result))));
        List<String[]> featureList = new ArrayList<String[]>();
        try {
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                String[] feature = new String[2];
                jsonReader.beginArray();
                feature[0] = jsonReader.nextString();
                feature[1] = jsonReader.nextString();
                jsonReader.endArray();
                featureList.add(feature);
            }
            jsonReader.endArray();
            return featureList;

        } catch (IOException e) {
            Log.e("ERROR", e.toString());
            return null;
        }
    }

    @Override
    protected void onCancelled(){
        toContinue = false;
        Log.i("INFO", "AvailableFeatureTask is cancelled.");
    }

    protected void launchActivity(String tagname) {
        Intent intent = null;
        if (tagname.equals("camera")) {
            intent = new Intent(context, CameraViewActivity.class);
        } else if (tagname.equals("mike")) {
            intent = new Intent(context, MikeViewActivity.class);
        }

        if (intent == null) {
            String sorryMessage = "Sorry, this feature is yet to be implemented.";
            Toast.makeText(context, sorryMessage, Toast.LENGTH_SHORT).show();
        } else {
            intent.putExtra("ServerIP", preferences.getString(Common.join(networkName, Common.CurrentServerIP), null));
            context.startActivity(intent);
        }
    }
}