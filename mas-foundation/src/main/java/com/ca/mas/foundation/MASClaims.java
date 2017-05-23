/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
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

public interface MASClaims {

    //Registered claim convenience methods
    String getIssuer();
    String getSubject();
    List<String> getAudience();
    Date getExpirationTime();
    Date getNotBefore();
    Date getIssuedAt();
    String getJwtId();

    //Claims object
    Map<String, Object> getClaims();

    class MASClaimsBuilder {
        private Map<String, Object> claims = new HashMap<>();

        public MASClaimsBuilder(MASClaims masClaims) {
            if (masClaims != null) {
                claims.putAll(masClaims.getClaims());
            }
        }

        public MASClaimsBuilder() {
        }

        public MASClaimsBuilder claim(String name, Object value) {
            claims.put(name, value);
            return this;
        }

        public MASClaimsBuilder issuer(String issuer) {
            claims.put(MASClaimsConstants.ISSUER, issuer);
            return this;
        }

        public MASClaimsBuilder subject(String subject) {
            claims.put(MASClaimsConstants.SUBJECT, subject);
            return this;
        }

        public MASClaimsBuilder audience(List<String> aud) {
            if (aud != null) {
                claims.put(MASClaimsConstants.AUDIENCE, aud);
            }
            return this;
        }

        public MASClaimsBuilder audience(String aud) {
            if (aud != null) {
                claims.put(MASClaimsConstants.AUDIENCE, Collections.singletonList(aud));
            }
            return this;
        }

        public MASClaimsBuilder expirationTime(Date exp) {
            claims.put(MASClaimsConstants.EXPIRATION, exp);
            return this;
        }

        public MASClaimsBuilder notBeforeTime(Date nbf) {
            claims.put(MASClaimsConstants.NOT_BEFORE, nbf);
            return this;
        }

        public MASClaimsBuilder issueTime(Date iat) {
            claims.put(MASClaimsConstants.ISSUED_AT, iat);
            return this;
        }

        public MASClaimsBuilder jwtId(String jti) {
            claims.put(MASClaimsConstants.JWT_ID, jti);
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
}
