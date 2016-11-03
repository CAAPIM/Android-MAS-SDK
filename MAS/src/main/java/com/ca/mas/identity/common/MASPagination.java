/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.common;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASRequest;

/**
 * <p>For example, to retrieve the first 10 Users, set the startIndex to 1
 * and the count to 10:</p>
 * <pre>
 * GET /Users?startIndex=1&amp;count=10
 * Host: example.com
 * Accept: application/scim+json
 * Authorization: Bearer h480djs93hd8
 * </pre>
 * <p>
 * The response to the query above returns metadata regarding paging
 * similar to the following example (actual resources removed for
 * brevity):
 * </p>
 * <pre>
 * {
 *  "totalResults":100,
 *  "itemsPerPage":10,
 *  "startIndex":1,
 *  "schemas":["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
 *  "Resources":[{
 *      ...
 *  }]
 * }
 * </pre>
 * <p>Figure 3: ListResponse Format for Returning Multiple Resources</p>
 * <p>Given the above example, to continue paging, set the startIndex to 11
 * and re-fetch, i.e., /Users?startIndex=11&amp;count=10.</p>
 */
public interface MASPagination {

    int PAGE_START_INDEX = 1;
    int PAGE_NO_PAGINATION = 0;
    String PAGE_START_EXP = "startIndex=";
    String PAGE_START = PAGE_START_EXP + "%s";
    String PAGE_INC_BY = "&count=%s";

    /**
     * <b>Pre-Conditions:</b> The call to {@link com.ca.mas.foundation.MAS#invoke(MASRequest, MASCallback)}  } must have successfully responded.<br>
     * <b>Description: </b> <i>totalResults</i> is a JSON value returned from SCIM for each successful
     * response. This value provides a ceiling on the number of iterations, or calls to {@link MASFilteredRequest#hasNext()} that
     * are necessary to retrieve all the results. The default value for this attribute is {@link MASFilteredRequest#PAGE_START_INDEX},
     * however, if a user overrides this value prior to the response from {@link MASFilteredRequest#hasNext()} then that user
     * supplied value will be the number of items returned in total, regardless of how many are available.
     *
     * @param totalResults the number of results that are available.
     */
    MASFilteredRequestBuilder setTotalResults(int totalResults);
}
