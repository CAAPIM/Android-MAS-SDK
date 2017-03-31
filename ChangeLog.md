# Version 4.0.00

### Bug fixes
- Populate MASUser.getEmailList() from scim interface [DE277223]
- Include server prefix in MQTT topic structure. [DE269619]
- Remove auto pagination for Group and User management [DE275216]
- TLS1.1 and 1.2 support for Android 4.4 [DE284027]


### New features
- Introduces new way of dynamically initializing SDK with enrollment URL. With this feature, application or system administrator can generate an URL specified to a user, so that the user can initialize SDK without having an application with built in `msso_config.json` deployed with the application. Server configuration and application level's implementation is required. [US279237]
- Introduces new way of performing social login through SDK.  SDK now performs social login with `CustomTabs` to ensure better security, and adopt modern way of performing oAuth web authentication. [US273008]
- Introduces new protection on authorization process with Proof Key for Code Exchange by OAuth Public Clients.  By default, PKCE process is enabled, and it can be disabled; however, it is strongly recommended to not disable it unless there is a specific use case. [US269512]
- JCenter integration, allow developers to use dependency manager to install MAS SDK. [US279239]
- Provide callback with error details when MASRequest is canceled. [US253874]
- Minimize the number of permissions requested by the Mobile SDK. [US285971]
- Remove Sponge castle dependency. [US238965]
- Introduces new way of performing user login. MASUser.login(MASIdToken idToken, final MASCallback<MASUser> callback) [US308531]
- New attribute on MASRequestBuilder to allow access non-protected endpoint on the gateway.  [US308531]

### Deprecated Methods
- `MASSocialLogin` class is deprecated. Please use `MASCustomTabs` to display social login web URL from `MASAuthenticationProvider` and use `MASAuthorizationResponse` class to handle incoming response from `CustomTabs`. [US279228]


# Version 1.3.00-CR1

### Bug fixes
- Remove default WebViewClient onReceivedSslError implementation . [DE247887]
- Allow developer to overwrite the authenticator_ca_mas setting for AMS. [DE265344]

# Version 1.3

### Bug fixes
- Prefixes are now included in MAS Identity calls. [DE246038]
- A cancelled MASRequest will no longer invoke the error callback. [DE247887]
- Received MASMessage objects will now include a topic. [DE254536]
- MQTT methods now run in the background thread. [DE255170]
- Fixed incorrect timestamps of received MASMessage objects. [DE255503]

### New features

- Client profile now updates when the SDK detects changes on the client ID. [US238458]
- User profile now persists locally on the device, matching with iOS' behaviour. [US238628]
- New streamlined OTP dialog fragment with custom logo support. [US238461]
- Added session locking along with a sample app. [US240423, US240504]
- Added a pub/sub sample app. [US240502]
- Android interfaces for sending MQTT messages and support for connecting to public brokers. [US240431]
- The SDK will now seamlessly handle the expiration/removal of client certificates from the server. [US240491, US240493]
- Debug logging enhancements. [US262854]

### Deprecated classes

- MASOtpSelectDeliveryChannelFragment and MASOtpAuthFragment have been merged into MASOTPDialogFragment.

# Version 1.2.00-CR1

### Bug fixes
- OTP now works when MAG is configured with an instance modifier.  [MAPI-1032]
- MASMessage objects cannot be empty. [MCT-475]
- MASUser.getCurrentUser should return null after device de-registration. [MCT-472]

### New features

- Provide a sample application for MASStorage. [MCT-352]
- Adding IDs to dynamic social login icons. [MCT-353]
- Provide a sample application for MAS Messaging. [MCT-373]
- Provide a sample application for MAS User Management. [MCT-374]
- Provide a sample application for Access API with Geolocation and OTP. [MCT-379]
- Android N Support. [MCT-377]

### Deprecated methods

- Deprecated some redundant methods in MASGroupIdentity, these methods are redundant with getGroupByFilter(). [MCT-503]

 [mag]: https://docops.ca.com/mag
 [mas.ca.com]: http://mas.ca.com/
 [docs]: http://mas.ca.com/docs/
 [blog]: http://mas.ca.com/blog/

 [releases]: ../../releases
 [contributing]: /CONTRIBUTING.md
 [license-link]: /LICENSE
