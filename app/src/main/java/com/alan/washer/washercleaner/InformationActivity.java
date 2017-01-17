package com.alan.washer.washercleaner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.alan.washer.washercleaner.model.AppData;
import com.alan.washer.washercleaner.model.Database.DataBase;
import com.alan.washer.washercleaner.model.Service;

public class InformationActivity extends AppCompatActivity implements View.OnClickListener {

    Service activeService;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        initValues();
        initView();
    }


    private void fillData() {
        TextView serviceUserName = (TextView)findViewById(R.id.clientName);
        TextView serviceUserCel = (TextView)findViewById(R.id.clientCel);
        TextView plates = (TextView)findViewById(R.id.plates);
        TextView serviceLabel = (TextView)findViewById(R.id.serviceLabel);
        TextView brand = (TextView)findViewById(R.id.brand);
        TextView address = (TextView)findViewById(R.id.address);
        serviceUserName.setText(activeService.clientName);
        serviceUserCel.setText(activeService.clientCel);
        plates.setText(activeService.plates);
        serviceLabel.setText(activeService.service);
        brand.setText(activeService.brand);
        address.setText(activeService.address);
    }

    private void initValues() {
        settings = getSharedPreferences(AppData.FILE,0);
        DataBase db = new DataBase(getBaseContext());
        activeService = db.getActiveService();
    }

    private void initView() {
        configureActionBar();
        fillData();
    }

    private void configureActionBar() {
        ActionBar optionsTitleBar = getSupportActionBar();
        if (optionsTitleBar != null) {
            optionsTitleBar.hide();
        }
    }

    @Override
    public void onClick(View v) {
        finish();
    }
}
