package com.nknytk.home_recorder_client;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by nknytk on 14/09/07.
 */
public class CameraViewTask extends AsyncTask<String, Bitmap, Bitmap> {
    Context context;
    ImageView view;
    SharedPreferences preferences;
    Integer interval = 300;
    boolean toContinue = true;
    String networkName = "";
    String[] availableCameras = null;

    protected CameraViewTask(Context context, ImageView pictureContainer) {
        this.context = context;
        view = pictureContainer;
        preferences = context.getSharedPreferences(Common.PrefKey, context.MODE_PRIVATE);
    }

    @Override
    protected Bitmap doInBackground(String... values) {
        Bitmap image = null;
        while (toContinue) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Log.e("ERROR", e.toString());
                toContinue = false;
            }

            String serverIP = preferences.getString(Common.join(networkName, Common.CurrentServerIP), null);
            if (serverIP == null) {
                publishProgress(null);
                availableCameras = null;
                continue;
            }
            Log.i("INFO", String.valueOf(serverIP));

            try {
                if (availableCameras == null) availableCameras = getCameraList(serverIP);
                if (availableCameras != null) {
                    String devicename = availableCameras[0];
                    String selectedDevice = (String)view.getTag();
                    for (String dname: availableCameras) {
                        if (dname.equals(selectedDevice)) {
                            devicename = selectedDevice;
                            break;
                        }
                    }
                    image = getImage(serverIP, devicename);
                }
            } catch (Error e) {
                Log.e("ERROR", e.toString());
                publishProgress(null);
                continue;
            }
            publishProgress(image);
        }
        return image;
    }

    @Override
    protected void onProgressUpdate(Bitmap... images) {
        if (images == null) {
            view.setImageResource(0);
        } else {

            view.setImageBitmap(images[0]);
        }
    }

    private Bitmap getImage(String ipaddr, String devicename) {
        String url = Common.getURL(ipaddr, "/camera/image", "device=" + devicename);
        HttpEntity result = getUrlContent(url);
        try {
            ByteArrayOutputStream outs = new ByteArrayOutputStream();
            result.writeTo(outs);
            ByteArrayInputStream ins = new ByteArrayInputStream(outs.toByteArray());
            Bitmap bmp = BitmapFactory.decodeStream(ins);
            return bmp;
        } catch (Exception e) {
            Log.e("ERROR", "ERROR", e);
            return null;
        }
    }

    private String[] getCameraList(String ipaddr) {
        String url = Common.getURL(ipaddr, "/camera/devicelist", null);
        HttpEntity result = getUrlContent(url);
        try {
            if (result == null) return null;
            String cameras = EntityUtils.toString(result, "UTF-8");
            if (cameras == null || cameras.equals("")) return null;
            return cameras.split(Common.Separator);
        } catch (IOException e) {
            return null;
        }

    }

    private HttpEntity getUrlContent(String url) {
        try {
            HttpGet req = new HttpGet(url);
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpResponse res = httpClient.execute(req);
            if (res.getStatusLine().toString().indexOf("200 OK") != -1) return res.getEntity();
            Log.e("WARN", res.getStatusLine().toString());
            return null;
        } catch (Exception e) {
            Log.e("ERROR", e.toString());
            Log.e("ERROR", url);
            return null;
        }
    }

    @Override
    protected void onCancelled(){
        toContinue = false;
        Log.i("INFO", String.valueOf(toContinue));
    }
}
