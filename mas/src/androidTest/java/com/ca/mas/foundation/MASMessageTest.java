/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import android.util.Base64;

import com.ca.mas.connecta.client.MASConnectaClient;
import com.ca.mas.connecta.util.ConnectaConsts;
import com.ca.mas.messaging.MASMessage;
import com.ca.mas.messaging.MASMessageException;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class MASMessageTest {

    private MASMessage mMessage;
    private String mSenderId = "admin";
    private String mVersion = "1.1";
    private String mPayloadString = "Test Payload";
    private byte[] mPayload = mPayloadString.getBytes();
    private boolean mDuplicate = true;
    private boolean mRetained = true;
    private long mSentTime = 1234567890L;
    private String mDisplayName = "Admin";
    private String mSenderType = "Employee";
    private String mTopic = "Test topic";
    private int mQos = MASConnectaClient.EXACTLY_ONCE;
    private String mContentType = "Test type";
    private String mContentEncoding = "Test encoding";

    @Before
    public void initTestMessage() {
        mMessage = MASMessage.newInstance();
        mMessage.setSenderId(mSenderId);
        mMessage.setVersion(mVersion);
        mMessage.setPayload(mPayload);
        mMessage.setDuplicate(mDuplicate);
        mMessage.setRetained(mRetained);
        mMessage.setSentTime(mSentTime);
        mMessage.setSenderType(mSenderType);
        mMessage.setContentType(mContentType);
        mMessage.setContentEncoding(mContentEncoding);
        mMessage.setQos(mQos);
        mMessage.setTopic(mTopic);
    }

    @Test
    public void testVerifyMessageContents() {
        assertEquals(mMessage.getSenderId(), mSenderId);
        assertEquals(mMessage.getVersion(), mVersion);
        assertEquals(mMessage.getPayload(), mPayload);
        assertEquals(mMessage.isDuplicate(), mDuplicate);
        assertEquals(mMessage.isRetained(), mRetained);
        assertEquals(mMessage.getSentTime(), mSentTime);
        assertEquals(mMessage.getSenderType(), mSenderType);
        assertEquals(mMessage.getContentType(), mContentType);
        assertEquals(mMessage.getContentEncoding(), mContentEncoding);
        assertEquals(mMessage.getQos(), mQos);
        assertEquals(mMessage.getTopic(), mTopic);
    }

    @Test
    public void testCreateMASMessageFromJSONStringEmptySender() throws MASMessageException {
        mMessage = MASMessage.newInstance();
        String input = "{" +
                "\"" + ConnectaConsts.KEY_VERSION + "\":\"" + mVersion + "\"," +
                "\"" + ConnectaConsts.KEY_SENDER_ID + "\":\"" + mSenderId + "\"," +
                "\"" + ConnectaConsts.KEY_DISPLAY_NAME + "\":\"" + mDisplayName + "\"," +
                "\"" + ConnectaConsts.KEY_SENT_TIME + "\":\"" + mSentTime + "\"," +
                "\"" + ConnectaConsts.KEY_CONTENT_TYPE + "\":\"" + mContentType + "\"," +
                "\"" + ConnectaConsts.KEY_CONTENT_ENCODING + "\":\"" + mContentEncoding + "\"," +
                "\"" + ConnectaConsts.KEY_PAYLOAD + "\":\"" + mPayloadString + "\"," +
                "\"" + ConnectaConsts.KEY_TOPIC + "\":\"" + mTopic + "\"" +
                "}";
        mMessage.createMASMessageFromJSONString(input);

        assertEquals(mMessage.getVersion(), mVersion);
        assertEquals(mMessage.getSenderId(), mSenderId);
        assertEquals(mMessage.getSenderType(), "");
        assertEquals(mMessage.getDisplayName(), mDisplayName);
        assertEquals(mMessage.getSentTime(), mSentTime);
        assertEquals(mMessage.getContentType(), mContentType);
        assertEquals(mMessage.getContentEncoding(), mContentEncoding);
        assertEquals(mMessage.getTopic(), mTopic);
        assertTrue(Arrays.equals(mMessage.getPayload(), Base64.decode(mPayload, Base64.NO_WRAP)));
    }

    @Test
    public void testCreateMASMessageFromJSONString() throws MASMessageException {
        mMessage = MASMessage.newInstance();
        String input = "{" +
                "\"" + ConnectaConsts.KEY_VERSION + "\":\"" + mVersion + "\"," +
                "\"" + ConnectaConsts.KEY_SENDER_ID + "\":\"" + mSenderId + "\"," +
                "\"" + ConnectaConsts.KEY_SENDER_TYPE + "\":\"" + mSenderType + "\"," +
                "\"" + ConnectaConsts.KEY_DISPLAY_NAME + "\":\"" + mDisplayName + "\"," +
                "\"" + ConnectaConsts.KEY_SENT_TIME + "\":\"" + mSentTime + "\"," +
                "\"" + ConnectaConsts.KEY_CONTENT_TYPE + "\":\"" + mContentType + "\"," +
                "\"" + ConnectaConsts.KEY_CONTENT_ENCODING + "\":\"" + mContentEncoding + "\"," +
                "\"" + ConnectaConsts.KEY_PAYLOAD + "\":\"" + mPayloadString + "\"," +
                "\"" + ConnectaConsts.KEY_TOPIC + "\":\"" + mTopic + "\"" +
                "}";
        mMessage.createMASMessageFromJSONString(input);

        assertEquals(mMessage.getVersion(), mVersion);
        assertEquals(mMessage.getSenderId(), mSenderId);
        assertEquals(mMessage.getSenderType(), mSenderType.toUpperCase());
        assertEquals(mMessage.getDisplayName(), mDisplayName);
        assertEquals(mMessage.getSentTime(), mSentTime);
        assertEquals(mMessage.getContentType(), mContentType);
        assertEquals(mMessage.getContentEncoding(), mContentEncoding);
        assertEquals(mMessage.getTopic(), mTopic);
        assertTrue(Arrays.equals(mMessage.getPayload(), Base64.decode(mPayload, Base64.NO_WRAP)));
    }

    @Test(expected = JSONException.class)
    public void testCreateMASMessageFromInvalidJSONException() throws JSONException {
        mMessage = MASMessage.newInstance();
        String invalidInput = "asdf:[]{]";
        try {
            mMessage.createMASMessageFromJSONString(invalidInput);
        } catch (MASMessageException e) {
            throw (JSONException) e.getCause();
        }
    }
}
