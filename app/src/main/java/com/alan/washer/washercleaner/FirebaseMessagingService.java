package com.alan.washer.washercleaner;


import android.content.SharedPreferences;
import android.util.Log;

import com.alan.washer.washercleaner.model.AppData;
import com.alan.washer.washercleaner.model.Database.DataBase;
import com.alan.washer.washercleaner.model.Service;
import com.alan.washer.washercleaner.model.User;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.List;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    SharedPreferences settings;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        settings = getSharedPreferences(AppData.FILE, 0);
        Boolean inBackground = settings.getBoolean(AppData.IN_BACKGROUND,false);
        if (remoteMessage.getData().get("state") == null)
            return;
        String state = remoteMessage.getData().get("state");
        switch (state){
            case "-1":
                String rating = remoteMessage.getData().get("rating");
                User user = new DataBase(getBaseContext()).readUser();
                user.rating = Double.valueOf(rating);
                new DataBase(getBaseContext()).saveUser(user);
                break;
            case "6":
                if (!inBackground)
                    sendPopUp(getString(R.string.notify_canceled));
                String serviceJson = remoteMessage.getData().get("serviceInfo");
                if (serviceJson == null)
                    return;
                deleteService(serviceJson);
                break;
        }
    }

    private void deleteService(String serviceJson){
        try {
            DataBase db = new DataBase(getBaseContext());
            List<Service> services = db.readServices();
            int i;
            JSONObject jsonService = new JSONObject(serviceJson);
            for (i = 0; i < services.size() ; i++)
            {
                if (services.get(i).id.equals(jsonService.getString("id")))
                    break;
            }
            services.remove(i);
            db.saveServices(services);
            AppData.notifyNewData(settings,true);
        } catch (Exception e){
            Log.i("ERROR","FireBase data");
        }
    }

    private void sendPopUp(final String message) {
        AppData.saveMessage(settings,message);
    }
}
