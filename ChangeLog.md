# Version 1.6.10

### Bug fixes
- Incorrect order of the callback between API request and Login. [DE341169]
- Failed to invoke callback.error when server provide invalid QRCode url. [DE340045]

### New features
- Support Android API 16+ [US406900]
Android 4.1.x is now supported with these limitations and changes:  
  - Android Account Manager (AMS) is not supported.
  - If you are using the MASStorage framework, Admins must change a context variable in the MASStorage policy to support Android 4.x.1.   See [Customize MASStorage Policy for Android 4.1.x](https://mas.ca.com/mag). If the policy is not updated, you will get an exception error when you try to delete data from cloud storage.
  - You must add the following dependency to your app module build.gradle file: ```compile org.bouncycastle:bcpkix-jdk15on:1.55```


# Version 1.6.00

### Bug fixes

- Local device deregistration will be performed only if the server deregistration is successful. [DE324143]
- MASUI's `Activity` classes now have their `android:exported` value set to `false` in its `AndroidManifest.xml`.  [DE319217]

### New features
- MASFoundation's `MASSharedStorage` class introduces a secure way of storing and sharing data across multiple applications by leveraging the Android Account Manager. [US416559]
- MASConnecta's MQTT integration has been improved and the sample app is updated. [US423907]
- Improved app testing with device registration. `DeviceIdentifier` is now generated from a unique asymmetric key pair. This enhances the existing Device Registration workflow by generating a unique device identifier that is sent to the server (instead of a static one). After a device is registered with an app, you should not get a "device registered" error after uninstall and reinstalls. [US390046]

# Version 1.5.00

### Bug fixes

- If SSO is disabled, non-shared token managers will be created. [DE284048]
- `MASMessage` now has correct receiver information and now implements the `Parcelable` interface. [DE299224]
- Key aliases now associate with the connected gateway. [DE290139]
- Re-registration can now successfully happen after offline de-registration. [DE290540]
- The authentication listener no longer incorrectly triggers upon successful registration when SSO is disabled. [DE300140]
- The user profile is no longer shared when SSO is disabled. [DE310770]

### New features

- The MAG module has been removed and in its place, the MAG/MAS packages have now been split into packages for MAS core, connecta, foundation, identity, and storage. These modules are now available as JCenter dependencies. [US339662]
- The ability to configure security configurations for external APIs, so that the SDK can securely connect to external APIs not on the primary gateway. [US344781]
- The SDK now supports multiple concurrent API requests. [US367676]
- More flexible and extensible authentication via the `MASAuthCredentials` class to assist with supporting future authentication types. [US349545]
- The ability to digitally sign the request as a JWT via the `MASClaimsBuilder` class. [US313138, US339651]
- Enhancing asymmetric/symmetric key handling for the local storage features. [US319275]
- Group messaging capabilities have been added. [US350277]
- The access token can now be retrieved from the MASUser object. [US357874]

### Deprecated Classes

- The 'Credentials' class is now refactored into the `MASCredentials` class. [US349545]

# Version 1.4.00

### Bug fixes
- Populate MASUser.getEmailList() from SCIM interface [DE277223]
- Include server prefix in MQTT topic structure. [DE269619]
- Remove auto pagination for Group and User management [DE275216]
- TLS1.1 and 1.2 support for Android 4.4 [DE284027]


### New features
- Introduces new way of dynamically initializing SDK with enrollment URL. With this feature, an application or system administrator can generate an URL specified to a user, so that the user can initialize SDK without having an application with built in `msso_config.json` deployed with the application. Server configuration and application level implementation is required. [US279237]
- Introduces new way of performing social login through SDK.  SDK now performs social login with `CustomTabs` to ensure better security, and adopts a modern way of performing OAuth web authentication. [US273008]
- Introduces new protection on authorization process with Proof Key for Code Exchange by OAuth Public Clients.  By default, PKCE process is enabled, and it can be disabled; however, it is strongly recommended to not disable it unless there is a specific use case. [US269512]
- Adds JCenter integration so developers can use dependency manager to install Mobile SDK. [US279239]
- Provides callback with error details when MASRequest is canceled. [US253874]
- Minimizes the number of permissions requested by the Mobile SDK. [US285971]
- Removes Spongy Castle dependency. [US238965]
- Introduces new way of performing user login. MASUser.login(MASIdToken idToken, final MASCallback<MASUser> callback) [US308531]
- Adds new attribute on MASRequestBuilder that allows access to unprotected endpoint on the Gateway.  [US308531]

### Deprecated Methods
- `MASSocialLogin` class is deprecated. Please use `MASCustomTabs` to display social login web URL from `MASAuthenticationProvider` and use `MASAuthorizationResponse` class to handle incoming response from `CustomTabs`. [US279228]
- `MASLoginFragment` login template has been removed. Please use `MASLoginActivity` to display the Login Dialog.  [US279228]
- `MASFilteredRequestBuilder.setTotalResults` has been removed.  Please use `setPagination` to handle result pagination. [DE275216]
- Resource files `xml/prefs.xml` and `xml/authenticator_ca_mas.xml` are removed from the Mobile SDK. [DE265344]

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
