/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.user;

import android.support.annotation.NonNull;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASTransformable;
import com.ca.mas.identity.util.IdentityConsts;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The <p><b>MASAddress</b> interface contains the common attribute {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1.2">address</a>}
 * components of a single user's address. This interface will be populated as a result of a successful call to SCIM using the {@link com.ca.mas.foundation.MASUser#getUserById(String, MASCallback)} )} API method.
 * A physical mailing address for this user.  Canonical type values of "work", "home", and "other".</p>
 * <pre>
 *  {
 *      "type": "work",
 *      "streetAddress": "100 Universal City Plaza",
 *      "locality": "Hollywood",
 *      "region": "CA",
 *      "postalCode": "91608",
 *      "country": "USA",
 *      "formatted": "100 Universal City Plaza\nHollywood, CA 91608 USA",
 *      "primary": true
 *  }
 * </pre>
 */
public class MASAddress implements MASTransformable {

    private String mFormattedAddr;
    private String mAddrType;
    private String mStreetAddr;
    private String mLocality;
    private String mRegion;
    private String mPostalCode;
    private String mCountry;
    private boolean mIsPrimary;

    /**
     * <b>Description:</b> Default no-args constructor.
     */
    public MASAddress() {
    }

    /**
     * <b>Description:</b> Convenience constructor for creating an Address object.
     *
     * @param streetAddr the street address, such as 123 Main St. Apt. #7.
     * @param locality   the city.
     * @param region     the province or state.
     * @param country    the 2 character country code.
     * @param postalCode the postal code such as a zip code.
     */
    public MASAddress(String streetAddr, String locality, String region, String country, String postalCode) {
        mStreetAddr = streetAddr;
        mLocality = locality;
        mRegion = region;
        mCountry = country;
        mPostalCode = postalCode;
    }

    @Override
    public void populate(@NonNull JSONObject addrObj) throws JSONException {
        mFormattedAddr = addrObj.optString(IdentityConsts.KEY_ADDR_FORMATTED);
        mAddrType = addrObj.optString(IdentityConsts.KEY_ADDR_TYPE);
        mStreetAddr = addrObj.optString(IdentityConsts.KEY_ADDR_STREET);
        mLocality = addrObj.optString(IdentityConsts.KEY_ADDR_LOCALITY);
        mRegion = addrObj.optString(IdentityConsts.KEY_ADDR_REGION);
        mPostalCode = addrObj.optString(IdentityConsts.KEY_ADDR_POSTAL);
        mCountry = addrObj.optString(IdentityConsts.KEY_ADDR_COUNTRY);
        mIsPrimary = addrObj.optBoolean(IdentityConsts.KEY_ADDR_PRIMARY);
    }

    @Override
    public JSONObject getAsJSONObject() throws JSONException {
        JSONObject jobj = new JSONObject();
        jobj.put(IdentityConsts.KEY_ADDR_FORMATTED, mFormattedAddr);
        jobj.put(IdentityConsts.KEY_ADDR_TYPE, mAddrType);
        jobj.put(IdentityConsts.KEY_ADDR_STREET, mStreetAddr);
        jobj.put(IdentityConsts.KEY_ADDR_LOCALITY, mLocality);
        jobj.put(IdentityConsts.KEY_ADDR_REGION, mRegion);
        jobj.put(IdentityConsts.KEY_ADDR_POSTAL, mPostalCode);
        jobj.put(IdentityConsts.KEY_ADDR_COUNTRY, mCountry);
        jobj.put(IdentityConsts.KEY_ADDR_PRIMARY, mIsPrimary);
        return jobj;
    }

    /**
     * <b>Description:</b> Setter for the user's country associated with this address.
     *
     * @param country formatted according to the ISO 3166-1 "alpha-2" country code.
     */
    public void setCountry(String country) {
        mCountry = country;
    }

    /**
     * <b>Description:</b> Getter representing the city or locality component.
     *
     * @return String containing the city or locality of this address. This attribute may contain white-space or be null.
     */
    public String getLocality() {
        return mLocality;
    }
    /**
     * <b>Description:</b> Getter that returns the full mailing address, formatted for display or use with a mailing label.
     *
     * @return String representing the complete address in a label format. This attribute may contain white-space or be null.
     */
    public String getFormatted() {
        return mFormattedAddr;
    }

    /**
     * <b>Description:</b> Getter that returns the type of address this information represents such as 'work', 'home', etc.
     *
     * @return String representing the type of address. This attribute may contain white-space or be null.
     */
    public String getType() {
        return mAddrType;
    }

    /**
     * <b>Description:</b> Getter representing the full street address component, which may include house number, street name, P.O. box, and multi-line
     * extended street address information.
     *
     * @return String containing the full address. This attribute may contain white-space or be null.
     */
    public String getStreetAddress() {
        return mStreetAddr;
    }


    /**
     * <b>Description:</b> The state or region component.
     *
     * @return String representing the region for this address. This attribute may contain white-space or be null.
     */
    public String getRegion() {
        return mRegion;
    }

    /**
     * <b>Description:</b> Getter for the user's postal code associated with this address.
     *
     * @return String representing the postal code for this address. This attribute may contain white-space or be null.
     */
    public String getPostalCode() {
        return mPostalCode;
    }

    /**
     * <b>Description:</b> Getter for the country name component.  When specified, the value MUST be in ISO 3166-1 "alpha-2" code format
     *
     * @return String formatted according to the ISO 3166-1 "alpha-2" country code. This attribute may contain white-space or be null.
     */
    public String getCountry() {
        return mCountry;
    }

    /**
     * <b>Description:</b> The predicate that signifies whether the address is the primary address.
     *
     * @return boolean true or false
     */
    public boolean isPrimary() {
        return mIsPrimary;
    }


}
