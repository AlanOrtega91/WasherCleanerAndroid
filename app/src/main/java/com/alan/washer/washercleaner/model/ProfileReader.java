package com.alan.washer.washercleaner.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.alan.washer.washercleaner.model.Database.DataBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class ProfileReader {

    private static String HTTP_LOCATION = "Cleaner/";
    private User user = new User();
    private List<Service> services = new ArrayList<>();

    public static void run(Context context, String email, String password) throws errorReadingProfile {
        try {
            SharedPreferences settings = context.getSharedPreferences(AppData.FILE, 0);
            ProfileReader profile = new ProfileReader();
            profile.logIn(email,password,context);
            DataBase db = new DataBase(context);
            db.saveUser(profile.user);
            AppData.saveData(settings,profile.user);
            db.saveServices(profile.services);
        } catch (errorReadingData e){
            Log.i("READING","Error reading in profile");
            throw new errorReadingProfile();
        }
    }

    private void logIn(String email, String password, Context context) throws errorReadingData {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "LogIn");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("email",email));
        params.add(new BasicNameValuePair("password",password));
        params.add(new BasicNameValuePair("device","android"));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if (!(response.getString("Status").compareTo("OK") == 0))
                throw new errorReadingData();

            double rating;
            if (response.isNull("Calificacion"))
                rating = 0;
            else
                rating = response.getDouble("Calificacion");
            readUser(response.getJSONObject("User Info"),rating, context);
            readHistory(response.getJSONArray("History"));
        } catch (Throwable e) {
            throw new errorReadingData();
        }
    }

    public static void run(Context context) throws errorReadingProfile {
        try {
            SharedPreferences settings = context.getSharedPreferences(AppData.FILE, 0);
            String token = settings.getString(AppData.TOKEN, null);
            ProfileReader profile = new ProfileReader();
            profile.initialRead(token,context);
            DataBase db = new DataBase(context);
            db.saveUser(profile.user);
            db.saveServices(profile.services);
        } catch (errorReadingData e){
            Log.i("READING","Error reading in profile");
            throw new errorReadingProfile();
        }
    }

    private void initialRead(String token, Context context) throws errorReadingData {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "InitialRead");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("token",token));
        params.add(new BasicNameValuePair("device","android"));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if (!(response.getString("Status").compareTo("OK") == 0))
                throw new errorReadingData();
            double rating;
            if (response.isNull("Calificacion"))
                rating = 0;
            else
                rating = response.getDouble("Calificacion");
            readUser(response.getJSONObject("User Info"),rating, context);
            readHistory(response.getJSONArray("History"));
        } catch (Throwable e) {
            throw new errorReadingData();
        }
    }

    private void readUser(JSONObject parameters, double rating, Context context){
        try {
            user.name = parameters.getString("Nombre");
            user.lastName = parameters.getString("PrimerApellido");
            user.email = parameters.getString("Email");
            user.id = parameters.getString("idLavador");
            user.token = parameters.getString("Token");
            user.phone = parameters.getString("Telefono");
            user.imagePath = User.saveEncodedImageToFileAndGetPath(User.getEncodedStringImageForUser(user.id), context);
            user.rating = rating;
        } catch (JSONException e){
            Log.i("READ","Error reading user");
        }
    }

    private void readHistory(JSONArray servicesResponse){
        try{
            for (int i=0;i < servicesResponse.length(); i++) {
                Service service = new Service();
                JSONObject jsonService = servicesResponse.getJSONObject(i);
                service.id = jsonService.getString("id");
                service.car = jsonService.getString("coche");
                service.status = jsonService.getString("status");
                service.service = jsonService.getString("servicio");
                service.price = jsonService.getString("precio");
                service.description = jsonService.getString("descripcion");
                service.latitud = jsonService.getDouble("latitud");
                service.longitud = jsonService.getDouble("longitud");
                service.clientName = jsonService.getString("nombreCliente");
                service.clientCel = jsonService.getString("telCliente");
                service.estimatedTime = jsonService.getString("tiempoEstimado");
                service.startedTime = jsonService.getString("fechaEmpezado");
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                service.estimatedTime = jsonService.getString("tiempoEstimado");
                if (!jsonService.isNull("horaFinalEstimada"))
                    service.finalTime = format.parse(jsonService.getString("horaFinalEstimada"));

                service.plates = jsonService.getString("Placas");
                service.brand = jsonService.getString("Marca");
                service.color = jsonService.getString("Color");
                service.type = jsonService.getString("Tipo");
                services.add(service);
            }
        }catch (Exception e){
            Log.i("READ","Error reading services history");
        }
    }

    public static void delete(Context context) {
        SharedPreferences settings = context.getSharedPreferences(AppData.FILE, 0);
        AppData.eliminateData(settings);
        DataBase db = new DataBase(context);
        db.deleteTableUser();
        db.deleteTableService();
    }

    public static class errorReadingProfile extends Exception {
    }

    private class errorReadingData extends Exception {
    }
}
