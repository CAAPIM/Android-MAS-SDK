package com.ca.mas.core.http;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class HttpConnectionQueue {

    public static HttpConnectionQueue _instance;
    private  boolean isCancled;

    private Map<Long, HttpURLConnection> urlConnectionMap = new HashMap<>();

    private HttpConnectionQueue(){}

    public static HttpConnectionQueue getInstance(){
        if(_instance == null){
            _instance = new HttpConnectionQueue();
        }

        return  _instance;
    }

    public Map<Long, HttpURLConnection> getUrlConnections() {
        return urlConnectionMap;
    }

    public void setUrlConnections(Map<Long, HttpURLConnection> urlConnectionMap) {
        this.urlConnectionMap = urlConnectionMap;
    }

    public void addUrlConnection(Long requestId, HttpURLConnection urlConnection){
        urlConnectionMap.put(requestId, urlConnection);
    }

    public boolean isCancled() {
        return isCancled;
    }

    public void setCancled(boolean cancled) {
        isCancled = cancled;
    }
}
