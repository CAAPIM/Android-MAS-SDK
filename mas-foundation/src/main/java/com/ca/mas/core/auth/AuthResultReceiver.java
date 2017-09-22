/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.auth;

import org.json.JSONObject;

/**
 * Abstract interface for cross device result receiver.
 * Use the result receiver under {@link com.ca.mas.core.MobileSso#authorize}
 */
public interface AuthResultReceiver {

    void setData(JSONObject data);
}
