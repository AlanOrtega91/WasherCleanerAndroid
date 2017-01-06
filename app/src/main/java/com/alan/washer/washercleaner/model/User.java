package com.alan.washer.washercleaner.model;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * User class for connecting with API
 * Created by Alan on 24/05/2016.
 */
public class User {
    private static String HTTP_LOCATION = "Cleaner/";
    public String name;
    public String lastName;
    public String email;
    public String id;
    public String token;
    public String phone;
    public String imagePath;
    public double rating;


    public void sendLogout() throws errorWithLogOut {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "LogOut");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("email", email));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if (!(response.getString("Status").compareTo("OK") == 0))
                throw new errorWithLogOut();

        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorWithLogOut();
        } catch (HttpServerConnection.connectionException e) {
            throw new errorWithLogOut();
        }
    }

    public static void updateLocation(String token, double latitud, double longitud) throws errorUpdatingLocation, noSessionFound {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "UpdateLocation");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("token",token));
        params.add(new BasicNameValuePair("latitud", String.valueOf(latitud)));
        params.add(new BasicNameValuePair("longitud", String.valueOf(longitud)));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if ((response.getString("Status").compareTo("SESSION ERROR") == 0))
                throw new noSessionFound();
            if (!(response.getString("Status").compareTo("OK") == 0))
                throw new errorUpdatingLocation();

        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorUpdatingLocation();
        } catch (HttpServerConnection.connectionException e) {
            throw new errorUpdatingLocation();
        }
    }

    public void saveFirebaseToken(String token, String pushNotificationToken) throws errorSavingFireBaseToken, noSessionFound {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "SavePushNotificationToken");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("token",token));
        params.add(new BasicNameValuePair("pushNotificationToken",pushNotificationToken));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if ((response.getString("Status").compareTo("SESSION ERROR") == 0))
                throw new noSessionFound();
            if (!(response.getString("Status").compareTo("OK") == 0))
                throw new errorSavingFireBaseToken();

        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorSavingFireBaseToken();
        } catch (HttpServerConnection.connectionException e) {
            throw new errorSavingFireBaseToken();
        }
    }

    static Bitmap getEncodedStringImageForUser(String id) {
        try {
            URL url = new URL("http://washer.mx/Vashen/images/cleaners/" + id + "/profile_image.jpg");
            InputStream is = url.openStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            Bitmap bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
            if (bm == null) {
                return null;
            } else {
                return bm;
            }
        } catch (Exception e) {
            return null;
        }
    }

    static String saveEncodedImageToFileAndGetPath(Bitmap imageBitmap, Context context) {
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath = new File(directory,"profile.png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mypath.getAbsolutePath();
    }

    public static Bitmap readImageBitmapFromFile(String path) {
        try {
            File f = new File(path);
            return BitmapFactory.decodeStream(new FileInputStream(f));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class errorUpdatingLocation extends Exception {
    }

    public static class noSessionFound extends Exception {
    }

    public static class errorWithLogOut extends Exception {
    }

    public static class errorSavingFireBaseToken extends Throwable {
    }
}
