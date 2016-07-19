/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.messaging.topic;

import com.ca.mas.connecta.client.MASConnectaClient;
import com.ca.mas.connecta.util.ConnectaConsts;
import com.ca.mas.foundation.MASConstants;
import com.ca.mas.foundation.MASException;

/**
 * <p><b>MASTopicBuilder</b> is the concrete implementation of the MASTopic interface and provides structured topic creation through
 * the <i>createTopic</i> method. </p>
 */
public class MASTopicBuilder {

    private String mUserId;
    private
    @MASMessagingSegment
    int mSegment = MASConstants.MAS_USER | MASConstants.MAS_APPLICATION;
    private String mCustomTopic;
    private int mQos = MASConnectaClient.EXACTLY_ONCE;

    /**
     * <b>Description:</b> The only constructor.
     */
    public MASTopicBuilder() {
    }

    /**
     * <b>Description:</b> Setter.
     *
     * @param qos the quality of service in the range [0, 2].
     * @return MASTopicBuilder the instance of this builder.
     * @throws MASException is thrown if the qos value is not in the correct range.
     */
    public MASTopicBuilder setQos(int qos) throws MASException {
        if (qos < MASConnectaClient.AT_MOST_ONCE || qos > MASConnectaClient.EXACTLY_ONCE) {
            throw new MASException("Only QoS 0, 1, and 2 are supported.");
        }
        mQos = qos;
        return this;
    }

    /**
     * <b>Description:</b> Builder.
     *
     * @param userId the userId for this topic.
     * @return MASTopicBuilder the instance of this builder.
     * @throws MASException if the builder user, group, app, or device ids are not mutually exclusive.
     */
    public MASTopicBuilder setUserId(String userId) throws MASException {
        mUserId = userId;
        return this;
    }

    /**
     * <b>Description:</b> Builder.
     *
     * @param segment The Messaging segment
     * @return MASTopicBuilder the instance of this builder.
     * @throws MASException if the builder user, group, app, or device ids are not mutually exclusive.
     */
    public MASTopicBuilder setMessagingSegment(@MASMessagingSegment int segment) throws MASException {
        this.mSegment = segment;
        return this;
    }


    /**
     * <b>Description:</b> Builder.
     *
     * @param customTopic the custom topic token following the .../custom/ part of the structured topic.
     * @return MASTopicBuilder the instance of this builder.
     */
    public MASTopicBuilder setCustomTopic(String customTopic) {
        mCustomTopic = customTopic;
        return this;
    }

    /**
     * Build the {@link MASTopic} instance
     */
    public MASTopic build() {
        if (mCustomTopic == null) {
            throw new IllegalArgumentException("Custom name is required");
        }
        if (mUserId == null &&
                (mSegment != MASConstants.MAS_APPLICATION)) {
            throw new IllegalArgumentException("User id is required");
        }
        return new MASTopic() {

            @Override
            public int getQos() {
                return mQos;
            }

            @Override
            public String toString() {
                String s = createTopic();
                return s;
            }
        };
    }

    private String createTopic() {
        switch (mSegment) {
            case MASConstants.MAS_USER:
                return String.format("/%s/users/%s/custom/%s", ConnectaConsts.TOPIC_VERSION_ORG, mUserId, mCustomTopic);
            case MASConstants.MAS_USER | MASConstants.MAS_APPLICATION:
                return String.format("/%s/client/users/%s/custom/%s", ConnectaConsts.TOPIC_VERSION_ORG, mUserId, mCustomTopic);
            case MASConstants.MAS_APPLICATION:
                return String.format("/%s/client/custom/%s", ConnectaConsts.TOPIC_VERSION_ORG, mCustomTopic);
            default:
                throw new IllegalArgumentException("Messaging segment is not supported");
        }
    }
}
