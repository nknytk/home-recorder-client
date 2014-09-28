package com.nknytk.home_recorder_client;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TableLayout;

/**
 * Created by nknytk on 14/09/28.
 */
public class GPIOViewActivity extends Activity {
    TableLayout table;
    GPIOTask updateTask;
    String serverIP;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gpio_view_activity);

        serverIP = getIntent().getExtras().getString("ServerIP", null);
        table = (TableLayout)findViewById(R.id.table);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (updateTask != null && updateTask.toContinue) return;

        updateTask = new GPIOTask(this, table, serverIP);
        Thread updaterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                updateTask.execute();
            }
        });
        updaterThread.start();
    }

    @Override
    protected void onPause(){
        updateTask.cancel(true);
        super.onPause();
    }
}