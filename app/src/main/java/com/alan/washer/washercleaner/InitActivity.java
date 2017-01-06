package com.alan.washer.washercleaner;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.alan.washer.washercleaner.model.AppData;
import com.alan.washer.washercleaner.model.Database.DataBase;
import com.alan.washer.washercleaner.model.ProfileReader;
import com.alan.washer.washercleaner.model.User;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

public class InitActivity extends AppCompatActivity {

    SharedPreferences settings;
    private Handler handler = new Handler(Looper.getMainLooper());
    String token;

    private static final int ACCESS_FINE_LOCATION = 1;
    private static final int INTERNET = 2;
    private static final int VIBRATE = 3;
    private boolean allPermissionsOk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        FirebaseMessaging.getInstance().subscribeToTopic("test");
        FirebaseInstanceId.getInstance().getToken();
        reviewPermissions();
        initValues();
        initView();
    }

    private void initValues() {
        settings = getSharedPreferences(AppData.FILE,0);
        token = settings.getString(AppData.TOKEN,null);
    }

    private void reviewPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET},INTERNET);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.VIBRATE}, VIBRATE);
        } else {
            allPermissionsOk = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (!allPermissionsOk)
                reviewPermissions();
        } else {
            createAlert();
        }
    }

    private void createAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Permission denied")
                .setMessage(getResources().getString(R.string.app_name) + " Can't work without this permission")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void initView() {
        ActionBar optionsTitleBar = getSupportActionBar();
        if (optionsTitleBar != null) optionsTitleBar.hide();
        Thread sendDecideNextView = new Thread(new Runnable() {
            @Override
            public void run() {
                decideNextView();
            }
        });
        sendDecideNextView.start();
    }

    private void decideNextView() {
        while (!allPermissionsOk);
        if (token == null)
            changeActivity(MainActivity.class);
        else {
            tryReadUser(token);
        }
    }

    private void tryReadUser(final String token) {
        Thread tryReadUserThread = new Thread(new Runnable() {
            @Override
            public void run() {
                User user;
                try {
                    ProfileReader.run(getBaseContext());
                    DataBase db = new DataBase(getBaseContext());
                    user = db.readUser();
                    user.token = token;
                    AppData.saveData(settings,user);
                    String fireBaseToken = settings.getString(AppData.FB_TOKEN,"");
                    user.saveFirebaseToken(token, fireBaseToken);
                    changeActivity(MapActivity.class);
                } catch (ProfileReader.errorReadingProfile e){
                    Log.i("PROFILE","Session not found");
                    ProfileReader.delete(getBaseContext());
                    changeActivity(MainActivity.class);
                } catch (User.errorSavingFireBaseToken e) {
                    e.printStackTrace();
                    ProfileReader.delete(getBaseContext());
                    changeActivity(MainActivity.class);
                } catch (User.noSessionFound e){
                    if (!MainActivity.onScreen)  postAlert(getString(R.string.session_error));
                    changeActivity(MainActivity.class);
                }
            }
        });
        tryReadUserThread.start();
    }

    private void postAlert(final String message)
    {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeActivity(Class activity) {
        Intent intent = new Intent(getBaseContext(),activity);
        startActivity(intent);
        finish();
    }
}
