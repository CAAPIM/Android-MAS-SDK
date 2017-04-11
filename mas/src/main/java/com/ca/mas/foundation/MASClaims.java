/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

import com.ca.mas.core.store.StorageProvider;
import com.nimbusds.jwt.util.DateUtils;

import org.json.JSONObject;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MASClaims {

    String CONTENT = "content";
    String CONTENT_TYPE = "content-type";

    //Registered Claim
    String getIssuer();
    String getSubject();
    List<String> getAudience();
    Date getExpirationTime();
    Date getNotBefore();
    Date getIssuedAt();
    String getJwtId();

    //Private Claim
    Map<String, Object> getClaims();


    class MASClaimsBuilder {

        private Date exp;
        private Map<String, Object> claims = new HashMap<>();

        public MASClaimsBuilder(MASClaims masClaims) {
            if (masClaims != null) {
                //Only allow to overwrite the private claim and the exp claim
                claims.putAll(masClaims.getClaims());
                exp = masClaims.getExpirationTime();
            }
        }

        public MASClaimsBuilder() {
        }

        public MASClaimsBuilder expirationTime(final Date exp) {
            this.exp = exp;
            return this;
        }

        public MASClaimsBuilder claim(@MASClaimsConstants String name, Object value) {
            claims.put(name, value);
            return this;
        }

        public MASClaims build () {

            return new MASClaims() {

                long currentTime = System.currentTimeMillis() / 1000;
                String jti = UUID.randomUUID().toString();
                Date issuedAt = DateUtils.fromSecondsSinceEpoch(currentTime);
                Date expDate = DateUtils.fromSecondsSinceEpoch(currentTime + 300L);

                @Override
                public String getIssuer() {
                    return String.format("device://%s/%s",
                            StorageProvider.getInstance().getTokenManager().getMagIdentifier(),
                            StorageProvider.getInstance().getClientCredentialContainer().getClientId());
                }

                @Override
                public String getSubject() {
                    if (MASUser.getCurrentUser() != null) {
                        return MASUser.getCurrentUser().getUserName();
                    } else {
                        return StorageProvider.getInstance().getClientCredentialContainer().getClientId();
                    }
                }

                @Override
                public List<String> getAudience() {
                    return Collections.singletonList(MASConfiguration.getCurrentConfiguration().getGatewayHostName());
                }

                @Override
                public Date getExpirationTime() {
                    if (exp == null) {
                        return expDate;
                    } else {
                        return exp;
                    }
                }

                @Override
                public Date getNotBefore() {
                    return null;
                }

                @Override
                public Date getIssuedAt() {
                    return issuedAt;
                }

                @Override
                public String getJwtId() {
                    return jti;
                }

                @Override
                public Map<String, Object> getClaims() {
                    return claims;
                }

            };

        }


    }


}
