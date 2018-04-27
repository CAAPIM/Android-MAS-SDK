/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.core.policy;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ca.mas.core.conf.ConfigurationProvider;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.error.MAGException;
import com.ca.mas.core.policy.exceptions.LocationInvalidException;
import com.ca.mas.core.policy.exceptions.LocationRequiredException;
import com.ca.mas.foundation.MASResponse;

import java.net.HttpURLConnection;
import java.util.Timer;
import java.util.TimerTask;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * A policy that adds location information to outbound requests.
 */
class LocationAssertion implements MssoAssertion {

    static final String DEFAULT_PROVIDER = LocationManager.NETWORK_PROVIDER;
    static final long DEFAULT_MIN_TIME = 300000L;
    static final float DEFAULT_MIN_DISTANCE = 100.0f;
    private volatile Location lastLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Context context;
    private MssoContext mssoContext;

    @Override
    public void init(@NonNull MssoContext mssoContext, @NonNull Context sysContext) {
        this.mssoContext = mssoContext;
        this.context = sysContext;
        setupLocation();
    }

    private void setupLocation() {
        ConfigurationProvider conf = mssoContext.getConfigurationProvider();
        if (conf == null)
            throw new NullPointerException("mssoContext.configurationProvider");
        Boolean enabled = conf.getProperty(ConfigurationProvider.PROP_LOCATION_ENABLED);
        if (enabled == null || !enabled) {
            close();
            return;
        }

        String locationProvider = conf.getProperty(ConfigurationProvider.PROP_LOCATION_PROVIDER_NAME);
        if (locationProvider == null)
            locationProvider = DEFAULT_PROVIDER;

        Long minTime = conf.getProperty(ConfigurationProvider.PROP_LOCATION_MIN_TIME);
        if (minTime == null)
            minTime = DEFAULT_MIN_TIME;

        Float minDistance = conf.getProperty(ConfigurationProvider.PROP_LOCATION_MIN_DISTANCE);
        if (minDistance == null)
            minDistance = DEFAULT_MIN_DISTANCE;

        try {
            initLocation(locationProvider, minTime, minDistance);
        } catch (Exception e) {
            if (DEBUG) Log.i(TAG, "No permission to access location: " + e.getMessage());
        }
    }

    @SuppressWarnings("MissingPermission")
    private void initLocation(String locationProvider, final long minTime, final float minDistance) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        lastLocation = getLastKnownLocation(locationProvider);
        if (locationManager != null) {

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    lastLocation = location;
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(final String provider) {

                    lastLocation = getLastKnownLocation(provider);
                    //The OS may delay the location update, and the last knox location may return null.
                    //Delay 5 sec to retrieve the location again.
                    if (lastLocation == null) {
                        final Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            private int count = 0;

                            @Override
                            public void run() {
                                lastLocation = getLastKnownLocation(provider);
                                count++;
                                if (lastLocation != null || count >= 5) {
                                    timer.cancel();
                                }
                            }
                        }, 3000, 1000);
                    }
                }

                @Override
                public void onProviderDisabled(String provider) {
                    lastLocation = null;
                }
            };

            locationManager.requestLocationUpdates(locationProvider, minTime, minDistance, locationListener);
        }
    }

    @SuppressWarnings("MissingPermission")
    private Location getLastKnownLocation(String locationProvider) {
        if (locationManager != null) {
            try {
                return locationManager.getLastKnownLocation(locationProvider);
            } catch (SecurityException e) {
                if (DEBUG) Log.d(TAG, "No permission to access location: " + e.getMessage());
            }
        }
        return null;
    }

    @Override
    public void processResponse(MssoContext mssoContext, RequestInfo request, MASResponse response) throws MAGException {
        int statusCode = response.getResponseCode();
        if (statusCode == 449 || statusCode ==448) {
            String responseContent = new String(response.getBody().getRawContent());
            if (responseContent.toLowerCase().contains("location")) {
                if (statusCode == 449) {
                    throw new LocationRequiredException("This application requires your location information. Please enable location services to continue.");
                } else {
                    //448
                    throw new LocationInvalidException("This location is unauthorized.");
                }
            }
        }
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void close() {
        if (locationListener != null && locationManager != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException e) {
                if (DEBUG) Log.i(TAG, "Unable to access location " + e.getMessage());
            }
            locationManager = null;
            locationListener = null;
            lastLocation = null;
        }
    }

    private Location getLastLocation() {
        if (lastLocation != null) {
            return lastLocation;
        } else {
            setupLocation();
            return lastLocation;
        }
    }

    @Override
    public void processRequest(MssoContext mssoContext, RequestInfo request) {
        if (getLastLocation() != null) {
            String loc = String.format("%f,%f", getLastLocation().getLatitude(), getLastLocation().getLongitude());
            request.getRequest().addHeader("geo-location", loc);
        }
    }
}
