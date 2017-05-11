/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.messaging;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Base64;

import com.ca.mas.connecta.util.ConnectaConsts;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.FoundationConsts;
import com.ca.mas.identity.ScimIdentifiable;
import com.ca.mas.messaging.util.MessagingConsts;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>The <b>MASMessage</b> data structure is used to send and receive messages from the MQTT message broker. This interface includes
 * MQTT connect option information regarding duplicate and retained messages used by the
 * {@link <a href="https://www.eclipse.org/paho/files/javadoc/org/eclipse/paho/client/mqttv3/MqttConnectOptions.html">MqttConnectOptions</a>}
 * class.</p>
 */
public abstract class MASMessage implements MASPayload, Parcelable {
    public static MASMessage newInstance(Intent intent) throws MASMessageException {
        return intent.getParcelableExtra(FoundationConsts.KEY_MESSAGE);
    }

    public static MASMessage newInstance() {

        return new MASMessage() {
            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(this.mSenderId);
                dest.writeString(this.mVersion);
                dest.writeByteArray(this.mPayload);
                dest.writeByte(this.mIsDuplicate ? (byte) 1 : (byte) 0);
                dest.writeByte(this.mIsRetained ? (byte) 1 : (byte) 0);
                dest.writeLong(this.mSentTime);
                dest.writeString(this.mDisplayName);
                dest.writeString(this.mSenderType);
                dest.writeString(this.mContentType);
                dest.writeString(this.mContentEncoding);
                dest.writeInt(this.mQos);
                dest.writeString(this.mTopic);
            }

            private String mSenderId;
            private String mVersion = "1.0";
            private byte[] mPayload = new byte[0];
            private boolean mIsDuplicate;
            private boolean mIsRetained;
            private long mSentTime = 0L;
            private String mDisplayName;
            private String mSenderType = ScimIdentifiable.ResourceType.User.toString();
            private String mContentType = MessagingConsts.DEFAULT_TEXT_PLAIN_CONTENT_TYPE;
            private String mContentEncoding = MessagingConsts.DEFAULT_BASE64_ENCODING;
            private int mQos = 2;
            private String mTopic;

            @Override
            public int getQos() {
                return mQos;
            }

            @Override
            public void setQos(int qos) {
                mQos = qos;
            }

            @Override
            public String getSenderId() {
                return mSenderId;
            }

            @Override
            public void setVersion(String version) {
                mVersion = version;
            }

            @Override
            public String getVersion() {
                return mVersion;
            }

            @Override
            public void setSenderId(String senderId) {
                mSenderId = senderId;
            }

            @Override
            public byte[] getPayload() {
                return mPayload;
            }

            @Override
            public void setPayload(byte[] payload) {
                this.mPayload = payload;
            }

            @Override
            public boolean isDuplicate() {
                return mIsDuplicate;
            }

            @Override
            public void setDuplicate(boolean isDuplicate) {
                this.mIsDuplicate = isDuplicate;
            }

            @Override
            public boolean isRetained() {
                return mIsRetained;
            }

            @Override
            public void setRetained(boolean isRetained) {
                this.mIsRetained = isRetained;
            }

            @Override
            public String getDisplayName() {
                return mDisplayName;
            }

            @Override
            public void setSenderType(String senderType) {
                mSenderType = senderType;
            }

            @Override
            public String getSenderType() {
                return mSenderType;
            }

            @Override
            public void setSentTime(long sentTime) {
                mSentTime = sentTime;
            }

            @Override
            public long getSentTime() {
                return mSentTime;
            }

            @Override
            public void setContentType(String contentType) {
                mContentType = contentType;
            }

            @Override
            public String getContentType() {
                return mContentType;
            }

            @Override
            public void setContentEncoding(String encoding) {
                mContentEncoding = encoding;
            }

            @Override
            public String getContentEncoding() {
                return mContentEncoding;
            }

            @Override
            public String getTopic() {
                return mTopic;
            }

            @Override
            public void setTopic(String topic) {
                mTopic = topic;
            }

            public void createMASMessageFromJSONString(String jsonStr) throws MASMessageException {
                try {
                    JSONObject jobj = new JSONObject(jsonStr);
                    mVersion = jobj.optString(ConnectaConsts.KEY_VERSION, MessagingConsts.DEFAULT_VERSION);
                    mSenderId = jobj.optString(ConnectaConsts.KEY_SENDER_ID);
                    mSenderType = jobj.optString(ConnectaConsts.KEY_SENDER_TYPE);
                    if (!TextUtils.isEmpty(mSenderType)) {
                        mSenderType = mSenderType.toUpperCase();
                    }
                    mDisplayName = jobj.optString(ConnectaConsts.KEY_DISPLAY_NAME);
                    mSentTime = jobj.getLong(ConnectaConsts.KEY_SENT_TIME);
                    mContentType = jobj.optString(ConnectaConsts.KEY_CONTENT_TYPE);
                    mContentEncoding = jobj.optString(ConnectaConsts.KEY_CONTENT_ENCODING, FoundationConsts.ENC_UTF8);
                    String payloadBefore = jobj.optString(ConnectaConsts.KEY_PAYLOAD, FoundationConsts.EMPTY);
                    mPayload = Base64.decode(payloadBefore.getBytes(), Base64.NO_WRAP);
                    mTopic = jobj.optString(ConnectaConsts.KEY_TOPIC);
                } catch (JSONException je) {
                    throw new MASMessageException(je);
                }
            }

            @Override
            public String createJSONStringFromMASMessage(Context context) throws MASMessageException {
                JSONObject jobj = new JSONObject();
                try {
                    String ver = getVersion();
                    if (TextUtils.isEmpty(ver)) {
                        ver = MessagingConsts.DEFAULT_VERSION;
                    }
                    jobj.put(ConnectaConsts.KEY_VERSION, ver);

                    String id = getSenderId();
                    if (TextUtils.isEmpty(id)) {
                        MASUser masUser = MASUser.getCurrentUser();
                        id = masUser.getId();
                    }
                    jobj.put(ConnectaConsts.KEY_SENDER_ID, id);

                    String senderType = getSenderType();
                    if (TextUtils.isEmpty(senderType)) {
                        senderType = MessagingConsts.DEFAULT_SENDER_TYPE;
                    }
                    jobj.put(ConnectaConsts.KEY_SENDER_TYPE, senderType.toUpperCase());

                    String dispName = getDisplayName();
                    if (TextUtils.isEmpty(dispName)) {
                        dispName = id;
                    }
                    jobj.put(ConnectaConsts.KEY_DISPLAY_NAME, dispName);

                    long sentTime = getSentTime();
                    if (sentTime == 0) {
                        sentTime = System.currentTimeMillis();
                    }
                    jobj.put(ConnectaConsts.KEY_SENT_TIME, sentTime);

                    String contentType = getContentType();
                    if (TextUtils.isEmpty(contentType)) {
                        contentType = MessagingConsts.DEFAULT_TEXT_PLAIN_CONTENT_TYPE;
                    }
                    jobj.put(ConnectaConsts.KEY_CONTENT_TYPE, contentType);

                    String contentEnc = getContentEncoding();
                    if (TextUtils.isEmpty(contentEnc)) {
                        contentEnc = MessagingConsts.DEFAULT_BASE64_ENCODING;
                    }
                    jobj.put(ConnectaConsts.KEY_CONTENT_ENCODING, contentEnc);

                    byte[] payload = getPayload();
                    if (payload != null && payload.length > 0) {
                        jobj.put(ConnectaConsts.KEY_PAYLOAD, new String(Base64.encode(payload, Base64.NO_WRAP)));
                    } else {
                        throw new MASMessageException("Parameter cannot be empty or null.");
                    }

                    String topic = getTopic();
                    if (!TextUtils.isEmpty(topic)) {
                        jobj.put(ConnectaConsts.KEY_TOPIC, topic);
                    }
                } catch (JSONException je) {
                    throw new MASMessageException(je);
                }
                return jobj.toString();
            }
        };
    }

    /**
     * <b>Description:</b> See {@link <a href="https://www.eclipse.org/paho/files/javadoc/org/eclipse/paho/client/mqttv3/MqttMessage.html#setDuplicate(boolean)">MqttMessage.setDuplication(boolean)</a>}.
     *
     * @param isDuplicate true or false.
     */
    public abstract void setDuplicate(boolean isDuplicate);

    /**
     * <b>Description:</b> <i>true</i> See {@link <a href="https://www.eclipse.org/paho/files/javadoc/org/eclipse/paho/client/mqttv3/MqttMessage.html#setDuplicate(boolean)">MqttMessage.setDuplication(boolean)</a>}.
     *
     * @return boolean true or false.
     */
    public abstract boolean isDuplicate();

    /**
     * <b>Description:</b> See {@link <a href="https://www.eclipse.org/paho/files/javadoc/org/eclipse/paho/client/mqttv3/MqttMessage.html#setRetained-boolean-">MqttMessage.setRetained(boolean)</a>}
     *
     * @param isRetained true or false.
     */
    public abstract void setRetained(boolean isRetained);

    /**
     * <b>Description:</b> See {@link <a href="https://www.eclipse.org/paho/files/javadoc/org/eclipse/paho/client/mqttv3/MqttMessage.html#isRetained--">MqttMessage.isRetained()</a>}
     *
     * @return boolean true or false.
     */
    public abstract boolean isRetained();

    /**
     * <b>Description:</b> Defaults to EXACTLY_ONCE. See {@link <a href="https://www.eclipse.org/paho/files/javadoc/org/eclipse/paho/client/mqttv3/MqttMessage.html#setRetained-boolean-">MqttMessage.setQos(int)</a>}
     *
     * @param qos either 0, 1, or 2 (default)
     */
    public abstract void setQos(int qos);

    /**
     * <b>Description:</b> Defaults to EXACTLY_ONCE. See {@link <a href="https://www.eclipse.org/paho/files/javadoc/org/eclipse/paho/client/mqttv3/MqttMessage.html#setRetained-boolean-">MqttMessage.setQos(int)</a>}
     *
     * @return int value of either 0, 1, or 2 (default)
     */
    public abstract int getQos();

    /**
     * <b>Description:</b> Set the topic that was used when this message is sent.
     *
     * @param topic the String representing the topic.
     */
    public abstract void setTopic(String topic);

    /**
     * <b>Description:</b> Getter for getting the topic this message arrived, on.
     *
     * @return String the topic.
     */
    public abstract String getTopic();
}
