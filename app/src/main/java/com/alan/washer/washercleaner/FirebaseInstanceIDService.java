package com.alan.washer.washercleaner;

import android.content.SharedPreferences;

import com.alan.washer.washercleaner.model.AppData;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FirebaseInstanceIDService extends FirebaseInstanceIdService{
    SharedPreferences settings;
    String fireBaseToken;

    @Override
    public void onTokenRefresh() {
        fireBaseToken = FirebaseInstanceId.getInstance().getToken();
        settings = getSharedPreferences(AppData.FILE, 0);
        AppData.saveFBToken(settings,fireBaseToken);
    }
}
