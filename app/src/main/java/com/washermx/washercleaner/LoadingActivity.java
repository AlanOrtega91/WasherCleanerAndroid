package com.washermx.washercleaner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.washermx.washercleaner.model.AppData;
import com.washermx.washercleaner.model.Database.DataBase;
import com.washermx.washercleaner.model.ProfileReader;
import com.washermx.washercleaner.model.User;

public class LoadingActivity extends AppCompatActivity {

    String email;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        ActionBar optionsTitleBar = getSupportActionBar();
        if (optionsTitleBar != null) optionsTitleBar.hide();
        Intent intent = getIntent();
        email = intent.getStringExtra(LoginActivity.EMAIL);
        password = intent.getStringExtra(LoginActivity.PASSWORD);
        Thread sendReadProfileThread = new Thread(new Runnable() {
            @Override
            public void run() {
                readProfile();
            }
        });
        sendReadProfileThread.start();
    }

    private void readProfile() {
        try {
            SharedPreferences settings = getSharedPreferences(AppData.FILE, 0);
            ProfileReader.run(getBaseContext(),email,password);
            User user = new DataBase(getBaseContext()).readUser();
            String token = settings.getString(AppData.TOKEN,null);
            String fireBaseToken = settings.getString(AppData.FB_TOKEN, "");
            user.saveFirebaseToken(token, fireBaseToken);
            changeActivity(MapActivity.class);
        } catch (ProfileReader.errorReadingProfile e) {
            postAlert(getResources().getString(R.string.error_logging_in));
            ProfileReader.delete(getBaseContext());
            finish();
        } catch (User.errorSavingFireBaseToken e) {
            Log.i("ERROR","FIREBASE" + e.getMessage());
            postAlert(getString(R.string.error_logging_in));
            ProfileReader.delete(getBaseContext());
            finish();
        } catch (User.noSessionFound e){
            if (!MainActivity.onScreen) postAlert(getString(R.string.error_sesion));
            ProfileReader.delete(getBaseContext());
            finish();
        }
    }

    private void postAlert(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeActivity(Class activity) {
        Intent intent = new Intent(getBaseContext(), activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
