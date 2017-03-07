# Android Mobile SDK
CA Mobile API Gateway provides enterprises with a secure mobile backend that integrates systems and allows control over which users, devices and applications can access your APIs. The gateway enables developers to leverage standards for API security such as (OAuth2, OpenID Connect, PKI) through client SDKs for Android.

CA Mobile App Services is an extension of CA Mobile API Gateway that provides powerful features through SDKs and APIs for developers to help accelerate the app development process.

For more information about our mobile products see [the website][mas.ca.com].

## Features

* **Secure API Calls** - *Protect APIs with Mutual TLS and control API access on application, user and device level.*
* **Authentication** - *Implement authentication with username/password, Facebook, Google, Twitter sign-in.*
* **Second Factor Auth** - *Secure critical APIs with One Time Password.*
* **Single Sign-On** -* Share user credentials between your apps without coding effort.*
* **Enterprise Browser** - *Extend single sign on session to web applications.*
* **Proximity Login** - *Transfer user session between devices and platforms.*
* **Fingerprint Sessions Lock** - *Support phone unlocking using fingerprint recognition.*
* **Messaging** - *Create collaborative apps with secure, reliable messaging.*
* **User Management** - *Seamlessly integrate your app with an existing enterprise user directory.*
* **Private Cloud Storage** - *Store data in a private cloud and access it from all of your devices.*
* **Pub/Sub** - *Create real-time, IoT-friendly apps using an MQTT-based Pub/Sub infrastructure.*
* **Adhoc Groups** - *Create groups on-the-fly for collaborative apps.*
* **Local Storage** - *Store data on devices with enterprise-grade encryption.*
* **UI Template** - *Provides resources to implement a user login dialog, Social Login, One-Time Password, and Proximity Login (QR code and BLE) saving developers the time of building those UI as well as providing them with a fast way for prototyping apps.*


## Get Started
* Read the "[Getting Started][get-started]" guide or watch some [video tutorials][video].
* Check out our [documentation][documentation] for more details and sample codes


## Communication
* Have general questions or need help? Open an [issue][issues]
* Find a bug? Open an [issue][issues] with the steps to reproduce it.
* Request a feature or have an idea? Open an [issue][issues].

## Installation
Edit your build.gradle file and add below dependency:
```gradle
    dependencies {
        compile 'com.ca:mas:1.3.01'
        compile 'com.ca:masui:1.3.01' //Only requires when using MASUI Template
    }
```

## How You Can Contribute
Contributions are welcome and much appreciated. To learn more, see the [Contribution Guidelines][contributing].

## Documentation
For more documentation and API references, go to our [main website][documentation]

## License
Copyright (c) 2016 CA. All rights reserved.

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
