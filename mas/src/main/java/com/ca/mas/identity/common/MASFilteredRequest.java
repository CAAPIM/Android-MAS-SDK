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
import android.text.TextUtils;
import android.util.Log;

import com.ca.mas.foundation.MASRequest;
import com.ca.mas.identity.util.IdentityConsts;
import com.ca.mas.identity.util.IdentityUtil;

import java.util.ArrayList;
import java.util.List;

import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

/**
 * <p><b>MASFilteredRequest</b> describes the APIs for user's to specify scim filters. This interface would be the backing set of APIs
 * so that an application developer could provide a simple query system for identity management.</p>
 */
public class MASFilteredRequest implements MASFilteredRequestBuilder, MASPagination {

    private int mStartIndex;
    private int mCount;
    boolean mIsPaging;
    String mSortUri;
    String mQueryCondition;
    List<String> mQueryComponents;
    private Uri uri;
    protected List<String> mAttributes;
    protected List<String> mExcludedAttributes;
    private List<String> mEntityAttributes;
    private String mFilterType;

    /**
     * <b>Constructor Type: </b> Default no-args.<br>
     * <b>Description: </b> Creates an object that initializes the class, only. Pagination and attributes are
     * not available with this object.
     */
    public MASFilteredRequest(List<String> entityAttributes, String filterType) {
        mEntityAttributes = entityAttributes;
        mFilterType = filterType;
        mStartIndex = MASPagination.PAGE_START_INDEX;
        mCount = MASPagination.PAGE_NO_PAGINATION;
        mIsPaging = false;
    }

    @Override
    public MASFilteredRequestBuilder setAttributes(List<String> attributes) {
        if(mExcludedAttributes != null) {
            throw new IllegalArgumentException("attributes and excludedAttributes are mutually exclusive.");
        }
        mAttributes = attributes;
        return this;
    }

    @Override
    public MASFilteredRequestBuilder setExcludedAttributes(List<String> excludedAttributes) {
        if(mAttributes != null) {
            throw new IllegalArgumentException("attributes and excludedAttributes are mutually exclusive.");
        }
        mExcludedAttributes = excludedAttributes;
        return this;
    }

    @Override
    public MASFilteredRequestBuilder setPagination(int start, int count) {
        mStartIndex = start;
        mCount = count;
        if (mCount > 0) {
            mIsPaging = true;
        }
        return this;
    }

    @Override
    public MASFilteredRequestBuilder setSortOrder(SortOrder sortOrder, @NonNull String attribute) {
        if (sortOrder == null) {
            sortOrder = SortOrder.ascending;
        }
        mSortUri = sortByUri(attribute, sortOrder);
        return this;
    }

    @Override
    public MASFilteredRequestBuilder isEqualTo(@NonNull String attribute, @NonNull String filterValue) {
        mQueryCondition = create(Operator.eq, attribute, filterValue);
        return this;
    }

    @Override
    public MASFilteredRequestBuilder isNotEqualTo(@NonNull String attribute, @NonNull String filterValue) {
        mQueryCondition = create(Operator.ne, attribute, filterValue);
        return this;
    }

    @Override
    public MASFilteredRequestBuilder contains(@NonNull String attribute, @NonNull String filterValue) {
        mQueryCondition = create(Operator.co, attribute, filterValue);
        return this;
    }

    @Override
    public MASFilteredRequestBuilder startsWith(@NonNull String attribute, @NonNull String filterValue) {
        mQueryCondition = create(Operator.sw, attribute, filterValue);
        return this;
    }

    @Override
    public MASFilteredRequestBuilder endsWith(@NonNull String attribute, @NonNull String filterValue) {
        mQueryCondition = create(Operator.ew, attribute, filterValue);
        return this;
    }

    @Override
    public MASFilteredRequestBuilder isPresent(@NonNull String attribute) {
        mQueryCondition = create(Operator.pr, attribute, null);
        return this;
    }

    @Override
    public MASFilteredRequestBuilder isGreaterThan(@NonNull String attribute, @NonNull String filterValue) {
        mQueryCondition = create(Operator.gt, attribute, filterValue);
        return this;
    }

    @Override
    public MASFilteredRequestBuilder isGreaterThanOrEqual(@NonNull String attribute, @NonNull String filterValue) {
        mQueryCondition = create(Operator.ge, attribute, filterValue);
        return this;
    }

    @Override
    public MASFilteredRequestBuilder isLessThan(@NonNull String attribute, @NonNull String filterValue) {
        mQueryCondition = create(Operator.lt, attribute, filterValue);
        return this;
    }

    @Override
    public MASFilteredRequestBuilder isLessThanOrEqual(@NonNull String attribute, @NonNull String filterValue) {
        mQueryCondition = create(Operator.le, attribute, filterValue);
        return this;
    }

    @Override
    public MASFilteredRequestBuilder createCompoundExpression(@NonNull Logical logical, MASRequest lhs, MASRequest rhs) {
        FilterFormatter filterFormatter = new FilterFormatter(logical, lhs, rhs);
        mQueryCondition = filterFormatter.toString();
        return this;
    }

    @Override
    public Uri createUri(@NonNull Context context) {
        // another request...
        if (uri != null) {
            return uri;
        }

        mQueryComponents = new ArrayList<>();
        StringBuilder fullUrl = new StringBuilder();
        if(mFilterType.equals(IdentityConsts.KEY_USER_ATTRIBUTES)) {
            fullUrl.append(IdentityUtil.getUserPath(context));
        }
        if(mFilterType.equals(IdentityConsts.KEY_GROUP_ATTRIBUTES)) {
            fullUrl.append(IdentityUtil.getGroupPath(context));
        }

        if(!TextUtils.isEmpty(mQueryCondition)) {
            mQueryComponents.add(mQueryCondition);
        }

        if (!TextUtils.isEmpty(mSortUri)) {
            mQueryComponents.add(mSortUri);
        }

        if (mAttributes != null) {
            // find the query string
            mQueryComponents.add(createNormalizedAttributes(mAttributes, IdentityConsts.KEY_ATTRIBUTES));
        }

        if(mExcludedAttributes != null) {
            // find the query string
            mQueryComponents.add(createNormalizedAttributes(mExcludedAttributes, IdentityConsts.KEY_EXCLUDED_ATTRIBUTES));
        }

        // Only add the pagination if this request requires paging.
        // This is added right at the end.
        if (mIsPaging) {
            String pagFilter = getPaginationFilter();
            if (pagFilter != null) {
                mQueryComponents.add(pagFilter);
            }
        }

        if(mQueryComponents.size()>0){
            fullUrl.append(IdentityConsts.QM);
        }

        for (int i = 0; i < mQueryComponents.size(); i++) {
            fullUrl.append(mQueryComponents.get(i));
            if (i < mQueryComponents.size() - 1) {
                fullUrl.append(IdentityConsts.AMP);
            }
        }


        String encUrl = fullUrl.toString().replaceAll(" ", IdentityConsts.ENC_SPACE);
        encUrl = encUrl.replaceAll("\"", IdentityConsts.ENC_DOUBLE_QUOTE);
        if (DEBUG) Log.d(TAG, "Encoded URL: " + encUrl);
        uri = Uri.parse(encUrl);
        return uri;
    }

    private String createNormalizedAttributes(List<String> attrs, String key) {
        List<String> normalizedAttributes = IdentityUtil.normalizeAttributes(attrs, mEntityAttributes);
        StringBuilder sb = new StringBuilder();
        sb.append(key);
        sb.append(IdentityConsts.EQ);
        for (int i = 0; i < normalizedAttributes.size(); i++) {
            sb.append(normalizedAttributes.get(i));
            if (i < normalizedAttributes.size() - 1) {
                sb.append(IdentityConsts.COMMA);
            }
        }
        return sb.toString();
    }

    /*
    Create the Query String pagination expression:
    startIndex=1&count=10, for example.
    */
    private String getPaginationFilter() {
        // we have gotten all available
        String start = String.format(MASPagination.PAGE_START, mStartIndex);
        String incBy = String.format(MASPagination.PAGE_INC_BY, mCount);
        String pagFilter = start + incBy;
        mStartIndex += mCount;
        return pagFilter;
    }

    private String create(Operator op, String attribute, String filterValue) {
        FilterFormatter filterFormatter = new FilterFormatter(op, attribute, filterValue);
        return filterFormatter.toString();
    }

    private String sortByUri(String attribute, SortOrder sortOrder) {
        StringBuilder sb = new StringBuilder();
        String sortBy = String.format(SORT_BY, attribute);
        sb.append(sortBy);
        sb.append(IdentityConsts.AMP);
        String so = String.format(SORT_ORDER, sortOrder.toString());
        sb.append(so);
        return sb.toString();
    }
}
