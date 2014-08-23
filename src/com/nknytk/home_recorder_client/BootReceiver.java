package com.nknytk.home_recorder_client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by nknytk on 14/08/23.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, AtHomeCertificationService.class);
        context.startService(i);
    }
}
