/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.conf;

import android.net.Uri;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.cert.TrustedCertificateConfiguration;

import org.json.JSONObject;

import java.net.URI;

/**
 * Interface implemented by a provider of MSSO configuration information.
 */
public interface ConfigurationProvider extends TrustedCertificateConfiguration, MobileSsoConfig {

    JSONObject getRaw();

    Server getServer();

    /**
     * @return the token server hostname, eg "oath.example.com".  Never null.
     */
    String getTokenHost();

    /**
     * @return the token server port, eg 8443.  Never null.
     */
    int getTokenPort();


    /**
     * @return the client ID, eg "846955e8-a8fb-4bea-bdd7-a16b39770c3d".  Never null.
     */
    String getClientId();

    /**
     * @return the client secret, eg "6ed4ffcb-4110-4c68-b280-cda17f127374".
     */
    String getClientSecret();

    /**
     * @return the client scope.
     */
    String getClientScope();

    /**
     * Get an arbitrary configuration property.
     *
     * @param propertyName the name of the property to get.  Required.
     * @param <T> the expected return type of the property value.
     * @return the property value, or null if it is not recognized or not provided.
     */
    <T> T getProperty(String propertyName);

    /**
     * Get the complete URI for the given operation.
     *
     * @param operation the operation name, eg {@link #PROP_TOKEN_URL_SUFFIX_REQUEST_TOKEN}.
     * @return the URI, or null if no URI is available for the specified operation name.
     */
    URI getTokenUri(String operation);

    /**
     * Get the complete URI for the given operation.
     *
     * @return the URI, or null if no URI is available for the userinfo.
     */
    Uri getUserInfoUri();


    /**
     * Get the absolute URI for the given path.
     *
     * @param relativePath the path to the resource.
     * @return the URI, or null if no suffix is provided.
     */
    URI getUri(String relativePath);


    /**
     * Based on the provided configuration the the SDK, retrieve the prefix attribute.
     *
     * @returnA the prefix configured for SDK
     */
    String getPrefix();



    // Configuration properties that are not currently documented as part of the public API.

    /**
     * Long, milliseconds.  Minimum time between location updates, if location enabled.  Default is 120000 milliseconds (two minutes).
     */
    String PROP_LOCATION_MIN_TIME = "msso.location.min.time";

    /**
     * Float, meters.  Minimum distance between location updates, if location enabled.  Default is 100 meters.
     */
    String PROP_LOCATION_MIN_DISTANCE = "msso.location.min.distance";


}
