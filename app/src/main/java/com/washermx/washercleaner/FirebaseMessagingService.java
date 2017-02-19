package com.washermx.washercleaner;


import android.content.SharedPreferences;
import android.util.Log;

import com.washermx.washercleaner.model.AppData;
import com.washermx.washercleaner.model.Database.DataBase;
import com.washermx.washercleaner.model.Service;
import com.washermx.washercleaner.model.User;
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
        String message;
        switch (state){
            case "-1":
                message = getString(R.string.rated);
                String rating = remoteMessage.getData().get("rating");
                User user = new DataBase(getBaseContext()).readUser();
                user.rating = Double.valueOf(rating);
                new DataBase(getBaseContext()).saveUser(user);
                if (inBackground) {
                    AlarmNotification.notify(getBaseContext(), message, InitActivity.class);
                }
                break;
            case "6":
                message = getString(R.string.notify_canceled);
                if (inBackground) {
                    AlarmNotification.notify(getBaseContext(), message, InitActivity.class);
                }
                else {
                    sendPopUp(message);
                }
                String serviceJson = remoteMessage.getData().get("serviceInfo");
                if (serviceJson != null) {
                    deleteService(serviceJson);
                }
                break;
        }
    }

    private void deleteService(String serviceJson){
        try {
            DataBase db = new DataBase(getBaseContext());
            List<Service> services = db.readServices();
            int i;
            JSONObject jsonService = new JSONObject(serviceJson);
            for (i = 0; i < services.size() ; i++) {
                if (services.get(i).id.equals(jsonService.getString("id"))) {
                    break;
                }
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
