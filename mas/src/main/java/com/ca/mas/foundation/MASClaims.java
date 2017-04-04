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
import java.util.List;
import java.util.UUID;

public interface MASClaims {

    //Registered Claim
    String getIssuer();
    String getSubject();
    List<String> getAudience();
    Date getExpirationTime();
    Date getNotBefore();
    Date getIssuedAt();
    String getJwtId();

    //Private Claim
    Object getContent();
    String getContentType();


    class MASClaimsBuilder {

        private Date exp;
        private Object content;
        private String contentType;

        MASClaimsBuilder expirationTime(final Date exp) {
            this.exp = exp;
            return this;
        }

        MASClaimsBuilder claim(String name, Object content) {
            this.content = content;
            return this;
        }

        MASClaims build () {

            return new MASClaims() {

                String jti = UUID.randomUUID().toString();
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
                    return exp;
                }

                @Override
                public Date getNotBefore() {
                    return null;
                }

                @Override
                public Date getIssuedAt() {
                    long currentTime = System.currentTimeMillis() / 1000;
                    return DateUtils.fromSecondsSinceEpoch(currentTime);
                }

                @Override
                public String getJwtId() {
                    return jti;
                }

                @Override
                public Object getContent() {
                    return content;
                }

                @Override
                public String getContentType() {
                    return contentType;
                }
            };

        }


    }


}
