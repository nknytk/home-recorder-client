package com.nknytk.home_recorder_client;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Spinner;

/**
 * Created by nknytk on 14/09/23.
 */
public class CameraViewActivity extends Activity {
    String serverIP;
    Spinner deviceSelector;
    ImageView imageView;
    CameraViewTask updateTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_view_activity);

        serverIP = getIntent().getExtras().getString("ServerIP", null);
        deviceSelector = (Spinner)findViewById(R.id.device_selector);
        imageView = (ImageView)findViewById(R.id.image);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (updateTask != null && updateTask.toContinue) return;

        updateTask = new CameraViewTask(this, deviceSelector, imageView, serverIP);
        Thread imageUpdaterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                updateTask.execute("");
            }
        });
        imageUpdaterThread.start();
    }

    @Override
    public void onPause() {
        updateTask.cancel(true);
        super.onPause();
    }
}