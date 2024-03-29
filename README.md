[![Maintenance](https://img.shields.io/badge/Maintained%3F-no-red.svg)](https://bitbucket.org/lbesson/ansi-colors)

⚠️ | Please note that this repo will be archived in the near future. Please do not submit any new changes as they are no longer being accepted. Please contact Broadcom support https://support.broadcom.com/ to report any defects or make a request for an update. Broadcom is continuing support for the SDK but will no longer maintain the public GitHub community.
:---: | :---

# Android™ Mobile SDK
## for CA Mobile API Gateway

CA Mobile API Gateway (MAG) provides enterprises with a secure mobile backend that integrates systems and allows control over which users, devices, and applications can access your APIs. The MAG enables developers to leverage standards for API security such as (OAuth2, OpenID Connect, PKI) through client SDKs for Android.

The CA Mobile API Gateway also provides powerful mobile backend services through SDKs and APIs for developers to help accelerate the app development process.

## Get Started
Check out our [documentation] for sample code and more.

## Android Mobile SDK Frameworks
The Android Mobile SDK consists of these frameworks:

- **MASConnecta** - Pub/Sub messagin service.
- **MASFoundation** - Core services to handle user authentication, device and app registration, requests and local storage of certificates, keys, and token credentials for accessing protected APIs.
- **MASStorage** - Storage services for private local storage.
- **MASUI** - Resources to implement a user login dialog, Social Login, One-Time Password, and Proximity Login (QR code and BLE).

For more information about our mobile products see the [developer website](http://techdocs.broadcom.com/content/broadcom/techdocs/us/en/ca-enterprise-software/layer7-api-management/mobile-sdk-for-ca-mobile-api-gateway/2-1.html).

## Features

* **Secure API Calls** - Protect APIs with Mutual TLS and control API access on application, user and device level.
* **Authentication** - Implement authentication with username/password, Facebook, Google, Twitter sign-in.
* **Second Factor Auth** - Secure critical APIs with One Time Password.
* **Single Sign-On** - Share user credentials between your apps.
* **Enterprise Browser** - Extend the single sign-on session to web applications.
* **Proximity Login** - Transfer the user session between devices and platforms.
* **Fingerprint Sessions Lock** - Support phone unlocking using fingerprint recognition.
* **Pub/Sub** - Create real-time, IoT-friendly apps using an MQTT-based Pub/Sub infrastructure.
* **Adhoc Groups** - Create groups on-the-fly for collaborative apps.
* **Local Storage** - Store data on devices with enterprise-grade encryption.
* **UI Template** - Provides resources to implement a user login dialog, Social Login, One-Time Password, and Proximity Login (QR code and BLE), to save time during UI creation and app prototyping.

## Installation
Edit your build.gradle file and add below dependency:
```gradle
    dependencies {
        implementation 'com.ca.apim:mas-foundation:2.2.0'

        implementation 'com.ca.apim:mas-connecta:2.2.0' // (Optional) Only required when using mas connecta
        implementation 'com.ca.apim:mas-storage:2.2.0' // (Optional) Only required when using mas storage
        implementation 'com.ca.apim:masui:2.2.0' // (Optional) Only required when using MASUI Template. The MAS UI library provides sample user interfaces for Login, OTP, Social Login, and Enterprise Browser.

        implementation 'org.bouncycastle:bcpkix-jdk15on:1.64' // (Optional) Only required when you want to support Android 4.1.x
    }
```
## SDK Releases
The compiled release binaries can be found here: [Releases][Releases]

## Sample Apps
**All sample apps have moved to GITHub as of 2.1.00 release. Links will redirect to the latest released versions.**

- [Sample-App-MAS-Android-Access-API-Geolocation-And-OTP](https://github.com/CAAPIM/Sample-App-MAS-Android-Access-API-Geolocation-And-OTP)
- [Sample-App-MAS-Android-Fingerprint-Sessions-Lock](https://github.com/CAAPIM/Sample-App-MAS-Android-Fingerprint-Sessions-Lock)
- [Sample-App-MAS-Android-Login-User-Authentication-And-Authorization](https://github.com/CAAPIM/Sample-App-MAS-Android-Login-User-Authentication-And-Authorization)
- [Sample-App-MAS-Android-Secure-Local-Storage](https://github.com/CAAPIM/Sample-App-MAS-Android-Secure-Local-Storage)
- [Sample-App-MAS-Android-MagTraining-BulkUpload](https://github.com/CAAPIM/SampleApp-MagTraining-BulkUpload)
- [Sample-App-MAS-Android-MagTraining-SessionTransfer-BLE](https://github.com/CAAPIM/SampleApp-MagTraining-SessionTransfer-BLE)
- [Sample-App-MAS-Android-MagTraining-UserAuthentication](https://github.com/CAAPIM/SampleApp-MagTraining-UserAuthentication)
- [Sample-App-MAS-Android-MagTraining-UnProtectedLogin](https://github.com/CAAPIM/SampleApp-MagTraining-UnProtectedLogin)
- [Sample-App-MAS-Android-MagTraining-ProtectedAPI](https://github.com/CAAPIM/SampleApp-MagTraining-ProtectedAPI)
- [Sample-App-MAS-Android-MagTraining-Setup](https://github.com/CAAPIM/SampleApp-MagTraining-Setup)
- [Sample-App-MAS-Android-UploadMultipartSampleApp](https://github.com/CAAPIM/UploadMultipartSampleApp)
- [Sample-App-MAS-Android-MagTraining-SessionTransfer-QRCode](https://github.com/CAAPIM/SampleApp-MagTraining-SessionTransfer-QRCode)
- [Sample-App-MAS-Android-MagTraining-InterAppSSO](https://github.com/CAAPIM/SampleApp-MAGTraining-InterAppSSO)

## Communication
- *Have general questions or need help?*, use [Stack Overflow][StackOverflow]. (Tag 'massdk')
- *Find a bug?*, open an [issue][issues] with the steps to reproduce it.
- *Request a feature or have an idea?*, open an [issue][issues].

## How You Can Contribute
Contributions are welcome and much appreciated. To learn more, see the [Contribution Guidelines][contributing].

## Documentation
For more documentation including API references, go to our [main website][docs].

## License
Copyright (c) 2019 Broadcom. All Rights Reserved.
The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.

This software may be modified and distributed under the terms
of the MIT license. See the [LICENSE][license-link] file for details.

[docs]: http://techdocs.broadcom.com/content/broadcom/techdocs/us/en/ca-enterprise-software/layer7-api-management/mobile-sdk-for-ca-mobile-api-gateway/2-2.html
[StackOverflow]: http://stackoverflow.com/questions/tagged/massdk
[issues]: https://github.com/CAAPIM/Android-MAS-SDK/issues
[releases]: ../../releases
[contributing]: /CONTRIBUTING.md
[license-link]: /LICENSE
[documentation]: http://techdocs.broadcom.com/content/broadcom/techdocs/us/en/ca-enterprise-software/layer7-api-management/mobile-sdk-for-ca-mobile-api-gateway/2-2.html
