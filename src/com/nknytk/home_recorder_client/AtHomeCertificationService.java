package com.nknytk.home_recorder_client;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by nknytk on 14/08/10.
 */
public class AtHomeCertificationService extends Service {

    SharedPreferences preferences;
    UDPRetriever UDPR;
    Context ServiceContext = this;
    int UDPPort = 19201;
    long lastResponse = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        preferences = getSharedPreferences(Common.PrefKey, MODE_PRIVATE);
        Log.i("INFO", "AtHomeCertificationService Started.");
        Thread replier = new Thread(new Runnable() {
            public  void run(){
                while (true) {
                    if (lastResponse < System.currentTimeMillis() - Common.UDPRetryIntervalMsec) {
                        if (preferences.getString(Common.CurrentServerIP, null) != null) {
                            SharedPreferences.Editor prefEditor = preferences.edit();
                            prefEditor.remove(Common.CurrentServerIP);
                            prefEditor.commit();
                        }
                    }

                    String[] receivedMessage;
                    try {
                        if (UDPR == null) UDPR = new UDPRetriever(ServiceContext, UDPPort);
                        receivedMessage = UDPR.receive(); // Receive {from_ipaddr, message_content}
                    } catch (Exception e) {
                        Log.e("ERROR", String.valueOf(e));
                        if (UDPR != null) UDPR.stopListening();
                        UDPR = null;
                        try {
                            Thread.sleep(Common.UDPRetryIntervalMsec);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        continue;
                    }

                    String settingNames = preferences.getString(Common.SettingNames, null);
                    if (settingNames == null) continue;

                    // Received content should be server_token<SEPARATOR>one_timeToken
                    String[] split_msg = receivedMessage[1].split(Common.Separator);
                    if(split_msg.length != 2) continue;

                    String networkName = null;
                    for (String sname: settingNames.split(Common.Separator)) {
                        String serverToken = preferences.getString(Common.join(sname, Common.SToken), "");
                        if (serverToken.equals(split_msg[0])){
                            networkName = sname;
                            break;
                        }
                    }

                    if (preferences.getBoolean(Common.join(networkName,Common.ForceCheck), true)) continue;
                    String clientToken = preferences.getString(Common.join(networkName, Common.CToken), "");
                    Integer digestRepetition = preferences.getInt(Common.join(networkName, Common.DigestRepetition), 300);

                    String responseMsg = Common.join(clientToken, split_msg[1]);
                    byte[] responseDigest = DigestMaker.repetitiveDigest(responseMsg.getBytes(), digestRepetition);
                    try {
                        UDPR.send(receivedMessage[0], responseDigest, UDPPort);
                        SharedPreferences.Editor prefEditor = preferences.edit();
                        prefEditor.putString(Common.join(networkName, Common.CurrentServerIP), receivedMessage[0]);
                        prefEditor.commit();
                        lastResponse = System.currentTimeMillis();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        replier.start();
        startForeground(1, new Notification());
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){
        UDPR.stopListening();
        Log.i("INFO", "AtHomeCertificationService Stopped.");
    }

}
