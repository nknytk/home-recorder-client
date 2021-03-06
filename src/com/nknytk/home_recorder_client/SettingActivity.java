package com.nknytk.home_recorder_client;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.lang.reflect.Array;

/**
 * Created by nknytk on 14/08/14.
 */
public class SettingActivity extends Activity {
    String name;
    EditText stokenView;
    EditText ctokenView;
    EditText digestRepetitionView;
    CheckBox forceEnableCheck;
    LinearLayout featureContainerLayout;
    Context context = this;
    SharedPreferences preferences;
    AvailableFeatureTask featureUpdater;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_acitivity);
        preferences = getSharedPreferences(Common.PrefKey, MODE_PRIVATE);
        name = getIntent().getStringExtra("name");
        setTitle(name);

        // Set current settings if exist
        stokenView = (EditText)findViewById(R.id.stoken);
        stokenView.setText(preferences.getString(Common.join(name, Common.SToken), ""));
        ctokenView = (EditText)findViewById(R.id.ctoken);
        ctokenView.setText(preferences.getString(Common.join(name, Common.CToken), ""));
        digestRepetitionView = (EditText)findViewById(R.id.drepetition);
        digestRepetitionView.setText(String.valueOf(
                preferences.getInt(Common.join(name, Common.DigestRepetition), 300)));
        forceEnableCheck = (CheckBox)findViewById(R.id.forceenable);
        forceEnableCheck.setChecked(preferences.getBoolean(Common.join(name, Common.ForceCheck), false));

        // set button actions
        Button okButton = (Button)findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (applyChange()) {
                    featureUpdater.cancel(true);
                    finish();
                }
            }
        });
        Button applyButton = (Button)findViewById(R.id.apply_button);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyChange();
            }
        });
        Button cancelButton = (Button)findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                featureUpdater.cancel(true);
                finish();
            }
        });

        featureContainerLayout = (LinearLayout)findViewById(R.id.features);
    }

    private boolean applyChange() {
        String stoken = stokenView.getText().toString().replace(Common.Separator, "");
        String ctoken = ctokenView.getText().toString().replace(Common.Separator, "");
        String repetition_str = digestRepetitionView.getText().toString().replace(Common.Separator, "");
        Boolean checked = forceEnableCheck.isChecked();

        // empty value is not allowed
        if ((name.equals("")) || (stoken.equals("")) || (ctoken.equals("")) || (repetition_str.equals(""))) {
            alert("Empty value is not allowed");
            return false;
        }

        // make home network name list.
        String settingNames = preferences.getString(Common.SettingNames, null);
        StringBuffer sb = new StringBuffer();
        boolean shouldAppendSeparator = false;

        if (settingNames != null) {
            for (String sname: settingNames.split(Common.Separator)) {
                if (sname.equals(name)) continue;
                if (shouldAppendSeparator) sb.append(Common.Separator);
                sb.append(sname);
                shouldAppendSeparator = true;
            }
        }

        if (shouldAppendSeparator) sb.append(Common.Separator);
        sb.append(name);
        String newSettingNames = sb.toString();
        Log.i("INFO", newSettingNames);

        // commit setting change
        SharedPreferences.Editor prefEditor = preferences.edit();
        prefEditor.putString(Common.SettingNames, newSettingNames);
        prefEditor.putString(Common.join(name, Common.SToken), stoken);
        prefEditor.putString(Common.join(name, Common.CToken), ctoken);
        prefEditor.putInt(Common.join(name, Common.DigestRepetition), Integer.valueOf(repetition_str));
        prefEditor.putBoolean(Common.join(name, Common.ForceCheck), checked);
        prefEditor.commit();
        setTitle(name);

        featureUpdater.networkName = name;
        return true;
    }

    private void startFeatureUpdating() {
        if (featureUpdater != null && featureUpdater.toContinue) {
            featureUpdater.networkName = name;
            return;
        }

        featureUpdater = new AvailableFeatureTask(context, featureContainerLayout);
        Thread featureUpdaterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                featureUpdater.execute("");
            }
        });
        featureUpdaterThread.start();
        featureUpdater.networkName = name;
    }

    private void alert(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Alert");
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.create().show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        featureUpdater.cancel(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startFeatureUpdating();
    }
}