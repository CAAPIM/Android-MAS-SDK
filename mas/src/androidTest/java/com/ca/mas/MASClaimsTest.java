/*
 *
 *  * Copyright (c) 2016 CA. All rights reserved.
 *  *
 *  * This software may be modified and distributed under the terms
 *  * of the MIT license.  See the LICENSE file for details.
 *  *
 *
 */

package com.ca.mas;

import com.ca.mas.foundation.MASClaims;
import com.ca.mas.foundation.MASClaimsBuilder;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class MASClaimsTest extends MASLoginTestBase {

    private String mJwtId = "1a2b3c4d5e6f";
    private String mIssuer = "CA Authority";
    private String mSubject = "JWT Test";
    private String mAudienceString = "General users";
    private List<String> mAudience = new ArrayList<>();
    private Date mIssueTime = new Date(1509924960000L);
    private Date mNotBeforeTime = new Date(1509924960000L);
    private Date mExpirationTime = new Date(1609924960000L);

    @Test
    public void testRegisteredClaims() {
        mAudience.add(mAudienceString);

        MASClaimsBuilder builder = new MASClaimsBuilder();
        MASClaims claims = builder
                .jwtId(mJwtId)
                .audience(mAudience)
                .subject(mSubject)
                .issuer(mIssuer)
                .issueTime(mIssueTime)
                .notBeforeTime(mNotBeforeTime)
                .expirationTime(mExpirationTime)
                .build();

        assertNotNull(claims);
        assertEquals(mJwtId, claims.getJwtId());
        assertEquals(mAudience, claims.getAudience());
        assertEquals(mSubject, claims.getSubject());
        assertEquals(mIssuer, claims.getIssuer());
        assertEquals(mIssueTime, claims.getIssuedAt());
        assertEquals(mNotBeforeTime, claims.getNotBefore());
        assertEquals(mExpirationTime, claims.getExpirationTime());
    }

    @Test
    public void testRegisteredClaimsAudienceString() {
        mAudience.add(mAudienceString);

        MASClaimsBuilder builder = new MASClaimsBuilder();
        MASClaims claims = builder
                .jwtId(mJwtId)
                .audience(mAudienceString)
                .subject(mSubject)
                .issuer(mIssuer)
                .issueTime(mIssueTime)
                .notBeforeTime(mNotBeforeTime)
                .expirationTime(mExpirationTime)
                .build();

        assertNotNull(claims);
        assertEquals(mJwtId, claims.getJwtId());
        assertEquals(mAudience, claims.getAudience());
        assertEquals(mSubject, claims.getSubject());
        assertEquals(mIssuer, claims.getIssuer());
        assertEquals(mIssueTime, claims.getIssuedAt());
        assertEquals(mNotBeforeTime, claims.getNotBefore());
        assertEquals(mExpirationTime, claims.getExpirationTime());
    }

    @Test
    public void testRegisteredClaimsAudienceNullString() {
        mAudience.add(mAudienceString);

        MASClaimsBuilder builder = new MASClaimsBuilder();
        MASClaims claims = builder
                .jwtId(mJwtId)
                .audience((String) null)
                .subject(mSubject)
                .issuer(mIssuer)
                .issueTime(mIssueTime)
                .notBeforeTime(mNotBeforeTime)
                .expirationTime(mExpirationTime)
                .build();

        assertNotNull(claims);
        assertEquals(mJwtId, claims.getJwtId());
        assertNotNull(mAudience);
        assertEquals(mSubject, claims.getSubject());
        assertEquals(mIssuer, claims.getIssuer());
        assertEquals(mIssueTime, claims.getIssuedAt());
        assertEquals(mNotBeforeTime, claims.getNotBefore());
        assertEquals(mExpirationTime, claims.getExpirationTime());
    }

    @Test
    public void testRegisteredClaimsAudienceNullList() {
        mAudience.add(mAudienceString);

        MASClaimsBuilder builder = new MASClaimsBuilder();
        MASClaims claims = builder
                .jwtId(mJwtId)
                .audience((List<String>) null)
                .subject(mSubject)
                .issuer(mIssuer)
                .issueTime(mIssueTime)
                .notBeforeTime(mNotBeforeTime)
                .expirationTime(mExpirationTime)
                .build();

        assertNotNull(claims);
        assertEquals(mJwtId, claims.getJwtId());
        assertNotNull(mAudience);
        assertEquals(mSubject, claims.getSubject());
        assertEquals(mIssuer, claims.getIssuer());
        assertEquals(mIssueTime, claims.getIssuedAt());
        assertEquals(mNotBeforeTime, claims.getNotBefore());
        assertEquals(mExpirationTime, claims.getExpirationTime());
    }

    @Test
    public void testDefaultClaims() {
        MASClaimsBuilder builder = new MASClaimsBuilder();
        MASClaims claims = builder.build();

        assertNotNull(claims);
        assertNotNull(claims.getJwtId());
        assertNotNull(claims.getIssuer());
        assertNotNull(claims.getSubject());
        assertNotNull(claims.getAudience());
        assertNull(claims.getNotBefore());

        Date issuedTime = claims.getIssuedAt();
        Date expirationTime = claims.getExpirationTime();
        assertNotNull(issuedTime);
        assertNotNull(expirationTime);
        Date expectedExpiration = new Date(issuedTime.getTime() + 300L);
        assertTrue(issuedTime.getTime() < expectedExpiration.getTime());
        assertTrue(expectedExpiration.getTime() <= expirationTime.getTime());
    }
}
