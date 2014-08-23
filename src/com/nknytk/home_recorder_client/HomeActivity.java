package com.nknytk.home_recorder_client;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.util.List;

public class HomeActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    Context MainActivity = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        confirmAtHomeCertificationServiceRunning();
        setContentView(R.layout.main);
        final SharedPreferences pref = getSharedPreferences(CommonVariables.PrefKey, MODE_PRIVATE);

        Boolean doCertification = pref.getBoolean(CommonVariables.DoCheck, false);
        CheckBox doCertificationCheck = (CheckBox)findViewById(R.id.do_certification);
        if (doCertification) doCertificationCheck.setChecked(true);
        System.out.println(doCertificationCheck);
        doCertificationCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                SharedPreferences.Editor prefEditor = pref.edit();
                if (cb.isChecked()) {
                    prefEditor.putBoolean(CommonVariables.DoCheck, true);
                } else {
                    prefEditor.putBoolean(CommonVariables.DoCheck, false);
                }
                prefEditor.commit();
            }
        });

        Button conf_button = (Button)findViewById(R.id.conf_button);
        conf_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity, SettingActivity.class);
                startActivity(intent);
            }
        });
    }

    private void confirmAtHomeCertificationServiceRunning() {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
        if(services!=null){
            for(ActivityManager.RunningServiceInfo info : services){
                if(info.service.getClassName().endsWith("AtHomeCertificationService")){
                    isRunning = true;
                    break;
                }
            }
        }

        if (!isRunning) {
            Intent i = new Intent(this, AtHomeCertificationService.class);
            startService(i);
        }
    }
}
