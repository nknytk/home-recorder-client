package com.nknytk.home_recorder_client;

/**
 * Created by nknytk on 14/08/10.
 */

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.*;

public class UDPRetriever {
    DatagramSocket receiveSocket;
    WifiManager wifiManager;
    WifiManager.MulticastLock multicastLock;
    WifiManager.WifiLock wifiLock;
    int port;

    public UDPRetriever (Context appContext, int port) throws Exception{
        wifiManager = (WifiManager)appContext.getSystemService(android.content.Context.WIFI_SERVICE);
        multicastLock = wifiManager.createMulticastLock("UDPRetriever"); // 任意の文字列
        multicastLock.setReferenceCounted(true);
        wifiLock = wifiManager.createWifiLock("UDPRetriever");
        wifiLock.setReferenceCounted(true);
        this.port = port;
    }

    public String[] receive() {
        String[] UDPMessage = new String[2];
        byte[] receiveBuffer = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

        // Retry loop to automatically recover listening state when Wifi turns from off to on
        while (true) {
            if (wifiManager.getWifiState() != wifiManager.WIFI_STATE_ENABLED) {
                stopListening();
                Log.i("INFO", "Wifi is off. Retry later.");
                try {
                    Thread.sleep(Common.UDPRetryIntervalMsec);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            try {
                if (receiveSocket == null) {
                    startListening();
                    Log.i("INFO", "Wifi turned on. Start Listening.");
                }
            } catch (SocketException e) {
                e.printStackTrace();
                continue;
            }

            try {
                receiveSocket.receive(receivePacket);
                break;
            } catch (SocketTimeoutException te) {
                Log.i("INFO", "UDP Socket Timeout. Retry.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        UDPMessage[0] = receivePacket.getSocketAddress().toString().split(":")[0].split("/")[1];
        UDPMessage[1] = new String(receivePacket.getData(), 0, receivePacket.getLength());
        return UDPMessage;
    }

    public void send(String ipAddrStr, byte[] byteMsg, int port) throws Exception{
        DatagramSocket sendSocket = new DatagramSocket();
        InetAddress sendTo = InetAddress.getByName(ipAddrStr);
        DatagramPacket sendPacket = new DatagramPacket(byteMsg, byteMsg.length, sendTo, port);
        sendSocket.send(sendPacket);
        sendSocket.close();
    }

    public void stopListening() {
        if (receiveSocket != null) receiveSocket.close();
        receiveSocket = null;
        while (multicastLock.isHeld()) multicastLock.release();
        while (wifiLock.isHeld()) wifiLock.release();
    }

    private void startListening() throws  SocketException{
        if(!wifiLock.isHeld()) wifiLock.acquire();
        Log.i("INFO", "Acquired Wifi Lock");
        if(!multicastLock.isHeld()) multicastLock.acquire();
        receiveSocket = new DatagramSocket(port);
        receiveSocket.setSoTimeout(Common.UDPRetryIntervalMsec);
    }
}

