/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.error.TargetApiException;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.http.MAGResponseBody;
import com.ca.mas.core.service.MssoClient;
import com.ca.mas.core.service.MssoIntents;

import java.net.HttpURLConnection;
import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

/**
 * Interface for receiving a callback result from {@link MobileSso#processRequest(com.ca.mas.core.http.MAGRequest, ResultReceiver)}.
 * Use this by creating a subclass and implement {@link #onSuccess(MAGResponse<T>)}, {@link #onError(MAGError)},
 * {@link #onRequestCancelled(Bundle)} )}
 *
 * @param <T> To provide the data type of the expected response object. The SDK converts the
 *           response stream data to the provided data type. By define this generic type, it only provides
 *          tighter type checks at compile time, developer has to define the expected response body in
 *           {@link com.ca.mas.core.http.MAGRequest.MAGRequestBuilder#responseBody(MAGResponseBody)}
 */

public abstract class MAGResultReceiver<T> extends ResultReceiver {


    public MAGResultReceiver(Handler handler) {
        super(handler);
    }

    public MAGResultReceiver() {
        super(null);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        try {
            switch (resultCode) {
                case MssoIntents.RESULT_CODE_SUCCESS:
                    long requestId = resultData.getLong(MssoIntents.RESULT_REQUEST_ID);
                    if (requestId == -1 || requestId == 0) {
                        onError(new MAGError(new IllegalStateException("Received result included an invalid request ID")));
                    } else {
                        MAGResponse<T> response = MssoClient.takeMAGResponse(requestId);
                        if (response != null) {
                            int responseCode = response.getResponseCode();
                            if (responseCode < HttpURLConnection.HTTP_OK || responseCode >= HttpURLConnection.HTTP_MULT_CHOICE) {
                                onError(new MAGError(response.getResponseMessage(), new TargetApiException(response)));
                                return;
                            }
                            onSuccess(response);
                        }
                    }
                    break;
                case MssoIntents.RESULT_CODE_ERR_CANCELED:
                    onRequestCancelled(resultData);
                    break;
                default:
                    MAGError error = (MAGError) resultData.getSerializable(MssoIntents.RESULT_ERROR);
                    if (error != null) {
                        if (DEBUG) Log.d(TAG,
                                "Error response with: " + error.getMessage(), error);
                        error.setResultCode(resultCode);
                        onError(error);
                    }
            }
        } catch (Throwable e) {
            if (DEBUG) Log.e(TAG, "Error handling response.", e);
        }

    }

    /**
     * Callback when successfully invoked the target API and the target API return http status code
     * within the range 200 - 299.
     *
     * @param response The HttpResponse of the endpoint.
     */
    public abstract void onSuccess(MAGResponse<T> response);

    /**
     * Callback when failed to invoke the MAG API and Target API.
     */
    public abstract void onError(MAGError error);

    /**
     * Callback when the request is cancelled.
     */
    public abstract void onRequestCancelled(Bundle data);

}
