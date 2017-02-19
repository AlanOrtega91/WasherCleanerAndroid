package com.washermx.washercleaner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.washermx.washercleaner.model.AppData;
import com.washermx.washercleaner.model.Database.DataBase;
import com.washermx.washercleaner.model.Service;

import java.util.List;

public class SummaryActivity extends AppCompatActivity {

    SharedPreferences settings;
    Service activeService;

    TextView date;
    TextView price;
    String idService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        initValues();
        initView();
    }

    private void initView() {
        date = (TextView) findViewById(R.id.date);
        price = (TextView) findViewById(R.id.price);
        configureView();
    }

    private void initValues() {
        settings = getSharedPreferences(AppData.FILE,0);
        idService = settings.getString(AppData.IDSERVICE,null);
        DataBase db = new DataBase(getBaseContext());
        List<Service> services = db.readServices();
        if (services.size() > 0) {
            for (int i = 0; i < services.size(); i++) {
                if (services.get(i).id.equals(idService))
                    activeService = services.get(i);
            }
        }
    }

    private void configureView() {
        date.setText(activeService.startedTime);
        price.setText(getString(R.string.price,activeService.price));
    }

    @Override
    public void onBackPressed() {
        changeActivity(MapActivity.class);
        finish();
    }

    private void changeActivity(Class activity) {
        Intent intent = new Intent(getBaseContext(), activity);
        startActivity(intent);
    }

    public void onClickContinue(View view) {
        onBackPressed();
    }
}
