/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.util;

import com.ca.mas.foundation.util.FoundationConsts;

/**
 * <p><b>IdentityConsts</b> contains the set of constants used in this {@link <a href="https://tools.ietf.org/html/rfc7644">SCIM implementation</a>}.</p>
 */
public class IdentityConsts extends FoundationConsts {

    public static final String KEY_SCHEMAS = "schemas";
    public static final String KEY_ID = "id";
    public static final String KEY_OWNER = "owner";
    public static final String KEY_MEMBERS = "members";
    public static final String KEY_EXTERNAL_ID = "externalId";
    public static final String KEY_USERNAME = "userName";
    public static final String KEY_DISPLAY_NAME = "displayName";
    public static final String KEY_DISPLAY = "display";
    public static final String KEY_NICK_NAME = "nickName";
    public static final String KEY_PROFILE_URL = "profileUrl";
    public static final String KEY_USER_TYPE = "userType";
    public static final String KEY_TITLE = "title";
    public static final String KEY_PREFERRED_LANG = "preferredLanguage";
    public static final String KEY_LOCALE = "locale";
    public static final String KEY_TIMEZONE = "timezone";
    public static final String KEY_ACTIVE = "active";
    public static final String KEY_PASSWORD = "password";

    public static final String KEY_NAME = "name";
    public static final String KEY_FORMATTED = "formatted";
    public static final String KEY_FAMILY_NAME = "familyName";
    public static final String KEY_GIVEN_NAME = "givenName";
    public static final String KEY_MIDDLE_NAME = "middleName";
    public static final String KEY_PREFIX_NAME = "honorificPrefix";
    public static final String KEY_SUFFIX_NAME = "honorificSuffix";

    public static final String KEY_ADDRS = "addresses";
    public static final String KEY_ADDR_FORMATTED = "formattedAddress";
    public static final String KEY_ADDR_TYPE = "addressType";
    public static final String KEY_ADDR_STREET = "streetAddress";
    public static final String KEY_ADDR_LOCALITY = "locality";
    public static final String KEY_ADDR_REGION = "region";
    public static final String KEY_ADDR_POSTAL = "postalCode";
    public static final String KEY_ADDR_COUNTRY = "postalCode";
    public static final String KEY_ADDR_IS_ACTIVE = "isActive";

    public static final String KEY_EMAILS = "emails";
    public static final String KEY_EMAIL_PRIMARY = "primary";
    public static final String KEY_PHONE_NUMBERS = "phoneNumbers";

    public static final String KEY_REFERENCE = "$ref";

    public static final String KEY_META = "meta";
    public static final String KEY_META_RESOURCE_TYPE = "resourceType";
    public static final String KEY_META_CREATED = "created";
    public static final String KEY_META_LAST_MODIFIED = "lastModified";
    public static final String KEY_META_VERSION = "version";
    public static final String KEY_META_LOCATION = "location";

    public static final String KEY_IMS = "ims";
    public static final String KEY_PHOTOS = "photos";
    public static final String KEY_X509CERTS = "x509Certificates";
    public static final String KEY_GROUPS = "groups";

    public static final String KEY_MY_SUB = "sub";
    public static final String KEY_MY_GIVEN_NAME = "given_name";
    public static final String KEY_MY_FAMILY_NAME = "family_name";
    public static final String KEY_MY_PREF_UNAME = "preferred_username";
    public static final String KEY_MY_PICTURE = "picture";
    public static final String KEY_MY_EMAIL = "email";
    public static final String KEY_MY_PHONE = "phone_number";
    public static final String KEY_MY_ADDRESS = "address";
    public static final String KEY_MY_STREET_ADDR = "street_address";
    public static final String KEY_MY_LOCALITY = "locality";
    public static final String KEY_MY_REGION = "region";
    public static final String KEY_MY_POSTAL_CODE = "postal_code";
    public static final String KEY_MY_COUNTRY = "country";
    public static final String KEY_PATH = "path";
    public static final String KEY_OPERATIONS = "Operations";
    public static final String KEY_OP = "op";

    public static final String PHOTO_TYPE_THUMBNAIL = "thumbnail";

    public static final String KEY_RESOURCES = "Resources";
    public static final String KEY_ATTRIBUTES = "attributes";
    public static final String KEY_EXCLUDED_ATTRIBUTES = "excludedAttributes";
    public static final String KEY_SUB_ATTRIBUTES = "subAttributes";
    public static final String KEY_FILTER = "filter";

    public static final String OP_ADD = "add";
    public static final String OP_REMOVE = "remove";
    public static final String OP_REPLACE = "replace";

    // -------- QUERY OPERATORS -----------------------------------------------
    public static final String OP_EQUAL = "eq";
    public static final String OP_NOT_EQUAL = "ne";
    public static final String OP_CONTAINS = "co";
    public static final String OP_STARTS_WITH = "sw";
    public static final String OP_ENDS_WITH = "ew";
    public static final String OP_IS_PRESENT = "pr";
    public static final String OP_GREATER_THAN = "gt";
    public static final String OP_GREATER_THAN_OR_EQUAL = "ge";
    public static final String OP_LESS_THAN = "lt";
    public static final String OP_LESS_THAN_OR_EQUAL = "le";

    // -------- SCHEMAS -------------------------------------------------------
    public static final String SCIM_SCHEMAS = "Schemas";
    public static final String SCHEMAS_PREFIX = "urn:ietf:params:scim:schemas:core:2.0:";
    public static final String SCHEMA_USER = SCHEMAS_PREFIX + "User";
    public static final String SCHEMA_GROUP = SCHEMAS_PREFIX + "Group";
    public static final String SCIM_USERS = "Users";
    public static final String SCIM_GROUPS = "Groups";
    public static final String SCHEMA_PATCH = "urn:ietf:params:scim:api:messages:2.0:PatchOp";

    public static final String KEY_USER_ATTRIBUTES = "userAttributesKey";
    public static final String KEY_GROUP_ATTRIBUTES = "groupAttributesKey";

    public static final String KEY_TOTAL_RESULTS = "totalResults";

    public static final String META_DATE_FORMAT = "yyyy-MM-dd:HH:mm:ss.SSSZ";

    public static final String KEY_GROUPS_BY_OWNER = KEY_OWNER + DOT + KEY_VALUE;
    public static final String KEY_GROUPS_BY_MEMBER = KEY_MEMBERS + DOT + KEY_VALUE;
    public static final String KEY_GROUPS_BY_NAME = KEY_DISPLAY_NAME;
    public static final int INDEX_START = 1;
    public static final int PAGE_SIZE = 12;

}
