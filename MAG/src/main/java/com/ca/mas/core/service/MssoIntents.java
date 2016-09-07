/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.service;

/**
 * <p>Holds constants related to intents used by the MSSO service.</p>
 * <p>If implementing a custom login dialog activity, you will need to start the login activity in response
 * to an OBTAIN_CREDENTIALS intent. When credentials are obtained you should startService with the CREDENTIALS_OBTAINED
 * intent. If the activity is canceled (including via the back button), you should startService with the CANCEL_REQUEST
 * intent. Response intents must include the EXTRA_REQUEST_ID long value copied from the OBTAIN_CREDENTIALS intent.</p>
 */
public interface MssoIntents {
    // Intent actions

    /** An Intent with this action triggers processing (or reprocessing) of a request expected to be already present in the MssoRequestQueue. Fired by MssoClient. Handled by MssoService. */
    String ACTION_PROCESS_REQUEST = "com.ca.mas.core.service.action.PROCESS_REQUEST";

    /** An Intent with this action is used to start a logon dialog activity when credentials are needed.  Fired by MssoService. Handled by logon dialog activity. */
    String ACTION_OBTAIN_CREDENTIALS = "com.ca.mas.core.service.action.OBTAIN_CREDENTIALS";

    /** An Intent with this action is used by a logon activity to send logon information back to the MssoService.  Fired by logon dialog activity.  Handled by MssoService. */
    String ACTION_CREDENTIALS_OBTAINED = "com.ca.mas.core.service.action.CREDENTIALS_OBTAINED";

    /** An Intent with this action is used by a Display otp protected data activity to send otp information back to the MssoService.  Fired by Display otp protected data activity .  Handled by MssoService. */
    String ACTION_VALIDATE_OTP = "com.ca.mas.core.service.action.VALIDATE_OTP";

    /** An Intent with this can be sent to the MssoService to indicate that a request should be canceled.  Fired by logon dialog activity.  Handled by MssoService. */
    String ACTION_CANCEL_REQUEST = "com.ca.mas.core.service.action.CANCEL_REQUEST";

    /** An Intent with this can be sent to the App to indicate that a webview should be rendered. Fired by clicking the App Icon in the enterprise browser */
    String ACTION_RENDER_WEBVIEW = "com.ca.mas.core.service.action.RENDER_WEBVIEW";

    String ACTION_LAUNCH_ENTERPRISE_BROWSER = "com.ca.mas.core.service.action.LAUNCH_ENTERPRISE_BROWSER";

    String ACTION_SYNC = "com.ca.mas.core.service.action.SYNC";

    // Request info

    /** The ID of the request to (re)process, or -1 to process all pending queued requests that are not currently being processed. */
    String EXTRA_REQUEST_ID = "com.ca.mas.core.service.req.extra.requestId";

    /** The user credentials being provided with CREDENTIALS_OBTAINED. */
    String EXTRA_CREDENTIALS = "com.ca.mas.core.service.req.extra.credentials";

    // Attributes for otp authentication
    /** The otp authentication otp value.*/
    String EXTRA_OTP_VALUE = "com.ca.mas.core.service.req.extra.auth.otp.value";

    // Attributes for otp authentication
    /** The otp delivery selected channels.*/
    String EXTRA_OTP_SELECTED_CHANNELS = "com.ca.mas.core.service.req.extra.auth.otp.channels";

    // Attributes for social login, below attributes can be empty if not using Social login

    /** The authentication provider.*/
    String EXTRA_AUTH_PROVIDERS = "com.ca.mas.core.service.req.extra.social.login.providers";

    /** The social login url */
    String EXTRA_SOCIAL_LOGIN_URL = "com.ca.mas.core.service.req.extra.social.login.url";

    /** List of Enterprise Application */
    String EXTRA_APPS = "com.ca.mas.core.service.extra.apps";

    /** The Enterprise Application */
    String EXTRA_APP = "com.ca.mas.core.service.extra.app";

    // Result info

    /** Result code indicating an HTTP request was delivered to the target server and a response was received (though the HTTP-level response code may be other than 200). */
    int RESULT_CODE_SUCCESS = 0;

    /** Result code indicating that no pending request was found in the MssoRequestQueue with the specified ID. */
    int RESULT_CODE_ERR_BAD_REQUEST_ID = 1;

    /** Result code indicating the a response could not be obtained due to an I/O error. */
    int RESULT_CODE_ERR_IO = 2;

    /** Result code indicating that a response could not be obtained due to an unexpected exception. */
    int RESULT_CODE_ERR_UNKNOWN = 3;

    /** Result code indicating that a request was canceled, perhaps by the user canceling a log on dialog. */
    int RESULT_CODE_ERR_CANCELED = 4;

    /** Result code indicating that device registration is awaiting approval by an administrator. */
    int RESULT_CODE_ERR_AWAITING_REGISTRATION = 5;

    /** Result code indicating that failed in oauth request process. */
    @Deprecated
    int RESULT_CODE_ERR_AUTHORIZE = 6;
    int RESULT_CODE_ERR_OAUTH = 6;

    /** Result code indicating that failed in device registration process . */
    int RESULT_CODE_ERR_REGISTRATION = 7;

    /** Result code indicating that location is turned off, but is required. */
    int RESULT_CODE_ERR_LOCATION_REQUIRED = -301;

    /** Result code indicating that user with associated location parameters is not permitted to access protected resources. */
    int RESULT_CODE_ERR_LOCATION_UNAUTHORIZED = -302;

    /** Result code indicating that MSISDN permission is turned off, but is required. */
    int RESULT_CODE_ERR_MSISDN_REQUIRED = -601;

    /** Result code indicating that user associated with msisdn is not permitted to access protected resources. */
    int RESULT_CODE_ERR_MSISDN_UNAUTHORIZED = -602;

    /** Result code indicating that user received invalid signature jason web token. */
    int RESULT_CODE_ERR_JWT_SIGNATURE_INVALID = -701;

    /** Result code indicating that user received invalid aud jason web token. */
    int RESULT_CODE_ERR_JWT_AUD_INVALID = -702;

    /** Result code indicating that user received invalid azp jason web token. */
    int RESULT_CODE_ERR_JWT_AZP_INVALID = -703;

    /** Result code indicating that user received expired jason web token. */
    int RESULT_CODE_ERR_JWT_EXPIRED = -704;

    /** Result code indicating that user received expired jason web token. */
    int RESULT_CODE_ERR_JWT_INVALID = -705;

    /** Result code indicating that failed to process the JSON Message from enterprise endpoint. */
    int RESULT_CODE_ERR_ENTERPRISE_BROWSER_INVALID_JSON = -801;

    /** Result code indicating that failed to process the Native URL from enterprise endpoint. */
    int RESULT_CODE_ERR_ENTERPRISE_BROWSER_NATIVE_APP_NOT_EXIST = -802;

    /** Result code indicating that failed to process the Auth URL from enterprise endpoint. */
    int RESULT_CODE_ERR_ENTERPRISE_BROWSER_INVALID_URL = -803;

    /** Result code indicating that there is not Native URL and Auth URL from enterprise endpoint. */
    int RESULT_CODE_ERR_ENTERPRISE_BROWSER_APP_NOT_EXIST = -804;

    /** Result code indicating that application configuration is wrong, so we can't obtain valid client credentials. */
    int RESULT_CODE_ERR_CLIENT_CREDENTIALS = -901;

    /** The ID of the request, for picking up the result from the MssoResponseQueue.  May be missing if the request was missing a requestId. */
    String RESULT_REQUEST_ID = "com.ca.mas.core.service.result.requestId";

    /** An error message related to a result code other than RESULT_CODE_SUCCESS. */
    String RESULT_ERROR_MESSAGE = "com.ca.mas.core.service.result.errorMessage";

    /** An error object related to a result code other than RESULT_CODE_SUCCESS. */
    String RESULT_ERROR = "com.ca.mas.core.service.result.error";
}
