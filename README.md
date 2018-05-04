# Android™ Mobile SDK
## for CA Mobile API Gateway

CA Mobile API Gateway (MAG) provides enterprises with a secure mobile backend that integrates systems and allows control over which users, devices, and applications can access your APIs. The MAG enables developers to leverage standards for API security such as (OAuth2, OpenID Connect, PKI) through client SDKs for Android.

The CA Mobile API Gateway also provides powerful mobile backend services through SDKs and APIs for developers to help accelerate the app development process.

## Get Started
* Check out our [documentation][documentation] for sample code, video tutorials, and more.  

## Android Mobile SDK Frameworks
The Android Mobile SDK consists of these frameworks:

- **MASConnecta** - Messaging and pub/sub services allowing users to message and send data to each other.
- **MASFoundation** - Core services to handle user authentication, device and app registration, requests and local storage of certificates, keys, and token credentials for accessing protected APIs.
- **MASIdentityManagement** - Identity management services to securely access users and groups from enterprise identity providers.
- **MASStorage** - Storage services for private local and cloud storage.
- **MASUI** - Resources to implement a user login dialog, Social Login, One-Time Password, and Proximity Login (QR code and BLE).

For more information about our mobile products see the [developer website][mas.ca.com].

## Features

* **Secure API Calls** - Protect APIs with Mutual TLS and control API access on application, user and device level.
* **Authentication** - Implement authentication with username/password, Facebook, Google, Twitter sign-in.
* **Second Factor Auth** - Secure critical APIs with One Time Password.
* **Single Sign-On** - Share user credentials between your apps.
* **Enterprise Browser** - Extend the single sign-on session to web applications.
* **Proximity Login** - Transfer the user session between devices and platforms.
* **Fingerprint Sessions Lock** - Support phone unlocking using fingerprint recognition.
* **Messaging** - Create collaborative apps with secure, reliable messaging.
* **User Management** - Seamlessly integrate your app with an existing enterprise user directory.
* **Private Cloud Storage** - Store data in a private cloud and access it from all of your devices.
* **Pub/Sub** - Create real-time, IoT-friendly apps using an MQTT-based Pub/Sub infrastructure.
* **Adhoc Groups** - Create groups on-the-fly for collaborative apps.
* **Local Storage** - Store data on devices with enterprise-grade encryption.
* **UI Template** - Provides resources to implement a user login dialog, Social Login, One-Time Password, and Proximity Login (QR code and BLE), to save time during UI creation and app prototyping.

## Installation
Edit your build.gradle file and add below dependency:
```gradle
    dependencies {
        implementation 'com.ca:mas-foundation:1.6.10'

        implementation 'com.ca:mas-connecta:1.6.10' // (Optional) Only required when using mas connecta
        implementation 'com.ca:mas-storage:1.6.10' // (Optional) Only required when using mas storage
        implementation 'com.ca:mas-identity-management:1.6.10' // (Optional) Only required when using mas identity management
        implementation 'com.ca:masui:1.6.10' // (Optional) Only required when using MASUI Template. The MAS UI library provides sample user interfaces for Login, OTP, Social Login, and Enterprise Browser.

        implementation 'org.bouncycastle:bcpkix-jdk15on:1.55' // (Optional) Only required when you want to support Android 4.1.x
    }
```
## SDK Releases
The compiled release binaries can be found here: [Releases][Releases]

## Communication
* Have general questions, need help, or have an idea and want to request a feature? Open an [issue][issues].
* Find a bug? Open an [issue][issues] with the steps to reproduce it.

## How You Can Contribute
Contributions are welcome and much appreciated. To learn more, see the [Contribution Guidelines][contributing].

## Documentation
For more documentation including API references, go to our main website: [mas.ca.com]: [http://mas.ca.com/]

## License
Copyright (c) 2017 CA. All rights reserved.

This software may be modified and distributed under the terms
of the MIT license. See the [LICENSE][license-link] file for details.

[mag]: https://docops.ca.com/mag
[mas.ca.com]: http://mas.ca.com/
[docs]: http://mas.ca.com/docs/
[blog]: http://mas.ca.com/blog/
[get-started]: http://mas.ca.com/get-started

[issues]: https://github.com/CAAPIM/Android-MAS-SDK/issues
[releases]: ../../releases
[contributing]: /CONTRIBUTING.md
[license-link]: /LICENSE
[video]: https://www.ca.com/us/developers/mas/videos.html
[documentation]: https://www.ca.com/us/developers/mas/docs.html
