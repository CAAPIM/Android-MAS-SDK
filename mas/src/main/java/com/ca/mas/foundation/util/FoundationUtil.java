/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation.util;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import com.ca.mas.connecta.client.MASConnectOptions;
import com.ca.mas.connecta.util.ConnectaConsts;
import com.ca.mas.core.MobileSso;
import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.conf.ConfigurationManager;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>The <b>FoundationUtil</b></p> class provides common request header and URL formatting that is used in web services.
 * This utility class contains general helper methods and acts as the base util class for all other util classes contained
 * in this SDK.
 */
public class FoundationUtil {

    /**
     * <b>Pre-Conditions:</b> The MAG SDK must be initialized.<br>
     * <b>Description:</b> Retrieve the UserInfo endpoint for initializing the Web Services handshake.
     *
     * @return URI - Of the form [host]/openid/connect/v1/userinfo.
     */
    public static Uri getUserInfo() {
        String userInfo = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider()
                .getProperty(FoundationConsts.KEY_CONFIG_USER_INFO);
        if (userInfo == null) {
            userInfo = "/openid/connect/v1/userinfo";
        }
        return new Uri.Builder().path(userInfo).build();
    }

    /**
     * <b>Pre-Conditions:</b> The MAG SDK must be initialized.<br>
     * <b>Description:</b> An accessor for returning the MobileSso instance.
     *
     * @return MobileSso
     */
    public static MobileSso getMobileSso() {
        return MobileSsoFactory.getInstance();
    }

    /**
     * <b>Pre-Conditions:</b> The MAG SDK must be initialized.<br>
     * <b>Description:</b> Convenience accessor for getting the current host defined in the msso_config.json file.
     *
     * @return String - the host name.
     */
    public static String getHost() {
        return ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getTokenHost();
    }

    /**
     * <b>Pre-Conditions:</b> The MAG SDK must be initialized.<br>
     * <b>Description:</b> Convenience accessor for retrieving the port as defined in the msso_config file.
     *
     * @return int - the port number.
     */
    public static int getPort() {
        return ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getTokenPort();
    }

    /**
     * <b>Pre-Conditions:</b> The MAG SDK must be initialized.<br>
     * <b>Description:</b> This method creates a fully qualified host and port representation of the web service base.
     *
     * @return - the FQDN as 'https://<host>:<port></port>
     */
    public static String getFqdn() {
        String scheme = FoundationConsts.HTTPS_SCIM_SCHEME;
        String host = FoundationUtil.getHost();
        int port = getPort();
        return scheme + FoundationConsts.COLON + FoundationConsts.FSLASH + FoundationConsts.FSLASH + host + FoundationConsts.COLON + port;
    }

    public static String getBrokerUrl( Context context ) throws IllegalStateException {
        return getBrokerUrl(context, null);
    }

    /**
     * <b>Pre-Conditions</b> The MAG SDK has to be initialized prior to calling this method.<br>
     * <b>Description</b> This method takes the information found in the ConfigurationProvider and
     * uses it to create a URL representing an ssl connection to the MQTT broker.
     *
     * @param context
     * @return String of the form 'ssl://host.com:8883'
     * @throws IllegalStateException
     */
    public static String getBrokerUrl(Context context, MASConnectOptions connectOptions) throws IllegalStateException {
        if (connectOptions != null && connectOptions.getServerURIs() != null && connectOptions.getServerURIs().length > 0) {
            // If MASConnectOptions have been set and server URIs have been set
            return connectOptions.getServerURIs()[0];
        } else {
            return (ConnectaConsts.SSL_MESSAGING_SCHEME + FoundationConsts.COLON + FoundationConsts.FSLASH + FoundationConsts.FSLASH) + getHost() + FoundationConsts.COLON + ConnectaConsts.SSL_MESSAGING_PORT;
        }
    }

}
