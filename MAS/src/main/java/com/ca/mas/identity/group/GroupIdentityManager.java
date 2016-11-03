/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.group;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.http.MAGResponseBody;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASException;
import com.ca.mas.foundation.MASGroup;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASRequestBody;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.notify.Callback;
import com.ca.mas.foundation.util.FoundationConsts;
import com.ca.mas.identity.common.MASFilteredRequest;
import com.ca.mas.identity.common.MASFilteredRequestBuilder;
import com.ca.mas.identity.util.IdentityConsts;
import com.ca.mas.identity.util.IdentityUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The <i>GroupIdentityManager</i> behaves as the controller between the MAS SDK and the
 * {@link <a href="https://tools.ietf.org/html/rfc7643">SCIM</a>} service. The GroupIdentityManager performs the same type
 * of operations as the {@link com.ca.mas.identity.user.UserIdentityManager} does for users. A MASGroup can be an enterprise or an
 * ad-hoc group. The difference is that an enterprise group cannot be created, updated, or deleted while and ad-hoc group can be created,
 * updated, or delete by the group's {@link MASOwner}.
 */
public class GroupIdentityManager implements MASGroupIdentity {

    private static String TAG = GroupIdentityManager.class.getSimpleName();

    private static GroupIdentityManager instance = new GroupIdentityManager();

    private GroupIdentityManager() {
    }

    public static GroupIdentityManager getInstance() {
        return instance;
    }

    @Override
    public void getGroupById(String id, final MASCallback<MASGroup> callback) {
        MASRequest masRequest = new MASRequest.MASRequestBuilder(Uri.parse(IdentityUtil.getGroupPath(MAS.getContext())
                + FoundationConsts.FSLASH + id))
                .header(IdentityConsts.HEADER_KEY_ACCEPT, IdentityConsts.HEADER_VALUE_ACCEPT)
                .header(IdentityConsts.HEADER_KEY_CONTENT_TYPE, IdentityConsts.HEADER_VALUE_CONTENT_TYPE)
                .responseBody(MAGResponseBody.jsonBody())
                .get()
                .build();

        MAS.invoke(masRequest, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                try {
                    Callback.onSuccess(callback, processGroupById(result.getBody().getContent()));
                } catch (JSONException je) {
                    onError(new MAGError(je));
                }
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    @Override
    public void getGroupByMember(MASUser member, final MASCallback<List<MASGroup>> callback){
        final String userId = member.getId();

        getGroupMetaData(new MASCallback<GroupAttributes>() {
            @Override
            public void onSuccess(GroupAttributes attributes) {
                List<String> attrs = attributes.getAttributes();
                MASFilteredRequest frb = new MASFilteredRequest(attrs, IdentityConsts.KEY_GROUP_ATTRIBUTES);
                frb.isEqualTo("members.value", userId);
                frb.setPagination(IdentityConsts.INDEX_START, IdentityConsts.PAGE_SIZE);
                getGroupsByFilter(frb, callback);
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    @Override
    public void getAllGroups(String userId, MASCallback<List<MASGroup>> callback) {
        getGroups(IdentityConsts.KEY_GROUPS_BY_OWNER, userId, callback);
    }

    @Override
    public void getGroupByGroupName(String groupName, MASCallback<List<MASGroup>> callback) {
        getGroups(IdentityConsts.KEY_GROUPS_BY_NAME, groupName, callback);
    }

    private void getGroups(final String key, final String value, final MASCallback<List<MASGroup>> callback) {

        getGroupMetaData(new MASCallback<GroupAttributes>() {
            @Override
            public void onSuccess(GroupAttributes attributes) {
                List<String> attrs = attributes.getAttributes();
                MASFilteredRequest frb = new MASFilteredRequest(attrs, IdentityConsts.KEY_GROUP_ATTRIBUTES);
                frb.isEqualTo(key, value);
                frb.setPagination(IdentityConsts.INDEX_START, IdentityConsts.PAGE_SIZE);
                frb.setSortOrder(MASFilteredRequestBuilder.SortOrder.descending, key);
                getGroupsByFilter(frb, callback);
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    @Override
    public void getGroupsByFilter(final MASFilteredRequest filteredRequest, final MASCallback<List<MASGroup>> callback) {
        Uri uri = filteredRequest.createUri(MAS.getContext());
        MASRequest masRequest = new MASRequest.MASRequestBuilder(uri)
                .header(IdentityConsts.HEADER_KEY_ACCEPT, IdentityConsts.HEADER_VALUE_ACCEPT)
                .header(IdentityConsts.HEADER_KEY_CONTENT_TYPE, IdentityConsts.HEADER_VALUE_CONTENT_TYPE)
                .responseBody(MAGResponseBody.jsonBody())
                .get()
                .build();

        MAS.invoke(masRequest, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                try {
                    List<MASGroup> container = new ArrayList<>();
                    JSONObject jsonObject = result.getBody().getContent();
                    processGroupsByFilter(filteredRequest, jsonObject, container, callback);
                } catch (Exception e) {
                    onError(new MAGError(e));
                }
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    private void createAdHocGroup(MASGroup group, final MASCallback<MASGroup> callback) {
        try {
            MASRequest masRequest = new MASRequest.MASRequestBuilder(Uri.parse(IdentityUtil.getGroupPath(MAS.getContext())))
                    .header(IdentityConsts.HEADER_KEY_ACCEPT, IdentityConsts.HEADER_VALUE_ACCEPT)
                    .header(IdentityConsts.HEADER_KEY_CONTENT_TYPE, IdentityConsts.HEADER_VALUE_CONTENT_TYPE)
                    .responseBody(MAGResponseBody.jsonBody())
                    .post(MASRequestBody.jsonBody(group.getAsJSONObject()))
                    .build();

            MAS.invoke(masRequest, new MASCallback<MASResponse<JSONObject>>() {
                @Override
                public void onSuccess(MASResponse<JSONObject> result) {
                    MASGroup group = MASGroup.newInstance();
                    try {
                        group.populate(result.getBody().getContent());
                        Callback.onSuccess(callback, group);
                    } catch (JSONException je) {
                        onError(new MAGError(je));
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Callback.onError(callback, e);
                }
            });
        } catch (JSONException e) {
            Callback.onError(callback, e);
        }
    }

    private void updateAdHocGroup(MASGroup group, MASCallback<MASGroup> callback) {
        groupUpdate(group, callback);
    }

    public void deleteAdHocGroup(MASGroup group, final MASCallback<Void> callback) {
        try {
            String url = IdentityUtil.getGroupPath(MAS.getContext()) + IdentityConsts.FSLASH + group.getId();
            MASRequest masRequest = new MASRequest.MASRequestBuilder(Uri.parse(url))
                    .header(IdentityConsts.HEADER_KEY_ACCEPT, IdentityConsts.HEADER_VALUE_ACCEPT)
                    .header(IdentityConsts.HEADER_KEY_CONTENT_TYPE, IdentityConsts.HEADER_VALUE_CONTENT_TYPE)
                    .responseBody(MAGResponseBody.jsonBody())
                    .delete(MASRequestBody.jsonBody(group.getAsJSONObject()))
                    .build();

            MAS.invoke(masRequest, new MASCallback<MASResponse<JSONObject>>() {
                @Override
                public void onSuccess(MASResponse<JSONObject> result) {
                    Callback.onSuccess(callback, null);
                }

                @Override
                public void onError(Throwable e) {
                    Callback.onError(callback, e);
                }
            });
        } catch (JSONException e) {
            Callback.onError(callback, e);
        }
    }

    @Override
    public void getGroupMetaData(final MASCallback<GroupAttributes> callback) {
        String schemaPath = IdentityUtil.getSchemasPath(MAS.getContext()) + FoundationConsts.FSLASH;
        MASRequest masRequest = new MASRequest.MASRequestBuilder(Uri.parse(schemaPath
                + IdentityConsts.SCHEMA_GROUP))
                .responseBody(MAGResponseBody.jsonBody())
                .get()
                .build();

        MAS.invoke(masRequest, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                try {
                    Callback.onSuccess(callback, doPopulateAttributes(result.getBody().getContent()));
                } catch (MASException me) {
                    onError(new MAGError(me));
                }
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    public void save(MASGroup group, MASCallback<MASGroup> callback) {
        if (group.getId() == null) {
            createAdHocGroup(group, callback);
        } else {
            updateAdHocGroup(group, callback);
        }
    }

    private GroupAttributes doPopulateAttributes(JSONObject jsonObject) throws MASException {
        String id = jsonObject.optString(IdentityConsts.KEY_ID);
        if (TextUtils.isEmpty(id)) {
            throw new MASException("The ID cannot be null!");
        }

        // -------- GROUP ATTRIBUTES ------------------------------
        try {
            if (id.equals(IdentityConsts.SCHEMA_GROUP)) {
                GroupAttributes groupAttributes = new GroupAttributes();
                groupAttributes.populate(jsonObject);
                return groupAttributes;
            } else {
                return null;
            }
        } catch (JSONException je) {
            throw new MASException(je);
        }
    }

    private void groupUpdate(final MASGroup group, final MASCallback<MASGroup> callback) {
        try {
            String updateUrl = IdentityUtil.getGroupPath(MAS.getContext()) + FoundationConsts.FSLASH + group.getId();
            MASRequest masRequest = new MASRequest.MASRequestBuilder(Uri.parse(updateUrl))
                    .header(IdentityConsts.HEADER_KEY_ACCEPT, IdentityConsts.HEADER_VALUE_ACCEPT)
                    .header(IdentityConsts.HEADER_KEY_CONTENT_TYPE, IdentityConsts.HEADER_VALUE_CONTENT_TYPE)
                    .responseBody(MAGResponseBody.jsonBody())
                    .put(MASRequestBody.jsonBody(group.getAsJSONObject()))
                    .build();

            MAS.invoke(masRequest, new MASCallback<MASResponse<JSONObject>>() {
                @Override
                public void onSuccess(MASResponse<JSONObject> result) {
                    try {
                        MASGroup updated = MASGroup.newInstance();
                        updated.populate(result.getBody().getContent());
                        Callback.onSuccess(callback, updated);
                    } catch (JSONException je) {
                        onError(new MAGError(je));
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Callback.onError(callback, e);
                }
            });
        } catch (JSONException e) {
            Callback.onError(callback, e);
        }
    }

    private MASGroup processGroupById(JSONObject jsonObject) throws JSONException {
        Log.d(TAG, "JSONObject: " + jsonObject.toString());
        // should only be 1 user
        MASGroup group = MASGroup.newInstance();
        // get the array 'Resources'
        if (jsonObject.has(IdentityConsts.KEY_RESOURCES)) {
            JSONArray jsonArray = jsonObject.getJSONArray(IdentityConsts.KEY_RESOURCES);

            // <i>setTotalResults</i> can be called repeatedly. Calling it with
            // the same value that it is currently set does not alter the functionality.
            int totalResults = jsonObject.optInt(IdentityConsts.KEY_TOTAL_RESULTS);
            if (totalResults != 1) {
                throw new IllegalStateException("Should not return more than 1 group");
            }
            // extract the object from the JSONArray - verified that there is only 1 by this point
            JSONObject arrElem = jsonArray.getJSONObject(0);
            group.populate(arrElem);
        } else {
            group.populate(jsonObject);
        }
        return group;
    }

    private void getGroups(final MASFilteredRequest filteredRequest, final List<MASGroup> container, final MASCallback<List<MASGroup>> callback) throws MASException {
        Uri uri = filteredRequest.createUri(MAS.getContext());
        MASRequest masRequest = new MASRequest.MASRequestBuilder(uri)
                .header(IdentityConsts.HEADER_KEY_ACCEPT, IdentityConsts.HEADER_VALUE_ACCEPT)
                .header(IdentityConsts.HEADER_KEY_CONTENT_TYPE, IdentityConsts.HEADER_VALUE_CONTENT_TYPE)
                .responseBody(MAGResponseBody.jsonBody())
                .get()
                .build();

        MAS.invoke(masRequest, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                try {
                    processGroupsByFilter(filteredRequest, result.getBody().getContent(), container, callback);
                } catch (Exception e) {
                    Callback.onError(callback, e);
                }
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });

    }

    private void processGroupsByFilter(MASFilteredRequest filteredRequest, JSONObject jsonObject, List<MASGroup> container, MASCallback<List<MASGroup>> callback) throws JSONException, MASException {

        // get the array 'Resources'
        if (jsonObject.has(IdentityConsts.KEY_RESOURCES)) {
            JSONArray jsonArray = jsonObject.getJSONArray(IdentityConsts.KEY_RESOURCES);
            // <i>setTotalResults</i> can be called repeatedly. Calling it with
            // the same value that it is currently set does not alter the functionality.
            int totalResults = jsonObject.optInt(IdentityConsts.KEY_TOTAL_RESULTS);
            if (jsonArray.length() > 0) {
                filteredRequest.setTotalResults(totalResults);

                // iterate through the array, creating a user for each entry
                for (int i = 0; i < jsonArray.length(); i++) {
                    MASGroup ident = MASGroup.newInstance();
                    JSONObject arrElem = jsonArray.getJSONObject(i);
                    ident.populate(arrElem);
                    container.add(ident);
                }// && totalResults == mGroupFilteredRequestBuilder.getCount()
                if (filteredRequest.hasNext()) {
                    getGroups(filteredRequest, container, callback);
                }
            }
        }
        Callback.onSuccess(callback, container);
    }

}
