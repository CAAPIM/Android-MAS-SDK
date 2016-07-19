/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.ca.mas.core.app.App;
import com.ca.mas.core.ent.InvalidResponseException;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.http.MAGResponseBody;
import com.ca.mas.core.service.MssoIntents;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * The top level interface for Enterprise Browser.
 */
@Deprecated
public class EnterpriseApp {

    private static String TAG = EnterpriseApp.class.getCanonicalName();

    private static EnterpriseApp enterpriseApp = new EnterpriseApp();

    private EnterpriseApp() {
    }

    /**
     * Retrieves an instance of the EnterpriseApp.
     * Make sure the MobileSso has been initialized by {@link MobileSsoFactory#getInstance()}
     * before retrieving the EnterpriseApp instance, otherwise throw {@link IllegalStateException}
     *
     * @return An instance of EnterpriseApp.
     */
    public static EnterpriseApp getInstance() {
        return enterpriseApp;
    }

    /**
     * <p>Submit a request to the Gateway to retrieve the Enterprise Apps list asynchronously.</p>
     * <p>For any error response to the request will eventually be delivered to the specified result receiver.</p>
     * <p>This method returns immediately to the calling thread.</p>
     * <p>On successful retrieval of the Enterprise Apps, an intent {@link com.ca.mas.core.service.MssoIntents#ACTION_LAUNCH_ENTERPRISE_BROWSER}
     * will be sent with the App list. Application can retrieve the App List with Extra {@link com.ca.mas.core.service.MssoIntents#EXTRA_APPS}.</p>
     *
     * @param context        The application Context
     * @param resultReceiver Receiver to receive any error for the enterprise browser endpoint. Find the result code under
     *                       {@link com.ca.mas.core.service.MssoIntents} RESULT_CODE_*,
     *                       To retrieve the error message from the returned Bundle with key
     *                       {@link com.ca.mas.core.service.MssoIntents#RESULT_ERROR_MESSAGE}
     * @param appClass       Class to override the default App implementation.
     */
    public void processEnterpriseApp(final Context context, final ResultReceiver resultReceiver, final Class<? extends App> appClass) {
        URI enterpriseAppLink = MobileSsoFactory.getInstance(context).getConfigurationProvider().getTokenUri(MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_ENTERPRISE_APPS);

        MAGRequest request = null;
        try {
            request = new MAGRequest.MAGRequestBuilder(enterpriseAppLink.toURL())
                    .responseBody(MAGResponseBody.jsonBody())
                    .build();
        } catch (MalformedURLException e) {
            Bundle result = new Bundle();
            Log.e(TAG, e.getMessage(), e);
            result.putSerializable(MssoIntents.RESULT_ERROR, new InvalidResponseException(e));
            result.putString(MssoIntents.RESULT_ERROR_MESSAGE, e.getMessage());
            resultReceiver.send(MssoIntents.RESULT_CODE_ERR_ENTERPRISE_BROWSER_INVALID_JSON, result);
            return;
        }

        MobileSsoFactory.getInstance(context).processRequest(request, new MAGResultReceiver<JSONObject>() {

            @Override
            public void onSuccess(MAGResponse<JSONObject> response) {
                List<App> apps = null;
                try {
                    apps = parse(response.getBody().getContent(), appClass);
                } catch (Exception e) {
                    String errorMessage = "Process Enterprise Browser endpoint error.";
                    MAGError error = new MAGError(new InvalidResponseException(errorMessage, e));
                    error.setResultCode(MssoIntents.RESULT_CODE_ERR_ENTERPRISE_BROWSER_INVALID_JSON);
                    onError(error);
                    return;
                }
                Intent intent = new Intent(MssoIntents.ACTION_LAUNCH_ENTERPRISE_BROWSER);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setPackage(context.getPackageName());
                intent.putExtra(MssoIntents.EXTRA_APPS, (java.io.Serializable) apps);
                context.startActivity(intent);
            }

            @Override
            public void onError(MAGError error) {
                Log.e(TAG, error.getMessage(), error.getCause());
                Bundle result = new Bundle();
                result.putSerializable(MssoIntents.RESULT_ERROR, error);
                result.putString(MssoIntents.RESULT_ERROR_MESSAGE, error.getMessage());
                resultReceiver.send(error.getResultCode(), result);
            }

            @Override
            public void onRequestCancelled() {

            }
        });
    }

    /**
     * <p>Submit a request to the Gateway to retrieve the Enterprise Apps list asynchronously.</p>
     * <p>For any error response to the request will eventually be delivered to the specified result receiver.</p>
     * <p>This method returns immediately to the calling thread.</p>
     * <p>On successful retrieval of the Enterprise Apps, an intent {@link com.ca.mas.core.service.MssoIntents#ACTION_LAUNCH_ENTERPRISE_BROWSER}
     * will be sent with the App list. Application can retrieve the App List with Extra {@link com.ca.mas.core.service.MssoIntents#EXTRA_APPS}.</p>
     *
     * @param context        The application Context
     * @param resultReceiver Receiver to receive any error for the enterprise browser endpoint. Find the result code under
     *                       {@link com.ca.mas.core.service.MssoIntents} RESULT_CODE_*,
     *                       To retrieve the error message from the returned Bundle with key
     *                       {@link com.ca.mas.core.service.MssoIntents#RESULT_ERROR_MESSAGE}
     */
    public void processEnterpriseApp(final Context context, final ResultReceiver resultReceiver) {
        processEnterpriseApp(context, resultReceiver, null);
    }

    private List<App> parse(JSONObject enterprise, Class<? extends App> appClass) throws Exception {
        Class<? extends App> app = appClass;
        if (app == null) {
            app = App.class;
        }
        List<App> result = new ArrayList<App>();
        JSONArray apps = enterprise.getJSONArray("enterprise-apps");
        for (int i = 0; i < apps.length(); i++) {
            result.add(app.getConstructor(JSONObject.class).newInstance(((JSONObject) apps.get(i)).getJSONObject("app")));
        }
        return result;
    }

}
