package com.nknytk.home_recorder_client;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.util.List;

public class HomeActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    Context MainActivity = this;
    SharedPreferences preferences;
    int MaxLinks = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(Common.PrefKey, MODE_PRIVATE);
        confirmAtHomeCertificationServiceRunning();
        setContentView(R.layout.main);
    }

    @Override
    protected void onResume(){
        super.onResume();
        resetView();
    }

    private void showRemoveDialog(String settingName) {
        final String sname = settingName;
        if(settingName.equals(Common.AddNewSetting)) return;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Remove Setting");
        alertDialogBuilder.setMessage("Do you want to remove '" + sname + "'?");
        alertDialogBuilder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                purgeSetting(sname);
                resetView();
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        alertDialogBuilder.create().show();
    }

    private void resetView() {
        String settingNames = preferences.getString(Common.SettingNames, null);

        if (settingNames == null) {
            settingNames = Common.AddNewSetting;
        } else if (settingNames.split(Common.Separator).length < MaxLinks) {
            StringBuffer sb = new StringBuffer(settingNames);
            sb.append(Common.Separator);
            sb.append(Common.AddNewSetting);
            settingNames = sb.toString();
        }
        String[] settingNameArray = settingNames.split(Common.Separator);

        ListView linkList = (ListView)findViewById(R.id.linklist);
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, settingNameArray);
        linkList.setAdapter(arrayAdapter);

        linkList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemname = (String) parent.getItemAtPosition(position);
                startSettingView(itemname);
            }
        });

        linkList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showRemoveDialog((String) parent.getItemAtPosition(position));
                return false;
            }
        });
    }

    protected void purgeSetting(String name) {
        SharedPreferences.Editor prefEditor = preferences.edit();

        String settingNames = preferences.getString(Common.SettingNames, null);
        if (settingNames.equals(name)) {
            prefEditor.remove(Common.SettingNames);
        } else {
            StringBuffer sb = new StringBuffer();
            boolean shouldAppendSeparator = false;
            for (String sname : settingNames.split(Common.Separator)) {
                if (sname.equals(name)) continue;
                if (shouldAppendSeparator) sb.append(Common.Separator);
                sb.append(sname);
                shouldAppendSeparator = true;
            }
            prefEditor.putString(Common.SettingNames, sb.toString());
        }

        prefEditor.remove(Common.join(name, Common.CurrentServerIP));
        prefEditor.remove(Common.join(name, Common.SToken));
        prefEditor.remove(Common.join(name, Common.CToken));
        prefEditor.remove(Common.join(name, Common.DigestRepetition));
        prefEditor.remove(Common.join(name, Common.ForceCheck));
        prefEditor.commit();

        if (getApplicationContext() == this) finish();
    }

    private void startSettingView (String itemname) {
        // when existing item is selected, directly move to setting view
        if (!itemname.equals(Common.AddNewSetting)) {
            Intent intent = new Intent(MainActivity, SettingActivity.class);
            intent.putExtra("name", itemname);
            startActivity(intent);
            return;
        }

        // when "Add new item" is selected, show a dialog to input new setting name
        final EditText settingNameInputView = new EditText(this);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("New home network setting");
        alertDialogBuilder.setMessage("Input home network name");
        alertDialogBuilder.setView(settingNameInputView);
        alertDialogBuilder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = settingNameInputView.getText().toString();
                if (newName.equals("") || newName.equals(Common.AddNewSetting)) {
                    Toast.makeText(MainActivity, "This name is not acceptable", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(MainActivity, SettingActivity.class);
                    intent.putExtra("name", newName);
                    startActivity(intent);
                }
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        alertDialogBuilder.create().show();
    }

    // start AtHomeCertificationService if it is not running
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