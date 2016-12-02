package com.alan.washer.washercleaner.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;


public class Service
{
    private static String HTTP_LOCATION = "Service/";
    public String status;
    public String car;
    public String service;
    public String price;
    public String description;
    public String startedTime;
    public Date finalTime;
    public Double latitud;
    public Double longitud;
    public String id;
    public String address;
    public String clientName;
    public String clientCel;
    public String estimatedTime;
    public String plates;
    public String brand;
    public String color;
    public String type;

    public static final int STARTED = 4;
    public static final int FINISHED = 5;



    public static long getDifferenceTimeInMillis(Date finalTime) {
        return finalTime.getTime() - new Date().getTime();
    }

    public static void changeServiceStatus(String idServicio, String statusId, String token) throws errorChangingStatusRequest, noSessionFound {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "ChangeServiceStatus");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("serviceId",idServicio));
        params.add(new BasicNameValuePair("statusId",statusId));
        params.add(new BasicNameValuePair("token",token));
        params.add(new BasicNameValuePair("cancelCode","0"));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if ((response.getString("Status").compareTo("SESSION ERROR") == 0))
                throw new noSessionFound();
            if (!(response.getString("Status").compareTo("OK") == 0))
                throw new errorChangingStatusRequest();

        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorChangingStatusRequest();
        } catch (HttpServerConnection.connectionException e) {
            throw new errorChangingStatusRequest();
        }
    }

    public static Service acceptService(String idService, String token) throws errorServiceTaken, noSessionFound {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "AcceptService");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("serviceId",idService));
        params.add(new BasicNameValuePair("token",token));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if ((response.getString("Status").compareTo("SESSION ERROR") == 0))
                throw new noSessionFound();
            if (!(response.getString("Status").compareTo("OK") == 0))
                throw new errorServiceTaken();

            JSONObject jsonService = response.getJSONObject("service info");
            Service service = new Service();
            service.id = jsonService.getString("id");
            service.car = jsonService.getString("coche");
            service.service = jsonService.getString("servicio");
            service.price = jsonService.getString("precio");
            service.description = jsonService.getString("descripcion");
            service.latitud = jsonService.getDouble("latitud");
            service.longitud = jsonService.getDouble("longitud");
            service.clientName = jsonService.getString("nombreCliente");
            service.clientCel = jsonService.getString("telCliente");
            service.status = "Accepted";
            service.estimatedTime = jsonService.getString("tiempoEstimado");
            service.plates = jsonService.getString("Placas");
            service.brand = jsonService.getString("Marca");
            service.color = jsonService.getString("Color");
            service.type = jsonService.getString("Tipo");
            return service;
        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorServiceTaken();
        } catch (HttpServerConnection.connectionException e) {
            throw new errorServiceTaken();
        }
    }

    public static List<Service> getServices(double latitud, double longitud, String token) throws errorGettingServices, noSessionFound {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "GetNearbyServices");
        List<Service> services = new ArrayList<>();
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("latitud", String.valueOf(latitud)));
        params.add(new BasicNameValuePair("longitud", String.valueOf(longitud)));
        params.add(new BasicNameValuePair("token",token));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if ((response.getString("Status").compareTo("SESSION ERROR") == 0))
                throw new noSessionFound();
            if (!(response.getString("Status").compareTo("OK") == 0))
                throw new errorGettingServices();

            JSONArray servicesResponse = response.getJSONArray("services");
            for (int i=0;i < servicesResponse.length(); i++) {
                JSONObject jsonService = servicesResponse.getJSONObject(i);
                Service service = new Service();
                service.id = jsonService.getString("idServicioPedido");
                service.address = jsonService.getString("Direccion");
                service.latitud = jsonService.getDouble("Latitud");
                service.longitud = jsonService.getDouble("Longitud");
                services.add(service);
            }
            return services;
        } catch (HttpServerConnection.connectionException e) {
            return services;
        } catch (JSONException e) {
            throw new errorGettingServices();
        }
    }

    public static void cancelService(String idServicio, String token) throws errorCancelingRequest, noSessionFound {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "ChangeServiceStatus");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("serviceId",idServicio));
        params.add(new BasicNameValuePair("statusId","6"));
        params.add(new BasicNameValuePair("token",token));
        params.add(new BasicNameValuePair("cancelCode", String.valueOf(2)));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if ((response.getString("Status").compareTo("SESSION ERROR") == 0))
                throw new noSessionFound();
            if (!(response.getString("Status").compareTo("OK") == 0))
                throw new errorCancelingRequest();

        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorCancelingRequest();
        } catch (HttpServerConnection.connectionException e){
            throw new errorCancelingRequest();
        }
    }

    public static class noSessionFound extends Exception {
    }
    public static class errorCancelingRequest extends Exception {
    }
    public static class errorGettingServices extends Exception {
    }

    public static class errorServiceTaken extends Exception {
    }

    public static class errorChangingStatusRequest extends Exception {
    }
}
