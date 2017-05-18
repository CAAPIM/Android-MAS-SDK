/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.common;

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

}
