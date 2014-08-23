package com.nknytk.home_recorder_client;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

/**
 * Created by nknytk on 14/08/14.
 */
public class SettingActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_acitivity);
        final SharedPreferences pref = getSharedPreferences(CommonVariables.PrefKey, MODE_PRIVATE);

        final EditText stokenString = (EditText)findViewById(R.id.stoken);
        final EditText ctokenString = (EditText)findViewById(R.id.ctoken);
        stokenString.setText(pref.getString(CommonVariables.SToken, ""));
        ctokenString.setText(pref.getString(CommonVariables.CToken, ""));

        final NumberPicker digestRepetition = (NumberPicker)findViewById(R.id.drepetition);
        final int minRepetition = 1000;
        int maxRepetition = 10000;
        String[] repetitionChoice = new String[maxRepetition/minRepetition];
        for (int i = minRepetition; i <= maxRepetition; i += minRepetition) {
            repetitionChoice[i/minRepetition-1] = String.valueOf(i);
        }
        digestRepetition.setMinValue(1);
        digestRepetition.setMaxValue(maxRepetition/minRepetition);
        digestRepetition.setDisplayedValues(repetitionChoice);
        digestRepetition.setValue(pref.getInt(CommonVariables.DigestRepetition, 3000)/minRepetition);

        Button okButton = (Button)findViewById(R.id.ok_button);
        Button cancelButton = (Button)findViewById(R.id.cancel_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor prefEditor = pref.edit();
                prefEditor.putString(CommonVariables.SToken, stokenString.getText().toString());
                prefEditor.putString(CommonVariables.CToken, ctokenString.getText().toString());
                prefEditor.putInt(CommonVariables.DigestRepetition, digestRepetition.getValue() * minRepetition);
                prefEditor.commit();
                finish();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}