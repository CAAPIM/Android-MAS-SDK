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
