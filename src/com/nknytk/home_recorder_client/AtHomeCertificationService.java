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

    UDPRetriever UDPR;
    Context ServiceContext = this;
    int UDPPort = 19201;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("INFO", "AtHomeCertificationService Started.");
        Thread replier = new Thread(new Runnable() {
            public  void run(){
                while (true) {
                    String[] receivedMessage;
                    try {
                        if (UDPR == null) UDPR = new UDPRetriever(ServiceContext, UDPPort);
                        receivedMessage = UDPR.receive(); // Receive {from_ipaddr, message_content}
                    } catch (Exception e) {
                        Log.e("ERROR", String.valueOf(e));
                        if (UDPR != null) UDPR.stopListening();
                        UDPR = null;
                        try {
                            Thread.sleep(CommonVariables.UDPRetryIntervalMsec);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        continue;
                    }

                    SharedPreferences pref = getSharedPreferences(CommonVariables.PrefKey, MODE_PRIVATE);
                    Boolean doCertification = pref.getBoolean(CommonVariables.DoCheck, false);
                    String clientToken = pref.getString(CommonVariables.CToken, null);
                    String serverToken = pref.getString(CommonVariables.SToken, null);
                    Integer digestRepetition = pref.getInt(CommonVariables.DigestRepetition, 3000);

                    Log.i("INFO", "UDP message from " + String.valueOf(receivedMessage[0]));
                    Log.i("INFO", "UDP message content " + String.valueOf(receivedMessage[1]));
                    Log.i("INFO", "UDP server token " + serverToken);
                    Log.i("INFO", "UDP client token " + clientToken);

                    if ((doCertification == false) || (clientToken == null) || (serverToken == null)) continue;

                    // Received content should be server_token<SEPARATOR>one_timeToken
                    String[] split_msg = receivedMessage[1].split(CommonVariables.TokenSeparator);
                    if (!split_msg[0].equals(serverToken) || split_msg.length != 2) continue;
                    String responseMsg = clientToken + CommonVariables.TokenSeparator + split_msg[1];
                    Log.i("INFO", "responseMsg: " + responseMsg);
                    byte[] responseDigest = DigestMaker.repetitiveDigest(responseMsg.getBytes(), digestRepetition);
                    Log.i("INFO", "responseDigest: " + String.valueOf(responseDigest));
                    try {
                        UDPR.send(receivedMessage[0], responseDigest, UDPPort);
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
