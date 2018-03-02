/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.util.Functions;

import java.net.URI;

/**
 * Constant to define Grant Types
 */
public enum MASGrantProvider {

    PASSWORD(
            new Functions.Unary<MASAuthCredentials, MssoContext>() {
                @Override
                public MASAuthCredentials call(MssoContext context) {
                    return context.getCredentials();
                }
            },
            new Functions.Unary<URI, MssoContext>() {
                @Override
                public URI call(MssoContext context) {
                    return context.getConfigurationProvider().getTokenUri(MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_REGISTER_DEVICE);
                }
            },
            new Functions.Nullary<Boolean>() {
                @Override
                public Boolean call() {
                    return true;
                }
            }),

    CLIENT_CREDENTIALS(
            new Functions.Unary<MASAuthCredentials, MssoContext>() {
                @Override
                public MASAuthCredentials call(MssoContext context) {
                    return new MASAuthCredentialsClientCredentials();
                }
            },
            new Functions.Unary<URI, MssoContext>() {
                @Override
                public URI call(MssoContext context) {
                    return context.getConfigurationProvider().getTokenUri(MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_REGISTER_DEVICE_CLIENT);
                }
            },
            new Functions.Nullary<Boolean>() {
                @Override
                public Boolean call() {
                    return false;
                }
            });

    private final Functions.Unary<MASAuthCredentials, MssoContext> getCredentials;
    private final Functions.Unary<URI, MssoContext> getRegistrationPath;
    private final Functions.Nullary<Boolean> isSessionSupported;

    /**
     * @param getCredentials      Return {@link MASAuthCredentials} subclass which the caller can use to authorize an MAG Request.
     *                            Each implementation of MASAuthCredentials can chose its own strategy for loading the credentials.
     *                            For example, an implementation might load the credentials from username/password form or
     *                            load clientId/ClientSecret.
     * @param getRegistrationPath Return MAG registration endpoint to perform the registration process.
     * @param isSessionSupported  Whether the grant type support session or not.
     */
    MASGrantProvider(
            Functions.Unary<MASAuthCredentials, MssoContext> getCredentials,
            Functions.Unary<URI, MssoContext> getRegistrationPath,
            Functions.Nullary<Boolean> isSessionSupported) {
        this.getCredentials = getCredentials;
        this.getRegistrationPath = getRegistrationPath;
        this.isSessionSupported = isSessionSupported;
    }

    public MASAuthCredentials getCredentials(MssoContext context) {
        return getCredentials.call(context);
    }

    public URI getRegistrationPath(MssoContext context) {
        return getRegistrationPath.call(context);

    }

    public boolean isSessionSupported() {
        return isSessionSupported.call();
    }

}
