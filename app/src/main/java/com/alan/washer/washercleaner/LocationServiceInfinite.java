package com.alan.washer.washercleaner;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alan.washer.washercleaner.model.AppData;
import com.alan.washer.washercleaner.model.Database.DataBase;
import com.alan.washer.washercleaner.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class LocationServiceInfinite extends Service {

    private static final int INTERVAL_IN_MILISECONDS = 1000;
    Timer findRequestsNearbyTimer = new Timer();
    Location location;
    LocationManager locationManager;
    SharedPreferences settings;
    List<com.alan.washer.washercleaner.model.Service> services = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        settings = getSharedPreferences(AppData.FILE, 0);
        findRequestsNearbyTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateCleanerLocation();
                if (new DataBase(getBaseContext()).getActiveService() == null)
                    findRequestsNearby();

            }
        }, 0, INTERVAL_IN_MILISECONDS * 5);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateCleanerLocation() {
        try {
            Criteria crit = new Criteria();
            crit.setAccuracy(Criteria.ACCURACY_FINE);
            String provider = locationManager.getBestProvider(crit,true);
            location = locationManager.getLastKnownLocation(provider);
            if (location == null){
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location == null)
                    return;
            }

            User.updateLocation(settings.getString(AppData.TOKEN,null), location.getLatitude(), location.getLongitude());
        } catch (SecurityException e){
            Log.i("LOCATION","Error updating Location security");
        } catch (User.errorUpdatingLocation e) {
            Log.i("LOCATION","Error updating Location");
        } catch (User.noSessionFound e){
            Log.i("LOCATION","Error with Session");
        }
    }

    private void findRequestsNearby() {
        if (new DataBase(getBaseContext()).getActiveService() == null) {
            try {
                int servicesAmount = services.size();
                services = com.alan.washer.washercleaner.model.Service.getServices(location.getLatitude(), location.getLongitude(), settings.getString(AppData.TOKEN,null));
                if (servicesAmount == 0 && services.size() != 0) {
                    if (settings.getBoolean(AppData.IN_BACKGROUND, false))
                        AlarmNotification.notify(getBaseContext(), getString(R.string.services_found), InitActivity.class);
                }
            } catch (com.alan.washer.washercleaner.model.Service.errorGettingServices e) {
                Log.i("SERVICE", "Error getting nearby requests try again later");
            } catch (com.alan.washer.washercleaner.model.Service.noSessionFound e){
                Log.i("LOCATION","Error with Session");
            }
        }
    }
}
