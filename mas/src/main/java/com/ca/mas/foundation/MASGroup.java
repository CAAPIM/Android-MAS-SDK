/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.support.annotation.NonNull;

import com.ca.mas.foundation.notify.Callback;
import com.ca.mas.identity.common.MASFilteredRequest;
import com.ca.mas.identity.group.GroupAttributes;
import com.ca.mas.identity.group.GroupIdentityManager;
import com.ca.mas.identity.group.MASGroupIdentity;
import com.ca.mas.identity.group.MASMember;
import com.ca.mas.identity.group.MASOwner;
import com.ca.mas.identity.user.MASMeta;
import com.ca.mas.identity.util.IdentityConsts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>The <b>MASGroup</b> class is a local representation of group data.</p>
 */
public abstract class MASGroup implements MASTransformable, MASGroupIdentity {
    public static MASGroup newInstance() {

        return new MASGroup() {
            private String mId;
            private String mValue;
            private String mReference;
            private String mGroupName;
            private MASOwner mOwner;
            private MASMeta mMeta;
            private List<MASMember> mMembersList = new ArrayList<>();

            @Override
            public String getId() {
                return mId;
            }

            @Override
            public String getValue() {
                return mValue;
            }

            @Override
            public String getReference() {
                return mReference;
            }

            @Override
            public String getGroupName() {
                return mGroupName;
            }

            @Override
            public void setGroupName(String groupName) {
                mGroupName = groupName;
            }

            @Override
            public List<MASMember> getMembers() {
                return mMembersList;
            }

            @Override
            public MASOwner getOwner() {
                if (mOwner == null) {
                    return new MASOwner(MASUser.getCurrentUser());
                } else {
                    return mOwner;
                }
            }

            @Override
            public void populate(@NonNull JSONObject jobj) throws JSONException {
                mId = jobj.optString(IdentityConsts.KEY_ID);
                mGroupName = jobj.optString(IdentityConsts.KEY_DISPLAY_NAME);
                mValue = jobj.optString(IdentityConsts.KEY_VALUE);
                mReference = jobj.optString(IdentityConsts.KEY_REFERENCE);
                if (jobj.has(IdentityConsts.KEY_OWNER)) {
                    JSONObject ownerObj = jobj.getJSONObject(IdentityConsts.KEY_OWNER);
                    if (ownerObj != null && ownerObj.has(IdentityConsts.KEY_VALUE)) {
                        String id = ownerObj.getString(IdentityConsts.KEY_VALUE);
                        String ref = ownerObj.getString(IdentityConsts.KEY_REFERENCE);
                        String display = ownerObj.getString(IdentityConsts.KEY_DISPLAY);
                        mOwner = new MASOwner(id, ref, display);
                    }
                }

                if (jobj.has(IdentityConsts.KEY_MEMBERS)) {
                    JSONArray membersArr = jobj.getJSONArray(IdentityConsts.KEY_MEMBERS);
                    for (int i = 0; i < membersArr.length(); i++) {
                        JSONObject memberObj = membersArr.getJSONObject(i);
                        if (memberObj != null && memberObj.has(IdentityConsts.KEY_VALUE)) {
                            MASMember member = new MASMember(
                                    memberObj.getString(IdentityConsts.KEY_TYPE),
                                    memberObj.getString(IdentityConsts.KEY_VALUE),
                                    memberObj.getString(IdentityConsts.KEY_REFERENCE),
                                    memberObj.getString(IdentityConsts.KEY_DISPLAY));
                            addMember(member);
                        }
                    }
                }

                // meta
                if (jobj.has(IdentityConsts.KEY_META)) {
                    JSONObject metaObj = jobj.getJSONObject(IdentityConsts.KEY_META);
                    mMeta = new MASMeta();
                    mMeta.populate(metaObj);
                }
            }

            @Override
            public JSONObject getAsJSONObject() throws JSONException {
                JSONObject jobj = new JSONObject();
                JSONArray schemas = new JSONArray();
                schemas.put(IdentityConsts.SCHEMA_GROUP);
                jobj.put(IdentityConsts.KEY_SCHEMAS, schemas);
                jobj.put(IdentityConsts.KEY_DISPLAY_NAME, mGroupName);
                JSONObject owner = new JSONObject();
                owner.put(IdentityConsts.KEY_VALUE, getOwner().getValue());
                owner.put(IdentityConsts.KEY_DISPLAY, getOwner().getDisplay());
                jobj.put(IdentityConsts.KEY_OWNER, owner);
                JSONArray members = new JSONArray();
                for (MASMember m : mMembersList) {
                    JSONObject member = new JSONObject();
                    member.put(IdentityConsts.KEY_VALUE, m.getValue());
                    member.put(IdentityConsts.KEY_DISPLAY, m.getDisplay());
                    member.put(IdentityConsts.KEY_TYPE, m.getType());
                    members.put(member);
                }
                jobj.put(IdentityConsts.KEY_MEMBERS, members);
                return jobj;
            }

            private boolean isGroupMember(MASMember member) {
                for (MASMember m : mMembersList) {
                    if (m.getValue().equals(member.getValue())) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void addMember(MASMember member) {
                if (!isGroupMember(member)) {
                    mMembersList.add(member);
                }
            }

            @Override
            public void removeMember(MASMember member) {
                MASMember m = getGroupMember(member.getValue());
                if (m != null) {
                    mMembersList.remove(m);
                }
            }

            private MASMember getGroupMember(String id) {
                for (MASMember m : mMembersList) {
                    if (m.getValue().equals(id)) {
                        return m;
                    }
                }
                return null;
            }

            @Override
            public MASMeta getMeta() {
                return mMeta;
            }

            @Override
            public void save(MASCallback<MASGroup> callback) {
                GroupIdentityManager.getInstance().save(this, callback);
            }

            @Override
            public void delete(MASCallback<Void> callback) {
                GroupIdentityManager.getInstance().deleteAdHocGroup(this, callback);
            }

            @Override
            public void getAllGroups(String userId, final MASCallback<List<MASGroup>> callback) {
                if (MASUser.getCurrentUser() != null && MASUser.getCurrentUser().getUserName() != null) {
                    GroupIdentityManager.getInstance().getAllGroups(MASUser.getCurrentUser().getUserName(), callback);
                } else {
                    MASUser.login(new MASCallback<MASUser>() {
                        @Override
                        public void onSuccess(MASUser result) {
                            GroupIdentityManager.getInstance().getAllGroups(result.getUserName(), callback);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Callback.onError(callback, e);
                        }
                    });
                }
            }

            @Override
            public void getGroupByGroupName(String groupName, MASCallback<List<MASGroup>> callback) {
                GroupIdentityManager.getInstance().getGroupByGroupName(groupName, callback);
            }

            @Override
            public void getGroupById(String id, MASCallback<MASGroup> callback) {
                GroupIdentityManager.getInstance().getGroupById(id, callback);
            }

            @Override
            public void getGroupByMember(MASUser user, MASCallback<List<MASGroup>> callback) {
                GroupIdentityManager.getInstance().getGroupByMember(user, callback);
            }

            @Override
            public void getGroupsByFilter(MASFilteredRequest filteredRequest,
                                          MASCallback<List<MASGroup>> callback) {
                GroupIdentityManager.getInstance().getGroupsByFilter(filteredRequest, callback);
            }

            @Override
            public void getGroupMetaData(MASCallback<GroupAttributes> callback) {
                GroupIdentityManager.getInstance().getGroupMetaData(callback);
            }
        };
    }

    /**
     * <b>Description:</b> An operation to be applied to this MASGroup.
     */
    /* public */
    enum PatchOp {
        ADD, REMOVE, REPLACE
    }

    /**
     * <b>Description:</b> Getter for the group ID.
     *
     * @return String representing the unique ID.
     */
    public abstract String getId();

    /**
     * <b>Description:</b> Value accessor which could be a GUID,
     * e.g. 'e9e30dba-f08f-4109-8486-d5c6a331660a'.
     *
     * @return String representation of the value.
     */
    public abstract String getValue();

    /**
     * <b>Description:</b>
     * The reference is a URI that can be used to make a web service call to get the details of this group,
     * e.g. '../Groups/e9e30dba-f08f-4109-8486-d5c6a331660a'.
     *
     * @return String which is a URI representing a specific group.
     */
    public abstract String getReference();

    /**
     * <b>Description:</b> This method returns the group name used to display. For example, this attribute will
     * contain a value such as 'Employees', 'Tour Guides', etc.
     *
     * @return String which is a free-form user friendly display name.
     */
    public abstract String getGroupName();

    /**
     * <b>Description:</b> The groupName is stored as the 'displayName' in the SCIM data model.
     * This value is used as the group's display name when creating an adhoc group.
     *
     * @param groupName String representing this group's name.
     */
    public abstract void setGroupName(String groupName);

    /**
     * <b>Description:</b> Getter.
     *
     * @return The user ID representing the owner of this group.
     */
    public abstract MASOwner getOwner();

    /**
     * <b>Description:</b> Getter.
     *
     * @return A list of user IDs representing the members of the group.
     */
    public abstract List<MASMember> getMembers();

    /**
     * <b>Description:</b> Add a new member to the group.
     *
     * @param member The MASUser to be added to the group.
     */
    public abstract void addMember(MASMember member);

    /**
     * <b>Description:</b> remove member from the group.
     *
     * @param member The MASUser to be removed from the group.
     */
    public abstract void removeMember(MASMember member);

    /**
     * <b>Description:</b> Get the metadata for the group, if it exists. Could be null.
     *
     * @return The metadata for this group, if it exists.
     */
    public abstract MASMeta getMeta();

    /**
     * Saves the group object in the cloud.
     *
     * @param callback Callback with either the MASGroup object or the error message.
     */
    public abstract void save(MASCallback<MASGroup> callback);

    /**
     * Deletes the group object in the cloud.
     *
     * @param callback Callback with either success or error.
     */
    public abstract void delete(MASCallback<Void> callback);
}
