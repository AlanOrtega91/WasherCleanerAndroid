package com.washermx.washercleaner;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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


public class LocationServiceInfinite extends Service implements LocationListener {

    private static final int INTERVAL_IN_MILISECONDS = 1000;
    Timer findRequestsNearbyTimer = new Timer();
    Location location;
    LocationManager locationManager;
    SharedPreferences settings;
    List<com.washermx.washercleaner.model.Service> services = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        configuraUbicacion();
        settings = getSharedPreferences(AppData.FILE, 0);
        findRequestsNearbyTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateCleanerLocation();
                if (new DataBase(getBaseContext()).getActiveService() == null)
                {
                    findRequestsNearby();
                }
            }
        }, 0, INTERVAL_IN_MILISECONDS/10);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void configuraUbicacion() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,INTERVAL_IN_MILISECONDS/10,1,this);
            }
        } catch (SecurityException ignored) {

        }
    }
    private void updateCleanerLocation() {
        try {
            if (location != null) {
                User.updateLocation(settings.getString(AppData.TOKEN, null), location.getLatitude(), location.getLongitude());
            }
        } catch (User.errorUpdatingLocation e) {
            Log.i("LOCATION","Error updating Location");
        } catch (User.noSessionFound e){
            Log.i("LOCATION","Error with Session");
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

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        long tiempo = SystemClock.elapsedRealtimeNanos() - this.location.getElapsedRealtimeNanos();
        Log.i("Ubicacion","Lat=" + this.location.getLatitude() + "   Lon=" + this.location.getLongitude() + "   Tiempo=" + tiempo/1000/1000/1000/60/60 + "h "+ tiempo/1000/1000/1000/60 + "m "+ tiempo/1000/1000/1000 + "s " + tiempo/1000/1000 + "M " + tiempo/1000 + "u ");
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

}
