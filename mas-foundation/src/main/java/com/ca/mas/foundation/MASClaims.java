/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 */
package com.ca.mas.foundation;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * An interface to get JWT claim values.
 */
public interface MASClaims {

    /**
     * Get the issuer for the claims object.
     * @return the issuer
     */
    String getIssuer();

    /**
     * Get the subject for the claims object.
     * @return the subject
     */
    String getSubject();

    /**
     * Get the audience for the claims object.
     * @return the subject
     */
    List<String> getAudience();

    /**
     * Get the expiration time for the claims object.
     * @return the subject
     */
    Date getExpirationTime();

    /**
     * Get the earliest processing time for the claims object.
     * @return the subject
     */
    Date getNotBefore();

    /**
     * Get the issue time for the claims object.
     * @return the subject
     */
    Date getIssuedAt();

    /**
     * Get the JWT ID for the claims object.
     * @return the subject
     */
    String getJwtId();

    /**
     * Get all the claim key-value pairs for the claims object.
     * @return the subject
     */
    Map<String, Object> getClaims();
}
