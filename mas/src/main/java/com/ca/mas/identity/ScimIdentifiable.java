/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity;

/**
 * <p><b>ScimIdentifiable</b> is an interface that should be implemented by any class that requires uniqueness within the system. A ScimIdentifiable
 * object can be a user, a message, a device, or any other entity that needs to be managed and categorized. The <i>id</i> value must be unique
 * within the system regardless of where this unique value originates. This could be a unique key in a database, a GUID generated in the application
 * using the MAS SDK, or a timestamp that is granular enough to guarantee uniqueness. The <i>displayName</i> is unconstrained and can be anything, or null.
 * The <i>externalId</i> is added to satisfy the {@link <a href=" https://tools.ietf.org/html/rfc7643">scim core schema<a>} and the
 * {@link <a href=" https://tools.ietf.org/html/rfc7644">scim protocol<a>} specification.
 * </p>
 *
 */
public interface ScimIdentifiable {

    /**
     * <b>Description:</b> These ResourceTypes are base objects within the system.
     */
    enum ResourceType {User, Device, Application, None}

    /**
     * <b>Description:</b> Return the unique id that has been previously set through the call to setId. If no value has been
     * set for the id then this value could be null.
     *
     * @return String representing the unique id. Can be null.
     */
    String getId();

    /**
     * <b>Description:</b> The value returned is based on the {@link <a href=" https://tools.ietf.org/html/rfc7643">scim core schema<a>}.
     *
     * @return String representing the externalId. Can be null.
     */
    String getExternalId();


    /**
     * <b>Description:</b> The string value set by the application developer.
     *
     * @return String the free-form display name. Can be null.
     */
    String getDisplayName();

    /**
     * <b>Description:</b> Get the previous set value of the default value for the long data type (0).
     *
     * @return long the value that is used for ordering this <i>ScimIdentifiable</i>
     */
   long getCardinality();


}
