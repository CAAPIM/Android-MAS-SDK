/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.common;

import android.support.annotation.NonNull;

import com.ca.mas.foundation.web.WebServiceRequest;
import com.ca.mas.identity.util.IdentityConsts;

/**
 * <p><b>FileFormatter</b> is a helper class for creating SCIM Identity Management expressions for querying.</p>
 * <p>NOTE: The constructor <i>FilterFormatter(@NonNull MASFilteredRequest.Logical logical, @NonNull WebServiceRequest lhs, @NonNull WebServiceRequest rhs)</i>
 * is for future use. The current server side implementation does not support the logical operators but once supported, two {@link com.ca.mas.foundation.web.WebServiceRequest}s can be combined
 * using AND, OR, or NOT to create a single {@link com.ca.mas.foundation.web.WebServiceRequest} representing a complex SCIM Identity Management query.</p>
 */
class FilterFormatter {

    private MASFilteredRequest.Operator mOp;
    private MASFilteredRequest.Logical mLogical;
    private String mAttribute;
    private String mFilterValue;
    private String mLhs;
    private String mRhs;

    /**
     * <b>Description:</b> Convenience constructor.
     *
     * @param logical operators AND, OR, or NOT
     * @param lhs     the left hand side of the expression.
     * @param rhs     the right hand side of the expression.
     */
    FilterFormatter(@NonNull MASFilteredRequest.Logical logical, @NonNull WebServiceRequest lhs, @NonNull WebServiceRequest rhs) {
        mLogical = logical;
        mLhs = lhs.getUri().getQuery();
        mRhs = rhs.getUri().getQuery();
    }

    /**
     * <b>Description:</b> Convenience constructor.
     *
     * @param op          is one of the operators such as 'eq' or 'co'
     * @param attribute   is the supported attribute
     * @param filterValue is the value used in the query.
     */
    FilterFormatter(MASFilteredRequest.Operator op, String attribute, String filterValue) {
        mOp = op;
        mAttribute = attribute;
        mFilterValue = filterValue;
    }

    @Override
    public String toString() {
        if (mOp != null) {
            return toStringOperator();
        } else {
            return toStringLogical();
        }
    }

    private String toStringOperator() {
        StringBuilder sb = new StringBuilder();
        sb.append(IdentityConsts.KEY_FILTER);
        sb.append(IdentityConsts.EQ);
        sb.append(mAttribute);
        sb.append(IdentityConsts.ENC_SPACE);
        sb.append(mOp);
        if (mOp == MASFilteredRequest.Operator.pr) {
            return sb.toString();
        }
        sb.append(IdentityConsts.ENC_SPACE);
        sb.append(IdentityConsts.ENC_DOUBLE_QUOTE);
        sb.append(mFilterValue);
        sb.append(IdentityConsts.ENC_DOUBLE_QUOTE);
        return sb.toString();
    }

    private String toStringLogical() {
        StringBuilder sb = new StringBuilder();
        mLhs = retrieveQueryNoPag(mLhs);
        sb.append(mLhs);
        sb.append(IdentityConsts.ENC_SPACE);
        sb.append(mLogical.toString());
        sb.append(IdentityConsts.ENC_SPACE);
        mRhs = retrieveQueryNoPag(mRhs);
        sb.append(mRhs);
        return sb.toString();
    }

    private String retrieveQueryNoPag(String query) {
        int index = query.indexOf(MASPagination.PAGE_START_EXP);
        if (index > -1) {
            return query.substring(0, index);
        }
        return query;
    }
}
