package com.alan.washer.washercleaner.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;


public class Product {
    private static String HTTP_LOCATION = "Cleaner/Product/";
    public String id;
    public String cantidad;
    public String name;
    public String descripcion;

    public Product(String id, String cantidad, String name, String descripcion) {
        this.id = id;
        this.cantidad = cantidad;
        this.name = name;
        this.descripcion = descripcion;
    }
    public static List<Product> getProducts(String token) throws errorGettingProducts, noSessionFound {
        String url = HttpServerConnection.buildURL(HTTP_LOCATION + "ReadProducts");
        List<Product> products = new ArrayList<>();
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("token",token));
        try {
            String jsonResponse = HttpServerConnection.sendHttpRequestPost(url,params);
            JSONObject response = new JSONObject(jsonResponse);
            if ((response.getString("Status").compareTo("SESSION ERROR") == 0))
                throw new noSessionFound();
            if (!(response.getString("Status").compareTo("OK") == 0))
                throw new errorGettingProducts();

            JSONArray servicesResponse = response.getJSONArray("Products");
            for (int i=0;i < servicesResponse.length(); i++) {
                JSONObject jsonService = servicesResponse.getJSONObject(i);
                Product product = new Product(jsonService.getString("idProducto"),
                        jsonService.getString("Cantidad"),jsonService.getString("Producto"),jsonService.getString("Descripcion"));
                products.add(product);
            }
            return products;
        } catch (JSONException e) {
            Log.i("ERROR","JSON ERROR");
            throw new errorGettingProducts();
        }  catch (HttpServerConnection.connectionException e) {
            throw new errorGettingProducts();
        }
    }

    public static class errorGettingProducts extends Exception {
    }
    public static class errorRefillingProduct extends Exception {
    }

    public static class noSessionFound extends Throwable {
    }
}
