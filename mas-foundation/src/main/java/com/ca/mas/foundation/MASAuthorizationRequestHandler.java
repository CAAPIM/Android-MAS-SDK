/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

/**
 * Handle OAuth Authorization Code flow.
 */
public interface MASAuthorizationRequestHandler {

     /**
      * Authorize the OAuth authorization code flow request.
      * @param request The request contains OAuth Client information.
      */
     void authorize(MASAuthorizationRequest request);
}
