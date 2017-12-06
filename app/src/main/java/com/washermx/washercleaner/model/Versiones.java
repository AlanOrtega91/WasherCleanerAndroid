package com.washermx.washercleaner.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;


public class Versiones {

    public static void leerVersion() throws actualizacionRequerida{
        String VERSION = "1.3.1";
        String url = "http://54.218.50.2/api/version/";
        List<NameValuePair> params = new ArrayList<>();
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if (response.getString("version").compareTo(VERSION) != 0)
            {
                if (response.getString("actualizacion").compareTo("si") == 0) {
                    throw new actualizacionRequerida();
                }
            }

        } catch (HttpServerConnection.connectionException | JSONException e) {
            Log.i("ERROR",e.getLocalizedMessage());
        }
    }

    public static class actualizacionRequerida extends Exception{}
}
