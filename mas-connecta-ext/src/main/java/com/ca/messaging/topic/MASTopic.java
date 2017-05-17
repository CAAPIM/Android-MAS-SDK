/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.messaging.topic;

/**
 * <p>The <b>MASTopic</b> interface enforces the topic properties available to the user.
 * The user should have access to;</p>
 * <ol>
 * <li>The topic - represented as a string value</li>
 * <li>The quality of service requested (qos) - the value in the range specified by the provider</li>
 * </ol>
 */
public interface MASTopic {

    /**
     * <b>Description:</b>
     * <ol>
     * <li>0 - at most once</li>
     * <li>1 - at least once</li>
     * <li>2 - exactly once</li>
     * </ol>
     *
     * @return int one of 0, 1, or 2 (the default).
     */
    int getQos();

}
