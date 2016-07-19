/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation.web;

import com.ca.mas.foundation.MASResultReceiver;

import org.json.JSONObject;

/**
 * <p><b>MASWebServiceClient</b> is a CRUD interface on the SCIM web service calls defined by get, put, post, and delete.
 * Usage of this interface follows the pattern of;
 * <pre>
 *     // create a concrete object that implements this interface, here.
 *     WebServiceRequest request = new WebServiceRequest(uri, map);
 *     try {
 *          concreteImpl.get(request, new MASResultReceiver<JSONObject>() {
 *              @Override
 *               public void onError(MAGError error) {
 *                   // handle the error
 *               }
 *
 *              @Override
 *              public void onSuccess(final MAGResponse<JSONObject> response) {
 *                  // process the response.
 *              }
 *         });
 *     } catch(MASException me) {
 *         // handle the issue.
 *     } finally {
 *         concreteImpl.removeMASListener(ConcreteImpl.this);
 *     }
 * </pre>
 */
public interface MASWebServiceClient {

    /**
     * <b>Pre-Conditions:</b> A network connection must exist and the MAG SDK must be initialized.<br>
     * <b>Description:</b> 'post' introduces the object to the data store and is semantically equivalent
     * to INSERT in SQL and CREATE in CRUD.
     *
     * @param request the request object that wraps the request properties
     * @param result the callback object that wraps the MAGResultReceiver.
     */
    /* public */ void post(WebServiceRequest request, MASResultReceiver<JSONObject> result);

    /**
     * <b>Pre-Conditions:</b> A network connection must exist and the MAG SDK must be initialized.<br>
     * <b>Description:</b> 'read' retrieves the object from the data store and is semantically equivalent
     * to SELECT in SQL and READ in CRUD.
     *
     * @param request the request object that wraps the request properties
     * @param result the callback object that wraps the MAGResultReceiver.
     */
    /* public */ void get(WebServiceRequest request, MASResultReceiver<JSONObject> result);

    /**
     * <b>Pre-Conditions:</b> A network connection must exist and the MAG SDK must be initialized.<br>
     * <b>Description:</b> 'put' alters the object in the data store and is semantically equivalent to
     * PUT in SQL and UPDATE in CRUD.
     *
     * @param request the request object that wraps the request properties
     * @param result the callback object that wraps the MAGResultReceiver.
     */
    /* public */ void put(WebServiceRequest request, MASResultReceiver<JSONObject> result);

    /**
     * <b>Pre-Conditions:</b> A network connection must exist and the MAG SDK must be initialized.<br>
     * <b>Description:</b> 'deleteFromEncryptedLocalStorage' removes the object from the data store and is semantically equivalent
     * to DELETE in SQL and CRUD.
     *
     * @param request the request object that wraps the request properties
     * @param result the callback object that wraps the MAGResultReceiver.
     */
    /* public */ void delete(WebServiceRequest request, MASResultReceiver<JSONObject> result);

}
