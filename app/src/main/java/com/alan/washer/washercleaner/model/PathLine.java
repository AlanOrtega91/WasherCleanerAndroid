package com.alan.washer.washercleaner.model;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;


public class PathLine {
    public LatLng startLocation;
    public String startAddress;
    public LatLng endLocation;
    public String endAddress;
    public String distance;
    public String duration;

    public PathLine(JSONObject path){
        try {
            double startLongitud = path.getJSONObject("start_location").getDouble("lng");
            double startLatitud = path.getJSONObject("start_location").getDouble("lat");
            double endLongitud = path.getJSONObject("end_location").getDouble("lng");
            double endLatitud = path.getJSONObject("end_location").getDouble("lat");
            this.startLocation = new LatLng(startLatitud,startLongitud);
            this.endLocation = new LatLng(endLatitud,endLongitud);
        } catch (Exception e){

        }
    }
}
