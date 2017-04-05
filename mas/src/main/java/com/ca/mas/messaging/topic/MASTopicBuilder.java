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
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.foundation.MASConstants;

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
    private boolean mEnforceTopicStructure = true;
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
     */
    public MASTopicBuilder setQos(int qos) {
        if (qos < MASConnectaClient.AT_MOST_ONCE || qos > MASConnectaClient.EXACTLY_ONCE) {
            throw new IllegalArgumentException("Only QoS 0, 1, and 2 are supported.");
        }
        mQos = qos;
        return this;
    }

    /**
     * <b>Description:</b> Builder.
     *
     * @param userId the userId for this topic.
     * @return MASTopicBuilder the instance of this builder.
     */
    public MASTopicBuilder setUserId(String userId) {
        mUserId = userId;
        return this;
    }

    /**
     * <b>Description:</b> Builder.
     *
     * @param segment The Messaging segment
     * @return MASTopicBuilder the instance of this builder.
     */
    public MASTopicBuilder setMessagingSegment(@MASMessagingSegment int segment) {
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
     * <b>Description:</b> Builder.
     *
     * @param enforce True to enforce topic structure which align to MAS Messaging protocol, false to allow free form of
     *                topic structure, set the free form topic structure using {@link #setCustomTopic(String)},
     *                other attributes which set using {@link #setUserId(String)}, {@link #setMessagingSegment(int)}
     *                will be ignored.
     *                By default, enforce topic structure is set to true.
     *
     * @return MASTopicBuilder the instance of this builder.
     */
    public MASTopicBuilder enforceTopicStructure(boolean enforce) {
        mEnforceTopicStructure = enforce;
        return this;
    }

    /**
     * Build the {@link MASTopic} instance
     */
    public MASTopic build() {
        if (mCustomTopic == null) {
            throw new IllegalArgumentException("Custom name is required");
        }
        if (mEnforceTopicStructure) {
            if (mUserId == null &&
                    (mSegment != MASConstants.MAS_APPLICATION)) {
                throw new IllegalArgumentException("User id is required");
            }
        }
        return new MASTopic() {

            @Override
            public int getQos() {
                return mQos;
            }

            @Override
            public String toString() {
                String topicStructure;
                if (mEnforceTopicStructure) {
                    topicStructure = createTopic();
                } else {
                    topicStructure = mCustomTopic;
                }
                return topicStructure;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                return o.toString().equals(toString());
            }
        };
    }

    private String createTopic() {
        String prefix = ConfigurationManager.getInstance()
                .getConnectedGatewayConfigurationProvider().getPrefix().trim();

        if (prefix.length() > 0)  {
            prefix = "/" + prefix;
        }

        switch (mSegment) {
            case MASConstants.MAS_USER:
                return String.format("%s/%s/users/%s/custom/%s", prefix, ConnectaConsts.TOPIC_VERSION_ORG, mUserId, mCustomTopic);
            case MASConstants.MAS_USER | MASConstants.MAS_APPLICATION:
                return String.format("%s/%s/client/users/%s/custom/%s", prefix, ConnectaConsts.TOPIC_VERSION_ORG, mUserId, mCustomTopic);
            case MASConstants.MAS_APPLICATION:
                return String.format("%s/%s/client/custom/%s", prefix, ConnectaConsts.TOPIC_VERSION_ORG, mCustomTopic);
            default:
                throw new IllegalArgumentException("Messaging segment is not supported");
        }
    }

}
