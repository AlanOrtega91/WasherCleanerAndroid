package com.washermx.washercleaner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.washermx.washercleaner.model.AppData;
import com.washermx.washercleaner.model.Database.DataBase;
import com.washermx.washercleaner.model.Service;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity implements View.OnClickListener {

    SharedPreferences settings;
    String idClient;
    List<Service> services = new ArrayList<>();
    ListView historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        initValues();
        initView();
    }

    private void initValues() {
        settings = getSharedPreferences(AppData.FILE,0);
        idClient = settings.getString(AppData.IDCLIENTE, null);
        DataBase db = new DataBase(this);
        services = db.getFinishedServices();
    }

    private void initView() {
        historyList = findViewById(R.id.historyList);
        configureActionBar();
        populateListView();
    }

    private void populateListView() {
        HistoryAdapter adapter = new HistoryAdapter();
        historyList.setAdapter(adapter);
    }

    private void configureActionBar() {
        ActionBar optionsTitleBar = getSupportActionBar();
        if (optionsTitleBar != null) {
            optionsTitleBar.setDisplayShowHomeEnabled(false);
            optionsTitleBar.setDisplayShowCustomEnabled(true);
            optionsTitleBar.setDisplayShowTitleEnabled(false);
            optionsTitleBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            optionsTitleBar.setCustomView(R.layout.titlebar_menu);
            Toolbar parent =(Toolbar) optionsTitleBar.getCustomView().getParent();
            parent.setContentInsetsAbsolute(0,0);
        }
        TextView menuButton = findViewById(R.id.menuButton);
        TextView menuTitle = findViewById(R.id.menuTitle);
        menuTitle.setText(R.string.history);
        menuButton.setText(R.string.menu_button);
        menuButton.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onClick(View v) {
        onBackPressed();
    }

    private class HistoryAdapter extends ArrayAdapter<Service> {
        HistoryAdapter()
        {
            super(HistoryActivity.this,R.layout.history_row,services);
        }

        @Override
        @NonNull public View getView(int position, View convertView,@NonNull ViewGroup parent ) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.history_row, parent, false);
            }
            try {
                Service service = services.get(position);
                TextView date = itemView.findViewById(R.id.serviceDate);
                TextView price = itemView.findViewById(R.id.servicePrice);
                TextView type = itemView.findViewById(R.id.serviceType);
                date.setText(service.startedTime);
                price.setText(getString(R.string.price,service.price));
                type.setText(service.service);
                return itemView;
            } catch (Exception e){
                return itemView;
            }
        }
    }
}
