/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.common;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.ca.mas.foundation.MASException;
import com.ca.mas.foundation.MASRequest;

import java.util.List;

/**
 * <p><b>MASFilteredRequestBuilder</b> defines all of the SCIM operations that are available for SCIM queries. Each Operator type is represented
 * by a specific method that requires only the attribute(s) and the value in order to create a well-formed representation of the
 * {@link  MASRequest} serviceable by the SCIM server implementation.</p>
 */
public interface MASFilteredRequestBuilder {

    String SORT_BY = "sortBy=%s";
    String SORT_ORDER = "sortOrder=%s";

    /**
     * <b>Description:</b> The <b>Operator</b> enum are the SCIM binary and unary operators as described in the
     * SCIM Protocol reference section {<a href="https://tools.ietf.org/html/rfc7644#page-15">3.4.4.2 Filtering</a>}.
     */
    enum Operator {
        eq, // equals to
        ne, // not equal to
        co, // contains
        sw, // starts with
        ew, // ends with
        pr, // unary: present
        gt, // greater than
        ge, // greater than or equal to
        lt, // less than
        le  // less than or equal to
    }

    /**
     * <b>Description:</b> The <b>Logical</b> enum are the SCIM logical operators as described in the
     * SCIM Protocol reference section {<a href="https://tools.ietf.org/html/rfc7644#page-15">3.4.4.2 Filtering</a>}.
     */
    enum Logical {
        and, // both conditions must be true
        or,  // either condition must be true
        not // a condition is not true
    }

    /**
     * <b>Description:</b> The <b>SortOrder</b> enum contains the sorting directives as described in the
     * SCIM Schema reference section {<a href="https://tools.ietf.org/html/rfc7644#section-3.4.2.3">3.4.2.3 Sorting</a>}.
     * The default order, if <i>sortBy</i> is used without a sortOrder is ascending.
     */
    enum SortOrder {
        ascending,
        descending
    }

    /**
     * <b>Pre-Conditions:</b> None.<br>
     * <b>Description:</b> Set the pagination start and the number of results per page.
     *
     * @param start the starting index that is greater than 0.
     * @param count the number of results per page.
     */
    MASFilteredRequestBuilder setPagination(int start, int count);

    /**
     * <b>Pre-Conditions:</b> None.<br>
     * <b>Description:</b> The list of attributes that should be returned in the result.
     *
     * @param attributes the list of allowable attributes.
     * @throws MASException if both attributes and excludedAttributes are trying to be set.
     */
    MASFilteredRequestBuilder setAttributes(List<String> attributes) throws MASException;

    /**
     * <b>Pre-Conditions:</b> None.<br>
     * <b>Description:</b> The list of attributes that should NOT be returned in the result.
     *
     * @param excludedAttributes
     * @throws MASException if both attributes and excludedAttributes are trying to be set.
     */
    MASFilteredRequestBuilder setExcludedAttributes(List<String> excludedAttributes) throws MASException;

    /**
     * <b>Description:</b> The Uri for the request is created based on the supplied filter(s).
     * @param context
     * @return
     */
    Uri createUri(@NonNull Context context);

    /**
     * <b>Description:</b> This method applies the filter <i>attribute <b>eq</b> filterValue</i>.
     *
     * @param attribute
     * @param filterValue
     */
    MASFilteredRequestBuilder isEqualTo(@NonNull String attribute, @NonNull String filterValue);

    /**
     * <b>Description:</b> This method applies the filter <i>attribute <b>ne</b> filterValue</i>.
     *
     * @param attribute
     * @param filterValue
     */
    MASFilteredRequestBuilder isNotEqualTo(@NonNull String attribute, @NonNull String filterValue);

    /**
     * <b>Description:</b> This method applies the filter <i>attribute <b>co</b> filterValue</i>.
     *
     * @param attribute
     * @param filterValue
     */
    MASFilteredRequestBuilder contains(@NonNull String attribute, @NonNull String filterValue);

    /**
     * <b>Description:</b> This method applies the filter <i>attribute <b>sw</b> filterValue</i>.
     *
     * @param attribute
     * @param filterValue
     */
    MASFilteredRequestBuilder startsWith(@NonNull String attribute, @NonNull String filterValue);

    /**
     * <b>Description:</b> This method applies the filter <i>attribute <b>ew</b> filterValue</i>.
     *
     * @param attribute
     * @param filterValue
     */
    MASFilteredRequestBuilder endsWith(@NonNull String attribute, @NonNull String filterValue);

    /**
     * <b>Description:</b> This method applies the unary filter <i>attribute</i> <b>pr</b>.
     *
     * @param attribute
     */
    MASFilteredRequestBuilder isPresent(@NonNull String attribute);

    /**
     * <b>Description:</b> This method applies the filter <i>attribute <b>gt</b> filterValue</i>.
     *
     * @param attribute
     * @param filterValue
     */
    MASFilteredRequestBuilder isGreaterThan(@NonNull String attribute, @NonNull String filterValue);

    /**
     * <b>Description:</b> This method applies the filter <i>attribute <b>ge</b> filterValue</i>.
     *
     * @param attribute
     * @param filterValue
     */
    MASFilteredRequestBuilder isGreaterThanOrEqual(@NonNull String attribute, @NonNull String filterValue);

    /**
     * <b>Description:</b> This method applies the filter <i>attribute <b>lt</b> filterValue</i>.
     *
     * @param attribute
     * @param filterValue
     */
    MASFilteredRequestBuilder isLessThan(@NonNull String attribute, @NonNull String filterValue);

    /**
     * <b>Description:</b> This method applies the filter <i>attribute <b>le</b> filterValue</i>.
     *
     * @param attribute
     * @param filterValue
     */
    MASFilteredRequestBuilder isLessThanOrEqual(@NonNull String attribute, @NonNull String filterValue);

    /**
     * <b>Description:</b> This method applies the filter <i>lhs <b>[and|or|not]</b> rhs</i>.
     *  @param logical One of the Logical operators - and, or, not.
     * @param lhs     A fully formed MASRequest. This is expected to use the Uri from the <i>createUri</i> method call.
     * @param rhs     A fully formed MASRequest. This is expected to use the Uri from the <i>createUri</i> method call.
     */
    MASFilteredRequestBuilder createCompoundExpression(Logical logical, MASRequest lhs, MASRequest rhs);

    /**
     * <b>Description:</b> This method applies the filter <i>sortBy = attribute&sortOrder=ascending</i> .
     *
     * @param sortOrder
     * @param attribute
     */
    MASFilteredRequestBuilder setSortOrder(SortOrder sortOrder, @NonNull String attribute);

    /**
     * <b>Description:</b> Is there another page to retrieve?.
     *
     * @return boolean
     */
    boolean hasNext();

}
