/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.identity.group;

import android.net.Uri;
import android.support.annotation.Keep;
import android.text.TextUtils;
import android.util.Log;

import com.ca.mas.foundation.FoundationConsts;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASGroup;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASRequestBody;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.MASResponseBody;
import com.ca.mas.foundation.notify.Callback;
import com.ca.mas.identity.common.MASFilteredRequest;
import com.ca.mas.identity.util.IdentityConsts;
import com.ca.mas.identity.util.IdentityUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * The <i>GroupIdentityManager</i> behaves as the controller between the MAS SDK and the
 * {@link <a href="https://tools.ietf.org/html/rfc7643">SCIM</a>} service. The GroupIdentityManager performs the same type
 * of operations as the {@link com.ca.mas.identity.user.MASUserRepository} does for users. A MASGroup can be an enterprise or an
 * ad-hoc group. The difference is that an enterprise group cannot be created, updated, or deleted while and ad-hoc group can be created,
 * updated, or delete by the group's {@link MASOwner}.
 */
@Keep
public class MASGroupRepositoryImpl implements MASGroupRepository {

    @Override
    public void getGroupById(String id, final MASCallback<MASGroup> callback) {
        MASRequest masRequest = new MASRequest.MASRequestBuilder(Uri.parse(IdentityUtil.getGroupPath()
                + FoundationConsts.FSLASH + id))
                .header(IdentityConsts.HEADER_KEY_ACCEPT, IdentityConsts.HEADER_VALUE_ACCEPT)
                .header(IdentityConsts.HEADER_KEY_CONTENT_TYPE, IdentityConsts.HEADER_VALUE_CONTENT_TYPE)
                .responseBody(MASResponseBody.jsonBody())
                .get()
                .build();

        MAS.invoke(masRequest, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                try {
                    Callback.onSuccess(callback, processGroupById(result.getBody().getContent()));
                } catch (JSONException je) {
                    Callback.onError(callback, je);
                }
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
                .responseBody(MASResponseBody.jsonBody())
                .get()
                .build();

        MAS.invoke(masRequest, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                try {
                    Callback.onSuccess(callback, parse(result.getBody().getContent()));
                } catch (JSONException je) {
                    Callback.onError(callback, je);
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
            MASRequest masRequest = new MASRequest.MASRequestBuilder(Uri.parse(IdentityUtil.getGroupPath()))
                    .header(IdentityConsts.HEADER_KEY_ACCEPT, IdentityConsts.HEADER_VALUE_ACCEPT)
                    .header(IdentityConsts.HEADER_KEY_CONTENT_TYPE, IdentityConsts.HEADER_VALUE_CONTENT_TYPE)
                    .responseBody(MASResponseBody.jsonBody())
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
                        Callback.onError(callback, je);
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

    @Override
    public void delete(MASGroup group, final MASCallback<Void> callback) {
        String url = IdentityUtil.getGroupPath() + IdentityConsts.FSLASH + group.getId();
        MASRequest masRequest = new MASRequest.MASRequestBuilder(Uri.parse(url))
                .header(IdentityConsts.HEADER_KEY_ACCEPT, IdentityConsts.HEADER_VALUE_ACCEPT)
                .header(IdentityConsts.HEADER_KEY_CONTENT_TYPE, IdentityConsts.HEADER_VALUE_CONTENT_TYPE)
                .responseBody(MASResponseBody.jsonBody())
                .delete(null)
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
    }

    @Override
    public void getGroupMetaData(final MASCallback<GroupAttributes> callback) {
        String schemaPath = IdentityUtil.getSchemasPath() + FoundationConsts.FSLASH;
        MASRequest masRequest = new MASRequest.MASRequestBuilder(Uri.parse(schemaPath
                + IdentityConsts.SCHEMA_GROUP))
                .responseBody(MASResponseBody.jsonBody())
                .get()
                .build();

        MAS.invoke(masRequest, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                try {
                    Callback.onSuccess(callback, doPopulateAttributes(result.getBody().getContent()));
                } catch (JSONException e) {
                    Callback.onError(callback, e);
                }
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    @Override
    public void save(MASGroup group, MASCallback<MASGroup> callback) {
        if (group.getId() == null) {
            createAdHocGroup(group, callback);
        } else {
            updateAdHocGroup(group, callback);
        }
    }

    private GroupAttributes doPopulateAttributes(JSONObject jsonObject) throws JSONException {
        String id = jsonObject.optString(IdentityConsts.KEY_ID);
        if (TextUtils.isEmpty(id)) {
            throw new IllegalArgumentException("The ID cannot be null!");
        }

        // -------- GROUP ATTRIBUTES ------------------------------
        if (id.equals(IdentityConsts.SCHEMA_GROUP)) {
            GroupAttributes groupAttributes = new GroupAttributes();
            groupAttributes.populate(jsonObject);
            return groupAttributes;
        } else {
            return null;
        }
    }

    private void groupUpdate(final MASGroup group, final MASCallback<MASGroup> callback) {
        try {
            String updateUrl = IdentityUtil.getGroupPath() + FoundationConsts.FSLASH + group.getId();
            MASRequest masRequest = new MASRequest.MASRequestBuilder(Uri.parse(updateUrl))
                    .header(IdentityConsts.HEADER_KEY_ACCEPT, IdentityConsts.HEADER_VALUE_ACCEPT)
                    .header(IdentityConsts.HEADER_KEY_CONTENT_TYPE, IdentityConsts.HEADER_VALUE_CONTENT_TYPE)
                    .responseBody(MASResponseBody.jsonBody())
                    .put(MASRequestBody.jsonBody(group.getAsJSONObject()))
                    .build();

            MAS.invoke(masRequest, new MASCallback<MASResponse<JSONObject>>() {
                @Override
                public void onSuccess(MASResponse<JSONObject> result) {
                    try {
                        MASGroup updated = MASGroup.newInstance();
                        updated.populate(result.getBody().getContent());
                        Callback.onSuccess(callback, updated);
                    } catch (JSONException e) {
                        Callback.onError(callback, e);
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
        if (DEBUG) Log.d(TAG,
                String.format("Group raw JSON data: %s", jsonObject.toString(4)));
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

    private List<MASGroup> parse(JSONObject jsonObject) throws JSONException {

        List<MASGroup> result = new ArrayList<>();
        // get the array 'Resources'
        if (jsonObject.has(IdentityConsts.KEY_RESOURCES)) {
            JSONArray jsonArray = jsonObject.getJSONArray(IdentityConsts.KEY_RESOURCES);
            // <i>setTotalResults</i> can be called repeatedly. Calling it with
            // the same value that it is currently set does not alter the functionality.
            //int totalResults = jsonObject.optInt(IdentityConsts.KEY_TOTAL_RESULTS);
            if (jsonArray.length() > 0) {

                // iterate through the array, creating a user for each entry
                for (int i = 0; i < jsonArray.length(); i++) {
                    MASGroup ident = MASGroup.newInstance();
                    JSONObject arrElem = jsonArray.getJSONObject(i);
                    ident.populate(arrElem);
                    result.add(ident);
                }
            }
        }
        return result;
    }


}
