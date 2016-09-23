# Version 1.2.00-CR1

### Bug fixes
- OTP now works when MAG is configured with an instance modifier.  [MAPI-1032]
- MASMessage objects cannot be empty. [MCT-475]
- MASUser.getCurrentUser should return null after device de-registration. [MCT-472]

### New features

- Provide sample application for MASStorage [MCT-352]
- Adding IDs to dynamic social login icons [MCT-353]
- Provide sample application for MAS Messaging [MCT-373]
- Provide sample application for MAS User Management [MCT-374]
- Provide sample application for Access API with Geolocation and OTP [MCT-379]
- Android N Support. [MCT-377]

### Deprecated methods

- Deprecated some redundant methods in MASGroupIdentity, methods are redundant with getGroupByFilter. [MCT-503]

 [mag]: https://docops.ca.com/mag
 [mas.ca.com]: http://mas.ca.com/
 [docs]: http://mas.ca.com/docs/
 [blog]: http://mas.ca.com/blog/

 [releases]: ../../releases
 [contributing]: /CONTRIBUTING.md
 [license-link]: /LICENSE
