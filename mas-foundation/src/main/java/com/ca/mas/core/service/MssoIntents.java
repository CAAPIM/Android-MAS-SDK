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

    /** An Intent with this action is used by a logon activity to send logon information back to the MssoService.  Fired by logon dialog activity.  Handled by MssoService. */
    String ACTION_CREDENTIALS_OBTAINED = "com.ca.mas.core.service.action.CREDENTIALS_OBTAINED";

    // Request info

    /** The ID of the request to (re)process, or -1 to process all pending queued requests that are not currently being processed. */
    String EXTRA_REQUEST_ID = "com.ca.mas.core.service.req.extra.requestId";

    /** The user credentials being provided with CREDENTIALS_OBTAINED. */
    String EXTRA_CREDENTIALS = "com.ca.mas.core.service.req.extra.credentials";

    String EXTRA_ADDITIONAL_HEADERS = "com.ca.mas.core.service.req.extra.additional.headers";

    /** The otp authentication otp handler.*/
    String EXTRA_OTP_HANDLER = "com.ca.mas.core.service.req.extra.auth.otp.handler";


    // Attributes for social login, below attributes can be empty if not using Social login

    /** The authentication provider.*/
    String EXTRA_AUTH_PROVIDERS = "com.ca.mas.core.service.req.extra.social.login.providers";

    // Result info

    /** Result code indicating an HTTP request was delivered to the target server and a response was received (though the HTTP-level response code may be other than 200). */
    int RESULT_CODE_SUCCESS = 0;

    /** Result code indicating the a response could not be obtained due to an I/O error. */
    int RESULT_CODE_ERR = 1;

    /** Result code indicating that a request was canceled, perhaps by the user canceling a log on dialog. */
    int RESULT_CODE_ERR_CANCELED = 2;

    /** The ID of the request, for picking up the result from the MssoResponseQueue.  May be missing if the request was missing a requestId. */
    String RESULT_REQUEST_ID = "com.ca.mas.core.service.result.requestId";

    /** An error message related to a result code other than RESULT_CODE_SUCCESS. */
    String RESULT_ERROR_MESSAGE = "com.ca.mas.core.service.result.errorMessage";

    /** An error object related to a result code other than RESULT_CODE_SUCCESS. */
    String RESULT_ERROR = "com.ca.mas.core.service.result.error";
}
