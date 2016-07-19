/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.user;

import com.ca.mas.foundation.MASGroup;
import com.ca.mas.foundation.MASTransformable;
import com.ca.mas.identity.ScimIdentifiable;

import java.util.List;

public interface ScimUser extends ScimIdentifiable, MASTransformable {

    /**
     * <b>Description:</b> See the SCIM definition of {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1">userName</a>}
     *
     * @return String the case-sensitive, required attribute.
     */
    String getUserName();

    /**
     * <b>Description:</b> See the SCIM definition of {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1">nickName</a>}
     *
     * @return String representing this user's nickname. This attribute may contain white-space or be null.
     */
    String getNickName();

    /**
     * <b>Description:</b> See the SCIM definition of {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1">profileUrl</a>}
     *
     * @return String representation of the user's profile URI. This attribute may contain white-space or be null.
     */
    String getProfileUrl();

    /**
     * <b>Description:</b> See the SCIM definition of {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1">userType</a>}
     *
     * @return String representing the relationship between the organization and the user. This attribute may contain white-space or be null.
     */
    String getUserType();

    /**
     * <b>Description:</b> See the SCIM definition of {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1">title</a>}
     *
     * @return String representing the user's title. This attribute may contain white-space or be null.
     */
    String getTitle();

    /**
     * <b>Description:</b> See the SCIM definition of {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1">preferredLanguage</a>}
     *
     * @return String representing the user's preferred language. This attribute may contain white-space or be null.
     */
    String getPreferredLanguage();

    /**
     * <b>Description:</b> See the SCIM definition of {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1">locale</a>}
     *
     * @return String representing the user's locale. This attribute may contain white-space or be null.
     */
    String getLocale();

    /**
     * <b>Description:</b> See the SCIM definition of {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1">timezone</a>}
     *
     * @return String representing the user's timezone. This attribute may be null.
     */
    String getTimeZone();

    /**
     * <b>Description:</b>See the SCIM definition of {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1">active</a>}
     *
     * @return boolean representing whether the user is active. Will either be true or false (default).
     */
    boolean isActive();

    /**
     * <b>Description:</b> See the SCIM definition of {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1">password</a>}
     *
     * @return String representing the user's password. This attribute may be null.
     */
    String getPassword();

    /**
     * <b>Description:</b> See the SCIM definition of {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1.2">addresses</a>}
     *
     * @return List<MASAddress> contains this user's addresses. This list could be null or empty.
     */
    List<MASAddress> getAddressList();

    /**
     * <b>Description:</b> See the SCIM definition of {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1.2">emails</a>}
     *
     * @return List<User.Email> contains this user's emails. This list could be null or empty.
     */
    List<MASEmail> getEmailList();

    /**
     * <b>Description:</b> See the SCIM definition of {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1.2">phoneNumbers</a>}
     *
     * @return List<User.Phone> contains this user's phone numbers. This list could be null or empty.
     */
    List<MASPhone> getPhoneList();

    /**
     * <b>Description:</b> See the SCIM definition of {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1.2">addresses</a>}
     *
     * @return List<MASAddress> contains this user's instant messaging addresses. This list could be null or empty.
     */
    List<MASIms> getImsList();

    /**
     * <b>Description:</b> See the SCIM definition of {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1.2">photos</a>}
     *
     * @return List<User.Photo> contains this user's photos. This list could be null or empty.
     */
    List<MASPhoto> getPhotoList();

    /**
     * <b>Description:</b> See {@link MASMeta}.
     *
     * @return The MASMeta item used to describe this user. It could be null.
     */
    MASMeta getMeta();

    /**
     * <b>Description:</b> See the SCIM definition of {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1.2">groups</a>}
     *
     * @return List<MASGroup> this user is a member of. This list could be null or empty.
     */
    List<MASGroup> getGroupList();

    /**
     * <b>Description:</b> See {@link MASName}.
     *
     * @return MASName the user's name object. Could be null.
     */
    MASName getName();

}
