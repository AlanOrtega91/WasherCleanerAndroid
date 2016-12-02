package com.alan.washer.washercleaner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    public static Boolean onScreen = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar optionsTitleBar = getSupportActionBar();
        if (optionsTitleBar != null) optionsTitleBar.hide();
    }

    @Override
    protected void onResume() {
        super.onResume();
        onScreen = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        onScreen = false;
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    public void changeToLogIn(View view) {
        changeActivity(LoginActivity.class);
    }

    private void changeActivity(Class activity) {
        Intent intent = new Intent(getBaseContext(), activity);
        startActivity(intent);
    }
}
