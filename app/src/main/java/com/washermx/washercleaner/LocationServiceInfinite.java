package com.washermx.washercleaner;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.washermx.washercleaner.model.AppData;
import com.washermx.washercleaner.model.Database.DataBase;
import com.washermx.washercleaner.model.User;

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
    List<com.washermx.washercleaner.model.Service> services = new ArrayList<>();
    Boolean skipUpdate = false;

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
                try {
                    updateCleanerLocation(getBestKnownLocation());
                    if (new DataBase(getBaseContext()).getActiveService() == null)
                        findRequestsNearby();
                } catch (errorReadingLocation e){
                    Log.i("LOCATION","Error updating Location security");
                }

            }
        }, 0, INTERVAL_IN_MILISECONDS * 5);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateCleanerLocation(Location location) {
        try {
            com.washermx.washercleaner.model.Service activeService = new DataBase(getBaseContext()).getActiveService();
            if (location != null) {
                if (activeService != null && activeService.status.equals("Started")) {
                    if (skipUpdate) {
                        skipUpdate = false;
                        return;
                    } else {
                        skipUpdate = true;
                    }
                }
                User.updateLocation(settings.getString(AppData.TOKEN, null), location.getLatitude(), location.getLongitude());
                Log.i("Location"," lat= " + location.getLatitude() + " long= " + location.getLongitude());
            }
        } catch (User.errorUpdatingLocation e) {
            Log.i("LOCATION","Error updating Location");
        } catch (User.noSessionFound e){
            Log.i("LOCATION","Error with Session");
        }
    }

    private Location getBestKnownLocation () throws errorReadingLocation {
        try {
            if (( location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null) {
                long locationTime = (SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos()) * 1000 * 1000;
                if (locationTime > 30 && locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
                    return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                } else {
                    throw new errorReadingLocation();
                }
            } else if (( location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) != null){
                long locationTime = (SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos()) * 1000 * 1000;
                if (locationTime > 30 && locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) != null) {
                    return locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                } else {
                    throw new errorReadingLocation();
                }
            } else {
                throw new errorReadingLocation();
            }
        } catch (SecurityException e) {
            throw new errorReadingLocation();
        }
    }

    private void findRequestsNearby() {
        if (new DataBase(getBaseContext()).getActiveService() == null && location != null) {
            try {
                int servicesAmount = services.size();
                services = com.washermx.washercleaner.model.Service.getServices(location.getLatitude(), location.getLongitude(), settings.getString(AppData.TOKEN,null));
                if (servicesAmount == 0 && services.size() != 0) {
                    if (settings.getBoolean(AppData.IN_BACKGROUND, false)) {
                        AlarmNotification.notify(getBaseContext(), getString(R.string.services_found), InitActivity.class);
                    }
                }
            } catch (com.washermx.washercleaner.model.Service.errorGettingServices e) {
                Log.i("SERVICE", "Error getting nearby requests try again later");
            } catch (com.washermx.washercleaner.model.Service.noSessionFound e){
                Log.i("LOCATION","Error with Session");
            }
        }
    }
    static class errorReadingLocation extends Throwable {
    }
}
