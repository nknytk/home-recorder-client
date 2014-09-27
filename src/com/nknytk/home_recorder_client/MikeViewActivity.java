package com.nknytk.home_recorder_client;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.VideoView;

/**
 * Created by nknytk on 14/09/23.
 */
//public class MikeViewActivity extends Activity implements MediaController.MediaPlayerControl {
public class MikeViewActivity extends Activity {
    String serverIP;
    Spinner deviceSelector;
    VideoView player;
    MikeViewTask updateTask;
    MediaController controller;
    ProgressDialog progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mike_view_activity);

        serverIP = getIntent().getExtras().getString("ServerIP", null);
        deviceSelector = (Spinner)findViewById(R.id.device_selector);

        player = (VideoView)findViewById(R.id.player);
        controller = new MediaController(this);
        controller.setAnchorView(deviceSelector);
        controller.setMediaPlayer(player);

        progress = new ProgressDialog(this);
        progress.setMessage("Loading sound...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        deviceSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                callPlayer();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                callPlayer();
            }

            private void callPlayer() {
                String device = (String) deviceSelector.getSelectedItem();
                if (player.isPlaying()) player.stopPlayback();
                String url = Common.getURL(serverIP, "/mike/mp3", Common.join("device=", device, ""));
                player.setVideoURI(Uri.parse(url));
                progress.show();

                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        progress.dismiss();
                        player.start();
                        controller.show(player.getDuration());
                    }
                });
            }
        });

        updateTask = new MikeViewTask(this, deviceSelector, serverIP);
        Thread updaterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                updateTask.execute();
            }
        });
        updaterThread.start();
    }
}