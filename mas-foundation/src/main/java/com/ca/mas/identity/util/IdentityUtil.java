/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.foundation.FoundationConsts;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.identity.user.MASPhoto;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * <p><b>IdentityUtil</b> implements helper methods used in creating {@link <a href="https://tools.ietf.org/html/rfc7644">SCIM</a>} URIs,
 * retreiving users and groups that have been retrieved via web service calls to SCIM, and for formatting date strings. Usages of
 * this class follow the static format of 'IdentityUtil.&lt;method name&gt;.</p>
 */
public class IdentityUtil {

    public static final Map<String, JSONObject> SCHEMA_MAP = new HashMap<>();

   public static Bitmap getThumbnail(List<MASPhoto> photoList) {
        return getPhoto(photoList);
    }

    /**
     * <b>Pre-Conditions: </b> Must be called after the meta-information has been populated.<br>
     * <b>Description: </b> Search for the attribute in the set of allowable attributes. The logic is as follows;
     * i. The exact match for the attribute must exist in the 'attributes' passed in, or
     * ii. The attribute must start with attrname. where the '.' precedes the fully qualified attribute name.
     *
     * @param providedAttributes
     * @param allowedAttributes
     * @return List<String> the list of attributes that exists in the attribute set.
     */
    public static List<String> normalizeAttributes(@NonNull List<String> providedAttributes, @NonNull List<String> allowedAttributes) {
        List<String> replacements = new ArrayList<>();
        for (String attribute : providedAttributes) {
            // if we have an exact match, then the attribute belongs
            if (allowedAttributes.contains(attribute)) {
                replacements.add(attribute);
            } else {
                for (String s : allowedAttributes) {
                    // if there is a set of attribute.subattributes that match, then they all must
                    // be part of the attribute set.
                    if (s.startsWith(attribute + IdentityConsts.DOT)) {
                        replacements.add(s);
                    }
                }
            }
        }
        return replacements;
    }

    /**
     * <b>Description:</b> Create and return the path component of a URI of the form
     * /SCIM/MAS/v2/Schemas
     * @return String the schemas path.
     */
    public static String getSchemasPath(){
        return getPath(IdentityConsts.SCIM_SCHEMAS);
    }

    /**
     * <b>Pre-Conditions:</b> The msso_config must be parsed.<br>
     * <b>Description:</b> This helper method returns the User's URI as defined in the msso_config.json endpoint description.
     * @return String the formatted URI for accessing Users, ex: SCIM/[provider]/v2/Users.
     */
    public static String getUserPath() {
        return getPath(IdentityConsts.SCIM_USERS);
    }

    public static String getGroupPath() {
        return getPath(IdentityConsts.SCIM_GROUPS);
    }


    /**
     * Returns the path component of the URL.
     * @param entity Entity to get path of.
     * @return       SCIM Path to entity as String.
     */
    private static String getPath(String entity) {
        StringBuilder sb = new StringBuilder();
        String scimPath = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider()
                .getProperty(FoundationConsts.KEY_CONFIG_SCIM_PATH);
        if (scimPath == null) {
            scimPath = "/SCIM/MAS/v2";
        }
        sb.append(scimPath);
        if (!TextUtils.isEmpty(entity)) {
            sb.append(FoundationConsts.FSLASH);
            sb.append(entity);
        }
        if (DEBUG) Log.d(TAG, "SCIM URL Path: " + sb.toString());
        return sb.toString();
    }

    /**
     * <b>Pre-Conditions:</b> The user must have been retrieved from SCIM.<br>
     * <b>Description:</b> Format the byte stream in the Photo attribute so it can be displayed as a
     *
     * @param photoList the list of photos from the {@link MASUser#getUserById(String, MASCallback)} call. See {@link MASPhoto}.
     * @return Drawable  the Android Drawable object for display. Could be null.
     */
    public static Bitmap getPhoto(List<MASPhoto> photoList) {
        if (photoList != null) {
            for (MASPhoto photo : photoList) {
                if (photo != null && photo.getType().equals(IdentityConsts.PHOTO_TYPE_THUMBNAIL)) {
                    String value = photo.getValue();
                    int offset = value.indexOf(IdentityConsts.BASE64) + IdentityConsts.BASE64.length();
                    byte[] subVal = Base64.decode(value.substring(offset), 0);
                    return BitmapFactory.decodeByteArray(subVal, 0, subVal.length);
                }
            }
        }
        return null;
    }

}
