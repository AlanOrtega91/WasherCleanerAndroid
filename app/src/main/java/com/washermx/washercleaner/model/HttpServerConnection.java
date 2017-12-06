package com.washermx.washercleaner.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;


class HttpServerConnection
{
    private static String VERSION = "1.3.1";
    private static String BASE = "http://54.218.50.2/api/" + VERSION + "/interfaz";

    static String buildURL(String location) {
        return BASE + "/" + location + "/";
    }

    static String sendHttpRequestPost(String urlPath, List<NameValuePair> params) throws connectionException {
        try {
            final RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(10000).setConnectTimeout(10000).setSocketTimeout(20000).build();
            CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
            HttpPost httpPost = new HttpPost(urlPath);
            httpPost.setConfig(requestConfig);
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = entity.getContent();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                httpPost.releaseConnection();
                httpClient.close();
                br.close();
                inputStream.close();
                return sb.toString();
            }
        } catch (Throwable e) {
            throw new connectionException();
        }
        throw new connectionException();
    }

    static class connectionException extends Exception {
    }
}
