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

import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.FoundationConsts;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>The <b>MASMessage</b> data structure is used to send and receive messages from the MQTT message broker. This interface includes
 * MQTT connect option information regarding duplicate and retained messages used by the
 * {@link <a href="https://www.eclipse.org/paho/files/javadoc/org/eclipse/paho/client/mqttv3/MqttConnectOptions.html">MqttConnectOptions</a>}
 * class.</p>
 */
public class MASMessage implements MASPayload, Parcelable {

    private String mSenderId;
    private String mVersion = "1.0";
    private byte[] mPayload = new byte[0];
    private boolean mIsDuplicate;
    private boolean mIsRetained;
    private long mSentTime = 0L;
    private String mDisplayName;
    private String mSenderType = "User";
    private String mContentType = MessagingConsts.DEFAULT_TEXT_PLAIN_CONTENT_TYPE;
    private String mContentEncoding = MessagingConsts.DEFAULT_BASE64_ENCODING;
    private int mQos = 2;
    private String mTopic;

    public static MASMessage newInstance() {
        return new MASMessage();
    }

    public static MASMessage newInstance(Intent intent) throws MASMessageException {
        return intent.getParcelableExtra(FoundationConsts.KEY_MESSAGE);
    }

    public int getQos() {
        return mQos;
    }

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

    public boolean isDuplicate() {
        return mIsDuplicate;
    }

    public void setDuplicate(boolean isDuplicate) {
        this.mIsDuplicate = isDuplicate;
    }

    public boolean isRetained() {
        return mIsRetained;
    }

    public void setRetained(boolean isRetained) {
        this.mIsRetained = isRetained;
    }

    public String getTopic() {
        return mTopic;
    }

    public void setTopic(String topic) {
        mTopic = topic;
    }

    public void createMASMessageFromJSONString(String jsonStr) throws MASMessageException {
        try {
            JSONObject jobj = new JSONObject(jsonStr);
            mVersion = jobj.optString(MessagingConsts.KEY_VERSION, MessagingConsts.DEFAULT_VERSION);
            mSenderId = jobj.optString(MessagingConsts.KEY_SENDER_ID);
            mSenderType = jobj.optString(MessagingConsts.KEY_SENDER_TYPE);
            if (!TextUtils.isEmpty(mSenderType)) {
                mSenderType = mSenderType.toUpperCase();
            }
            mDisplayName = jobj.optString(MessagingConsts.KEY_DISPLAY_NAME);
            mSentTime = jobj.getLong(MessagingConsts.KEY_SENT_TIME);
            mContentType = jobj.optString(MessagingConsts.KEY_CONTENT_TYPE);
            mContentEncoding = jobj.optString(MessagingConsts.KEY_CONTENT_ENCODING, FoundationConsts.ENC_UTF8);
            String payloadBefore = jobj.optString(MessagingConsts.KEY_PAYLOAD, FoundationConsts.EMPTY);
            mPayload = Base64.decode(payloadBefore.getBytes(), Base64.NO_WRAP);
            mTopic = jobj.optString(MessagingConsts.KEY_TOPIC);
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
            jobj.put(MessagingConsts.KEY_VERSION, ver);

            String id = getSenderId();
            if (TextUtils.isEmpty(id)) {
                MASUser masUser = MASUser.getCurrentUser();
                id = masUser.getId();
            }
            jobj.put(MessagingConsts.KEY_SENDER_ID, id);

            String senderType = getSenderType();
            if (TextUtils.isEmpty(senderType)) {
                senderType = MessagingConsts.DEFAULT_SENDER_TYPE;
            }
            jobj.put(MessagingConsts.KEY_SENDER_TYPE, senderType.toUpperCase());

            String dispName = getDisplayName();
            if (TextUtils.isEmpty(dispName)) {
                dispName = id;
            }
            jobj.put(MessagingConsts.KEY_DISPLAY_NAME, dispName);

            long sentTime = getSentTime();
            if (sentTime == 0) {
                sentTime = System.currentTimeMillis();
            }
            jobj.put(MessagingConsts.KEY_SENT_TIME, sentTime);

            String contentType = getContentType();
            if (TextUtils.isEmpty(contentType)) {
                contentType = MessagingConsts.DEFAULT_TEXT_PLAIN_CONTENT_TYPE;
            }
            jobj.put(MessagingConsts.KEY_CONTENT_TYPE, contentType);

            String contentEnc = getContentEncoding();
            if (TextUtils.isEmpty(contentEnc)) {
                contentEnc = MessagingConsts.DEFAULT_BASE64_ENCODING;
            }
            jobj.put(MessagingConsts.KEY_CONTENT_ENCODING, contentEnc);

            byte[] payload = getPayload();
            if (payload != null && payload.length > 0) {
                jobj.put(MessagingConsts.KEY_PAYLOAD, new String(Base64.encode(payload, Base64.NO_WRAP)));
            } else {
                throw new MASMessageException("Parameter cannot be empty or null.");
            }

            String topic = getTopic();
            if (!TextUtils.isEmpty(topic)) {
                jobj.put(MessagingConsts.KEY_TOPIC, topic);
            }
        } catch (JSONException je) {
            throw new MASMessageException(je);
        }
        return jobj.toString();
    }

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

    public MASMessage() {
    }

    protected MASMessage(Parcel in) {
        this.mSenderId = in.readString();
        this.mVersion = in.readString();
        this.mPayload = in.createByteArray();
        this.mIsDuplicate = in.readByte() != 0;
        this.mIsRetained = in.readByte() != 0;
        this.mSentTime = in.readLong();
        this.mDisplayName = in.readString();
        this.mSenderType = in.readString();
        this.mContentType = in.readString();
        this.mContentEncoding = in.readString();
        this.mQos = in.readInt();
        this.mTopic = in.readString();
    }

    public static final Creator<MASMessage> CREATOR = new Creator<MASMessage>() {
        @Override
        public MASMessage createFromParcel(Parcel source) {
            return new MASMessage(source);
        }

        @Override
        public MASMessage[] newArray(int size) {
            return new MASMessage[size];
        }
    };
}

