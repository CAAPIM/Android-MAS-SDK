/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.user;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ca.mas.foundation.MASGroup;
import com.ca.mas.foundation.MASTransformable;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.util.FoundationConsts;
import com.ca.mas.identity.util.IdentityConsts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * See {@link MASUser}.
 */
public class User implements ScimUser {

    protected String mId;
    private String mExternalId;
    protected MASName mName;
    protected String mUserName;
    private String mNickName;
    private String mDisplayName;
    private String mProfileUrl;
    private String mUserType;
    private String mTitle;
    private String mPreferredLanguage;
    private String mLocale;
    private String mTimeZone;
    private boolean mIsActive;
    private String mPassword;
    private MASMeta mMeta;
    private long mCardinality;
    private JSONObject mSource;

    private final List<MASAddress> mAddressList;
    private final List<MASEmail> mEmailList;
    private final List<MASPhone> mPhoneList;
    private final List<MASPhoto> mPhotoList;
    private final List<MASIms> mImsList;
    private final List<X509Cert> mCertList;
    private final List<MASGroup> mGroupList;

    /**
     * <b>Description:</b> No args constructor.
     */
    public User() {
        mAddressList = new ArrayList<>();
        mEmailList = new ArrayList<>();
        mPhoneList = new ArrayList<>();
        mImsList = new ArrayList<>();
        mPhotoList = new ArrayList<>();
        mCertList = new ArrayList<>();
        mGroupList = new ArrayList<>();
    }

    @Override
    public void populate(@NonNull final JSONObject jsonObject) throws JSONException {

        this.mSource = jsonObject;

        if (jsonObject.has(IdentityConsts.KEY_MY_SUB)) {
            //Map the OPENID userinfo to Scim definition
            //Should verify the sub with the id_token
            mUserName = jsonObject.optString(IdentityConsts.KEY_MY_PREF_UNAME, IdentityConsts.EMPTY);
            mId = mUserName;
            final String photo = jsonObject.optString(IdentityConsts.KEY_MY_PICTURE, null);
            if (photo != null) {
                mPhotoList.add(new MASPhoto() {
                    @Override
                    public String getValue() {
                        return photo;
                    }
                });
            }

            final String email = jsonObject.optString(IdentityConsts.KEY_MY_EMAIL, null);
            if (email != null) {
                mEmailList.add(new MASEmail() {
                    @Override
                    public String getValue() {
                        return email;
                    }
                });
            }

            final String phone = jsonObject.optString(IdentityConsts.KEY_MY_PHONE, null);
            if (phone != null) {
                mPhoneList.add(new MASPhone() {
                    @Override
                    public String getValue() {
                        return phone;
                    }
                });
            }

            final String givenName = jsonObject.optString(IdentityConsts.KEY_MY_GIVEN_NAME, null);
            final String familyName = jsonObject.optString(IdentityConsts.KEY_MY_FAMILY_NAME, null);
            final String middleName = jsonObject.optString(IdentityConsts.KEY_MY_MIDDLE_NAME, null);
            mName = new MASName() {
                @Override
                public String getGivenName() {
                    return givenName;
                }

                @Override
                public String getFamilyName() {
                    return familyName;
                }

                @Override
                public String getMiddleName() {
                    return middleName;
                }
            };

            JSONObject addrObj = jsonObject.optJSONObject(IdentityConsts.KEY_MY_ADDRESS);
            if (addrObj != null) {
                String streetAddr = addrObj.optString(IdentityConsts.KEY_MY_STREET_ADDR, IdentityConsts.EMPTY);
                String locality = addrObj.optString(IdentityConsts.KEY_MY_LOCALITY, IdentityConsts.EMPTY);
                String region = addrObj.optString(IdentityConsts.KEY_MY_REGION, IdentityConsts.EMPTY);
                String postalCode = addrObj.optString(IdentityConsts.KEY_MY_POSTAL_CODE, IdentityConsts.EMPTY);
                String country = addrObj.optString(IdentityConsts.KEY_MY_COUNTRY, IdentityConsts.EMPTY);
                MASAddress address = new MASAddress(streetAddr, locality, region, country, postalCode);
                mAddressList.add(address);
            }

            return;
        }

        //SCIM definition
        mId = jsonObject.optString(IdentityConsts.KEY_ID);
        mExternalId = jsonObject.optString(IdentityConsts.KEY_EXTERNAL_ID);
        mUserName = jsonObject.optString(IdentityConsts.KEY_USERNAME);
        mDisplayName = jsonObject.optString(IdentityConsts.KEY_DISPLAY_NAME);
        mNickName = jsonObject.optString(IdentityConsts.KEY_NICK_NAME);
        mProfileUrl = jsonObject.optString(IdentityConsts.KEY_PROFILE_URL);
        mUserType = jsonObject.optString(IdentityConsts.KEY_USER_TYPE);
        mTitle = jsonObject.optString(IdentityConsts.KEY_TITLE);
        mPreferredLanguage = jsonObject.optString(IdentityConsts.KEY_PREFERRED_LANG);
        mLocale = jsonObject.optString(IdentityConsts.KEY_LOCALE);
        mTimeZone = jsonObject.optString(IdentityConsts.KEY_TIMEZONE);
        mPassword = jsonObject.optString(IdentityConsts.KEY_PASSWORD);

        mIsActive = jsonObject.optBoolean(IdentityConsts.KEY_ACTIVE, false);

        // meta
        if (jsonObject.has(IdentityConsts.KEY_META)) {
            JSONObject metaObj = jsonObject.getJSONObject(IdentityConsts.KEY_META);
            mMeta = new MASMeta();
            mMeta.populate(metaObj);
        }

        // name
        if (jsonObject.has(IdentityConsts.KEY_NAME)) {
            JSONObject nameObj = jsonObject.getJSONObject(IdentityConsts.KEY_NAME);
            mName = new MASName();
            mName.populate(nameObj);
        }
        // address
        if (jsonObject.has(IdentityConsts.KEY_ADDRS)) {
            JSONArray addrArr = jsonObject.getJSONArray(IdentityConsts.KEY_ADDRS);
            if (addrArr != null) {
                for (int i = 0; i < addrArr.length(); i++) {
                    JSONObject addrObj = addrArr.getJSONObject(i);
                    MASAddress address = new MASAddress();
                    address.populate(addrObj);
                    mAddressList.add(address);
                }
            }
        }
        // email
        if (jsonObject.has(IdentityConsts.KEY_EMAILS)) {
            JSONArray emailArr = jsonObject.getJSONArray(IdentityConsts.KEY_EMAILS);
            if (emailArr != null) {
                for (int i = 0; i < emailArr.length(); i++) {
                    JSONObject emailObj = emailArr.getJSONObject(i);
                    MASEmail email = new MASEmail();
                    email.populate(emailObj);
                    mEmailList.add(email);
                }
            }
        }
        // phoneNumbers
        if (jsonObject.has(IdentityConsts.KEY_PHONE_NUMBERS)) {
            JSONArray phoneArr = jsonObject.getJSONArray(IdentityConsts.KEY_PHONE_NUMBERS);
            if (phoneArr != null) {
                for (int i = 0; i < phoneArr.length(); i++) {
                    JSONObject phoneObj = phoneArr.getJSONObject(i);
                    MASPhone phone = new MASPhone();
                    phone.populate(phoneObj);
                    mPhoneList.add(phone);
                }
            }
        }
        // ims
        if (jsonObject.has(IdentityConsts.KEY_IMS)) {
            JSONArray imsArr = jsonObject.getJSONArray(IdentityConsts.KEY_IMS);
            if (imsArr != null) {
                for (int i = 0; i < imsArr.length(); i++) {
                    JSONObject imsObj = imsArr.getJSONObject(i);
                    MASIms ims = new MASIms();
                    ims.populate(imsObj);
                    mImsList.add(ims);
                }
            }
        }

        // photos
        if (jsonObject.has(IdentityConsts.KEY_PHOTOS)) {
            JSONArray photosArr = jsonObject.getJSONArray(IdentityConsts.KEY_PHOTOS);
            if (photosArr != null) {
                for (int i = 0; i < photosArr.length(); i++) {
                    JSONObject photoObj = photosArr.getJSONObject(i);
                    MASPhoto photo = new MASPhoto();
                    photo.populate(photoObj);
                    mPhotoList.add(photo);
                }
            }
        }
        // certs
        if (jsonObject.has(IdentityConsts.KEY_X509CERTS)) {
            JSONArray certsArr = jsonObject.getJSONArray(IdentityConsts.KEY_X509CERTS);
            if (certsArr != null) {
                for (int i = 0; i < certsArr.length(); i++) {
                    JSONObject certObj = certsArr.getJSONObject(i);
                    X509Cert x509Cert = new X509Cert();
                    x509Cert.populate(certObj);
                    mCertList.add(x509Cert);
                }
            }
        }
        // groups
        if (jsonObject.has(IdentityConsts.KEY_GROUPS)) {
            JSONArray groupArr = jsonObject.getJSONArray(IdentityConsts.KEY_GROUPS);
            if (groupArr != null) {
                for (int i = 0; i < groupArr.length(); i++) {
                    JSONObject groupObj = groupArr.getJSONObject(i);
                    MASGroup group = MASGroup.newInstance();
                    group.populate(groupObj);
                    mGroupList.add(group);
                }
            }
        }
    }

    @Override
    public JSONObject getAsJSONObject() throws JSONException {
        return mSource;
    }

    // --------- ScimIdentifiable ----------------------------------------------

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public String getExternalId() {
        return mExternalId;
    }

    @Override
    public String getDisplayName() {
        return mDisplayName;
    }

    @Override
    public long getCardinality() {
        return mCardinality;
    }

    @Override
    public List<MASAddress> getAddressList() {
        return mAddressList;
    }

    @Override
    public List<MASEmail> getEmailList() {
        return mEmailList;
    }

    @Override
    public List<MASPhone> getPhoneList() {
        return mPhoneList;
    }

    @Override
    public List<MASIms> getImsList() {
        return mImsList;
    }

    @Override
    public List<MASPhoto> getPhotoList() {
        return mPhotoList;
    }

    @Override
    public String getNickName() {
        return null;
    }

    @Override
    public String getProfileUrl() {
        return mProfileUrl;
    }

    @Override
    public String getUserName() {
        return mUserName;
    }

    @Override
    public String getUserType() {
        return mUserType;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getPreferredLanguage() {
        return mPreferredLanguage;
    }

    @Override
    public String getLocale() {
        return mLocale;
    }

    @Override
    public String getTimeZone() {
        return mTimeZone;
    }

    @Override
    public boolean isActive() {
        return mIsActive;
    }

    @Override
    public String getPassword() {
        return mPassword;
    }

    @Override
    public MASMeta getMeta() {
        return mMeta;
    }

    @Override
    public List<MASGroup> getGroupList() {
        return mGroupList;
    }

    /**
     * <b>Description:</b> The user's certificate object. See {@link com.ca.mas.foundation.MASTransformable}
     */
    public class X509Cert implements MASTransformable {

        private String mValue;

        @Override
        public void populate(@NonNull JSONObject jobj) throws JSONException {
            mValue = jobj.optString(FoundationConsts.KEY_VALUE);
        }

        @Override
        public JSONObject getAsJSONObject() throws JSONException {
            JSONObject jobj = new JSONObject();
            jobj.put(FoundationConsts.KEY_VALUE, mValue);
            return jobj;
        }
    }

    @Override
    public MASName getName() {
        return mName;
    }

    public JSONObject getSource() {
        return mSource;
    }

}


