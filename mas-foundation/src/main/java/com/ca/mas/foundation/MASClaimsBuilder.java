/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import com.ca.mas.core.store.StorageProvider;
import com.nimbusds.jwt.util.DateUtils;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.ca.mas.foundation.MASClaimsConstants.EXPIRATION;
import static com.ca.mas.foundation.MASClaimsConstants.ISSUED_AT;
import static com.ca.mas.foundation.MASClaimsConstants.JWT_ID;

/**
 * A builder to construct MASClaims objects.
 */
public class MASClaimsBuilder {
    private Map<String, Object> claims = new HashMap<>();

    /**
     * Initialize a builder object.
     */
    public MASClaimsBuilder() {
    }

    /**
     * Initialize a builder object with an existing MASClaims object.
     * @param masClaims the claims
     */
    public MASClaimsBuilder(MASClaims masClaims) {
        if (masClaims != null) {
            claims.putAll(masClaims.getClaims());
        }
    }

    /**
     * Sets the issuer for the MASClaims object.
     * See https://tools.ietf.org/html/rfc7519#section-4.1.1 for further information.
     * @param issuer the claim issuer
     * @return the builder
     */
    public MASClaimsBuilder issuer(String issuer) {
        claims.put(MASClaimsConstants.ISSUER, issuer);
        return this;
    }

    /**
     * Sets the subject for the MASClaims object.
     * See https://tools.ietf.org/html/rfc7519#section-4.1.2 for further information.
     * @param subject the subject
     * @return the builder
     */
    public MASClaimsBuilder subject(String subject) {
        claims.put(MASClaimsConstants.SUBJECT, subject);
        return this;
    }

    /**
     * Sets a singular audience for the MASClaims object.
     * See https://tools.ietf.org/html/rfc7519#section-4.1.3 for further information.
     * @param aud the audience
     * @return the builder
     */
    public MASClaimsBuilder audience(String aud) {
        if (aud != null) {
            claims.put(MASClaimsConstants.AUDIENCE, Collections.singletonList(aud));
        }
        return this;
    }

    /**
     * Sets an audience list for the MASClaims object.
     * See https://tools.ietf.org/html/rfc7519#section-4.1.3 for further information.
     * @param audList the audience list
     * @return the builder
     */
    public MASClaimsBuilder audience(List<String> audList) {
        if (audList != null) {
            claims.put(MASClaimsConstants.AUDIENCE, audList);
        }
        return this;
    }

    /**
     * Sets the expiration time for the MASClaims object.
     * See https://tools.ietf.org/html/rfc7519#section-4.1.4 for further information.
     * @param exp the expiration
     * @return the builder
     */
    public MASClaimsBuilder expirationTime(Date exp) {
        claims.put(EXPIRATION, exp);
        return this;
    }

    /**
     * Sets the earliest processing time for the MASClaims object.
     * See https://tools.ietf.org/html/rfc7519#section-4.1.5 for further information.
     * @param nbf the earliest processing time
     * @return the builder
     */
    public MASClaimsBuilder notBeforeTime(Date nbf) {
        claims.put(MASClaimsConstants.NOT_BEFORE, nbf);
        return this;
    }

    /**
     * Sets the issue time for the MASClaims object.
     * See https://tools.ietf.org/html/rfc7519#section-4.1.6 for further information.
     * @param iat the issue time
     * @return the builder
     */
    public MASClaimsBuilder issueTime(Date iat) {
        claims.put(ISSUED_AT, iat);
        return this;
    }

    /**
     * Sets the JWT ID for the MASClaims object.
     * See https://tools.ietf.org/html/rfc7519#section-4.1.7 for further information.
     * @param jti the JWT ID
     * @return the builder
     */
    public MASClaimsBuilder jwtId(String jti) {
        claims.put(JWT_ID, jti);
        return this;
    }

    /**
     * Sets a claim key-value pair for the MASClaims object.
     * See https://tools.ietf.org/html/rfc7519#section-4.2 and
     * https://tools.ietf.org/html/rfc7519#section-4.3 for further information.
     * @param name the claim name
     * @param value the claim value
     * @return      the builder
     */
    public MASClaimsBuilder claim(String name, Object value) {
        claims.put(name, value);
        return this;
    }

    public MASClaims build() {

        return new MASClaims() {
            long currentTime = System.currentTimeMillis() / 1000;

            @Override
            public String getIssuer() {
                if (claims.containsKey(MASClaimsConstants.ISSUER)) {
                    return (String) claims.get(MASClaimsConstants.ISSUER);
                } else {
                    return String.format("device://%s/%s",
                            StorageProvider.getInstance().getTokenManager().getMagIdentifier(),
                            StorageProvider.getInstance().getClientCredentialContainer().getClientId());
                }
            }

            @Override
            public String getSubject() {
                if (claims.containsKey(MASClaimsConstants.SUBJECT)) {
                    return (String) claims.get(MASClaimsConstants.SUBJECT);
                } else if (MASUser.getCurrentUser() != null) {
                    return MASUser.getCurrentUser().getUserName();
                } else {
                    return StorageProvider.getInstance().getClientCredentialContainer().getClientId();
                }
            }

            @Override
            public List<String> getAudience() {
                if (claims.containsKey(MASClaimsConstants.AUDIENCE)) {
                    return (List<String>) claims.get(MASClaimsConstants.AUDIENCE);
                } else {
                    return Collections.singletonList(MASConfiguration.getCurrentConfiguration().getGatewayHostName());
                }
            }

            @Override
            public Date getExpirationTime() {
                if (claims.containsKey(EXPIRATION)) {
                    return (Date) claims.get(EXPIRATION);
                } else {
                    return DateUtils.fromSecondsSinceEpoch(currentTime + 300L);
                }
            }

            @Override
            public Date getNotBefore() {
                if (claims.containsKey(MASClaimsConstants.NOT_BEFORE)) {
                    return (Date) claims.get(MASClaimsConstants.NOT_BEFORE);
                } else {
                    return null;
                }
            }

            @Override
            public Date getIssuedAt() {
                if (claims.containsKey(ISSUED_AT)) {
                    return (Date) claims.get(ISSUED_AT);
                } else {
                    return DateUtils.fromSecondsSinceEpoch(currentTime);
                }
            }

            @Override
            public String getJwtId() {
                if (claims.containsKey(JWT_ID)) {
                    return (String) claims.get(JWT_ID);
                } else {
                    return UUID.randomUUID().toString();
                }
            }

            @Override
            public Map<String, Object> getClaims() {
                return claims;
            }
        };
    }
}
