/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation.util;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import com.ca.mas.connecta.util.ConnectaConsts;
import com.ca.mas.core.MobileSso;
import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.conf.ConfigurationProvider;
import com.ca.mas.foundation.MAS;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>The <b>FoundationUtil</b></p> class provides common request header and URL formatting that is used in web services.
 * This utility class contains general helper methods and acts as the base util class for all other util classes contained
 * in this SDK.
 */
public class FoundationUtil {

    private static String deviceId;

    /**
     * <b>Pre-Conditions:</b> The MAG SDK must be initialized.<br>
     * <b>Description:</b> Retrieve the UserInfo endpoint for initializing the Web Services handshake.
     *
     * @return URI - Of the form [host]/openid/connect/v1/userinfo.
     */
    public static URI getUserInfo() {
        MobileSso mobileSso = FoundationUtil.getMobileSso();
        String userInfo = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider()
                .getProperty(FoundationConsts.KEY_CONFIG_USER_INFO);
        if (userInfo == null) {
            userInfo = "/openid/connect/v1/userinfo";
        }
        return mobileSso.getURI(userInfo);
    }

    /**
     * <b>Pre-Conditions:</b> The MAG SDK must be initialized.<br>
     * <b>Description:</b> Getter that returns the set of default headers. These will
     * be overridden with a subsequent call to add a header with the same key, such as
     * in a SCIM request.
     *
     * @return Map<String, String> the set of headers.
     */
    public static Map<String, String> getStandardHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(FoundationConsts.HEADER_KEY_ACCEPT, FoundationConsts.MT_APP_JSON);
        headers.put(FoundationConsts.HEADER_KEY_CONTENT_TYPE, FoundationConsts.MT_APP_JSON);
        headers.put(FoundationConsts.HEADER_KEY_USER_AGENT, getUserAgentId());
        return headers;
    }

    /**
     * <b>Pre-Conditions:</b> The MAG SDK must be initialized.<br>
     * <b>Description:</b> Create a user agent string of the form;
     * Mozilla/5.0 (Linux; <Android Version>; <Build Tag etc.>) AppleWebKit/<WebKit Rev>
     * (KHTML, like Gecko) Chrome/<Chrome Rev> Mobile Safari/<WebKit Rev>
     *
     * @return String representing the user agent header.
     */
    private static String getUserAgentId() {
        return FoundationConsts.UA_MOZILLA + FoundationConsts.SPACE + Build.VERSION.RELEASE + FoundationConsts.SEMI_COLON + Build.VERSION.CODENAME + FoundationConsts.CLOSE_PAREN + FoundationConsts.SPACE + FoundationConsts.UA_KHTML + FoundationConsts.SPACE + FoundationConsts.UA_APP_MOBILE_APP_SERVICES;
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

    /**
     * <b>Pre-Conditions</b> The MAG SDK has to be initialized prior to calling this method.<br>
     * <b>Description</b> This method takes the information found in the ConfigurationProvider and
     * uses it to create a URL representing an ssl connection to the MQTT broker.
     *
     * @param context
     * @return String of the form 'ssl://host.com:8883'
     * @throws IllegalStateException
     */
    public static String getBrokerUrl(Context context) throws IllegalStateException {
        if (ConnectaConsts.DEBUG_MQTT) {
            return (ConnectaConsts.TCP_MESSAGING_SCHEME + FoundationConsts.COLON + FoundationConsts.FSLASH + FoundationConsts.FSLASH) + getHost() + FoundationConsts.COLON + ConnectaConsts.TCP_MESSAGING_PORT;

        } else {
            return (ConnectaConsts.SSL_MESSAGING_SCHEME + FoundationConsts.COLON + FoundationConsts.FSLASH + FoundationConsts.FSLASH) + getHost() + FoundationConsts.COLON + ConnectaConsts.SSL_MESSAGING_PORT;
        }
    }


    /**
     * <b>Pre-Conditions:</b> The MAG SDK must be initialized.<br>
     * <b>Description:</b> Convenience method for setting the deviceId.
     *
     * @param deviceId
     */
    public static void setDeviceId(String deviceId) {
        FoundationUtil.deviceId = deviceId;
    }

    /**
     * <b>Pre-Conditions:</b> The MAG SDK must be initialized.<br>
     * <b>Description:</b> Convenience method for retrieving the deviceId that was previously set.
     *
     * @return - the deviceId. Can be null.
     */
    public static String getDeviceId() {
        return FoundationUtil.deviceId;
    }

}
