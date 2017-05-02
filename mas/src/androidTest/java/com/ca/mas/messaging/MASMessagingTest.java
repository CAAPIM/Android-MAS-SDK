/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.messaging;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.util.Base64;

import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASTestBase;
import com.ca.mas.connecta.client.MASConnectaClient;
import com.ca.mas.connecta.util.ConnectaConsts;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.messaging.util.MessagingConsts;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class MASMessagingTest extends MASTestBase {
    protected static final int DEFAULT_MAX = 10485760;
    private MASMessage mMessage;
    private String mSenderId = "admin";
    private String mVersion = "1.1";
    private String mPayloadString = "Test Payload";
    private byte[] mPayload = mPayloadString.getBytes();
    private boolean mDuplicate = true;
    private boolean mRetained = true;
    private long mSentTime = 1234567890L;
    private String mDisplayName = "admin";
    private String mSenderType = "User";
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
                "\"" + ConnectaConsts.KEY_QOS + "\":" + mQos + "," +
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
        assertEquals(mMessage.getQos(), mQos);
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
                "\"" + ConnectaConsts.KEY_QOS + "\":" + mQos + "," +
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
        assertEquals(mMessage.getQos(), mQos);
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

    @Test
    public void testCreateJSONStringFromMASMessage() throws Exception {
        String expected = "{" +
                "\"" + ConnectaConsts.KEY_VERSION + "\":\"" + mVersion + "\"," +
                "\"" + ConnectaConsts.KEY_SENDER_ID + "\":\"" + mSenderId + "\"," +
                "\"" + ConnectaConsts.KEY_SENDER_TYPE + "\":\"" + mSenderType.toUpperCase() + "\"," +
                "\"" + ConnectaConsts.KEY_DISPLAY_NAME + "\":\"" + mDisplayName + "\"," +
                "\"" + ConnectaConsts.KEY_SENT_TIME + "\":" + mSentTime + "," +
                "\"" + ConnectaConsts.KEY_CONTENT_TYPE + "\":\"" + mContentType + "\"," +
                "\"" + ConnectaConsts.KEY_CONTENT_ENCODING + "\":\"" + mContentEncoding + "\"," +
                "\"" + ConnectaConsts.KEY_QOS + "\":" + mQos + "," +
                "\"" + ConnectaConsts.KEY_PAYLOAD + "\":\"" + new String(Base64.encode(mPayload, Base64.NO_WRAP)) + "\"," +
                "\"" + ConnectaConsts.KEY_TOPIC + "\":\"" + mTopic + "\"" +
                "}";
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        try {
            String jsonString = mMessage.createJSONStringFromMASMessage(context);
            assertEquals(expected, jsonString);
        } catch (MASMessageException e) {
            throw (JSONException) e.getCause();
        }
    }

    private void login() throws Exception {
        MAS.start(getContext(), getConfig("/msso_config.json"));
        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("admin", "7layer".toCharArray(), callback);
        assertNotNull(callback.get());
    }

    @Test
    public void testCreateJSONStringFromNewMASMessageWithDefaultAttributes() throws Exception {
        login();
        mMessage = MASMessage.newInstance();
        mMessage.setPayload(mPayload);
        long startTime = System.currentTimeMillis();

        String expectedPayload = new String(Base64.encode(mPayload, Base64.NO_WRAP));
        try {
            String jsonString = mMessage.createJSONStringFromMASMessage(getContext());
            JSONObject jsonObject = new JSONObject(jsonString);
            assertEquals(jsonObject.getString(ConnectaConsts.KEY_VERSION), "1.0");
            assertEquals(jsonObject.getString(ConnectaConsts.KEY_SENDER_ID), mSenderId);
            assertEquals(jsonObject.getString(ConnectaConsts.KEY_SENDER_TYPE), mSenderType.toUpperCase());
            assertEquals(jsonObject.getString(ConnectaConsts.KEY_DISPLAY_NAME), mDisplayName);
            long sentTime = jsonObject.getLong(ConnectaConsts.KEY_SENT_TIME);
            assertTrue(startTime <= sentTime && sentTime <= System.currentTimeMillis());
            assertEquals(jsonObject.getString(ConnectaConsts.KEY_CONTENT_TYPE), MessagingConsts.DEFAULT_TEXT_PLAIN_CONTENT_TYPE);
            assertEquals(jsonObject.getString(ConnectaConsts.KEY_CONTENT_ENCODING), MessagingConsts.DEFAULT_BASE64_ENCODING);
            assertEquals(jsonObject.getInt(ConnectaConsts.KEY_QOS), MASConnectaClient.EXACTLY_ONCE);
            assertEquals(jsonObject.getString(ConnectaConsts.KEY_PAYLOAD), expectedPayload);
        } catch (MASMessageException e) {
            throw (JSONException) e.getCause();
        }
    }

    @Test
    public void testCreateJSONStringFromNewMASMessageWithEmptyAttributes() throws Exception {
        login();
        mMessage = MASMessage.newInstance();
        mMessage.setPayload(mPayload);
        mMessage.setVersion("");
        mMessage.setSenderType("");
        mMessage.setContentType("");
        mMessage.setContentEncoding("");
        mMessage.setQos(0);
        long startTime = System.currentTimeMillis();

        String expectedPayload = new String(Base64.encode(mPayload, Base64.NO_WRAP));
        try {
            String jsonString = mMessage.createJSONStringFromMASMessage(getContext());
            JSONObject jsonObject = new JSONObject(jsonString);
            assertEquals(jsonObject.getString(ConnectaConsts.KEY_VERSION), "1.0");
            assertEquals(jsonObject.getString(ConnectaConsts.KEY_SENDER_ID), mSenderId);
            assertEquals(jsonObject.getString(ConnectaConsts.KEY_SENDER_TYPE), mSenderType.toUpperCase());
            assertEquals(jsonObject.getString(ConnectaConsts.KEY_DISPLAY_NAME), mDisplayName);
            long sentTime = jsonObject.getLong(ConnectaConsts.KEY_SENT_TIME);
            assertTrue(startTime <= sentTime && sentTime <= System.currentTimeMillis());
            assertEquals(jsonObject.getString(ConnectaConsts.KEY_CONTENT_TYPE), MessagingConsts.DEFAULT_TEXT_PLAIN_CONTENT_TYPE);
            assertEquals(jsonObject.getString(ConnectaConsts.KEY_CONTENT_ENCODING), MessagingConsts.DEFAULT_BASE64_ENCODING);
            assertEquals(jsonObject.getInt(ConnectaConsts.KEY_QOS), MASConnectaClient.AT_MOST_ONCE);
            assertEquals(jsonObject.getString(ConnectaConsts.KEY_PAYLOAD), expectedPayload);
        } catch (MASMessageException e) {
            throw (JSONException) e.getCause();
        }
    }

    @Test(expected = MASMessageException.class)
    public void testCreateJSONStringFromNullPayloadMASMessage() throws Exception {
        login();

        mMessage = MASMessage.newInstance();
        mMessage.setPayload(null);
        try {
            mMessage.createJSONStringFromMASMessage(getContext());
        } catch (MASMessageException e) {
            assertEquals(e.getMessage(), "Parameter cannot be empty or null.");
            throw e;
        }
    }

    @Test(expected = MASMessageException.class)
    public void testCreateJSONStringFromEmptyPayloadMASMessage() throws Exception {
        login();

        mMessage = MASMessage.newInstance();
        try {
            mMessage.createJSONStringFromMASMessage(getContext());
            mMessage.setPayload(new byte[0]);
        } catch (MASMessageException e) {
            assertEquals(e.getMessage(), "Parameter cannot be empty or null.");
            throw e;
        }
    }
}
