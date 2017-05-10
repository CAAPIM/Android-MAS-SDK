/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.request.internal;

import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGResponse;

import java.io.IOException;

/**
 * A Request that send to the SDK but not to the Gateway.
 */
public interface LocalRequest extends MAGRequest {

    MAGResponse send(MssoContext context) throws IOException;

}
